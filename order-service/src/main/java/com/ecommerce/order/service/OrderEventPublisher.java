package com.ecommerce.order.service;

import com.ecommerce.order.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for publishing order events to Kafka topics.
 * Handles event routing, error handling, and logging.
 */
@Service
public class OrderEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.order-created}")
    private String orderCreatedTopic;
    
    @Value("${kafka.topics.order-updated}")
    private String orderUpdatedTopic;
    
    @Value("${kafka.topics.order-cancelled}")
    private String orderCancelledTopic;
    
    @Value("${kafka.topics.order-confirmed}")
    private String orderConfirmedTopic;
    
    /**
     * Publishes an order event to the appropriate Kafka topic based on event type.
     * 
     * @param event The order event to publish
     */
    public void publishOrderEvent(OrderEvent event) {
        String topic = getTopicForEvent(event);
        String key = event.getOrderId(); // Use orderId as partition key for ordering
        
        logger.info("Publishing order event: {} to topic: {} with key: {}", 
                   event.getEventType(), topic, key);
        
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully published order event: {} to topic: {} with offset: {}",
                               event.getEventType(), topic, result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish order event: {} to topic: {}", 
                                event.getEventType(), topic, ex);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error publishing order event: {} to topic: {}", 
                        event.getEventType(), topic, e);
            throw new RuntimeException("Failed to publish order event", e);
        }
    }
    
    /**
     * Publishes an order event synchronously.
     * Use this method when you need to ensure the event is published before proceeding.
     * 
     * @param event The order event to publish
     * @throws Exception if publishing fails
     */
    public void publishOrderEventSync(OrderEvent event) throws Exception {
        String topic = getTopicForEvent(event);
        String key = event.getOrderId();
        
        logger.info("Publishing order event synchronously: {} to topic: {} with key: {}", 
                   event.getEventType(), topic, key);
        
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, event).get();
            logger.info("Successfully published order event synchronously: {} to topic: {} with offset: {}",
                       event.getEventType(), topic, result.getRecordMetadata().offset());
        } catch (Exception e) {
            logger.error("Failed to publish order event synchronously: {} to topic: {}", 
                        event.getEventType(), topic, e);
            throw e;
        }
    }
    
    /**
     * Determines the appropriate Kafka topic based on the event type.
     * 
     * @param event The order event
     * @return The topic name for the event
     */
    private String getTopicForEvent(OrderEvent event) {
        switch (event.getEventType()) {
            case "ORDER_CREATED":
                return orderCreatedTopic;
            case "ORDER_UPDATED":
                return orderUpdatedTopic;
            case "ORDER_CANCELLED":
                return orderCancelledTopic;
            case "ORDER_CONFIRMED":
                return orderConfirmedTopic;
            default:
                throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        }
    }
    
    /**
     * Publishes an order event with custom headers.
     * 
     * @param event The order event to publish
     * @param headers Custom headers to include with the message
     */
    public void publishOrderEventWithHeaders(OrderEvent event, 
                                           org.springframework.messaging.MessageHeaders headers) {
        String topic = getTopicForEvent(event);
        String key = event.getOrderId();
        
        logger.info("Publishing order event with headers: {} to topic: {} with key: {}", 
                   event.getEventType(), topic, key);
        
        try {
            org.springframework.messaging.Message<OrderEvent> message = 
                org.springframework.messaging.support.MessageBuilder
                    .withPayload(event)
                    .copyHeaders(headers.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                            java.util.Map.Entry::getKey, 
                            java.util.Map.Entry::getValue)))
                    .build();
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, key, message.getPayload());
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully published order event with headers: {} to topic: {} with offset: {}",
                               event.getEventType(), topic, result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish order event with headers: {} to topic: {}", 
                                event.getEventType(), topic, ex);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error publishing order event with headers: {} to topic: {}", 
                        event.getEventType(), topic, e);
            throw new RuntimeException("Failed to publish order event with headers", e);
        }
    }
}
