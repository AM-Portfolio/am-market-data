package com.am.marketdata.scraper.service.operation;

import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.equity.StockInsidicesData;
import com.am.marketdata.kafka.producer.KafkaProducerService;
import com.am.marketdata.scraper.client.api.NSEApi;
import com.am.marketdata.scraper.exception.DataFetchException;
import com.am.marketdata.scraper.service.common.AbstractMarketDataOperation;
import com.am.marketdata.scraper.service.common.DataFetcher;
import com.am.marketdata.scraper.service.common.DataProcessor;
import com.am.marketdata.scraper.service.common.DataValidator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Operation for fetching and processing NSE stock indices data
 */
@Slf4j
@Component
public class StockIndicesDataOperation extends AbstractMarketDataOperation<NSEStockInsidicesData, List<StockInsidicesData>> {
    
    private final NSEApi nseApi;
    private final KafkaProducerService kafkaProducer;
    @Qualifier("stockIndicesProcessingTimer")
    private final Timer fetchTimer;
    private NSEStockInsidicesData lastFetchedData;
    
    @Value("${market.data.max.retries:3}")
    private int maxRetries;
    
    @Value("${market.data.retry.delay.ms:1000}")
    private long retryDelayMs;
    
    public StockIndicesDataOperation(
            DataFetcher dataFetcher,
            DataValidator<NSEStockInsidicesData> stockIndicesDataValidator,
            DataProcessor<NSEStockInsidicesData, List<StockInsidicesData>> stockIndicesDataProcessor,
            MeterRegistry meterRegistry,
            Executor executor,
            NSEApi nseApi,
            KafkaProducerService kafkaProducer) {
        super(dataFetcher, stockIndicesDataValidator, stockIndicesDataProcessor, meterRegistry, executor);
        this.nseApi = nseApi;
        this.kafkaProducer = kafkaProducer;
        this.fetchTimer = Timer.builder("stock-indices.fetch.time")
            .tag("data.type", getDataTypeName())
            .description("Time taken to fetch stock indices data")
            .register(meterRegistry);
    }

    public StockIndicesDataOperation withIndexSymbol(String indexSymbol) {
        return (StockIndicesDataOperation) super.withIndexSymbol(indexSymbol);
    }
    
    @Override
    protected String getDataTypeName() {
        return "stock-indices";
    }
    
    @Override
    protected Timer getFetchTimer() {
        return fetchTimer;
    }
    
    @Override
    protected void handleSuccess(List<StockInsidicesData> result) {
        log.info("Successfully processed {} stock indices", result.size());
    }
    
    @Override
    protected NSEStockInsidicesData fetchData() {
        try {
            lastFetchedData = nseApi.getStockbyInsidices(getIndexSymbol());
            return lastFetchedData;
        } catch (Exception e) {
            throw new DataFetchException(getDataTypeName(), maxRetries, "Failed to fetch stock indices data", e);
        }
    }
    
    /**
     * Get the current index symbol
     * 
     * @return The index symbol
     */
    public String getIndexSymbol() {
        return super.getIndexSymbol();
    }

    /**
     * Get the last fetched data from the API
     * 
     * @return The last fetched data, or null if no data has been fetched yet
     */
    public NSEStockInsidicesData getLastFetchedData() {
        return lastFetchedData;
    }
}
