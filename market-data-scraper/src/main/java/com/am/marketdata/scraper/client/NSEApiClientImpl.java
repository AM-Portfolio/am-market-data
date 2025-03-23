package com.am.marketdata.scraper.client;

import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.NseETFResponse;
import com.am.marketdata.scraper.client.api.NSEApi;
import com.am.marketdata.scraper.client.executor.RequestExecutor;
import com.am.marketdata.scraper.client.handler.ETFRequestHandler;
import com.am.marketdata.scraper.client.handler.IndicesRequestHandler;
import com.am.marketdata.scraper.client.handler.StockIndicesRequestHandler;
import com.am.marketdata.scraper.exception.NSEApiException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.web.client.RestTemplate;


/**
 * Implementation of NSE API client
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NSEApiClientImpl implements NSEApi {

    private final RequestExecutor requestExecutor;
    private final ObjectMapper objectMapper;

    @Autowired
    @Qualifier("nseRestTemplate")
    private RestTemplate restTemplate;

    @Value("${nse.api.base-url:https://www.nseindia.com}")
    private String baseUrl;

    private ETFRequestHandler etfRequestHandler;
    private IndicesRequestHandler indicesRequestHandler;
    private StockIndicesRequestHandler stockIndicesRequestHandler;

    @PostConstruct
    public void init() {
        log.info("Initializing NSE API Client with base URL: {}", baseUrl);
        
        etfRequestHandler = new ETFRequestHandler(objectMapper);
        indicesRequestHandler = new IndicesRequestHandler(objectMapper);
        stockIndicesRequestHandler = new StockIndicesRequestHandler(objectMapper);
    }

    @Override
    public NseETFResponse getETFs() throws NSEApiException {
        //return requestExecutor.execute(etfRequestHandler);
        return null;
    }

    @Override
    public NSEStockInsidicesData getStockbyInsidices(String index) throws NSEApiException {
        // return requestExecutor.execute(
        //     stockIndicesRequestHandler.withIndexSymbol(index)
        // );
        return null;
    }

    @Override
    public NSEIndicesResponse getAllIndices() throws NSEApiException {
        return requestExecutor.execute(indicesRequestHandler);
    }
}
