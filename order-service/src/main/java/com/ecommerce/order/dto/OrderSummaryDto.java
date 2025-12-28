package com.ecommerce.order.dto;

import java.math.BigDecimal;

public class OrderSummaryDto {
    
    private int totalOrders;
    private int pendingOrders;
    private int deliveredOrders;
    private BigDecimal totalSpent;
    
    // Constructors
    public OrderSummaryDto() {}
    
    public OrderSummaryDto(int totalOrders, int pendingOrders, int deliveredOrders, BigDecimal totalSpent) {
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.deliveredOrders = deliveredOrders;
        this.totalSpent = totalSpent;
    }
    
    // Getters and setters
    public int getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public int getPendingOrders() {
        return pendingOrders;
    }
    
    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }
    
    public int getDeliveredOrders() {
        return deliveredOrders;
    }
    
    public void setDeliveredOrders(int deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }
    
    public BigDecimal getTotalSpent() {
        return totalSpent;
    }
    
    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }
}