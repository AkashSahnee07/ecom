package com.ecommerce.recommendation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.UUID;

import java.time.LocalDateTime;

/**
 * Entity representing user behavior data for recommendation algorithms.
 * Tracks various user interactions with products to build recommendation models.
 */
@Entity
@Table(name = "user_behaviors", indexes = {
    @Index(name = "idx_user_timestamp", columnList = "user_id, timestamp"),
    @Index(name = "idx_product_action", columnList = "product_id, action_type"),
    @Index(name = "idx_session_timestamp", columnList = "session_id, timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "session_id")
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Double price;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "brand_id")
    private String brandId;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "referrer")
    private String referrer;

    @Column(name = "search_query")
    private String searchQuery;

    @Column(name = "page_url")
    private String pageUrl;

    @Column(name = "weight")
    private Double weight = 1.0;

    @Column(name = "processed")
    private Boolean processed = false;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum representing different types of user actions.
     */
    public enum ActionType {
        VIEW("Product View", 1.0),
        CLICK("Product Click", 1.2),
        ADD_TO_CART("Add to Cart", 2.0),
        REMOVE_FROM_CART("Remove from Cart", -0.5),
        ADD_TO_WISHLIST("Add to Wishlist", 1.5),
        REMOVE_FROM_WISHLIST("Remove from Wishlist", -0.3),
        PURCHASE("Purchase", 5.0),
        REVIEW("Review", 3.0),
        SHARE("Share", 2.5),
        SEARCH("Search", 0.8),
        COMPARE("Compare", 1.8),
        QUICK_VIEW("Quick View", 0.9),
        IMAGE_ZOOM("Image Zoom", 0.7),
        VIDEO_PLAY("Video Play", 1.3),
        DOWNLOAD("Download", 2.2),
        BOOKMARK("Bookmark", 1.4),
        RECOMMENDATION_CLICK("Recommendation Click", 1.1);

        private final String displayName;
        private final Double defaultWeight;

        ActionType(String displayName, Double defaultWeight) {
            this.displayName = displayName;
            this.defaultWeight = defaultWeight;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Double getDefaultWeight() {
            return defaultWeight;
        }

        public boolean isPositiveAction() {
            return defaultWeight > 0;
        }

        public boolean isNegativeAction() {
            return defaultWeight < 0;
        }

        public boolean isHighValueAction() {
            return defaultWeight >= 2.0;
        }

        public boolean isPurchaseRelated() {
            return this == PURCHASE || this == ADD_TO_CART || this == REMOVE_FROM_CART;
        }

        public boolean isEngagementAction() {
            return this == VIEW || this == CLICK || this == QUICK_VIEW || 
                   this == IMAGE_ZOOM || this == VIDEO_PLAY;
        }
    }

    // Business methods
    public boolean isRecentBehavior(int hours) {
        return timestamp.isAfter(LocalDateTime.now().minusHours(hours));
    }

    public boolean isHighValueAction() {
        return actionType.isHighValueAction();
    }

    public boolean isPurchaseAction() {
        return actionType == ActionType.PURCHASE;
    }

    public boolean isEngagementAction() {
        return actionType.isEngagementAction();
    }

    public Double getCalculatedWeight() {
        return weight != null ? weight : actionType.getDefaultWeight();
    }

    public boolean isValidForRecommendation() {
        return userId != null && productId != null && 
               actionType.isPositiveAction() && !processed;
    }

    public void markAsProcessed() {
        this.processed = true;
    }

    public boolean isFromMobileDevice() {
        return deviceType != null && 
               (deviceType.toLowerCase().contains("mobile") || 
                deviceType.toLowerCase().contains("android") ||
                deviceType.toLowerCase().contains("iphone"));
    }

    public boolean isFromSearch() {
        return searchQuery != null && !searchQuery.trim().isEmpty();
    }

    public boolean hasValidSession() {
        return sessionId != null && !sessionId.trim().isEmpty();
    }

    public void calculateInteractionScore() {
        if (actionType != null) {
            this.weight = actionType.getDefaultWeight();
        } else {
            this.weight = 1.0;
        }
    }

    public Double getInteractionScore() {
        return this.weight;
    }

    public UUID getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getProductId() {
        return this.productId;
    }

    public ActionType getActionType() {
        return this.actionType;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getBehaviorType() {
        return this.actionType != null ? this.actionType.name() : null;
    }

    public String getProductCategory() {
        return this.categoryId;
    }

    public String getBrandId() {
        return this.brandId;
    }

    public Integer getDuration() {
        return this.durationSeconds;
    }

    public static UserBehaviorBuilder builder() {
        return new UserBehaviorBuilder();
    }

    public static class UserBehaviorBuilder {
        private String userId;
        private String productId;
        private String sessionId;
        private ActionType actionType;
        private LocalDateTime timestamp;
        private Integer durationSeconds;
        private Integer quantity;
        private Double price;
        private String categoryId;
        private String brandId;
        private String deviceType;
        private String ipAddress;
        private String userAgent;
        private String referrer;
        private String searchQuery;
        private String pageUrl;

        public UserBehaviorBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserBehaviorBuilder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public UserBehaviorBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public UserBehaviorBuilder actionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public UserBehaviorBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public UserBehaviorBuilder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public UserBehaviorBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public UserBehaviorBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public UserBehaviorBuilder categoryId(String categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public UserBehaviorBuilder brandId(String brandId) {
            this.brandId = brandId;
            return this;
        }

        public UserBehaviorBuilder deviceType(String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public UserBehaviorBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public UserBehaviorBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public UserBehaviorBuilder referrer(String referrer) {
            this.referrer = referrer;
            return this;
        }

        public UserBehaviorBuilder searchQuery(String searchQuery) {
            this.searchQuery = searchQuery;
            return this;
        }

        public UserBehaviorBuilder pageUrl(String pageUrl) {
            this.pageUrl = pageUrl;
            return this;
        }

        public UserBehavior build() {
            UserBehavior behavior = new UserBehavior();
            behavior.userId = this.userId;
            behavior.productId = this.productId;
            behavior.sessionId = this.sessionId;
            behavior.actionType = this.actionType;
            behavior.timestamp = this.timestamp;
            behavior.durationSeconds = this.durationSeconds;
            behavior.quantity = this.quantity;
            behavior.price = this.price;
            behavior.categoryId = this.categoryId;
            behavior.brandId = this.brandId;
            behavior.deviceType = this.deviceType;
            behavior.ipAddress = this.ipAddress;
            behavior.userAgent = this.userAgent;
            behavior.referrer = this.referrer;
            behavior.searchQuery = this.searchQuery;
            behavior.pageUrl = this.pageUrl;
            behavior.weight = 1.0;
            behavior.processed = false;
            return behavior;
        }
    }
}
