package com.am.marketdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScans({
    @ComponentScan("com.am.marketdata"),
    @ComponentScan("com.am.marketdata.scraper"),
    @ComponentScan("com.am.marketdata.service"),
    @ComponentScan("com.am.marketdata.kafka"),
    @ComponentScan("com.am.marketdata.api"),
    @ComponentScan("com.am.marketdata.common"),
    @ComponentScan("com.am.marketdata.config"),
    @ComponentScan("com.am.marketdata.repository"),
    @ComponentScan("com.am.marketdata.model"),
    @ComponentScan("com.am.common.investment.*"),
    @ComponentScan("com.am.marketdata.kafka"),
    @ComponentScan("com.am.common.amcommondata.mapper"),
    @ComponentScan("com.am.common.amcommondata.service"),
    @ComponentScan("com.am.common.amcommondata.domain"),
    @ComponentScan("com.am.common.amcommondata.domain.asset"),
})
@EnableJpaRepositories(
    basePackages = {
        "com.am.marketdata.repository",
        "com.am.common.amcommondata.domain",
        "com.am.common.amcommondata.domain.asset",
        "com.am.common.amcommondata.repository.portfolio",
        "com.am.common.amcommondata.repository.asset"
    }
)
@EntityScan(basePackages = {
    "com.am.marketdata.model",
    "com.am.common.amcommondata.domain",
    "com.am.common.amcommondata.domain.asset",
    "com.am.common.investment.persistence.model"
})
@EnableRetry
@EnableScheduling
public class MarketDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketDataApplication.class, args);
    }
}
