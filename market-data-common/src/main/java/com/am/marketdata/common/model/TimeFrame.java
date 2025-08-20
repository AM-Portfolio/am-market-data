package com.am.marketdata.common.model;

/**
 * Enum representing time frames/intervals for historical data.
 * Maps user-friendly values to Zerodha API compatible values.
 */
public enum TimeFrame {
    MINUTE("minute", "minute"),
    THREE_MINUTE("3min", "3minute"),
    FIVE_MINUTE("5min", "5minute"),
    TEN_MINUTE("10min", "10minute"),
    FIFTEEN_MINUTE("15min", "15minute"),
    THIRTY_MINUTE("30min", "30minute"),
    HOUR("hour", "60minute"),
    DAY("day", "day");

    private final String apiValue;
    private final String zerodhaValue;

    TimeFrame(String apiValue, String zerodhaValue) {
        this.apiValue = apiValue;
        this.zerodhaValue = zerodhaValue;
    }

    /**
     * Get the value used in API requests
     * @return API value
     */
    public String getApiValue() {
        return apiValue;
    }

    /**
     * Get the value used for Zerodha API
     * @return Zerodha API value
     */
    public String getZerodhaValue() {
        return zerodhaValue;
    }

    /**
     * Find TimeFrame by API value
     * @param value API value to search for
     * @return Matching TimeFrame or DAY if not found
     */
    public static TimeFrame fromApiValue(String value) {
        if (value == null) {
            return DAY; // Default
        }
        
        for (TimeFrame timeFrame : TimeFrame.values()) {
            if (timeFrame.getApiValue().equalsIgnoreCase(value)) {
                return timeFrame;
            }
        }
        
        return DAY; // Default to day if not found
    }

    /**
     * Convert API value to Zerodha value
     * @param apiValue API value to convert
     * @return Zerodha compatible value
     */
    public static String toZerodhaValue(String apiValue) {
        return fromApiValue(apiValue).getZerodhaValue();
    }
}
