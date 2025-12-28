package com.ecommerce.inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.ecommerce.inventory.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // JPA configuration is handled by Spring Boot auto-configuration
    // This class enables JPA auditing and repository scanning
}