package com.ecommerce.review.controller;

import com.ecommerce.review.entity.Review;
import com.ecommerce.review.service.ModerationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for review moderation operations.
 * This controller is intended for use by administrators and moderators.
 */
@RestController
@RequestMapping("/moderation")
@Tag(name = "Review Moderation", description = "APIs for moderating reviews")
public class ModerationController {

    private static final Logger log = LoggerFactory.getLogger(ModerationController.class);
    
    private final ModerationService moderationService;
    
    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }
    
    /**
     * Manually approve a review.
     */
    @PostMapping("/{reviewId}/approve")
    @Operation(summary = "Approve review", description = "Manually approve a review")
    public ResponseEntity<Void> approveReview(
            @Parameter(description = "Review ID") @PathVariable String reviewId,
            @Parameter(description = "Moderator ID") @RequestParam String moderatorId) {
        
        try {
            moderationService.approveReview(reviewId, moderatorId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for approving review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error approving review: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Manually reject a review.
     */
    @PostMapping("/{reviewId}/reject")
    @Operation(summary = "Reject review", description = "Manually reject a review")
    public ResponseEntity<Void> rejectReview(
            @Parameter(description = "Review ID") @PathVariable String reviewId,
            @Parameter(description = "Moderator ID") @RequestParam String moderatorId,
            @Parameter(description = "Rejection reason") @RequestParam String reason) {
        
        try {
            moderationService.rejectReview(reviewId, moderatorId, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for rejecting review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error rejecting review: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mark a review as spam.
     */
    @PostMapping("/{reviewId}/spam")
    @Operation(summary = "Mark as spam", description = "Mark a review as spam")
    public ResponseEntity<Void> markAsSpam(
            @Parameter(description = "Review ID") @PathVariable String reviewId,
            @Parameter(description = "Moderator ID") @RequestParam String moderatorId) {
        
        try {
            moderationService.markAsSpam(reviewId, moderatorId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for marking review {} as spam: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error marking review as spam: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Hide a review.
     */
    @PostMapping("/{reviewId}/hide")
    @Operation(summary = "Hide review", description = "Hide a review from public view")
    public ResponseEntity<Void> hideReview(
            @Parameter(description = "Review ID") @PathVariable String reviewId,
            @Parameter(description = "Moderator ID") @RequestParam String moderatorId,
            @Parameter(description = "Reason for hiding") @RequestParam String reason) {
        
        try {
            moderationService.hideReview(reviewId, moderatorId, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for hiding review {}: {}", reviewId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error hiding review: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get reviews pending moderation.
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending reviews", description = "Get list of reviews pending moderation")
    public ResponseEntity<List<Review>> getReviewsPendingModeration(
            @Parameter(description = "Limit number of results") @RequestParam(defaultValue = "50") int limit) {
        
        try {
            List<Review> reviews = moderationService.getReviewsPendingModeration(limit);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching reviews pending moderation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get moderation statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get moderation stats", description = "Get statistics about moderation activities")
    public ResponseEntity<ModerationService.ModerationStats> getModerationStats() {
        try {
            ModerationService.ModerationStats stats = moderationService.getModerationStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching moderation statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Batch approve multiple reviews.
     */
    @PostMapping("/batch/approve")
    public ResponseEntity<Map<String, Object>> batchApproveReviews(
            @RequestBody List<String> reviewIds,
            @RequestParam String moderatorId) {
        
        try {
            int successCount = 0;
            int errorCount = 0;
            
            for (String reviewId : reviewIds) {
                try {
                    moderationService.approveReview(reviewId, moderatorId);
                    successCount++;
                } catch (Exception e) {
                    log.error("Error approving review in batch: {}", reviewId, e);
                    errorCount++;
                }
            }
            
            Map<String, Object> result = Map.of(
                    "total", reviewIds.size(),
                    "approved", successCount,
                    "errors", errorCount
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in batch approve operation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Batch reject multiple reviews.
     */
    @PostMapping("/batch/reject")
    public ResponseEntity<Map<String, Object>> batchRejectReviews(
            @RequestBody List<String> reviewIds,
            @RequestParam String moderatorId,
            @RequestParam String reason) {
        
        try {
            int successCount = 0;
            int errorCount = 0;
            
            for (String reviewId : reviewIds) {
                try {
                    moderationService.rejectReview(reviewId, moderatorId, reason);
                    successCount++;
                } catch (Exception e) {
                    log.error("Error rejecting review in batch: {}", reviewId, e);
                    errorCount++;
                }
            }
            
            Map<String, Object> result = Map.of(
                    "total", reviewIds.size(),
                    "rejected", successCount,
                    "errors", errorCount
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in batch reject operation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get review moderation history.
     */
    @GetMapping("/{reviewId}/history")
    public ResponseEntity<Map<String, Object>> getModerationHistory(@PathVariable String reviewId) {
        try {
            // This would typically fetch moderation history from a separate audit table
            // For now, we'll return basic information
            Map<String, Object> history = Map.of(
                    "reviewId", reviewId,
                    "message", "Moderation history feature not yet implemented",
                    "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching moderation history for review: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get moderation queue summary.
     */
    @GetMapping("/queue/summary")
    public ResponseEntity<Map<String, Object>> getModerationQueueSummary() {
        try {
            ModerationService.ModerationStats stats = moderationService.getModerationStats();
            
            Map<String, Object> summary = Map.of(
                    "pendingReviews", stats.getPendingReviews(),
                    "approvedToday", stats.getApprovedToday(),
                    "rejectedToday", stats.getRejectedToday(),
                    "spamToday", stats.getSpamToday(),
                    "lastUpdated", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error fetching moderation queue summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Search reviews by moderation criteria.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Review>> searchReviewsForModeration(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minSpamScore,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "50") int limit) {
        
        try {
            // This would implement advanced search functionality for moderators
            // For now, return empty list with a message
            log.info("Moderation search requested with keyword: {}, minSpamScore: {}, userId: {}", 
                    keyword, minSpamScore, userId);
            
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Error searching reviews for moderation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get moderation performance metrics.
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getModerationMetrics(
            @RequestParam(required = false) String moderatorId,
            @RequestParam(defaultValue = "7") int days) {
        
        try {
            // This would calculate performance metrics for moderators
            Map<String, Object> metrics = Map.of(
                    "message", "Moderation metrics feature not yet implemented",
                    "moderatorId", moderatorId != null ? moderatorId : "all",
                    "period", days + " days",
                    "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error fetching moderation metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Export moderation data for analysis.
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportModerationData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "json") String format) {
        
        try {
            // This would export moderation data in various formats
            Map<String, Object> exportInfo = Map.of(
                    "message", "Moderation data export feature not yet implemented",
                    "startDate", startDate != null ? startDate : "not specified",
                    "endDate", endDate != null ? endDate : "not specified",
                    "format", format,
                    "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(exportInfo);
        } catch (Exception e) {
            log.error("Error exporting moderation data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}