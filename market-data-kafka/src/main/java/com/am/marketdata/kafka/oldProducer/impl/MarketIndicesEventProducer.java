package com.am.marketdata.kafka.oldProducer.impl;

import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import com.am.marketdata.kafka.oldProducer.AbstractEventProducer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer for market indices price update events
 */
@Component
public class MarketIndicesEventProducer extends AbstractEventProducer<MarketIndexIndicesPriceUpdateEvent> {
    
    public MarketIndicesEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }
    
    @Override
    public Class<MarketIndexIndicesPriceUpdateEvent> getEventType() {
        return MarketIndexIndicesPriceUpdateEvent.class;
    }
}
