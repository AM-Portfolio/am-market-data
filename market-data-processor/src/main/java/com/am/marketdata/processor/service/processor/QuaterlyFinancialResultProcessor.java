package com.am.marketdata.processor.service.processor;

import com.am.common.investment.model.equity.financial.resultstatement.QuaterlyResult;
import com.am.common.investment.service.StockFinancialPerformanceService;
import com.am.marketdata.kafka.oldProducer.StockPortfolioProducerService;
import com.am.marketdata.kafka.producer.MarketDataPublisherFacade;
import com.am.marketdata.processor.exception.ProcessorException;
import com.am.marketdata.processor.service.common.DataProcessor;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Processor for NSE Stock Indices data
 * Transforms raw NSE stock indices data into domain model and publishes to Kafka
 */
@Slf4j
@RequiredArgsConstructor
public class QuaterlyFinancialResultProcessor implements DataProcessor<QuaterlyResult, Void> {
    
    //private final StockPortfolioProducerService stockPortfolioProducer;
    private final MarketDataPublisherFacade marketDataPublisherFacade;
    private final StockFinancialPerformanceService stockFinancialPerformanceService;

    @Qualifier("quaterlyFinancialProcessingTimer")
    private final Timer processTimer;
    

    @Override
    @SneakyThrows
    public Void process(QuaterlyResult data) {
        if (data == null || data.getFinancialResults() == null || data.getFinancialResults().isEmpty()) {
            log.warn("Received null or empty stock quaterly financials response");
            throw new ProcessorException(getDataTypeName(),ProcessorException.ProcessorErrorType.INVALID_DATA,"Null or empty stock quaterly financials data");
        }

        log.info("Processing {} stock quaterly financials", data.getFinancialResults().size());

        try {
            // Save to the database
            stockFinancialPerformanceService.saveQuaterlyResult(data);

            log.info("Processing stock quaterly financials data. Symbol: {}, Count: {}", 
                data.getSymbol(),
                data.getFinancialResults().size());
            // Publish to Kafka
            try {
                marketDataPublisherFacade.publishQuaterlyFinancialsUpdate(data.getSymbol(), data);
                log.debug("Published stock quaterly financials data for symbol: {}", data.getSymbol());
            } catch (Exception e) {
                log.error("Failed to publish stock quaterly financials data for symbol: {}", data.getSymbol(), e);
                // Continue processing other items
            }
            
            log.info("Successfully published stock quaterly financials data to Kafka");
            return null;
        } catch (Exception e) {
            log.error("Error processing stock quaterly financials data for symbol: {}", data.getSymbol(), e);
            throw new ProcessorException(getDataTypeName(),ProcessorException.ProcessorErrorType.PERSISTENCE_ERROR,"Failed to process stock quaterly financials data", e);
        }
    }
    
    @Override
    public String getDataTypeName() {
        return "stock-quaterly-financials";
    }
    
    @Override
    public Timer getProcessingTimer() {
        return processTimer;
    }
}