package com.ecommerce.recommendation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Entity representing user profiles for personalized recommendations.
 * Stores user preferences, behavior patterns, and demographic information.
 */
@Entity
@Table(name = "user_profiles", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_segment_active", columnList = "user_segment, is_active"),
    @Index(name = "idx_location_age", columnList = "location, age_group"),
    @Index(name = "idx_updated_at", columnList = "updated_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group")
    private AgeGroup ageGroup;

    @Column(name = "location")
    private String location;

    @Column(name = "country")
    private String country;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "language_preference")
    private String languagePreference;

    @Column(name = "currency_preference")
    private String currencyPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_segment")
    private UserSegment userSegment;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_spent")
    private Double totalSpent = 0.0;

    @Column(name = "average_order_value")
    private Double averageOrderValue;

    @Column(name = "lifetime_value")
    private Double lifetimeValue;

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @ElementCollection
    @CollectionTable(name = "user_preferred_categories", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "category_id")
    private Set<String> preferredCategories;

    @ElementCollection
    @CollectionTable(name = "user_preferred_brands", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "brand_id")
    private Set<String> preferredBrands;

    @ElementCollection
    @CollectionTable(name = "user_price_preferences", joinColumns = @JoinColumn(name = "user_profile_id"))
    @MapKeyColumn(name = "preference_key")
    @Column(name = "preference_value")
    private Map<String, String> pricePreferences;

    @ElementCollection
    @CollectionTable(name = "user_behavior_scores", joinColumns = @JoinColumn(name = "user_profile_id"))
    @MapKeyColumn(name = "behavior_type")
    @Column(name = "score")
    private Map<String, Double> behaviorScores;

    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "interest")
    private List<String> interests;

    @Column(name = "browsing_pattern")
    private String browsingPattern;

    @Column(name = "shopping_frequency")
    private String shoppingFrequency;

    @Column(name = "preferred_shopping_time")
    private String preferredShoppingTime;

    @Column(name = "device_preference")
    private String devicePreference;

    @Column(name = "notification_preferences")
    private String notificationPreferences;

    @Column(name = "privacy_level")
    private String privacyLevel;

    @Column(name = "recommendation_feedback_score")
    private Double recommendationFeedbackScore;

    @Column(name = "click_through_rate")
    private Double clickThroughRate;

    @Column(name = "conversion_rate")
    private Double conversionRate;

    @Column(name = "return_rate")
    private Double returnRate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_premium")
    private Boolean isPremium = false;

    @Column(name = "marketing_consent")
    private Boolean marketingConsent = false;

    @Column(name = "personalization_enabled")
    private Boolean personalizationEnabled = true;

    @Column(name = "engagement_score")
    private Double engagementScore;

    @Column(name = "browsing_score")
    private Double browsingScore;

    @Column(name = "allow_personalized_recommendations")
    private Boolean allowPersonalizedRecommendations = true;

    @Column(name = "allow_email_notifications")
    private Boolean allowEmailNotifications = false;

    @Column(name = "allow_push_notifications")
    private Boolean allowPushNotifications = false;

    @Column(name = "loyalty_score")
    private Double loyaltyScore;

    @Column(name = "purchase_score")
    private Double purchaseScore;

    @Column(name = "social_score")
    private Double socialScore;

    @Column(name = "days_since_last_order")
    private Integer daysSinceLastOrder;

    @Column(name = "days_since_registration")
    private Integer daysSinceRegistration;

    @Column(name = "login_frequency")
    private Double loginFrequency;

    @Column(name = "session_duration")
    private Double sessionDuration;

    @Column(name = "page_views_per_session")
    private Double pageViewsPerSession;

    @Column(name = "bounce_rate")
    private Double bounceRate;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum representing user gender.
     */
    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    /**
     * Enum representing user age groups.
     */
    public enum AgeGroup {
        TEEN("13-17", "Teen"),
        YOUNG_ADULT("18-24", "Young Adult"),
        ADULT("25-34", "Adult"),
        MIDDLE_AGED("35-44", "Middle Aged"),
        MATURE("45-54", "Mature"),
        SENIOR("55-64", "Senior"),
        ELDERLY("65+", "Elderly");

        private final String range;
        private final String displayName;

        AgeGroup(String range, String displayName) {
            this.range = range;
            this.displayName = displayName;
        }

        public String getRange() {
            return range;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum representing user segments for targeted recommendations.
     */
    public enum UserSegment {
        NEW_USER("New User", "Recently registered users"),
        CASUAL_SHOPPER("Casual Shopper", "Infrequent shoppers"),
        REGULAR_CUSTOMER("Regular Customer", "Frequent shoppers"),
        VIP_CUSTOMER("VIP Customer", "High-value customers"),
        BARGAIN_HUNTER("Bargain Hunter", "Price-sensitive shoppers"),
        BRAND_LOYALIST("Brand Loyalist", "Brand-focused shoppers"),
        IMPULSE_BUYER("Impulse Buyer", "Quick decision makers"),
        RESEARCH_ORIENTED("Research Oriented", "Thorough researchers"),
        SEASONAL_SHOPPER("Seasonal Shopper", "Holiday/event shoppers"),
        DORMANT_USER("Dormant User", "Inactive users"),
        CHURNED_USER("Churned User", "Lost customers");

        private final String displayName;
        private final String description;

        UserSegment(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public boolean isActiveSegment() {
            return this != DORMANT_USER && this != CHURNED_USER;
        }

        public boolean isHighValueSegment() {
            return this == VIP_CUSTOMER || this == REGULAR_CUSTOMER;
        }
    }

    // Business methods
    public boolean isNewUser() {
        return userSegment == UserSegment.NEW_USER || 
               (registrationDate != null && registrationDate.isAfter(LocalDateTime.now().minusDays(30)));
    }

    public boolean isHighValueCustomer() {
        return userSegment != null && userSegment.isHighValueSegment();
    }

    public boolean isActiveUser() {
        return isActive && lastLoginDate != null && 
               lastLoginDate.isAfter(LocalDateTime.now().minusDays(30));
    }

    public boolean isDormantUser() {
        return lastLoginDate == null || 
               lastLoginDate.isBefore(LocalDateTime.now().minusDays(90));
    }

    public boolean hasRecentPurchase() {
        return lastOrderDate != null && 
               lastOrderDate.isAfter(LocalDateTime.now().minusDays(30));
    }

    public boolean isPriceConscious() {
        return userSegment == UserSegment.BARGAIN_HUNTER;
    }

    public boolean isBrandLoyal() {
        return userSegment == UserSegment.BRAND_LOYALIST;
    }

    public boolean hasGoodRecommendationFeedback() {
        return recommendationFeedbackScore != null && recommendationFeedbackScore >= 3.5;
    }

    public boolean hasHighEngagement() {
        return clickThroughRate != null && clickThroughRate >= 0.05 &&
               conversionRate != null && conversionRate >= 0.02;
    }

    public void updateOrderStats(Double orderValue) {
        this.totalOrders = (this.totalOrders == null ? 0 : this.totalOrders) + 1;
        this.totalSpent = (this.totalSpent == null ? 0.0 : this.totalSpent) + orderValue;
        this.averageOrderValue = this.totalSpent / this.totalOrders;
        this.lastOrderDate = LocalDateTime.now();
        updateUserSegment();
    }

    public void updateLoginActivity() {
        this.lastLoginDate = LocalDateTime.now();
    }

    public void addPreferredCategory(String categoryId) {
        if (preferredCategories != null) {
            preferredCategories.add(categoryId);
        }
    }

    public void addPreferredBrand(String brandId) {
        if (preferredBrands != null) {
            preferredBrands.add(brandId);
        }
    }

    public void updateBehaviorScore(String behaviorType, Double score) {
        if (behaviorScores != null) {
            behaviorScores.put(behaviorType, score);
        }
    }

    public Double getBehaviorScore(String behaviorType) {
        return behaviorScores != null ? behaviorScores.get(behaviorType) : 0.0;
    }

    public void updateRecommendationFeedback(Double feedbackScore) {
        if (this.recommendationFeedbackScore == null) {
            this.recommendationFeedbackScore = feedbackScore;
        } else {
            // Weighted average with more weight on recent feedback
            this.recommendationFeedbackScore = (this.recommendationFeedbackScore * 0.7) + (feedbackScore * 0.3);
        }
    }

    public void updateEngagementMetrics(Double ctr, Double cr) {
        this.clickThroughRate = ctr;
        this.conversionRate = cr;
    }

    private void updateUserSegment() {
        if (totalOrders == null || totalOrders == 0) {
            this.userSegment = UserSegment.NEW_USER;
        } else if (totalOrders >= 20 && (totalSpent != null && totalSpent >= 2000)) {
            this.userSegment = UserSegment.VIP_CUSTOMER;
        } else if (totalOrders >= 5) {
            this.userSegment = UserSegment.REGULAR_CUSTOMER;
        } else {
            this.userSegment = UserSegment.CASUAL_SHOPPER;
        }
    }

    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (email != null) {
            return email.split("@")[0];
        }
        return "User " + userId;
    }

    public boolean canReceivePersonalizedRecommendations() {
        return isActive && personalizationEnabled && 
               (privacyLevel == null || !privacyLevel.equals("STRICT"));
    }

    public int getDaysSinceLastOrder() {
        if (lastOrderDate == null) return Integer.MAX_VALUE;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(lastOrderDate, LocalDateTime.now());
    }

    public int getDaysSinceLastLogin() {
        if (lastLoginDate == null) return Integer.MAX_VALUE;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(lastLoginDate.toLocalDate(), LocalDateTime.now().toLocalDate());
    }

    // Explicit getter methods for compilation
    public Set<String> getPreferredCategories() {
        return this.preferredCategories;
    }

    public Set<String> getPreferredBrands() {
        return this.preferredBrands;
    }

    public UserSegment getUserSegment() {
        return this.userSegment;
    }

    // Explicit getter methods for compilation issues
    public String getUserId() {
        return this.userId;
    }

    public UUID getId() {
        return this.id;
    }

    public void setPreferredCategories(List<String> categories) {
        this.preferredCategories = categories != null ? Set.copyOf(categories) : Set.of();
    }

    public void setPreferredBrands(List<String> brands) {
        this.preferredBrands = brands != null ? Set.copyOf(brands) : Set.of();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setAgeGroup(AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setEngagementScore(Double engagementScore) {
        this.engagementScore = engagementScore;
    }

    public void setBrowsingScore(double browsingScore) {
        this.browsingScore = browsingScore;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserSegment(UserSegment userSegment) {
        this.userSegment = userSegment;
    }

    public void setAllowPersonalizedRecommendations(boolean allowPersonalizedRecommendations) {
        this.allowPersonalizedRecommendations = allowPersonalizedRecommendations;
    }

    public void setAllowEmailNotifications(boolean allowEmailNotifications) {
        this.allowEmailNotifications = allowEmailNotifications;
    }

    public void setAllowPushNotifications(boolean allowPushNotifications) {
        this.allowPushNotifications = allowPushNotifications;
    }

    public void setPrivacyLevel(String privacyLevel) {
        this.privacyLevel = privacyLevel;
    }

    public void setLoyaltyScore(Double loyaltyScore) {
        this.loyaltyScore = loyaltyScore;
    }

    public void setPurchaseScore(Double purchaseScore) {
        this.purchaseScore = purchaseScore;
    }

    public void setSocialScore(Double socialScore) {
        this.socialScore = socialScore;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public void setAverageOrderValue(Double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public void setDaysSinceLastOrder(Integer daysSinceLastOrder) {
        this.daysSinceLastOrder = daysSinceLastOrder;
    }

    public void setDaysSinceRegistration(Integer daysSinceRegistration) {
        this.daysSinceRegistration = daysSinceRegistration;
    }

    public void setLoginFrequency(Double loginFrequency) {
        this.loginFrequency = loginFrequency;
    }

    public void setSessionDuration(Double sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public void setPageViewsPerSession(Double pageViewsPerSession) {
        this.pageViewsPerSession = pageViewsPerSession;
    }

    public void setBounceRate(Double bounceRate) {
        this.bounceRate = bounceRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setPersonalizationEnabled(boolean personalizationEnabled) {
        this.personalizationEnabled = personalizationEnabled;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public Double getEngagementScore() {
        return engagementScore;
    }

    public Long getDaysSinceRegistration() {
        if (registrationDate == null) {
            return 0L;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(registrationDate.toLocalDate(), LocalDateTime.now().toLocalDate());
    }

    public Double getAverageOrderValue() {
        return averageOrderValue;
    }
}