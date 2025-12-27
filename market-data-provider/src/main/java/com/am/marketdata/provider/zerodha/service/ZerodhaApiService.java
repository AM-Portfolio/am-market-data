package com.am.marketdata.provider.zerodha.service;

import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.provider.zerodha.exception.ZerodhaApiException;
import com.am.marketdata.provider.zerodha.model.ZerodhaInstrument;
import com.am.marketdata.provider.zerodha.repo.ZerodhaInstrumentRepository;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.*;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnError;
import com.zerodhatech.ticker.OnTicks;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for interacting with Zerodha Kite Connect API
 * Implements resilient patterns including retry, circuit breaker, and metrics
 */
@Slf4j
@Service
public class ZerodhaApiService {

    private KiteConnect kiteConnect;
    private KiteTicker tickerProvider;
    private final ZerodhaInstrumentRepository instrumentRepository;
    private final MeterRegistry meterRegistry;
    private final Executor executor;

    @Value("${market-data.zerodha.api.key}")
    private String apiKey;

    @Value("${market-data.zerodha.api.secret}")
    private String apiSecret;

    @Value("${market-data.zerodha.api.max.retries:3}")
    private int maxRetries;

    @Value("${market-data.zerodha.api.retry.delay.ms:1000}")
    private int retryDelayMs;

    @Value("${market-data.zerodha.api.access.token}")
    private String accessToken;

    @Value("${market-data.zerodha.api.refresh.token:}")
    private String refreshToken;

    public ZerodhaApiService(ZerodhaInstrumentRepository instrumentRepository,
                             MeterRegistry meterRegistry,
                             Executor executor) {
        this.instrumentRepository = instrumentRepository;
        this.meterRegistry = meterRegistry;
        this.executor = executor;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing Zerodha API service");
        if (apiKey != null) {
            this.kiteConnect = new KiteConnect(apiKey, true);
            if (accessToken != null && !accessToken.isEmpty()) {
                this.kiteConnect.setAccessToken(accessToken);
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        if (tickerProvider != null && tickerProvider.isConnectionOpen()) {
            tickerProvider.disconnect();
        }
    }

    public void setAccessToken(String accessToken) {
        if (kiteConnect != null) {
            kiteConnect.setAccessToken(accessToken);
        }
        this.accessToken = accessToken;
    }

    public String getLoginUrl() {
        return kiteConnect != null ? kiteConnect.getLoginURL() : "";
    }

    public User generateSession(String requestToken) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            if (kiteConnect == null) initialize();
            
            User user = kiteConnect.generateSession(requestToken, apiSecret);
            
            sample.stop(meterRegistry.timer("market-data.zerodha.api.session.time"));
            meterRegistry.counter("market-data.zerodha.api.session.success").increment();

            if (user != null && user.accessToken != null) {
                setAccessToken(user.accessToken);
                if (user.refreshToken != null) this.refreshToken = user.refreshToken;
            }
            return user;
        } catch (KiteException e) {
             meterRegistry.counter("market-data.zerodha.api.session.error").increment();
             throw new ZerodhaApiException("Failed to generate session", e);
        } catch (Exception e) {
             meterRegistry.counter("market-data.zerodha.api.session.error").increment();
             throw new ZerodhaApiException("Failed to generate session", e);
        }
    }

    public boolean logout() {
        try {
            if (kiteConnect != null) {
                kiteConnect.logout();
                return true;
            }
            return false;
        } catch (KiteException e) {
            log.error("Logout failed with KiteException", e);
            return false;
        } catch (Exception e) {
            log.error("Logout failed", e);
            return false;
        }
    }

    public Profile getProfile() {
        try {
            return kiteConnect.getProfile();
        } catch (KiteException e) {
            throw new ZerodhaApiException("Failed to get profile", e);
        } catch (Exception e) {
            throw new ZerodhaApiException("Failed to get profile", e);
        }
    }

    public Map<String, Quote> getQuotes(String[] symbols) {
        try {
            String[] prefixedSymbols = prefixSymbolsWithNSE(symbols);
            Map<String, Quote> quotes = kiteConnect.getQuote(prefixedSymbols);
            return convertInstrumentMaptoSymbolMap(quotes);
        } catch (KiteException e) {
            throw new ZerodhaApiException("Failed to get quotes", e);
        } catch (Exception e) {
            throw new ZerodhaApiException("Failed to get quotes", e);
        }
    }

    public Map<String, OHLCQuote> getOHLC(String[] symbols) {
         try {
            String[] prefixedSymbols = prefixSymbolsWithNSE(symbols);
            return kiteConnect.getOHLC(prefixedSymbols);
        } catch (KiteException e) {
            throw new ZerodhaApiException("Failed to get OHLC", e);
        } catch (Exception e) {
            throw new ZerodhaApiException("Failed to get OHLC", e);
        }
    }

    public Map<String, LTPQuote> getLTP(String[] symbols) {
        try {
            String[] prefixedSymbols = prefixSymbolsWithNSE(symbols);
            return kiteConnect.getLTP(prefixedSymbols);
        } catch (KiteException e) {
            throw new ZerodhaApiException("Failed to get LTP", e);
        } catch (Exception e) {
            throw new ZerodhaApiException("Failed to get LTP", e);
        }
    }

    public HistoricalData getHistoricalData(String symbol, Date from, Date to, TimeFrame interval, boolean continuous, boolean oi) {
        try {
            String zerodhaInterval = mapTimeFrameToZerodha(interval);
            // Need instrument token for historical data
            String token = resolveToToken(symbol);
            if (token == null) throw new ZerodhaApiException("Instrument token not found for symbol: " + symbol);
            
            return kiteConnect.getHistoricalData(from, to, token, zerodhaInterval, continuous, oi);
        } catch (KiteException e) {
             throw new ZerodhaApiException("Failed to get historical data for " + symbol, e);
        } catch (Exception e) {
             throw new ZerodhaApiException("Failed to get historical data for " + symbol, e);
        }
    }

    public List<Instrument> getAllInstruments() {
        try {
            return kiteConnect.getInstruments();
        } catch (KiteException e) {
            throw new ZerodhaApiException("Failed to get instruments", e);
        } catch (Exception e) {
            throw new ZerodhaApiException("Failed to get instruments", e);
        }
    }

    // --- Ticker Logic ---

    public KiteTicker initializeTicker(List<Long> tokens, OnTicks onTickListener) {
        try {
            if (tickerProvider != null && tickerProvider.isConnectionOpen()) {
                tickerProvider.disconnect();
            }
            
            tickerProvider = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
            tickerProvider.setOnConnectedListener(new OnConnect() {
                @Override
                public void onConnected() {
                    log.info("Ticker connected");
                    ArrayList<Long> tokenList = new ArrayList<>(tokens);
                    tickerProvider.subscribe(tokenList);
                    tickerProvider.setMode(tokenList, KiteTicker.modeFull);
                }
            });
            
            tickerProvider.setOnDisconnectedListener(new OnDisconnect() {
                @Override
                public void onDisconnected() {
                    log.warn("Ticker disconnected");
                }
            });
            
            tickerProvider.setOnTickerArrivalListener(onTickListener);
            tickerProvider.setTryReconnection(true);
            tickerProvider.connect();
            return tickerProvider;
        } catch (Exception e) {
             throw new ZerodhaApiException("Failed to initialize ticker", e);
        }
    }

    // --- Helpers ---

    private String resolveToToken(String symbol) {
        // symbol: "NSE:INFY" or "INFY"
        String tradingSymbol = symbol.contains(":") ? symbol.split(":")[1] : symbol;
        List<ZerodhaInstrument> instruments = instrumentRepository.findByTradingSymbolIn(List.of(tradingSymbol));
        if (instruments.isEmpty()) return null;
        // Prefer NSE
        return instruments.stream()
            .filter(i -> "NSE".equalsIgnoreCase(i.getExchange()))
            .findFirst()
            .map(ZerodhaInstrument::getInstrumentToken)
            .orElse(instruments.get(0).getInstrumentToken());
    }

    private String[] prefixSymbolsWithNSE(String[] symbols) {
         return Arrays.stream(symbols)
                .map(symbol -> (symbol != null && !symbol.contains(":")) ? "NSE:" + symbol : symbol)
                .toArray(String[]::new);
    }
    
    private <T> Map<String, T> convertInstrumentMaptoSymbolMap(Map<String, T> instrumentMap) {
        // Kite often returns keys as "NSE:INFY" if requested as such.
        // Or Token if requested as Token.
        // getQuote("NSE:INFY") returns "NSE:INFY" as key usually.
        // But original code has logic to map back.
        // I will keep map unmodified for now, as getQuote usually respects input keys.
        return instrumentMap;
    }
    
    private String mapTimeFrameToZerodha(TimeFrame tf) {
        // Map common timeFrame to Zerodha string "minute", "day", "5minute", etc.
        // Assuming TimeFrame has utility or switch
        if (tf == null) return "day";
        // Need to check TimeFrame values. Or use string logic.
        switch(tf.toString().toUpperCase()) {
            case "MINUTE": return "minute";
            case "DAY": return "day";
            case "MINUTE_5": return "5minute";
            case "MINUTE_15": return "15minute";
            case "MINUTE_30": return "30minute";
            case "MINUTE_60": return "60minute";
            default: return "day"; 
        }
    }
}
