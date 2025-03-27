package com.am.marketdata.external.api.client;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.am.marketdata.external.api.model.ApiResponse;
import com.am.marketdata.external.api.registry.ApiEndpoint;
import com.am.marketdata.external.api.registry.ApiEndpointRegistry;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic client for making API calls using registered endpoints
 */
@Component
@Slf4j
public class GenericApiClient {
    
    private final ApiClient apiClient;
    private final ApiEndpointRegistry endpointRegistry;
    
    /**
     * Constructor with dependencies
     * 
     * @param apiClient Client for making API requests
     * @param endpointRegistry Registry for API endpoints
     */
    public GenericApiClient(ApiClient apiClient, ApiEndpointRegistry endpointRegistry) {
        this.apiClient = apiClient;
        this.endpointRegistry = endpointRegistry;
        log.info("GenericApiClient initialized");
    }
    
    /**
     * Make a GET request to a registered endpoint
     * 
     * @param endpointId ID of the registered endpoint
     * @return API response
     */
    @CircuitBreaker(name = "defaultCircuitBreaker", fallbackMethod = "getApiFallback")
    @Retry(name = "defaultRetry")
    public ApiResponse get(String endpointId) {
        return get(endpointId, null);
    }
    
    /**
     * Make a GET request to a registered endpoint with additional headers
     * 
     * @param endpointId ID of the registered endpoint
     * @param additionalHeaders Additional headers to include in the request
     * @return API response
     */
    @CircuitBreaker(name = "defaultCircuitBreaker", fallbackMethod = "getApiFallback")
    @Retry(name = "defaultRetry")
    public ApiResponse get(String endpointId, Map<String, String> additionalHeaders) {
        ApiEndpoint endpoint = getEndpointOrThrow(endpointId);
        
        // Use endpoint-specific circuit breaker and retry if configured
        String circuitBreakerName = endpoint.getCircuitBreakerConfigName() != null ? 
                endpoint.getCircuitBreakerConfigName() : "defaultCircuitBreaker";
        String retryName = endpoint.getRetryConfigName() != null ? 
                endpoint.getRetryConfigName() : "defaultRetry";
        
        return executeWithResilience(endpoint, additionalHeaders, circuitBreakerName, retryName);
    }
    
    /**
     * Make a POST request to a registered endpoint
     * 
     * @param endpointId ID of the registered endpoint
     * @param body Request body
     * @return API response
     */
    @CircuitBreaker(name = "defaultCircuitBreaker", fallbackMethod = "postApiFallback")
    @Retry(name = "defaultRetry")
    public ApiResponse post(String endpointId, Object body) {
        return post(endpointId, body, null);
    }
    
    /**
     * Make a POST request to a registered endpoint with additional headers
     * 
     * @param endpointId ID of the registered endpoint
     * @param body Request body
     * @param additionalHeaders Additional headers to include in the request
     * @return API response
     */
    @CircuitBreaker(name = "defaultCircuitBreaker", fallbackMethod = "postApiFallback")
    @Retry(name = "defaultRetry")
    public ApiResponse post(String endpointId, Object body, Map<String, String> additionalHeaders) {
        ApiEndpoint endpoint = getEndpointOrThrow(endpointId);
        Map<String, String> headers = endpoint.getMergedHeaders(additionalHeaders);
        
        log.debug("Making POST request to endpoint {}: {}", endpointId, endpoint.getFullUrl());
        return apiClient.post(endpoint.getFullUrl(), body, headers);
    }
    
    /**
     * Check if an endpoint exists
     * 
     * @param endpointId ID of the registered endpoint
     * @return true if exists, false otherwise
     */
    public boolean hasEndpoint(String endpointId) {
        return endpointRegistry.hasEndpoint(endpointId);
    }
    
    /**
     * Execute a GET request with resilience patterns
     * 
     * @param endpoint Endpoint to call
     * @param additionalHeaders Additional headers to include
     * @param circuitBreakerName Circuit breaker configuration name
     * @param retryName Retry configuration name
     * @return API response
     */
    private ApiResponse executeWithResilience(
            ApiEndpoint endpoint, 
            Map<String, String> additionalHeaders,
            String circuitBreakerName,
            String retryName) {
        
        Map<String, String> headers = endpoint.getMergedHeaders(additionalHeaders);
        
        log.debug("Making GET request to endpoint {}: {}", endpoint.getId(), endpoint.getFullUrl());
        return apiClient.get(endpoint.getFullUrl(), headers);
    }
    
    /**
     * Fallback method for GET requests
     * 
     * @param endpointId Endpoint ID
     * @param additionalHeaders Additional headers
     * @param e Exception that triggered the fallback
     * @return Fallback API response
     */
    public ApiResponse getApiFallback(String endpointId, Map<String, String> additionalHeaders, Exception e) {
        log.error("Circuit breaker triggered for GET request to endpoint {}: {}", endpointId, e.getMessage());
        return ApiResponse.failure(503, "Service temporarily unavailable: " + e.getMessage(), 0);
    }
    
    /**
     * Fallback method for POST requests
     * 
     * @param endpointId Endpoint ID
     * @param body Request body
     * @param additionalHeaders Additional headers
     * @param e Exception that triggered the fallback
     * @return Fallback API response
     */
    public ApiResponse postApiFallback(String endpointId, Object body, Map<String, String> additionalHeaders, Exception e) {
        log.error("Circuit breaker triggered for POST request to endpoint {}: {}", endpointId, e.getMessage());
        return ApiResponse.failure(503, "Service temporarily unavailable: " + e.getMessage(), 0);
    }
    
    /**
     * Get an endpoint by ID or throw an exception if not found
     * 
     * @param endpointId Endpoint ID
     * @return The endpoint
     * @throws IllegalArgumentException if the endpoint is not found
     */
    private ApiEndpoint getEndpointOrThrow(String endpointId) {
        ApiEndpoint endpoint = endpointRegistry.getEndpoint(endpointId);
        if (endpoint == null) {
            throw new IllegalArgumentException("Unknown endpoint ID: " + endpointId);
        }
        return endpoint;
    }
}
