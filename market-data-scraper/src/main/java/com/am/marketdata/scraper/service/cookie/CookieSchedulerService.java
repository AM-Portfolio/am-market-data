package com.am.marketdata.scraper.service.cookie;

import com.am.marketdata.scraper.client.api.NSEApi;
import com.am.marketdata.scraper.exception.CookieException;
import com.am.marketdata.scraper.model.WebsiteCookies;
import com.am.marketdata.scraper.service.MarketDataProcessingService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value="nse-scraper-scheduler", havingValue ="true", matchIfMissing = true)
public class CookieSchedulerService {
    private final NSEApi nseApi;
    private final CookieCacheService cacheService;
    private final CookieScraperService scraperService;
    private final MarketDataProcessingService marketDataProcessingService;

    @PostConstruct
    public void initialize() {
        MDC.put("scheduler", "cookie-init");
        MDC.put("execution_time", LocalDateTime.now().toString());
        try {
            log.info("Initializing CookieSchedulerService");
            
            // First fetch and store cookies
            log.info("Fetching initial cookies");
            refreshCookiesInternal();
            
            // Schedule market data processing to start after a delay
            log.info("Scheduling market data processing to start after 5 seconds");
            Thread marketDataThread = new Thread(() -> {
                try {
                    Thread.sleep(5000); // Wait for 5 seconds
                    MDC.put("scheduler", "market-data-init");
                    MDC.put("execution_time", LocalDateTime.now().toString());
                    try {
                        log.info("Starting initial market data processing");
                        marketDataProcessingService.fetchAndProcessMarketData();
                        log.info("Completed initial market data processing");
                    } catch (Exception e) {
                        log.error("Failed initial market data processing: {}", e.getMessage(), e);
                    } finally {
                        MDC.clear();
                    }
                } catch (InterruptedException e) {
                    log.warn("Market data processing initialization thread interrupted");
                }
            });
            marketDataThread.setDaemon(true);
            marketDataThread.start();
            
        } catch (Exception e) {
            log.error("Failed to initialize service: {}", e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }

    @ConditionalOnProperty(value="app.scheduler.cookie.enable", havingValue = "true", matchIfMissing = true)
    @Scheduled(cron = "${app.scheduler.cookie.refresh}")  
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

    @ConditionalOnProperty(value="app.scheduler.market-data.indices.enable", havingValue = "true", matchIfMissing = true)
    @Scheduled(cron = "${app.scheduler.market-data.indices.fetch}")  
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

    // // Run every 2 minutes continuously, 24/7, at 15 seconds past
    // @ConditionalOnProperty(value="app.scheduler.market-data.nse-indices.enable", havingValue = "true", matchIfMissing = true)
    // @Scheduled(cron = "${app.scheduler.market-data.nse-indices.fetch}")
    // public void scheduleNseStockIndicesProcessing() {
    //     MDC.put("scheduler", "nse-stock-indices");
    //     MDC.put("execution_time", LocalDateTime.now().toString());
    //     try {
    //         log.info("Starting nse stock indices processing");
    //         marketDataProcessingService.fetchAndProcessMarketData();
    //         log.info("Completed nse stock indices processing");
    //     } catch (Exception e) {
    //         log.error("Failed to process nse stock indices: {}", e.getMessage(), e);
    //     } finally {
    //         MDC.clear();
    //     }
    // }

    private void refreshCookiesInternal() {
        try {
            String currentCookies = cacheService.getCookies();
            log.info("Current cookies before refresh: {}", maskCookieValues(currentCookies));
            
            WebsiteCookies websiteCookies = scraperService.scrapeCookies();
            String cookies = String.join("; ", websiteCookies.getCookiesString());
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
