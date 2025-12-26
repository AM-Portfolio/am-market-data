package com.am.marketdata.api.model;

import java.util.List;
import lombok.Data;

@Data
public class StreamConnectRequest {
    private String provider; // e.g., "UPSTOX"
    private List<String> instrumentKeys;
    private String mode; // e.g., "FULL", "LTPC"
    private Boolean expandIndices = false; // Whether to expand index symbols to constituents (default: false)
    private String timeFrame = "1D"; // TimeFrame for historical data calculation (default: 1D)
    private Boolean isIndexSymbol = false; // Whether the symbol is an index (for caching purposes)
    private Boolean stream = true; // Whether to start a continuous stream (default: true)
}
