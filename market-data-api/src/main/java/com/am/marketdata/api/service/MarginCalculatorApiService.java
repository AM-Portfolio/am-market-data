package com.am.marketdata.api.service;

import com.marketdata.common.model.margin.MarginCalculationRequest;
import com.marketdata.common.model.margin.MarginCalculationResponse;
import com.marketdata.service.margin.MarginCalculatorService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * API service for margin calculation operations
 * Acts as a bridge between the controller and core service layer
 */
@Slf4j
@Service
public class MarginCalculatorApiService {

    private final MarginCalculatorService marginCalculatorService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public MarginCalculatorApiService(
            MarginCalculatorService marginCalculatorService,
            MeterRegistry meterRegistry) {
        this.marginCalculatorService = marginCalculatorService;
        this.meterRegistry = meterRegistry;
        log.info("Initializing Margin Calculator API Service");
    }

    /**
     * Calculate margin requirement for a list of positions
     * 
     * @param request The margin calculation request containing positions
     * @return MarginCalculationResponse with calculated margins
     */
    public MarginCalculationResponse calculateMargin(MarginCalculationRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        log.info("Processing margin calculation request for {} positions", request.getPositions().size());
        
        try {
            // Validate request
            validateRequest(request);
            
            // Call service layer
            MarginCalculationResponse response = marginCalculatorService.calculateMargin(request);
            
            // Record metrics
            sample.stop(meterRegistry.timer("market-data.api.margin.calculation.time"));
            meterRegistry.counter("market-data.api.margin.calculation.success").increment();
            
            return response;
        } catch (Exception e) {
            log.error("Error processing margin calculation request: {}", e.getMessage(), e);
            meterRegistry.counter("market-data.api.margin.calculation.error").increment();
            throw e;
        }
    }

    /**
     * Calculate margin requirement asynchronously
     * 
     * @param request The margin calculation request
     * @return CompletableFuture with the margin calculation response
     */
    public CompletableFuture<MarginCalculationResponse> calculateMarginAsync(MarginCalculationRequest request) {
        log.info("Processing async margin calculation request for {} positions", request.getPositions().size());
        meterRegistry.counter("market-data.api.margin.calculation.async").increment();
        
        try {
            // Validate request
            validateRequest(request);
            
            // Call service layer asynchronously
            return marginCalculatorService.calculateMarginAsync(request);
        } catch (Exception e) {
            log.error("Error processing async margin calculation request: {}", e.getMessage(), e);
            meterRegistry.counter("market-data.api.margin.calculation.async.error").increment();
            CompletableFuture<MarginCalculationResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Validate the margin calculation request
     * 
     * @param request The request to validate
     * @throws IllegalArgumentException if the request is invalid
     */
    private void validateRequest(MarginCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Margin calculation request cannot be null");
        }
        
        if (request.getPositions() == null || request.getPositions().isEmpty()) {
            throw new IllegalArgumentException("Positions list cannot be empty");
        }
        
        // Validate each position
        for (int i = 0; i < request.getPositions().size(); i++) {
            MarginCalculationRequest.Position position = request.getPositions().get(i);
            
            if (position.getTradingSymbol() == null || position.getTradingSymbol().isEmpty()) {
                throw new IllegalArgumentException("Trading symbol is required for position at index " + i);
            }
            
            if (position.getType() == null || position.getType().isEmpty()) {
                throw new IllegalArgumentException("Position type is required for position at index " + i);
            }
            
            if (position.getExchange() == null || position.getExchange().isEmpty()) {
                throw new IllegalArgumentException("Exchange is required for position at index " + i);
            }
            
            if (position.getPrice() == null) {
                throw new IllegalArgumentException("Price is required for position at index " + i);
            }
            
            // Validate option-specific fields if position type is option
            if ("option".equalsIgnoreCase(position.getType())) {
                if (position.getOptionType() == null || position.getOptionType().isEmpty()) {
                    throw new IllegalArgumentException("Option type (CE/PE) is required for option position at index " + i);
                }
                
                if (position.getStrikePrice() == null) {
                    throw new IllegalArgumentException("Strike price is required for option position at index " + i);
                }
                
                if (position.getExpiry() == null || position.getExpiry().isEmpty()) {
                    throw new IllegalArgumentException("Expiry date is required for option position at index " + i);
                }
            }
        }
    }
}
