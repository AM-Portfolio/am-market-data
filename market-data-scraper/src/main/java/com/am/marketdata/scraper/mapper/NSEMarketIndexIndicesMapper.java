package com.am.marketdata.scraper.mapper;

import com.am.common.investment.model.equity.FundamentalRatios;
import com.am.common.investment.model.equity.HistoricalComparison;
import com.am.common.investment.model.equity.MarketBreadth;
import com.am.common.investment.model.equity.MarketData;
import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.marketdata.common.model.NSEIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("nseMarketIndexIndicesMapper")
public class NSEMarketIndexIndicesMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    public static List<MarketIndexIndices> convertToMarketIndexIndices(List<NSEIndex> data) {
        return data.stream()
            .map(NSEMarketIndexIndicesMapper::convertToMarketIndices)
            .collect(Collectors.toList());
    }
    
    public static MarketIndexIndices convertToMarketIndices(NSEIndex data) {
        return MarketIndexIndices.builder()
            .key(data.getKey())
            .index(data.getIndex())
            .indexSymbol(data.getIndexSymbol())
            .timestamp(LocalDateTime.now()) // Using current time instead of fixed timestamp
            .marketData(MarketData.builder()
                .open(data.getOpen())
                .high(data.getHigh())
                .low(data.getLow())
                .last(data.getLast())
                .previousClose(data.getPreviousClose())
                .percentChange(data.getPercentChange())
                .build())
            .fundamentalRatios(FundamentalRatios.builder()
                .priceToEarningRation(parseDouble(data.getPe()))
                .priceToBookRation(parseDouble(data.getPb()))
                .dividenYield(parseDouble(data.getDy()))
                .build())
            .marketBreadth(MarketBreadth.builder()
                .advances(String.valueOf(data.getAdvances()))
                .declines(String.valueOf(data.getDeclines()))
                .unchanged(String.valueOf(data.getUnchanged()))
                .build())
            .historicalComparison(HistoricalComparison.builder()
                .value(data.getLast())
                .date365dAgo(parseDate(data.getDate365dAgo()))
                .date30dAgo(parseDate(data.getDate30dAgo()))
                .previousDay(data.getPreviousClose())
                .build())
            .build();
    }

    private static LocalDateTime parseDate(String date) {
        if (date == null || date.equals("-")) {
            return LocalDateTime.now().minusDays(30); // Default fallback
        }
        try {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            return LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
        } catch (Exception e) {
            log.error("Error parsing date: {}", date, e);
            return null;
        }
    }

    private static Double parseDouble(String value) {
        if (value == null || value.equals("-") || value.equals("")) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            log.error("Error parsing double: {}", value, e);
            return null;
        }
    }
}
