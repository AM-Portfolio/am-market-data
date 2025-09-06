package com.am.marketdata.redis.service;

import com.am.marketdata.common.model.TimeFrame;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing stock price data in Redis cache.
 * Provides higher-level operations and additional functionality on top of the StockRedisCache.
 * Delegates to specialized services for historical data, symbol-specific operations, and bar calculation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockCacheService {

    private final StockRedisCache stockRedisCache;
    private final BarCalculator barCalculator;
    private final StockCacheHistoricalService historicalService;
    private final StockCacheSymbolService symbolService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Cache intraday bars for a symbol
     */
    public boolean cacheIntradayBars(String symbol, String interval, List<OHLCVTPoint> bars) {
        // Create StockBars object for the symbol service
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        StockBars stockBars = StockBars.builder()
                .symbol(symbol)
                .interval(interval)
                .startDate(dateStr)
                .bars(bars)
                .build();
        return symbolService.cacheIntradayBars(List.of(stockBars));
    }

    /**
     * Cache a historical bar for a symbol and date
     */
    public boolean cacheHistoricalBar(String symbol, String date, OHLCVTPoint bar, TimeFrame timeFrame) {
        // Create StockBars object for the historical service
        StockBars stockBars = StockBars.builder()
                .symbol(symbol)
                .interval(timeFrame.getApiValue())
                .startDate(date)
                .bars(List.of(bar))
                .build();
        return historicalService.cacheHistoricalBar(List.of(stockBars));
    }

    /**
     * Get bars for a symbol with cache statistics tracking
     */
    public StockBars getBarsWithStats(String symbol, String interval, String date) {
        return symbolService.getBarsWithStats(symbol, interval, date);
    }

    /**
     * Get today's intraday bars for a symbol
     */
    public StockBars getTodayIntradayBars(String symbol, String interval) {
        return symbolService.getTodayIntradayBars(symbol, interval);
    }

    /**
     * Clear intraday data for a specific date
     */
    public long clearIntradayDataForDate(String date) {
        return stockRedisCache.clearIntradayDataForDate(date);
    }

    /**
     * Clear today's intraday data
     */
    public long clearTodayIntradayData() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return clearIntradayDataForDate(today);
    }

    /**
     * Get combined cache hit statistics from all services
     */
    public Map<String, Integer> getCacheHitStats() {
        Map<String, Integer> combinedStats = new ConcurrentHashMap<>();
        combinedStats.putAll(historicalService.getCacheHitStats());
        combinedStats.putAll(symbolService.getCacheHitStats());
        return combinedStats;
    }

    /**
     * Get combined cache miss statistics from all services
     */
    public Map<String, Integer> getCacheMissStats() {
        Map<String, Integer> combinedStats = new ConcurrentHashMap<>();
        combinedStats.putAll(historicalService.getCacheMissStats());
        combinedStats.putAll(symbolService.getCacheMissStats());
        return combinedStats;
    }

    /**
     * Reset cache statistics in all services
     */
    public void resetCacheStats() {
        historicalService.resetCacheStats();
        symbolService.resetCacheStats();
    }
    
    /**
     * Get bars for multiple symbols with cache statistics tracking
     */
    public Map<String, StockBars> getMultiSymbolBarsWithStats(List<String> symbols, String interval, String date) {
        return symbolService.getMultiSymbolBarsWithStats(symbols, interval, date);
    }
    
    /**
     * Get today's bars for multiple symbols
     */
    public Map<String, StockBars> getTodayMultiSymbolBars(List<String> symbols, String interval) {
        return symbolService.getTodayMultiSymbolBars(symbols, interval);
    }
    
    /**
     * Get historical bars for multiple symbols within a date range
     */
    public Map<String, List<StockBars>> getHistoricalBarsWithStats(List<String> symbols, String startDate, String endDate, String interval) {
        return historicalService.getHistoricalBarsWithStats(symbols, startDate, endDate, interval);
    }


    
    /**
     * Process raw price data into interval-based bars and cache them
     * 
     * @param symbol The stock symbol
     * @param rawPrices List of raw price points with timestamp, price, and volume
     * @param date The date for which to calculate bars
     * @return Map of interval to success status
     */
    public Map<String, Boolean> processAndCacheRawPriceData(String symbol, List<OHLCVTPoint> rawPrices, LocalDate date) {
        if (rawPrices == null || rawPrices.isEmpty()) {
            log.warn("No raw price data provided for symbol: {}", symbol);
            return Map.of();
        }
        
        Map<String, Boolean> result = new HashMap<>();
        
        try {
            // Process and cache intraday bars (5m, 15m, 30m, 1h, 4h)
            for (String interval : List.of("5m", "15m", "30m", "1h", "4h")) {
                List<OHLCVTPoint> bars = barCalculator.calculateBars(rawPrices, interval, date);
                if (!bars.isEmpty()) {
                    // Create StockBars object for the symbol service
                    StockBars stockBars = StockBars.builder()
                            .symbol(symbol)
                            .interval(interval)
                            .startDate(date.format(DATE_FORMATTER))
                            .bars(bars)
                            .build();
                    boolean success = symbolService.cacheIntradayBars(List.of(stockBars));
                    result.put(interval, success);
                    log.debug("Processed and cached {} {} bars for {} on {}", bars.size(), interval, symbol, date);
                }
            }
            
            // Process and cache daily bar
            List<OHLCVTPoint> dailyBars = barCalculator.calculateBars(rawPrices, "1d", date);
            if (!dailyBars.isEmpty()) {
                // Create StockBars object for the historical service
                StockBars stockBars = StockBars.builder()
                        .symbol(symbol)
                        .interval("1d")
                        .startDate(date.format(DATE_FORMATTER))
                        .bars(List.of(dailyBars.get(0)))
                        .build();
                boolean success = historicalService.cacheHistoricalBar(List.of(stockBars));
                result.put("1d", success);
                log.debug("Processed and cached daily bar for {} on {}", symbol, date);
            }
            
        } catch (Exception e) {
            log.error("Error processing raw price data for {}: {}", symbol, e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Process raw price data for multiple symbols and cache them
     * 
     * @param symbolPrices Map of symbol to raw price points
     * @param date The date for which to calculate bars
     * @return Map of symbol to interval results
     */
    public Map<String, Map<String, Boolean>> processAndCacheMultiSymbolData(
            Map<String, List<OHLCVTPoint>> symbolPrices, LocalDate date) {
        
        Map<String, Map<String, Boolean>> result = new HashMap<>();
        
        for (Map.Entry<String, List<OHLCVTPoint>> entry : symbolPrices.entrySet()) {
            String symbol = entry.getKey();
            List<OHLCVTPoint> prices = entry.getValue();
            
            Map<String, Boolean> symbolResult = processAndCacheRawPriceData(symbol, prices, date);
            result.put(symbol, symbolResult);
        }
        
        return result;
    }
    
    
    public OHLCVTPoint createPricePoint(LocalDateTime timestamp, double price, long volume) {
        return OHLCVTPoint.builder()
                .time(timestamp)
                .open(price)
                .high(price)
                .low(price)
                .close(price)
                .volume(volume)
                .build();
    }

    public OHLCVTPoint createPricePoint(LocalDateTime timestamp, double open, double high, double low, double close, long volume) {
        return OHLCVTPoint.builder()
                .time(timestamp)
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .volume(volume)
                .build();
    }
}
