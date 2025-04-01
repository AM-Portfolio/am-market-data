package com.am.marketdata.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.am.marketdata.kafka.oldProducer.AbstractEventProducer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and caching event producers
 * Implements the Factory pattern to create producers on demand
 */
@Slf4j
@Component
public class EventProducerFactory {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<Class<?>, EventProducer<?>> producerCache = new ConcurrentHashMap<>();

    public EventProducerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Get or create a producer for the specified event type
     * 
     * @param eventType The class of the event
     * @param <T> The event type
     * @return The producer for the event type
     */
    @SuppressWarnings("unchecked")
    public <T> EventProducer<T> getProducer(Class<T> eventType) {
        return (EventProducer<T>) producerCache.computeIfAbsent(
            eventType, 
            type -> createProducer(type)
        );
    }

    /**
     * Create a new producer for the specified event type
     * 
     * @param eventType The class of the event
     * @param <T> The event type
     * @return The new producer
     */
    @SuppressWarnings("unchecked")
    private <T> EventProducer<T> createProducer(Class<T> eventType) {
        log.info("Creating new producer for event type: {}", eventType.getSimpleName());
        return new GenericEventProducer<>(kafkaTemplate, eventType);
    }

    /**
     * Generic implementation of EventProducer that can handle any event type
     * 
     * @param <T> The event type
     */
    private static class GenericEventProducer<T> extends AbstractEventProducer<T> {
        private final Class<T> eventType;

        public GenericEventProducer(KafkaTemplate<String, Object> kafkaTemplate, Class<T> eventType) {
            super(kafkaTemplate);
            this.eventType = eventType;
        }

        @Override
        public Class<T> getEventType() {
            return eventType;
        }
    }
}
