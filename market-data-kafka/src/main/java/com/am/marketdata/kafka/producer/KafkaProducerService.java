package com.am.marketdata.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockInsidicesEventData;
import com.am.common.investment.model.events.EquityPriceUpdateEvent;

@Slf4j
@Service
public class KafkaProducerService {

    private final BaseKafkaProducer<EquityPriceUpdateEvent> equityProducer;
    private final BaseKafkaProducer<StockIndicesPriceUpdateEvent> stockIndicesProducer;
    private final BaseKafkaProducer<MarketIndexIndicesPriceUpdateEvent> indicesProducer;

    @Value("${app.kafka.stock-price-topic}")
    private String stockPriceTopic;

    @Value("${app.kafka.stock-indices-topic}")
    private String stockIndicesTopic;

    @Value("${app.kafka.nse-indices-topic}")
    private String nseIndicesTopic;
    
    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.equityProducer = new BaseKafkaProducer<>(kafkaTemplate) {};
        this.stockIndicesProducer = new BaseKafkaProducer<>(kafkaTemplate) {};
        this.indicesProducer = new BaseKafkaProducer<>(kafkaTemplate) {};
    }

    public void sendEquityPriceUpdates(List<EquityPrice> equityPrices) {
        var event = EquityPriceUpdateEvent.builder()
            .eventType("EQUITY_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .equityPrices(equityPrices)
            .build();
        
        equityProducer.sendEvent(event, stockPriceTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendStockIndicesUpdate(StockInsidicesEventData stockIndice) {
        var event = StockIndicesPriceUpdateEvent.builder()
            .eventType("STOCK_INDICES_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .stockIndices(stockIndice)
            .build();
        
        stockIndicesProducer.sendEvent(event, stockIndicesTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendIndicesUpdate(List<MarketIndexIndices> marketIndexIndices) {
        var event = MarketIndexIndicesPriceUpdateEvent.builder()
            .eventType("MARKET_INDICES_PRICE_UPDATE")  
            .timestamp(LocalDateTime.now())
            .marketIndices(marketIndexIndices)
            .build();
        
        indicesProducer.sendEvent(event, nseIndicesTopic, event.getEventType(), event.getTimestamp());
    }
}
