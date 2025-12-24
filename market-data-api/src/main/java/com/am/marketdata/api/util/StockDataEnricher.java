package com.am.marketdata.api.util;

import com.am.common.investment.model.stockindice.StockData;
import com.am.marketdata.common.log.AppLogger;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class to enrich StockData with live price information
 * Provides reusable logic for fetching prices and calculating metrics
 */
@Component
@RequiredArgsConstructor
public class StockDataEnricher {

    private final AppLogger log = AppLogger.getLogger();
    private final MarketDataService marketDataService;

    /**
     * Enriched stock data with price information
     */
    public static class EnrichedStockData {
        private final StockData stockData;
        private final Double lastPrice;
        private final Double change;
        private final Double percentChange;
        private final Double previousClose;

        public EnrichedStockData(StockData stockData, OHLCQuote quote) {
            this.stockData = stockData;

            if (quote != null) {
                double lp = quote.getLastPrice();
                double pc = quote.getPreviousClose();

                this.lastPrice = lp;
                this.previousClose = pc;

                // Always calculate change and percentChange since OHLCQuote doesn't provide
                // them
                if (pc != 0) {
                    this.change = lp - pc;
                    this.percentChange = (change / pc) * 100.0;
                } else {
                    this.change = null;
                    this.percentChange = null;
                }
            } else {
                this.lastPrice = null;
                this.change = null;
                this.percentChange = null;
                this.previousClose = null;
            }
        }

        public StockData getStockData() {
            return stockData;
        }

        public String getSymbol() {
            return stockData != null ? stockData.getSymbol() : null;
        }

        public Double getLastPrice() {
            return lastPrice;
        }

        public Double getChange() {
            return change;
        }

        public Double getPercentChange() {
            return percentChange != null ? percentChange : 0.0;
        }

        public Double getPreviousClose() {
            return previousClose;
        }

        public boolean hasValidPrice() {
            return lastPrice != null && percentChange != null;
        }
    }

    /**
     * Enrich a list of StockData with live price information
     * 
     * @param stockDataList List of StockData to enrich
     * @return List of EnrichedStockData with price information
     */
    public List<EnrichedStockData> enrichWithPrices(List<StockData> stockDataList) {
        return enrichWithPrices(stockDataList, null);
    }

    /**
     * Enrich a list of StockData with price information for a specific time frame
     * 
     * @param stockDataList List of StockData to enrich
     * @param timeFrame     TimeFrame for price data (null for current/live prices)
     * @return List of EnrichedStockData with price information
     */
    public List<EnrichedStockData> enrichWithPrices(List<StockData> stockDataList,
            com.am.marketdata.common.model.TimeFrame timeFrame) {
        if (stockDataList == null || stockDataList.isEmpty()) {
            return Collections.emptyList();
        }

        // Extract symbols
        List<String> symbols = stockDataList.stream()
                .filter(sd -> sd != null && sd.getSymbol() != null)
                .map(StockData::getSymbol)
                .collect(Collectors.toList());

        if (symbols.isEmpty()) {
            log.warn("enrichWithPrices", "No valid symbols found in stock data list");
            return Collections.emptyList();
        }

        // Fetch prices for all symbols (with optional time frame)
        Map<String, OHLCQuote> priceData = fetchLivePrices(symbols, timeFrame);

        // Enrich each StockData with price information
        return stockDataList.stream()
                .filter(sd -> sd != null && sd.getSymbol() != null)
                .map(sd -> new EnrichedStockData(sd, priceData.get(sd.getSymbol())))
                .filter(EnrichedStockData::hasValidPrice) // Only keep stocks with valid prices
                .collect(Collectors.toList());
    }

    /**
     * Fetch prices for a list of symbols with optional time frame
     * 
     * @param symbols   List of symbols to fetch prices for
     * @param timeFrame TimeFrame for price data (null for current/live)
     * @return Map of symbol to OHLCQuote
     */
    private Map<String, OHLCQuote> fetchLivePrices(List<String> symbols,
            com.am.marketdata.common.model.TimeFrame timeFrame) {
        try {
            String timeFrameStr = timeFrame != null ? timeFrame.getApiValue() : "current";
            log.info("fetchLivePrices",
                    "Fetching prices for " + symbols.size() + " symbols with timeFrame: " + timeFrameStr);

            // Call market data service to get OHLC data
            Map<String, OHLCQuote> prices = marketDataService.getOHLC(symbols, timeFrame, false, null);

            log.info("fetchLivePrices", "Retrieved prices for " + prices.size() + " symbols");
            return prices;
        } catch (Exception e) {
            log.error("fetchLivePrices", "Error fetching prices", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Sort enriched data by percentage change
     * 
     * @param enrichedData List to sort
     * @param descending   True for descending (gainers), false for ascending
     *                     (losers)
     * @return Sorted list
     */
    public List<EnrichedStockData> sortByPercentChange(List<EnrichedStockData> enrichedData, boolean descending) {
        Comparator<EnrichedStockData> comparator = Comparator.comparingDouble(EnrichedStockData::getPercentChange);

        if (descending) {
            comparator = comparator.reversed();
        }

        return enrichedData.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Group enriched data by a custom grouping function
     * 
     * @param enrichedData     List to group
     * @param groupingFunction Function to extract group key
     * @return Map of group key to list of EnrichedStockData
     */
    public Map<String, List<EnrichedStockData>> groupBy(
            List<EnrichedStockData> enrichedData,
            java.util.function.Function<EnrichedStockData, String> groupingFunction) {

        return enrichedData.stream()
                .filter(esd -> groupingFunction.apply(esd) != null)
                .collect(Collectors.groupingBy(groupingFunction));
    }

    /**
     * Calculate average percentage change for a group
     * 
     * @param group List of EnrichedStockData
     * @return Average percentage change
     */
    public double calculateAveragePercentChange(List<EnrichedStockData> group) {
        return group.stream()
                .mapToDouble(EnrichedStockData::getPercentChange)
                .average()
                .orElse(0.0);
    }
}
