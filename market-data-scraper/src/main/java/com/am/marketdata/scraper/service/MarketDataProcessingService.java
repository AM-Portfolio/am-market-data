package com.am.marketdata.scraper.service;

import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.scraper.exception.MarketDataException;
import com.am.marketdata.scraper.service.operation.ETFDataOperation;
import com.am.marketdata.scraper.service.operation.IndicesDataOperation;
import com.am.marketdata.scraper.service.operation.StockIndicesDataOperation;
import com.am.marketdata.scraper.service.validator.StockIndicesValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Service for processing market data
 * Uses Template Method Pattern via AbstractMarketDataOperation for standardized processing flow
 * Uses Strategy Pattern for different data validators and processors
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataProcessingService {
    
    private final IndicesDataOperation indicesDataOperation;
    private final ETFDataOperation etfDataOperation;
    private final StockIndicesDataOperation stockIndicesDataOperation;
    private final StockIndicesValidator stockIndicesValidator;

    /**
     * Initialize the service
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing market data processing service");
    }

    /**
     * Clean up resources
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down market data processing service");
    }

    /**
     * Scheduled method to fetch and process market data
     * Runs both indices and ETF operations in parallel
     */
    @Scheduled(fixedRate = 5000)
    public void fetchAndProcessMarketData() {
        CompletableFuture<Boolean> indicesFuture = indicesDataOperation.executeAsync();
        CompletableFuture<Boolean> etfFuture = etfDataOperation.executeAsync();
        CompletableFuture<Boolean> stockIndicesFuture = stockIndicesDataOperation.executeAsync();

        try {
            CompletableFuture.allOf(indicesFuture, etfFuture, stockIndicesFuture).get();
            boolean indicesProcessed = indicesFuture.get();
            boolean etfProcessed = etfFuture.get();
            boolean stockIndicesProcessed = stockIndicesFuture.get();

            logProcessingStatus(indicesProcessed, etfProcessed, stockIndicesProcessed);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MarketDataException("Interrupted while fetching market data", e);
        } catch (ExecutionException e) {
            throw new MarketDataException("Error fetching market data", e.getCause());
        }
    }

    /**
     * Log the status of the processing operations
     * 
     * @param indicesProcessed Whether indices were successfully processed
     * @param etfProcessed Whether ETFs were successfully processed
     * @param stockIndicesProcessed Whether stock indices were successfully processed
     */
    private void logProcessingStatus(boolean indicesProcessed, boolean etfProcessed, boolean stockIndicesProcessed) {
        if (!indicesProcessed && !etfProcessed && !stockIndicesProcessed) {
            throw new RuntimeException("Failed to process both indices and ETF data");
        } else if (!indicesProcessed) {
            log.warn("Indices data processing failed but ETF data was processed successfully");
        } else if (!etfProcessed) {
            log.warn("ETF data processing failed but indices data was processed successfully");
        } else if (!stockIndicesProcessed) {
            log.warn("Stock indices data processing failed but indices and ETF data were processed successfully");
        } else {
            log.info("Successfully processed both indices and ETF data and stock indices data");
        }
    }
}
