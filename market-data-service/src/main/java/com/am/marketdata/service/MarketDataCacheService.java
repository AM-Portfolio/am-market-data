package com.am.marketdata.service;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.model.historical.OHLCVTPoint;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.redis.model.StockBars;
import com.am.marketdata.redis.service.StockCacheService;
import com.am.marketdata.redis.util.CacheLoggingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
public class MarketDataCacheService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataCacheService.class);
    private static final String DEFAULT_INTERVAL = "5m";

    private final StockCacheService stockCacheService;

    public MarketDataCacheService(StockCacheService stockCacheService) {
        this.stockCacheService = stockCacheService;
    }

    public void cacheOHLCData(Map<String, OHLCQuote> ohlcData) {
        try {
            LocalDate today = LocalDate.now();
            Map<String, List<OHLCVTPoint>> symbolPrices = new HashMap<>();
            
            // Convert OHLC quotes to OHLCVTPoint objects
            for (Map.Entry<String, OHLCQuote> entry : ohlcData.entrySet()) {
                String fullSymbol = entry.getKey();
                String symbol = fullSymbol.replace("NSE:", "");
                OHLCQuote quote = entry.getValue();
                
                // Create OHLCVTPoint from OHLCQuote
                OHLCVTPoint point = stockCacheService.createPricePoint(
                    LocalDateTime.now(), 
                    quote.getOhlc().getOpen(), 
                    quote.getOhlc().getHigh(), 
                    quote.getOhlc().getLow(), 
                    quote.getOhlc().getClose(), 
                    0L // Default volume as it might not be available in OHLCQuote
                );
                
                // Add to map
                symbolPrices.computeIfAbsent(symbol, k -> new ArrayList<>()).add(point);
            }
            
            // Process and cache data for each symbol
            if (!symbolPrices.isEmpty()) {
                // Use the specialized cache logging utility
                CacheLoggingUtil.logBatchOHLCCaching(log, symbolPrices, today);
                
                // Process and cache the data
                stockCacheService.processAndCacheMultiSymbolData(symbolPrices, today);
            }
        } catch (Exception e) {
            // Use the specialized exception logging
            CacheLoggingUtil.logCacheException(log, "CACHE_OHLC", null, "Error caching OHLC data", e);
            // Don't rethrow as this is a non-critical operation
        }
    }

    public void cacheHistoricalData(String symbol, TimeFrame timeFrame, HistoricalData historicalData) {
        try {
            if (historicalData == null || historicalData.getDataPoints() == null || historicalData.getDataPoints().isEmpty()) {
                log.warn("No historical data to cache for symbol: {}", symbol);
                return;
            }
            
            // Get the data points directly as OHLCVTPoint objects
            List<OHLCVTPoint> points = (List<OHLCVTPoint>) historicalData.getDataPoints();
            
            // Cache the historical data
            if (!points.isEmpty()) {
                // Use the specialized logging utility
                CacheLoggingUtil.logHistoricalDataCaching(log, symbol, timeFrame.getApiValue(), points);
                
                // For daily data, use the historical bar caching
                if (timeFrame == TimeFrame.DAY || timeFrame == TimeFrame.WEEK || 
                    timeFrame == TimeFrame.MONTH || timeFrame == TimeFrame.YEAR) {
                    
                    // Cache each day's data point individually
                    for (OHLCVTPoint point : points) {
                        LocalDate date = point.getTime().toLocalDate();
                        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                        stockCacheService.cacheHistoricalBar(symbol, dateStr, point, timeFrame);
                    }
                } else {
                    // For intraday data, use the intraday bars caching
                    stockCacheService.cacheIntradayBars(symbol, timeFrame.getApiValue(), points);
                }
            }
        } catch (Exception e) {
            // Use the specialized exception logging
            CacheLoggingUtil.logCacheException(log, "CACHE_HISTORICAL", symbol, 
                "Error caching historical data", e);
            // Don't rethrow as this is a non-critical operation
        }
    }

    public Map<String, OHLCQuote> getOHLCFromCache(List<String> tradingSymbols, TimeFrame timeFrame) {
        try {
            // Clean symbols (remove NSE: prefix if present)
            List<String> cleanSymbols = tradingSymbols.stream()
                .map(symbol -> symbol.replace("NSE:", ""))
                .collect(Collectors.toList());
            
            // Log the cache retrieval operation
            log.debug("Attempting to retrieve OHLC data from cache for {} symbols with timeFrame {}", 
                cleanSymbols.size(), timeFrame.getApiValue());
            
            // Try to get data from cache
            Map<String, StockBars> cachedBars = 
                stockCacheService.getTodayMultiSymbolBars(cleanSymbols, timeFrame.getApiValue());
            
            if (cachedBars == null || cachedBars.isEmpty()) {
                log.debug("No OHLC data found in cache for the requested symbols");
                return Collections.emptyMap();
            }
            
            // Convert cached data to OHLCQuote format
            Map<String, OHLCQuote> result = new HashMap<>();
            Map<String, String> cacheHits = new HashMap<>();
            
            for (Map.Entry<String, StockBars> entry : cachedBars.entrySet()) {
                String symbol = entry.getKey();
                StockBars bars = entry.getValue();
                
                if (bars != null && bars.getBars() != null && !bars.getBars().isEmpty()) {
                    // Get the latest bar
                    OHLCVTPoint latestBar = bars.getBars().get(bars.getBars().size() - 1);
                    
                    // Create OHLCQuote from the latest bar
                    OHLCQuote quote = createOHLCQuoteFromBar(latestBar);
                    result.put("NSE:" + symbol, quote);
                    
                    // Record the cache hit for logging
                    cacheHits.put(symbol, String.format("O:%.2f,H:%.2f,L:%.2f,C:%.2f", 
                        latestBar.getOpen(), latestBar.getHigh(), latestBar.getLow(), latestBar.getClose()));
                }
            }
            
            if (!result.isEmpty()) {
                // Log the cache hits with values
                log.info("Retrieved OHLC data from cache for {} symbols with values: {}", 
                    result.size(), cacheHits);
            }
            
            return result;
        } catch (Exception e) {
            // Use the specialized exception logging
            CacheLoggingUtil.logCacheException(log, "GET_OHLC_CACHE", String.join(", ", tradingSymbols), 
                "Error retrieving OHLC data from cache", e);
            return Collections.emptyMap();
        }
    }

    public HistoricalData getHistoricalDataFromCache(String symbol, TimeFrame timeFrame, String fromDate, String toDate) {
        try {
            // Log the cache retrieval attempt
            log.debug("Attempting to retrieve historical data from cache for symbol: {} with timeFrame: {} from: {} to: {}", 
                symbol, timeFrame.getApiValue(), fromDate, toDate);
            
            // Parse dates
            LocalDate from = LocalDate.parse(fromDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate to = LocalDate.parse(toDate, DateTimeFormatter.ISO_LOCAL_DATE);
            
            // For daily data
            if (timeFrame == TimeFrame.DAY || timeFrame == TimeFrame.WEEK || timeFrame == TimeFrame.MONTH || timeFrame == TimeFrame.YEAR) {
                // Get historical bars for each day in the range
                List<OHLCVTPoint> points = new ArrayList<>();
                Map<String, String> cacheHits = new HashMap<>();
                
                // Iterate through each day in the range
                LocalDate current = from;
                while (!current.isAfter(to)) {
                    String dateStr = current.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    String cacheKey = String.format("stock:historical:%s:%s:%s", 
                        symbol.toUpperCase(), timeFrame.getApiValue(), dateStr);
                    
                    StockBars stockBars = stockCacheService.getBarsWithStats(symbol, timeFrame.getApiValue(), dateStr);
                    OHLCVTPoint bar = null;
                    if (stockBars != null && stockBars.getBars() != null && !stockBars.getBars().isEmpty()) {
                        bar = stockBars.getBars().get(0);
                        
                        // Record the cache hit for logging
                        cacheHits.put(cacheKey, String.format("O:%.2f,H:%.2f,L:%.2f,C:%.2f", 
                            bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose()));
                    }
                    
                    if (bar != null) {
                        points.add(bar);
                    }
                    
                    current = current.plusDays(1);
                }
                
                if (!points.isEmpty()) {
                    // Log the cache hits
                    log.info("Retrieved {} historical data points from cache for symbol: {} with values: {}", 
                        points.size(), symbol, cacheHits);
                    
                    return convertToHistoricalData(symbol, points);
                }
            } else {
                // For intraday data
                // Get intraday bars for the specified interval
                String dateStr = from.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String cacheKey = String.format("stock:intraday:%s:%s:%s", 
                    symbol.toUpperCase(), timeFrame.getApiValue(), dateStr);
                
                StockBars stockBars = stockCacheService.getBarsWithStats(symbol, timeFrame.getApiValue(), dateStr);
                List<OHLCVTPoint> bars = (stockBars != null) ? stockBars.getBars() : null;
                
                if (bars != null && !bars.isEmpty()) {
                    // Log the cache hit
                    log.info("Retrieved {} intraday data points from cache for symbol: {} with key: {}", 
                        bars.size(), symbol, cacheKey);
                    
                    // Log detailed data at debug level
                    if (log.isDebugEnabled()) {
                        for (OHLCVTPoint bar : bars) {
                            log.debug("Retrieved data point: time={}, open={}, high={}, low={}, close={}, volume={}",
                                bar.getTime(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume());
                        }
                    }
                    
                    return convertToHistoricalData(symbol, bars);
                }
            }
            
            log.debug("No historical data found in cache for symbol: {} with timeFrame: {}", symbol, timeFrame.getApiValue());
            return null;
        } catch (Exception e) {
            // Use the specialized exception logging
            CacheLoggingUtil.logCacheException(log, "GET_HISTORICAL_CACHE", symbol, 
                "Error retrieving historical data from cache", e);
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
    private HistoricalData convertToHistoricalData(String symbol, List<OHLCVTPoint> points) {
        HistoricalData historicalData = new HistoricalData();
        historicalData.setTradingSymbol(symbol);
        
        // Set the OHLCVTPoint list directly as dataPoints
        historicalData.setDataPoints(points);
        
        // No need for additional logging here as the calling methods already log the details
        
        return historicalData;
    }

    /**
     * Create an OHLCQuote object from an OHLCVTPoint
     *
     * @param bar The OHLCVTPoint bar
     * @return OHLCQuote object
     */
    private OHLCQuote createOHLCQuoteFromBar(OHLCVTPoint bar) {
        // Create a new OHLCQuote object
        OHLCQuote quote = new OHLCQuote();
        
        // Create and set the OHLC object
        OHLCQuote.OHLC ohlc = new OHLCQuote.OHLC();
        ohlc.setOpen(bar.getOpen());
        ohlc.setHigh(bar.getHigh());
        ohlc.setLow(bar.getLow());
        ohlc.setClose(bar.getClose());
        
        // Set the OHLC and last price in the quote
        quote.setOhlc(ohlc);
        quote.setLastPrice(bar.getClose()); // Set last price to close price
        
        // Log at debug level
        if (log.isDebugEnabled()) {
            log.debug("Converted OHLC data point: time={}, O={}, H={}, L={}, C={}", 
                bar.getTime(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());
        }
        
        return quote;
    }
}
