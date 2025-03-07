package com.am.marketdata.upstock.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "upstox.auth")
public class UpstoxAuthConfig {
    private String apiKey;
    private String code;
    private String secretKey;
    private String redirectUri;
    private String authorizationUrl;
    private String tokenUrl;
    private String scope;
    private String baseUrl;
    
     @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 