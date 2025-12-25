package com.am.marketdata.api.service;

import com.am.marketdata.api.websocket.MarketDataWebSocketHandler;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class MarketDataPollingService {

    private final MarketDataFetchService marketDataFetchService;
    private final MarketDataWebSocketHandler webSocketHandler;

    // Scheduler for polling
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<String, ScheduledFuture<?>> activeStreams = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Value("${market-data.stream.poll-interval-seconds:10}")
    private int pollIntervalSeconds;

    public void connectStream(List<String> instrumentKeys, String modeStr, String provider, String timeFrame,
            Boolean isIndexSymbol) {
        // Symbols are already resolved by the controller based on expandIndices
        // parameter
        // No need to resolve again here
        log.info(
                "Initiating stream simulation via polling for {} instruments. Provider: {}, TimeFrame: {}, IsIndexSymbol: {}",
                instrumentKeys.size(), provider, timeFrame, isIndexSymbol);

        String providerKey = provider != null ? provider.toUpperCase() : "UNKNOWN";
        final String finalTimeFrame = timeFrame != null ? timeFrame : "1D";

        // Cancel existing stream if any for this provider
        disconnectStream(providerKey);

        Runnable pollingTask = () -> {
            try {
                Set<String> keys = new HashSet<>(instrumentKeys);

                // Parallel Execution using CompletableFuture for independent tasks

                // Task 1: Fetch Live OHLC Data
                CompletableFuture<Map<String, OHLCQuote>> liveDataFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return marketDataFetchService.getOHLC(keys, false, TimeFrame.DAY, false);
                    } catch (Exception e) {
                        log.error("Error fetching live OHLC data", e);
                        return new HashMap<>();
                    }
                });

                // Task 2: Fetch Historical Data (if applicable)
                CompletableFuture<Map<String, Object>> historicalDataFuture;
                if ("1D".equalsIgnoreCase(finalTimeFrame) || "1W".equalsIgnoreCase(finalTimeFrame)
                        || "1M".equalsIgnoreCase(finalTimeFrame)) {
                    historicalDataFuture = CompletableFuture.supplyAsync(() -> {
                        try {
                            return fetchHistoricalData(keys, finalTimeFrame, isIndexSymbol);
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
                Map<String, OHLCQuote> liveOhlcData = liveDataFuture.get();
                Map<String, Object> historicalResponse = historicalDataFuture.get();

                // Merge Data
                Map<String, OHLCQuote> enrichedData = mergeData(liveOhlcData, historicalResponse);

                // Step 3: Build and broadcast quote updates
                if (enrichedData != null && !enrichedData.isEmpty()) {
                    Map<String, com.am.marketdata.api.model.MarketDataUpdate.QuoteChange> quoteUpdates = buildQuoteUpdates(
                            enrichedData);

                    com.am.marketdata.api.model.MarketDataUpdate update = com.am.marketdata.api.model.MarketDataUpdate
                            .builder()
                            .provider(providerKey)
                            .timestamp(System.currentTimeMillis())
                            .quotes(quoteUpdates)
                            .build();

                    webSocketHandler.broadcast(update);
                }
            } catch (Exception e) {
                log.error("Error during polling stream execution for provider {}", providerKey, e);
            }
        };

        // Schedule task - use configured interval
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(pollingTask, 0, pollIntervalSeconds,
                TimeUnit.SECONDS);
        activeStreams.put(providerKey, future);
        log.info("Polling stream started for provider: {} with interval: {} seconds", providerKey, pollIntervalSeconds);
    }

    public void disconnectStream(String provider) {
        if (provider == null)
            return;
        String key = provider.toUpperCase();
        if (activeStreams.containsKey(key)) {
            ScheduledFuture<?> future = activeStreams.get(key);
            if (future != null && !future.isCancelled()) {
                future.cancel(true);
            }
            activeStreams.remove(key);
            log.info("Polling stream stopped for provider: {}", provider);
        }
    }

    /**
     * Fetches historical data based on timeFrame
     */
    private Map<String, Object> fetchHistoricalData(Set<String> symbols, String timeFrame, Boolean isIndexSymbol) {
        // Calculate historical date range based on timeFrame
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate historicalDate;

        switch (timeFrame.toUpperCase()) {
            case "1D":
            case "DAY":
                historicalDate = today.minusDays(1);
                break;
            case "1W":
            case "WEEK":
                historicalDate = today.minusWeeks(1);
                break;
            case "1M":
            case "MONTH":
                historicalDate = today.minusMonths(1);
                break;
            default:
                historicalDate = today.minusDays(1);
        }

        String historicalDateStr = historicalDate.toString();

        // Convert LocalDate to Date for API call
        java.util.Date fromDate = java.util.Date.from(
                historicalDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Date toDate = fromDate;

        log.info("Fetching historical data for {} symbols from {} (timeFrame: {})",
                symbols.size(), historicalDateStr, timeFrame);

        Map<String, Object> additionalParams = new HashMap<>();
        if (isIndexSymbol != null && isIndexSymbol) {
            additionalParams.put("isIndexSymbol", true);
        }

        return marketDataFetchService.getHistoricalDataMultipleSymbols(
                symbols,
                fromDate,
                toDate,
                TimeFrame.DAY,
                "STOCK",
                additionalParams,
                false);
    }

    /**
     * Merges historical data into live OHLC data
     */
    private Map<String, OHLCQuote> mergeData(Map<String, OHLCQuote> liveData, Map<String, Object> historicalResponse) {
        Map<String, OHLCQuote> enrichedData = new HashMap<>();

        if (liveData == null || liveData.isEmpty()) {
            return enrichedData;
        }

        for (Map.Entry<String, OHLCQuote> entry : liveData.entrySet()) {
            String symbol = entry.getKey();
            OHLCQuote liveQuote = entry.getValue();

            double previousClose = liveQuote.getPreviousClose();

            // Extract historical data from response
            if (historicalResponse != null && historicalResponse.containsKey(symbol)) {
                Object symbolData = historicalResponse.get(symbol);

                if (symbolData instanceof com.am.common.investment.model.historical.HistoricalData) {
                    com.am.common.investment.model.historical.HistoricalData historicalData = (com.am.common.investment.model.historical.HistoricalData) symbolData;

                    if (historicalData.getDataPoints() != null && !historicalData.getDataPoints().isEmpty()) {
                        // Get the last data point (historical close)
                        var dataPoints = historicalData.getDataPoints();
                        var lastPoint = dataPoints.get(dataPoints.size() - 1);

                        if (lastPoint.getClose() > 0) {
                            previousClose = lastPoint.getClose();
                            log.debug("Updated previous close for {}: {} (from historical data)",
                                    symbol, previousClose);
                        }
                    }
                }
            }

            // Build enriched quote with previous close from historical data
            OHLCQuote enrichedQuote = OHLCQuote.builder()
                    .lastPrice(liveQuote.getLastPrice())
                    .previousClose(previousClose)
                    .ohlc(liveQuote.getOhlc())
                    .build();

            enrichedData.put(symbol, enrichedQuote);
        }
        return enrichedData;
    }

    /**
     * Builds QuoteChange objects from OHLC quotes
     * Separated for better maintainability
     */
    private Map<String, com.am.marketdata.api.model.MarketDataUpdate.QuoteChange> buildQuoteUpdates(
            Map<String, OHLCQuote> ohlcQuotes) {

        Map<String, com.am.marketdata.api.model.MarketDataUpdate.QuoteChange> quoteUpdates = new HashMap<>();

        for (Map.Entry<String, OHLCQuote> entry : ohlcQuotes.entrySet()) {
            String symbol = entry.getKey();
            OHLCQuote quote = entry.getValue();

            double lastPrice = quote.getLastPrice();
            double open = 0.0;
            double high = 0.0;
            double low = 0.0;
            double close = 0.0;

            // Extract OHLC data if available
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
            }

            com.am.marketdata.api.model.MarketDataUpdate.QuoteChange update = com.am.marketdata.api.model.MarketDataUpdate.QuoteChange
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
