package com.am.marketdata.upstock.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.am.marketdata.upstock.model.common.StockQuote;
import java.util.Map;

public class MarketQuoteResponse {
    private String status;
    
    @JsonProperty("data")
    private Map<String, StockQuote> data;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, StockQuote> getData() { return data; }
    public void setData(Map<String, StockQuote> data) { this.data = data; }
}