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
import java.util.Objects;

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
                .open(parseDouble(data.getOpen()))
                .high(parseDouble(data.getHigh()))
                .low(parseDouble(data.getLow()))
                .last(parseDouble(data.getLast()))
                .previousClose(parseDouble(data.getPreviousDay()))
                .percentChange(parseDouble(data.getPercentChange()))
                .variation(parseDouble(data.getVariation()))
                .yearHigh(parseDouble(data.getYearHigh()))
                .yearLow(parseDouble(data.getYearLow()))
                .indicativeClose(null)  // No indicative close in NSEIndex
                .build())
            .fundamentalRatios(FundamentalRatios.builder()
                .priceToEarningRation(parseDouble(data.getPe()))
                .priceToBookRation(parseDouble(data.getPb()))
                .build())
            .build();
    }

    public static List<MarketIndexIndices> convertToMarketIndexIndices(List<NSEIndex> data) {
        List<MarketIndexIndices> result = data.stream()
            .map(index -> {
                try {
                    MarketIndexIndices mapped = convertToMarketIndices(index);
                    if (mapped == null) {
                        log.warn("Failed to map NSEIndex record: {}", index);
                        return null;
                    }
                    return mapped;
                } catch (Exception e) {
                    log.error("Error mapping NSEIndex record: {}", index, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (result.size() != data.size()) {
            log.warn("Mapping completed with some failures. Successfully mapped: {}/{} records", 
                result.size(), data.size());
        }
        
        return result;
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double value: {}", value, e);
            return null;
        }
    }
}
