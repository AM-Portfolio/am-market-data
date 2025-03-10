package com.am.marketdata.kafka.producer;

import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.am.common.investment.model.equity.MarketIndexIndices;

@Service
@Slf4j
public class MarketIndiciesPriceKafkaProducer extends BaseKafkaProducer<MarketIndexIndicesPriceUpdateEvent> {
    
    public MarketIndiciesPriceKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.nse-indices-topic}") String topic) {
        super(kafkaTemplate, topic);
    }

    public void sendIndicesUpdate(List<MarketIndexIndices> marketIndexIndices) {
        var event = MarketIndexIndicesPriceUpdateEvent.builder()
            .eventType("MARKET_INDICES_PRICE_UPDATE")  
            .timestamp(LocalDateTime.now())
            .marketIndices(marketIndexIndices)
            .build();
        
        sendEvent(event, event.getEventType(), event.getTimestamp());
    }
}
