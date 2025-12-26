package com.am.marketdata.scheduler.service;

import com.am.marketdata.internal.service.MarketDataIngestionService;
import com.am.marketdata.internal.service.MarketDataHistoricalSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.time.LocalTime;

/**
 * Scheduler to control Market Data Ingestion.
 * Starts ingestion at market open and stops at market close.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataIngestionScheduler {

    private final MarketDataIngestionService ingestionService;
    private final MarketDataHistoricalSyncService historicalSyncService;

    @Value("${scheduler.ingestion.enabled:true}")
    private boolean enabled;

    @Value("${scheduler.ingestion.symbols:NIFTY 50,NIFTY BANK}")
    private List<String> symbols;

    @Value("${scheduler.ingestion.provider:ZERODHA}")
    private String provider;

    @Value("${scheduler.ingestion.force:true}")
    private boolean forceRefresh;

    // Market Hours Config
    @Value("${scheduler.market.start:09:15}")
    private String marketStartTime;

    @Value("${scheduler.market.end:15:30}")
    private String marketEndTime;

    @PostConstruct
    public void init() {
        if (enabled && isMarketOpen()) {
            log.info("Application started during market hours. Triggering ingestion.");
            startIngestion();
        }
    }

    /**
     * Start Ingestion at Market Open (e.g., 9:15 AM)
     */
    @Scheduled(cron = "${scheduler.ingestion.start-cron:0 15 9 * * MON-FRI}")
    public void scheduledStart() {
        if (!enabled)
            return;
        log.info("Scheduled trigger: Starting Market Data Ingestion");
        startIngestion();
    }

    /**
     * Stop Ingestion at Market Close (e.g., 3:30 PM)
     */
    @Scheduled(cron = "${scheduler.ingestion.stop-cron:0 30 15 * * MON-FRI}")
    public void scheduledStop() {
        if (!enabled)
            return;
        log.info("Scheduled trigger: Stopping Market Data Ingestion");
        ingestionService.stopIngestion(provider);
    }

    /**
     * Run Historical Sync (Smart Delta) at 07:15 AM
     */
    @Scheduled(cron = "${scheduler.historical.sync-cron:0 15 7 * * *}")
    public void scheduledHistoricalSync() {
        if (!enabled)
            return;
        log.info("Scheduled trigger: Starting Historical Data Sync (Smart Delta)");
        historicalSyncService.syncHistoricalData();
    }

    private void startIngestion() {
        // Trigger for NIFTY 50 and BANKNIFTY by default
        // In real scenario, this list might come from DB or config
        ingestionService.startIngestion(symbols, provider, "1D", true, forceRefresh);
    }

    private boolean isMarketOpen() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.parse(marketStartTime);
        LocalTime end = LocalTime.parse(marketEndTime);
        return now.isAfter(start) && now.isBefore(end);
    }
}
