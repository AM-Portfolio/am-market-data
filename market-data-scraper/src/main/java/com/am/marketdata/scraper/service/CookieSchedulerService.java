package com.am.marketdata.scraper.service;

import com.am.marketdata.scraper.client.NSEApiClient;
import com.am.marketdata.scraper.exception.CookieException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieSchedulerService {
    private final NSEApiClient nseApiClient;
    private final CookieCacheService cacheService;
    private final MarketDataProcessingService marketDataProcessingService;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Initializing CookieSchedulerService");
            refreshCookies();
            
            // If within trading hours, start market data processing
            if (isWithinTradingHours()) {
                log.info("Within trading hours, starting initial market data processing");
                marketDataProcessingService.fetchAndProcessMarketData();
            } else {
                log.info("Outside trading hours, skipping initial market data processing");
            }
        } catch (Exception e) {
            log.error("Failed to initialize service: {}", e.getMessage(), e);
        }
    }

    // Run every hour for cookie refresh
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledCookieRefresh() {
        try {
            log.info("Starting scheduled cookie refresh");
            String currentCookies = cacheService.getCookies();
            log.info("Current cookies before refresh: {}", maskCookieValues(currentCookies));
            
            refreshCookies();
            
            String newCookies = cacheService.getCookies();
            log.info("New cookies after refresh: {}", maskCookieValues(newCookies));
        } catch (CookieException e) {
            log.error("Failed to refresh cookies in scheduled task. Current cookies: {}, Error: {}", 
                maskCookieValues(cacheService.getCookies()), e.getMessage(), e);
            cacheService.invalidateCookies();
        }
    }

    // Run every 2 minutes between 9:15 AM and 3:35 PM IST on weekdays
    @Scheduled(cron = "0 */2 9-15 * * MON-FRI", zone = "Asia/Kolkata")
    public void scheduleMarketDataProcessing() {
        try {
            // Only process between 9:15 AM and 3:35 PM
            if (isWithinTradingHours()) {
                log.info("Starting scheduled market data processing");
                marketDataProcessingService.fetchAndProcessMarketData();
            } else {
                log.debug("Outside trading hours, skipping market data processing");
            }
        } catch (Exception e) {
            log.error("Failed to process market data: {}", e.getMessage(), e);
        }
    }

    public void refreshCookies() {
        try {
            log.info("Attempting to refresh cookies");
            HttpHeaders headers = nseApiClient.fetchCookies();
            if (headers == null || !headers.containsKey(HttpHeaders.SET_COOKIE)) {
                String msg = "No cookies received from NSE API";
                log.error(msg);
                throw new CookieException(msg);
            }

            String cookies = String.join("; ", headers.get(HttpHeaders.SET_COOKIE));
            log.info("Successfully fetched new cookies: {}", maskCookieValues(cookies));
            cacheService.storeCookies(cookies);
        } catch (Exception e) {
            String currentCookies = cacheService.getCookies();
            log.error("Failed to refresh cookies. Current cookies in cache: {}, Error: {}", 
                maskCookieValues(currentCookies), e.getMessage(), e);
            throw new CookieException("Failed to refresh cookies: " + e.getMessage(), e);
        }
    }

    private boolean isWithinTradingHours() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        LocalTime marketStart = LocalTime.of(9, 15);
        LocalTime marketEnd = LocalTime.of(15, 35);
        
        return !now.isBefore(marketStart) && !now.isAfter(marketEnd);
    }

    private String maskCookieValues(String cookies) {
        if (cookies == null) return "null";
        // Split cookies and mask values while preserving names
        return Stream.of(cookies.split(";"))
            .map(cookie -> {
                String[] parts = cookie.split("=", 2);
                return parts.length > 1 
                    ? parts[0].trim() + "=*****" 
                    : cookie.trim() + "=*****";
            })
            .collect(Collectors.joining("; "));
    }
}
