package com.am.marketdata.scraper.service;

import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.model.events.StockInsidicesEventData;
import com.am.common.investment.model.events.mapper.StockIndicesEventDataMapper;
import com.am.common.investment.service.MarketIndexIndicesService;
import com.am.common.investment.service.StockIndicesMarketDataService;
import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.kafka.producer.KafkaProducerService;
import com.am.marketdata.scraper.client.NSEApiClient;
import com.am.marketdata.scraper.cookie.CookieManager;
import com.am.marketdata.scraper.exception.CookieException;
import com.am.marketdata.scraper.exception.DataFetchException;
import com.am.marketdata.scraper.exception.DataValidationException;
import com.am.marketdata.scraper.exception.MarketDataException;
import com.am.marketdata.scraper.mapper.NSEMarketIndexIndicesMapper;
import com.am.marketdata.scraper.mapper.StockIndicesMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
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
    private static final String METRIC_SUCCESS_COUNT = METRIC_PREFIX + "success.count";
    private static final String METRIC_FAILURE_COUNT = METRIC_PREFIX + "failure.count";
    private static final String METRIC_RETRY_COUNT = METRIC_PREFIX + "retry.count";
    private static final String TAG_DATA_TYPE = "data.type";

    private final NSEApiClient nseApiClient;
    private final KafkaProducerService kafkaProducer;
    private final MarketIndexIndicesService indexIndicesService;
    private final StockIndicesMarketDataService stockIndicesMarketDataService;
    private final MeterRegistry meterRegistry;
    private final CookieManager cookieManager;

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
    private Timer stockIndicesFetchTimer;
    private Timer indicesProcessTimer;
    private Timer stockIndicesProcessTimer;

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

        stockIndicesFetchTimer = Timer.builder(METRIC_FETCH_TIME)
            .tag(TAG_DATA_TYPE, "stock_indices")
            .description("Time taken to fetch stock indices data")
            .register(meterRegistry);

        indicesProcessTimer = Timer.builder(METRIC_PROCESS_TIME)
            .tag(TAG_DATA_TYPE, "indices")
            .description("Time taken to process indices data")
            .register(meterRegistry);

        stockIndicesProcessTimer = Timer.builder(METRIC_PROCESS_TIME)
            .tag(TAG_DATA_TYPE, "stock_indices")
            .description("Time taken to process stock indices data")
            .register(meterRegistry);
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
            log.info("Shutting down market data thread pool");
        }
    }

    /**
     * Fetch and process both indices and stock indices data
     * This method is called by the regular scheduler (every 2 minutes)
     */
    public void fetchAndProcessMarketData() {
        try {
            // Refresh cookies if needed
            cookieManager.refreshIfNeeded();
            
            // For regular processing, we only fetch indices
            // Stock indices are handled by a separate scheduler
            CompletableFuture<Boolean> indicesFuture = fetchAndProcessIndices();
            
            try {
                boolean indicesProcessed = indicesFuture.get();
                log.info("Regular market data processing completed. Indices processed: {}", 
                        indicesProcessed ? "success" : "failed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MarketDataException("Interrupted while fetching market data", e);
            } catch (ExecutionException e) {
                throw new MarketDataException("Error fetching market data", e.getCause());
            }
        } catch (CookieException e) {
            log.error("Cookie refresh failed: {}", e.getMessage());
            throw new MarketDataException("Failed to fetch market data: Cookie refresh failed", e);
        }
    }
    
    /**
     * Fetch and process only stock indices data
     * This method is called by the stock indices scheduler at specific times
     * @return true if stock indices were successfully processed, false otherwise
     */
    public boolean fetchAndProcessStockIndicesOnly() {
        try {
            // Refresh cookies if needed
            cookieManager.refreshIfNeeded();
            
            log.info("Starting stock indices only processing");
            CompletableFuture<Boolean> stockIndicesFuture = fetchAndProcessStockIndices();
            
            try {
                boolean stockIndicesProcessed = stockIndicesFuture.get();
                log.info("Stock indices processing completed: {}", 
                        stockIndicesProcessed ? "success" : "failed");
                return stockIndicesProcessed;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while fetching stock indices data", e);
                return false;
            } catch (ExecutionException e) {
                log.error("Error fetching stock indices data: {}", e.getCause().getMessage());
                return false;
            }
        } catch (CookieException e) {
            log.error("Cookie refresh failed during stock indices fetch: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during stock indices fetch: {}", e.getMessage(), e);
            return false;
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
                        throw new DataValidationException("indices", "Invalid or stale data");
                    }
                } catch (DataValidationException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("Failed to process indices data", e);
                    meterRegistry.counter(METRIC_FAILURE_COUNT, TAG_DATA_TYPE, "indices").increment();
                    throw new MarketDataException("Failed to process indices data", e);
                }
            }
            return false;
        }, executor);
    }

    private CompletableFuture<Boolean> fetchAndProcessStockIndices() {
        return CompletableFuture.supplyAsync(() -> {
            Timer.Sample fetchSample = Timer.start();
            NSEStockInsidicesData response = fetchStockIndicesWithRetry();
            fetchSample.stop(stockIndicesFetchTimer);

            if (response != null) {
                try {
                    if (validateStockIndicesData(response)) {
                        Timer.Sample processSample = Timer.start();
                        processAndSendStockIndicesData(response);
                        processSample.stop(stockIndicesProcessTimer);

                        log.info("Successfully processed and sent stock indices data to Kafka");
                        meterRegistry.counter(METRIC_SUCCESS_COUNT, TAG_DATA_TYPE, "stock_indices").increment();
                        return true;
                    } else {
                        log.warn("Skipping invalid or stale stock indices data");
                        throw new DataValidationException("stock_indices", "Invalid or stale data");
                    }
                } catch (DataValidationException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("Failed to process stock indices data", e);
                    meterRegistry.counter(METRIC_FAILURE_COUNT, TAG_DATA_TYPE, "stock_indices").increment();
                    throw new MarketDataException("Failed to process stock indices data", e);
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
                throw new DataFetchException("indices", maxRetries, "Failed to fetch indices data", e);
            }
        }, maxRetries, retryDelayMs);
    }

    private NSEStockInsidicesData fetchStockIndicesWithRetry() {
        return retryOnFailure(() -> {
            try {
                log.info("Fetching NSE stock indices data...");
                return nseApiClient.getStockIndices();
            } catch (Exception e) {
                throw new DataFetchException("stock_indices", maxRetries, "Failed to fetch stock indices data", e);
            }
        }, maxRetries, retryDelayMs);
    }

    private <T> T retryOnFailure(Supplier<T> operation, int maxRetries, long retryDelayMs) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                meterRegistry.counter(METRIC_RETRY_COUNT, TAG_DATA_TYPE, 
                    e instanceof DataFetchException ? ((DataFetchException) e).getDataType() : "unknown").increment();
                
                if (attempt < maxRetries) {
                    long delay = retryDelayMs * (long) Math.pow(2, attempt - 1);
                    log.warn("Attempt {} failed, retrying in {} ms", attempt, delay, e);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new MarketDataException("Retry interrupted", ie);
                    }
                }
            }
        }
        throw new MarketDataException("Operation failed after " + maxRetries + " retries", lastException);
    }

    private boolean validateIndicesData(NSEIndicesResponse response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            log.warn("Received empty indices response");
            return false;
        }
        return true;
    }

    private boolean validateStockIndicesData(NSEStockInsidicesData response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            log.warn("Received empty stock indices response");
            return false;
        }

        if (response.getMarketStatus() == null) {
            log.warn("Stock indices response missing market status");
            return false;
        }

        // Parse and validate trade date
        try {
            String tradeDate = response.getMarketStatus().getTradeDate();
            if (tradeDate == null) {
                log.warn("Stock indices response missing trade date");
                return false;
            }

            LocalDateTime marketTime = LocalDateTime.parse(tradeDate, MARKET_STATUS_DATE_FORMAT);
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            long minutesOld = java.time.Duration.between(marketTime, now).toMinutes();

            if (minutesOld > maxDataAgeMinutes) {
                log.warn("Stock indices data is too old: {} minutes", minutesOld);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to parse stock indices trade date", e);
            return false;
        }

        return true;
    }

    private void processAndSendStockIndicesData(NSEStockInsidicesData stockIndicesResponse) {
        if (stockIndicesResponse == null || stockIndicesResponse.getData() == null) {
            log.warn("Received null or empty stock indices response");
            return;
        }

        List<NSEStockInsidicesData.StockData> stocks = stockIndicesResponse.getData();
        log.info("Processing {} stocks", stocks.size());

        try {
            StockInsidicesEventData stockIndice = StockIndicesMapper.convertToStockIndices(stockIndicesResponse);
            kafkaProducer.sendStockIndicesUpdate(stockIndice);
            log.info("Successfully processed stock indices data. Market Status: {}, Advances: {}, Declines: {}", 
                stockIndice.getMarketStatus() != null ? stockIndice.getMarketStatus().getMarketStatus() : "N/A",
                stockIndice.getAdvance().getAdvances(),
                stockIndice.getAdvance().getDeclines()
            );

            saveStockIndicesAndGetData(stockIndice);

        } catch (Exception e) {
            log.error("Failed to process ETF data", e);
            throw new RuntimeException("Error processing ETF data", e);
        }
    }

    private void saveStockIndicesAndGetData(StockInsidicesEventData stockIndicesResponse) {
        log.info("Saving stock indices data to database...");
        try {
            var stockIndicesMarketData = StockIndicesEventDataMapper.toMarketData(stockIndicesResponse);
            stockIndicesMarketDataService.save(stockIndicesMarketData);
            log.info("Successfully saved stock indices data to database");
        } catch (Exception e) {
            log.error("Error saving stock indices data to database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save stock indices data", e);
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
