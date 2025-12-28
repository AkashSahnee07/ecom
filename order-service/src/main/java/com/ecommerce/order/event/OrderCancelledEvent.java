package com.ecommerce.order.event;

import java.math.BigDecimal;

/**
 * Event published when an order is cancelled.
 */
public class OrderCancelledEvent extends OrderEvent {
    
    private String previousStatus;
    private BigDecimal refundAmount;
    private String currency;
    private String cancellationReason;
    private String cancelledBy;
    private boolean refundRequired;
    
    public OrderCancelledEvent() {
        super();
    }
    
    public OrderCancelledEvent(String orderId, String userId, String previousStatus, 
                             BigDecimal refundAmount, String currency, 
                             String cancellationReason, String cancelledBy, 
                             boolean refundRequired) {
        super(orderId, userId);
        this.previousStatus = previousStatus;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.cancellationReason = cancellationReason;
        this.cancelledBy = cancelledBy;
        this.refundRequired = refundRequired;
    }
    
    public OrderCancelledEvent(String orderId, String userId, String correlationId, 
                             String previousStatus, BigDecimal refundAmount, 
                             String currency, String cancellationReason, 
                             String cancelledBy, boolean refundRequired) {
        super(orderId, userId, correlationId);
        this.previousStatus = previousStatus;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.cancellationReason = cancellationReason;
        this.cancelledBy = cancelledBy;
        this.refundRequired = refundRequired;
    }
    
    @Override
    public String getEventType() {
        return "ORDER_CANCELLED";
    }
    
    // Getters and Setters
    public String getPreviousStatus() {
        return previousStatus;
    }
    
    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public String getCancelledBy() {
        return cancelledBy;
    }
    
    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }
    
    public boolean isRefundRequired() {
        return refundRequired;
    }
    
    public void setRefundRequired(boolean refundRequired) {
        this.refundRequired = refundRequired;
    }
}