package com.am.marketdata.scraper.mapper;

import com.am.common.investment.model.equity.FundamentalRatios;
import com.am.common.investment.model.equity.MarketData;
import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.marketdata.common.model.NSEIndicesResponse.NSEIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("nseMarketIndexIndicesMapper")
public class NSEMarketIndexIndicesMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    public static MarketIndexIndices convertToMarketIndices(NSEIndex data) {
        return MarketIndexIndices.builder()
            .key(data.getKey())
            .index(data.getIndex())
            .indexSymbol(data.getIndexSymbol())
            .timestamp(LocalDateTime.now())
            .marketData(MarketData.builder()
                .open(data.getOpen())
                .high(data.getHigh())
                .low(data.getLow())
                .last(data.getLast())
                .previousClose(data.getPreviousClose())
                .percentChange(data.getPercentChange())
                .variation(data.getVariation())
                .yearHigh(data.getYearHigh())
                .yearLow(data.getYearLow())
                .indicativeClose(data.getIndicativeClose())
                .build())
            .fundamentalRatios(FundamentalRatios.builder()
                .priceToEarningRation(parseDouble(data.getPe()))
                .priceToBookRation(parseDouble(data.getPb()))
                .build())
            .build();
    }

    public static List<MarketIndexIndices> convertToMarketIndexIndices(List<NSEIndex> data) {
        return data.stream()
            .map(NSEMarketIndexIndicesMapper::convertToMarketIndices)
            .collect(Collectors.toList());
    }

    private static Double parseDouble(String value) {
        if (value == null || value.equals("-")) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double value: {}", value);
            return null;
        }
    }
}
