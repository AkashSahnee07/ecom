package com.ecommerce.review.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration for the Review Service.
 * Configures PostgreSQL database connection and JPA settings.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.ecommerce.review.repository"
)
@Profile("!test")
public class DatabaseConfig {

    /**
     * Primary data source configuration.
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Entity Manager Factory configuration.
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.ecommerce.review.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        
        return em;
    }

    /**
     * Transaction Manager configuration.
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    /**
     * Hibernate properties configuration.
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        
        // Database dialect
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        
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
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        
        // Statistics (disable in production)
        properties.setProperty("hibernate.generate_statistics", "false");
        
        // Naming strategy
        properties.setProperty("hibernate.physical_naming_strategy", 
            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        
        // Time zone
        properties.setProperty("hibernate.jdbc.time_zone", "UTC");
        
        return properties;
    }
}