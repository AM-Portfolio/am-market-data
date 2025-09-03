package com.am.marketdata.redis.util;

import com.am.common.investment.model.historical.OHLCVTPoint;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for bar calculation operations
 */
@Slf4j
public class BarCalculatorUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    // Map of interval to minutes
    public static final Map<String, Integer> INTERVAL_MINUTES = Map.of(
            "5m", 5,
            "15m", 15,
            "30m", 30,
            "1h", 60,
            "4h", 240,
            "1d", 1440
    );
    
    private BarCalculatorUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Validate that the interval is supported
     */
    public static void validateInterval(String interval) {
        if (!INTERVAL_MINUTES.containsKey(interval)) {
            throw new IllegalArgumentException("Unsupported interval: " + interval + 
                    ". Supported intervals are: " + String.join(", ", INTERVAL_MINUTES.keySet()));
        }
    }
    
    /**
     * Calculate the start time of an interval
     */
    public static LocalDateTime calculateIntervalStart(LocalDateTime timestamp, int intervalMinutes) {
        LocalTime time = timestamp.toLocalTime();
        int totalMinutes = time.getHour() * 60 + time.getMinute();
        int intervalNumber = totalMinutes / intervalMinutes;
        
        int startHour = (intervalNumber * intervalMinutes) / 60;
        int startMinute = (intervalNumber * intervalMinutes) % 60;
        
        return timestamp.toLocalDate().atTime(startHour, startMinute);
    }
    
    /**
     * Create an OHLCV bar from a list of price points
     */
    public static OHLCVTPoint createBar(List<OHLCVTPoint> prices, LocalDateTime barTime) {
        if (prices == null || prices.isEmpty()) {
            return null;
        }
        
        double open = prices.get(0).getOpen();
        double close = prices.get(prices.size() - 1).getClose();
        
        double high = prices.stream()
                .mapToDouble(OHLCVTPoint::getHigh)
                .max()
                .orElse(0.0);
                
        double low = prices.stream()
                .mapToDouble(OHLCVTPoint::getLow)
                .min()
                .orElse(0.0);
                
        long volume = prices.stream()
                .mapToLong(OHLCVTPoint::getVolume)
                .sum();
                
        return OHLCVTPoint.builder()
                .time(barTime)
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .volume(volume)
                .build();
    }
    
    /**
     * Convert a date to string format
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Create a price point from raw data
     */
    public static OHLCVTPoint createPricePoint(LocalDateTime timestamp, double price, long volume) {
        return OHLCVTPoint.builder()
                .time(timestamp)
                .open(price)
                .high(price)
                .low(price)
                .close(price)
                .volume(volume)
                .build();
    }
    
    /**
     * Group price points by interval
     */
    public static Map<LocalDateTime, List<OHLCVTPoint>> groupPricesByInterval(
            List<OHLCVTPoint> prices, int intervalMinutes, LocalDate date) {
        
        Map<LocalDateTime, List<OHLCVTPoint>> result = new TreeMap<>();
        
        // Initialize all intervals for the day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        // Create empty buckets for all intervals in the day
        LocalDateTime currentInterval = startOfDay;
        while (currentInterval.isBefore(endOfDay)) {
            result.put(currentInterval, new ArrayList<>());
            currentInterval = currentInterval.plusMinutes(intervalMinutes);
        }
        
        // Assign each price to its interval
        for (OHLCVTPoint price : prices) {
            LocalDateTime timestamp = price.getTime();
            if (timestamp.toLocalDate().equals(date)) {
                LocalDateTime intervalStart = calculateIntervalStart(timestamp, intervalMinutes);
                result.computeIfAbsent(intervalStart, k -> new ArrayList<>()).add(price);
            }
        }
        
        return result;
    }
}
