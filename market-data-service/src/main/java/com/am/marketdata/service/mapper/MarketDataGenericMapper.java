package com.am.marketdata.service.mapper;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.historical.OHLCVTPoint;
import com.am.marketdata.common.model.OHLCQuoteV1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper for converting OHLC data to EquityPrice objects
 */
@Component
public class MarketDataGenericMapper {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarketDataGenericMapper.class);

    /**
     * Convert LTP data to EquityPrice objects
     *
     * @param ohlcData      Map of trading symbol to OHLC quote
     * @param instrumentMap Map of trading symbol to InstrumentV1
     * @return List of EquityPrice objects
     */
    public List<EquityPrice> mapLTPquoteToEquityPrices(Map<String, OHLCQuoteV1> ltpData) {
        List<EquityPrice> equityPrices = new ArrayList<>();

        if (ltpData == null || ltpData.isEmpty()) {
            log.warn("No OHLC data to map");
            return equityPrices;
        }

        for (Map.Entry<String, OHLCQuoteV1> entry : ltpData.entrySet()) {
            String key = entry.getKey();
            OHLCQuoteV1 quote = entry.getValue();

            try {
                // Parse the key to extract exchange and symbol
                String exchange = "NSE"; // Default exchange
                String symbol = key;

                // If key contains a colon, split it to get exchange and symbol
                if (key.contains(":")) {
                    String[] parts = key.split(":", 2);
                    exchange = parts[0];
                    symbol = parts[1];
                }

                // Create and populate EquityPrice object
                EquityPrice price = new EquityPrice();
                price.setSymbol(symbol);
                price.setLastPrice(quote.getLastPrice());
                price.setExchange(exchange);
                equityPrices.add(price);
            } catch (Exception e) {
                log.error("Error mapping LTP data for symbol {}: {}", key, e.getMessage(), e);
            }
        }

        log.info("Mapped {} LTP quotes to equity prices", equityPrices.size());
        return equityPrices;
    }

    /**
     * Convert LTP data to EquityPrice objects
     *
     * @param ohlcData      Map of trading symbol to OHLC quote
     * @param instrumentMap Map of trading symbol to InstrumentV1
     * @return List of EquityPrice objects
     */
    public List<EquityPrice> mapOHLCquoteToEquityPrices(Map<String, OHLCQuoteV1> ohlcData) {
        List<EquityPrice> equityPrices = new ArrayList<>();

        if (ohlcData == null || ohlcData.isEmpty()) {
            log.warn("No OHLC data to map");
            return equityPrices;
        }

        for (Map.Entry<String, OHLCQuoteV1> entry : ohlcData.entrySet()) {
            String key = entry.getKey();
            OHLCQuoteV1 quote = entry.getValue();

            try {
                // Parse the key to extract exchange and symbol
                String exchange = "NSE"; // Default exchange
                String symbol = key;

                // If key contains a colon, split it to get exchange and symbol
                if (key.contains(":")) {
                    String[] parts = key.split(":", 2);
                    exchange = parts[0];
                    symbol = parts[1];
                }

                // Create and populate EquityPrice object
                EquityPrice price = new EquityPrice();
                price.setSymbol(symbol);
                if (quote.getOhlc() != null) {
                    price.setOhlcv(OHLCVTPoint.builder().open(quote.getOhlc().getOpen()).high(quote.getOhlc().getHigh())
                            .low(quote.getOhlc().getLow())
                            .close(quote.getOhlc().getClose()).build());
                }
                price.setExchange(exchange);
                equityPrices.add(price);
            } catch (Exception e) {
                log.error("Error mapping OHLC data for symbol {}: {}", key, e.getMessage(), e);
            }
        }

        log.info("Mapped {} OHLC quotes to equity prices", equityPrices.size());
        return equityPrices;
    }

}
