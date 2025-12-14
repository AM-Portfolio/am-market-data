package com.am.marketdata.service;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.Instrument;
import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.service.instrument.InstrumentService;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.mapper.InstrumentMapper;
import com.am.marketdata.mapper.KiteModelMapper;
import com.am.marketdata.service.util.DataSourceType;
import com.am.marketdata.service.util.HistoricalDataRetriever;
import com.am.marketdata.service.util.MarketDataRetrievalUtil;
import com.am.marketdata.service.util.OHLCDataRetriever;
import com.marketdata.common.MarketDataProvider;
import com.marketdata.common.MarketDataProviderFactory;
import com.zerodhatech.models.LTPQuote;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Implementation of MarketDataService
 * Handles all market data processing logic including fetching, validation, and
 * processing
 */
@Slf4j
@Service
public class MarketDataService {

    private final MarketDataProviderFactory providerFactory;
    private final InstrumentService instrumentService;
    private final MeterRegistry meterRegistry;
    private final InstrumentMapper instrumentMapper;
    private final KiteModelMapper kiteModelMapper;
    private final MarketDataPersistenceService persistenceService;
    private final MarketDataRetrievalUtil marketDataRetrievalUtil;

    @Value("${market.data.max.retries:3}")
    private int maxRetries;

    @Value("${market.data.retry.delay.ms:1000}")
    private int retryDelayMs;

    public MarketDataService(MarketDataProviderFactory providerFactory, InstrumentService instrumentService,
            MeterRegistry meterRegistry, InstrumentMapper instrumentMapper,
            KiteModelMapper kiteModelMapper, MarketDataPersistenceService persistenceService,
            MarketDataRetrievalUtil marketDataRetrievalUtil) {
        this.providerFactory = providerFactory;
        this.instrumentService = instrumentService;
        this.meterRegistry = meterRegistry;
        this.instrumentMapper = instrumentMapper;
        this.kiteModelMapper = kiteModelMapper;
        this.persistenceService = persistenceService;
        this.marketDataRetrievalUtil = marketDataRetrievalUtil;
    }

    private OHLCDataRetriever createOHLCDataRetriever(String providerName) {
        return OHLCDataRetriever.builder()
                .persistenceService(persistenceService)
                .providerFactory(providerFactory)
                .retrievalOrder(Arrays.asList(DataSourceType.CACHE, DataSourceType.DATABASE, DataSourceType.PROVIDER))
                .cacheResults(true)
                .targetProviderName(providerName)
                .build();
    }

    private String resolveProviderName(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            providerName = persistenceService.getMarketDataCacheService().getActiveProvider();
        }
        if (providerName == null) {
            providerName = "zerodha";
        }
        return providerName;
    }

    public Map<String, String> getLoginUrl(String providerName) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            providerName = resolveProviderName(providerName);
            MarketDataProvider provider = providerFactory.getProvider(providerName);
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

    public Object generateSession(String requestToken) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            if (requestToken == null || requestToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Request token cannot be null or empty");
            }
            String providerName = persistenceService.getMarketDataCacheService().getActiveProvider();
            providerName = resolveProviderName(providerName);
            String finalProviderName = providerName;
            finalProviderName = "upstock"; // For lambda

            MarketDataProvider provider = providerFactory.getProvider(finalProviderName);
            Object session = marketDataRetrievalUtil.retryOnFailure(() -> provider.generateSession(requestToken),
                    "generateSession");

            // Set active provider
            persistenceService.getMarketDataCacheService().setActiveProvider(finalProviderName);

            return session;
        } catch (Exception e) {
            log.error("Error generating session: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "generateSession").increment();
            throw new RuntimeException("Failed to generate session", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "generateSession"));
        }
    }

    public Map<String, Object> getQuotes(String[] symbols, String providerName) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            providerName = resolveProviderName(providerName);
            // validateSymbols(symbols);

            MarketDataProvider provider = providerFactory.getProvider(providerName);
            // Must use final variable in lambda
            final MarketDataProvider finalProvider = provider;
            return retryOnFailure(() -> finalProvider.getQuotes(symbols), "getQuotes");
        } catch (Exception e) {
            log.error("Error getting quotes: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getQuotes").increment();
            throw new RuntimeException("Failed to get quotes", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getQuotes"));
        }
    }

    public Map<String, OHLCQuote> getOHLC(List<String> tradingSymbols, TimeFrame timeFrame, boolean forceRefresh,
            String providerName) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            providerName = resolveProviderName(providerName);

            OHLCDataRetriever retriever = createOHLCDataRetriever(providerName);
            Map<String, OHLCQuote> result = retriever.retrieveData(tradingSymbols, timeFrame, forceRefresh);
            return result;
        } catch (Exception e) {
            log.error("Error getting OHLC data for timeFrame {}: {}", timeFrame, e.getMessage(), e);
            meterRegistry
                    .counter("market.data.failure.count", "operation", "getOHLC", "timeFrame", timeFrame.getApiValue())
                    .increment();
            throw new RuntimeException("Failed to get OHLC data for timeFrame " + timeFrame, e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getOHLC", "timeFrame",
                    timeFrame.getApiValue()));
        }
    }

    public HistoricalData getHistoricalData(String symbol, Date fromDate, Date toDate, TimeFrame interval,
            boolean continuous, Map<String, Object> additionalParams, String providerName) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            providerName = resolveProviderName(providerName);

            HistoricalDataRetriever retriever = HistoricalDataRetriever.builder()
                    .persistenceService(persistenceService)
                    .providerFactory(providerFactory)
                    .retrievalOrder(
                            Arrays.asList(DataSourceType.CACHE, DataSourceType.DATABASE, DataSourceType.PROVIDER))
                    .cacheResults(true)
                    .fromDate(fromDate)
                    .toDate(toDate)
                    .interval(interval)
                    .continuous(continuous)
                    .additionalParams(additionalParams)
                    .targetProviderName(providerName)
                    .build();

            Map<String, HistoricalData> result = retriever.retrieveData(
                    Collections.singletonList(symbol),
                    interval,
                    false);

            return result.get(symbol);
        } catch (Exception e) {
            log.error("Error getting historical data: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getHistoricalData").increment();
            throw new RuntimeException("Failed to get historical data", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getHistoricalData"));
        }
    }

    public List<Instrument> getAllSymbols(String providerName) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            providerName = resolveProviderName(providerName);
            log.info("Fetching instruments from provider: {}", providerName);

            MarketDataProvider provider = providerFactory.getProvider(providerName);
            final MarketDataProvider finalProvider = provider;
            List<com.zerodhatech.models.Instrument> instruments = retryOnFailure(
                    () -> finalProvider.getAllInstruments(),
                    "getAllInstruments");

            List<com.zerodhatech.models.Instrument> zerodhaInstruments = instruments.stream()
                    .filter(obj -> obj instanceof com.zerodhatech.models.Instrument)
                    .map(obj -> (com.zerodhatech.models.Instrument) obj)
                    .collect(Collectors.toList());

            if (zerodhaInstruments != null && !zerodhaInstruments.isEmpty()) {
                log.info("Fetched {} symbols from provider, converting to common model", zerodhaInstruments.size());

                List<Instrument> commonInstruments = instrumentMapper.toCommonInstruments(zerodhaInstruments);

                log.info("Converted {} instruments, saving to database", commonInstruments.size());

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

    public List<Instrument> getSymbolPagination(int page, int size, String symbol, String type, String exchange,
            String providerName) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            providerName = resolveProviderName(providerName);
            // Get all instruments first
            List<Instrument> allInstruments = getAllSymbols(providerName);

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

    public List<Object> getSymbolsForExchange(String exchange, String providerName) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            if (exchange == null || exchange.trim().isEmpty()) {
                throw new IllegalArgumentException("Exchange cannot be null or empty");
            }

            providerName = resolveProviderName(providerName);
            MarketDataProvider provider = providerFactory.getProvider(providerName);
            final MarketDataProvider finalProvider = provider;
            return retryOnFailure(() -> finalProvider.getSymbolsForExchange(exchange), "getSymbolsForExchange");
        } catch (Exception e) {
            log.error("Error getting symbols for exchange {}: {}", exchange, e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getSymbolsForExchange").increment();
            throw new RuntimeException("Failed to get symbols for exchange", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.operation.time", "operation", "getSymbolsForExchange"));
        }
    }

    public Map<String, Object> logout(String providerName) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            providerName = resolveProviderName(providerName);
            MarketDataProvider provider = providerFactory.getProvider(providerName);
            final MarketDataProvider finalProvider = provider;
            boolean success = retryOnFailure(() -> finalProvider.logout(), "logout");

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
     * Execute a supplier with retry logic
     * 
     * @param supplier      The supplier to execute
     * @param operationName The name of the operation (for logging)
     * @param <T>           The return type
     * @return The result of the supplier
     */
    private <T> T retryOnFailure(Supplier<T> supplier, String operationName) {
        try {
            // Convert Supplier to Callable for compatibility with MarketDataRetrievalUtil
            return marketDataRetrievalUtil.retryOnFailure(() -> supplier.get(), operationName);
        } catch (Exception e) {
            log.error("Error in operation {}: {}", operationName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute operation: " + operationName, e);
        }
    }

    /**
     * Fetch live prices directly from the provider using instrument IDs
     * 
     * @param instrumentIds List of instrument IDs
     * @return List of equity prices
     */
    private List<EquityPrice> fetchLivePricesFromProvider(List<String> tradingSymbols, String providerName) {
        providerName = resolveProviderName(providerName);
        log.info("[DATA_SOURCE] Fetching live prices directly from PROVIDER: {} with {} instrument IDs", providerName,
                tradingSymbols.size());

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
        log.debug("[DATA_SOURCE] Calling provider.getLTP with instrument IDs: {}", (Object) symbols);
        Map<String, LTPQuote> ltpData;
        try {
            MarketDataProvider provider = providerFactory.getProvider(providerName);
            final MarketDataProvider finalProvider = provider;
            ltpData = retryOnFailure(() -> finalProvider.getLTP(symbols), "getLTP");
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

    public List<EquityPrice> getLivePrices(List<String> tradingSymbols, String providerName) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            log.info("Fetching live prices for {} instruments", tradingSymbols != null ? tradingSymbols.size() : "all");

            // Get data directly from provider - caching is handled at the service level
            return fetchLivePricesFromProvider(tradingSymbols, providerName);
        } catch (Exception e) {
            log.error("Error fetching live prices: {}", e.getMessage(), e);
            meterRegistry.counter("market.data.failure.count", "operation", "getLivePrices").increment();
            throw new RuntimeException("Failed to get live prices", e);
        } finally {
            timer.stop(meterRegistry.timer("market.data.request.time", "operation", "getLivePrices"));
        }
    }

}
