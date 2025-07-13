package com.marketdata.common;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Instrument;

/**
 * Common interface for market data providers (Zerodha, Upstox, etc.)
 * This abstraction allows switching between different providers through configuration
 */
public interface MarketDataProvider {
    
    /**
     * Initialize the provider
     */
    void initialize();
    
    /**
     * Clean up resources
     */
    void cleanup();
    
    /**
     * Set access token for authentication
     * @param accessToken Access token
     */
    void setAccessToken(String accessToken);
    
    /**
     * Get login URL for authentication
     * @return Login URL
     */
    String getLoginUrl();
    
    /**
     * Generate session from request token
     * @param requestToken Request token
     * @return Session information
     */
    Object generateSession(String requestToken);
    
    /**
     * Get quotes for instruments
     * @param instruments Array of instruments
     * @return Map of instrument to quote data
     */
    Map<String, Object> getQuotes(String[] instruments);
    
    /**
     * Get OHLC data for instruments
     * @param instruments Array of instruments
     * @return Map of instrument to OHLC data
     */
    Map<String, Object> getOHLC(String[] instruments);
    
    /**
     * Get last traded price for instruments
     * @param instruments Array of instruments
     * @return Map of instrument to LTP data
     */
    Map<String, Object> getLTP(String[] instruments);
    
    /**
     * Get historical data for an instrument
     * @param instrumentId Instrument identifier
     * @param from From date
     * @param to To date
     * @param interval Interval (minute, day, etc.)
     * @param continuous Continuous flag
     * @param additionalParams Additional provider-specific parameters
     * @return Historical data
     */
    HistoricalData getHistoricalData(String instrumentId, Date from, Date to, String interval, 
                            boolean continuous, Map<String, Object> additionalParams);
    
    /**
     * Initialize ticker for real-time data
     * @param instrumentIds List of instrument identifiers
     * @param tickListener Callback for tick data
     * @return Ticker instance
     */
    Object initializeTicker(List<String> instrumentIds, Object tickListener);
    
    /**
     * Check if ticker is connected
     * @return true if connected
     */
    boolean isTickerConnected();
    
    /**
     * Get all available instruments
     * @return List of instruments
     */
    List<Instrument> getAllInstruments();
    
    /**
     * Get instruments for a specific exchange
     * @param exchange Exchange name
     * @return List of instruments
     */
    List<Object> getInstrumentsForExchange(String exchange);
    
    /**
     * Execute operation asynchronously
     * @param operation Operation to execute
     * @param <T> Return type
     * @return CompletableFuture with result
     */
    <T> CompletableFuture<T> executeAsync(ProviderOperation<T> operation);
    
    /**
     * Logout and invalidate session
     * @return true if successful
     */
    boolean logout();
    
    /**
     * Get provider name
     * @return Provider name (e.g., "zerodha", "upstox")
     */
    String getProviderName();
    
    /**
     * Functional interface for provider operations
     * @param <T> Return type
     */
    @FunctionalInterface
    interface ProviderOperation<T> {
        T execute() throws Exception;
    }
}
