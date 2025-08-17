package com.am.marketdata.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis configuration for market data caching
 */
@Configuration
@EnableCaching
public class RedisConfig {
    
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${REDIS_HOSTNAME:redis.dev.svc.cluster.local}")
    private String redisHost;

    // @Value("${spring.data.redis.port:6379}")
    // private String redisPort;

    @Value("${REDIS_PASSWORD}")
    private String redisPassword;

    @Value("${market.data.cache.ttl.seconds:300}")
    private long cacheTimeToLiveSeconds;

    /**
     * Redis connection factory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        
        log.info("Redis connection details - Host: {}, Port: {}", redisHost, 6379);
        log.info("Redis password is {}", redisPassword != null && !redisPassword.isEmpty() ? "provided" : "not provided");
        
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(6379);
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            log.info("Setting Redis password for authentication");
            redisConfig.setPassword(redisPassword);
        } else {
            log.warn("No Redis password provided, connecting without authentication");
        }
        
        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * ObjectMapper configured with JavaTimeModule for handling Java 8 date/time types
     * Using @Qualifier to avoid conflicts with other ObjectMapper beans
     * Using @Primary to make this the default ObjectMapper when no qualifier is specified
     */
    @Bean
    @Primary
    @Qualifier("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
    
    /**
     * Redis template for operations with JSR310 support
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        
        // Create Jackson serializer with JSR310 support (non-deprecated approach)
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);
        
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis cache manager with TTL configuration and JSR310 support
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, 
                                         @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        // Create Jackson serializer with JSR310 support (non-deprecated approach)
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);
        
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(cacheTimeToLiveSeconds))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
