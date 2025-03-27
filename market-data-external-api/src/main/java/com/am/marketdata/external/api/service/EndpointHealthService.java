package com.am.marketdata.external.api.service;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.am.marketdata.external.api.client.TradeBrainClient;
import com.am.marketdata.external.api.model.ApiResponse;
import com.am.marketdata.external.api.registry.ApiEndpoint;
import com.am.marketdata.external.api.registry.ApiEndpointRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for checking health status of API endpoints
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EndpointHealthService {
    
    private final TradeBrainClient tradeBrainClient;
    private final ApiEndpointRegistry endpointRegistry;
    
    @Value("${external.api.health-check.timeout-ms:4000}")
    private long healthCheckTimeoutMs;
    
    @Value("${external.api.health-check.default-symbol:RELIANCE}")
    private String defaultSymbol;
    
    /**
     * Check health status of all registered endpoints with a delay between checks
     * 
     * @param delayMs Delay in milliseconds between endpoint checks
     * @return Health status of all endpoints
     */
    public Map<String, Object> checkEndpointsHealth(long delayMs) {
        log.debug("Checking health status of all endpoints with delay: {}ms, timeout: {}ms, and default symbol: {}", 
                delayMs, healthCheckTimeoutMs, defaultSymbol);
        
        Collection<ApiEndpoint> endpoints = endpointRegistry.getAllEndpoints();
        Map<String, Object> healthStatus = new HashMap<>();
        
        for (ApiEndpoint endpoint : endpoints) {
            if (endpoint.isHealthCheckEnabled()) {
                try {
                    // Add delay between endpoint checks
                    if (delayMs > 0) {
                        Thread.sleep(delayMs);
                    }
                    
                    // Check if the endpoint URL contains a symbol placeholder
                    ApiResponse response;
                    if (endpoint.getUrl().contains("{symbol}")) {
                        log.debug("Endpoint {} contains symbol placeholder, using default symbol: {}", 
                                endpoint.getId(), defaultSymbol);
                        response = callEndpointWithSymbolAndTimeout(endpoint.getId(), defaultSymbol, healthCheckTimeoutMs);
                    } else {
                        response = callEndpointWithTimeout(endpoint.getId(), healthCheckTimeoutMs);
                    }
                    
                    Map<String, Object> endpointStatus = new HashMap<>();
                    endpointStatus.put("id", endpoint.getId());
                    endpointStatus.put("name", endpoint.getName());
                    endpointStatus.put("url", endpoint.getUrl());
                    endpointStatus.put("status", response.isSuccess() ? "UP" : "DOWN");
                    endpointStatus.put("response_time_ms", response.getResponseTimeMs());
                    endpointStatus.put("last_checked", Instant.now().toString());
                    
                    if (!response.isSuccess()) {
                        endpointStatus.put("error", response.getErrorMessage());
                    }
                    
                    healthStatus.put(endpoint.getId(), endpointStatus);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while checking endpoint health: {}", endpoint.getId());
                    healthStatus.put(endpoint.getId(), createErrorStatus(endpoint, "INTERRUPTED", 
                            "Health check was interrupted"));
                }
            }
        }
        
        return healthStatus;
    }
    
    /**
     * Call an endpoint with a timeout
     * 
     * @param endpointId Endpoint ID
     * @param timeoutMs Timeout in milliseconds
     * @return ApiResponse from the endpoint or timeout error response
     */
    private ApiResponse callEndpointWithTimeout(String endpointId, long timeoutMs) {
        CompletableFuture<ApiResponse> future = CompletableFuture.supplyAsync(() -> 
            tradeBrainClient.checkEndpointHealth(endpointId)
        );
        
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("Timeout after {}ms while checking endpoint health: {}", timeoutMs, endpointId);
            return ApiResponse.error(
                    HttpStatus.REQUEST_TIMEOUT.value(),
                    "Request timed out after " + timeoutMs + "ms",
                    timeoutMs
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while checking endpoint health: {}", endpointId);
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Health check was interrupted",
                    0
            );
        } catch (ExecutionException e) {
            log.error("Error while checking endpoint health: {}", endpointId, e);
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error during health check: " + e.getMessage(),
                    0
            );
        }
    }
    
    /**
     * Call an endpoint with a symbol parameter and timeout
     * 
     * @param endpointId Endpoint ID
     * @param symbol Symbol to use for the endpoint
     * @param timeoutMs Timeout in milliseconds
     * @return ApiResponse from the endpoint or timeout error response
     */
    private ApiResponse callEndpointWithSymbolAndTimeout(String endpointId, String symbol, long timeoutMs) {
        CompletableFuture<ApiResponse> future = CompletableFuture.supplyAsync(() -> {
            try {
                return tradeBrainClient.checkEndpointHealthWithSymbol(endpointId, symbol);
            } catch (Exception e) {
                log.error("Error calling endpoint with symbol: {}, {}", endpointId, symbol, e);
                return ApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error calling endpoint with symbol: " + e.getMessage(),
                        0
                );
            }
        });
        
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("Timeout after {}ms while checking endpoint health with symbol {}: {}", 
                    timeoutMs, symbol, endpointId);
            return ApiResponse.error(
                    HttpStatus.REQUEST_TIMEOUT.value(),
                    "Request timed out after " + timeoutMs + "ms",
                    timeoutMs
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while checking endpoint health with symbol {}: {}", symbol, endpointId);
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Health check was interrupted",
                    0
            );
        } catch (ExecutionException e) {
            log.error("Error while checking endpoint health with symbol {}: {}", symbol, endpointId, e);
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error during health check: " + e.getMessage(),
                    0
            );
        }
    }
    
    /**
     * Create error status map for an endpoint
     * 
     * @param endpoint The endpoint being checked
     * @param status Error status code
     * @param errorMessage Error message
     * @return Error status map
     */
    private Map<String, Object> createErrorStatus(ApiEndpoint endpoint, String status, String errorMessage) {
        Map<String, Object> errorStatus = new HashMap<>();
        errorStatus.put("id", endpoint.getId());
        errorStatus.put("name", endpoint.getName());
        errorStatus.put("url", endpoint.getUrl());
        errorStatus.put("status", status);
        errorStatus.put("last_checked", Instant.now().toString());
        errorStatus.put("error", errorMessage);
        return errorStatus;
    }
}
