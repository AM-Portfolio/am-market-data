package com.am.marketdata.api.service;

import com.am.marketdata.common.model.NSEIndex;

import java.util.List;

/**
 * Interface for Market Index API Service
 */
public interface MarketIndexApiService {
    /**
     * Get all market indices
     */
    List<NSEIndex> getAllIndices();

    /**
     * Get a specific index by symbol
     */
    NSEIndex getIndex(String symbol);
}
