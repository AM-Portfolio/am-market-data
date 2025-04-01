package com.am.marketdata.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.am.common.investment.model.events.EquityPriceUpdateEvent;
import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockIndicesPriceUpdateEvent;
import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEvent;
import com.am.marketdata.common.model.events.QuaterlyFinancialsUpdateEvent;
import com.am.marketdata.kafka.config.KafkaTopicsConfig;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for event type metadata
 * Centralizes configuration for all event types
 */
@Slf4j
@Component
public class EventTypeRegistry {

    private final Map<String, EventTypeMetadata> eventTypesByName = new HashMap<>();
    private final Map<Class<?>, EventTypeMetadata> eventTypesByClass = new HashMap<>();
    private final KafkaTopicsConfig topicsConfig;

    public EventTypeRegistry(KafkaTopicsConfig topicsConfig) {
        this.topicsConfig = topicsConfig;
    }

    @PostConstruct
    public void initialize() {
        // Register all event types
        registerEventType(EventTypeNames.EQUITY_PRICE_UPDATE, topicsConfig.getStockPriceTopic(), EquityPriceUpdateEvent.class);
        registerEventType(EventTypeNames.STOCK_INDICES_UPDATE, topicsConfig.getStockIndicesTopic(), StockIndicesPriceUpdateEvent.class);
        registerEventType(EventTypeNames.MARKET_INDICES_UPDATE, topicsConfig.getNseIndicesTopic(), MarketIndexIndicesPriceUpdateEvent.class);
        registerEventType(EventTypeNames.BOARD_OF_DIRECTORS_UPDATE, topicsConfig.getBoardOfDirectorsTopic(), BoardOfDirectorsUpdateEvent.class);
        registerEventType(EventTypeNames.QUATERLY_FINANCIALS_UPDATE, topicsConfig.getQuaterlyFinancialsTopic(), QuaterlyFinancialsUpdateEvent.class);
        
        // Add more event types here as needed
        
        log.info("Registered {} event types", eventTypesByName.size());
    }

    private void registerEventType(String eventTypeName, String topicName, Class<?> eventClass) {
        EventTypeMetadata metadata = new EventTypeMetadata(eventTypeName, topicName, eventClass);
        eventTypesByName.put(eventTypeName, metadata);
        eventTypesByClass.put(eventClass, metadata);
        
        log.debug("Registered event type: {}", eventTypeName);
    }

    @Data
    @AllArgsConstructor
    public static class EventTypeMetadata {
        private String eventTypeName;
        private String topicName;
        private Class<?> eventClass;
    }

    public EventTypeMetadata getByName(String eventTypeName) {
        return eventTypesByName.get(eventTypeName);
    }

    public EventTypeMetadata getByClass(Class<?> eventClass) {
        return eventTypesByClass.get(eventClass);
    }
}
