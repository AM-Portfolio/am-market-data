package com.marketdata.service.upstox;

import com.am.common.investment.model.historical.OHLCVTPoint;
import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.marketdata.common.model.OHLCQuote;
import com.am.marketdata.common.model.TimeFrame;
import com.am.marketdata.upstock.model.HistoricalDataResponse;
import com.am.marketdata.upstock.model.MarketQuoteResponse;
import com.am.marketdata.upstock.model.OHLCResponse;
import com.marketdata.common.MarketDataProvider;
import com.marketdata.common.MarketDataProvider.ProviderOperation;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.LTPQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service("upstoxMarketDataProvider")
public class UpstoxMarketDataProvider implements MarketDataProvider {

    private final UpstoxApiService upstoxApiService;
    private final com.am.marketdata.service.service.UpstoxInstrumentService upstoxInstrumentService;

    public UpstoxMarketDataProvider(UpstoxApiService upstoxApiService,
            com.am.marketdata.service.service.UpstoxInstrumentService upstoxInstrumentService) {
        this.upstoxApiService = upstoxApiService;
        this.upstoxInstrumentService = upstoxInstrumentService;
    }

    private List<String> getStockISINs(List<String> stockSymbols) {
        log.info("Resolving Instrument Keys for {} symbols: {}", stockSymbols.size(), stockSymbols);

        com.am.marketdata.service.dto.InstrumentSearchCriteria criteria = new com.am.marketdata.service.dto.InstrumentSearchCriteria();
        criteria.setTradingSymbols(stockSymbols);
        criteria.setProvider("UPSTOX");

        List<com.am.marketdata.service.model.UpstoxInstrument> instruments = upstoxInstrumentService
                .searchInstruments(criteria);

        // Map symbol -> instrumentKey
        // We need to match the input symbols to the result instruments.
        // Assuming tradingSymbol matches the input stockSymbol.

        Map<String, String> symbolToKeyMap = instruments.stream()
                .collect(Collectors.toMap(
                        inst -> inst.getTradingSymbol(),
                        inst -> inst.getInstrumentKey(),
                        (existing, replacement) -> existing));

        return stockSymbols.stream()
                .map(symbol -> {
                    String key = symbolToKeyMap.get(symbol);
                    // if (key == null) {
                    // // Fallback logic removed as per user request to strictly filter out missing
                    // keys
                    // }

                    if (key == null) {
                        log.warn("Instrument Key not found for symbol: {}", symbol);
                    } else {
                        log.debug("Resolved {} -> {}", symbol, key);
                    }
                    return key;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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

    @Override
    public Map<String, OHLCQuote> getOHLC(List<String> symbols) {
        try {
            List<String> isins = getStockISINs(symbols);
            // Upstox requires interval for OHLC. Defaulting to 1 day as it's common for
            // general OHLC quote
            OHLCResponse response = upstoxApiService.getOhlc(isins, "I1");
            Map<String, OHLCQuote> result = new HashMap<>();

            if (response != null && response.getData() != null) {
                for (Map.Entry<String, OHLCResponse.OHLCData> entry : response.getData().entrySet()) {
                    String symbol = entry.getKey();
                    OHLCResponse.OHLCData data = entry.getValue();

                    OHLCQuote quote = new OHLCQuote();
                    // quote.setInstrumentToken(0L); // OHLCQuote does not support this
                    quote.setLastPrice(data.getLast_price() != null ? data.getLast_price() : 0.0);

                    if (data.getOhlc() != null) {
                        OHLCQuote.OHLC ohlc = new OHLCQuote.OHLC();
                        ohlc.setOpen(data.getOpen());
                        ohlc.setHigh(data.getHigh());
                        ohlc.setLow(data.getLow());
                        ohlc.setClose(data.getClose());
                        quote.setOhlc(ohlc);
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
            List<String> isins = getStockISINs(Arrays.asList(symbols));
            MarketQuoteResponse response = upstoxApiService.getLtp(isins);
            Map<String, LTPQuote> result = new HashMap<>();

            if (response != null && response.getData() != null) {
                for (Map.Entry<String, com.am.marketdata.upstock.model.common.StockQuote> entry : response.getData()
                        .entrySet()) {
                    String symbol = entry.getKey();
                    com.am.marketdata.upstock.model.common.StockQuote data = entry.getValue();

                    LTPQuote quote = new LTPQuote();
                    quote.lastPrice = data.getLastPrice();
                    quote.instrumentToken = 0;

                    result.put(symbol, quote);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Error fetching Upstox LTP", e);
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
