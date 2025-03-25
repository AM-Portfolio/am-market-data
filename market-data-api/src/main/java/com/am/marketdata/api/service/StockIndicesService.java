package com.am.marketdata.api.service;

import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.common.investment.service.StockIndicesMarketDataService;
import com.am.marketdata.scraper.service.MarketDataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockIndicesService {
    
    private final MarketDataProcessingService marketDataProcessingService;
    private final StockIndicesMarketDataService stockIndicesMarketDataService;

    public List<StockIndicesMarketData> getLatestIndicesData(List<String> indexSymbols) {
        try {
            // Process each symbol individually in parallel
            List<CompletableFuture<Boolean>> futures = indexSymbols.stream()
                .map(symbol -> marketDataProcessingService.fetchAndProcessStockIndices(symbol)
                    .exceptionally(e -> {
                        log.error("Error fetching data for symbol: {}", symbol, e);
                        return false;
                    }))
                .collect(Collectors.toList());

            // Wait for all futures to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // Add a small delay to ensure data is persisted
            TimeUnit.SECONDS.sleep(1);

            // Return the data from database (either fresh or last known)
            List<StockIndicesMarketData> result = indexSymbols.stream()
                .map(symbol -> {
                    try {
                        return stockIndicesMarketDataService.findByIndexSymbol(symbol);
                    } catch (Exception e) {
                        log.error("Error retrieving data for symbol: {}", symbol, e);
                        return null;
                    }
                })
                .filter(data -> data != null)
                .collect(Collectors.toList());

            if (result.isEmpty()) {
                log.warn("No data found for any of the requested symbols: {}", indexSymbols);
            } else {
                log.info("Retrieved data for {}/{} symbols", result.size(), indexSymbols.size());
            }

            return result;

        } catch (Exception e) {
            log.error("Error processing stock indices request", e);
            return new ArrayList<>();
        }
    }

    public StockIndicesMarketData getLatestIndexData(String indexSymbol) {
        try {
            List<StockIndicesMarketData> data = getLatestIndicesData(List.of(indexSymbol));
            return data.isEmpty() ? null : data.get(0);
        } catch (Exception e) {
            log.error("Error while fetching stock index data for symbol: {}", indexSymbol, e);
            throw new RuntimeException("Failed to fetch stock index data", e);
        }
    }
}
