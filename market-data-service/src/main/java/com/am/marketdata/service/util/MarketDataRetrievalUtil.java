package com.am.marketdata.service.util;

import com.am.marketdata.common.model.OHLCQuote;
import com.marketdata.common.MarketDataProvider;
import com.am.marketdata.service.MarketDataPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;

import java.util.*;
import java.util.function.Supplier;

/**
 * Utility class for retrieving market data from different sources
 */
@Slf4j
public class MarketDataRetrievalUtil {

    /**
     * Retrieve OHLC data from cache
     *
     * @param persistenceService The persistence service to use
     * @param tradingSymbols List of trading symbols
     * @param remainingSymbols Set of symbols that still need to be retrieved (will be modified)
     * @return Map of symbol to OHLC quote
     */
    public static Map<String, OHLCQuote> retrieveFromCache(
            MarketDataPersistenceService persistenceService,
            List<String> tradingSymbols,
            Set<String> remainingSymbols) {
        
        log.info("[DATA_SOURCE] Attempting to fetch OHLC data from cache for {} symbols", 
                remainingSymbols.size());
        
        Map<String, OHLCQuote> cachedData = persistenceService.getOHLCData(tradingSymbols, false);
        
        if (cachedData != null && !cachedData.isEmpty()) {
            log.info("[DATA_SOURCE] Found {} OHLC quotes in cache", cachedData.size());
            
            // Remove found symbols from the remaining set
            cachedData.keySet().forEach(symbol -> 
                remainingSymbols.remove(symbol.replace("NSE:", "")));
            
            log.info("[DATA_SOURCE] {} symbols remaining after cache lookup", remainingSymbols.size());
        } else {
            log.info("[DATA_SOURCE] No OHLC data found in cache");
        }
        
        return cachedData != null ? cachedData : Collections.emptyMap();
    }
    
    /**
     * Retrieve OHLC data from database
     *
     * @param persistenceService The persistence service to use
     * @param remainingSymbols Set of symbols that still need to be retrieved (will be modified)
     * @return Map of symbol to OHLC quote
     */
    public static Map<String, OHLCQuote> retrieveFromDatabase(
            MarketDataPersistenceService persistenceService,
            Set<String> remainingSymbols) {
        
        if (remainingSymbols.isEmpty()) {
            return Collections.emptyMap();
        }
        
        log.info("[DATA_SOURCE] Attempting to fetch OHLC data from database for {} symbols", 
                remainingSymbols.size());
        
        List<String> remainingSymbolsList = new ArrayList<>(remainingSymbols);
        
        // Force refresh is true here because we want to bypass cache and go directly to database
        Map<String, OHLCQuote> dbData = persistenceService.getOHLCData(remainingSymbolsList, true);
        
        if (dbData != null && !dbData.isEmpty()) {
            log.info("[DATA_SOURCE] Found {} OHLC quotes in database", dbData.size());
            
            // Remove found symbols from the remaining set
            dbData.keySet().forEach(symbol -> 
                remainingSymbols.remove(symbol.replace("NSE:", "")));
            
            log.info("[DATA_SOURCE] {} symbols remaining after database lookup", remainingSymbols.size());
        } else {
            log.info("[DATA_SOURCE] No OHLC data found in database");
        }
        
        return dbData != null ? dbData : Collections.emptyMap();
    }
    
    /**
     * Retrieve OHLC data from provider
     *
     * @param provider The market data provider
     * @param retryTemplate The retry template to use for provider calls
     * @param symbols List of symbols to retrieve
     * @return Map of symbol to OHLC quote
     */
    public static Map<String, OHLCQuote> retrieveFromProvider(
            MarketDataProvider provider,
            RetryTemplate retryTemplate,
            List<String> symbols) {
        
        if (symbols.isEmpty()) {
            return Collections.emptyMap();
        }
        
        log.info("[DATA_SOURCE] Fetching OHLC data from provider for {} symbols", symbols.size());
        
        Map<String, OHLCQuote> providerData = retryWithTemplate(retryTemplate, 
                () -> provider.getOHLC(symbols), "getOHLC");
        
        if (providerData != null && !providerData.isEmpty()) {
            log.info("[DATA_SOURCE] Successfully fetched {} OHLC quotes from provider", providerData.size());
        } else {
            log.warn("[DATA_SOURCE] No OHLC data returned from provider");
        }
        
        return providerData != null ? providerData : Collections.emptyMap();
    }
    
    /**
     * Execute a supplier with retry logic
     *
     * @param retryTemplate The retry template to use
     * @param supplier The supplier to execute
     * @param operationName The name of the operation (for logging)
     * @param <T> The return type
     * @return The result of the supplier
     */
    public static <T> T retryWithTemplate(RetryTemplate retryTemplate, Supplier<T> supplier, String operationName) {
        try {
            return retryTemplate.execute(context -> {
                int retryCount = context.getRetryCount();
                if (retryCount > 0) {
                    log.warn("Retry attempt {} for operation {}", retryCount, operationName);
                }
                return supplier.get();
            });
        } catch (Exception e) {
            log.error("Operation {} failed after retries: {}", operationName, e.getMessage(), e);
            // Rethrow the exception instead of returning null
            throw new RuntimeException("Operation " + operationName + " failed after retries", e);
        }
    }
    
    /**
     * Save OHLC data to persistence asynchronously
     *
     * @param persistenceService The persistence service to use
     * @param data The data to save
     */
    public static void saveDataAsync(MarketDataPersistenceService persistenceService, Map<String, OHLCQuote> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        
        try {
            persistenceService.saveOHLCData(data);
            log.debug("Initiated async save of {} OHLC quotes", data.size());
        } catch (Exception e) {
            log.error("Error initiating async save of OHLC data: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Validate trading symbols
     *
     * @param tradingSymbols List of trading symbols
     * @throws IllegalArgumentException if the symbols are invalid
     */
    public static void validateSymbols(List<String> tradingSymbols) {
        if (tradingSymbols == null) {
            throw new IllegalArgumentException("Trading symbols cannot be null");
        }
    }
}








