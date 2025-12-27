package com.am.marketdata.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.am.marketdata.common.util.CustomDoubleDeserializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified DTO for real-time market data updates via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketDataUpdateV1 {

    private String instrumentKey;

    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double lastPrice;

    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double change;

    @JsonProperty("pChange")
    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double pChange;

    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double open;

    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double high;

    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double low;

    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double previousClose;

    private Long volume;

    private String timestamp;
}
