// package com.am.marketdata.service.impl;

// import com.am.common.investment.model.historical.HistoricalData;
// import com.am.common.investment.model.historical.OHLCVTPoint;
// import com.am.common.investment.model.historical.OHLCVTPoint;
// import com.am.marketdata.common.model.TimeFrame;
// import com.am.marketdata.service.TimeFrameAggregationService;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;

// import java.math.BigDecimal;
// import java.time.*;
// import java.util.*;
// import java.util.stream.Collectors;

// /**
//  * Implementation of TimeFrameAggregationService for aggregating historical data
//  * into timeframes not directly supported by data providers.
//  */
// @Slf4j
// @Service
// public class TimeFrameAggregationServiceImpl implements TimeFrameAggregationService {

//     @Override
//     public HistoricalData aggregateTimeFrame(HistoricalData sourceData, TimeFrame targetTimeFrame) {
//         if (sourceData == null || sourceData.getDataPoints() == null || sourceData.getDataPoints().isEmpty()) {
//             log.warn("Cannot aggregate empty historical data");
//             return sourceData;
//         }

//         if (!targetTimeFrame.requiresAggregation()) {
//             log.debug("No aggregation needed for timeframe {}", targetTimeFrame);
//             return sourceData;
//         }

//         log.info("Aggregating historical data from {} to {} timeframe", 
//                 sourceData.getInterval(), targetTimeFrame.getUserValue());

//         // Create a new HistoricalData object for the aggregated data
//         HistoricalData aggregatedData = new HistoricalData();
//         aggregatedData.setTradingSymbol(sourceData.getTradingSymbol());
//         aggregatedData.setInterval(targetTimeFrame.getUserValue());

//         // Group data points by the target timeframe
//         Map<LocalDateTime, List<OHLCVTPoint>> groupedPoints = groupDataPointsByTimeFrame(
//                 (List<OHLCVTPoint>) sourceData.getDataPoints(), targetTimeFrame);

//         // Aggregate each group into a single data point
//         List<OHLCVTPoint> aggregatedPoints = groupedPoints.entrySet().stream()
//                 .map(entry -> aggregateDataPoints(entry.getKey(), entry.getValue()))
//                 .sorted(Comparator.comparing(point -> point.getTime().atZone(ZoneId.systemDefault()).toInstant()))
//                 .collect(Collectors.toList());

//         aggregatedData.setDataPoints(aggregatedPoints);
        
//         log.info("Successfully aggregated {} source data points into {} {} data points",
//                 sourceData.getDataPoints().size(), aggregatedPoints.size(), targetTimeFrame.getUserValue());

//         return aggregatedData;
//     }

//     @Override
//     public boolean requiresAggregation(TimeFrame timeFrame) {
//         return timeFrame != null && timeFrame.requiresAggregation();
//     }

//     @Override
//     public TimeFrame getBaseTimeFrame(TimeFrame targetTimeFrame) {
//         if (targetTimeFrame == null) {
//             return TimeFrame.DAY; // Default
//         }
//         return targetTimeFrame.getBaseTimeFrame();
//     }

//     @Override
//     public Date[] calculateAdjustedDateRange(Date fromDate, Date toDate, TimeFrame targetTimeFrame) {
//         if (!requiresAggregation(targetTimeFrame) || fromDate == null || toDate == null) {
//             return new Date[] { fromDate, toDate };
//         }

//         Calendar calendar = Calendar.getInstance();
        
//         // For week and month timeframes, we need to extend the from date to ensure we have complete periods
//         switch (targetTimeFrame) {
//             case FOUR_HOUR:
//                 // For 4-hour, we need to ensure we have complete 4-hour blocks
//                 // Extend from date by up to 3 hours earlier to align with 4-hour blocks
//                 calendar.setTime(fromDate);
//                 int hour = calendar.get(Calendar.HOUR_OF_DAY);
//                 int adjustHours = hour % 4;
//                 if (adjustHours > 0) {
//                     calendar.add(Calendar.HOUR_OF_DAY, -adjustHours);
//                     fromDate = calendar.getTime();
//                 }
//                 break;
                
//             case WEEK:
//                 // For weekly, extend from date to previous Sunday/Monday (depending on week start)
//                 calendar.setTime(fromDate);
//                 int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
//                 // Assuming weeks start on Monday (adjust if needed)
//                 int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
//                 if (daysToSubtract > 0) {
//                     calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
//                     fromDate = calendar.getTime();
//                 }
//                 break;
                
//             case MONTH:
//                 // For monthly, extend from date to start of month
//                 calendar.setTime(fromDate);
//                 calendar.set(Calendar.DAY_OF_MONTH, 1);
//                 fromDate = calendar.getTime();
//                 break;
                
//             case YEAR:
//                 // For yearly, extend from date to start of year
//                 calendar.setTime(fromDate);
//                 calendar.set(Calendar.DAY_OF_YEAR, 1);
//                 fromDate = calendar.getTime();
//                 break;
                
//             default:
//                 // No adjustment needed for other timeframes
//                 break;
//         }
        
//         return new Date[] { fromDate, toDate };
//     }

//     /**
//      * Group data points by the target timeframe
//      * 
//      * @param dataPoints Source data points
//      * @param targetTimeFrame Target timeframe
//      * @return Map of grouped data points
//      */
//     private Map<LocalDateTime, List<OHLCVTPoint>> groupDataPointsByTimeFrame(
//             List<OHLCVTPoint> dataPoints, TimeFrame targetTimeFrame) {
        
//         Map<LocalDateTime, List<OHLCVTPoint>> groupedPoints = new HashMap<>();
        
//         for (OHLCVTPoint point : dataPoints) {
//             // Get LocalDateTime directly from OHLCVTPoint
//             LocalDateTime timestamp = point.getTime();
            
//             // Calculate the key timestamp for this group based on target timeframe
//             LocalDateTime keyTimestamp = calculateGroupKey(timestamp, targetTimeFrame);
            
//             // Add the data point to its group
//             groupedPoints.computeIfAbsent(keyTimestamp, k -> new ArrayList<>()).add(point);
//         }
        
//         return groupedPoints;
//     }
    
//     /**
//      * Calculate the group key timestamp for a data point based on target timeframe
//      * 
//      * @param timestamp Original timestamp
//      * @param targetTimeFrame Target timeframe
//      * @return Group key timestamp
//      */
//     private LocalDateTime calculateGroupKey(LocalDateTime timestamp, TimeFrame targetTimeFrame) {
//         switch (targetTimeFrame) {
//             case FOUR_HOUR:
//                 // Group by 4-hour blocks (0-3, 4-7, 8-11, etc.)
//                 int hour = timestamp.getHour();
//                 return timestamp.withHour(hour - (hour % 4)).withMinute(0).withSecond(0).withNano(0);
                
//             case WEEK:
//                 // Group by week (starting from Monday)
//                 return timestamp.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
                
//             case MONTH:
//                 // Group by month (starting from day 1)
//                 return timestamp.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                
//             case YEAR:
//                 // Group by year (starting from day 1 of month 1)
//                 return timestamp.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                
//             default:
//                 // Should not happen as we check requiresAggregation() first
//                 log.warn("Unexpected timeframe for aggregation: {}", targetTimeFrame);
//                 return timestamp;
//         }
//     }
    
//     /**
//      * Aggregate a group of data points into a single data point
//      * 
//      * @param keyTimestamp Key timestamp for the group
//      * @param points Data points in the group
//      * @return Aggregated data point
//      */
//     private OHLCVTPoint aggregateDataPoints(LocalDateTime keyTimestamp, List<OHLCVTPoint> points) {
//         if (points == null || points.isEmpty()) {
//             throw new IllegalArgumentException("Cannot aggregate empty data points");
//         }
        
//         // Sort points by timestamp to ensure correct OHLC calculation
//         points.sort(Comparator.comparing(point -> point.getTime()));
        
//         // First point's open is the open for the aggregated candle
//         BigDecimal open = points.get(0).getOpen();
        
//         // Last point's close is the close for the aggregated candle
//         BigDecimal close = points.get(points.size() - 1).getClose();
        
//         // High is the maximum high across all points
//         BigDecimal high = points.stream()
//                 .map(OHLCVTPoint::getHigh)
//                 .max(BigDecimal::compareTo)
//                 .orElse(BigDecimal.ZERO);
        
//         // Low is the minimum low across all points
//         BigDecimal low = points.stream()
//                 .map(OHLCVTPoint::getLow)
//                 .min(BigDecimal::compareTo)
//                 .orElse(BigDecimal.ZERO);
        
//         // Volume is the sum of volumes across all points
//         long volume = points.stream()
//                 .mapToLong(OHLCVTPoint::getVolume)
//                 .sum();
        
//         // Create the aggregated data point
//         OHLCVTPoint aggregatedPoint = new OHLCVTPoint();
//         aggregatedPoint.setTime(keyTimestamp);
//         aggregatedPoint.setOpen(open);
//         aggregatedPoint.setHigh(high);
//         aggregatedPoint.setLow(low);
//         aggregatedPoint.setClose(close);
//         aggregatedPoint.setVolume(volume);
        
//         return aggregatedPoint;
//     }
// }
