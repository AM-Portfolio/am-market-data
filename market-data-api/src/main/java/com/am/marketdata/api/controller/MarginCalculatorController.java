package com.am.marketdata.api.controller;

import com.marketdata.common.model.margin.MarginCalculationRequest;
import com.marketdata.common.model.margin.MarginCalculationResponse;
import com.am.marketdata.api.service.MarginCalculatorApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for margin calculation operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/margin")
@Tag(name = "Margin Calculator", description = "APIs for calculating margin requirements")
public class MarginCalculatorController {

    private final MarginCalculatorApiService marginCalculatorApiService;

    @Autowired
    public MarginCalculatorController(MarginCalculatorApiService marginCalculatorApiService) {
        this.marginCalculatorApiService = marginCalculatorApiService;
    }

    /**
     * Calculate margin requirement for a list of positions
     * 
     * @param request The margin calculation request containing positions
     * @return MarginCalculationResponse with calculated margins
     */
    @PostMapping("/calculate")
    @Operation(
        summary = "Calculate margin requirements", 
        description = "Calculate margin requirements for a list of positions"
    )
    public ResponseEntity<MarginCalculationResponse> calculateMargin(
            @RequestBody MarginCalculationRequest request) {
        
        log.info("Received margin calculation request for {} positions", request.getPositions().size());
        
        try {
            MarginCalculationResponse response = marginCalculatorApiService.calculateMargin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating margin: {}", e.getMessage(), e);
            
            MarginCalculationResponse errorResponse = MarginCalculationResponse.builder()
                    .status("ERROR")
                    .error("Failed to calculate margin: " + e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Calculate margin requirement asynchronously
     * 
     * @param request The margin calculation request containing positions
     * @return CompletableFuture with MarginCalculationResponse
     */
    @PostMapping("/calculate-async")
    @Operation(
        summary = "Calculate margin requirements asynchronously", 
        description = "Calculate margin requirements for a list of positions asynchronously"
    )
    public CompletableFuture<ResponseEntity<MarginCalculationResponse>> calculateMarginAsync(
            @RequestBody MarginCalculationRequest request) {
        
        log.info("Received async margin calculation request for {} positions", request.getPositions().size());
        
        return marginCalculatorApiService.calculateMarginAsync(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("Error calculating margin asynchronously: {}", ex.getMessage(), ex);
                    
                    MarginCalculationResponse errorResponse = MarginCalculationResponse.builder()
                            .status("ERROR")
                            .error("Failed to calculate margin: " + ex.getMessage())
                            .build();
                    
                    return ResponseEntity.badRequest().body(errorResponse);
                });
    }
}
