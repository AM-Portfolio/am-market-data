package com.am.marketdata.scraper.mapper;

import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.equity.StockInsidicesData;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting NSE stock indices data to domain model
 */
@Slf4j
public class StockIndicesMapper {

    /**
     * Convert NSE market insidices data to domain stock indices
     * 
     * @param data NSE market insidices data
     * @return Domain stock indices
     */
    public static StockInsidicesData convertToStockIndices(NSEStockInsidicesData data) {
        if (data == null || data.getData() == null) {
            log.warn("Null or empty stock indices data");
            return null;
        }

        return new StockInsidicesData(
            data.getName(),
            convertAdvance(data.getAdvance()),
            data.getTimestamp(),
            convertStockDataList(data.getData()),
            convertMetadata(data.getMetadata()),
            convertMarketStatus(data.getMarketStatus()),
            data.getDate30dAgo(),
            data.getDate365dAgo()
        );
    }

    protected static StockInsidicesData.Advance convertAdvance(NSEStockInsidicesData.Advance advance) {
        if (advance == null) {
            return null;
        }
        return new StockInsidicesData.Advance(
            Integer.parseInt(advance.getDeclines()),
            Integer.parseInt(advance.getAdvances()),
            Integer.parseInt(advance.getUnchanged())
        );
    }

    protected static List<StockInsidicesData.StockData> convertStockDataList(List<NSEStockInsidicesData.StockData> stockDataList) {
        if (stockDataList == null) {
            return null;
        }
        return stockDataList.stream()
            .map(StockIndicesMapper::convertStockData)
            .collect(Collectors.toList());
    }

    private static StockInsidicesData.StockData convertStockData(NSEStockInsidicesData.StockData stockData) {
        if (stockData == null) {
            return null;
        }
        return StockInsidicesData.StockData.builder()
            .priority(stockData.getPriority())
            .symbol(stockData.getSymbol())
            .identifier(stockData.getIdentifier())
            .series(stockData.getSeries())
            .open(stockData.getOpen())
            .dayHigh(stockData.getDayHigh())
            .dayLow(stockData.getDayLow())
            .lastPrice(stockData.getLastPrice())
            .previousClose(stockData.getPreviousClose())
            .change(stockData.getChange())
            .pChange(stockData.getPChange())
            .totalTradedVolume(stockData.getTotalTradedVolume())
            .totalTradedValue(stockData.getTotalTradedValue())
            .yearHigh(stockData.getYearHigh())
            .yearLow(stockData.getYearLow())
            .perChange365d(stockData.getPerChange365d())
            .date365dAgo(stockData.getDate365dAgo())
            .perChange30d(stockData.getPerChange30d())
            .date30dAgo(stockData.getDate30dAgo())
            .build();
    }

    protected static StockInsidicesData.Metadata convertMetadata(NSEStockInsidicesData.Metadata metadata) {
        if (metadata == null) {
            return null;
        }
        return StockInsidicesData.Metadata.builder()
            .indexName(metadata.getIndexName())
            .open(metadata.getOpen())
            .high(metadata.getHigh())
            .low(metadata.getLow())
            .previousClose(metadata.getPreviousClose())
            .change(metadata.getChange())
            .percChange(metadata.getPercChange())
            .build();   
    }

    protected static StockInsidicesData.MarketStatus convertMarketStatus(NSEStockInsidicesData.MarketStatus marketStatus) {
        if (marketStatus == null) {
            return null;
        }
        return StockInsidicesData.MarketStatus.builder()
            .market(marketStatus.getMarket())
            .marketStatus(marketStatus.getMarketStatus())
            .tradeDate(marketStatus.getTradeDate())
            .index(marketStatus.getIndex())
            .variation(marketStatus.getVariation())
            .percentChange(marketStatus.getPercentChange())
            .build();
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double value for field {}: value='{}'", "", value, e);
            return null;
        }
    }
}
