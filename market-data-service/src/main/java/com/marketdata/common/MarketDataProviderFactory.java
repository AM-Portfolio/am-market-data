package com.marketdata.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating market data provider instances based on configuration
 */
@Slf4j
@Component
public class MarketDataProviderFactory {

    private final ApplicationContext applicationContext;
    
    @Value("${market-data.provider:zerodha}")
    private String activeProvider;
    
    public MarketDataProviderFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Get the configured market data provider
     * @return MarketDataProvider implementation
     */
    public MarketDataProvider getProvider() {
        log.info("Creating market data provider: {}", activeProvider);
        
        switch (activeProvider.toLowerCase()) {
            case "zerodha":
                return applicationContext.getBean("zerodhaMarketDataProvider", MarketDataProvider.class);
            case "upstox":
                return applicationContext.getBean("upstoxMarketDataProvider", MarketDataProvider.class);
            default:
                log.warn("Unknown provider '{}', falling back to Zerodha", activeProvider);
                return applicationContext.getBean("zerodhaMarketDataProvider", MarketDataProvider.class);
        }
    }
}
