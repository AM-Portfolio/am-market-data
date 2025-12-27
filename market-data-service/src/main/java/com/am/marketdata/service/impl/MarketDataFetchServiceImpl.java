package com.am.marketdata.service.impl;

import com.am.marketdata.api.service.MarketDataFetchService;
import com.am.marketdata.common.model.OHLCQuoteV1;
import com.am.marketdata.common.model.TimeFrameV1;
import com.am.marketdata.service.MarketDataService;
import com.marketdata.common.dto.HistoricalDataRequest;
import com.marketdata.common.model.HistoricalDataResponseV1;
import com.marketdata.common.model.OHLCRequest;
import com.marketdata.common.model.QuotesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of MarketDataFetchService that delegates to MarketDataService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataFetchServiceImpl implements MarketDataFetchService {

    private final MarketDataService marketDataService;

    @Override
    public Map<String, String> getLoginUrl(String provider) {
        return marketDataService.getLoginUrl(provider);
    }

    @Override
    public Object generateSession(String requestToken) {
        return marketDataService.generateSession(requestToken);
    }

    @Override
    public Map<String, Object> getQuotes(String symbols, String timeFrame, boolean refresh) {
        String[] symbolList = symbols != null ? symbols.split(",") : new String[0];
        // Note: provider is currently resolved in MarketDataService
        return marketDataService.getQuotes(symbolList, null);
    }

    @Override
    public Map<String, Object> getQuotesPost(QuotesRequest request) {
        String[] symbolList = request.getSymbols() != null ? request.getSymbols().split(",") : new String[0];
        return marketDataService.getQuotes(symbolList, null);
    }

    @Override
    public Map<String, OHLCQuoteV1> getOHLC(OHLCRequest request) {
        TimeFrameV1 tf = TimeFrameV1.fromApiValue(request.getTimeFrame());
        List<String> symbolList = request.getSymbols() != null ? Arrays.asList(request.getSymbols().split(","))
                : List.of();
        return marketDataService.getOHLC(symbolList, tf, request.isForceRefresh(), null);
    }

    @Override
    public HistoricalDataResponseV1 getHistoricalData(HistoricalDataRequest request) {
        // This is a complex mapping, for now return empty response as we are "reviving"
        // the structure
        log.warn("getHistoricalData not fully implemented in MarketDataFetchServiceImpl wrapper yet");
        return new HistoricalDataResponseV1();
    }

    @Override
    public List<Object> getSymbolsForExchange(String exchange) {
        return marketDataService.getSymbolsForExchange(exchange, null);
    }

    @Override
    public Map<String, Object> logout() {
        return marketDataService.logout(null);
    }

    @Override
    public Map<String, Object> getLivePrices(String symbols, boolean indexSymbol, boolean forceRefresh) {
        List<String> symbolList = symbols != null ? Arrays.asList(symbols.split(",")) : null;
        List<?> prices = marketDataService.getLivePrices(symbolList, null, forceRefresh);
        return Map.of("prices", prices);
    }

    @Override
    public Map<String, Object> getLiveLTP(String symbols, String timeframe, boolean indexSymbol, boolean forceRefresh) {
        log.warn("getLiveLTP not fully implemented in MarketDataFetchServiceImpl wrapper yet");
        return Map.of("message", "Not implemented yet");
    }
}
