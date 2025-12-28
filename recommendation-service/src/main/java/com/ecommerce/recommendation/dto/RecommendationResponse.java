package com.ecommerce.recommendation.dto;

import com.ecommerce.recommendation.entity.ProductRecommendation.AlgorithmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for product recommendations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    
    private Long userId;
    private List<RecommendationItem> recommendations;
    private AlgorithmType algorithmUsed;
    private Integer totalRecommendations;
    private Double averageScore;
    private Double averageConfidence;
    private LocalDateTime generatedAt;
    private Long processingTimeMs;
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private Long productId;
        private String productName;
        private String productDescription;
        private String category;
        private String brand;
        private Double price;
        private String imageUrl;
        private Double score;
        private Double confidence;
        private AlgorithmType algorithmType;
        private String reason;
        private Map<String, Object> attributes;
        private LocalDateTime recommendedAt;
        
        // Product details from external service
        private Boolean inStock;
        private Integer stockQuantity;
        private Double rating;
        private Integer reviewCount;
        private Double discount;
        private List<String> tags;
    }
}