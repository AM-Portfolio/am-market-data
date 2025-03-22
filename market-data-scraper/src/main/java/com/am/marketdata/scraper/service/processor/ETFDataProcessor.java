package com.am.marketdata.scraper.service.processor;

import com.am.common.investment.model.equity.ETFIndies;
import com.am.marketdata.common.model.NseETFResponse;
import com.am.marketdata.kafka.producer.KafkaProducerService;
import com.am.marketdata.scraper.exception.MarketDataException;
import com.am.marketdata.scraper.mapper.ETFIndicesMapper;
import com.am.marketdata.scraper.service.common.DataProcessor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Processor for NSE ETF data
 */
@Slf4j
@RequiredArgsConstructor
public class ETFDataProcessor implements DataProcessor<NseETFResponse, List<ETFIndies>> {
    
    private final KafkaProducerService kafkaProducer;
    private final MeterRegistry meterRegistry;
    @Qualifier("etfProcessingTimer")
    private final Timer processTimer;
    
    @Override
    public List<ETFIndies> process(NseETFResponse data) throws MarketDataException {
        if (data == null || data.getData() == null) {
            log.warn("Received null or empty ETF response");
            throw new MarketDataException("Null or empty ETF data");
        }

        log.info("Processing {} ETFs", data.getData().size());

        try {
            List<ETFIndies> etfIndies = ETFIndicesMapper.convertToETFIndices(data.getData());
            
            log.info("Successfully processed ETF data. Market Status: {}, Advances: {}, Declines: {}", 
                data.getMarketStatus() != null ? data.getMarketStatus().getMarketStatus() : "N/A",
                data.getAdvances(),
                data.getDeclines()
            );
            
            return etfIndies;
        } catch (Exception e) {
            log.error("Failed to process ETF data", e);
            throw new MarketDataException("Failed to process ETF data", e);
        }
    }
    
    @Override
    public String getDataTypeName() {
        return "etf";
    }
    
    @Override
    public Timer getProcessingTimer() {
        return processTimer;
    }
}
