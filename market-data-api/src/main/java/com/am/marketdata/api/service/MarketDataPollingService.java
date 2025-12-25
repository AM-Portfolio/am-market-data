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

                // Step 1: Fetch live OHLC data
                Map<String, OHLCQuote> liveOhlcData = marketDataFetchService.getOHLC(
                        keys, false, TimeFrame.DAY, false);

                // Step 2: Fetch historical data if timeFrame is 1D
                Map<String, OHLCQuote> enrichedData = new HashMap<>();

                if ("1D".equalsIgnoreCase(finalTimeFrame) || "1W".equalsIgnoreCase(finalTimeFrame)
                        || "1M".equalsIgnoreCase(finalTimeFrame)) {
                    // Fetch historical data for previous close and OHLC based on timeFrame
                    enrichedData = fetchAndMergeHistoricalData(keys, liveOhlcData, finalTimeFrame, isIndexSymbol);
                } else {
                    // For other timeframes, use live data as-is
                    enrichedData = liveOhlcData;
                }

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
     * Fetches historical data based on timeFrame and merges with live OHLC data
     * to calculate complete OHLC and previous close
     * 
     * @param symbols   Symbols to fetch data for
     * @param liveData  Current live OHLC data
     * @param timeFrame TimeFrame (1D, 1W, 1M, etc.)
     * @return Enriched OHLC data with historical previous close
     */
    private Map<String, OHLCQuote> fetchAndMergeHistoricalData(
            Set<String> symbols, Map<String, OHLCQuote> liveData, String timeFrame, Boolean isIndexSymbol) {
        try {
            // Calculate historical date range based on timeFrame
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate historicalDate;

            switch (timeFrame.toUpperCase()) {
                case "1D":
                case "DAY":
                    // Fetch yesterday's data
                    historicalDate = today.minusDays(1);
                    break;
                case "1W":
                case "WEEK":
                    // Fetch last week's data (7 days ago)
                    historicalDate = today.minusWeeks(1);
                    break;
                case "1M":
                case "MONTH":
                    // Fetch last month's data (30 days ago)
                    historicalDate = today.minusMonths(1);
                    break;
                default:
                    // Default to yesterday
                    historicalDate = today.minusDays(1);
            }

            String historicalDateStr = historicalDate.toString();

            // Convert LocalDate to Date for API call
            java.util.Date fromDate = java.util.Date.from(
                    historicalDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            java.util.Date toDate = fromDate;

            log.info("Fetching historical data for {} symbols from {} (timeFrame: {})",
                    symbols.size(), historicalDateStr, timeFrame);

            // Fetch historical data for the calculated date
            Map<String, Object> additionalParams = new HashMap<>();
            if (isIndexSymbol != null && isIndexSymbol) {
                additionalParams.put("isIndexSymbol", true);
            }

            Map<String, Object> historicalResponse = marketDataFetchService.getHistoricalDataMultipleSymbols(
                    symbols,
                    fromDate,
                    toDate,
                    TimeFrame.DAY,
                    "STOCK",
                    additionalParams,
                    false);

            // Merge historical and live data
            Map<String, OHLCQuote> enrichedData = new HashMap<>();

            for (String symbol : symbols) {
                OHLCQuote liveQuote = liveData.get(symbol);
                if (liveQuote == null)
                    continue;

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

            log.info("Enriched {} symbols with historical data from {}",
                    enrichedData.size(), historicalDateStr);
            return enrichedData;

        } catch (Exception e) {
            log.error("Error fetching/merging historical data, falling back to live data", e);
            return liveData;
        }
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
