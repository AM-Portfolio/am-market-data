package com.am.marketdata.scraper.client.config;

import com.am.marketdata.scraper.client.executor.RequestExecutor;
import com.am.marketdata.scraper.service.cookie.CookieCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {

    @Autowired
    private CookieCacheService cookieCacheService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Value("${nse.api.base-url:https://www.nseindia.com}")
    private String baseUrl;

    @Bean
    @Qualifier("nseObjectMapper")
    public ObjectMapper nseObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RequestExecutor requestExecutor(@Qualifier("nseObjectMapper") ObjectMapper objectMapper, RestTemplate restTemplate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "Mozilla/5.0");
        
        return new RequestExecutor(restTemplate, meterRegistry, baseUrl, headers);
    }
}
