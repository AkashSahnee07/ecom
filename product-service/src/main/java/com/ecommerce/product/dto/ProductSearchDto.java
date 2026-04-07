package com.ecommerce.product.dto;

import java.math.BigDecimal;

public class ProductSearchDto {
    
    private String keyword;
    private Long categoryId;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private Boolean featured;
    private Boolean inStock;
    
    // Constructors
    public ProductSearchDto() {}
    
    public ProductSearchDto(String keyword, Long categoryId, String brand, 
                           BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minRating) {
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.brand = brand;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minRating = minRating;
    }
    
    // Getters and Setters
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public BigDecimal getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }
    
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public BigDecimal getMinRating() {
        return minRating;
    }
    
    public void setMinRating(BigDecimal minRating) {
        this.minRating = minRating;
    }
    
    public Boolean getFeatured() {
        return featured;
    }
    
    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }
    
    public Boolean getInStock() {
        return inStock;
    }
    
    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }
}
