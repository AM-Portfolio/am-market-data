package com.am.marketdata.api.service;

import com.am.marketdata.common.model.NSEStockInsidicesData;

import java.util.List;

/**
 * Interface for Stock Indices API Service
 */
public interface StockIndicesApiService {
    /**
     * Get stock indices data for a specific index
     * 
     * @param indexSymbol  The index symbol (e.g., "NIFTY 50", "NIFTY BANK")
     * @param forceRefresh Whether to force refresh from source
     * @return Stock indices data
     */
    NSEStockInsidicesData getStockIndices(String indexSymbol, boolean forceRefresh);

    /**
     * Get stock indices data for multiple indices
     * 
     * @param indexSymbols List of index symbols
     * @param forceRefresh Whether to force refresh from source
     * @return List of stock indices data
     */
    List<NSEStockInsidicesData> getStockIndicesBatch(List<String> indexSymbols, boolean forceRefresh);

    /**
     * Get available index symbols
     * 
     * @return Map of broad market and sector indices
     */
    java.util.Map<String, List<String>> getAvailableIndices();
}
