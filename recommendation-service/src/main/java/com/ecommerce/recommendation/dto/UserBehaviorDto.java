package com.ecommerce.recommendation.dto;

import com.ecommerce.recommendation.entity.UserBehavior.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for user behavior tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorDto {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Action type is required")
    private ActionType actionType;
    
    private String sessionId;
    private String deviceType;
    private String source;
    private Integer duration;
    private Double rating;
    private Integer quantity;
    private Double price;
    private String category;
    private String brand;
    private LocalDateTime timestamp;
    private Map<String, Object> contextData;
    
    // Request tracking
    private String userAgent;
    private String ipAddress;
    private String referrer;
    
    // Additional metadata
    private String campaignId;
    private String experimentId;
    private String abTestGroup;
}