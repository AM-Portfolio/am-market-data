package com.am.marketdata.api.service.impl;

import com.am.marketdata.api.service.InvestmentInstrumentService;
import com.am.marketdata.api.service.MarketDataCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of MarketDataCacheService using Redis
 */
@Slf4j
@Service
public class MarketDataCacheServiceImpl implements MarketDataCacheService {

    private final InvestmentInstrumentService investmentInstrumentService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Cache statistics counters
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    @Value("${market.data.cache.ttl.seconds:300}")
    private long cacheTimeToLiveSeconds;
    
    @Value("${market.data.cache.enabled:true}")
    private boolean cacheEnabled;

    public MarketDataCacheServiceImpl(InvestmentInstrumentService investmentInstrumentService, 
                                     RedisTemplate<String, Object> redisTemplate) {
        this.investmentInstrumentService = investmentInstrumentService;
        this.redisTemplate = redisTemplate;
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
}
