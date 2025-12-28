package com.ecommerce.cart.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class AddToCartDto {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String productName;
    
    @NotBlank(message = "Product SKU is required")
    @Size(max = 100, message = "Product SKU must not exceed 100 characters")
    private String productSku;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999, message = "Quantity must not exceed 999")
    private Integer quantity;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;
    
    private Long categoryId;
    
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String categoryName;
    
    // Constructors
    public AddToCartDto() {}
    
    public AddToCartDto(Long productId, String productName, String productSku, 
                       BigDecimal price, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.price = price;
        this.quantity = quantity;
    }
    
    public AddToCartDto(Long productId, String productName, String productSku, 
                       BigDecimal price, Integer quantity, String imageUrl, 
                       String brand, Long categoryId, String categoryName) {
        this(productId, productName, productSku, price, quantity);
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
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
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}