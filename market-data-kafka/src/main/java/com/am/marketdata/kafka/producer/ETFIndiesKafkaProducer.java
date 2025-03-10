package com.am.marketdata.kafka.producer;

import com.am.common.investment.model.events.ETFIndicesPriceUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import com.am.common.investment.model.equity.ETFIndies;

@Service
@Slf4j
public class ETFIndiesKafkaProducer extends BaseKafkaProducer<ETFIndicesPriceUpdateEvent> {
    
    public ETFIndiesKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.nse-etf-topic}") String topic) {
        super(kafkaTemplate, topic);
    }

    public void sendETFUpdate(List<ETFIndies> etfIndies) {
        var event = ETFIndicesPriceUpdateEvent.builder()
            .eventType("ETF_PRICE_UPDATE")
            .timestamp(etfIndies.get(0).getTimestamp())
            .etfIndies(etfIndies)
            .build();
        
        sendEvent(event, event.getEventType(), event.getTimestamp());
    }
}
