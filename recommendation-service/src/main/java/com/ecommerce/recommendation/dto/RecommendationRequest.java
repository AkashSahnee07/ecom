package com.ecommerce.recommendation.dto;

import com.ecommerce.recommendation.entity.ProductRecommendation.AlgorithmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for generating product recommendations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit cannot exceed 100")
    @Builder.Default
    private Integer limit = 10;
    
    private AlgorithmType algorithmType;
    
    private List<Long> excludeProductIds;
    
    private List<String> includeCategories;
    
    private List<String> excludeCategories;
    
    @Min(value = 0, message = "Min price cannot be negative")
    private Double minPrice;
    
    @Min(value = 0, message = "Max price cannot be negative")
    private Double maxPrice;
    
    private List<String> brands;
    
    @Builder.Default
    private Boolean includePurchased = false;
    
    @Builder.Default
    private Boolean includeViewed = true;
    
    @Min(value = 0, message = "Diversity factor must be between 0 and 1")
    @Max(value = 1, message = "Diversity factor must be between 0 and 1")
    @Builder.Default
    private Double diversityFactor = 0.3;
    
    @Min(value = 0, message = "Freshness weight must be between 0 and 1")
    @Max(value = 1, message = "Freshness weight must be between 0 and 1")
    @Builder.Default
    private Double freshnessWeight = 0.1;
    
    private Map<String, Object> contextData;
    
    @Builder.Default
    private Boolean useCache = true;
}