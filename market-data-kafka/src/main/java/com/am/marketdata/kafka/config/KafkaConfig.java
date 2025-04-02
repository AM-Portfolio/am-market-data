package com.am.marketdata.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.am.common.investment.model.equity.financial.balancesheet.StockBalanceSheet;
import com.am.common.investment.model.equity.financial.cashflow.StockCashFlow;
import com.am.common.investment.model.equity.financial.factsheetdividend.StockFactSheetDividend;
import com.am.common.investment.model.equity.financial.profitandloss.StockProfitAndLoss;
import com.am.common.investment.model.equity.financial.resultstatement.StockFinancialResult;
import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEvent;
import com.am.marketdata.common.model.events.QuaterlyFinancialsUpdateEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${app.kafka.stock-price-topic}")
    private String stockPriceTopic;

    @Value("${app.kafka.nse-indices-topic}")
    private String nseIndicesTopic;

    @Value("${app.kafka.board-of-directors-topic}")
    private String boardOfDirectorsTopic;

    @Value("${app.kafka.quaterly-financials-topic}")
    private String quaterlyFinancialsTopic;

    @Value("${app.kafka.stock-results-financials-topic}")
    private String stockResultsFinancialsTopic;

    @Value("${app.kafka.stock-profit-and-loss-financials-topic}")
    private String stockProfitAndLossFinancialsTopic;

    @Value("${app.kafka.stock-balance-sheet-financials-topic}")
    private String stockBalanceSheetFinancialsTopic;

    @Value("${app.kafka.stock-cash-flow-financials-topic}")
    private String stockCashFlowFinancialsTopic;

    @Value("${app.kafka.stock-fact-sheet-dividend-financials-topic}")
    private String stockFactSheetDividendFinancialsTopic;

    @Bean
    public NewTopic createTopic() {
        return new NewTopic(stockPriceTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createNseIndicesTopic() {
        return new NewTopic(nseIndicesTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createBoardOfDirectorsTopic() {
        return new NewTopic(boardOfDirectorsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createQuaterlyFinancialsTopic() {
        return new NewTopic(quaterlyFinancialsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createStockResultsFinancialsTopic() {
        return new NewTopic(stockResultsFinancialsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createStockProfitAndLossFinancialsTopic() {
        return new NewTopic(stockProfitAndLossFinancialsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createStockBalanceSheetFinancialsTopic() {
        return new NewTopic(stockBalanceSheetFinancialsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createStockCashFlowFinancialsTopic() {
        return new NewTopic(stockCashFlowFinancialsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createStockFactSheetDividendFinancialsTopic() {
        return new NewTopic(stockFactSheetDividendFinancialsTopic, 1, (short) 1);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, BoardOfDirectorsUpdateEvent> ProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, BoardOfDirectorsUpdateEvent> boardOfDirectorsKafkaTemplate() {
        return new KafkaTemplate<>(ProducerFactory());
    }

    @Bean
    public ProducerFactory<String, QuaterlyFinancialsUpdateEvent> quaterlyFinancialsProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, QuaterlyFinancialsUpdateEvent> quaterlyFinancialsKafkaTemplate() {
        return new KafkaTemplate<>(quaterlyFinancialsProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ProducerFactory<String, StockBalanceSheet> stockBalanceSheetProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, StockBalanceSheet> stockBalanceSheetKafkaTemplate() {
        return new KafkaTemplate<>(stockBalanceSheetProducerFactory());
    }

    @Bean
    public ProducerFactory<String, StockCashFlow> stockCashFlowProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, StockCashFlow> stockCashFlowKafkaTemplate() {
        return new KafkaTemplate<>(stockCashFlowProducerFactory());
    }

    @Bean
    public ProducerFactory<String, StockFactSheetDividend> stockFactSheetDividendProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, StockFactSheetDividend> stockFactSheetDividendKafkaTemplate() {
        return new KafkaTemplate<>(stockFactSheetDividendProducerFactory());
    }

    @Bean
    public ProducerFactory<String, StockFinancialResult> stockFinancialResultProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, StockFinancialResult> stockFinancialResultKafkaTemplate() {
        return new KafkaTemplate<>(stockFinancialResultProducerFactory());
    }

    @Bean
    public ProducerFactory<String, StockProfitAndLoss> stockProfitAndLossProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, StockProfitAndLoss> stockProfitAndLossKafkaTemplate() {
        return new KafkaTemplate<>(stockProfitAndLossProducerFactory());
    }
}
