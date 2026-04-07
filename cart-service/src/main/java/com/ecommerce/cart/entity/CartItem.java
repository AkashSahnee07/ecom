package com.ecommerce.cart.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItem implements Serializable {
    
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String imageUrl;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime addedAt;
    
    // Constructors
    public CartItem() {
        this.addedAt = LocalDateTime.now();
    }
    
    public CartItem(Long productId, String productName, String productSku, 
                   BigDecimal price, Integer quantity) {
        this();
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }
    
    public CartItem(Long productId, String productName, String productSku, 
                   BigDecimal price, Integer quantity, String imageUrl, 
                   String brand, Long categoryId, String categoryName) {
        this(productId, productName, productSku, price, quantity);
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
    
    // Helper methods
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        this.subtotal = this.price.multiply(BigDecimal.valueOf(newQuantity));
    }
    
    public void updatePrice(BigDecimal newPrice) {
        this.price = newPrice;
        this.subtotal = newPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
    
    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { 
        this.price = price;
        if (this.quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        if (this.price != null) {
            this.subtotal = this.price.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return productId != null ? productId.equals(cartItem.productId) : cartItem.productId == null;
    }
    
    @Override
    public int hashCode() {
        return productId != null ? productId.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "CartItem{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + subtotal +
                '}';
    }
}
