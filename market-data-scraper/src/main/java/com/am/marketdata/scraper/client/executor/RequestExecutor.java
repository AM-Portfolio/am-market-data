package com.am.marketdata.scraper.client.executor;

import com.am.marketdata.scraper.client.handler.RequestHandler;
import com.am.marketdata.scraper.exception.NSEApiException;
import com.am.marketdata.scraper.service.cookie.CookieCacheService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    private static final int TIMEOUT_SECONDS = 5;

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
        
        // Configure timeout
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT_SECONDS * 1000);
        requestFactory.setReadTimeout(TIMEOUT_SECONDS * 1000);
        restTemplate.setRequestFactory(requestFactory);
    }

    public <T> T execute(RequestHandler<T> handler) throws NSEApiException {
        String endpoint = handler.getEndpoint();
        
        // Record request attempt
        meterRegistry.counter(METRIC_REQUEST_COUNT, TAG_ENDPOINT, endpoint).increment();
        
        try {
            Timer.Sample sample = Timer.start();
            
            // Get cookies from cache
            String cookies = cookieCacheService.getCookies();
            if (cookies == null) {
                throw new NSEApiException(
                    endpoint,
                    HttpStatusCode.valueOf(401),
                    "",
                    "No valid cookies found in cache",
                    null
                );
            }

            // Create headers with cookies
            HttpHeaders headers = new HttpHeaders(baseHeaders);
            headers.set(HttpHeaders.COOKIE, cookies);
            
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
