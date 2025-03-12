package com.am.marketdata.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataSchedulerService {
    private final ISINService isinService;
    private final EquityPriceProcessingService equityPriceProcessingService;

    @PostConstruct
    public void initialize() {
        log.info("Initializing StockDataSchedulerService");
        if (isWithinTradingHours()) {
            fetchAndPersistStockData();
        }
    }

    @Scheduled(cron = "5 */2 * * * *")  // Runs at 5 seconds past every 2 minutes
    @Transactional
    public void fetchAndPersistStockData() {
        if (!isWithinTradingHours()) {
            log.debug("Outside trading hours, skipping stock data processing");
            return;
        }

        log.info("=== Starting scheduled stock data fetch and persist job ===");
        try {
            List<String> isins = isinService.findDistinctIsins();
            boolean success = equityPriceProcessingService.processEquityPrices(isins);
            log.info("=== Completed scheduled stock data fetch and persist job. Success: {} ===", success);
        } catch (Exception e) {
            log.error("Error in stock data scheduler: {}", e.getMessage(), e);
        }
    }

    private boolean isWithinTradingHours() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        DayOfWeek day = now.getDayOfWeek();
        
        // Only run on weekdays (Monday to Friday)
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }

        LocalTime time = now.toLocalTime();
        LocalTime marketOpen = LocalTime.of(9, 15);  // 9:15 AM IST
        LocalTime marketClose = LocalTime.of(15, 35); // 3:35 PM IST

        return !time.isBefore(marketOpen) && !time.isAfter(marketClose);
    }
}