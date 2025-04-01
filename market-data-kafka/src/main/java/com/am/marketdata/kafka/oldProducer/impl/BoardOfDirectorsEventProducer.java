package com.am.marketdata.kafka.oldProducer.impl;

import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEvent;
import com.am.marketdata.kafka.oldProducer.AbstractEventProducer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer for board of directors update events
 */
@Component
public class BoardOfDirectorsEventProducer extends AbstractEventProducer<BoardOfDirectorsUpdateEvent> {
    
    public BoardOfDirectorsEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }
    
    @Override
    public Class<BoardOfDirectorsUpdateEvent> getEventType() {
        return BoardOfDirectorsUpdateEvent.class;
    }
}
