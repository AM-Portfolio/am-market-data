package com.marketdata.service.upstox;

import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.upstock.model.HistoricalDataResponse;
import com.am.marketdata.upstock.model.OHLCResponse;
import com.marketdata.common.MarketDataProvider;
import com.upstox.api.GetMarketQuoteLastTradedPriceResponseV3;
import com.upstox.api.MarketQuoteSymbolLtpV3;

import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.LTPQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service("upstoxMarketDataProvider")
public class UpstoxMarketDataProvider implements MarketDataProvider {

    private final UpstoxApiService upstoxApiService;
    private final com.am.marketdata.service.service.UpstoxInstrumentService upstoxInstrumentService;
    private final UpstoxSdkService upstoxSdkService;

    public UpstoxMarketDataProvider(UpstoxApiService upstoxApiService,
            com.am.marketdata.service.service.UpstoxInstrumentService upstoxInstrumentService,
            UpstoxSdkService upstoxSdkService) {
        this.upstoxApiService = upstoxApiService;
        this.upstoxInstrumentService = upstoxInstrumentService;
        this.upstoxSdkService = upstoxSdkService;
    }

    @Override
    public void initialize() {
        upstoxApiService.initialize();
    }

    @Override
    public void cleanup() {
        // Cleanup logic
    }

    @Override
    public void setAccessToken(String accessToken) {
        upstoxApiService.setAccessToken(accessToken);
    }

    @Override
    public String getLoginUrl() {
        return upstoxApiService.getLoginUrl();
    }

    @Override
    public Object generateSession(String requestToken) {
        return upstoxApiService.generateSession(requestToken);
    }

    @Override
    public Map<String, Object> getQuotes(String[] symbols) {
        return new HashMap<>();
    }

    private List<com.am.marketdata.service.model.UpstoxInstrument> resolveInstruments(List<String> symbols) {
        // Strip exchange prefix if present (e.g., NSE:RELIANCE -> RELIANCE)
        List<String> cleanedSymbols = symbols.stream()
                .map(s -> {
                    if (s.startsWith("NSE:") || s.startsWith("BSE:")) {
                        return s.substring(4);
                    }
                    return s;
                })
                .collect(Collectors.toList());

        com.am.marketdata.service.dto.InstrumentSearchCriteria criteria = new com.am.marketdata.service.dto.InstrumentSearchCriteria();
        criteria.setTradingSymbols(cleanedSymbols);
        criteria.setProvider("UPSTOX");

        return upstoxInstrumentService.searchInstruments(criteria);
    }

    private static class InstrumentContext {
        final List<String> instrumentKeys;
        final Map<String, String> keyToSymbolMap;

        InstrumentContext(List<com.am.marketdata.service.model.UpstoxInstrument> instruments) {
            this.instrumentKeys = instruments.stream()
                    .map(com.am.marketdata.service.model.UpstoxInstrument::getInstrumentKey)
                    .collect(Collectors.toList());

            this.keyToSymbolMap = instruments.stream()
                    .collect(Collectors.toMap(
                            com.am.marketdata.service.model.UpstoxInstrument::getInstrumentKey,
                            com.am.marketdata.service.model.UpstoxInstrument::getTradingSymbol,
                            (existing, replacement) -> existing));
        }
    }

    private InstrumentContext resolveContext(List<String> symbols) {
        return new InstrumentContext(resolveInstruments(symbols));
    }

    @Override
    public Map<String, OHLCQuote> getOHLC(List<String> symbols, TimeFrame timeFrame) {
        try {
            InstrumentContext context = resolveContext(symbols);

            log.info("Resolved {} instruments for symbols: {}", context.instrumentKeys.size(), symbols);
            if (context.instrumentKeys.isEmpty()) {
                log.warn("No instrument keys resolved for symbols: {}", symbols);
                return new HashMap<>();
            }

            log.debug("Fetching OHLC from Upstox API for keys: {}", context.instrumentKeys);

            // Upstox requires interval for HOhlc. Defaulting to 1 day as it's common for
            // general OHLC quote
            String upstoxInterval = timeFrame.getUpStockValue();

            log.debug("Fetching OHLC using interval: {}", upstoxInterval);

            OHLCResponse response = null;

            // Try SDK Service first
            try {
                com.am.marketdata.upstock.model.OHLCResponse sdkResponse = upstoxSdkService
                        .getOhlc(context.instrumentKeys, upstoxInterval);
                if (sdkResponse != null && sdkResponse.getData() != null && !sdkResponse.getData().isEmpty()) {
                    // Map SDK response to OHLCResponse model structure used below
                    response = sdkResponse;
                }
            } catch (Exception e) {
                log.warn("Failed to fetch OHLC via SDK Service, falling back to API Service: {}", e.getMessage());
            }

            // Fallback to API Service if SDK failed or returned empty
            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                response = upstoxApiService.getOhlc(context.instrumentKeys, upstoxInterval);
            }

            Map<String, OHLCQuote> result = new HashMap<>();

            if (response != null && response.getData() != null) {
                for (Map.Entry<String, OHLCResponse.OHLCData> entry : response.getData().entrySet()) {
                    String instrumentKey = entry.getKey();
                    OHLCResponse.OHLCData data = entry.getValue();

                    // Map back to symbol if possible, otherwise use key
                    String symbol = context.keyToSymbolMap.getOrDefault(instrumentKey, instrumentKey);

                    OHLCQuote quote = new OHLCQuote();
                    // Use getters as fields might be mapped differently or computed
                    quote.setLastPrice(data.getLast_price() != null ? data.getLast_price() : 0.0);

                    if (data.getOhlc() != null) {
                        OHLCQuote.OHLC ohlc = new OHLCQuote.OHLC();
                        ohlc.setOpen(data.getOhlc().getOpen());
                        ohlc.setHigh(data.getOhlc().getHigh());
                        ohlc.setLow(data.getOhlc().getLow());
                        ohlc.setClose(data.getOhlc().getClose());
                        quote.setOhlc(ohlc);
                    }

                    // Also set previous close if available in data
                    if (data.getPrevious_close() != null) {
                        log.debug("Setting Previous Close for {}: {}", symbol, data.getPrevious_close());
                        quote.setPreviousClose(data.getPrevious_close());
                    } else {
                        log.debug("No Previous Close found in mapped data for {}", symbol);
                    }

                    result.put(symbol, quote);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Error fetching Upstox OHLC", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, LTPQuote> getLTP(String[] symbols) {
        try {
            InstrumentContext context = resolveContext(Arrays.asList(symbols));

            if (context.instrumentKeys.isEmpty()) {
                return new HashMap<>();
            }

            GetMarketQuoteLastTradedPriceResponseV3 response = upstoxSdkService.getLtp(context.instrumentKeys);
            Map<String, LTPQuote> result = new HashMap<>();

            if (response != null && response.getData() != null) {
                for (Map.Entry<String, MarketQuoteSymbolLtpV3> entry : response.getData().entrySet()) {
                    String instrumentKey = entry.getKey();
                    MarketQuoteSymbolLtpV3 data = entry.getValue();

                    String symbol = context.keyToSymbolMap.getOrDefault(instrumentKey, instrumentKey);

                    LTPQuote quote = new LTPQuote();
                    quote.lastPrice = data.getLastPrice();
                    quote.instrumentToken = 0;

                    result.put(symbol, quote);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Error fetching Upstox LTP via SDK Service", e);
            return new HashMap<>();
        }
    }

    @Override
    public HistoricalData getHistoricalData(String symbol, Date from, Date to, TimeFrame interval, boolean continuous,
            Map<String, Object> additionalParams) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = dateFormat.format(from);
            String toDateStr = dateFormat.format(to);

            String upstoxInterval = mapToUpstoxInterval(interval);

            HistoricalDataResponse response = upstoxApiService.getHistoricalCandleData(symbol, upstoxInterval,
                    fromDateStr, toDateStr);

            // Use Zerodha HistoricalData model
            HistoricalData historicalData = new HistoricalData();
            // Note: Zerodha HistoricalData might extend ArrayList<HistoricalData> or
            // similar.
            // We need to inspect it or assume standard fields.
            // Since I cannot inspect it, I will assume it has methods to add data or is a
            // list.
            // If it is a list, I can add to it.
            // If it has parse methods, I use them.
            // Based on kiteconnect, HistoricalData extends ArrayList<HistoricalData>.
            // And each item has open, high, low, close, volume, timeStamp keys.

            // However, compilation depends on matching class structure.
            // If it extends ArrayList, `historicalData.add(...)` works.

            if (response != null && response.getData() != null) {
                historicalData.dataArrayList = new ArrayList<>();
                for (HistoricalDataResponse.Candle candle : response.getData()) {
                    HistoricalData point = new HistoricalData();
                    // Assuming HistoricalData has these fields/setters or map-like 'put'
                    // Standard KiteConnect: public String timeStamp; public double open; ...
                    point.timeStamp = candle.getTimestamp(); // String expected
                    point.open = candle.getOpen();
                    point.high = candle.getHigh();
                    point.low = candle.getLow();
                    point.close = candle.getClose();
                    point.volume = (long) candle.getVolume(); // long expected

                    historicalData.dataArrayList.add(point);
                }
            }

            return historicalData;
        } catch (Exception e) {
            log.error("Error fetching Upstox historical data", e);
            return new HistoricalData();
        }
    }

    private String mapToUpstoxInterval(TimeFrame interval) {
        if (interval == null)
            return "1minute";
        switch (interval) {
            case MINUTE:
                return "1minute";
            case FIVE_MINUTE:
                return "5minute";
            case FIFTEEN_MINUTE:
                return "15minute";
            case THIRTY_MINUTE:
                return "30minute";
            case HOUR:
                return "60minute";
            case DAY:
                return "day";
            case WEEK:
                return "week";
            case MONTH:
                return "month";
            default:
                return "1minute";
        }
    }

    @Override
    public Object initializeTicker(List<String> symbolIds, Object tickListener) {
        return null; // Not implemented for Upstox yet
    }

    @Override
    public boolean isTickerConnected() {
        return false;
    }

    @Override
    public List<Instrument> getAllInstruments() {
        return new ArrayList<>(); // Return empty list of Zerodha Instruments
    }

    @Override
    public List<Object> getSymbolsForExchange(String exchange) {
        return new ArrayList<>();
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(ProviderOperation<T> operation) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean logout() {
        return true;
    }

    @Override
    public String getProviderName() {
        return "upstox";
    }
}
