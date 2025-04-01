package com.am.marketdata.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.am.marketdata.api",  // External API module
    "com.am.marketdata.common",  // Common API module
    "com.am.marketdata.service",  // Services
    "com.am.marketdata.tradebrain"
})
public class MarketDataApiModuleConfig {
    
}
