package com.am.marketdata.kafka.oldProducer.impl;

import com.am.common.investment.model.events.EquityPriceUpdateEvent;
import com.am.marketdata.kafka.oldProducer.AbstractEventProducer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer for equity price update events
 */
@Component
public class EquityPriceEventProducer extends AbstractEventProducer<EquityPriceUpdateEvent> {
    
    public EquityPriceEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }
    
    @Override
    public Class<EquityPriceUpdateEvent> getEventType() {
        return EquityPriceUpdateEvent.class;
    }
}
