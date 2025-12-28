package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.*;
import com.ecommerce.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for managing notifications
 */
@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailNotificationService emailService;
    
    @Autowired
    private SmsNotificationService smsService;
    
    @Autowired
    private PushNotificationService pushService;
    
    @Autowired
    private TemplateService templateService;
    
    /**
     * Create and send a notification
     */
    public Notification createNotification(String recipientId, String recipientEmail, 
                                         NotificationType type, NotificationChannel channel, 
                                         String subject, String content) {
        return createNotification(recipientId, recipientEmail, null, type, channel, subject, content, null, null);
    }
    
    /**
     * Create notification with all parameters
     */
    public Notification createNotification(String recipientId, String recipientEmail, String recipientPhone,
                                         String recipientDeviceToken, NotificationType type, 
                                         NotificationChannel channel, String subject, String content,
                                         NotificationPriority priority, LocalDateTime scheduledTime,
                                         Map<String, Object> templateVariables, String correlationId,
                                         String sourceService) {
        
        logger.info("Creating notification for recipient: {}, type: {}, channel: {}", recipientId, type, channel);
        
        // Check for duplicate notifications
        if (isDuplicateNotification(recipientId, type, subject)) {
            logger.warn("Duplicate notification detected for recipient: {}, type: {}", recipientId, type);
            return null;
        }
        
        // Create notification entity
        Notification notification = new Notification(recipientId, recipientEmail, type, channel, subject, content);
        notification.setRecipientPhone(recipientPhone);
        notification.setRecipientDeviceToken(recipientDeviceToken);
        notification.setPriority(priority != null ? priority : NotificationPriority.fromNotificationType(type));
        notification.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID().toString());
        notification.setSourceService(sourceService);
        notification.setScheduledAt(scheduledTime);
        
        // Convert template variables
        if (templateVariables != null) {
            Map<String, String> convertedVariables = new HashMap<>();
            for (Map.Entry<String, Object> entry : templateVariables.entrySet()) {
                convertedVariables.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
            notification.setTemplateVariables(convertedVariables);
        }
        
        // Save notification
        notification = notificationRepository.save(notification);
        
        // Send immediately if not scheduled
        if (scheduledTime == null || scheduledTime.isBefore(LocalDateTime.now())) {
            sendNotification(notification);
        }
        
        return notification;
    }
    
    /**
     * Create and send a notification with template
     */
    public Notification createNotification(String recipientId, String recipientEmail, String recipientPhone,
                                         NotificationType type, NotificationChannel channel, 
                                         String subject, String content, String templateId, 
                                         Map<String, String> templateVariables) {
        
        logger.info("Creating notification for recipient: {}, type: {}, channel: {}", recipientId, type, channel);
        
        // Check for duplicate notifications
        if (isDuplicateNotification(recipientId, type, subject)) {
            logger.warn("Duplicate notification detected for recipient: {}, type: {}", recipientId, type);
            return null;
        }
        
        // Create notification entity
        Notification notification = new Notification(recipientId, recipientEmail, type, channel, subject, content);
        notification.setRecipientPhone(recipientPhone);
        notification.setTemplateId(templateId);
        notification.setTemplateVariables(templateVariables);
        notification.setPriority(NotificationPriority.fromNotificationType(type));
        notification.setCorrelationId(UUID.randomUUID().toString());
        
        // Process template if provided
        if (templateId != null && templateVariables != null) {
            try {
                String processedContent = templateService.processTemplate(templateId, (Map<String, Object>) (Map<?, ?>) templateVariables);
                notification.setContent(processedContent);
            } catch (Exception e) {
                logger.error("Failed to process template: {}", templateId, e);
                notification.setErrorMessage("Template processing failed: " + e.getMessage());
            }
        }
        
        // Save notification
        notification = notificationRepository.save(notification);
        
        // Send notification asynchronously
        sendNotificationAsync(notification);
        
        return notification;
    }
    
    /**
     * Schedule a notification for future delivery
     */
    public Notification scheduleNotification(String recipientId, String recipientEmail, String recipientPhone,
                                           NotificationType type, NotificationChannel channel, 
                                           String subject, String content, LocalDateTime scheduledAt) {
        
        logger.info("Scheduling notification for recipient: {}, scheduled at: {}", recipientId, scheduledAt);
        
        Notification notification = new Notification(recipientId, recipientEmail, type, channel, subject, content);
        notification.setRecipientPhone(recipientPhone);
        notification.setScheduledAt(scheduledAt);
        notification.setStatus(NotificationStatus.SCHEDULED);
        notification.setPriority(NotificationPriority.fromNotificationType(type));
        notification.setCorrelationId(UUID.randomUUID().toString());
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Send notification asynchronously
     */
    @Async
    public CompletableFuture<Void> sendNotificationAsync(Notification notification) {
        try {
            sendNotification(notification);
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", notification.getId(), e);
            markNotificationAsFailed(notification.getId(), e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Create and send notification asynchronously with all parameters
     */
    @Async
    public CompletableFuture<Void> sendNotificationAsync(String recipientId, String recipientEmail, String recipientPhone, 
                                                        String recipientDeviceToken, NotificationType type, 
                                                        NotificationChannel channel, String subject, String content,
                                                        NotificationPriority priority, Map<String, Object> templateVariables,
                                                        String correlationId, String sourceService) {
        
        // Convert template variables
        Map<String, String> convertedVariables = null;
        if (templateVariables != null) {
            convertedVariables = new HashMap<>();
            for (Map.Entry<String, Object> entry : templateVariables.entrySet()) {
                convertedVariables.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
        }
        try {
            Notification notification = new Notification(recipientId, recipientEmail, type, channel, subject, content);
            notification.setRecipientPhone(recipientPhone);
            notification.setRecipientDeviceToken(recipientDeviceToken);
            notification.setPriority(priority);
            notification.setTemplateVariables(convertedVariables);
            notification.setCorrelationId(correlationId);
            notification.setSourceService(sourceService);
            
            notification = notificationRepository.save(notification);
            sendNotification(notification);
        } catch (Exception e) {
            logger.error("Failed to send notification for recipient: {}", recipientId, e);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Send notification synchronously
     */
    public void sendNotification(Notification notification) {
        logger.info("Sending notification: {}, channel: {}", notification.getId(), notification.getChannel());
        
        try {
            // Update status to processing
            notification.setStatus(NotificationStatus.PROCESSING);
            notificationRepository.save(notification);
            
            // Send based on channel
            boolean sent = false;
            switch (notification.getChannel()) {
                case EMAIL:
                    sent = emailService.sendEmail(notification);
                    break;
                case SMS:
                    sent = smsService.sendSms(notification);
                    break;
                case PUSH:
                    sent = pushService.sendPushNotification(notification);
                    break;
                default:
                    logger.warn("Unsupported notification channel: {}", notification.getChannel());
                    throw new UnsupportedOperationException("Channel not supported: " + notification.getChannel());
            }
            
            if (sent) {
                notification.markAsSent();
                logger.info("Notification sent successfully: {}", notification.getId());
            } else {
                throw new RuntimeException("Failed to send notification through " + notification.getChannel());
            }
            
        } catch (Exception e) {
            logger.error("Error sending notification: {}", notification.getId(), e);
            notification.markAsFailed(e.getMessage());
            
            // Schedule retry if possible
            if (notification.canRetry()) {
                scheduleRetry(notification);
            }
        } finally {
            notificationRepository.save(notification);
        }
    }
    
    /**
     * Process scheduled notifications
     */
    @Transactional
    public void processScheduledNotifications() {
        logger.debug("Processing scheduled notifications");
        
        List<Notification> scheduledNotifications = notificationRepository
                .findScheduledNotifications(NotificationStatus.SCHEDULED, LocalDateTime.now());
        
        for (Notification notification : scheduledNotifications) {
            try {
                notification.setStatus(NotificationStatus.PENDING);
                notificationRepository.save(notification);
                sendNotificationAsync(notification);
            } catch (Exception e) {
                logger.error("Failed to process scheduled notification: {}", notification.getId(), e);
            }
        }
    }
    
    /**
     * Process retry notifications
     */
    @Transactional
    public void processRetryNotifications() {
        logger.debug("Processing retry notifications");
        
        List<Notification> retryNotifications = notificationRepository
                .findRetryableNotifications(NotificationStatus.FAILED);
        
        for (Notification notification : retryNotifications) {
            try {
                // Check if enough time has passed for retry
                LocalDateTime retryTime = notification.getFailedAt()
                        .plusMinutes(notification.getPriority().getRetryDelayMinutes());
                
                if (LocalDateTime.now().isAfter(retryTime)) {
                    notification.setStatus(NotificationStatus.RETRY);
                    notification.incrementRetryCount();
                    notificationRepository.save(notification);
                    sendNotificationAsync(notification);
                }
            } catch (Exception e) {
                logger.error("Failed to process retry notification: {}", notification.getId(), e);
            }
        }
    }
    
    /**
     * Get notifications for a recipient
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationHistory(String recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
    }
    
    public Page<Notification> getScheduledNotifications(Pageable pageable) {
        return notificationRepository.findByStatusAndScheduledAtBefore(
            NotificationStatus.SCHEDULED, 
            LocalDateTime.now().plusMinutes(30), 
            pageable
        );
    }
    
    public boolean markAsCancelled(UUID notificationId, String reason) {
        Optional<Notification> optionalNotification = notificationRepository.findById(Long.parseLong(notificationId.toString().replaceAll("-", "").substring(0, 10)));
        if (optionalNotification.isEmpty()) {
            logger.warn("Notification not found: {}", notificationId);
            return false;
        }
        
        Notification notification = optionalNotification.get();
        
        notification.setStatus(NotificationStatus.CANCELLED);
        notification.setErrorMessage(reason);
        notification.setUpdatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        logger.info("Notification {} marked as cancelled: {}", notificationId, reason);
        return true;
    }
    
    public boolean retryNotification(UUID notificationId) {
        // Convert UUID to Long ID (simplified approach)
        Long id = Long.parseLong(notificationId.toString().replaceAll("-", "").substring(0, 10));
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (optionalNotification.isEmpty()) {
            logger.warn("Notification not found for retry: {}", notificationId);
            return false;
        }
        
        Notification notification = optionalNotification.get();
        if (!notification.canRetry()) {
            logger.warn("Notification {} cannot be retried", notificationId);
            return false;
        }
        
        notification.incrementRetryCount();
        notification.setStatus(NotificationStatus.PENDING);
        notification.setUpdatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        // Send the notification asynchronously
        sendNotificationAsync(notification);
        
        logger.info("Notification {} queued for retry", notificationId);
        return true;
    }
    
    public boolean markAsDelivered(UUID notificationId, LocalDateTime deliveredAt) {
        Long id = Long.parseLong(notificationId.toString().replaceAll("-", "").substring(0, 10));
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (optionalNotification.isEmpty()) {
            logger.warn("Notification not found: {}", notificationId);
            return false;
        }
        
        Notification notification = optionalNotification.get();
        notification.markAsDelivered();
        notification.setDeliveredAt(deliveredAt);
        notification.setUpdatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        logger.info("Notification {} marked as delivered", notificationId);
        return true;
    }
    
    public boolean markAsFailed(UUID notificationId, String errorMessage) {
        Long id = Long.parseLong(notificationId.toString().replaceAll("-", "").substring(0, 10));
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (optionalNotification.isEmpty()) {
            logger.warn("Notification not found: {}", notificationId);
            return false;
        }
        
        Notification notification = optionalNotification.get();
        notification.markAsFailed(errorMessage);
        notification.setUpdatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        logger.info("Notification {} marked as failed: {}", notificationId, errorMessage);
        return true;
    }
    
    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }
    
    /**
     * Mark notification as delivered (webhook callback)
     */
    @Transactional
    public void markNotificationAsDelivered(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.markAsDelivered();
            notificationRepository.save(notification);
            logger.info("Notification marked as delivered: {}", notificationId);
        }
    }
    
    /**
     * Mark notification as failed
     */
    @Transactional
    public void markNotificationAsFailed(Long notificationId, String errorMessage) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.markAsFailed(errorMessage);
            notificationRepository.save(notification);
            logger.warn("Notification marked as failed: {}, error: {}", notificationId, errorMessage);
        }
    }
    
    /**
     * Cancel notification
     */
    @Transactional
    public boolean cancelNotification(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            if (notification.getStatus().isActive()) {
                notification.setStatus(NotificationStatus.CANCELLED);
                notification.setUpdatedAt(LocalDateTime.now());
                notificationRepository.save(notification);
                logger.info("Notification cancelled: {}", notificationId);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get notification statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getNotificationStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        // Status statistics
        List<Object[]> statusStats = notificationRepository.getNotificationStatsByStatus(startDate, endDate);
        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] stat : statusStats) {
            statusCounts.put(((NotificationStatus) stat[0]).name(), (Long) stat[1]);
        }
        stats.put("statusStats", statusCounts);
        
        // Channel statistics
        List<Object[]> channelStats = notificationRepository.getNotificationStatsByChannel(startDate, endDate);
        Map<String, Long> channelCounts = new HashMap<>();
        for (Object[] stat : channelStats) {
            channelCounts.put(((NotificationChannel) stat[0]).name(), (Long) stat[1]);
        }
        stats.put("channelStats", channelCounts);
        
        // Type statistics
        List<Object[]> typeStats = notificationRepository.getNotificationStatsByType(startDate, endDate);
        Map<String, Long> typeCounts = new HashMap<>();
        for (Object[] stat : typeStats) {
            typeCounts.put(((NotificationType) stat[0]).name(), (Long) stat[1]);
        }
        stats.put("typeStats", typeCounts);
        
        return stats;
    }
    
    /**
     * Clean up old notifications
     */
    @Transactional
    public int cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<NotificationStatus> finalStatuses = Arrays.asList(
                NotificationStatus.DELIVERED, 
                NotificationStatus.CANCELLED, 
                NotificationStatus.EXPIRED
        );
        
        int deletedCount = notificationRepository.deleteOldNotifications(cutoffDate, finalStatuses);
        logger.info("Cleaned up {} old notifications older than {} days", deletedCount, daysToKeep);
        return deletedCount;
    }
    
    /**
     * Process timed out notifications
     */
    @Transactional
    public void processTimedOutNotifications() {
        List<NotificationStatus> activeStatuses = Arrays.asList(
                NotificationStatus.PENDING, 
                NotificationStatus.PROCESSING, 
                NotificationStatus.RETRY
        );
        
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30); // 30 minutes timeout
        List<Notification> timedOutNotifications = notificationRepository
                .findTimedOutNotifications(activeStatuses, timeoutThreshold);
        
        for (Notification notification : timedOutNotifications) {
            notification.markAsFailed("Processing timeout");
            notificationRepository.save(notification);
            logger.warn("Notification timed out: {}", notification.getId());
        }
    }
    
    /**
     * Process a single notification
     */
    @Transactional
    public void processNotification(Notification notification) {
        try {
            logger.debug("Processing notification: {}", notification.getId());
            
            // Update status to processing
            notification.setStatus(NotificationStatus.PROCESSING);
            notification.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            // Send the notification asynchronously
            sendNotificationAsync(notification);
            
        } catch (Exception e) {
            logger.error("Error processing notification: {}", notification.getId(), e);
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
            throw e;
        }
    }
    
    // Private helper methods
    
    private boolean isDuplicateNotification(String recipientId, NotificationType type, String subject) {
        LocalDateTime timeWindow = LocalDateTime.now().minusMinutes(5); // 5 minutes window
        List<Notification> duplicates = notificationRepository.findDuplicateNotifications(
                recipientId, type, subject, timeWindow, NotificationStatus.CANCELLED
        );
        return !duplicates.isEmpty();
    }
    
    private void scheduleRetry(Notification notification) {
        int delayMinutes = notification.getPriority().getRetryDelayMinutes();
        LocalDateTime retryTime = LocalDateTime.now().plusMinutes(delayMinutes);
        notification.setScheduledAt(retryTime);
        notification.setStatus(NotificationStatus.SCHEDULED);
        logger.info("Scheduled retry for notification: {} at {}", notification.getId(), retryTime);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByType(NotificationType type, Pageable pageable) {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByCorrelationId(String correlationId) {
        return notificationRepository.findByCorrelationIdOrderByCreatedAtDesc(correlationId);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByRecipient(String recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {
        List<Notification> notifications = notificationRepository.findByStatusOrderByPriorityDescCreatedAtAsc(status);
        return new org.springframework.data.domain.PageImpl<>(notifications, pageable, notifications.size());
    }

    @Transactional
    public Notification sendNotificationSync(String recipientId, String recipientEmail, String recipientPhone, 
                                           String recipientDeviceToken, NotificationType type, 
                                           NotificationChannel channel, String subject, String content,
                                           NotificationPriority priority, Map<String, Object> templateVariables,
                                           String correlationId, String sourceService) {
        
        // Convert Map<String, Object> to Map<String, String> for template processing
        Map<String, String> stringTemplateVariables = new HashMap<>();
        if (templateVariables != null) {
            templateVariables.forEach((key, value) -> 
                stringTemplateVariables.put(key, value != null ? value.toString() : null)
            );
        }
        
        Notification notification = createNotification(
            recipientId, recipientEmail, recipientPhone, type, channel, 
            subject, content, null, stringTemplateVariables
        );
        
        if (notification != null) {
            notification.setPriority(priority);
            notification.setCorrelationId(correlationId);
            notification.setSourceService(sourceService);
            if (recipientDeviceToken != null) {
                notification.setRecipientDeviceToken(recipientDeviceToken);
            }
            notification = notificationRepository.save(notification);
            
            // Send immediately
            sendNotification(notification);
        }
        
        return notification;
    }
}