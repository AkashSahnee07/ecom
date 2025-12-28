package com.ecommerce.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when an order is confirmed (payment successful and inventory reserved).
 */
public class OrderConfirmedEvent extends OrderEvent {
    
    private BigDecimal totalAmount;
    private String currency;
    private String paymentId;
    private String paymentMethod;
    private LocalDateTime estimatedDeliveryDate;
    private String shippingMethod;
    private String trackingNumber;
    
    public OrderConfirmedEvent() {
        super();
    }
    
    public OrderConfirmedEvent(String orderId, String userId, BigDecimal totalAmount, 
                             String currency, String paymentId, String paymentMethod, 
                             LocalDateTime estimatedDeliveryDate, String shippingMethod, 
                             String trackingNumber) {
        super(orderId, userId);
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paymentId = paymentId;
        this.paymentMethod = paymentMethod;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.shippingMethod = shippingMethod;
        this.trackingNumber = trackingNumber;
    }
    
    public OrderConfirmedEvent(String orderId, String userId, String correlationId, 
                             BigDecimal totalAmount, String currency, String paymentId, 
                             String paymentMethod, LocalDateTime estimatedDeliveryDate, 
                             String shippingMethod, String trackingNumber) {
        super(orderId, userId, correlationId);
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paymentId = paymentId;
        this.paymentMethod = paymentMethod;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.shippingMethod = shippingMethod;
        this.trackingNumber = trackingNumber;
    }
    
    @Override
    public String getEventType() {
        return "ORDER_CONFIRMED";
    }
    
    // Getters and Setters
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
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }
    
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
    
    public String getShippingMethod() {
        return shippingMethod;
    }
    
    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
}