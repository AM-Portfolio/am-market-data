package com.am.marketdata.service.impl;

import com.am.marketdata.service.MarketDataService;
import com.marketdata.common.MarketDataProvider;
import com.marketdata.common.MarketDataProviderFactory;
import com.zerodhatech.models.OHLCQuote;
import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.Instrument;
import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.service.EquityService;
import com.am.common.investment.service.historical.HistoricalDataService;
import com.am.common.investment.service.instrument.InstrumentService;
import com.am.marketdata.mapper.HistoryDataMapper;
import com.am.marketdata.mapper.InstrumentMapper;
import com.am.marketdata.mapper.OHLCMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Implementation of MarketDataService
 * Handles all market data processing logic including fetching, validation, and processing
 */
@Slf4j
@Service
public class MarketDataServiceImpl implements MarketDataService {

    private final MarketDataProviderFactory providerFactory;
    private final InstrumentService instrumentService;
    private final HistoricalDataService historicalDataService;
    private final MeterRegistry meterRegistry;
    private final InstrumentMapper instrumentMapper;
    private final OHLCMapper ohlcMapper;
    private final EquityService equityService;
    private ThreadPoolTaskExecutor marketDataExecutor;

    @Value("${market.data.thread.pool.size:5}")
    private int threadPoolSize;

    @Value("${market.data.thread.queue.capacity:10}")
    private int queueCapacity;

    @Value("${market.data.max.retries:3}")
    private int maxRetries;

    @Value("${market.data.retry.delay.ms:1000}")
    private int retryDelayMs;

    @Value("${market.data.max.age.minutes:15}")
    private int maxAgeMinutes;

    public MarketDataServiceImpl(MarketDataProviderFactory providerFactory, InstrumentService instrumentService, HistoricalDataService historicalDataService, MeterRegistry meterRegistry, InstrumentMapper instrumentMapper, OHLCMapper ohlcMapper, EquityService equityService) {
        this.providerFactory = providerFactory;
        this.instrumentService = instrumentService;
        this.historicalDataService = historicalDataService;
        this.meterRegistry = meterRegistry;
        this.instrumentMapper = instrumentMapper;
        this.ohlcMapper = ohlcMapper;
        this.equityService = equityService;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing MarketDataService with threadPoolSize={}, queueCapacity={}, maxRetries={}, retryDelayMs={}",
                threadPoolSize, queueCapacity, maxRetries, retryDelayMs);
        
        // Initialize thread pool for market data operations
        marketDataExecutor = new ThreadPoolTaskExecutor();
        marketDataExecutor.setCorePoolSize(threadPoolSize);
        marketDataExecutor.setMaxPoolSize(threadPoolSize);
        marketDataExecutor.setQueueCapacity(queueCapacity);
        marketDataExecutor.setThreadNamePrefix("market-data-");
        marketDataExecutor.initialize();
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down MarketDataService thread pool");
        if (marketDataExecutor != null) {
            marketDataExecutor.shutdown();
        }
    }

    @Override
    public Map<String, String> getLoginUrl() {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            MarketDataProvider provider = providerFactory.getProvider();
            String loginUrl = provider.getLoginUrl();
            
            Map<String, String> response = new HashMap<>();
            response.put("loginUrl", loginUrl);
            response.put("provider", provider.getProviderName());
            
            return response;
        } catch (Exception e) {
            log.error("Error getting login URL: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getLoginUrl").increment();
            throw new RuntimeException("Failed to get login URL", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getLoginUrl"));
        }
    }

    @Override
    public Object generateSession(String requestToken) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            if (requestToken == null || requestToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Request token cannot be null or empty");
            }
            
            MarketDataProvider provider = providerFactory.getProvider();
            return retryOnFailure(() -> provider.generateSession(requestToken), "generateSession");
        } catch (Exception e) {
            log.error("Error generating session: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "generateSession").increment();
            throw new RuntimeException("Failed to generate session", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "generateSession"));
        }
    }

    @Override
    public Map<String, Object> getQuotes(String[] symbols) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            validateInstruments(symbols);
            
            MarketDataProvider provider = providerFactory.getProvider();
            return retryOnFailure(() -> provider.getQuotes(symbols), "getQuotes");
        } catch (Exception e) {
            log.error("Error getting quotes: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getQuotes").increment();
            throw new RuntimeException("Failed to get quotes", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getQuotes"));
        }
    }

    @Override
    public Map<String, OHLCQuote> getOHLC(String[] symbols) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            validateInstruments(symbols);
            
            MarketDataProvider provider = providerFactory.getProvider();
            return retryOnFailure(() -> provider.getOHLC(symbols), "getOHLC");
        } catch (Exception e) {
            log.error("Error getting OHLC data: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getOHLC").increment();
            throw new RuntimeException("Failed to get OHLC data", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getOHLC"));
        }
    }

    @Override
    public Map<String, Object> getLTP(String[] instruments) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            validateInstruments(instruments);
            
            MarketDataProvider provider = providerFactory.getProvider();
            return retryOnFailure(() -> provider.getLTP(instruments), "getLTP");
        } catch (Exception e) {
            log.error("Error getting LTP data: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getLTP").increment();
            throw new RuntimeException("Failed to get LTP data", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getLTP"));
        }
    }

    @Override
    public HistoricalData getHistoricalData(String symbol, Date fromDate, Date toDate, String interval, boolean continuous, Map<String, Object> additionalParams) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            // Validate inputs
            if (symbol == null || symbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }
            if (fromDate == null || toDate == null) {
                throw new IllegalArgumentException("From date and to date cannot be null");
            }
            if (fromDate.after(toDate)) {
                throw new IllegalArgumentException("From date cannot be after to date");
            }
            if (interval == null || interval.trim().isEmpty()) {
                throw new IllegalArgumentException("Interval cannot be null or empty");
            }
            
            MarketDataProvider provider = providerFactory.getProvider();
            com.zerodhatech.models.HistoricalData zerodhaHistoricalData = retryOnFailure(() -> provider.getHistoricalData(
                    symbol, fromDate, toDate, interval, continuous, additionalParams), "getHistoricalData");

            HistoryDataMapper historicalDataMapper = new HistoryDataMapper();
            HistoricalData historicalData = historicalDataMapper.toCommonHistoricalData(zerodhaHistoricalData);

            historicalData.setTradingSymbol(symbol);
            
            // Make saveHistoricalData asynchronous but wait for the result
            CompletableFuture<Void> saveTask = CompletableFuture.supplyAsync(() -> {
                try {
                    log.debug("Saving historical data asynchronously for symbol: {}", symbol);
                    historicalDataService.saveHistoricalData(historicalData);
                    log.debug("Successfully saved historical data for symbol: {}", symbol);
                    return null;
                } catch (Exception e) {
                    log.error("Error saving historical data asynchronously: {}", e.getMessage(), e);
                    throw new CompletionException(e);
                }
            }, marketDataExecutor.getExecutorService());
            
            // Wait for the save operation to complete
            try {
                saveTask.join();
            } catch (CompletionException e) {
                log.error("Failed to save historical data: {}", e.getMessage(), e);
                // We don't rethrow here as we still want to return the data even if saving failed
            }
            
            return historicalData;
        } catch (Exception e) {
            log.error("Error getting historical data: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getHistoricalData").increment();
            throw new RuntimeException("Failed to get historical data", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getHistoricalData"));
        }
    }

    @Override
    public List<Instrument> getAllInstruments() {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            // First check if instruments are already present in the database
            List<Instrument> existingInstruments = null;
            
            if (existingInstruments != null && !existingInstruments.isEmpty()) {
                log.info("Found {} existing instruments in database", existingInstruments.size());
                return existingInstruments;
            }
            
            log.info("No instruments found in database, fetching from provider");
            
            // If not found in database, fetch from provider
            MarketDataProvider provider = providerFactory.getProvider();
            List<com.zerodhatech.models.Instrument> instruments = retryOnFailure(() -> provider.getAllInstruments(), "getAllInstruments");
            
            // Convert the generic List<Object> to List<com.zerodhatech.models.Instrument>
            List<com.zerodhatech.models.Instrument> zerodhaInstruments = instruments.stream()
                .filter(obj -> obj instanceof com.zerodhatech.models.Instrument)
                .map(obj -> (com.zerodhatech.models.Instrument) obj)
                .collect(Collectors.toList());
            
            if (zerodhaInstruments != null && !zerodhaInstruments.isEmpty()) {
                log.info("Fetched {} instruments from provider, converting to common model", zerodhaInstruments.size());
                
                // Convert Zerodha instruments to common Instrument model
                List<Instrument> commonInstruments = instrumentMapper.toCommonInstruments(zerodhaInstruments);
                
                log.info("Converted {} instruments, saving to database", commonInstruments.size());
                
                // Save the converted instruments to the database
                instrumentService.saveAll(commonInstruments);
                
                log.info("Successfully saved {} instruments to database", commonInstruments.size());
                
                return commonInstruments;
            } else {
                log.warn("No instruments returned from provider");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error getting all instruments: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getAllInstruments").increment();
            throw new RuntimeException("Failed to get all instruments", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getAllInstruments"));
        }
    }
    
    @Override
    public List<Instrument> getInstrumentPagination(int page, int size, String symbol, String type, String exchange) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            // Get all instruments first
            List<Instrument> allInstruments = getAllInstruments();
            
            // Apply filters if provided
            List<Instrument> filteredInstruments = allInstruments.stream()
                .filter(instrument -> symbol == null || symbol.isEmpty() || 
                    instrument.getTradingSymbol().toLowerCase().contains(symbol.toLowerCase()))
                .filter(instrument -> type == null || type.isEmpty() || 
                    (instrument.getInstrumentType() != null && 
                     instrument.getInstrumentType().toString().equalsIgnoreCase(type)))
                .filter(instrument -> exchange == null || exchange.isEmpty() || 
                    (instrument.getSegment() != null && 
                     instrument.getSegment().toString().equalsIgnoreCase(exchange)))
                .collect(Collectors.toList());
            
            // Apply pagination
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, filteredInstruments.size());
            
            // Check if fromIndex is valid
            if (fromIndex >= filteredInstruments.size()) {
                return new ArrayList<>();
            }
            
            log.info("Returning page {} of size {} (filtered from {} instruments)", 
                page, size, filteredInstruments.size());
                
            return filteredInstruments.subList(fromIndex, toIndex);
        } catch (Exception e) {
            log.error("Error getting paginated instruments: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getInstruments").increment();
            throw new RuntimeException("Failed to get paginated instruments", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getInstruments"));
        }
    }

    @Override
    public List<Object> getSymbolsForExchange(String exchange) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            if (exchange == null || exchange.trim().isEmpty()) {
                throw new IllegalArgumentException("Exchange cannot be null or empty");
            }
            
            MarketDataProvider provider = providerFactory.getProvider();
            return retryOnFailure(() -> provider.getInstrumentsForExchange(exchange), "getInstrumentsForExchange");
        } catch (Exception e) {
            log.error("Error getting instruments for exchange {}: {}", exchange, e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getInstrumentsForExchange").increment();
            throw new RuntimeException("Failed to get instruments for exchange", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getInstrumentsForExchange"));
        }
    }

    @Override
    public Map<String, Object> logout() {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            MarketDataProvider provider = providerFactory.getProvider();
            boolean success = retryOnFailure(() -> provider.logout(), "logout");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("provider", provider.getProviderName());
            
            return response;
        } catch (Exception e) {
            log.error("Error logging out: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "logout").increment();
            throw new RuntimeException("Failed to logout", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "logout"));
        }
    }

    /**
     * Generic method to retry operations on failure with exponential backoff
     * 
     * @param callable The operation to retry
     * @param operationName Name of the operation for metrics and logging
     * @param <T> Return type of the operation
     * @return Result of the operation
     * @throws Exception If all retry attempts fail
     */
    private <T> T retryOnFailure(Callable<T> callable, String operationName) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                T result = callable.call();
                if (attempt > 1) {
                    log.info("Operation {} succeeded after {} attempts", operationName, attempt);
                }
                meterRegistry.counter("market.data.success.count", "operation", operationName).increment();
                return result;
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} for operation {} failed: {}", attempt, operationName, e.getMessage());
                meterRegistry.counter("market.data.retry.count", "operation", operationName).increment();
                
                if (attempt < maxRetries) {
                    try {
                        // Exponential backoff
                        long delay = retryDelayMs * (long) Math.pow(2, attempt - 1);
                        log.debug("Waiting {}ms before retry attempt {}", delay, attempt + 1);
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        log.error("Operation {} failed after {} attempts", operationName, maxRetries);
        throw new RuntimeException("Operation failed after " + maxRetries + " attempts", lastException);
    }

    /**
     * Validate instrument array
     * 
     * @param instruments Array of instruments to validate
     */
    private void validateInstruments(String[] instruments) {
        if (instruments == null || instruments.length == 0) {
            throw new IllegalArgumentException("Instruments cannot be null or empty");
        }
        
        for (String instrument : instruments) {
            if (instrument == null || instrument.trim().isEmpty()) {
                throw new IllegalArgumentException("Instrument cannot be null or empty");
            }
        }
    }
    
    /**
     * Get instruments by instrument IDs or all available instruments if no IDs provided
     * 
     * @param instrumentIds List of instrument IDs or null/empty for all instruments
     * @return Map of trading symbols to Instrument objects
     */
    private List<Instrument> getSymbolsToProcess(List<String> tradingSymbols) {
       return instrumentService.getInstrumentByTradingsymbols(tradingSymbols);
    }
    
    /**
     * Fetch live prices directly from the provider using instrument IDs
     * 
     * @param instrumentIds List of instrument IDs
     * @return List of equity prices
     */
    private List<EquityPrice> fetchLivePricesFromProvider(List<Long> instrumentIds) {
        log.info("[DATA_SOURCE] Fetching live prices directly from PROVIDER with {} instrument IDs", instrumentIds.size());
        
        if (instrumentIds == null || instrumentIds.isEmpty()) {
            log.warn("No valid instrument IDs provided");
            return Collections.emptyList();
        }
        
        log.info("Fetching live prices for {} instruments", instrumentIds.size());
        
        // Convert instrument IDs to string array for provider API
        String[] instrumentArray = instrumentIds.stream()
            .map(id -> id.toString())
            .toArray(String[]::new);
        
        // Create a map of trading symbols to instruments for the mapper
        Map<String, Instrument> instrumentMap = new HashMap<>();
        for (Long id : instrumentIds) {
            try {
                Optional<Instrument> instrumentOpt = instrumentService.getInstrumentByInstrumentToken(id);
                if (instrumentOpt.isPresent()) {
                    Instrument instrument = instrumentOpt.get();
                    String instrumentToken = instrument.getInstrumentToken().toString();
                    if (instrumentToken != null) {
                        instrumentMap.put(instrumentToken, instrument);
                    }
                }
            } catch (Exception e) {
                log.warn("Error retrieving instrument for ID {}: {}", id, e.getMessage());
            }
        }
        
        if (instrumentMap.isEmpty()) {
            log.warn("Could not find any instruments matching the provided IDs");
            return Collections.emptyList();
        }
        
        // Get OHLC data from provider with retry mechanism
        log.debug("[DATA_SOURCE] Calling provider.getOHLC with instrument IDs: {}", Arrays.toString(instrumentArray));
        Map<String, OHLCQuote> ohlcData;
        try {
            ohlcData = retryOnFailure(() -> providerFactory.getProvider().getOHLC(instrumentArray), "getOHLC");
        } catch (Exception e) {
            log.error("Error fetching OHLC data from provider: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
        log.debug("[DATA_SOURCE] Provider returned {} OHLC quotes", ohlcData != null ? ohlcData.size() : 0);
        
        if (ohlcData == null || ohlcData.isEmpty()) {
            log.warn("Provider returned empty OHLC data");
            return Collections.emptyList();
        }
        
        // Map OHLC data to equity prices using the mapper
        List<EquityPrice> prices = ohlcMapper.toEquityPrices(ohlcData, instrumentMap);
        log.info("[DATA_SOURCE] Successfully mapped {} OHLC quotes to {} equity prices from PROVIDER", 
                ohlcData != null ? ohlcData.size() : 0, prices.size());
        
        return prices;
    }
    
    @Override
    public List<EquityPrice> getLivePrices(List<String> tradingSymbols) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            log.info("Fetching live prices for {} instruments", tradingSymbols != null ? tradingSymbols.size() : "all");
            
            // Get symbols to process
            List<Instrument> instruments = getSymbolsToProcess(tradingSymbols);
            List<Long> instrumentIds = instruments.stream().map(Instrument::getInstrumentToken).collect(Collectors.toList());
            List<String> symbols = instruments.stream().map(Instrument::getTradingSymbol).collect(Collectors.toList());
            
            // Use the retry mechanism for resilience
            return retryOnFailure(new Callable<List<EquityPrice>>() {
                @Override
                public List<EquityPrice> call() throws Exception {
                    log.info("[DATA_SOURCE] Attempting to fetch prices from DATABASE first for {} symbols", symbols.size());
                    // First try to get prices from the database
                    List<EquityPrice> equityPrices = equityService.getPricesByTradingSymbols(symbols);
                    
                    // If database query returns empty results, fetch all from the provider
                    if (equityPrices == null || equityPrices.isEmpty()) {
                        log.info("[DATA_SOURCE] No prices found in DATABASE, switching to PROVIDER source");
                        equityPrices = fetchLivePricesFromProvider(instrumentIds);
                        log.info("[DATA_SOURCE] Successfully fetched {} prices from PROVIDER", equityPrices.size());
                        return equityPrices;
                    }
                    
                    log.info("[DATA_SOURCE] Successfully fetched {} prices from DATABASE", equityPrices.size());
                    
                    // Check if we got all requested symbols from the database
                    Set<String> foundSymbols = equityPrices.stream()
                            .map(EquityPrice::getSymbol)
                            .collect(Collectors.toSet());
                    
                    // Find missing symbols
                    List<String> missingSymbols = symbols.stream()
                            .filter(symbol -> !foundSymbols.contains(symbol))
                            .collect(Collectors.toList());
                    
                    // If we have missing symbols, fetch them from the provider
                    if (!missingSymbols.isEmpty()) {
                        log.info("[DATA_SOURCE] Found {} symbols in DATABASE, fetching {} missing symbols from PROVIDER", 
                                foundSymbols.size(), missingSymbols.size());
                        
                        // Get instrument IDs for missing symbols
                        List<Long> missingInstrumentIds = instruments.stream()
                                .filter(instrument -> missingSymbols.contains(instrument.getTradingSymbol()))
                                .map(Instrument::getInstrumentToken)
                                .collect(Collectors.toList());
                        
                        // Fetch missing symbols from provider
                        List<EquityPrice> missingPrices = fetchLivePricesFromProvider(missingInstrumentIds);
                        log.info("[DATA_SOURCE] Successfully fetched {} missing prices from PROVIDER", missingPrices.size());
                        
                        // Merge results
                        equityPrices.addAll(missingPrices);
                        log.info("[DATA_SOURCE] Combined {} total prices from DATABASE and PROVIDER", equityPrices.size());
                    }
                    
                    return equityPrices;
                }
            }, "getLivePrices");
        } catch (Exception e) {
            log.error("Error fetching live prices: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getLivePrices").increment();
            throw new RuntimeException("Failed to get live prices", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.request.time", "operation", "getLivePrices"));
        }
    }

    /**
     * ThreadPoolTaskExecutor for managing thread pool
     */
    private static class ThreadPoolTaskExecutor {
        private ExecutorService executorService;
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
        private String threadNamePrefix;

        public ExecutorService getExecutorService() {
            return executorService;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        public void initialize() {
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueCapacity);
            ThreadFactory threadFactory = r -> {
                Thread thread = new Thread(r);
                thread.setName(threadNamePrefix + thread.getId());
                return thread;
            };
            
            executorService = new ThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    60L,
                    TimeUnit.SECONDS,
                    queue,
                    threadFactory
            );
        }

        public void shutdown() {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
