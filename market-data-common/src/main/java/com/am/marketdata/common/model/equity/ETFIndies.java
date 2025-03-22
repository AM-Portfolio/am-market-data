package com.am.marketdata.common.model.equity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.am.common.investment.model.equity.MarketData;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Domain model for ETF indices data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  
public class ETFIndies {
    
    private String symbol;
    private String assets;
    private LocalDateTime timestamp;
    private MarketData marketData;
    private MetaData metaData;
}
