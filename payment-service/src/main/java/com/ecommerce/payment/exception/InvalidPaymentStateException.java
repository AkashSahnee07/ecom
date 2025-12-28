package com.ecommerce.payment.exception;

import com.ecommerce.payment.entity.PaymentStatus;

public class InvalidPaymentStateException extends RuntimeException {
    
    private String paymentId;
    private PaymentStatus currentStatus;
    private String operation;
    
    public InvalidPaymentStateException(String message) {
        super(message);
    }
    
    public InvalidPaymentStateException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidPaymentStateException(String paymentId, PaymentStatus currentStatus, String operation) {
        super("Cannot perform operation '" + operation + "' on payment " + paymentId + 
              " with current status: " + currentStatus);
        this.paymentId = paymentId;
        this.currentStatus = currentStatus;
        this.operation = operation;
    }
    
    public InvalidPaymentStateException(String paymentId, PaymentStatus currentStatus, 
                                       PaymentStatus requiredStatus, String operation) {
        super("Cannot perform operation '" + operation + "' on payment " + paymentId + 
              ". Current status: " + currentStatus + ", Required status: " + requiredStatus);
        this.paymentId = paymentId;
        this.currentStatus = currentStatus;
        this.operation = operation;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public PaymentStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public String getOperation() {
        return operation;
    }
}