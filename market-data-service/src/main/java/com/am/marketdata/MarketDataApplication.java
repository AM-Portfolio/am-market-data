package com.am.marketdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScans({
    @ComponentScan("com.am.common.amcommondata.service"),
    @ComponentScan("com.am.common.investment.*"),
    @ComponentScan("com.am.marketdata")
})
@EnableJpaRepositories(basePackages = {
    "com.am.common.amcommondata.repository.asset"
})
@EntityScan(basePackages = {
    "com.am.common.amcommondata.domain",
    "com.am.common.amcommondata.domain.asset",
})
@SpringBootApplication
@EnableScheduling
public class MarketDataApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MarketDataApplication.class, args);
    }
}
