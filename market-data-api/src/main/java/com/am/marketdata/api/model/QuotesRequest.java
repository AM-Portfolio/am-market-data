package com.am.marketdata.api.model;

import com.am.marketdata.common.model.TimeFrame;
import lombok.Data;

/**
 * Request model for quotes endpoint with timeframe support
 */
@Data
public class QuotesRequest {
    private String symbols;
    private TimeFrame timeFrame = TimeFrame.FIVE_MINUTE; // Default to 5-minute timeframe
    private boolean forceRefresh = false;
    private boolean indexSymbol = false;
}
