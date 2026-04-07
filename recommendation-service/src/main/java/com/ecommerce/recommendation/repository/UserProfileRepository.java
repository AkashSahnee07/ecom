package com.ecommerce.recommendation.repository;

import com.ecommerce.recommendation.entity.UserProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserProfile entity.
 * Provides data access methods for user profile management and analysis.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, java.util.UUID> {

    /**
     * Find user profile by user ID.
     */
    Optional<UserProfile> findByUserId(String userId);

    /**
     * Find user profile by email.
     */
    Optional<UserProfile> findByEmail(String email);

    /**
     * Find active user profiles.
     */
    List<UserProfile> findByIsActiveTrueOrderByUpdatedAtDesc();

    /**
     * Find users by segment.
     */
    List<UserProfile> findByUserSegmentAndIsActiveTrueOrderByTotalSpentDesc(UserProfile.UserSegment userSegment);

    /**
     * Find users by age group.
     */
    List<UserProfile> findByAgeGroupAndIsActiveTrueOrderByUpdatedAtDesc(UserProfile.AgeGroup ageGroup);

    /**
     * Find users by location.
     */
    List<UserProfile> findByLocationAndIsActiveTrueOrderByUpdatedAtDesc(String location);

    /**
     * Find users by country.
     */
    List<UserProfile> findByCountryAndIsActiveTrueOrderByUpdatedAtDesc(String country);

    /**
     * Find premium users.
     */
    List<UserProfile> findByIsPremiumTrueAndIsActiveTrueOrderByLifetimeValueDesc();

    /**
     * Find high-value customers (VIP and regular customers).
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.userSegment IN ('VIP_CUSTOMER', 'REGULAR_CUSTOMER') ORDER BY up.lifetimeValue DESC")
    List<UserProfile> findHighValueCustomers(Pageable pageable);

    /**
     * Find new users (registered recently or marked as new).
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND (up.userSegment = 'NEW_USER' OR up.registrationDate >= :since)")
    List<UserProfile> findNewUsers(@Param("since") LocalDateTime since);

    /**
     * Find dormant users (haven't logged in recently).
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND (up.lastLoginDate IS NULL OR up.lastLoginDate < :threshold)")
    List<UserProfile> findDormantUsers(@Param("threshold") LocalDateTime threshold);

    /**
     * Find active users (logged in recently).
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.lastLoginDate >= :since")
    List<UserProfile> findActiveUsers(@Param("since") LocalDateTime since);

    /**
     * Find users by spending range.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.totalSpent BETWEEN :minSpent AND :maxSpent")
    List<UserProfile> findUsersBySpendingRange(@Param("minSpent") Double minSpent, @Param("maxSpent") Double maxSpent);

    /**
     * Find users by order count range.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.totalOrders BETWEEN :minOrders AND :maxOrders")
    List<UserProfile> findUsersByOrderRange(@Param("minOrders") Integer minOrders, @Param("maxOrders") Integer maxOrders);

    /**
     * Find users with high engagement.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.clickThroughRate >= :minCTR AND up.conversionRate >= :minCR")
    List<UserProfile> findHighEngagementUsers(@Param("minCTR") Double minCTR, @Param("minCR") Double minCR);

    /**
     * Find users with good recommendation feedback.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.recommendationFeedbackScore >= :minScore")
    List<UserProfile> findUsersWithGoodFeedback(@Param("minScore") Double minScore);

    /**
     * Find users who consent to marketing.
     */
    List<UserProfile> findByMarketingConsentTrueAndIsActiveTrueOrderByUpdatedAtDesc();

    /**
     * Find users with personalization enabled.
     */
    List<UserProfile> findByPersonalizationEnabledTrueAndIsActiveTrueOrderByUpdatedAtDesc();

    /**
     * Find users by preferred categories.
     */
    @Query("SELECT up FROM UserProfile up JOIN up.preferredCategories pc WHERE up.isActive = true AND pc = :categoryId")
    List<UserProfile> findUsersByPreferredCategory(@Param("categoryId") String categoryId);

    /**
     * Find users by preferred brands.
     */
    @Query("SELECT up FROM UserProfile up JOIN up.preferredBrands pb WHERE up.isActive = true AND pb = :brandId")
    List<UserProfile> findUsersByPreferredBrand(@Param("brandId") String brandId);

    /**
     * Find similar users based on demographics.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.userId != :userId AND up.ageGroup = :ageGroup AND up.location = :location AND up.userSegment = :userSegment")
    List<UserProfile> findSimilarUsers(@Param("userId") String userId, @Param("ageGroup") UserProfile.AgeGroup ageGroup, @Param("location") String location, @Param("userSegment") UserProfile.UserSegment userSegment);

    /**
     * Get user segment statistics.
     */
    /**
     * Get user segment statistics.
     * Note: This method should be implemented in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getUserSegmentStatistics();

    /**
     * Get age group statistics.
     */
    /**
     * Get age group statistics.
     * Note: This method should be implemented in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getAgeGroupStatistics();

    /**
     * Get location-based statistics.
     */
    /**
     * Get location statistics.
     * Note: This method should be implemented in service layer using MongoTemplate aggregation
     */
    // List<Object[]> getLocationStatistics();

    /**
     * Find users needing profile update.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.updatedAt < :threshold")
    List<UserProfile> findProfilesNeedingUpdate(@Param("threshold") LocalDateTime threshold);

    /**
     * Update user login activity.
     */
    /**
     * Update last login date.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void updateLastLoginDate(String userId);

    /**
     * Update user order statistics.
     */
    /**
     * Update order statistics.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void updateOrderStatistics(String userId, Double orderValue);

    /**
     * Update user engagement metrics.
     */
    /**
     * Update engagement metrics.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void updateEngagementMetrics(String userId, Double ctr, Double cr);

    /**
     * Update recommendation feedback score.
     */
    /**
     * Update recommendation feedback.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void updateRecommendationFeedback(String userId, Double score);

    /**
     * Update user segment.
     */
    /**
     * Update user segment.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void updateUserSegment(String userId, UserProfile.UserSegment segment);

    /**
     * Deactivate user profile.
     */
    /**
     * Deactivate user profile.
     * Note: This method should be implemented in service layer using MongoTemplate
     */
    // void deactivateUserProfile(String userId);

    /**
     * Count users by segment.
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.userSegment = :segment AND up.isActive = true")
    Long countUsersBySegment(@Param("segment") UserProfile.UserSegment segment);

    /**
     * Count active users.
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.isActive = true")
    Long countActiveUsers();

    /**
     * Find users with recent purchases.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.lastOrderDate >= :since")
    List<UserProfile> findUsersWithRecentPurchases(@Param("since") LocalDateTime since);

    /**
     * Find users by lifetime value range.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.lifetimeValue >= :minValue AND up.lifetimeValue <= :maxValue")
    List<UserProfile> findUsersByLifetimeValueRange(@Param("minValue") Double minValue, @Param("maxValue") Double maxValue);

    /**
     * Find users by device preference.
     */
    List<UserProfile> findByDevicePreferenceAndIsActiveTrueOrderByUpdatedAtDesc(String devicePreference);

    /**
     * Find users by shopping frequency.
     */
    List<UserProfile> findByShoppingFrequencyAndIsActiveTrueOrderByUpdatedAtDesc(String shoppingFrequency);

    /**
     * Check if user exists and is active.
     */
    boolean existsByUserIdAndIsActiveTrue(String userId);

    /**
     * Find users for targeted campaigns.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.marketingConsent = true AND up.userSegment IN :targetSegments")
    List<UserProfile> findUsersForTargetedCampaigns(@Param("targetSegments") List<UserProfile.UserSegment> targetSegments);

    /**
     * Find users needing re-engagement.
     */
    @Query("SELECT up FROM UserProfile up WHERE up.isActive = true AND up.lastLoginDate < :threshold AND up.totalOrders > 0")
    List<UserProfile> findUsersNeedingReengagement(@Param("threshold") LocalDateTime threshold);
}
