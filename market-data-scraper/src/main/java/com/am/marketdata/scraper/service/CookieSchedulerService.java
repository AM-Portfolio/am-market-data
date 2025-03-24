package com.am.marketdata.scraper.service;

import com.am.marketdata.scraper.client.NSEApiClient;
import com.am.marketdata.scraper.exception.CookieException;
import com.am.marketdata.scraper.model.WebsiteCookies;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieSchedulerService {
    private final NSEApiClient nseApiClient;
    private final CookieCacheService cacheService;
    private final CookieScraperService cookieScraperService;
    private final MarketDataProcessingService marketDataProcessingService;
    private final CookieValidator cookieValidator;
    
    // Minutes before expiration to trigger refresh
    private static final int EXPIRY_THRESHOLD_MINUTES = 10;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Initializing CookieSchedulerService");
            refreshCookies();
            
            // If within trading hours, start market data processing
            if (isWithinTradingHours()) {
                log.info("Within trading hours, starting initial market data processing");
                //marketDataProcessingService.fetchAndProcessMarketData();
            } else {
                log.info("Outside trading hours, skipping initial market data processing");
            }
        } catch (Exception e) {
            log.error("Failed to initialize service: {}", e.getMessage(), e);
        }
    }

    // Run every hour for cookie refresh
    //@Scheduled(cron = "0 0 * * * *")
    public void scheduledCookieRefresh() {
        try {
            log.info("Starting scheduled cookie refresh");
            String currentCookies = cacheService.getCookies();
            log.debug("Current cookies before refresh: {}", maskCookieValues(currentCookies));
            
            WebsiteCookies websiteCookies = cookieScraperService.scrapeCookies();
            String newCookies = String.join("; ", websiteCookies.getCookiesString());
            log.debug("New cookies after refresh: {}", maskCookieValues(newCookies));
            cacheService.storeCookies(newCookies);
        } catch (CookieException e) {
            log.error("Failed to refresh cookies in scheduled task. Current cookies: {}, Error: {}", 
                maskCookieValues(cacheService.getCookies()), e.getMessage(), e);
            cacheService.invalidateCookies();
        }
    }

    // Run every 2 minutes
    @Scheduled(cron = "0 */2 * * * *", zone = "Asia/Kolkata")
    public void scheduleMarketDataProcessing() {
        try {
            // Only process between 9:15 AM and 3:35 PM
            if (isWithinTradingHours()) {
                String cookies = cacheService.getCookies();
                if (cookies == null) {
                    log.warn("No valid cookies found in cache, attempting to refresh cookies");
                    refreshCookies();
                } else if (cookieValidator.areAnyRequiredCookiesExpiringSoon(cookies, EXPIRY_THRESHOLD_MINUTES)) {
                    log.info("Cookies are about to expire, refreshing them");
                    refreshCookies();
                } else if (!cookieValidator.areRequiredCookiesValid(cookies)) {
                    log.warn("Invalid cookies found in cache, refreshing cookies");
                    refreshCookies();
                } else {
                    log.debug("Cookies are valid and not expiring soon");
                }
                
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
            
            // Validate current cookies before refreshing
            String currentCookies = cacheService.getCookies();
            if (currentCookies != null) {
                if (cookieValidator.areRequiredCookiesValid(currentCookies) && 
                    !cookieValidator.areAnyRequiredCookiesExpiringSoon(currentCookies, EXPIRY_THRESHOLD_MINUTES)) {
                    log.info("Current cookies are still valid and not expiring soon, no refresh needed");
                    return;
                }
                
                // Log detailed validation results for debugging
                Map<String, CookieValidator.ValidationResult> validationResults = 
                    cookieValidator.validateAllCookies(currentCookies);
                logValidationResults(validationResults);
            }

            // Attempt to refresh cookies with retry mechanism
            WebsiteCookies websiteCookies = fetchCookiesWithRetry(3);
            
            String cookies = websiteCookies.getCookiesString();
            if (!cookieValidator.areRequiredCookiesValid(cookies)) {
                Map<String, CookieValidator.ValidationResult> validationResults = 
                    cookieValidator.validateAllCookies(cookies);
                logValidationResults(validationResults);
                throw new CookieException("Invalid cookies received from NSE");
            }
            
            log.debug("Successfully fetched and validated new cookies: {}", maskCookieValues(cookies));
            cacheService.storeCookies(cookies);
        } catch (Exception e) {
            String currentCookies = cacheService.getCookies();
            log.error("Failed to refresh cookies. Current cookies in cache: {}, Error: {}", 
                maskCookieValues(currentCookies), e.getMessage(), e);
            throw new CookieException("Failed to refresh cookies: " + e.getMessage(), e);
        }
    }
    
    /**
     * Logs detailed validation results for debugging
     */
    private void logValidationResults(Map<String, CookieValidator.ValidationResult> results) {
        log.debug("Cookie validation results:");
        for (Map.Entry<String, CookieValidator.ValidationResult> entry : results.entrySet()) {
            CookieValidator.ValidationResult result = entry.getValue();
            if (!result.isValid()) {
                log.warn("Cookie '{}' validation failed: {}", entry.getKey(), result);
            } else {
                log.debug("Cookie '{}': {}", entry.getKey(), result);
            }
        }
    }

    private WebsiteCookies fetchCookiesWithRetry(int maxAttempts) throws Exception {
        int attempt = 0;
        while (attempt < maxAttempts) {
            attempt++;
            try {
                WebsiteCookies cookies = cookieScraperService.scrapeCookies();
                if (cookies != null && !cookies.getCookiesString().isEmpty()) {
                    return cookies;
                }
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                log.warn("Cookie fetch attempt {} failed, retrying in {} seconds", 
                    attempt, attempt * 2);
                Thread.sleep(attempt * 2000);
            }
        }
        throw new CookieException("Failed to fetch valid cookies after " + maxAttempts + " attempts");
    }

    private boolean isWithinTradingHours() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        LocalTime marketStart = LocalTime.of(9, 15);
        LocalTime marketEnd = LocalTime.of(15, 35);
        
        //return !now.isBefore(marketStart) && !now.isAfter(marketEnd);
        return true;
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
