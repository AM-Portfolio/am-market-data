package com.am.marketdata.scraper.client.executor;

import com.am.marketdata.scraper.client.handler.RequestHandler;
import com.am.marketdata.scraper.exception.NSEApiException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Executor for API requests
 * Implements Template Method Pattern for standardized request execution
 */
@Slf4j
public class RequestExecutor {
    
    private static final String METRIC_PREFIX = "nse.api.";
    private static final String METRIC_REQUEST_TIME = METRIC_PREFIX + "request.time";
    private static final String METRIC_ERROR_COUNT = METRIC_PREFIX + "error.count";
    private static final String METRIC_REQUEST_COUNT = METRIC_PREFIX + "request.count";
    private static final String TAG_ENDPOINT = "endpoint";
    private static final String TAG_ERROR_TYPE = "error_type";
    
    private final RestTemplate restTemplate;
    private final MeterRegistry meterRegistry;
    private final String baseUrl;
    private final HttpHeaders headers;

    public RequestExecutor(RestTemplate restTemplate, MeterRegistry meterRegistry, String baseUrl, HttpHeaders headers) {
        this.restTemplate = restTemplate;
        this.meterRegistry = meterRegistry;
        this.baseUrl = baseUrl;
        this.headers = headers;
    }

    public <T> T execute(RequestHandler<T> handler) throws NSEApiException {
        String endpoint = handler.getEndpoint();
        
        // Record request attempt
        meterRegistry.counter(METRIC_REQUEST_COUNT, TAG_ENDPOINT, endpoint).increment();
        
        try {
            Timer.Sample sample = Timer.start();
            
            // Build request
            HttpEntity<?> request = new HttpEntity<>(handler.getBody(), headers);
            
            // Execute request
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + endpoint,
                handler.getMethod(),
                request,
                String.class
            );
            
            // Record response time
            sample.stop(meterRegistry.timer(METRIC_REQUEST_TIME, TAG_ENDPOINT, endpoint));
            
            // Process response
            return handler.processResponse(response.getBody());
            
        } catch (Exception e) {
            // Record error
            meterRegistry.counter(METRIC_ERROR_COUNT, 
                TAG_ENDPOINT, endpoint,
                TAG_ERROR_TYPE, handler.getErrorType(e)
            ).increment();
            
            throw new NSEApiException(
                endpoint,
                handler.getHttpStatus(e),
                handler.getResponseBody(e),
                handler.getErrorMessage(e),
                e
            );
        }
    }
}
