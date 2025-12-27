package com.am.marketdata.provider.zerodha.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class ZerodhaApiConfig {

    @Value("${market-data.zerodha.api.thread.pool.size:5}")
    private int threadPoolSize;

    @Value("${market-data.zerodha.api.thread.queue.capacity:10}")
    private int queueCapacity;

    @Value("${market-data.zerodha.api.max.retries:3}")
    private int maxRetries;

    @Value("${market-data.zerodha.api.retry.delay.ms:1000}")
    private int retryDelayMs;

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

        return RetryRegistry.of(config);
    }
}
