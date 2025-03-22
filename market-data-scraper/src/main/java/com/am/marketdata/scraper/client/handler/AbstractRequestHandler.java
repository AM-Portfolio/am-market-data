package com.am.marketdata.scraper.client.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

/**
 * Abstract base class for request handlers
 * Implements common functionality for all handlers
 * 
 * @param <T> The response type
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRequestHandler<T> implements RequestHandler<T> {
    
    protected final ObjectMapper objectMapper;
    
    @Override
    public T processResponse(ResponseEntity<T> response) {
        // Default implementation just returns the body
        return response.getBody();
    }
    
    /**
     * Safely log response details, catching any exceptions
     * 
     * @param response The response to log
     */
    protected void safelyLogResponse(T response) {
        try {
            logResponse(response);
        } catch (Exception e) {
            log.warn("Failed to log response details for endpoint {}: {}", 
                getEndpoint(), e.getMessage());
        }
    }
    
    /**
     * Convert an object to JSON string safely
     * 
     * @param obj The object to convert
     * @return JSON string or error message
     */
    protected String toJsonSafely(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return "[Failed to serialize]";
        }
    }
}
