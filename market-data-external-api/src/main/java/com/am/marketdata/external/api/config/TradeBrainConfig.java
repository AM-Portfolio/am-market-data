package com.am.marketdata.external.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Data
@Configuration
@Primary
@ConfigurationProperties(prefix = "trade-brain")
public class TradeBrainConfig {
    private String baseUrl;
    private Api api;
    private Headers headers;

    @Data
    public static class Api {
        private String indexAll;
    }

    @Data
    public static class Headers {
        private String userAgent;
        private String accept;
        private String acceptLanguage;
    }
}
