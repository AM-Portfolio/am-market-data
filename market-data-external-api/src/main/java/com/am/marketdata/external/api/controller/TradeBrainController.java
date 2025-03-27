package com.am.marketdata.external.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.am.marketdata.external.api.client.TradeBrainClient;
import com.am.marketdata.external.api.model.ApiResponse;
import com.am.marketdata.external.api.service.ApiResponseProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for TradeBrain API endpoints
 */
@RestController
@RequestMapping("/api/tradebrain")
@RequiredArgsConstructor
@Slf4j
public class TradeBrainController {
    
    private final TradeBrainClient tradeBrainClient;
    private final ApiResponseProcessor apiResponseProcessor;
    
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
     * Get stock indices data from TradeBrain
     * 
     * @return Stock indices data
     */
    @GetMapping("/stock-indices")
    public ResponseEntity<String> getStockIndicesData() {
        log.debug("Getting stock indices data from TradeBrain");
        
        ApiResponse response = tradeBrainClient.getStockIndicesData();
        
        if (!response.isSuccess()) {
            log.error("Failed to get stock indices data: {}", response.getErrorMessage());
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to get stock indices data: " + response.getErrorMessage());
        }
        
        return ResponseEntity.ok(response.getData());
    }
    
    /**
     * Get all indices data (both market and stock) from TradeBrain
     * 
     * @return Combined indices data
     */
    @GetMapping("/all-indices")
    public ResponseEntity<String> getAllIndicesData() {
        log.debug("Getting all indices data from TradeBrain");
        
        // Process both requests asynchronously
        var marketIndicesFuture = apiResponseProcessor.processRequestAsync(
                TradeBrainClient.TRADEBRAIN_ENDPOINT_INDEX, 
                ApiResponse::getData);
        
        var stockIndicesFuture = apiResponseProcessor.processRequestAsync(
                TradeBrainClient.TRADEBRAIN_STOCK_ENDPOINT_INDEX, 
                ApiResponse::getData);
        
        try {
            // Wait for both futures to complete
            String marketIndices = marketIndicesFuture.get();
            String stockIndices = stockIndicesFuture.get();
            
            if (marketIndices == null && stockIndices == null) {
                log.error("Failed to get any indices data");
                return ResponseEntity.status(503)
                        .body("Failed to get any indices data from TradeBrain");
            }
            
            // Combine results
            StringBuilder result = new StringBuilder();
            if (marketIndices != null) {
                result.append("Market Indices:\n").append(marketIndices);
            }
            
            if (stockIndices != null) {
                if (result.length() > 0) {
                    result.append("\n\n");
                }
                result.append("Stock Indices:\n").append(stockIndices);
            }
            
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            log.error("Error processing indices data: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body("Error processing indices data: " + e.getMessage());
        }
    }
}
