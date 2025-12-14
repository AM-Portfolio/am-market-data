package com.marketdata.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.marketdata.service.upstox.UpstoxApiService;
import com.marketdata.service.upstox.UpstoxMarketDataProvider;

import java.time.Duration;

/**
 * Configuration for Upstox API integration
 */
@Slf4j
@Configuration
@EnableScheduling
public class UpstoxApiConfig {

    @Value("${market-data.upstox.api.max.retries:3}")
    private int maxRetries;

    @Value("${market-data.upstox.api.retry.delay.ms:1000}")
    private int retryDelayMs;

    @Bean(name = "upstoxApiService")
    public UpstoxApiService upstoxApiService(com.am.marketdata.upstock.client.UpStockClient upStockClient) {
        log.info("Creating Upstox API service");
        return new UpstoxApiService(upStockClient);
    }

    @Bean(name = "upstoxMarketDataProvider")
    public UpstoxMarketDataProvider upstoxMarketDataProvider(UpstoxApiService upstoxApiService) {
        log.info("Creating Upstox market data provider");
        return new UpstoxMarketDataProvider(upstoxApiService);
    }

    @Bean(name = "marketDataUpstoxRetryRegistry")
    public RetryRegistry retryRegistry(MeterRegistry meterRegistry) {
        log.info("Creating Upstox retry registry with max retries: {}, base delay: {}ms",
                maxRetries, retryDelayMs);

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxRetries)
                .waitDuration(Duration.ofMillis(retryDelayMs))
                .retryExceptions(Exception.class)
                .ignoreExceptions(InterruptedException.class)
                .build();

        return RetryRegistry.of(config);
    }
}
