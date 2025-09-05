package com.am.marketdata.common.model;

/**
 * Enum representing time frames/intervals for historical data.
 * Maps user-friendly values to Zerodha API compatible values.
 * For timeframes not directly supported by Zerodha, client-side aggregation is used.
 */
public enum TimeFrame {
    MINUTE("minute", "minute"),
    THREE_MINUTE("3min", "3minute"),
    FIVE_MINUTE("5min", "5minute"),
    TEN_MINUTE("10min", "10minute"),
    FIFTEEN_MINUTE("15min", "15minute"),
    THIRTY_MINUTE("30min", "30minute"),
    HOUR("hour", "60minute"),
    FOUR_HOUR("4hour", "60minute", true, 4), // 4-hour - requires aggregation from hourly data
    DAY("day", "day"),
    WEEK("week", "day", true, 7), // Weekly - requires aggregation from daily data
    MONTH("month", "day", true, 30), // Monthly - requires aggregation from daily data
    YEAR("year", "day", true, 365); // Yearly - requires aggregation from daily data

    private final String userValue;
    private final String zerodhaValue;
    private final boolean requiresAggregation;
    private final int aggregationFactor;

    TimeFrame(String userValue, String zerodhaValue) {
        this(userValue, zerodhaValue, false, 1);
    }

    TimeFrame(String userValue, String zerodhaValue, boolean requiresAggregation, int aggregationFactor) {
        this.userValue = userValue;
        this.zerodhaValue = zerodhaValue;
        this.requiresAggregation = requiresAggregation;
        this.aggregationFactor = aggregationFactor;
    }

    /**
     * Get the user-friendly value
     * @return User value
     */
    public String getUserValue() {
        return userValue;
    }

    /**
     * Get the value used in API requests
     * @return API value
     */
    public String getApiValue() {
        return userValue;
    }

    /**
     * Get the value used for Zerodha API
     * @return Zerodha API value
     */
    public String getZerodhaValue() {
        return zerodhaValue;
    }

    /**
     * Check if this timeframe requires client-side aggregation
     * @return true if aggregation is required
     */
    public boolean requiresAggregation() {
        return requiresAggregation;
    }

    /**
     * Get the factor by which to aggregate base timeframe data
     * For example, FOUR_HOUR aggregates 4 hourly candles
     * @return aggregation factor
     */
    public int getAggregationFactor() {
        return aggregationFactor;
    }

    /**
     * Get the base timeframe used for fetching data when aggregation is required
     * @return base TimeFrame for aggregation
     */
    public TimeFrame getBaseTimeFrame() {
        if (!requiresAggregation) {
            return this;
        }
        
        switch (this) {
            case FOUR_HOUR:
                return HOUR;
            case WEEK:
            case MONTH:
            case YEAR:
                return DAY;
            default:
                return this;
        }
    }

    /**
     * Find TimeFrame by API value
     * @param value API value to search for
     * @return Matching TimeFrame or null if not found
     * @throws IllegalArgumentException if value is null or no matching TimeFrame is found
     */
    public static TimeFrame fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("API value cannot be null");
        }
        
        for (TimeFrame timeFrame : TimeFrame.values()) {
            if (timeFrame.getUserValue().equalsIgnoreCase(value) || 
                timeFrame.getApiValue().equalsIgnoreCase(value)) {
                return timeFrame;
            }
        }
        
        throw new IllegalArgumentException("No TimeFrame found for API value: " + value);
    }

    /**
     * Convert API value to Zerodha value
     * @param apiValue API value to convert
     * @return Zerodha compatible value
     * @throws IllegalArgumentException if apiValue is null or no matching TimeFrame is found
     */
    public static String toZerodhaValue(String apiValue) {
        return fromApiValue(apiValue).getZerodhaValue();
    }
}
