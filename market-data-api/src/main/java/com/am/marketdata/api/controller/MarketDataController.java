package com.am.marketdata.api.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.am.marketdata.api.service.MarketDataCacheService;
import com.am.marketdata.common.model.TimeFrame;

// Removed unused import
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.am.marketdata.api.service.InvestmentInstrumentService;
import com.am.marketdata.service.MarketDataService;

/**
 * REST API controller for market data operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/market-data")
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final InvestmentInstrumentService investmentInstrumentService;
    private final MarketDataCacheService marketDataCacheService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public MarketDataController(MarketDataService marketDataService, 
                               InvestmentInstrumentService investmentInstrumentService,
                               MarketDataCacheService marketDataCacheService) {
        this.marketDataService = marketDataService;
        this.investmentInstrumentService = investmentInstrumentService;
        this.marketDataCacheService = marketDataCacheService;
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    /**
     * Get login URL for authentication
     * @return Login URL
     */
    @GetMapping("/auth/login-url")
    public ResponseEntity<Map<String, String>> getLoginUrl() {
        try {
            Map<String, String> response = marketDataService.getLoginUrl();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting login URL: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate session from request token
     * @param requestToken Request token
     * @return Session information
     */
    @PostMapping("/auth/session")
    public ResponseEntity<Object> generateSession(@RequestParam("requestToken") String requestToken) {
        try {
            Object session = marketDataService.generateSession(requestToken);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error generating session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get quotes for symbols
     * @param symbols Comma-separated list of symbols
     * @return Map of symbol to quote data with metadata
     */
    @GetMapping("/quotes")
    public ResponseEntity<Map<String, Object>> getQuotes(
            @RequestParam("symbols") String symbols,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh) {
        try {
            log.info("Controller received request for quotes for symbols: {}, forceRefresh: {}", symbols, forceRefresh);
            List<String> symbolList = Arrays.asList(symbols.split(","));
            
            // Use cache service instead of direct service call
            Map<String, Map<String, Object>> quotesMap = marketDataCacheService.getQuotes(symbolList, forceRefresh);
            
            // Check if there was an error
            if (quotesMap.containsKey("ERROR")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", quotesMap.get("ERROR").get("error"));
                errorResponse.put("message", quotesMap.get("ERROR").get("message"));
                return ResponseEntity.internalServerError().body(errorResponse);
            }
            
            // Convert to the response format expected by clients
            Map<String, Object> response = new HashMap<>();
            response.put("quotes", quotesMap);
            response.put("count", quotesMap.size());
            response.put("timestamp", new Date());
            response.put("cached", !forceRefresh);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {                                                                                                                                                                        
            log.error("Unexpected error in controller while getting quotes: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch quotes");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get OHLC data for symbols
     * @param symbols Comma-separated list of symbols
     * @param forceRefresh Whether to force a refresh from the source
     * @return Map of symbol to OHLC data with cache status
     */
    @GetMapping("/ohlc")
    public ResponseEntity<Map<String, Object>> getOHLC(
            @RequestParam("symbols") String symbols,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh) {
        try {
            log.info("Controller received request for OHLC data for symbols: {}, forceRefresh: {}", symbols, forceRefresh);
            String[] symbolArray = symbols.split(",");
            
            // Use cache service instead of direct service call
            Map<String, Object> response = marketDataCacheService.getOHLC(symbolArray, forceRefresh);
            
            // Check if there was an error
            if (response.containsKey("error")) {
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Add cache status to response if not already present
            if (!response.containsKey("cached")) {
                response.put("cached", !forceRefresh);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting OHLC: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch OHLC data");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get last traded price for symbols
     * @param symbols Comma-separated list of symbols
     * @return Map of symbol to LTP data
     */
    @GetMapping("/ltp")
    public ResponseEntity<Map<String, Object>> getLTP(@RequestParam("symbols") String symbols) {
        try {
            String[] symbolArray = symbols.split(",");
            Map<String, Object> ltp = marketDataService.getLTP(symbolArray);
            return ResponseEntity.ok(ltp);
        } catch (Exception e) {
            log.error("Error getting LTP: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get historical data for one or more instruments
     * @param symbols Trading symbols (comma-separated list)
     * @param from From date (yyyy-MM-dd)
     * @param to To date (yyyy-MM-dd)
     * @param interval Interval (minute, day, etc.)
     * @param continuous Whether to use continuous data
     * @param instrumentType Type of instrument (STOCK, OPTION, MUTUAL_FUND, etc.)
     * @param filterType Filter type for data points (ALL, START_END, CUSTOM)
     * @param filterFrequency When using CUSTOM filter, return every Nth data point
     * @param additionalParams Additional parameters
     * @return Historical data with metadata
     */
    @GetMapping("/historical-data")
    public ResponseEntity<Map<String, Object>> getHistoricalData(
            @RequestParam("symbols") String symbols,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam(value = "interval", defaultValue = "day") String interval,
            @RequestParam(value = "continuous", defaultValue = "false") boolean continuous,
            @RequestParam(value = "instrumentType", required = false) String instrumentType,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh,
            @RequestParam(value = "filterType", defaultValue = "ALL") String filterType,
            @RequestParam(value = "filterFrequency", defaultValue = "1") int filterFrequency,
            @RequestParam(required = false) Map<String, Object> additionalParams) {
        
        try {
            // Parse the symbols into a list
            List<String> symbolList = Arrays.asList(symbols.split(",")).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            if (symbolList.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "No valid symbols provided");
                errorResponse.put("message", "Please provide at least one valid symbol");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("Controller received request for historical data for symbols: {} from {} to {}, interval: {}, filterType: {}, forceRefresh: {}", 
                    symbolList, from, to, interval, filterType, forceRefresh);
            
            Date fromDate;
            Date toDate;
            try {
                fromDate = dateFormat.parse(from);
                toDate = dateFormat.parse(to);
            } catch (ParseException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid date format");
                errorResponse.put("message", "Use yyyy-MM-dd format for dates");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Add filter parameters to additionalParams
            if (additionalParams == null) {
                additionalParams = new HashMap<>();
            }
            additionalParams.put("filterType", filterType);
            additionalParams.put("filterFrequency", filterFrequency);
            
            // Use cache service instead of direct service call
            Map<String, Object> response = marketDataCacheService.getHistoricalDataMultipleSymbols(
                symbolList, fromDate, toDate, interval, instrumentType, additionalParams, forceRefresh);
            
            // Check if there was an error
            if (response.containsKey("error")) {
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Add cache status to response
            if (!response.containsKey("cached")) {
                response.put("cached", !forceRefresh);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error in controller while getting historical data: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch historical data");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get all available symbols
     * @return List of symbols
     */
    @GetMapping("/symbols")
    public ResponseEntity<Map<String, Object>> searchSymbols(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String exchange) {
        try {
            log.info("Controller received request to search symbols with page={}, size={}, symbol={}, type={}, exchange={}", 
                    page, size, symbol, type, exchange);
            
            // Delegate to the service for business logic
            Map<String, Object> response = investmentInstrumentService.searchInstruments(page, size, symbol, type, exchange);
            
            // Check if there was an error
            if (response.containsKey("error")) {
                return ResponseEntity.internalServerError().body(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error in controller while searching symbols: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to search symbols");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get symbols for a specific exchange
     * @param exchange Exchange name
     * @return List of symbols for the exchange
     */
    @GetMapping("/symbols/{exchange}")
    public ResponseEntity<List<Object>> getSymbolsForExchange(@PathVariable String exchange) {
        try {
            List<Object> symbols = marketDataService.getSymbolsForExchange(exchange);
            return ResponseEntity.ok(symbols);
        } catch (Exception e) {
            log.error("Error getting symbols for exchange: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Logout and invalidate session
     * @return Success status
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        try {
            Map<String, Object> response = marketDataService.logout();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error logging out: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get option chain data for a given underlying instrument
     * 
     * @param underlyingSymbol Symbol of the underlying instrument
     * @param expiryDate Optional expiry date (yyyy-MM-dd)
     * @return Option chain data with calls and puts
     */
    @GetMapping("/option-chain")
    public ResponseEntity<Map<String, Object>> getOptionChain(
            @RequestParam("symbol") String underlyingSymbol,
            @RequestParam(required = false) String expiryDate,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh) {
        try {
            log.info("Controller received request for option chain for symbol: {} with expiry: {}, forceRefresh: {}", 
                    underlyingSymbol, expiryDate, forceRefresh);
            
            Date expiry = null;
            if (expiryDate != null && !expiryDate.isEmpty()) {
                try {
                    expiry = dateFormat.parse(expiryDate);
                } catch (ParseException e) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid date format");
                    errorResponse.put("message", "Use yyyy-MM-dd format for expiry date");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
            
            // Use cache service instead of direct service call
            Map<String, Object> response = marketDataCacheService.getOptionChain(underlyingSymbol, expiry, forceRefresh);
            
            // Check if there was an error
            if (response.containsKey("error")) {
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Add cache status to response
            if (!response.containsKey("cached")) {
                response.put("cached", !forceRefresh);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error in controller while getting option chain: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch option chain");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get mutual fund details including NAV, returns, etc.
     * 
     * @param schemeCode Mutual fund scheme code
     * @return Mutual fund details
     */
    @GetMapping("/mutual-fund/{schemeCode}")
    public ResponseEntity<Map<String, Object>> getMutualFundDetails(
            @PathVariable String schemeCode,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh) {
        try {
            log.info("Controller received request for mutual fund details for scheme code: {}, forceRefresh: {}", 
                    schemeCode, forceRefresh);
            
            // Use cache service instead of direct service call
            Map<String, Object> response = marketDataCacheService.getMutualFundDetails(schemeCode, forceRefresh);
            
            // Check if there was an error
            if (response.containsKey("error")) {
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Add cache status to response
            if (!response.containsKey("cached")) {
                response.put("cached", !forceRefresh);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error in controller while fetching mutual fund details: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch mutual fund details");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get mutual fund NAV history
     * 
     * @param schemeCode Mutual fund scheme code
     * @param from Start date (yyyy-MM-dd)
     * @param to End date (yyyy-MM-dd)
     * @return NAV history data
     */
    @GetMapping("/mutual-fund/{schemeCode}/history")
    public ResponseEntity<Map<String, Object>> getMutualFundNavHistory(
            @PathVariable String schemeCode,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh) {
        try {
            log.info("Controller received request for mutual fund NAV history for scheme code: {} from {} to {}, forceRefresh: {}", 
                    schemeCode, from, to, forceRefresh);
            
            Date fromDate;
            Date toDate;
            try {
                fromDate = dateFormat.parse(from);
                toDate = dateFormat.parse(to);
            } catch (ParseException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid date format");
                errorResponse.put("message", "Use yyyy-MM-dd format for dates");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Use cache service instead of direct service call
            Map<String, Object> response = marketDataCacheService.getMutualFundNavHistory(schemeCode, fromDate, toDate, forceRefresh);
            
            // Check if there was an error
            if (response.containsKey("error")) {
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Add cache status to response
            if (!response.containsKey("cached")) {
                response.put("cached", !forceRefresh);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error in controller while fetching mutual fund NAV history: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch mutual fund NAV history");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get live prices for all symbols or filtered by symbol IDs
     * 
     * @param symbols Optional comma-separated list of trading symbols to filter by
     * @return Map containing prices, count, timestamp and processing time
     */
    @GetMapping("/live-prices")
    public ResponseEntity<Map<String, Object>> getLivePrices(
            @RequestParam(name = "symbols", required = false) String symbols,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh) {
        try {
            List<String> symbolList = null;
            if (symbols != null && !symbols.isEmpty()) {
                symbolList = Arrays.asList(symbols.split(","));
                log.info("Controller received request for live prices for {} symbols, forceRefresh: {}", symbolList.size(), forceRefresh);
            } else {
                log.info("Controller received request for all available symbols, forceRefresh: {}", forceRefresh);
            }
            
            // Use cache service instead of direct service call
            Map<String, Object> response = marketDataCacheService.getLivePrices(symbolList, forceRefresh);
            
            // Check if there was an error
            if (response.containsKey("error")) {
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Add cache status to response
            if (!response.containsKey("cached")) {
                response.put("cached", !forceRefresh);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error in controller while fetching live prices: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch live prices");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
