package com.ecommerce.shipping.dto;

import com.ecommerce.shipping.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "DTO for creating a new shipment")
public class CreateShipmentDto {
    
    @NotNull(message = "Order ID is required")
    @Schema(description = "Order ID for which shipment is being created", example = "12345")
    private Long orderId;
    
    @NotNull(message = "Carrier is required")
    @Size(min = 2, max = 100, message = "Carrier name must be between 2 and 100 characters")
    @Schema(description = "Shipping carrier name", example = "FedEx")
    private String carrier;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    @Schema(description = "Shipping address details")
    private Address shippingAddress;
    
    @Valid
    @Schema(description = "Billing address details")
    private Address billingAddress;
    
    @Positive(message = "Weight must be positive")
    @Schema(description = "Package weight in kg", example = "2.5")
    private BigDecimal weight;
    
    @Size(max = 200, message = "Dimensions cannot exceed 200 characters")
    @Schema(description = "Package dimensions (L x W x H)", example = "30x20x15 cm")
    private String dimensions;
    
    @Positive(message = "Shipping cost must be positive")
    @Schema(description = "Shipping cost", example = "15.99")
    private BigDecimal shippingCost;
    
    @Size(max = 50, message = "Priority cannot exceed 50 characters")
    @Schema(description = "Shipping priority", example = "STANDARD", allowableValues = {"EXPRESS", "PRIORITY", "STANDARD"})
    private String priority;
    
    @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
    @Schema(description = "Special delivery instructions", example = "Leave at front door")
    private String specialInstructions;
    
    // Default constructor
    public CreateShipmentDto() {}
    
    // Constructor with required fields
    public CreateShipmentDto(Long orderId, String carrier, Address shippingAddress) {
        this.orderId = orderId;
        this.carrier = carrier;
        this.shippingAddress = shippingAddress;
    }
    
    // Full constructor
    public CreateShipmentDto(Long orderId, String carrier, Address shippingAddress, Address billingAddress,
                           BigDecimal weight, String dimensions, BigDecimal shippingCost, 
                           String priority, String specialInstructions) {
        this.orderId = orderId;
        this.carrier = carrier;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.weight = weight;
        this.dimensions = dimensions;
        this.shippingCost = shippingCost;
        this.priority = priority;
        this.specialInstructions = specialInstructions;
    }
    
    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getCarrier() {
        return carrier;
    }
    
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
    
    public Address getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public Address getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public String getDimensions() {
        return dimensions;
    }
    
    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }
    
    public BigDecimal getShippingCost() {
        return shippingCost;
    }
    
    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
    
    @Override
    public String toString() {
        return "CreateShipmentDto{" +
                "orderId=" + orderId +
                ", carrier='" + carrier + '\'' +
                ", shippingAddress=" + shippingAddress +
                ", billingAddress=" + billingAddress +
                ", weight=" + weight +
                ", dimensions='" + dimensions + '\'' +
                ", shippingCost=" + shippingCost +
                ", priority='" + priority + '\'' +
                ", specialInstructions='" + specialInstructions + '\'' +
                '}';
    }
}
