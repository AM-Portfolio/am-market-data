package com.am.marketdata.scraper.service.processor;

import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.service.MarketIndexIndicesService;
import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.scraper.exception.MarketDataException;
import com.am.marketdata.scraper.mapper.NSEMarketIndexIndicesMapper;
import com.am.marketdata.scraper.service.common.DataProcessor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Processor for NSE indices data
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndicesDataProcessor implements DataProcessor<NSEIndicesResponse, List<MarketIndexIndices>> {
    
    private final MarketIndexIndicesService indexIndicesService;
    private final MeterRegistry meterRegistry;
    private final Timer processTimer;
    
    public IndicesDataProcessor(MarketIndexIndicesService indexIndicesService, MeterRegistry meterRegistry) {
        this.indexIndicesService = indexIndicesService;
        this.meterRegistry = meterRegistry;
        this.processTimer = Timer.builder("market.data.process.time")
            .tag("data.type", getDataTypeName())
            .description("Time taken to process indices data")
            .register(meterRegistry);
    }
    
    @Override
    public List<MarketIndexIndices> process(NSEIndicesResponse data) throws MarketDataException {
        log.info("Processing indices data...");
        try {
            List<MarketIndexIndices> indices = NSEMarketIndexIndicesMapper.convertToMarketIndexIndices(data.getData());
            
            // Save each index to the database
            for (MarketIndexIndices index : indices) {
                indexIndicesService.save(index);
            }
            
            log.info("Successfully processed {} indices", indices.size());
            return indices;
        } catch (Exception e) {
            log.error("Error processing indices data: {}", e.getMessage(), e);
            throw new MarketDataException("Failed to process indices data", e);
        }
    }
    
    @Override
    public String getDataTypeName() {
        return "indices";
    }
    
    @Override
    public Timer getProcessingTimer() {
        return processTimer;
    }
}
