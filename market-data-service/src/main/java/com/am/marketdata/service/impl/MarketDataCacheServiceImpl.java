package com.am.marketdata.service.impl;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.marketdata.redis.model.StockBars;
import com.am.marketdata.redis.service.StockCacheService;
import com.am.marketdata.service.MarketDataCacheService;
import com.zerodhatech.models.OHLCQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of MarketDataCacheService for caching market data in Redis
 */
@Service("serviceModuleMarketDataCacheService")
public class MarketDataCacheServiceImpl implements MarketDataCacheService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataCacheServiceImpl.class);
    private static final String DEFAULT_INTERVAL = "5m";

    private final StockCacheService stockCacheService;

    public MarketDataCacheServiceImpl(StockCacheService stockCacheService) {
        this.stockCacheService = stockCacheService;
    }

    @Override
    public void cacheOHLCData(Map<String, OHLCQuote> ohlcData) {
        try {
            LocalDate today = LocalDate.now();
            Map<String, List<com.am.common.investment.model.historical.OHLCVTPoint>> symbolPrices = new HashMap<>();
            
            // Convert OHLC quotes to OHLCVTPoint objects
            for (Map.Entry<String, OHLCQuote> entry : ohlcData.entrySet()) {
                String fullSymbol = entry.getKey();
                String symbol = fullSymbol.replace("NSE:", "");
                OHLCQuote quote = entry.getValue();
                
                // Create OHLCVTPoint from OHLCQuote
                com.am.common.investment.model.historical.OHLCVTPoint point = 
                    stockCacheService.createPricePoint(
                        LocalDateTime.now(), 
                        quote.ohlc.open, 
                        quote.ohlc.high, 
                        quote.ohlc.low, 
                        quote.ohlc.close, 
                        0L // Default volume as it might not be available in OHLCQuote
                    );
                
                // Add to map
                symbolPrices.computeIfAbsent(symbol, k -> new ArrayList<>()).add(point);
            }
            
            // Process and cache data for each symbol
            if (!symbolPrices.isEmpty()) {
                log.info("Caching OHLC data for {} symbols", symbolPrices.size());
                stockCacheService.processAndCacheMultiSymbolData(symbolPrices, today);
                log.info("Successfully cached OHLC data");
            }
        } catch (Exception e) {
            log.error("Error caching OHLC data: {}", e.getMessage(), e);
            // Don't rethrow as this is a non-critical operation
        }
    }

    @Override
    public void cacheHistoricalData(String symbol, String interval, HistoricalData historicalData) {
        try {
            if (historicalData == null || historicalData.getDataPoints() == null || historicalData.getDataPoints().isEmpty()) {
                log.warn("No historical data to cache for symbol: {}", symbol);
                return;
            }
            
            log.info("Caching historical data for symbol: {} with interval: {}", symbol, interval);
            
            // Get the data points directly as OHLCVTPoint objects
            List<com.am.common.investment.model.historical.OHLCVTPoint> points = 
                (List<com.am.common.investment.model.historical.OHLCVTPoint>) historicalData.getDataPoints();
            
            // Cache the historical data
            if (!points.isEmpty()) {
                // For daily data, use the historical bar caching
                if ("1d".equals(interval) || "day".equals(interval)) {
                    // Cache each day's data point individually
                    for (com.am.common.investment.model.historical.OHLCVTPoint point : points) {
                        LocalDate date = point.getTime().toLocalDate();
                        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                        stockCacheService.cacheHistoricalBar(symbol, dateStr, point);
                    }
                    log.info("Successfully cached {} daily historical bars for {}", points.size(), symbol);
                } else {
                    // For intraday data, use the intraday bars caching
                    boolean success = stockCacheService.cacheIntradayBars(symbol, interval, points);
                    log.info("Cached intraday historical data for {} with status: {}", symbol, success);
                }
                log.info("Successfully cached historical data for {}", symbol);
            }
        } catch (Exception e) {
            log.error("Error caching historical data for symbol {}: {}", symbol, e.getMessage(), e);
            // Don't rethrow as this is a non-critical operation
        }
    }

    @Override
    public Map<String, OHLCQuote> getOHLCFromCache(List<String> tradingSymbols) {
        try {
            // Clean symbols (remove NSE: prefix if present)
            List<String> cleanSymbols = tradingSymbols.stream()
                .map(symbol -> symbol.replace("NSE:", ""))
                .collect(Collectors.toList());
            
            // Try to get data from cache
            Map<String, StockBars> cachedBars = 
                stockCacheService.getTodayMultiSymbolBars(cleanSymbols, DEFAULT_INTERVAL);
            
            if (cachedBars == null || cachedBars.isEmpty()) {
                return Collections.emptyMap();
            }
            
            // Convert cached data to OHLCQuote format
            Map<String, OHLCQuote> result = new HashMap<>();
            
            for (Map.Entry<String, StockBars> entry : cachedBars.entrySet()) {
                String symbol = entry.getKey();
                StockBars bars = entry.getValue();
                
                if (bars != null && bars.getBars() != null && !bars.getBars().isEmpty()) {
                    // Get the latest bar
                    com.am.common.investment.model.historical.OHLCVTPoint latestBar = 
                        bars.getBars().get(bars.getBars().size() - 1);
                    
                    // Create OHLCQuote from the latest bar
                    OHLCQuote quote = createOHLCQuoteFromBar(latestBar);
                    result.put("NSE:" + symbol, quote);
                }
            }
            
            if (!result.isEmpty()) {
                log.info("Retrieved OHLC data from cache for {} symbols", result.size());
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error retrieving OHLC data from cache: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public HistoricalData getHistoricalDataFromCache(String symbol, String interval, String fromDate, String toDate) {
        try {
            // Parse dates
            LocalDate from = LocalDate.parse(fromDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate to = LocalDate.parse(toDate, DateTimeFormatter.ISO_LOCAL_DATE);
            
            // For daily data
            if ("1d".equals(interval) || "day".equals(interval)) {
                // Get historical bars for each day in the range
                List<com.am.common.investment.model.historical.OHLCVTPoint> points = new ArrayList<>();
                
                // Iterate through each day in the range
                LocalDate current = from;
                while (!current.isAfter(to)) {
                    String dateStr = current.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    StockBars stockBars = stockCacheService.getBarsWithStats(symbol, "1d", dateStr);
                    com.am.common.investment.model.historical.OHLCVTPoint bar = null;
                    if (stockBars != null && stockBars.getBars() != null && !stockBars.getBars().isEmpty()) {
                        bar = stockBars.getBars().get(0);
                    }
                    
                    if (bar != null) {
                        points.add(bar);
                    }
                    
                    current = current.plusDays(1);
                }
                
                if (!points.isEmpty()) {
                    return convertToHistoricalData(symbol, points);
                }
            } else {
                // For intraday data
                // Get intraday bars for the specified interval
                String dateStr = from.format(DateTimeFormatter.ISO_LOCAL_DATE);
                StockBars stockBars = stockCacheService.getBarsWithStats(symbol, interval, dateStr);
                List<com.am.common.investment.model.historical.OHLCVTPoint> bars = 
                    (stockBars != null) ? stockBars.getBars() : null;
                
                if (bars != null && !bars.isEmpty()) {
                    return convertToHistoricalData(symbol, bars);
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error retrieving historical data from cache for symbol {}: {}", symbol, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert OHLCVTPoint list to HistoricalData
     *
     * @param symbol The trading symbol
     * @param points List of OHLCVTPoint objects
     * @return HistoricalData object
     */
    private HistoricalData convertToHistoricalData(String symbol, List<com.am.common.investment.model.historical.OHLCVTPoint> points) {
        HistoricalData historicalData = new HistoricalData();
        historicalData.setTradingSymbol(symbol);
        
        // Set the OHLCVTPoint list directly as dataPoints
        historicalData.setDataPoints(points);
        
        if (!points.isEmpty()) {
            log.info("Retrieved historical data from cache for symbol {} with {} data points", symbol, points.size());
        }
        
        return historicalData;
    }

    /**
     * Create an OHLCQuote object from an OHLCVTPoint
     *
     * @param bar The OHLCVTPoint bar
     * @return OHLCQuote object
     */
    private OHLCQuote createOHLCQuoteFromBar(com.am.common.investment.model.historical.OHLCVTPoint bar) {
        // Create a new OHLCQuote object
        OHLCQuote quote = new OHLCQuote();
        
        // Create the OHLC inner object - using reflection to set fields since OHLC is an inner class
        try {
            // Create OHLC instance
            Class<?> ohlcClass = Class.forName("com.zerodhatech.models.OHLCQuote$OHLC");
            Object ohlcInstance = ohlcClass.getDeclaredConstructor().newInstance();
            
            // Set fields using reflection
            Field openField = ohlcClass.getDeclaredField("open");
            openField.setAccessible(true);
            openField.set(ohlcInstance, bar.getOpen());
            
            Field highField = ohlcClass.getDeclaredField("high");
            highField.setAccessible(true);
            highField.set(ohlcInstance, bar.getHigh());
            
            Field lowField = ohlcClass.getDeclaredField("low");
            lowField.setAccessible(true);
            lowField.set(ohlcInstance, bar.getLow());
            
            Field closeField = ohlcClass.getDeclaredField("close");
            closeField.setAccessible(true);
            closeField.set(ohlcInstance, bar.getClose());
            
            // Set the ohlc field in the quote
            Field ohlcField = OHLCQuote.class.getDeclaredField("ohlc");
            ohlcField.setAccessible(true);
            ohlcField.set(quote, ohlcInstance);
            
        } catch (Exception e) {
            log.error("Error creating OHLCQuote from bar: {}", e.getMessage(), e);
        }
        
        return quote;
    }
}
