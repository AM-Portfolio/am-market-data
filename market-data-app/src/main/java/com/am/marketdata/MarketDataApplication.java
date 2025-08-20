package com.am.marketdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

//import com.am.marketdata.config.ISINConfig;
//import com.am.marketdata.scraper.config.NSEIndicesConfig;
import com.am.common.investment.persistence.config.InfluxDBConfig;
//import com.am.marketdata.external.api.config.ExternalApiAutoConfiguration;
//import com.am.marketdata.processor.config.ProcessorModuleConfig;
//import com.am.marketdata.scheduler.config.SchedulerAutoConfiguration;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
// @EnableConfigurationProperties({ISINConfig.class, NSEIndicesConfig.class})
// @Import({ExternalApiAutoConfiguration.class, ProcessorModuleConfig.class, SchedulerAutoConfiguration.class})
//@EnableConfigurationProperties({NSEIndicesConfig.class})
//@Import({ExternalApiAutoConfiguration.class, InfluxDBConfig.class})
@Import({InfluxDBConfig.class})
@ComponentScans({
    @ComponentScan("com.am.marketdata"),
    @ComponentScan("com.am.marketdata.external.api"),
    @ComponentScan("com.am.marketdata.scheduler"),
    @ComponentScan("com.am.marketdata.service"),
    @ComponentScan("com.am.marketdata.kafka"),
    @ComponentScan("com.am.marketdata.api"),
    @ComponentScan("com.am.marketdata.common"),
    @ComponentScan("com.am.marketdata.config"),
    @ComponentScan("com.am.marketdata.repository"),
    @ComponentScan("com.am.marketdata.model"),
    @ComponentScan("com.am.common.investment.service"),
    @ComponentScan("com.am.common.investment.persistence"),
    @ComponentScan("com.am.common.investment.persistence.config"),
    @ComponentScan("com.am.marketdata.processor"),
    @ComponentScan("com.marketdata"),
    @ComponentScan("com.marketdata.common"),
    @ComponentScan("com.marketdata.service"),
    @ComponentScan("com.marketdata.config")
})
// @EnableJpaRepositories(
//     basePackages = {
//         "com.am.marketdata.repository",
//         "com.am.common.amcommondata.domain",
//         "com.am.common.amcommondata.domain.asset",
//         "com.am.common.amcommondata.repository.portfolio",
//         "com.am.common.amcommondata.repository.asset"
//     }
// )
@EnableMongoRepositories(basePackages = "com.am.common.investment.persistence.repository")
@EnableRetry
@EnableScheduling
public class MarketDataApplication {
    private static final Logger logger = LoggerFactory.getLogger(MarketDataApplication.class);
    
    /**
     * Helper method to get environment variable with fallback
     * @param primaryKey Primary environment variable name
     * @param fallbackKey Fallback environment variable name
     * @return Value of the environment variable or null if not found
     */
    private static String getEnvWithFallback(String primaryKey, String fallbackKey) {
        String value = System.getenv(primaryKey);
        if (value == null || value.isEmpty()) {
            value = System.getenv(fallbackKey);
        }
        return value;
    }
    
    public static void main(String[] args) {
        // Log environment variables to debug what values are being used
        logger.info("=== ENVIRONMENT VARIABLES DEBUG ====");
        
        // MongoDB variables
        String mongoUrl = getEnvWithFallback("MONGODB_URL", "MONGO_URL");
        String mongoDb = getEnvWithFallback("MONGODB_DATABASE", "MONGO_DATABASE");
        String mongoUsername = getEnvWithFallback("MONGODB_USERNAME", "MONGO_USERNAME");
        String mongoPassword = getEnvWithFallback("MONGODB_PASSWORD", "MONGO_PASSWORD");
        
        logger.info("MongoDB URL: {}", mongoUrl != null ? "[SET]" : "[NOT SET]");
        logger.info("MongoDB Database: {}", mongoDb);
        logger.info("MongoDB Username: {}", mongoUsername != null ? "[SET]" : "[NOT SET]");
        logger.info("MongoDB Password: {}", mongoPassword != null ? "[SET]" : "[NOT SET]");
        
        // Redis variables
        logger.info("Redis Hostname: {}", System.getenv("REDIS_HOSTNAME"));
        logger.info("Redis Password: {}", System.getenv("REDIS_PASSWORD") != null ? "[SET]" : "[NOT SET]");
        
        // InfluxDB variables
        logger.info("InfluxDB URL: {}", System.getenv("INFLUXDB_URL"));
        logger.info("InfluxDB Token: {}", System.getenv("INFLUXDB_TOKEN") != null ? "[SET]" : "[NOT SET]");
        logger.info("InfluxDB Org: {}", System.getenv("INFLUXDB_ORG"));
        logger.info("InfluxDB Bucket: {}", System.getenv("INFLUXDB_BUCKET"));
        
        // Log active profiles
        logger.info("=== SPRING PROPERTIES DEBUG ====");
        logger.info("Active profiles: {}", System.getProperty("spring.profiles.active"));
        logger.info("spring.data.mongodb.uri: {}", System.getProperty("spring.data.mongodb.uri"));
        logger.info("spring.data.redis.url: {}", System.getProperty("spring.data.redis.url"));
        logger.info("spring.influx.url: {}", System.getProperty("spring.influx.url"));
        
        SpringApplication.run(MarketDataApplication.class, args);
    }
}
