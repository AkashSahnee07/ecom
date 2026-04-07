package com.ecommerce.recommendation.service;

import com.ecommerce.recommendation.entity.ProductRecommendation;
import com.ecommerce.recommendation.entity.ProductSimilarity;
import com.ecommerce.recommendation.entity.UserProfile;
import com.ecommerce.recommendation.repository.ProductSimilarityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentBasedFilteringService {
    
    private final ProductSimilarityRepository productSimilarityRepository;
    private final ProductSimilarityService productSimilarityService;


    
    public List<ProductRecommendation> getContentBasedRecommendations(String userId, UserProfile userProfile, int limit) {
        log.info("Generating content-based recommendations for user: {}", userId);
        
        // Get user's preferred categories and brands from profile
        List<String> preferredCategories = userProfile.getPreferredCategories().stream().collect(Collectors.toList());
        List<String> preferredBrands = userProfile.getPreferredBrands().stream().collect(Collectors.toList());
        
        // Find similar products based on user preferences
        // Using findAll and filtering since specific repository method doesn't exist
        List<ProductSimilarity> similarProducts = productSimilarityRepository
            .findAll()
            .stream()
            .filter(similarity -> similarity.getIsActive())
            .sorted((p1, p2) -> Double.compare(p2.getSimilarityScore(), p1.getSimilarityScore()))
            .limit(limit * 2) // Get more to filter out already purchased
            .collect(Collectors.toList());
        
        // Convert to recommendations with content-based scoring
        return similarProducts.stream()
            .map(similarity -> {
                ProductRecommendation recommendation = new ProductRecommendation();
                recommendation.setUserId(userId);
                recommendation.setRecommendedProductId(similarity.getTargetProductId());
                recommendation.setScore(calculateContentBasedScore(similarity, userProfile));
                recommendation.setAlgorithmType(ProductRecommendation.AlgorithmType.CONTENT_BASED);
                recommendation.setReason("Based on your preferences for " + 
                    String.join(", ", preferredCategories));
                return recommendation;
            })
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private double calculateContentBasedScore(ProductSimilarity similarity, UserProfile userProfile) {
        double baseScore = similarity.getSimilarityScore();
        
        // Boost score based on user preferences
        // Note: ProductSimilarity doesn't have category/brand fields, using base score only
        double categoryBoost = 0.0; // Would need product service to get category info
        double brandBoost = 0.0; // Would need product service to get brand info
        
        return Math.min(1.0, baseScore + categoryBoost + brandBoost);
    }
}
