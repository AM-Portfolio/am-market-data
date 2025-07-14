package com.am.marketdata.mapper;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.Instrument;
import com.zerodhatech.models.OHLCQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper for converting OHLC data to EquityPrice objects
 */
@Slf4j
@Component
public class OHLCMapper {

    /**
     * Convert OHLC data to EquityPrice objects
     *
     * @param ohlcData Map of trading symbol to OHLC quote
     * @param instrumentMap Map of trading symbol to Instrument
     * @return List of EquityPrice objects
     */
    public List<EquityPrice> toEquityPrices(Map<String, OHLCQuote> ohlcData, Map<String, Instrument> instrumentMap) {
        List<EquityPrice> equityPrices = new ArrayList<>();
        
        if (ohlcData == null || ohlcData.isEmpty()) {
            log.warn("No OHLC data to map");
            return equityPrices;
        }
        
        for (Map.Entry<String, OHLCQuote> entry : ohlcData.entrySet()) {
            String symbol = entry.getKey();
            OHLCQuote quote = entry.getValue();
            
            Instrument instrument = instrumentMap.get(symbol);
            if (instrument == null) {
                log.debug("No instrument found for symbol: {}", symbol);
                continue;
            }
            
            try {
                // Create and populate EquityPrice object
                EquityPrice price = new EquityPrice();
                mapInstrumentFields(price, instrument);
                mapOhlcFields(price, quote);
                equityPrices.add(price);
            } catch (Exception e) {
                log.error("Error mapping OHLC data for symbol {}: {}", symbol, e.getMessage(), e);
            }
        }
        
        log.info("Mapped {} OHLC quotes to equity prices", equityPrices.size());
        return equityPrices;
    }
    
    /**
     * Maps instrument fields to the equity price object
     * 
     * @param price The equity price object to populate
     * @param instrument The instrument data
     */
    private void mapInstrumentFields(EquityPrice price, Instrument instrument) {
       price.setSymbol(instrument.getTradingSymbol());
       price.setIsin(instrument.getIsin());
    }
    
    /**
     * Maps OHLC fields to the equity price object
     * 
     * @param price The equity price object to populate
     * @param quote The OHLC quote data
     */
    private void mapOhlcFields(EquityPrice price, OHLCQuote quote) {
        price.setOpen(quote.ohlc.open);
        price.setHigh(quote.ohlc.high);
        price.setLow(quote.ohlc.low);
        price.setClose(quote.ohlc.close);
    }
}
