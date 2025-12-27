package com.am.marketdata.api.service;

import java.util.List;
import java.util.Map;

/**
 * Interface for Market Analytics API Service
 */
public interface MarketAnalyticsApiService {
    /**
     * Get market summary analytics
     * 
     * @return Market summary data
     */
    Map<String, Object> getMarketSummary();

    /**
     * Get sector performance analytics
     * 
     * @return Sector-wise performance data
     */
    Map<String, Object> getSectorPerformance();

    /**
     * Get top gainers and losers
     * 
     * @param limit Number of stocks to return
     * @return Map containing gainers and losers
     */
    Map<String, List<Object>> getTopMovers(int limit);

    /**
     * Get market breadth (advances, declines, unchanged)
     * 
     * @return Market breadth data
     */
    Map<String, Object> getMarketBreadth();
}
