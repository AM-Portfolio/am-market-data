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

    public void connectStream(List<String> instrumentKeys, String modeStr, String provider) {
        log.info("Initiating stream simulation via polling for {} instruments. Provider: {}", instrumentKeys.size(),
                provider);

        String providerKey = provider != null ? provider.toUpperCase() : "UNKNOWN";

        // Cancel existing stream if any for this provider
        disconnectStream(providerKey);

        Runnable pollingTask = () -> {
            try {
                // Using getOHLC to fetch data
                Set<String> keys = new HashSet<>(instrumentKeys);

                // Fetch latest daily data or appropriate timeframe
                // ForceRefresh = true to get latest from provider
                // Using TimeFrame.DAY as default snapshot
                Map<String, Object> response = marketDataFetchService.getOHLC(keys, false, TimeFrame.DAY, true);

                if (response != null && response.containsKey("quotes")) {
                    @SuppressWarnings("unchecked")
                    Map<String, OHLCQuote> quotes = (Map<String, OHLCQuote>) response.get("quotes");

                    if (quotes != null && !quotes.isEmpty()) {

                        com.am.marketdata.api.model.MarketDataUpdate update = com.am.marketdata.api.model.MarketDataUpdate
                                .builder()
                                .provider(providerKey)
                                .timestamp(System.currentTimeMillis())
                                .quotes(quotes)
                                .build();

                        webSocketHandler.broadcast(update);
                    }
                }
            } catch (Exception e) {
                log.error("Error during polling stream execution for provider {}", providerKey, e);
            }
        };

        // Schedule task - every 1 second
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(pollingTask, 0, 1, TimeUnit.SECONDS);
        activeStreams.put(providerKey, future);
        log.info("Polling stream started for provider: {}", providerKey);
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
