package com.ecommerce.recommendation.repository;

import com.ecommerce.recommendation.entity.UserBehavior;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserBehavior entity.
 * Provides data access methods for user behavior tracking and analysis.
 */
@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, java.util.UUID> {

    /**
     * Find all behaviors for a specific user.
     */
    List<UserBehavior> findByUserIdOrderByTimestampDesc(String userId);

    /**
     * Find behaviors for a user with pagination.
     */
    Page<UserBehavior> findByUserId(String userId, Pageable pageable);

    /**
     * Find all behaviors for a specific product.
     */
    List<UserBehavior> findByProductIdOrderByTimestampDesc(String productId);

    /**
     * Find behaviors by action type.
     */
    List<UserBehavior> findByActionTypeOrderByTimestampDesc(UserBehavior.ActionType actionType);

    /**
     * Find behaviors for a user and specific action type.
     */
    List<UserBehavior> findByUserIdAndActionTypeOrderByTimestampDesc(String userId, UserBehavior.ActionType actionType);

    /**
     * Find behaviors for a product and specific action type.
     */
    List<UserBehavior> findByProductIdAndActionTypeOrderByTimestampDesc(String productId, UserBehavior.ActionType actionType);

    /**
     * Find behaviors within a date range.
     */
    List<UserBehavior> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find user behaviors within a date range.
     */
    List<UserBehavior> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            String userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find product behaviors within a date range.
     */
    List<UserBehavior> findByProductIdAndTimestampBetweenOrderByTimestampDesc(
            String productId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find recent behaviors for a user (last N days).
     */
    @Query("SELECT ub FROM UserBehavior ub WHERE ub.userId = :userId AND ub.timestamp >= :since ORDER BY ub.timestamp DESC")
    List<UserBehavior> findRecentUserBehaviors(@Param("userId") String userId, @Param("since") LocalDateTime since);

    /**
     * Find recent behaviors for a product (last N days).
     */
    @Query("SELECT ub FROM UserBehavior ub WHERE ub.productId = :productId AND ub.timestamp >= :since ORDER BY ub.timestamp DESC")
    List<UserBehavior> findRecentProductBehaviors(@Param("productId") String productId, @Param("since") LocalDateTime since);

    /**
     * Find users who interacted with a specific product.
     */
    @Query("SELECT DISTINCT ub.userId FROM UserBehavior ub WHERE ub.productId = :productId")
    List<String> findUsersByProduct(@Param("productId") String productId);

    /**
     * Find products interacted by a specific user.
     */
    @Query("SELECT DISTINCT ub.productId FROM UserBehavior ub WHERE ub.userId = :userId")
    List<String> findProductsByUser(@Param("userId") String userId);

    /**
     * Find users who purchased a specific product.
     */
    @Query("SELECT DISTINCT ub.userId FROM UserBehavior ub WHERE ub.productId = :productId AND ub.actionType = 'PURCHASE'")
    List<String> findPurchasersByProduct(@Param("productId") String productId);

    /**
     * Find products purchased by a specific user.
     */
    @Query("SELECT DISTINCT ub.productId FROM UserBehavior ub WHERE ub.userId = :userId AND ub.actionType = 'PURCHASE'")
    List<String> findPurchasedProductsByUser(@Param("userId") String userId);

    /**
     * Find users with similar behavior patterns (collaborative filtering).
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> findSimilarUsers(String userId, int minCommonProducts);

    /**
     * Find products frequently bought together.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> findFrequentlyBoughtTogether(String productId, int daysDiff);

    /**
     * Get user behavior statistics.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getUserBehaviorStats(String userId);

    /**
     * Get product interaction statistics.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getProductInteractionStats(String productId);

    /**
     * Find trending products based on recent interactions.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> findTrendingProducts(LocalDateTime since, Pageable pageable);

    /**
     * Get user category preferences.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getUserCategoryPreferences(String userId);

    /**
     * Get user brand preferences.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getUserBrandPreferences(String userId);

    /**
     * Calculate user engagement scores.
     * TODO: Implement in service layer using MongoTemplate aggregation
     */
    // List<Object[]> calculateUserEngagementScores(LocalDateTime since);

    /**
     * Find abandoned cart products.
     */
    @Query("SELECT DISTINCT ub.productId FROM UserBehavior ub " +
           "WHERE ub.userId = :userId AND ub.actionType = 'ADD_TO_CART' " +
           "AND ub.productId NOT IN (" +
           "    SELECT ub2.productId FROM UserBehavior ub2 " +
           "    WHERE ub2.userId = :userId AND ub2.actionType = 'PURCHASE' " +
           "    AND ub2.timestamp > ub.timestamp" +
           ")")
    List<String> findAbandonedCartProducts(@Param("userId") String userId);

    /**
     * Count behaviors by user and action type within date range.
     */
    @Query("SELECT COUNT(ub) FROM UserBehavior ub WHERE ub.userId = :userId AND ub.actionType = :actionType AND ub.timestamp BETWEEN :startDate AND :endDate")
    Long countByUserAndActionTypeAndDateRange(@Param("userId") String userId, @Param("actionType") UserBehavior.ActionType actionType, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find last behavior for user and product.
     */
    Optional<UserBehavior> findFirstByUserIdAndProductIdOrderByTimestampDesc(String userId, String productId);

    /**
     * Check if user has specific behavior for product.
     */
    boolean existsByUserIdAndProductIdAndActionType(String userId, String productId, UserBehavior.ActionType actionType);

    /**
     * Delete old behaviors before a specific date.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserBehavior ub WHERE ub.timestamp < :cutoffDate")
    void deleteOldBehaviors(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find behaviors with high interaction value.
     */
    @Query("SELECT ub FROM UserBehavior ub " +
           "WHERE ub.weight >= :minValue " +
           "ORDER BY ub.weight DESC, ub.timestamp DESC")
    List<UserBehavior> findHighValueInteractions(@Param("minValue") Double minValue, Pageable pageable);

    /**
     * Find session-based behaviors.
     */
    List<UserBehavior> findBySessionIdOrderByTimestampAsc(String sessionId);

    /**
     * Find behaviors by device type.
     */
    List<UserBehavior> findByDeviceTypeOrderByTimestampDesc(String deviceType);

    /**
     * Find behaviors by location.
     */
    // Removed: 'location' field does not exist in UserBehavior entity.
}