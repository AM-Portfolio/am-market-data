package com.am.marketdata.upstock.model;

import lombok.Data;
import java.util.List;

@Data
public class HistoricalDataResponse {
    private String status;
    private List<Candle> data;

    @Data
    public static class Candle {
        private String timestamp;
        private Double open;
        private Double high;
        private Double low;
        private Double close;
        private Long volume;
        private Long oi;
    }
} 