package com.am.marketdata.api.controller;

import com.am.marketdata.api.service.MarketDataPollingApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for Market Data Polling operations
 */
@RestController
@RequestMapping("/api/v1/polling")
@Tag(name = "Market Data Polling", description = "Endpoints for real-time market data polling")
@RequiredArgsConstructor
public class MarketDataPollingController {

    private final MarketDataPollingApiService marketDataPollingApiService;

    @GetMapping("/data")
    @Operation(summary = "Poll market data", description = "Poll live market data for given symbols")
    public ResponseEntity<Map<String, Object>> pollMarketData(
            @RequestParam String symbols,
            @RequestParam(defaultValue = "1m") String timeFrame,
            @RequestParam(defaultValue = "false") boolean indexSymbol) {
        return ResponseEntity.ok(marketDataPollingApiService.pollMarketData(symbols, timeFrame, indexSymbol));
    }

    @GetMapping("/status")
    @Operation(summary = "Get connection status", description = "Returns streaming connection status")
    public ResponseEntity<Map<String, Object>> getConnectionStatus() {
        return ResponseEntity.ok(marketDataPollingApiService.getConnectionStatus());
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to symbols", description = "Subscribe to symbols for real-time updates")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestBody List<String> symbols) {
        return ResponseEntity.ok(marketDataPollingApiService.subscribe(symbols));
    }

    @PostMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe from symbols", description = "Unsubscribe from symbols")
    public ResponseEntity<Map<String, Object>> unsubscribe(@RequestBody List<String> symbols) {
        return ResponseEntity.ok(marketDataPollingApiService.unsubscribe(symbols));
    }

    @GetMapping("/schema/update")
    @Operation(summary = "Internal use for SDK generation")
    public com.am.marketdata.common.model.MarketDataUpdateV1 getUpdateSchema() {
        return null;
    }
}
