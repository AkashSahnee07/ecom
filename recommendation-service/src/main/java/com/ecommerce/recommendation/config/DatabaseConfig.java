package com.ecommerce.recommendation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration for the recommendation service.
 * Configures PostgreSQL connection, JPA settings, and transaction management.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
@EnableJpaRepositories(
    basePackages = "com.ecommerce.recommendation.repository"
)
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {

    // Use Spring Boot's auto-configured DataSource (HikariCP) bound from spring.datasource.*

    /**
     * Entity manager factory configuration.
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.ecommerce.recommendation.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        
        return em;
    }

    /**
     * Transaction manager configuration.
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    /**
     * Hibernate properties configuration.
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        
        // DDL generation
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        
        // SQL logging (disable in production)
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "false");
        
        // Performance settings
        properties.setProperty("hibernate.jdbc.batch_size", "25");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        
        // Connection pool settings
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        
        // Cache settings
        // Delegate cache configuration to Spring Boot via application.yml profiles.
        // Removing explicit JCache region factory to avoid missing dependency issues.
        
        // Statistics (disable in production)
        properties.setProperty("hibernate.generate_statistics", "false");
        
        // Naming strategy
        // Use Spring Boot defaults for Hibernate naming strategies (Hibernate 6 compatible).
        
        return properties;
    }
}