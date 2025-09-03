package com.am.marketdata.service.impl;

import com.am.marketdata.service.MarketDataService;
import com.am.marketdata.service.MarketDataPersistenceService;
import com.am.marketdata.common.model.OHLCQuote;
import com.marketdata.common.MarketDataProvider;
import com.marketdata.common.MarketDataProviderFactory;
import com.zerodhatech.models.LTPQuote;
import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.Instrument;
import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.service.instrument.InstrumentService;
import com.am.marketdata.mapper.HistoryDataMapper;
import com.am.marketdata.mapper.InstrumentMapper;
import com.am.marketdata.mapper.KiteModelMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    private final MeterRegistry meterRegistry;
    private final InstrumentMapper instrumentMapper;
    private final KiteModelMapper kiteModelMapper;
    private final MarketDataPersistenceService persistenceService;

    @Value("${market.data.max.retries:3}")
    private int maxRetries;

    @Value("${market.data.retry.delay.ms:1000}")
    private int retryDelayMs;


    public MarketDataServiceImpl(MarketDataProviderFactory providerFactory, InstrumentService instrumentService, 
                               MeterRegistry meterRegistry, InstrumentMapper instrumentMapper, 
                               KiteModelMapper kiteModelMapper, MarketDataPersistenceService persistenceService) {
        this.providerFactory = providerFactory;
        this.instrumentService = instrumentService;
        this.meterRegistry = meterRegistry;
        this.instrumentMapper = instrumentMapper;
        this.kiteModelMapper = kiteModelMapper;
        this.persistenceService = persistenceService;
    }


    @Override
    public Map<String, OHLCQuote> getOHLC(List<String> tradingSymbols, boolean forceRefresh) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            validateSymbols(tradingSymbols);
            
            // Result map to store all found quotes
            Map<String, OHLCQuote> result = new HashMap<>();
            
            // Keep track of symbols we still need to find
            Set<String> remainingSymbols = new HashSet<>(tradingSymbols);
            
            // Step 1: Try to get data from cache first (if not forcing refresh)
            if (!forceRefresh) {
                log.info("[DATA_SOURCE] Attempting to fetch OHLC data from cache for {} symbols", 
                        remainingSymbols.size());
                Map<String, OHLCQuote> cachedData = persistenceService.getOHLCData(tradingSymbols, false);
                
                if (cachedData != null && !cachedData.isEmpty()) {
                    log.info("[DATA_SOURCE] Found {} OHLC quotes in cache", cachedData.size());
                    result.putAll(cachedData);
                    
                    // Remove found symbols from the remaining set
                    cachedData.keySet().forEach(symbol -> 
                        remainingSymbols.remove(symbol.replace("NSE:", "")));
                    
                    log.info("[DATA_SOURCE] {} symbols remaining after cache lookup", remainingSymbols.size());
                } else {
                    log.info("[DATA_SOURCE] No OHLC data found in cache");
                }
            } else {
                log.info("[DATA_SOURCE] Skipping cache lookup due to force refresh");
            }
            
            // Step 2: If we still have symbols to find, try database
            if (!remainingSymbols.isEmpty()) {
                log.info("[DATA_SOURCE] Attempting to fetch OHLC data from database for {} symbols", 
                        remainingSymbols.size());
                
                // Convert to list for database lookup
                List<String> remainingSymbolsList = new ArrayList<>(remainingSymbols);
                
                // Force refresh is false here because we're explicitly looking for these symbols in the database
                Map<String, OHLCQuote> dbData = persistenceService.getOHLCData(remainingSymbolsList, true);
                
                if (dbData != null && !dbData.isEmpty()) {
                    log.info("[DATA_SOURCE] Found {} OHLC quotes in database", dbData.size());
                    result.putAll(dbData);
                    
                    // Remove found symbols from the remaining set
                    dbData.keySet().forEach(symbol -> 
                        remainingSymbols.remove(symbol.replace("NSE:", "")));
                    
                    log.info("[DATA_SOURCE] {} symbols remaining after database lookup", remainingSymbols.size());
                } else {
                    log.info("[DATA_SOURCE] No OHLC data found in database");
                }
            }
            
            // Step 3: If we still have symbols to find, fetch from provider API
            if (!remainingSymbols.isEmpty()) {
                log.info("[DATA_SOURCE] Fetching OHLC data from provider API for {} symbols", remainingSymbols.size());
                
                // Format symbols for provider API call
                List<String> symbols = remainingSymbols.stream()
                    .map(id -> "NSE:" + id)
                    .collect(Collectors.toList());
                    
                MarketDataProvider provider = providerFactory.getProvider();
                Map<String, OHLCQuote> providerData = retryOnFailure(() -> provider.getOHLC(symbols), "getOHLC");
                
                if (providerData != null && !providerData.isEmpty()) {
                    log.info("[DATA_SOURCE] Successfully fetched {} OHLC quotes from provider", providerData.size());
                    result.putAll(providerData);
                    
                    // Save the data to persistence layer asynchronously
                    persistenceService.saveOHLCData(providerData);
                } else {
                    log.warn("[DATA_SOURCE] Provider returned empty OHLC data");
                }
            }
            
            // Log final results
            if (result.isEmpty()) {
                log.warn("[DATA_SOURCE] Could not find OHLC data for any of the requested symbols");
            } else if (result.size() < tradingSymbols.size()) {
                log.warn("[DATA_SOURCE] Partial OHLC data: found {} out of {} requested symbols", 
                        result.size(), tradingSymbols.size());
            } else {
                log.info("[DATA_SOURCE] Successfully retrieved OHLC data for all {} requested symbols", 
                        tradingSymbols.size());
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error getting OHLC data: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getOHLC").increment();
            throw new RuntimeException("Failed to get OHLC data", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getOHLC"));
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
            //validateSymbols(symbols);
            
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
            
            // Convert dates to string format for persistence service
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = sdf.format(fromDate);
            String toDateStr = sdf.format(toDate);
            
            // First try to get data from persistence layer (cache or database)
            log.info("[DATA_SOURCE] Attempting to fetch historical data from persistence layer for symbol: {}", symbol);
            HistoricalData historicalData = persistenceService.getHistoricalData(symbol, interval, fromDateStr, toDateStr);
            
            // Check if we got the data from persistence
            if (historicalData == null || historicalData.getDataPoints() == null || historicalData.getDataPoints().isEmpty()) {
                log.info("[DATA_SOURCE] No historical data found in persistence layer for symbol: {}", symbol);
                
                // If not found in persistence, fetch from provider
                log.info("[DATA_SOURCE] Fetching historical data from provider for symbol: {}", symbol);
                MarketDataProvider provider = providerFactory.getProvider();
                com.zerodhatech.models.HistoricalData zerodhaHistoricalData = retryOnFailure(() -> provider.getHistoricalData(
                        symbol, fromDate, toDate, interval, continuous, additionalParams), "getHistoricalData");

                HistoryDataMapper historicalDataMapper = new HistoryDataMapper();
                historicalData = historicalDataMapper.toCommonHistoricalData(zerodhaHistoricalData);
                historicalData.setTradingSymbol(symbol);
                
                // Save the data to persistence layer asynchronously
                log.info("[DATA_SOURCE] Saving historical data to persistence layer for symbol: {}", symbol);
                persistenceService.saveHistoricalData(symbol, interval, historicalData);
            } else {
                log.info("[DATA_SOURCE] Found historical data in persistence layer for symbol: {}, data points: {}", 
                         symbol, historicalData.getDataPoints().size());
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
    public List<Instrument> getAllSymbols() {
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
                log.info("Fetched {} symbols from provider, converting to common model", zerodhaInstruments.size());
                
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
            log.error("Error fetching all symbols: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getAllSymbols").increment();
            throw new RuntimeException("Failed to get all instruments", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getAllSymbols"));
        }
    }
    
    @Override
    public List<Instrument> getSymbolPagination(int page, int size, String symbol, String type, String exchange) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            // Get all instruments first
            List<Instrument> allInstruments = getAllSymbols();
            
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
            log.error("Error fetching paginated symbols: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getSymbolPagination").increment();
            throw new RuntimeException("Failed to get paginated instruments", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getSymbolPagination"));
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
            return retryOnFailure(() -> provider.getSymbolsForExchange(exchange), "getSymbolsForExchange");
        } catch (Exception e) {
            log.error("Error getting symbols for exchange {}: {}", exchange, e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getSymbolsForExchange").increment();
            throw new RuntimeException("Failed to get symbols for exchange", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getSymbolsForExchange"));
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
     * Validate symbols array
     * 
     * @param symbols Array of symbols to validate
     */
    private void validateSymbols(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("Symbols cannot be null or empty");
        }
        
        for (String symbol : symbols) {
            if (symbol == null || symbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }
        }
    }

    
    /**
     * Fetch live prices directly from the provider using instrument IDs
     * 
     * @param instrumentIds List of instrument IDs
     * @return List of equity prices
     */
    private List<EquityPrice> fetchLivePricesFromProvider(List<String> tradingSymbols) {
        log.info("[DATA_SOURCE] Fetching live prices directly from PROVIDER with {} instrument IDs", tradingSymbols.size());
        
        if (tradingSymbols == null || tradingSymbols.isEmpty()) {
            log.warn("No valid instrument IDs provided");
            return Collections.emptyList();
        }
        
        log.info("Fetching live prices for {} instruments", tradingSymbols.size());
        
        // Convert instrument IDs to string array for provider API
        String[] symbols = tradingSymbols.stream()
            .map(id -> "NSE:" + id.toString())
            .toArray(String[]::new);
        
        
        // Get OHLC data from provider with retry mechanism
        log.debug("[DATA_SOURCE] Calling provider.getLTP with instrument IDs: {}", symbols);
        Map<String, LTPQuote> ltpData;
        try {
            ltpData = retryOnFailure(() -> providerFactory.getProvider().getLTP(symbols), "getLTP");
        } catch (Exception e) {
            log.error("Error fetching OHLC data from provider: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
        log.debug("[DATA_SOURCE] Provider returned {} OHLC quotes", ltpData != null ? ltpData.size() : 0);
        
        if (ltpData == null || ltpData.isEmpty()) {
            log.warn("Provider returned empty OHLC data");
            return Collections.emptyList();
        }
        
        // Map OHLC data to equity prices using the mapper
        List<EquityPrice> prices = kiteModelMapper.mapLTPquoteToEquityPrices(ltpData);
        log.info("[DATA_SOURCE] Successfully mapped {} OHLC quotes to {} equity prices from PROVIDER", 
                ltpData != null ? ltpData.size() : 0, prices.size());
        
        return prices;
    }
    
    @Override
    public List<EquityPrice> getLivePrices(List<String> tradingSymbols) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            log.info("Fetching live prices for {} instruments", tradingSymbols != null ? tradingSymbols.size() : "all");
            
            // Get data directly from provider - caching is handled at the service level
            return fetchLivePricesFromProvider(tradingSymbols);
        } catch (Exception e) {
            log.error("Error fetching live prices: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getLivePrices").increment();
            throw new RuntimeException("Failed to get live prices", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.request.time", "operation", "getLivePrices"));
        }
    }

}
