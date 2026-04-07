package com.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderDto {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Items list is required")
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemDto> items;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressDto shippingAddress;
    
    @Valid
    private BillingAddressDto billingAddress;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private BigDecimal taxAmount;
    
    private BigDecimal shippingAmount;
    
    private BigDecimal discountAmount;
    
    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    // Constructors
    public CreateOrderDto() {}
    
    public CreateOrderDto(String userId, List<OrderItemDto> items, ShippingAddressDto shippingAddress, String paymentMethod) {
        this.userId = userId;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }
    
    public ShippingAddressDto getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddressDto shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public BillingAddressDto getBillingAddress() { return billingAddress; }
    public void setBillingAddress(BillingAddressDto billingAddress) { this.billingAddress = billingAddress; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    
    public BigDecimal getShippingAmount() { return shippingAmount; }
    public void setShippingAmount(BigDecimal shippingAmount) { this.shippingAmount = shippingAmount; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
