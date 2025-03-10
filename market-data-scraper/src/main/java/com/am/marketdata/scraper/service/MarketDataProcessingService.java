package com.am.marketdata.scraper.service;

import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.service.MarketIndexIndicesService;
import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.common.model.NseETFResponse;
import com.am.marketdata.common.model.NseETF;
import com.am.marketdata.kafka.producer.MarketIndiciesPriceKafkaProducer;
import com.am.marketdata.scraper.client.NSEApiClient;
import com.am.marketdata.scraper.mapper.NSEMarketIndexIndicesMapper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataProcessingService {
    private static final String CONFIG_THREAD_POOL_SIZE = "${market.data.thread.pool.size:5}";
    private static final String CONFIG_THREAD_QUEUE_CAPACITY = "${market.data.thread.queue.capacity:10}";
    private static final String CONFIG_MAX_RETRIES = "${market.data.max.retries:3}";
    private static final String CONFIG_RETRY_DELAY_MS = "${market.data.retry.delay.ms:1000}";
    private static final String CONFIG_MAX_DATA_AGE_MINUTES = "${market.data.max.age.minutes:15}";
    private static final String THREAD_PREFIX = "market-data-";
    private static final DateTimeFormatter MARKET_STATUS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    // Metric names
    private static final String METRIC_PREFIX = "market.data.";
    private static final String METRIC_FETCH_TIME = METRIC_PREFIX + "fetch.time";
    private static final String METRIC_PROCESS_TIME = METRIC_PREFIX + "process.time";
    private static final String METRIC_RETRY_COUNT = METRIC_PREFIX + "retry.count";
    private static final String METRIC_SUCCESS_COUNT = METRIC_PREFIX + "success.count";
    private static final String METRIC_FAILURE_COUNT = METRIC_PREFIX + "failure.count";
    private static final String TAG_DATA_TYPE = "data.type";
    private static final String TAG_OPERATION = "operation";

    private final NSEApiClient nseApiClient;
    private final MarketIndiciesPriceKafkaProducer kafkaProducer;
    private final MarketIndexIndicesService indexIndicesService;
    private final MeterRegistry meterRegistry;

    @Value(CONFIG_THREAD_POOL_SIZE)
    private int threadPoolSize;

    @Value(CONFIG_THREAD_QUEUE_CAPACITY)
    private int queueCapacity;

    @Value(CONFIG_MAX_RETRIES)
    private int maxRetries;

    @Value(CONFIG_RETRY_DELAY_MS)
    private long retryDelayMs;

    @Value(CONFIG_MAX_DATA_AGE_MINUTES)
    private long maxDataAgeMinutes;

    private ThreadPoolTaskExecutor executor;
    private Timer indicesFetchTimer;
    private Timer etfFetchTimer;
    private Timer indicesProcessTimer;
    private Timer etfProcessTimer;

    @PostConstruct
    public void initialize() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(THREAD_PREFIX);
        executor.initialize();
        log.info("Initialized market data thread pool with size: {}, queue capacity: {}, max retries: {}, retry delay: {}ms",
            threadPoolSize, queueCapacity, maxRetries, retryDelayMs);

        // Initialize metrics
        indicesFetchTimer = Timer.builder(METRIC_FETCH_TIME)
            .tag(TAG_DATA_TYPE, "indices")
            .description("Time taken to fetch indices data")
            .register(meterRegistry);

        etfFetchTimer = Timer.builder(METRIC_FETCH_TIME)
            .tag(TAG_DATA_TYPE, "etf")
            .description("Time taken to fetch ETF data")
            .register(meterRegistry);

        indicesProcessTimer = Timer.builder(METRIC_PROCESS_TIME)
            .tag(TAG_DATA_TYPE, "indices")
            .description("Time taken to process indices data")
            .register(meterRegistry);

        etfProcessTimer = Timer.builder(METRIC_PROCESS_TIME)
            .tag(TAG_DATA_TYPE, "etf")
            .description("Time taken to process ETF data")
            .register(meterRegistry);
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
            log.info("Shutting down market data thread pool");
        }
    }

    public void fetchAndProcessMarketData() {
        CompletableFuture<Boolean> indicesFuture = fetchAndProcessIndices();
        CompletableFuture<Boolean> etfFuture = fetchAndProcessETFs();

        try {
            CompletableFuture.allOf(indicesFuture, etfFuture).get();
            boolean indicesProcessed = indicesFuture.get();
            boolean etfProcessed = etfFuture.get();

            logProcessingStatus(indicesProcessed, etfProcessed);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while fetching market data", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error fetching market data", e.getCause());
        }
    }

    private CompletableFuture<Boolean> fetchAndProcessIndices() {
        return CompletableFuture.supplyAsync(() -> {
            Timer.Sample fetchSample = Timer.start();
            NSEIndicesResponse response = fetchIndicesWithRetry();
            fetchSample.stop(indicesFetchTimer);

            if (response != null) {
                try {
                    if (validateIndicesData(response)) {
                        Timer.Sample processSample = Timer.start();
                        log.info("Successfully fetched NSE indices data");
                        var indices = saveIndicesAndGetData(response);
                        kafkaProducer.sendIndicesUpdate(indices);
                        processSample.stop(indicesProcessTimer);

                        log.info("Successfully processed and sent indices data to Kafka");
                        meterRegistry.counter(METRIC_SUCCESS_COUNT, TAG_DATA_TYPE, "indices").increment();
                        return true;
                    } else {
                        log.warn("Skipping invalid or stale indices data");
                    }
                } catch (Exception e) {
                    log.error("Failed to process indices data", e);
                    meterRegistry.counter(METRIC_FAILURE_COUNT, TAG_DATA_TYPE, "indices").increment();
                }
            }
            return false;
        }, executor);
    }

    private CompletableFuture<Boolean> fetchAndProcessETFs() {
        return CompletableFuture.supplyAsync(() -> {
            Timer.Sample fetchSample = Timer.start();
            NseETFResponse response = fetchETFsWithRetry();
            fetchSample.stop(etfFetchTimer);

            if (response != null) {
                try {
                    if (validateETFData(response)) {
                        Timer.Sample processSample = Timer.start();
                        processAndSendETFData(response);
                        processSample.stop(etfProcessTimer);

                        meterRegistry.counter(METRIC_SUCCESS_COUNT, TAG_DATA_TYPE, "etf").increment();
                        return true;
                    } else {
                        log.warn("Skipping invalid or stale ETF data");
                    }
                } catch (Exception e) {
                    log.error("Failed to process ETF data", e);
                    meterRegistry.counter(METRIC_FAILURE_COUNT, TAG_DATA_TYPE, "etf").increment();
                }
            }
            return false;
        }, executor);
    }

    private NSEIndicesResponse fetchIndicesWithRetry() {
        return retryOnFailure(() -> {
            try {
                log.info("Fetching NSE indices data...");
                return nseApiClient.getAllIndices();
            } catch (Exception e) {
                log.error("Failed to fetch indices data", e);
                return null;
            }
        }, "indices", maxRetries);
    }

    private NseETFResponse fetchETFsWithRetry() {
        return retryOnFailure(() -> {
            try {
                log.info("Fetching NSE ETF data...");
                return nseApiClient.getETFs();
            } catch (Exception e) {
                log.error("Failed to fetch ETF data", e);
                return null;
            }
        }, "etf", maxRetries);
    }

    private void logProcessingStatus(boolean indicesProcessed, boolean etfProcessed) {
        if (!indicesProcessed && !etfProcessed) {
            throw new RuntimeException("Failed to process both indices and ETF data");
        } else if (!indicesProcessed) {
            log.warn("Indices data processing failed but ETF data was processed successfully");
        } else if (!etfProcessed) {
            log.warn("ETF data processing failed but indices data was processed successfully");
        } else {
            log.info("Successfully processed both indices and ETF data");
        }
    }

    private <T> T retryOnFailure(Supplier<T> operation, String operationType, int maxRetries) {
        int retryCount = 0;
        T result = null;
        Exception lastException = null;

        while (retryCount < maxRetries && result == null) {
            if (retryCount > 0) {
                try {
                    log.info("Retrying {} data fetch, attempt {} of {}", operationType, retryCount + 1, maxRetries);
                    TimeUnit.MILLISECONDS.sleep(retryDelayMs * retryCount); // Exponential backoff
                    meterRegistry.counter(METRIC_RETRY_COUNT, TAG_DATA_TYPE, operationType).increment();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry delay", e);
                }
            }

            try {
                result = operation.get();
                if (result != null && retryCount > 0) {
                    log.info("Successfully fetched {} data after {} retries", operationType, retryCount);
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed for {} data: {}", retryCount + 1, operationType, e.getMessage());
            }

            retryCount++;
        }

        if (result == null && lastException != null) {
            log.error("All {} retries failed for {} data", maxRetries, operationType, lastException);
        }

        return result;
    }

    private boolean validateIndicesData(NSEIndicesResponse response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            log.warn("Received empty indices response");
            return false;
        }
        return true;
    }

    private boolean validateETFData(NseETFResponse response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            log.warn("Received empty ETF response");
            return false;
        }

        if (response.getMarketStatus() == null) {
            log.warn("ETF response missing market status");
            return false;
        }

        // Parse and validate trade date
        try {
            String tradeDate = response.getMarketStatus().getTradeDate();
            if (tradeDate == null) {
                log.warn("ETF response missing trade date");
                return false;
            }

            LocalDateTime marketTime = LocalDateTime.parse(tradeDate, MARKET_STATUS_DATE_FORMAT);
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            long minutesOld = java.time.Duration.between(marketTime, now).toMinutes();

            if (minutesOld > maxDataAgeMinutes) {
                log.warn("ETF data is too old: {} minutes", minutesOld);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to parse ETF trade date", e);
            return false;
        }

        return true;
    }

    private void processAndSendETFData(NseETFResponse etfResponse) {
        if (etfResponse == null || etfResponse.getData() == null) {
            log.warn("Received null or empty ETF response");
            return;
        }

        List<NseETF> etfs = etfResponse.getData();
        log.info("Processing {} ETFs", etfs.size());

        try {
            // TODO: Add ETF processing logic here
            // For example:
            // 1. Save ETFs to database
            // 2. Send ETF updates to Kafka
            // 3. Update market status if needed

            log.info("Successfully processed ETF data. Market Status: {}, Advances: {}, Declines: {}", 
                etfResponse.getMarketStatus() != null ? etfResponse.getMarketStatus().getMarketStatus() : "N/A",
                etfResponse.getAdvances(),
                etfResponse.getDeclines()
            );

        } catch (Exception e) {
            log.error("Failed to process ETF data", e);
            throw new RuntimeException("Error processing ETF data", e);
        }
    }

    private List<MarketIndexIndices> saveIndicesAndGetData(NSEIndicesResponse indicesResponse) {
        log.info("Saving indices data to database...");
        try {
            List<MarketIndexIndices> indices = NSEMarketIndexIndicesMapper.convertToMarketIndexIndices(indicesResponse.getData());
            indices.forEach(indexIndicesService::save);
            log.info("Successfully saved indices data to database");
            return indices;
        } catch (Exception e) {
            log.error("Error saving indices data to database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save indices data", e);
        }
    }
}
