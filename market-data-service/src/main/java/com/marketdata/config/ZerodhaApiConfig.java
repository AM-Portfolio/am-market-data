package com.marketdata.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.marketdata.common.MarketDataProviderFactory;
import com.marketdata.service.zerodha.ZerodhaApiService;
import com.marketdata.service.zerodha.ZerodhaMarketDataProvider;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration for Zerodha API integration
 * Sets up necessary beans and configuration for Zerodha API
 */
@Slf4j
@Configuration
@EnableScheduling
public class ZerodhaApiConfig {

    @Value("${market-data.zerodha.api.thread.pool.size:5}")
    private int threadPoolSize;

    @Value("${market-data.zerodha.api.thread.queue.capacity:10}")
    private int queueCapacity;

    @Value("${market-data.zerodha.api.max.retries:3}")
    private int maxRetries;

    @Value("${market-data.zerodha.api.retry.delay.ms:1000}")
    private int retryDelayMs;

    @Value("${market-data.zerodha.api.retry.max.duration.ms:10000}")
    private int retryMaxDurationMs;

    /**
     * Creates a thread pool executor for Zerodha API operations
     * @return Configured ThreadPoolExecutor
     */
    @Bean(name = "zerodhaThreadPoolExecutor")
    public ThreadPoolExecutor threadPoolExecutor() {
        log.info("Creating Zerodha thread pool executor with size: {}, queue capacity: {}", 
                threadPoolSize, queueCapacity);
        
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("zerodha-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };
        
        return new ThreadPoolExecutor(
                threadPoolSize,
                threadPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Creates a retry registry for Zerodha API operations
     * @param meterRegistry Micrometer registry for metrics
     * @return Configured RetryRegistry
     */
    @Bean(name = "marketDataZerodhaRetryRegistry")
    public RetryRegistry retryRegistry(MeterRegistry meterRegistry) {
        log.info("Creating Zerodha retry registry with max retries: {}, base delay: {}ms", 
                maxRetries, retryDelayMs);
        
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxRetries)
                .waitDuration(Duration.ofMillis(retryDelayMs))
                .retryExceptions(Exception.class)
                .ignoreExceptions(InterruptedException.class)
                .build();
        
        RetryRegistry retryRegistry = RetryRegistry.of(config);
        
        // // Register metrics
        // retryRegistry.getEventPublisher()
        //         .onRetry(event -> meterRegistry.counter("zerodha.api.retry.count").increment());
        
        return retryRegistry;
    }
    
    /**
     * Creates the Zerodha API service
     * @param meterRegistry Metrics registry
     * @param threadPoolExecutor Thread pool for async operations
     * @return ZerodhaApiService instance
     */
    @Bean
    public ZerodhaApiService zerodhaApiService(MeterRegistry meterRegistry, ThreadPoolExecutor threadPoolExecutor) {
        log.info("Creating Zerodha API service");
        return new ZerodhaApiService(meterRegistry, threadPoolExecutor);
    }
    
    /**
     * Creates the Zerodha market data provider
     * @param zerodhaApiService Zerodha API service
     * @return ZerodhaMarketDataProvider instance
     */
    @Bean(name = "zerodhaMarketDataProvider")
    public ZerodhaMarketDataProvider zerodhaMarketDataProvider(ZerodhaApiService zerodhaApiService) {
        log.info("Creating Zerodha market data provider");
        return new ZerodhaMarketDataProvider(zerodhaApiService);
    }
    
    /**
     * Creates the market data provider factory
     * @param applicationContext Spring application context
     * @return MarketDataProviderFactory instance
     */
    @Bean
    @Primary
    public MarketDataProviderFactory marketDataProviderFactory(ApplicationContext applicationContext) {
        log.info("Creating market data provider factory");
        return new MarketDataProviderFactory(applicationContext);
    }
}
