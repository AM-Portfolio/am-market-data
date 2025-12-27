package com.am.marketdata.service.mapper;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.historical.OHLCVTPoint;
import com.am.marketdata.common.model.OHLCQuoteV1;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper class to convert between OHLCQuoteV1 and EquityPrice objects
 */
@Component
public class OHLCMapper {

    /**
     * Convert a Zerodha OHLCQuoteV1 to our service OHLCQuoteV1 model
     *
     * @param zerodhaQuote The Zerodha OHLCQuoteV1 object
     * @return Service layer OHLCQuoteV1 object
     */
    public OHLCQuoteV1 toServiceOHLCQuote(com.zerodhatech.models.OHLCQuote zerodhaQuote) {
        if (zerodhaQuote == null) {
            return null;
        }

        OHLCQuoteV1 ohlcQuote = new OHLCQuoteV1();
        ohlcQuote.setLastPrice(zerodhaQuote.lastPrice);

        // Create and set the nested OHLC object
        OHLCQuoteV1.OHLC ohlc = new OHLCQuoteV1.OHLC();
        if (zerodhaQuote.ohlc != null) {
            ohlc.setOpen(zerodhaQuote.ohlc.open);
            ohlc.setHigh(zerodhaQuote.ohlc.high);
            ohlc.setLow(zerodhaQuote.ohlc.low);
            ohlc.setClose(zerodhaQuote.ohlc.close);
        }
        ohlcQuote.setOhlc(ohlc);

        return ohlcQuote;
    }

    /**
     * Convert a map of Zerodha OHLCQuotes to a map of service OHLCQuotes
     *
     * @param zerodhaOhlcMap Map of symbol to Zerodha OHLCQuote
     * @return Map of symbol to service OHLCQuote
     */
    public Map<String, OHLCQuoteV1> toServiceOHLCQuoteMap(
            Map<String, com.zerodhatech.models.OHLCQuote> zerodhaOhlcMap) {
        if (zerodhaOhlcMap == null || zerodhaOhlcMap.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, OHLCQuoteV1> result = new HashMap<>(zerodhaOhlcMap.size());

        for (Map.Entry<String, com.zerodhatech.models.OHLCQuote> entry : zerodhaOhlcMap.entrySet()) {
            result.put(entry.getKey(), toServiceOHLCQuote(entry.getValue()));
        }

        return result;
    }

    /**
     * Convert an OHLCQuoteV1 to an EquityPrice object
     *
     * @param symbol      The trading symbol
     * @param OHLCQuoteV1 The OHLCQuoteV1 object
     * @return EquityPrice object
     */
    public EquityPrice toEquityPrice(String symbol, OHLCQuoteV1 ohlcQuote) {
        if (ohlcQuote == null || ohlcQuote.getOhlc() == null) {
            return null;
        }

        // Clean symbol (remove exchange prefix if present)
        String cleanSymbol = symbol.replace("NSE:", "");

        return EquityPrice.builder()
                .symbol(cleanSymbol)
                .lastPrice(ohlcQuote.getLastPrice())
                .ohlcv(OHLCVTPoint.builder().open(ohlcQuote.getOhlc().getOpen()).high(ohlcQuote.getOhlc().getHigh())
                        .low(ohlcQuote.getOhlc().getLow()).close(ohlcQuote.getOhlc().getClose()).build())
                .time(ZonedDateTime.now().toInstant())
                .exchange("NSE")
                .build();
    }

    /**
     * Convert a map of OHLCQuotes to a list of EquityPrice objects
     *
     * @param ohlcData Map of symbol to OHLCQuote
     * @return List of EquityPrice objects
     */
    public List<EquityPrice> toEquityPriceList(Map<String, OHLCQuoteV1> ohlcData) {
        if (ohlcData == null || ohlcData.isEmpty()) {
            return new ArrayList<>();
        }

        List<EquityPrice> prices = new ArrayList<>(ohlcData.size());

        for (Map.Entry<String, OHLCQuoteV1> entry : ohlcData.entrySet()) {
            EquityPrice price = toEquityPrice(entry.getKey(), entry.getValue());
            if (price != null) {
                prices.add(price);
            }
        }

        return prices;
    }
}
