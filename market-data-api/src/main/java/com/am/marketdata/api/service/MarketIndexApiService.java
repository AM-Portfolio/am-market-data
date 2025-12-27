package com.am.marketdata.api.service;

import com.am.marketdata.common.model.NSEIndexV1;

import java.util.List;

/**
 * Interface for Market Index API Service
 */
public interface MarketIndexApiService {
    /**
     * Get all market indices
     */
    List<NSEIndexV1> getAllIndices();

    /**
     * Get a specific index by symbol
     */
    NSEIndexV1 getIndex(String symbol);
}
