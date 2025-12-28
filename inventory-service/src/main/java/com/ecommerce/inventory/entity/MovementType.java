package com.ecommerce.inventory.entity;

public enum MovementType {
    INBOUND("Stock received/added"),
    OUTBOUND("Stock shipped/removed"),
    RESERVED("Stock reserved for order"),
    RELEASED("Reserved stock released"),
    CONFIRMED("Reserved stock confirmed/consumed"),
    ADJUSTMENT_POSITIVE("Positive stock adjustment"),
    ADJUSTMENT_NEGATIVE("Negative stock adjustment"),
    TRANSFER_IN("Stock transferred in from another warehouse"),
    TRANSFER_OUT("Stock transferred out to another warehouse"),
    DAMAGED("Stock marked as damaged"),
    EXPIRED("Stock marked as expired"),
    RETURNED("Stock returned from customer"),
    RECOUNT("Stock recount adjustment"),
    INITIAL("Initial stock setup");
    
    private final String description;
    
    MovementType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isInbound() {
        return this == INBOUND || this == TRANSFER_IN || this == RETURNED || 
               this == ADJUSTMENT_POSITIVE || this == INITIAL || this == RELEASED;
    }
    
    public boolean isOutbound() {
        return this == OUTBOUND || this == TRANSFER_OUT || this == DAMAGED || 
               this == EXPIRED || this == ADJUSTMENT_NEGATIVE || this == CONFIRMED;
    }
    
    public boolean isReservation() {
        return this == RESERVED || this == RELEASED || this == CONFIRMED;
    }
    
    public boolean isAdjustment() {
        return this == ADJUSTMENT_POSITIVE || this == ADJUSTMENT_NEGATIVE || this == RECOUNT;
    }
    
    public boolean isTransfer() {
        return this == TRANSFER_IN || this == TRANSFER_OUT;
    }
}