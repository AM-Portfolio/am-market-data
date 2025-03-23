package com.am.marketdata.scraper.client.executor;

import com.am.marketdata.scraper.client.handler.RequestHandler;
import com.am.marketdata.scraper.exception.NSEApiException;
import com.am.marketdata.scraper.service.cookie.CookieCacheService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Executor for API requests
 * Implements Template Method Pattern for standardized request execution
 */
@Slf4j
@Component
public class RequestExecutor {
    
    private static final String METRIC_PREFIX = "nse.api.";
    private static final String METRIC_REQUEST_TIME = METRIC_PREFIX + "request.time";
    private static final String METRIC_ERROR_COUNT = METRIC_PREFIX + "error.count";
    private static final String METRIC_REQUEST_COUNT = METRIC_PREFIX + "request.count";
    private static final String TAG_ENDPOINT = "endpoint";
    private static final String TAG_ERROR_TYPE = "error_type";
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int READ_TIMEOUT_SECONDS = 10;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final RestTemplate restTemplate;
    private final MeterRegistry meterRegistry;
    private final CookieCacheService cookieCacheService;
    private final String baseUrl;
    private final HttpHeaders baseHeaders;


    public RequestExecutor(RestTemplate restTemplate, MeterRegistry meterRegistry, CookieCacheService cookieCacheService, String baseUrl, HttpHeaders baseHeaders) {
        this.restTemplate = restTemplate;
        this.meterRegistry = meterRegistry;
        this.cookieCacheService = cookieCacheService;
        this.baseUrl = baseUrl;
        this.baseHeaders = baseHeaders;
        
        // Configure connection pooling
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_SECONDS * 1000);
        requestFactory.setReadTimeout(READ_TIMEOUT_SECONDS * 1000);
        requestFactory.setBufferRequestBody(false);
        restTemplate.setRequestFactory(requestFactory);
    }

    private void initializeRequest(String endpoint) {
        // Record request attempt
        meterRegistry.counter(METRIC_REQUEST_COUNT, TAG_ENDPOINT, endpoint).increment();
    }

    private String getValidCookies(String endpoint) throws NSEApiException {
        String cookies = cookieCacheService.getCookies();
        if (cookies == null) {
            throw new NSEApiException(
                endpoint,
                HttpStatus.valueOf(401),
                "",
                "No valid cookies found in cache",
                null
            );
        }
        return cookies;
    }

    private HttpHeaders createRequestHeaders(String cookies) {
        HttpHeaders headers = new HttpHeaders(baseHeaders);
        headers.set(HttpHeaders.COOKIE, cookies);
        return headers;
    }

    private HttpEntity<?> buildRequest(Object body, HttpHeaders headers) {
        return new HttpEntity<>(body, headers);
    }

    private void logCurlCommand(String endpoint, HttpMethod method, String cookies, HttpHeaders headers, Object body) {
        StringBuilder curlCommand = new StringBuilder("curl -X ").append(method.name())
            .append(" -H 'Cookie: " + cookies + "'\n")
            .append(headers.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(HttpHeaders.COOKIE))
                .map(entry -> "-H '" + entry.getKey() + ": " + entry.getValue() + "'\n")
                .collect(Collectors.joining()))
            .append("'" + baseUrl + endpoint + "'\n");
        
        log.debug("API request CURL command:\n{}", curlCommand.toString());
        log.info("API request details: method={}, endpoint={}, cookies={}, headers={}, body={}", 
            method.name(), endpoint, cookies, headers, body);
        log.info("API request CURL command with request body:\n{} -d '{}'", curlCommand.toString(), body);
        log.info("API request headers: {}", headers);
        log.info("API request body: {}", body);
        log.info("API request URL: {}", baseUrl + endpoint);
    }

    private void recordRequestTime(String endpoint, Timer.Sample sample) {
        sample.stop(meterRegistry.timer(METRIC_REQUEST_TIME, TAG_ENDPOINT, endpoint));
    }

    private ResponseEntity<String> executeRequest(String endpoint, HttpMethod method, HttpEntity<?> request) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + endpoint,
                method,
                request,
                String.class
            );
            log.info("API request executed successfully. Response status code: {}", response.getStatusCode());
            log.info("API response headers: {}", response.getHeaders());
            log.info("API response body: {}", response.getBody());
            return response;
        } catch (Exception e) {
            log.error("API request failed. Error: {}", e.getMessage());
            throw e;
        }
    }

    private void recordError(String endpoint, Exception e, RequestHandler handler) {
        meterRegistry.counter(METRIC_ERROR_COUNT, 
            TAG_ENDPOINT, endpoint,
            TAG_ERROR_TYPE, handler.getErrorType(e)
        ).increment();
    }

    private void logErrorDetails(String endpoint, Exception e, int attempt) {
        log.warn("API request failed (attempt {}/{}) for endpoint {}: {}", 
            attempt, MAX_RETRIES, endpoint, e.getMessage());
    }

    private void logFailedCurlCommand(String endpoint, HttpMethod method, String cookies, Object body) {
        StringBuilder failedCurlCommand = new StringBuilder("curl -X ").append(method.name())
            .append(" -H 'Cookie: " + cookies + "'\n")
            .append(baseHeaders.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(HttpHeaders.COOKIE))
                .map(entry -> "-H '" + entry.getKey() + ": " + entry.getValue() + "'\n")
                .collect(Collectors.joining()))
            .append("-d '" + body + "'\n")
            .append("'" + baseUrl + endpoint + "'\n");
        log.error("Failed API request CURL command with request body:\n{}", failedCurlCommand.toString());
    }

    private boolean shouldRetry(Exception e) {
        return e instanceof java.net.SocketTimeoutException || 
               e instanceof java.net.SocketException;
    }

    private void handleRetry(String endpoint, int attempt) throws InterruptedException {
        if (attempt < MAX_RETRIES) {
            log.info("Retrying in {}ms...", RETRY_DELAY_MS);
            Thread.sleep(RETRY_DELAY_MS);
        }
    }

    private void throwFinalException(String endpoint, Exception e, RequestHandler handler) throws NSEApiException {
        log.error("Final exception for endpoint {}: {}", endpoint, e.getMessage());
        throw new NSEApiException(
            endpoint,
            handler.getHttpStatus(e),
            handler.getResponseBody(e),
            handler.getErrorMessage(e),
            e
        );
    }

    public <T> T execute(RequestHandler<T> handler) throws NSEApiException {
        String endpoint = handler.getEndpoint();
        
        initializeRequest(endpoint);
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String cookies = getValidCookies(endpoint);
                HttpHeaders headers = createRequestHeaders(cookies);
                HttpEntity<?> request = buildRequest(handler.getBody(), headers);
                
                //logCurlCommand(endpoint, handler.getMethod(), cookies, headers, handler.getBody());
                
                ResponseEntity<String> response = executeRequest(endpoint, handler.getMethod(), request);
                return handler.processResponse(response.getBody());
                
            } catch (Exception e) {
                lastException = e;
                
                // Record and log error
                //recordError(endpoint, e, handler);
                logErrorDetails(endpoint, e, attempt);
                //logFailedCurlCommand(endpoint, handler.getMethod(), cookieCacheService.getCookies(), handler.getBody());
                
                // Check if we should retry
                if (shouldRetry(e)) {
                    try {
                        handleRetry(endpoint, attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new NSEApiException(
                            endpoint,
                            HttpStatus.valueOf(500),
                            "",
                            "Request interrupted",
                            ie
                        );
                    }
                } else {
                    // If not retrying, throw the exception
                    throwFinalException(endpoint, e, handler);
                }
            }
        }
        
        // If we reached here, all retries failed
        throw new NSEApiException(
            endpoint,
            HttpStatus.valueOf(500),
            "",
            "All retries failed",
            lastException
        );
    }
}
