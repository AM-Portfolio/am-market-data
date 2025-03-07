package com.am.marketdata.upstock.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class OHLCResponse {
    private String status;
    private Map<String, OHLCData> data;

    @Data
    public static class OHLCData {
        private Double open;
        private Double high;
        private Double low;
        private Double close;
        @JsonProperty("last_price")
        private Double lastPrice;
        private Double volume;
        @JsonProperty("average_price")
        private Double averagePrice;
    }
} 