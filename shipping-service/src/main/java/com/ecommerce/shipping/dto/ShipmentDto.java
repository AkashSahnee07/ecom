package com.ecommerce.shipping.dto;

import com.ecommerce.shipping.entity.Address;
import com.ecommerce.shipping.entity.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "DTO for shipment data")
public class ShipmentDto {
    
    @Schema(description = "Shipment ID", example = "1")
    private Long id;
    
    @Schema(description = "Order ID", example = "12345")
    private Long orderId;
    
    @Schema(description = "Tracking number", example = "TRK1234567890")
    private String trackingNumber;
    
    @Schema(description = "Current shipment status")
    private ShipmentStatus status;
    
    @Schema(description = "Shipping carrier", example = "FedEx")
    private String carrier;
    
    @Schema(description = "Shipping address")
    private Address shippingAddress;
    
    @Schema(description = "Billing address")
    private Address billingAddress;
    
    @Schema(description = "Package weight in kg", example = "2.5")
    private BigDecimal weight;
    
    @Schema(description = "Package dimensions", example = "30x20x15 cm")
    private String dimensions;
    
    @Schema(description = "Shipping cost", example = "15.99")
    private BigDecimal shippingCost;
    
    @Schema(description = "Shipping priority", example = "STANDARD")
    private String priority;
    
    @Schema(description = "Special delivery instructions", example = "Leave at front door")
    private String specialInstructions;
    
    @Schema(description = "Expected delivery date")
    private LocalDateTime expectedDeliveryDate;
    
    @Schema(description = "Actual shipped date")
    private LocalDateTime shippedAt;
    
    @Schema(description = "Actual delivered date")
    private LocalDateTime deliveredAt;
    
    @Schema(description = "Cancellation date")
    private LocalDateTime cancelledAt;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    @Schema(description = "List of tracking events")
    private List<TrackingEventDto> trackingEvents;
    
    // Default constructor
    public ShipmentDto() {}
    
    // Constructor with essential fields
    public ShipmentDto(Long id, Long orderId, String trackingNumber, ShipmentStatus status, String carrier) {
        this.id = id;
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.carrier = carrier;
    }
    
    // Business methods
    public boolean isDelivered() {
        return status == ShipmentStatus.DELIVERED;
    }
    
    public boolean isCancelled() {
        return status == ShipmentStatus.CANCELLED;
    }
    
    public boolean isInTransit() {
        return status == ShipmentStatus.IN_TRANSIT || status == ShipmentStatus.OUT_FOR_DELIVERY;
    }
    
    public boolean hasTrackingEvents() {
        return trackingEvents != null && !trackingEvents.isEmpty();
    }
    
    public int getTrackingEventCount() {
        return trackingEvents != null ? trackingEvents.size() : 0;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public ShipmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(ShipmentStatus status) {
        this.status = status;
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
    
    public LocalDateTime getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }
    
    public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
    
    public LocalDateTime getShippedAt() {
        return shippedAt;
    }
    
    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<TrackingEventDto> getTrackingEvents() {
        return trackingEvents;
    }
    
    public void setTrackingEvents(List<TrackingEventDto> trackingEvents) {
        this.trackingEvents = trackingEvents;
    }
    
    @Override
    public String toString() {
        return "ShipmentDto{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", status=" + status +
                ", carrier='" + carrier + '\'' +
                ", weight=" + weight +
                ", shippingCost=" + shippingCost +
                ", priority='" + priority + '\'' +
                ", expectedDeliveryDate=" + expectedDeliveryDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
