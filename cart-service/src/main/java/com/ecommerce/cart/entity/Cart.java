package com.ecommerce.cart.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RedisHash(value = "cart", timeToLive = 86400) // 24 hours TTL
public class Cart implements Serializable {
    
    @Id
    private String id;
    
    @Indexed
    private Long userId;
    
    private List<CartItem> items = new ArrayList<>();
    
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    private Integer totalItems = 0;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructors
    public Cart() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Cart(Long userId) {
        this();
        this.userId = userId;
        this.id = "cart:" + userId;
    }
    
    // Helper methods
    public void addItem(CartItem item) {
        // Check if item already exists
        CartItem existingItem = items.stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            existingItem.setSubtotal(existingItem.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
        } else {
            items.add(item);
        }
        
        calculateTotals();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        calculateTotals();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateItemQuantity(Long productId, Integer quantity) {
        CartItem item = items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
        
        if (item != null) {
            if (quantity <= 0) {
                removeItem(productId);
            } else {
                item.setQuantity(quantity);
                item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(quantity)));
                calculateTotals();
                this.updatedAt = LocalDateTime.now();
            }
        }
    }
    
    public void clearCart() {
        items.clear();
        calculateTotals();
        this.updatedAt = LocalDateTime.now();
    }
    
    private void calculateTotals() {
        this.totalAmount = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { 
        this.items = items; 
        calculateTotals();
    }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
