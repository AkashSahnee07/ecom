package com.ecommerce.recommendation.service;

import com.ecommerce.recommendation.entity.ProductRecommendation;
import com.ecommerce.recommendation.entity.UserBehavior;
import com.ecommerce.recommendation.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrendingRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(TrendingRecommendationService.class);
    
    private final UserBehaviorRepository userBehaviorRepository;


    
    @Cacheable(value = "trending-products", key = "#userId + '_' + #limit")
    public List<ProductRecommendation> getTrendingRecommendations(String userId, int limit) {
        log.info("Generating trending recommendations for user: {}", userId);
        
        // Get recent user behaviors (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<UserBehavior> recentBehaviors = userBehaviorRepository
             .findByTimestampBetweenOrderByTimestampDesc(weekAgo, LocalDateTime.now());
        
        // Calculate trending scores based on recent activities
        Map<String, Double> trendingScores = calculateTrendingScores(recentBehaviors);
        
        // Convert to recommendations
        return trendingScores.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .limit(limit)
            .map(entry -> {
                ProductRecommendation recommendation = new ProductRecommendation();
                recommendation.setUserId(userId);
                recommendation.setRecommendedProductId(entry.getKey());
                recommendation.setScore(entry.getValue());
                recommendation.setAlgorithmType(ProductRecommendation.AlgorithmType.TRENDING);
                recommendation.setReason("Currently trending product");
                return recommendation;
            })
            .collect(Collectors.toList());
    }
    
    private Map<String, Double> calculateTrendingScores(List<UserBehavior> behaviors) {
        Map<String, Double> scores = behaviors.stream()
            .collect(Collectors.groupingBy(
                UserBehavior::getProductId,
                Collectors.summingDouble(this::getBehaviorWeight)
            ));
        
        // Normalize scores to 0-1 range
        double maxScore = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        
        return scores.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue() / maxScore
            ));
    }
    
    private double getBehaviorWeight(UserBehavior behavior) {
        // Weight different behaviors differently
        switch (behavior.getBehaviorType()) {
            case "PURCHASE":
                return 5.0;
            case "ADD_TO_CART":
                return 3.0;
            case "VIEW":
                return 1.0;
            case "LIKE":
                return 2.0;
            case "SHARE":
                return 2.5;
            default:
                return 0.5;
        }
    }
    
    @Cacheable(value = "category-trending", key = "#category + '_' + #limit")
    public List<ProductRecommendation> getTrendingByCategory(String userId, String category, int limit) {
        log.info("Generating trending recommendations for category: {} and user: {}", category, userId);
        
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<UserBehavior> categoryBehaviors = userBehaviorRepository
             .findByTimestampBetweenOrderByTimestampDesc(weekAgo, LocalDateTime.now())
             .stream()
             .filter(behavior -> category.equals(behavior.getProductCategory()))
             .collect(Collectors.toList());
        
        Map<String, Double> trendingScores = calculateTrendingScores(categoryBehaviors);
        
        return trendingScores.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .limit(limit)
            .map(entry -> {
                ProductRecommendation recommendation = new ProductRecommendation();
                recommendation.setUserId(userId);
                recommendation.setRecommendedProductId(entry.getKey());
                recommendation.setScore(entry.getValue());
                recommendation.setAlgorithmType(ProductRecommendation.AlgorithmType.TRENDING);
                recommendation.setReason("Trending in " + category + " category");
                return recommendation;
            })
            .collect(Collectors.toList());
    }
}
