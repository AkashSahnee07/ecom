package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryResponseDto;
import com.ecommerce.inventory.dto.StockAdjustmentDto;
import com.ecommerce.inventory.dto.StockReservationDto;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.MovementType;
import com.ecommerce.inventory.entity.StockMovement;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.InventoryNotFoundException;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.repository.StockMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private StockMovementRepository stockMovementRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // Create or update inventory
    public InventoryResponseDto createOrUpdateInventory(String productId, String warehouseId, 
                                                       Integer quantity, Integer minimumStockLevel, 
                                                       Integer reorderPoint, Integer reorderQuantity) {
        Optional<Inventory> existingInventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
        
        Inventory inventory;
        if (existingInventory.isPresent()) {
            inventory = existingInventory.get();
            Integer previousQuantity = inventory.getTotalQuantity();
            inventory.setTotalQuantity(quantity);
            inventory.setAvailableQuantity(quantity - inventory.getReservedQuantity());
            inventory.setMinimumStockLevel(minimumStockLevel);
            inventory.setReorderPoint(reorderPoint);
            inventory.setReorderQuantity(reorderQuantity);
            
            // Record stock movement
            recordStockMovement(productId, warehouseId, MovementType.ADJUSTMENT_POSITIVE, 
                              quantity - previousQuantity, previousQuantity, quantity, 
                              null, "INVENTORY_UPDATE", "Inventory updated", "SYSTEM");
        } else {
            inventory = new Inventory(productId, warehouseId, quantity, minimumStockLevel, reorderPoint, reorderQuantity);
            
            // Record initial stock movement
            recordStockMovement(productId, warehouseId, MovementType.INITIAL, 
                              quantity, 0, quantity, 
                              null, "INVENTORY_CREATION", "Initial inventory setup", "SYSTEM");
        }
        
        inventory = inventoryRepository.save(inventory);
        
        // Publish inventory event
        publishInventoryEvent("INVENTORY_UPDATED", inventory);
        
        return convertToDto(inventory);
    }
    
    // Get inventory by product and warehouse
    public InventoryResponseDto getInventory(String productId, String warehouseId) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new InventoryNotFoundException("productId", productId, "warehouseId", warehouseId));
        return convertToDto(inventory);
    }
    
    // Get all inventory for a product across warehouses
    public List<InventoryResponseDto> getInventoryByProduct(String productId) {
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        return inventories.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // Get all inventory for a warehouse
    public Page<InventoryResponseDto> getInventoryByWarehouse(String warehouseId, Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findByWarehouseId(warehouseId, pageable);
        return inventories.map(this::convertToDto);
    }
    
    // Reserve stock for an order
    public boolean reserveStock(StockReservationDto reservationDto) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseIdWithLock(
            reservationDto.getProductId(), reservationDto.getWarehouseId())
            .orElseThrow(() -> new InventoryNotFoundException("productId", reservationDto.getProductId(), 
                                                            "warehouseId", reservationDto.getWarehouseId()));
        
        if (!inventory.canReserve(reservationDto.getQuantity())) {
            throw new InsufficientStockException(reservationDto.getProductId(), 
                                               reservationDto.getQuantity(), inventory.getAvailableQuantity());
        }
        
        Integer previousAvailable = inventory.getAvailableQuantity();
        inventory.reserveStock(reservationDto.getQuantity());
        inventoryRepository.save(inventory);
        
        // Record stock movement
        recordStockMovement(reservationDto.getProductId(), reservationDto.getWarehouseId(), 
                          MovementType.RESERVED, reservationDto.getQuantity(), 
                          previousAvailable, inventory.getAvailableQuantity(),
                          reservationDto.getOrderId(), "ORDER", 
                          "Stock reserved for order", reservationDto.getUserId());
        
        // Publish inventory event
        publishInventoryEvent("STOCK_RESERVED", inventory);
        
        return true;
    }
    
    // Release reserved stock
    public boolean releaseReservation(String productId, String warehouseId, Integer quantity, 
                                     String orderId, String userId) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseIdWithLock(productId, warehouseId)
            .orElseThrow(() -> new InventoryNotFoundException("productId", productId, "warehouseId", warehouseId));
        
        Integer previousReserved = inventory.getReservedQuantity();
        inventory.releaseReservation(quantity);
        inventoryRepository.save(inventory);
        
        // Record stock movement
        recordStockMovement(productId, warehouseId, MovementType.RELEASED, quantity, 
                          previousReserved, inventory.getReservedQuantity(),
                          orderId, "ORDER", "Reserved stock released", userId);
        
        // Publish inventory event
        publishInventoryEvent("STOCK_RELEASED", inventory);
        
        return true;
    }
    
    // Confirm reservation (consume reserved stock)
    public boolean confirmReservation(String productId, String warehouseId, Integer quantity, 
                                     String orderId, String userId) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseIdWithLock(productId, warehouseId)
            .orElseThrow(() -> new InventoryNotFoundException("productId", productId, "warehouseId", warehouseId));
        
        Integer previousTotal = inventory.getTotalQuantity();
        inventory.confirmReservation(quantity);
        inventoryRepository.save(inventory);
        
        // Record stock movement
        recordStockMovement(productId, warehouseId, MovementType.CONFIRMED, quantity, 
                          previousTotal, inventory.getTotalQuantity(),
                          orderId, "ORDER", "Reserved stock confirmed", userId);
        
        // Publish inventory event
        publishInventoryEvent("STOCK_CONFIRMED", inventory);
        
        // Check if reorder is needed
        if (inventory.needsReorder()) {
            publishReorderEvent(inventory);
        }
        
        return true;
    }
    
    // Adjust stock (positive or negative)
    public InventoryResponseDto adjustStock(StockAdjustmentDto adjustmentDto) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseIdWithLock(
            adjustmentDto.getProductId(), adjustmentDto.getWarehouseId())
            .orElseThrow(() -> new InventoryNotFoundException("productId", adjustmentDto.getProductId(), 
                                                            "warehouseId", adjustmentDto.getWarehouseId()));
        
        Integer previousQuantity = inventory.getTotalQuantity();
        MovementType movementType;
        
        if (adjustmentDto.getQuantity() > 0) {
            inventory.addStock(adjustmentDto.getQuantity());
            movementType = MovementType.ADJUSTMENT_POSITIVE;
        } else {
            inventory.removeStock(Math.abs(adjustmentDto.getQuantity()));
            movementType = MovementType.ADJUSTMENT_NEGATIVE;
        }
        
        inventoryRepository.save(inventory);
        
        // Record stock movement
        recordStockMovement(adjustmentDto.getProductId(), adjustmentDto.getWarehouseId(), 
                          movementType, Math.abs(adjustmentDto.getQuantity()), 
                          previousQuantity, inventory.getTotalQuantity(),
                          adjustmentDto.getReferenceId(), adjustmentDto.getReferenceType(), 
                          adjustmentDto.getReason(), adjustmentDto.getPerformedBy());
        
        // Publish inventory event
        publishInventoryEvent("STOCK_ADJUSTED", inventory);
        
        return convertToDto(inventory);
    }
    
    // Get low stock items
    public List<InventoryResponseDto> getLowStockItems() {
        List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();
        return lowStockItems.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // Get items needing reorder
    public List<InventoryResponseDto> getItemsNeedingReorder() {
        List<Inventory> reorderItems = inventoryRepository.findItemsNeedingReorder();
        return reorderItems.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // Get overstock items
    public List<InventoryResponseDto> getOverstockItems() {
        List<Inventory> overstockItems = inventoryRepository.findOverstockItems();
        return overstockItems.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // Get total quantity for a product across all warehouses
    public Integer getTotalQuantityForProduct(String productId) {
        Integer total = inventoryRepository.getTotalQuantityForProduct(productId);
        return total != null ? total : 0;
    }
    
    // Get available quantity for a product across all warehouses
    public Integer getAvailableQuantityForProduct(String productId) {
        Integer available = inventoryRepository.getAvailableQuantityForProduct(productId);
        return available != null ? available : 0;
    }
    
    // Check if product is available in sufficient quantity
    public boolean isProductAvailable(String productId, Integer requiredQuantity) {
        return getAvailableQuantityForProduct(productId) >= requiredQuantity;
    }
    
    // Get inventory summary for a product
    public ProductInventorySummary getProductInventorySummary(String productId) {
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        
        if (inventories.isEmpty()) {
            throw new InventoryNotFoundException("productId", productId);
        }
        
        Integer totalQuantity = inventories.stream().mapToInt(Inventory::getTotalQuantity).sum();
        Integer availableQuantity = inventories.stream().mapToInt(Inventory::getAvailableQuantity).sum();
        Integer reservedQuantity = inventories.stream().mapToInt(Inventory::getReservedQuantity).sum();
        
        return new ProductInventorySummary(productId, totalQuantity, availableQuantity, 
                                         reservedQuantity, inventories.size());
    }
    
    // Record stock movement
    private void recordStockMovement(String productId, String warehouseId, MovementType movementType,
                                   Integer quantity, Integer previousQuantity, Integer newQuantity,
                                   String referenceId, String referenceType, String reason, String performedBy) {
        StockMovement movement = new StockMovement(productId, warehouseId, movementType, quantity,
                                                 previousQuantity, newQuantity, referenceId, 
                                                 referenceType, reason, performedBy);
        stockMovementRepository.save(movement);
    }
    
    // Publish inventory event to Kafka
    private void publishInventoryEvent(String eventType, Inventory inventory) {
        InventoryEvent event = new InventoryEvent(eventType, inventory.getProductId(), 
                                                inventory.getWarehouseId(), inventory.getTotalQuantity(),
                                                inventory.getAvailableQuantity(), inventory.getReservedQuantity(),
                                                LocalDateTime.now());
        kafkaTemplate.send("inventory-events", event);
    }
    
    // Publish reorder event
    private void publishReorderEvent(Inventory inventory) {
        ReorderEvent event = new ReorderEvent(inventory.getProductId(), inventory.getWarehouseId(),
                                            inventory.getTotalQuantity(), inventory.getReorderQuantity(),
                                            inventory.getReorderPoint(), LocalDateTime.now());
        kafkaTemplate.send("reorder-events", event);
    }
    
    // Convert entity to DTO
    private InventoryResponseDto convertToDto(Inventory inventory) {
        InventoryResponseDto dto = new InventoryResponseDto();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProductId());
        dto.setWarehouseId(inventory.getWarehouseId());
        dto.setAvailableQuantity(inventory.getAvailableQuantity());
        dto.setReservedQuantity(inventory.getReservedQuantity());
        dto.setTotalQuantity(inventory.getTotalQuantity());
        dto.setMinimumStockLevel(inventory.getMinimumStockLevel());
        dto.setMaximumStockLevel(inventory.getMaximumStockLevel());
        dto.setReorderPoint(inventory.getReorderPoint());
        dto.setReorderQuantity(inventory.getReorderQuantity());
        dto.setLocation(inventory.getLocation());
        dto.setNotes(inventory.getNotes());
        dto.setLowStock(inventory.isLowStock());
        dto.setNeedsReorder(inventory.needsReorder());
        dto.setOverstock(inventory.isOverstock());
        dto.setStockUtilization(inventory.getStockUtilization().doubleValue());
        dto.setCreatedAt(inventory.getCreatedAt());
        dto.setUpdatedAt(inventory.getUpdatedAt());
        return dto;
    }
    
    // Event classes
    public static class InventoryEvent {
        private String eventType;
        private String productId;
        private String warehouseId;
        private Integer totalQuantity;
        private Integer availableQuantity;
        private Integer reservedQuantity;
        private LocalDateTime timestamp;
        
        public InventoryEvent(String eventType, String productId, String warehouseId, 
                            Integer totalQuantity, Integer availableQuantity, Integer reservedQuantity,
                            LocalDateTime timestamp) {
            this.eventType = eventType;
            this.productId = productId;
            this.warehouseId = warehouseId;
            this.totalQuantity = totalQuantity;
            this.availableQuantity = availableQuantity;
            this.reservedQuantity = reservedQuantity;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getEventType() { return eventType; }
        public String getProductId() { return productId; }
        public String getWarehouseId() { return warehouseId; }
        public Integer getTotalQuantity() { return totalQuantity; }
        public Integer getAvailableQuantity() { return availableQuantity; }
        public Integer getReservedQuantity() { return reservedQuantity; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class ReorderEvent {
        private String productId;
        private String warehouseId;
        private Integer currentQuantity;
        private Integer reorderQuantity;
        private Integer reorderPoint;
        private LocalDateTime timestamp;
        
        public ReorderEvent(String productId, String warehouseId, Integer currentQuantity,
                          Integer reorderQuantity, Integer reorderPoint, LocalDateTime timestamp) {
            this.productId = productId;
            this.warehouseId = warehouseId;
            this.currentQuantity = currentQuantity;
            this.reorderQuantity = reorderQuantity;
            this.reorderPoint = reorderPoint;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getProductId() { return productId; }
        public String getWarehouseId() { return warehouseId; }
        public Integer getCurrentQuantity() { return currentQuantity; }
        public Integer getReorderQuantity() { return reorderQuantity; }
        public Integer getReorderPoint() { return reorderPoint; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class ProductInventorySummary {
        private String productId;
        private Integer totalQuantity;
        private Integer availableQuantity;
        private Integer reservedQuantity;
        private Integer warehouseCount;
        
        public ProductInventorySummary(String productId, Integer totalQuantity, Integer availableQuantity,
                                     Integer reservedQuantity, Integer warehouseCount) {
            this.productId = productId;
            this.totalQuantity = totalQuantity;
            this.availableQuantity = availableQuantity;
            this.reservedQuantity = reservedQuantity;
            this.warehouseCount = warehouseCount;
        }
        
        // Getters
        public String getProductId() { return productId; }
        public Integer getTotalQuantity() { return totalQuantity; }
        public Integer getAvailableQuantity() { return availableQuantity; }
        public Integer getReservedQuantity() { return reservedQuantity; }
        public Integer getWarehouseCount() { return warehouseCount; }
    }
}