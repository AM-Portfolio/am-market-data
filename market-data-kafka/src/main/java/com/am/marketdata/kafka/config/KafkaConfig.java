package com.am.marketdata.kafka.config;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.marketdata.common.model.events.BalanceSheetFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEventV1;
import com.am.marketdata.common.model.events.CashFlowFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.FactSheetFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.QuarterlyFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.StockProfitAndLossFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.StockResultsFinancialsUpdateEventV1;
import com.am.common.investment.model.events.EquityPriceUpdateEvent;
import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockIndicesPriceUpdateEvent;
import com.am.marketdata.kafka.producer.BaseKafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
@org.springframework.context.annotation.DependsOn("kafkaProperties")
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        return new KafkaAdmin(configs); 
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public Map<String, Object> kafkaConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        if(kafkaProperties.getProperties() != null && kafkaProperties.getProperties().getSaslJaasConfig() != null && !kafkaProperties.getProperties().getSaslJaasConfig().isEmpty()) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaProperties.getProperties().getSecurityProtocol());
            props.put(SaslConfigs.SASL_MECHANISM, kafkaProperties.getProperties().getSaslMechanism());
            props.put(SaslConfigs.SASL_JAAS_CONFIG, kafkaProperties.getProperties().getSaslJaasConfig());
        }


        return props;
    }

    /**
     * Generic producer factory that can handle any type of object
     * @return ProducerFactory for any object type
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = kafkaConfigs();
        
        // Use producer properties if available, otherwise use defaults
        KafkaProperties.ProducerProperties producerProps = kafkaProperties.getProducer();
        if (producerProps != null) {
            configProps.put(ProducerConfig.ACKS_CONFIG, producerProps.getAcks());
            configProps.put(ProducerConfig.RETRIES_CONFIG, producerProps.getRetries());
            configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, producerProps.getBatchSize());
            configProps.put(ProducerConfig.LINGER_MS_CONFIG, producerProps.getLingerMs());
            configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, producerProps.getBufferMemory());
        } else {
            // Default values
            configProps.put(ProducerConfig.ACKS_CONFIG, "all");
            configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
            configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
            configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
            configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        }
        
        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>(objectMapper());
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), jsonSerializer);
    }

    /**
     * Generic KafkaTemplate that can be used for any type of object
     * @return KafkaTemplate for any object type
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public BaseKafkaProducer<EquityPriceUpdateEvent> equityProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<StockIndicesPriceUpdateEvent> stockIndicesProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<MarketIndexIndicesPriceUpdateEvent> indicesProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<BoardOfDirectorsUpdateEventV1> boardOfDirectorsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<QuarterlyFinancialsUpdateEventV1> quaterlyFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<BalanceSheetFinancialsUpdateEventV1> balanceSheetFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<CashFlowFinancialsUpdateEventV1> cashFlowFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<StockProfitAndLossFinancialsUpdateEventV1> profitAndLossFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<StockResultsFinancialsUpdateEventV1> resultsFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<FactSheetFinancialsUpdateEventV1> factSheetFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<BoardOfDirectors> boardOfDirectors() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }
}
