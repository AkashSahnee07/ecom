package com.ecommerce.recommendation.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for the recommendation service.
 * Configures producers and consumers for handling recommendation-related events.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Kafka producer configuration.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Performance and reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        // Idempotence for exactly-once semantics
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        // Compression
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka template for sending messages.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Kafka consumer configuration.
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Consumer behavior
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit for reliability
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutes
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10 seconds
        
        // Deserialization settings
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ecommerce.*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.lang.Object");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka listener container factory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Concurrency settings
        factory.setConcurrency(3); // Number of consumer threads
        
        // Acknowledgment mode
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Error handling
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        
        // Batch processing (optional)
        factory.setBatchListener(false);
        
        return factory;
    }

    /**
     * Kafka listener container factory for batch processing.
     */
    @Bean("batchKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(2);
        
        // Batch acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        
        return factory;
    }

    /**
     * Consumer factory for user behavior events.
     */
    @Bean("userBehaviorConsumerFactory")
    public ConsumerFactory<String, Object> userBehaviorConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-user-behavior");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Specific settings for user behavior processing
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Higher batch size
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ecommerce.*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Listener container factory for user behavior events.
     */
    @Bean("userBehaviorKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> userBehaviorKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(userBehaviorConsumerFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(4); // Higher concurrency for user behavior processing
        
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        
        return factory;
    }

    /**
     * Consumer factory for product events.
     */
    @Bean("productEventConsumerFactory")
    public ConsumerFactory<String, Object> productEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-product-events");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ecommerce.*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Listener container factory for product events.
     */
    @Bean("productEventKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> productEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(productEventConsumerFactory());
        factory.setConcurrency(2);
        
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }
}