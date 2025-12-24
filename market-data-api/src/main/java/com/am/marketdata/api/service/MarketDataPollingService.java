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
    private final com.am.marketdata.api.util.InstrumentUtils instrumentUtils;

    // Scheduler for polling
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<String, ScheduledFuture<?>> activeStreams = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Value("${market-data.stream.poll-interval-seconds:10}")
    private int pollIntervalSeconds;

    public void connectStream(List<String> instrumentKeys, String modeStr, String provider) {
        // Resolve symbols using InstrumentUtils
        Set<String> resolvedKeys = instrumentUtils.resolveSymbols(instrumentKeys);

        log.info("Initiating stream simulation via polling for {} instruments (resolved from {}). Provider: {}",
                resolvedKeys.size(), instrumentKeys.size(), provider);

        String providerKey = provider != null ? provider.toUpperCase() : "UNKNOWN";

        // Cancel existing stream if any for this provider
        disconnectStream(providerKey);

        Runnable pollingTask = () -> {
            try {
                // Using getOHLC to fetch data
                Set<String> keys = new HashSet<>(resolvedKeys);

                // Fetch latest daily data or appropriate timeframe
                // ForceRefresh = true to get latest from provider
                // Using TimeFrame.DAY as default snapshot
                Map<String, OHLCQuote> response = marketDataFetchService.getOHLC(keys, false, TimeFrame.DAY, false);

                if (response != null && !response.isEmpty()) {
                    // response IS the quotes map
                    Map<String, OHLCQuote> ohlcQuotes = response;

                    if (!ohlcQuotes.isEmpty()) {
                        Map<String, com.am.marketdata.api.model.MarketDataUpdate.QuoteChange> quoteUpdates = new HashMap<>();

                        for (Map.Entry<String, OHLCQuote> entry : ohlcQuotes.entrySet()) {
                            String symbol = entry.getKey();
                            OHLCQuote quote = entry.getValue();

                            double lastPrice = quote.getLastPrice();
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
                                    .change(change)
                                    .changePercent(changePercent)
                                    .build();

                            quoteUpdates.put(symbol, update);
                        }

                        com.am.marketdata.api.model.MarketDataUpdate update = com.am.marketdata.api.model.MarketDataUpdate
                                .builder()
                                .provider(providerKey)
                                .timestamp(System.currentTimeMillis())
                                .quotes(quoteUpdates)
                                .build();

                        webSocketHandler.broadcast(update);
                    }
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
}
