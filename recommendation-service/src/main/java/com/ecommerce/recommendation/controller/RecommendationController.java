package com.ecommerce.recommendation.controller;

import com.ecommerce.recommendation.entity.ProductRecommendation;
import com.ecommerce.recommendation.entity.UserBehavior;
import com.ecommerce.recommendation.service.RecommendationEngine;
import com.ecommerce.recommendation.service.UserBehaviorService;
import com.ecommerce.recommendation.service.CollaborativeFilteringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for product recommendation operations.
 * Provides endpoints for generating recommendations, tracking user behavior,
 * and managing recommendation preferences.
 */
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Recommendations", description = "Product recommendation operations")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);
    private final RecommendationEngine recommendationEngine;
    private final UserBehaviorService userBehaviorService;
    private final CollaborativeFilteringService collaborativeFilteringService;

    /**
     * Get personalized product recommendations for a user.
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get personalized recommendations", 
               description = "Generate personalized product recommendations for a specific user")
    public ResponseEntity<List<ProductRecommendation>> getRecommendations(
            @Parameter(description = "User ID", required = true)
            @PathVariable @NotBlank String userId,
            
            @Parameter(description = "Maximum number of recommendations (1-50)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
            
            @Parameter(description = "Algorithm type filter")
            @RequestParam(required = false) ProductRecommendation.AlgorithmType algorithmType,
            
            @Parameter(description = "Include diversity in recommendations")
            @RequestParam(defaultValue = "true") boolean includeDiversity) {
        
        log.info("Getting recommendations for user: {} with limit: {}", userId, limit);
        
        try {
            List<ProductRecommendation> recommendations;
            
            if (algorithmType != null) {
                // Get recommendations from specific algorithm
                // All recommendation types use the personalized approach
                recommendations = recommendationEngine.generatePersonalizedRecommendations(userId, limit);
            } else {
                // Get mixed recommendations
                recommendations = recommendationEngine.generatePersonalizedRecommendations(userId, limit);
            }
            
            // Diversity filtering is handled internally in the recommendation engine
            
            log.info("Generated {} recommendations for user: {}", recommendations.size(), userId);
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("Error getting recommendations for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get similar product recommendations.
     */
    @GetMapping("/products/{productId}/similar")
    @Operation(summary = "Get similar products", 
               description = "Get products similar to the specified product")
    public ResponseEntity<List<ProductRecommendation>> getSimilarProducts(
            @Parameter(description = "Product ID", required = true)
            @PathVariable @NotBlank String productId,
            
            @Parameter(description = "User ID for personalization")
            @RequestParam(required = false) String userId,
            
            @Parameter(description = "Maximum number of recommendations (1-50)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.info("Getting similar products for product: {} with limit: {}", productId, limit);
        
        try {
            List<ProductRecommendation> recommendations = 
                recommendationEngine.generateSimilarProductRecommendations(productId, userId, limit);
            
            log.info("Generated {} similar product recommendations for product: {}", 
                    recommendations.size(), productId);
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("Error getting similar products for product: {}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get category-based recommendations.
     */
    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Get category recommendations", 
               description = "Get popular products in a specific category")
    public ResponseEntity<List<ProductRecommendation>> getCategoryRecommendations(
            @Parameter(description = "Category ID", required = true)
            @PathVariable @NotBlank String categoryId,
            
            @Parameter(description = "User ID for personalization")
            @RequestParam(required = false) String userId,
            
            @Parameter(description = "Maximum number of recommendations (1-50)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.info("Getting category recommendations for category: {} with limit: {}", categoryId, limit);
        
        try {
            // Use personalized recommendations filtered by category preference
            List<ProductRecommendation> recommendations = 
                recommendationEngine.generatePersonalizedRecommendations(userId, limit);
            
            log.info("Generated {} category recommendations for category: {}", 
                    recommendations.size(), categoryId);
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("Error getting category recommendations for category: {}", categoryId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get cross-sell recommendations (frequently bought together).
     */
    @GetMapping("/cross-sell")
    @Operation(summary = "Get cross-sell recommendations", 
               description = "Get products frequently bought together with cart items")
    public ResponseEntity<List<ProductRecommendation>> getCrossSellRecommendations(
            @Parameter(description = "Product IDs in cart", required = true)
            @RequestParam List<String> cartProductIds,
            
            @Parameter(description = "User ID for personalization")
            @RequestParam(required = false) String userId,
            
            @Parameter(description = "Maximum number of recommendations (1-20)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        
        log.info("Getting cross-sell recommendations for cart products: {} with limit: {}", 
                cartProductIds, limit);
        
        try {
            List<ProductRecommendation> recommendations = new ArrayList<>();
            // Generate cross-sell recommendations for each product
            for (String productId : cartProductIds) {
                List<ProductRecommendation> recs = 
                    recommendationEngine.generateCrossSellRecommendations(userId, productId, limit / cartProductIds.size());
                recommendations.addAll(recs);
            }
            
            log.info("Generated {} cross-sell recommendations for cart", recommendations.size());
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("Error getting cross-sell recommendations for cart: {}", cartProductIds, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get up-sell recommendations (higher-value alternatives).
     */
    @GetMapping("/up-sell")
    @Operation(summary = "Get up-sell recommendations", 
               description = "Get higher-value alternatives to cart items")
    public ResponseEntity<List<ProductRecommendation>> getUpSellRecommendations(
            @Parameter(description = "Product IDs in cart", required = true)
            @RequestParam List<String> cartProductIds,
            
            @Parameter(description = "User ID for personalization")
            @RequestParam(required = false) String userId,
            
            @Parameter(description = "Maximum number of recommendations (1-20)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        
        log.info("Getting up-sell recommendations for cart products: {} with limit: {}", 
                cartProductIds, limit);
        
        try {
            List<ProductRecommendation> recommendations = new ArrayList<>();
            // Generate up-sell recommendations for each product
            for (String productId : cartProductIds) {
                List<ProductRecommendation> recs = 
                    recommendationEngine.generateUpSellRecommendations(userId, productId, limit / cartProductIds.size());
                recommendations.addAll(recs);
            }
            
            log.info("Generated {} up-sell recommendations for cart", recommendations.size());
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("Error getting up-sell recommendations for cart: {}", cartProductIds, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get abandoned cart recovery recommendations.
     */
    @GetMapping("/users/{userId}/abandoned-cart")
    @Operation(summary = "Get abandoned cart recommendations", 
               description = "Get recommendations to recover abandoned cart items")
    public ResponseEntity<List<ProductRecommendation>> getAbandonedCartRecommendations(
            @Parameter(description = "User ID", required = true)
            @PathVariable @NotBlank String userId,
            
            @Parameter(description = "Maximum number of recommendations (1-20)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        
        log.info("Getting abandoned cart recommendations for user: {} with limit: {}", userId, limit);
        
        try {
            List<ProductRecommendation> recommendations = 
                recommendationEngine.generateAbandonedCartRecommendations(userId, limit);
            
            log.info("Generated {} abandoned cart recommendations for user: {}", 
                    recommendations.size(), userId);
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("Error getting abandoned cart recommendations for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Track user behavior/interaction.
     */
    @PostMapping("/behavior")
    @Operation(summary = "Track user behavior", 
               description = "Record user interaction with products for recommendation learning")
    public ResponseEntity<Void> trackUserBehavior(
            @Parameter(description = "User behavior data", required = true)
            @Valid @RequestBody UserBehaviorRequest request) {
        
        log.info("Tracking user behavior: {} for user: {} on product: {}", 
                request.getActionType(), request.getUserId(), request.getProductId());
        
        try {
            Map<String, Object> metadata = new HashMap<>();
             metadata.put("sessionId", request.getSessionId());
             metadata.put("deviceType", request.getDeviceType());
             metadata.put("location", request.getLocation());
             metadata.put("referrer", request.getReferrer());
             metadata.put("duration", request.getDuration());
             metadata.put("rating", request.getRating());
             metadata.put("quantity", request.getQuantity());
             metadata.put("price", request.getPrice());
             
             UserBehavior behavior = userBehaviorService.recordBehavior(
                     request.getUserId(),
                     request.getProductId(),
                     request.getActionType(),
                     metadata
             );
            
            log.info("Successfully tracked user behavior for user: {}", request.getUserId());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error tracking user behavior for user: {}", request.getUserId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get user's recommendation history.
     */
    @GetMapping("/users/{userId}/history")
    @Operation(summary = "Get recommendation history", 
               description = "Get user's recommendation history with pagination")
    public ResponseEntity<Page<ProductRecommendation>> getRecommendationHistory(
            @Parameter(description = "User ID", required = true)
            @PathVariable @NotBlank String userId,
            
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        
        log.info("Getting recommendation history for user: {}", userId);
        
        try {
            // This would typically be implemented in a RecommendationHistoryService
            // For now, return empty page
            return ResponseEntity.ok(Page.empty(pageable));
            
        } catch (Exception e) {
            log.error("Error getting recommendation history for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get user behavior analytics.
     */
    @GetMapping("/users/{userId}/analytics")
    @Operation(summary = "Get user behavior analytics", 
               description = "Get analytics and insights about user behavior")
    public ResponseEntity<Map<String, Object>> getUserBehaviorAnalytics(
            @Parameter(description = "User ID", required = true)
            @PathVariable @NotBlank String userId) {
        
        log.info("Getting behavior analytics for user: {}", userId);
        
        try {
            Map<String, Object> analytics = userBehaviorService.getUserBehaviorStats(userId);
            
            log.info("Retrieved behavior analytics for user: {}", userId);
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            log.error("Error getting behavior analytics for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get recently viewed products for a user.
     */
    @GetMapping("/users/{userId}/recently-viewed")
    @Operation(summary = "Get recently viewed products", 
               description = "Get user's recently viewed products")
    public ResponseEntity<List<String>> getRecentlyViewedProducts(
            @Parameter(description = "User ID", required = true)
            @PathVariable @NotBlank String userId,
            
            @Parameter(description = "Maximum number of products (1-50)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.info("Getting recently viewed products for user: {} with limit: {}", userId, limit);
        
        try {
            List<String> recentlyViewed = userBehaviorService.getRecentlyViewedProducts(userId, limit);
            
            log.info("Retrieved {} recently viewed products for user: {}", 
                    recentlyViewed.size(), userId);
            return ResponseEntity.ok(recentlyViewed);
            
        } catch (Exception e) {
            log.error("Error getting recently viewed products for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get user's preferred categories.
     */
    @GetMapping("/users/{userId}/preferred-categories")
    @Operation(summary = "Get preferred categories", 
               description = "Get user's preferred product categories based on behavior")
    public ResponseEntity<List<String>> getPreferredCategories(
            @Parameter(description = "User ID", required = true)
            @PathVariable @NotBlank String userId,
            
            @Parameter(description = "Maximum number of categories (1-20)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        
        log.info("Getting preferred categories for user: {} with limit: {}", userId, limit);
        
        try {
            List<String> preferredCategories = userBehaviorService.getUserPreferredCategories(userId, limit);
            
            log.info("Retrieved {} preferred categories for user: {}", 
                    preferredCategories.size(), userId);
            return ResponseEntity.ok(preferredCategories);
            
        } catch (Exception e) {
            log.error("Error getting preferred categories for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Request DTO for user behavior tracking.
     */
    public static class UserBehaviorRequest {
        @NotBlank
        private String userId;
        
        @NotBlank
        private String productId;
        
        private UserBehavior.ActionType actionType;
        private String sessionId;
        private String deviceType;
        private String location;
        private String referrer;
        private Integer duration;
        private Double rating;
        private Integer quantity;
        private Double price;
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public UserBehavior.ActionType getActionType() { return actionType; }
        public void setActionType(UserBehavior.ActionType actionType) { this.actionType = actionType; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getReferrer() { return referrer; }
        public void setReferrer(String referrer) { this.referrer = referrer; }
        
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
}