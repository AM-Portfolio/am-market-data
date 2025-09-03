package com.am.marketdata.redis.service;

import com.am.common.investment.model.historical.OHLCVTPoint;
import com.am.marketdata.redis.util.BarCalculatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for calculating OHLCV bars from raw price data
 */
@Slf4j
@Service
public class BarCalculator {

    /**
     * Calculate OHLCV bars for a given interval
     * 
     * @param prices List of price points
     * @param interval The interval (5m, 15m, 30m, 1h, 4h, 1d)
     * @param date The date for which to calculate bars
     * @return List of calculated OHLCV bars
     */
    public List<OHLCVTPoint> calculateBars(List<OHLCVTPoint> prices, String interval, LocalDate date) {
        if (prices == null || prices.isEmpty()) {
            return Collections.emptyList();
        }

        BarCalculatorUtil.validateInterval(interval);
        
        // Sort prices by timestamp
        prices.sort(Comparator.comparing(OHLCVTPoint::getTime));
        
        if (interval.equals("1d")) {
            return calculateDailyBar(prices, date);
        } else {
            return calculateIntradayBars(prices, interval, date);
        }
    }

    /**
     * Calculate intraday bars (5m, 15m, 30m, 1h, 4h)
     */
    private List<OHLCVTPoint> calculateIntradayBars(List<OHLCVTPoint> prices, String interval, LocalDate date) {
        int minutes = BarCalculatorUtil.INTERVAL_MINUTES.get(interval);
        Map<LocalDateTime, List<OHLCVTPoint>> groupedPrices = 
                BarCalculatorUtil.groupPricesByInterval(prices, minutes, date);
        
        List<OHLCVTPoint> bars = new ArrayList<>();
        
        for (Map.Entry<LocalDateTime, List<OHLCVTPoint>> entry : groupedPrices.entrySet()) {
            LocalDateTime barTime = entry.getKey();
            List<OHLCVTPoint> barPrices = entry.getValue();
            
            if (!barPrices.isEmpty()) {
                bars.add(BarCalculatorUtil.createBar(barPrices, barTime));
            }
        }
        
        return bars;
    }

    /**
     * Calculate a single daily bar
     */
    private List<OHLCVTPoint> calculateDailyBar(List<OHLCVTPoint> prices, LocalDate date) {
        LocalDateTime barTime = date.atStartOfDay();
        return Collections.singletonList(BarCalculatorUtil.createBar(prices, barTime));
    }
}
