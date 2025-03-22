package com.am.marketdata.scraper.client.handler;

import com.am.marketdata.common.model.NseETFResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for ETF API requests
 */
@Slf4j
@Component
public class ETFRequestHandler extends AbstractRequestHandler<NseETFResponse> {
    
    private static final String ENDPOINT = "/api/etf";
    private static final String METRIC_NAME = "etf";
    
    public ETFRequestHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }
    
    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }
    
    @Override
    public Class<NseETFResponse> getResponseType() {
        return NseETFResponse.class;
    }
    
    @Override
    public void logResponse(NseETFResponse response) {
        log.info("ETF Response - Raw: {}", toJsonSafely(response));
        if (response != null && response.getData() != null) {
            log.info("ETF Summary - Count: {}, First ETF: {}", 
                response.getData().size(),
                response.getData().isEmpty() ? "none" : 
                    String.format("%s (Last: %.2f, Change: %.2f%%)", 
                        response.getData().get(0).getSymbol(),
                        response.getData().get(0).getLastTradedPrice(),
                        response.getData().get(0).getPercentChange()
                    )
            );
        }
    }
    
    @Override
    public String getMetricName() {
        return METRIC_NAME;
    }
}
