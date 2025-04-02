package com.am.marketdata.kafka.config;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.marketdata.common.model.events.BalanceSheetFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEvent;
import com.am.marketdata.common.model.events.CashFlowFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.FactSheetFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.QuaterlyFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.StockProfitAndLossFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.StockResultsFinancialsUpdateEvent;
import com.am.common.investment.model.events.EquityPriceUpdateEvent;
import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockIndicesPriceUpdateEvent;
import com.am.marketdata.kafka.producer.BaseKafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
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
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic createTopic() {
        return new NewTopic(kafkaProperties.getTopics().getStockPrice(), 1, (short) 1);
    }

    @Bean
    public NewTopic createBoardOfDirectorsTopic() {
        return new NewTopic(kafkaProperties.getTopics().getStockBoardOfDirectors(), 1, (short) 1);
    }

    @Bean
    public NewTopic createQuaterlyFinancialsTopic() {
        return new NewTopic(kafkaProperties.getTopics().getStockQuaterlyFinancials(), 1, (short) 1);
    }

    @Bean
    public NewTopic createBalanceSheetFinancialsTopic() {
        return new NewTopic(kafkaProperties.getTopics().getStockBalanceSheetFinancials(), 1, (short) 1);
    }

    @Bean
    public NewTopic createCashFlowFinancialsTopic() {
        return new NewTopic(kafkaProperties.getTopics().getStockCashFlowFinancials(), 1, (short) 1);
    }

    @Bean
    public NewTopic createProfitAndLossFinancialsTopic() {
        return new NewTopic(kafkaProperties.getTopics().getStockProfitAndLossFinancials(), 1, (short) 1);
    }

    @Bean
    public NewTopic createResultsFinancialsTopic() {
        return new NewTopic(kafkaProperties.getTopics().getStockResultsFinancials(), 1, (short) 1);
    }

    @Bean
    public NewTopic createFactSheetDividendFinancialsTopic() {
        return new NewTopic(kafkaProperties.getTopics().getStockFactSheetDividendFinancials(), 1, (short) 1);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Generic producer factory that can handle any type of object
     * @return ProducerFactory for any object type
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
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
    public BaseKafkaProducer<BoardOfDirectorsUpdateEvent> boardOfDirectorsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<QuaterlyFinancialsUpdateEvent> quaterlyFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<BalanceSheetFinancialsUpdateEvent> balanceSheetFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<CashFlowFinancialsUpdateEvent> cashFlowFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<StockProfitAndLossFinancialsUpdateEvent> profitAndLossFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<StockResultsFinancialsUpdateEvent> resultsFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<FactSheetFinancialsUpdateEvent> factSheetFinancialsProducer() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }

    @Bean
    public BaseKafkaProducer<BoardOfDirectors> boardOfDirectors() {
        return new BaseKafkaProducer<>(kafkaTemplate());
    }
}
