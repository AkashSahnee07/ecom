package com.ecommerce.payment.exception;

public class PaymentProcessingException extends RuntimeException {
    
    private String paymentId;
    private String errorCode;
    
    public PaymentProcessingException(String message) {
        super(message);
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PaymentProcessingException(String paymentId, String message) {
        super("Payment processing failed for payment ID " + paymentId + ": " + message);
        this.paymentId = paymentId;
    }
    
    public PaymentProcessingException(String paymentId, String message, String errorCode) {
        super("Payment processing failed for payment ID " + paymentId + ": " + message);
        this.paymentId = paymentId;
        this.errorCode = errorCode;
    }
    
    public PaymentProcessingException(String paymentId, String message, Throwable cause) {
        super("Payment processing failed for payment ID " + paymentId + ": " + message, cause);
        this.paymentId = paymentId;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}