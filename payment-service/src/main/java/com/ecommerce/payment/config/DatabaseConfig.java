package com.ecommerce.payment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.ecommerce.payment.repository")
@EntityScan(basePackages = "com.ecommerce.payment.entity")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    // JPA configuration for Payment service
}
