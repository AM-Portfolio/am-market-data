package com.am.marketdata.api.service;

import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.common.investment.service.StockIndicesMarketDataService;
import com.am.marketdata.scraper.service.MarketDataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockIndicesService {
    
    private final MarketDataProcessingService marketDataProcessingService;
    private final StockIndicesMarketDataService stockIndicesMarketDataService;
    private final MarketDataCacheService marketDataCacheService;
    
    @Value("${market.data.cache.enabled:true}")
    private boolean cacheEnabled;

    public List<StockIndicesMarketData> getLatestIndicesData(List<String> indexSymbols) {
        return getLatestIndicesData(indexSymbols, false);
    }
    
    public List<StockIndicesMarketData> getLatestIndicesData(List<String> indexSymbols, boolean forceRefresh) {
        try {
            // Check if we should use cache
            if (cacheEnabled && !forceRefresh) {
                List<StockIndicesMarketData> cachedData = marketDataCacheService.getStockIndicesData(indexSymbols, false);
                if (cachedData != null) {
                    log.info("Retrieved {} indices from cache (cached={})", cachedData.size(), true);   
                    return cachedData;
                }
            }
            
            // If cache miss or disabled, process fresh data
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
                log.info("Retrieved data for {}/{} symbols (fresh)", result.size(), indexSymbols.size());
            }

            return result;

        } catch (Exception e) {
            log.error("Error processing stock indices request", e);
            return new ArrayList<>();
        }
    }

    public StockIndicesMarketData getLatestIndexData(String indexSymbol) {
        return getLatestIndexData(indexSymbol, false);
    }
    
    public StockIndicesMarketData getLatestIndexData(String indexSymbol, boolean forceRefresh) {
        try {
            // Check if we should use cache
            if (cacheEnabled && !forceRefresh) {
                StockIndicesMarketData cachedData = marketDataCacheService.getStockIndexData(indexSymbol, false);
                if (cachedData != null) {
                    log.info("Retrieved index data for {} from cache (cached={})", indexSymbol, "true");
                    return cachedData;
                }
            }
            
            // If cache miss or disabled, get fresh data
            List<StockIndicesMarketData> data = getLatestIndicesData(List.of(indexSymbol), forceRefresh);
            return data.isEmpty() ? null : data.get(0);
        } catch (Exception e) {
            log.error("Error while fetching stock index data for symbol: {}", indexSymbol, e);
            throw new RuntimeException("Failed to fetch stock index data", e);
        }
    }
}
