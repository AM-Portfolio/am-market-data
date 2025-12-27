package com.am.marketdata.api.controller;

import com.am.marketdata.api.service.BrokerageCalculatorApiService;
import com.marketdata.common.model.margin.BrokerageCalculationRequest;
import com.marketdata.common.model.margin.BrokerageCalculationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for brokerage and tax calculations
 */
@RestController
@RequestMapping("/api/v1/brokerage")
@Tag(name = "Brokerage Calculator", description = "Endpoints for calculating brokerage, taxes, and other charges")
@RequiredArgsConstructor
public class BrokerageCalculatorController {

    private final BrokerageCalculatorApiService brokerageCalculatorService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate brokerage and charges", description = "Calculates total brokerage, taxes, and other charges based on trade details")
    public ResponseEntity<BrokerageCalculationResponse> calculateBrokerage(
            @RequestBody BrokerageCalculationRequest request) {
        return ResponseEntity.ok(brokerageCalculatorService.calculateBrokerage(request));
    }
}
