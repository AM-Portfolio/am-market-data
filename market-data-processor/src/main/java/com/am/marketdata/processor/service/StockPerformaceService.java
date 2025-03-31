package com.am.marketdata.processor.service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.common.investment.service.StockFinancialPerformanceService;
import com.am.marketdata.processor.service.operation.StockOverviewDataOperation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockPerformaceService {

    private final StockOverviewDataOperation stockOverviewDataOperation;
    private final StockFinancialPerformanceService stockFinancialPerformanceService;

    /**
     * Fetch and process stock overview data, then get board of directors
     * @param symbol Stock symbol to process
     * @return Optional BoardOfDirectors data
     */
    public Optional<BoardOfDirectors> fetchAndProcessStockOverview(String symbol) {
        try {
            // Execute the stock overview operation and wait for completion
            CompletableFuture<Boolean> result = stockOverviewDataOperation
                .withSymbol(symbol)
                .executeAsync()
                .exceptionally(ex -> {
                    log.error("Failed to process stock overview for symbol {}: {}", symbol, ex.getMessage());
                    return false;
                });

            // Wait for the operation to complete
            boolean operationSuccess = result.get();

            if (operationSuccess) {
                // Only fetch board of directors if the operation was successful
                return stockFinancialPerformanceService.getBoardOfDirectors(symbol);
            } else {
                log.warn("Stock overview processing failed for symbol: {}", symbol);
                return Optional.empty();
            }

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error processing stock overview for symbol {}: {}", symbol, e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted status
            return Optional.empty();
        }
    }
}
