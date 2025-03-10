package com.am.marketdata.kafka.producer;

import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.am.common.investment.model.equity.MarketIndexIndices;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketIndiciesPriceKafkaProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.nse-indices-topic}")
    private String topic;

    public void sendIndicesUpdate(List<MarketIndexIndices> marketIndexIndices) {
        var marketIndexIndicesPriceUpdateEvent = MarketIndexIndicesPriceUpdateEvent.builder()
            .eventType("EQUITY_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .indexIndices(marketIndexIndices)
            .build();
        
        sendEquityPriceUpdates(marketIndexIndicesPriceUpdateEvent);
    }

    public void sendEquityPriceUpdates(MarketIndexIndicesPriceUpdateEvent event) {
        try {
            log.info("Sending market indices price update to Kafka. EventType: {}, Timestamp: {}", 
                    event.getEventType(), event.getTimestamp());
            
            RecordHeaders headers = new RecordHeaders();
            headers.add("eventType", event.getEventType().getBytes());
            headers.add("timestamp", String.valueOf(event.getTimestamp()).getBytes());
            
            // Convert LocalDateTime to milliseconds since epoch
            long timestampMillis = event.getTimestamp()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
            
            ProducerRecord<String, Object> record = 
                new ProducerRecord<>(topic, null, timestampMillis, 
                                   event.getEventType(), event, headers);
            
            kafkaTemplate.send(record).get();
            log.info("Successfully sent market indices price update to Kafka");
        } catch (Exception e) {
            log.error("Failed to send market indices price update to Kafka", e);
            throw new RuntimeException("Failed to send market indices price update to Kafka", e);
        }
    }
}
