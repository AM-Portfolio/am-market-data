package com.am.marketdata.scraper.client.handler;

import com.am.marketdata.common.model.NSEIndicesResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for Indices API requests
 */
@Slf4j
@Component
public class IndicesRequestHandler extends AbstractRequestHandler<NSEIndicesResponse> {
    
    private static final String ENDPOINT = "/api/allIndices";
    private static final String METRIC_NAME = "indices";
    
    public IndicesRequestHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }
    
    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }
    
    @Override
    public Class<NSEIndicesResponse> getResponseType() {
        return NSEIndicesResponse.class;
    }
    
    @Override
    public void logResponse(NSEIndicesResponse response) {
        log.info("Indices Response - Raw: {}", toJsonSafely(response));
        if (response != null && response.getData() != null) {
            log.info("Indices Summary - Count: {}, First Index: {}", 
                response.getData().size(),
                response.getData().isEmpty() ? "none" : 
                    String.format("%s (Last: %.2f, Change: %.2f%%)", 
                        response.getData().get(0).getIndexSymbol(),
                        response.getData().get(0).getLast(),
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
