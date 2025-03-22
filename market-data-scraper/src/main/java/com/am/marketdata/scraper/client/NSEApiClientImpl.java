package com.am.marketdata.scraper.client;

import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.NseETFResponse;
import com.am.marketdata.scraper.client.api.NSEApi;
import com.am.marketdata.scraper.client.executor.RequestExecutor;
import com.am.marketdata.scraper.client.handler.ETFRequestHandler;
import com.am.marketdata.scraper.client.handler.IndicesRequestHandler;
import com.am.marketdata.scraper.client.handler.StockIndicesRequestHandler;
import com.am.marketdata.scraper.exception.NSEApiException;
import com.am.marketdata.scraper.service.cookie.CookieCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of NSE API client
 * Uses Strategy Pattern for different request types and Template Method Pattern for request execution
 */
@Slf4j
@Component
public class NSEApiClientImpl implements NSEApi {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36";
    private static final String METRIC_PREFIX = "nse.api.";
    private static final String METRIC_ERROR_COUNT = METRIC_PREFIX + "error.count";
    private static final String TAG_ENDPOINT = "endpoint";
    private static final String TAG_ERROR_TYPE = "error_type";
    private static final String COOKIE_ENDPOINT = "cookie";

    @Qualifier("nseApiRestTemplate")
    private final RestTemplate restTemplate;
    private final CookieCacheService cookieCacheService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    
    // Request handlers
    private final ETFRequestHandler etfRequestHandler;
    private final IndicesRequestHandler indicesRequestHandler;
    private final StockIndicesRequestHandler stockIndicesRequestHandler;
    
    @Value("${nse.api.base-url:https://www.nseindia.com}")
    private String baseUrl;

    /**
     * Create a new NSE API client
     */
    public NSEApiClientImpl(
            @Qualifier("nseApiRestTemplate") RestTemplate restTemplate,
            CookieCacheService cookieCacheService,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry,
            ETFRequestHandler etfRequestHandler,
            IndicesRequestHandler indicesRequestHandler,
            StockIndicesRequestHandler stockIndicesRequestHandler) {
        this.restTemplate = restTemplate;
        this.cookieCacheService = cookieCacheService;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.etfRequestHandler = etfRequestHandler;
        this.indicesRequestHandler = indicesRequestHandler;
        this.stockIndicesRequestHandler = stockIndicesRequestHandler;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing NSE API Client with base URL: {}", baseUrl);
    }

    @Override
    public NseETFResponse getETFs() {
        return createExecutor().execute(etfRequestHandler);
    }

    @Override
    public NSEStockInsidicesData getStockbyInsidices(String indexSymbol) {
        return createExecutor().execute(
            stockIndicesRequestHandler.withIndexSymbol(indexSymbol)
        );
    }

    @Override
    public NSEIndicesResponse getAllIndices() {
        return createExecutor().execute(indicesRequestHandler);
    }

    @Override
    public HttpHeaders fetchCookies() {
        try {
            log.info("Fetching cookies from NSE homepage");
            HttpEntity<String> entity = new HttpEntity<>(createBasicHeaders());
            
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            log.info("Received response from NSE homepage - Status: {}", response.getStatusCode());
            return response.getHeaders();
            
        } catch (HttpClientErrorException e) {
            recordError(COOKIE_ENDPOINT, "client_error");
            log.error("Client error fetching cookies - Status: {}, Body: {}", 
                e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new NSEApiException("/", e.getStatusCode(), e.getResponseBodyAsString(), 
                "Client error fetching cookies", e);
                
        } catch (HttpServerErrorException e) {
            recordError(COOKIE_ENDPOINT, "server_error");
            log.error("Server error fetching cookies - Status: {}, Body: {}", 
                e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new NSEApiException("/", e.getStatusCode(), e.getResponseBodyAsString(), 
                "Server error fetching cookies", e);
                
        } catch (ResourceAccessException e) {
            recordError(COOKIE_ENDPOINT, "network_error");
            log.error("Network error fetching cookies: {}", e.getMessage(), e);
            throw new NSEApiException("/", HttpStatus.SERVICE_UNAVAILABLE, null, 
                "Network error fetching cookies", e);
                
        } catch (Exception e) {
            recordError(COOKIE_ENDPOINT, "unexpected_error");
            log.error("Unexpected error fetching cookies: {}", e.getMessage(), e);
            throw new NSEApiException("/", HttpStatus.INTERNAL_SERVER_ERROR, null, 
                "Unexpected error fetching cookies", e);
        }
    }

    /**
     * Create a request executor with current cookies
     * 
     * @return A configured request executor
     */
    private RequestExecutor createExecutor() {
        String cookies = getCookiesOrThrow();
        HttpHeaders headers = createHttpEntity(cookies).getHeaders();
        return new RequestExecutor(restTemplate, meterRegistry, baseUrl, headers);
    }

    /**
     * Record an error in metrics
     * 
     * @param endpoint The endpoint name
     * @param errorType The error type
     */
    private void recordError(String endpoint, String errorType) {
        meterRegistry.counter(METRIC_ERROR_COUNT,
            TAG_ENDPOINT, endpoint,
            TAG_ERROR_TYPE, errorType
        ).increment();
    }

    /**
     * Get cookies from cache or throw exception
     * 
     * @return The cookies string
     * @throws NSEApiException if no cookies are available
     */
    private String getCookiesOrThrow() {
        String cookies = cookieCacheService.getCookies();
        if (cookies == null || cookies.isEmpty()) {
            log.error("No cookies available in cache");
            throw new NSEApiException("/", HttpStatus.UNAUTHORIZED, null, 
                "No cookies available for NSE API request");
        }
        log.debug("Using cookies for request: {}", maskCookieValues(cookies));
        return cookies;
    }

    /**
     * Create basic headers for initial requests
     * 
     * @return HTTP headers
     */
    private HttpHeaders createBasicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    /**
     * Create an HTTP entity with cookies
     * 
     * @param cookies The cookies string
     * @return HTTP entity with headers
     */
    private HttpEntity<String> createHttpEntity(String cookies) {
        HttpHeaders headers = createBasicHeaders();
        headers.set(HttpHeaders.COOKIE, cookies);
        return new HttpEntity<>(headers);
    }

    /**
     * Mask cookie values for logging
     * 
     * @param cookies The cookies string
     * @return Masked cookies string
     */
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
}
