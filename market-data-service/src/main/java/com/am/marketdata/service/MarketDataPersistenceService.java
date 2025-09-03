package com.am.marketdata.service;

import com.am.common.investment.model.historical.HistoricalData;
import com.zerodhatech.models.OHLCQuote;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for persisting market data to both database and cache
 * This service abstracts the persistence logic to make it easier to adapt to new brokers
 */
public interface MarketDataPersistenceService {

    /**
     * Save OHLC data to both database and cache
     * @param ohlcData Map of symbol to OHLC data
     * @return CompletableFuture for async operations
     */
    CompletableFuture<Void> saveOHLCData(Map<String, OHLCQuote> ohlcData);
    
    /**
     * Save historical data to both database and cache
     * @param symbol Trading symbol
     * @param interval Data interval (e.g., "1d", "5m")
     * @param historicalData Historical data to save
     * @return CompletableFuture for async operations
     */
    CompletableFuture<Void> saveHistoricalData(String symbol, String interval, HistoricalData historicalData);
    
    /**
     * Retrieve OHLC data from cache or database
     * @param tradingSymbols List of trading symbols
     * @return Map of symbol to OHLC data
     */
    Map<String, OHLCQuote> getOHLCData(List<String> tradingSymbols);
    
    /**
     * Retrieve historical data from cache or database
     * @param symbol Trading symbol
     * @param interval Data interval (e.g., "1d", "5m")
     * @param fromDate Start date in format yyyy-MM-dd
     * @param toDate End date in format yyyy-MM-dd
     * @return Historical data if found, null otherwise
     */
    HistoricalData getHistoricalData(String symbol, String interval, String fromDate, String toDate);
}
