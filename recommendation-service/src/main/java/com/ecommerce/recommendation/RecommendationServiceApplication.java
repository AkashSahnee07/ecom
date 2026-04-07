package com.ecommerce.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Recommendation Service Application
 * 
 * This microservice provides intelligent product recommendations using machine learning algorithms.
 * Key responsibilities:
 * - Collaborative filtering recommendations
 * - Content-based filtering recommendations
 * - Hybrid recommendation algorithms
 * - Real-time recommendation updates
 * - User behavior analysis and tracking
 * - Product similarity calculations
 * - Trending products identification
 * - Personalized recommendation scoring
 * - A/B testing for recommendation strategies
 * - Recommendation performance analytics
 * 
 * Features:
 * - Multiple recommendation algorithms (collaborative, content-based, hybrid)
 * - Real-time user behavior tracking
 * - Machine learning model training and inference
 * - Recommendation caching and optimization
 * - Analytics and performance monitoring
 * - Integration with user, product, and order services
 * 
 * @author Ecommerce Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableCaching
@EnableTransactionManagement
public class RecommendationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendationServiceApplication.class, args);
    }
}
