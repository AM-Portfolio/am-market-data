package com.am.marketdata.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.am.marketdata.kafka.model.NSEIndicesEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class NSEIndicesKafkaProducer {
    @Qualifier("nseIndicesKafkaTemplate")
    private final KafkaTemplate<String, NSEIndicesEvent> kafkaTemplate;

    @Value("${app.kafka.nse-indices-topic}")
    private String topic;

    public void sendIndicesUpdate(NSEIndicesEvent event) {
        try {
            log.info("Sending NSE indices update to Kafka. Timestamp: {}", event.getTimestamp());
            kafkaTemplate.send(topic, event).get();
            log.info("Successfully sent NSE indices update to Kafka");
        } catch (Exception e) {
            log.error("Failed to send NSE indices update to Kafka", e);
            throw new RuntimeException("Failed to send NSE indices update to Kafka", e);
        }
    }
}
