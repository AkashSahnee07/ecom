package com.ecommerce.notification.listener;

import com.ecommerce.notification.entity.NotificationChannel;
import com.ecommerce.notification.entity.NotificationPriority;
import com.ecommerce.notification.entity.NotificationType;
import com.ecommerce.notification.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka event listener for processing notification events from other microservices
 */
@Component
public class NotificationEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Listen to order events
     */
    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received order event from topic: {}, offset: {}", topic, offset);
            
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            JsonNode orderData = event.get("data");
            
            switch (eventType) {
                case "ORDER_CREATED":
                    handleOrderCreated(orderData);
                    break;
                case "ORDER_CONFIRMED":
                    handleOrderConfirmed(orderData);
                    break;
                case "ORDER_SHIPPED":
                    handleOrderShipped(orderData);
                    break;
                case "ORDER_DELIVERED":
                    handleOrderDelivered(orderData);
                    break;
                case "ORDER_CANCELLED":
                    handleOrderCancelled(orderData);
                    break;
                default:
                    logger.warn("Unknown order event type: {}", eventType);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Failed to process order event: {}", message, e);
            // Don't acknowledge on error - message will be retried
        }
    }
    
    /**
     * Listen to payment events
     */
    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void handlePaymentEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received payment event from topic: {}", topic);
            
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            JsonNode paymentData = event.get("data");
            
            switch (eventType) {
                case "PAYMENT_PROCESSED":
                    handlePaymentProcessed(paymentData);
                    break;
                case "PAYMENT_FAILED":
                    handlePaymentFailed(paymentData);
                    break;
                case "PAYMENT_REFUNDED":
                    handlePaymentRefunded(paymentData);
                    break;
                default:
                    logger.warn("Unknown payment event type: {}", eventType);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Failed to process payment event: {}", message, e);
        }
    }
    
    /**
     * Listen to user events
     */
    @KafkaListener(topics = "user-events", groupId = "notification-service")
    public void handleUserEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received user event from topic: {}", topic);
            
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            JsonNode userData = event.get("data");
            
            switch (eventType) {
                case "USER_REGISTERED":
                    handleUserRegistered(userData);
                    break;
                case "USER_EMAIL_VERIFICATION_REQUESTED":
                    handleEmailVerificationRequested(userData);
                    break;
                case "PASSWORD_RESET_REQUESTED":
                    handlePasswordResetRequested(userData);
                    break;
                case "SECURITY_ALERT":
                    handleSecurityAlert(userData);
                    break;
                default:
                    logger.warn("Unknown user event type: {}", eventType);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Failed to process user event: {}", message, e);
        }
    }
    
    /**
     * Listen to inventory events
     */
    @KafkaListener(topics = "inventory-events", groupId = "notification-service")
    public void handleInventoryEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received inventory event from topic: {}", topic);
            
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            JsonNode inventoryData = event.get("data");
            
            switch (eventType) {
                case "LOW_STOCK_ALERT":
                    handleLowStockAlert(inventoryData);
                    break;
                case "OUT_OF_STOCK":
                    handleOutOfStock(inventoryData);
                    break;
                case "STOCK_REPLENISHED":
                    handleStockReplenished(inventoryData);
                    break;
                default:
                    logger.warn("Unknown inventory event type: {}", eventType);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Failed to process inventory event: {}", message, e);
        }
    }
    
    /**
     * Listen to promotional events
     */
    @KafkaListener(topics = "promotion-events", groupId = "notification-service")
    public void handlePromotionEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received promotion event from topic: {}", topic);
            
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            JsonNode promotionData = event.get("data");
            
            switch (eventType) {
                case "PROMOTION_STARTED":
                    handlePromotionStarted(promotionData);
                    break;
                case "DISCOUNT_OFFER":
                    handleDiscountOffer(promotionData);
                    break;
                case "FLASH_SALE":
                    handleFlashSale(promotionData);
                    break;
                default:
                    logger.warn("Unknown promotion event type: {}", eventType);
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Failed to process promotion event: {}", message, e);
        }
    }
    
    // Order Event Handlers
    private void handleOrderCreated(JsonNode orderData) {
        String customerId = orderData.get("customerId").asText();
        String orderId = orderData.get("orderId").asText();
        String customerEmail = orderData.get("customerEmail").asText();
        String customerName = orderData.get("customerName").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderId", orderId);
        
        // Send order created notification (internal tracking)
        notificationService.sendNotificationAsync(
            customerId, customerEmail, null, null,
            NotificationType.ORDER_CREATED,
            NotificationChannel.EMAIL,
            "Order Created",
            "Your order has been created and is being processed.",
            NotificationPriority.MEDIUM,
            variables,
            orderId,
            "order-service"
        );
    }
    
    private void handleOrderConfirmed(JsonNode orderData) {
        String customerId = orderData.get("customerId").asText();
        String orderId = orderData.get("orderId").asText();
        String customerEmail = orderData.get("customerEmail").asText();
        String customerPhone = orderData.has("customerPhone") ? orderData.get("customerPhone").asText() : null;
        String customerName = orderData.get("customerName").asText();
        String orderTotal = orderData.get("total").asText();
        String orderItems = orderData.get("items").toString();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderId", orderId);
        variables.put("orderTotal", orderTotal);
        variables.put("orderItems", orderItems);
        variables.put("orderDate", orderData.get("createdAt").asText());
        
        // Send email confirmation
        notificationService.sendNotificationAsync(
            customerId, customerEmail, null, null,
            NotificationType.ORDER_CONFIRMATION,
            NotificationChannel.EMAIL,
            "Order Confirmation - #" + orderId,
            "Your order has been confirmed!",
            NotificationPriority.HIGH,
            variables,
            orderId,
            "order-service"
        );
        
        // Send SMS if phone number available
        if (customerPhone != null && !customerPhone.trim().isEmpty()) {
            notificationService.sendNotificationAsync(
                customerId, null, customerPhone, null,
                NotificationType.ORDER_CONFIRMATION,
                NotificationChannel.SMS,
                "Order Confirmed",
                "Your order has been confirmed!",
                NotificationPriority.HIGH,
                variables,
                orderId,
                "order-service"
            );
        }
    }
    
    private void handleOrderShipped(JsonNode orderData) {
        String customerId = orderData.get("customerId").asText();
        String orderId = orderData.get("orderId").asText();
        String customerEmail = orderData.get("customerEmail").asText();
        String customerPhone = orderData.has("customerPhone") ? orderData.get("customerPhone").asText() : null;
        String customerName = orderData.get("customerName").asText();
        String trackingNumber = orderData.get("trackingNumber").asText();
        String carrier = orderData.get("carrier").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderId", orderId);
        variables.put("trackingNumber", trackingNumber);
        variables.put("carrier", carrier);
        variables.put("expectedDelivery", orderData.get("expectedDelivery").asText());
        
        // Send email notification
        notificationService.sendNotificationAsync(
            customerId, customerEmail, null, null,
            NotificationType.ORDER_SHIPPED,
            NotificationChannel.EMAIL,
            "Your Order Has Shipped - #" + orderId,
            "Your order is on the way!",
            NotificationPriority.HIGH,
            variables,
            orderId,
            "shipping-service"
        );
        
        // Send SMS notification
        if (customerPhone != null && !customerPhone.trim().isEmpty()) {
            notificationService.sendNotificationAsync(
                customerId, null, customerPhone, null,
                NotificationType.ORDER_SHIPPED,
                NotificationChannel.SMS,
                "Order Shipped",
                "Your order is on the way!",
                NotificationPriority.HIGH,
                variables,
                orderId,
                "shipping-service"
            );
        }
    }
    
    private void handleOrderDelivered(JsonNode orderData) {
        String customerId = orderData.get("customerId").asText();
        String orderId = orderData.get("orderId").asText();
        String customerEmail = orderData.get("customerEmail").asText();
        String customerName = orderData.get("customerName").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderId", orderId);
        variables.put("deliveryDate", orderData.get("deliveredAt").asText());
        variables.put("deliveryAddress", orderData.get("deliveryAddress").asText());
        
        // Send delivery confirmation
        notificationService.sendNotificationAsync(
            customerId, customerEmail, null, null,
            NotificationType.ORDER_DELIVERED,
            NotificationChannel.EMAIL,
            "Order Delivered - #" + orderId,
            "Your order has been delivered!",
            NotificationPriority.MEDIUM,
            variables,
            orderId,
            "shipping-service"
        );
    }
    
    private void handleOrderCancelled(JsonNode orderData) {
        String customerId = orderData.get("customerId").asText();
        String orderId = orderData.get("orderId").asText();
        String customerEmail = orderData.get("customerEmail").asText();
        String customerName = orderData.get("customerName").asText();
        String reason = orderData.get("cancellationReason").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderId", orderId);
        variables.put("reason", reason);
        
        // Send cancellation notification
        notificationService.sendNotificationAsync(
            customerId, customerEmail, null, null,
            NotificationType.ORDER_CANCELLED,
            NotificationChannel.EMAIL,
            "Order Cancelled - #" + orderId,
            "Your order has been cancelled.",
            NotificationPriority.HIGH,
            variables,
            orderId,
            "order-service"
        );
    }
    
    // Payment Event Handlers
    private void handlePaymentProcessed(JsonNode paymentData) {
        String customerId = paymentData.get("customerId").asText();
        String orderId = paymentData.get("orderId").asText();
        String customerEmail = paymentData.get("customerEmail").asText();
        String customerName = paymentData.get("customerName").asText();
        String amount = paymentData.get("amount").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderId", orderId);
        variables.put("amount", amount);
        variables.put("paymentMethod", paymentData.get("paymentMethod").asText());
        variables.put("transactionId", paymentData.get("transactionId").asText());
        
        // Send payment success notification
        notificationService.sendNotificationAsync(
            customerId, customerEmail, null, null,
            NotificationType.PAYMENT_SUCCESS,
            NotificationChannel.EMAIL,
            "Payment Successful - #" + orderId,
            "Your payment has been processed successfully.",
            NotificationPriority.HIGH,
            variables,
            orderId,
            "payment-service"
        );
    }
    
    private void handlePaymentFailed(JsonNode paymentData) {
        String customerId = paymentData.get("customerId").asText();
        String orderId = paymentData.get("orderId").asText();
        String customerEmail = paymentData.get("customerEmail").asText();
        String customerName = paymentData.get("customerName").asText();
        String amount = paymentData.get("amount").asText();
        String failureReason = paymentData.get("failureReason").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderId", orderId);
        variables.put("amount", amount);
        variables.put("failureReason", failureReason);
        variables.put("paymentRetryLink", "https://ecommerce.com/payment/retry/" + orderId);
        
        // Send payment failure notification
        notificationService.sendNotificationAsync(
            customerId, customerEmail, null, null,
            NotificationType.PAYMENT_FAILED,
            NotificationChannel.EMAIL,
            "Payment Failed - #" + orderId,
            "We were unable to process your payment.",
            NotificationPriority.CRITICAL,
            variables,
            orderId,
            "payment-service"
        );
    }
    
    private void handlePaymentRefunded(JsonNode paymentData) {
        String customerId = paymentData.get("customerId").asText();
        String orderId = paymentData.get("orderId").asText();
        String customerEmail = paymentData.get("customerEmail").asText();
        String customerName = paymentData.get("customerName").asText();
        String refundAmount = paymentData.get("refundAmount").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("orderId", orderId);
        variables.put("refundAmount", refundAmount);
        variables.put("refundReason", paymentData.get("refundReason").asText());
        
        // Send refund notification
        notificationService.sendNotificationAsync(
            customerId, customerEmail, null, null,
            NotificationType.REFUND_PROCESSED,
            NotificationChannel.EMAIL,
            "Refund Processed - #" + orderId,
            "Your refund has been processed.",
            NotificationPriority.MEDIUM,
            variables,
            orderId,
            "payment-service"
        );
    }
    
    // User Event Handlers
    private void handleUserRegistered(JsonNode userData) {
        String userId = userData.get("userId").asText();
        String email = userData.get("email").asText();
        String name = userData.get("name").asText();
        String verificationToken = userData.get("verificationToken").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", name);
        variables.put("verificationLink", "https://ecommerce.com/verify-email?token=" + verificationToken);
        
        // Send welcome email
        notificationService.sendNotificationAsync(
            userId, email, null, null,
            NotificationType.WELCOME,
            NotificationChannel.EMAIL,
            "Welcome to E-Commerce Platform!",
            "Welcome to our platform!",
            NotificationPriority.MEDIUM,
            variables,
            userId,
            "user-service"
        );
    }
    
    private void handleEmailVerificationRequested(JsonNode userData) {
        String userId = userData.get("userId").asText();
        String email = userData.get("email").asText();
        String name = userData.get("name").asText();
        String verificationToken = userData.get("verificationToken").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", name);
        variables.put("verificationLink", "https://ecommerce.com/verify-email?token=" + verificationToken);
        
        // Send email verification
        notificationService.sendNotificationAsync(
            userId, email, null, null,
            NotificationType.EMAIL_VERIFICATION,
            NotificationChannel.EMAIL,
            "Please Verify Your Email",
            "Please verify your email address.",
            NotificationPriority.HIGH,
            variables,
            userId,
            "user-service"
        );
    }
    
    private void handlePasswordResetRequested(JsonNode userData) {
        String userId = userData.get("userId").asText();
        String email = userData.get("email").asText();
        String name = userData.get("name").asText();
        String resetToken = userData.get("resetToken").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", name);
        variables.put("resetLink", "https://ecommerce.com/reset-password?token=" + resetToken);
        variables.put("expirationTime", "15 minutes");
        
        // Send password reset email
        notificationService.sendNotificationAsync(
            userId, email, null, null,
            NotificationType.PASSWORD_RESET,
            NotificationChannel.EMAIL,
            "Password Reset Request",
            "Reset your password.",
            NotificationPriority.HIGH,
            variables,
            userId,
            "user-service"
        );
    }
    
    private void handleSecurityAlert(JsonNode userData) {
        String userId = userData.get("userId").asText();
        String email = userData.get("email").asText();
        String name = userData.get("name").asText();
        String alertMessage = userData.get("alertMessage").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", name);
        variables.put("alertMessage", alertMessage);
        variables.put("alertTime", userData.get("alertTime").asText());
        variables.put("alertLocation", userData.get("location").asText());
        variables.put("secureAccountLink", "https://ecommerce.com/account/security");
        
        // Send security alert
        notificationService.sendNotificationAsync(
            userId, email, null, null,
            NotificationType.SECURITY_ALERT,
            NotificationChannel.EMAIL,
            "Security Alert",
            "Security alert for your account.",
            NotificationPriority.CRITICAL,
            variables,
            userId,
            "user-service"
        );
    }
    
    // Inventory Event Handlers
    private void handleLowStockAlert(JsonNode inventoryData) {
        String productId = inventoryData.get("productId").asText();
        String productName = inventoryData.get("productName").asText();
        int currentStock = inventoryData.get("currentStock").asInt();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("productName", productName);
        variables.put("currentStock", String.valueOf(currentStock));
        
        // Send low stock alert to admin (you might want to get admin emails from config)
        notificationService.sendNotificationAsync(
            "admin", "admin@ecommerce.com", null, null,
            NotificationType.LOW_STOCK_ALERT,
            NotificationChannel.EMAIL,
            "Low Stock Alert - " + productName,
            "Product is running low on stock.",
            NotificationPriority.HIGH,
            variables,
            productId,
            "inventory-service"
        );
    }
    
    private void handleOutOfStock(JsonNode inventoryData) {
        String productId = inventoryData.get("productId").asText();
        String productName = inventoryData.get("productName").asText();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("productName", productName);
        
        // Send out of stock alert to admin
        notificationService.sendNotificationAsync(
            "admin", "admin@ecommerce.com", null, null,
            NotificationType.OUT_OF_STOCK,
            NotificationChannel.EMAIL,
            "Out of Stock - " + productName,
            "Product is out of stock.",
            NotificationPriority.CRITICAL,
            variables,
            productId,
            "inventory-service"
        );
    }
    
    private void handleStockReplenished(JsonNode inventoryData) {
        String productId = inventoryData.get("productId").asText();
        String productName = inventoryData.get("productName").asText();
        int newStock = inventoryData.get("newStock").asInt();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("productName", productName);
        variables.put("newStock", String.valueOf(newStock));
        
        // Send stock replenished notification to admin
        notificationService.sendNotificationAsync(
            "admin", "admin@ecommerce.com", null, null,
            NotificationType.STOCK_REPLENISHED,
            NotificationChannel.EMAIL,
            "Stock Replenished - " + productName,
            "Product stock has been replenished.",
            NotificationPriority.MEDIUM,
            variables,
            productId,
            "inventory-service"
        );
    }
    
    // Promotion Event Handlers
    private void handlePromotionStarted(JsonNode promotionData) {
        String promotionId = promotionData.get("promotionId").asText();
        String title = promotionData.get("title").asText();
        String description = promotionData.get("description").asText();
        String promoCode = promotionData.get("promoCode").asText();
        int discount = promotionData.get("discount").asInt();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("offerTitle", title);
        variables.put("offerDescription", description);
        variables.put("promoCode", promoCode);
        variables.put("discount", String.valueOf(discount));
        variables.put("expiryDate", promotionData.get("expiryDate").asText());
        variables.put("shopLink", "https://ecommerce.com/shop");
        
        // Send promotional notification (this would typically be sent to a list of users)
        // For now, we'll send to a general promotional topic or admin
        notificationService.sendNotificationAsync(
            "promotion", "marketing@ecommerce.com", null, null,
            NotificationType.PROMOTIONAL,
            NotificationChannel.EMAIL,
            title,
            description,
            NotificationPriority.LOW,
            variables,
            promotionId,
            "promotion-service"
        );
    }
    
    private void handleDiscountOffer(JsonNode promotionData) {
        // Similar to promotion started but for specific discount offers
        handlePromotionStarted(promotionData);
    }
    
    private void handleFlashSale(JsonNode promotionData) {
        String saleId = promotionData.get("saleId").asText();
        String title = promotionData.get("title").asText();
        String description = promotionData.get("description").asText();
        int discount = promotionData.get("discount").asInt();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("offerTitle", title);
        variables.put("offerDescription", description);
        variables.put("discount", String.valueOf(discount));
        variables.put("saleEndTime", promotionData.get("endTime").asText());
        variables.put("shopLink", "https://ecommerce.com/flash-sale");
        
        // Send flash sale notification with high priority
        notificationService.sendNotificationAsync(
            "flash-sale", "marketing@ecommerce.com", null, null,
            NotificationType.FLASH_SALE,
            NotificationChannel.EMAIL,
            "Flash Sale: " + title,
            "Limited time flash sale!",
            NotificationPriority.HIGH,
            variables,
            saleId,
            "promotion-service"
        );
    }
}