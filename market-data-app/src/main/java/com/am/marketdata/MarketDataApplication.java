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
import com.am.marketdata.config.MetricsConfig;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
// @EnableConfigurationProperties({ISINConfig.class, NSEIndicesConfig.class})
// @Import({ExternalApiAutoConfiguration.class, ProcessorModuleConfig.class, SchedulerAutoConfiguration.class})
//@EnableConfigurationProperties({NSEIndicesConfig.class})
//@Import({ExternalApiAutoConfiguration.class, InfluxDBConfig.class})
@Import({InfluxDBConfig.class, MetricsConfig.class})
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
    
    public static void main(String[] args) {
        // Log environment variables to debug what values are being used
        logger.info("=== ENVIRONMENT VARIABLES DEBUG ====");
        
        // MongoDB variables
        String mongoUrl = System.getenv("MONGODB_URL");
        String mongoDb = System.getenv("MONGODB_DATABASE");
        String mongoUsername = System.getenv("MONGODB_USERNAME");
        String mongoPassword = System.getenv("MONGODB_PASSWORD");
        String redisHostname = System.getenv("REDIS_HOSTNAME");
        String redisPassword = System.getenv("REDIS_PASSWORD");
        String influxUrl = System.getenv("INFLUXDB_URL");
        String influxToken = System.getenv("INFLUXDB_TOKEN");
        String influxOrg = System.getenv("INFLUXDB_ORG");
        String influxBucket = System.getenv("INFLUXDB_BUCKET");
        
        logger.info("MongoDB URL: {}", mongoUrl != null ? mongoUrl : "[NOT SET]");
        logger.info("MongoDB Database: {}", mongoDb != null ? mongoDb : "[NOT SET]");
        logger.info("MongoDB Username: {}", mongoUsername != null ? mongoUsername : "[NOT SET]");
        logger.info("MongoDB Password: {}", mongoPassword != null ? mongoPassword : "[NOT SET]");
        
        // Redis variables
        logger.info("Redis Hostname: {}", redisHostname != null ? redisHostname : "[NOT SET]");
        logger.info("Redis Password: {}", redisPassword != null ? redisPassword : "[NOT SET]");
        
        // InfluxDB variables
        logger.info("InfluxDB URL: {}", influxUrl != null ? influxUrl : "[NOT SET]");
        logger.info("InfluxDB Token: {}", influxToken != null ? influxToken : "[NOT SET]");
        logger.info("InfluxDB Org: {}", influxOrg != null ? influxOrg : "[NOT SET]");
        logger.info("InfluxDB Bucket: {}", influxBucket != null ? influxBucket : "[NOT SET]");
        
        // Log active profiles
        logger.info("=== SPRING PROPERTIES DEBUG ====");
        logger.info("Active profiles: {}", System.getProperty("spring.profiles.active"));
        logger.info("spring.data.mongodb.uri: {}", System.getProperty("spring.data.mongodb.uri"));
        logger.info("spring.data.redis.host: {}", System.getProperty("spring.data.redis.host"));
        logger.info("spring.data.redis.port: {}", System.getProperty("spring.data.redis.port"));
        logger.info("spring.data.redis.password: {}", System.getProperty("spring.data.redis.password"));
        logger.info("spring.influx.url: {}", System.getProperty("spring.influx.url"));
        
        SpringApplication.run(MarketDataApplication.class, args);
    }
}
