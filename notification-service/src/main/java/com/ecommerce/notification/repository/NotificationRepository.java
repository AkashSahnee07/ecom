package com.ecommerce.notification.repository;

import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.entity.NotificationChannel;
import com.ecommerce.notification.entity.NotificationPriority;
import com.ecommerce.notification.entity.NotificationStatus;
import com.ecommerce.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Notification entity operations
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find notifications by recipient ID
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);
    
    /**
     * Find notifications by recipient ID and status
     */
    List<Notification> findByRecipientIdAndStatus(String recipientId, NotificationStatus status);
    
    /**
     * Find notifications by status
     */
    List<Notification> findByStatusOrderByPriorityDescCreatedAtAsc(NotificationStatus status);
    
    /**
     * Find notifications by status and priority
     */
    List<Notification> findByStatusAndPriorityOrderByCreatedAtAsc(NotificationStatus status, NotificationPriority priority);
    
    /**
     * Find notifications by channel and status
     */
    List<Notification> findByChannelAndStatusOrderByPriorityDescCreatedAtAsc(NotificationChannel channel, NotificationStatus status);
    
    /**
     * Find notifications by type
     */
    Page<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type, Pageable pageable);
    
    /**
     * Find notifications by correlation ID
     */
    List<Notification> findByCorrelationIdOrderByCreatedAtDesc(String correlationId);
    
    /**
     * Find scheduled notifications that are ready to be processed
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.scheduledAt <= :currentTime ORDER BY n.priority DESC, n.scheduledAt ASC")
    List<Notification> findScheduledNotifications(@Param("status") NotificationStatus status, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find notifications by status and scheduled time with pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.scheduledAt <= :currentTime ORDER BY n.priority DESC, n.scheduledAt ASC")
    Page<Notification> findByStatusAndScheduledAtBefore(@Param("status") NotificationStatus status, @Param("currentTime") LocalDateTime currentTime, Pageable pageable);
    
    /**
     * Count notifications by status and created after date
     */
    long countByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime createdAt);
    
    /**
     * Count notifications created after date
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);
    
    /**
     * Find notifications by status and created before date
     */
    List<Notification> findByStatusAndCreatedAtBefore(NotificationStatus status, LocalDateTime createdAt);
    
    /**
     * Find failed notifications that are eligible for retry
     * Uses status and retryCount to select retryable notifications, ordered by priority and failure time.
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetries ORDER BY n.priority DESC, n.failedAt ASC")
    Page<Notification> findFailedNotificationsForRetry(@Param("status") NotificationStatus status, Pageable pageable);
    
    /**
     * Find failed notifications that can be retried
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetries ORDER BY n.priority DESC, n.failedAt ASC")
    List<Notification> findRetryableNotifications(@Param("status") NotificationStatus status);
    
    /**
     * Find notifications by recipient and date range
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientAndDateRange(
            @Param("recipientId") String recipientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    /**
     * Count notifications by status
     */
    long countByStatus(NotificationStatus status);
    
    /**
     * Count notifications by recipient and status
     */
    long countByRecipientIdAndStatus(String recipientId, NotificationStatus status);
    
    /**
     * Count notifications by type and date range
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :type AND n.createdAt BETWEEN :startDate AND :endDate")
    long countByTypeAndDateRange(
            @Param("type") NotificationType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find notifications that have exceeded processing timeout
     */
    @Query("SELECT n FROM Notification n WHERE n.status IN :statuses AND n.updatedAt < :timeoutThreshold")
    List<Notification> findTimedOutNotifications(
            @Param("statuses") List<NotificationStatus> statuses,
            @Param("timeoutThreshold") LocalDateTime timeoutThreshold);
    
    /**
     * Update notification status
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.updatedAt = :updatedAt WHERE n.id = :id")
    int updateNotificationStatus(
            @Param("id") Long id,
            @Param("status") NotificationStatus status,
            @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Update notification status and error message
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.errorMessage = :errorMessage, n.failedAt = :failedAt, n.updatedAt = :updatedAt WHERE n.id = :id")
    int updateNotificationStatusWithError(
            @Param("id") Long id,
            @Param("status") NotificationStatus status,
            @Param("errorMessage") String errorMessage,
            @Param("failedAt") LocalDateTime failedAt,
            @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Mark notification as sent
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.sentAt = :sentAt, n.updatedAt = :updatedAt WHERE n.id = :id")
    int markNotificationAsSent(
            @Param("id") Long id,
            @Param("status") NotificationStatus status,
            @Param("sentAt") LocalDateTime sentAt,
            @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Mark notification as delivered
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.deliveredAt = :deliveredAt, n.updatedAt = :updatedAt WHERE n.id = :id")
    int markNotificationAsDelivered(
            @Param("id") Long id,
            @Param("status") NotificationStatus status,
            @Param("deliveredAt") LocalDateTime deliveredAt,
            @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Increment retry count
     */
    @Modifying
    @Query("UPDATE Notification n SET n.retryCount = n.retryCount + 1, n.updatedAt = :updatedAt WHERE n.id = :id")
    int incrementRetryCount(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Delete old notifications
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate AND n.status IN :finalStatuses")
    int deleteOldNotifications(
            @Param("cutoffDate") LocalDateTime cutoffDate,
            @Param("finalStatuses") List<NotificationStatus> finalStatuses);
    
    /**
     * Delete notifications by status and created date
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.status = :status AND n.createdAt < :cutoffDate")
    int deleteByStatusAndCreatedAtBefore(
            @Param("status") NotificationStatus status,
            @Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Delete notifications by status, created date, and retry count
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.status = :status AND n.createdAt < :cutoffDate AND n.retryCount >= :minRetryCount")
    int deleteByStatusAndCreatedAtBeforeAndRetryCountGreaterThanEqual(
            @Param("status") NotificationStatus status,
            @Param("cutoffDate") LocalDateTime cutoffDate,
            @Param("minRetryCount") int minRetryCount);
    
    /**
     * Find notifications for batch processing
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.priority IN :priorities ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findNotificationsForBatchProcessing(
            @Param("status") NotificationStatus status,
            @Param("priorities") List<NotificationPriority> priorities,
            Pageable pageable);
    
    /**
     * Find duplicate notifications (same recipient, type, and content within time window)
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.type = :type AND n.subject = :subject AND n.createdAt > :timeWindow AND n.status != :excludeStatus")
    List<Notification> findDuplicateNotifications(
            @Param("recipientId") String recipientId,
            @Param("type") NotificationType type,
            @Param("subject") String subject,
            @Param("timeWindow") LocalDateTime timeWindow,
            @Param("excludeStatus") NotificationStatus excludeStatus);
    
    /**
     * Get notification statistics by status
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate GROUP BY n.status")
    List<Object[]> getNotificationStatsByStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get notification statistics by channel
     */
    @Query("SELECT n.channel, COUNT(n) FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate GROUP BY n.channel")
    List<Object[]> getNotificationStatsByChannel(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get notification statistics by type
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate GROUP BY n.type")
    List<Object[]> getNotificationStatsByType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}