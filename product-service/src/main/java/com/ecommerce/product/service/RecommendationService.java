package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.entity.ProductRecommendation;
import com.ecommerce.product.entity.ProductRecommendation.RecommendationType;
import com.ecommerce.product.entity.UserPreference;
import com.ecommerce.product.repository.ProductRecommendationRepository;
import com.ecommerce.product.repository.UserPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

@Service
@Transactional
public class RecommendationService {
    
    @Autowired
    private ProductRecommendationRepository recommendationRepository;
    
    @Autowired
    private UserPreferenceRepository userPreferenceRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CollaborativeFilteringService collaborativeFilteringService;
    
    @Autowired
    private ContentBasedFilteringService contentBasedFilteringService;
    
    // Get personalized recommendations for a user
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getRecommendationsForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductRecommendation> recommendations = recommendationRepository
                .findByUserIdAndNotExpired(userId, LocalDateTime.now(), pageable);
        
        List<ProductResponseDto> productDtos = recommendations.getContent().stream()
                .map(rec -> {
                    try {
                        return productService.getProductById(rec.getProductId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(product -> product != null)
                .collect(Collectors.toList());
        
        return new PageImpl<>(productDtos, pageable, recommendations.getTotalElements());
    }
    
    // Get recommendations by type
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getRecommendationsByType(Long userId, RecommendationType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductRecommendation> recommendations = recommendationRepository
                .findByUserIdAndTypeAndNotExpired(userId, type, LocalDateTime.now(), pageable);
        
        List<ProductResponseDto> productDtos = recommendations.getContent().stream()
                .map(rec -> {
                    try {
                        return productService.getProductById(rec.getProductId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(product -> product != null)
                .collect(Collectors.toList());
        
        return new PageImpl<>(productDtos, pageable, recommendations.getTotalElements());
    }
    
    // Generate recommendations for a user
    public void generateRecommendationsForUser(Long userId) {
        // Clean up old recommendations first
        cleanupExpiredRecommendations();
        
        // Get user preferences
        Optional<UserPreference> userPrefOpt = userPreferenceRepository.findByUserId(userId);
        
        // Generate collaborative filtering recommendations
        List<ProductRecommendation> collaborativeRecs = collaborativeFilteringService
                .generateRecommendations(userId, 20);
        saveRecommendations(collaborativeRecs);
        
        // Generate content-based recommendations
        if (userPrefOpt.isPresent()) {
            List<ProductRecommendation> contentBasedRecs = contentBasedFilteringService
                    .generateRecommendations(userId, 20);
            saveRecommendations(contentBasedRecs);
        }
        
        // Generate trending recommendations as fallback
        generateTrendingRecommendations(userId, 10);
    }
    
    // Generate trending recommendations
    private void generateTrendingRecommendations(Long userId, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<ProductResponseDto> trendingProducts = productService.getTopRatedProducts(pageable);
            
            for (ProductResponseDto product : trendingProducts.getContent()) {
                if (!recommendationRepository.existsByUserIdAndProductIdAndRecommendationType(
                        userId, product.getId(), RecommendationType.TRENDING)) {
                    
                    ProductRecommendation recommendation = new ProductRecommendation(
                            userId, product.getId(), 0.7, RecommendationType.TRENDING,
                            "Trending product with high ratings");
                    recommendationRepository.save(recommendation);
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the entire process
            System.err.println("Error generating trending recommendations: " + e.getMessage());
        }
    }
    
    // Save or update recommendations
    private void saveRecommendations(List<ProductRecommendation> recommendations) {
        for (ProductRecommendation rec : recommendations) {
            ProductRecommendation existing = recommendationRepository
                    .findByUserIdAndProductIdAndType(rec.getUserId(), rec.getProductId(), rec.getRecommendationType());
            
            if (existing != null) {
                // Update existing recommendation with new score
                existing.setScore(rec.getScore());
                existing.setReason(rec.getReason());
                existing.setExpiresAt(LocalDateTime.now().plusDays(7));
                recommendationRepository.save(existing);
            } else {
                recommendationRepository.save(rec);
            }
        }
    }
    
    // Clean up expired recommendations
    public void cleanupExpiredRecommendations() {
        recommendationRepository.deleteExpiredRecommendations(LocalDateTime.now());
    }
    
    // Update user preferences
    public UserPreference updateUserPreferences(Long userId, UserPreference preferences) {
        Optional<UserPreference> existingOpt = userPreferenceRepository.findByUserId(userId);
        
        if (existingOpt.isPresent()) {
            UserPreference existing = existingOpt.get();
            existing.setPreferredCategories(preferences.getPreferredCategories());
            existing.setPreferredBrands(preferences.getPreferredBrands());
            existing.setMinPrice(preferences.getMinPrice());
            existing.setMaxPrice(preferences.getMaxPrice());
            existing.setMinRating(preferences.getMinRating());
            return userPreferenceRepository.save(existing);
        } else {
            preferences.setUserId(userId);
            return userPreferenceRepository.save(preferences);
        }
    }
    
    // Get user preferences
    @Transactional(readOnly = true)
    public Optional<UserPreference> getUserPreferences(Long userId) {
        return userPreferenceRepository.findByUserId(userId);
    }
    
    // Get recommendation statistics
    @Transactional(readOnly = true)
    public List<Object[]> getRecommendationStats(Long userId) {
        return recommendationRepository.getRecommendationStatsByUser(userId, LocalDateTime.now());
    }
    
    // Remove specific recommendation
    public void removeRecommendation(Long userId, Long productId) {
        recommendationRepository.deleteByUserIdAndProductId(userId, productId);
    }
    
    // Clear all recommendations for a user
    public void clearUserRecommendations(Long userId) {
        recommendationRepository.deleteByUserId(userId);
    }
}