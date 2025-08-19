// package com.am.marketdata.api.controller;

// import java.util.Optional;

// import org.springframework.http.ResponseEntity;
// import org.springframework.validation.annotation.Validated;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.am.common.investment.model.board.BoardOfDirectors;
// import com.am.common.investment.model.equity.financial.balancesheet.StockBalanceSheet;
// import com.am.common.investment.model.equity.financial.cashflow.StockCashFlow;
// import com.am.common.investment.model.equity.financial.factsheetdividend.StockFactSheetDividend;
// import com.am.common.investment.model.equity.financial.profitandloss.StockProfitAndLoss;
// import com.am.common.investment.model.equity.financial.resultstatement.QuaterlyResult;
// import com.am.marketdata.processor.service.StockPerformaceService;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import io.swagger.v3.oas.annotations.media.Content;
// import io.swagger.v3.oas.annotations.media.Schema;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import jakarta.validation.constraints.NotBlank;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// /**
//  * Controller for handling board of directors related endpoints
//  */
// @RestController
// @RequestMapping("/api/v1/stocks")
// @Tag(name = "Board of Directors", description = "APIs for managing board of directors information")
// @Slf4j
// @Validated
// @RequiredArgsConstructor
// public class StockPortfolioController {
    
//     private final StockPerformaceService stockFinancialPerformaceService;

//     @GetMapping("/{symbol}/board-of-directors")
//     @Operation(summary = "Get board of directors for a stock")
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "Successfully retrieved board of directors", 
//             content = @Content(schema = @Schema(implementation = BoardOfDirectors.class))),
//         @ApiResponse(responseCode = "404", description = "No board of directors found for the symbol"),
//         @ApiResponse(responseCode = "500", description = "Internal server error")
//     })
//     public ResponseEntity<BoardOfDirectors> getBoardOfDirectors(
//             @Parameter(description = "Stock symbol", required = true)
//             @PathVariable("symbol") @NotBlank String symbol) {
        
//         log.info("Fetching board of directors for symbol: {}", symbol);
        
//         Optional<BoardOfDirectors> directors = stockFinancialPerformaceService.fetchAndProcessStockOverview(symbol);
        
//         if (directors.isEmpty()) {
//             log.warn("No board of directors found for symbol: {}", symbol);
//             return ResponseEntity.notFound().build();
//         }
        
//         log.info("Successfully retrieved board of directors for symbol: {}", symbol);
//         return ResponseEntity.ok(directors.get());
//     }


//     @GetMapping("/{symbol}/quaterly-financials")
//     @Operation(summary = "Get quaterly financials for a stock")
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "Successfully retrieved quaterly financials", 
//             content = @Content(schema = @Schema(implementation = QuaterlyResult.class))),
//         @ApiResponse(responseCode = "404", description = "No quaterly financials found for the symbol"),
//         @ApiResponse(responseCode = "500", description = "Internal server error")
//     })
//     public ResponseEntity<QuaterlyResult> getQuaterlyFinancials(
//             @Parameter(description = "Stock symbol", required = true)
//             @PathVariable("symbol") @NotBlank String symbol) {
        
//         log.info("Fetching quaterly financials for symbol: {}", symbol);
        
//         Optional<QuaterlyResult> quaterlyResult = stockFinancialPerformaceService.fetchAndProcessQuaterlyFinancials(symbol);
        
//         if (quaterlyResult.isEmpty()) {
//             log.warn("No quaterly financials found for symbol: {}", symbol);
//             return ResponseEntity.notFound().build();
//         }
        
//         log.info("Successfully retrieved quaterly financials for symbol: {}", symbol);
//         return ResponseEntity.ok(quaterlyResult.get());
//     }

    
//     @GetMapping("/{symbol}/balance-sheet")
//     @Operation(summary = "Get balance sheet for a stock")
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "Successfully retrieved balance sheet", 
//             content = @Content(schema = @Schema(implementation = StockBalanceSheet.class))),
//         @ApiResponse(responseCode = "404", description = "No balance sheet found for the symbol"),
//         @ApiResponse(responseCode = "500", description = "Internal server error")
//     })
//     public ResponseEntity<StockBalanceSheet> getBalanceSheet(
//             @Parameter(description = "Stock symbol", required = true)
//             @PathVariable("symbol") @NotBlank String symbol) {
        
//         log.info("Fetching balance sheet for symbol: {}", symbol);
        
//         Optional<StockBalanceSheet> balanceSheet = stockFinancialPerformaceService.fetchAndProcessBalanceSheet(symbol);
        
//         if (balanceSheet.isEmpty()) {
//             log.warn("No balance sheet found for symbol: {}", symbol);
//             return ResponseEntity.notFound().build();
//         }
        
//         log.info("Successfully retrieved balance sheet for symbol: {}", symbol);
//         return ResponseEntity.ok(balanceSheet.get());
//     }

//     @GetMapping("/{symbol}/profit-and-loss")
//     @Operation(summary = "Get profit and loss for a stock")
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "Successfully retrieved profit and loss", 
//             content = @Content(schema = @Schema(implementation = StockProfitAndLoss.class))),
//         @ApiResponse(responseCode = "404", description = "No profit and loss found for the symbol"),
//         @ApiResponse(responseCode = "500", description = "Internal server error")
//     })
//     public ResponseEntity<StockProfitAndLoss> getProfitAndLoss(
//             @Parameter(description = "Stock symbol", required = true)
//             @PathVariable("symbol") @NotBlank String symbol) {
        
//         log.info("Fetching profit and loss for symbol: {}", symbol);
        
//         Optional<StockProfitAndLoss> profitAndLoss = stockFinancialPerformaceService.fetchAndProcessProfitAndLoss(symbol);
        
//         if (profitAndLoss.isEmpty()) {
//             log.warn("No profit and loss found for symbol: {}", symbol);
//             return ResponseEntity.notFound().build();
//         }
        
//         log.info("Successfully retrieved profit and loss for symbol: {}", symbol);
//         return ResponseEntity.ok(profitAndLoss.get());
//     }

//     @GetMapping("/{symbol}/cash-flow")
//     @Operation(summary = "Get cash flow for a stock")
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "Successfully retrieved cash flow", 
//             content = @Content(schema = @Schema(implementation = StockCashFlow.class))),
//         @ApiResponse(responseCode = "404", description = "No cash flow found for the symbol"),
//         @ApiResponse(responseCode = "500", description = "Internal server error")
//     })
//     public ResponseEntity<StockCashFlow> getCashFlow(
//             @Parameter(description = "Stock symbol", required = true)
//             @PathVariable("symbol") @NotBlank String symbol) {
        
//         log.info("Fetching cash flow for symbol: {}", symbol);
        
//         Optional<StockCashFlow> cashFlow = stockFinancialPerformaceService.fetchAndProcessCashFlow(symbol);
        
//         if (cashFlow.isEmpty()) {
//             log.warn("No cash flow found for symbol: {}", symbol);
//             return ResponseEntity.notFound().build();
//         }
        
//         log.info("Successfully retrieved cash flow for symbol: {}", symbol);
//         return ResponseEntity.ok(cashFlow.get());
//     }

//     @GetMapping("/{symbol}/factsheet-dividend")
//     @Operation(summary = "Get factsheet dividend for a stock")
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "Successfully retrieved factsheet dividend", 
//             content = @Content(schema = @Schema(implementation = StockFactSheetDividend.class))),
//         @ApiResponse(responseCode = "404", description = "No factsheet dividend found for the symbol"),
//         @ApiResponse(responseCode = "500", description = "Internal server error")
//     })
//     public ResponseEntity<StockFactSheetDividend> getFactsheetDividend(
//             @Parameter(description = "Stock symbol", required = true)
//             @PathVariable("symbol") @NotBlank String symbol) {
        
//         log.info("Fetching factsheet dividend for symbol: {}", symbol);
        
//         Optional<StockFactSheetDividend> factsheetDividend = stockFinancialPerformaceService.fetchAndProcessFactSheetDividend(symbol);
        
//         if (factsheetDividend.isEmpty()) {
//             log.warn("No factsheet dividend found for symbol: {}", symbol);
//             return ResponseEntity.notFound().build();
//         }
        
//         log.info("Successfully retrieved factsheet dividend for symbol: {}", symbol);
//         return ResponseEntity.ok(factsheetDividend.get());
//     }
// }
