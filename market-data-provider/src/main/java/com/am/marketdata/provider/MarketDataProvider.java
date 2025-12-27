package com.am.marketdata.provider;

import com.marketdata.common.dto.*;
import com.marketdata.common.model.*;
import java.util.*;

/**
 * Provider interface for market data operations.
 * Implementations: Upstox, Zerodha, etc.
 * 
 * This interface defines the contract that all provider implementations must follow.
 * Service layer will depend ONLY on this interface, not on specific implementations.
 */
public interface MarketDataProvider {
    
    /**
     * Get real-time quotes for given symbols
     * @param symbols List of trading symbols
     * @return Map of symbol to OHLC quote
     */
    Map<String, OHLCQuote> getQuotes(List<String> symbols);
    
    /**
     * Get historical OHLC data
     * @param symbols List of trading symbols
     * @param from Start date (YYYY-MM-DD)
     * @param to End date (YYYY-MM-DD)
     * @param interval Time interval (1d, 1h, 5m, etc.)
     * @return Map of symbol to list of OHLC data
     */
    Map<String, List<OHLCData>> getHistoricalData(
        List<String> symbols, 
        String from, 
        String to, 
        String interval
    );
    
    /**
     * Get all instruments for an exchange
     * @param exchange Exchange name (NSE, BSE, etc.)
     * @return List of instruments
     */
    List<Instrument> getInstruments(String exchange);
    
    /**
     * Search instruments by symbol or name
     * @param query Search query
     * @return List of matching instruments
     */
    List<Instrument> searchInstruments(String query);
    
    /**
     * Login to provider with credentials
     * @param apiKey Provider API key
     * @param apiSecret Provider API secret
     */
    void login(String apiKey, String apiSecret);
    
    /**
     * Logout from provider
     */
    void logout();
    
    /**
     * Check if session is valid
     * @return true if session is active
     */
    boolean isSessionValid();
    
    /**
     * Get provider name (upstox, zerodha, etc.)
     * @return Provider name
     */
    String getProviderName();
    
    /**
     * Generate session/access token
     * @param requestToken Request token from OAuth flow
     * @return Access token
     */
    String generateSession(String requestToken);
}
