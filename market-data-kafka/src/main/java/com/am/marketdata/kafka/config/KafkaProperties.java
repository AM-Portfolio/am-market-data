package com.am.marketdata.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component("kafkaProperties")
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {
    
    private String bootstrapServers;
    private String groupId;
    private TopicProperties topics;
    private ProducerProperties producer;
    private Properties properties;
    
    @Data
    public static class TopicProperties {
        private String stockPrice;
        private String nseIndices;
        private String stockIndices;
        private String stockBoardOfDirectors;
        private String stockQuaterlyFinancials;
        private String stockBalanceSheetFinancials;
        private String stockCashFlowFinancials;
        private String stockProfitAndLossFinancials;
        private String stockResultsFinancials;
        private String stockFactSheetDividendFinancials;
    }
    
    @Data
    public static class ProducerProperties {
        private String keySerializer;
        private String valueSerializer;
        private String acks;
        private int retries;
        private int batchSize;
        private int lingerMs;
        private int bufferMemory;
    }

    @Data
    public static class Properties {
        private String securityProtocol;
        private String saslMechanism;
        private String saslJaasConfig;
    }
}
