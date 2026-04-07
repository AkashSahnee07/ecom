package com.ecommerce.recommendation.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration for the recommendation service.
 * Configures caching for recommendations, user profiles, and similarity data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    /**
     * Redis connection factory configuration.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.setPassword(redisPassword);
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setValidateConnection(true);
        
        return factory;
    }

    /**
     * Redis template configuration with JSON serialization.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // Set serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();
        
        return template;
    }

    /**
     * Cache manager configuration with different TTL for different cache types.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configure JSON serialization for cache
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) // Default 1 hour TTL
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
            .disableCachingNullValues();
        
        // Specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Personalized recommendations - 30 minutes TTL
        cacheConfigurations.put("personalizedRecommendations", 
            defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Collaborative filtering recommendations - 1 hour TTL
        cacheConfigurations.put("collaborativeRecommendations", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Similar products - 2 hours TTL (more stable)
        cacheConfigurations.put("similarProducts", 
            defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Similar users - 4 hours TTL
        cacheConfigurations.put("similarUsers", 
            defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // Product similarities for collaborative filtering - 6 hours TTL
        cacheConfigurations.put("similarProductsForCF", 
            defaultConfig.entryTtl(Duration.ofHours(6)));
        
        // Category recommendations - 2 hours TTL
        cacheConfigurations.put("categoryRecommendations", 
            defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Cross-sell recommendations - 1 hour TTL
        cacheConfigurations.put("crossSellRecommendations", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Up-sell recommendations - 1 hour TTL
        cacheConfigurations.put("upSellRecommendations", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // User behavior stats - 15 minutes TTL
        cacheConfigurations.put("userBehaviorStats", 
            defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // User profiles - 2 hours TTL
        cacheConfigurations.put("userProfiles", 
            defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Product similarity calculations - 12 hours TTL
        cacheConfigurations.put("productSimilarities", 
            defaultConfig.entryTtl(Duration.ofHours(12)));
        
        // Recently viewed products - 30 minutes TTL
        cacheConfigurations.put("recentlyViewed", 
            defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Preferred categories - 4 hours TTL
        cacheConfigurations.put("preferredCategories", 
            defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // User segments - 6 hours TTL
        cacheConfigurations.put("userSegments", 
            defaultConfig.entryTtl(Duration.ofHours(6)));
        
        // Trending products - 1 hour TTL
        cacheConfigurations.put("trendingProducts", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Popular products in category - 2 hours TTL
        cacheConfigurations.put("popularInCategory", 
            defaultConfig.entryTtl(Duration.ofHours(2)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }

    /**
     * Redis template specifically for recommendation data.
     */
    @Bean("recommendationRedisTemplate")
    public RedisTemplate<String, Object> recommendationRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure serialization for recommendation objects
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        
        // Custom serializer for recommendation objects
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(objectMapper);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis template for user behavior data.
     */
    @Bean("userBehaviorRedisTemplate")
    public RedisTemplate<String, Object> userBehaviorRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure serialization optimized for user behavior data
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis template for similarity calculations.
     */
    @Bean("similarityRedisTemplate")
    public RedisTemplate<String, Double> similarityRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Double> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}