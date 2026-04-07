package com.ecommerce.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Shipping Service.
 * 
 * This microservice handles:
 * - Shipment creation and management
 * - Tracking events and status updates
 * - Integration with shipping carriers
 * - Real-time tracking updates
 * - Notification publishing
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class ShippingServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ShippingServiceApplication.class, args);
    }
}
