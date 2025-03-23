package com.am.marketdata.scraper.client.handler;

import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Handler for Stock Indices API requests
 */
@Slf4j
@Component
public class StockIndicesRequestHandler extends AbstractRequestHandler<NSEStockInsidicesData> {
    
    private static final String ENDPOINT_PREFIX = "/api/equity-stockIndices?index=";
    private static final String METRIC_NAME = "stock-insidices";
    
    private String indexSymbol;
    
    public StockIndicesRequestHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }
    
    /**
     * Set the index symbol for this request
     * 
     * @param indexSymbol The index symbol to request
     * @return This handler instance for method chaining
     */
    public StockIndicesRequestHandler withIndexSymbol(String indexSymbol) {
        this.indexSymbol = indexSymbol;
        return this;
    }
    
    @Override
    public String getEndpoint() {
        return ENDPOINT_PREFIX + indexSymbol;
    }
    
    @Override
    public Class<NSEStockInsidicesData> getResponseType() {
        return NSEStockInsidicesData.class;
    }
    
    @Override
    public void logResponse(NSEStockInsidicesData response) {
        log.info("Market Insidices Response - Raw: {}", toJsonSafely(response));
        if (response != null && response.getData() != null) {
            log.info("Market Insidices Summary - Count: {}", response.getData().size());
            String indexNames = response.getData().stream()
                .map(data -> String.format("%s (Last: %.2f, Change: %.2f%%)", 
                    data.getSymbol(),
                    data.getLastPrice(),
                    data.getPChange()))
                .collect(Collectors.joining(", "));
            log.info("Market Insidices - Indexes: {}", indexNames);
        }
    }
    
    @Override
    public String getMetricName() {
        return METRIC_NAME;
    }
}
