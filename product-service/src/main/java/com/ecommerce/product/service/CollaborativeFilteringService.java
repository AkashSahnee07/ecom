package com.ecommerce.product.service;

import com.ecommerce.product.entity.ProductRecommendation;
import com.ecommerce.product.entity.ProductRecommendation.RecommendationType;
import com.ecommerce.product.entity.UserPreference;
import com.ecommerce.product.repository.UserPreferenceRepository;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollaborativeFilteringService {
    
    @Autowired
    private UserPreferenceRepository userPreferenceRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    // Generate collaborative filtering recommendations
    @Transactional(readOnly = true)
    public List<ProductRecommendation> generateRecommendations(Long userId, int limit) {
        List<ProductRecommendation> recommendations = new ArrayList<>();
        
        try {
            // Get user's preferences
            UserPreference userPreference = userPreferenceRepository.findByUserId(userId).orElse(null);
            if (userPreference == null) {
                return recommendations;
            }
            
            // Find users with similar preferences
            List<UserPreference> similarUsers = findSimilarUsers(userId, userPreference);
            
            // Calculate similarity scores and generate recommendations
            Map<Long, Double> productScores = new HashMap<>();
            
            for (UserPreference similarUser : similarUsers) {
                double similarity = calculateUserSimilarity(userPreference, similarUser);
                
                // Get products preferred by similar users
                List<Long> preferredProducts = getPreferredProductsForUser(similarUser);
                
                for (Long productId : preferredProducts) {
                    productScores.merge(productId, similarity * 0.8, Double::sum);
                }
            }
            
            // Convert to recommendations and sort by score
            recommendations = productScores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> new ProductRecommendation(
                            userId, 
                            entry.getKey(), 
                            Math.min(entry.getValue(), 1.0), // Cap at 1.0
                            RecommendationType.COLLABORATIVE_FILTERING,
                            "Based on users with similar preferences"
                    ))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("Error in collaborative filtering: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    // Find users with similar preferences
    private List<UserPreference> findSimilarUsers(Long userId, UserPreference userPreference) {
        List<UserPreference> similarUsers = new ArrayList<>();
        
        // Find users with similar categories
        if (userPreference.getPreferredCategories() != null && !userPreference.getPreferredCategories().isEmpty()) {
            similarUsers.addAll(userPreferenceRepository.findUsersWithSimilarCategories(
                    userId, userPreference.getPreferredCategories()));
        }
        
        // Find users with similar brands
        if (userPreference.getPreferredBrands() != null && !userPreference.getPreferredBrands().isEmpty()) {
            similarUsers.addAll(userPreferenceRepository.findUsersWithSimilarBrands(
                    userId, userPreference.getPreferredBrands()));
        }
        
        // Find users with similar price range
        if (userPreference.getMinPrice() != null && userPreference.getMaxPrice() != null) {
            similarUsers.addAll(userPreferenceRepository.findUsersWithSimilarPriceRange(
                    userId, userPreference.getMinPrice(), userPreference.getMaxPrice()));
        }
        
        // Remove duplicates
        return similarUsers.stream().distinct().collect(Collectors.toList());
    }
    
    // Calculate similarity between two users
    private double calculateUserSimilarity(UserPreference user1, UserPreference user2) {
        double similarity = 0.0;
        int factors = 0;
        
        // Category similarity
        if (user1.getPreferredCategories() != null && user2.getPreferredCategories() != null) {
            long commonCategories = user1.getPreferredCategories().stream()
                    .filter(user2.getPreferredCategories()::contains)
                    .count();
            int totalCategories = Math.max(user1.getPreferredCategories().size(), 
                                         user2.getPreferredCategories().size());
            if (totalCategories > 0) {
                similarity += (double) commonCategories / totalCategories;
                factors++;
            }
        }
        
        // Brand similarity
        if (user1.getPreferredBrands() != null && user2.getPreferredBrands() != null) {
            long commonBrands = user1.getPreferredBrands().stream()
                    .filter(user2.getPreferredBrands()::contains)
                    .count();
            int totalBrands = Math.max(user1.getPreferredBrands().size(), 
                                     user2.getPreferredBrands().size());
            if (totalBrands > 0) {
                similarity += (double) commonBrands / totalBrands;
                factors++;
            }
        }
        
        // Price range similarity
        if (user1.getMinPrice() != null && user1.getMaxPrice() != null &&
            user2.getMinPrice() != null && user2.getMaxPrice() != null) {
            
            double overlap = Math.max(0, 
                    Math.min(user1.getMaxPrice(), user2.getMaxPrice()) - 
                    Math.max(user1.getMinPrice(), user2.getMinPrice()));
            double union = Math.max(user1.getMaxPrice(), user2.getMaxPrice()) - 
                          Math.min(user1.getMinPrice(), user2.getMinPrice());
            
            if (union > 0) {
                similarity += overlap / union;
                factors++;
            }
        }
        
        return factors > 0 ? similarity / factors : 0.0;
    }
    
    // Get products that a user might prefer based on their preferences
    private List<Long> getPreferredProductsForUser(UserPreference userPreference) {
        List<Long> productIds = new ArrayList<>();
        
        try {
            // Get products from preferred categories
            if (userPreference.getPreferredCategories() != null) {
                for (Long categoryId : userPreference.getPreferredCategories()) {
                    productRepository.findByCategoryId(categoryId).stream()
                            .limit(5) // Limit to avoid too many products
                            .forEach(product -> productIds.add(product.getId()));
                }
            }
            
            // Get products from preferred brands
            if (userPreference.getPreferredBrands() != null) {
                for (String brand : userPreference.getPreferredBrands()) {
                    productRepository.findByBrand(brand).stream()
                            .limit(5) // Limit to avoid too many products
                            .forEach(product -> productIds.add(product.getId()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting preferred products: " + e.getMessage());
        }
        
        return productIds.stream().distinct().collect(Collectors.toList());
    }
}
