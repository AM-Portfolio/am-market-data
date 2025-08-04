package com.am.marketdata.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.Instrument;
import com.am.common.investment.model.historical.HistoricalData;
import com.zerodhatech.models.OHLCQuote;

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
     * Get quotes for symbols
     * @param symbols Array of symbol identifiers
     * @return Map of symbol to quote data
     */
    Map<String, Object> getQuotes(String[] symbols);
    
    /**
     * Get OHLC data for symbols
     * @param symbols Array of symbol identifiers
     * @return Map of symbol to OHLC data
     */
    Map<String, OHLCQuote> getOHLC(String[] symbols);
    
    /**
     * Get last traded price for instruments
     * @param symbols Array of instrument identifiers
     * @return Map of instrument to LTP data
     */
    Map<String, Object> getLTP(String[] symbols);
    
    /**
     * Get historical data for an instrument
     * @param symbol Trading symbol
     * @param fromDate From date
     * @param toDate To date
     * @param interval Interval (minute, day, etc.)
     * @param continuous Continuous flag
     * @param additionalParams Additional parameters
     * @return Historical data
     */
    HistoricalData getHistoricalData(
            String symbol, 
            Date fromDate, 
            Date toDate, 
            String interval, 
            boolean continuous, 
            Map<String, Object> additionalParams);
    
    /**
     * Get all available symbols
     * @return List of symbols
     */
    List<Instrument> getAllSymbols();
    
    /**
     * Get paginated and filtered symbols
     * 
     * @param page Page number (0-based)
     * @param size Number of records per page
     * @param symbol Filter by trading symbol (optional)
     * @param type Filter by symbol type (optional)
     * @param exchange Filter by exchange (optional)
     * @return Filtered and paginated list of symbols
     */
    List<Instrument> getSymbolPagination(int page, int size, String symbol, String type, String exchange);
    
    /**
     * Get symbols for a specific exchange
     * @param exchange Exchange name
     * @return List of symbols for the exchange
     */
    List<Object> getSymbolsForExchange(String exchange);
    
    /**
     * Logout and invalidate session
     * @return Success status and provider information
     */
    Map<String, Object> logout();
    
    /**
     * Get live prices for all symbols or filtered by symbol IDs
     * @param symbols Optional list of symbol IDs to filter by (if null or empty, returns all available prices)
     * @return List of equity prices with current market data
     */
    List<EquityPrice> getLivePrices(List<String> symbols);
}
