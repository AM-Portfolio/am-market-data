package com.am.marketdata.service.util;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.mapper.HistoryDataMapper;
import com.am.marketdata.service.MarketDataPersistenceService;
import com.marketdata.common.MarketDataProvider;
import com.marketdata.common.MarketDataProviderFactory;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Concrete implementation of AbstractMarketDataRetriever for Historical data.
 * Handles retrieval of historical data from cache, database, and provider.
 */
@Slf4j
public class HistoricalDataRetriever extends AbstractMarketDataRetriever<String, HistoricalData> {

    private final Date fromDate;
    private final Date toDate;
    private final TimeFrame interval;
    private final boolean continuous;
    private final Map<String, Object> additionalParams;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private HistoricalDataRetriever(
            MarketDataPersistenceService persistenceService,
            MarketDataProviderFactory providerFactory,
            List<DataSourceType> retrievalOrder,
            boolean cacheResults,
            Date fromDate,
            Date toDate,
            TimeFrame interval,
            boolean continuous,
            Map<String, Object> additionalParams,
            String targetProviderName) {
        super(persistenceService, providerFactory, retrievalOrder, cacheResults, targetProviderName);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.interval = interval;
        this.continuous = continuous;
        this.additionalParams = additionalParams != null ? additionalParams : new HashMap<>();
    }

    /**
     * Retrieve historical data from cache
     *
     * @param allSymbols       All symbols being requested
     * @param remainingSymbols Set of symbols that still need to be retrieved (will
     *                         be modified)
     * @param timeFrame        The time frame for the data (ignored as we use the
     *                         interval from constructor)
     * @return Map of symbol to historical data
     */
    @Override
    protected Map<String, HistoricalData> retrieveFromCache(List<String> allSymbols, Set<String> remainingSymbols,
            TimeFrame timeFrame) {
        if (remainingSymbols.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("[CACHE] Attempting to fetch historical data from cache for {} symbols",
                remainingSymbols.size());

        Map<String, HistoricalData> result = new HashMap<>();
        String fromDateStr = dateFormat.format(fromDate);
        String toDateStr = dateFormat.format(toDate);

        for (String symbol : new ArrayList<>(remainingSymbols)) {
            try {
                HistoricalData data = persistenceService.getHistoricalData(symbol, interval, fromDateStr, toDateStr);
                if (data != null && data.getDataPoints() != null && !data.getDataPoints().isEmpty()) {
                    result.put(symbol, data);
                    remainingSymbols.remove(symbol);
                    log.debug("[CACHE] Found historical data for symbol: {}", symbol);
                }
            } catch (Exception e) {
                log.warn("[CACHE] Error retrieving historical data for symbol {}: {}", symbol, e.getMessage());
            }
        }

        log.info("[CACHE] Found historical data for {}/{} symbols in cache",
                result.size(), allSymbols.size());
        log.info("[CACHE] {} symbols remaining after cache lookup", remainingSymbols.size());

        return result;
    }

    /**
     * Retrieve historical data from database
     *
     * @param remainingSymbols Set of symbols that still need to be retrieved (will
     *                         be modified)
     * @param timeFrame        The time frame for the data (ignored as we use the
     *                         interval from constructor)
     * @return Map of symbol to historical data
     */
    @Override
    protected Map<String, HistoricalData> retrieveFromDatabase(Set<String> remainingSymbols, TimeFrame timeFrame) {
        if (remainingSymbols.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("[DATABASE] Attempting to fetch historical data from database for {} symbols",
                remainingSymbols.size());

        Map<String, HistoricalData> result = new HashMap<>();
        String fromDateStr = dateFormat.format(fromDate);
        String toDateStr = dateFormat.format(toDate);

        for (String symbol : new ArrayList<>(remainingSymbols)) {
            try {
                // Force database lookup by setting forceRefresh to true in the persistence
                // service
                HistoricalData data = persistenceService.getHistoricalData(symbol, interval, fromDateStr, toDateStr);
                if (data != null && data.getDataPoints() != null && !data.getDataPoints().isEmpty()) {
                    result.put(symbol, data);
                    remainingSymbols.remove(symbol);
                    log.debug("[DATABASE] Found historical data for symbol: {}", symbol);
                }
            } catch (Exception e) {
                log.warn("[DATABASE] Error retrieving historical data for symbol {}: {}", symbol, e.getMessage());
            }
        }

        log.info("[DATABASE] Found historical data for {} symbols in database", result.size());
        log.info("[DATABASE] {} symbols remaining after database lookup", remainingSymbols.size());

        return result;
    }

    /**
     * Retrieve historical data from provider
     *
     * @param provider The market data provider
     * @param symbols  List of symbols to retrieve
     * @return Map of symbol to historical data
     */
    @Override
    protected Map<String, HistoricalData> retrieveFromProvider(MarketDataProvider provider, List<String> symbols) {
        if (symbols.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("[PROVIDER] Fetching historical data from provider for {} symbols", symbols.size());

        Map<String, HistoricalData> result = new HashMap<>();
        HistoryDataMapper historicalDataMapper = new HistoryDataMapper();

        for (String symbol : symbols) {
            try {
                com.zerodhatech.models.HistoricalData zerodhaHistoricalData = provider.getHistoricalData(symbol,
                        fromDate, toDate, interval, continuous, additionalParams);

                if (zerodhaHistoricalData != null) {
                    HistoricalData historicalData = historicalDataMapper.toCommonHistoricalData(zerodhaHistoricalData);
                    historicalData.setTradingSymbol(symbol);
                    result.put(symbol, historicalData);
                    log.debug("[PROVIDER] Successfully fetched historical data for symbol: {}", symbol);
                } else {
                    log.warn("[PROVIDER] No historical data returned for symbol: {}", symbol);
                }
            } catch (Exception e) {
                log.error("[PROVIDER] Error fetching historical data for symbol {}: {}", symbol, e.getMessage(), e);
            }
        }

        log.info("[PROVIDER] Successfully fetched historical data for {}/{} symbols",
                result.size(), symbols.size());

        return result;
    }

    /**
     * Save historical data to persistence asynchronously (both database and cache)
     *
     * @param data The data to save
     */
    @Override
    protected void saveDataAsync(Map<String, HistoricalData> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, HistoricalData> entry : data.entrySet()) {
                persistenceService.saveHistoricalData(entry.getKey(), interval, entry.getValue());
            }
            log.debug("Initiated async save of historical data for {} symbols to database and cache", data.size());
        } catch (Exception e) {
            log.error("Error initiating async save of historical data: {}", e.getMessage(), e);
        }
    }

    /**
     * Update only the cache with the provided historical data, without saving to
     * database
     *
     * @param data The data to update in the cache
     */
    @Override
    protected void updateCacheOnly(Map<String, HistoricalData> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, HistoricalData> entry : data.entrySet()) {
                // Use the MarketDataCacheService directly to update only the cache
                persistenceService.getMarketDataCacheService().cacheHistoricalData(
                        entry.getKey(), interval, entry.getValue());
            }
            log.debug("Updated cache with historical data for {} symbols", data.size());
        } catch (Exception e) {
            log.error("Error updating cache with historical data: {}", e.getMessage(), e);
        }
    }

    /**
     * Builder for HistoricalDataRetriever
     */
    public static class Builder extends AbstractBuilder<String, HistoricalData, Builder, HistoricalDataRetriever> {
        private Date fromDate;
        private Date toDate;
        private TimeFrame interval;
        private boolean continuous;
        private Map<String, Object> additionalParams;

        public Builder fromDate(Date fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder toDate(Date toDate) {
            this.toDate = toDate;
            return this;
        }

        public Builder interval(TimeFrame interval) {
            this.interval = interval;
            return this;
        }

        public Builder continuous(boolean continuous) {
            this.continuous = continuous;
            return this;
        }

        public Builder additionalParams(Map<String, Object> additionalParams) {
            this.additionalParams = additionalParams;
            return this;
        }

        @Override
        public HistoricalDataRetriever build() {
            if (persistenceService == null) {
                throw new IllegalStateException("PersistenceService must be provided");
            }
            if (providerFactory == null) {
                throw new IllegalStateException("ProviderFactory must be provided");
            }
            if (fromDate == null) {
                throw new IllegalStateException("FromDate must be provided");
            }
            if (toDate == null) {
                throw new IllegalStateException("ToDate must be provided");
            }
            if (interval == null) {
                throw new IllegalStateException("Interval must be provided");
            }

            return new HistoricalDataRetriever(
                    persistenceService,
                    providerFactory,
                    retrievalOrder,
                    cacheResults != null ? cacheResults : true,
                    fromDate,
                    toDate,
                    interval,
                    continuous,
                    additionalParams,
                    targetProviderName);
        }
    }

    /**
     * Create a new builder for HistoricalDataRetriever
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
