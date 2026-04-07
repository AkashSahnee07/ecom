package com.ecommerce.review.service;

import com.ecommerce.review.entity.Review;
import com.ecommerce.review.entity.ReviewStatus;
import com.ecommerce.review.entity.ReviewSentiment;
import com.ecommerce.review.entity.ReviewVote;
import com.ecommerce.review.repository.ReviewRepository;
import com.ecommerce.review.repository.ReviewVoteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for managing product reviews.
 * Handles review creation, moderation, voting, and analytics.
 */
@Service
@Transactional
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ModerationService moderationService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final ImageService imageService;
    private final NotificationService notificationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public ReviewService(ReviewRepository reviewRepository, ReviewVoteRepository reviewVoteRepository,
                        ModerationService moderationService, SentimentAnalysisService sentimentAnalysisService,
                        ImageService imageService, NotificationService notificationService,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.reviewRepository = reviewRepository;
        this.reviewVoteRepository = reviewVoteRepository;
        this.moderationService = moderationService;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.imageService = imageService;
        this.notificationService = notificationService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Create a new review for a product.
     */
    public Review createReview(String productId, String userId, String title, String content, 
                              Integer rating, Boolean verifiedPurchase, List<MultipartFile> images) {
        log.info("Creating review for product: {} by user: {}", productId, userId);
        
        Long productIdLong = Long.parseLong(productId);
        Long userIdLong = Long.parseLong(userId);
        
        // Check if user already reviewed this product
        if (reviewRepository.existsByProductIdAndUserId(productIdLong, userIdLong)) {
            throw new IllegalArgumentException("User has already reviewed this product");
        }
        
        Review review = Review.builder()
                .productId(productIdLong)
                .userId(userIdLong)
                .title(title)
                .content(content)
                .rating(rating)
                .verifiedPurchase(verifiedPurchase != null ? verifiedPurchase : false)
                .status(ReviewStatus.PENDING)
                .build();
        
        // Process images if provided
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = imageService.uploadImages(images);
            review.setImageUrls(imageUrls);
        }
        
        // Analyze sentiment
        ReviewSentiment sentiment = sentimentAnalysisService.analyzeSentiment(content);
        review.setSentiment(sentiment);
        review.setSentimentScore(sentimentAnalysisService.getSentimentScore(content));
        
        // Auto-moderation check
        moderationService.performAutoModeration(review);
        
        Review savedReview = reviewRepository.save(review);
        
        // Send notification events
        notificationService.sendReviewCreatedNotification(savedReview);
        
        // Publish event to Kafka
        kafkaTemplate.send("review-events", "review.created", savedReview);
        
        log.info("Review created successfully with ID: {}", savedReview.getId());
        return savedReview;
    }
    
    /**
     * Get reviews for a specific product with pagination and filtering.
     */
    @Transactional(readOnly = true)
    public Page<Review> getProductReviews(String productId, ReviewStatus status, 
                                         Integer minRating, Integer maxRating, 
                                         Boolean verifiedOnly, String sortBy, Pageable pageable) {
        log.debug("Fetching reviews for product: {} with filters", productId);
        Long productIdLong = Long.parseLong(productId);
        
        if (status != null) {
            return reviewRepository.findByProductIdAndStatus(productIdLong, status, pageable);
        }
        
        if (verifiedOnly != null && verifiedOnly) {
            return reviewRepository.findByProductIdAndStatusAndVerifiedPurchase(productIdLong, ReviewStatus.APPROVED, verifiedOnly, pageable);
        }
        
        // For rating filters, we'll use the basic method and filter in memory for now
        // In a production system, you'd want to add these specific methods to the repository
        return reviewRepository.findByProductIdAndStatus(productIdLong, ReviewStatus.APPROVED, pageable);
    }
    
    /**
     * Get a specific review by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Review> getReviewById(String reviewId) {
        return reviewRepository.findById(Long.parseLong(reviewId));
    }
    
    /**
     * Update an existing review.
     */
    public Review updateReview(String reviewId, String userId, String title, 
                              String content, Integer rating, List<MultipartFile> newImages) {
        Review review = reviewRepository.findById(Long.parseLong(reviewId))
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        // Check ownership
        if (!review.getUserId().equals(Long.parseLong(userId))) {
            throw new IllegalArgumentException("User not authorized to update this review");
        }
        
        // Update fields
        if (title != null) review.setTitle(title);
        if (content != null) {
            review.setContent(content);
            // Re-analyze sentiment
            ReviewSentiment sentiment = sentimentAnalysisService.analyzeSentiment(content);
            review.setSentiment(sentiment);
            review.setSentimentScore(sentimentAnalysisService.getSentimentScore(content));
        }
        if (rating != null) review.setRating(rating);
        
        // Handle image updates
        if (newImages != null && !newImages.isEmpty()) {
            // Delete old images
            if (review.getImageUrls() != null) {
                imageService.deleteImages(review.getImageUrls());
            }
            // Upload new images
            List<String> imageUrls = imageService.uploadImages(newImages);
            review.setImageUrls(imageUrls);
        }
        
        // Reset status for re-moderation
        review.setStatus(ReviewStatus.PENDING);
        moderationService.performAutoModeration(review);
        
        Review updatedReview = reviewRepository.save(review);
        
        // Publish event
        kafkaTemplate.send("review-events", "review.updated", updatedReview);
        
        log.info("Review updated successfully: {}", reviewId);
        return updatedReview;
    }
    
    /**
     * Delete a review.
     */
    public void deleteReview(String reviewId, String userId) {
        Long reviewIdLong = Long.parseLong(reviewId);
        Long userIdLong = Long.parseLong(userId);
        
        Review review = reviewRepository.findById(reviewIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        // Check ownership
        if (!review.getUserId().equals(userIdLong)) {
            throw new IllegalArgumentException("User not authorized to delete this review");
        }
        
        // Delete associated images
        if (review.getImageUrls() != null) {
            imageService.deleteImages(review.getImageUrls());
        }
        
        // Soft delete
        review.setStatus(ReviewStatus.DELETED);
        reviewRepository.save(review);
        
        // Publish event
        kafkaTemplate.send("review-events", "review.deleted", review);
        
        log.info("Review deleted successfully: {}", reviewId);
    }
    
    /**
     * Vote on a review (helpful/not helpful).
     */
    public void voteOnReview(String reviewId, String userId, ReviewVote.VoteType voteType, 
                            String ipAddress, String userAgent) {
        Long reviewIdLong = Long.parseLong(reviewId);
        Long userIdLong = Long.parseLong(userId);
        
        Review review = reviewRepository.findById(reviewIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        // Check if user already voted
        Optional<ReviewVote> existingVote = reviewVoteRepository.findByReviewIdAndUserId(reviewIdLong, userIdLong);
        
        if (existingVote.isPresent()) {
            ReviewVote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                // Remove vote if same type
                reviewVoteRepository.delete(vote);
                updateVoteCounts(review, voteType, -1);
            } else {
                // Change vote type
                updateVoteCounts(review, vote.getVoteType(), -1);
                vote.setVoteType(voteType);
                vote.setIpAddress(ipAddress);
                vote.setUserAgent(userAgent);
                reviewVoteRepository.save(vote);
                updateVoteCounts(review, voteType, 1);
            }
        } else {
            // Create new vote
            ReviewVote vote = ReviewVote.builder()
                    .reviewId(reviewIdLong)
                    .userId(userIdLong)
                    .voteType(voteType)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            reviewVoteRepository.save(vote);
            updateVoteCounts(review, voteType, 1);
        }
        
        reviewRepository.save(review);
        log.debug("Vote recorded for review: {} by user: {}", reviewId, userId);
    }
    
    private void updateVoteCounts(Review review, ReviewVote.VoteType voteType, int delta) {
        if (voteType == ReviewVote.VoteType.HELPFUL) {
            review.setHelpfulCount(review.getHelpfulCount() + delta);
        } else {
            review.setNotHelpfulCount(review.getNotHelpfulCount() + delta);
        }
    }
    
    /**
     * Get review statistics for a product.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProductReviewStats(String productId) {
        Long productIdLong = Long.parseLong(productId);
        Long totalReviews = reviewRepository.countByProductIdAndStatus(productIdLong, ReviewStatus.APPROVED);
        Double averageRating = reviewRepository.getAverageRatingByProductId(productIdLong, ReviewStatus.APPROVED);
        List<Object[]> ratingDistributionData = reviewRepository.getRatingDistribution(productIdLong, ReviewStatus.APPROVED);
        Long verifiedReviews = reviewRepository.countVerifiedPurchaseReviews(productIdLong, ReviewStatus.APPROVED);
        Long reviewsWithImages = reviewRepository.countReviewsWithImages(productIdLong, ReviewStatus.APPROVED);
        
        // Convert rating distribution to Map
        Map<Integer, Long> ratingDistribution = new java.util.HashMap<>();
        for (Object[] row : ratingDistributionData) {
            ratingDistribution.put((Integer) row[0], (Long) row[1]);
        }
        
        return Map.of(
                "totalReviews", totalReviews,
                "averageRating", averageRating != null ? averageRating : 0.0,
                "ratingDistribution", ratingDistribution,
                "verifiedReviews", verifiedReviews,
                "reviewsWithImages", reviewsWithImages
        );
    }
    
    /**
     * Get user's reviews with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Review> getUserReviews(String userId, Pageable pageable) {
        Long userIdLong = Long.parseLong(userId);
        return reviewRepository.findByUserIdAndStatus(userIdLong, ReviewStatus.APPROVED, pageable);
    }
    
    /**
     * Search reviews by content.
     */
    @Transactional(readOnly = true)
    public Page<Review> searchReviews(String query, String productId, Pageable pageable) {
        if (productId != null) {
            Long productIdLong = Long.parseLong(productId);
            List<Review> reviews = reviewRepository.searchReviews(productIdLong, ReviewStatus.APPROVED, query, pageable);
            return new org.springframework.data.domain.PageImpl<>(reviews, pageable, reviews.size());
        }
        List<Review> reviews = reviewRepository.searchAllReviews(ReviewStatus.APPROVED, query, pageable);
        return new org.springframework.data.domain.PageImpl<>(reviews, pageable, reviews.size());
    }
    
    /**
     * Get featured reviews for a product.
     */
    @Transactional(readOnly = true)
    public List<Review> getFeaturedReviews(String productId, int limit) {
        Long productIdLong = Long.parseLong(productId);
        return reviewRepository.findByProductIdAndStatusAndFeatured(productIdLong, ReviewStatus.APPROVED, true);
    }
    
    /**
     * Get most helpful reviews for a product.
     */
    @Transactional(readOnly = true)
    public List<Review> getMostHelpfulReviews(String productId, int limit) {
        Long productIdLong = Long.parseLong(productId);
        return reviewRepository.findMostHelpfulReviews(productIdLong, ReviewStatus.APPROVED, 
                org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    /**
     * Get recent reviews across all products.
     */
    @Transactional(readOnly = true)
    public List<Review> getRecentReviews(int limit) {
        return reviewRepository.findNewestReviews(
                null, ReviewStatus.APPROVED, org.springframework.data.domain.PageRequest.of(0, limit));
    }
}