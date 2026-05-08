package com.marketdata.service.upstox;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.marketdata.upstock.adapter.UpStockAdapter;
import com.marketdata.common.MarketDataProvider;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.OHLCQuote;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Upstox implementation of the MarketDataProvider interface.
 * Delegates to UpStockAdapter which wraps the Upstox REST API client.
 */
@Service("upstoxMarketDataProvider")
@RequiredArgsConstructor
public class UpstoxMarketDataProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(UpstoxMarketDataProvider.class);

    private final UpStockAdapter upStockAdapter;

    @Override
    public void initialize() {
        log.info("UpstoxMarketDataProvider initialized");
    }

    @Override
    public void cleanup() {
        log.info("UpstoxMarketDataProvider cleanup");
    }

    @Override
    public void setAccessToken(String accessToken) {
        log.info("setAccessToken called on UpstoxMarketDataProvider — token is managed via UpstoxConfig");
    }

    @Override
    public String getLoginUrl() {
        throw new UnsupportedOperationException("getLoginUrl not implemented for Upstox provider");
    }

    @Override
    public Object generateSession(String requestToken) {
        throw new UnsupportedOperationException("generateSession not implemented for Upstox provider");
    }

    /**
     * Get full market quotes from Upstox.
     * Symbols should be in "NSE_EQ|SYMBOL" or "NSE:SYMBOL" format.
     * Returns a Map keyed by trading symbol (without exchange prefix).
     */
    @Override
    public Map<String, Object> getQuotes(String[] symbols) {
        try {
            List<String> symbolList = Arrays.stream(symbols)
                .map(this::formatSymbolForUpstox)
                .collect(Collectors.toList());

            log.info("UpstoxMarketDataProvider.getQuotes for symbols: {}", symbolList);
            List<EquityPrice> prices = upStockAdapter.getStocks(symbolList);
            log.info("Upstox returned {} prices", prices.size());

            Map<String, Object> result = new HashMap<>();
            for (EquityPrice ep : prices) {
                if (ep.getSymbol() != null) {
                    Map<String, Object> quoteMap = new HashMap<>();
                    quoteMap.put("symbol", ep.getSymbol());
                    quoteMap.put("lastPrice", ep.getClose());   // close is LTP from Upstox
                    quoteMap.put("openPrice", ep.getOpen());
                    quoteMap.put("highPrice", ep.getHigh());
                    quoteMap.put("lowPrice", ep.getLow());
                    quoteMap.put("closePrice", ep.getClose());
                    quoteMap.put("volume", ep.getVolume());
                    result.put(ep.getSymbol(), quoteMap);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Error fetching quotes from Upstox: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Get OHLC data from Upstox.
     */
    @Override
    public Map<String, OHLCQuote> getOHLC(String[] symbols) {
        try {
            List<String> symbolList = Arrays.stream(symbols)
                .map(this::formatSymbolForUpstox)
                .collect(Collectors.toList());

            log.info("UpstoxMarketDataProvider.getOHLC for symbols: {}", symbolList);
            List<EquityPrice> prices = upStockAdapter.getStocksOHLC(symbolList);

            Map<String, OHLCQuote> result = new HashMap<>();
            for (EquityPrice ep : prices) {
                if (ep.getSymbol() != null) {
                    OHLCQuote quote = new OHLCQuote();
                    quote.lastPrice = ep.getClose() != null ? ep.getClose() : 0.0;
                    quote.ohlc = new com.zerodhatech.models.OHLC();
                    quote.ohlc.open = ep.getOpen() != null ? ep.getOpen() : 0.0;
                    quote.ohlc.high = ep.getHigh() != null ? ep.getHigh() : 0.0;
                    quote.ohlc.low = ep.getLow() != null ? ep.getLow() : 0.0;
                    quote.ohlc.close = ep.getClose() != null ? ep.getClose() : 0.0;
                    result.put(ep.getSymbol(), quote);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Error fetching OHLC from Upstox: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Get LTP (Last Traded Price) from Upstox.
     * Returns the same structure as getQuotes but only with lastPrice populated.
     */
    @Override
    public Map<String, Object> getLTP(String[] symbols) {
        Map<String, Object> quotes = getQuotes(symbols);
        Map<String, Object> ltpResult = new HashMap<>();
        for (Map.Entry<String, Object> entry : quotes.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<?, ?> q = (Map<?, ?>) entry.getValue();
                Map<String, Object> ltp = new HashMap<>();
                ltp.put("lastPrice", q.get("lastPrice"));
                ltpResult.put(entry.getKey(), ltp);
            }
        }
        return ltpResult;
    }

    @Override
    public HistoricalData getHistoricalData(String symbol, Date from, Date to, String interval,
                                             boolean continuous, Map<String, Object> additionalParams) {
        throw new UnsupportedOperationException("getHistoricalData not yet implemented for Upstox provider. Use the Zerodha provider.");
    }

    @Override
    public Object initializeTicker(List<String> symbolIds, Object tickListener) {
        throw new UnsupportedOperationException("initializeTicker not implemented for Upstox provider");
    }

    @Override
    public boolean isTickerConnected() {
        return false;
    }

    @Override
    public List<Instrument> getAllInstruments() {
        log.warn("getAllInstruments not implemented for Upstox — returning empty list");
        return new ArrayList<>();
    }

    @Override
    public List<Object> getSymbolsForExchange(String exchange) {
        log.warn("getSymbolsForExchange not implemented for Upstox — returning empty list");
        return new ArrayList<>();
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(ProviderOperation<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.execute();
            } catch (Exception e) {
                throw new RuntimeException("Error executing async Upstox operation", e);
            }
        });
    }

    @Override
    public boolean logout() {
        log.info("Upstox logout — no persistent session to invalidate");
        return true;
    }

    @Override
    public String getProviderName() {
        return "upstox";
    }

    /**
     * Formats symbol for Upstox v2 API.
     * Ensures "EXCHANGE|SYMBOL" format. Defaults to "NSE_EQ|" if no exchange provided.
     */
    private String formatSymbolForUpstox(String symbol) {
        if (symbol == null) return null;
        if (symbol.contains("|")) return symbol;
        if (symbol.contains(":")) return symbol.replace(":", "|");
        // Default to NSE Equity if no exchange prefix
        return "NSE_EQ|" + symbol;
    }
}
