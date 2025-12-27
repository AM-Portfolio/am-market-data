package com.am.marketdata.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for market data providers.
 * Loaded from application.yml under 'provider' prefix.
 */
@Data
@ConfigurationProperties(prefix = "provider")
public class ProviderProperties {
    
    /**
     * Provider type to use: upstox, zerodha
     */
    private String type = "upstox";
    
    /**
     * Upstox configuration
     */
    private UpstoxConfig upstox = new UpstoxConfig();
    
    /**
     * Zerodha configuration
     */
    private ZerodhaConfig zerodha = new ZerodhaConfig();
    
    @Data
    public static class UpstoxConfig {
        private String apiKey;
        private String apiSecret;
        private String redirectUri;
        private String baseUrl = "https://api.upstox.com/v2";
    }
    
    @Data
    public static class ZerodhaConfig {
        private String apiKey;
        private String apiSecret;
        private String redirectUri;
        private String baseUrl = "https://api.kite.trade";
    }
}
