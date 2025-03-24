package com.am.marketdata.kafka.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

import com.am.marketdata.common.model.stockindices.StockInsidicesData;

@Data
@Builder
public class StockIndicesPriceUpdateEvent {
    private String eventType;
    private LocalDateTime timestamp;
    private StockInsidicesData stockIndices;
}