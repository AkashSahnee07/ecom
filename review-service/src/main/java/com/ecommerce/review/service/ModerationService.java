package com.ecommerce.review.service;

import com.ecommerce.review.entity.Review;
import com.ecommerce.review.entity.ReviewStatus;
import com.ecommerce.review.repository.ReviewRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for moderating reviews including spam detection and content filtering.
 */
@Service
@Transactional
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);
    
    private final ReviewRepository reviewRepository;
    
    public ModerationService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }
    
    @Value("${review.moderation.auto-approve:true}")
    private boolean autoApprove;
    
    @Value("${review.moderation.spam-threshold:0.7}")
    private double spamThreshold;
    
    @Value("${review.moderation.min-length:10}")
    private int minContentLength;
    
    @Value("${review.moderation.max-length:5000}")
    private int maxContentLength;
    
    // Common spam patterns
    private static final List<String> SPAM_KEYWORDS = Arrays.asList(
        "buy now", "click here", "free money", "guaranteed", "limited time",
        "act now", "call now", "urgent", "winner", "congratulations",
        "viagra", "casino", "lottery", "investment", "loan"
    );
    
    // Profanity filter (basic implementation)
    private static final List<String> PROFANITY_WORDS = Arrays.asList(
        "damn", "hell", "crap", "stupid", "idiot", "moron"
        // Add more as needed
    );
    
    // Patterns for suspicious content
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+");
    
    /**
     * Perform automatic moderation on a review.
     */
    public void performAutoModeration(Review review) {
        log.debug("Performing auto-moderation for review: {}", review.getId());
        
        double spamScore = calculateSpamScore(review);
        review.setSpamScore(spamScore);
        
        // Check for immediate rejection criteria
        if (shouldReject(review, spamScore)) {
            review.setStatus(ReviewStatus.REJECTED);
            log.info("Review {} automatically rejected (spam score: {})", review.getId(), spamScore);
            return;
        }
        
        // Check for spam
        if (spamScore >= spamThreshold) {
            review.setStatus(ReviewStatus.SPAM);
            log.info("Review {} marked as spam (score: {})", review.getId(), spamScore);
            return;
        }
        
        // Check for profanity
        if (containsProfanity(review.getContent()) || containsProfanity(review.getTitle())) {
            review.setStatus(ReviewStatus.UNDER_REVIEW);
            log.info("Review {} flagged for profanity review", review.getId());
            return;
        }
        
        // Auto-approve if enabled and passes all checks
        if (autoApprove && spamScore < spamThreshold * 0.5) {
            review.setStatus(ReviewStatus.APPROVED);
            log.debug("Review {} auto-approved", review.getId());
        } else {
            review.setStatus(ReviewStatus.UNDER_REVIEW);
            log.debug("Review {} requires manual review", review.getId());
        }
    }
    
    /**
     * Calculate spam score for a review.
     */
    private double calculateSpamScore(Review review) {
        double score = 0.0;
        String content = (review.getContent() + " " + review.getTitle()).toLowerCase();
        
        // Length checks
        if (review.getContent().length() < minContentLength) {
            score += 0.3;
        }
        if (review.getContent().length() > maxContentLength) {
            score += 0.2;
        }
        
        // Spam keyword detection
        long spamKeywordCount = SPAM_KEYWORDS.stream()
            .mapToLong(keyword -> countOccurrences(content, keyword))
            .sum();
        score += Math.min(spamKeywordCount * 0.15, 0.5);
        
        // Suspicious patterns
        if (EMAIL_PATTERN.matcher(content).find()) {
            score += 0.4;
        }
        if (PHONE_PATTERN.matcher(content).find()) {
            score += 0.3;
        }
        if (URL_PATTERN.matcher(content).find()) {
            score += 0.5;
        }
        
        // Repetitive content
        if (isRepetitive(review.getContent())) {
            score += 0.3;
        }
        
        // All caps content
        if (isAllCaps(review.getContent())) {
            score += 0.2;
        }
        
        // Excessive punctuation
        if (hasExcessivePunctuation(review.getContent())) {
            score += 0.2;
        }
        
        // Check for duplicate content from same user
        if (hasDuplicateContent(review)) {
            score += 0.6;
        }
        
        return Math.min(score, 1.0);
    }
    
    /**
     * Check if review should be immediately rejected.
     */
    private boolean shouldReject(Review review, double spamScore) {
        // Reject if extremely high spam score
        if (spamScore >= 0.9) {
            return true;
        }
        
        // Reject if content is too short or too long
        if (review.getContent().length() < 5 || review.getContent().length() > 10000) {
            return true;
        }
        
        // Reject if rating is invalid
        if (review.getRating() < 1 || review.getRating() > 5) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if content contains profanity.
     */
    private boolean containsProfanity(String content) {
        if (content == null) return false;
        
        String lowerContent = content.toLowerCase();
        return PROFANITY_WORDS.stream()
            .anyMatch(lowerContent::contains);
    }
    
    /**
     * Count occurrences of a substring in text.
     */
    private long countOccurrences(String text, String substring) {
        return (text.length() - text.replace(substring, "").length()) / substring.length();
    }
    
    /**
     * Check if content is repetitive.
     */
    private boolean isRepetitive(String content) {
        if (content.length() < 20) return false;
        
        String[] words = content.toLowerCase().split("\\s+");
        if (words.length < 5) return false;
        
        // Check for repeated words
        long uniqueWords = Arrays.stream(words).distinct().count();
        double repetitionRatio = (double) uniqueWords / words.length;
        
        return repetitionRatio < 0.5;
    }
    
    /**
     * Check if content is mostly in uppercase.
     */
    private boolean isAllCaps(String content) {
        if (content.length() < 10) return false;
        
        long upperCaseCount = content.chars()
            .filter(Character::isUpperCase)
            .count();
        long letterCount = content.chars()
            .filter(Character::isLetter)
            .count();
        
        return letterCount > 0 && (double) upperCaseCount / letterCount > 0.7;
    }
    
    /**
     * Check if content has excessive punctuation.
     */
    private boolean hasExcessivePunctuation(String content) {
        long punctuationCount = content.chars()
            .filter(ch -> "!@#$%^&*()_+-=[]{}|;':,.<>?".indexOf(ch) >= 0)
            .count();
        
        return punctuationCount > content.length() * 0.2;
    }
    
    /**
     * Check if user has submitted similar content before.
     */
    private boolean hasDuplicateContent(Review review) {
        List<Review> userReviews = reviewRepository.findByUserIdAndStatus(
            review.getUserId(), 
            ReviewStatus.APPROVED
        );
        
        return userReviews.stream()
            .anyMatch(existingReview -> 
                !existingReview.getId().equals(review.getId()) &&
                calculateSimilarity(review.getContent(), existingReview.getContent()) > 0.8
            );
    }
    
    /**
     * Calculate similarity between two strings (simple implementation).
     */
    private double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) return 0.0;
        
        String[] words1 = str1.toLowerCase().split("\\s+");
        String[] words2 = str2.toLowerCase().split("\\s+");
        
        List<String> list1 = Arrays.asList(words1);
        List<String> list2 = Arrays.asList(words2);
        
        long commonWords = list1.stream()
            .filter(list2::contains)
            .count();
        
        int totalWords = Math.max(words1.length, words2.length);
        return totalWords > 0 ? (double) commonWords / totalWords : 0.0;
    }
    
    /**
     * Manually approve a review.
     */
    public void approveReview(String reviewId, String moderatorId) {
        Review review = reviewRepository.findById(Long.parseLong(reviewId))
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        review.setStatus(ReviewStatus.APPROVED);
        review.setModeratedBy(Long.parseLong(moderatorId));
        review.setModeratedAt(LocalDateTime.now());
        
        reviewRepository.save(review);
        log.info("Review {} manually approved by moderator {}", reviewId, moderatorId);
    }
    
    /**
     * Manually reject a review.
     */
    public void rejectReview(String reviewId, String moderatorId, String reason) {
        Review review = reviewRepository.findById(Long.parseLong(reviewId))
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        review.setStatus(ReviewStatus.REJECTED);
        review.setModerationNotes(reason);
        review.setModeratedBy(Long.parseLong(moderatorId));
        review.setModeratedAt(LocalDateTime.now());
        
        reviewRepository.save(review);
        log.info("Review {} manually rejected by moderator {} - Reason: {}", 
                reviewId, moderatorId, reason);
    }
    
    /**
     * Mark review as spam.
     */
    public void markAsSpam(String reviewId, String moderatorId) {
        Review review = reviewRepository.findById(Long.parseLong(reviewId))
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        review.setStatus(ReviewStatus.SPAM);
        review.setModeratedBy(Long.parseLong(moderatorId));
        review.setModeratedAt(LocalDateTime.now());
        
        reviewRepository.save(review);
        log.info("Review {} marked as spam by moderator {}", reviewId, moderatorId);
    }
    
    /**
     * Hide a review (soft delete).
     */
    public void hideReview(String reviewId, String moderatorId, String reason) {
        Review review = reviewRepository.findById(Long.parseLong(reviewId))
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        review.setStatus(ReviewStatus.HIDDEN);
        review.setModerationNotes(reason);
        review.setModeratedBy(Long.parseLong(moderatorId));
        review.setModeratedAt(LocalDateTime.now());
        
        reviewRepository.save(review);
        log.info("Review {} hidden by moderator {} - Reason: {}", 
                reviewId, moderatorId, reason);
    }
    
    /**
     * Get reviews pending moderation.
     */
    public List<Review> getReviewsPendingModeration(int limit) {
        return reviewRepository.findByStatus(
            ReviewStatus.UNDER_REVIEW,
            org.springframework.data.domain.PageRequest.of(0, limit)
        ).getContent();
    }
    
    /**
     * Get moderation statistics.
     */
    public ModerationStats getModerationStats() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        
        // Use available repository methods - these need to be implemented or use existing ones
        long pendingCount = reviewRepository.countReviewsSince(ReviewStatus.UNDER_REVIEW, startOfDay);
        long approvedToday = reviewRepository.countReviewsSince(ReviewStatus.APPROVED, startOfDay);
        long rejectedToday = reviewRepository.countReviewsSince(ReviewStatus.REJECTED, startOfDay);
        long spamToday = reviewRepository.countReviewsSince(ReviewStatus.SPAM, startOfDay);
        
        return new ModerationStats(pendingCount, approvedToday, rejectedToday, spamToday);
    }
    
    /**
     * Data class for moderation statistics.
     */
    public static class ModerationStats {
        private long pendingReviews;
        private long approvedToday;
        private long rejectedToday;
        private long spamToday;
        
        public ModerationStats(long pendingReviews, long approvedToday, long rejectedToday, long spamToday) {
            this.pendingReviews = pendingReviews;
            this.approvedToday = approvedToday;
            this.rejectedToday = rejectedToday;
            this.spamToday = spamToday;
        }
        
        public long getPendingReviews() {
            return pendingReviews;
        }
        
        public void setPendingReviews(long pendingReviews) {
            this.pendingReviews = pendingReviews;
        }
        
        public long getApprovedToday() {
            return approvedToday;
        }
        
        public void setApprovedToday(long approvedToday) {
            this.approvedToday = approvedToday;
        }
        
        public long getRejectedToday() {
            return rejectedToday;
        }
        
        public void setRejectedToday(long rejectedToday) {
            this.rejectedToday = rejectedToday;
        }
        
        public long getSpamToday() {
            return spamToday;
        }
        
        public void setSpamToday(long spamToday) {
            this.spamToday = spamToday;
        }
    }
}
