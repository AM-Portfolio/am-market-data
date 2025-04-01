package com.am.marketdata.service;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.service.EquityService;
import com.am.marketdata.kafka.oldProducer.KafkaProducerService;
import com.am.marketdata.upstock.adapter.UpStockAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquityPriceProcessingService {
    private final UpStockAdapter upStockAdapter;
    private final EquityService equityService;
    private final KafkaProducerService kafkaProducerService;
    private final MeterRegistry meterRegistry;

    private static final int BATCH_SIZE = 50;
    private static final String NSE_PREFIX = "NSE_EQ|";

    @Transactional
    public boolean processEquityPrices(List<String> isins) {
        if (isins.isEmpty()) {
            log.warn("No stocks found to process");
            return false;
        }

        Timer.Sample processingTimer = Timer.start(meterRegistry);
        
        try {
            // Format ISINs with NSE prefix
            Set<String> formattedIsins = formatIsins(isins);
            
            // Process in batches
            List<List<String>> batches = partition(formattedIsins.stream().toList(), BATCH_SIZE);
            log.info("Processing {} stocks in {} batches", isins.size(), batches.size());

            List<EquityPrice> allUpdatedStocks = new ArrayList<>();
            boolean hasErrors = false;

            // Track metrics for batch processing
            meterRegistry.counter("equity.price.batch.total").increment(batches.size());

            for (List<String> batch : batches) {
                try {
                    Timer.Sample batchTimer = Timer.start(meterRegistry);
                    var equityPrices = upStockAdapter.getStocksOHLC(batch);
                    
                    if (!equityPrices.isEmpty()) {
                        equityService.saveAllPrices(equityPrices);
                        allUpdatedStocks.addAll(equityPrices);
                        meterRegistry.counter("equity.price.batch.success").increment();
                    } else {
                        log.warn("Received empty response for batch. Skipping Kafka event.");
                        meterRegistry.counter("equity.price.batch.empty").increment();
                        hasErrors = true;
                    }
                    
                    batchTimer.stop(meterRegistry.timer("equity.price.batch.processing.time"));
                } catch (Exception e) {
                    log.error("Error processing batch: {}", e.getMessage(), e);
                    meterRegistry.counter("equity.price.batch.error").increment();
                    hasErrors = true;
                }
            }

            // Send Kafka events if we have data and no errors occurred
            if (!allUpdatedStocks.isEmpty() && !hasErrors) {
                log.info("Sending Kafka events for {} updated stocks", allUpdatedStocks.size());
                kafkaProducerService.sendEquityPriceUpdates(allUpdatedStocks);
                meterRegistry.counter("equity.price.kafka.events.sent").increment();
                processingTimer.stop(meterRegistry.timer("equity.price.total.processing.time"));
                return true;
            } else {
                log.warn("Skipping Kafka events due to errors or no data");
                meterRegistry.counter("equity.price.kafka.events.skipped").increment();
            }

        } catch (Exception e) {
            log.error("Error in equity price processing: {}", e.getMessage(), e);
            meterRegistry.counter("equity.price.processing.error").increment();
        }
        
        processingTimer.stop(meterRegistry.timer("equity.price.total.processing.time"));
        return false;
    }

    private Set<String> formatIsins(List<String> isins) {
        return isins.stream()
            .map(isin -> NSE_PREFIX + isin)
            .collect(Collectors.toSet());
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        return list.stream()
            .collect(Collectors.groupingBy(item -> list.indexOf(item) / size))
            .values()
            .stream()
            .toList();
    }
}
