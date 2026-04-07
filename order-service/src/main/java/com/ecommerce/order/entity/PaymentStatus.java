package com.ecommerce.order.entity;

public enum PaymentStatus {
    PENDING("Payment is pending"),
    PROCESSING("Payment is being processed"),
    COMPLETED("Payment has been completed"),
    FAILED("Payment has failed"),
    CANCELLED("Payment has been cancelled"),
    REFUNDED("Payment has been refunded"),
    PARTIALLY_REFUNDED("Payment has been partially refunded");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
    
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == REFUNDED;
    }
}
