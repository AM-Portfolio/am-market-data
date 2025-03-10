package com.am.marketdata.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.equity.EquityPrice;
import com.portfolio.kafka.model.EquityPriceUpdateEvent;

@Slf4j
@Service
public class KafkaProducerService extends BaseKafkaProducer<EquityPriceUpdateEvent> {

    public KafkaProducerService(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic) {
        super(kafkaTemplate, topic);
    }

    public void sendEquityPriceUpdates(List<EquityPrice> equityPrices) {
        var event = EquityPriceUpdateEvent.builder()
            .eventType("EQUITY_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .equityPrices(equityPrices)
            .build();
        
        sendEvent(event, event.getEventType(), event.getTimestamp());
    }
}
