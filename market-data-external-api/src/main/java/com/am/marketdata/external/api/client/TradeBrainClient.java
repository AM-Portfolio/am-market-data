package com.am.marketdata.external.api.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.am.marketdata.external.api.config.TradeBrainConfig;
import com.am.marketdata.external.api.model.ApiResponse;
import com.am.marketdata.external.api.registry.ApiEndpoint;
import com.am.marketdata.external.api.registry.ApiEndpointRegistry;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for TradeBrain API calls
 */
@Component
@Slf4j
public class TradeBrainClient {
    
    private final ApiClient apiClient;
    private final TradeBrainConfig tradeBrainConfig;
    private final ApiEndpointRegistry endpointRegistry;
    
    // Endpoint IDs
    public static final String TRADEBRAIN_ENDPOINT_INDEX = "tradebrain.index";
    public static final String TRADEBRAIN_STOCK_ENDPOINT_INDEX = "tradebrain.stock.index";
    
    /**
     * Constructor with dependencies
     * 
     * @param apiClient Base API client
     * @param tradeBrainConfig TradeBrain configuration
     * @param endpointRegistry API endpoint registry
     */
    public TradeBrainClient(
            ApiClient apiClient, 
            TradeBrainConfig tradeBrainConfig,
            ApiEndpointRegistry endpointRegistry) {
        this.apiClient = apiClient;
        this.tradeBrainConfig = tradeBrainConfig;
        this.endpointRegistry = endpointRegistry;
    }
    
    /**
     * Initialize the client and register endpoints
     */
    @PostConstruct
    public void init() {
        // Create default headers
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", tradeBrainConfig.getHeaders().getUserAgent());
        headers.put("Accept", tradeBrainConfig.getHeaders().getAccept());
        headers.put("Accept-Language", tradeBrainConfig.getHeaders().getAcceptLanguage());
        
        // Register indices endpoint
        ApiEndpoint indicesEndpoint = ApiEndpoint.builder()
                .id(TRADEBRAIN_ENDPOINT_INDEX)
                .name("TradeBrain Indices")
                .baseUrl(tradeBrainConfig.getBaseUrl())
                .path(tradeBrainConfig.getApi().getIndexAll())
                .method("GET")
                .defaultHeaders(headers)
                .healthCheckEnabled(true)
                .build();
        endpointRegistry.registerEndpoint(indicesEndpoint);
        
        // Register stock indices endpoint
        ApiEndpoint stockIndicesEndpoint = ApiEndpoint.builder()
                .id(TRADEBRAIN_STOCK_ENDPOINT_INDEX)
                .name("TradeBrain Stock Indices")
                .baseUrl(tradeBrainConfig.getBaseUrl())
                .path(tradeBrainConfig.getApi().getIndexAll())
                .method("GET")
                .defaultHeaders(headers)
                .healthCheckEnabled(true)
                .build();
        endpointRegistry.registerEndpoint(stockIndicesEndpoint);
        
        log.info("TradeBrain API client initialized with base URL: {}", tradeBrainConfig.getBaseUrl());
    }
    
    /**
     * Get market indices data
     * 
     * @return ApiResponse containing indices data
     */
    public ApiResponse getIndicesData() {
        ApiEndpoint endpoint = endpointRegistry.getEndpoint(TRADEBRAIN_ENDPOINT_INDEX);
        String url = endpoint.getUrl();
        return apiClient.get(url, endpoint.getHeaders());
    }
    
    /**
     * Get stock indices data
     * 
     * @return ApiResponse containing stock indices data
     */
    public ApiResponse getStockIndicesData() {
        ApiEndpoint endpoint = endpointRegistry.getEndpoint(TRADEBRAIN_STOCK_ENDPOINT_INDEX);
        String url = endpoint.getUrl();
        return apiClient.get(url, endpoint.getHeaders());
    }
}
