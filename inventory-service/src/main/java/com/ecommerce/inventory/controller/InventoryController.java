package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryResponseDto;
import com.ecommerce.inventory.dto.StockAdjustmentDto;
import com.ecommerce.inventory.dto.StockReservationDto;
import com.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "*")
@Tag(name = "Inventory Management", description = "APIs for managing inventory stock")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    // Create or update inventory
    @PostMapping
    @Operation(summary = "Create or Update Inventory", description = "Creates new inventory record or updates existing one")
    public ResponseEntity<InventoryResponseDto> createOrUpdateInventory(
            @RequestParam String productId,
            @RequestParam String warehouseId,
            @RequestParam Integer quantity,
            @RequestParam(required = false, defaultValue = "10") Integer minimumStockLevel,
            @RequestParam(required = false, defaultValue = "20") Integer reorderPoint,
            @RequestParam(required = false, defaultValue = "100") Integer reorderQuantity) {
        
        InventoryResponseDto inventory = inventoryService.createOrUpdateInventory(
            productId, warehouseId, quantity, minimumStockLevel, reorderPoint, reorderQuantity);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(inventory);
    }
    
    // Get inventory by product and warehouse
    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    @Operation(summary = "Get Inventory", description = "Retrieves inventory details for a specific product in a warehouse")
    public ResponseEntity<InventoryResponseDto> getInventory(
            @PathVariable String productId,
            @PathVariable String warehouseId) {
        
        InventoryResponseDto inventory = inventoryService.getInventory(productId, warehouseId);
        return ResponseEntity.ok(inventory);
    }
    
    // Get all inventory for a product across warehouses
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get Product Inventory", description = "Retrieves inventory details for a product across all warehouses")
    public ResponseEntity<List<InventoryResponseDto>> getInventoryByProduct(
            @PathVariable String productId) {
        
        List<InventoryResponseDto> inventories = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(inventories);
    }
    
    // Get all inventory for a warehouse
    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get Warehouse Inventory", description = "Retrieves all inventory items for a specific warehouse")
    public ResponseEntity<Page<InventoryResponseDto>> getInventoryByWarehouse(
            @PathVariable String warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InventoryResponseDto> inventories = inventoryService.getInventoryByWarehouse(warehouseId, pageable);
        return ResponseEntity.ok(inventories);
    }
    
    // Reserve stock
    @PostMapping("/reserve")
    @Operation(summary = "Reserve Stock", description = "Reserves stock for an order")
    public ResponseEntity<Map<String, Object>> reserveStock(@Valid @RequestBody StockReservationDto reservationDto) {
        boolean success = inventoryService.reserveStock(reservationDto);
        
        Map<String, Object> response = Map.of(
            "success", success,
            "message", "Stock reserved successfully",
            "productId", reservationDto.getProductId(),
            "warehouseId", reservationDto.getWarehouseId(),
            "quantity", reservationDto.getQuantity(),
            "orderId", reservationDto.getOrderId()
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Release reservation
    @PostMapping("/release")
    @Operation(summary = "Release Reservation", description = "Releases reserved stock")
    public ResponseEntity<Map<String, Object>> releaseReservation(
            @RequestParam String productId,
            @RequestParam String warehouseId,
            @RequestParam Integer quantity,
            @RequestParam String orderId,
            @RequestParam String userId) {
        
        boolean success = inventoryService.releaseReservation(productId, warehouseId, quantity, orderId, userId);
        
        Map<String, Object> response = Map.of(
            "success", success,
            "message", "Reservation released successfully",
            "productId", productId,
            "warehouseId", warehouseId,
            "quantity", quantity,
            "orderId", orderId
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Confirm reservation
    @PostMapping("/confirm")
    @Operation(summary = "Confirm Reservation", description = "Confirms a stock reservation")
    public ResponseEntity<Map<String, Object>> confirmReservation(
            @RequestParam String productId,
            @RequestParam String warehouseId,
            @RequestParam Integer quantity,
            @RequestParam String orderId,
            @RequestParam String userId) {
        
        boolean success = inventoryService.confirmReservation(productId, warehouseId, quantity, orderId, userId);
        
        Map<String, Object> response = Map.of(
            "success", success,
            "message", "Reservation confirmed successfully",
            "productId", productId,
            "warehouseId", warehouseId,
            "quantity", quantity,
            "orderId", orderId
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Adjust stock
    @PostMapping("/adjust")
    @Operation(summary = "Adjust Stock", description = "Adjusts stock quantity manually")
    public ResponseEntity<InventoryResponseDto> adjustStock(@Valid @RequestBody StockAdjustmentDto adjustmentDto) {
        InventoryResponseDto inventory = inventoryService.adjustStock(adjustmentDto);
        return ResponseEntity.ok(inventory);
    }
    
    // Get low stock items
    @GetMapping("/low-stock")
    @Operation(summary = "Get Low Stock Items", description = "Retrieves items with low stock")
    public ResponseEntity<List<InventoryResponseDto>> getLowStockItems() {
        List<InventoryResponseDto> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }
    
    // Get items needing reorder
    @GetMapping("/reorder")
    @Operation(summary = "Get Items Needing Reorder", description = "Retrieves items that need reordering")
    public ResponseEntity<List<InventoryResponseDto>> getItemsNeedingReorder() {
        List<InventoryResponseDto> reorderItems = inventoryService.getItemsNeedingReorder();
        return ResponseEntity.ok(reorderItems);
    }
    
    // Get overstock items
    @GetMapping("/overstock")
    @Operation(summary = "Get Overstock Items", description = "Retrieves items that are overstocked")
    public ResponseEntity<List<InventoryResponseDto>> getOverstockItems() {
        List<InventoryResponseDto> overstockItems = inventoryService.getOverstockItems();
        return ResponseEntity.ok(overstockItems);
    }
    
    // Check product availability
    @GetMapping("/availability/{productId}")
    @Operation(summary = "Check Product Availability", description = "Checks if product is available in required quantity")
    public ResponseEntity<Map<String, Object>> checkProductAvailability(
            @PathVariable String productId,
            @RequestParam Integer requiredQuantity) {
        
        boolean available = inventoryService.isProductAvailable(productId, requiredQuantity);
        Integer totalAvailable = inventoryService.getAvailableQuantityForProduct(productId);
        
        Map<String, Object> response = Map.of(
            "productId", productId,
            "requiredQuantity", requiredQuantity,
            "available", available,
            "totalAvailableQuantity", totalAvailable
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Get product inventory summary
    @GetMapping("/summary/{productId}")
    @Operation(summary = "Get Product Inventory Summary", description = "Retrieves summary of product inventory")
    public ResponseEntity<InventoryService.ProductInventorySummary> getProductInventorySummary(
            @PathVariable String productId) {
        
        InventoryService.ProductInventorySummary summary = inventoryService.getProductInventorySummary(productId);
        return ResponseEntity.ok(summary);
    }
    
    // Get total quantity for a product
    @GetMapping("/total/{productId}")
    @Operation(summary = "Get Total Quantity", description = "Retrieves total quantity of a product")
    public ResponseEntity<Map<String, Object>> getTotalQuantityForProduct(@PathVariable String productId) {
        Integer totalQuantity = inventoryService.getTotalQuantityForProduct(productId);
        Integer availableQuantity = inventoryService.getAvailableQuantityForProduct(productId);
        
        Map<String, Object> response = Map.of(
            "productId", productId,
            "totalQuantity", totalQuantity,
            "availableQuantity", availableQuantity
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Health check endpoint
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Checks service health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = Map.of(
            "status", "UP",
            "service", "Inventory Service",
            "timestamp", java.time.LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(response);
    }
}