package com.am.marketdata.service;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.marketdata.common.model.TimeFrameV1;

import java.util.Date;

/**
 * Service for aggregating historical data into higher timeframes
 * that are not directly supported by data providers.
 */
public interface TimeFrameAggregationService {

    /**
     * Aggregate historical data to a higher timeframe
     * 
     * @param sourceData      The source historical data in a lower timeframe
     * @param targetTimeFrame The target TimeFrameV1 to aggregate to
     * @return Aggregated historical data
     */
    HistoricalData aggregateTimeFrame(HistoricalData sourceData, TimeFrameV1 targetTimeFrame);

    /**
     * Check if a TimeFrameV1 requires client-side aggregation
     * 
     * @param TimeFrameV1 The TimeFrameV1 to check
     * @return true if the TimeFrameV1 requires aggregation
     */
    boolean requiresAggregation(TimeFrameV1 timeFrame);

    /**
     * Get the base TimeFrameV1 needed to aggregate to the target timeframe
     * 
     * @param targetTimeFrame The target timeframe
     * @return The base TimeFrameV1 needed for aggregation
     */
    TimeFrameV1 getBaseTimeFrame(TimeFrameV1 targetTimeFrame);

    /**
     * Calculate the adjusted date range needed to fetch enough data for aggregation
     * 
     * @param fromDate        Original from date
     * @param toDate          Original to date
     * @param targetTimeFrame Target timeframe
     * @return Adjusted date range as [fromDate, toDate]
     */
    Date[] calculateAdjustedDateRange(Date fromDate, Date toDate, TimeFrameV1 targetTimeFrame);
}
