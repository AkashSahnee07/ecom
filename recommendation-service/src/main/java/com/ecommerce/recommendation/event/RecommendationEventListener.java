package com.ecommerce.recommendation.event;

import com.ecommerce.recommendation.entity.UserBehavior;
import com.ecommerce.recommendation.entity.ProductSimilarity;
import com.ecommerce.recommendation.service.UserBehaviorService;
import com.ecommerce.recommendation.service.UserProfileService;
import com.ecommerce.recommendation.service.ProductSimilarityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka event listener for processing recommendation-related events.
 * Handles user behavior events, product events, and order events from other microservices.
 */
@Component
@RequiredArgsConstructor
public class RecommendationEventListener {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEventListener.class);
    
    private final UserBehaviorService userBehaviorService;
    private final UserProfileService userProfileService;
    private final ProductSimilarityService productSimilarityService;
    private final ObjectMapper objectMapper;

    /**
     * Handle user behavior events (views, clicks, searches, etc.).
     */
    @KafkaListener(
        topics = "user-behavior-events",
        groupId = "recommendation-service-user-behavior",
        containerFactory = "userBehaviorKafkaListenerContainerFactory"
    )
    public void handleUserBehaviorEvents(
            @Payload List<Map<String, Object>> events,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received {} user behavior events from topic: {}, partition: {}, offset: {}", 
                events.size(), topic, partition, offset);
        
        try {
            for (Map<String, Object> eventData : events) {
                processUserBehaviorEvent(eventData);
            }
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            log.info("Successfully processed {} user behavior events", events.size());
            
        } catch (Exception e) {
            log.error("Error processing user behavior events from offset: {}", offset, e);
            // Don't acknowledge - message will be retried
        }
    }

    /**
     * Handle product events (created, updated, deleted).
     */
    @KafkaListener(
        topics = "product-events",
        groupId = "recommendation-service-product-events",
        containerFactory = "productEventKafkaListenerContainerFactory"
    )
    public void handleProductEvents(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received product event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            processProductEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed product event for product: {}", 
                    event.get("productId"));
            
        } catch (Exception e) {
            log.error("Error processing product event from offset: {}", offset, e);
        }
    }

    /**
     * Handle order events (completed, cancelled).
     */
    @KafkaListener(
        topics = "order-events",
        groupId = "recommendation-service-order-events"
    )
    public void handleOrderEvents(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.info("Received order event from topic: {}", topic);
        
        try {
            processOrderEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed order event for order: {}", 
                    event.get("orderId"));
            
        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    /**
     * Handle cart events (add, remove, abandon).
     */
    @KafkaListener(
        topics = "cart-events",
        groupId = "recommendation-service-cart-events"
    )
    public void handleCartEvents(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.info("Received cart event from topic: {}", topic);
        
        try {
            processCartEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed cart event for user: {}", 
                    event.get("userId"));
            
        } catch (Exception e) {
            log.error("Error processing cart event", e);
        }
    }

    /**
     * Handle user registration/profile events.
     */
    @KafkaListener(
        topics = "user-events",
        groupId = "recommendation-service-user-events"
    )
    public void handleUserEvents(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.info("Received user event from topic: {}", topic);
        
        try {
            processUserEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed user event for user: {}", 
                    event.get("userId"));
            
        } catch (Exception e) {
            log.error("Error processing user event", e);
        }
    }

    /**
     * Process individual user behavior event.
     */
    private void processUserBehaviorEvent(Map<String, Object> eventData) {
        try {
            String eventType = (String) eventData.get("eventType");
            String userId = (String) eventData.get("userId");
            String productId = (String) eventData.get("productId");
            
            if (userId == null || productId == null) {
                log.warn("Invalid user behavior event - missing userId or productId: {}", eventData);
                return;
            }
            
            // Map event type to action type
            UserBehavior.ActionType actionType = mapEventTypeToActionType(eventType);
            
            if (actionType == null) {
                log.warn("Unknown event type: {}", eventType);
                return;
            }
            
            // Create user behavior record
            UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .productId(productId)
                .actionType(actionType)
                .sessionId((String) eventData.get("sessionId"))
                .deviceType((String) eventData.get("deviceType"))
                .referrer((String) eventData.get("referrer"))
                .durationSeconds(getIntegerValue(eventData, "duration"))
                .quantity(getIntegerValue(eventData, "quantity"))
                .price(getDoubleValue(eventData, "price"))
                .timestamp(LocalDateTime.now())
                .build();
            
            // Record the behavior
            userBehaviorService.recordBehavior(
                behavior.getUserId(),
                behavior.getProductId(),
                behavior.getActionType(),
                createMetadataMap(behavior)
            );
            
            // Update user profile if needed
            if (shouldUpdateUserProfile(actionType)) {
                userProfileService.updateBehaviorScores(userId);
            }
            
            log.debug("Processed user behavior event: {} for user: {} on product: {}", 
                     eventType, userId, productId);
            
        } catch (Exception e) {
            log.error("Error processing user behavior event: {}", eventData, e);
            throw e;
        }
    }

    /**
     * Process product event (created, updated, deleted).
     */
    private void processProductEvent(Map<String, Object> eventData) {
        try {
            String eventType = (String) eventData.get("eventType");
            String productId = (String) eventData.get("productId");
            
            if (productId == null) {
                log.warn("Invalid product event - missing productId: {}", eventData);
                return;
            }
            
            switch (eventType) {
                case "PRODUCT_CREATED":
                case "PRODUCT_UPDATED":
                    // Trigger similarity recalculation for the product
                    List<ProductSimilarity.SimilarityType> allTypes = Arrays.asList(
                        ProductSimilarity.SimilarityType.CONTENT_BASED,
                        ProductSimilarity.SimilarityType.COLLABORATIVE,
                        ProductSimilarity.SimilarityType.CATEGORY
                    );
                    productSimilarityService.refreshProductSimilarities(productId, allTypes, eventData);
                    log.debug("Triggered similarity refresh for product: {}", productId);
                    break;
                    
                case "PRODUCT_DELETED":
                    // Clean up similarities for deleted product - using repository directly
                    // productSimilarityService.cleanupProductSimilarities(productId);
                    log.debug("Product deleted - similarities will be cleaned up by scheduled task: {}", productId);
                    break;
                    
                default:
                    log.debug("Unhandled product event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing product event: {}", eventData, e);
            throw e;
        }
    }

    /**
     * Process order event (completed, cancelled).
     */
    private void processOrderEvent(Map<String, Object> eventData) {
        try {
            String eventType = (String) eventData.get("eventType");
            String userId = (String) eventData.get("userId");
            String orderId = (String) eventData.get("orderId");
            
            if (userId == null || orderId == null) {
                log.warn("Invalid order event - missing userId or orderId: {}", eventData);
                return;
            }
            
            switch (eventType) {
                case "ORDER_COMPLETED":
                    // Process completed order
                    processCompletedOrder(eventData);
                    break;
                    
                case "ORDER_CANCELLED":
                    // Handle order cancellation
                    log.debug("Order cancelled: {} for user: {}", orderId, userId);
                    break;
                    
                default:
                    log.debug("Unhandled order event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing order event: {}", eventData, e);
            throw e;
        }
    }

    /**
     * Process cart event (add, remove, abandon).
     */
    private void processCartEvent(Map<String, Object> eventData) {
        try {
            String eventType = (String) eventData.get("eventType");
            String userId = (String) eventData.get("userId");
            String productId = (String) eventData.get("productId");
            
            if (userId == null) {
                log.warn("Invalid cart event - missing userId: {}", eventData);
                return;
            }
            
            UserBehavior.ActionType actionType = null;
            
            switch (eventType) {
                case "CART_ADD":
                    actionType = UserBehavior.ActionType.ADD_TO_CART;
                    break;
                case "CART_REMOVE":
                    actionType = UserBehavior.ActionType.REMOVE_FROM_CART;
                    break;
                case "CART_ABANDON":
                    // Handle cart abandonment - could trigger abandoned cart recommendations
                    log.debug("Cart abandoned by user: {}", userId);
                    return;
                default:
                    log.debug("Unhandled cart event type: {}", eventType);
                    return;
            }
            
            if (productId != null && actionType != null) {
                // Record cart behavior
                UserBehavior behavior = UserBehavior.builder()
                    .userId(userId)
                    .productId(productId)
                    .actionType(actionType)
                    .sessionId((String) eventData.get("sessionId"))
                    .quantity(getIntegerValue(eventData, "quantity"))
                    .price(getDoubleValue(eventData, "price"))
                    .timestamp(LocalDateTime.now())
                    .build();
                
                userBehaviorService.recordBehavior(
                behavior.getUserId(),
                behavior.getProductId(),
                behavior.getActionType(),
                createMetadataMap(behavior)
            );
            }
            
        } catch (Exception e) {
            log.error("Error processing cart event: {}", eventData, e);
            throw e;
        }
    }

    /**
     * Process user event (registration, profile update).
     */
    private void processUserEvent(Map<String, Object> eventData) {
        try {
            String eventType = (String) eventData.get("eventType");
            String userId = (String) eventData.get("userId");
            
            if (userId == null) {
                log.warn("Invalid user event - missing userId: {}", eventData);
                return;
            }
            
            switch (eventType) {
                case "USER_REGISTERED":
                    // Create initial user profile
                    String email = (String) eventData.get("email");
                    if (email != null) {
                        userProfileService.createUserProfile(userId, email);
                        log.debug("Created user profile for new user: {}", userId);
                    }
                    break;
                    
                case "USER_PROFILE_UPDATED":
                    // Update user profile preferences
                    List<String> categories = (List<String>) eventData.get("preferredCategories");
                    List<String> brands = (List<String>) eventData.get("preferredBrands");
                    Map<String, Object> preferences = (Map<String, Object>) eventData.get("preferences");
                    userProfileService.updateUserPreferences(userId, categories, brands, preferences);
                    log.debug("Updated user profile for user: {}", userId);
                    break;
                    
                default:
                    log.debug("Unhandled user event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing user event: {}", eventData, e);
            throw e;
        }
    }

    /**
     * Process completed order to record purchase behaviors.
     */
    private void processCompletedOrder(Map<String, Object> eventData) {
        try {
            String userId = (String) eventData.get("userId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = 
                (List<Map<String, Object>>) eventData.get("orderItems");
            
            if (orderItems == null || orderItems.isEmpty()) {
                log.warn("Order completed event has no items: {}", eventData);
                return;
            }
            
            // Record purchase behavior for each item
            for (Map<String, Object> item : orderItems) {
                String productId = (String) item.get("productId");
                
                if (productId != null) {
                    UserBehavior behavior = UserBehavior.builder()
                        .userId(userId)
                        .productId(productId)
                        .actionType(UserBehavior.ActionType.PURCHASE)
                        .quantity(getIntegerValue(item, "quantity"))
                        .price(getDoubleValue(item, "price"))
                        .timestamp(LocalDateTime.now())
                        .build();
                    
                    userBehaviorService.recordBehavior(
                behavior.getUserId(),
                behavior.getProductId(),
                behavior.getActionType(),
                createMetadataMap(behavior)
            );
                }
            }
            
            // Update user profile with purchase information
            userProfileService.updateUserProfileFromOrder(userId, eventData);
            
            log.debug("Processed completed order with {} items for user: {}", 
                     orderItems.size(), userId);
            
        } catch (Exception e) {
            log.error("Error processing completed order: {}", eventData, e);
            throw e;
        }
    }

    // Helper methods
    
    private UserBehavior.ActionType mapEventTypeToActionType(String eventType) {
        if (eventType == null) return null;
        
        switch (eventType.toUpperCase()) {
            case "PRODUCT_VIEW":
            case "PAGE_VIEW":
                return UserBehavior.ActionType.VIEW;
            case "PRODUCT_CLICK":
            case "CLICK":
                return UserBehavior.ActionType.CLICK;
            case "SEARCH":
                return UserBehavior.ActionType.SEARCH;
            case "FILTER":
                return UserBehavior.ActionType.SEARCH;
            case "SORT":
                return UserBehavior.ActionType.SEARCH;
            case "SHARE":
                return UserBehavior.ActionType.SHARE;
            case "LIKE":
            case "FAVORITE":
                return UserBehavior.ActionType.ADD_TO_WISHLIST;
            case "REVIEW":
            case "RATE":
                return UserBehavior.ActionType.REVIEW;
            case "WISHLIST_ADD":
                return UserBehavior.ActionType.ADD_TO_WISHLIST;
            case "WISHLIST_REMOVE":
                return UserBehavior.ActionType.REMOVE_FROM_WISHLIST;
            default:
                return null;
        }
    }
    
    private boolean shouldUpdateUserProfile(UserBehavior.ActionType actionType) {
        return actionType == UserBehavior.ActionType.PURCHASE ||
               actionType == UserBehavior.ActionType.ADD_TO_WISHLIST ||
               actionType == UserBehavior.ActionType.REVIEW;
    }
    
    private Map<String, Object> createMetadataMap(UserBehavior behavior) {
        Map<String, Object> metadata = new HashMap<>();
        if (behavior.getSessionId() != null) metadata.put("sessionId", behavior.getSessionId());
        if (behavior.getDeviceType() != null) metadata.put("deviceType", behavior.getDeviceType());
        if (behavior.getReferrer() != null) metadata.put("referrer", behavior.getReferrer());
         if (behavior.getDurationSeconds() != null) metadata.put("duration", behavior.getDurationSeconds());
        if (behavior.getRating() != null) metadata.put("rating", behavior.getRating());
        if (behavior.getQuantity() != null) metadata.put("quantity", behavior.getQuantity());
        if (behavior.getPrice() != null) metadata.put("price", behavior.getPrice());
        return metadata;
    }
    
    private Integer getIntegerValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Double getDoubleValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
