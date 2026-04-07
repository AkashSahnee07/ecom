package com.ecommerce.recommendation.service;

import com.ecommerce.recommendation.entity.ProductRecommendation;
import com.ecommerce.recommendation.entity.UserBehavior;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementing collaborative filtering algorithms for product recommendations.
 * Uses user-based and item-based collaborative filtering techniques.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CollaborativeFilteringService {

    private static final Logger log = LoggerFactory.getLogger(CollaborativeFilteringService.class);
    private final UserBehaviorService userBehaviorService;
    private final UserProfileService userProfileService;


    
    // Minimum thresholds for collaborative filtering
    private static final int MIN_SIMILAR_USERS = 5;
    private static final int MIN_COMMON_ITEMS = 3;
    private static final double MIN_SIMILARITY_THRESHOLD = 0.3;
    private static final int MIN_USER_INTERACTIONS = 5;
    
    /**
     * Generate recommendations using collaborative filtering.
     */
    @Cacheable(value = "collaborativeRecommendations", key = "#userId + '_' + #maxRecommendations")
    public List<ProductRecommendation> generateRecommendations(String userId, int maxRecommendations) {
        log.info("Generating collaborative filtering recommendations for user: {}", userId);
        
        try {
            // Check if user has enough interaction data
            if (!hasEnoughInteractionData(userId)) {
                log.info("User {} has insufficient interaction data for collaborative filtering", userId);
                return Collections.emptyList();
            }
            
            // Get user-based recommendations (60% weight)
            List<ProductRecommendation> userBasedRecs = 
                generateUserBasedRecommendations(userId, maxRecommendations);
            
            // Get item-based recommendations (40% weight)
            List<ProductRecommendation> itemBasedRecs = 
                generateItemBasedRecommendations(userId, maxRecommendations);
            
            // Combine and rank recommendations
            List<ProductRecommendation> combinedRecs = 
                combineRecommendations(userBasedRecs, itemBasedRecs, 0.6, 0.4);
            
            // Apply final ranking and filtering
            List<ProductRecommendation> finalRecs = combinedRecs.stream()
                .sorted((ProductRecommendation r1, ProductRecommendation r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .limit(maxRecommendations)
                .collect(Collectors.toList());
            
            // Set rank positions
            for (int i = 0; i < finalRecs.size(); i++) {
                finalRecs.get(i).setRankPosition(i + 1);
            }
            
            log.info("Generated {} collaborative filtering recommendations for user: {}", 
                    finalRecs.size(), userId);
            
            return finalRecs;
            
        } catch (Exception e) {
            log.error("Error generating collaborative filtering recommendations for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Generate user-based collaborative filtering recommendations.
     */
    private List<ProductRecommendation> generateUserBasedRecommendations(String userId, int maxRecommendations) {
        log.debug("Generating user-based recommendations for user: {}", userId);
        
        try {
            // Find similar users
            List<String> similarUsers = findSimilarUsers(userId, MIN_SIMILAR_USERS * 2);
            
            if (similarUsers.size() < MIN_SIMILAR_USERS) {
                log.debug("Not enough similar users found for user: {}", userId);
                return Collections.emptyList();
            }
            
            // Get user's interaction history
            Set<String> userInteractedProducts = getUserInteractedProducts(userId);
            
            // Collect recommendations from similar users
            Map<String, Double> productScores = new HashMap<>();
            Map<String, Integer> productCounts = new HashMap<>();
            
            for (String similarUserId : similarUsers) {
                double userSimilarity = calculateUserSimilarity(userId, similarUserId);
                
                if (userSimilarity < MIN_SIMILARITY_THRESHOLD) {
                    continue;
                }
                
                // Get products liked by similar user
                List<String> likedProducts = getUserLikedProducts(similarUserId);
                
                for (String productId : likedProducts) {
                    // Skip products user has already interacted with
                    if (!userInteractedProducts.contains(productId)) {
                        double score = userSimilarity * getProductPreferenceScore(similarUserId, productId);
                        
                        productScores.merge(productId, score, Double::sum);
                        productCounts.merge(productId, 1, Integer::sum);
                    }
                }
            }
            
            // Create recommendations
            List<ProductRecommendation> recommendations = productScores.entrySet().stream()
                .filter(entry -> productCounts.get(entry.getKey()) >= 2) // At least 2 similar users liked it
                .map(entry -> {
                    String productId = entry.getKey();
                    double totalScore = entry.getValue();
                    int count = productCounts.get(productId);
                    double avgScore = totalScore / count;
                    
                    return ProductRecommendation.builder()
                        .userId(userId)
                        .recommendedProductId(productId)
                        .algorithmType(ProductRecommendation.AlgorithmType.COLLABORATIVE_FILTERING)
                        .score(avgScore)
                        .confidence(calculateConfidence(count, similarUsers.size()))
                        .reason(String.format("Liked by %d similar users", count))
                        .createdAt(LocalDateTime.now())
                        .isActive(true)
                        .build();
                })
                .sorted((ProductRecommendation r1, ProductRecommendation r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .limit(maxRecommendations)
                .collect(Collectors.toList());
            
            log.debug("Generated {} user-based recommendations for user: {}", 
                     recommendations.size(), userId);
            
            return recommendations;
            
        } catch (Exception e) {
            log.error("Error generating user-based recommendations for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Generate item-based collaborative filtering recommendations.
     */
    private List<ProductRecommendation> generateItemBasedRecommendations(String userId, int maxRecommendations) {
        log.debug("Generating item-based recommendations for user: {}", userId);
        
        try {
            // Get user's highly rated/liked products
            List<String> userLikedProducts = getUserLikedProducts(userId);
            
            if (userLikedProducts.isEmpty()) {
                log.debug("No liked products found for user: {}", userId);
                return Collections.emptyList();
            }
            
            // Get user's interaction history to exclude
            Set<String> userInteractedProducts = getUserInteractedProducts(userId);
            
            Map<String, Double> productScores = new HashMap<>();
            Map<String, Integer> productCounts = new HashMap<>();
            
            // For each liked product, find similar products
            for (String likedProductId : userLikedProducts) {
                List<String> similarProducts = findSimilarProducts(likedProductId, 20);
                double userPreference = getProductPreferenceScore(userId, likedProductId);
                
                for (String similarProductId : similarProducts) {
                    // Skip products user has already interacted with
                    if (!userInteractedProducts.contains(similarProductId)) {
                        double itemSimilarity = calculateItemSimilarity(likedProductId, similarProductId);
                        
                        if (itemSimilarity >= MIN_SIMILARITY_THRESHOLD) {
                            double score = userPreference * itemSimilarity;
                            
                            productScores.merge(similarProductId, score, Double::sum);
                            productCounts.merge(similarProductId, 1, Integer::sum);
                        }
                    }
                }
            }
            
            // Create recommendations
            List<ProductRecommendation> recommendations = productScores.entrySet().stream()
                .filter(entry -> productCounts.get(entry.getKey()) >= 2) // Similar to at least 2 liked products
                .map(entry -> {
                    String productId = entry.getKey();
                    double totalScore = entry.getValue();
                    int count = productCounts.get(productId);
                    double avgScore = totalScore / count;
                    
                    return ProductRecommendation.builder()
                        .userId(userId)
                        .recommendedProductId(productId)
                        .algorithmType(ProductRecommendation.AlgorithmType.COLLABORATIVE_FILTERING)
                        .score(avgScore)
                        .confidence(calculateConfidence(count, userLikedProducts.size()))
                        .reason(String.format("Similar to %d products you liked", count))
                        .createdAt(LocalDateTime.now())
                        .isActive(true)
                        .build();
                })
                .sorted((ProductRecommendation r1, ProductRecommendation r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .limit(maxRecommendations)
                .collect(Collectors.toList());
            
            log.debug("Generated {} item-based recommendations for user: {}", 
                     recommendations.size(), userId);
            
            return recommendations;
            
        } catch (Exception e) {
            log.error("Error generating item-based recommendations for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Find users similar to the given user.
     */
    @Cacheable(value = "similarUsers", key = "#userId + '_' + #limit")
    private List<String> findSimilarUsers(String userId, int limit) {
        try {
            // Use the user behavior service to find similar users
            List<String> similarUsers = userBehaviorService.findSimilarUsers(userId, limit);
            
            // Filter users with sufficient interaction data
            return similarUsers.stream()
                .filter(this::hasEnoughInteractionData)
                .limit(limit)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error finding similar users for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Find products similar to the given product.
     */
    @Cacheable(value = "similarProductsForCF", key = "#productId + '_' + #limit")
    private List<String> findSimilarProducts(String productId, int limit) {
        try {
            // This would typically use the ProductSimilarityService
            // For now, we'll use a simplified approach based on user behavior patterns
            
            // Find users who interacted with this product
            List<String> usersWhoLikedProduct = getUsersWhoLikedProduct(productId);
            
            if (usersWhoLikedProduct.size() < MIN_COMMON_ITEMS) {
                return Collections.emptyList();
            }
            
            // Find other products these users liked
            Map<String, Integer> productCounts = new HashMap<>();
            
            for (String userId : usersWhoLikedProduct) {
                List<String> userLikedProducts = getUserLikedProducts(userId);
                
                for (String otherProductId : userLikedProducts) {
                    if (!otherProductId.equals(productId)) {
                        productCounts.merge(otherProductId, 1, Integer::sum);
                    }
                }
            }
            
            // Return products liked by multiple users who also liked the source product
            return productCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= MIN_COMMON_ITEMS)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error finding similar products for product: {}", productId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Calculate similarity between two users based on their behavior.
     */
    private double calculateUserSimilarity(String userId1, String userId2) {
        try {
            // Get interaction data for both users
            Set<String> user1Products = getUserInteractedProducts(userId1);
            Set<String> user2Products = getUserInteractedProducts(userId2);
            
            // Calculate Jaccard similarity
            Set<String> intersection = new HashSet<>(user1Products);
            intersection.retainAll(user2Products);
            
            Set<String> union = new HashSet<>(user1Products);
            union.addAll(user2Products);
            
            if (union.isEmpty()) {
                return 0.0;
            }
            
            double jaccardSimilarity = (double) intersection.size() / union.size();
            
            // Enhance with preference correlation for common products
            double preferenceCorrelation = calculatePreferenceCorrelation(userId1, userId2, intersection);
            
            // Combine Jaccard similarity with preference correlation
            return (jaccardSimilarity * 0.6) + (preferenceCorrelation * 0.4);
            
        } catch (Exception e) {
            log.error("Error calculating user similarity between {} and {}", userId1, userId2, e);
            return 0.0;
        }
    }
    
    /**
     * Calculate similarity between two items based on user interactions.
     */
    private double calculateItemSimilarity(String productId1, String productId2) {
        try {
            // Get users who interacted with each product
            List<String> product1Users = getUsersWhoLikedProduct(productId1);
            List<String> product2Users = getUsersWhoLikedProduct(productId2);
            
            // Calculate Jaccard similarity
            Set<String> intersection = new HashSet<>(product1Users);
            intersection.retainAll(product2Users);
            
            Set<String> union = new HashSet<>(product1Users);
            union.addAll(product2Users);
            
            if (union.isEmpty()) {
                return 0.0;
            }
            
            return (double) intersection.size() / union.size();
            
        } catch (Exception e) {
            log.error("Error calculating item similarity between {} and {}", productId1, productId2, e);
            return 0.0;
        }
    }
    
    /**
     * Calculate preference correlation between two users for common products.
     */
    private double calculatePreferenceCorrelation(String userId1, String userId2, Set<String> commonProducts) {
        if (commonProducts.size() < 2) {
            return 0.5; // Neutral correlation for insufficient data
        }
        
        try {
            List<Double> user1Scores = new ArrayList<>();
            List<Double> user2Scores = new ArrayList<>();
            
            for (String productId : commonProducts) {
                double score1 = getProductPreferenceScore(userId1, productId);
                double score2 = getProductPreferenceScore(userId2, productId);
                
                user1Scores.add(score1);
                user2Scores.add(score2);
            }
            
            // Calculate Pearson correlation coefficient
            return calculatePearsonCorrelation(user1Scores, user2Scores);
            
        } catch (Exception e) {
            log.error("Error calculating preference correlation between {} and {}", userId1, userId2, e);
            return 0.5;
        }
    }
    
    /**
     * Calculate Pearson correlation coefficient.
     */
    private double calculatePearsonCorrelation(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.size() < 2) {
            return 0.0;
        }
        
        int n = x.size();
        double sumX = x.stream().mapToDouble(Double::doubleValue).sum();
        double sumY = y.stream().mapToDouble(Double::doubleValue).sum();
        double sumXY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;
        
        for (int i = 0; i < n; i++) {
            double xi = x.get(i);
            double yi = y.get(i);
            
            sumXY += xi * yi;
            sumX2 += xi * xi;
            sumY2 += yi * yi;
        }
        
        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        
        if (denominator == 0) {
            return 0.0;
        }
        
        return numerator / denominator;
    }
    
    /**
     * Combine user-based and item-based recommendations.
     */
    private List<ProductRecommendation> combineRecommendations(
            List<ProductRecommendation> userBasedRecs,
            List<ProductRecommendation> itemBasedRecs,
            double userWeight,
            double itemWeight) {
        
        Map<String, ProductRecommendation> combinedRecs = new HashMap<>();
        
        // Add user-based recommendations with weight
        for (ProductRecommendation rec : userBasedRecs) {
            rec.setScore(rec.getScore() * userWeight);
            combinedRecs.put(rec.getRecommendedProductId(), rec);
        }
        
        // Add item-based recommendations with weight
        for (ProductRecommendation rec : itemBasedRecs) {
            String productId = rec.getRecommendedProductId();
            
            if (combinedRecs.containsKey(productId)) {
                // Combine scores
                ProductRecommendation existing = combinedRecs.get(productId);
                double combinedScore = existing.getScore() + (rec.getScore() * itemWeight);
                existing.setScore(combinedScore);
                
                // Use higher confidence
                if (rec.getConfidence() != null && 
                    (existing.getConfidence() == null || rec.getConfidence() > existing.getConfidence())) {
                    existing.setConfidence(rec.getConfidence());
                }
                
                // Combine reasons
                existing.setReason(existing.getReason() + "; " + rec.getReason());
            } else {
                rec.setScore(rec.getScore() * itemWeight);
                combinedRecs.put(productId, rec);
            }
        }
        
        return new ArrayList<>(combinedRecs.values());
    }
    
    // Helper methods
    
    private boolean hasEnoughInteractionData(String userId) {
        try {
            Map<String, Object> stats = userBehaviorService.getUserBehaviorStats(userId);
            Long totalInteractions = (Long) stats.getOrDefault("totalInteractions", 0L);
            return totalInteractions >= MIN_USER_INTERACTIONS;
        } catch (Exception e) {
            log.error("Error checking interaction data for user: {}", userId, e);
            return false;
        }
    }
    
    private Set<String> getUserInteractedProducts(String userId) {
        try {
            // Get all products user has interacted with (viewed, purchased, etc.)
            List<UserBehavior> behaviors = userBehaviorService.getUserBehaviorsInDateRange(
                userId, LocalDateTime.now().minusDays(90), LocalDateTime.now());
            
            return behaviors.stream()
                .map(UserBehavior::getProductId)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error getting interacted products for user: {}", userId, e);
            return Collections.emptySet();
        }
    }
    
    private List<String> getUserLikedProducts(String userId) {
        try {
            // Get products with positive interactions (purchase, high rating, add to wishlist)
            List<UserBehavior> positiveBehaviors = new ArrayList<>();
            
            positiveBehaviors.addAll(userBehaviorService.getUserBehaviorsByAction(
                userId, UserBehavior.ActionType.PURCHASE));
            positiveBehaviors.addAll(userBehaviorService.getUserBehaviorsByAction(
                userId, UserBehavior.ActionType.ADD_TO_WISHLIST));
            positiveBehaviors.addAll(userBehaviorService.getUserBehaviorsByAction(
                userId, UserBehavior.ActionType.REVIEW));
            
            return positiveBehaviors.stream()
                .map(UserBehavior::getProductId)
                .distinct()
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting liked products for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    private List<String> getUsersWhoLikedProduct(String productId) {
        try {
            // This would typically query the database for users who had positive interactions
            // For now, we'll return an empty list as this requires cross-service data
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting users who liked product: {}", productId, e);
            return Collections.emptyList();
        }
    }
    
    private double getProductPreferenceScore(String userId, String productId) {
        try {
            // Calculate preference score based on user interactions with the product
            List<UserBehavior> behaviors = userBehaviorService.getUserBehaviorsByProduct(userId, productId);
            
            double score = 0.0;
            
            for (UserBehavior behavior : behaviors) {
                switch (behavior.getActionType()) {
                    case PURCHASE:
                        score += 1.0;
                        break;
                    case ADD_TO_WISHLIST:
                        score += 0.8;
                        break;
                    case REVIEW:
                        score += 0.7;
                        break;
                    case ADD_TO_CART:
                        score += 0.6;
                        break;
                    case VIEW:
                        score += 0.1;
                        break;
                    default:
                        score += 0.05;
                }
            }
            
            return Math.min(score, 1.0); // Cap at 1.0
            
        } catch (Exception e) {
            log.error("Error calculating preference score for user: {} and product: {}", userId, productId, e);
            return 0.5; // Default neutral score
        }
    }
    
    private double calculateConfidence(int supportCount, int totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }
        
        double ratio = (double) supportCount / totalCount;
        
        // Apply confidence boost for higher support
        if (supportCount >= 10) {
            ratio *= 1.2;
        } else if (supportCount >= 5) {
            ratio *= 1.1;
        }
        
        return Math.min(ratio, 1.0);
    }
}