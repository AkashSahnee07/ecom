package com.ecommerce.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", indexes = {
    @Index(name = "idx_product_id", columnList = "productId"),
    @Index(name = "idx_warehouse_id", columnList = "warehouseId"),
    @Index(name = "idx_product_warehouse", columnList = "productId, warehouseId", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String productId;
    
    @Column(nullable = false)
    private String warehouseId;
    
    @Column(nullable = false)
    private Integer availableQuantity;
    
    @Column(nullable = false)
    private Integer reservedQuantity;
    
    @Column(nullable = false)
    private Integer totalQuantity;
    
    @Column(nullable = false)
    private Integer minimumStockLevel;
    
    @Column(nullable = false)
    private Integer maximumStockLevel;
    
    @Column(nullable = false)
    private Integer reorderPoint;
    
    @Column(nullable = false)
    private Integer reorderQuantity;
    
    @Column(length = 50)
    private String location; // Warehouse location/bin
    
    @Column(length = 500)
    private String notes;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Inventory() {}
    
    public Inventory(String productId, String warehouseId, Integer totalQuantity, 
                    Integer minimumStockLevel, Integer reorderPoint, Integer reorderQuantity) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.reservedQuantity = 0;
        this.minimumStockLevel = minimumStockLevel;
        this.maximumStockLevel = totalQuantity * 2; // Default to 2x current stock
        this.reorderPoint = reorderPoint;
        this.reorderQuantity = reorderQuantity;
    }
    
    // Business methods
    public boolean canReserve(Integer quantity) {
        return availableQuantity >= quantity;
    }
    
    public void reserveStock(Integer quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Insufficient available stock to reserve " + quantity + " units");
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }
    
    public void releaseReservation(Integer quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Cannot release more than reserved quantity");
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }
    
    public void confirmReservation(Integer quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Cannot confirm more than reserved quantity");
        }
        this.reservedQuantity -= quantity;
        this.totalQuantity -= quantity;
    }
    
    public void addStock(Integer quantity) {
        this.totalQuantity += quantity;
        this.availableQuantity += quantity;
    }
    
    public void removeStock(Integer quantity) {
        if (availableQuantity < quantity) {
            throw new IllegalStateException("Cannot remove more than available quantity");
        }
        this.availableQuantity -= quantity;
        this.totalQuantity -= quantity;
    }
    
    public boolean isLowStock() {
        return totalQuantity <= minimumStockLevel;
    }
    
    public boolean needsReorder() {
        return totalQuantity <= reorderPoint;
    }
    
    public boolean isOverstock() {
        return totalQuantity >= maximumStockLevel;
    }
    
    public Integer getStockUtilization() {
        if (maximumStockLevel == 0) return 0;
        return (totalQuantity * 100) / maximumStockLevel;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    
    public Integer getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(Integer minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }
    
    public Integer getMaximumStockLevel() { return maximumStockLevel; }
    public void setMaximumStockLevel(Integer maximumStockLevel) { this.maximumStockLevel = maximumStockLevel; }
    
    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
    
    public Integer getReorderQuantity() { return reorderQuantity; }
    public void setReorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}