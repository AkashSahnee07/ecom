package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockAdjustmentDto {
    
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;
    
    @NotNull(message = "Quantity is required")
    private Integer quantity; // Can be positive or negative
    
    private String referenceId;
    
    private String referenceType;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    @NotBlank(message = "Performed by is required")
    private String performedBy;
    
    private String notes;
    
    // Default constructor
    public StockAdjustmentDto() {}
    
    // Constructor with required fields
    public StockAdjustmentDto(String productId, String warehouseId, Integer quantity, 
                             String reason, String performedBy) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.reason = reason;
        this.performedBy = performedBy;
    }
    
    // Getters and Setters
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getWarehouseId() {
        return warehouseId;
    }
    
    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    
    public String getReferenceType() {
        return referenceType;
    }
    
    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getPerformedBy() {
        return performedBy;
    }
    
    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}