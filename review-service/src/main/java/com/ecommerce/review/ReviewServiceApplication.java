package com.ecommerce.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Review Service Application
 * 
 * This microservice handles all review and rating operations including:
 * - Product reviews and ratings management
 * - Review moderation and approval workflows
 * - Rating aggregation and statistics
 * - Review search and filtering
 * - Event-driven review processing via Kafka
 * - Review analytics and reporting
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class ReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}