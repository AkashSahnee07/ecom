package com.ecommerce.review.service;

import com.ecommerce.review.entity.Review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending notifications related to review events.
 * This service publishes events to Kafka which can be consumed by the notification-service.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public NotificationService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    private static final String NOTIFICATION_TOPIC = "notification-events";
    
    /**
     * Send notification when a new review is created.
     */
    public void sendReviewCreatedNotification(Review review) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "REVIEW_CREATED");
            notificationData.put("reviewId", review.getId());
            notificationData.put("productId", review.getProductId());
            notificationData.put("userId", review.getUserId());
            notificationData.put("rating", review.getRating());
            notificationData.put("title", review.getTitle());
            notificationData.put("verifiedPurchase", review.getVerifiedPurchase());
            notificationData.put("timestamp", review.getCreatedDate());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "review.created", notificationData);
            log.debug("Review created notification sent for review: {}", review.getId());
            
        } catch (Exception e) {
            log.error("Failed to send review created notification for review: {}", review.getId(), e);
        }
    }
    
    /**
     * Send notification when a review is approved.
     */
    public void sendReviewApprovedNotification(Review review) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "REVIEW_APPROVED");
            notificationData.put("reviewId", review.getId());
            notificationData.put("productId", review.getProductId());
            notificationData.put("userId", review.getUserId());
            notificationData.put("rating", review.getRating());
            notificationData.put("title", review.getTitle());
            notificationData.put("moderatedBy", review.getModeratedBy());
            notificationData.put("timestamp", review.getModeratedAt());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "review.approved", notificationData);
            log.debug("Review approved notification sent for review: {}", review.getId());
            
        } catch (Exception e) {
            log.error("Failed to send review approved notification for review: {}", review.getId(), e);
        }
    }
    
    /**
     * Send notification when a review is rejected.
     */
    public void sendReviewRejectedNotification(Review review) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "REVIEW_REJECTED");
            notificationData.put("reviewId", review.getId());
            notificationData.put("productId", review.getProductId());
            notificationData.put("userId", review.getUserId());
            notificationData.put("title", review.getTitle());
            notificationData.put("moderatedBy", review.getModeratedBy());
            notificationData.put("moderationReason", review.getModerationNotes());
            notificationData.put("timestamp", review.getModeratedAt());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "review.rejected", notificationData);
            log.debug("Review rejected notification sent for review: {}", review.getId());
            
        } catch (Exception e) {
            log.error("Failed to send review rejected notification for review: {}", review.getId(), e);
        }
    }
    
    /**
     * Send notification when a review receives a helpful vote.
     */
    public void sendReviewHelpfulNotification(Review review, String voterUserId) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "REVIEW_HELPFUL_VOTE");
            notificationData.put("reviewId", review.getId());
            notificationData.put("productId", review.getProductId());
            notificationData.put("reviewAuthorId", review.getUserId());
            notificationData.put("voterUserId", voterUserId);
            notificationData.put("helpfulCount", review.getHelpfulCount());
            notificationData.put("timestamp", java.time.LocalDateTime.now());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "review.helpful.vote", notificationData);
            log.debug("Review helpful notification sent for review: {}", review.getId());
            
        } catch (Exception e) {
            log.error("Failed to send review helpful notification for review: {}", review.getId(), e);
        }
    }
    
    /**
     * Send notification when a review is replied to.
     */
    public void sendReviewReplyNotification(Review review, String replyContent, String replierUserId) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "REVIEW_REPLY");
            notificationData.put("reviewId", review.getId());
            notificationData.put("productId", review.getProductId());
            notificationData.put("reviewAuthorId", review.getUserId());
            notificationData.put("replierUserId", replierUserId);
            notificationData.put("replyContent", replyContent);
            notificationData.put("timestamp", java.time.LocalDateTime.now());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "review.reply", notificationData);
            log.debug("Review reply notification sent for review: {}", review.getId());
            
        } catch (Exception e) {
            log.error("Failed to send review reply notification for review: {}", review.getId(), e);
        }
    }
    
    /**
     * Send notification when a review is featured.
     */
    public void sendReviewFeaturedNotification(Review review) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "REVIEW_FEATURED");
            notificationData.put("reviewId", review.getId());
            notificationData.put("productId", review.getProductId());
            notificationData.put("userId", review.getUserId());
            notificationData.put("rating", review.getRating());
            notificationData.put("title", review.getTitle());
            notificationData.put("timestamp", java.time.LocalDateTime.now());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "review.featured", notificationData);
            log.debug("Review featured notification sent for review: {}", review.getId());
            
        } catch (Exception e) {
            log.error("Failed to send review featured notification for review: {}", review.getId(), e);
        }
    }
    
    /**
     * Send notification for review milestone (e.g., 100th review for a product).
     */
    public void sendReviewMilestoneNotification(String productId, long reviewCount) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "REVIEW_MILESTONE");
            notificationData.put("productId", productId);
            notificationData.put("reviewCount", reviewCount);
            notificationData.put("timestamp", java.time.LocalDateTime.now());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "review.milestone", notificationData);
            log.debug("Review milestone notification sent for product: {} (count: {})", productId, reviewCount);
            
        } catch (Exception e) {
            log.error("Failed to send review milestone notification for product: {}", productId, e);
        }
    }
    
    /**
     * Send notification when a product's average rating changes significantly.
     */
    public void sendRatingChangeNotification(String productId, double oldRating, double newRating, long totalReviews) {
        try {
            // Only send if the change is significant (more than 0.5 stars)
            if (Math.abs(newRating - oldRating) < 0.5) {
                return;
            }
            
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "PRODUCT_RATING_CHANGE");
            notificationData.put("productId", productId);
            notificationData.put("oldRating", oldRating);
            notificationData.put("newRating", newRating);
            notificationData.put("totalReviews", totalReviews);
            notificationData.put("timestamp", java.time.LocalDateTime.now());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "product.rating.change", notificationData);
            log.debug("Rating change notification sent for product: {} ({} -> {})", 
                    productId, oldRating, newRating);
            
        } catch (Exception e) {
            log.error("Failed to send rating change notification for product: {}", productId, e);
        }
    }
    
    /**
     * Send notification when spam is detected.
     */
    public void sendSpamDetectedNotification(Review review) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "SPAM_DETECTED");
            notificationData.put("reviewId", review.getId());
            notificationData.put("productId", review.getProductId());
            notificationData.put("userId", review.getUserId());
            notificationData.put("spamScore", review.getSpamScore());
            notificationData.put("content", review.getContent());
            notificationData.put("timestamp", review.getCreatedDate());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "spam.detected", notificationData);
            log.debug("Spam detected notification sent for review: {}", review.getId());
            
        } catch (Exception e) {
            log.error("Failed to send spam detected notification for review: {}", review.getId(), e);
        }
    }
    
    /**
     * Send notification for review analytics summary.
     */
    public void sendReviewAnalyticsSummary(String productId, Map<String, Object> analytics) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "REVIEW_ANALYTICS_SUMMARY");
            notificationData.put("productId", productId);
            notificationData.put("analytics", analytics);
            notificationData.put("timestamp", java.time.LocalDateTime.now());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "review.analytics.summary", notificationData);
            log.debug("Review analytics summary sent for product: {}", productId);
            
        } catch (Exception e) {
            log.error("Failed to send review analytics summary for product: {}", productId, e);
        }
    }
    
    /**
     * Send notification when a user receives a badge for reviewing.
     */
    public void sendReviewBadgeNotification(String userId, String badgeType, String badgeDescription) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "REVIEW_BADGE_EARNED");
            notificationData.put("userId", userId);
            notificationData.put("badgeType", badgeType);
            notificationData.put("badgeDescription", badgeDescription);
            notificationData.put("timestamp", java.time.LocalDateTime.now());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "review.badge.earned", notificationData);
            log.debug("Review badge notification sent for user: {} (badge: {})", userId, badgeType);
            
        } catch (Exception e) {
            log.error("Failed to send review badge notification for user: {}", userId, e);
        }
    }
    
    /**
     * Send notification for moderation queue alerts.
     */
    public void sendModerationQueueAlert(long pendingCount, long urgentCount) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "MODERATION_QUEUE_ALERT");
            notificationData.put("pendingCount", pendingCount);
            notificationData.put("urgentCount", urgentCount);
            notificationData.put("timestamp", java.time.LocalDateTime.now());
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, "moderation.queue.alert", notificationData);
            log.debug("Moderation queue alert sent (pending: {}, urgent: {})", pendingCount, urgentCount);
            
        } catch (Exception e) {
            log.error("Failed to send moderation queue alert", e);
        }
    }
}
