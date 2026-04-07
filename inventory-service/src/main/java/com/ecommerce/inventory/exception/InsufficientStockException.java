package com.ecommerce.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    private String productId;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    
    public InsufficientStockException(String productId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient stock for product %s. Requested: %d, Available: %d", 
                          productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Getters
    public String getProductId() {
        return productId;
    }
    
    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
