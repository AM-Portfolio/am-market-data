package com.am.marketdata.upstock.model;

import java.util.List;

public class HistoricalDataResponse {
    private String status;
    private List<Candle> data;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Candle> getData() { return data; }
    public void setData(List<Candle> data) { this.data = data; }

    public static class Candle {
        private String timestamp;
        private Double open;
        private Double high;
        private Double low;
        private Double close;
        private Long volume;
        private Long oi;

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public Double getOpen() { return open; }
        public void setOpen(Double open) { this.open = open; }
        public Double getHigh() { return high; }
        public void setHigh(Double high) { this.high = high; }
        public Double getLow() { return low; }
        public void setLow(Double low) { this.low = low; }
        public Double getClose() { return close; }
        public void setClose(Double close) { this.close = close; }
        public Long getVolume() { return volume; }
        public void setVolume(Long volume) { this.volume = volume; }
        public Long getOi() { return oi; }
        public void setOi(Long oi) { this.oi = oi; }
    }
}