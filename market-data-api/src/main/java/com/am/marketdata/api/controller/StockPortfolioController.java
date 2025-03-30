package com.am.marketdata.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.marketdata.tradebrain.service.stockdetails.StockPortfolioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for handling board of directors related endpoints
 */
@RestController
@RequestMapping("/api/v1/stocks")
@Tag(name = "Board of Directors", description = "APIs for managing board of directors information")
@Slf4j
@Validated
@RequiredArgsConstructor
public class StockPortfolioController {
    
    private final StockPortfolioService stockPortfolioService;

    /**
     * Get board of directors for a specific stock
     * 
     * @param symbol Stock symbol
     * @return Board of directors information
     */
    @GetMapping("/{symbol}/board-of-directors")
    @Operation(summary = "Get board of directors for a stock")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved board of directors", 
            content = @Content(schema = @Schema(implementation = BoardOfDirectors.class))),
        @ApiResponse(responseCode = "404", description = "No board of directors found for the symbol"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BoardOfDirectors> getBoardOfDirectors(
            @Parameter(description = "Stock symbol", required = true)
            @PathVariable("symbol") @NotBlank String symbol) {
        
        log.info("Fetching board of directors for symbol: {}", symbol);
        
        
        BoardOfDirectors directors = stockPortfolioService.fetchAndPublishBoardOfDirectors(symbol);
        if (directors == null) {
            log.warn("No board of directors found for symbol: {}", symbol);
            return ResponseEntity.notFound().build();
        }
        
        log.info("Successfully retrieved board of directors for symbol: {}", symbol);
        return ResponseEntity.ok(directors);
    }
}
