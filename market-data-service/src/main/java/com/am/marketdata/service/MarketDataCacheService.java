package com.am.marketdata.service;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;

import java.util.List;
import java.util.Map;

/**
 * Service for caching market data in Redis
 */
public interface MarketDataCacheService {

    /**
     * Cache OHLC data in Redis
     *
     * @param ohlcData Map of symbol to OHLC quote
     */
    void cacheOHLCData(Map<String, OHLCQuote> ohlcData);

    /**
     * Cache historical data in Redis
     *
     * @param symbol The trading symbol
     * @param interval The data interval (e.g., "1d", "1h")
     * @param historicalData The historical data to cache
     */
    void cacheHistoricalData(String symbol, TimeFrame interval, HistoricalData historicalData);

    /**
     * Retrieve OHLC data from cache
     *
     * @param tradingSymbols List of trading symbols
     * @return Map of symbol to OHLCQuote if found in cache, empty map otherwise
     */
    Map<String, OHLCQuote> getOHLCFromCache(List<String> tradingSymbols);

    /**
     * Retrieve historical data from cache
     *
     * @param symbol The trading symbol
     * @param interval The data interval (e.g., "1d", "1h")
     * @param fromDate From date in ISO format (YYYY-MM-DD)
     * @param toDate To date in ISO format (YYYY-MM-DD)
     * @return HistoricalData if found in cache, null otherwise
     */
    HistoricalData getHistoricalDataFromCache(String symbol, TimeFrame interval, String fromDate, String toDate);
}
