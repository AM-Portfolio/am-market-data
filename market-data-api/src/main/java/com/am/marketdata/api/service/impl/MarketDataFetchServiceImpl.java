package com.am.marketdata.api.service.impl;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.model.historical.OHLCVTPoint;

import java.time.LocalDateTime;
import java.time.ZoneId;

import com.am.common.investment.model.stockindice.StockData;
import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.common.investment.service.StockIndicesMarketDataService;
import com.am.marketdata.api.service.InvestmentInstrumentService;
import com.am.marketdata.api.service.MarketDataFetchService;
import com.am.marketdata.service.MarketDataService;
import com.am.marketdata.common.model.OHLCQuote;
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
    
    
    @Override
    public Map<String, Object> getHistoricalData(String symbol, Date fromDate, Date toDate, 
                                              String interval, String instrumentType, 
                                              Map<String, Object> additionalParams, boolean forceRefresh) {
        return fetchHistoricalData(symbol, fromDate, toDate, interval, instrumentType, additionalParams);
    }
    
    private Map<String, Object> fetchHistoricalData(String symbol, Date fromDate, Date toDate, 
                                                         String interval, String instrumentType, 
                                                         Map<String, Object> additionalParams) {
        return investmentInstrumentService.getHistoricalData(
            symbol, fromDate, toDate, interval, instrumentType, additionalParams);
    }
    

    @Override
    public Map<String, Object> getHistoricalDataMultipleSymbols(Set<String> symbols, Date fromDate, Date toDate, 
                                                         String interval, String instrumentType, 
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
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int totalDataPoints = 0;
        
        // Process each symbol
        for (String symbol : symbols) {
            try {
                Map<String, Object> singleResult = getHistoricalData(symbol, fromDate, toDate, 
                                                           interval, instrumentType, additionalParams, false);
                if (singleResult != null && !singleResult.containsKey("error")) {
                    // Add to the results
                    symbolsData.put(symbol, singleResult);
                    successCount++;
                    
                    // Count data points
                    if (singleResult.containsKey("count")) {
                        totalDataPoints += (int) singleResult.get("count");
                    }
                } else {
                    log.warn("Failed to get historical data for symbol: {}", symbol);
                    symbolsData.put(symbol, Collections.singletonMap("error", "Failed to fetch data"));
                }
            } catch (Exception e) {
                log.error("Error processing historical data for symbol {}: {}", symbol, e.getMessage(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "Failed to fetch historical data");
                errorResult.put("message", e.getMessage());
                symbolsData.put(symbol, errorResult);
            }
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
        aggregatedResult.put("processingTimeMs", (endTime - startTime));
        
        log.info("Successfully processed historical data for {}/{} symbols with {} total data points in {}ms", 
                successCount, symbols.size(), totalDataPoints, (endTime - startTime));
        
        return aggregatedResult;
    }
    
    /**
     * Apply filtering to historical data based on filter parameters
     * 
     * @param data Original historical data response
     * @param params Parameters containing filter settings
     * @return Filtered historical data
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> applyDataFiltering(Map<String, Object> data, Map<String, Object> params) {
        String filterType = params.get("filterType").toString();
        int filterFrequency = params.containsKey("filterFrequency") ? 
                Integer.parseInt(params.get("filterFrequency").toString()) : 1;
        
        // If no filtering needed (ALL type)
        if ("ALL".equalsIgnoreCase(filterType)) {
            return data;
        }
        
        // For CUSTOM type, ensure filterFrequency is at least 2
        if ("CUSTOM".equalsIgnoreCase(filterType) && filterFrequency < 2) {
            log.warn("CUSTOM filter type specified but filterFrequency is less than 2 ({}). Using default of 2.", filterFrequency);
            filterFrequency = 2;
        }
        
        // Get the historical data points
        Map<String, Object> result = new HashMap<>(data);
        Object dataObj = data.get("data");
        
        // Handle different data types
        List<OHLCVTPoint> originalPoints = new ArrayList<>();
        String tradingSymbol = "";
        String interval = "";
        
        // Extract data from either HistoricalData object or Map
        if (dataObj instanceof HistoricalData) {
            HistoricalData historicalData = (HistoricalData) dataObj;
            originalPoints = historicalData.getDataPoints();
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
                    
                    originalPoints.add(point);
                }
            }
        } else {
            log.warn("Unexpected data type for filtering: {}", dataObj != null ? dataObj.getClass().getName() : "null");
            return data; // Return original data if unexpected type
        }
        
        if (originalPoints.isEmpty()) {
            log.warn("No data points found for filtering");
            return data; // Nothing to filter
        }
        
        // Apply filtering based on type
        List<OHLCVTPoint> filteredPoints = new ArrayList<>();
        
        if ("START_END".equalsIgnoreCase(filterType)) {
            // Only include first and last points
            filteredPoints.add(originalPoints.get(0)); // First point
            
            if (originalPoints.size() > 1) {
                filteredPoints.add(originalPoints.get(originalPoints.size() - 1)); // Last point
            }
        } else if ("CUSTOM".equalsIgnoreCase(filterType)) {
            // Include every Nth point
            for (int i = 0; i < originalPoints.size(); i += filterFrequency) {
                filteredPoints.add(originalPoints.get(i));
            }
            
            // Always include the last point if not already included
            int lastIndex = originalPoints.size() - 1;
            if (lastIndex >= 0 && lastIndex % filterFrequency != 0) {
                filteredPoints.add(originalPoints.get(lastIndex));
            }
        }
        
        // Create a new HistoricalData object with filtered points
        HistoricalData filteredData = new HistoricalData();
        filteredData.setDataPoints(filteredPoints);
        filteredData.setTradingSymbol(tradingSymbol);
        filteredData.setInterval(interval);
        
        // Update the result
        result.put("data", filteredData);
        result.put("count", filteredPoints.size());
        result.put("filtered", true);
        result.put("filterType", filterType);
        result.put("originalCount", originalPoints.size());
        
        log.debug("Applied {} filtering to historical data, reduced from {} to {} points", 
                filterType, originalPoints.size(), filteredPoints.size());
        
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
