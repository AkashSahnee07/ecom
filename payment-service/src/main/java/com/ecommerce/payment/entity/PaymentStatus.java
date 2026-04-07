package com.ecommerce.payment.entity;

public enum PaymentStatus {
    PENDING("Payment is pending"),
    PROCESSING("Payment is being processed"),
    COMPLETED("Payment completed successfully"),
    FAILED("Payment failed"),
    CANCELLED("Payment was cancelled"),
    REFUNDED("Payment was fully refunded"),
    PARTIALLY_REFUNDED("Payment was partially refunded"),
    EXPIRED("Payment has expired");
    
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
        return this == COMPLETED || this == FAILED || 
               this == CANCELLED || this == REFUNDED || this == EXPIRED;
    }
    
    public boolean canBeRefunded() {
        return this == COMPLETED || this == PARTIALLY_REFUNDED;
    }
    
    public boolean isPending() {
        return this == PENDING || this == PROCESSING;
    }
}
