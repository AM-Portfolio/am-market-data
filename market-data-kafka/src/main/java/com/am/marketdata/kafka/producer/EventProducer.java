package com.am.marketdata.kafka.producer;

import java.time.LocalDateTime;

/**
 * Generic interface for all event producers
 * @param <T> Type of event to be produced
 */
public interface EventProducer<T> {
    
    /**
     * Send an event to Kafka
     * 
     * @param event The event to send
     * @param topic The Kafka topic to send to
     * @param eventType The type of event
     * @param timestamp The timestamp of the event
     */
    void sendEvent(T event, String topic, String eventType, LocalDateTime timestamp);
    
    /**
     * Get the class of event this producer handles
     * 
     * @return The event class
     */
    Class<T> getEventType();
}
