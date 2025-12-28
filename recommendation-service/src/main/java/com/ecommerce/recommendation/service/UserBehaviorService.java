package com.ecommerce.recommendation.service;

import com.ecommerce.recommendation.entity.UserBehavior;
import com.ecommerce.recommendation.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing user behavior data and analytics.
 * Tracks user interactions with products and provides insights for recommendations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBehaviorService {

    private static final Logger log = LoggerFactory.getLogger(UserBehaviorService.class);
    private final UserBehaviorRepository userBehaviorRepository;
    
    /**
     * Record a user behavior event.
     */
    public UserBehavior recordBehavior(String userId, String productId, 
                                     UserBehavior.ActionType actionType, 
                                     Map<String, Object> metadata) {
        
        log.debug("Recording behavior: user={}, product={}, action={}", 
                 userId, productId, actionType);
        
        try {
            UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .productId(productId)
                .actionType(actionType)
                .timestamp(LocalDateTime.now())
                .sessionId(generateSessionId(userId))
                .deviceType(extractDeviceType(metadata))
                .durationSeconds(extractDuration(metadata))
                .quantity(extractQuantity(metadata))
                .price(extractPrice(metadata))
                .categoryId(extractCategoryId(metadata))
                .brandId(extractBrandId(metadata))
                .searchQuery(extractSearchQuery(metadata))
                .referrer(extractReferrerUrl(metadata))
                .pageUrl(extractPageUrl(metadata))
                .userAgent(extractUserAgent(metadata))
                .ipAddress(extractIpAddress(metadata))
                .build();
            
            // Set interaction score based on action type
            behavior.calculateInteractionScore();
            
            UserBehavior savedBehavior = userBehaviorRepository.save(behavior);
            
            log.info("Recorded behavior: id={}, user={}, product={}, action={}, score={}", 
                    savedBehavior.getId(), userId, productId, actionType, 
                    savedBehavior.getInteractionScore());
            
            return savedBehavior;
            
        } catch (Exception e) {
            log.error("Error recording behavior for user: {} and product: {}", userId, productId, e);
            throw new RuntimeException("Failed to record user behavior", e);
        }
    }
    
    /**
     * Get user behaviors by user ID with pagination.
     */
    public Page<UserBehavior> getUserBehaviors(String userId, Pageable pageable) {
        return userBehaviorRepository.findByUserId(userId, pageable);
    }
    
    /**
     * Get user behaviors for a specific product.
     */
    public List<UserBehavior> getUserBehaviorsByProduct(String userId, String productId) {
        return userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId).stream()
            .filter(behavior -> productId.equals(behavior.getProductId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get user behaviors by action type.
     */
    public List<UserBehavior> getUserBehaviorsByAction(String userId, UserBehavior.ActionType actionType) {
        return userBehaviorRepository.findByUserIdAndActionTypeOrderByTimestampDesc(userId, actionType);
    }
    
    /**
     * Get user behaviors within a date range.
     */
    public List<UserBehavior> getUserBehaviorsInDateRange(String userId, 
                                                        LocalDateTime startDate, 
                                                        LocalDateTime endDate) {
        return userBehaviorRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
                userId, startDate, endDate);
    }
    
    /**
     * Get user behaviors by category.
     */
    public List<UserBehavior> getUserBehaviorByCategory(String userId, String categoryId) {
        return userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId).stream()
            .filter(behavior -> categoryId.equals(behavior.getProductCategory()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get recently viewed products by user.
     */
    @Cacheable(value = "recentlyViewed", key = "#userId")
    public List<String> getRecentlyViewedProducts(String userId, int limit) {
        List<UserBehavior> viewBehaviors = userBehaviorRepository
                .findByUserIdAndActionTypeOrderByTimestampDesc(userId, UserBehavior.ActionType.VIEW);
        
        return viewBehaviors.stream()
                .map(UserBehavior::getProductId)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get frequently bought together products.
     */
    @Cacheable(value = "frequentlyBoughtTogether", key = "#productId + '_' + #daysBack")
    public List<String> getFrequentlyBoughtTogether(String productId, int daysBack) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        // Implementation using existing methods - simplified version
        return userBehaviorRepository.findByProductIdOrderByTimestampDesc(productId).stream()
            .filter(behavior -> behavior.getTimestamp().isAfter(since))
            .filter(behavior -> UserBehavior.ActionType.PURCHASE.equals(behavior.getActionType()))
            .map(UserBehavior::getProductId)
            .distinct()
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * Get abandoned cart products for a user.
     */
    public List<String> getAbandonedCartProducts(String userId) {
        return userBehaviorRepository.findAbandonedCartProducts(userId);
    }
    
    /**
     * Get user's preferred categories based on behavior.
     */
    @Cacheable(value = "userPreferredCategories", key = "#userId")
    public List<String> getUserPreferredCategories(String userId, int limit) {
        return userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId).stream()
            .map(UserBehavior::getProductCategory)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(category -> category, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Get user's preferred brands based on behavior.
     */
    @Cacheable(value = "userPreferredBrands", key = "#userId")
    public List<String> getUserPreferredBrands(String userId, int limit) {
        return userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId).stream()
            .filter(behavior -> behavior.getBrandId() != null)
            .collect(Collectors.groupingBy(behavior -> behavior.getBrandId(), Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate user engagement score.
     */
    @Cacheable(value = "userEngagementScore", key = "#userId")
    public Double calculateUserEngagementScore(String userId) {
        List<UserBehavior> behaviors = userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId);
        return behaviors.stream()
            .mapToDouble(behavior -> behavior.getActionType().getDefaultWeight())
            .sum();
    }
    
    /**
     * Get user behavior statistics.
     */
    public Map<String, Object> getUserBehaviorStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Total interactions
            Long totalInteractions = (long) userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId).size();
            stats.put("totalInteractions", totalInteractions);
            
            // Interactions by action type
            for (UserBehavior.ActionType actionType : UserBehavior.ActionType.values()) {
                Long count = userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId)
                    .stream()
                    .filter(behavior -> behavior.getActionType() == actionType)
                    .count();
                stats.put(actionType.name().toLowerCase() + "Count", count);
            }
            
            // Recent activity (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Long recentInteractions = userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId)
                    .stream()
                    .filter(behavior -> behavior.getTimestamp().isAfter(thirtyDaysAgo))
                    .count();
            stats.put("recentInteractions", recentInteractions);
            
            // Engagement score
            Double engagementScore = calculateUserEngagementScore(userId);
            stats.put("engagementScore", engagementScore);
            
            // Most active day of week
            String mostActiveDay = userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId)
                    .stream()
                    .collect(Collectors.groupingBy(
                        behavior -> behavior.getTimestamp().getDayOfWeek().toString(),
                        Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("UNKNOWN");
            stats.put("mostActiveDay", mostActiveDay);
            
            // Average session duration
            Double avgSessionDuration = userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId)
                    .stream()
                    .filter(behavior -> behavior.getDuration() != null)
                    .mapToInt(behavior -> behavior.getDuration())
                    .average()
                    .orElse(0.0);
            stats.put("averageSessionDuration", avgSessionDuration);
            
            // Preferred categories and brands
            stats.put("preferredCategories", getUserPreferredCategories(userId, 5));
            stats.put("preferredBrands", getUserPreferredBrands(userId, 5));
            
        } catch (Exception e) {
            log.error("Error calculating user behavior stats for user: {}", userId, e);
            stats.put("error", "Failed to calculate statistics");
        }
        
        return stats;
    }
    
    /**
     * Get product interaction statistics.
     */
    public Map<String, Object> getProductInteractionStats(String productId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<UserBehavior> productBehaviors = userBehaviorRepository.findByProductIdOrderByTimestampDesc(productId);
            
            // Total interactions
            Long totalInteractions = (long) productBehaviors.size();
            stats.put("totalInteractions", totalInteractions);
            
            // Unique users
            Long uniqueUsers = productBehaviors.stream()
                    .map(UserBehavior::getUserId)
                    .distinct()
                    .count();
            stats.put("uniqueUsers", uniqueUsers);
            
            // Interactions by action type
            for (UserBehavior.ActionType actionType : UserBehavior.ActionType.values()) {
                Long count = productBehaviors.stream()
                        .filter(behavior -> behavior.getActionType() == actionType)
                        .count();
                stats.put(actionType.name().toLowerCase() + "Count", count);
            }
            
            // Conversion rate (purchases / views)
            Long views = productBehaviors.stream()
                    .filter(behavior -> behavior.getActionType() == UserBehavior.ActionType.VIEW)
                    .count();
            Long purchases = productBehaviors.stream()
                    .filter(behavior -> behavior.getActionType() == UserBehavior.ActionType.PURCHASE)
                    .count();
            
            if (views > 0) {
                double conversionRate = (purchases.doubleValue() / views.doubleValue()) * 100;
                stats.put("conversionRate", conversionRate);
            } else {
                stats.put("conversionRate", 0.0);
            }
            
            // Average interaction score
            Double avgInteractionScore = productBehaviors.stream()
                    .mapToDouble(behavior -> behavior.getCalculatedWeight())
                    .average()
                    .orElse(0.0);
            stats.put("averageInteractionScore", avgInteractionScore);
            
        } catch (Exception e) {
            log.error("Error calculating product interaction stats for product: {}", productId, e);
            stats.put("error", "Failed to calculate statistics");
        }
        
        return stats;
    }
    
    /**
     * Get trending products based on recent behavior.
     */
    @Cacheable(value = "trendingProducts", key = "#daysBack + '_' + #limit")
    public List<String> getTrendingProducts(int daysBack, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return userBehaviorRepository.findAll().stream()
                .filter(behavior -> behavior.getTimestamp().isAfter(since))
                .collect(Collectors.groupingBy(
                    UserBehavior::getProductId,
                    Collectors.summingDouble(UserBehavior::getCalculatedWeight)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Get similar users based on behavior patterns.
     */
    @Cacheable(value = "similarUsers", key = "#userId + '_' + #limit")
    public List<String> findSimilarUsers(String userId, int limit) {
        List<UserBehavior> userBehaviors = userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId);
        Set<String> userProducts = userBehaviors.stream()
                .map(UserBehavior::getProductId)
                .collect(Collectors.toSet());
        
        return userBehaviorRepository.findAll().stream()
                .filter(behavior -> !behavior.getUserId().equals(userId))
                .collect(Collectors.groupingBy(UserBehavior::getUserId))
                .entrySet().stream()
                .map(entry -> {
                    Set<String> otherUserProducts = entry.getValue().stream()
                            .map(UserBehavior::getProductId)
                            .collect(Collectors.toSet());
                    long commonProducts = userProducts.stream()
                            .mapToLong(product -> otherUserProducts.contains(product) ? 1 : 0)
                            .sum();
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), commonProducts);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Clean up old behavior data.
     */
    public int cleanupOldBehaviorData(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        
        log.info("Cleaning up behavior data older than: {}", cutoffDate);
        
        try {
            List<UserBehavior> oldBehaviors = userBehaviorRepository.findAll().stream()
                    .filter(behavior -> behavior.getTimestamp().isBefore(cutoffDate))
                    .collect(Collectors.toList());
            
            oldBehaviors.forEach(behavior -> userBehaviorRepository.delete(behavior));
            int deletedCount = oldBehaviors.size();
            
            log.info("Cleaned up {} old behavior records", deletedCount);
            
            return deletedCount;
            
        } catch (Exception e) {
            log.error("Error cleaning up old behavior data", e);
            throw new RuntimeException("Failed to cleanup old behavior data", e);
        }
    }
    
    /**
     * Batch record multiple behaviors.
     */
    public List<UserBehavior> recordBehaviors(List<UserBehavior> behaviors) {
        log.info("Recording {} behaviors in batch", behaviors.size());
        
        try {
            // Calculate interaction scores
            behaviors.forEach(UserBehavior::calculateInteractionScore);
            
            List<UserBehavior> savedBehaviors = userBehaviorRepository.saveAll(behaviors);
            
            log.info("Successfully recorded {} behaviors", savedBehaviors.size());
            
            return savedBehaviors;
            
        } catch (Exception e) {
            log.error("Error recording behaviors in batch", e);
            throw new RuntimeException("Failed to record behaviors", e);
        }
    }
    
    // Helper methods for extracting metadata
    
    private String generateSessionId(String userId) {
        return userId + "_" + System.currentTimeMillis();
    }
    
    private String extractDeviceType(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.get("deviceType") : null;
    }
    
    private String extractSource(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.get("source") : null;
    }
    
    private Integer extractDuration(Map<String, Object> metadata) {
        if (metadata != null && metadata.containsKey("duration")) {
            Object duration = metadata.get("duration");
            if (duration instanceof Number) {
                return ((Number) duration).intValue();
            }
        }
        return null;
    }
    
    private Integer extractQuantity(Map<String, Object> metadata) {
        if (metadata != null && metadata.containsKey("quantity")) {
            Object quantity = metadata.get("quantity");
            if (quantity instanceof Number) {
                return ((Number) quantity).intValue();
            }
        }
        return 1; // Default quantity
    }
    
    private Double extractPrice(Map<String, Object> metadata) {
        if (metadata != null && metadata.containsKey("price")) {
            Object price = metadata.get("price");
            if (price instanceof Number) {
                return ((Number) price).doubleValue();
            }
        }
        return null;
    }
    
    private String extractCategoryId(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.get("categoryId") : null;
    }
    
    private String extractBrandId(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.get("brandId") : null;
    }
    
    private String extractSearchQuery(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.get("searchQuery") : null;
    }
    
    private String extractReferrerUrl(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.get("referrerUrl") : null;
    }
    
    private String extractPageUrl(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.get("pageUrl") : null;
    }
    
    private String extractUserAgent(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.get("userAgent") : null;
    }
    
    private String extractIpAddress(Map<String, Object> metadata) {
        return metadata != null ? (String) metadata.get("ipAddress") : null;
    }
}