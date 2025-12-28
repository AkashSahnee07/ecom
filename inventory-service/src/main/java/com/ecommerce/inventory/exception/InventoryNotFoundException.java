package com.ecommerce.inventory.exception;

public class InventoryNotFoundException extends RuntimeException {
    private String field;
    private String value;
    private String additionalField;
    private String additionalValue;
    
    public InventoryNotFoundException(String field, String value) {
        super(String.format("Inventory not found with %s: %s", field, value));
        this.field = field;
        this.value = value;
    }
    
    public InventoryNotFoundException(String field, String value, String additionalField, String additionalValue) {
        super(String.format("Inventory not found with %s: %s and %s: %s", 
                          field, value, additionalField, additionalValue));
        this.field = field;
        this.value = value;
        this.additionalField = additionalField;
        this.additionalValue = additionalValue;
    }
    
    public InventoryNotFoundException(String message) {
        super(message);
    }
    
    public InventoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Getters
    public String getField() {
        return field;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getAdditionalField() {
        return additionalField;
    }
    
    public String getAdditionalValue() {
        return additionalValue;
    }
}