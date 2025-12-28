package com.ecommerce.recommendation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing product recommendations generated for users.
 * Stores precomputed recommendations with scores and metadata.
 */
@Entity
@Table(name = "product_recommendations", indexes = {
    @Index(name = "idx_user_score", columnList = "user_id, score"),
    @Index(name = "idx_algorithm_score", columnList = "algorithm_type, score"),
    @Index(name = "idx_active_expires", columnList = "is_active, expires_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "recommended_product_id", nullable = false)
    private String recommendedProductId;

    @Column(name = "source_product_id")
    private String sourceProductId;

    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm_type", nullable = false)
    private AlgorithmType algorithmType;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "brand_id")
    private String brandId;

    @Column(name = "price")
    private Double price;

    @Column(name = "discount_percentage")
    private Double discountPercentage;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_clicked")
    private Boolean isClicked = false;

    @Column(name = "is_purchased")
    private Boolean isPurchased = false;

    @Column(name = "click_count")
    private Integer clickCount = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "reason")
    private String reason;

    @ElementCollection
    @CollectionTable(name = "product_recommendation_metadata", joinColumns = @JoinColumn(name = "recommendation_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum representing different recommendation algorithms.
     */
    public enum AlgorithmType {
        COLLABORATIVE_FILTERING("Collaborative Filtering", "Users who liked this also liked"),
        CONTENT_BASED("Content-Based", "Similar products you might like"),
        HYBRID("Hybrid", "Recommended for you"),
        TRENDING("Trending", "Trending now"),
        POPULAR("Popular", "Popular products"),
        RECENTLY_VIEWED("Recently Viewed", "Continue browsing"),
        CATEGORY_BASED("Category-Based", "More in this category"),
        BRAND_BASED("Brand-Based", "More from this brand"),
        PRICE_BASED("Price-Based", "Similar price range"),
        SEASONAL("Seasonal", "Seasonal recommendations"),
        CROSS_SELL("Cross-Sell", "Frequently bought together"),
        UP_SELL("Up-Sell", "Upgrade options"),
        PERSONALIZED("Personalized", "Just for you"),
        LOCATION_BASED("Location-Based", "Popular in your area"),
        TIME_BASED("Time-Based", "Perfect timing");

        private final String displayName;
        private final String description;

        AlgorithmType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public boolean isPersonalized() {
            return this == COLLABORATIVE_FILTERING || this == HYBRID || 
                   this == PERSONALIZED || this == RECENTLY_VIEWED;
        }

        public boolean isContentBased() {
            return this == CONTENT_BASED || this == CATEGORY_BASED || 
                   this == BRAND_BASED || this == PRICE_BASED;
        }

        public boolean isTrendingBased() {
            return this == TRENDING || this == POPULAR || this == SEASONAL;
        }

        public boolean isBusinessRule() {
            return this == CROSS_SELL || this == UP_SELL;
        }
    }

    // Business methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isHighConfidence() {
        return confidence != null && confidence >= 0.8;
    }

    public boolean isHighScore() {
        return score >= 0.7;
    }

    public boolean isTopRanked() {
        return rankPosition != null && rankPosition <= 10;
    }

    public boolean hasBeenInteracted() {
        return isClicked || isPurchased || viewCount > 0;
    }

    public Double getClickThroughRate() {
        if (viewCount == 0) return 0.0;
        return (double) clickCount / viewCount;
    }

    public Double getConversionRate() {
        if (clickCount == 0) return 0.0;
        return isPurchased ? 1.0 / clickCount : 0.0;
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void incrementClickCount() {
        this.clickCount = (this.clickCount == null ? 0 : this.clickCount) + 1;
        this.isClicked = true;
    }

    public void markAsPurchased() {
        this.isPurchased = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void extendExpiry(int hours) {
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(hours);
        } else {
            expiresAt = expiresAt.plusHours(hours);
        }
    }

    public boolean isValidRecommendation() {
        return isActive && !isExpired() && score > 0;
    }

    public String getFormattedScore() {
        return String.format("%.2f", score * 100) + "%";
    }

    public String getFormattedConfidence() {
        if (confidence == null) return "N/A";
        return String.format("%.1f", confidence * 100) + "%";
    }

    public boolean isSimilarTo(ProductRecommendation other) {
        return this.userId.equals(other.userId) && 
               this.recommendedProductId.equals(other.recommendedProductId) &&
               this.algorithmType == other.algorithmType;
    }

    public void updateScore(Double newScore, String updateReason) {
        this.score = newScore;
        this.reason = updateReason;
        this.updatedAt = LocalDateTime.now();
    }

    // Explicit setter methods for compilation
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRecommendedProductId(String recommendedProductId) {
        this.recommendedProductId = recommendedProductId;
    }

    public void setSourceProductId(String sourceProductId) {
        this.sourceProductId = sourceProductId;
    }

    public void setAlgorithmType(AlgorithmType algorithmType) {
        this.algorithmType = algorithmType;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Double getScore() {
        return this.score;
    }

    public Double getPrice() {
        return this.price;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRecommendedProductId() {
        return this.recommendedProductId;
    }

    public Double getConfidence() {
        return this.confidence;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public String getBrandId() {
        return this.brandId;
    }

    public Double getDiscountPercentage() {
        return this.discountPercentage;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public AlgorithmType getAlgorithmType() {
        return this.algorithmType;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getReason() {
        return reason;
    }

    public static ProductRecommendationBuilder builder() {
        return new ProductRecommendationBuilder();
    }

    public static class ProductRecommendationBuilder {
        private String userId;
        private String recommendedProductId;
        private String sourceProductId;
        private AlgorithmType algorithmType;
        private Double score;
        private Double confidence;
        private Integer rankPosition;
        private String categoryId;
        private String brandId;
        private Double price;
        private String reason;
        private LocalDateTime expiresAt;
        private Map<String, String> metadata;

        public ProductRecommendationBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public ProductRecommendationBuilder recommendedProductId(String recommendedProductId) {
            this.recommendedProductId = recommendedProductId;
            return this;
        }

        public ProductRecommendationBuilder sourceProductId(String sourceProductId) {
            this.sourceProductId = sourceProductId;
            return this;
        }

        public ProductRecommendationBuilder algorithmType(AlgorithmType algorithmType) {
            this.algorithmType = algorithmType;
            return this;
        }

        public ProductRecommendationBuilder score(Double score) {
            this.score = score;
            return this;
        }

        public ProductRecommendationBuilder confidence(Double confidence) {
            this.confidence = confidence;
            return this;
        }

        public ProductRecommendationBuilder rankPosition(Integer rankPosition) {
            this.rankPosition = rankPosition;
            return this;
        }

        public ProductRecommendationBuilder categoryId(String categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public ProductRecommendationBuilder brandId(String brandId) {
            this.brandId = brandId;
            return this;
        }

        public ProductRecommendationBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public ProductRecommendationBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public ProductRecommendationBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public ProductRecommendationBuilder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ProductRecommendationBuilder createdAt(LocalDateTime createdAt) {
            // Note: createdAt is handled by JPA @CreationTimestamp
            return this;
        }

        public ProductRecommendationBuilder isActive(Boolean isActive) {
            // Note: isActive field will be set in build method
            return this;
        }

        public ProductRecommendation build() {
            ProductRecommendation recommendation = new ProductRecommendation();
            recommendation.userId = this.userId;
            recommendation.recommendedProductId = this.recommendedProductId;
            recommendation.sourceProductId = this.sourceProductId;
            recommendation.algorithmType = this.algorithmType;
            recommendation.score = this.score;
            recommendation.confidence = this.confidence;
            recommendation.rankPosition = this.rankPosition;
            recommendation.categoryId = this.categoryId;
            recommendation.brandId = this.brandId;
            recommendation.price = this.price;
            recommendation.reason = this.reason;
            recommendation.expiresAt = this.expiresAt;
            recommendation.metadata = this.metadata;
            return recommendation;
        }
    }
}