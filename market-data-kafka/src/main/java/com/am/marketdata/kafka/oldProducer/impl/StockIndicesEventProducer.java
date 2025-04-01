package com.am.marketdata.kafka.oldProducer.impl;

import com.am.common.investment.model.events.StockIndicesPriceUpdateEvent;
import com.am.marketdata.kafka.oldProducer.AbstractEventProducer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer for stock indices price update events
 */
@Component
public class StockIndicesEventProducer extends AbstractEventProducer<StockIndicesPriceUpdateEvent> {
    
    public StockIndicesEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }
    
    @Override
    public Class<StockIndicesPriceUpdateEvent> getEventType() {
        return StockIndicesPriceUpdateEvent.class;
    }
}
