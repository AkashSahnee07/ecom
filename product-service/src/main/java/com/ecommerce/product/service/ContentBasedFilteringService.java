package com.ecommerce.product.service;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductRecommendation;
import com.ecommerce.product.entity.ProductRecommendation.RecommendationType;
import com.ecommerce.product.entity.UserPreference;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.UserPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContentBasedFilteringService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserPreferenceRepository userPreferenceRepository;
    
    // Generate content-based recommendations
    @Transactional(readOnly = true)
    public List<ProductRecommendation> generateRecommendations(Long userId, int limit) {
        List<ProductRecommendation> recommendations = new ArrayList<>();
        
        try {
            // Get user's preferences
            UserPreference userPreference = userPreferenceRepository.findByUserId(userId).orElse(null);
            if (userPreference == null) {
                return recommendations;
            }
            
            // Get all products that match user preferences
            List<Product> candidateProducts = getCandidateProducts(userPreference);
            
            // Calculate content similarity scores
            Map<Long, Double> productScores = new HashMap<>();
            
            for (Product product : candidateProducts) {
                double score = calculateContentSimilarity(userPreference, product);
                if (score > 0.3) { // Only include products with decent similarity
                    productScores.put(product.getId(), score);
                }
            }
            
            // Convert to recommendations and sort by score
            recommendations = productScores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        Product product = productRepository.findById(entry.getKey()).orElse(null);
                        String reason = generateRecommendationReason(userPreference, product);
                        return new ProductRecommendation(
                                userId, 
                                entry.getKey(), 
                                entry.getValue(),
                                RecommendationType.CONTENT_BASED,
                                reason
                        );
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("Error in content-based filtering: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    // Get candidate products based on user preferences
    private List<Product> getCandidateProducts(UserPreference userPreference) {
        List<Product> candidates = new ArrayList<>();
        
        try {
            // Get products from preferred categories
            if (userPreference.getPreferredCategories() != null && !userPreference.getPreferredCategories().isEmpty()) {
                for (Long categoryId : userPreference.getPreferredCategories()) {
                    candidates.addAll(productRepository.findByCategoryId(categoryId));
                }
            }
            
            // Get products from preferred brands
            if (userPreference.getPreferredBrands() != null && !userPreference.getPreferredBrands().isEmpty()) {
                for (String brand : userPreference.getPreferredBrands()) {
                    candidates.addAll(productRepository.findByBrand(brand));
                }
            }
            
            // Get products within price range
            if (userPreference.getMinPrice() != null && userPreference.getMaxPrice() != null) {
                candidates.addAll(productRepository.findByPriceBetween(
                        BigDecimal.valueOf(userPreference.getMinPrice()), 
                        BigDecimal.valueOf(userPreference.getMaxPrice())));
            }
            
            // Get products with minimum rating
            if (userPreference.getMinRating() != null) {
                candidates.addAll(productRepository.findByAverageRatingGreaterThanEqual(
                        BigDecimal.valueOf(userPreference.getMinRating())));
            }
            
            // Remove duplicates
            candidates = candidates.stream().distinct().collect(Collectors.toList());
            
        } catch (Exception e) {
            System.err.println("Error getting candidate products: " + e.getMessage());
        }
        
        return candidates;
    }
    
    // Calculate content similarity between user preferences and product
    private double calculateContentSimilarity(UserPreference userPreference, Product product) {
        double similarity = 0.0;
        int factors = 0;
        
        // Category match
        if (userPreference.getPreferredCategories() != null && 
            userPreference.getPreferredCategories().contains(product.getCategoryId())) {
            similarity += 0.4; // High weight for category match
            factors++;
        }
        
        // Brand match
        if (userPreference.getPreferredBrands() != null && 
            userPreference.getPreferredBrands().contains(product.getBrand())) {
            similarity += 0.3; // Medium weight for brand match
            factors++;
        }
        
        // Price range match
        if (userPreference.getMinPrice() != null && userPreference.getMaxPrice() != null) {
            BigDecimal productPrice = product.getPrice();
            BigDecimal minPrice = BigDecimal.valueOf(userPreference.getMinPrice());
            BigDecimal maxPrice = BigDecimal.valueOf(userPreference.getMaxPrice());
            if (productPrice.compareTo(minPrice) >= 0 && 
                productPrice.compareTo(maxPrice) <= 0) {
                similarity += 0.2; // Lower weight for price match
                factors++;
            }
        }
        
        // Rating match
        if (userPreference.getMinRating() != null && product.getAverageRating() != null) {
            BigDecimal minRating = BigDecimal.valueOf(userPreference.getMinRating());
            if (product.getAverageRating().compareTo(minRating) >= 0) {
                similarity += 0.1; // Bonus for meeting rating requirement
                factors++;
            }
        }
        
        // Normalize by number of factors considered
        return factors > 0 ? similarity : 0.0;
    }
    
    // Generate a human-readable reason for the recommendation
    private String generateRecommendationReason(UserPreference userPreference, Product product) {
        List<String> reasons = new ArrayList<>();
        
        if (product == null) {
            return "Recommended based on your preferences";
        }
        
        if (userPreference.getPreferredCategories() != null && 
            userPreference.getPreferredCategories().contains(product.getCategoryId())) {
            reasons.add("matches your preferred category");
        }
        
        if (userPreference.getPreferredBrands() != null && 
            userPreference.getPreferredBrands().contains(product.getBrand())) {
            reasons.add("from your preferred brand " + product.getBrand());
        }
        
        if (userPreference.getMinPrice() != null && userPreference.getMaxPrice() != null) {
            BigDecimal productPrice = product.getPrice();
            BigDecimal minPrice = BigDecimal.valueOf(userPreference.getMinPrice());
            BigDecimal maxPrice = BigDecimal.valueOf(userPreference.getMaxPrice());
            if (productPrice.compareTo(minPrice) >= 0 && 
                productPrice.compareTo(maxPrice) <= 0) {
                reasons.add("within your price range");
            }
        }
        
        if (userPreference.getMinRating() != null && product.getAverageRating() != null) {
            BigDecimal minRating = BigDecimal.valueOf(userPreference.getMinRating());
            if (product.getAverageRating().compareTo(minRating) >= 0) {
                reasons.add("meets your rating criteria");
            }
        }
        
        if (reasons.isEmpty()) {
            return "Recommended based on your preferences";
        }
        
        return "This product " + String.join(", ", reasons);
    }
    
    // Generate recommendations for similar products
    @Transactional(readOnly = true)
    public List<ProductRecommendation> generateSimilarProductRecommendations(Long productId, Long userId, int limit) {
        List<ProductRecommendation> recommendations = new ArrayList<>();
        
        try {
            Product baseProduct = productRepository.findById(productId).orElse(null);
            if (baseProduct == null) {
                return recommendations;
            }
            
            // Find products with similar attributes
            List<Product> similarProducts = new ArrayList<>();
            
            // Same category products
            similarProducts.addAll(productRepository.findByCategoryId(baseProduct.getCategoryId()));
            
            // Same brand products
            if (baseProduct.getBrand() != null) {
                similarProducts.addAll(productRepository.findByBrand(baseProduct.getBrand()));
            }
            
            // Similar price range products (±20%)
            BigDecimal minPrice = baseProduct.getPrice().multiply(new BigDecimal("0.8"));
            BigDecimal maxPrice = baseProduct.getPrice().multiply(new BigDecimal("1.2"));
            similarProducts.addAll(productRepository.findByPriceBetween(minPrice, maxPrice));
            
            // Remove the base product and duplicates
            similarProducts = similarProducts.stream()
                    .filter(p -> !p.getId().equals(productId))
                    .distinct()
                    .collect(Collectors.toList());
            
            // Calculate similarity scores
            Map<Long, Double> productScores = new HashMap<>();
            
            for (Product product : similarProducts) {
                double score = calculateProductSimilarity(baseProduct, product);
                if (score > 0.2) {
                    productScores.put(product.getId(), score);
                }
            }
            
            // Convert to recommendations
            recommendations = productScores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> new ProductRecommendation(
                            userId, 
                            entry.getKey(), 
                            entry.getValue(),
                            RecommendationType.SIMILAR_PRODUCTS,
                            "Similar to product you viewed"
                    ))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("Error generating similar product recommendations: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    // Calculate similarity between two products
    private double calculateProductSimilarity(Product product1, Product product2) {
        double similarity = 0.0;
        
        // Category similarity
        if (product1.getCategoryId().equals(product2.getCategoryId())) {
            similarity += 0.4;
        }
        
        // Brand similarity
        if (product1.getBrand() != null && product1.getBrand().equals(product2.getBrand())) {
            similarity += 0.3;
        }
        
        // Price similarity (closer prices get higher scores)
        BigDecimal priceDiff = product1.getPrice().subtract(product2.getPrice()).abs();
        BigDecimal avgPrice = product1.getPrice().add(product2.getPrice()).divide(new BigDecimal("2"));
        double priceRatio = priceDiff.divide(avgPrice, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        similarity += Math.max(0, 0.2 * (1 - priceRatio)); // Max 0.2 points for price similarity
        
        // Rating similarity
        if (product1.getAverageRating() != null && product2.getAverageRating() != null) {
            double ratingDiff = Math.abs(product1.getAverageRating().doubleValue() - 
                                       product2.getAverageRating().doubleValue());
            similarity += Math.max(0, 0.1 * (1 - ratingDiff / 5.0)); // Max 0.1 points for rating similarity
        }
        
        return similarity;
    }
}