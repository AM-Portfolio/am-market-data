package com.am.marketdata.provider.config;

import com.am.marketdata.provider.MarketDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Market Data Provider module.
 * This configuration is automatically loaded when module is on classpath.
 * 
 * Provider selection is controlled by application.yml property: provider.type
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.am.marketdata.provider")
@EnableConfigurationProperties(ProviderProperties.class)
@ComponentScan(basePackages = "com.am.marketdata.provider")
@org.springframework.data.mongodb.repository.config.EnableMongoRepositories(basePackages = "com.am.marketdata.provider")
public class ProviderAutoConfiguration {
    
    // Providers are loaded via ComponentScan
    // Selection logic will be handled via @ConditionalOnProperty on the provider classes themselves
    // or via Primary configuration.
}
