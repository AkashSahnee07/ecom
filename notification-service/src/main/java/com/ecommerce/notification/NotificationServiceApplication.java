package com.ecommerce.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Notification Service Application
 * 
 * This microservice handles all notification-related operations including:
 * - Email notifications (order confirmations, shipping updates, etc.)
 * - SMS notifications for critical updates
 * - Push notifications for mobile apps
 * - Notification templates and preferences management
 * - Event-driven notification processing via Kafka
 * - Notification history and delivery tracking
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}