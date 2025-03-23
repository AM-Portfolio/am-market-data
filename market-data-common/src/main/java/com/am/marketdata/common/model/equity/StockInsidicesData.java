package com.am.marketdata.common.model.equity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain model for stock indices data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockInsidicesData {
    
    private String name;
    private Advance advance;
    private String timestamp;
    private List<StockData> data;
    private Metadata metadata;
    private MarketStatus marketStatus;
    private String date30dAgo;
    private String date365dAgo;

    @Data
    @AllArgsConstructor
    @SuperBuilder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Advance {
        private Integer declines;
        private Integer advances;
        private Integer unchanged;
    }

    @Data
    @AllArgsConstructor
    @SuperBuilder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockData {
        private int priority;
        private String symbol;
        private String identifier;
        private String series;
        private Double open;
        private Double dayHigh;
        private Double dayLow;
        private Double lastPrice;
        private Double previousClose;
        private Double change;
        private Double pChange;
        private long totalTradedVolume;
        private Double stockIndClosePrice;
        private Double totalTradedValue;
        private Double yearHigh;
        private Double ffmc;
        private Double yearLow;
        private Double nearWKH;
        private Double nearWKL;
        private String perChange365d;
        private String date365dAgo;
        private String date30dAgo;
        private Double perChange30d;
    }
    
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String indexName;
        private Double open;
        private Double high;
        private Double low;
        private Double previousClose;
        private Double last;
        private Double percChange;
        private Double change;
        private String timeVal;
        private Double yearHigh;
        private Double yearLow;
        private Double indicativeClose;
        private long totalTradedVolume;
        private Double totalTradedValue;
        @JsonProperty("ffmc_sum")
        private double ffmcSum;
    }

    @Data
    @SuperBuilder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MarketStatus {
        private String market;
        private String marketStatus;
        private String tradeDate;
        private String index;
        private Double last;
        private Double variation;
        private Double percentChange;
        private String marketStatusMessage;
    }
}
