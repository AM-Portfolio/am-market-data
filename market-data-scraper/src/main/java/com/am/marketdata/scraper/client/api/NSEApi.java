package com.am.marketdata.scraper.client.api;

import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.NseETFResponse;
import org.springframework.http.HttpHeaders;

/**
 * Interface for NSE API operations
 * Defines the contract for all NSE API interactions
 */
public interface NSEApi {
    
    /**
     * Get ETF data from NSE API
     * 
     * @return ETF response data
     */
    NseETFResponse getETFs();
    
    /**
     * Get stock data by index symbol
     * 
     * @param indexSymbol The index symbol to get stock data for
     * @return Stock data for the specified index
     */
    NSEStockInsidicesData getStockbyInsidices(String indexSymbol);
    
    /**
     * Get all indices data from NSE API
     * 
     * @return All indices data
     */
    NSEIndicesResponse getAllIndices();
    
    /**
     * Fetch cookies from NSE API
     * 
     * @return HTTP headers containing cookies
     */
    HttpHeaders fetchCookies();
}
