package com.am.marketdata.api.controller;

import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.marketdata.api.service.StockIndicesService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nse-indices")
@RequiredArgsConstructor
public class MarketIndexController {
    
    private final StockIndicesService stockIndicesService;

    @GetMapping("/{indexSymbol}")
    @Operation(summary = "Get latest market data for a single NSE index")
    public ResponseEntity<StockIndicesMarketData> getLatestIndexData(
            @Parameter(description = "NSE Index symbol", required = true)
            @PathVariable("indexSymbol") String indexSymbol
    ) {
        return ResponseEntity.ok(stockIndicesService.getLatestIndexData(indexSymbol));
    }

    @PostMapping("/batch")
    @Operation(summary = "Get latest market data for multiple NSE indices")
    public ResponseEntity<List<StockIndicesMarketData>> getLatestIndicesData(
            @Parameter(description = "List of NSE Index symbols", required = true)
            @RequestBody List<String> indexSymbols) {
        return ResponseEntity.ok(stockIndicesService.getLatestIndicesData(indexSymbols));
    }   
}
