package com.ecommerce.order.exception;

import com.ecommerce.order.entity.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {
    
    public InvalidOrderStatusException(String message) {
        super(message);
    }
    
    public InvalidOrderStatusException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidOrderStatusException(OrderStatus currentStatus, OrderStatus newStatus) {
        super("Cannot change order status from " + currentStatus + " to " + newStatus);
    }
    
    public InvalidOrderStatusException(Long orderId, OrderStatus currentStatus, String operation) {
        super("Cannot perform " + operation + " on order " + orderId + " with status " + currentStatus);
    }
}