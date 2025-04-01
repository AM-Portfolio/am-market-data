package com.am.marketdata.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.kafka.producer")
public class KafkaProducerConfig {
    private String bootstrapServers;
    private String keySerializer;
    private String valueSerializer;
    private int batchSize = 16384;
    private int lingerMs = 1;
    private int bufferMemory = 33554432;
    private String acks = "all";
    private int retries = 3;
    private int maxInFlightRequestsPerConnection = 5;
    private int maxBlockMs = 60000;
    private int requestTimeoutMs = 30000;
}
