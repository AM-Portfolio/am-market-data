package com.am.marketdata.api.controller;

import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.marketdata.api.service.StockIndicesService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * REST controller for NSE (National Stock Exchange) indices data
 * Provides endpoints for retrieving market data for various NSE indices
 */
@RestController
@RequestMapping("/api/v1/nse-indices")
@RequiredArgsConstructor
@Tag(name = "NSE Indices", description = "APIs for retrieving market data for various NSE indices")
public class MarketIndexController {
    private static final Logger log = LoggerFactory.getLogger(MarketIndexController.class);
    private final StockIndicesService stockIndicesService;

    /**
     * Get latest market data for a single NSE index
     * 
     * @param indexSymbol NSE Index symbol (e.g., NIFTY, BANKNIFTY, FINNIFTY)
     * @param forceRefresh Whether to force refresh from source instead of using cache
     * @return Market data for the requested NSE index
     */
    @GetMapping(value = "/{indexSymbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get latest market data for a single NSE index",
            description = "Retrieves the latest market data for a specified NSE index including current value, change, high/low, and other metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Index data retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockIndicesMarketData.class))),
            @ApiResponse(responseCode = "404", description = "Index symbol not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StockIndicesMarketData> getLatestIndexData(
            @Parameter(description = "NSE Index symbol (e.g., NIFTY, BANKNIFTY, FINNIFTY)", required = true)
            @PathVariable("indexSymbol") String indexSymbol,
            @Parameter(description = "Force refresh from source instead of using cache")
            @RequestParam(value = "forceRefresh", required = false, defaultValue = "false") boolean forceRefresh
    ) {
        StockIndicesMarketData data = stockIndicesService.getLatestIndexData(indexSymbol, forceRefresh);
        
        return ResponseEntity.ok(data);
    }

    /**
     * Get latest market data for multiple NSE indices (commented out)
     * 
     * @param indexSymbols List of NSE Index symbols
     * @param forceRefresh Whether to force refresh from source instead of using cache
     * @return List of market data for the requested NSE indices
     */
    // @PostMapping(value = "/batch", produces = MediaType.APPLICATION_JSON_VALUE)
    // @Operation(summary = "Get latest market data for multiple NSE indices",
    //         description = "Retrieves the latest market data for multiple NSE indices in a single request")
    // @ApiResponses(value = {
    //         @ApiResponse(responseCode = "200", description = "Indices data retrieved successfully",
    //                 content = @Content(mediaType = "application/json",
    //                         schema = @Schema(implementation = StockIndicesMarketData.class))),
    //         @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
    //         @ApiResponse(responseCode = "500", description = "Internal server error")
    // })
    // public ResponseEntity<List<StockIndicesMarketData>> getLatestIndicesData(
    //         @Parameter(description = "List of NSE Index symbols (e.g., NIFTY, BANKNIFTY, FINNIFTY)", required = true)
    //         @RequestBody List<String> indexSymbols,
    //         @Parameter(description = "Force refresh from source instead of using cache")
    //         @RequestParam(value = "forceRefresh", required = false, defaultValue = "false") boolean forceRefresh) {
        
    //     List<StockIndicesMarketData> data = stockIndicesService.getLatestIndicesData(indexSymbols, forceRefresh);
        
    //     return ResponseEntity.ok(data);
    // }   
}
