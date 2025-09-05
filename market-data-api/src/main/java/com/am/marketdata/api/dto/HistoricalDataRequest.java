package com.am.marketdata.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import com.am.marketdata.common.model.TimeFrame;

/**
 * Request DTO for historical data API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalDataRequest {
    
    private String symbols;
    
    private String from;
    
    private String to;
    
    private TimeFrame interval;
    
    private boolean continuous;
    
    private String instrumentType;
    
    private boolean forceRefresh;
    
    @Builder.Default
    private String filterType = "ALL";

    @Builder.Default
    private int filterFrequency = 1;
    
    private Map<String, Object> additionalParams;
}
