package com.am.marketdata.api.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service interface for unified access to investment instruments data (stocks, mutual funds, options, etc.)
 * This service acts as a facade for the underlying market data services while keeping
 * business logic out of controllers.
 */
public interface InvestmentInstrumentService {

    /**
     * Get live prices for instruments (stocks, ETFs, etc.) by trading symbols
     * 
     * @param symbols Optional list of trading symbols (null or empty for all available instruments)
     * @return Map containing prices, count, timestamp and processing time
     */
    Map<String, Object> getLivePrices(List<String> symbols);
    
    /**
     * Get historical data for any instrument type
     * 
     * @param symbol Trading symbol
     * @param fromDate Start date
     * @param toDate End date
     * @param interval Data interval (minute, day, etc.)
     * @param instrumentType Type of instrument (STOCK, OPTION, MUTUAL_FUND, etc.)
     * @param additionalParams Additional parameters specific to instrument type
     * @return Historical data response with metadata
     */
    Map<String, Object> getHistoricalData(String symbol, Date fromDate, Date toDate, 
                                         String interval, String instrumentType, Map<String, Object> additionalParams);
    
    /**
     * Search for instruments across all types with pagination and filtering
     * 
     * @param page Page number (0-based)
     * @param size Number of records per page
     * @param symbol Filter by trading symbol (optional)
     * @param type Filter by instrument type (optional)
     * @param exchange Filter by exchange (optional)
     * @return Map containing instruments, pagination info and metadata
     */
    Map<String, Object> searchInstruments(int page, int size, String symbol, String type, String exchange);
    
    /**
     * Get quotes for a list of instruments
     * @param tradingSymbols List of trading symbols
     * @return Map containing quote data for each symbol
     */
    Map<String, Map<String, Object>> getQuotes(List<String> tradingSymbols);
    
    /**
     * Get option chain data for a given underlying instrument
     * 
     * @param underlyingSymbol Symbol of the underlying instrument
     * @param expiryDate Optional expiry date, if null will return the nearest expiry
     * @return Option chain data with calls and puts
     */
    Map<String, Object> getOptionChain(String underlyingSymbol, Date expiryDate);
    
    /**
     * Get mutual fund details including NAV, returns, etc.
     * 
     * @param schemeCode Mutual fund scheme code
     * @return Mutual fund details
     */
    Map<String, Object> getMutualFundDetails(String schemeCode);
    
    /**
     * Get mutual fund NAV history
     * 
     * @param schemeCode Mutual fund scheme code
     * @param from Start date
     * @param to End date
     * @return NAV history data
     */
    Map<String, Object> getMutualFundNavHistory(String schemeCode, Date from, Date to);
}
