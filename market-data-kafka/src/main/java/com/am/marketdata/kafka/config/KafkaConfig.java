package com.am.marketdata.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    private final KafkaTopicsConfig topicsConfig;
    private final KafkaProducerConfig producerConfig;

    public KafkaConfig(KafkaTopicsConfig topicsConfig, 
                       KafkaProducerConfig producerConfig) {
        this.topicsConfig = topicsConfig;
        this.producerConfig = producerConfig;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", producerConfig.getBootstrapServers());
        props.put("key.serializer", producerConfig.getKeySerializer());
        props.put("value.serializer", producerConfig.getValueSerializer());
        props.put("batch.size", producerConfig.getBatchSize());
        props.put("linger.ms", producerConfig.getLingerMs());
        props.put("buffer.memory", producerConfig.getBufferMemory());
        props.put("acks", producerConfig.getAcks());
        props.put("retries", producerConfig.getRetries());
        props.put("max.in.flight.requests.per.connection", 
            producerConfig.getMaxInFlightRequestsPerConnection());
        props.put("max.block.ms", producerConfig.getMaxBlockMs());
        props.put("request.timeout.ms", producerConfig.getRequestTimeoutMs());
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic stockPriceTopic() {
        return new NewTopic(topicsConfig.getStockPriceTopic(), 
            topicsConfig.getPartitions(), 
            topicsConfig.getReplicationFactor());
    }

    @Bean
    public NewTopic nseIndicesTopic() {
        return new NewTopic(topicsConfig.getNseIndicesTopic(), 
            topicsConfig.getPartitions(), 
            topicsConfig.getReplicationFactor());
    }

    @Bean
    public NewTopic boardOfDirectorsTopic() {
        return new NewTopic(topicsConfig.getBoardOfDirectorsTopic(), 
            topicsConfig.getPartitions(), 
            topicsConfig.getReplicationFactor());
    }

    @Bean
    public NewTopic quaterlyFinancialsTopic() {
        return new NewTopic(topicsConfig.getQuaterlyFinancialsTopic(), 
            topicsConfig.getPartitions(), 
            topicsConfig.getReplicationFactor());
    }
}
