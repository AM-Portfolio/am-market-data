package com.am.marketdata.redis.service;

import com.am.marketdata.redis.cache.StockRedisCache;
import com.am.common.investment.model.historical.OHLCVTPoint;
import com.am.marketdata.redis.model.StockBars;
import com.am.marketdata.redis.util.BarCalculatorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing symbol-specific stock price data in Redis cache.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockCacheSymbolService {

    private final StockRedisCache stockRedisCache;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    // Cache hit statistics (for monitoring purposes)
    private final Map<String, Integer> cacheHits = new ConcurrentHashMap<>();
    private final Map<String, Integer> cacheMisses = new ConcurrentHashMap<>();

    /**
     * Cache intraday bars for a symbol with individual parameters
     */
    public boolean cacheIntradayBars(String symbol, String interval, String date, List<OHLCVTPoint> bars) {
        StockBars stockBars = StockBars.builder()
                .symbol(symbol)
                .interval(interval)
                .startDate(date)
                .bars(bars)
                .build();
        return cacheIntradayBars(List.of(stockBars));
    }
    
    /**
     * Cache intraday bars for a symbol using StockBars list
     */
    public boolean cacheIntradayBars(List<StockBars> stockBarsList) {
        return stockRedisCache.saveIntradayBars(stockBarsList);
    }
    

    /**
     * Get bars for a symbol with cache statistics tracking
     */
    public StockBars getBarsWithStats(String symbol, String interval, String date) {
        String cacheKey = symbol + ":" + interval + ":" + date;
        StockBars result = stockRedisCache.getBars(symbol, interval, date);
        
        if (result != null) {
            // Cache hit
            cacheHits.compute(cacheKey, (k, v) -> (v == null) ? 1 : v + 1);
        } else {
            // Cache miss
            cacheMisses.compute(cacheKey, (k, v) -> (v == null) ? 1 : v + 1);
        }
        
        return result;
    }

    /**
     * Get today's intraday bars for a symbol
     */
    public StockBars getTodayIntradayBars(String symbol, String interval) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return getBarsWithStats(symbol, interval, today);
    }
    
    /**
     * Get bars for multiple symbols with cache statistics tracking
     */
    public Map<String, StockBars> getMultiSymbolBarsWithStats(List<String> symbols, String interval, String date) {
        // Use Redis's multi-key operation for better performance
        Map<String, StockBars> result = stockRedisCache.getMultiSymbolBars(symbols, interval, date);
        
        // Update cache hit/miss statistics
        for (String symbol : symbols) {
            String cacheKey = symbol + ":" + interval + ":" + date;
            if (result.containsKey(symbol)) {
                // Cache hit
                cacheHits.compute(cacheKey, (k, v) -> (v == null) ? 1 : v + 1);
            } else {
                // Cache miss
                cacheMisses.compute(cacheKey, (k, v) -> (v == null) ? 1 : v + 1);
            }
        }
        
        return result;
    }
    
    /**
     * Get today's bars for multiple symbols
     */
    public Map<String, StockBars> getTodayMultiSymbolBars(List<String> symbols, String interval) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return getMultiSymbolBarsWithStats(symbols, interval, today);
    }
    
    /**
     * Process intraday bars for a symbol and interval
     */
    public boolean processIntradayBars(String symbol, String interval, List<OHLCVTPoint> rawPrices, LocalDate date) {
        if (rawPrices == null || rawPrices.isEmpty()) {
            log.warn("No raw price data provided for symbol: {} and interval: {}", symbol, interval);
            return false;
        }
        
        try {
            BarCalculatorUtil.validateInterval(interval);
            
            // Sort prices by timestamp
            rawPrices.sort(Comparator.comparing(OHLCVTPoint::getTime));
            
            // Group prices by interval
            int minutes = BarCalculatorUtil.INTERVAL_MINUTES.get(interval);
            Map<LocalDateTime, List<OHLCVTPoint>> groupedPrices = 
                    BarCalculatorUtil.groupPricesByInterval(rawPrices, minutes, date);
            
            // Create bars for each interval
            List<OHLCVTPoint> bars = new ArrayList<>();
            for (Map.Entry<LocalDateTime, List<OHLCVTPoint>> entry : groupedPrices.entrySet()) {
                LocalDateTime barTime = entry.getKey();
                List<OHLCVTPoint> barPrices = entry.getValue();
                
                if (!barPrices.isEmpty()) {
                    bars.add(BarCalculatorUtil.createBar(barPrices, barTime));
                }
            }
            
            if (!bars.isEmpty()) {
                String dateStr = BarCalculatorUtil.formatDate(date);
                boolean success = stockRedisCache.saveIntradayBars(symbol, interval, dateStr, bars);
                log.debug("Processed and cached {} bars for {}:{} on {}", bars.size(), symbol, interval, dateStr);
                return success;
            }
        } catch (Exception e) {
            log.error("Error processing {} interval for {}: {}", interval, symbol, e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get cache hit statistics
     */
    public Map<String, Integer> getCacheHitStats() {
        return new ConcurrentHashMap<>(cacheHits);
    }

    /**
     * Get cache miss statistics
     */
    public Map<String, Integer> getCacheMissStats() {
        return new ConcurrentHashMap<>(cacheMisses);
    }

    /**
     * Reset cache statistics
     */
    public void resetCacheStats() {
        cacheHits.clear();
        cacheMisses.clear();
    }
}
