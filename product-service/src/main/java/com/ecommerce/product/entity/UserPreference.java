package com.ecommerce.product.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_preferences")
public class UserPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ElementCollection
    @CollectionTable(name = "user_preferred_categories", joinColumns = @JoinColumn(name = "user_preference_id"))
    @Column(name = "category_id")
    private List<Long> preferredCategories;
    
    @ElementCollection
    @CollectionTable(name = "user_preferred_brands", joinColumns = @JoinColumn(name = "user_preference_id"))
    @Column(name = "brand")
    private List<String> preferredBrands;
    
    @Column(name = "min_price")
    private Double minPrice;
    
    @Column(name = "max_price")
    private Double maxPrice;
    
    @Column(name = "min_rating")
    private Double minRating;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public UserPreference() {}
    
    public UserPreference(Long userId, List<Long> preferredCategories, List<String> preferredBrands, 
                         Double minPrice, Double maxPrice, Double minRating) {
        this.userId = userId;
        this.preferredCategories = preferredCategories;
        this.preferredBrands = preferredBrands;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minRating = minRating;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public List<Long> getPreferredCategories() {
        return preferredCategories;
    }
    
    public void setPreferredCategories(List<Long> preferredCategories) {
        this.preferredCategories = preferredCategories;
    }
    
    public List<String> getPreferredBrands() {
        return preferredBrands;
    }
    
    public void setPreferredBrands(List<String> preferredBrands) {
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
