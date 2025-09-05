package com.am.marketdata.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request model for OHLC data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OHLCRequest {
    private String symbols;
    
    @JsonProperty("isIndexSymbol")
    private boolean indexSymbol = false;
    
    @JsonProperty("refresh")
    private boolean forceRefresh = false;
    
    public boolean isIndexSymbol() {
        return indexSymbol;
    }
    
    public void setIndexSymbol(boolean indexSymbol) {
        this.indexSymbol = indexSymbol;
    }
}
