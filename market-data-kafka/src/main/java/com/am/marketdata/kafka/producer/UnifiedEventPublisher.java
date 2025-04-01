package com.am.marketdata.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Unified publisher for all event types
 * Implements both Factory and Template Method patterns to eliminate duplicate code
 * and provide a consistent interface for all event types.
 */
@Slf4j
@Service
public class UnifiedEventPublisher {

    private final EventProducerFactory producerFactory;
    private final EventTypeRegistry eventTypeRegistry;
    private final Map<Class<?>, BuilderMetadata> builderMetadataCache = new HashMap<>();

    public UnifiedEventPublisher(EventProducerFactory producerFactory, EventTypeRegistry eventTypeRegistry) {
        this.producerFactory = producerFactory;
        this.eventTypeRegistry = eventTypeRegistry;
    }

    /**
     * Generic method to publish any event type
     * 
     * @param eventType The event type name
     * @param builderConfigurator A function to configure the event builder
     * @return true if published successfully, false otherwise
     */
    public boolean publish(String eventType, BuilderConfigurator builderConfigurator) {
        try {
            EventTypeRegistry.EventTypeMetadata metadata = eventTypeRegistry.getByName(eventType);
            if (metadata == null) {
                log.error("Unknown event type: {}", eventType);
                return false;
            }

            // Create event using reflection and builder pattern
            Object event = createEvent(metadata.getEventClass(), eventType, builderConfigurator);
            
            // Get producer for this event type
            EventProducer<?> producer = producerFactory.getProducer(metadata.getEventClass());
            
            // Send event
            sendEvent(producer, event, metadata.getTopicName(), eventType);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to publish {} event: {}", eventType, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Create an event using reflection and builder pattern
     */
    private Object createEvent(Class<?> eventClass, String eventType, BuilderConfigurator builderConfigurator) throws Exception {
        // Get or create builder metadata
        BuilderMetadata builderMetadata = getBuilderMetadata(eventClass);
        
        // Create builder
        Object builder = builderMetadata.builderMethod.invoke(null);
        
        // Set standard fields
        builderMetadata.eventTypeMethod.invoke(builder, eventType);
        builderMetadata.timestampMethod.invoke(builder, LocalDateTime.now());
        
        // Let consumer configure the builder
        builderConfigurator.configure(builder);
        
        // Build and return the event
        return builderMetadata.buildMethod.invoke(builder);
    }

    /**
     * Send an event using the appropriate producer
     */
    @SuppressWarnings("unchecked")
    private <T> void sendEvent(EventProducer<?> producer, Object event, String topic, String eventType) {
        EventProducer<T> typedProducer = (EventProducer<T>) producer;
        T typedEvent = (T) event;
        typedProducer.sendEvent(typedEvent, topic, eventType, LocalDateTime.now());
    }

    /**
     * Get or create builder metadata for an event class
     */
    private BuilderMetadata getBuilderMetadata(Class<?> eventClass) {
        return builderMetadataCache.computeIfAbsent(eventClass, this::createBuilderMetadata);
    }

    /**
     * Create builder metadata for an event class
     */
    private BuilderMetadata createBuilderMetadata(Class<?> eventClass) {
        try {
            // Find builder method
            Method builderMethod = eventClass.getMethod("builder");
            Class<?> builderClass = builderMethod.getReturnType();
            
            // Find builder methods
            Method eventTypeMethod = builderClass.getMethod("eventType", String.class);
            Method timestampMethod = builderClass.getMethod("timestamp", LocalDateTime.class);
            Method buildMethod = builderClass.getMethod("build");
            
            return new BuilderMetadata(builderMethod, eventTypeMethod, timestampMethod, buildMethod);
        } catch (Exception e) {
            log.error("Failed to create builder metadata for {}: {}", eventClass.getName(), e.getMessage(), e);
            throw new IllegalArgumentException("Invalid event class: " + eventClass.getName(), e);
        }
    }

    /**
     * Publish multiple events in parallel with resilient error handling
     * Implements the partial success strategy pattern from your market data processing
     */
    public void publishMultipleAsync(List<Runnable> publishers) {
        CompletableFuture<?>[] futures = publishers.stream()
            .map(publisher -> CompletableFuture.runAsync(() -> {
                try {
                    publisher.run();
                } catch (Exception e) {
                    log.error("Failed to publish event: {}", e.getMessage());
                    // Swallow exception to allow other publishers to continue
                }
            }))
            .toArray(CompletableFuture[]::new);
        
        // Wait for all futures to complete
        CompletableFuture.allOf(futures).join();
    }

    /**
     * Functional interface for configuring a builder
     */
    @FunctionalInterface
    public interface BuilderConfigurator {
        void configure(Object builder);
    }

    /**
     * Metadata for an event builder
     */
    private static class BuilderMetadata {
        private final Method builderMethod;
        private final Method eventTypeMethod;
        private final Method timestampMethod;
        private final Method buildMethod;

        public BuilderMetadata(Method builderMethod, Method eventTypeMethod, Method timestampMethod, Method buildMethod) {
            this.builderMethod = builderMethod;
            this.eventTypeMethod = eventTypeMethod;
            this.timestampMethod = timestampMethod;
            this.buildMethod = buildMethod;
        }
    }
}
