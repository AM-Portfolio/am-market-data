package com.am.marketdata.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.common.investment.model.equity.financial.resultstatement.QuaterlyResult;
import com.am.common.investment.service.StockFinancialPerformanceService;
import com.am.marketdata.kafka.producer.StockPortfolioProducerService;
import com.am.marketdata.processor.service.common.DataProcessor;
import com.am.marketdata.processor.service.common.DataValidator;
import com.am.marketdata.processor.service.mapper.StockBoardOfDirectorsMapper;
import com.am.marketdata.processor.service.mapper.StockQuaterlyResultFinanceMapper;
import com.am.marketdata.processor.service.processor.QuaterlyFinancialResultProcessor;
import com.am.marketdata.processor.service.processor.StockBoardOfDiretorsProcessor;
import com.am.marketdata.processor.service.validator.QuaterlyFinanceResultValidator;
import com.am.marketdata.processor.service.validator.StockBoardOfDirectorValidator;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {
    "com.am.marketdata.processor",
    "com.am.marketdata.processor.service",
    "com.am.common.investment.service.mapper",
    "com.am.common.investment.service",
    "com.am.marketdata.common",
    "com.am.marketdata.external" // Assuming external API package
})
@Import({
    // Import any required configurations from other modules
    // This will be populated as needed
})
@EnableJpaRepositories(basePackages = "com.am.marketdata.processor.repository")
@EntityScan(basePackages = "com.am.marketdata.processor.entity")
public class ProcessorModuleConfig {

    /**
     * Dedicated thread pool for data processing tasks
     * Based on memory #5ba7ad90-715c-4238-929a-4a58c2ace265
     */
    @Bean(name = "processorTaskExecutor")
    @Primary
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("market-processor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public DataValidator<BoardOfDirectors> stockBoardOfDirectoreValidator() {
        return new StockBoardOfDirectorValidator();
    }
    
    @Bean
    public StockBoardOfDirectorsMapper stockBoardOfDirectorsMapper() {
        return new StockBoardOfDirectorsMapper();
    }
    @Bean
    public DataProcessor<BoardOfDirectors, Void> stockBoardOfDirectoeProcessor(
            StockPortfolioProducerService stockPortfolioProducer,
            StockFinancialPerformanceService stockFinancialPerformanceService,
            @Qualifier("boardOfDirectoreProcessingTimer") Timer processTimer) {
        return new StockBoardOfDiretorsProcessor(stockPortfolioProducer, stockFinancialPerformanceService, processTimer);
    }

    @Bean("boardOfDirectoreProcessingTimer")
    @Primary
    public Timer boardOfDirectoreProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("board.of.directors.processing.time")
            .description("Time taken to process stock board of directors data")
            .register(meterRegistry);
    }

    @Bean("quaterlyFinancialsProcessingTimer")
    public Timer quaterlyFinancialsProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("quaterly.financials.processing.time")
            .description("Time taken to process stock quaterly financials data")
            .register(meterRegistry);
    }

    @Bean
    @Primary
    public DataValidator<QuaterlyResult> stockQuaterlyFinancialsValidator() {
        return new QuaterlyFinanceResultValidator();
    }
    
    @Bean
    public StockQuaterlyResultFinanceMapper stockQuaterlyResultFinanceMapper() {
        return new StockQuaterlyResultFinanceMapper();
    }
    
    @Bean
    @Primary
    public DataProcessor<QuaterlyResult, Void> stockQuaterlyFinancialsProcessor(
            StockPortfolioProducerService stockPortfolioProducer,
            StockFinancialPerformanceService stockFinancialPerformanceService,
            @Qualifier("quaterlyFinancialsProcessingTimer") Timer processTimer) {
        return new QuaterlyFinancialResultProcessor(stockPortfolioProducer, stockFinancialPerformanceService, processTimer);
    }

}
