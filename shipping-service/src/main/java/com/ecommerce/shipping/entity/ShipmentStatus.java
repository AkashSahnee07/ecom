package com.ecommerce.shipping.entity;

public enum ShipmentStatus {
    CREATED("Created", "Shipment has been created"),
    PROCESSING("Processing", "Shipment is being processed"),
    SHIPPED("Shipped", "Shipment has been shipped"),
    IN_TRANSIT("In Transit", "Shipment is in transit"),
    OUT_FOR_DELIVERY("Out for Delivery", "Shipment is out for delivery"),
    DELIVERED("Delivered", "Shipment has been delivered"),
    EXCEPTION("Exception", "Shipment has encountered an exception"),
    CANCELLED("Cancelled", "Shipment has been cancelled");
    
    private final String displayName;
    private final String description;
    
    ShipmentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Helper methods to check status categories
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
    
    public boolean isActive() {
        return this == PROCESSING || this == SHIPPED || this == IN_TRANSIT || this == OUT_FOR_DELIVERY;
    }
    
    public boolean isException() {
        return this == EXCEPTION;
    }
    
    public boolean canBeCancelled() {
        return this == CREATED || this == PROCESSING;
    }
}
