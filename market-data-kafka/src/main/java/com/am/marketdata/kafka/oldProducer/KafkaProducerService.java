package com.am.marketdata.kafka.oldProducer;
    
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockInsidicesEventData;
import com.am.marketdata.kafka.config.KafkaTopicsConfig;
import com.am.common.investment.model.events.EquityPriceUpdateEvent;

@Slf4j
@Service
public class KafkaProducerService {

    private final BaseKafkaProducer<EquityPriceUpdateEvent> equityProducer;
    private final BaseKafkaProducer<StockIndicesPriceUpdateEvent> stockIndicesProducer;
    private final BaseKafkaProducer<MarketIndexIndicesPriceUpdateEvent> indicesProducer;
    private final KafkaTopicsConfig topicsConfig;
    
    public KafkaProducerService(KafkaTemplate<String, Object> objectKafkaTemplate, KafkaTopicsConfig topicsConfig) {
        this.equityProducer = new BaseKafkaProducer<>(objectKafkaTemplate) {};
        this.stockIndicesProducer = new BaseKafkaProducer<>(objectKafkaTemplate) {};
        this.indicesProducer = new BaseKafkaProducer<>(objectKafkaTemplate) {};
        this.topicsConfig = topicsConfig;
    }

    public void sendEquityPriceUpdates(List<EquityPrice> equityPrices) {
        var event = EquityPriceUpdateEvent.builder()
            .eventType("EQUITY_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .equityPrices(equityPrices)
            .build();
        
        equityProducer.sendEvent(event, topicsConfig.getStockPriceTopic(), event.getEventType(), event.getTimestamp());
    }

    public void sendStockIndicesUpdate(StockInsidicesEventData stockIndice) {
        var event = StockIndicesPriceUpdateEvent.builder()
            .eventType("STOCK_INDICES_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .stockIndices(stockIndice)
            .build();
        
        stockIndicesProducer.sendEvent(event, topicsConfig.getStockIndicesTopic(), event.getEventType(), event.getTimestamp());
    }

    public void sendIndicesUpdate(List<MarketIndexIndices> marketIndexIndices) {
        var event = MarketIndexIndicesPriceUpdateEvent.builder()
            .eventType("MARKET_INDICES_PRICE_UPDATE")  
            .timestamp(LocalDateTime.now())
            .marketIndices(marketIndexIndices)
            .build();
        
        indicesProducer.sendEvent(event, topicsConfig.getNseIndicesTopic(), event.getEventType(), event.getTimestamp());
    }
}
