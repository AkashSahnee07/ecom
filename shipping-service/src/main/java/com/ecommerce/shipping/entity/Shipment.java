package com.ecommerce.shipping.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments", indexes = {
    @Index(name = "idx_shipment_order_id", columnList = "orderId"),
    @Index(name = "idx_shipment_tracking_number", columnList = "trackingNumber"),
    @Index(name = "idx_shipment_status", columnList = "status"),
    @Index(name = "idx_shipment_carrier", columnList = "carrier"),
    @Index(name = "idx_shipment_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class Shipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;
    
    @Column(nullable = false, unique = true)
    private String trackingNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;
    
    @Column(nullable = false)
    private String carrier;
    
    @Column(nullable = false)
    private String serviceType;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal shippingCost;
    
    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weight;
    
    @Column(name = "length_cm", precision = 10, scale = 2)
    private BigDecimal lengthCm;
    
    @Column(name = "width_cm", precision = 10, scale = 2)
    private BigDecimal widthCm;
    
    @Column(name = "height_cm", precision = 10, scale = 2)
    private BigDecimal heightCm;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "origin_name")),
        @AttributeOverride(name = "addressLine1", column = @Column(name = "origin_address_line1")),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "origin_address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "origin_city")),
        @AttributeOverride(name = "state", column = @Column(name = "origin_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "origin_postal_code")),
        @AttributeOverride(name = "country", column = @Column(name = "origin_country")),
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "origin_phone"))
    })
    private Address senderAddress;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "destination_name")),
        @AttributeOverride(name = "addressLine1", column = @Column(name = "destination_address_line1")),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "destination_address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "destination_city")),
        @AttributeOverride(name = "state", column = @Column(name = "destination_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "destination_postal_code")),
        @AttributeOverride(name = "country", column = @Column(name = "destination_country")),
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "destination_phone"))
    })
    private Address recipientAddress;
    
    @Column
    private LocalDateTime estimatedDeliveryDate;
    
    @Column
    private LocalDateTime actualDeliveryDate;
    
    @Column
    private LocalDateTime shippedDate;
    
    @Column(name = "special_instructions", length = 1000)
    private String notes;
    
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrackingEvent> trackingEvents = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Shipment() {}
    
    // Constructor with required fields
    public Shipment(Long orderId, String trackingNumber, String carrier, String serviceType,
                   Address senderAddress, Address recipientAddress) {
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.serviceType = serviceType;
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.status = ShipmentStatus.CREATED;
    }
    
    // Business methods
    public void updateStatus(ShipmentStatus newStatus) {
        if (canTransitionTo(newStatus)) {
            this.status = newStatus;
            
            // Set specific dates based on status
            switch (newStatus) {
                case SHIPPED:
                    this.shippedDate = LocalDateTime.now();
                    break;
                case DELIVERED:
                    this.actualDeliveryDate = LocalDateTime.now();
                    break;
                default:
                    break;
            }
        } else {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", this.status, newStatus));
        }
    }
    
    public boolean canTransitionTo(ShipmentStatus newStatus) {
        switch (this.status) {
            case CREATED:
                return newStatus == ShipmentStatus.PROCESSING || newStatus == ShipmentStatus.CANCELLED;
            case PROCESSING:
                return newStatus == ShipmentStatus.SHIPPED || newStatus == ShipmentStatus.CANCELLED;
            case SHIPPED:
                return newStatus == ShipmentStatus.IN_TRANSIT || newStatus == ShipmentStatus.DELIVERED ||
                       newStatus == ShipmentStatus.EXCEPTION;
            case IN_TRANSIT:
                return newStatus == ShipmentStatus.OUT_FOR_DELIVERY || newStatus == ShipmentStatus.DELIVERED ||
                       newStatus == ShipmentStatus.EXCEPTION;
            case OUT_FOR_DELIVERY:
                return newStatus == ShipmentStatus.DELIVERED || newStatus == ShipmentStatus.EXCEPTION;
            case DELIVERED:
            case CANCELLED:
            case EXCEPTION:
                return false; // Terminal states
            default:
                return false;
        }
    }
    
    public boolean isDelivered() {
        return this.status == ShipmentStatus.DELIVERED;
    }
    
    public boolean isCancelled() {
        return this.status == ShipmentStatus.CANCELLED;
    }
    
    public boolean isInProgress() {
        return this.status == ShipmentStatus.PROCESSING || this.status == ShipmentStatus.SHIPPED ||
               this.status == ShipmentStatus.IN_TRANSIT || this.status == ShipmentStatus.OUT_FOR_DELIVERY;
    }
    
    public void addTrackingEvent(TrackingEvent event) {
        this.trackingEvents.add(event);
        event.setShipment(this);
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
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public BigDecimal getShippingCost() {
        return shippingCost;
    }
    
    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public String getDimensions() {
        if (lengthCm != null && widthCm != null && heightCm != null) {
            return lengthCm + "x" + widthCm + "x" + heightCm + " cm";
        }
        return null;
    }
    
    public void setDimensions(String dimensions) {
        // Parse dimensions string like "30x20x15 cm" or "30x20x15"
        if (dimensions != null && !dimensions.trim().isEmpty()) {
            String[] parts = dimensions.replace(" cm", "").split("x");
            if (parts.length == 3) {
                try {
                    this.lengthCm = new BigDecimal(parts[0].trim());
                    this.widthCm = new BigDecimal(parts[1].trim());
                    this.heightCm = new BigDecimal(parts[2].trim());
                } catch (NumberFormatException e) {
                    // Ignore invalid format
                }
            }
        }
    }
    
    public BigDecimal getLengthCm() {
        return lengthCm;
    }
    
    public void setLengthCm(BigDecimal lengthCm) {
        this.lengthCm = lengthCm;
    }
    
    public BigDecimal getWidthCm() {
        return widthCm;
    }
    
    public void setWidthCm(BigDecimal widthCm) {
        this.widthCm = widthCm;
    }
    
    public BigDecimal getHeightCm() {
        return heightCm;
    }
    
    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }
    
    public Address getSenderAddress() {
        return senderAddress;
    }
    
    public void setSenderAddress(Address senderAddress) {
        this.senderAddress = senderAddress;
    }
    
    public Address getRecipientAddress() {
        return recipientAddress;
    }
    
    public void setRecipientAddress(Address recipientAddress) {
        this.recipientAddress = recipientAddress;
    }
    
    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }
    
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
    
    public LocalDateTime getActualDeliveryDate() {
        return actualDeliveryDate;
    }
    
    public void setActualDeliveryDate(LocalDateTime actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }
    
    public LocalDateTime getShippedDate() {
        return shippedDate;
    }
    
    public void setShippedDate(LocalDateTime shippedDate) {
        this.shippedDate = shippedDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<TrackingEvent> getTrackingEvents() {
        return trackingEvents;
    }
    
    public void setTrackingEvents(List<TrackingEvent> trackingEvents) {
        this.trackingEvents = trackingEvents;
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
}
