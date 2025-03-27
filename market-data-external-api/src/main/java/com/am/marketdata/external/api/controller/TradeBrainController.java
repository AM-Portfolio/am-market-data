package com.am.marketdata.external.api.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.am.marketdata.external.api.client.TradeBrainClient;
import com.am.marketdata.external.api.model.ApiResponse;
import com.am.marketdata.external.api.registry.ApiEndpoint;
import com.am.marketdata.external.api.registry.ApiEndpointRegistry;
import com.am.marketdata.external.api.service.EndpointHealthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for TradeBrain API
 */
@RestController
@RequestMapping("/api/tradebrain")
@RequiredArgsConstructor
@Slf4j
public class TradeBrainController {
    
    private final TradeBrainClient tradeBrainClient;
    private final ApiEndpointRegistry endpointRegistry;
    private final EndpointHealthService endpointHealthService;
    
    /**
     * Get market indices data from TradeBrain
     * 
     * @return Market indices data
     */
    @GetMapping("/indices")
    public ResponseEntity<String> getIndicesData() {
        log.debug("Getting market indices data from TradeBrain");
        
        ApiResponse response = tradeBrainClient.getIndicesData();
        
        if (!response.isSuccess()) {
            log.error("Failed to get market indices data: {}", response.getErrorMessage());
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to get market indices data: " + response.getErrorMessage());
        }
        
        return ResponseEntity.ok(response.getData());
    }
    
    /**
     * Check health status of all registered endpoints
     * 
     * @return Health status of all endpoints
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkEndpointsHealth() {
        log.debug("Checking health status of all endpoints");
        
        // Use the service to check endpoint health with a 5-second delay
        Map<String, Object> healthStatus = endpointHealthService.checkEndpointsHealth(5000);
        
        return ResponseEntity.ok(healthStatus);
    }
    
    /**
     * Get all registered endpoints
     * 
     * @return Map of all registered endpoints grouped by prefix
     */
    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, Object>> getAllEndpoints() {
        log.debug("Getting all registered endpoints");
        
        Collection<ApiEndpoint> allEndpoints = endpointRegistry.getAllEndpoints();
        
        // Group endpoints by their prefix (e.g., tradebrain.company.profile.*)
        Map<String, Object> groupedEndpoints = new HashMap<>();
        
        for (ApiEndpoint endpoint : allEndpoints) {
            String id = endpoint.getId();
            
            // Skip non-TradeBrain endpoints
            if (!id.startsWith("tradebrain.")) {
                continue;
            }
            
            // Create endpoint info
            Map<String, Object> endpointInfo = new HashMap<>();
            endpointInfo.put("name", endpoint.getName());
            endpointInfo.put("path", endpoint.getPath());
            endpointInfo.put("method", endpoint.getMethod());
            endpointInfo.put("url", endpoint.getUrl());
            endpointInfo.put("healthCheckEnabled", endpoint.isHealthCheckEnabled());
            
            // Split the ID by dots to create a hierarchical structure
            String[] parts = id.split("\\.");
            
            // Build the hierarchical structure
            addToHierarchy(groupedEndpoints, parts, 0, endpointInfo);
        }
        
        return ResponseEntity.ok(groupedEndpoints);
    }
    
    /**
     * Get a specific endpoint by ID
     * 
     * @param id Endpoint ID
     * @return Endpoint details
     */
    @GetMapping("/endpoints/{id}")
    public ResponseEntity<?> getEndpoint(@PathVariable String id) {
        log.debug("Getting endpoint with ID: {}", id);
        
        ApiEndpoint endpoint = endpointRegistry.getEndpoint("tradebrain." + id);
        
        if (endpoint == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> endpointInfo = new HashMap<>();
        endpointInfo.put("id", endpoint.getId());
        endpointInfo.put("name", endpoint.getName());
        endpointInfo.put("baseUrl", endpoint.getBaseUrl());
        endpointInfo.put("path", endpoint.getPath());
        endpointInfo.put("method", endpoint.getMethod());
        endpointInfo.put("url", endpoint.getUrl());
        endpointInfo.put("healthCheckEnabled", endpoint.isHealthCheckEnabled());
        
        return ResponseEntity.ok(endpointInfo);
    }
    
    /**
     * Get stock details by symbol
     * 
     * @param symbol Stock symbol
     * @return Stock details
     */
    @GetMapping("/stock/{symbol}")
    public ResponseEntity<String> getStockDetails(@PathVariable String symbol) {
        log.debug("Getting stock details for symbol: {}", symbol);
        
        ApiResponse response = tradeBrainClient.getStockDetails(symbol);
        
        if (!response.isSuccess()) {
            log.error("Failed to get stock details: {}", response.getErrorMessage());
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to get stock details: " + response.getErrorMessage());
        }
        
        return ResponseEntity.ok(response.getData());
    }
    
    /**
     * Recursively add an endpoint to the hierarchical structure
     * 
     * @param current Current level in the hierarchy
     * @param parts Parts of the endpoint ID
     * @param index Current index in the parts array
     * @param endpointInfo Endpoint information
     */
    @SuppressWarnings("unchecked")
    private void addToHierarchy(Map<String, Object> current, String[] parts, int index, Map<String, Object> endpointInfo) {
        if (index >= parts.length) {
            return;
        }
        
        String part = parts[index];
        
        if (index == parts.length - 1) {
            // Last part, add the endpoint info
            current.put(part, endpointInfo);
        } else {
            // Not the last part, create or get the next level
            if (!current.containsKey(part)) {
                current.put(part, new HashMap<String, Object>());
            }
            
            // Move to the next level
            addToHierarchy((Map<String, Object>) current.get(part), parts, index + 1, endpointInfo);
        }
    }
}
