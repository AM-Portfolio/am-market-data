package com.am.marketdata.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "isin")
public record ISINConfig(List<String> list) {
    public ISINConfig {
        list = list != null ? list : new ArrayList<>();
    }
}
