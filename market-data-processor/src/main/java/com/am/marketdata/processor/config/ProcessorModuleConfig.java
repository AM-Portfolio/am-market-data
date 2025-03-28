package com.am.marketdata.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {
    "com.am.marketdata.processor",
    "com.am.marketdata.processor.service",
    "com.am.marketdata.common",
    "com.am.marketdata.external" // Assuming external API package
})
@Import({
    // Import any required configurations from other modules
    // This will be populated as needed
})
@EnableJpaRepositories(basePackages = "com.am.marketdata.processor.repository")
@EntityScan(basePackages = "com.am.marketdata.processor.entity")
public class ProcessorModuleConfig {

    /**
     * Dedicated thread pool for data processing tasks
     * Based on memory #5ba7ad90-715c-4238-929a-4a58c2ace265
     */
    @Bean(name = "processorTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("market-processor-");
        executor.initialize();
        return executor;
    }
}
