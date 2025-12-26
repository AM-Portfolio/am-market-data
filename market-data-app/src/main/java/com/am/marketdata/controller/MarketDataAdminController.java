package com.am.marketdata.controller;

import com.am.marketdata.internal.model.IngestionJobLog;
import com.am.marketdata.internal.repository.IngestionJobLogRepository;
import com.am.marketdata.internal.service.MarketDataHistoricalSyncService;
import com.am.marketdata.internal.service.MarketDataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class MarketDataAdminController {

    private final IngestionJobLogRepository ingestionJobLogRepository;
    private final MarketDataHistoricalSyncService historicalSyncService;
    private final MarketDataIngestionService ingestionService;

    @GetMapping("/logs")
    public List<IngestionJobLog> getLogs(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ingestionJobLogRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"))).getContent();
    }

    @PostMapping("/sync/historical")
    public ResponseEntity<String> triggerHistoricalSync() {
        log.info("Manual trigger: Historical Sync");
        // Running asynchronously to avoid blocking
        new Thread(historicalSyncService::syncHistoricalData).start();
        return ResponseEntity.ok("Historical Sync Triggered");
    }

    @PostMapping("/ingestion/start")
    public ResponseEntity<String> startIngestion(@RequestParam(defaultValue = "UPSTOX") String provider,
            @RequestParam(defaultValue = "NIFTY 50,NIFTY BANK") List<String> symbols) {
        log.info("Manual trigger: Start Ingestion");
        ingestionService.startIngestion(symbols, provider, "1D", true, true);
        return ResponseEntity.ok("Ingestion Started");
    }

    @PostMapping("/ingestion/stop")
    public ResponseEntity<String> stopIngestion(@RequestParam String provider) {
        log.info("Manual trigger: Stop Ingestion");
        ingestionService.stopIngestion(provider);
        return ResponseEntity.ok("Ingestion Stopped");
    }
}
