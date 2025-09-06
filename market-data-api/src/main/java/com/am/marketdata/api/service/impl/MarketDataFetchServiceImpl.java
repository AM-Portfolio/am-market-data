package com.am.marketdata.api.service.impl;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.model.historical.OHLCVTPoint;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

import com.am.common.investment.model.stockindice.StockData;
import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.common.investment.service.StockIndicesMarketDataService;
import com.am.marketdata.api.dto.HistoricalDataRequest;
import com.am.marketdata.api.service.InvestmentInstrumentService;
import com.am.marketdata.api.service.MarketDataFetchService;
import com.am.marketdata.service.MarketDataService;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MarketDataFetchServiceImpl.class);

    private final InvestmentInstrumentService investmentInstrumentService;
    private final MarketDataService marketDataService;
    private final StockIndicesMarketDataService stockIndicesMarketDataService;



    public MarketDataFetchServiceImpl(InvestmentInstrumentService investmentInstrumentService,
                                     MarketDataService marketDataService,
                                     StockIndicesMarketDataService stockIndicesMarketDataService) {
        this.investmentInstrumentService = investmentInstrumentService;
        this.marketDataService = marketDataService;
        this.stockIndicesMarketDataService = stockIndicesMarketDataService;
    }


    @Override
    public Map<String, Map<String, Object>> getQuotes(Set<String> tradingSymbols, boolean forceRefresh) {
        return investmentInstrumentService.getQuotes(tradingSymbols.stream().collect(Collectors.toList()));
    }

    @Override
    public Map<String, Object> getLivePrices(Set<String> symbols, boolean indexSymbol, boolean forceRefresh) {
        Set<String> symbolsSet = getSymbols(new HashSet<>(symbols), indexSymbol);
        return investmentInstrumentService.getLivePrices(new ArrayList<>(symbolsSet));
    }

    private Set<String> getSymbols(Set<String> symbols, boolean indexSymbol) {
        Set<String> symbolsSet = new HashSet<>(symbols);
        
        if (indexSymbol) {
            List<StockIndicesMarketData> indicesData = stockIndicesMarketDataService.findByIndexSymbols(symbols);
            // Extract symbols from indices data
            
            if (indicesData != null) {
                for (StockIndicesMarketData data : indicesData) {
                    // Extract constituent symbols from the index data if available
                    if (data != null && data.getData() != null) {
                        for (StockData stockData : data.getData()) {
                            if (stockData != null && stockData.getSymbol() != null) {
                                symbolsSet.add(stockData.getSymbol());
                            }
                        }
                    }
                }
            }
        } 

        return symbolsSet;
    }

    /**
     * Process historical data for a single symbol with filtering if requested
     * 
     * @param symbol Symbol to process
     * @param fromDate From date
     * @param toDate To date
     * @param interval Data interval
     * @param instrumentType Instrument type
     * @param additionalParams Additional parameters
     * @param forceRefresh Whether to force refresh from provider
     * @param filterParams Filter parameters
     * @return Result containing the processed data and metrics
     */
    private SymbolProcessingResult processSymbolHistoricalData(String symbol, Date fromDate, Date toDate, 
                                                       TimeFrame interval, String instrumentType, 
                                                       Map<String, Object> additionalParams, boolean forceRefresh,
                                                       FilterParams filterParams) {
        try {
            // Get raw historical data
            Map<String, Object> singleResult =  investmentInstrumentService.getHistoricalData(
                symbol, fromDate, toDate, interval, instrumentType, additionalParams);
            
            if (singleResult != null && !singleResult.containsKey("error")) {
                // Get original count
                int originalCount = singleResult.containsKey("count") ? (int) singleResult.get("count") : 0;
                
                // Apply data filtering if requested
                if (filterParams.isFiltered) {
                    singleResult = applyDataFiltering(singleResult, additionalParams);
                }
                
                // Get filtered count
                int filteredCount = singleResult.containsKey("count") ? (int) singleResult.get("count") : originalCount;
                
                return new SymbolProcessingResult(symbol, singleResult, true, originalCount, filteredCount, null);
            } else {
                log.warn("Failed to get historical data for symbol: {}", symbol);
                return new SymbolProcessingResult(symbol, 
                        Collections.singletonMap("error", "Failed to fetch data"), 
                        false, 0, 0, null);
            }
        } catch (Exception e) {
            log.error("Error processing historical data for symbol {}: {}", symbol, e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to fetch historical data");
            errorResult.put("message", e.getMessage());
            return new SymbolProcessingResult(symbol, errorResult, false, 0, 0, e);
        }
    }
    
    /**
     * Class to hold the result of processing a single symbol
     */
    private static class SymbolProcessingResult {
        final Map<String, Object> data;
        final boolean success;
        final int originalCount;
        final int filteredCount;
        
        SymbolProcessingResult(String symbol, Map<String, Object> data, boolean success, 
                              int originalCount, int filteredCount, Exception error) {
            this.data = data;
            this.success = success;
            this.originalCount = originalCount;
            this.filteredCount = filteredCount;
        }
    }
    
    @Override
    public Map<String, Object> getHistoricalDataMultipleSymbols(Set<String> symbols, Date fromDate, Date toDate, 
                                                         TimeFrame interval, String instrumentType, 
                                                         Map<String, Object> additionalParams, boolean forceRefresh) {
        log.info("Processing historical data request for multiple symbols: {} from {} to {}", 
                symbols, fromDate, toDate);
        
        // Prepare the result container
        Map<String, Object> aggregatedResult = new HashMap<>();
        Map<String, Object> symbolsData = new HashMap<>();
        aggregatedResult.put("data", symbolsData);
        
        if (symbols == null || symbols.isEmpty()) {
            log.warn("No symbols provided for historical data request");
            return aggregatedResult;
        }
        
        // Extract filter parameters once
        FilterParams filterParams = extractFilterParams(additionalParams);
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int totalDataPoints = 0;
        int totalFilteredDataPoints = 0;
        
        // Process each symbol
        for (String symbol : symbols) {
            SymbolProcessingResult result = processSymbolHistoricalData(
                symbol, fromDate, toDate, interval, instrumentType, additionalParams, forceRefresh, filterParams);
            
            // Add to results
            symbolsData.put(symbol, result.data);
            
            // Update metrics
            if (result.success) {
                successCount++;
                totalDataPoints += result.originalCount;
                totalFilteredDataPoints += result.filteredCount;
            }
        }
        
        // If no filtering, filtered count equals total count
        if (!filterParams.isFiltered) {
            totalFilteredDataPoints = totalDataPoints;
        }
        
        long endTime = System.currentTimeMillis();
        
        // Build the aggregated response
        aggregatedResult.put("data", symbolsData);
        aggregatedResult.put("symbols", symbols);
        aggregatedResult.put("fromDate", new SimpleDateFormat("yyyy-MM-dd").format(fromDate));
        aggregatedResult.put("toDate", new SimpleDateFormat("yyyy-MM-dd").format(toDate));
        aggregatedResult.put("interval", interval);
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
        
        // Log results
        // logProcessingResults(successCount, symbols.size(), totalDataPoints, totalFilteredDataPoints, 
        //                    filterParams.isFiltered, (endTime - startTime));
        
        return aggregatedResult;
    }
    
    /**
     * Extract filter parameters from the additionalParams map
     * 
     * @param params Parameters map containing filter settings
     * @return FilterParams object with extracted parameters
     */
    private FilterParams extractFilterParams(Map<String, Object> params) {
        if (params == null) {
            return new FilterParams("ALL", 1, false);
        }
        
        String filterType = params.containsKey("filterType") ? 
                params.get("filterType").toString() : "ALL";
                
        int filterFrequency = params.containsKey("filterFrequency") ? 
                Integer.parseInt(params.get("filterFrequency").toString()) : 1;
        
        // For CUSTOM type, ensure filterFrequency is at least 2
        if ("CUSTOM".equalsIgnoreCase(filterType) && filterFrequency < 2) {
            log.warn("CUSTOM filter type specified but filterFrequency is less than 2 ({}). Using default of 2.", filterFrequency);
            filterFrequency = 2;
        }
        
        boolean isFiltered = !"ALL".equalsIgnoreCase(filterType);
        
        return new FilterParams(filterType, filterFrequency, isFiltered);
    }
    
    /**
     * Simple class to hold filter parameters
     */
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
    
    /**
     * Class to hold historical data information extracted from different data structures
     */
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
    
    /**
     * Apply filtering strategy to data points based on filter parameters
     * 
     * @param dataPoints Original data points to filter
     * @param filterParams Filter parameters
     * @return Filtered list of data points
     */
    private List<OHLCVTPoint> applyFilterStrategy(List<OHLCVTPoint> dataPoints, FilterParams filterParams) {
        List<OHLCVTPoint> filteredPoints = new ArrayList<>();
        
        if ("START_END".equalsIgnoreCase(filterParams.filterType)) {
            // Only include first and last points
            filteredPoints.add(dataPoints.get(0)); // First point
            
            if (dataPoints.size() > 1) {
                filteredPoints.add(dataPoints.get(dataPoints.size() - 1)); // Last point
            }
        } else if ("CUSTOM".equalsIgnoreCase(filterParams.filterType)) {
            // Include every Nth point
            for (int i = 0; i < dataPoints.size(); i += filterParams.filterFrequency) {
                filteredPoints.add(dataPoints.get(i));
            }
            
            // Always include the last point if not already included
            int lastIndex = dataPoints.size() - 1;
            if (lastIndex >= 0 && lastIndex % filterParams.filterFrequency != 0) {
                filteredPoints.add(dataPoints.get(lastIndex));
            }
        } else {
            // Default case - return all points
            return new ArrayList<>(dataPoints);
        }
        
        return filteredPoints;
    }
    
    /**
     * Extract historical data information from different data structures
     * 
     * @param data The data map containing historical data
     * @return HistoricalDataInfo object with extracted data points and metadata
     */
    @SuppressWarnings("unchecked") // Needed for type safety with Map and List casts
    private HistoricalDataInfo extractHistoricalDataInfo(Map<String, Object> data) {
        Object dataObj = data.get("data");
        List<OHLCVTPoint> dataPoints = new ArrayList<>();
        String tradingSymbol = "";
        String interval = "";
        
        // Extract data from either HistoricalData object or Map
        if (dataObj instanceof HistoricalData) {
            HistoricalData historicalData = (HistoricalData) dataObj;
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
                        // Convert Date to LocalDateTime if needed
                        if (pointMap.get("timestamp") instanceof Date) {
                            Date date = (Date) pointMap.get("timestamp");
                            point.setTime(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                        } else if (pointMap.get("timestamp") instanceof LocalDateTime) {
                            point.setTime((LocalDateTime) pointMap.get("timestamp"));
                        }
                    }
                    
                    if (pointMap.containsKey("open")) {
                        point.setOpen(Double.parseDouble(pointMap.get("open").toString()));
                    }
                    
                    if (pointMap.containsKey("high")) {
                        point.setHigh(Double.parseDouble(pointMap.get("high").toString()));
                    }
                    
                    if (pointMap.containsKey("low")) {
                        point.setLow(Double.parseDouble(pointMap.get("low").toString()));
                    }
                    
                    if (pointMap.containsKey("close")) {
                        point.setClose(Double.parseDouble(pointMap.get("close").toString()));
                    }
                    
                    if (pointMap.containsKey("volume")) {
                        point.setVolume(Long.parseLong(pointMap.get("volume").toString()));
                    }
                    
                    dataPoints.add(point);
                }
            }
        } else {
            log.warn("Unexpected data type for filtering: {}", dataObj != null ? dataObj.getClass().getName() : "null");
            return null; // Return null if unexpected type
        }
        
        if (dataPoints.isEmpty()) {
            log.warn("No data points found for filtering");
            return null; // Return null if no data points
        }
        
        return new HistoricalDataInfo(dataPoints, tradingSymbol, interval);
    }
    
    /**
     * Apply filtering to historical data based on filter parameters
     * 
     * @param data Original historical data response
     * @param params Parameters containing filter settings
     * @return Filtered historical data
     */
    private Map<String, Object> applyDataFiltering(Map<String, Object> data, Map<String, Object> params) {
        FilterParams filterParams = extractFilterParams(params);
        
        // If no filtering needed (ALL type)
        if (!filterParams.isFiltered) {
            return data;
        }
        
        // Get the historical data points
        Map<String, Object> result = new HashMap<>(data);
        
        // Extract data points and metadata
        HistoricalDataInfo dataInfo = extractHistoricalDataInfo(data);
        
        // Return original data if extraction failed or no data points found
        if (dataInfo == null || dataInfo.dataPoints.isEmpty()) {
            return data;
        }
        
        List<OHLCVTPoint> originalPoints = dataInfo.dataPoints;
        
        // Apply filtering based on type
        List<OHLCVTPoint> filteredPoints = applyFilterStrategy(originalPoints, filterParams);
        
        // Create a new HistoricalData object with filtered points
        HistoricalData filteredData = new HistoricalData();
        filteredData.setDataPoints(filteredPoints);
        filteredData.setTradingSymbol(dataInfo.tradingSymbol);
        filteredData.setInterval(dataInfo.interval);
        
        // Update the result
        result.put("data", filteredData);
        result.put("count", filteredPoints.size());
        result.put("filtered", true);
        result.put("filterType", filterParams.filterType);
        result.put("originalCount", originalPoints.size());
        
        log.debug("Applied {} filtering to historical data, reduced from {} to {} points", 
                filterParams.filterType, originalPoints.size(), filteredPoints.size());
        
        return result;
    }

    @Override
    public Map<String, Object> getOptionChain(String underlyingSymbol, Date expiryDate, boolean forceRefresh) {
        log.debug("Fetching option chain for symbol: {} with expiry date: {}", underlyingSymbol, expiryDate);
        return fetchOptionChain(underlyingSymbol, expiryDate);
    }
    
    private Map<String, Object> fetchOptionChain(String underlyingSymbol, Date expiryDate) {
        Map<String, Object> data = investmentInstrumentService.getOptionChain(underlyingSymbol, expiryDate);
        return data;
    }
    

    @Override
    public Map<String, Object> getMutualFundDetails(String schemeCode, boolean forceRefresh) {
        log.debug("Fetching mutual fund details for scheme code: {}", schemeCode);
        return fetchMutualFundDetails(schemeCode);
    }
    
    private Map<String, Object> fetchMutualFundDetails(String schemeCode) {
        Map<String, Object> details = investmentInstrumentService.getMutualFundDetails(schemeCode);
        return details;
    }
    
    @Override
    public Map<String, Object> getMutualFundNavHistory(String schemeCode, Date from, Date to, boolean forceRefresh) {
        log.debug("Fetching mutual fund NAV history for scheme code: {} from: {} to: {}", schemeCode, from, to);
        return fetchMutualFundNavHistory(schemeCode, from, to);
    }
    
    private Map<String, Object> fetchMutualFundNavHistory(String schemeCode, Date from, Date to) {
        Map<String, Object> history = investmentInstrumentService.getMutualFundNavHistory(schemeCode, from, to);
        return history;
    }
    
    @Override
    public Map<String, Object> processHistoricalDataRequest(HistoricalDataRequest request) throws Exception {
        log.info("Processing historical data request for symbols: {} from {} to {}, interval: {}, filterType: {}", 
                request.getSymbols(), request.getFrom(), request.getTo(), request.getInterval(), request.getFilterType());
        
        // Parse symbols
        Set<String> symbolList = parseSymbols(request.getSymbols());
        if (symbolList.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No valid symbols provided");
            errorResponse.put("message", "Please provide at least one valid symbol");
            return errorResponse;
        }
        
        // Parse dates
        Date fromDate;
        Date toDate;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            fromDate = dateFormat.parse(request.getFrom());
            toDate = dateFormat.parse(request.getTo());
        } catch (ParseException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid date format");
            errorResponse.put("message", "Use yyyy-MM-dd format for dates");
            return errorResponse;
        }
        
        // Prepare additional parameters
        Map<String, Object> additionalParams = request.getAdditionalParams();
        if (additionalParams == null) {
            additionalParams = new HashMap<>();
        }
        additionalParams.put("filterType", request.getFilterType());
        additionalParams.put("filterFrequency", request.getFilterFrequency());
        
        // Get historical data
        Map<String, Object> response = getHistoricalDataMultipleSymbols(
            symbolList, fromDate, toDate, request.getInterval(), request.getInstrumentType(), 
            additionalParams, request.isForceRefresh());
        
        // Add cache status if not present
        if (!response.containsKey("cached")) {
            response.put("cached", !request.isForceRefresh());
        }
        
        return response;
    }
    
    /**
     * Parse comma-separated symbols into a set
     * 
     * @param symbolsString Comma-separated symbols
     * @return Set of symbols
     */
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
    public Map<String, Object> getOHLC(Set<String> symbols, boolean isIndexSymbol, boolean forceRefresh) {

        symbols = getSymbols(symbols, isIndexSymbol);

        Map<String, OHLCQuote> ohlcData = marketDataService.getOHLC(new ArrayList<>(symbols), forceRefresh);
        
        // Create response with cache status
        Map<String, Object> response = new HashMap<>();
        response.put("data", ohlcData);
        response.put("cached", !forceRefresh);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    
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
    
    /**
     * Find symbols from StockIndicesMarketData that are not included in the passed list of symbols
     * 
     * @param indexSymbols List of index symbols to search for market data
     * @param symbolsToCheck List of symbols to check against the data
     * @return List of symbols that are in the market data but not in the symbolsToCheck list
     */
    public List<String> findMissingSymbols(List<String> indexSymbols, List<String> symbolsToCheck) {
        log.debug("Finding symbols not included in the passed list: {}", symbolsToCheck);
        
        if (symbolsToCheck == null || symbolsToCheck.isEmpty()) {
            log.warn("Empty symbols list provided to check against, returning empty list");
            return Collections.emptyList();
        }
        
        // Get all stock indices market data
        Set<StockIndicesMarketData> indicesData = getStockIndicesData(new HashSet<>(indexSymbols), false);
        
        if (indicesData == null || indicesData.isEmpty()) {
            log.warn("No stock indices market data found for symbols: {}", indexSymbols);
            return Collections.emptyList();
        }
        
        // Create a set of symbols to check for faster lookups
        Set<String> symbolsSet = new HashSet<>(symbolsToCheck);
        
        // Collect all symbols from the data that are not in the symbolsToCheck list
        List<String> missingSymbols = indicesData.stream()
            .filter(data -> data != null && data.getData() != null)
            .flatMap(data -> data.getData().stream())
            .filter(stockData -> stockData != null && stockData.getSymbol() != null)
            .map(stockData -> stockData.getSymbol())
            .distinct()
            .filter(symbol -> !symbolsSet.contains(symbol))
            .collect(Collectors.toList());
        
        log.info("Found {} symbols that are not included in the passed list of {} symbols", 
                missingSymbols.size(), symbolsToCheck.size());
        
        return missingSymbols;
    }
}
