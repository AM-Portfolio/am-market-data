package com.am.marketdata.scraper.client.handler;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
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
     * Get the HTTP method for this request
     * 
     * @return The HTTP method
     */
    HttpMethod getMethod();
    
    /**
     * Get the request body
     * 
     * @return The request body
     */
    Object getBody();
    
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
    T processResponse(String response);
    
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
    
    /**
     * Get the error type for a given exception
     * 
     * @param e The exception
     * @return The error type
     */
    String getErrorType(Exception e);
    
    /**
     * Get the HTTP status code for a given exception
     * 
     * @param e The exception
     * @return The HTTP status code
     */
    HttpStatusCode getHttpStatus(Exception e);
    
    /**
     * Get the response body for a given exception
     * 
     * @param e The exception
     * @return The response body
     */
    String getResponseBody(Exception e);
    
    /**
     * Get the error message for a given exception
     * 
     * @param e The exception
     * @return The error message
     */
    String getErrorMessage(Exception e);
}
