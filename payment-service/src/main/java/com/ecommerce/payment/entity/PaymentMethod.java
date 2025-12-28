package com.ecommerce.payment.entity;

public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    PAYPAL("PayPal"),
    STRIPE("Stripe"),
    BANK_TRANSFER("Bank Transfer"),
    DIGITAL_WALLET("Digital Wallet"),
    APPLE_PAY("Apple Pay"),
    GOOGLE_PAY("Google Pay"),
    CRYPTOCURRENCY("Cryptocurrency"),
    CASH_ON_DELIVERY("Cash on Delivery");
    
    private final String displayName;
    
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean requiresOnlineProcessing() {
        return this != CASH_ON_DELIVERY;
    }
    
    public boolean supportsRefunds() {
        return this != CASH_ON_DELIVERY;
    }
    
    public boolean isDigitalWallet() {
        return this == DIGITAL_WALLET || this == APPLE_PAY || 
               this == GOOGLE_PAY || this == PAYPAL;
    }
    
    public boolean isCardPayment() {
        return this == CREDIT_CARD || this == DEBIT_CARD;
    }
}