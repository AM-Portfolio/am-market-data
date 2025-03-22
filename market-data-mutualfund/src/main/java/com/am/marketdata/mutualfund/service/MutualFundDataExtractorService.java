package com.am.marketdata.mutualfund.service;

import com.am.marketdata.common.model.MutualFundData;
import com.am.marketdata.common.model.MutualFundScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MutualFundDataExtractorService {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final MutualFundApiClient mutualFundApiClient;
    private final MutualFundCacheService mutualFundCacheService;
    private final MutualFundDataProcessor mutualFundDataProcessor;

    @Scheduled(cron = "0 0/15 9-15 * * MON-FRI", zone = "Asia/Kolkata")
    @Transactional
    public void extractAndProcessMutualFundData() {
        log.info("Starting mutual fund data extraction process");
        
        try {
            // Fetch mutual fund schemes
            List<MutualFundScheme> schemes = mutualFundCacheService.getMutualFundSchemes();
            
            // Process each scheme in parallel
            List<CompletableFuture<Void>> futures = schemes.stream()
                .map(scheme -> CompletableFuture.supplyAsync(() -> 
                    retryOnFailure(() -> processMutualFundScheme(scheme), MAX_RETRIES)
                ))
                .toList();
            
            // Wait for all processing to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            log.info("Completed mutual fund data extraction process");
        } catch (Exception e) {
            log.error("Error in mutual fund data extraction process", e);
        }
    }

    private MutualFundData processMutualFundScheme(MutualFundScheme scheme) {
        try {
            // Fetch data from API
            MutualFundData data = mutualFundApiClient.fetchMutualFundData(scheme);
            
            // Process and validate data
            if (mutualFundDataProcessor.validateData(data)) {
                mutualFundDataProcessor.processData(data);
                return data;
            }
            
            log.warn("Invalid data for scheme: {}", scheme.getCode());
            return null;
        } catch (Exception e) {
            log.error("Error processing scheme: {}", scheme.getCode(), e);
            throw e;
        }
    }

    private <T> T retryOnFailure(Supplier<T> operation, int maxRetries) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                if (attempt < maxRetries) {
                    long delay = RETRY_DELAY_MS * Math.pow(2, attempt - 1);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("Operation failed after " + maxRetries + " retries", lastException);
    }
}
