package com.am.marketdata.mapper;

import com.am.common.investment.model.equity.EquityPrice;
import com.zerodhatech.models.OHLCQuote;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper class to convert between OHLCQuote and EquityPrice objects
 */
@Component
public class OHLCMapper {

    /**
     * Convert an OHLCQuote to an EquityPrice object
     *
     * @param symbol The trading symbol
     * @param ohlcQuote The OHLCQuote object
     * @return EquityPrice object
     */
    public EquityPrice toEquityPrice(String symbol, OHLCQuote ohlcQuote) {
        if (ohlcQuote == null || ohlcQuote.ohlc == null) {
            return null;
        }
        
        // Clean symbol (remove exchange prefix if present)
        String cleanSymbol = symbol.replace("NSE:", "");
        
        return EquityPrice.builder()
            .symbol(cleanSymbol)
            .open(ohlcQuote.ohlc.open)
            .high(ohlcQuote.ohlc.high)
            .low(ohlcQuote.ohlc.low)
            .close(ohlcQuote.ohlc.close)
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
    public List<EquityPrice> toEquityPriceList(Map<String, OHLCQuote> ohlcData) {
        if (ohlcData == null || ohlcData.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<EquityPrice> prices = new ArrayList<>(ohlcData.size());
        
        for (Map.Entry<String, OHLCQuote> entry : ohlcData.entrySet()) {
            EquityPrice price = toEquityPrice(entry.getKey(), entry.getValue());
            if (price != null) {
                prices.add(price);
            }
        }
        
        return prices;
    }
}
