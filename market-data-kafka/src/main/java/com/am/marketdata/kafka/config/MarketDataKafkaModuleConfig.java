package com.am.marketdata.kafka.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {
    "com.am.marketdata.kafka",
    "com.am.marketdata.kafka.config",
    "com.am.marketdata.common",
    "com.am.marketdata.external"
})
@Import({KafkaConfig.class, KafkaTopicsConfig.class, KafkaProducerConfig.class})
public class MarketDataKafkaModuleConfig {
    
}
