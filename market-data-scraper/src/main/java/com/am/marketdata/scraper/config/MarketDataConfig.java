package com.am.marketdata.scraper.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.NseETFResponse;
import com.am.marketdata.common.model.equity.StockInsidicesData;
import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.kafka.producer.KafkaProducerService;
import com.am.marketdata.scraper.service.processor.StockIndicesProcessor;
import com.am.marketdata.scraper.service.validator.ETFDataValidator;
import com.am.marketdata.scraper.service.validator.IndicesDataValidator;
import com.am.marketdata.scraper.service.validator.StockIndicesValidator;
import com.am.marketdata.scraper.service.processor.ETFDataProcessor;
import com.am.marketdata.scraper.service.processor.IndicesDataProcessor;
import com.am.marketdata.scraper.service.common.DataProcessor;
import com.am.marketdata.scraper.service.common.DataValidator;
import com.am.common.investment.model.equity.ETFIndies;
import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.service.MarketIndexIndicesService;

@Configuration
@EnableScheduling
public class MarketDataConfig {
    
    // Configuration will be added here as needed
    
    @Bean("nseApiProcessingTimer")
    public Timer nseApiProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("nse.api.processing.time")
            .description("Time taken to process NSE API responses")
            .register(meterRegistry);
    }
    
    @Bean("indicesProcessingTimer")
    public Timer indicesProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("indices.processing.time")
            .description("Time taken to process indices data")
            .register(meterRegistry);
    }
    
    @Bean("etfProcessingTimer")
    public Timer etfProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("etf.processing.time")
            .description("Time taken to process ETF data")
            .register(meterRegistry);
    }
    
    @Bean("stockIndicesProcessingTimer")
    public Timer stockIndicesProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("stock_indices.processing.time")
            .description("Time taken to process stock indices data")
            .register(meterRegistry);
    }
    
    @Bean
    @Primary
    public DataProcessor<NSEStockInsidicesData, List<StockInsidicesData>> stockIndicesDataProcessor(
            KafkaProducerService kafkaProducer,
            MeterRegistry meterRegistry,
            @Qualifier("stockIndicesProcessingTimer") Timer processTimer) {
        return new StockIndicesProcessor(kafkaProducer, meterRegistry, processTimer);
    }
    
    @Bean
    public DataProcessor<NseETFResponse, List<ETFIndies>> etfDataProcessor(
            KafkaProducerService kafkaProducer,
            MeterRegistry meterRegistry,
            @Qualifier("etfProcessingTimer") Timer processTimer) {
        return new ETFDataProcessor(kafkaProducer, meterRegistry, processTimer);
    }

    @Bean
    public DataProcessor<NSEIndicesResponse, List<MarketIndexIndices>> indicesDataProcessor(
            MarketIndexIndicesService indexIndicesService,
            MeterRegistry meterRegistry,
            @Qualifier("indicesProcessingTimer") Timer processTimer) {
        return new IndicesDataProcessor(indexIndicesService, meterRegistry, processTimer);
    }

    @Bean
    public DataValidator<NseETFResponse> etfDataValidator() {
        return new ETFDataValidator();
    }

    @Bean
    public DataValidator<NSEIndicesResponse> indicesDataValidator() {
        return new IndicesDataValidator();
    }

    @Bean
    @Primary
    public DataValidator<NSEStockInsidicesData> stockIndicesDataValidator() {
        return new StockIndicesValidator();
    }
    
}
