package com.am.marketdata.upstock.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class OHLCResponse {
    private static final Logger log = LoggerFactory.getLogger(OHLCResponse.class);
    private String status;
    private Map<String, OHLCData> data;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, OHLCData> getData() { return data; }
    public void setData(Map<String, OHLCData> data) { this.data = data; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OHLCData {
        @JsonProperty("ohlc")
        private OHLC ohlc;
        
        @JsonProperty("last_price")
        private Double lastPrice;
        
        @JsonProperty("instrument_token")
        private String instrumentToken;

        public OHLC getOhlc() { return ohlc; }
        public void setOhlc(OHLC ohlc) { this.ohlc = ohlc; }
        public Double getLastPrice() { return lastPrice; }
        public void setLastPrice(Double lastPrice) { this.lastPrice = lastPrice; }
        public String getInstrumentToken() { return instrumentToken; }
        public void setInstrumentToken(String instrumentToken) { this.instrumentToken = instrumentToken; }

        public Double getOpen() {
            return ohlc != null ? ohlc.getOpen() : null;
        }

        public Double getHigh() {
            return ohlc != null ? ohlc.getHigh() : null;
        }

        public Double getLow() {
            return ohlc != null ? ohlc.getLow() : null;
        }

        public Double getClose() {
            return ohlc != null ? ohlc.getClose() : null;
        }

        public String getISIN() {
            if (instrumentToken == null) {
                log.debug("instrumentToken is null");
                return null;
            }
            log.debug("Processing instrumentToken: {}", instrumentToken);
            // Extract ISIN from format like "NSE_EQ|INF204KB16I7"
            int pipeIndex = instrumentToken.indexOf('|');
            if (pipeIndex >= 0 && pipeIndex + 1 < instrumentToken.length()) {
                String isin = instrumentToken.substring(pipeIndex + 1);
                log.debug("Extracted ISIN: {}", isin);
                return isin;
            }
            log.debug("Could not extract ISIN from instrumentToken");
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OHLC {
        private Double open;
        private Double high;
        private Double low;
        private Double close;

        public Double getOpen() { return open; }
        public void setOpen(Double open) { this.open = open; }
        public Double getHigh() { return high; }
        public void setHigh(Double high) { this.high = high; }
        public Double getLow() { return low; }
        public void setLow(Double low) { this.low = low; }
        public Double getClose() { return close; }
        public void setClose(Double close) { this.close = close; }
    }
}