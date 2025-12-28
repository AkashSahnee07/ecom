package com.ecommerce.shipping.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events", indexes = {
    @Index(name = "idx_tracking_event_shipment_id", columnList = "shipment_id"),
    @Index(name = "idx_tracking_event_status", columnList = "status"),
    @Index(name = "idx_tracking_event_timestamp", columnList = "eventTimestamp")
})
@EntityListeners(AuditingEntityListener.class)
public class TrackingEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;
    
    @Column(nullable = false)
    private LocalDateTime eventTimestamp;
    
    @Column(name = "location_city")
    private String location;
    
    @Column(length = 1000)
    private String description;
    
    @Column
    private String eventCode;
    
    @Column(name = "carrier_event_id")
    private String carrierEventCode;
    
    @Column(length = 2000)
    private String notes;
    
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public TrackingEvent() {}
    
    // Constructor with required fields
    public TrackingEvent(Shipment shipment, ShipmentStatus status, LocalDateTime eventTimestamp, String description) {
        this.shipment = shipment;
        this.status = status;
        this.eventTimestamp = eventTimestamp;
        this.description = description;
    }
    
    // Full constructor
    public TrackingEvent(Shipment shipment, ShipmentStatus status, LocalDateTime eventTimestamp, 
                        String location, String description, String eventCode, String carrierEventCode, String notes) {
        this.shipment = shipment;
        this.status = status;
        this.eventTimestamp = eventTimestamp;
        this.location = location;
        this.description = description;
        this.eventCode = eventCode;
        this.carrierEventCode = carrierEventCode;
        this.notes = notes;
    }
    
    // Business methods
    public boolean isDeliveryEvent() {
        return this.status == ShipmentStatus.DELIVERED;
    }
    
    public boolean isExceptionEvent() {
        return this.status == ShipmentStatus.EXCEPTION;
    }
    
    public boolean isStatusChangeEvent() {
        return this.status != null;
    }
    
    public String getFormattedEventDescription() {
        StringBuilder sb = new StringBuilder();
        
        if (description != null && !description.trim().isEmpty()) {
            sb.append(description);
        } else {
            sb.append(status.getDescription());
        }
        
        if (location != null && !location.trim().isEmpty()) {
            sb.append(" at ").append(location);
        }
        
        return sb.toString();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Shipment getShipment() {
        return shipment;
    }
    
    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }
    
    public ShipmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }
    
    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getEventCode() {
        return eventCode;
    }
    
    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }
    
    public String getCarrierEventCode() {
        return carrierEventCode;
    }
    
    public void setCarrierEventCode(String carrierEventCode) {
        this.carrierEventCode = carrierEventCode;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}