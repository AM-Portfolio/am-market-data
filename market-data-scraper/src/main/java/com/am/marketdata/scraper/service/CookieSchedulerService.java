package com.am.marketdata.scraper.service;

import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.kafka.model.NSEIndicesEvent;
import com.am.marketdata.kafka.producer.NSEIndicesKafkaProducer;
import com.am.marketdata.scraper.client.NSEApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieSchedulerService {
    private final CookieScraperService scraperService;
    private final CookieCacheService cacheService;
    private final NSEApiClient nseApiClient;
    private final NSEIndicesKafkaProducer kafkaProducer;
    private static final String NSE_URL = "https://www.nseindia.com/";

    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
    public void scheduledCookieRefresh() {
        try {
            refreshCookiesAndFetchData();
        } catch (Exception e) {
            log.error("Failed to refresh cookies in scheduled task. Will retry in 15 minutes.", e);
            // Invalidate current cookies as they might be stale
            cacheService.invalidateCookies();
        }
    }

    @Scheduled(fixedRate = 900000) // Run every 15 minutes (900000 ms)
    public void retryFailedCookieRefresh() {
        // Only try to refresh if we don't have valid cookies
        if (cacheService.getCookies() == null) {
            try {
                refreshCookiesAndFetchData();
            } catch (Exception e) {
                log.error("Failed to refresh cookies in retry task", e);
            }
        }
    }

    private void refreshCookiesAndFetchData() {
        log.info("Starting scheduled cookie refresh and data fetch");
        
        // First refresh cookies
        var websiteCookies = scraperService.scrapeCookies(NSE_URL);
        if (websiteCookies != null && websiteCookies.getCookiesString() != null) {
            cacheService.storeCookies(websiteCookies.getCookiesString());
            log.info("Successfully refreshed cookies");
            
            // Then fetch indices data
            try {
                NSEIndicesResponse indicesResponse = nseApiClient.getAllIndices();
                log.info("Successfully fetched NSE indices data");
                
                // Create event with timestamp
                NSEIndicesEvent event = NSEIndicesEvent.builder()
                    .indices(indicesResponse.getData())
                    .timestamp(LocalDateTime.now())
                    .build();
                
                // Send to Kafka
                kafkaProducer.sendIndicesUpdate(event);
                log.info("Successfully processed and sent indices data to Kafka");
                
            } catch (Exception e) {
                log.error("Failed to fetch or process NSE indices data", e);
                throw e;
            }
        } else {
            log.warn("No cookies were obtained from the scraper");
            throw new RuntimeException("Failed to obtain cookies");
        }
    }
}
