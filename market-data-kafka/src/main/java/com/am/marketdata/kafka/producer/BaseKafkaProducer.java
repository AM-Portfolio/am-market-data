package com.am.marketdata.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
public class BaseKafkaProducer<T> {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Send an event to a Kafka topic
     * 
     * @param topic The topic to send the event to
     * @param event The event to send
     */
    public void send(String topic, T event) {
        if (event == null) {
            log.error("Cannot send null event to Kafka");
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        try {
            log.debug("Sending event to Kafka topic: {}", topic);
            kafkaTemplate.send(topic, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Message sent successfully to topic: {}, partition: {}, offset: {}", 
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send message to topic: {}", topic, ex);
                        throw new RuntimeException("Failed to send message to Kafka", ex);
                    }
                });
        } catch (Exception e) {
            log.error("Failed to send event to Kafka topic: {}", topic, e);
            throw new RuntimeException("Failed to send event to Kafka", e);
        }
    }

    /**
     * Send an event with additional metadata to a Kafka topic
     * 
     * @param event The event to send
     * @param topic The topic to send to
     * @param eventType The type of event
     * @param timestamp The timestamp of the event
     */
    public void sendEvent(T event, String topic, String eventType, LocalDateTime timestamp) {
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
            
            kafkaTemplate.send(record)
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
