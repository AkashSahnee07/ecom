package com.ecommerce.recommendation.service;

import com.ecommerce.recommendation.entity.ProductRecommendation;
import com.ecommerce.recommendation.entity.UserBehavior;
import com.ecommerce.recommendation.entity.UserProfile;
import com.ecommerce.recommendation.entity.ProductSimilarity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core recommendation engine that implements various machine learning algorithms
 * for generating personalized product recommendations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEngine.class);

    private final UserBehaviorService userBehaviorService;
    private final UserProfileService userProfileService;
    private final ProductSimilarityService productSimilarityService;
    private final CollaborativeFilteringService collaborativeFilteringService;
    private final ContentBasedFilteringService contentBasedFilteringService;
    private final HybridRecommendationService hybridRecommendationService;
    private final TrendingRecommendationService trendingRecommendationService;



    /**
     * Generate personalized recommendations for a user using hybrid approach.
     */
    public List<ProductRecommendation> generatePersonalizedRecommendations(
            String userId, int maxRecommendations) {
        
        log.info("Generating personalized recommendations for user: {}", userId);
        
        try {
            // Get user profile and behavior
            Optional<UserProfile> userProfileOpt = userProfileService.getUserProfile(userId);
            if (userProfileOpt.isEmpty()) {
                log.warn("User profile not found for user: {}", userId);
                return generateDefaultRecommendations(maxRecommendations);
            }
            
            UserProfile userProfile = userProfileOpt.get();
            
            // Check if user allows personalized recommendations
            if (!userProfile.canReceivePersonalizedRecommendations()) {
                log.info("User {} has disabled personalized recommendations", userId);
                return generateNonPersonalizedRecommendations(maxRecommendations);
            }
            
            List<ProductRecommendation> recommendations = new ArrayList<>();
            
            // 1. Collaborative Filtering (40% weight)
            List<ProductRecommendation> collaborativeRecs = 
                collaborativeFilteringService.generateRecommendations(userId, maxRecommendations / 2);
            recommendations.addAll(weightRecommendations(collaborativeRecs, 0.4));
            
            // 2. Content-Based Filtering (30% weight)
            List<ProductRecommendation> contentRecs = 
                contentBasedFilteringService.getContentBasedRecommendations(userId, userProfile, maxRecommendations / 2);
            recommendations.addAll(weightRecommendations(contentRecs, 0.3));
            
            // 3. Hybrid Approach (20% weight)
            List<ProductRecommendation> hybridRecs = 
                hybridRecommendationService.getHybridRecommendations(userId, userProfile, maxRecommendations / 3);
            recommendations.addAll(weightRecommendations(hybridRecs, 0.2));
            
            // 4. Trending Products (10% weight)
            List<ProductRecommendation> trendingRecs = 
                trendingRecommendationService.getTrendingRecommendations(userId, maxRecommendations / 4);
            recommendations.addAll(weightRecommendations(trendingRecs, 0.1));
            
            // Merge and rank recommendations
            List<ProductRecommendation> finalRecommendations = 
                mergeAndRankRecommendations(recommendations, userProfile, maxRecommendations);
            
            log.info("Generated {} personalized recommendations for user: {}", 
                    finalRecommendations.size(), userId);
            
            return finalRecommendations;
            
        } catch (Exception e) {
            log.error("Error generating personalized recommendations for user: {}", userId, e);
            return generateDefaultRecommendations(maxRecommendations);
        }
    }
    
    /**
     * Generate recommendations based on a specific product (similar products).
     */
    public List<ProductRecommendation> generateSimilarProductRecommendations(
            String userId, String productId, int maxRecommendations) {
        
        log.info("Generating similar product recommendations for product: {} and user: {}", 
                productId, userId);
        
        try {
            List<ProductSimilarity> similarities = 
                productSimilarityService.findSimilarProducts(productId, maxRecommendations * 2);
            
            List<ProductRecommendation> recommendations = similarities.stream()
                .filter(similarity -> similarity.isValidSimilarity())
                .map(similarity -> createRecommendationFromSimilarity(userId, similarity))
                .collect(Collectors.toList());
            
            // Apply user preferences and ranking
            Optional<UserProfile> userProfileOpt = userProfileService.getUserProfile(userId);
            if (userProfileOpt.isPresent()) {
                recommendations = applyUserPreferences(recommendations, userProfileOpt.get());
            }
            
            return recommendations.stream()
                .limit(maxRecommendations)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error generating similar product recommendations for product: {}", productId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Generate category-based recommendations.
     */
    public List<ProductRecommendation> generateCategoryRecommendations(
            String userId, String categoryId, int maxRecommendations) {
        
        log.info("Generating category recommendations for category: {} and user: {}", 
                categoryId, userId);
        
        try {
            // Get user's behavior in this category
            List<UserBehavior> categoryBehaviors = 
                userBehaviorService.getUserBehaviorByCategory(userId, categoryId);
            
            // Get trending products in category
            List<ProductRecommendation> trendingInCategory = 
                trendingRecommendationService.getTrendingByCategory(userId, categoryId, maxRecommendations);
            
            // Apply user preferences
            Optional<UserProfile> userProfileOpt = userProfileService.getUserProfile(userId);
            if (userProfileOpt.isPresent()) {
                trendingInCategory = applyUserPreferences(trendingInCategory, userProfileOpt.get());
            }
            
            // Set algorithm type and user ID
            trendingInCategory.forEach(rec -> {
                rec.setUserId(userId);
                rec.setAlgorithmType(ProductRecommendation.AlgorithmType.CATEGORY_BASED);
                rec.setCategoryId(categoryId);
            });
            
            return trendingInCategory.stream()
                .limit(maxRecommendations)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error generating category recommendations for category: {}", categoryId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Generate cross-sell recommendations (frequently bought together).
     */
    public List<ProductRecommendation> generateCrossSellRecommendations(
            String userId, String productId, int maxRecommendations) {
        
        log.info("Generating cross-sell recommendations for product: {} and user: {}", 
                productId, userId);
        
        try {
            // Get frequently bought together products
            List<String> frequentlyBoughtTogether = 
                userBehaviorService.getFrequentlyBoughtTogether(productId, 7); // within 7 days
            
            List<ProductRecommendation> crossSellRecs = frequentlyBoughtTogether.stream()
                .limit(maxRecommendations)
                .map(relatedProductId -> {
                    ProductRecommendation rec = new ProductRecommendation();
                    rec.setUserId(userId);
                    rec.setRecommendedProductId(relatedProductId);
                    rec.setSourceProductId(productId);
                    rec.setAlgorithmType(ProductRecommendation.AlgorithmType.CROSS_SELL);
                    rec.setScore(0.8); // Base score for cross-sell
                    rec.setConfidence(0.7);
                    rec.setReason("Frequently bought together");
                    rec.setCreatedAt(LocalDateTime.now());
                    rec.setIsActive(true);
                    
                    return rec;
                })
                .collect(Collectors.toList());
            
            // Apply user preferences
            Optional<UserProfile> userProfileOpt = userProfileService.getUserProfile(userId);
            if (userProfileOpt.isPresent()) {
                crossSellRecs = applyUserPreferences(crossSellRecs, userProfileOpt.get());
            }
            
            return crossSellRecs;
            
        } catch (Exception e) {
            log.error("Error generating cross-sell recommendations for product: {}", productId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Generate up-sell recommendations (higher-value alternatives).
     */
    public List<ProductRecommendation> generateUpSellRecommendations(
            String userId, String productId, int maxRecommendations) {
        
        log.info("Generating up-sell recommendations for product: {} and user: {}", 
                productId, userId);
        
        try {
            // Find similar but higher-priced products
            List<ProductSimilarity> similarities = 
                productSimilarityService.findSimilarProducts(productId, maxRecommendations * 2);
            
            List<ProductRecommendation> upSellRecs = similarities.stream()
                .filter(similarity -> similarity.isValidSimilarity())
                .filter(similarity -> similarity.getPriceSimilarity() != null && similarity.getPriceSimilarity() > 0.5)
                .map(similarity -> {
                    ProductRecommendation rec = createRecommendationFromSimilarity(userId, similarity);
                    rec.setAlgorithmType(ProductRecommendation.AlgorithmType.UP_SELL);
                    rec.setSourceProductId(productId);
                    rec.setReason("Premium alternative with better features");
                    // Boost score for higher-priced items
                    rec.setScore(rec.getScore() * 1.1);
                    return rec;
                })
                .sorted((r1, r2) -> {
                    Double price1 = r1.getPrice() != null ? r1.getPrice() : 0.0;
                    Double price2 = r2.getPrice() != null ? r2.getPrice() : 0.0;
                    return Double.compare(price2, price1); // Higher price first
                })
                .limit(maxRecommendations)
                .collect(Collectors.toList());
            
            return upSellRecs;
            
        } catch (Exception e) {
            log.error("Error generating up-sell recommendations for product: {}", productId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Generate recommendations for abandoned cart recovery.
     */
    public List<ProductRecommendation> generateAbandonedCartRecommendations(
            String userId, int maxRecommendations) {
        
        log.info("Generating abandoned cart recommendations for user: {}", userId);
        
        try {
            List<String> abandonedProducts = userBehaviorService.getAbandonedCartProducts(userId);
            
            if (abandonedProducts.isEmpty()) {
                return Collections.emptyList();
            }
            
            List<ProductRecommendation> recommendations = abandonedProducts.stream()
                .limit(maxRecommendations)
                .map(productId -> {
                    ProductRecommendation rec = new ProductRecommendation();
                    rec.setUserId(userId);
                    rec.setRecommendedProductId(productId);
                    rec.setAlgorithmType(ProductRecommendation.AlgorithmType.RECENTLY_VIEWED);
                    rec.setScore(0.9); // High score for abandoned cart items
                    rec.setConfidence(0.8);
                    rec.setReason("You left this in your cart");
                    rec.setCreatedAt(LocalDateTime.now());
                    rec.setIsActive(true);
                    
                    return rec;
                })
                .collect(Collectors.toList());
            
            return recommendations;
            
        } catch (Exception e) {
            log.error("Error generating abandoned cart recommendations for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    // Helper methods
    
    private List<ProductRecommendation> weightRecommendations(
            List<ProductRecommendation> recommendations, double weight) {
        
        return recommendations.stream()
            .map(rec -> {
                rec.setScore(rec.getScore() * weight);
                return rec;
            })
            .collect(Collectors.toList());
    }
    
    private List<ProductRecommendation> mergeAndRankRecommendations(
            List<ProductRecommendation> recommendations, 
            UserProfile userProfile, 
            int maxRecommendations) {
        
        // Group by product ID and merge scores
        Map<String, ProductRecommendation> mergedRecs = new HashMap<>();
        
        for (ProductRecommendation rec : recommendations) {
            String productId = rec.getRecommendedProductId();
            
            if (mergedRecs.containsKey(productId)) {
                // Merge scores (weighted average)
                ProductRecommendation existing = mergedRecs.get(productId);
                double combinedScore = (existing.getScore() + rec.getScore()) / 2;
                existing.setScore(combinedScore);
                
                // Use the algorithm with higher confidence
                if (rec.getConfidence() != null && 
                    (existing.getConfidence() == null || rec.getConfidence() > existing.getConfidence())) {
                    existing.setAlgorithmType(rec.getAlgorithmType());
                    existing.setConfidence(rec.getConfidence());
                }
            } else {
                mergedRecs.put(productId, rec);
            }
        }
        
        // Apply user preferences and final ranking
        List<ProductRecommendation> finalRecs = new ArrayList<>(mergedRecs.values());
        finalRecs = applyUserPreferences(finalRecs, userProfile);
        
        // Sort by score and apply diversity
        finalRecs.sort((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));
        finalRecs = applyDiversityFilter(finalRecs, maxRecommendations);
        
        // Set rank positions
        for (int i = 0; i < finalRecs.size(); i++) {
            finalRecs.get(i).setRankPosition(i + 1);
        }
        
        return finalRecs.stream()
            .limit(maxRecommendations)
            .collect(Collectors.toList());
    }
    
    private List<ProductRecommendation> applyUserPreferences(
            List<ProductRecommendation> recommendations, UserProfile userProfile) {
        
        return recommendations.stream()
            .map(rec -> {
                double scoreBoost = 1.0;
                
                // Boost for preferred categories
                if (userProfile.getPreferredCategories() != null && 
                    rec.getCategoryId() != null &&
                    userProfile.getPreferredCategories().contains(rec.getCategoryId())) {
                    scoreBoost += 0.2;
                }
                
                // Boost for preferred brands
                if (userProfile.getPreferredBrands() != null && 
                    rec.getBrandId() != null &&
                    userProfile.getPreferredBrands().contains(rec.getBrandId())) {
                    scoreBoost += 0.15;
                }
                
                // Adjust for user segment
                if (userProfile.getUserSegment() != null) {
                    switch (userProfile.getUserSegment()) {
                        case BARGAIN_HUNTER:
                            if (rec.getDiscountPercentage() != null && rec.getDiscountPercentage() > 0) {
                                scoreBoost += 0.3;
                            }
                            break;
                        case VIP_CUSTOMER:
                            if (rec.getPrice() != null && rec.getPrice() > 100) {
                                scoreBoost += 0.1; // Slight boost for premium products
                            }
                            break;
                        case BRAND_LOYALIST:
                            // Already handled by brand preference
                            break;
                        default:
                            break;
                    }
                }
                
                rec.setScore(Math.min(rec.getScore() * scoreBoost, 1.0));
                return rec;
            })
            .collect(Collectors.toList());
    }
    
    private List<ProductRecommendation> applyDiversityFilter(
            List<ProductRecommendation> recommendations, int maxRecommendations) {
        
        List<ProductRecommendation> diverseRecs = new ArrayList<>();
        Set<String> seenCategories = new HashSet<>();
        Set<String> seenBrands = new HashSet<>();
        
        for (ProductRecommendation rec : recommendations) {
            if (diverseRecs.size() >= maxRecommendations) {
                break;
            }
            
            // Ensure category diversity (max 40% from same category)
            boolean categoryDiverse = rec.getCategoryId() == null || 
                seenCategories.size() < 3 || 
                !seenCategories.contains(rec.getCategoryId()) ||
                diverseRecs.size() < maxRecommendations * 0.4;
            
            // Ensure brand diversity (max 30% from same brand)
            boolean brandDiverse = rec.getBrandId() == null || 
                seenBrands.size() < 3 || 
                !seenBrands.contains(rec.getBrandId()) ||
                diverseRecs.size() < maxRecommendations * 0.3;
            
            if (categoryDiverse && brandDiverse) {
                diverseRecs.add(rec);
                if (rec.getCategoryId() != null) seenCategories.add(rec.getCategoryId());
                if (rec.getBrandId() != null) seenBrands.add(rec.getBrandId());
            }
        }
        
        // Fill remaining slots if needed
        if (diverseRecs.size() < maxRecommendations) {
            recommendations.stream()
                .filter(rec -> !diverseRecs.contains(rec))
                .limit(maxRecommendations - diverseRecs.size())
                .forEach(diverseRecs::add);
        }
        
        return diverseRecs;
    }
    
    private ProductRecommendation createRecommendationFromSimilarity(
            String userId, ProductSimilarity similarity) {
        
        ProductRecommendation rec = new ProductRecommendation();
        rec.setUserId(userId);
        rec.setRecommendedProductId(similarity.getTargetProductId());
        rec.setSourceProductId(similarity.getSourceProductId());
        rec.setAlgorithmType(getAlgorithmTypeFromSimilarity(similarity.getSimilarityType()));
        rec.setScore(similarity.getSimilarityScore());
        rec.setConfidence(similarity.getConfidence());
        rec.setCategoryId(similarity.getCategoryId());
        rec.setBrandId(similarity.getBrandId());
        rec.setPrice(null); // ProductSimilarity doesn't have price field
        rec.setReason("Similar to products you viewed");
        rec.setCreatedAt(LocalDateTime.now());
        rec.setIsActive(true);
        return rec;
    }
    
    private ProductRecommendation.AlgorithmType getAlgorithmTypeFromSimilarity(
            ProductSimilarity.SimilarityType similarityType) {
        
        switch (similarityType) {
            case CONTENT_BASED:
            case SEMANTIC:
            case VISUAL:
                return ProductRecommendation.AlgorithmType.CONTENT_BASED;
            case COLLABORATIVE:
            case BEHAVIORAL:
                return ProductRecommendation.AlgorithmType.COLLABORATIVE_FILTERING;
            case CATEGORY:
                return ProductRecommendation.AlgorithmType.CATEGORY_BASED;
            case BRAND:
                return ProductRecommendation.AlgorithmType.BRAND_BASED;
            case PRICE:
                return ProductRecommendation.AlgorithmType.PRICE_BASED;
            default:
                return ProductRecommendation.AlgorithmType.HYBRID;
        }
    }
    
    private List<ProductRecommendation> generateDefaultRecommendations(int maxRecommendations) {
        log.info("Generating default trending recommendations");
        return trendingRecommendationService.getTrendingRecommendations("default", maxRecommendations);
    }
    
    private List<ProductRecommendation> generateNonPersonalizedRecommendations(int maxRecommendations) {
        log.info("Generating non-personalized recommendations");
        return trendingRecommendationService.getTrendingRecommendations("popular", maxRecommendations);
    }
}