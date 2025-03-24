package com.am.marketdata.scraper.service;

import com.am.marketdata.scraper.cookie.CookieManager;
import com.am.marketdata.scraper.exception.CookieException;
import com.am.marketdata.scraper.exception.MarketDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Scheduler service specifically for stock indices data.
 * Runs at 9:30 AM and 4:00 PM with retry mechanism.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockIndicesSchedulerService {
    private static final String CONFIG_RETRY_INTERVAL_MINUTES = "${stock.indices.retry.interval.minutes:15}";
    private static final String CONFIG_MAX_RETRIES = "${stock.indices.max.retries:20}"; // ~5 hours of retries
    
    private final CookieManager cookieManager;
    private final MarketDataProcessingService marketDataProcessingService;
    
    @Value(CONFIG_RETRY_INTERVAL_MINUTES)
    private int retryIntervalMinutes;
    
    @Value(CONFIG_MAX_RETRIES)
    private int maxRetries;
    
    private final AtomicBoolean morningFetchCompleted = new AtomicBoolean(false);
    private final AtomicBoolean eveningFetchCompleted = new AtomicBoolean(false);
    private int currentRetryCount = 0;
    private LocalDate lastProcessedDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing StockIndicesSchedulerService with retry interval: {} minutes, max retries: {}", 
                retryIntervalMinutes, maxRetries);
        
        // Reset flags at startup based on current time
        resetFlagsIfNeeded();
    }
    
    /**
     * Morning schedule at 9:30 AM IST
     */
    @Scheduled(cron = "0 30 9 * * *", zone = "Asia/Kolkata")
    public void scheduleMorningStockIndicesFetch() {
        resetFlagsIfNeeded();
        
        if (!morningFetchCompleted.get()) {
            log.info("Starting scheduled morning stock indices fetch at 9:30 AM");
            fetchStockIndicesWithRetry(true);
        } else {
            log.debug("Morning stock indices already fetched successfully today");
        }
    }
    
    /**
     * Evening schedule at 4:00 PM IST
     */
    @Scheduled(cron = "0 0 16 * * *", zone = "Asia/Kolkata")
    public void scheduleEveningStockIndicesFetch() {
        if (!eveningFetchCompleted.get()) {
            log.info("Starting scheduled evening stock indices fetch at 4:00 PM");
            fetchStockIndicesWithRetry(false);
        } else {
            log.debug("Evening stock indices already fetched successfully today");
        }
    }
    
    /**
     * Retry scheduler that runs every X minutes if needed
     */
    @Scheduled(cron = "${stock.indices.retry.cron:0 */15 * * * *}", zone = "Asia/Kolkata")
    public void retryFailedFetch() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        
        // Only retry during trading hours
        if (isWithinTradingHours()) {
            // Morning session retry (9:30 AM - 3:30 PM)
            if (now.isAfter(LocalTime.of(9, 30)) && 
                now.isBefore(LocalTime.of(15, 30)) && 
                !morningFetchCompleted.get() && 
                currentRetryCount < maxRetries) {
                
                log.info("Retrying morning stock indices fetch (attempt {}/{})", 
                        currentRetryCount + 1, maxRetries);
                fetchStockIndicesWithRetry(true);
            }
            // Evening session retry (4:00 PM - 5:30 PM)
            else if (now.isAfter(LocalTime.of(16, 0)) && 
                     now.isBefore(LocalTime.of(17, 30)) && 
                     !eveningFetchCompleted.get() && 
                     currentRetryCount < maxRetries) {
                
                log.info("Retrying evening stock indices fetch (attempt {}/{})", 
                        currentRetryCount + 1, maxRetries);
                fetchStockIndicesWithRetry(false);
            }
        }
    }
    
    /**
     * Fetch stock indices with retry logic
     * @param isMorningSession true if this is the morning session, false for evening
     */
    private void fetchStockIndicesWithRetry(boolean isMorningSession) {
        try {
            // Refresh cookies if needed
            cookieManager.refreshIfNeeded();
            
            // Attempt to fetch and process stock indices
            boolean success = marketDataProcessingService.fetchAndProcessStockIndicesOnly();
            
            if (success) {
                log.info("Successfully fetched and processed stock indices data");
                if (isMorningSession) {
                    morningFetchCompleted.set(true);
                } else {
                    eveningFetchCompleted.set(true);
                }
                currentRetryCount = 0; // Reset retry count on success
            } else {
                handleFailure(isMorningSession);
            }
        } catch (CookieException e) {
            log.error("Cookie refresh failed during stock indices fetch: {}", e.getMessage());
            handleFailure(isMorningSession);
        } catch (MarketDataException e) {
            log.error("Failed to fetch stock indices: {}", e.getMessage());
            handleFailure(isMorningSession);
        } catch (Exception e) {
            log.error("Unexpected error during stock indices fetch: {}", e.getMessage(), e);
            handleFailure(isMorningSession);
        }
    }
    
    private void handleFailure(boolean isMorningSession) {
        currentRetryCount++;
        String session = isMorningSession ? "morning" : "evening";
        
        if (currentRetryCount >= maxRetries) {
            log.error("Maximum retry attempts ({}) reached for {} stock indices fetch. Giving up until next scheduled run.", 
                    maxRetries, session);
            // Mark as completed to stop retrying
            if (isMorningSession) {
                morningFetchCompleted.set(true);
            } else {
                eveningFetchCompleted.set(true);
            }
            currentRetryCount = 0;
        } else {
            log.warn("Will retry {} stock indices fetch in {} minutes (attempt {}/{})", 
                    session, retryIntervalMinutes, currentRetryCount, maxRetries);
        }
    }
    
    /**
     * Reset flags if it's a new day
     */
    private void resetFlagsIfNeeded() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        
        if (!today.equals(lastProcessedDate)) {
            log.info("New day detected, resetting stock indices fetch flags");
            morningFetchCompleted.set(false);
            eveningFetchCompleted.set(false);
            currentRetryCount = 0;
            lastProcessedDate = today;
        }
        
        // Also reset flags based on time
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        if (now.isBefore(LocalTime.of(9, 30))) {
            // Before 9:30 AM, reset morning flag
            morningFetchCompleted.set(false);
        }
        if (now.isBefore(LocalTime.of(16, 0))) {
            // Before 4:00 PM, reset evening flag
            eveningFetchCompleted.set(false);
        }
    }
    
    private boolean isWithinTradingHours() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        LocalTime marketStart = LocalTime.of(9, 15);
        LocalTime marketEnd = LocalTime.of(17, 30); // Extended to allow for evening data fetch
        
        return !now.isBefore(marketStart) && !now.isAfter(marketEnd);
    }
}
