package com.ecommerce.notification.scheduler;

import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.entity.NotificationStatus;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks for notification processing
 * Handles retry processing, timeout handling, and cleanup operations
 */
@Component
@ConditionalOnProperty(name = "notification.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Value("${notification.scheduler.retry-processing.batch-size:100}")
    private int retryBatchSize;
    
    @Value("${notification.scheduler.timeout-processing.timeout-minutes:30}")
    private int timeoutMinutes;
    
    @Value("${notification.scheduler.cleanup.retention-days:30}")
    private int retentionDays;
    
    /**
     * Process failed notifications for retry
     * Runs every 5 minutes by default
     */
    @Scheduled(cron = "${notification.scheduler.retry-processing.cron:0 */5 * * * *}")
    @ConditionalOnProperty(name = "notification.scheduler.retry-processing.enabled", havingValue = "true", matchIfMissing = true)
    @Transactional
    public void processRetryNotifications() {
        try {
            logger.info("Starting retry notification processing...");
            
            Pageable pageable = PageRequest.of(0, retryBatchSize);
            
            // Find failed notifications that are eligible for retry
            List<Notification> failedNotifications = notificationRepository
                .findFailedNotificationsForRetry(NotificationStatus.FAILED, pageable)
                .getContent();
            
            if (failedNotifications.isEmpty()) {
                logger.debug("No failed notifications found for retry processing");
                return;
            }
            
            logger.info("Found {} failed notifications for retry processing", failedNotifications.size());
            
            int processedCount = 0;
            int successCount = 0;
            int failedCount = 0;
            
            for (Notification notification : failedNotifications) {
                try {
                    logger.debug("Processing retry for notification ID: {}", notification.getId());
                    
                    // Check if notification is still eligible for retry
                    if (notification.canRetry()) {
                        // Process the notification
                        notificationService.processNotification(notification);
                        successCount++;
                    } else {
                        // Mark as permanently failed if max retries exceeded
                        notification.setStatus(NotificationStatus.FAILED);
                        notification.setErrorMessage("Maximum retry attempts exceeded");
                        notification.setUpdatedAt(LocalDateTime.now());
                        notificationRepository.save(notification);
                        failedCount++;
                        
                        logger.warn("Notification ID {} marked as permanently failed after {} retries", 
                                  notification.getId(), notification.getRetryCount());
                    }
                    
                    processedCount++;
                    
                } catch (Exception e) {
                    logger.error("Failed to process retry for notification ID: {}", 
                               notification.getId(), e);
                    failedCount++;
                }
            }
            
            logger.info("Retry processing completed. Processed: {}, Success: {}, Failed: {}", 
                       processedCount, successCount, failedCount);
            
        } catch (Exception e) {
            logger.error("Error during retry notification processing", e);
        }
    }
    
    /**
     * Process notifications that have timed out
     * Runs every 10 minutes by default
     */
    @Scheduled(cron = "${notification.scheduler.timeout-processing.cron:0 */10 * * * *}")
    @ConditionalOnProperty(name = "notification.scheduler.timeout-processing.enabled", havingValue = "true", matchIfMissing = true)
    @Transactional
    public void processTimeoutNotifications() {
        try {
            logger.info("Starting timeout notification processing...");
            
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
            
            // Find notifications that are stuck in SENDING status for too long
            List<Notification> timedOutNotifications = notificationRepository
                .findByStatusAndCreatedAtBefore(NotificationStatus.SENDING, timeoutThreshold);
            
            if (timedOutNotifications.isEmpty()) {
                logger.debug("No timed out notifications found");
                return;
            }
            
            logger.info("Found {} timed out notifications", timedOutNotifications.size());
            
            int processedCount = 0;
            
            for (Notification notification : timedOutNotifications) {
                try {
                    logger.debug("Processing timeout for notification ID: {}", notification.getId());
                    
                    if (notification.canRetry()) {
                        // Mark for retry
                        notification.setStatus(NotificationStatus.FAILED);
                        notification.setErrorMessage("Notification timed out after " + timeoutMinutes + " minutes");
                        notification.incrementRetryCount();
                        notification.calculateNextRetryTime();
                        notification.setUpdatedAt(LocalDateTime.now());
                        
                        logger.info("Notification ID {} marked for retry due to timeout", notification.getId());
                    } else {
                        // Mark as permanently failed
                        notification.setStatus(NotificationStatus.FAILED);
                        notification.setErrorMessage("Notification timed out and max retries exceeded");
                        notification.setUpdatedAt(LocalDateTime.now());
                        
                        logger.warn("Notification ID {} marked as permanently failed due to timeout", 
                                  notification.getId());
                    }
                    
                    notificationRepository.save(notification);
                    processedCount++;
                    
                } catch (Exception e) {
                    logger.error("Failed to process timeout for notification ID: {}", 
                               notification.getId(), e);
                }
            }
            
            logger.info("Timeout processing completed. Processed: {} notifications", processedCount);
            
        } catch (Exception e) {
            logger.error("Error during timeout notification processing", e);
        }
    }
    
    /**
     * Clean up old notifications
     * Runs daily at 2 AM by default
     */
    @Scheduled(cron = "${notification.scheduler.cleanup.cron:0 0 2 * * *}")
    @ConditionalOnProperty(name = "notification.scheduler.cleanup.enabled", havingValue = "true", matchIfMissing = true)
    @Transactional
    public void cleanupOldNotifications() {
        try {
            logger.info("Starting cleanup of old notifications...");
            
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            
            // Delete old delivered notifications
            int deliveredDeleted = notificationRepository
                .deleteByStatusAndCreatedAtBefore(NotificationStatus.DELIVERED, cutoffDate);
            
            // Delete old failed notifications (permanently failed)
            int failedDeleted = notificationRepository
                .deleteByStatusAndCreatedAtBeforeAndRetryCountGreaterThanEqual(
                    NotificationStatus.FAILED, cutoffDate, 3);
            
            // Delete old cancelled notifications
            int cancelledDeleted = notificationRepository
                .deleteByStatusAndCreatedAtBefore(NotificationStatus.CANCELLED, cutoffDate);
            
            int totalDeleted = deliveredDeleted + failedDeleted + cancelledDeleted;
            
            logger.info("Cleanup completed. Deleted {} notifications (Delivered: {}, Failed: {}, Cancelled: {})", 
                       totalDeleted, deliveredDeleted, failedDeleted, cancelledDeleted);
            
            // Log statistics after cleanup
            logNotificationStatistics();
            
        } catch (Exception e) {
            logger.error("Error during notification cleanup", e);
        }
    }
    
    /**
     * Process scheduled notifications
     * Runs every minute to check for notifications that should be sent
     */
    @Scheduled(cron = "0 * * * * *") // Every minute
    @ConditionalOnProperty(name = "notification.scheduler.scheduled-processing.enabled", havingValue = "true", matchIfMissing = true)
    @Transactional
    public void processScheduledNotifications() {
        try {
            logger.debug("Checking for scheduled notifications...");
            
            LocalDateTime now = LocalDateTime.now();
            Pageable pageable = PageRequest.of(0, 50); // Process up to 50 at a time
            
            // Find scheduled notifications that are ready to be sent
            List<Notification> scheduledNotifications = notificationRepository
                .findByStatusAndScheduledAtBefore(NotificationStatus.SCHEDULED, now, pageable).getContent();
            
            if (scheduledNotifications.isEmpty()) {
                logger.debug("No scheduled notifications ready for processing");
                return;
            }
            
            logger.info("Found {} scheduled notifications ready for processing", scheduledNotifications.size());
            
            int processedCount = 0;
            int successCount = 0;
            int failedCount = 0;
            
            for (Notification notification : scheduledNotifications) {
                try {
                    logger.debug("Processing scheduled notification ID: {}", notification.getId());
                    
                    // Update status to PENDING and process
                    notification.setStatus(NotificationStatus.PENDING);
                    notification.setUpdatedAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    
                    // Process the notification
                    notificationService.processNotification(notification);
                    successCount++;
                    processedCount++;
                    
                } catch (Exception e) {
                    logger.error("Failed to process scheduled notification ID: {}", 
                               notification.getId(), e);
                    failedCount++;
                    processedCount++;
                }
            }
            
            logger.info("Scheduled notification processing completed. Processed: {}, Success: {}, Failed: {}", 
                       processedCount, successCount, failedCount);
            
        } catch (Exception e) {
            logger.error("Error during scheduled notification processing", e);
        }
    }
    
    /**
     * Generate and log notification statistics
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @ConditionalOnProperty(name = "notification.scheduler.statistics.enabled", havingValue = "true", matchIfMissing = true)
    public void logNotificationStatistics() {
        try {
            logger.info("Generating notification statistics...");
            
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
            
            // Get statistics for last 24 hours
            long totalLast24h = notificationRepository.countByCreatedAtAfter(last24Hours);
            long deliveredLast24h = notificationRepository.countByStatusAndCreatedAtAfter(
                NotificationStatus.DELIVERED, last24Hours);
            long failedLast24h = notificationRepository.countByStatusAndCreatedAtAfter(
                NotificationStatus.FAILED, last24Hours);
            long pendingLast24h = notificationRepository.countByStatusAndCreatedAtAfter(
                NotificationStatus.PENDING, last24Hours);
            
            // Get statistics for last hour
            long totalLastHour = notificationRepository.countByCreatedAtAfter(lastHour);
            long deliveredLastHour = notificationRepository.countByStatusAndCreatedAtAfter(
                NotificationStatus.DELIVERED, lastHour);
            long failedLastHour = notificationRepository.countByStatusAndCreatedAtAfter(
                NotificationStatus.FAILED, lastHour);
            
            // Calculate success rates
            double successRate24h = totalLast24h > 0 ? (double) deliveredLast24h / totalLast24h * 100 : 0;
            double successRateHour = totalLastHour > 0 ? (double) deliveredLastHour / totalLastHour * 100 : 0;
            
            logger.info("=== Notification Statistics ===");
            logger.info("Last 24 hours - Total: {}, Delivered: {}, Failed: {}, Pending: {}, Success Rate: {:.2f}%", 
                       totalLast24h, deliveredLast24h, failedLast24h, pendingLast24h, successRate24h);
            logger.info("Last hour - Total: {}, Delivered: {}, Failed: {}, Success Rate: {:.2f}%", 
                       totalLastHour, deliveredLastHour, failedLastHour, successRateHour);
            
            // Get current queue sizes
            long currentPending = notificationRepository.countByStatus(NotificationStatus.PENDING);
            long currentSending = notificationRepository.countByStatus(NotificationStatus.SENDING);
            long currentScheduled = notificationRepository.countByStatus(NotificationStatus.SCHEDULED);
            
            logger.info("Current queues - Pending: {}, Sending: {}, Scheduled: {}", 
                       currentPending, currentSending, currentScheduled);
            logger.info("===============================");
            
        } catch (Exception e) {
            logger.error("Error generating notification statistics", e);
        }
    }
}