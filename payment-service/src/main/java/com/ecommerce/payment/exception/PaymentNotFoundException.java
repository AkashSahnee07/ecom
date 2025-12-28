package com.ecommerce.payment.exception;

public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PaymentNotFoundException(String field, String value) {
        super("Payment not found with " + field + ": " + value);
    }
}