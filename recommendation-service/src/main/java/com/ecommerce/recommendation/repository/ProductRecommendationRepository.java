package com.ecommerce.recommendation.repository;

import com.ecommerce.recommendation.entity.ProductRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductRecommendation entity.
 * Provides data access methods for managing product recommendations.
 */
@Repository
public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, java.util.UUID> {

    /**
     * Find all active recommendations for a user.
     */
    List<ProductRecommendation> findByUserIdAndIsActiveTrueOrderByScoreDesc(String userId);

    /**
     * Find recommendations for a user with pagination.
     */
    Page<ProductRecommendation> findByUserIdAndIsActiveTrue(String userId, Pageable pageable);

    /**
     * Find top N recommendations for a user.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId AND pr.isActive = true AND (pr.expiresAt IS NULL OR pr.expiresAt > CURRENT_TIMESTAMP) ORDER BY pr.score DESC")
    List<ProductRecommendation> findTopRecommendationsForUser(@Param("userId") String userId, Pageable pageable);

    /**
     * Find recommendations by algorithm type.
     */
    List<ProductRecommendation> findByUserIdAndAlgorithmTypeAndIsActiveTrueOrderByScoreDesc(
            String userId, ProductRecommendation.AlgorithmType algorithmType);

    /**
     * Find recommendations for a specific product.
     */
    List<ProductRecommendation> findByRecommendedProductIdAndIsActiveTrueOrderByScoreDesc(String productId);

    /**
     * Find recommendations based on source product.
     */
    List<ProductRecommendation> findBySourceProductIdAndIsActiveTrueOrderByScoreDesc(String sourceProductId);

    /**
     * Find recommendations by category.
     */
    List<ProductRecommendation> findByCategoryIdAndIsActiveTrueOrderByScoreDesc(String categoryId);

    /**
     * Find recommendations by brand.
     */
    List<ProductRecommendation> findByBrandIdAndIsActiveTrueOrderByScoreDesc(String brandId);

    /**
     * Find high confidence recommendations.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId AND pr.isActive = true AND pr.confidence >= :minConfidence ORDER BY pr.confidence DESC")
    List<ProductRecommendation> findHighConfidenceRecommendations(@Param("userId") String userId, @Param("minConfidence") Double minConfidence);

    /**
     * Find high score recommendations.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId AND pr.isActive = true AND pr.score >= :minScore ORDER BY pr.score DESC")
    List<ProductRecommendation> findHighScoreRecommendations(@Param("userId") String userId, @Param("minScore") Double minScore);

    /**
     * Find personalized recommendations.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId AND pr.isActive = true AND pr.algorithmType IN ('COLLABORATIVE_FILTERING', 'HYBRID', 'PERSONALIZED') ORDER BY pr.score DESC")
    List<ProductRecommendation> findPersonalizedRecommendations(@Param("userId") String userId, Pageable pageable);

    /**
     * Find trending recommendations.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.isActive = true AND pr.algorithmType IN ('TRENDING', 'POPULAR') AND pr.createdAt >= :since ORDER BY pr.score DESC")
    List<ProductRecommendation> findTrendingRecommendations(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Find recommendations by price range.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId AND pr.isActive = true AND pr.price BETWEEN :minPrice AND :maxPrice ORDER BY pr.score DESC")
    List<ProductRecommendation> findByPriceRange(@Param("userId") String userId, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    /**
     * Find discounted recommendations.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId AND pr.isActive = true AND pr.discountPercentage IS NOT NULL AND pr.discountPercentage > 0 ORDER BY pr.discountPercentage DESC")
    List<ProductRecommendation> findDiscountedRecommendations(@Param("userId") String userId);

    /**
     * Find clicked recommendations.
     */
    List<ProductRecommendation> findByUserIdAndIsClickedTrueOrderByUpdatedAtDesc(String userId);

    /**
     * Find purchased recommendations.
     */
    List<ProductRecommendation> findByUserIdAndIsPurchasedTrueOrderByUpdatedAtDesc(String userId);

    /**
     * Find successful recommendations (purchased).
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.isPurchased = true AND pr.createdAt >= :since ORDER BY pr.createdAt DESC")
    List<ProductRecommendation> findSuccessfulRecommendations(@Param("since") LocalDateTime since);

    /**
     * Find expired recommendations.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.expiresAt IS NOT NULL AND pr.expiresAt <= CURRENT_TIMESTAMP")
    List<ProductRecommendation> findExpiredRecommendations();

    /**
     * Check if recommendation exists for user and product.
     */
    boolean existsByUserIdAndRecommendedProductIdAndAlgorithmTypeAndIsActiveTrue(
            String userId, String productId, ProductRecommendation.AlgorithmType algorithmType);

    /**
     * Find existing recommendation for update.
     */
    Optional<ProductRecommendation> findByUserIdAndRecommendedProductIdAndAlgorithmType(
            String userId, String productId, ProductRecommendation.AlgorithmType algorithmType);

    /**
     * Get recommendation statistics by algorithm.
     */
    @Query("SELECT pr.algorithmType, AVG(pr.score), AVG(pr.confidence) FROM ProductRecommendation pr WHERE pr.isActive = true GROUP BY pr.algorithmType")
    List<Object[]> getRecommendationStatsByAlgorithm();

    /**
     * Get user recommendation performance.
     */
    /**
     * Get user recommendation performance.
     * Note: This method should be implemented in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getUserRecommendationPerformance(String userId);

    /**
     * Get algorithm performance metrics.
     */
    /**
     * Get algorithm performance metrics.
     * Note: This method should be implemented in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getAlgorithmPerformanceMetrics(LocalDateTime since);

    /**
     * Find top performing recommendations.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.isActive = true AND (pr.isClicked = true OR pr.isPurchased = true) ORDER BY pr.score DESC")
    List<ProductRecommendation> findTopPerformingRecommendations(Pageable pageable);

    /**
     * Update recommendation interaction.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void updateRecommendationClick(String id, boolean isClicked);

    /**
     * Update recommendation purchase.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void markRecommendationAsPurchased(String userId, String productId);

    /**
     * Increment view count.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void incrementViewCount(String id);

    /**
     * Deactivate old recommendations.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // int deactivateExpiredRecommendations();

    /**
     * Deactivate recommendations for user.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void deactivateUserRecommendations(String userId);

    /**
     * Delete old recommendations.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void deleteOldRecommendations(LocalDateTime cutoffDate);

    /**
     * Find recommendations needing refresh.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.isActive = true AND pr.updatedAt < :refreshThreshold")
    List<ProductRecommendation> findRecommendationsNeedingRefresh(@Param("refreshThreshold") LocalDateTime refreshThreshold);

    /**
     * Count active recommendations by user.
     */
    @Query("SELECT COUNT(pr) FROM ProductRecommendation pr WHERE pr.userId = :userId AND pr.isActive = true")
    Long countActiveRecommendationsByUser(@Param("userId") String userId);

    /**
     * Find similar recommendations.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId != :userId AND pr.recommendedProductId = :productId AND pr.isActive = true")
    List<ProductRecommendation> findSimilarRecommendations(@Param("userId") String userId, @Param("productId") String productId, Pageable pageable);

    /**
     * Find cross-sell opportunities.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId AND pr.isActive = true AND pr.algorithmType IN ('CROSS_SELL', 'UP_SELL') AND pr.sourceProductId = :sourceProductId")
    List<ProductRecommendation> findCrossSellRecommendations(@Param("userId") String userId, @Param("sourceProductId") String sourceProductId);

    /**
     * Find recommendations by rank position.
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId AND pr.isActive = true AND pr.rankPosition IS NOT NULL")
    List<ProductRecommendation> findRankedRecommendations(@Param("userId") String userId);
}
