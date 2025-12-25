package com.am.marketdata.api.controller;

import com.am.marketdata.api.model.StreamConnectRequest;
import com.am.marketdata.api.service.MarketDataPollingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/market-data/stream")
@RequiredArgsConstructor
@Tag(name = "Market Data Stream", description = "APIs for managing market data WebSocket streams")
public class MarketDataStreamController {

    private final MarketDataPollingService pollingService;
    private final com.am.marketdata.api.util.InstrumentUtils instrumentUtils;

    @PostMapping("/connect")
    @Operation(summary = "Connect to market data stream", description = "Initiates a WebSocket connection for the specified provider and instruments")
    public ResponseEntity<String> connect(@RequestBody StreamConnectRequest request) {
        try {
            log.info("Received stream connection request for provider: {}", request.getProvider());

            // Use expandIndices from request (defaults to false if not provided)
            boolean expandIndices = request.getExpandIndices() != null ? request.getExpandIndices() : false;

            // Resolve symbols/indices based on expandIndices parameter
            java.util.Set<String> resolvedSymbols = instrumentUtils.resolveSymbols(
                    request.getInstrumentKeys(),
                    expandIndices);
            log.info("Resolved {} symbols to {} for stream (expandIndices={})",
                    request.getInstrumentKeys().size(), resolvedSymbols.size(), expandIndices);

            pollingService.connectStream(new java.util.ArrayList<>(resolvedSymbols), request.getMode(),
                    request.getProvider());
            return ResponseEntity.ok("Stream connection initiated successfully");
        } catch (Exception e) {
            log.error("Failed to initiate stream connection: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    @PostMapping("/disconnect")
    @Operation(summary = "Disconnect market data stream", description = "Disconnects the WebSocket stream for the specified provider")
    public ResponseEntity<String> disconnect(@RequestParam String provider) {
        try {
            pollingService.disconnectStream(provider);
            return ResponseEntity.ok("Disconnected successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }
}
