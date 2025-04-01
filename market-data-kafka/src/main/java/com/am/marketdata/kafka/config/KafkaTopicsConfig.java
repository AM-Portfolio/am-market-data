package com.am.marketdata.kafka.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

@Data
@Component
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaTopicsConfig {
    private String stockPriceTopic;
    private String stockIndicesTopic;
    private String nseIndicesTopic;
    private String boardOfDirectorsTopic;
    private String quaterlyFinancialsTopic;
    
    // Topic configuration properties
    private int partitions = 1;
    private short replicationFactor = 1;
    private Map<String, String> topicConfig = new HashMap<>();
}
