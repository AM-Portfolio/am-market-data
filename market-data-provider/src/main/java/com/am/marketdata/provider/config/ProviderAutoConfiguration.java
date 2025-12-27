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
public class ProviderAutoConfiguration {
    
    /**
     * Create Upstox provider bean when provider.type=upstox
     */
    @Bean
    @ConditionalOnProperty(name = "provider.type", havingValue = "upstox", matchIfMissing = true)
    public MarketDataProvider upstoxProvider(ProviderProperties properties) {
        log.info("Configuring Upstox Market Data Provider");
        // Implementation will be added in next step
        // return new UpstoxMarketDataProvider(properties.getUpstox());
        throw new UnsupportedOperationException("Upstox provider implementation pending");
    }
    
    /**
     * Create Zerodha provider bean when provider.type=zerodha
     */
    @Bean
    @ConditionalOnProperty(name = "provider.type", havingValue = "zerodha")
    public MarketDataProvider zerodhaProvider(ProviderProperties properties) {
        log.info("Configuring Zerodha Market Data Provider");
        // Implementation will be added in next step
        // return new ZerodhaMarketDataProvider(properties.getZerodha());
        throw new UnsupportedOperation("Zerodha provider implementation pending");
    }
}
