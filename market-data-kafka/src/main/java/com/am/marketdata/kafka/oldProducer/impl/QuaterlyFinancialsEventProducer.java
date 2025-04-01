package com.am.marketdata.kafka.oldProducer.impl;

import com.am.marketdata.common.model.events.QuaterlyFinancialsUpdateEvent;
import com.am.marketdata.kafka.oldProducer.AbstractEventProducer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer for quarterly financials update events
 */
@Component
public class QuaterlyFinancialsEventProducer extends AbstractEventProducer<QuaterlyFinancialsUpdateEvent> {
    
    public QuaterlyFinancialsEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }
    
    @Override
    public Class<QuaterlyFinancialsUpdateEvent> getEventType() {
        return QuaterlyFinancialsUpdateEvent.class;
    }
}
