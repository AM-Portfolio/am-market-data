package com.am.marketdata.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MarketDataConsumer {
    
    @KafkaListener(topics = "${kafka.topic.market-data}")
    public void consume(String message) {
        // Implement message consumption logic
    }
}
