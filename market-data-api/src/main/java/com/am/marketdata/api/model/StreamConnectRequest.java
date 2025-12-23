package com.am.marketdata.api.model;

import java.util.List;
import lombok.Data;

@Data
public class StreamConnectRequest {
    private String provider; // e.g., "UPSTOX"
    private List<String> instrumentKeys;
    private String mode; // e.g., "FULL", "LTPC"
}
