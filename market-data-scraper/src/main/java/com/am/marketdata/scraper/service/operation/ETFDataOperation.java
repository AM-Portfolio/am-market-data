package com.am.marketdata.scraper.service.operation;

import com.am.common.investment.model.equity.ETFIndies;
import com.am.marketdata.common.model.NseETFResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Operation for fetching and processing NSE ETF data
 */
@Slf4j
@Component
public class ETFDataOperation extends AbstractMarketDataOperation<NseETFResponse, List<ETFIndies>> {
    
    private final NSEApi nseApi;
    private final KafkaProducerService kafkaProducer;
    private final Timer fetchTimer;
    
    @Value("${market.data.max.retries:3}")
    private int maxRetries;
    
    @Value("${market.data.retry.delay.ms:1000}")
    private long retryDelayMs;
    
    public ETFDataOperation(
            DataFetcher dataFetcher,
            DataValidator<NseETFResponse> validator,
            DataProcessor<NseETFResponse, List<ETFIndies>> processor,
            MeterRegistry meterRegistry,
            Executor executor,
            NSEApi nseApi,
            KafkaProducerService kafkaProducer) {
        super(dataFetcher, validator, processor, meterRegistry, executor);
        this.nseApi = nseApi;
        this.kafkaProducer = kafkaProducer;
        this.fetchTimer = Timer.builder("market.data.fetch.time")
            .tag("data.type", getDataTypeName())
            .description("Time taken to fetch ETF data")
            .register(meterRegistry);
    }
    
    @Override
    protected String getDataTypeName() {
        return "etf";
    }
    
    @Override
    protected Timer getFetchTimer() {
        return fetchTimer;
    }
    
    @Override
    protected NseETFResponse fetchData() {
        return dataFetcher.executeWithRetry(() -> {
            try {
                log.info("Fetching NSE ETF data...");
                return nseApi.getETFs();
            } catch (Exception e) {
                throw new DataFetchException(getDataTypeName(), maxRetries, "Failed to fetch ETF data", e);
            }
        }, getDataTypeName(), maxRetries, retryDelayMs);
    }
    
    @Override
    protected void handleSuccess(List<ETFIndies> result) {
        // Send the processed data to Kafka
        kafkaProducer.sendETFUpdate(result);
        log.info("Successfully sent ETF data to Kafka");
    }
}
