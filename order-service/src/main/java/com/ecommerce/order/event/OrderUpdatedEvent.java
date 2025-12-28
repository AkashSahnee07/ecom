package com.ecommerce.order.event;

import java.math.BigDecimal;

/**
 * Event published when an order is updated.
 */
public class OrderUpdatedEvent extends OrderEvent {
    
    private String previousStatus;
    private String newStatus;
    private BigDecimal totalAmount;
    private String currency;
    private String updateReason;
    private String updatedBy;
    
    public OrderUpdatedEvent() {
        super();
    }
    
    public OrderUpdatedEvent(String orderId, String userId, String previousStatus, 
                           String newStatus, BigDecimal totalAmount, String currency, 
                           String updateReason, String updatedBy) {
        super(orderId, userId);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.updateReason = updateReason;
        this.updatedBy = updatedBy;
    }
    
    public OrderUpdatedEvent(String orderId, String userId, String correlationId, 
                           String previousStatus, String newStatus, BigDecimal totalAmount, 
                           String currency, String updateReason, String updatedBy) {
        super(orderId, userId, correlationId);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.updateReason = updateReason;
        this.updatedBy = updatedBy;
    }
    
    @Override
    public String getEventType() {
        return "ORDER_UPDATED";
    }
    
    // Getters and Setters
    public String getPreviousStatus() {
        return previousStatus;
    }
    
    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }
    
    public String getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getUpdateReason() {
        return updateReason;
    }
    
    public void setUpdateReason(String updateReason) {
        this.updateReason = updateReason;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}