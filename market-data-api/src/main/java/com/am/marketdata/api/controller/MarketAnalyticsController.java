package com.am.marketdata.api.controller;

import com.am.marketdata.api.service.MarketAnalyticsService;
import com.am.marketdata.common.log.AppLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/market-analytics")
@RequiredArgsConstructor
@Tag(name = "Market Analytics", description = "APIs for market analysis including Top Movers, Sector Performance, and Market Cap Analysis")
public class MarketAnalyticsController {

    private final AppLogger log = AppLogger.getLogger(MarketAnalyticsController.class);
    private final MarketAnalyticsService marketAnalyticsService;

    /**
     * Get Top Gainers or Losers
     * 
     * @param type        "gainers" or "losers" (default: gainers)
     * @param limit       Number of records to return (default: 10)
     * @param indexSymbol Market index to analyze (default: NIFTY 500)
     * @return List of top movers
     */
    @GetMapping(value = "/movers", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Top Gainers/Losers", description = "Retrieves top performing or worst performing stocks from the specified market index")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Map<String, Object>>> getMovers(
            @RequestParam(defaultValue = "gainers") String type,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String indexSymbol,
            @RequestParam(required = false) String timeFrame) {
        try {
            String index = indexSymbol != null ? indexSymbol : "NIFTY 500";
            com.am.marketdata.common.model.TimeFrame tf = timeFrame != null
                    ? com.am.marketdata.common.model.TimeFrame.fromApiValue(timeFrame)
                    : null;
            log.info("getMovers",
                    "Fetching top " + limit + " " + type + " from " + index + " with timeFrame: " + timeFrame);
            List<Map<String, Object>> movers = marketAnalyticsService.getMovers(limit, type, indexSymbol, tf);
            return ResponseEntity.ok(movers);
        } catch (Exception e) {
            log.error("getMovers", "Error fetching movers", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get Sector Performance
     * 
     * @param indexSymbol Market index to analyze (default: NIFTY 500)
     * @return List of sectors and their average performance
     */
    @GetMapping(value = "/sectors", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Sector Performance", description = "Aggregates market performance by sector (Industry) from the specified index")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Map<String, Object>>> getSectorPerformance(
            @RequestParam(required = false) String indexSymbol,
            @RequestParam(required = false) String timeFrame) {
        try {
            String index = indexSymbol != null ? indexSymbol : "NIFTY 500";
            com.am.marketdata.common.model.TimeFrame tf = timeFrame != null
                    ? com.am.marketdata.common.model.TimeFrame.fromApiValue(timeFrame)
                    : null;
            log.info("getSectorPerformance",
                    "Fetching sector performance from " + index + " with timeFrame: " + timeFrame);
            List<Map<String, Object>> sectors = marketAnalyticsService.getSectorPerformance(indexSymbol, tf);
            return ResponseEntity.ok(sectors);
        } catch (Exception e) {
            log.error("getSectorPerformance", "Error fetching sector performance", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
