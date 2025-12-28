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
 * Entity representing similarity scores between products.
 * Used for content-based and hybrid recommendation algorithms.
 */
@Entity
@Table(name = "product_similarities", indexes = {
    @Index(name = "idx_source_score", columnList = "source_product_id, similarity_score"),
    @Index(name = "idx_target_score", columnList = "target_product_id, similarity_score"),
    @Index(name = "idx_type_category", columnList = "similarity_type, category_id"),
    @Index(name = "idx_updated_at", columnList = "updated_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_product_id", nullable = false)
    private String sourceProductId;

    @Column(name = "target_product_id", nullable = false)
    private String targetProductId;

    @Enumerated(EnumType.STRING)
    @Column(name = "similarity_type", nullable = false)
    private SimilarityType similarityType;

    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "subcategory_id")
    private String subcategoryId;

    @Column(name = "brand_id")
    private String brandId;

    @Column(name = "price_similarity")
    private Double priceSimilarity;

    @Column(name = "feature_similarity")
    private Double featureSimilarity;

    @Column(name = "description_similarity")
    private Double descriptionSimilarity;

    @Column(name = "rating_similarity")
    private Double ratingSimilarity;

    @Column(name = "behavioral_similarity")
    private Double behavioralSimilarity;

    @Column(name = "visual_similarity")
    private Double visualSimilarity;

    @Column(name = "tag_similarity")
    private Double tagSimilarity;

    @Column(name = "seasonal_similarity")
    private Double seasonalSimilarity;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "calculation_method")
    private String calculationMethod;

    @Column(name = "algorithm_version")
    private String algorithmVersion;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verification_score")
    private Double verificationScore;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "success_rate")
    private Double successRate;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ElementCollection
    @CollectionTable(name = "product_similarity_feature_weights", joinColumns = @JoinColumn(name = "similarity_id"))
    @MapKeyColumn(name = "feature_name")
    @Column(name = "weight_value")
    private Map<String, Double> featureWeights;

    @ElementCollection
    @CollectionTable(name = "product_similarity_metadata", joinColumns = @JoinColumn(name = "similarity_id"))
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
     * Enum representing different types of similarity calculations.
     */
    public enum SimilarityType {
        CONTENT_BASED("Content-Based", "Based on product attributes and features"),
        COLLABORATIVE("Collaborative", "Based on user behavior patterns"),
        HYBRID("Hybrid", "Combination of content and collaborative"),
        COSINE("Cosine Similarity", "Cosine similarity between feature vectors"),
        EUCLIDEAN("Euclidean Distance", "Euclidean distance between feature vectors"),
        JACCARD("Jaccard Index", "Jaccard similarity for categorical features"),
        PEARSON("Pearson Correlation", "Pearson correlation coefficient"),
        MANHATTAN("Manhattan Distance", "Manhattan distance between vectors"),
        SEMANTIC("Semantic Similarity", "Based on semantic analysis of descriptions"),
        VISUAL("Visual Similarity", "Based on image analysis and visual features"),
        BEHAVIORAL("Behavioral Similarity", "Based on user interaction patterns"),
        CATEGORY("Category-Based", "Based on category and taxonomy similarity"),
        BRAND("Brand-Based", "Based on brand relationships"),
        PRICE("Price-Based", "Based on price range similarity"),
        RATING("Rating-Based", "Based on rating and review similarity"),
        TEMPORAL("Temporal", "Based on time-based patterns"),
        LOCATION("Location-Based", "Based on geographical preferences"),
        CROSS_CATEGORY("Cross-Category", "Similarity across different categories");

        private final String displayName;
        private final String description;

        SimilarityType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public boolean isContentBased() {
            return this == CONTENT_BASED || this == SEMANTIC || this == VISUAL || 
                   this == CATEGORY || this == BRAND || this == PRICE;
        }

        public boolean isCollaborativeBased() {
            return this == COLLABORATIVE || this == BEHAVIORAL;
        }

        public boolean isHybrid() {
            return this == HYBRID;
        }

        public boolean isMathematical() {
            return this == COSINE || this == EUCLIDEAN || this == JACCARD || 
                   this == PEARSON || this == MANHATTAN;
        }

        public boolean requiresFeatureVectors() {
            return isMathematical() || this == CONTENT_BASED || this == SEMANTIC;
        }
    }

    // Business methods
    public boolean isHighSimilarity() {
        return similarityScore >= 0.8;
    }

    public boolean isModerateSimilarity() {
        return similarityScore >= 0.5 && similarityScore < 0.8;
    }

    public boolean isLowSimilarity() {
        return similarityScore < 0.5;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isReliable() {
        return isVerified && confidence != null && confidence >= 0.7;
    }

    public boolean isFrequentlyUsed() {
        return usageCount != null && usageCount >= 10;
    }

    public boolean hasGoodSuccessRate() {
        return successRate != null && successRate >= 0.6;
    }

    public boolean isValidSimilarity() {
        return isActive && !isExpired() && similarityScore > 0;
    }

    public boolean isSameBrand() {
        return brandId != null && !brandId.isEmpty();
    }

    public boolean isSameCategory() {
        return categoryId != null && !categoryId.isEmpty();
    }

    public boolean isCrossCategorySimilarity() {
        return similarityType == SimilarityType.CROSS_CATEGORY;
    }

    public void incrementUsage() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
        this.lastUsedAt = LocalDateTime.now();
    }

    public void updateSuccessRate(boolean wasSuccessful) {
        if (usageCount == null || usageCount == 0) {
            this.successRate = wasSuccessful ? 1.0 : 0.0;
        } else {
            double currentSuccesses = (successRate == null ? 0.0 : successRate) * usageCount;
            if (wasSuccessful) currentSuccesses++;
            this.successRate = currentSuccesses / (usageCount + 1);
        }
    }

    public void verify(Double verificationScore) {
        this.isVerified = true;
        this.verificationScore = verificationScore;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void extendExpiry(int days) {
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(days);
        } else {
            expiresAt = expiresAt.plusDays(days);
        }
    }

    public Double getWeightedSimilarityScore() {
        double weightedScore = similarityScore;
        
        if (confidence != null) {
            weightedScore *= confidence;
        }
        
        if (successRate != null && usageCount != null && usageCount > 5) {
            weightedScore *= (0.8 + (successRate * 0.2));
        }
        
        return Math.min(weightedScore, 1.0);
    }

    public String getFormattedScore() {
        return String.format("%.2f", similarityScore * 100) + "%";
    }

    public String getFormattedConfidence() {
        if (confidence == null) return "N/A";
        return String.format("%.1f", confidence * 100) + "%";
    }

    public String getSimilarityLevel() {
        if (isHighSimilarity()) return "High";
        if (isModerateSimilarity()) return "Moderate";
        return "Low";
    }

    public boolean isReciprocalTo(ProductSimilarity other) {
        return this.sourceProductId.equals(other.targetProductId) && 
               this.targetProductId.equals(other.sourceProductId) &&
               this.similarityType == other.similarityType;
    }

    public void updateScore(Double newScore, String method, String version) {
        this.similarityScore = newScore;
        this.calculationMethod = method;
        this.algorithmVersion = version;
        this.updatedAt = LocalDateTime.now();
    }

    public Double getFeatureWeight(String featureName) {
        return featureWeights != null ? featureWeights.get(featureName) : 0.0;
    }

    public void setFeatureWeight(String featureName, Double weight) {
        if (featureWeights != null) {
            featureWeights.put(featureName, weight);
        }
    }

    public Double getOverallFeatureSimilarity() {
        double total = 0.0;
        int count = 0;
        
        if (priceSimilarity != null) { total += priceSimilarity; count++; }
        if (featureSimilarity != null) { total += featureSimilarity; count++; }
        if (descriptionSimilarity != null) { total += descriptionSimilarity; count++; }
        if (ratingSimilarity != null) { total += ratingSimilarity; count++; }
        if (visualSimilarity != null) { total += visualSimilarity; count++; }
        if (tagSimilarity != null) { total += tagSimilarity; count++; }
        
        return count > 0 ? total / count : 0.0;
    }

    public boolean hasMinimumDataQuality() {
        return similarityScore > 0.1 && 
               (confidence == null || confidence >= 0.3) &&
               calculationMethod != null && !calculationMethod.isEmpty();
    }

    // Explicit getter methods for compilation
    public Double getPriceSimilarity() {
        return this.priceSimilarity;
    }

    public String getTargetProductId() {
        return this.targetProductId;
    }

    public String getSourceProductId() {
        return this.sourceProductId;
    }

    public SimilarityType getSimilarityType() {
        return this.similarityType;
    }

    public Double getSimilarityScore() {
        return this.similarityScore;
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
}