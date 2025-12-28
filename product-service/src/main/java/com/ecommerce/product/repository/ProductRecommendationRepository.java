package com.ecommerce.product.repository;

import com.ecommerce.product.entity.ProductRecommendation;
import com.ecommerce.product.entity.ProductRecommendation.RecommendationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, Long> {
    
    // Find recommendations for a user, ordered by score
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId " +
           "AND pr.expiresAt > :now ORDER BY pr.score DESC")
    Page<ProductRecommendation> findByUserIdAndNotExpired(@Param("userId") Long userId, 
                                                          @Param("now") LocalDateTime now, 
                                                          Pageable pageable);
    
    // Find recommendations by type for a user
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId " +
           "AND pr.recommendationType = :type AND pr.expiresAt > :now ORDER BY pr.score DESC")
    Page<ProductRecommendation> findByUserIdAndTypeAndNotExpired(@Param("userId") Long userId,
                                                                 @Param("type") RecommendationType type,
                                                                 @Param("now") LocalDateTime now,
                                                                 Pageable pageable);
    
    // Find top recommendations for a user
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId " +
           "AND pr.expiresAt > :now ORDER BY pr.score DESC")
    List<ProductRecommendation> findTopRecommendationsForUser(@Param("userId") Long userId,
                                                              @Param("now") LocalDateTime now,
                                                              Pageable pageable);
    
    // Check if recommendation already exists
    boolean existsByUserIdAndProductIdAndRecommendationType(Long userId, Long productId, RecommendationType type);
    
    // Find existing recommendation to update score
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId " +
           "AND pr.productId = :productId AND pr.recommendationType = :type")
    ProductRecommendation findByUserIdAndProductIdAndType(@Param("userId") Long userId,
                                                          @Param("productId") Long productId,
                                                          @Param("type") RecommendationType type);
    
    // Find recommendations by user, product and type (Optional return)
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId " +
           "AND pr.productId = :productId AND pr.recommendationType = :type")
    java.util.Optional<ProductRecommendation> findByUserIdAndProductIdAndRecommendationType(@Param("userId") Long userId,
                                                                                            @Param("productId") Long productId,
                                                                                            @Param("type") RecommendationType type);
    
    // Find recommendations updated before a certain time
    List<ProductRecommendation> findByCreatedAtBefore(LocalDateTime dateTime);
    
    // Count recommendations by user and type
    Long countByUserIdAndRecommendationType(Long userId, RecommendationType type);
    
    // Find latest recommendation for user
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.userId = :userId " +
           "ORDER BY pr.createdAt DESC")
    java.util.Optional<ProductRecommendation> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // Delete expired recommendations
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductRecommendation pr WHERE pr.expiresAt <= :now")
    void deleteExpiredRecommendations(@Param("now") LocalDateTime now);
    
    // Delete recommendations for a specific user and product
    @Modifying
    @Transactional
    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    // Delete all recommendations for a user
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
    
    // Count recommendations by type
    Long countByRecommendationType(RecommendationType type);
    
    // Find recommendations for a product (to see who it's recommended to)
    List<ProductRecommendation> findByProductId(Long productId);
    
    // Get recommendation statistics for a user
    @Query("SELECT pr.recommendationType, COUNT(pr), AVG(pr.score) FROM ProductRecommendation pr " +
           "WHERE pr.userId = :userId AND pr.expiresAt > :now GROUP BY pr.recommendationType")
    List<Object[]> getRecommendationStatsByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}