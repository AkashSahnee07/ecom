package com.ecommerce.shipping.dto;

import com.ecommerce.shipping.entity.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for updating shipment status")
public class UpdateShipmentStatusDto {
    
    @NotNull(message = "Status is required")
    @Schema(description = "New shipment status", required = true)
    private ShipmentStatus status;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Status update description", example = "Package picked up from warehouse")
    private String description;
    
    @Size(max = 200, message = "Location cannot exceed 200 characters")
    @Schema(description = "Current location of the package", example = "New York Distribution Center")
    private String location;
    
    @Size(max = 100, message = "Event code cannot exceed 100 characters")
    @Schema(description = "Internal event code", example = "PKG_PICKUP")
    private String eventCode;
    
    @Size(max = 100, message = "Carrier event code cannot exceed 100 characters")
    @Schema(description = "Carrier-specific event code", example = "FDX_001")
    private String carrierEventCode;
    
    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    @Schema(description = "Additional notes or comments", example = "Package requires signature upon delivery")
    private String notes;
    
    // Default constructor
    public UpdateShipmentStatusDto() {}
    
    // Constructor with required fields
    public UpdateShipmentStatusDto(ShipmentStatus status, String description) {
        this.status = status;
        this.description = description;
    }
    
    // Constructor with location
    public UpdateShipmentStatusDto(ShipmentStatus status, String description, String location) {
        this.status = status;
        this.description = description;
        this.location = location;
    }
    
    // Full constructor
    public UpdateShipmentStatusDto(ShipmentStatus status, String description, String location, 
                                 String eventCode, String carrierEventCode, String notes) {
        this.status = status;
        this.description = description;
        this.location = location;
        this.eventCode = eventCode;
        this.carrierEventCode = carrierEventCode;
        this.notes = notes;
    }
    
    // Business methods
    public boolean hasLocation() {
        return location != null && !location.trim().isEmpty();
    }
    
    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
    
    public boolean hasEventCode() {
        return eventCode != null && !eventCode.trim().isEmpty();
    }
    
    public boolean hasCarrierEventCode() {
        return carrierEventCode != null && !carrierEventCode.trim().isEmpty();
    }
    
    public String getFormattedDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return status != null ? status.getDescription() : "Status updated";
    }
    
    // Getters and Setters
    public ShipmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
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
    
    @Override
    public String toString() {
        return "UpdateShipmentStatusDto{" +
                "status=" + status +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", eventCode='" + eventCode + '\'' +
                ", carrierEventCode='" + carrierEventCode + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}