package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.Notification;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.TopicManagementResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for sending push notifications using Firebase Cloud Messaging (FCM)
 */
@Service
public class PushNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    
    @Value("${notification.push.enabled:false}")
    private boolean pushEnabled;
    
    @Value("${notification.push.firebase.project-id:}")
    private String firebaseProjectId;
    
    @Value("${notification.push.default-icon:ic_notification}")
    private String defaultIcon;
    
    @Value("${notification.push.default-sound:default}")
    private String defaultSound;
    
    @Value("${notification.push.click-action:FLUTTER_NOTIFICATION_CLICK}")
    private String defaultClickAction;
    
    private FirebaseMessaging firebaseMessaging;
    
    @PostConstruct
    public void init() {
        if (pushEnabled) {
            try {
                if (FirebaseApp.getApps().isEmpty()) {
                    logger.warn("Firebase app not initialized. Push notifications will be disabled.");
                    pushEnabled = false;
                } else {
                    firebaseMessaging = FirebaseMessaging.getInstance();
                    logger.info("Firebase push notification service initialized successfully");
                }
            } catch (Exception e) {
                logger.error("Failed to initialize Firebase push notification service", e);
                pushEnabled = false;
            }
        } else {
            logger.warn("Push notification service is disabled");
        }
    }
    
    /**
     * Send push notification to a single device
     */
    public boolean sendPushNotification(Notification notification) {
        if (!pushEnabled || firebaseMessaging == null) {
            logger.warn("Push notifications are disabled or not properly configured");
            return false;
        }
        
        if (notification.getRecipientDeviceToken() == null || 
            notification.getRecipientDeviceToken().trim().isEmpty()) {
            logger.error("No device token provided for notification: {}", notification.getId());
            return false;
        }
        
        try {
            Message message = buildMessage(notification);
            String response = firebaseMessaging.send(message);
            
            logger.info("Push notification sent successfully for notification: {}, FCM response: {}", 
                       notification.getId(), response);
            return true;
            
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send push notification for notification: {}, Error: {}", 
                        notification.getId(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending push notification for notification: {}", 
                        notification.getId(), e);
            return false;
        }
    }
    
    /**
     * Send push notification asynchronously
     */
    public CompletableFuture<Boolean> sendPushNotificationAsync(Notification notification) {
        return CompletableFuture.supplyAsync(() -> sendPushNotification(notification));
    }
    
    /**
     * Send push notifications to multiple devices
     */
    public void sendMulticastNotification(List<String> deviceTokens, Notification notification) {
        if (!pushEnabled || firebaseMessaging == null) {
            logger.warn("Push notifications are disabled or not properly configured");
            return;
        }
        
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            logger.warn("No device tokens provided for multicast notification");
            return;
        }
        
        try {
            MulticastMessage message = buildMulticastMessage(deviceTokens, notification);
            BatchResponse response = firebaseMessaging.sendMulticast(message);
            
            logger.info("Multicast notification sent: {} successful, {} failed for notification: {}", 
                       response.getSuccessCount(), response.getFailureCount(), notification.getId());
            
            // Log failed tokens for cleanup
            if (response.getFailureCount() > 0) {
                List<String> failedTokens = getFailedTokens(deviceTokens, response);
                logger.warn("Failed device tokens for notification {}: {}", 
                           notification.getId(), failedTokens);
            }
            
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send multicast notification for notification: {}", 
                        notification.getId(), e);
        }
    }
    
    /**
     * Send topic-based notification
     */
    public boolean sendTopicNotification(String topic, Notification notification) {
        if (!pushEnabled || firebaseMessaging == null) {
            logger.warn("Push notifications are disabled or not properly configured");
            return false;
        }
        
        try {
            Message message = buildTopicMessage(topic, notification);
            String response = firebaseMessaging.send(message);
            
            logger.info("Topic notification sent successfully to topic '{}' for notification: {}, FCM response: {}", 
                       topic, notification.getId(), response);
            return true;
            
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send topic notification to '{}' for notification: {}", 
                        topic, notification.getId(), e);
            return false;
        }
    }
    
    /**
     * Subscribe device token to topic
     */
    public boolean subscribeToTopic(List<String> deviceTokens, String topic) {
        if (!pushEnabled || firebaseMessaging == null) {
            logger.warn("Push notifications are disabled or not properly configured");
            return false;
        }
        
        try {
            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(deviceTokens, topic);
            logger.info("Subscribed {} devices to topic '{}', {} successful, {} failed", 
                       deviceTokens.size(), topic, response.getSuccessCount(), response.getFailureCount());
            return response.getSuccessCount() > 0;
            
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to subscribe devices to topic '{}'", topic, e);
            return false;
        }
    }
    
    /**
     * Unsubscribe device token from topic
     */
    public boolean unsubscribeFromTopic(List<String> deviceTokens, String topic) {
        if (!pushEnabled || firebaseMessaging == null) {
            logger.warn("Push notifications are disabled or not properly configured");
            return false;
        }
        
        try {
            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(deviceTokens, topic);
            logger.info("Unsubscribed {} devices from topic '{}', {} successful, {} failed", 
                       deviceTokens.size(), topic, response.getSuccessCount(), response.getFailureCount());
            return response.getSuccessCount() > 0;
            
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to unsubscribe devices from topic '{}'", topic, e);
            return false;
        }
    }
    
    /**
     * Build FCM message for single device
     */
    private Message buildMessage(Notification notification) {
        Map<String, String> data = createDataPayload(notification);
        
        return Message.builder()
            .setToken(notification.getRecipientDeviceToken())
            .setNotification(createNotificationPayload(notification))
            .putAllData(data)
            .setAndroidConfig(createAndroidConfig(notification))
            .setApnsConfig(createApnsConfig(notification))
            .build();
    }
    
    /**
     * Build multicast message
     */
    private MulticastMessage buildMulticastMessage(List<String> deviceTokens, Notification notification) {
        Map<String, String> data = createDataPayload(notification);
        
        return MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setNotification(createNotificationPayload(notification))
            .putAllData(data)
            .setAndroidConfig(createAndroidConfig(notification))
            .setApnsConfig(createApnsConfig(notification))
            .build();
    }
    
    /**
     * Build topic message
     */
    private Message buildTopicMessage(String topic, Notification notification) {
        Map<String, String> data = createDataPayload(notification);
        
        return Message.builder()
            .setTopic(topic)
            .setNotification(createNotificationPayload(notification))
            .putAllData(data)
            .setAndroidConfig(createAndroidConfig(notification))
            .setApnsConfig(createApnsConfig(notification))
            .build();
    }
    
    /**
     * Create notification payload
     */
    private com.google.firebase.messaging.Notification createNotificationPayload(Notification notification) {
        return com.google.firebase.messaging.Notification.builder()
            .setTitle(notification.getSubject())
            .setBody(notification.getContent())
            .setImage(getImageUrl(notification))
            .build();
    }
    
    /**
     * Create data payload
     */
    private Map<String, String> createDataPayload(Notification notification) {
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", notification.getId().toString());
        data.put("type", notification.getType().name());
        data.put("priority", notification.getPriority().name());
        
        if (notification.getCorrelationId() != null) {
            data.put("correlationId", notification.getCorrelationId());
        }
        
        if (notification.getSourceService() != null) {
            data.put("sourceService", notification.getSourceService());
        }
        
        // Add custom data from template variables
        if (notification.getTemplateVariables() != null) {
            notification.getTemplateVariables().forEach((key, value) -> {
                if (value != null) {
                    data.put("custom_" + key, value.toString());
                }
            });
        }
        
        return data;
    }
    
    /**
     * Create Android-specific configuration
     */
    private AndroidConfig createAndroidConfig(Notification notification) {
        AndroidNotification androidNotification = AndroidNotification.builder()
            .setIcon(defaultIcon)
            .setSound(defaultSound)
            .setClickAction(defaultClickAction)
            .setChannelId(getChannelId(notification))
            .setPriority(getAndroidPriority(notification))
            .build();
        
        return AndroidConfig.builder()
            .setNotification(androidNotification)
            .setPriority(getAndroidMessagePriority(notification))
            .build();
    }
    
    /**
     * Create APNS (iOS) configuration
     */
    private ApnsConfig createApnsConfig(Notification notification) {
        Aps aps = Aps.builder()
            .setAlert(ApsAlert.builder()
                .setTitle(notification.getSubject())
                .setBody(notification.getContent())
                .build())
            .setSound(defaultSound)
            .setBadge(1)
            .build();
        
        return ApnsConfig.builder()
            .setAps(aps)
            .build();
    }
    
    /**
     * Get Android notification channel ID based on notification type
     */
    private String getChannelId(Notification notification) {
        switch (notification.getType()) {
            case ORDER_CONFIRMATION:
            case ORDER_SHIPPED:
            case ORDER_DELIVERED:
                return "order_updates";
            case PAYMENT_SUCCESS:
            case PAYMENT_FAILED:
                return "payment_updates";
            case PROMOTIONAL:
            case DISCOUNT_OFFER:
                return "promotions";
            case SECURITY_ALERT:
            case PASSWORD_RESET:
                return "security";
            default:
                return "general";
        }
    }
    
    /**
     * Get Android notification priority
     */
    private AndroidNotification.Priority getAndroidPriority(Notification notification) {
        switch (notification.getPriority()) {
            case CRITICAL:
                return AndroidNotification.Priority.MAX;
            case HIGH:
                return AndroidNotification.Priority.HIGH;
            case MEDIUM:
                return AndroidNotification.Priority.DEFAULT;
            case LOW:
                return AndroidNotification.Priority.LOW;
            default:
                return AndroidNotification.Priority.DEFAULT;
        }
    }
    
    /**
     * Get Android message priority
     */
    private AndroidConfig.Priority getAndroidMessagePriority(Notification notification) {
        switch (notification.getPriority()) {
            case CRITICAL:
            case HIGH:
                return AndroidConfig.Priority.HIGH;
            default:
                return AndroidConfig.Priority.NORMAL;
        }
    }
    
    /**
     * Get image URL from notification
     */
    private String getImageUrl(Notification notification) {
        if (notification.getTemplateVariables() != null) {
            Object imageUrl = notification.getTemplateVariables().get("imageUrl");
            return imageUrl != null ? imageUrl.toString() : null;
        }
        return null;
    }
    
    /**
     * Get failed tokens from batch response
     */
    private List<String> getFailedTokens(List<String> deviceTokens, BatchResponse response) {
        return response.getResponses().stream()
            .filter(sendResponse -> !sendResponse.isSuccessful())
            .map(sendResponse -> {
                int index = response.getResponses().indexOf(sendResponse);
                return deviceTokens.get(index);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Check if push notification service is available
     */
    public boolean isServiceAvailable() {
        return pushEnabled && firebaseMessaging != null;
    }
    
    /**
     * Validate device token format
     */
    public boolean isValidDeviceToken(String deviceToken) {
        return deviceToken != null && 
               !deviceToken.trim().isEmpty() && 
               deviceToken.length() > 50; // FCM tokens are typically much longer
    }
}
