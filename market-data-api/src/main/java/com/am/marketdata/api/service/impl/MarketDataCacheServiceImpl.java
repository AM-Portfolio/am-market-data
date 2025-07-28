package com.am.marketdata.api.service.impl;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.model.historical.OHLCVTPoint;

import java.time.LocalDateTime;
import java.time.ZoneId;
import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.common.investment.service.StockIndicesMarketDataService;
import com.am.marketdata.api.service.InvestmentInstrumentService;
import com.am.marketdata.api.service.MarketDataCacheService;
import com.am.marketdata.service.MarketDataService;
import com.zerodhatech.models.OHLCQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Implementation of MarketDataCacheService using Redis
 */
@Slf4j
@Service
public class MarketDataCacheServiceImpl implements MarketDataCacheService {

    private final InvestmentInstrumentService investmentInstrumentService;
    private final MarketDataService marketDataService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StockIndicesMarketDataService stockIndicesMarketDataService;
    
    // Cache statistics counters
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    @Value("${market.data.cache.ttl.seconds:300}")
    private long cacheTimeToLiveSeconds;
    
    @Value("${market.data.cache.enabled:true}")
    private boolean cacheEnabled;

    public MarketDataCacheServiceImpl(InvestmentInstrumentService investmentInstrumentService,
                                     MarketDataService marketDataService,
                                     RedisTemplate<String, Object> redisTemplate,
                                     StockIndicesMarketDataService stockIndicesMarketDataService) {
        this.investmentInstrumentService = investmentInstrumentService;
        this.marketDataService = marketDataService;
        this.redisTemplate = redisTemplate;
        this.stockIndicesMarketDataService = stockIndicesMarketDataService;
    }
    
    /**
     * Set whether caching is enabled
     * @param cacheEnabled true to enable caching, false to disable
     */
    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }
    
    /**
     * Set the cache time-to-live in seconds
     * @param cacheTtlSeconds time-to-live in seconds
     */
    public void setCacheTtlSeconds(long cacheTtlSeconds) {
        this.cacheTimeToLiveSeconds = cacheTtlSeconds;
    }

    @Override
    public Map<String, Map<String, Object>> getQuotes(List<String> tradingSymbols, boolean forceRefresh) {
        if (!cacheEnabled || forceRefresh) {
            cacheMisses.incrementAndGet();
            log.debug("Cache disabled or force refresh requested for quotes");
            return fetchAndCacheQuotes(tradingSymbols);
        }
        
        String cacheKey = buildQuotesCacheKey(tradingSymbols);
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> cachedQuotes = (Map<String, Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedQuotes != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for quotes with key: {}", cacheKey);
            return cachedQuotes;
        } else {
            cacheMisses.incrementAndGet();
            log.debug("Cache miss for quotes with key: {}", cacheKey);
            return fetchAndCacheQuotes(tradingSymbols);
        }
    }
    
    private Map<String, Map<String, Object>> fetchAndCacheQuotes(List<String> tradingSymbols) {
        Map<String, Map<String, Object>> quotes = investmentInstrumentService.getQuotes(tradingSymbols);
        
        // Don't cache error responses
        if (quotes != null && !quotes.containsKey("ERROR")) {
            String cacheKey = buildQuotesCacheKey(tradingSymbols);
            redisTemplate.opsForValue().set(cacheKey, quotes, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            log.debug("Cached quotes with key: {}", cacheKey);
        }
        
        return quotes;
    }
    
    private String buildQuotesCacheKey(List<String> tradingSymbols) {
        return "quotes:" + (tradingSymbols != null ? String.join(",", tradingSymbols) : "all");
    }

    @Override
    public Map<String, Object> getLivePrices(List<String> symbols, boolean forceRefresh) {
        if (!cacheEnabled || forceRefresh) {
            cacheMisses.incrementAndGet();
            log.debug("Cache disabled or force refresh requested for live prices");
            return fetchAndCacheLivePrices(symbols);
        }
        
        String cacheKey = buildLivePricesCacheKey(symbols);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedPrices = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedPrices != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for live prices with key: {}", cacheKey);
            return cachedPrices;
        } else {
            cacheMisses.incrementAndGet();
            log.debug("Cache miss for live prices with key: {}", cacheKey);
            return fetchAndCacheLivePrices(symbols);
        }
    }
    
    private Map<String, Object> fetchAndCacheLivePrices(List<String> symbols) {
        Map<String, Object> prices = investmentInstrumentService.getLivePrices(symbols);
        
        // Don't cache error responses
        if (prices != null && !prices.containsKey("error")) {
            String cacheKey = buildLivePricesCacheKey(symbols);
            redisTemplate.opsForValue().set(cacheKey, prices, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            log.debug("Cached live prices with key: {}", cacheKey);
        }
        
        return prices;
    }
    
    private String buildLivePricesCacheKey(List<String> symbols) {
        return "live-prices:" + (symbols != null ? String.join(",", symbols) : "all");
    }

    @Override
    public Map<String, Object> getHistoricalData(String symbol, Date fromDate, Date toDate, 
                                              String interval, String instrumentType, 
                                              Map<String, Object> additionalParams, boolean forceRefresh) {
        if (!cacheEnabled || forceRefresh) {
            cacheMisses.incrementAndGet();
            log.debug("Cache disabled or force refresh requested for historical data");
            return fetchAndCacheHistoricalData(symbol, fromDate, toDate, interval, instrumentType, additionalParams);
        }
        
        String cacheKey = buildHistoricalDataCacheKey(symbol, fromDate, toDate, interval, instrumentType);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedData != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for historical data with key: {}", cacheKey);
            return cachedData;
        } else {
            cacheMisses.incrementAndGet();
            log.debug("Cache miss for historical data with key: {}", cacheKey);
            return fetchAndCacheHistoricalData(symbol, fromDate, toDate, interval, instrumentType, additionalParams);
        }
    }
    
    private Map<String, Object> fetchAndCacheHistoricalData(String symbol, Date fromDate, Date toDate, 
                                                         String interval, String instrumentType, 
                                                         Map<String, Object> additionalParams) {
        Map<String, Object> data = investmentInstrumentService.getHistoricalData(
            symbol, fromDate, toDate, interval, instrumentType, additionalParams);
        
        // Don't cache error responses
        if (data != null && !data.containsKey("error")) {
            String cacheKey = buildHistoricalDataCacheKey(symbol, fromDate, toDate, interval, instrumentType);
            redisTemplate.opsForValue().set(cacheKey, data, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            log.debug("Cached historical data with key: {}", cacheKey);
        }
        
        return data;
    }
    
    private String buildHistoricalDataCacheKey(String symbol, Date fromDate, Date toDate, 
                                            String interval, String instrumentType) {
        return String.format("historical:%s:%s:%s:%s:%s", 
                            symbol, 
                            fromDate.getTime(), 
                            toDate.getTime(), 
                            interval, 
                            instrumentType != null ? instrumentType : "default");
    }

    @Override
    public Map<String, Object> getHistoricalDataMultipleSymbols(List<String> symbols, Date fromDate, Date toDate, 
                                                         String interval, String instrumentType, 
                                                         Map<String, Object> additionalParams, boolean forceRefresh) {
        log.info("Processing historical data request for multiple symbols: {} from {} to {}", 
                symbols, fromDate, toDate);
        
        // Prepare the result container
        Map<String, Object> aggregatedResult = new HashMap<>();
        Map<String, Object> symbolsData = new HashMap<>();
        long startTime = System.currentTimeMillis();
        int totalDataPoints = 0;
        int successCount = 0;
        
        // Process each symbol
        for (String symbol : symbols) {
            try {
                Map<String, Object> singleResult = getHistoricalData(
                    symbol, fromDate, toDate, interval, instrumentType, additionalParams, forceRefresh);
                
                // Skip if there was an error for this symbol
                if (singleResult.containsKey("error")) {
                    log.warn("Error fetching historical data for symbol {}: {}", 
                            symbol, singleResult.get("message"));
                    symbolsData.put(symbol, singleResult);
                    continue;
                }
                
                // Apply filtering if needed
                if (additionalParams != null && additionalParams.containsKey("filterType")) {
                    singleResult = applyDataFiltering(singleResult, additionalParams);
                }
                
                // Add to the results
                symbolsData.put(symbol, singleResult);
                successCount++;
                
                // Count data points
                if (singleResult.containsKey("count")) {
                    totalDataPoints += (int) singleResult.get("count");
                }
            } catch (Exception e) {
                log.error("Error processing historical data for symbol {}: {}", symbol, e.getMessage(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "Failed to fetch historical data");
                errorResult.put("message", e.getMessage());
                symbolsData.put(symbol, errorResult);
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // Build the aggregated response
        aggregatedResult.put("data", symbolsData);
        aggregatedResult.put("symbols", symbols);
        aggregatedResult.put("fromDate", new SimpleDateFormat("yyyy-MM-dd").format(fromDate));
        aggregatedResult.put("toDate", new SimpleDateFormat("yyyy-MM-dd").format(toDate));
        aggregatedResult.put("interval", interval);
        aggregatedResult.put("totalSymbols", symbols.size());
        aggregatedResult.put("successfulSymbols", successCount);
        aggregatedResult.put("totalDataPoints", totalDataPoints);
        aggregatedResult.put("processingTimeMs", (endTime - startTime));
        
        log.info("Successfully processed historical data for {}/{} symbols with {} total data points in {}ms", 
                successCount, symbols.size(), totalDataPoints, (endTime - startTime));
        
        return aggregatedResult;
    }
    
    /**
     * Apply filtering to historical data based on filter parameters
     * 
     * @param data Original historical data response
     * @param params Parameters containing filter settings
     * @return Filtered historical data
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> applyDataFiltering(Map<String, Object> data, Map<String, Object> params) {
        String filterType = params.get("filterType").toString();
        int filterFrequency = params.containsKey("filterFrequency") ? 
                Integer.parseInt(params.get("filterFrequency").toString()) : 1;
        
        // If no filtering needed (ALL type)
        if ("ALL".equalsIgnoreCase(filterType)) {
            return data;
        }
        
        // For CUSTOM type, ensure filterFrequency is at least 2
        if ("CUSTOM".equalsIgnoreCase(filterType) && filterFrequency < 2) {
            log.warn("CUSTOM filter type specified but filterFrequency is less than 2 ({}). Using default of 2.", filterFrequency);
            filterFrequency = 2;
        }
        
        // Get the historical data points
        Map<String, Object> result = new HashMap<>(data);
        Object dataObj = data.get("data");
        
        // Handle different data types
        List<OHLCVTPoint> originalPoints = new ArrayList<>();
        String tradingSymbol = "";
        String interval = "";
        
        // Extract data from either HistoricalData object or Map
        if (dataObj instanceof HistoricalData) {
            HistoricalData historicalData = (HistoricalData) dataObj;
            originalPoints = historicalData.getDataPoints();
            tradingSymbol = historicalData.getTradingSymbol();
            interval = historicalData.getInterval();
        } else if (dataObj instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) dataObj;
            
            if (dataMap.containsKey("tradingSymbol")) {
                tradingSymbol = dataMap.get("tradingSymbol").toString();
            }
            
            if (dataMap.containsKey("interval")) {
                interval = dataMap.get("interval").toString();
            }
            
            if (dataMap.containsKey("dataPoints") && dataMap.get("dataPoints") instanceof List) {
                List<Map<String, Object>> pointMaps = (List<Map<String, Object>>) dataMap.get("dataPoints");
                
                for (Map<String, Object> pointMap : pointMaps) {
                    OHLCVTPoint point = new OHLCVTPoint();
                    
                    if (pointMap.containsKey("timestamp")) {
                        // Convert Date to LocalDateTime if needed
                        if (pointMap.get("timestamp") instanceof Date) {
                            Date date = (Date) pointMap.get("timestamp");
                            point.setTime(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                        } else if (pointMap.get("timestamp") instanceof LocalDateTime) {
                            point.setTime((LocalDateTime) pointMap.get("timestamp"));
                        }
                    }
                    
                    if (pointMap.containsKey("open")) {
                        point.setOpen(Double.parseDouble(pointMap.get("open").toString()));
                    }
                    
                    if (pointMap.containsKey("high")) {
                        point.setHigh(Double.parseDouble(pointMap.get("high").toString()));
                    }
                    
                    if (pointMap.containsKey("low")) {
                        point.setLow(Double.parseDouble(pointMap.get("low").toString()));
                    }
                    
                    if (pointMap.containsKey("close")) {
                        point.setClose(Double.parseDouble(pointMap.get("close").toString()));
                    }
                    
                    if (pointMap.containsKey("volume")) {
                        point.setVolume(Long.parseLong(pointMap.get("volume").toString()));
                    }
                    
                    originalPoints.add(point);
                }
            }
        } else {
            log.warn("Unexpected data type for filtering: {}", dataObj != null ? dataObj.getClass().getName() : "null");
            return data; // Return original data if unexpected type
        }
        
        if (originalPoints.isEmpty()) {
            log.warn("No data points found for filtering");
            return data; // Nothing to filter
        }
        
        // Apply filtering based on type
        List<OHLCVTPoint> filteredPoints = new ArrayList<>();
        
        if ("START_END".equalsIgnoreCase(filterType)) {
            // Only include first and last points
            filteredPoints.add(originalPoints.get(0)); // First point
            
            if (originalPoints.size() > 1) {
                filteredPoints.add(originalPoints.get(originalPoints.size() - 1)); // Last point
            }
        } else if ("CUSTOM".equalsIgnoreCase(filterType)) {
            // Include every Nth point
            for (int i = 0; i < originalPoints.size(); i += filterFrequency) {
                filteredPoints.add(originalPoints.get(i));
            }
            
            // Always include the last point if not already included
            int lastIndex = originalPoints.size() - 1;
            if (lastIndex >= 0 && lastIndex % filterFrequency != 0) {
                filteredPoints.add(originalPoints.get(lastIndex));
            }
        }
        
        // Create a new HistoricalData object with filtered points
        HistoricalData filteredData = new HistoricalData();
        filteredData.setDataPoints(filteredPoints);
        filteredData.setTradingSymbol(tradingSymbol);
        filteredData.setInterval(interval);
        
        // Update the result
        result.put("data", filteredData);
        result.put("count", filteredPoints.size());
        result.put("filtered", true);
        result.put("filterType", filterType);
        result.put("originalCount", originalPoints.size());
        
        log.debug("Applied {} filtering to historical data, reduced from {} to {} points", 
                filterType, originalPoints.size(), filteredPoints.size());
        
        return result;
    }

    @Override
    public Map<String, Object> getOptionChain(String underlyingSymbol, Date expiryDate, boolean forceRefresh) {
        if (!cacheEnabled || forceRefresh) {
            cacheMisses.incrementAndGet();
            log.debug("Cache disabled or force refresh requested for option chain");
            return fetchAndCacheOptionChain(underlyingSymbol, expiryDate);
        }
        
        String cacheKey = buildOptionChainCacheKey(underlyingSymbol, expiryDate);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedData != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for option chain with key: {}", cacheKey);
            return cachedData;
        } else {
            cacheMisses.incrementAndGet();
            log.debug("Cache miss for option chain with key: {}", cacheKey);
            return fetchAndCacheOptionChain(underlyingSymbol, expiryDate);
        }
    }
    
    private Map<String, Object> fetchAndCacheOptionChain(String underlyingSymbol, Date expiryDate) {
        Map<String, Object> data = investmentInstrumentService.getOptionChain(underlyingSymbol, expiryDate);
        
        // Don't cache error responses
        if (data != null && !data.containsKey("error")) {
            String cacheKey = buildOptionChainCacheKey(underlyingSymbol, expiryDate);
            redisTemplate.opsForValue().set(cacheKey, data, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            log.debug("Cached option chain with key: {}", cacheKey);
        }
        
        return data;
    }
    
    private String buildOptionChainCacheKey(String underlyingSymbol, Date expiryDate) {
        return String.format("option-chain:%s:%s", 
                            underlyingSymbol, 
                            expiryDate != null ? expiryDate.getTime() : "nearest");
    }

    @Override
    public Map<String, Object> getMutualFundDetails(String schemeCode, boolean forceRefresh) {
        if (!cacheEnabled || forceRefresh) {
            cacheMisses.incrementAndGet();
            log.debug("Cache disabled or force refresh requested for mutual fund details");
            return fetchAndCacheMutualFundDetails(schemeCode);
        }
        
        String cacheKey = buildMutualFundDetailsCacheKey(schemeCode);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedData != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for mutual fund details with key: {}", cacheKey);
            return cachedData;
        } else {
            cacheMisses.incrementAndGet();
            log.debug("Cache miss for mutual fund details with key: {}", cacheKey);
            return fetchAndCacheMutualFundDetails(schemeCode);
        }
    }
    
    private Map<String, Object> fetchAndCacheMutualFundDetails(String schemeCode) {
        Map<String, Object> data = investmentInstrumentService.getMutualFundDetails(schemeCode);
        
        // Don't cache error responses
        if (data != null && !data.containsKey("error")) {
            String cacheKey = buildMutualFundDetailsCacheKey(schemeCode);
            redisTemplate.opsForValue().set(cacheKey, data, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            log.debug("Cached mutual fund details with key: {}", cacheKey);
        }
        
        return data;
    }
    
    private String buildMutualFundDetailsCacheKey(String schemeCode) {
        return "mutual-fund-details:" + schemeCode;
    }

    @Override
    public Map<String, Object> getMutualFundNavHistory(String schemeCode, Date from, Date to, boolean forceRefresh) {
        if (!cacheEnabled || forceRefresh) {
            cacheMisses.incrementAndGet();
            log.debug("Cache disabled or force refresh requested for mutual fund NAV history");
            return fetchAndCacheMutualFundNavHistory(schemeCode, from, to);
        }
        
        String cacheKey = buildMutualFundNavHistoryCacheKey(schemeCode, from, to);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedData != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for mutual fund NAV history with key: {}", cacheKey);
            return cachedData;
        } else {
            cacheMisses.incrementAndGet();
            log.debug("Cache miss for mutual fund NAV history with key: {}", cacheKey);
            return fetchAndCacheMutualFundNavHistory(schemeCode, from, to);
        }
    }
    
    private Map<String, Object> fetchAndCacheMutualFundNavHistory(String schemeCode, Date from, Date to) {
        Map<String, Object> data = investmentInstrumentService.getMutualFundNavHistory(schemeCode, from, to);
        
        // Don't cache error responses
        if (data != null && !data.containsKey("error")) {
            String cacheKey = buildMutualFundNavHistoryCacheKey(schemeCode, from, to);
            redisTemplate.opsForValue().set(cacheKey, data, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            log.debug("Cached mutual fund NAV history with key: {}", cacheKey);
        }
        
        return data;
    }
    
    private String buildMutualFundNavHistoryCacheKey(String schemeCode, Date from, Date to) {
        return String.format("mutual-fund-nav-history:%s:%s:%s", 
                            schemeCode, 
                            from.getTime(), 
                            to.getTime());
    }

    @Override
    public void clearAllCaches() {
        log.info("Clearing all market data caches");
        // Get all keys matching our cache patterns
        for (String pattern : List.of("quotes:*", "live-prices:*", "historical:*", 
                                     "option-chain:*", "mutual-fund-details:*", "mutual-fund-nav-history:*")) {
            redisTemplate.keys(pattern).forEach(key -> {
                redisTemplate.delete(key);
                log.debug("Deleted cache key: {}", key);
            });
        }
    }

    @Override
    public void clearCache(String cacheKey) {
        log.info("Clearing cache with key: {}", cacheKey);
        redisTemplate.delete(cacheKey);
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("hits", cacheHits.get());
        stats.put("misses", cacheMisses.get());
        stats.put("ratio", calculateHitRatio());
        stats.put("enabled", cacheEnabled);
        stats.put("ttl_seconds", cacheTimeToLiveSeconds);
        
        // Add key counts by type
        Map<String, Long> keyCounts = new HashMap<>();
        for (String pattern : List.of("quotes:*", "live-prices:*", "historical:*", 
                                     "option-chain:*", "mutual-fund-details:*", "mutual-fund-nav-history:*")) {
            keyCounts.put(pattern.replace(":*", ""), (long) redisTemplate.keys(pattern).size());
        }
        stats.put("key_counts", keyCounts);
        
        return stats;
    }
    
    private double calculateHitRatio() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        
        if (total == 0) {
            return 0.0;
        }
        
        return (double) hits / total;
    }
    
    @Override
    public Map<String, Object> getOHLC(String[] symbols, boolean forceRefresh) {
        if (!cacheEnabled || forceRefresh) {
            cacheMisses.incrementAndGet();
            log.debug("Cache disabled or force refresh requested for OHLC data");
            return fetchAndCacheOHLC(symbols);
        }
        
        String cacheKey = buildOHLCCacheKey(symbols);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedData != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for OHLC data with key: {}", cacheKey);
            return cachedData;
        } else {
            cacheMisses.incrementAndGet();
            log.debug("Cache miss for OHLC data with key: {}", cacheKey);
            return fetchAndCacheOHLC(symbols);
        }
    }
    
    private Map<String, Object> fetchAndCacheOHLC(String[] symbols) {
        Map<String, OHLCQuote> ohlcData = marketDataService.getOHLC(symbols);
        
        // Create response with cache status
        Map<String, Object> response = new HashMap<>();
        response.put("data", ohlcData);
        response.put("cached", false);
        response.put("timestamp", System.currentTimeMillis());
        
        // Don't cache if null or empty
        if (ohlcData != null && !ohlcData.isEmpty()) {
            String cacheKey = buildOHLCCacheKey(symbols);
            redisTemplate.opsForValue().set(cacheKey, response, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            log.debug("Cached OHLC data with key: {}", cacheKey);
        }
        
        return response;
    }
    
    private String buildOHLCCacheKey(String[] symbols) {
        return "ohlc:" + (symbols != null ? String.join(",", symbols) : "all");
    }
    
    @Override
    public StockIndicesMarketData getStockIndexData(String indexSymbol, boolean forceRefresh) {
        if (!cacheEnabled || forceRefresh) {
            cacheMisses.incrementAndGet();
            log.debug("Cache disabled or force refresh requested for stock index data: {}", indexSymbol);
            return fetchAndCacheStockIndexData(indexSymbol);
        }
        
        String cacheKey = buildStockIndexCacheKey(indexSymbol);
        
        Object rawCachedData = redisTemplate.opsForValue().get(cacheKey);
        StockIndicesMarketData cachedData = null;
        
        if (rawCachedData instanceof Map) {
            // Handle the case where Redis returns a Map instead of the actual object
            log.debug("Converting Map to StockIndicesMarketData for key: {}", cacheKey);
            // We need to fetch the data again since we can't reliably convert the Map
            cachedData = stockIndicesMarketDataService.findByIndexSymbol(indexSymbol);
            // Update the cache with the proper object
            if (cachedData != null) {
                redisTemplate.opsForValue().set(cacheKey, cachedData, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            }
        } else if (rawCachedData instanceof StockIndicesMarketData) {
            cachedData = (StockIndicesMarketData) rawCachedData;
        }
        
        if (cachedData != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for stock index data with key: {}", cacheKey);
            return cachedData;
        } else {
            cacheMisses.incrementAndGet();
            log.debug("Cache miss for stock index data with key: {}", cacheKey);
            return fetchAndCacheStockIndexData(indexSymbol);
        }
    }
    
    private StockIndicesMarketData fetchAndCacheStockIndexData(String indexSymbol) {
        StockIndicesMarketData indexData = stockIndicesMarketDataService.findByIndexSymbol(indexSymbol);
        
        // Don't cache if null
        if (indexData != null) {
            String cacheKey = buildStockIndexCacheKey(indexSymbol);
            redisTemplate.opsForValue().set(cacheKey, indexData, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            log.debug("Cached stock index data with key: {}", cacheKey);
        }
        
        return indexData;
    }
    
    private String buildStockIndexCacheKey(String indexSymbol) {
        return "stock-index:" + indexSymbol;
    }
    
    @Override
    public List<StockIndicesMarketData> getStockIndicesData(List<String> indexSymbols, boolean forceRefresh) {
        if (!cacheEnabled || forceRefresh) {
            cacheMisses.incrementAndGet();
            log.debug("Cache disabled or force refresh requested for stock indices data");
            return fetchAndCacheStockIndicesData(indexSymbols);
        }
        
        String cacheKey = buildStockIndicesCacheKey(indexSymbols);
        
        Object rawCachedData = redisTemplate.opsForValue().get(cacheKey);
        List<StockIndicesMarketData> cachedData = null;
        
        if (rawCachedData instanceof List) {
            try {
                List<?> dataList = (List<?>) rawCachedData;
                
                // Check if we can safely cast the list elements
                if (!dataList.isEmpty() && dataList.get(0) instanceof StockIndicesMarketData) {
                    @SuppressWarnings("unchecked")
                    List<StockIndicesMarketData> typedList = (List<StockIndicesMarketData>) dataList;
                    cachedData = typedList;
                } else {
                    // Need to fetch fresh data as we can't safely cast
                    log.debug("Cannot safely cast cached list data for key: {}", cacheKey);
                    cachedData = indexSymbols.stream()
                        .map(symbol -> stockIndicesMarketDataService.findByIndexSymbol(symbol))
                        .filter(data -> data != null)
                        .collect(Collectors.toList());
                    // Update cache with properly typed objects
                    if (cachedData != null && !cachedData.isEmpty()) {
                        redisTemplate.opsForValue().set(cacheKey, cachedData, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
                    }
                }
            } catch (ClassCastException e) {
                log.debug("ClassCastException when processing cached data: {}", e.getMessage());
                cachedData = indexSymbols.stream()
                    .map(symbol -> stockIndicesMarketDataService.findByIndexSymbol(symbol))
                    .filter(data -> data != null)
                    .collect(Collectors.toList());
                // Update cache with properly typed objects
                if (cachedData != null && !cachedData.isEmpty()) {
                    redisTemplate.opsForValue().set(cacheKey, cachedData, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
                }
            }
        }
        
        if (cachedData != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for stock indices data with key: {}", cacheKey);
            return cachedData;
        } else {
            cacheMisses.incrementAndGet();
            log.debug("Cache miss for stock indices data with key: {}", cacheKey);
            return fetchAndCacheStockIndicesData(indexSymbols);
        }
    }
    
    private List<StockIndicesMarketData> fetchAndCacheStockIndicesData(List<String> indexSymbols) {
        List<StockIndicesMarketData> indicesData = indexSymbols.stream()
            .map(symbol -> stockIndicesMarketDataService.findByIndexSymbol(symbol))
            .filter(data -> data != null)
            .collect(Collectors.toList());
        
        // Don't cache if null or empty
        if (indicesData != null && !indicesData.isEmpty()) {
            String cacheKey = buildStockIndicesCacheKey(indexSymbols);
            redisTemplate.opsForValue().set(cacheKey, indicesData, cacheTimeToLiveSeconds, TimeUnit.SECONDS);
            log.debug("Cached stock indices data with key: {}", cacheKey);
        }
        
        return indicesData;
    }
    
    private String buildStockIndicesCacheKey(List<String> indexSymbols) {
        return "stock-indices:" + (indexSymbols != null ? String.join(",", indexSymbols) : "all");
    }
}
