package com.ecommerce.order.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.ecommerce.order.repository")
@EntityScan(basePackages = "com.ecommerce.order.entity")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    // JPA and database configuration
}