package com.am.marketdata.api.controller;

import com.am.marketdata.api.service.StockIndicesApiService;
import com.am.marketdata.common.model.NSEStockIndicesDataV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for Stock Indices operations
 */
@RestController
@RequestMapping("/api/v1/stock-indices")
@Tag(name = "Stock Indices", description = "Endpoints for retrieving stock indices constituent data")
@RequiredArgsConstructor
public class StockIndicesController {

    private final StockIndicesApiService stockIndicesApiService;

    @GetMapping("/{indexSymbol}")
    @Operation(summary = "Get stock indices data", description = "Returns constituent stocks for a specific index")
    public ResponseEntity<NSEStockIndicesDataV1> getStockIndices(
            @PathVariable String indexSymbol,
            @RequestParam(defaultValue = "false") boolean forceRefresh) {
        NSEStockIndicesDataV1 data = stockIndicesApiService.getStockIndices(indexSymbol, forceRefresh);
        if (data != null) {
            return ResponseEntity.ok(data);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/batch")
    @Operation(summary = "Get multiple stock indices", description = "Returns constituent stocks for multiple indices")
    public ResponseEntity<List<NSEStockIndicesDataV1>> getStockIndicesBatch(
            @RequestBody List<String> indexSymbols,
            @RequestParam(defaultValue = "false") boolean forceRefresh) {
        return ResponseEntity.ok(stockIndicesApiService.getStockIndicesBatch(indexSymbols, forceRefresh));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available indices", description = "Returns list of available broad market and sector indices")
    public ResponseEntity<Map<String, List<String>>> getAvailableIndices() {
        return ResponseEntity.ok(stockIndicesApiService.getAvailableIndices());
    }
}
