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
import com.marketdata.service.upstox.UpstoxSdkService;

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
    public UpstoxApiService upstoxApiService(
            com.am.marketdata.upstock.client.UpStockClient upStockClient,
            org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            com.am.marketdata.upstock.config.UpstoxConfig upstoxConfig) {
        log.info("Creating Upstox API service");
        return new UpstoxApiService(upStockClient, redisTemplate, objectMapper, upstoxConfig);
    }

    @Bean(name = "upstoxSdkService")
    public UpstoxSdkService upstoxSdkService(
            org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
            com.am.marketdata.upstock.config.UpstoxConfig upstoxConfig) {
        log.info("Creating Upstox SDK service");
        return new UpstoxSdkService(redisTemplate, upstoxConfig);
    }

    @Bean(name = "upstoxMarketDataProvider")
    public UpstoxMarketDataProvider upstoxMarketDataProvider(UpstoxApiService upstoxApiService,
            com.am.marketdata.service.service.UpstoxInstrumentService upstoxInstrumentService,
            UpstoxSdkService upstoxSdkService) {
        log.info("Creating Upstox market data provider");
        return new UpstoxMarketDataProvider(upstoxApiService, upstoxInstrumentService, upstoxSdkService);
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
