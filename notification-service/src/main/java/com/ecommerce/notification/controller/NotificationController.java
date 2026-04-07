package com.ecommerce.notification.controller;

import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.entity.NotificationChannel;
import com.ecommerce.notification.entity.NotificationPriority;
import com.ecommerce.notification.entity.NotificationStatus;
import com.ecommerce.notification.entity.NotificationType;
import com.ecommerce.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for notification management
 */
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notification Management", description = "APIs for managing notifications")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Create and send a new notification
     */
    @PostMapping
    @Operation(summary = "Create Notification", description = "Creates and schedules a new notification")
    public ResponseEntity<?> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        try {
            logger.info("Creating notification for recipient: {}, type: {}", 
                       request.getRecipientId(), request.getType());
            
            Notification notification = notificationService.createNotification(
                request.getRecipientId(),
                request.getRecipientEmail(),
                request.getRecipientPhone(),
                request.getRecipientDeviceToken(),
                request.getType(),
                request.getChannel(),
                request.getSubject(),
                request.getContent(),
                request.getPriority(),
                request.getScheduledTime(),
                request.getTemplateVariables(),
                request.getCorrelationId(),
                request.getSourceService()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
            
        } catch (Exception e) {
            logger.error("Failed to create notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create notification: " + e.getMessage()));
        }
    }
    
    /**
     * Send notification immediately
     */
    @PostMapping("/send")
    @Operation(summary = "Send Notification Immediately", description = "Sends a notification immediately without scheduling")
    public ResponseEntity<?> sendNotification(@Valid @RequestBody CreateNotificationRequest request) {
        try {
            logger.info("Sending immediate notification for recipient: {}, type: {}", 
                       request.getRecipientId(), request.getType());
            
            Notification notification = notificationService.sendNotificationSync(
                request.getRecipientId(),
                request.getRecipientEmail(),
                request.getRecipientPhone(),
                request.getRecipientDeviceToken(),
                request.getType(),
                request.getChannel(),
                request.getSubject(),
                request.getContent(),
                request.getPriority(),
                request.getTemplateVariables(),
                request.getCorrelationId(),
                request.getSourceService()
            );
            
            if (notification != null) {
                return ResponseEntity.ok(notification);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send notification"));
            }
            
        } catch (Exception e) {
            logger.error("Failed to send notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to send notification: " + e.getMessage()));
        }
    }
    
    /**
     * Send notification asynchronously
     */
    @PostMapping("/send-async")
    @Operation(summary = "Send Notification Asynchronously", description = "Queues a notification for sending asynchronously")
    public ResponseEntity<?> sendNotificationAsync(@Valid @RequestBody CreateNotificationRequest request) {
        try {
            logger.info("Sending async notification for recipient: {}, type: {}", 
                       request.getRecipientId(), request.getType());
            
            notificationService.sendNotificationAsync(
                request.getRecipientId(),
                request.getRecipientEmail(),
                request.getRecipientPhone(),
                request.getRecipientDeviceToken(),
                request.getType(),
                request.getChannel(),
                request.getSubject(),
                request.getContent(),
                request.getPriority(),
                request.getTemplateVariables(),
                request.getCorrelationId(),
                request.getSourceService()
            );
            
            return ResponseEntity.accepted().body(Map.of("message", "Notification queued for sending"));
            
        } catch (Exception e) {
            logger.error("Failed to queue notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to queue notification: " + e.getMessage()));
        }
    }
    
    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getNotification(@PathVariable UUID id) {
        try {
            Optional<Notification> notificationOpt = notificationService.getNotificationById(Long.valueOf(id.toString().hashCode()));
            if (notificationOpt.isPresent()) {
                return ResponseEntity.ok(notificationOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to get notification: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get notification: " + e.getMessage()));
        }
    }
    
    /**
     * Get notifications for a recipient
     */
    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "Get Notifications by Recipient", description = "Retrieves notifications for a specific recipient")
    public ResponseEntity<?> getNotificationsByRecipient(
            @PathVariable String recipientId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Notification> notifications = notificationService.getNotificationsByRecipient(recipientId, pageable);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            logger.error("Failed to get notifications for recipient: {}", recipientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get notifications: " + e.getMessage()));
        }
    }
    
    /**
     * Get notifications by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getNotificationsByStatus(
            @PathVariable NotificationStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Notification> notifications = notificationService.getNotificationsByStatus(status, pageable);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            logger.error("Failed to get notifications by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get notifications: " + e.getMessage()));
        }
    }
    
    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get Notifications by Type", description = "Retrieves notifications filtered by type")
    public ResponseEntity<?> getNotificationsByType(
            @PathVariable NotificationType type,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Notification> notifications = notificationService.getNotificationsByType(type, pageable);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            logger.error("Failed to get notifications by type: {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get notifications: " + e.getMessage()));
        }
    }
    
    /**
     * Get notifications by correlation ID
     */
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<?> getNotificationsByCorrelationId(@PathVariable String correlationId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByCorrelationId(correlationId);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            logger.error("Failed to get notifications by correlation ID: {}", correlationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get notifications: " + e.getMessage()));
        }
    }
    
    /**
     * Update notification status
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update Notification Status", description = "Updates the status of an existing notification")
    public ResponseEntity<?> updateNotificationStatus(
            @PathVariable UUID id,
            @RequestBody UpdateStatusRequest request) {
        
        try {
            boolean updated = false;
            
            switch (request.getStatus()) {
                case DELIVERED:
                    updated = notificationService.markAsDelivered(id, request.getDeliveryTime());
                    break;
                case FAILED:
                    updated = notificationService.markAsFailed(id, request.getErrorMessage());
                    break;
                case CANCELLED:
                    updated = notificationService.markAsCancelled(id, request.getReason());
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status update: " + request.getStatus()));
            }
            
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Failed to update notification status: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update status: " + e.getMessage()));
        }
    }
    
    /**
     * Retry failed notification
     */
    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry Notification", description = "Retries sending a failed notification")
    public ResponseEntity<?> retryNotification(@PathVariable UUID id) {
        try {
            boolean retried = notificationService.retryNotification(id);
            
            if (retried) {
                return ResponseEntity.ok(Map.of("message", "Notification retry initiated"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot retry notification"));
            }
            
        } catch (Exception e) {
            logger.error("Failed to retry notification: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retry notification: " + e.getMessage()));
        }
    }
    
    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get Notification Statistics", description = "Retrieves statistics about notifications within a date range")
    public ResponseEntity<?> getNotificationStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            Map<String, Object> stats = notificationService.getNotificationStatistics(startDate, endDate);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Failed to get notification statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get scheduled notifications
     */
    @GetMapping("/scheduled")
    @Operation(summary = "Get Scheduled Notifications", description = "Retrieves a list of notifications scheduled for future delivery")
    public ResponseEntity<?> getScheduledNotifications(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("scheduledTime").ascending());
            Page<Notification> notifications = notificationService.getScheduledNotifications(pageable);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            logger.error("Failed to get scheduled notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get scheduled notifications: " + e.getMessage()));
        }
    }
    
    /**
     * Cancel scheduled notification
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancelNotification(@PathVariable UUID id, @RequestParam(name = "reason") String reason) {
        try {
            boolean cancelled = notificationService.markAsCancelled(id, reason);
            
            if (cancelled) {
                return ResponseEntity.ok(Map.of("message", "Notification cancelled successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Failed to cancel notification: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to cancel notification: " + e.getMessage()));
        }
    }
    
    /**
     * Cleanup old notifications
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "Cleanup Old Notifications", description = "Removes notifications older than the specified number of days")
    public ResponseEntity<?> cleanupOldNotifications(@RequestParam(name = "daysOld", defaultValue = "30") int daysOld) {
        try {
            int deletedCount = notificationService.cleanupOldNotifications(daysOld);
            return ResponseEntity.ok(Map.of(
                "message", "Cleanup completed",
                "deletedCount", deletedCount
            ));
            
        } catch (Exception e) {
            logger.error("Failed to cleanup old notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to cleanup notifications: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "notification-service",
            "timestamp", LocalDateTime.now()
        ));
    }
    
    // Request DTOs
    public static class CreateNotificationRequest {
        private String recipientId;
        private String recipientEmail;
        private String recipientPhone;
        private String recipientDeviceToken;
        private NotificationType type;
        private NotificationChannel channel;
        private String subject;
        private String content;
        private NotificationPriority priority = NotificationPriority.MEDIUM;
        private LocalDateTime scheduledTime;
        private Map<String, Object> templateVariables;
        private String correlationId;
        private String sourceService;
        
        // Getters and setters
        public String getRecipientId() { return recipientId; }
        public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
        
        public String getRecipientEmail() { return recipientEmail; }
        public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
        
        public String getRecipientPhone() { return recipientPhone; }
        public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
        
        public String getRecipientDeviceToken() { return recipientDeviceToken; }
        public void setRecipientDeviceToken(String recipientDeviceToken) { this.recipientDeviceToken = recipientDeviceToken; }
        
        public NotificationType getType() { return type; }
        public void setType(NotificationType type) { this.type = type; }
        
        public NotificationChannel getChannel() { return channel; }
        public void setChannel(NotificationChannel channel) { this.channel = channel; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public NotificationPriority getPriority() { return priority; }
        public void setPriority(NotificationPriority priority) { this.priority = priority; }
        
        public LocalDateTime getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
        
        public Map<String, Object> getTemplateVariables() { return templateVariables; }
        public void setTemplateVariables(Map<String, Object> templateVariables) { this.templateVariables = templateVariables; }
        
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        
        public String getSourceService() { return sourceService; }
        public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    }
    
    public static class UpdateStatusRequest {
        private NotificationStatus status;
        private LocalDateTime deliveryTime;
        private String errorMessage;
        private String reason;
        
        // Getters and setters
        public NotificationStatus getStatus() { return status; }
        public void setStatus(NotificationStatus status) { this.status = status; }
        
        public LocalDateTime getDeliveryTime() { return deliveryTime; }
        public void setDeliveryTime(LocalDateTime deliveryTime) { this.deliveryTime = deliveryTime; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
