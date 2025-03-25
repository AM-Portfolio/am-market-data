package com.am.marketdata.scraper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "nse")
public class NSEIndicesConfig {
    private List<String> broadMarketIndices;
    private List<String> sectorIndices;
}
