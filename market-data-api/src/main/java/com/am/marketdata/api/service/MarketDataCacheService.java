package com.am.marketdata.api.service;

import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Cache service for market data
 * Handles caching of responses from MarketDataService and InvestmentInstrumentService
 */
public interface MarketDataCacheService {

    /**
     * Get quotes from cache or service
     * 
     * @param tradingSymbols List of trading symbols
     * @param forceRefresh Whether to force a refresh from the source
     * @return Map containing quote data for each symbol
     */
    Map<String, Map<String, Object>> getQuotes(List<String> tradingSymbols, boolean forceRefresh);
    
    /**
     * Get live prices from cache or service
     * 
     * @param symbols Optional list of trading symbols (null or empty for all available instruments)
     * @param forceRefresh Whether to force a refresh from the source
     * @return Map containing prices, count, timestamp and processing time
     */
    Map<String, Object> getLivePrices(List<String> symbols, boolean forceRefresh);
    
    /**
     * Get historical data from cache or service
     * 
     * @param symbol Trading symbol
     * @param fromDate Start date
     * @param toDate End date
     * @param interval Data interval (minute, day, etc.)
     * @param instrumentType Type of instrument (STOCK, OPTION, MUTUAL_FUND, etc.)
     * @param additionalParams Additional parameters specific to instrument type
     * @param forceRefresh Whether to force a refresh from the source
     * @return Historical data response with metadata
     */
    Map<String, Object> getHistoricalData(String symbol, Date fromDate, Date toDate, 
                                       String interval, String instrumentType, 
                                       Map<String, Object> additionalParams, boolean forceRefresh);
    
    /**
     * Get historical data for multiple symbols from cache or service
     * 
     * @param symbols List of trading symbols
     * @param fromDate Start date
     * @param toDate End date
     * @param interval Data interval (minute, day, etc.)
     * @param instrumentType Type of instrument (STOCK, OPTION, MUTUAL_FUND, etc.)
     * @param additionalParams Additional parameters specific to instrument type including:
     *                        - filterType: Type of filtering (ALL, START_END, CUSTOM)
     *                        - filterFrequency: When using CUSTOM filter, return every Nth data point
     * @param forceRefresh Whether to force a refresh from the source
     * @return Historical data response with metadata for all symbols
     */
    Map<String, Object> getHistoricalDataMultipleSymbols(List<String> symbols, Date fromDate, Date toDate, 
                                       String interval, String instrumentType, 
                                       Map<String, Object> additionalParams, boolean forceRefresh);
    
    /**
     * Get option chain data from cache or service
     * 
     * @param underlyingSymbol Symbol of the underlying instrument
     * @param expiryDate Optional expiry date, if null will return the nearest expiry
     * @param forceRefresh Whether to force a refresh from the source
     * @return Option chain data with calls and puts
     */
    Map<String, Object> getOptionChain(String underlyingSymbol, Date expiryDate, boolean forceRefresh);
    
    /**
     * Get mutual fund details from cache or service
     * 
     * @param schemeCode Mutual fund scheme code
     * @param forceRefresh Whether to force a refresh from the source
     * @return Mutual fund details
     */
    Map<String, Object> getMutualFundDetails(String schemeCode, boolean forceRefresh);
    
    /**
     * Get mutual fund NAV history from cache or service
     * 
     * @param schemeCode Mutual fund scheme code
     * @param from Start date
     * @param to End date
     * @param forceRefresh Whether to force a refresh from the source
     * @return NAV history data
     */
    Map<String, Object> getMutualFundNavHistory(String schemeCode, Date from, Date to, boolean forceRefresh);
    
    /**
     * Clear all cached data
     */
    void clearAllCaches();
    
    /**
     * Clear specific cache by key
     * 
     * @param cacheKey Cache key to clear
     */
    void clearCache(String cacheKey);
    
    /**
     * Get cache statistics
     * 
     * @return Map containing cache statistics (hits, misses, etc.)
     */
    Map<String, Object> getCacheStatistics();
    
    /**
     * Get OHLC data from cache or service
     * 
     * @param symbols Array of trading symbols
     * @param forceRefresh Whether to force a refresh from the source
     * @return Map of symbol to OHLC data with cache status
     */
    Map<String, Object> getOHLC(String[] symbols, boolean forceRefresh);
    
    /**
     * Get latest stock index data from cache or service
     * 
     * @param indexSymbol Stock index symbol
     * @param forceRefresh Whether to force a refresh from the source
     * @return Stock index market data with cache status information
     */
    StockIndicesMarketData getStockIndexData(String indexSymbol, boolean forceRefresh);
    
    /**
     * Get latest stock indices data from cache or service
     * 
     * @param indexSymbols List of stock index symbols
     * @param forceRefresh Whether to force a refresh from the source
     * @return List of stock indices market data with cache status information
     */
    List<StockIndicesMarketData> getStockIndicesData(List<String> indexSymbols, boolean forceRefresh);
}
