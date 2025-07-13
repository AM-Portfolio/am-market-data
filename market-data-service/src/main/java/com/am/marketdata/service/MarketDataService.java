package com.am.marketdata.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.am.common.investment.model.equity.Instrument;
import com.am.common.investment.model.historical.HistoricalData;

/**
 * Service for market data operations
 * Handles all market data processing logic including fetching, validation, and processing
 */
public interface MarketDataService {
    
    /**
     * Get login URL for authentication
     * @return Login URL and provider information
     */
    Map<String, String> getLoginUrl();
    
    /**
     * Generate session from request token
     * @param requestToken Request token
     * @return Session information
     */
    Object generateSession(String requestToken);
    
    /**
     * Get quotes for instruments
     * @param instruments Array of instrument identifiers
     * @return Map of instrument to quote data
     */
    Map<String, Object> getQuotes(String[] instruments);
    
    /**
     * Get OHLC data for instruments
     * @param instruments Array of instrument identifiers
     * @return Map of instrument to OHLC data
     */
    Map<String, Object> getOHLC(String[] instruments);
    
    /**
     * Get last traded price for instruments
     * @param instruments Array of instrument identifiers
     * @return Map of instrument to LTP data
     */
    Map<String, Object> getLTP(String[] instruments);
    
    /**
     * Get historical data for an instrument
     * @param instrumentId Instrument identifier
     * @param fromDate From date
     * @param toDate To date
     * @param interval Interval (minute, day, etc.)
     * @param continuous Continuous flag
     * @param additionalParams Additional parameters
     * @return Historical data
     */
    HistoricalData getHistoricalData(
            String instrumentId, 
            Date fromDate, 
            Date toDate, 
            String interval, 
            boolean continuous, 
            Map<String, Object> additionalParams);
    
    /**
     * Get all available instruments
     * @return List of instruments
     */
    List<Instrument> getAllInstruments();
    
    /**
     * Get paginated and filtered instruments
     * 
     * @param page Page number (0-based)
     * @param size Number of records per page
     * @param symbol Filter by trading symbol (optional)
     * @param type Filter by instrument type (optional)
     * @param exchange Filter by exchange (optional)
     * @return Filtered and paginated list of instruments
     */
    List<Instrument> getInstrumentPagination(int page, int size, String symbol, String type, String exchange);
    
    /**
     * Get instruments for a specific exchange
     * @param exchange Exchange name
     * @return List of instruments for the exchange
     */
    List<Object> getInstrumentsForExchange(String exchange);
    
    /**
     * Logout and invalidate session
     * @return Success status and provider information
     */
    Map<String, Object> logout();
}
