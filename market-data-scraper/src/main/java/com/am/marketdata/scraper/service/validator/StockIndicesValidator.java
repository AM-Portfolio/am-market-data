package com.am.marketdata.scraper.service.validator;

import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.scraper.service.common.DataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Validator for NSE stock indices data
 */
@Slf4j
@RequiredArgsConstructor
public class StockIndicesValidator implements DataValidator<NSEStockInsidicesData> {
    
    private static final DateTimeFormatter MARKET_STATUS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
    
    @Value("${market.data.max.age.minutes:1500}")
    private long maxDataAgeMinutes;
    
    @Override
    public boolean isValid(NSEStockInsidicesData response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            log.warn("Received empty stock indices response for index: {}", response.getName());
            return false;
        }

        if (response.getMarketStatus() == null) {
            log.warn("Stock indices response missing market status for index: {}", response.getName());
            return false;
        }

        // Parse and validate market status date
        try {
            String marketStatusDate = response.getMarketStatus().getTradeDate();
            if (marketStatusDate == null) {
                log.warn("Stock indices response missing market status date for index: {}", response.getName());
                return false;
            }

            LocalDateTime marketTime = LocalDateTime.parse(marketStatusDate, MARKET_STATUS_DATE_FORMAT);
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            long minutesOld = java.time.Duration.between(marketTime, now).toMinutes();

            if (minutesOld > maxDataAgeMinutes) {
                log.warn("Stock indices data is too old for index {}: {} minutes old", response.getName(), minutesOld);
                return false;
            }

            // Validate stock data
            for (NSEStockInsidicesData.StockData stockData : response.getData()) {
                if (stockData.getSymbol() == null || 
                    stockData.getLastPrice() == null ||
                    stockData.getPreviousClose() == null) {
                    log.warn("Invalid stock data for symbol {} in index {}", 
                        stockData.getSymbol(), response.getName());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Error validating stock indices data for index {}", response.getName(), e);
            return false;
        }
    }
    
    @Override
    public String getDataTypeName() {
        return "stock-indices";
    }
}
