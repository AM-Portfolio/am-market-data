package com.am.marketdata.service.impl;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.service.EquityService;
import com.am.common.investment.service.historical.HistoricalDataService;
import com.am.marketdata.mapper.OHLCMapper;
import com.am.marketdata.service.MarketDataCacheService;
import com.am.marketdata.service.MarketDataPersistenceService;
import com.zerodhatech.models.OHLCQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of MarketDataPersistenceService
 * Handles saving and retrieving market data from both database and cache
 */
@Slf4j
@Service
public class MarketDataPersistenceServiceImpl implements MarketDataPersistenceService {

    private final HistoricalDataService historicalDataService;
    private final MarketDataCacheService marketDataCacheService;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final EquityService equityService;
    private final OHLCMapper ohlcMapper;

    public MarketDataPersistenceServiceImpl(
            HistoricalDataService historicalDataService,
            MarketDataCacheService marketDataCacheService,
            EquityService equityService,
            OHLCMapper ohlcMapper,
            @Qualifier("marketDataPersistenceExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.historicalDataService = historicalDataService;
        this.marketDataCacheService = marketDataCacheService;
        this.equityService = equityService;
        this.ohlcMapper = ohlcMapper;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public CompletableFuture<Void> saveOHLCData(Map<String, OHLCQuote> ohlcData) {
        if (ohlcData == null || ohlcData.isEmpty()) {
            log.warn("No OHLC data to save");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                // First save to database using EquityService
                log.debug("Saving {} OHLC data points to database", ohlcData.size());
                List<EquityPrice> equityPrices = ohlcMapper.toEquityPriceList(ohlcData);
                equityService.saveAllPrices(equityPrices);
                log.debug("Successfully saved {} equity prices to database", equityPrices.size());
                
                // Then update the cache
                marketDataCacheService.cacheOHLCData(ohlcData);
                log.debug("Successfully cached OHLC data for {} symbols", ohlcData.size());
            } catch (Exception e) {
                log.error("Error saving OHLC data: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to save OHLC data", e);
            }
        }, taskExecutor);
    }

    @Override
    public CompletableFuture<Void> saveHistoricalData(String symbol, String interval, HistoricalData historicalData) {
        if (historicalData == null || historicalData.getDataPoints() == null || historicalData.getDataPoints().isEmpty()) {
            log.warn("No historical data to save for symbol: {}", symbol);
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                // First save to database
                log.debug("Saving historical data to database for symbol: {}", symbol);
                historicalDataService.saveHistoricalData(historicalData);
                log.debug("Successfully saved historical data for symbol: {}", symbol);
                
                // Then update the cache
                marketDataCacheService.cacheHistoricalData(symbol, interval, historicalData);
                log.debug("Successfully cached historical data for symbol: {}", symbol);
            } catch (Exception e) {
                log.error("Error saving historical data for symbol {}: {}", symbol, e.getMessage(), e);
                throw new RuntimeException("Failed to save historical data", e);
            }
        }, taskExecutor);
    }

    @Override
    public Map<String, OHLCQuote> getOHLCData(List<String> tradingSymbols) {
        if (tradingSymbols == null || tradingSymbols.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            // First try to get from cache
            Map<String, OHLCQuote> cachedData = marketDataCacheService.getOHLCFromCache(tradingSymbols);
            if (!cachedData.isEmpty()) {
                log.debug("Retrieved OHLC data from cache for {} symbols", cachedData.size());
                return cachedData;
            }
            
            // If not in cache, try to get from database
            log.debug("No OHLC data found in cache, fetching from database for {} symbols", tradingSymbols.size());
            
            // Clean symbols (remove NSE: prefix if present)
            List<String> cleanSymbols = tradingSymbols.stream()
                .map(symbol -> symbol.replace("NSE:", ""))
                .collect(Collectors.toList());
            
            // Get equity prices from database
            List<EquityPrice> equityPrices = equityService.getPricesByTradingSymbols(cleanSymbols);
            
            if (equityPrices.isEmpty()) {
                log.debug("No OHLC data found in database for the requested symbols");
                return Collections.emptyMap();
            }
            
            // Convert equity prices to OHLCQuote format
            Map<String, OHLCQuote> result = new HashMap<>();
            for (EquityPrice price : equityPrices) {
                OHLCQuote quote = createOHLCQuoteFromEquityPrice(price);
                result.put("NSE:" + price.getSymbol(), quote);
            }
            
            log.debug("Retrieved OHLC data from database for {} symbols", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error retrieving OHLC data: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Maps the interval format from API format to database format
     * 
     * @param apiInterval Interval in API format (e.g., "1d", "day", "1m", "minute")
     * @return Interval in database format
     */
    private String mapIntervalFormat(String apiInterval) {
        if (apiInterval == null || apiInterval.isEmpty()) {
            return "day"; // Default to daily interval
        }
        
        // Convert to lowercase for case-insensitive comparison
        String interval = apiInterval.toLowerCase();
        
        // Map common interval formats
        if (interval.contains("day") || interval.equals("1d") || interval.equals("d")) {
            return "day";
        } else if (interval.contains("week") || interval.equals("1w") || interval.equals("w")) {
            return "week";
        } else if (interval.contains("month") || interval.equals("1m") || interval.equals("m")) {
            return "month";
        } else if (interval.contains("minute") || interval.contains("min")) {
            // Extract number of minutes if specified (e.g., "5minute" -> "5minute")
            if (interval.matches("\\d+.*")) {
                String minutes = interval.replaceAll("[^\\d]", "");
                return minutes + "minute";
            }
            return "minute";
        } else if (interval.contains("hour") || interval.contains("hr") || interval.equals("1h") || interval.equals("h")) {
            return "hour";
        }
        
        // Return as is if no mapping found
        return interval;
    }
    
    /**
     * Creates an OHLCQuote object from an EquityPrice object
     * 
     * @param price EquityPrice object
     * @return OHLCQuote object
     */
    private OHLCQuote createOHLCQuoteFromEquityPrice(EquityPrice price) {
        OHLCQuote quote = new OHLCQuote();
        quote.ohlc.open = price.getOpen();
        quote.ohlc.high = price.getHigh();
        quote.ohlc.low = price.getLow();
        quote.ohlc.close = price.getClose();
        quote.lastPrice = price.getClose(); // Set last price to close price
        
        // Set additional fields if available
        // if (price.getVolume() != null) {
        //     quote.ohlc. = price.getVolume().intValue();
        // }
        
        return quote;
    }
    
    @Override
    public HistoricalData getHistoricalData(String symbol, String interval, String fromDate, String toDate) {
        if (symbol == null || symbol.isEmpty() || interval == null || interval.isEmpty()) {
            return null;
        }

        try {
            // First try to get from cache
            HistoricalData cachedData = marketDataCacheService.getHistoricalDataFromCache(
                symbol, interval, fromDate, toDate);
                
            if (cachedData != null && cachedData.getDataPoints() != null && !cachedData.getDataPoints().isEmpty()) {
                log.debug("Retrieved historical data from cache for symbol: {}", symbol);
                return cachedData;
            }
            
            // If not in cache, try to get from database
            log.debug("No historical data found in cache, fetching from database for symbol: {}", symbol);
            
            // Clean symbol (remove NSE: prefix if present)
            String cleanSymbol = symbol.replace("NSE:", "");
            
            // Parse dates - handle potential format variations
            LocalDate from;
            LocalDate to;
            try {
                from = LocalDate.parse(fromDate, DateTimeFormatter.ISO_LOCAL_DATE);
                to = LocalDate.parse(toDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                // Try alternative format if ISO format fails
                DateTimeFormatter alternativeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                from = LocalDate.parse(fromDate, alternativeFormatter);
                to = LocalDate.parse(toDate, alternativeFormatter);
            }
            
            // Map interval to the format expected by the database service
            String mappedInterval = mapIntervalFormat(interval);
            
            // Get historical data from database using HistoricalDataService
            // Convert LocalDate to Instant at the start of the day in UTC
            Instant fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant toInstant = to.atStartOfDay(ZoneId.systemDefault()).toInstant();
            
            // Handle Optional return type
            HistoricalData historicalData = historicalDataService.getHistoricalData(
                cleanSymbol, fromInstant, toInstant, mappedInterval).orElse(null);
            
            if (historicalData != null && historicalData.getDataPoints() != null && !historicalData.getDataPoints().isEmpty()) {
                log.debug("Retrieved historical data from database for symbol: {}", symbol);
                return historicalData;
            }
            
            log.debug("No historical data found in database for symbol: {}", symbol);
            return null;
        } catch (Exception e) {
            log.error("Error retrieving historical data for symbol {}: {}", symbol, e.getMessage(), e);
            return null;
        }
    }
}
