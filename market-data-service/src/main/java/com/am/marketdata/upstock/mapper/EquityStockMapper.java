package com.am.marketdata.upstock.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.marketdata.upstock.model.common.StockQuote;

@Component
public class EquityStockMapper {
    

    public List<EquityPrice> getEquityPrices(List<StockQuote> marketQuotes) {
        if (marketQuotes == null || marketQuotes.isEmpty()) {
            return List.of();
        }
        return marketQuotes.stream().map(this::getEquityPrice).collect(Collectors.toList());
    }

    public EquityPrice getEquityPrice(StockQuote stockQuote) {
        return EquityPrice.builder()
            .symbol(stockQuote.getSymbol())
            .open(stockQuote.getOpenPrice())
            .high(stockQuote.getHighPrice())
            .low(stockQuote.getLowPrice())
            .close(stockQuote.getClosePrice())
            .volume(stockQuote.getVolume())
            .time(stockQuote.getLastUpdateTime().toInstant())
            .build();
    }
}
