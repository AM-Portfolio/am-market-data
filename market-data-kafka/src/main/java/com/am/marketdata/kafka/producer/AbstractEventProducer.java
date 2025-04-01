package com.am.marketdata.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for all event producers implementing the Template Method pattern
 * @param <T> Type of event to be produced
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventProducer<T> implements EventProducer<T> {
    
    protected final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Override
    public void sendEvent(T event, String topic, String eventType, LocalDateTime timestamp) {
        try {
            log.info("Sending {} event to topic: {}", eventType, topic);
            
            // Pre-processing hook
            beforeSend(event, topic, eventType, timestamp);
            
            // Create record headers
            RecordHeaders headers = createHeaders(eventType, timestamp);
            
            // Convert timestamp to epoch millis
            long timestampMillis = convertTimestampToMillis(timestamp);
            
            // Create producer record
            ProducerRecord<String, Object> record = createProducerRecord(
                topic, eventType, event, headers, timestampMillis);
            
            // Send the record
            CompletableFuture<?> future = doSend(record);
            
            // Handle completion
            handleCompletion(future, topic, eventType);
            
            // Post-processing hook
            afterSend(event, topic, eventType, timestamp);
            
        } catch (Exception e) {
            // Error handling
            handleError(e, event, topic, eventType);
        }
    }
    
    /**
     * Hook method called before sending the event
     */
    protected void beforeSend(T event, String topic, String eventType, LocalDateTime timestamp) {
        // Default implementation does nothing, subclasses can override
    }
    
    /**
     * Hook method called after sending the event
     */
    protected void afterSend(T event, String topic, String eventType, LocalDateTime timestamp) {
        // Default implementation does nothing, subclasses can override
    }
    
    /**
     * Create Kafka record headers
     */
    protected RecordHeaders createHeaders(String eventType, LocalDateTime timestamp) {
        RecordHeaders headers = new RecordHeaders();
        headers.add("eventType", eventType.getBytes());
        headers.add("timestamp", String.valueOf(timestamp).getBytes());
        return headers;
    }
    
    /**
     * Convert LocalDateTime to epoch milliseconds
     */
    protected long convertTimestampToMillis(LocalDateTime timestamp) {
        return timestamp
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    }
    
    /**
     * Create the producer record
     */
    protected ProducerRecord<String, Object> createProducerRecord(
            String topic, String eventType, T event, RecordHeaders headers, long timestampMillis) {
        return new ProducerRecord<>(topic, null, timestampMillis, eventType, event, headers);
    }
    
    /**
     * Send the record to Kafka
     */
    protected CompletableFuture<?> doSend(ProducerRecord<String, Object> record) {
        return kafkaTemplate.send(record);
    }
    
    /**
     * Handle completion of the send operation
     */
    protected void handleCompletion(CompletableFuture<?> future, String topic, String eventType) {
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully to topic: {}", topic);
            } else {
                log.error("Failed to send {} message to topic: {}", eventType, topic, ex);
                throw new RuntimeException("Failed to send message to Kafka", ex);
            }
        });
    }
    
    /**
     * Handle errors during send operation
     */
    protected void handleError(Exception e, T event, String topic, String eventType) {
        log.error("Failed to send {} event to topic: {}", eventType, topic, e);
        throw new RuntimeException("Failed to send event to Kafka", e);
    }
}
