package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.entity.ProductRecommendation.RecommendationType;
import com.ecommerce.product.entity.UserPreference;
import com.ecommerce.product.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    // Get personalized recommendations for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ProductResponseDto>> getRecommendationsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "score") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductResponseDto> recommendations = recommendationService
                    .getRecommendationsForUser(userId, page, size);
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get recommendations by type
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<Page<ProductResponseDto>> getRecommendationsByType(
            @PathVariable Long userId,
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "score") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            RecommendationType recommendationType;
            try {
                recommendationType = RecommendationType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductResponseDto> recommendations = recommendationService
                    .getRecommendationsByType(userId, recommendationType, page, size);
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get similar products based on a specific product
    @GetMapping("/similar/{productId}")
    public ResponseEntity<Page<ProductResponseDto>> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // For now, return general recommendations (similar products feature to be enhanced)
            Page<ProductResponseDto> recommendations = recommendationService
                    .getRecommendationsForUser(userId != null ? userId : 1L, page, size);
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Generate fresh recommendations for a user
    @PostMapping("/generate/{userId}")
    public ResponseEntity<String> generateRecommendations(@PathVariable Long userId) {
        try {
            recommendationService.generateRecommendationsForUser(userId);
            return ResponseEntity.ok("Recommendations generated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to generate recommendations: " + e.getMessage());
        }
    }
    
    // Update user preferences (this would typically be called from user service)
    @PostMapping("/preferences/{userId}")
    public ResponseEntity<String> updateUserPreferences(
            @PathVariable Long userId,
            @RequestBody UserPreferenceRequest request) {
        
        try {
            // Create UserPreference object
            UserPreference preferences = new UserPreference();
            preferences.setUserId(userId);
            preferences.setPreferredCategories(request.getPreferredCategories());
            preferences.setPreferredBrands(request.getPreferredBrands());
            preferences.setMinPrice(request.getMinPrice());
            preferences.setMaxPrice(request.getMaxPrice());
            preferences.setMinRating(request.getMinRating());
            
            recommendationService.updateUserPreferences(userId, preferences);
            
            // Regenerate recommendations after preference update
            recommendationService.generateRecommendationsForUser(userId);
            
            return ResponseEntity.ok("User preferences updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to update preferences: " + e.getMessage());
        }
    }
    
    // Get trending/popular products
    @GetMapping("/trending")
    public ResponseEntity<Page<ProductResponseDto>> getTrendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // This could be enhanced to show actual trending products
            // For now, return general recommendations
            Page<ProductResponseDto> recommendations = recommendationService
                    .getRecommendationsForUser(1L, page, size); // Default user for trending
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Clean up expired recommendations
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupExpiredRecommendations() {
        try {
            recommendationService.cleanupExpiredRecommendations();
            return ResponseEntity.ok("Expired recommendations cleaned up successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to cleanup recommendations: " + e.getMessage());
        }
    }
    
    // DTO for user preference updates
    public static class UserPreferenceRequest {
        private java.util.List<Long> preferredCategories;
        private java.util.List<String> preferredBrands;
        private Double minPrice;
        private Double maxPrice;
        private Double minRating;
        
        // Constructors
        public UserPreferenceRequest() {}
        
        public UserPreferenceRequest(java.util.List<Long> preferredCategories, 
                                   java.util.List<String> preferredBrands,
                                   Double minPrice, Double maxPrice, Double minRating) {
            this.preferredCategories = preferredCategories;
            this.preferredBrands = preferredBrands;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.minRating = minRating;
        }
        
        // Getters and setters
        public java.util.List<Long> getPreferredCategories() {
            return preferredCategories;
        }
        
        public void setPreferredCategories(java.util.List<Long> preferredCategories) {
            this.preferredCategories = preferredCategories;
        }
        
        public java.util.List<String> getPreferredBrands() {
            return preferredBrands;
        }
        
        public void setPreferredBrands(java.util.List<String> preferredBrands) {
            this.preferredBrands = preferredBrands;
        }
        
        public Double getMinPrice() {
            return minPrice;
        }
        
        public void setMinPrice(Double minPrice) {
            this.minPrice = minPrice;
        }
        
        public Double getMaxPrice() {
            return maxPrice;
        }
        
        public void setMaxPrice(Double maxPrice) {
            this.maxPrice = maxPrice;
        }
        
        public Double getMinRating() {
            return minRating;
        }
        
        public void setMinRating(Double minRating) {
            this.minRating = minRating;
        }
    }
}