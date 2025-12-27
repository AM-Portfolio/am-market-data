package com.am.marketdata.api.controller;

import com.am.marketdata.api.service.MarketIndexApiService;
import com.am.marketdata.common.model.NSEIndex;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Market Index operations
 */
@RestController
@RequestMapping("/api/v1/indices")
@Tag(name = "Market Indices", description = "Endpoints for retrieving market index data")
@RequiredArgsConstructor
public class MarketIndexController {

    private final MarketIndexApiService marketIndexApiService;

    @GetMapping("/all")
    @Operation(summary = "Get all market indices", description = "Returns all available market indices")
    public ResponseEntity<List<NSEIndex>> getAllIndices() {
        return ResponseEntity.ok(marketIndexApiService.getAllIndices());
    }

    @GetMapping("/{symbol}")
    @Operation(summary = "Get index by symbol", description = "Returns a specific index by its symbol")
    public ResponseEntity<NSEIndex> getIndex(@PathVariable String symbol) {
        NSEIndex index = marketIndexApiService.getIndex(symbol);
        if (index != null) {
            return ResponseEntity.ok(index);
        }
        return ResponseEntity.notFound().build();
    }
}
