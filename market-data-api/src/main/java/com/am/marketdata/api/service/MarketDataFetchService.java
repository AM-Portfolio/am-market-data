package com.am.marketdata.api.service;

import com.am.marketdata.common.model.OHLCQuote;
import com.marketdata.common.dto.HistoricalDataRequest;
import com.marketdata.common.model.HistoricalDataResponseV1;
import com.marketdata.common.model.OHLCRequest;
import com.marketdata.common.model.QuotesRequest;

import java.util.List;
import java.util.Map;

/**
 * Interface for Market Data Fetch Service
 * Defines the contract for fetching market data
 */
public interface MarketDataFetchService {

    Map<String, String> getLoginUrl(String provider);

    Object generateSession(String requestToken);

    Map<String, Object> getQuotes(String symbols, String timeFrame, boolean refresh);

    Map<String, Object> getQuotesPost(QuotesRequest request);

    Map<String, OHLCQuote> getOHLC(OHLCRequest request);

    HistoricalDataResponseV1 getHistoricalData(HistoricalDataRequest request);

    List<Object> getSymbolsForExchange(String exchange);

    Map<String, Object> logout();

    Map<String, Object> getLivePrices(String symbols, boolean indexSymbol, boolean forceRefresh);

    Map<String, Object> getLiveLTP(String symbols, String timeframe, boolean indexSymbol, boolean forceRefresh);
}
