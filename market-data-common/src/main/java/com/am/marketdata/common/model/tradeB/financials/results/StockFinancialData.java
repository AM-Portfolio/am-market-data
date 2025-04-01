package com.am.marketdata.common.model.tradeB.financials.results;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockFinancialData {
    private Map<String, QuarterlyFinancialMetrics> quarterlyData;
}
