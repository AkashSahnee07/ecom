package com.ecommerce.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CartMergeDto {
    
    @NotNull(message = "Items list is required")
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<CartItemDto> items;
    
    // Constructors
    public CartMergeDto() {}
    
    public CartMergeDto(List<CartItemDto> items) {
        this.items = items;
    }
    
    // Getters and setters
    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
}
