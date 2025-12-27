package com.am.marketdata.api.controller;

import com.am.marketdata.api.service.MarginCalculatorApiService;
import com.marketdata.common.model.margin.MarginCalculationRequest;
import com.marketdata.common.model.margin.MarginCalculationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for margin requirement calculations
 */
@RestController
@RequestMapping("/api/v1/margin")
@Tag(name = "Margin Calculator", description = "Endpoints for calculating margin requirements for trades")
@RequiredArgsConstructor
public class MarginCalculatorController {

    private final MarginCalculatorApiService marginCalculatorService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate margin requirements", description = "Calculates SPAN, exposure, and total margin requirements for various positions")
    public ResponseEntity<MarginCalculationResponse> calculateMargin(@RequestBody MarginCalculationRequest request) {
        return ResponseEntity.ok(marginCalculatorService.calculateMargin(request));
    }
}
