package com.am.marketdata.internal.service;

import com.am.marketdata.api.model.HistoricalDataResponseV1;
import com.am.marketdata.api.service.MarketDataFetchService;
import com.am.marketdata.api.util.InstrumentUtils;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.internal.model.IngestionJobLog;
import com.am.marketdata.internal.model.MarketDataIngestionStatus;
import com.am.marketdata.internal.repository.IngestionJobLogRepository;
import com.am.marketdata.internal.repository.MarketDataIngestionStatusRepository;
import com.am.marketdata.common.log.AppLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketDataHistoricalSyncService {

    private final AppLogger log = AppLogger.getLogger(MarketDataHistoricalSyncService.class);

    private final MarketDataFetchService marketDataFetchService;
    private final InstrumentUtils instrumentUtils;
    private final MarketDataIngestionStatusRepository ingestionStatusRepository;
    private final IngestionJobLogRepository ingestionJobLogRepository;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private final ExecutorService batchExecutor = Executors.newFixedThreadPool(10);
    private static final int BATCH_SIZE = 20;

    /**
     * Triggered by Scheduler at 07:15 AM
     */
    public void syncHistoricalData(String symbol, boolean forceRefresh) {
        String jobId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        log.info("syncHistoricalData", "Starting Historical Data Sync Job: {} (Force Refresh: {})", jobId,
                forceRefresh);

        IngestionJobLog jobLog = IngestionJobLog.builder()
                .jobId(jobId)
                .startTime(startTime)
                .status("RUNNING")
                .failedSymbols(new ArrayList<>())
                .logs(new ArrayList<>())
                .build();

        addLog(jobLog, "Starting Historical Data Sync Job: " + jobId + ", Force Refresh: " + forceRefresh);
        jobLog = ingestionJobLogRepository.save(jobLog);

        try {
            // 1. Get Symbols
            Set<String> allSymbols;
            if (symbol != null && !symbol.trim().isEmpty()) {
                allSymbols = java.util.Arrays.stream(symbol.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(java.util.stream.Collectors.toSet());
                addLog(jobLog, "Targeting symbols: " + allSymbols);
            } else {
                allSymbols = getAllSymbolsToSync();
            }
            jobLog.setTotalSymbols(allSymbols.size());
            addLog(jobLog, "Found " + allSymbols.size() + " symbols to sync");
            log.info("syncHistoricalData", "Found {} symbols to sync", allSymbols.size());

            // 2. Resolve Status & Group into Buckets
            Map<LocalDate, List<String>> buckets = groupSymbolsByStartDate(allSymbols, forceRefresh);

            if (!forceRefresh) {
                // Comments preserved
            }

            int successCount = 0;
            int failureCount = 0;
            long totalPayloadSize = 0;
            List<String> failedSymbols = new ArrayList<>();

            // 3. Process Buckets
            for (Map.Entry<LocalDate, List<String>> entry : buckets.entrySet()) {
                LocalDate fromDate = entry.getKey();
                List<String> symbolsInBucket = entry.getValue();

                log.info("syncHistoricalData", "Processing bucket for date {}: {} symbols", fromDate,
                        symbolsInBucket.size());
                addLog(jobLog, "Processing bucket for date " + fromDate + ": " + symbolsInBucket.size() + " symbols");

                ProcessingResult result = processBucket(symbolsInBucket, fromDate, jobLog, forceRefresh);
                successCount += result.successCount;
                failureCount += result.failureCount;
                totalPayloadSize += result.totalPayloadSize;
                failedSymbols.addAll(result.failedSymbols);
            }

            // 4. Update Job Log
            jobLog.setEndTime(LocalDateTime.now());
            jobLog.setDurationMs(java.time.Duration.between(startTime, jobLog.getEndTime()).toMillis());
            jobLog.setSuccessCount(successCount);
            jobLog.setFailureCount(failureCount);
            jobLog.setFailedSymbols(failedSymbols);
            jobLog.setPayloadSize(totalPayloadSize);
            jobLog.setStatus(failureCount == 0 ? "SUCCESS" : (successCount > 0 ? "PARTIAL_SUCCESS" : "FAILED"));

            addLog(jobLog, "Job Completed. Saved: " + successCount + ", Failed: " + failureCount + ", Payload: "
                    + (totalPayloadSize / 1024) + " KB");

        } catch (Exception e) {
            log.error("syncHistoricalData", "Fatal error in Historical Data Sync Job", e);
            jobLog.setEndTime(LocalDateTime.now());
            jobLog.setStatus("FAILED");
            jobLog.setMessage(e.getMessage());
            addLog(jobLog, "Fatal Error: " + e.getMessage());
        } finally {
            ingestionJobLogRepository.save(jobLog);
            log.info("syncHistoricalData", "Historical Data Sync Job Completed. Status: {}", jobLog.getStatus());
        }
    }

    private Set<String> getAllSymbolsToSync() {
        // Fetch NSE 500 Constitutents
        // Assuming "NIFTY 500" is the index we want. Or user mentioned "NSE 500 list".
        // Let's use NIFTY 500 if available, else NIFTY 50.
        Set<String> indices = new HashSet<>(Arrays.asList("NIFTY 50", "NIFTY BANK", "NIFTY 500"));

        // This resolves index to constituents
        Set<String> resolvedSymbols = instrumentUtils.resolveSymbols(new ArrayList<>(indices), true);

        // Add indices themselves
        resolvedSymbols.addAll(indices);

        return resolvedSymbols;
    }

    private Map<LocalDate, List<String>> groupSymbolsByStartDate(Set<String> symbols, boolean forceRefresh) {
        Map<LocalDate, List<String>> buckets = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate defaultStart = today.minusYears(10);

        List<MarketDataIngestionStatus> statuses = ingestionStatusRepository.findAllById(symbols);
        Map<String, LocalDate> statusMap = statuses.stream()
                .collect(Collectors.toMap(MarketDataIngestionStatus::getSymbol,
                        MarketDataIngestionStatus::getLastIngestionDate));

        for (String symbol : symbols) {
            LocalDate startDate;

            if (forceRefresh) {
                // Force refresh: Always fetch from historical start, ignore last sync date
                startDate = defaultStart;
            } else {
                // Normal mode: Use incremental sync from last sync date
                LocalDate lastDate = statusMap.get(symbol);
                LocalDate nextDate = (lastDate != null) ? lastDate.plusDays(1) : defaultStart;

                // If already up to date, skip
                if (!nextDate.isBefore(today)) {
                    continue;
                }
                startDate = nextDate;
            }

            buckets.computeIfAbsent(startDate, k -> new ArrayList<>()).add(symbol);
        }
        return buckets;
    }

    private ProcessingResult processBucket(List<String> symbols, LocalDate fromDate, IngestionJobLog jobLog,
            boolean forceRefresh) {
        ProcessingResult totalResult = new ProcessingResult();
        LocalDate toDate = LocalDate.now(); // Up to current

        // Batching within bucket
        List<List<String>> batches = chunkList(symbols, BATCH_SIZE);
        List<CompletableFuture<ProcessingResult>> futures = new ArrayList<>();

        for (List<String> batch : batches) {
            CompletableFuture<ProcessingResult> future = CompletableFuture.supplyAsync(() -> {
                return fetchBatch(new HashSet<>(batch), fromDate, toDate, jobLog, forceRefresh);
            }, batchExecutor);
            futures.add(future);
        }

        // Wait and Combine
        for (CompletableFuture<ProcessingResult> f : futures) {
            try {
                ProcessingResult batchResult = f.join();
                totalResult.add(batchResult);
            } catch (Exception e) {
                log.error("processBucket", "Error processing batch", e);
            }
        }

        return totalResult;
    }

    private ProcessingResult fetchBatch(Set<String> batch, LocalDate fromDate, LocalDate toDate,
            IngestionJobLog jobLog, boolean forceRefresh) {
        ProcessingResult result = new ProcessingResult();
        try {
            // Log locally for debug, but maybe not all batch start to avoid spam in main
            // log,
            // but user asked for "what is happening inside".
            // Let's add significant events.
            addLogAsync(jobLog, "Fetching batch of " + batch.size() + " symbols from " + fromDate + " to " + toDate
                    + " (Force Refresh: " + forceRefresh + ")");

            Date from = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date to = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Use getHistoricalDataMultipleSymbols
            HistoricalDataResponseV1 response = marketDataFetchService.getHistoricalDataMultipleSymbols(
                    batch,
                    from,
                    to,
                    TimeFrame.DAY,
                    "STOCK", // Instrument Type
                    new HashMap<>(), // Additional Params
                    forceRefresh // Force Refresh (we need to fetch from provider)
            );

            // Access data from response
            Map<String, com.am.common.investment.model.historical.HistoricalData> dataMap = response.getData();

            if (dataMap == null) {
                result.failureCount += batch.size();
                result.failedSymbols.addAll(batch);
                return result;
            }

            for (String symbol : batch) {
                if (dataMap.containsKey(symbol) &&
                        dataMap.get(symbol).getDataPoints() != null &&
                        !dataMap.get(symbol).getDataPoints().isEmpty()) {

                    // Success
                    result.successCount++;
                    updateStatus(symbol, toDate.minusDays(0)); // Start of today? or Yesterday?
                    // Usually we consider data synced up to "yesterday" if we ran in morning.
                    // But if market is open/closed, let's just mark the date we fetched.
                    // If we fetched "to" date, we mark "to". (Since toDate is NOT inclusive in some
                    // APIs, but here we passed it)
                    // MarketDataFetchService usually treats 'to' as inclusive.
                    // We mark 'toDate' as synced.
                    // Wait, if toDate is today, and market is open?
                    // The job runs at 7:15 AM. Market is closed. So "Today" effectively means "data
                    // up to yesterday close".
                    // Actually, if we fetch with to=Today 7:15 AM, provider gives up to Yesterday
                    // Close.
                    // So we can mark "Yesterday" as the last sync date.
                    // Let's use `toDate.minusDays(1)` to be safe if running early morning.
                    // Actually, let's mark the date of the LAST candle we got.
                    // But that requires parsing the candle.
                    // Simple logic: we asked for data up to `toDate`. If success, we mark `toDate`
                    // (or `toDate -1` since job is pre-market).
                    // Let's mark `toDate.minusDays(1)`.

                } else {
                    // Fail or Empty
                    log.warn("fetchBatch", "No data for symbol {}", symbol);
                    result.failureCount++;
                    result.failedSymbols.add(symbol);
                }
            }

            // Estimate Payload Size (very rough: JSON string length or object grap)
            // Since we don't have serialized size here easily without overhead,
            // we'll estimate based on data points count * avg size (e.g., 50 bytes per
            // candle)
            // or just rely on a simple heuristic if available.
            // Better: if 'response' came from HTTP, we might not have content length here.
            // Let's iterate data points to count.
            long batchSize = 0;
            if (dataMap != null) {
                for (var entry : dataMap.entrySet()) {
                    if (entry.getValue().getDataPoints() != null) {
                        // timestamp(8) + open(8) + high(8) + low(8) + close(8) + volume(8) ~ 48 bytes +
                        // overhead
                        // Let's assume ~100 bytes per record for JSON representation
                        batchSize += entry.getValue().getDataPoints().size() * 100L;
                    }
                }
            }
            result.totalPayloadSize += batchSize;
            addLogAsync(jobLog, "Batch processed. Estimated size: " + (batchSize / 1024) + " KB");

        } catch (Exception e) {
            log.error("fetchBatch", "Error fetching batch starting {}", fromDate, e);
            result.failureCount += batch.size();
            result.failedSymbols.addAll(batch);
        }
        return result;
    }

    private void updateStatus(String symbol, LocalDate date) {
        MarketDataIngestionStatus status = MarketDataIngestionStatus.builder()
                .symbol(symbol)
                .lastIngestionDate(date)
                .lastUpdateTimestamp(LocalDateTime.now())
                .lastStatus("SUCCESS")
                .build();
        ingestionStatusRepository.save(status);
    }

    private <T> List<List<T>> chunkList(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }

    private static class ProcessingResult {
        int successCount = 0;
        int failureCount = 0;
        long totalPayloadSize = 0; // Track payload size
        List<String> failedSymbols = new ArrayList<>();

        void add(ProcessingResult other) {
            this.successCount += other.successCount;
            this.failureCount += other.failureCount;
            this.totalPayloadSize += other.totalPayloadSize;
            this.failedSymbols.addAll(other.failedSymbols);
        }
    }

    private void addLog(IngestionJobLog jobLog, String message) {
        String timestamp = LocalDateTime.now().toString();
        String logEntry = "[" + timestamp + "] " + message;

        // Push to Redis List
        String key = "job:logs:" + jobLog.getJobId();
        redisTemplate.opsForList().rightPush(key, logEntry);
        // Set TTL to 24 hours (86400 seconds) if it doesn't exist or refresh it
        redisTemplate.expire(key, java.time.Duration.ofHours(24));
    }

    // Thread-safe log addition for async operations
    private void addLogAsync(IngestionJobLog jobLog, String message) {
        addLog(jobLog, message);
    }
}
