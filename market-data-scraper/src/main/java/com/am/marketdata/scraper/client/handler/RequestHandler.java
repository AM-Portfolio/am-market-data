package com.am.marketdata.scraper.client.handler;

import org.springframework.http.ResponseEntity;

/**
 * Interface for handling API requests
 * Implements Strategy Pattern for different request types
 * 
 * @param <T> The response type
 */
public interface RequestHandler<T> {
    
    /**
     * Get the endpoint path for this request
     * 
     * @return The endpoint path
     */
    String getEndpoint();
    
    /**
     * Get the response type class
     * 
     * @return The response type class
     */
    Class<T> getResponseType();
    
    /**
     * Process the response before returning
     * 
     * @param response The response from the API
     * @return The processed response
     */
    T processResponse(ResponseEntity<T> response);
    
    /**
     * Log the response details
     * 
     * @param response The response to log
     */
    void logResponse(T response);
    
    /**
     * Get the name of this endpoint for metrics
     * 
     * @return The endpoint name for metrics
     */
    String getMetricName();
}
