package com.ecommerce.recommendation.repository;

import com.ecommerce.recommendation.entity.ProductSimilarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ProductSimilarity entity.
 * Provides data access methods for product similarity management and retrieval.
 */
@Repository
public interface ProductSimilarityRepository extends JpaRepository<ProductSimilarity, UUID> {

    /**
     * Find similar products for a given source product.
     */
    List<ProductSimilarity> findBySourceProductIdAndIsActiveTrueOrderBySimilarityScoreDesc(String sourceProductId);

    /**
     * Find similar products with pagination.
     */
    Page<ProductSimilarity> findBySourceProductIdAndIsActiveTrue(String sourceProductId, Pageable pageable);

    /**
     * Find top N similar products.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND (ps.expiresAt IS NULL OR ps.expiresAt > CURRENT_TIMESTAMP) ORDER BY ps.similarityScore DESC")
    List<ProductSimilarity> findTopSimilarProducts(@Param("sourceProductId") String sourceProductId, Pageable pageable);

    /**
     * Find similarities by similarity type.
     */
    List<ProductSimilarity> findBySourceProductIdAndSimilarityTypeAndIsActiveTrueOrderBySimilarityScoreDesc(
            String sourceProductId, ProductSimilarity.SimilarityType similarityType);

    /**
     * Find similarities for target product.
     */
    List<ProductSimilarity> findByTargetProductIdAndIsActiveTrueOrderBySimilarityScoreDesc(String targetProductId);

    /**
     * Find bidirectional similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ((ps.sourceProductId = :productId1 AND ps.targetProductId = :productId2) OR (ps.sourceProductId = :productId2 AND ps.targetProductId = :productId1)) AND ps.isActive = true")
    List<ProductSimilarity> findBidirectionalSimilarities(@Param("productId1") String productId1, @Param("productId2") String productId2);

    /**
     * Find products with high similarity scores.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.similarityScore >= :minScore ORDER BY ps.similarityScore DESC")
    List<ProductSimilarity> findHighSimilarityProducts(@Param("sourceProductId") String sourceProductId, @Param("minScore") Double minScore);

    /**
     * Find similarities by category.
     */
    List<ProductSimilarity> findByCategoryIdAndIsActiveTrueOrderBySimilarityScoreDesc(String categoryId);

    /**
     * Find similarities by brand.
     */
    List<ProductSimilarity> findByBrandIdAndIsActiveTrueOrderBySimilarityScoreDesc(String brandId);

    /**
     * Find content-based similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.similarityType IN ('CONTENT_BASED', 'SEMANTIC', 'VISUAL', 'CATEGORY', 'BRAND') ORDER BY ps.similarityScore DESC")
    List<ProductSimilarity> findContentBasedSimilarities(@Param("sourceProductId") String sourceProductId);

    /**
     * Find collaborative filtering similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.similarityType IN ('COLLABORATIVE', 'BEHAVIORAL') ORDER BY ps.similarityScore DESC")
    List<ProductSimilarity> findCollaborativeSimilarities(@Param("sourceProductId") String sourceProductId);

    /**
     * Find verified similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.isVerified = true ORDER BY ps.similarityScore DESC")
    List<ProductSimilarity> findVerifiedSimilarities(@Param("sourceProductId") String sourceProductId);

    /**
     * Find frequently used similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.usageCount >= :minUsage ORDER BY ps.usageCount DESC")
    List<ProductSimilarity> findFrequentlyUsedSimilarities(@Param("sourceProductId") String sourceProductId, @Param("minUsage") Integer minUsage);

    /**
     * Find successful similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.successRate >= :minSuccessRate ORDER BY ps.successRate DESC")
    List<ProductSimilarity> findSuccessfulSimilarities(@Param("sourceProductId") String sourceProductId, @Param("minSuccessRate") Double minSuccessRate);

    /**
     * Find cross-category similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.similarityType = 'CROSS_CATEGORY' ORDER BY ps.similarityScore DESC")
    List<ProductSimilarity> findCrossCategorySimilarities(@Param("sourceProductId") String sourceProductId);

    /**
     * Find similarities by calculation method.
     */
    List<ProductSimilarity> findByCalculationMethodAndIsActiveTrueOrderBySimilarityScoreDesc(String calculationMethod);

    /**
     * Find similarities by algorithm version.
     */
    List<ProductSimilarity> findByAlgorithmVersionAndIsActiveTrueOrderBySimilarityScoreDesc(String algorithmVersion);

    /**
     * Find expired similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.expiresAt IS NOT NULL AND ps.expiresAt <= CURRENT_TIMESTAMP")
    List<ProductSimilarity> findExpiredSimilarities();

    /**
     * Check if similarity exists.
     */
    boolean existsBySourceProductIdAndTargetProductIdAndSimilarityTypeAndIsActiveTrue(
            String sourceProductId, String targetProductId, ProductSimilarity.SimilarityType similarityType);

    /**
     * Find existing similarity for update.
     */
    Optional<ProductSimilarity> findBySourceProductIdAndTargetProductIdAndSimilarityType(
            String sourceProductId, String targetProductId, ProductSimilarity.SimilarityType similarityType);

    /**
     * Get similarity statistics by type.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getSimilarityStatsByType();

    /**
     * Get category similarity statistics.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getCategorySimilarityStats();

    /**
     * Get algorithm performance metrics.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getAlgorithmPerformanceMetrics();

    /**
     * Find similarities needing refresh.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.isActive = true AND ps.updatedAt < :refreshThreshold")
    List<ProductSimilarity> findSimilaritiesNeedingRefresh(@Param("refreshThreshold") LocalDateTime refreshThreshold);

    /**
     * Find top performing similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.isActive = true AND ps.usageCount > 0 AND ps.successRate IS NOT NULL ORDER BY ps.successRate DESC, ps.usageCount DESC")
    List<ProductSimilarity> findTopPerformingSimilarities(Pageable pageable);

    /**
     * Increment usage count.
     * TODO: Implement in service layer using MongoTemplate
     */
    // void incrementUsageCount(String id);

    /**
     * Update success rate.
     * TODO: Implement in service layer using MongoTemplate
     */
    // void updateSuccessRate(String id, Double successRate);

    /**
     * Verify similarity.
     * TODO: Implement in service layer using MongoTemplate
     */
    // void verifySimilarity(String id, Double verificationScore);

    /**
     * Deactivate expired similarities.
     * TODO: Implement in service layer using MongoTemplate
     */
    // int deactivateExpiredSimilarities();

    /**
     * Deactivate product similarities.
     * TODO: Implement in service layer using MongoTemplate
     */
    // void deactivateProductSimilarities(String productId);

    /**
     * Delete old similarities.
     * TODO: Implement in service layer using MongoTemplate
     */
    // void deleteOldSimilarities(LocalDateTime cutoffDate);

    /**
     * Count active similarities for a product.
     */
    @Query("SELECT COUNT(ps) FROM ProductSimilarity ps WHERE ps.sourceProductId = :productId AND ps.isActive = true")
    Long countActiveSimilaritiesByProduct(@Param("productId") String productId);

    /**
     * Find mutual similarities (A similar to B and B similar to A).
     */
    // Complex query - TODO: Implement in service layer using MongoTemplate
    // List<ProductSimilarity> findMutualSimilarities();

    /**
     * Find asymmetric similarities (A similar to B but not B to A).
     */
    // Complex query - TODO: Implement in service layer using MongoTemplate
    // List<ProductSimilarity> findAsymmetricSimilarities();

    /**
     * Find similarities by confidence range.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.confidence >= :minConfidence AND ps.confidence <= :maxConfidence ORDER BY ps.confidence DESC")
    List<ProductSimilarity> findByConfidenceRange(@Param("sourceProductId") String sourceProductId, @Param("minConfidence") Double minConfidence, @Param("maxConfidence") Double maxConfidence);

    /**
     * Find similarities by feature similarity.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.featureSimilarity >= :minFeatureSimilarity ORDER BY ps.featureSimilarity DESC")
    List<ProductSimilarity> findByFeatureSimilarity(@Param("sourceProductId") String sourceProductId, @Param("minFeatureSimilarity") Double minFeatureSimilarity);

    /**
     * Find similarities by visual similarity.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.visualSimilarity IS NOT NULL AND ps.visualSimilarity >= :minVisualSimilarity ORDER BY ps.visualSimilarity DESC")
    List<ProductSimilarity> findByVisualSimilarity(@Param("sourceProductId") String sourceProductId, @Param("minVisualSimilarity") Double minVisualSimilarity);

    /**
     * Find recent similarities.
     */
    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.sourceProductId = :sourceProductId AND ps.isActive = true AND ps.createdAt >= :since ORDER BY ps.createdAt DESC")
    List<ProductSimilarity> findRecentSimilarities(@Param("sourceProductId") String sourceProductId, @Param("since") LocalDateTime since);
    
    /**
     * Delete old similarities before a certain date.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductSimilarity ps WHERE ps.createdAt < :before")
    void deleteOldSimilarities(@Param("before") LocalDateTime before);
    
    /**
     * Deactivate similarities by source product.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProductSimilarity ps SET ps.isActive = false WHERE ps.sourceProductId = :sourceProductId")
    void deactivateSimilaritiesBySourceProduct(@Param("sourceProductId") String sourceProductId);
}
