package com.am.marketdata.internal.service;

import com.am.marketdata.service.provider.MarketDataProviderFactory;
import com.am.marketdata.common.model.OHLCQuoteV1;
import com.am.marketdata.common.model.TimeFrameV1;
import com.marketdata.common.model.MarketDataUpdate;
import com.am.marketdata.service.MarketDataService;
// import com.am.marketdata.api.util.InstrumentUtils;
import com.marketdata.common.model.HistoricalDataResponseV1;
// import com.am.marketdata.api.service.MarketDataFetchService;
import com.am.common.investment.model.historical.HistoricalData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service for internal market data ingestion and orchestration.
 * Responsible for keeping the cache warm by polling upstream providers.
 */
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class MarketDataIngestionService {

    private final MarketDataService marketDataService;
    // private final InstrumentUtils instrumentUtils;
    private final MarketDataProviderFactory marketDataProviderFactory;

    // Scheduler for polling
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<String, ScheduledFuture<?>> activeStreams = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Value("${market-data.stream.poll-interval-seconds:10}")
    private int pollIntervalSeconds;

    /**
     * Start ingestion stream for a set of instruments
     */
    public void startIngestion(List<String> instrumentKeys, String provider, String timeFrame, Boolean isIndexSymbol) {
        startIngestion(instrumentKeys, provider, timeFrame, isIndexSymbol, false);
    }

    public void startIngestion(List<String> instrumentKeys, String provider, String timeFrame, Boolean isIndexSymbol,
            boolean forceRefresh) {

        // Resolve Symbols
        // Set<String> resolvedSymbols = instrumentUtils.resolveSymbols(instrumentKeys,
        // false);
        Set<String> resolvedSymbols = new HashSet<>(instrumentKeys);
        log.info(
                "Starting data ingestion for {} instruments. Provider: {}, TimeFrame: {}",
                resolvedSymbols.size(), provider, timeFrame);

        String providerKey = provider != null ? provider.toUpperCase() : "UNKNOWN";
        final String finalTimeFrame = TimeFrameV1 != null ? TimeFrameV1 : "1D";

        // Cancel existing stream if any for this provider
        stopIngestion(providerKey);

        Runnable pollingTask = () -> {
            try {
                // Trigger fetch - this inherently updates the cache via
                // MarketDataFetchServiceImpl
                fetchMarketDataUpdate(
                        resolvedSymbols,
                        finalTimeFrame,
                        isIndexSymbol,
                        providerKey,
                        forceRefresh);

                log.debug("Ingestion polling cycle completed for provider {}", providerKey);

            } catch (Exception e) {
                log.error("Error during ingestion polling for provider {}", providerKey, e);
            }
        };

        // Schedule task
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(pollingTask, 0, pollIntervalSeconds,
                TimeUnit.SECONDS);
        activeStreams.put(providerKey, future);
        log.info("Ingestion started for provider: {} with interval: {} seconds", providerKey, pollIntervalSeconds);
    }

    public void stopIngestion(String provider) {
        if (provider == null)
            return;
        String key = provider.toUpperCase();
        if (activeStreams.containsKey(key)) {
            ScheduledFuture<?> future = activeStreams.get(key);
            if (future != null && !future.isCancelled()) {
                future.cancel(true);
            }
            activeStreams.remove(key);
            log.info("Ingestion stopped for provider: {}", provider);
        }
    }

    public MarketDataUpdate fetchMarketDataUpdate(
            Set<String> keys, String timeFrame, Boolean isIndexSymbol, String providerKey) {
        return fetchMarketDataUpdate(keys, timeFrame, isIndexSymbol, providerKey, false);
    }

    public MarketDataUpdate fetchMarketDataUpdate(
            Set<String> keys, String timeFrame, Boolean isIndexSymbol, String providerKey, boolean forceRefresh) {
        try {
            // Task 1: Fetch Live OHLC Data (Populates Cache)
            CompletableFuture<Map<String, OHLCQuoteV1>> liveDataFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return marketDataService.getOHLC(new ArrayList<>(keys), TimeFrame.DAY, forceRefresh, null);
                } catch (Exception e) {
                    log.error("Error fetching live OHLC data", e);
                    return new HashMap<>();
                }
            });

            // Task 2: Fetch Historical Data (if applicable)
            CompletableFuture<Map<String, HistoricalData>> historicalDataFuture;
            if ("1D".equalsIgnoreCase(TimeFrameV1) || "1W".equalsIgnoreCase(TimeFrameV1)
                    || "1M".equalsIgnoreCase(TimeFrameV1)) {
                historicalDataFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return fetchHistoricalData(keys, timeFrame, isIndexSymbol, forceRefresh);
                    } catch (Exception e) {
                        log.error("Error fetching historical data", e);
                        return new HashMap<>();
                    }
                });
            } else {
                historicalDataFuture = CompletableFuture.completedFuture(new HashMap<>());
            }

            // Wait for both tasks to complete
            CompletableFuture.allOf(liveDataFuture, historicalDataFuture).join();

            // Get results
            Map<String, OHLCQuoteV1> liveOhlcData = liveDataFuture.get();
            Map<String, HistoricalData> historicalResponse = historicalDataFuture.get();

            // Merge Data
            Map<String, OHLCQuoteV1> enrichedData = mergeData(liveOhlcData, historicalResponse);

            if (enrichedData != null && !enrichedData.isEmpty()) {
                Map<String, MarketDataUpdate.QuoteChange> quoteUpdates = buildQuoteUpdates(
                        enrichedData);

                return MarketDataUpdate.builder()
                        .timestamp(System.currentTimeMillis())
                        .quotes(quoteUpdates)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error fetching market data update", e);
        }
        return null;
    }

    private Map<String, HistoricalData> fetchHistoricalData(Set<String> symbols,
            String timeFrame, Boolean isIndexSymbol, boolean forceRefresh) {
        // Calculate historical date range based on timeFrame
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate historicalDate;
        java.time.LocalDate startDate;

        switch (timeFrame.toUpperCase()) {
            case "1D":
            case "DAY":
                historicalDate = today.minusDays(1);
                startDate = historicalDate.minusDays(7);
                break;
            case "1W":
            case "WEEK":
                historicalDate = today.minusWeeks(1);
                startDate = historicalDate.minusWeeks(4);
                break;
            case "1M":
            case "MONTH":
                historicalDate = today.minusMonths(1);
                startDate = historicalDate.minusMonths(6);
                break;
            default:
                historicalDate = today.minusDays(1);
                startDate = historicalDate.minusDays(7);
        }

        java.util.Date fromDate = java.util.Date.from(
                startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Date toDate = java.util.Date.from(
                historicalDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());

        Map<String, Object> additionalParams = new HashMap<>();
        if (isIndexSymbol != null && isIndexSymbol) {
            additionalParams.put("isIndexSymbol", true);
        }

        return marketDataService.getHistoricalDataBatch(
                new ArrayList<>(symbols),
                fromDate,
                toDate,
                TimeFrame.DAY,
                false, // continuous
                additionalParams,
                null,
                isIndexSymbol != null ? isIndexSymbol : false,
                forceRefresh);
    }

    private Map<String, OHLCQuoteV1> mergeData(Map<String, OHLCQuoteV1> liveData,
            Map<String, HistoricalData> historicalResponse) {
        Map<String, OHLCQuoteV1> enrichedData = new HashMap<>();

        if (liveData == null || liveData.isEmpty()) {
            return enrichedData;
        }

        for (Map.Entry<String, OHLCQuote> entry : liveData.entrySet()) {
            String symbol = entry.getKey();
            OHLCQuoteV1 liveQuote = entry.getValue();

            double previousClose = liveQuote.getPreviousClose();

            if (historicalResponse != null) {
                Map<String, HistoricalData> symbolsData = historicalResponse;

                if (symbolsData != null && symbolsData.containsKey(symbol)) {
                    HistoricalData historicalData = symbolsData.get(symbol);
                    if (historicalData != null && historicalData.getDataPoints() != null
                            && !historicalData.getDataPoints().isEmpty()) {
                        var dataPoints = historicalData.getDataPoints();
                        var lastPoint = dataPoints.get(dataPoints.size() - 1);
                        if (lastPoint.getClose() > 0) {
                            previousClose = lastPoint.getClose();
                        }
                    }
                }
            }

            OHLCQuoteV1 enrichedQuote = OHLCQuote.builder()
                    .lastPrice(liveQuote.getLastPrice())
                    .previousClose(previousClose)
                    .ohlc(liveQuote.getOhlc())
                    .build();

            enrichedData.put(symbol, enrichedQuote);
        }
        return enrichedData;
    }

    private Map<String, com.marketdata.common.model.MarketDataUpdate.QuoteChange> buildQuoteUpdates(
            Map<String, OHLCQuoteV1> ohlcQuotes) {

        Map<String, MarketDataUpdate.QuoteChange> quoteUpdates = new HashMap<>();

        for (Map.Entry<String, OHLCQuote> entry : ohlcQuotes.entrySet()) {
            String symbol = entry.getKey();
            OHLCQuoteV1 quote = entry.getValue();

            double lastPrice = quote.getLastPrice();
            double open = 0.0;
            double high = 0.0;
            double low = 0.0;
            double close = 0.0;

            if (quote.getOhlc() != null) {
                open = quote.getOhlc().getOpen();
                high = quote.getOhlc().getHigh();
                low = quote.getOhlc().getLow();
                close = quote.getOhlc().getClose();
            }

            double prevClose = quote.getPreviousClose();
            double change = 0.0;
            double changePercent = 0.0;

            if (prevClose > 0) {
                change = lastPrice - prevClose;
                changePercent = (change / prevClose) * 100;
                change = BigDecimal.valueOf(change).setScale(2, RoundingMode.HALF_UP).doubleValue();
                changePercent = BigDecimal.valueOf(changePercent).setScale(2, RoundingMode.HALF_UP).doubleValue();
            }

            MarketDataUpdate.QuoteChange update = MarketDataUpdate.QuoteChange
                    .builder()
                    .lastPrice(lastPrice)
                    .open(open)
                    .high(high)
                    .low(low)
                    .close(close)
                    .previousClose(prevClose)
                    .change(change)
                    .changePercent(changePercent)
                    .build();

            quoteUpdates.put(symbol, update);
        }

        return quoteUpdates;
    }
}
