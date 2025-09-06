package com.am.marketdata.service.util;

import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.service.MarketDataPersistenceService;
import com.marketdata.common.MarketDataProvider;
import com.marketdata.common.MarketDataProviderFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Concrete implementation of AbstractMarketDataRetriever for OHLC data.
 * Handles retrieval of OHLC quotes from cache, database, and provider.
 */
@Slf4j
public class OHLCDataRetriever extends AbstractMarketDataRetriever<String, OHLCQuote> {
    
    @Getter @Setter
    private TimeFrame timeFrame = TimeFrame.FIVE_MINUTE; // Default to 5-minute timeframe
    
    private OHLCDataRetriever(
            MarketDataPersistenceService persistenceService,
            MarketDataProviderFactory providerFactory,
            List<DataSourceType> retrievalOrder,
            boolean cacheResults) {
        super(persistenceService, providerFactory, retrievalOrder, cacheResults);
    }
    
    /**
     * Retrieve OHLC data from cache
     *
     * @param allSymbols All symbols being requested
     * @param remainingSymbols Set of symbols that still need to be retrieved (will be modified)
     * @param timeFrame The time frame for the OHLC data
     * @return Map of symbol to OHLC quote
     */
    @Override
    protected Map<String, OHLCQuote> retrieveFromCache(List<String> allSymbols, Set<String> remainingSymbols, TimeFrame timeFrame) {
        log.info("[CACHE] Attempting to fetch OHLC data from cache for {} symbols with timeFrame {}", 
                remainingSymbols.size(), timeFrame.getApiValue());
        
        // Pass timeFrame to persistence service if it supports it
        Map<String, OHLCQuote> cachedData = persistenceService.getOHLCData(allSymbols, timeFrame, false);
        
        if (cachedData != null && !cachedData.isEmpty()) {
            log.info("[CACHE] Found {} OHLC quotes in cache for timeFrame {}", cachedData.size(), timeFrame.getApiValue());
            
            // Remove found symbols from the remaining set
            cachedData.keySet().forEach(symbol -> 
                remainingSymbols.remove(symbol.replace("NSE:", "")));
            
            log.info("[CACHE] {} symbols remaining after cache lookup for timeFrame {}", remainingSymbols.size(), timeFrame.getApiValue());
        } else {
            log.info("[CACHE] No OHLC data found in cache for timeFrame {}", timeFrame.getApiValue());
        }
        
        return cachedData != null ? cachedData : Collections.emptyMap();
    }
    
    /**
     * Retrieve OHLC data from database
     *
     * @param remainingSymbols Set of symbols that still need to be retrieved (will be modified)
     * @param timeFrame The time frame for the OHLC data
     * @return Map of symbol to OHLC quote
     */
    @Override
    protected Map<String, OHLCQuote> retrieveFromDatabase(Set<String> remainingSymbols, TimeFrame timeFrame) {
        if (remainingSymbols.isEmpty()) {
            return Collections.emptyMap();
        }
        
        log.info("[DATABASE] Attempting to fetch OHLC data from database for {} symbols with timeFrame {}", 
                remainingSymbols.size(), timeFrame.getApiValue());
        
        List<String> remainingSymbolsList = new ArrayList<>(remainingSymbols);
        
        // Force refresh is true here because we want to bypass cache and go directly to database
        Map<String, OHLCQuote> dbData = persistenceService.getOHLCData(remainingSymbolsList, timeFrame, true);
        
        if (dbData != null && !dbData.isEmpty()) {
            log.info("[DATABASE] Found {} OHLC quotes in database for timeFrame {}", dbData.size(), timeFrame.getApiValue());
            
            // Remove found symbols from the remaining set
            dbData.keySet().forEach(symbol -> 
                remainingSymbols.remove(symbol.replace("NSE:", "")));
            
            log.info("[DATABASE] {} symbols remaining after database lookup for timeFrame {}", remainingSymbols.size(), timeFrame.getApiValue());
        } else {
            log.info("[DATABASE] No OHLC data found in database for timeFrame {}", timeFrame.getApiValue());
        }
        
        return dbData != null ? dbData : Collections.emptyMap();
    }
    
    /**
     * Retrieve OHLC data from provider
     *
     * @param provider The market data provider
     * @param symbols List of symbols to retrieve
     * @return Map of symbol to OHLC quote
     */
    @Override
    protected Map<String, OHLCQuote> retrieveFromProvider(MarketDataProvider provider, List<String> symbols) {
        if (symbols.isEmpty()) {
            return Collections.emptyMap();
        }
        
        log.info(provider.getProviderName() + " Fetching OHLC data from provider for {} symbols with timeFrame {}", 
                symbols.size(), timeFrame.getApiValue());
        
        try {
            // Pass the timeFrame to the provider
            Map<String, OHLCQuote> providerData = provider.getOHLC(symbols);
            
            if (providerData != null && !providerData.isEmpty()) {
                log.info(provider.getProviderName() + " Successfully fetched {} OHLC quotes from provider with timeFrame {}", 
                        providerData.size(), timeFrame.getApiValue());
            } else {
                log.info(provider.getProviderName() + " No OHLC data returned from provider for timeFrame {}", 
                        timeFrame.getApiValue());
            }
            
            return providerData != null ? providerData : Collections.emptyMap();
        } catch (Exception e) {
            log.error(provider.getProviderName() + " Error fetching OHLC data for timeFrame {}: {}", 
                    timeFrame.getApiValue(), e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * Save OHLC data to persistence asynchronously
     *
     * @param data The data to save
     */
    @Override
    protected void saveDataAsync(Map<String, OHLCQuote> data) {
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
     * Builder for OHLCDataRetriever
     */
    public static class Builder extends AbstractBuilder<String, OHLCQuote, Builder, OHLCDataRetriever> {
        @Override
        public OHLCDataRetriever build() {
            if (persistenceService == null) {
                throw new IllegalStateException("PersistenceService must be provided");
            }
            if (providerFactory == null) {
                throw new IllegalStateException("ProviderFactory must be provided");
            }
            
            return new OHLCDataRetriever(
                    persistenceService,
                    providerFactory,
                    retrievalOrder,
                    cacheResults != null ? cacheResults : true
            );
        }
    }
    
    /**
     * Create a new builder for OHLCDataRetriever
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
