package com.ecommerce.recommendation.service;

import com.ecommerce.recommendation.entity.UserProfile;
import com.ecommerce.recommendation.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing user profiles and preferences.
 * Handles user segmentation, preference tracking, and profile analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);
    private final UserProfileRepository userProfileRepository;
    private final UserBehaviorService userBehaviorService;
    
    /**
     * Get user profile by user ID.
     */
    @Cacheable(value = "userProfile", key = "#userId")
    @Transactional(readOnly = true)
    public Optional<UserProfile> getUserProfile(String userId) {
        return userProfileRepository.findByUserId(userId);
    }
    
    /**
     * Create or update user profile.
     */
    @CacheEvict(value = "userProfile", key = "#userProfile.userId")
    public UserProfile saveUserProfile(UserProfile userProfile) {
        log.info("Saving user profile for user: {}", userProfile.getUserId());
        
        try {
            // Update calculated fields
            userProfile.updateEngagementMetrics(0.0, 0.0); // Default values for CTR and CR
            
            UserProfile savedProfile = userProfileRepository.save(userProfile);
            
            log.info("User profile saved successfully with ID: {}", savedProfile.getId());
            
            return savedProfile;
            
        } catch (Exception e) {
            log.error("Error saving user profile for user: {}", userProfile.getUserId(), e);
            throw new RuntimeException("Failed to save user profile", e);
        }
    }
    
    /**
     * Create a new user profile with default settings.
     */
    public UserProfile createUserProfile(String userId, String email) {
        log.info("Creating new user profile for user: {}", userId);
        
        try {
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            profile.setEmail(email);
            profile.setUserSegment(UserProfile.UserSegment.NEW_USER);
            profile.setAgeGroup(UserProfile.AgeGroup.ADULT);
            profile.setGender(UserProfile.Gender.PREFER_NOT_TO_SAY);
            profile.setPreferredCategories(new ArrayList<>());
            profile.setPreferredBrands(new ArrayList<>());
            profile.setAllowPersonalizedRecommendations(true);
            profile.setAllowEmailNotifications(true);
            profile.setAllowPushNotifications(true);
            profile.setPrivacyLevel("STANDARD");
            profile.setEngagementScore(0.0);
            profile.setLoyaltyScore(0.0);
            profile.setPurchaseScore(0.0);
            profile.setBrowsingScore(0.0);
            profile.setSocialScore(0.0);
            profile.setTotalOrders(0);
            profile.setTotalSpent(0.0);
            profile.setAverageOrderValue(0.0);
            profile.setDaysSinceLastOrder(0);
            profile.setDaysSinceRegistration(0);
            profile.setLoginFrequency(0.0);
            profile.setSessionDuration(0.0);
            profile.setPageViewsPerSession(0.0);
            profile.setBounceRate(0.0);
            profile.setConversionRate(0.0);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            
            return saveUserProfile(profile);
            
        } catch (Exception e) {
            log.error("Error creating user profile for user: {}", userId, e);
            throw new RuntimeException("Failed to create user profile", e);
        }
    }
    
    /**
     * Update user preferences.
     */
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserProfile updateUserPreferences(String userId, 
                                           List<String> preferredCategories,
                                           List<String> preferredBrands,
                                           Map<String, Object> preferences) {
        
        log.info("Updating preferences for user: {}", userId);
        
        try {
            UserProfile profile = getUserProfile(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
            
            // Update preferences
            if (preferredCategories != null) {
                profile.setPreferredCategories(preferredCategories);
            }
            
            if (preferredBrands != null) {
                profile.setPreferredBrands(preferredBrands);
            }
            
            // Update other preferences from map
            if (preferences != null) {
                updatePreferencesFromMap(profile, preferences);
            }
            
            profile.setUpdatedAt(LocalDateTime.now());
            
            return saveUserProfile(profile);
            
        } catch (Exception e) {
            log.error("Error updating preferences for user: {}", userId, e);
            throw new RuntimeException("Failed to update user preferences", e);
        }
    }
    
    /**
     * Update user demographics.
     */
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserProfile updateUserDemographics(String userId,
                                            UserProfile.Gender gender,
                                            UserProfile.AgeGroup ageGroup,
                                            String location,
                                            String timezone) {
        
        log.info("Updating demographics for user: {}", userId);
        
        try {
            UserProfile profile = getUserProfile(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
            
            if (gender != null) {
                profile.setGender(gender);
            }
            
            if (ageGroup != null) {
                profile.setAgeGroup(ageGroup);
            }
            
            if (location != null) {
                profile.setLocation(location);
            }
            
            if (timezone != null) {
                profile.setTimezone(timezone);
            }
            
            profile.setUpdatedAt(LocalDateTime.now());
            
            return saveUserProfile(profile);
            
        } catch (Exception e) {
            log.error("Error updating demographics for user: {}", userId, e);
            throw new RuntimeException("Failed to update user demographics", e);
        }
    }
    
    /**
     * Update user behavior scores based on recent activity.
     */
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserProfile updateBehaviorScores(String userId) {
        log.info("Updating behavior scores for user: {}", userId);
        
        try {
            UserProfile profile = getUserProfile(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
            
            // Calculate engagement score from behavior service
            Double engagementScore = userBehaviorService.calculateUserEngagementScore(userId);
            if (engagementScore != null) {
                profile.setEngagementScore(engagementScore);
            }
            
            // Calculate other scores based on behavior patterns
            Map<String, Object> behaviorStats = userBehaviorService.getUserBehaviorStats(userId);
            
            // Update browsing score
            Long viewCount = (Long) behaviorStats.getOrDefault("viewCount", 0L);
            Long searchCount = (Long) behaviorStats.getOrDefault("searchCount", 0L);
            profile.setBrowsingScore(calculateBrowsingScore(viewCount, searchCount));
            
            // Update purchase score
            Long purchaseCount = (Long) behaviorStats.getOrDefault("purchaseCount", 0L);
            Long cartCount = (Long) behaviorStats.getOrDefault("add_to_cartCount", 0L);
            profile.setPurchaseScore(calculatePurchaseScore(purchaseCount, cartCount));
            
            // Update social score
            Long shareCount = (Long) behaviorStats.getOrDefault("shareCount", 0L);
            Long reviewCount = (Long) behaviorStats.getOrDefault("reviewCount", 0L);
            profile.setSocialScore(calculateSocialScore(shareCount, reviewCount));
            
            // Update loyalty score based on overall activity
            profile.setLoyaltyScore(calculateLoyaltyScore(profile));
            
            profile.setUpdatedAt(LocalDateTime.now());
            
            return saveUserProfile(profile);
            
        } catch (Exception e) {
            log.error("Error updating behavior scores for user: {}", userId, e);
            throw new RuntimeException("Failed to update behavior scores", e);
        }
    }
    
    /**
     * Update user segment based on behavior and profile data.
     */
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserProfile updateUserSegment(String userId) {
        log.info("Updating user segment for user: {}", userId);
        
        try {
            UserProfile profile = getUserProfile(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));
            
            UserProfile.UserSegment newSegment = calculateUserSegment(profile);
            
            if (!newSegment.equals(profile.getUserSegment())) {
                log.info("User segment changed for user {}: {} -> {}", 
                        userId, profile.getUserSegment(), newSegment);
                profile.setUserSegment(newSegment);
                profile.setUpdatedAt(LocalDateTime.now());
                
                return saveUserProfile(profile);
            }
            
            return profile;
            
        } catch (Exception e) {
            log.error("Error updating user segment for user: {}", userId, e);
            throw new RuntimeException("Failed to update user segment", e);
        }
    }
    
    /**
     * Record user login activity.
     */
    public void recordLoginActivity(String userId) {
        try {
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                profile.updateLoginActivity();
                userProfileRepository.save(profile);
            }
            log.debug("Updated login activity for user: {}", userId);
        } catch (Exception e) {
            log.error("Error recording login activity for user: {}", userId, e);
        }
    }
    
    /**
     * Update order statistics.
     */
    @CacheEvict(value = "userProfile", key = "#userId")
    public void updateOrderStatistics(String userId, double orderValue) {
        try {
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                profile.updateOrderStats(orderValue);
                userProfileRepository.save(profile);
            }
            log.debug("Updated order statistics for user: {} with value: {}", userId, orderValue);
        } catch (Exception e) {
            log.error("Error updating order statistics for user: {}", userId, e);
        }
    }

    /**
     * Update user profile from order data.
     */
    @CacheEvict(value = "userProfile", key = "#userId")
    public void updateUserProfileFromOrder(String userId, Map<String, Object> eventData) {
        try {
            log.info("Updating user profile from order for user: {}", userId);
            
            // Extract order value from event data
            Double orderValue = null;
            if (eventData.containsKey("orderValue")) {
                orderValue = ((Number) eventData.get("orderValue")).doubleValue();
            } else if (eventData.containsKey("totalAmount")) {
                orderValue = ((Number) eventData.get("totalAmount")).doubleValue();
            }
            
            if (orderValue != null && orderValue > 0) {
                updateOrderStatistics(userId, orderValue);
                
                // Update behavior scores after order
                updateBehaviorScores(userId);
                
                // Update user segment based on new order data
                updateUserSegment(userId);
                
                log.info("Successfully updated user profile from order for user: {} with value: {}", userId, orderValue);
            } else {
                log.warn("No valid order value found in event data for user: {}", userId);
            }
            
        } catch (Exception e) {
            log.error("Error updating user profile from order for user: {}", userId, e);
        }
    }
    
    /**
     * Get users by segment.
     */
    @Transactional(readOnly = true)
    public Page<UserProfile> getUsersBySegment(UserProfile.UserSegment segment, Pageable pageable) {
        return Page.empty(pageable); // TODO: Implement pagination for user segment query
    }
    
    /**
     * Get users by age group.
     */
    @Transactional(readOnly = true)
    public Page<UserProfile> getUsersByAgeGroup(UserProfile.AgeGroup ageGroup, Pageable pageable) {
        return Page.empty(pageable); // TODO: Implement pagination for age group query
    }
    
    /**
     * Get users by location.
     */
    @Transactional(readOnly = true)
    public Page<UserProfile> getUsersByLocation(String location, Pageable pageable) {
        return Page.empty(pageable); // TODO: Implement pagination for location query
    }
    
    /**
     * Get high-value customers.
     */
    @Transactional(readOnly = true)
    public List<UserProfile> getHighValueCustomers(double minSpent, int limit) {
        return userProfileRepository.findHighValueCustomers(Pageable.ofSize(limit));
    }
    
    /**
     * Get users with high engagement.
     */
    @Transactional(readOnly = true)
    public List<UserProfile> getHighEngagementUsers(double minEngagementScore, int limit) {
        return userProfileRepository.findHighEngagementUsers(minEngagementScore, 0.0).stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * Get inactive users.
     */
    @Transactional(readOnly = true)
    public List<UserProfile> getInactiveUsers(int daysSinceLastLogin, int limit) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSinceLastLogin);
        return userProfileRepository.findDormantUsers(cutoffDate).stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * Get user profile statistics.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserProfileStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Total users
            Long totalUsers = userProfileRepository.count();
            stats.put("totalUsers", totalUsers);
            
            // Users by segment
            Map<String, Long> segmentStats = new HashMap<>();
            for (UserProfile.UserSegment segment : UserProfile.UserSegment.values()) {
                Long count = userProfileRepository.countUsersBySegment(segment);
                segmentStats.put(segment.name(), count);
            }
            stats.put("usersBySegment", segmentStats);
            
            // Users by age group
            Map<String, Long> ageGroupStats = new HashMap<>();
            for (UserProfile.AgeGroup ageGroup : UserProfile.AgeGroup.values()) {
                Long count = (long) userProfileRepository.findByAgeGroupAndIsActiveTrueOrderByUpdatedAtDesc(ageGroup).size();
                ageGroupStats.put(ageGroup.name(), count);
            }
            stats.put("usersByAgeGroup", ageGroupStats);
            
            // Average metrics
            Double avgEngagementScore = 0.0; // TODO: Implement average engagement score calculation
            stats.put("averageEngagementScore", avgEngagementScore);
            
            Double avgTotalSpent = 0.0; // TODO: Implement average total spent calculation
            stats.put("averageTotalSpent", avgTotalSpent);
            
            Double avgOrderValue = 0.0; // TODO: Implement average order value calculation
            stats.put("averageOrderValue", avgOrderValue);
            
            // Active users (logged in within last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Long activeUsers = userProfileRepository.countActiveUsers();
            stats.put("activeUsers", activeUsers);
            
        } catch (Exception e) {
            log.error("Error calculating user profile statistics", e);
            stats.put("error", "Failed to calculate statistics");
        }
        
        return stats;
    }
    
    /**
     * Bulk update user segments.
     */
    @Transactional
    public int bulkUpdateUserSegments() {
        log.info("Starting bulk update of user segments");
        
        try {
            List<UserProfile> profiles = userProfileRepository.findAll();
            int updatedCount = 0;
            
            for (UserProfile profile : profiles) {
                UserProfile.UserSegment newSegment = calculateUserSegment(profile);
                
                if (!newSegment.equals(profile.getUserSegment())) {
                    profile.setUserSegment(newSegment);
                    profile.setUpdatedAt(LocalDateTime.now());
                    userProfileRepository.save(profile);
                    updatedCount++;
                }
            }
            
            log.info("Bulk updated {} user segments", updatedCount);
            
            return updatedCount;
            
        } catch (Exception e) {
            log.error("Error during bulk update of user segments", e);
            throw new RuntimeException("Failed to bulk update user segments", e);
        }
    }
    
    // Helper methods
    
    private void updatePreferencesFromMap(UserProfile profile, Map<String, Object> preferences) {
        if (preferences.containsKey("allowPersonalizedRecommendations")) {
            profile.setAllowPersonalizedRecommendations(
                (Boolean) preferences.get("allowPersonalizedRecommendations"));
        }
        
        if (preferences.containsKey("allowEmailNotifications")) {
            profile.setAllowEmailNotifications(
                (Boolean) preferences.get("allowEmailNotifications"));
        }
        
        if (preferences.containsKey("allowPushNotifications")) {
            profile.setAllowPushNotifications(
                (Boolean) preferences.get("allowPushNotifications"));
        }
        
        if (preferences.containsKey("privacyLevel")) {
            String privacyLevelStr = (String) preferences.get("privacyLevel");
            profile.setPrivacyLevel(privacyLevelStr.toUpperCase());
        }
    }
    
    private double calculateBrowsingScore(Long viewCount, Long searchCount) {
        double score = 0.0;
        
        if (viewCount != null && viewCount > 0) {
            score += Math.min(viewCount * 0.1, 50.0); // Max 50 points from views
        }
        
        if (searchCount != null && searchCount > 0) {
            score += Math.min(searchCount * 0.2, 30.0); // Max 30 points from searches
        }
        
        return Math.min(score, 100.0); // Cap at 100
    }
    
    private double calculatePurchaseScore(Long purchaseCount, Long cartCount) {
        double score = 0.0;
        
        if (purchaseCount != null && purchaseCount > 0) {
            score += Math.min(purchaseCount * 10.0, 70.0); // Max 70 points from purchases
        }
        
        if (cartCount != null && cartCount > 0) {
            score += Math.min(cartCount * 2.0, 30.0); // Max 30 points from cart adds
        }
        
        return Math.min(score, 100.0); // Cap at 100
    }
    
    private double calculateSocialScore(Long shareCount, Long reviewCount) {
        double score = 0.0;
        
        if (shareCount != null && shareCount > 0) {
            score += Math.min(shareCount * 5.0, 40.0); // Max 40 points from shares
        }
        
        if (reviewCount != null && reviewCount > 0) {
            score += Math.min(reviewCount * 10.0, 60.0); // Max 60 points from reviews
        }
        
        return Math.min(score, 100.0); // Cap at 100
    }
    
    private double calculateLoyaltyScore(UserProfile profile) {
        double score = 0.0;
        
        // Based on total orders
        if (profile.getTotalOrders() != null && profile.getTotalOrders() > 0) {
            score += Math.min(profile.getTotalOrders() * 5.0, 40.0);
        }
        
        // Based on total spent
        if (profile.getTotalSpent() != null && profile.getTotalSpent() > 0) {
            score += Math.min(profile.getTotalSpent() / 100.0, 30.0); // $100 = 1 point, max 30
        }
        
        // Based on engagement score
        if (profile.getEngagementScore() != null) {
            score += profile.getEngagementScore() * 0.3; // Max 30 points
        }
        
        return Math.min(score, 100.0); // Cap at 100
    }
    
    private UserProfile.UserSegment calculateUserSegment(UserProfile profile) {
        // New user (less than 30 days, no orders)
        if (profile.getDaysSinceRegistration() != null && 
            profile.getDaysSinceRegistration() < 30 && 
            (profile.getTotalOrders() == null || profile.getTotalOrders() == 0)) {
            return UserProfile.UserSegment.NEW_USER;
        }
        
        // VIP customer (high spending)
        if (profile.getTotalSpent() != null && profile.getTotalSpent() > 1000.0) {
            return UserProfile.UserSegment.VIP_CUSTOMER;
        }
        
        // Bargain hunter (high engagement with discounted items)
        if (profile.getEngagementScore() != null && profile.getEngagementScore() > 70.0 &&
            profile.getAverageOrderValue() != null && profile.getAverageOrderValue() < 50.0) {
            return UserProfile.UserSegment.BARGAIN_HUNTER;
        }
        
        // Brand loyalist (consistent brand preferences)
        if (profile.getPreferredBrands() != null && profile.getPreferredBrands().size() <= 3 &&
            profile.getTotalOrders() != null && profile.getTotalOrders() > 5) {
            return UserProfile.UserSegment.BRAND_LOYALIST;
        }
        
        // Occasional buyer (some orders but low frequency)
        if (profile.getTotalOrders() != null && profile.getTotalOrders() > 0 &&
            profile.getDaysSinceLastOrder() > 90) {
            return UserProfile.UserSegment.CASUAL_SHOPPER;
        }
        
        // Regular customer (default for active users)
        if (profile.getTotalOrders() != null && profile.getTotalOrders() > 0) {
            return UserProfile.UserSegment.REGULAR_CUSTOMER;
        }
        
        // Browser (no purchases but active)
        if (profile.getEngagementScore() != null && profile.getEngagementScore() > 30.0) {
            return UserProfile.UserSegment.NEW_USER;
        }
        
        // Default to new user
        return UserProfile.UserSegment.NEW_USER;
    }
}