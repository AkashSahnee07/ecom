package com.ecommerce.cart.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartSummaryDto {
    
    private Integer totalItems;
    private BigDecimal totalAmount;
    private Integer uniqueItems;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;
    
    // Constructors
    public CartSummaryDto() {}
    
    public CartSummaryDto(Integer totalItems, BigDecimal totalAmount, 
                         Integer uniqueItems, LocalDateTime lastUpdated) {
        this.totalItems = totalItems;
        this.totalAmount = totalAmount;
        this.uniqueItems = uniqueItems;
        this.lastUpdated = lastUpdated;
    }
    
    // Getters and setters
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public Integer getUniqueItems() { return uniqueItems; }
    public void setUniqueItems(Integer uniqueItems) { this.uniqueItems = uniqueItems; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}