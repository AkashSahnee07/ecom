package com.ecommerce.shipping.dto;

import com.ecommerce.shipping.entity.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO for tracking event data")
public class TrackingEventDto {
    
    @Schema(description = "Tracking event ID", example = "1")
    private Long id;
    
    @Schema(description = "Event status")
    private ShipmentStatus status;
    
    @Schema(description = "Event timestamp")
    private LocalDateTime eventTimestamp;
    
    @Schema(description = "Event location", example = "New York Distribution Center")
    private String location;
    
    @Schema(description = "Event description", example = "Package picked up from warehouse")
    private String description;
    
    @Schema(description = "Internal event code", example = "PKG_PICKUP")
    private String eventCode;
    
    @Schema(description = "Carrier-specific event code", example = "FDX_001")
    private String carrierEventCode;
    
    @Schema(description = "Additional notes", example = "Package requires signature upon delivery")
    private String notes;
    
    @Schema(description = "Event creation timestamp")
    private LocalDateTime createdAt;
    
    // Default constructor
    public TrackingEventDto() {}
    
    // Constructor with essential fields
    public TrackingEventDto(Long id, ShipmentStatus status, LocalDateTime eventTimestamp, String description) {
        this.id = id;
        this.status = status;
        this.eventTimestamp = eventTimestamp;
        this.description = description;
    }
    
    // Constructor with location
    public TrackingEventDto(Long id, ShipmentStatus status, LocalDateTime eventTimestamp, 
                          String location, String description) {
        this.id = id;
        this.status = status;
        this.eventTimestamp = eventTimestamp;
        this.location = location;
        this.description = description;
    }
    
    // Full constructor
    public TrackingEventDto(Long id, ShipmentStatus status, LocalDateTime eventTimestamp, 
                          String location, String description, String eventCode, 
                          String carrierEventCode, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.eventTimestamp = eventTimestamp;
        this.location = location;
        this.description = description;
        this.eventCode = eventCode;
        this.carrierEventCode = carrierEventCode;
        this.notes = notes;
        this.createdAt = createdAt;
    }
    
    // Business methods
    public boolean hasLocation() {
        return location != null && !location.trim().isEmpty();
    }
    
    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
    
    public boolean isDeliveryEvent() {
        return status == ShipmentStatus.DELIVERED;
    }
    
    public boolean isExceptionEvent() {
        return status == ShipmentStatus.EXCEPTION;
    }
    
    public String getFormattedDescription() {
        StringBuilder sb = new StringBuilder();
        
        if (description != null && !description.trim().isEmpty()) {
            sb.append(description);
        } else if (status != null) {
            sb.append(status.getDescription());
        } else {
            sb.append("Status update");
        }
        
        if (hasLocation()) {
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
    
    @Override
    public String toString() {
        return "TrackingEventDto{" +
                "id=" + id +
                ", status=" + status +
                ", eventTimestamp=" + eventTimestamp +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", eventCode='" + eventCode + '\'' +
                ", carrierEventCode='" + carrierEventCode + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}