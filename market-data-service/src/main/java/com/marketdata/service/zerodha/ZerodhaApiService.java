package com.marketdata.service.zerodha;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.*;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnTicks;
import com.zerodhatech.ticker.OnError;
import com.zerodhatech.ticker.OnOrderUpdate;

import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Service for interacting with Zerodha Kite Connect API
 * Implements resilient patterns including retry, circuit breaker, and metrics
 */
@Slf4j
@Service
public class ZerodhaApiService {

    private KiteConnect kiteConnect;
    private KiteTicker tickerProvider;
    private final MeterRegistry meterRegistry;
    private final ThreadPoolExecutor threadPoolExecutor;
    
    @Value("${market-data.zerodha.api.key}")
    private String apiKey;
    
    @Value("${market-data.zerodha.api.secret}")
    private String apiSecret;
    
    @Value("${market-data.zerodha.api.max.retries:3}")
    private int maxRetries;
    
    @Value("${market-data.zerodha.api.retry.delay.ms:1000}")
    private int retryDelayMs;
    
    @Value("${market-data.zerodha.ticker.reconnect.max.retries:10}")
    private int tickerMaxRetries;
    
    @Value("${market-data.zerodha.ticker.reconnect.interval:30}")
    private int tickerReconnectInterval;

    @Value("${market-data.zerodha.api.access.token}")
    private String accessToken;
    
    @Value("${market-data.zerodha.api.refresh.token:}")
    private String refreshToken;

    public ZerodhaApiService(MeterRegistry meterRegistry, ThreadPoolExecutor threadPoolExecutor) {
        this.meterRegistry = meterRegistry;
        this.threadPoolExecutor = threadPoolExecutor;
        initialize();
        log.info("Initializing Zerodha API service");
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing Zerodha API service with API key: {}", apiKey);
        
        // Re-initialize KiteConnect with injected properties
        this.kiteConnect = new KiteConnect(apiKey, true);
        
        // Set access token if available
        if (accessToken != null && !accessToken.isEmpty()) {
            log.info("Setting access token: {}", accessToken.substring(0, Math.min(5, accessToken.length())) + "...");
            this.kiteConnect.setAccessToken(accessToken);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (tickerProvider != null && tickerProvider.isConnectionOpen()) {
            log.info("Disconnecting Zerodha ticker");
            tickerProvider.disconnect();
        }
        log.info("Cleaned up Zerodha API service resources");
    }

    /**
     * Sets the access token for API authentication
     * @param accessToken The access token from Zerodha
     */
    public void setAccessToken(String accessToken) {
        kiteConnect.setAccessToken(accessToken);
        this.accessToken = accessToken;
        log.info("Set Zerodha access token: {}", maskToken(accessToken));
    }

    /**
     * Generate session URL for user login
     * @return Login URL for Zerodha authentication
     */
    public String getLoginUrl() {
        return kiteConnect.getLoginURL();
    }

    /**
     * Generate access token from request token
     * @param requestToken Request token received after login
     * @return User object containing access token
     */
    //@Retry(name = "marketDataZerodhaApi")
    public User generateSession(String requestToken) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Check if KiteConnect is properly initialized
            if (kiteConnect == null) {
                log.info("KiteConnect is null, initializing it now");
                initializeKiteConnect();
            }
            
            // First try to refresh the token if we have a refresh token
            // if (refreshToken != null && !refreshToken.isEmpty()) {
            //     try {
            //         log.info("Attempting to renew access token using refresh token");
                    
            //         // Validate API key and secret
            //         if (apiKey == null || apiKey.isEmpty()) {
            //             throw new IllegalStateException("API key is not configured");
            //         }
                    
            //         if (apiSecret == null || apiSecret.isEmpty()) {
            //             throw new IllegalStateException("API secret is not configured");
            //         }
                    
            //         // Try to renew the token
            //         TokenSet tokenSet = kiteConnect.renewAccessToken(refreshToken, apiSecret);
                    
            //         if (tokenSet != null && tokenSet.accessToken != null) {
            //             log.info("Successfully renewed access token: {}", 
            //                     tokenSet.accessToken.substring(0, Math.min(5, tokenSet.accessToken.length())) + "...");
                        
            //             // Save the new tokens
            //             setAccessToken(tokenSet.accessToken);
            //             this.refreshToken = tokenSet.refreshToken;
                        
            //             // Record metrics
            //             sample.stop(meterRegistry.timer("market-data.zerodha.api.token.refresh.time"));
            //             meterRegistry.counter("market-data.zerodha.api.token.refresh.success").increment();
                        
            //             // Create a User object to return
            //             User user = new User();
            //             user.accessToken = tokenSet.accessToken;
            //             user.refreshToken = tokenSet.refreshToken;
            //             return user;
            //         }
            //     } catch (Exception e) {
            //         log.warn("Failed to renew access token using refresh token: {}", e.getMessage());
            //         meterRegistry.counter("market-data.zerodha.api.token.refresh.failure").increment();
            //         // Continue to regular session generation
            //     }
            // }
            
            // If refresh token is not available or refresh failed, generate a new session
            // Validate parameters
            if (requestToken == null || requestToken.isEmpty()) {
                throw new IllegalArgumentException("Request token cannot be null or empty");
            }
            
            log.info("Generating Zerodha session with request token: {}", requestToken);
            log.info("Using API key: {}, API secret length: {}", apiKey, apiSecret != null ? apiSecret.length() : 0);
            
            // Validate API key and secret
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("API key is not configured");
            }
            
            if (apiSecret == null || apiSecret.isEmpty()) {
                throw new IllegalStateException("API secret is not configured");
            }
            
            // Debug the KiteConnect instance
            log.debug("KiteConnect instance: {}", kiteConnect);
            
            // Generate session with detailed logging
            log.info("Calling kiteConnect.generateSession with requestToken length: {}, apiSecret length: {}", 
                    requestToken.length(), apiSecret.length());
            
            User user = kiteConnect.generateSession(requestToken, apiSecret);
            
            // Record metrics
            sample.stop(meterRegistry.timer("market-data.zerodha.api.session.time"));
            meterRegistry.counter("market-data.zerodha.api.session.success").increment();
            
            // Log success
            if (user != null && user.accessToken != null) {
                log.info("Successfully generated Zerodha session, access token: {}", 
                        user.accessToken.substring(0, Math.min(5, user.accessToken.length())) + "...");
                setAccessToken(user.accessToken);
                
                // Store refresh token if available
                if (user.refreshToken != null && !user.refreshToken.isEmpty()) {
                    this.refreshToken = user.refreshToken;
                    log.info("Stored refresh token for future use");
                }
            } else {
                log.warn("Generated session but user or access token is null");
            }
            
            return user;
        } catch (KiteException | IOException e) {
            // Record error metrics
            meterRegistry.counter("market-data.zerodha.api.session.error", "error_type", getErrorType(e)).increment();
            
            // Enhanced error logging
            log.error("Failed to generate Zerodha session: {}", e.getMessage(), e);
            log.error("Error details - Request token: {}, API key: {}", 
                    requestToken != null ? requestToken.substring(0, Math.min(5, requestToken.length())) + "..." : "null", 
                    apiKey != null ? apiKey.substring(0, Math.min(5, apiKey.length())) + "..." : "null");
            
            throw new ZerodhaApiException("Failed to generate session: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            meterRegistry.counter("market-data.zerodha.api.session.error", "error_type", "unexpected").increment();
            log.error("Unexpected error generating Zerodha session: {}", e.getMessage(), e);
            throw new ZerodhaApiException("Unexpected error generating session: " + e.getMessage(), e);
        }    }

    /**
     * Get user profile information
     * @return Profile object with user details
     */
    //@Retry(name = "marketDataZerodhaApi")
    public Profile getProfile() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Profile profile = kiteConnect.getProfile();
            sample.stop(meterRegistry.timer("market-data.zerodha.api.profile.time"));
            meterRegistry.counter("market-data.zerodha.api.profile.success").increment();
            return profile;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.profile.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get profile: {}", e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get profile", e);
        }
    }

    /**
     * Get quotes for multiple instruments
     * @param instruments Array of instruments in format [exchange:tradingsymbol] (e.g., ["NSE:INFY", "BSE:SBIN"])
     * @return Map of instrument to Quote object
     */
    //@Retry(name = "marketDataZerodhaApi")
    public Map<String, Quote> getQuotes(String[] instruments) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Map<String, Quote> quotes = kiteConnect.getQuote(instruments);
            sample.stop(meterRegistry.timer("market-data.zerodha.api.quotes.time"));
            meterRegistry.counter("market-data.zerodha.api.quotes.success").increment();
            return quotes;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.quotes.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get quotes for instruments {}: {}", Arrays.toString(instruments), e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get quotes", e);
        }
    }

    /**
     * Get OHLC and last price for multiple instruments
     * @param instruments Array of instruments in format [exchange:tradingsymbol] (e.g., ["NSE:INFY", "BSE:SBIN"])
     * @return Map of instrument to OHLC object
     */
    //@Retry(name = "marketDataZerodhaApi")
    public Map<String, OHLCQuote> getOHLC(String[] instruments) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Map<String, OHLCQuote> ohlc = kiteConnect.getOHLC(instruments);
            sample.stop(meterRegistry.timer("market-data.zerodha.api.ohlc.time"));
            meterRegistry.counter("market-data.zerodha.api.ohlc.success").increment();
            return ohlc;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.ohlc.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get OHLC for instruments {}: {}", Arrays.toString(instruments), e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get OHLC", e);
        }
    }

    /**
     * Get last price for multiple instruments
     * @param instruments Array of instruments in format [exchange:tradingsymbol] (e.g., ["NSE:INFY", "BSE:SBIN"])
     * @return Map of instrument to LTP object
     */
    //@Retry(name = "marketDataZerodhaApi")
    public Map<String, LTPQuote> getLTP(String[] instruments) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Map<String, LTPQuote> ltp = kiteConnect.getLTP(instruments);
            sample.stop(meterRegistry.timer("market-data.zerodha.api.ltp.time"));
            meterRegistry.counter("market-data.zerodha.api.ltp.success").increment();
            return ltp;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.ltp.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get LTP for instruments {}: {}", Arrays.toString(instruments), e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get LTP", e);
        }
    }

    /**
     * Get historical data for an instrument
     * @param instrumentToken Instrument token
     * @param from From date
     * @param to To date
     * @param interval Interval (minute, day, etc.)
     * @param continuous Continuous flag for F&O contracts
     * @param oi Include open interest
     * @return Historical data object
     */
    //@Retry(name = "marketDataZerodhaApi")
    public HistoricalData getHistoricalData(String instrumentToken, Date from, Date to, String interval, boolean continuous, boolean oi) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            HistoricalData historicalData = kiteConnect.getHistoricalData(from, to, instrumentToken, interval, continuous, oi);
            sample.stop(meterRegistry.timer("market-data.zerodha.api.historical.time"));
            meterRegistry.counter("market-data.zerodha.api.historical.success").increment();
            return historicalData;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.historical.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get historical data for instrument {}: {}", instrumentToken, e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get historical data", e);
        }
    }

    /**
     * Get all available instruments
     * @return List of instruments
     */
    @Retry(name = "marketDataZerodhaApi")
    public List<Instrument> getAllInstruments() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            List<Instrument> instruments = kiteConnect.getInstruments();
            sample.stop(meterRegistry.timer("market-data.zerodha.api.instruments.time"));
            meterRegistry.counter("market-data.zerodha.api.instruments.success").increment();
            return instruments;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.instruments.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get all instruments: {}", e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get instruments", e);
        }
    }

    /**
     * Get instruments for a specific exchange
     * @param exchange Exchange name (NSE, BSE, etc.)
     * @return List of instruments for the exchange
     */
    @Retry(name = "marketDataZerodhaApi")
    public List<Instrument> getInstrumentsForExchange(String exchange) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            List<Instrument> instruments = kiteConnect.getInstruments(exchange);
            sample.stop(meterRegistry.timer("market-data.zerodha.api.instruments.exchange.time"));
            meterRegistry.counter("market-data.zerodha.api.instruments.exchange.success").increment();
            return instruments;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.instruments.exchange.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get instruments for exchange {}: {}", exchange, e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get instruments for exchange", e);
        }
    }

    /**
     * Get margins for a segment
     * @param segment Segment (equity, commodity)
     * @return Margin object
     */
    @Retry(name = "marketDataZerodhaApi")
    public Margin getMargins(String segment) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Margin margins = kiteConnect.getMargins(segment);
            sample.stop(meterRegistry.timer("market-data.zerodha.api.margins.time"));
            meterRegistry.counter("market-data.zerodha.api.margins.success").increment();
            return margins;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.margins.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get margins for segment {}: {}", segment, e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get margins", e);
        }
    }

    /**
     * Get all orders
     * @return List of orders
     */
    //@Retry(name = "marketDataZerodhaApi")
    public List<Order> getOrders() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            List<Order> orders = kiteConnect.getOrders();
            sample.stop(meterRegistry.timer("market-data.zerodha.api.orders.time"));
            meterRegistry.counter("market-data.zerodha.api.orders.success").increment();
            return orders;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.orders.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get orders: {}", e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get orders", e);
        }
    }

    /**
     * Get trades
     * @return List of trades
     */
    @Retry(name = "marketDataZerodhaApi")
    public List<Trade> getTrades() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            List<Trade> trades = kiteConnect.getTrades();
            sample.stop(meterRegistry.timer("market-data.zerodha.api.trades.time"));
            meterRegistry.counter("market-data.zerodha.api.trades.success").increment();
            return trades;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.trades.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get trades: {}", e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get trades", e);
        }
    }

    /**
     * Get positions (day and net)
     * @return Map containing day and net positions
     */
    @Retry(name = "marketDataZerodhaApi")
    public Map<String, List<Position>> getPositions() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Map<String, List<Position>> positions = kiteConnect.getPositions();
            sample.stop(meterRegistry.timer("market-data.zerodha.api.positions.time"));
            meterRegistry.counter("market-data.zerodha.api.positions.success").increment();
            return positions;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.positions.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get positions: {}", e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get positions", e);
        }
    }

    /**
     * Get holdings
     * @return List of holdings
     */
    @Retry(name = "marketDataZerodhaApi")
    public List<Holding> getHoldings() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            List<Holding> holdings = kiteConnect.getHoldings();
            sample.stop(meterRegistry.timer("market-data.zerodha.api.holdings.time"));
            meterRegistry.counter("market-data.zerodha.api.holdings.success").increment();
            return holdings;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.holdings.error", "error_type", getErrorType(e)).increment();
            log.error("Failed to get holdings: {}", e.getMessage(), e);
            throw new ZerodhaApiException("Failed to get holdings", e);
        }
    }

    /**
     * Initialize and connect ticker for real-time data
     * @param tokens List of instrument tokens to subscribe
     * @param onTickListener Callback for tick data
     * @return Connected KiteTicker instance
     */
    public KiteTicker initializeTicker(List<Long> tokens, OnTicks onTickListener) {
        if (tickerProvider != null && tickerProvider.isConnectionOpen()) {
            log.info("Ticker already connected, disconnecting first");
            tickerProvider.disconnect();
        }

        log.info("Initializing Zerodha ticker with {} tokens", tokens.size());
        tickerProvider = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
        
        // Configure ticker
        tickerProvider.setTryReconnection(true);
        //tickerProvider.setMaximumRetries(tickerMaxRetries);
        //tickerProvider.setMaximumRetryInterval(tickerReconnectInterval);
        
        // Set listeners
        tickerProvider.setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                log.info("Ticker connected successfully");
                meterRegistry.counter("zerodha.ticker.connect").increment();
                // Convert List<Long> to ArrayList<Long> for API compatibility
                ArrayList<Long> tokenList = new ArrayList<>(tokens);
                tickerProvider.subscribe(tokenList);
                tickerProvider.setMode(tokenList, KiteTicker.modeFull);
            }
        });
        
        tickerProvider.setOnDisconnectedListener(new OnDisconnect() {
            @Override
            public void onDisconnected() {
                log.warn("Ticker disconnected");
                meterRegistry.counter("zerodha.ticker.disconnect").increment();
            }
        });
        
        tickerProvider.setOnErrorListener(new OnError() {
            @Override
            public void onError(Exception exception) {
                log.error("Ticker error: {}", exception.getMessage(), exception);
                meterRegistry.counter("zerodha.ticker.error", "type", "exception").increment();
            }
            
            @Override
            public void onError(KiteException kiteException) {
                log.error("Ticker KiteException: {}", kiteException.getMessage(), kiteException);
                meterRegistry.counter("zerodha.ticker.error", "type", "kite_exception").increment();
            }
            
            @Override
            public void onError(String error) {
                log.error("Ticker error: {}", error);
                meterRegistry.counter("zerodha.ticker.error", "type", "string").increment();
            }
        });
        
        // Set tick listener
        tickerProvider.setOnTickerArrivalListener(onTickListener);
        
        // Connect
        tickerProvider.connect();
        log.info("Ticker connection initiated");
        
        return tickerProvider;
    }

    /**
     * Disconnect the ticker
     */
    public void disconnectTicker() {
        if (tickerProvider != null && tickerProvider.isConnectionOpen()) {
            log.info("Disconnecting ticker");
            tickerProvider.disconnect();
            meterRegistry.counter("zerodha.ticker.manual_disconnect").increment();
        } else {
            log.info("Ticker not connected, nothing to disconnect");
        }
    }

    /**
     * Check if ticker is connected
     * @return true if connected, false otherwise
     */
    public boolean isTickerConnected() {
        return tickerProvider != null && tickerProvider.isConnectionOpen();
    }

    /**
     * Logout and invalidate session
     * @return true if logout successful
     */
    public boolean logout() {
        try {
            kiteConnect.logout();
            meterRegistry.counter("market-data.zerodha.api.logout.success").increment();
            log.info("Logged out of Zerodha API");
            return true;
        } catch (KiteException | IOException e) {
            meterRegistry.counter("market-data.zerodha.api.logout.error").increment();
            log.error("Failed to logout: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Retry a function with exponential backoff
     * @param operation Function to retry
     * @param <T> Return type
     * @return Result of the operation
     */
    private <T> T retryWithBackoff(ZerodhaOperation<T> operation) {
        Exception lastException = null;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                long delay = retryDelayMs * (long) Math.pow(2, attempt);
                log.warn("Attempt {} failed, retrying after {}ms: {}", attempt + 1, delay, e.getMessage());
                meterRegistry.counter("market-data.zerodha.api.retry").increment();
                try {
                    TimeUnit.MILLISECONDS.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ZerodhaApiException("Retry interrupted", ie);
                }
            }
        }
        throw new ZerodhaApiException("All retry attempts failed", lastException);
    }

    /**
     * Async version of API calls using CompletableFuture
     * @param operation Operation to execute asynchronously
     * @param <T> Return type
     * @return CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeAsync(ZerodhaOperation<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return retryWithBackoff(operation);
            } catch (Exception e) {
                log.error("Async operation failed: {}", e.getMessage(), e);
                throw new ZerodhaApiException("Async operation failed", e);
            }
        }, threadPoolExecutor);
    }

    /**
     * Get error type for metrics
     * @param e Throwable
     * @return Error type string
     */
    private String getErrorType(Throwable e) {
        if (e instanceof KiteException) {
            KiteException ke = (KiteException) e;
            try {
                // Try to get the HTTP status code from the exception message or use reflection
                String message = ke.getMessage();
                if (message != null && message.contains("403")) return "unauthorized";
                if (message != null && message.contains("401")) return "unauthorized";
                if (message != null && message.contains("400")) return "client_error";
                if (message != null && message.contains("404")) return "client_error";
                if (message != null && message.contains("500")) return "server_error";
                return "kite_error";
            } catch (Exception ex) {
                return "kite_error";
            }
        } else if (e instanceof IOException) {
            return "network_error";
        } else {
            return "unexpected_error";
        }
    }

    /**
     * Mask API key for logging
     * @param key API key
     * @return Masked key
     */
    private String maskApiKey(String key) {
        if (key == null || key.length() < 8) {
            return "*****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    /**
     * Mask token for logging
     * @param token Token
     * @return Masked token
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "*****";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }

    /**
     * Functional interface for operations that can throw exceptions
     * @param <T> Return type
     */
    @FunctionalInterface
    public interface ZerodhaOperation<T> {
        T execute() throws Exception;
    }
}
