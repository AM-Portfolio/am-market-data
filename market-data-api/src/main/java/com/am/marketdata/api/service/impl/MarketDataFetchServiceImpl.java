package com.am.marketdata.api.service.impl;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.model.historical.OHLCVTPoint;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.common.investment.service.StockIndicesMarketDataService;
import com.am.marketdata.api.dto.HistoricalDataRequest;
import com.am.marketdata.api.service.MarketDataFetchService;
import com.am.marketdata.service.MarketDataService;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.common.log.AppLogger;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of MarketDataFetchService
 */
@Service
public class MarketDataFetchServiceImpl implements MarketDataFetchService {

    private final AppLogger log = AppLogger.getLogger();

    private final MarketDataService marketDataService;
    private final StockIndicesMarketDataService stockIndicesMarketDataService;
    private final com.am.marketdata.api.util.InstrumentUtils instrumentUtils;

    public MarketDataFetchServiceImpl(MarketDataService marketDataService,
            StockIndicesMarketDataService stockIndicesMarketDataService,
            com.am.marketdata.api.util.InstrumentUtils instrumentUtils) {
        this.marketDataService = marketDataService;
        this.stockIndicesMarketDataService = stockIndicesMarketDataService;
        this.instrumentUtils = instrumentUtils;
    }

    @Override
    public Map<String, Map<String, Object>> getQuotes(Set<String> tradingSymbols, boolean forceRefresh) {
        // Use marketDataService.getOHLC with DAY timeframe for quotes
        Map<String, OHLCQuote> ohlcData = marketDataService.getOHLC(
                new ArrayList<>(tradingSymbols), TimeFrame.DAY, forceRefresh, null);

        // Convert to expected format
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Map.Entry<String, OHLCQuote> entry : ohlcData.entrySet()) {
            Map<String, Object> quoteData = new HashMap<>();
            OHLCQuote quote = entry.getValue();
            quoteData.put("lastPrice", quote.getLastPrice());
            if (quote.getOhlc() != null) {
                quoteData.put("open", quote.getOhlc().getOpen());
                quoteData.put("high", quote.getOhlc().getHigh());
                quoteData.put("low", quote.getOhlc().getLow());
                quoteData.put("close", quote.getOhlc().getClose());
            }
            result.put(entry.getKey(), quoteData);
        }
        return result;
    }

    @Override
    public Map<String, Object> getQuotes(Set<String> tradingSymbols, boolean isIndexSymbol, TimeFrame timeFrame,
            boolean forceRefresh) {
        String methodName = "getQuotes";
        log.info(methodName,
                String.format("Getting quotes for %d symbols with timeFrame: %s, isIndexSymbol: %b, forceRefresh: %b",
                        tradingSymbols.size(), timeFrame.getApiValue(), isIndexSymbol, forceRefresh));

        // Get all symbols including index constituents if requested
        Set<String> symbols = getSymbols(tradingSymbols, isIndexSymbol);

        // Get OHLC data with timeframe support (pass null provider)
        Map<String, OHLCQuote> ohlcData = marketDataService.getOHLC(new ArrayList<>(symbols), timeFrame, forceRefresh,
                null);

        // Create response with cache status
        Map<String, Object> response = new HashMap<>();
        response.put("quotes", ohlcData);
        response.put("count", ohlcData.size());
        response.put("cached", !forceRefresh);
        response.put("timestamp", System.currentTimeMillis());
        response.put("timeFrame", timeFrame.getApiValue());
        response.put("source", forceRefresh ? "provider" : "cache");

        return response;
    }

    @Override
    public Map<String, Object> getLivePrices(Set<String> symbols, boolean indexSymbol, boolean forceRefresh) {
        Set<String> symbolsSet = getSymbols(new HashSet<>(symbols), indexSymbol);
        List<com.am.common.investment.model.equity.EquityPrice> prices = marketDataService.getLivePrices(
                new ArrayList<>(symbolsSet), null);

        Map<String, Object> result = new HashMap<>();
        result.put("prices", prices);
        result.put("count", prices.size());
        return result;
    }

    /**
     * Resolves symbols based on whether they are index symbols or not.
     * 
     * @param symbols     Set of input symbols
     * @param indexSymbol If true, symbols are treated as index symbols and returned
     *                    as-is without DB expansion.
     *                    If false, symbols are resolved; indices are expanded to
     *                    their constituent stocks via DB lookup.
     * @return Set of resolved symbols
     */
    private Set<String> getSymbols(Set<String> symbols, boolean indexSymbol) {
        // Pass !indexSymbol as expandIndices flag to InstrumentUtils
        // If indexSymbol=true, we want expandIndices=false (don't expand, return as-is)
        // If indexSymbol=false, we want expandIndices=true (expand indices to
        // constituent stocks)
        boolean expandIndices = !indexSymbol;

        log.debug("getSymbols",
                String.format("indexSymbol=%b, expandIndices=%b, symbols=%s", indexSymbol, expandIndices, symbols));

        return instrumentUtils.resolveSymbols(new ArrayList<>(symbols), expandIndices);
    }

    @Override
    public Map<String, Object> getHistoricalDataMultipleSymbols(Set<String> symbols, Date fromDate, Date toDate,
            TimeFrame interval, String instrumentType,
            Map<String, Object> additionalParams, boolean forceRefresh) {
        String methodName = "getHistoricalDataMultipleSymbols";
        log.info(methodName, String.format(
                "[BATCH_HISTORICAL] getHistoricalDataMultipleSymbols: Processing historical data request for %d symbols from %s to %s, interval: %s (apiValue: %s)",
                symbols.size(), fromDate, toDate, interval, interval.getApiValue()));

        Map<String, Object> aggregatedResult = new HashMap<>();
        Map<String, Object> symbolsData = new HashMap<>();

        if (symbols == null || symbols.isEmpty()) {
            log.warn(methodName, "No symbols provided for historical data request");
            aggregatedResult.put("data", symbolsData);
            return aggregatedResult;
        }

        FilterParams filterParams = extractFilterParams(additionalParams);
        long startTime = System.currentTimeMillis();

        try {
            // Use batch retrieval instead of looping through symbols
            log.info(methodName, String.format(
                    "[BATCH_HISTORICAL] Calling marketDataService.getHistoricalDataBatch for %d symbols",
                    symbols.size()));

            // Detect if this might be an index symbol (single symbol requests are often
            // indices)
            // Detect if this is an index symbol from parameters
            boolean isIndexSymbol = false;
            if (additionalParams != null && additionalParams.containsKey("isIndexSymbol")) {
                Object paramValue = additionalParams.get("isIndexSymbol");
                if (paramValue instanceof Boolean) {
                    isIndexSymbol = (Boolean) paramValue;
                } else if (paramValue instanceof String) {
                    isIndexSymbol = Boolean.parseBoolean((String) paramValue);
                }
            }
            Map<String, HistoricalData> batchResult = marketDataService.getHistoricalDataBatch(
                    new ArrayList<>(symbols), fromDate, toDate, interval, false, additionalParams, null, isIndexSymbol);

            int successCount = 0;
            int totalDataPoints = 0;
            int totalFilteredDataPoints = 0;

            // Process batch results
            for (String symbol : symbols) {
                HistoricalData historicalData = batchResult.get(symbol);

                if (historicalData != null && historicalData.getDataPoints() != null
                        && !historicalData.getDataPoints().isEmpty()) {
                    List<OHLCVTPoint> dataPoints = historicalData.getDataPoints();
                    int originalCount = dataPoints.size();

                    // Apply filtering if requested
                    if (filterParams.isFiltered) {
                        dataPoints = applyFilterStrategy(dataPoints, filterParams);
                    }

                    int filteredCount = dataPoints.size();

                    Map<String, Object> successData = new HashMap<>();
                    successData.put("status", "success");
                    successData.put("dataPoints", dataPoints);
                    successData.put("count", filteredCount);
                    symbolsData.put(symbol, successData);

                    successCount++;
                    totalDataPoints += originalCount;
                    totalFilteredDataPoints += filteredCount;
                } else {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("status", "error");
                    errorData.put("message", "No data found for symbol");
                    symbolsData.put(symbol, errorData);
                }
            }

            if (!filterParams.isFiltered) {
                totalFilteredDataPoints = totalDataPoints;
            }

            long endTime = System.currentTimeMillis();

            aggregatedResult.put("data", symbolsData);
            aggregatedResult.put("symbols", symbols);
            aggregatedResult.put("fromDate", new SimpleDateFormat("yyyy-MM-dd").format(fromDate));
            aggregatedResult.put("toDate", new SimpleDateFormat("yyyy-MM-dd").format(toDate));
            aggregatedResult.put("interval", interval.getApiValue());
            aggregatedResult.put("intervalEnum", interval.name());
            aggregatedResult.put("totalSymbols", symbols.size());
            aggregatedResult.put("successfulSymbols", successCount);
            aggregatedResult.put("totalDataPoints", totalDataPoints);
            aggregatedResult.put("filteredDataPoints", totalFilteredDataPoints);
            aggregatedResult.put("filtered", filterParams.isFiltered);
            aggregatedResult.put("filterType", filterParams.filterType);
            if (filterParams.isFiltered) {
                aggregatedResult.put("filterFrequency", filterParams.filterFrequency);
            }
            aggregatedResult.put("processingTimeMs", (endTime - startTime));

            log.info(methodName, String.format(
                    "[BATCH_HISTORICAL] Completed batch processing. Total symbols: %d, Successful: %d, Total data points: %d, Processing time: %dms",
                    symbols.size(), successCount, totalDataPoints, (endTime - startTime)));

        } catch (Exception e) {
            log.error(methodName, "Error in batch historical data retrieval: " + e.getMessage(), e);
            aggregatedResult.put("error", "Failed to retrieve batch historical data");
            aggregatedResult.put("message", e.getMessage());
        }

        return aggregatedResult;
    }

    private FilterParams extractFilterParams(Map<String, Object> params) {
        if (params == null) {
            return new FilterParams("ALL", 1, false);
        }

        String filterType = params.containsKey("filterType") ? params.get("filterType").toString() : "ALL";

        int filterFrequency = params.containsKey("filterFrequency")
                ? Integer.parseInt(params.get("filterFrequency").toString())
                : 1;

        if ("CUSTOM".equalsIgnoreCase(filterType) && filterFrequency < 2) {
            log.warn("extractFilterParams",
                    String.format(
                            "CUSTOM filter type specified but filterFrequency is less than 2 (%d). Using default of 2.",
                            filterFrequency));
            filterFrequency = 2;
        }

        boolean isFiltered = !"ALL".equalsIgnoreCase(filterType);

        return new FilterParams(filterType, filterFrequency, isFiltered);
    }

    private static class FilterParams {
        final String filterType;
        final int filterFrequency;
        final boolean isFiltered;

        FilterParams(String filterType, int filterFrequency, boolean isFiltered) {
            this.filterType = filterType;
            this.filterFrequency = filterFrequency;
            this.isFiltered = isFiltered;
        }
    }

    private static class HistoricalDataInfo {
        final List<OHLCVTPoint> dataPoints;
        final String tradingSymbol;
        final String interval;

        HistoricalDataInfo(List<OHLCVTPoint> dataPoints, String tradingSymbol, String interval) {
            this.dataPoints = dataPoints;
            this.tradingSymbol = tradingSymbol;
            this.interval = interval;
        }
    }

    private List<OHLCVTPoint> applyFilterStrategy(List<OHLCVTPoint> dataPoints, FilterParams filterParams) {
        List<OHLCVTPoint> filteredPoints = new ArrayList<>();

        if ("START_END".equalsIgnoreCase(filterParams.filterType)) {
            filteredPoints.add(dataPoints.get(0));

            if (dataPoints.size() > 1) {
                filteredPoints.add(dataPoints.get(dataPoints.size() - 1));
            }
        } else if ("CUSTOM".equalsIgnoreCase(filterParams.filterType)) {
            for (int i = 0; i < dataPoints.size(); i += filterParams.filterFrequency) {
                filteredPoints.add(dataPoints.get(i));
            }

            int lastIndex = dataPoints.size() - 1;
            if (lastIndex >= 0 && lastIndex % filterParams.filterFrequency != 0) {
                filteredPoints.add(dataPoints.get(lastIndex));
            }
        } else {
            return new ArrayList<>(dataPoints);
        }

        return filteredPoints;
    }

    @SuppressWarnings("unchecked")
    private HistoricalDataInfo extractHistoricalDataInfo(Map<String, Object> data) {
        Object dataObj = data.get("data");
        List<OHLCVTPoint> dataPoints = new ArrayList<>();
        String tradingSymbol = "";
        String interval = "";

        if (dataObj instanceof HistoricalData) {
            // Note: HistoricalData import needs to be correct.
            // In MarketDataFetchServiceImpl originally it was
            // com.am.common.investment.model.historical.HistoricalData
            // But here I imported
            // com.am.common.investment.model.historical.db.HistoricalData?
            // Need to be careful. Original import:
            // import com.am.common.investment.model.historical.HistoricalData;
            // Let's stick to original import if possible.
            // Ah, I see: import
            // com.am.common.investment.model.historical.db.HistoricalData; in my new code
            // snippet
            // I should use "com.am.common.investment.model.historical.HistoricalData"
            // unlessdb is correct.
            // MarketDataService returns
            // "com.am.common.investment.model.historical.HistoricalData".
            // So I should fix import in this file.

            com.am.common.investment.model.historical.HistoricalData historicalData = (com.am.common.investment.model.historical.HistoricalData) dataObj;
            dataPoints = historicalData.getDataPoints();
            tradingSymbol = historicalData.getTradingSymbol();
            interval = historicalData.getInterval();
        } else if (dataObj instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) dataObj;

            if (dataMap.containsKey("tradingSymbol")) {
                tradingSymbol = dataMap.get("tradingSymbol").toString();
            }

            if (dataMap.containsKey("interval")) {
                interval = dataMap.get("interval").toString();
            }

            if (dataMap.containsKey("dataPoints") && dataMap.get("dataPoints") instanceof List) {
                List<Map<String, Object>> pointMaps = (List<Map<String, Object>>) dataMap.get("dataPoints");

                for (Map<String, Object> pointMap : pointMaps) {
                    OHLCVTPoint point = new OHLCVTPoint();

                    if (pointMap.containsKey("timestamp")) {
                        if (pointMap.get("timestamp") instanceof Date) {
                            Date date = (Date) pointMap.get("timestamp");
                            point.setTime(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                        } else if (pointMap.get("timestamp") instanceof LocalDateTime) {
                            point.setTime((LocalDateTime) pointMap.get("timestamp"));
                        }
                    }

                    if (pointMap.containsKey("open"))
                        point.setOpen(Double.parseDouble(pointMap.get("open").toString()));
                    if (pointMap.containsKey("high"))
                        point.setHigh(Double.parseDouble(pointMap.get("high").toString()));
                    if (pointMap.containsKey("low"))
                        point.setLow(Double.parseDouble(pointMap.get("low").toString()));
                    if (pointMap.containsKey("close"))
                        point.setClose(Double.parseDouble(pointMap.get("close").toString()));
                    if (pointMap.containsKey("volume"))
                        point.setVolume(Long.parseLong(pointMap.get("volume").toString()));

                    dataPoints.add(point);
                }
            }
        } else {
            log.warn("extractHistoricalDataInfo",
                    "Unexpected data type for filtering: " + (dataObj != null ? dataObj.getClass().getName() : "null"));
            return null;
        }

        if (dataPoints.isEmpty()) {
            log.warn("extractHistoricalDataInfo", "No data points found for filtering");
            return null;
        }

        return new HistoricalDataInfo(dataPoints, tradingSymbol, interval);
    }

    private Map<String, Object> applyDataFiltering(Map<String, Object> data, Map<String, Object> params) {
        FilterParams filterParams = extractFilterParams(params);

        if (!filterParams.isFiltered) {
            return data;
        }

        Map<String, Object> result = new HashMap<>(data);
        HistoricalDataInfo dataInfo = extractHistoricalDataInfo(data);

        if (dataInfo == null || dataInfo.dataPoints.isEmpty()) {
            return data;
        }

        List<OHLCVTPoint> originalPoints = dataInfo.dataPoints;
        List<OHLCVTPoint> filteredPoints = applyFilterStrategy(originalPoints, filterParams);

        // Use correct HistoricalData class
        com.am.common.investment.model.historical.HistoricalData filteredData = new com.am.common.investment.model.historical.HistoricalData();
        filteredData.setDataPoints(filteredPoints);
        filteredData.setTradingSymbol(dataInfo.tradingSymbol);
        filteredData.setInterval(dataInfo.interval);

        result.put("data", filteredData);
        result.put("count", filteredPoints.size());
        result.put("filtered", true);
        result.put("filterType", filterParams.filterType);
        result.put("originalCount", originalPoints.size());

        return result;
    }

    @Override
    public Map<String, Object> getOptionChain(String underlyingSymbol, Date expiryDate, boolean forceRefresh) {
        log.debug("getOptionChain",
                "Fetching option chain for symbol: " + underlyingSymbol + " with expiry date: " + expiryDate);
        // Option chain functionality not yet migrated to MarketDataService
        Map<String, Object> result = new HashMap<>();
        result.put("error", "Option chain not yet supported");
        return result;
    }

    @Override
    public Map<String, Object> getMutualFundDetails(String schemeCode, boolean forceRefresh) {
        log.debug("getMutualFundDetails", "Fetching mutual fund details for scheme code: " + schemeCode);
        // Mutual fund functionality not yet migrated to MarketDataService
        Map<String, Object> result = new HashMap<>();
        result.put("error", "Mutual fund details not yet supported");
        return result;
    }

    @Override
    public Map<String, Object> getMutualFundNavHistory(String schemeCode, Date from, Date to, boolean forceRefresh) {
        log.debug("getMutualFundNavHistory",
                "Fetching mutual fund NAV history for scheme code: " + schemeCode + " from: " + from + " to: " + to);
        // Mutual fund functionality not yet migrated to MarketDataService
        Map<String, Object> result = new HashMap<>();
        result.put("error", "Mutual fund NAV history not yet supported");
        return result;
    }

    @Override
    public Map<String, Object> processHistoricalDataRequest(HistoricalDataRequest request) throws Exception {
        String methodName = "processHistoricalDataRequest";
        log.info(methodName, String.format(
                "[INTERVAL_TRACE] Controller → Service: Processing historical data request for symbols: %s from %s to %s, interval: %s (enum: %s, apiValue: %s), filterType: %s, isIndexSymbol: %b",
                request.getSymbols(), request.getFrom(), request.getTo(),
                request.getInterval(),
                request.getInterval().name(),
                request.getInterval().getApiValue(),
                request.getFilterType(),
                request.isIndexSymbol()));

        // Resolve symbols - DON'T expand if isIndexSymbol is true
        // isIndexSymbol=true means we want the index itself, not its constituents
        // isIndexSymbol=false means expand indices to constituent stocks
        Set<String> symbolList;
        if (request.isIndexSymbol()) {
            log.info(methodName, "[INTERVAL_TRACE] isIndexSymbol=true, returning index symbols as-is: {}",
                    request.getSymbols());
            symbolList = parseSymbols(request.getSymbols());
            // Pass expandIndices=false to keep index symbols as-is
            symbolList = instrumentUtils.resolveSymbols(new ArrayList<>(symbolList), false);
            log.info(methodName, "[INTERVAL_TRACE] Kept {} index symbols without expansion",
                    symbolList.size());
        } else {
            log.info(methodName, "[INTERVAL_TRACE] isIndexSymbol=false, expanding indices to constituent stocks");
            Set<String> parsedSymbols = parseSymbols(request.getSymbols());
            // Pass expandIndices=true to expand indices to constituent stocks
            symbolList = instrumentUtils.resolveSymbols(new ArrayList<>(parsedSymbols), true);
            log.info(methodName, "[INTERVAL_TRACE] Expanded {} symbols to {} stocks",
                    parsedSymbols.size(), symbolList.size());
        }

        if (symbolList.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No valid symbols provided");
            errorResponse.put("message", "Please provide at least one valid symbol");
            return errorResponse;
        }

        Date fromDate;
        Date toDate;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            fromDate = dateFormat.parse(request.getFrom());

            // If 'to' date is not provided, use current date
            if (request.getTo() == null || request.getTo().trim().isEmpty()) {
                toDate = new Date(); // Current date
                log.info(methodName, "[INTERVAL_TRACE] 'to' date not provided, using current date: {}",
                        dateFormat.format(toDate));
            } else {
                toDate = dateFormat.parse(request.getTo());
            }
        } catch (ParseException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid date format");
            errorResponse.put("message", "Use yyyy-MM-dd format for dates");
            return errorResponse;
        }

        Map<String, Object> additionalParams = request.getAdditionalParams();
        if (additionalParams == null) {
            additionalParams = new HashMap<>();
        }
        additionalParams.put("filterType", request.getFilterType());
        additionalParams.put("filterFrequency", request.getFilterFrequency());

        // Here we still call getHistoricalDataMultipleSymbols which we updated to NOT
        // take providerName
        // So request.getProviderName() is ignored.
        // If we want to support it, we'd need to bypass
        // "getHistoricalDataMultipleSymbols" or update it to take providerName...
        // But I updated "getHistoricalDataMultipleSymbols" to NOT take providerName.
        // So essentially providerName in DTO is now ignored and sticky session is
        // enforced.
        // This is consistent with "sticky session" goal.

        log.info(methodName, String.format(
                "[INTERVAL_TRACE] Service → getHistoricalDataMultipleSymbols: Calling with interval: %s (apiValue: %s)",
                request.getInterval(), request.getInterval().getApiValue()));

        Map<String, Object> response = getHistoricalDataMultipleSymbols(
                symbolList, fromDate, toDate, request.getInterval(),
                request.getInstrumentType(),
                additionalParams, request.isForceRefresh());

        if (!response.containsKey("cached")) {
            response.put("cached", !request.isForceRefresh());
        }

        log.info(methodName, String.format(
                "[INTERVAL_TRACE] Service → Controller: Returning response with interval: %s",
                response.get("interval")));

        return response;
    }

    private Set<String> parseSymbols(String symbolsString) {
        if (symbolsString == null || symbolsString.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(symbolsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, OHLCQuote> getOHLC(Set<String> symbols, boolean isIndexSymbol, TimeFrame timeFrame,
            boolean forceRefresh) {

        symbols = getSymbols(symbols, isIndexSymbol);

        Map<String, OHLCQuote> ohlcData = marketDataService.getOHLC(new ArrayList<>(symbols), timeFrame, forceRefresh,
                null);

        if (ohlcData != null) {
            log.info("getOHLC", "Fetched OHLC data for keys: " + ohlcData.keySet());
            return ohlcData;
        } else {
            log.warn("getOHLC", "Fetched OHLC data is null");
            return new HashMap<>();
        }
    }

    @Override
    public StockIndicesMarketData getStockIndexData(String indexSymbol, boolean forceRefresh) {
        return stockIndicesMarketDataService.findByIndexSymbol(indexSymbol);
    }

    @Override
    public Set<StockIndicesMarketData> getStockIndicesData(Set<String> indexSymbols, boolean forceRefresh) {
        Set<StockIndicesMarketData> indicesData = indexSymbols.stream()
                .map(symbol -> stockIndicesMarketDataService.findByIndexSymbol(symbol))
                .filter(data -> data != null)
                .collect(Collectors.toSet());

        return indicesData;
    }

    public List<String> findMissingSymbols(List<String> indexSymbols, List<String> symbolsToCheck) {
        log.debug("findMissingSymbols", "Finding symbols not included in the passed list: " + symbolsToCheck);

        if (symbolsToCheck == null || symbolsToCheck.isEmpty()) {
            return Collections.emptyList();
        }

        Set<StockIndicesMarketData> indicesData = getStockIndicesData(new HashSet<>(indexSymbols), false);

        if (indicesData == null || indicesData.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> symbolsSet = new HashSet<>(symbolsToCheck);

        List<String> missingSymbols = indicesData.stream()
                .filter(data -> data != null && data.getData() != null)
                .flatMap(data -> data.getData().stream())
                .filter(stockData -> stockData != null && stockData.getSymbol() != null)
                .map(stockData -> stockData.getSymbol())
                .distinct()
                .filter(symbol -> !symbolsSet.contains(symbol))
                .collect(Collectors.toList());

        return missingSymbols;
    }

    public Map<String, Object> getHistoricalChartsData(String symbol, String range) {
        String methodName = "getHistoricalChartsData";
        log.info(methodName, String.format("Fetching historical charts for symbol: %s, range: %s",
                symbol, range));

        String interval;
        java.time.LocalDate to = java.time.LocalDate.now();
        java.time.LocalDate from;

        if ("5Y".equalsIgnoreCase(range)) {
            interval = "month"; // Monthly
            from = to.minusYears(5);
        } else {
            // Default to 1Y
            interval = "day"; // Daily
            from = to.minusYears(1);
        }

        // Construct HistoricalDataRequest
        HistoricalDataRequest request = new HistoricalDataRequest();
        request.setSymbols(symbol); // Expects String, not List
        request.setFrom(from.toString());
        request.setTo(to.toString());
        request.setInterval(TimeFrame.fromApiValue(interval)); // Convert string to TimeFrame
        request.setFilterType("price");

        try {
            return processHistoricalDataRequest(request);
        } catch (Exception e) {
            log.error(methodName, "Error fetching historical charts for " + symbol + ": " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch chart data");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }
}
