package com.am.marketdata.scraper.service;

import com.am.marketdata.scraper.client.NSEApiClient;
import com.am.marketdata.scraper.exception.CookieException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookieSchedulerService {
    private final NSEApiClient nseApiClient;
    private final CookieCacheService cacheService;
    private final MarketDataProcessingService marketDataProcessingService;

    @PostConstruct
    public void initialize() {
        MDC.put("scheduler", "cookie-init");
        MDC.put("execution_time", LocalDateTime.now().toString());
        try {
            log.info("Initializing CookieSchedulerService");
            refreshCookiesInternal();
            
            // Start initial market data processing
            log.info("Starting initial market data processing");
            marketDataProcessingService.fetchAndProcessMarketData();
        } catch (Exception e) {
            log.error("Failed to initialize service: {}", e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }

    // Run every hour for cookie refresh at 10 seconds past the hour
    @Scheduled(cron = "10 0 * * * *")
    public void scheduledCookieRefresh() {
        MDC.put("scheduler", "cookie-refresh");
        MDC.put("execution_time", LocalDateTime.now().toString());
        try {
            log.info("Starting scheduled cookie refresh");
            refreshCookiesInternal();
            log.info("Completed scheduled cookie refresh");
        } catch (Exception e) {
            log.error("Failed to refresh cookies: {}", e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }

    // Run every 2 minutes continuously, 24/7, at 15 seconds past
    @Scheduled(cron = "15 */2 * * * *")
    public void scheduleMarketDataProcessing() {
        MDC.put("scheduler", "market-data");
        MDC.put("execution_time", LocalDateTime.now().toString());
        try {
            log.info("Starting market data processing");
            marketDataProcessingService.fetchAndProcessMarketData();
            log.info("Completed market data processing");
        } catch (Exception e) {
            log.error("Failed to process market data: {}", e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }

    private void refreshCookiesInternal() {
        try {
            String currentCookies = cacheService.getCookies();
            log.info("Current cookies before refresh: {}", maskCookieValues(currentCookies));
            
            HttpHeaders headers = nseApiClient.fetchCookies();
            if (headers == null || !headers.containsKey(HttpHeaders.SET_COOKIE)) {
                String msg = "No cookies received from NSE API";
                log.error(msg);
                throw new CookieException(msg);
            }

            String cookies = String.join("; ", headers.get(HttpHeaders.SET_COOKIE));
            log.info("Successfully fetched new cookies: {}", maskCookieValues(cookies));
            cacheService.storeCookies(cookies);
            
            String newCookies = cacheService.getCookies();
            log.info("New cookies after refresh: {}", maskCookieValues(newCookies));
        } catch (Exception e) {
            String currentCookies = cacheService.getCookies();
            log.error("Failed to refresh cookies. Current cookies in cache: {}, Error: {}", 
                maskCookieValues(currentCookies), e.getMessage(), e);
            throw new CookieException("Failed to refresh cookies: " + e.getMessage(), e);
        }
    }

    private String maskCookieValues(String cookies) {
        if (cookies == null) return "null";
        return cookies.replaceAll("=[^;]*", "=*****");
    }
}
