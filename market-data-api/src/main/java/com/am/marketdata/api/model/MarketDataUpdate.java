package com.am.marketdata.api.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class MarketDataUpdate {
    private String provider;
    private String symbol;
    private Double ltp;
    private Long timestamp;
    private Map<String, com.am.marketdata.common.model.OHLCQuote> quotes;
    private Map<String, Object> additionalData; // Generic fallback
}
