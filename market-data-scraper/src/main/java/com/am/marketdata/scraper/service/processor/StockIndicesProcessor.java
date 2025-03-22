package com.am.marketdata.scraper.service.processor;

import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.equity.StockInsidicesData;
import com.am.marketdata.kafka.producer.KafkaProducerService;
import com.am.marketdata.scraper.exception.MarketDataException;
import com.am.marketdata.scraper.mapper.StockIndicesMapper;
import com.am.marketdata.scraper.service.common.DataProcessor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Processor for NSE Stock Indices data
 * Transforms raw NSE stock indices data into domain model and publishes to Kafka
 */
@Slf4j
@RequiredArgsConstructor
public class StockIndicesProcessor implements DataProcessor<NSEStockInsidicesData, List<StockInsidicesData>> {
    
    private final KafkaProducerService kafkaProducer;
    private final MeterRegistry meterRegistry;
    @Qualifier("stockIndicesProcessingTimer")
    private final Timer processTimer;
    
    @Override
    public List<StockInsidicesData> process(NSEStockInsidicesData data) throws MarketDataException {
        if (data == null || data.getData() == null) {
            log.warn("Received null or empty stock indices response");
            throw new MarketDataException("Null or empty stock indices data");
        }

        log.info("Processing {} stock indices", data.getData().size());

        try {
            // Convert NSE stock indices data to domain model
            StockInsidicesData stockIndices = StockIndicesMapper.convertToStockIndices(data);
            
            log.info("Successfully processed stock indices data. Index: {}, Count: {}, Advances: {}, Declines: {}", 
                data.getName(),
                stockIndices.getData().size(),
                data.getAdvance() != null ? data.getAdvance().getAdvances() : "N/A",
                data.getAdvance() != null ? data.getAdvance().getDeclines() : "N/A");
            
            // Publish to Kafka
            try {
                kafkaProducer.sendStockIndicesUpdate(stockIndices);
                log.debug("Published stock indices data for index: {}", stockIndices.getName());
            } catch (Exception e) {
                log.error("Failed to publish stock indices data for index: {}", stockIndices.getName(), e);
                // Continue processing other items
            }
            
            log.info("Successfully published stock indices data to Kafka");
            return Collections.singletonList(stockIndices);
        } catch (Exception e) {
            log.error("Error processing stock indices data for index: {}", data.getName(), e);
            throw new MarketDataException("Failed to process stock indices data", e);
        }
    }
    
    @Override
    public String getDataTypeName() {
        return "stock-indices";
    }
    
    @Override
    public Timer getProcessingTimer() {
        return processTimer;
    }
}
