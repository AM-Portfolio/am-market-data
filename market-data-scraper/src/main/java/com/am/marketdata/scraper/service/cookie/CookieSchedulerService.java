package com.am.marketdata.scraper.service.cookie;

import com.am.marketdata.scraper.exception.CookieException;
import com.am.marketdata.scraper.model.WebsiteCookies;
import com.am.marketdata.scraper.service.MarketDataProcessingService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value="nse-scraper-scheduler", havingValue ="true", matchIfMissing = true)
public class CookieSchedulerService {
    private final CookieCacheService cacheService;
    private final CookieScraperService scraperService;
    private final MarketDataProcessingService marketDataProcessingService;

    @Value("${NSE_API_MANUAL_COOKIES:}")
    private String manualCookies;
    
    @Value("${NSE_API_USE_MANUAL_COOKIES:false}")
    private boolean useManualCookies;

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
            
            // Try to use manual cookies first if available
            if (tryUseManualCookies()) {
                log.info("Using manually provided cookies instead of scraping");
                return;
            }
            
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
    
    /**
     * Attempts to use manually provided cookies from application properties.
     * This is a fallback mechanism when cookie scraping fails.
     * 
     * @return true if manual cookies were used, false otherwise
     */
    private boolean tryUseManualCookies() {
        try {
            // Check if manual cookies should be used
            if (!useManualCookies) {
                log.debug("Manual cookies are disabled by configuration");
                return false;
            }
            
            // Use cookies from configuration
            if (manualCookies != null && !manualCookies.isEmpty()) {
                log.info("Using manually provided cookies from configuration: {}", maskCookieValues(manualCookies));
                cacheService.storeCookies(manualCookies);
                return true;
            }
            
            log.warn("Manual cookies are enabled but no cookies were provided in configuration");
            return false;
        } catch (Exception e) {
            log.warn("Failed to use manual cookies: {}", e.getMessage());
            return false;
        }
    }

    private String maskCookieValues(String cookies) {
        if (cookies == null) return "null";
        return cookies.replaceAll("=[^;]*", "=*****");
    }
}
