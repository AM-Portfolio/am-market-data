package com.am.marketdata.provider.zerodha;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.model.historical.OHLCVTPoint;
import com.am.marketdata.common.model.InstrumentV1;
import com.am.marketdata.common.model.OHLCQuoteV1;
import com.am.marketdata.common.model.TimeFrameV1;
import com.am.marketdata.provider.AMMarketDataProvider;
import com.am.marketdata.provider.dto.InstrumentSearchCriteria;
import com.am.marketdata.provider.zerodha.model.ZerodhaInstrument;
import com.am.marketdata.provider.zerodha.service.ZerodhaApiService;
import com.am.marketdata.provider.zerodha.service.ZerodhaInstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service("zerodhaMarketDataProvider")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "market-data.provider.type", havingValue = "zerodha")
@RequiredArgsConstructor
public class ZerodhaMarketDataProvider implements AMMarketDataProvider {

    private final ZerodhaApiService zerodhaApiService;
    private final ZerodhaInstrumentService instrumentService;

    private static final String PROVIDER_NAME = "ZERODHA";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public void login(String apiKey, String apiSecret) {
        // Zerodha login is OAuth based via generateSession logic usually
        // Typically triggered via web flow.
    }

    @Override
    public void logout() {
        zerodhaApiService.logout();
    }

    @Override
    public boolean isSessionValid() {
        try {
            return zerodhaApiService.getProfile() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String generateSession(String requestToken) {
        com.zerodhatech.models.User user = zerodhaApiService.generateSession(requestToken);
        return user.accessToken;
    }

    @Override
    public Map<String, OHLCQuoteV1> getQuotes(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, OHLCQuoteV1> resultMap = new HashMap<>();
        try {
            // Zerodha's getOHLC uses "NSE:SYMBOL" format
            String[] symbolArray = symbols.stream()
                    .map(s -> s.startsWith("NSE:") ? s : "NSE:" + s)
                    .toArray(String[]::new);

            // Map common OHLCQuoteV1 from Zerodha model
            Map<String, com.zerodhatech.models.OHLCQuote> zerodhaQuotes = zerodhaApiService.getOHLC(symbolArray);

            if (zerodhaQuotes != null) {
                for (Map.Entry<String, com.zerodhatech.models.OHLCQuote> entry : zerodhaQuotes.entrySet()) {
                    // Try to match key back to input symbol (handle NSE: prefix)
                    String key = entry.getKey();
                    String cleanSymbol = key.contains(":") ? key.split(":")[1] : key;

                    resultMap.put(cleanSymbol, mapToCommonOHLC(entry.getValue()));
                }
            }
        } catch (Exception e) {
            log.error("Error getQuotes", e);
        }
        return resultMap;
    }

    @Override
    public Map<String, HistoricalData> getHistoricalData(List<String> symbols, String from, String to,
            String interval) {
        Map<String, HistoricalData> resultMap = new HashMap<>();

        Date fromDate = parseDate(from);
        Date toDate = parseDate(to);
        // Map interval string (e.g. "1d") to TimeFrameV1 enum if needed by logic
        TimeFrameV1 timeFrame = mapInterval(interval);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String symbol : symbols) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    com.zerodhatech.models.HistoricalData result = zerodhaApiService.getHistoricalData(symbol, fromDate,
                            toDate, timeFrame, false, false);
                    if (result != null && result.dataArrayList != null) {
                        HistoricalData data = mapToCommonHistory(result, symbol);
                        synchronized (resultMap) {
                            resultMap.put(symbol, data);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error fetching history for {}", symbol, e);
                }
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return resultMap;
    }

    @Override
    public List<InstrumentV1> getInstruments(String exchange) {
        InstrumentSearchCriteria criteria = new InstrumentSearchCriteria();
        criteria.setExchanges(Collections.singletonList(exchange));

        List<ZerodhaInstrument> zList = instrumentService.searchInstruments(criteria);
        return zList.stream().map(i -> mapToCommonInstrument(i)).collect(Collectors.toList());
    }

    @Override
    public List<InstrumentV1> searchInstruments(String query) {
        InstrumentSearchCriteria criteria = new InstrumentSearchCriteria();
        criteria.setQueries(Collections.singletonList(query));

        List<ZerodhaInstrument> zList = instrumentService.searchInstruments(criteria);
        return zList.stream().map(i -> mapToCommonInstrument(i)).collect(Collectors.toList());
    }

    // --- Mappers ---

    private OHLCQuoteV1 mapToCommonOHLC(com.zerodhatech.models.OHLCQuote zQuote) {
        OHLCQuoteV1 q = new OHLCQuoteV1();
        q.setLastPrice(zQuote.lastPrice);
        if (zQuote.ohlc != null) {
            OHLCQuoteV1.OHLC ohlc = new OHLCQuoteV1.OHLC();
            ohlc.setOpen(zQuote.ohlc.open);
            ohlc.setHigh(zQuote.ohlc.high);
            ohlc.setLow(zQuote.ohlc.low);
            ohlc.setClose(zQuote.ohlc.close);
            q.setOhlc(ohlc);
        }
        return q;
    }

    private HistoricalData mapToCommonHistory(com.zerodhatech.models.HistoricalData zData, String symbol) {
        HistoricalData data = new HistoricalData();
        data.setTradingSymbol(symbol);
        List<OHLCVTPoint> points = new ArrayList<>();

        if (zData.dataArrayList != null) {
            for (com.zerodhatech.models.HistoricalData zPoint : zData.dataArrayList) {
                OHLCVTPoint p = new OHLCVTPoint();
                try {
                    // Parse ISO-8601 string to LocalDateTime
                    // e.g. 2023-10-01T09:15:00+0530
                    String ts = zPoint.timeStamp;
                    // Handle potential format variations
                    java.time.Instant instant;
                    if (ts != null) {
                        // Fix zone format if needed
                        if (ts.contains("+0530") && !ts.contains(":")) {
                            ts = ts.replace("+0530", "+05:30");
                        }
                        instant = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(ts,
                                java.time.Instant::from);
                        p.setTime(java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()));
                    } else {
                        p.setTime(java.time.LocalDateTime.now());
                    }
                } catch (Exception e) {
                    log.debug("Date parsing failed for {}: {}", zPoint.timeStamp, e.getMessage());
                    p.setTime(java.time.LocalDateTime.now());
                }
                p.setOpen(zPoint.open);
                p.setHigh(zPoint.high);
                p.setLow(zPoint.low);
                p.setClose(zPoint.close);
                p.setVolume((long) zPoint.volume);
                points.add(p);
            }
        }
        data.setDataPoints(points);
        return data;
    }

    private InstrumentV1 mapToCommonInstrument(ZerodhaInstrument zInst) {
        return InstrumentV1.builder()
                .instrumentToken(zInst.getInstrumentToken())
                .tradingSymbol(zInst.getTradingSymbol())
                .name(zInst.getName())
                .exchange(zInst.getExchange())
                .exchangeToken(zInst.getExchangeToken())
                .expiry(zInst.getExpiry())
                .instrumentType(zInst.getInstrumentType())
                .lotSize(zInst.getLotSize() != null ? zInst.getLotSize().doubleValue() : null)
                .tickSize(zInst.getTickSize())
                .strikePrice(zInst.getStrike())
                .segment(zInst.getSegment())
                .build();
    }

    private Date parseDate(String d) {
        try {
            return java.sql.Date.valueOf(d);
        } catch (Exception e) {
            try {
                // Try ISO
                return java.util.Date
                        .from(java.time.LocalDate.parse(d).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            } catch (Exception ex) {
                return new Date();
            }
        }
    }

    // Unused method removed: parseDateIso

    private TimeFrameV1 mapInterval(String interval) {
        // Map "1d" -> TimeFrameV1.DAY
        if ("1d".equalsIgnoreCase(interval))
            return TimeFrameV1.DAY;
        if ("1m".equalsIgnoreCase(interval))
            return TimeFrameV1.MINUTE;
        // Default
        return TimeFrameV1.DAY;
    }

}
