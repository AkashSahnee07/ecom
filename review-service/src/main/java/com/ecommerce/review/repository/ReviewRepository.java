package com.ecommerce.review.repository;

import com.ecommerce.review.entity.Review;
import com.ecommerce.review.entity.ReviewStatus;
import com.ecommerce.review.entity.ReviewSentiment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Review Repository Interface
 * 
 * Provides data access methods for Review entities with custom queries
 * for complex review operations, analytics, and reporting.
 * 
 * @author E-Commerce Platform Team
 * @version 1.0
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Basic Queries
    List<Review> findByProductIdAndStatus(Long productId, ReviewStatus status);
    
    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);
    
    List<Review> findByUserIdAndStatus(Long userId, ReviewStatus status);
    
    Page<Review> findByUserIdAndStatus(Long userId, ReviewStatus status, Pageable pageable);
    
    List<Review> findByStatus(ReviewStatus status);
    
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);
    
    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);
    
    boolean existsByProductIdAndUserId(Long productId, Long userId);
    
    // Rating Queries
    List<Review> findByProductIdAndStatusAndRating(Long productId, ReviewStatus status, Integer rating);
    
    List<Review> findByProductIdAndStatusAndRatingGreaterThanEqual(Long productId, ReviewStatus status, Integer rating);
    
    List<Review> findByProductIdAndStatusAndRatingLessThanEqual(Long productId, ReviewStatus status, Integer rating);
    
    // Verified Purchase Queries
    List<Review> findByProductIdAndStatusAndVerifiedPurchase(Long productId, ReviewStatus status, Boolean verifiedPurchase);
    
    Page<Review> findByProductIdAndStatusAndVerifiedPurchase(Long productId, ReviewStatus status, Boolean verifiedPurchase, Pageable pageable);
    
    // Date Range Queries
    List<Review> findByProductIdAndStatusAndCreatedDateBetween(Long productId, ReviewStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Review> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Sentiment Queries
    List<Review> findByProductIdAndStatusAndSentiment(Long productId, ReviewStatus status, ReviewSentiment sentiment);
    
    List<Review> findBySentiment(ReviewSentiment sentiment);
    
    // Featured Reviews
    List<Review> findByProductIdAndStatusAndFeatured(Long productId, ReviewStatus status, Boolean featured);
    
    List<Review> findByFeaturedAndStatus(Boolean featured, ReviewStatus status);
    
    // Reply Queries
    List<Review> findByParentReviewId(Long parentReviewId);
    
    List<Review> findByParentReviewIdAndStatus(Long parentReviewId, ReviewStatus status);
    
    // Moderation Queries
    List<Review> findByModeratedBy(Long moderatorId);
    
    List<Review> findByModeratedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Spam Detection
    List<Review> findBySpamScoreGreaterThan(Double spamScore);
    
    List<Review> findByStatusAndSpamScoreGreaterThan(ReviewStatus status, Double spamScore);
    
    // Custom Queries
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = :status ORDER BY r.helpfulCount DESC, r.createdDate DESC")
    List<Review> findMostHelpfulReviews(@Param("productId") Long productId, @Param("status") ReviewStatus status, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = :status ORDER BY r.createdDate DESC")
    List<Review> findNewestReviews(@Param("productId") Long productId, @Param("status") ReviewStatus status, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = :status ORDER BY r.rating DESC, r.createdDate DESC")
    List<Review> findHighestRatedReviews(@Param("productId") Long productId, @Param("status") ReviewStatus status, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = :status ORDER BY r.rating ASC, r.createdDate DESC")
    List<Review> findLowestRatedReviews(@Param("productId") Long productId, @Param("status") ReviewStatus status, Pageable pageable);
    
    // Statistics Queries
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = :status")
    Long countByProductIdAndStatus(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = :status")
    Double getAverageRatingByProductId(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = :status GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = :status AND r.verifiedPurchase = true")
    Long countVerifiedPurchaseReviews(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = :status AND r.imageUrls IS NOT EMPTY")
    Long countReviewsWithImages(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    // Sentiment Statistics
    @Query("SELECT r.sentiment, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = :status AND r.sentiment IS NOT NULL GROUP BY r.sentiment")
    List<Object[]> getSentimentDistribution(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    @Query("SELECT AVG(r.sentimentScore) FROM Review r WHERE r.productId = :productId AND r.status = :status AND r.sentimentScore IS NOT NULL")
    Double getAverageSentimentScore(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    // Helpfulness Statistics
    @Query("SELECT SUM(r.helpfulCount) FROM Review r WHERE r.productId = :productId AND r.status = :status")
    Long getTotalHelpfulVotes(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    @Query("SELECT SUM(r.totalVotes) FROM Review r WHERE r.productId = :productId AND r.status = :status")
    Long getTotalVotes(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    // Search Queries
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = :status AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Review> searchReviews(@Param("productId") Long productId, @Param("status") ReviewStatus status, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.status = :status AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Review> searchAllReviews(@Param("status") ReviewStatus status, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Tag Queries
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = :status AND :tag MEMBER OF r.tags")
    List<Review> findByProductIdAndStatusAndTag(@Param("productId") Long productId, @Param("status") ReviewStatus status, @Param("tag") String tag);
    
    // Update Queries
    @Modifying
    @Query("UPDATE Review r SET r.status = :status, r.moderatedBy = :moderatorId, r.moderatedAt = :moderatedAt, r.moderationNotes = :notes WHERE r.id = :reviewId")
    int updateReviewStatus(@Param("reviewId") Long reviewId, @Param("status") ReviewStatus status, 
                          @Param("moderatorId") Long moderatorId, @Param("moderatedAt") LocalDateTime moderatedAt, 
                          @Param("notes") String notes);
    
    @Modifying
    @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount + 1, r.totalVotes = r.totalVotes + 1 WHERE r.id = :reviewId")
    int incrementHelpfulCount(@Param("reviewId") Long reviewId);
    
    @Modifying
    @Query("UPDATE Review r SET r.notHelpfulCount = r.notHelpfulCount + 1, r.totalVotes = r.totalVotes + 1 WHERE r.id = :reviewId")
    int incrementNotHelpfulCount(@Param("reviewId") Long reviewId);
    
    @Modifying
    @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount - 1, r.totalVotes = r.totalVotes - 1 WHERE r.id = :reviewId AND r.helpfulCount > 0")
    int decrementHelpfulCount(@Param("reviewId") Long reviewId);
    
    @Modifying
    @Query("UPDATE Review r SET r.notHelpfulCount = r.notHelpfulCount - 1, r.totalVotes = r.totalVotes - 1 WHERE r.id = :reviewId AND r.notHelpfulCount > 0")
    int decrementNotHelpfulCount(@Param("reviewId") Long reviewId);
    
    @Modifying
    @Query("UPDATE Review r SET r.replyCount = r.replyCount + 1 WHERE r.id = :reviewId")
    int incrementReplyCount(@Param("reviewId") Long reviewId);
    
    @Modifying
    @Query("UPDATE Review r SET r.replyCount = r.replyCount - 1 WHERE r.id = :reviewId AND r.replyCount > 0")
    int decrementReplyCount(@Param("reviewId") Long reviewId);
    
    @Modifying
    @Query("UPDATE Review r SET r.featured = :featured WHERE r.id = :reviewId")
    int updateFeaturedStatus(@Param("reviewId") Long reviewId, @Param("featured") Boolean featured);
    
    @Modifying
    @Query("UPDATE Review r SET r.spamScore = :spamScore WHERE r.id = :reviewId")
    int updateSpamScore(@Param("reviewId") Long reviewId, @Param("spamScore") Double spamScore);
    
    @Modifying
    @Query("UPDATE Review r SET r.sentimentScore = :sentimentScore, r.sentiment = :sentiment WHERE r.id = :reviewId")
    int updateSentiment(@Param("reviewId") Long reviewId, @Param("sentimentScore") Double sentimentScore, @Param("sentiment") ReviewSentiment sentiment);
    
    // Cleanup Queries
    @Modifying
    @Query("DELETE FROM Review r WHERE r.status = :status AND r.createdDate < :cutoffDate")
    int deleteOldReviewsByStatus(@Param("status") ReviewStatus status, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Analytics Queries
    @Query("SELECT DATE(r.createdDate), COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = :status AND r.createdDate >= :startDate GROUP BY DATE(r.createdDate) ORDER BY DATE(r.createdDate)")
    List<Object[]> getReviewCountByDate(@Param("productId") Long productId, @Param("status") ReviewStatus status, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT r.rating, AVG(r.helpfulCount) FROM Review r WHERE r.productId = :productId AND r.status = :status GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getAverageHelpfulnessByRating(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.status = :status AND r.createdDate >= :startDate")
    Long countReviewsSince(@Param("status") ReviewStatus status, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT r.productId, COUNT(r), AVG(r.rating) FROM Review r WHERE r.status = :status GROUP BY r.productId ORDER BY COUNT(r) DESC")
    List<Object[]> getTopReviewedProducts(@Param("status") ReviewStatus status, Pageable pageable);
}