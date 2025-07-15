package com.marketdata.service.zerodha;

import com.am.common.investment.service.instrument.InstrumentService;
import com.marketdata.common.MarketDataProvider;
import com.zerodhatech.models.*;
import com.zerodhatech.ticker.OnTicks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Zerodha implementation of the MarketDataProvider interface
 */
@Slf4j
@Service("zerodhaMarketDataProvider")
public class ZerodhaMarketDataProvider implements MarketDataProvider {

    private final ZerodhaApiService zerodhaApiService;

    public ZerodhaMarketDataProvider(ZerodhaApiService zerodhaApiService) {
        this.zerodhaApiService = zerodhaApiService;
        log.info("Initialized Zerodha market data provider");
    }

    @PostConstruct
    @Override
    public void initialize() {
        log.info("Initializing Zerodha market data provider");
        zerodhaApiService.initialize();
    }

    @PreDestroy
    @Override
    public void cleanup() {
        log.info("Cleaning up Zerodha market data provider");
        zerodhaApiService.cleanup();
    }

    @Override
    public void setAccessToken(String accessToken) {
        zerodhaApiService.setAccessToken(accessToken);
    }

    @Override
    public String getLoginUrl() {
        return zerodhaApiService.getLoginUrl();
    }

    @Override
    public Object generateSession(String requestToken) {
        return zerodhaApiService.generateSession(requestToken);
    }

    
    @Override
    public Map<String, Object> getQuotes(String[] symbols) {
        try {
            Map<String, Quote> quotes = zerodhaApiService.getQuotes(symbols);
            return new HashMap<>(quotes);
        } catch (Exception e) {
            log.error("Error getting quotes from Zerodha: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, OHLCQuote> getOHLC(String[] symbols) {
        try {
            Map<String, OHLCQuote> ohlc = zerodhaApiService.getOHLC(symbols);
            return new HashMap<>(ohlc);
        } catch (Exception e) {
            log.error("Error getting OHLC from Zerodha: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getLTP(String[] symbols) {
        try {
            Map<String, LTPQuote> ltp = zerodhaApiService.getLTP(symbols);
            return new HashMap<>(ltp);
        } catch (Exception e) {
            log.error("Error getting LTP from Zerodha: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public HistoricalData getHistoricalData(String symbol, Date from, Date to, String interval, 
                                   boolean continuous, Map<String, Object> additionalParams) {
        boolean oi = additionalParams != null && additionalParams.containsKey("oi") ? 
                    (Boolean) additionalParams.get("oi") : false;
        return zerodhaApiService.getHistoricalData(symbol, from, to, interval, continuous, oi);
    }

    @Override
    public Object initializeTicker(List<String> instrumentIds, Object tickListener) {
        // Convert string instrument IDs to longs for Zerodha
        List<Long> tokens = instrumentIds.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        return zerodhaApiService.initializeTicker(tokens, (OnTicks) tickListener);
    }

    @Override
    public boolean isTickerConnected() {
        return zerodhaApiService.isTickerConnected();
    }

    @Override
    public List<Instrument> getAllInstruments() {
        try {
            List<Instrument> instruments = zerodhaApiService.getAllInstruments();
            return new ArrayList<>(instruments);
        } catch (Exception e) {
            log.error("Error getting all instruments from Zerodha: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Object> getSymbolsForExchange(String exchange) {
        try {
            List<Instrument> instruments = zerodhaApiService.getInstrumentsForExchange(exchange);
            return new ArrayList<>(instruments);
        } catch (Exception e) {
            log.error("Error getting instruments for exchange from Zerodha: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(ProviderOperation<T> operation) {
        return zerodhaApiService.executeAsync(() -> {
            try {
                return operation.execute();
            } catch (Exception e) {
                throw new ZerodhaApiException("Error executing async operation", e);
            }
        });
    }

    @Override
    public boolean logout() {
        return zerodhaApiService.logout();
    }

    @Override
    public String getProviderName() {
        return "zerodha";
    }
}
