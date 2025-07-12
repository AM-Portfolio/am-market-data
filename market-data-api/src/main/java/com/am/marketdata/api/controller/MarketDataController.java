package com.am.marketdata.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.marketdata.common.MarketDataProvider;
import com.marketdata.common.MarketDataProviderFactory;

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

    private final MarketDataProviderFactory providerFactory;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public MarketDataController(MarketDataProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    /**
     * Get login URL for authentication
     * @return Login URL
     */
    @GetMapping("/auth/login-url")
    public ResponseEntity<Map<String, String>> getLoginUrl() {
        try {
            MarketDataProvider provider = providerFactory.getProvider();
            String loginUrl = provider.getLoginUrl();
            
            Map<String, String> response = new HashMap<>();
            response.put("loginUrl", loginUrl);
            response.put("provider", provider.getProviderName());
            
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
            MarketDataProvider provider = providerFactory.getProvider();
            Object session = provider.generateSession(requestToken);
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
            MarketDataProvider provider = providerFactory.getProvider();
            String[] instrumentArray = instruments.split(",");
            Map<String, Object> quotes = provider.getQuotes(instrumentArray);
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
    public ResponseEntity<Map<String, Object>> getOHLC(@RequestParam("instruments") String instruments) {
        try {
            MarketDataProvider provider = providerFactory.getProvider();
            String[] instrumentArray = instruments.split(",");
            Map<String, Object> ohlc = provider.getOHLC(instrumentArray);
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
            MarketDataProvider provider = providerFactory.getProvider();
            String[] instrumentArray = instruments.split(",");
            Map<String, Object> ltp = provider.getLTP(instrumentArray);
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
    public ResponseEntity<Object> getHistoricalData(
            @RequestParam("instrumentId") String instrumentId,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("interval") String interval,
            @RequestParam(name = "continuous") boolean continuous,
            @RequestParam(required = false) Map<String, Object> additionalParams) {
        
        try {
            MarketDataProvider provider = providerFactory.getProvider();
            Date fromDate = dateFormat.parse(from);
            Date toDate = dateFormat.parse(to);
            
            Object historicalData = provider.getHistoricalData(
                    instrumentId, fromDate, toDate, interval, continuous, additionalParams);
            
            return ResponseEntity.ok(historicalData);
        } catch (ParseException e) {
            log.error("Invalid date format: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Use yyyy-MM-dd"));
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
    public ResponseEntity<List<Object>> getAllInstruments() {
        try {
            MarketDataProvider provider = providerFactory.getProvider();
            List<Object> instruments = provider.getAllInstruments();
            return ResponseEntity.ok(instruments);
        } catch (Exception e) {
            log.error("Error getting all instruments: {}", e.getMessage(), e);
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
            MarketDataProvider provider = providerFactory.getProvider();
            List<Object> instruments = provider.getInstrumentsForExchange(exchange);
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
            MarketDataProvider provider = providerFactory.getProvider();
            boolean success = provider.logout();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("provider", provider.getProviderName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error logging out: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
