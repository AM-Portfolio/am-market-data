package com.am.marketdata.kafka.oldProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseKafkaProducer<T> {
    
    protected final KafkaTemplate<String, Object> objectKafkaTemplate;

    protected void sendEvent(T event, String topic,String eventType, LocalDateTime timestamp) {
        try {
            log.info("Sending event to Kafka. EventType: {}, Timestamp: {}", eventType, timestamp);
            
            RecordHeaders headers = new RecordHeaders();
            headers.add("eventType", eventType.getBytes());
            headers.add("timestamp", String.valueOf(timestamp).getBytes());
            
            long timestampMillis = timestamp
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
            
            ProducerRecord<String, Object> record = 
                new ProducerRecord<>(topic, null, timestampMillis, eventType, event, headers);
            
            objectKafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent successfully to topic: {}, partition: {}, offset: {}", 
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send message", ex);
                        throw new RuntimeException("Failed to send message to Kafka", ex);
                    }
                });
        } catch (Exception e) {
            log.error("Failed to send event to Kafka", e);
            throw new RuntimeException("Failed to send event to Kafka", e);
        }
    }
}
