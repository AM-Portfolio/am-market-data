package com.am.marketdata.scraper.client.executor;

import com.am.marketdata.scraper.client.handler.RequestHandler;
import com.am.marketdata.scraper.exception.NSEApiException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
    
    /**
     * Create a new request executor
     * 
     * @param restTemplate The REST template to use
     * @param meterRegistry The meter registry for metrics
     * @param baseUrl The base URL for API requests
     * @param headers The HTTP headers to include in requests
     */
    public RequestExecutor(RestTemplate restTemplate, MeterRegistry meterRegistry, 
                           String baseUrl, HttpHeaders headers) {
        this.restTemplate = restTemplate;
        this.meterRegistry = meterRegistry;
        this.baseUrl = baseUrl;
        this.headers = headers;
    }
    
    /**
     * Execute a request using the provided handler
     * 
     * @param <T> The response type
     * @param handler The request handler
     * @return The response
     */
    public <T> T execute(RequestHandler<T> handler) {
        String endpoint = handler.getEndpoint();
        String url = baseUrl + endpoint;
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // Get or create timer for this endpoint
        Timer timer = Timer.builder(METRIC_REQUEST_TIME)
            .tag(TAG_ENDPOINT, handler.getMetricName())
            .description("Time taken for " + handler.getMetricName() + " API requests")
            .register(meterRegistry);
        
        // Record request count
        meterRegistry.counter(METRIC_REQUEST_COUNT, TAG_ENDPOINT, handler.getMetricName()).increment();
        
        return timer.record(() -> {
            try {
                log.info("Calling NSE API - Endpoint: {}, URL: {}", endpoint, url);
                
                ResponseEntity<T> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity, 
                    handler.getResponseType()
                );
                
                if (response.getBody() == null) {
                    recordError(handler.getMetricName(), "empty_response");
                    throw new NSEApiException(endpoint, response.getStatusCode(), "null", "Empty response from NSE API");
                }
                
                // Process and log the response
                T result = handler.processResponse(response);
                handler.logResponse(result);
                
                return result;
                
            } catch (Exception e) {
                handleException(e, endpoint, handler.getMetricName());
                throw e;
            }
        });
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
     * Handle an exception from an API call
     * 
     * @param e The exception
     * @param endpoint The endpoint
     * @param metricName The metric name
     */
    private void handleException(Exception e, String endpoint, String metricName) {
        if (e instanceof NSEApiException) {
            // Already handled and logged in the NSEApiClient
            recordError(metricName, ((NSEApiException) e).getErrorType());
        } else {
            log.error("Unexpected error calling NSE API - Endpoint: {}, Error: {}", endpoint, e.getMessage(), e);
            recordError(metricName, "unexpected_error");
        }
    }
}
