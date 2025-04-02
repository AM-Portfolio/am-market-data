package com.am.marketdata.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.model.events.StockInsidicesEventData;
import com.am.marketdata.kafka.config.KafkaProperties;
import com.am.common.investment.model.events.EquityPriceUpdateEvent;
import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockIndicesPriceUpdateEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final BaseKafkaProducer<EquityPriceUpdateEvent> equityProducer;
    private final BaseKafkaProducer<StockIndicesPriceUpdateEvent> stockIndicesProducer;
    private final BaseKafkaProducer<MarketIndexIndicesPriceUpdateEvent> indicesProducer;
    private final KafkaProperties kafkaProperties;

    public void sendEquityPriceUpdates(List<EquityPrice> equityPrices) {
        var event = EquityPriceUpdateEvent.builder()
            .eventType("EQUITY_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .equityPrices(equityPrices)
            .build();
        
        equityProducer.sendEvent(event, kafkaProperties.getTopics().getStockPrice(), event.getEventType(), event.getTimestamp());
    }

    public void sendStockIndicesUpdate(StockInsidicesEventData stockIndice) {
        var event = StockIndicesPriceUpdateEvent.builder()
            .eventType("STOCK_INDICES_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .stockIndices(stockIndice)
            .build();
        
        stockIndicesProducer.sendEvent(event, kafkaProperties.getTopics().getStockIndices(), event.getEventType(), event.getTimestamp());
    }

    public void sendIndicesUpdate(List<MarketIndexIndices> marketIndexIndices) {
        var event = MarketIndexIndicesPriceUpdateEvent.builder()
            .eventType("MARKET_INDICES_PRICE_UPDATE")  
            .timestamp(LocalDateTime.now())
            .marketIndices(marketIndexIndices)
            .build();
        
        indicesProducer.sendEvent(event, kafkaProperties.getTopics().getNseIndices(), event.getEventType(), event.getTimestamp());
    }
}
