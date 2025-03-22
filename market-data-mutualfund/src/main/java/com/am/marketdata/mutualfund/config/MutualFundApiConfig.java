package com.am.marketdata.mutualfund.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mutual.fund.api")
@Data
public class MutualFundApiConfig {
    private String baseUrl;
    private int maxRetries;
    private long retryDelayMs;
    private int requestTimeoutMs;
    private int connectionTimeoutMs;
}
