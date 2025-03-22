package com.am.marketdata.scraper.mapper;

import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.equity.StockInsidicesData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting NSE stock indices data to domain model
 */
@Slf4j
@Component("stockIndicesMapper")
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

    private static StockInsidicesData.Advance convertAdvance(NSEStockInsidicesData.Advance advance) {
        if (advance == null) {
            return null;
        }
        return new StockInsidicesData.Advance(
            advance.getDeclines(),
            advance.getAdvances(),
            advance.getUnchanged()
        );
    }

    private static List<StockInsidicesData.StockData> convertStockDataList(List<NSEStockInsidicesData.StockData> stockDataList) {
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
        return new StockInsidicesData.StockData(
            stockData.getPriority(),
            stockData.getSymbol(),
            stockData.getIdentifier(),
            stockData.getSeries(),
            stockData.getOpen(),
            stockData.getDayHigh(),
            stockData.getDayLow(),
            stockData.getLastPrice(),
            stockData.getPreviousClose(),
            stockData.getChange(),
            stockData.getPChange(),
            stockData.getTotalTradedVolume(),
            stockData.getStockIndClosePrice(),
            stockData.getTotalTradedValue(),
            stockData.getYearHigh(),
            stockData.getFfmc(),
            stockData.getYearLow(),
            stockData.getNearWKH(),
            stockData.getNearWKL(),
            stockData.getPerChange365d(),
            stockData.getDate365dAgo(),
            stockData.getDate30dAgo(),
            stockData.getPerChange30d()
        );
    }

    private static StockInsidicesData.Metadata convertMetadata(NSEStockInsidicesData.Metadata metadata) {
        if (metadata == null) {
            return null;
        }
        return new StockInsidicesData.Metadata(
            metadata.getIndexName(),
            metadata.getOpen(),
            metadata.getHigh(),
            metadata.getLow(),
            metadata.getPreviousClose(),
            metadata.getLast(),
            metadata.getPercChange(),
            metadata.getChange(),
            metadata.getTimeVal(),
            metadata.getYearHigh(),
            metadata.getYearLow(),
            metadata.getIndicativeClose(),
            metadata.getTotalTradedVolume(),
            metadata.getTotalTradedValue(),
            metadata.getFfmcSum()
        );
    }

    private static StockInsidicesData.MarketStatus convertMarketStatus(NSEStockInsidicesData.MarketStatus marketStatus) {
        if (marketStatus == null) {
            return null;
        }
        return new StockInsidicesData.MarketStatus(
            marketStatus.getMarket(),
            marketStatus.getMarketStatus(),
            marketStatus.getTradeDate(),
            marketStatus.getIndex(),
            marketStatus.getLast(),
            marketStatus.getVariation(),
            marketStatus.getPercentChange(),
            marketStatus.getMarketStatusMessage()
        );
    }
}
