package com.am.marketdata.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.marketdata.kafka.model.EquityPriceUpdateEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topicName;

    public void sendEquityPriceUpdates(List<EquityPrice> equityPrices) {
        var equityPriceUpdateEvent = EquityPriceUpdateEvent.builder()
            .eventType("EQUITY_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .equityPrices(equityPrices)
            .build();
        
        sendMessage(equityPriceUpdateEvent);
    }

    public void sendMessage(EquityPriceUpdateEvent equityPriceUpdateEvent) {
        RecordHeaders headers = new RecordHeaders();
        headers.add("eventType", equityPriceUpdateEvent.getEventType().getBytes());
        headers.add("timestamp", String.valueOf(equityPriceUpdateEvent.getTimestamp()).getBytes());

        ProducerRecord<String, Object> record = new ProducerRecord<>(topicName, null, 
        equityPriceUpdateEvent.getEventType(), equityPriceUpdateEvent, headers);
        
        kafkaTemplate.send(record)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message sent successfully to topic: {}, partition: {}, offset: {}", 
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send message", ex);
                }
            });
    }

}
