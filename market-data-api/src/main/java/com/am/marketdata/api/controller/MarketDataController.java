package com.am.marketdata.api.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.am.marketdata.api.dto.HistoricalDataRequest;
import com.am.marketdata.api.model.OHLCRequest;
import com.am.marketdata.api.model.QuotesRequest;
import com.am.marketdata.api.service.InvestmentInstrumentService;
import com.am.marketdata.api.service.MarketDataFetchService;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.service.MarketDataService;
import com.am.common.investment.model.historical.OHLCVTPoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.am.marketdata.common.log.AppLogger;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller for market data operations
 * Provides endpoints for fetching various types of market data including
 * quotes, OHLC, historical data,
 * option chains, mutual fund details, and more
 */
@RestController
@RequestMapping("/api/v1/market-data")
@Tag(name = "Market Data", description = "APIs for retrieving various types of market data including quotes, historical data, option chains, and more")
public class MarketDataController {

    private final AppLogger log = AppLogger.getLogger(MarketDataController.class);
    private final MarketDataService marketDataService;
    private final InvestmentInstrumentService investmentInstrumentService;
    private final MarketDataFetchService marketDataCacheService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public MarketDataController(MarketDataService marketDataService,
            InvestmentInstrumentService investmentInstrumentService,
            MarketDataFetchService marketDataCacheService) {
        this.marketDataService = marketDataService;
        this.investmentInstrumentService = investmentInstrumentService;
        this.marketDataCacheService = marketDataCacheService;
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    /**
     * Get login URL for authentication
     * 
     * @return Login URL for broker authentication
     */
    @GetMapping(value = "/auth/login-url", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get login URL for broker authentication", description = "Returns a URL that can be used to authenticate with the broker's login page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login URL generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, String>> getLoginUrl(
            @RequestParam(required = false) String provider) {
        try {
            Map<String, String> response = marketDataService.getLoginUrl(provider);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("getLoginUrl", "Error getting login URL", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate session from request token
     * 
     * @param requestToken    Request token from broker authentication
     * @param requestTokenAlt Alternative request token parameter name
     * @param status          Authentication status
     * @return Session information
     */
    @GetMapping(value = "/auth/session", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generate session from request token", description = "Creates a new authenticated session using the request token obtained from broker login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request token or authentication failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Object> generateSession(
            @RequestParam(value = "request_token", required = false) String requestToken,
            @RequestParam(value = "requestToken", required = false) String requestTokenAlt,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "status", required = false, defaultValue = "success") String status) {
        try {
            // Check status parameter - only proceed if it's "success" or not provided
            if (!"success".equalsIgnoreCase(status)) {
                log.error("generateSession", "Authentication failed with status: " + status);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Authentication failed");
                errorResponse.put("message", "Login was not successful. Status: " + status);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Use request_token if provided, otherwise fall back to requestToken
            String token = requestToken != null ? requestToken : requestTokenAlt;

            if (code != null) {
                token = code;
            }
            if (token == null) {
                log.error("generateSession",
                        "No request token provided in either request_token or requestToken parameters");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing request token");
                errorResponse.put("message", "No request token provided");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("generateSession", "Generating session with token: " + token);
            Object session = marketDataService.generateSession(token);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("generateSession", "Error generating session: " + e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get quotes for symbols with timeframe support
     * 
     * @param symbols      Comma-separated list of symbols
     * @param timeFrameStr The timeframe for quotes (e.g., 5m, 15m, 1H, 1D)
     * @param forceRefresh Whether to force refresh from provider
     * @return Map of symbol to quote data with metadata
     */
    @GetMapping(value = "/quotes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get quotes for multiple symbols", description = "Retrieves latest quotes for multiple symbols with support for different timeframes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotes retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getQuotes(
            @RequestParam("symbols") String symbols,
            @RequestParam(name = "timeFrame", defaultValue = "5m") String timeFrameStr,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh) {
        try {
            log.info("getQuotes",
                    String.format(
                            "Controller received request for quotes for symbols: %s, timeFrame: %s, forceRefresh: %s",
                            symbols, timeFrameStr, forceRefresh));

            // Parse symbols and timeframe
            Set<String> symbolList = parseSymbols(symbols);
            TimeFrame timeFrame = TimeFrame.fromApiValue(timeFrameStr);

            // Use cache service instead of direct service call
            Map<String, Object> quotesResponse = marketDataCacheService.getQuotes(symbolList, false, timeFrame,
                    forceRefresh);

            // Check if there was an error
            if (quotesResponse.containsKey("ERROR")) {
                Map<String, Object> errorResponse = new HashMap<>();
                @SuppressWarnings("unchecked")
                Map<String, Object> errorDetails = (Map<String, Object>) quotesResponse.get("ERROR");
                errorResponse.put("error", errorDetails.get("error"));
                errorResponse.put("message", errorDetails.get("message"));
                return ResponseEntity.internalServerError().body(errorResponse);
            }

            return ResponseEntity.ok(quotesResponse);
        } catch (IllegalArgumentException e) {
            // Handle invalid timeframe
            log.error("getQuotes", "Invalid timeFrame parameter: " + timeFrameStr, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "INVALID_PARAMETER");
            errorResponse.put("message", "Invalid timeFrame parameter: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("getQuotes", "Error processing quotes request: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get quotes for symbols with timeframe support (POST version)
     * 
     * @param request The quotes request containing symbols and timeframe
     * @return Map of symbol to quote data with metadata
     */
    @PostMapping(value = "/quotes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get quotes for multiple symbols (POST)", description = "Retrieves latest quotes for multiple symbols with support for different timeframes using POST request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotes retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getQuotesPost(@RequestBody QuotesRequest request) {
        try {
            log.info("getQuotesPost", String.format(
                    "Controller received POST request for quotes for symbols: %s, timeFrame: %s, forceRefresh: %s",
                    request.getSymbols(), request.getTimeFrame(), request.isForceRefresh()));

            // Parse symbols
            Set<String> symbolList = parseSymbols(request.getSymbols());

            // Use cache service instead of direct service call
            Map<String, Object> quotesResponse = marketDataCacheService.getQuotes(
                    symbolList, request.isIndexSymbol(), TimeFrame.fromApiValue(request.getTimeFrame()),
                    request.isForceRefresh());

            // Check if there was an error
            if (quotesResponse.containsKey("ERROR")) {
                Map<String, Object> errorResponse = new HashMap<>();
                @SuppressWarnings("unchecked")
                Map<String, Object> errorDetails = (Map<String, Object>) quotesResponse.get("ERROR");
                errorResponse.put("error", errorDetails.get("error"));
                errorResponse.put("message", errorDetails.get("message"));
                return ResponseEntity.internalServerError().body(errorResponse);
            }

            return ResponseEntity.ok(quotesResponse);
        } catch (Exception e) {
            log.error("getQuotesPost", "Error processing quotes request: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get OHLC data for symbols
     * 
     * @param request Request body containing symbols and options
     * @return Map of symbol to OHLC data with cache status
     */
    @PostMapping(value = "/ohlc", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get OHLC data for multiple symbols", description = "Retrieves Open-High-Low-Close data for multiple symbols with support for different timeframes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OHLC data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getOHLC(@RequestBody OHLCRequest request) {
        try {
            log.info("getOHLC",
                    String.format(
                            "Controller received POST request for OHLC data for symbols: %s, timeFrame: %s, forceRefresh: %s, indexSymbol: %s",
                            request.getSymbols(), request.getTimeFrame(), request.isForceRefresh(),
                            request.isIndexSymbol()));
            Set<String> symbolList = parseSymbols(request.getSymbols());

            // Use cache service instead of direct service call
            Map<String, OHLCQuote> response = marketDataCacheService.getOHLC(
                    symbolList, request.isIndexSymbol(), TimeFrame.fromApiValue(request.getTimeFrame()),
                    request.isForceRefresh());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("getOHLC", "Error getting OHLC: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch OHLC data");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get historical data for one or more instruments
     * 
     * @param request Request body containing symbols, date range, and other
     *                parameters
     * @return Historical data with metadata
     */
    @PostMapping(value = "/historical-data", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get historical market data", description = "Retrieves historical price and volume data for one or more instruments with filtering options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historical data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getHistoricalData(@RequestBody HistoricalDataRequest request) {
        log.info("getHistoricalData",
                String.format(
                        "Controller received POST request for historical data for symbols: %s from %s to %s, interval: %s, filterType: %s, forceRefresh: %s",
                        request.getSymbols(), request.getFrom(), request.getTo(),
                        TimeFrame.fromApiValue(request.getInterval()),
                        request.getFilterType(), request.isForceRefresh()));

        try {
            // Delegate all processing to the service
            Map<String, Object> response = marketDataCacheService.processHistoricalDataRequest(request);

            // Check if there was an error
            if (response.containsKey("error")) {
                // Determine if it's a client error or server error
                String errorType = response.get("error").toString();
                if (errorType.contains("No valid symbols") || errorType.contains("Invalid date format")) {
                    return ResponseEntity.badRequest().body(response);
                } else {
                    return ResponseEntity.internalServerError().body(response);
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("getHistoricalData",
                    "Unexpected error in controller while getting historical data: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch historical data");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get historical charts data (1Y Daily or 5Y Monthly)
     * 
     * @param symbol Symbol to fetch data for
     * @param range  Range (1Y or 5Y)
     * @return Historical data
     */
    @GetMapping(value = "/historical-charts/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get historical charts data", description = "Retrieves historical data for charts with various time frames (10m, 1H, 1D, 1W, 1M, 5Y, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chart data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getHistoricalCharts(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1D") String range) {
        try {
            log.info("Fetching historical charts for symbol: {}, range: {}", symbol, range);

            String interval = "1D";
            java.time.LocalDateTime to = java.time.LocalDateTime.now();
            java.time.LocalDateTime from = to.minusDays(1);
            boolean isIntraday = false;

            // Determine Interval and From Time based on Range
            switch (range.toUpperCase()) {
                case "10M": // 10 Minutes
                    interval = "1m";
                    from = to.minusMinutes(10);
                    isIntraday = true;
                    break;
                case "15M": // 15 Minutes
                    interval = "1m";
                    from = to.minusMinutes(15);
                    isIntraday = true;
                    break;
                case "30M": // 30 Minutes
                    interval = "1m";
                    from = to.minusMinutes(30);
                    isIntraday = true;
                    break;
                case "1H": // 1 Hour
                    interval = "1m";
                    from = to.minusHours(1);
                    isIntraday = true;
                    break;
                case "4H": // 4 Hours
                    interval = "5m"; // 5min interval for 4 hour chart
                    from = to.minusHours(4);
                    isIntraday = true;
                    break;
                case "1D": // 1 Day
                    interval = "5m"; // 5min interval for daily chart (standard)
                    from = to.minusDays(1); // Or start of day? usually 24h rolling or market open
                    break;
                case "1W": // 1 Week
                    interval = "1H"; // Hourly
                    from = to.minusWeeks(1);
                    break;
                case "1M": // 1 Month
                    interval = "1D";
                    from = to.minusMonths(1);
                    break;
                case "5Y": // 5 Years
                    interval = "1W"; // Weekly (or Monthly?)
                    from = to.minusYears(5);
                    break;
                default:
                    // Fallback to 1Y Daily
                    interval = "1D";
                    from = to.minusYears(1);
            }

            // Construct Request - Date format yyyy-MM-dd is standard for APIs even for
            // intraday usually
            // but we might need to filter manually if the API gives us full days.
            HistoricalDataRequest request = HistoricalDataRequest.builder()
                    .symbols(symbol)
                    .from(from.toLocalDate().toString()) // API typically takes Date Only
                    .to(to.toLocalDate().toString())
                    .interval(interval)
                    .filterType("price")
                    .build();

            // Fetch Data
            Map<String, Object> response = marketDataCacheService.processHistoricalDataRequest(request);

            if (response.containsKey("error")) {
                return ResponseEntity.status(500).body(response);
            }

            // FILTERING Logic
            // If Intraday or specifc logic, we filter the dataPoints to ensure they are >=
            // from time
            if (response.containsKey("data")) {
                Object dataObj = response.get("data");
                // Structure: { "SYMBOL": { "dataPoints": [ [time, o, h, l, c, v], ... ] } }
                // OR { "SYMBOL": [ ... ] } depending on service implementation.
                // Based on previous conversations, it's nested: data -> SYMBOL -> dataPoints
                // list.

                if (dataObj instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                    if (dataMap.containsKey(symbol)) {
                        Object symbolDataObj = dataMap.get(symbol);
                        if (symbolDataObj instanceof Map) {
                            Map<String, Object> innerData = (Map<String, Object>) symbolDataObj;
                            if (innerData.containsKey("dataPoints")) {
                                Object pointsObj = innerData.get("dataPoints");
                                if (pointsObj instanceof List) {
                                    List<?> points = (List<?>) pointsObj;
                                    long minTime = from.atZone(java.time.ZoneId.of("Asia/Kolkata")).toInstant()
                                            .toEpochMilli();

                                    List<Object> filteredPoints = points.stream()
                                            .filter(p -> {
                                                try {
                                                    long timestamp = 0;
                                                    if (p instanceof OHLCVTPoint) {
                                                        timestamp = ((OHLCVTPoint) p).getTime()
                                                                .atZone(java.time.ZoneId.systemDefault()).toInstant()
                                                                .toEpochMilli();
                                                    } else if (p instanceof Map) {
                                                        Object t = ((Map<?, ?>) p).get("time"); // or timestamp
                                                        // ... parsing logic if needed
                                                        return true; // Skip complex map parsing for now
                                                    } else if (p instanceof List) {
                                                        Object t = ((List<?>) p).get(0);
                                                        if (t instanceof Number)
                                                            timestamp = ((Number) t).longValue();
                                                    }
                                                    return timestamp >= minTime;
                                                } catch (Exception e) {
                                                    return true;
                                                }
                                            })
                                            .collect(Collectors.toList());

                                    innerData.put("dataPoints", filteredPoints);
                                }
                            }
                        }
                    }
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("getHistoricalCharts", "Error fetching historical charts for " + symbol + ": " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch chart data");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get all available symbols with pagination and filtering
     * 
     * @param page     Page number for pagination
     * @param size     Page size for pagination
     * @param symbol   Symbol filter
     * @param type     Instrument type filter
     * @param exchange Exchange filter
     * @return List of symbols with pagination metadata
     */
    @GetMapping(value = "/symbols", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search for available trading symbols", description = "Search for available trading symbols with pagination and filtering options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Symbols retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> searchSymbols(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String exchange) {
        try {
            log.info("searchSymbols", String.format(
                    "Controller received request to search symbols with page=%d, size=%d, symbol=%s, type=%s, exchange=%s",
                    page, size, symbol, type, exchange));

            Map<String, Object> response = investmentInstrumentService.searchInstruments(page, size, symbol, type,
                    exchange);

            // Check if there was an error
            if (response.containsKey("error")) {
                return ResponseEntity.internalServerError().body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("searchSymbols", "Unexpected error in controller while searching symbols: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to search symbols");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get symbols for a specific exchange
     * 
     * @param exchange Exchange name
     * @return List of symbols for the exchange
     */
    @GetMapping(value = "/symbols/{exchange}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get symbols for a specific exchange", description = "Retrieves all available trading symbols for a specific exchange")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Symbols retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Object>> getSymbolsForExchange(
            @PathVariable String exchange) {
        try {
            List<Object> symbols = marketDataService.getSymbolsForExchange(exchange, null);
            return ResponseEntity.ok(symbols);
        } catch (Exception e) {
            log.error("getSymbolsForExchange", "Error getting symbols for exchange: " + e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Logout and invalidate session
     * 
     * @return Success status
     */
    @PostMapping(value = "/auth/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Logout and invalidate session", description = "Invalidates the current broker session and clears authentication tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> logout() {
        try {
            Map<String, Object> response = marketDataService.logout(null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("logout", "Error logging out", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get option chain data for a given underlying instrument
     * 
     * @param underlyingSymbol Symbol of the underlying instrument
     * @param expiryDate       Optional expiry date (yyyy-MM-dd)
     * @param forceRefresh     Whether to force refresh from provider
     * @return Option chain data with calls and puts
     */
    @GetMapping(value = "/option-chain", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get option chain data", description = "Retrieves option chain data including calls and puts for a given underlying instrument")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Option chain data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
            Map<String, Object> response = marketDataCacheService.getOptionChain(underlyingSymbol, expiry,
                    forceRefresh);

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
            log.error("getOptionChain", "Unexpected error in controller while getting option chain: " + e.getMessage(),
                    e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch option chain");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get mutual fund details including NAV, returns, etc.
     * 
     * @param schemeCode   Mutual fund scheme code
     * @param forceRefresh Whether to force refresh from provider
     * @return Mutual fund details
     */
    @GetMapping(value = "/mutual-fund/{schemeCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get mutual fund details", description = "Retrieves detailed information about a mutual fund including NAV, returns, and other metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mutual fund details retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
            log.error("getMutualFundDetails",
                    "Unexpected error in controller while fetching mutual fund details: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch mutual fund details");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get mutual fund NAV history
     * 
     * @param schemeCode   Mutual fund scheme code
     * @param from         Start date (yyyy-MM-dd)
     * @param to           End date (yyyy-MM-dd)
     * @param forceRefresh Whether to force refresh from provider
     * @return NAV history data
     */
    @GetMapping(value = "/mutual-fund/{schemeCode}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get mutual fund NAV history", description = "Retrieves historical Net Asset Value (NAV) data for a mutual fund over a specified date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "NAV history retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format or request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getMutualFundNavHistory(
            @PathVariable String schemeCode,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh) {
        try {
            log.info(
                    "Controller received request for mutual fund NAV history for scheme code: {} from {} to {}, forceRefresh: {}",
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
            Map<String, Object> response = marketDataCacheService.getMutualFundNavHistory(schemeCode, fromDate, toDate,
                    forceRefresh);

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
            log.error("getMutualFundNavHistory",
                    "Unexpected error in controller while fetching mutual fund NAV history: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch mutual fund NAV history");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get live prices for all symbols or filtered by symbol IDs
     * 
     * @param symbols      Optional comma-separated list of trading symbols to
     *                     filter by
     * @param indexSymbol  Whether the symbols are index symbols
     * @param forceRefresh Whether to force refresh from provider
     * @return Map containing prices, count, timestamp and processing time
     */
    @GetMapping(value = "/live-prices", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get live market prices", description = "Retrieves real-time market prices for specified symbols or all available symbols")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Live prices retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getLivePrices(
            @RequestParam(name = "symbols", required = false) String symbols,
            @RequestParam(name = "isIndexSymbol", required = false) boolean indexSymbol,
            @RequestParam(name = "refresh", defaultValue = "false") boolean forceRefresh) {
        try {
            Set<String> symbolList = parseSymbols(symbols);

            // Use cache service instead of direct service call
            Map<String, Object> response = marketDataCacheService.getLivePrices(symbolList, indexSymbol, forceRefresh);

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
            log.error("getLivePrices", "Unexpected error in controller while fetching live prices: " + e.getMessage(),
                    e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch live prices");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Utility method to convert comma-separated string to Set of symbols
     * 
     * @param symbols Comma-separated string of symbols
     * @return Set of trimmed symbols, or empty set if input is null/empty
     */
    private Set<String> parseSymbols(String symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
