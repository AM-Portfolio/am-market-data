package com.am.marketdata.provider;

import com.am.marketdata.common.model.Instrument;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.common.investment.model.historical.HistoricalData;
import java.util.*;

/**
 * Provider interface for market data operations.
 * Implementations: Upstox, Zerodha, etc.
 * 
 * This interface defines the contract that all provider implementations must
 * follow.
 * Service layer will depend ONLY on this interface, not on specific
 * implementations.
 */
public interface AMMarketDataProvider {

    /**
     * Get real-time quotes for given symbols
     * 
     * @param symbols List of trading symbols
     * @return Map of symbol to OHLC quote
     */
    Map<String, OHLCQuote> getQuotes(List<String> symbols);

    /**
     * Get historical OHLC data
     * 
     * @param symbols  List of trading symbols
     * @param from     Start date (YYYY-MM-DD)
     * @param to       End date (YYYY-MM-DD)
     * @param interval Time interval (1d, 1h, 5m, etc.)
     * @return Map of symbol to HistoricalData
     */
    Map<String, HistoricalData> getHistoricalData(
            List<String> symbols,
            String from,
            String to,
            String interval);

    /**
     * Get all instruments for an exchange
     * 
     * @param exchange Exchange name (NSE, BSE, etc.)
     * @return List of instruments
     */
    List<Instrument> getInstruments(String exchange);

    /**
     * Search instruments by symbol or name
     * 
     * @param query Search query
     * @return List of matching instruments
     */
    List<Instrument> searchInstruments(String query);

    /**
     * Login to provider with credentials
     * 
     * @param apiKey    Provider API key
     * @param apiSecret Provider API secret
     */
    void login(String apiKey, String apiSecret);

    /**
     * Logout from provider
     */
    void logout();

    /**
     * Check if session is valid
     * 
     * @return true if session is active
     */
    boolean isSessionValid();

    /**
     * Get provider name (upstox, zerodha, etc.)
     * 
     * @return Provider name
     */
    String getProviderName();

    /**
     * Generate session/access token
     * 
     * @param requestToken Request token from OAuth flow
     * @return Access token
     */
    String generateSession(String requestToken);

    // --- Bridge Methods for Service Compatibility ---

    default Map<String, OHLCQuote> getLTP(String[] symbols) {
        return getQuotes(Arrays.asList(symbols));
    }

    default Map<String, HistoricalData> getHistoricalDataEx(List<String> symbols, Date from, Date to,
            com.am.marketdata.common.model.TimeFrame interval, boolean continuous,
            Map<String, Object> additionalParams) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String fromStr = sdf.format(from);
        String toStr = sdf.format(to);
        String intervalStr = "1d";
        if (interval != null) {
            String tf = interval.toString().toUpperCase();
            if (tf.contains("DAY"))
                intervalStr = "1d";
            else if (tf.contains("MINUTE"))
                intervalStr = "1m";
            else if (tf.contains("HOUR"))
                intervalStr = "60m";
            else if (tf.contains("WEEK"))
                intervalStr = "1w";
            else if (tf.contains("MONTH"))
                intervalStr = "1M";
        }
        return getHistoricalData(symbols, fromStr, toStr, intervalStr);
    }

    default Map<String, OHLCQuote> getOHLC(List<String> symbols, com.am.marketdata.common.model.TimeFrame timeFrame) {
        // Assuming OHLC request for recent data is same as Quote
        return getQuotes(symbols);
    }

    default List<Instrument> getAllInstruments() {
        return getInstruments("NSE");
    }

    default List<String> getSymbolsForExchange(String exchange) {
        return getInstruments(exchange).stream()
                .map(Instrument::getTradingSymbol)
                .toList();
    }

    default String getLoginUrl() {
        return "";
    }
}
