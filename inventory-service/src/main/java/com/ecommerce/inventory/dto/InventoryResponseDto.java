package com.ecommerce.inventory.dto;

import java.time.LocalDateTime;

public class InventoryResponseDto {
    private Long id;
    private String productId;
    private String warehouseId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
    private Integer minimumStockLevel;
    private Integer maximumStockLevel;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private String location;
    private String notes;
    private Boolean lowStock;
    private Boolean needsReorder;
    private Boolean overstock;
    private Double stockUtilization;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public InventoryResponseDto() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    
    public Integer getReservedQuantity() {
        return reservedQuantity;
    }
    
    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
    
    public Integer getTotalQuantity() {
        return totalQuantity;
    }
    
    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    
    public Integer getMinimumStockLevel() {
        return minimumStockLevel;
    }
    
    public void setMinimumStockLevel(Integer minimumStockLevel) {
        this.minimumStockLevel = minimumStockLevel;
    }
    
    public Integer getMaximumStockLevel() {
        return maximumStockLevel;
    }
    
    public void setMaximumStockLevel(Integer maximumStockLevel) {
        this.maximumStockLevel = maximumStockLevel;
    }
    
    public Integer getReorderPoint() {
        return reorderPoint;
    }
    
    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }
    
    public Integer getReorderQuantity() {
        return reorderQuantity;
    }
    
    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Boolean getLowStock() {
        return lowStock;
    }
    
    public void setLowStock(Boolean lowStock) {
        this.lowStock = lowStock;
    }
    
    public Boolean getNeedsReorder() {
        return needsReorder;
    }
    
    public void setNeedsReorder(Boolean needsReorder) {
        this.needsReorder = needsReorder;
    }
    
    public Boolean getOverstock() {
        return overstock;
    }
    
    public void setOverstock(Boolean overstock) {
        this.overstock = overstock;
    }
    
    public Double getStockUtilization() {
        return stockUtilization;
    }
    
    public void setStockUtilization(Double stockUtilization) {
        this.stockUtilization = stockUtilization;
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