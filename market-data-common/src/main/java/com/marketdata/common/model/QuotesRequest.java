package com.marketdata.common.model;

import com.am.marketdata.common.model.TimeFrameV1;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Request model for quotes endpoint with TimeFrameV1 support
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotesRequest {
    private String symbols;
    private String timeFrame = TimeFrameV1.FIVE_MINUTE.getApiValue(); // Default to 5-minute timeframe
    private boolean forceRefresh = false;
    private boolean indexSymbol = false;
}
