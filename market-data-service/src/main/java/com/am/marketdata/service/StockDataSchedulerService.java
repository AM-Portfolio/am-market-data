package com.am.marketdata.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.stereotype.Service;

import com.am.common.amcommondata.service.AssetService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataSchedulerService {
    private final AssetService assetService;
    private final EquityPriceProcessingService equityPriceProcessingService;

    @Transactional
    public void fetchAndPersistStockData() {
        log.info("=== Starting scheduled stock data fetch and persist job ===");
        try {
            List<String> isins = assetService.findDistinctIsins();
            boolean success = equityPriceProcessingService.processEquityPrices(isins);
            log.info("=== Completed scheduled stock data fetch and persist job. Success: {} ===", success);
        } catch (Exception e) {
            log.error("Error in stock data scheduler: {}", e.getMessage(), e);
        }
    }
}