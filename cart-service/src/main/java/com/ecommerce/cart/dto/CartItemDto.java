package com.ecommerce.cart.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItemDto {
    
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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addedAt;
    
    // Constructors
    public CartItemDto() {}
    
    public CartItemDto(Long productId, String productName, String productSku, 
                      BigDecimal price, Integer quantity, BigDecimal subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }
    
    public CartItemDto(Long productId, String productName, String productSku, 
                      BigDecimal price, Integer quantity, BigDecimal subtotal, 
                      String imageUrl, String brand, Long categoryId, 
                      String categoryName, LocalDateTime addedAt) {
        this(productId, productName, productSku, price, quantity, subtotal);
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.addedAt = addedAt;
    }
    
    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
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
}