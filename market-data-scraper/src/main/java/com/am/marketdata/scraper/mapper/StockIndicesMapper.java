package com.am.marketdata.scraper.mapper;

import com.am.common.investment.model.events.StockInsidicesEventData;
import com.am.marketdata.common.model.NSEStockIndicesDataV1;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting NSE stock indices data to domain model
 */
public class StockIndicesMapper {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StockIndicesMapper.class);

    /**
     * Convert NSE market insidices data to domain stock indices
     * 
     * @param data NSE market insidices data
     * @return Domain stock indices
     */
    public static StockInsidicesEventData convertToStockIndices(NSEStockIndicesDataV1 data) {
        if (data == null || data.getData() == null) {
            log.warn("Null or empty stock indices data");
            return null;
        }

        return new StockInsidicesEventData(
                data.getName(),
                data.getName(),
                convertAdvance(data.getAdvance()),
                data.getTimestamp(),
                convertStockDataList(data.getData()),
                convertIndexMetadata(data.getMetadata()),
                convertMarketStatus(data.getMarketStatus()),
                data.getDate30dAgo(),
                data.getDate365dAgo(),
                "1.0");
    }

    protected static StockInsidicesEventData.Advance convertAdvance(NSEStockIndicesDataV1.Advance advance) {
        if (advance == null) {
            return null;
        }
        return new StockInsidicesEventData.Advance(
                Integer.parseInt(advance.getDeclines()),
                Integer.parseInt(advance.getAdvances()),
                Integer.parseInt(advance.getUnchanged()));
    }

    protected static List<StockInsidicesEventData.StockData> convertStockDataList(
            List<NSEStockIndicesDataV1.StockData> stockDataList) {
        if (stockDataList == null) {
            return null;
        }
        return stockDataList.stream()
                .map(StockIndicesMapper::convertStockData)
                .collect(Collectors.toList());
    }

    private static StockInsidicesEventData.StockData convertStockData(NSEStockIndicesDataV1.StockData stockData) {
        if (stockData == null) {
            return null;
        }
        return StockInsidicesEventData.StockData.builder()
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
                .metadata(convertMetadata(stockData.getMeta()))
                .build();
    }

    protected static StockInsidicesEventData.IndexMetadata convertIndexMetadata(
            NSEStockIndicesDataV1.IndexMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        return StockInsidicesEventData.IndexMetadata.builder()
                .indexName(metadata.getIndexName())
                .open(metadata.getOpen())
                .high(metadata.getHigh())
                .low(metadata.getLow())
                .previousClose(metadata.getPreviousClose())
                .change(metadata.getChange())
                .percChange(metadata.getPercChange())
                .build();
    }

    protected static StockInsidicesEventData.Metadata convertMetadata(NSEStockIndicesDataV1.Metadata metadata) {
        if (metadata == null) {
            return null;
        }
        return StockInsidicesEventData.Metadata.builder()
                .symbol(metadata.getSymbol())
                .companyName(metadata.getCompanyName())
                .industry(metadata.getIndustry())
                .activeSeries(metadata.getActiveSeries())
                .isin(metadata.getIsin())
                .build();
    }

    protected static StockInsidicesEventData.MarketStatus convertMarketStatus(
            NSEStockIndicesDataV1.MarketStatus marketStatus) {
        if (marketStatus == null) {
            return null;
        }
        return StockInsidicesEventData.MarketStatus.builder()
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
