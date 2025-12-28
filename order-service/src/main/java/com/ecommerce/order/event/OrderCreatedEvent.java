package com.ecommerce.order.event;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event published when a new order is created.
 */
public class OrderCreatedEvent extends OrderEvent {
    
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private String shippingAddress;
    private String billingAddress;
    private List<OrderItemDto> items;
    private String paymentMethod;
    
    public OrderCreatedEvent() {
        super();
    }
    
    public OrderCreatedEvent(String orderId, String userId, BigDecimal totalAmount, 
                           String currency, String status, String shippingAddress, 
                           String billingAddress, List<OrderItemDto> items, String paymentMethod) {
        super(orderId, userId);
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.items = items;
        this.paymentMethod = paymentMethod;
    }
    
    public OrderCreatedEvent(String orderId, String userId, String correlationId, 
                           BigDecimal totalAmount, String currency, String status, 
                           String shippingAddress, String billingAddress, 
                           List<OrderItemDto> items, String paymentMethod) {
        super(orderId, userId, correlationId);
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.items = items;
        this.paymentMethod = paymentMethod;
    }
    
    @Override
    public String getEventType() {
        return "ORDER_CREATED";
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public List<OrderItemDto> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    /**
     * DTO for order items in the event
     */
    public static class OrderItemDto {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        
        public OrderItemDto() {}
        
        public OrderItemDto(String productId, String productName, Integer quantity, 
                          BigDecimal unitPrice, BigDecimal totalPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }
        
        // Getters and Setters
        public String getProductId() {
            return productId;
        }
        
        public void setProductId(String productId) {
            this.productId = productId;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getUnitPrice() {
            return unitPrice;
        }
        
        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
        
        public BigDecimal getTotalPrice() {
            return totalPrice;
        }
        
        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}