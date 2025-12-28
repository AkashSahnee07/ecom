package com.ecommerce.order.entity;

public enum OrderStatus {
    PENDING("Order is pending confirmation"),
    CONFIRMED("Order has been confirmed"),
    PROCESSING("Order is being processed"),
    SHIPPED("Order has been shipped"),
    DELIVERED("Order has been delivered"),
    CANCELLED("Order has been cancelled"),
    RETURNED("Order has been returned"),
    REFUNDED("Order has been refunded");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this != CANCELLED && this != RETURNED && this != REFUNDED;
    }
    
    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED || this == RETURNED || this == REFUNDED;
    }
}