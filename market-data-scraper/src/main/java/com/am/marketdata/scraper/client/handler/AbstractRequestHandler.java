package com.am.marketdata.scraper.client.handler;

import com.am.marketdata.scraper.exception.NSEApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

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
    public Object getBody() {
        return null; // Default is no body
    }
    
    @Override
    public HttpMethod getMethod() {
        return HttpMethod.GET; // Default is GET
    }
    
    @Override
    public T processResponse(String response) {
        try {
            return objectMapper.readValue(response, getResponseType());
        } catch (Exception e) {
            log.error("Failed to parse response for endpoint {}: {}", getEndpoint(), e.getMessage(), e);
            throw new RuntimeException("Failed to parse response", e);
        }
    }
    
    @Override
    public String getErrorType(Exception e) {
        if (e instanceof NSEApiException) {
            return ((NSEApiException) e).getErrorType();
        }
        return "unexpected_error";
    }
    
    @Override
    public HttpStatusCode getHttpStatus(Exception e) {
        if (e instanceof NSEApiException) {
            return ((NSEApiException) e).getStatusCode();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    @Override
    public String getResponseBody(Exception e) {
        if (e instanceof NSEApiException) {
            return ((NSEApiException) e).getResponseBody();
        }
        return "";
    }
    
    @Override
    public String getErrorMessage(Exception e) {
        return e.getMessage();
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
