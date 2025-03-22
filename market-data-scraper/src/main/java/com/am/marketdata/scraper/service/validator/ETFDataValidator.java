package com.am.marketdata.scraper.service.validator;

import com.am.marketdata.common.model.NseETFResponse;
import com.am.marketdata.scraper.service.common.DataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Validator for NSE ETF data
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ETFDataValidator implements DataValidator<NseETFResponse> {
    
    private static final DateTimeFormatter MARKET_STATUS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
    
    @Value("${market.data.max.age.minutes:15}")
    private long maxDataAgeMinutes;
    
    @Override
    public boolean isValid(NseETFResponse response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            log.warn("Received empty ETF response");
            return false;
        }

        if (response.getMarketStatus() == null) {
            log.warn("ETF response missing market status");
            return false;
        }

        // Parse and validate trade date
        try {
            String tradeDate = response.getMarketStatus().getTradeDate();
            if (tradeDate == null) {
                log.warn("ETF response missing trade date");
                return false;
            }

            LocalDateTime marketTime = LocalDateTime.parse(tradeDate, MARKET_STATUS_DATE_FORMAT);
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            long minutesOld = java.time.Duration.between(marketTime, now).toMinutes();

            if (minutesOld > maxDataAgeMinutes) {
                log.warn("ETF data is too old: {} minutes", minutesOld);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to parse ETF trade date", e);
            return false;
        }

        return true;
    }
    
    @Override
    public String getDataTypeName() {
        return "etf";
    }
}
