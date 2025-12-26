package com.marketdata.service.upstox;

import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.upstock.model.HistoricalDataResponse;
import com.am.marketdata.upstock.model.OHLCResponse;
import com.marketdata.common.MarketDataProvider;
import com.upstox.api.GetMarketQuoteLastTradedPriceResponseV3;
import com.upstox.api.MarketQuoteSymbolLtpV3;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.model.historical.OHLCVTPoint;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.LTPQuote;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.am.marketdata.common.log.AppLogger;
import org.springframework.stereotype.Service;

@Service("upstoxMarketDataProvider")
public class UpstoxMarketDataProvider implements MarketDataProvider {

    private final AppLogger log = AppLogger.getLogger();

    private final UpstoxApiService upstoxApiService;
    private final com.am.marketdata.service.service.UpstoxInstrumentService upstoxInstrumentService;
    private final UpstoxSdkService upstoxSdkService;
    private final UpstoxIndexIdentifier indexIdentifier;

    public UpstoxMarketDataProvider(UpstoxApiService upstoxApiService,
            com.am.marketdata.service.service.UpstoxInstrumentService upstoxInstrumentService,
            UpstoxSdkService upstoxSdkService,
            UpstoxIndexIdentifier indexIdentifier) {
        this.upstoxApiService = upstoxApiService;
        this.upstoxInstrumentService = upstoxInstrumentService;
        this.upstoxSdkService = upstoxSdkService;
        this.indexIdentifier = indexIdentifier;
    }

    // ... (initialize, cleanup, setAccessToken, getLoginUrl, generateSession,
    // getQuotes methods remain unchanged)

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
        if (symbols == null || symbols.isEmpty())
            return new ArrayList<>();

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

        InstrumentContext(List<com.am.marketdata.service.model.UpstoxInstrument> instruments,
                Map<String, String> mappedIndices) {
            this.instrumentKeys = new ArrayList<>();
            this.keyToSymbolMap = new HashMap<>();

            // Add DB Instruments
            if (instruments != null) {
                this.instrumentKeys.addAll(instruments.stream()
                        .map(com.am.marketdata.service.model.UpstoxInstrument::getInstrumentKey)
                        .collect(Collectors.toList()));

                this.keyToSymbolMap.putAll(instruments.stream()
                        .collect(Collectors.toMap(
                                com.am.marketdata.service.model.UpstoxInstrument::getInstrumentKey,
                                com.am.marketdata.service.model.UpstoxInstrument::getTradingSymbol,
                                (existing, replacement) -> existing)));
            }

            // Add Mapped Indices
            if (mappedIndices != null) {
                for (Map.Entry<String, String> entry : mappedIndices.entrySet()) {
                    // Symbol -> Key map from identifier
                    String symbol = entry.getKey();
                    String key = entry.getValue();

                    if (!this.instrumentKeys.contains(key)) {
                        this.instrumentKeys.add(key);
                        // Map Key -> Symbol for reverse lookup
                        this.keyToSymbolMap.put(key, symbol);
                    }
                }
            }
        }
    }

    private InstrumentContext resolveContext(List<String> symbols) {
        // 1. Identify and resolve known Indices
        Map<String, String> resolvedIndices = indexIdentifier.resolveIndices(symbols); // Symbol -> Key

        // 2. Identify remaining symbols to lookup in DB
        List<String> symbolsForDb = symbols.stream()
                .filter(s -> !resolvedIndices.containsKey(s))
                .collect(Collectors.toList());

        if (!symbolsForDb.isEmpty()) {
            log.info("resolveContext", "Symbols not resolved as indices (will lookup in DB): " + symbolsForDb);
        }

        // 3. Lookup remaining symbols
        List<com.am.marketdata.service.model.UpstoxInstrument> dbInstruments = resolveInstruments(symbolsForDb);

        // 4. Combine
        return new InstrumentContext(dbInstruments, resolvedIndices);
    }

    @Override
    public Map<String, OHLCQuote> getOHLC(List<String> symbols, TimeFrame timeFrame) {
        try {
            InstrumentContext context = resolveContext(symbols);

            log.info("getOHLC",
                    String.format("Resolved %d instruments for symbols: %s", context.instrumentKeys.size(), symbols));
            if (context.instrumentKeys.isEmpty()) {
                log.warn("getOHLC", "No instrument keys resolved for symbols: " + symbols);
                return new HashMap<>();
            }

            log.info("getOHLC", "Fetching OHLC from Upstox API for keys: " + context.instrumentKeys);

            // Upstox requires interval for HOhlc. Defaulting to 1 day as it's common for
            // general OHLC quote
            String upstoxInterval = timeFrame.getUpStockValue();

            log.debug("getOHLC", "Fetching OHLC using interval: " + upstoxInterval);

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
                log.warn("getOHLC",
                        "Failed to fetch OHLC via SDK Service, falling back to API Service: " + e.getMessage());
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
                        log.debug("getOHLC",
                                String.format("Setting Previous Close for %s: %s", symbol, data.getPrevious_close()));
                        quote.setPreviousClose(data.getPrevious_close());
                    } else {
                        log.debug("getOHLC", "No Previous Close found in mapped data for " + symbol);
                    }

                    result.put(symbol, quote);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("getOHLC", "Error fetching Upstox OHLC", e);
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

            // Log for debugging
            log.info("getLTP", "Fetching LTP for keys: " + context.instrumentKeys);

            GetMarketQuoteLastTradedPriceResponseV3 response = upstoxSdkService.getLtp(context.instrumentKeys);
            Map<String, LTPQuote> result = new HashMap<>();

            if (response != null && response.getData() != null) {
                for (Map.Entry<String, MarketQuoteSymbolLtpV3> entry : response.getData().entrySet()) {
                    String instrumentKey = entry.getKey();
                    MarketQuoteSymbolLtpV3 data = entry.getValue();

                    // Map back to symbol using the context map
                    String symbol = context.keyToSymbolMap.getOrDefault(instrumentKey, instrumentKey);

                    LTPQuote quote = new LTPQuote();
                    quote.lastPrice = data.getLastPrice();
                    quote.instrumentToken = 0;

                    result.put(symbol, quote);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("getLTP", "Error fetching Upstox LTP via SDK Service", e);
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

            String unit = mapToUpstoxInterval(interval);
            int intervalValue = getUpstoxIntervalValue(interval);

            // Resolve instrument key first as SDK works with keys
            List<String> symbolsList = Collections.singletonList(symbol);
            InstrumentContext context = resolveContext(symbolsList);
            String instrumentKey = null;
            if (!context.instrumentKeys.isEmpty()) {
                instrumentKey = context.instrumentKeys.get(0);
            } else {
                log.warn("getHistoricalData", "Could not resolve instrument key for historical data symbol: " + symbol);
            }

            HistoricalDataResponse response = null;

            // 1. Try SDK Service if key resolved
            if (instrumentKey != null) {
                try {
                    log.info("getHistoricalData",
                            "Fetching historical data via SDK for instrument key: " + instrumentKey + ", unit: " + unit
                                    + ", interval: " + intervalValue);
                    response = upstoxSdkService.getHistoricalCandleData(instrumentKey, unit, intervalValue, toDateStr,
                            fromDateStr);
                } catch (Exception e) {
                    log.warn("getHistoricalData", "Failed to fetch historical data via SDK: " + e.getMessage());
                }
            }

            // 2. Try API Service if SDK failed or returned empty
            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                log.info("getHistoricalData",
                        "Fetching historical data via API for instrument key: " + instrumentKey + ", unit: " + unit
                                + ", interval: " + intervalValue);
                response = upstoxApiService.getHistoricalCandleData(instrumentKey, "day", toDateStr,
                        fromDateStr);
            }

            // Map to Common HistoricalData model
            HistoricalData historicalData = new HistoricalData();
            if (response != null && response.getData() != null) {
                List<OHLCVTPoint> dataPoints = new ArrayList<>();
                for (HistoricalDataResponse.Candle candle : response.getData()) {
                    OHLCVTPoint point = new OHLCVTPoint();
                    try {
                        // Parse timestamp
                        // Upstox sample: "2024-04-12T00:00:00+05:30"
                        if (candle.getTimestamp() != null) {
                            // Using Instant parser for ISO 8601 strings
                            java.time.Instant instant = java.time.Instant
                                    .parse(candle.getTimestamp().replace("+0530", "+05:30"));
                            point.setTime(java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()));
                        } else {
                            point.setTime(java.time.LocalDateTime.now());
                        }
                    } catch (Exception e) {
                        try {
                            // Fallback date only
                            java.time.LocalDate ld = java.time.LocalDate.parse(candle.getTimestamp());
                            point.setTime(ld.atStartOfDay());
                        } catch (Exception ex) {
                            point.setTime(java.time.LocalDateTime.now());
                        }
                    }

                    point.setOpen(candle.getOpen());
                    point.setHigh(candle.getHigh());
                    point.setLow(candle.getLow());
                    point.setClose(candle.getClose());
                    point.setVolume(candle.getVolume() != null ? candle.getVolume() : 0L);

                    dataPoints.add(point);
                }
                historicalData.setDataPoints(dataPoints);
            }

            return historicalData;
        } catch (Exception e) {
            log.error("getHistoricalData", "Error fetching Upstox historical data", e);
            return new HistoricalData();
        }
    }

    private int getUpstoxIntervalValue(TimeFrame interval) {
        if (interval == null)
            return 1;
        switch (interval) {
            case MINUTE:
                return 1;
            case FIVE_MINUTE:
                return 5;
            case TEN_MINUTE:
                return 10;
            case FIFTEEN_MINUTE:
                return 15;
            case THIRTY_MINUTE:
                return 30;
            case HOUR:
                return 1;
            case DAY:
                return 1;
            case WEEK:
                return 1;
            case MONTH:
                return 1;
            default:
                return 1;
        }
    }

    private String mapToUpstoxInterval(TimeFrame interval) {
        if (interval == null)
            return "minutes";
        switch (interval) {
            case MINUTE:
                return "minutes";
            case FIVE_MINUTE:
                return "minutes";
            case FIFTEEN_MINUTE:
                return "minutes";
            case THIRTY_MINUTE:
                return "minutes";
            case HOUR:
                return "hours";
            case DAY:
                return "days";
            case WEEK:
                return "weeks";
            case MONTH:
                return "months";
            default:
                return "days";
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
