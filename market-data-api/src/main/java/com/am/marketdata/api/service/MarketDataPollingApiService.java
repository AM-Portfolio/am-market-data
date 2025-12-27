package com.am.marketdata.api.service;

import java.util.Map;

/**
 * Interface for Market Data Polling API Service
 * Used for real-time or near-real-time data polling
 */
public interface MarketDataPollingApiService {
    /**
     * Poll live market data for given symbols
     * 
     * @param symbols     Comma-separated list of symbols
     * @param timeFrame   Time frame for the data
     * @param indexSymbol Whether symbols are index symbols
     * @return Polling response with market data
     */
    Map<String, Object> pollMarketData(String symbols, String timeFrame, boolean indexSymbol);

    /**
     * Get streaming connection status
     * 
     * @return Connection status information
     */
    Map<String, Object> getConnectionStatus();

    /**
     * Subscribe to symbols for polling
     * 
     * @param symbols List of symbols to subscribe
     * @return Subscription confirmation
     */
    Map<String, Object> subscribe(java.util.List<String> symbols);

    /**
     * Unsubscribe from symbols
     * 
     * @param symbols List of symbols to unsubscribe
     * @return Unsubscription confirmation
     */
    Map<String, Object> unsubscribe(java.util.List<String> symbols);
}
