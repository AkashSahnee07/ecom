package com.ecommerce.recommendation.service;

import com.ecommerce.recommendation.entity.ProductRecommendation;
import com.ecommerce.recommendation.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HybridRecommendationService {
    
    private static final Logger log = LoggerFactory.getLogger(HybridRecommendationService.class);
    private final CollaborativeFilteringService collaborativeFilteringService;
    private final ContentBasedFilteringService contentBasedFilteringService;
    private final TrendingRecommendationService trendingRecommendationService;


    
    // Weights for different recommendation types
    private static final double COLLABORATIVE_WEIGHT = 0.5;
    private static final double CONTENT_BASED_WEIGHT = 0.3;
    private static final double TRENDING_WEIGHT = 0.2;
    
    public List<ProductRecommendation> getHybridRecommendations(String userId, UserProfile userProfile, int limit) {
        log.info("Generating hybrid recommendations for user: {}", userId);
        
        // Get recommendations from different algorithms
        List<ProductRecommendation> collaborativeRecs = collaborativeFilteringService
            .generateRecommendations(userId, limit * 2);
        
        List<ProductRecommendation> contentBasedRecs = contentBasedFilteringService
            .getContentBasedRecommendations(userId, userProfile, limit * 2);
        
        List<ProductRecommendation> trendingRecs = trendingRecommendationService
            .getTrendingRecommendations(userId, limit);
        
        // Combine and weight the recommendations
        Map<String, ProductRecommendation> combinedRecs = new HashMap<>();
        
        // Process collaborative filtering recommendations
        collaborativeRecs.forEach(rec -> {
            String productId = rec.getRecommendedProductId();
            ProductRecommendation combined = combinedRecs.getOrDefault(productId, createBaseRecommendation(userId, productId));
            combined.setScore(combined.getScore() + (rec.getScore() * COLLABORATIVE_WEIGHT));
            combined.setAlgorithmType(ProductRecommendation.AlgorithmType.HYBRID);
            addToReason(combined, "collaborative filtering");
            combinedRecs.put(productId, combined);
        });
        
        // Process content-based recommendations
        contentBasedRecs.forEach(rec -> {
            String productId = rec.getRecommendedProductId();
            ProductRecommendation combined = combinedRecs.getOrDefault(productId, createBaseRecommendation(userId, productId));
            combined.setScore(combined.getScore() + (rec.getScore() * CONTENT_BASED_WEIGHT));
            combined.setAlgorithmType(ProductRecommendation.AlgorithmType.HYBRID);
            addToReason(combined, "content similarity");
            combinedRecs.put(productId, combined);
        });
        
        // Process trending recommendations
        trendingRecs.forEach(rec -> {
            String productId = rec.getRecommendedProductId();
            ProductRecommendation combined = combinedRecs.getOrDefault(productId, createBaseRecommendation(userId, productId));
            combined.setScore(combined.getScore() + (rec.getScore() * TRENDING_WEIGHT));
            combined.setAlgorithmType(ProductRecommendation.AlgorithmType.HYBRID);
            addToReason(combined, "trending products");
            combinedRecs.put(productId, combined);
        });
        
        // Sort by combined score and return top recommendations
        return combinedRecs.values().stream()
            .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private ProductRecommendation createBaseRecommendation(String userId, String productId) {
        ProductRecommendation recommendation = new ProductRecommendation();
        recommendation.setUserId(userId);
        recommendation.setRecommendedProductId(productId);
        recommendation.setScore(0.0);
        recommendation.setAlgorithmType(ProductRecommendation.AlgorithmType.HYBRID);
        recommendation.setReason("");
        return recommendation;
    }
    
    private void addToReason(ProductRecommendation recommendation, String reason) {
        String currentReason = recommendation.getReason();
        if (currentReason == null || currentReason.isEmpty()) {
            recommendation.setReason("Based on " + reason);
        } else if (!currentReason.contains(reason)) {
            recommendation.setReason(currentReason + ", " + reason);
        }
    }
}