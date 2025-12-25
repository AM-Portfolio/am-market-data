package com.am.marketdata.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import com.am.marketdata.common.model.TimeFrame;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Request DTO for historical data API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoricalDataRequest {

    private String symbols;

    private String from;

    private String to;

    @Builder.Default
    private TimeFrame interval = TimeFrame.MINUTE;

    private boolean continuous;

    private String instrumentType;

    private boolean forceRefresh;

    @Builder.Default
    private String filterType = "ALL";

    @Builder.Default
    private int filterFrequency = 1;

    private Map<String, Object> additionalParams;
}
