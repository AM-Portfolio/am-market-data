package com.am.marketdata.scraper.client;

import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.common.model.NseETFResponse;
import com.am.marketdata.scraper.service.CookieCacheService;
import com.am.marketdata.scraper.exception.NSEApiException;
import com.am.marketdata.scraper.exception.CookieException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class NSEApiClient {
    @Qualifier("nseApiRestTemplate")
    private final RestTemplate restTemplate;
    private final CookieCacheService cookieCacheService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Value("${nse.api.base-url:https://www.nseindia.com}")
    private String baseUrl;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36";

    // Metric names
    private static final String METRIC_PREFIX = "nse.api.";
    private static final String METRIC_REQUEST_TIME = METRIC_PREFIX + "request.time";
    private static final String METRIC_ERROR_COUNT = METRIC_PREFIX + "error.count";
    private static final String METRIC_REQUEST_COUNT = METRIC_PREFIX + "request.count";
    private static final String TAG_ENDPOINT = "endpoint";
    private static final String TAG_ERROR_TYPE = "error_type";

    private Timer etfRequestTimer;
    private Timer indicesRequestTimer;

    @PostConstruct
    public void initialize() {
        // Initialize timers for each endpoint
        etfRequestTimer = Timer.builder(METRIC_REQUEST_TIME)
            .tag(TAG_ENDPOINT, "etf")
            .description("Time taken for ETF API requests")
            .register(meterRegistry);

        indicesRequestTimer = Timer.builder(METRIC_REQUEST_TIME)
            .tag(TAG_ENDPOINT, "indices")
            .description("Time taken for indices API requests")
            .register(meterRegistry);
    }

    public NseETFResponse getETFs() {
        return etfRequestTimer.record(() -> executeApiCall("/api/etf", NseETFResponse.class, this::logETFResponse));
    }

    public NSEIndicesResponse getAllIndices() {
        return indicesRequestTimer.record(() -> executeApiCall("/api/allIndices", NSEIndicesResponse.class, this::logIndicesResponse));
    }

    public HttpHeaders fetchCookies() {
        try {
            log.info("Fetching fresh cookies from NSE API");
            HttpEntity<String> entity = new HttpEntity<>(createBasicHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl, HttpMethod.GET, entity, String.class);
            
            HttpHeaders headers = response.getHeaders();
            if (headers != null && headers.containsKey(HttpHeaders.SET_COOKIE)) {
                log.info("Successfully fetched cookies: {}", 
                    maskCookieValues(String.join("; ", headers.get(HttpHeaders.SET_COOKIE))));
            } else {
                log.warn("No cookies found in response headers");
            }
            return headers;
        } catch (Exception e) {
            log.error("Failed to fetch cookies from NSE API: {}", e.getMessage(), e);
            throw new CookieException("Failed to fetch cookies from NSE API: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createBasicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private <T> T executeApiCall(String endpoint, Class<T> responseType, ResponseLogger<T> responseLogger) {
        String cookies = getCookiesOrThrow();
        String url = baseUrl + endpoint;
        HttpEntity<String> entity = createHttpEntity(cookies);

        // Record request count
        meterRegistry.counter(METRIC_REQUEST_COUNT, TAG_ENDPOINT, endpoint).increment();

        try {
            log.info("Calling NSE API - Endpoint: {}, Cookies: {}, Headers: {}", 
                endpoint, 
                maskCookieValues(cookies),
                maskSensitiveHeaders(entity.getHeaders()));
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            
            if (response.getBody() == null) {
                recordError(endpoint, "empty_response");
                throw new NSEApiException(endpoint, response.getStatusCode(), "null", "Empty response from NSE API");
            }

            logApiResponse(endpoint, response, responseLogger);
            return response.getBody();

        } catch (HttpClientErrorException.Unauthorized e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("Unauthorized access to NSE API - Endpoint: {}, Response: {}, Headers: {}", 
                endpoint, responseBody, maskSensitiveHeaders(e.getResponseHeaders()));
            cookieCacheService.invalidateCookies();
            recordError(endpoint, "unauthorized");
            throw new NSEApiException(endpoint, HttpStatus.UNAUTHORIZED, responseBody, "Unauthorized access, cookies might be expired", e);
        
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("Client error from NSE API - Endpoint: {}, Status: {}, Response: {}, Headers: {}", 
                endpoint, e.getStatusCode(), responseBody, maskSensitiveHeaders(e.getResponseHeaders()));
            recordError(endpoint, "client_error");
            throw new NSEApiException(endpoint, e.getStatusCode(), responseBody, "Client error from NSE API", e);
        
        } catch (HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("Server error from NSE API - Endpoint: {}, Status: {}, Response: {}, Headers: {}", 
                endpoint, e.getStatusCode(), responseBody, maskSensitiveHeaders(e.getResponseHeaders()));
            recordError(endpoint, "server_error");
            throw new NSEApiException(endpoint, e.getStatusCode(), responseBody, "Server error from NSE API", e);
        
        } catch (ResourceAccessException e) {
            log.error("Network error calling NSE API - Endpoint: {}, Error: {}", endpoint, e.getMessage());
            recordError(endpoint, "network_error");
            throw new NSEApiException(endpoint, HttpStatus.SERVICE_UNAVAILABLE, "N/A", "Network error accessing NSE API", e);
        
        } catch (Exception e) {
            log.error("Unexpected error calling NSE API - Endpoint: {}, Error: {}", endpoint, e.getMessage(), e);
            recordError(endpoint, "unexpected_error");
            throw new NSEApiException(endpoint, HttpStatus.INTERNAL_SERVER_ERROR, "N/A", "Unexpected error calling NSE API", e);
        }
    }

    private void recordError(String endpoint, String errorType) {
        meterRegistry.counter(METRIC_ERROR_COUNT,
            TAG_ENDPOINT, endpoint,
            TAG_ERROR_TYPE, errorType
        ).increment();
    }

    private String getCookiesOrThrow() {
        String cookies = cookieCacheService.getCookies();
        if (cookies == null) {
            throw new CookieException("No valid cookies found in cache");
        }
        return cookies;
    }

    private HttpEntity<String> createHttpEntity(String cookies) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookies);
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(headers);
    }

    private HttpHeaders maskSensitiveHeaders(HttpHeaders headers) {
        if (headers == null) return null;
        HttpHeaders masked = new HttpHeaders();
        headers.forEach((key, value) -> {
            if (HttpHeaders.COOKIE.equalsIgnoreCase(key)) {
                masked.put(key, Collections.singletonList("*****"));
            } else {
                masked.put(key, value);
            }
        });
        return masked;
    }

    private String maskCookieValues(String cookies) {
        if (cookies == null) return null;
        // Split cookies and mask values while preserving names
        return Stream.of(cookies.split(";"))
            .map(cookie -> {
                String[] parts = cookie.split("=", 2);
                return parts.length > 1 
                    ? parts[0].trim() + "=*****" 
                    : cookie.trim() + "=*****";
            })
            .collect(Collectors.joining("; "));
    }

    private <T> void logApiResponse(String endpoint, ResponseEntity<T> response, ResponseLogger<T> responseLogger) {
        try {
            log.info("NSE API Response - Endpoint: {}, Status: {}, Headers: {}", 
                endpoint, response.getStatusCode(), maskSensitiveHeaders(response.getHeaders()));
            
            if (response.getBody() != null) {
                responseLogger.log(response.getBody());
            }
        } catch (Exception e) {
            log.warn("Failed to log API response details - Endpoint: {}", endpoint, e);
        }
    }

    @FunctionalInterface
    private interface ResponseLogger<T> {
        void log(T response) throws Exception;
    }

    private void logETFResponse(NseETFResponse etfs) throws Exception {
        log.info("ETF Response - Raw: {}", objectMapper.writeValueAsString(etfs));
        if (etfs.getData() != null) {
            log.info("ETF Summary - Count: {}, First ETF: {}", 
                etfs.getData().size(),
                etfs.getData().isEmpty() ? "none" : 
                    String.format("%s (Last: %.2f, Change: %.2f%%)", 
                        etfs.getData().get(0).getSymbol(),
                        etfs.getData().get(0).getLastTradedPrice(),
                        etfs.getData().get(0).getPercentChange()
                    )
            );
        }
    }

    private void logIndicesResponse(NSEIndicesResponse indices) throws Exception {
        log.info("Indices Response - Raw: {}", objectMapper.writeValueAsString(indices));
        if (indices.getData() != null) {
            log.info("Indices Summary - Count: {}, First Index: {}", 
                indices.getData().size(),
                indices.getData().isEmpty() ? "none" : 
                    String.format("%s (Last: %.2f, Change: %.2f%%)", 
                        indices.getData().get(0).getIndexSymbol(),
                        indices.getData().get(0).getLast(),
                        indices.getData().get(0).getPercentChange()
                    )
            );
        }
    }
}
