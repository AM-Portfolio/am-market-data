package com.am.marketdata.provider.upstox;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.model.historical.OHLCVTPoint;
import com.am.marketdata.common.model.Instrument;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.provider.MarketDataProvider;
import com.am.marketdata.provider.dto.InstrumentSearchCriteria;
import com.am.marketdata.provider.upstox.model.UpstoxInstrument;
import com.am.marketdata.provider.upstox.service.UpstoxApiService;
import com.am.marketdata.provider.upstox.service.UpstoxInstrumentService;
import com.am.marketdata.provider.upstox.service.UpstoxSdkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service("upstoxMarketDataProvider") // Bean name for selection
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "market-data.provider.type", havingValue = "upstox")
@RequiredArgsConstructor
public class UpstoxMarketDataProvider implements MarketDataProvider {

    private final UpstoxApiService upstoxApiService;
    private final UpstoxSdkService upstoxSdkService;
    private final UpstoxInstrumentService upstoxInstrumentService;
    private final UpstoxIndexIdentifier indexIdentifier;

    private static final String PROVIDER_NAME = "UPSTOX";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public Map<String, OHLCQuote> getQuotes(List<String> symbols) {
        // Resolve symbols to keys
        Map<String, String> symbolToKeyMap = resolveInstruments(symbols);
        List<String> validKeys = new ArrayList<>(symbolToKeyMap.values());

        if (validKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, OHLCQuote> resultMap = new HashMap<>();

        try {
            // Try SDK First
            com.am.marketdata.provider.upstox.model.OHLCResponse response = upstoxSdkService.getOhlc(validKeys, "I1"); // "I1" usually means 1 minute or daily? Check API docs. Usually "I30" or similar.
            // Wait, getQuotes typically means "LTP" or "Live Quote".
            // Interface says getQuotes returns OHLCQuote.
            // Upstox API "full" quote gives OHLC.
            
            // If SDK fails or returns empty, logic:
            if (response != null && "success".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                for (Map.Entry<String, String> entry : symbolToKeyMap.entrySet()) {
                    String symbol = entry.getKey();
                    String key = entry.getValue();
                    
                    if (response.getData().containsKey(key)) {
                        com.am.marketdata.provider.upstox.model.OHLCResponse.OHLCData data = response.getData().get(key);
                        resultMap.put(symbol, mapToCommonOHLC(data));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching quotes from Upstox SDK", e);
            // Fallback to API Service?
        }
        
        return resultMap;
    }

    @Override
    public Map<String, HistoricalData> getHistoricalData(List<String> symbols, String from, String to, String interval) {
        Map<String, HistoricalData> resultMap = new HashMap<>();
        Map<String, String> symbolToKeyMap = resolveInstruments(symbols);

        // Parallel processing for historical data
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<String, String> entry : symbolToKeyMap.entrySet()) {
            String symbol = entry.getKey();
            String key = entry.getValue();

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Fetch data
                    com.am.marketdata.provider.upstox.model.HistoricalDataResponse response = 
                        upstoxSdkService.getHistoricalCandleData(key, interval, 1, to, from); // interval logic check needed
                        
                    if (response != null && "success".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        HistoricalData data = mapToHistoricalData(response.getData().getCandles(), symbol);
                        synchronized (resultMap) {
                            resultMap.put(symbol, data);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error fetching historical data for {}", symbol, e);
                }
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return resultMap;
    }

    @Override
    public List<Instrument> getInstruments(String exchange) {
        // Use InstrumentService to search
        InstrumentSearchCriteria criteria = new InstrumentSearchCriteria();
        criteria.setExchanges(Collections.singletonList(exchange));
        
        List<UpstoxInstrument> instruments = upstoxInstrumentService.searchInstruments(criteria);
        return instruments.stream().map(this::mapToCommonInstrument).collect(Collectors.toList());
    }

    @Override
    public List<Instrument> searchInstruments(String query) {
        InstrumentSearchCriteria criteria = new InstrumentSearchCriteria();
        criteria.setQueries(Collections.singletonList(query));
        
        List<UpstoxInstrument> instruments = upstoxInstrumentService.searchInstruments(criteria);
        return instruments.stream().map(this::mapToCommonInstrument).collect(Collectors.toList());
    }

    @Override
    public void login(String apiKey, String apiSecret) {
        // Upstox uses OAuth code flow, managed via generateSession
    }

    @Override
    public void logout() {
        upstoxApiService.setAccessToken(null);
    }

    @Override
    public boolean isSessionValid() {
        return upstoxApiService.getAccessToken() != null;
    }

    @Override
    public String generateSession(String requestToken) {
        // This returns JSON string usually, check interface
        // Interface returns String (access token)
        Object session = upstoxApiService.generateSession(requestToken);
        // upstoxApiService handles caching
        return upstoxApiService.getAccessToken();
    }
    
    // --- Private Helpers ---

    private Map<String, String> resolveInstruments(List<String> symbols) {
        Map<String, String> map = new HashMap<>();
        
        // 1. Resolve Indices first
        Map<String, String> indicesMap = indexIdentifier.resolveIndices(symbols);
        map.putAll(indicesMap);
        
        // 2. Resolve Stocks/Others from DB
        List<String> remainingSymbols = new ArrayList<>(symbols);
        remainingSymbols.removeAll(indicesMap.keySet());
        
        if (!remainingSymbols.isEmpty()) {
            List<UpstoxInstrument> instruments = upstoxInstrumentService.searchInstruments(
                new InstrumentSearchCriteria(null, null, null, null, null, remainingSymbols, null, "UPSTOX"));
                
            for (UpstoxInstrument inst : instruments) {
                // Map based on trading symbol or asset symbol matching
                if (remainingSymbols.contains(inst.getTradingSymbol())) {
                   map.put(inst.getTradingSymbol(), inst.getInstrumentKey());
                } else if (remainingSymbols.contains(inst.getAssetSymbol())) {
                   map.put(inst.getAssetSymbol(), inst.getInstrumentKey());
                }
            }
        }
        
        return map;
    }

    private OHLCQuote mapToCommonOHLC(com.am.marketdata.provider.upstox.model.OHLCResponse.OHLCData data) {
        OHLCQuote quote = new OHLCQuote();
        quote.setLastPrice(data.getLast_price());
        
        if (data.getOhlc() != null) {
            OHLCQuote.OHLC ohlc = new OHLCQuote.OHLC();
            ohlc.setOpen(data.getOhlc().getOpen());
            ohlc.setHigh(data.getOhlc().getHigh());
            ohlc.setLow(data.getOhlc().getLow());
            ohlc.setClose(data.getOhlc().getClose());
            quote.setOhlc(ohlc);
        }
        if (data.getPrevious_close() != null) {
             // quote.setPreviousClose(data.getPrevious_close());
        }
        return quote;
    }
    
    private HistoricalData mapToHistoricalData(List<List<Object>> candles, String symbol) {
        HistoricalData data = new HistoricalData();
        data.setTradingSymbol(symbol);
        List<OHLCVTPoint> points = new ArrayList<>();
        
        if (candles != null) {
            for (List<Object> candle : candles) {
                // [timestamp, open, high, low, close, volume, oi]
                try {
                    String timestampStr = (String) candle.get(0);
                    
                    OHLCVTPoint point = new OHLCVTPoint();
                    point.setTime(parseDateToLDT(timestampStr));
                    point.setOpen(toDouble(candle.get(1)));
                    point.setHigh(toDouble(candle.get(2)));
                    point.setLow(toDouble(candle.get(3)));
                    point.setClose(toDouble(candle.get(4)));
                    point.setVolume(toLong(candle.get(5)));
                    
                    points.add(point);
                } catch (Exception e) {
                   log.debug("Error parsing candle for {}: {}", symbol, candle);
                }
            }
        }
        data.setDataPoints(points);
        return data;
    }

    private Instrument mapToCommonInstrument(UpstoxInstrument uInst) {
        return Instrument.builder()
            .instrumentToken(uInst.getInstrumentKey())
            .tradingSymbol(uInst.getTradingSymbol())
            .name(uInst.getName())
            .exchange(uInst.getExchange())
            .exchangeToken(uInst.getExchangeToken())
            .expiry(uInst.getExpiry() != null ? uInst.getExpiry().toString() : null)
            .instrumentType(uInst.getInstrumentType())
            //.isin(uInst.getIsin()) // ISIN not in common Instrument builder yet? Check Common.
            .lotSize(uInst.getLotSize())
            .tickSize(uInst.getTickSize())
            .strikePrice(uInst.getStrikePrice())
            .segment(uInst.getSegment())
            .build();
    }
    
    private double toDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }
    
    private long toLong(Object val) {
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }
    
    private java.time.LocalDateTime parseDateToLDT(String dateStr) {
        try {
             return java.time.LocalDateTime.ofInstant(
                 java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(dateStr, java.time.Instant::from),
                 java.time.ZoneId.systemDefault()
             );
        } catch (Exception e) {
            return java.time.LocalDateTime.now();
        }
    }
}
