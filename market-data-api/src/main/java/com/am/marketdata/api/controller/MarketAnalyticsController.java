package com.am.marketdata.api.controller;

import com.am.marketdata.api.service.MarketAnalyticsApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controller for Market Analytics operations
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Market Analytics", description = "Endpoints for market analytics, summaries, and insights")
@RequiredArgsConstructor
public class MarketAnalyticsController {

    private final MarketAnalyticsApiService marketAnalyticsApiService;

    @GetMapping("/summary")
    @Operation(summary = "Get market summary", description = "Returns overall market summary with key metrics")
    public ResponseEntity<Map<String, Object>> getMarketSummary() {
        return ResponseEntity.ok(marketAnalyticsApiService.getMarketSummary());
    }

    @GetMapping("/sectors")
    @Operation(summary = "Get sector performance", description = "Returns sector-wise performance data")
    public ResponseEntity<Map<String, Object>> getSectorPerformance() {
        return ResponseEntity.ok(marketAnalyticsApiService.getSectorPerformance());
    }

    @GetMapping("/movers")
    @Operation(summary = "Get top gainers and losers", description = "Returns top gaining and losing stocks")
    public ResponseEntity<Map<String, List<Object>>> getTopMovers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(marketAnalyticsApiService.getTopMovers(limit));
    }

    @GetMapping("/breadth")
    @Operation(summary = "Get market breadth", description = "Returns market breadth data (advances, declines, unchanged)")
    public ResponseEntity<Map<String, Object>> getMarketBreadth() {
        return ResponseEntity.ok(marketAnalyticsApiService.getMarketBreadth());
    }
}
