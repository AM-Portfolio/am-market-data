package com.am.marketdata.kafka.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.am.marketdata.common.model.NSEIndex;

@Data
@Builder
public class NSEIndicesEvent {
    private List<NSEIndex> indices;
    private LocalDateTime timestamp;
}
