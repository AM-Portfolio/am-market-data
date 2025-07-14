package com.am.marketdata.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.Instrument;
import com.am.common.investment.model.historical.HistoricalData;
import com.am.marketdata.service.MarketDataService;
import com.zerodhatech.models.OHLCQuote;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * REST API controller for market data operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/market-data")
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
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
     * Get quotes for instruments
     * @param instruments Comma-separated list of instruments
     * @return Map of instrument to quote data
     */
    @GetMapping("/quotes")
    public ResponseEntity<Map<String, Object>> getQuotes(@RequestParam String instruments) {
        try {
            String[] instrumentArray = instruments.split(",");
            Map<String, Object> quotes = marketDataService.getQuotes(instrumentArray);
            return ResponseEntity.ok(quotes);
        } catch (Exception e) {
            log.error("Error getting quotes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get OHLC data for instruments
     * @param instruments Comma-separated list of instruments
     * @return Map of instrument to OHLC data
     */
    @GetMapping("/ohlc")
    public ResponseEntity<Map<String, OHLCQuote>> getOHLC(@RequestParam("instruments") String instruments) {
        try {
            String[] instrumentArray = instruments.split(",");
            Map<String, OHLCQuote> ohlc = marketDataService.getOHLC(instrumentArray);
            return ResponseEntity.ok(ohlc);
        } catch (Exception e) {
            log.error("Error getting OHLC: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get last traded price for instruments
     * @param instruments Comma-separated list of instruments
     * @return Map of instrument to LTP data
     */
    @GetMapping("/ltp")
    public ResponseEntity<Map<String, Object>> getLTP(@RequestParam("instruments") String instruments) {
        try {
            String[] instrumentArray = instruments.split(",");
            Map<String, Object> ltp = marketDataService.getLTP(instrumentArray);
            return ResponseEntity.ok(ltp);
        } catch (Exception e) {
            log.error("Error getting LTP: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get historical data for an instrument
     * @param instrumentId Instrument identifier
     * @param from From date (yyyy-MM-dd)
     * @param to To date (yyyy-MM-dd)
     * @param interval Interval (minute, day, etc.)
     * @param continuous Continuous flag
     * @return Historical data
     */
    @GetMapping("/historical")
    public ResponseEntity<HistoricalData> getHistoricalData(
            @RequestParam("instrumentId") String instrumentId,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("interval") String interval,
            @RequestParam(name = "continuous") boolean continuous,
            @RequestParam(required = false) Map<String, Object> additionalParams) {
        
        try {
            Date fromDate = dateFormat.parse(from);
            Date toDate = dateFormat.parse(to);
            
            HistoricalData historicalData = marketDataService.getHistoricalData(
                    instrumentId, fromDate, toDate, interval, continuous, additionalParams);
            
            return ResponseEntity.ok(historicalData);
        } catch (Exception e) {
            log.error("Error getting historical data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all available instruments
     * @return List of instruments
     */
    @GetMapping("/instruments")
    public ResponseEntity<List<Instrument>> getAllInstruments() {
        try {
            List<Instrument> instruments = marketDataService.getAllInstruments();
            return ResponseEntity.ok(instruments);
        } catch (Exception e) {
            log.error("Error getting all instruments: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get paginated and filtered instruments
     * 
     * @param page Page number (0-based)
     * @param size Number of records per page (default 10)
     * @param symbol Filter by trading symbol (optional)
     * @param type Filter by instrument type (optional)
     * @param exchange Filter by exchange (optional)
     * @return Filtered and paginated list of instruments
     */
    @GetMapping("/instruments/search")
    public ResponseEntity<Map<String, Object>> searchInstruments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String exchange) {
        try {
            List<Instrument> instruments = marketDataService.getInstrumentPagination(page, size, symbol, type, exchange);
            
            // Get total count for pagination metadata
            List<Instrument> allInstruments = marketDataService.getAllInstruments();
            long totalCount = allInstruments.stream()
                .filter(instrument -> symbol == null || symbol.isEmpty() || 
                    instrument.getTradingSymbol().toLowerCase().contains(symbol.toLowerCase()))
                .filter(instrument -> type == null || type.isEmpty() || 
                    (instrument.getInstrumentType() != null && 
                     instrument.getInstrumentType().toString().equalsIgnoreCase(type)))
                .filter(instrument -> exchange == null || exchange.isEmpty() || 
                    (instrument.getSegment() != null && 
                     instrument.getSegment().toString().equalsIgnoreCase(exchange)))
                .count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("instruments", instruments);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalItems", totalCount);
            response.put("totalPages", Math.ceil((double) totalCount / size));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting paginated instruments: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get instruments for a specific exchange
     * @param exchange Exchange name
     * @return List of instruments for the exchange
     */
    @GetMapping("/instruments/{exchange}")
    public ResponseEntity<List<Object>> getInstrumentsForExchange(@PathVariable String exchange) {
        try {
            List<Object> instruments = marketDataService.getInstrumentsForExchange(exchange);
            return ResponseEntity.ok(instruments);
        } catch (Exception e) {
            log.error("Error getting instruments for exchange: {}", e.getMessage(), e);
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
     * Get live prices for all instruments or filtered by instrument IDs
     * 
     * @param instrumentIds Optional comma-separated list of instrument IDs to filter by
     * @return List of equity prices with current market data
     */
    @GetMapping("/live-prices")
    public ResponseEntity<Map<String, Object>> getLivePrices(
            @RequestParam(name = "symbols", required = false) String symbols) {
        try {
            List<String> idList = null;
            if (symbols != null && !symbols.isEmpty()) {
                idList = Arrays.asList(symbols.split(","));
                log.info("Fetching live prices for {} instruments", idList.size());
            } else {
                log.info("Fetching live prices for all available instruments");
            }
            
            long startTime = System.currentTimeMillis();
            List<EquityPrice> prices = marketDataService.getLivePrices(idList);
            long endTime = System.currentTimeMillis();
            
            Map<String, Object> response = new HashMap<>();
            response.put("prices", prices);
            response.put("count", prices.size());
            response.put("timestamp", new Date());
            response.put("processingTimeMs", (endTime - startTime));
            
            log.info("Successfully fetched {} live prices in {}ms", prices.size(), (endTime - startTime));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching live prices: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch live prices");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
