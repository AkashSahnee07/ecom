package com.ecommerce.shipping.controller;

import com.ecommerce.shipping.dto.CreateShipmentDto;
import com.ecommerce.shipping.dto.ShipmentDto;
import com.ecommerce.shipping.dto.TrackingEventDto;
import com.ecommerce.shipping.dto.UpdateShipmentStatusDto;
import com.ecommerce.shipping.entity.ShipmentStatus;
import com.ecommerce.shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping")
@Tag(name = "Shipping", description = "Shipping management operations")
@Validated
public class ShippingController {
    
    private static final Logger logger = LoggerFactory.getLogger(ShippingController.class);
    
    @Autowired
    private ShippingService shippingService;
    
    @PostMapping("/shipments")
    @Operation(summary = "Create new shipment", description = "Creates a new shipment for an order")
    public ResponseEntity<ShipmentDto> createShipment(
            @Valid @RequestBody CreateShipmentDto createShipmentDto) {
        
        logger.info("Creating shipment for order: {}", createShipmentDto.getOrderId());
        
        ShipmentDto shipment = shippingService.createShipment(createShipmentDto);
        
        logger.info("Shipment created successfully with ID: {}", shipment.getId());
        return new ResponseEntity<>(shipment, HttpStatus.CREATED);
    }
    
    @GetMapping("/shipments/{shipmentId}")
    @Operation(summary = "Get shipment by ID", description = "Retrieves shipment details by shipment ID")
    public ResponseEntity<ShipmentDto> getShipmentById(
            @Parameter(description = "Shipment ID") @PathVariable @NotNull Long shipmentId) {
        
        logger.info("Retrieving shipment with ID: {}", shipmentId);
        
        ShipmentDto shipment = shippingService.getShipmentById(shipmentId);
        return ResponseEntity.ok(shipment);
    }
    
    @GetMapping("/shipments/tracking/{trackingNumber}")
    @Operation(summary = "Track shipment", description = "Retrieves shipment details by tracking number")
    public ResponseEntity<ShipmentDto> trackShipment(
            @Parameter(description = "Tracking number") @PathVariable @NotBlank String trackingNumber) {
        
        logger.info("Tracking shipment with tracking number: {}", trackingNumber);
        
        ShipmentDto shipment = shippingService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(shipment);
    }
    
    @GetMapping("/shipments/order/{orderId}")
    @Operation(summary = "Get shipment by order ID", description = "Retrieves shipment details by order ID")
    public ResponseEntity<ShipmentDto> getShipmentByOrderId(
            @Parameter(description = "Order ID") @PathVariable @NotNull Long orderId) {
        
        logger.info("Retrieving shipment for order: {}", orderId);
        
        ShipmentDto shipment = shippingService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(shipment);
    }
    
    @GetMapping("/shipments")
    @Operation(summary = "Get shipments by status", description = "Retrieves paginated list of shipments by status")
    public ResponseEntity<Page<ShipmentDto>> getShipmentsByStatus(
            @Parameter(description = "Shipment status") @RequestParam ShipmentStatus status,
            Pageable pageable) {
        
        logger.info("Retrieving shipments with status: {}", status);
        
        Page<ShipmentDto> shipments = shippingService.getShipmentsByStatus(status, pageable);
        return ResponseEntity.ok(shipments);
    }
    
    @PutMapping("/shipments/{shipmentId}/status")
    @Operation(summary = "Update shipment status", description = "Updates the status of a shipment")
    public ResponseEntity<ShipmentDto> updateShipmentStatus(
            @Parameter(description = "Shipment ID") @PathVariable @NotNull Long shipmentId,
            @Valid @RequestBody UpdateShipmentStatusDto updateDto) {
        
        logger.info("Updating status for shipment: {} to {}", shipmentId, updateDto.getStatus());
        
        ShipmentDto shipment = shippingService.updateShipmentStatus(shipmentId, updateDto);
        
        logger.info("Shipment status updated successfully");
        return ResponseEntity.ok(shipment);
    }
    
    @GetMapping("/shipments/{shipmentId}/tracking")
    @Operation(summary = "Get tracking events", description = "Retrieves tracking events for a shipment")
    public ResponseEntity<List<TrackingEventDto>> getTrackingEvents(
            @Parameter(description = "Shipment ID") @PathVariable @NotNull Long shipmentId) {
        
        logger.info("Retrieving tracking events for shipment: {}", shipmentId);
        
        List<TrackingEventDto> events = shippingService.getTrackingEvents(shipmentId);
        return ResponseEntity.ok(events);
    }
    
    @PutMapping("/shipments/{shipmentId}/cancel")
    @Operation(summary = "Cancel shipment", description = "Cancels a shipment")
    public ResponseEntity<ShipmentDto> cancelShipment(
            @Parameter(description = "Shipment ID") @PathVariable @NotNull Long shipmentId,
            @Parameter(description = "Cancellation reason") @RequestParam @NotBlank String reason) {
        
        logger.info("Cancelling shipment: {} with reason: {}", shipmentId, reason);
        
        ShipmentDto shipment = shippingService.cancelShipment(shipmentId, reason);
        
        logger.info("Shipment cancelled successfully");
        return ResponseEntity.ok(shipment);
    }
    
    @GetMapping("/shipments/overdue")
    @Operation(summary = "Get overdue shipments", description = "Retrieves list of overdue shipments")
    public ResponseEntity<List<ShipmentDto>> getOverdueShipments() {
        
        logger.info("Retrieving overdue shipments");
        
        List<ShipmentDto> shipments = shippingService.getOverdueShipments();
        return ResponseEntity.ok(shipments);
    }
    
    @GetMapping("/shipments/attention")
    @Operation(summary = "Get shipments requiring attention", description = "Retrieves shipments that require attention (exceptions, delays, etc.)")
    public ResponseEntity<List<ShipmentDto>> getShipmentsRequiringAttention() {
        
        logger.info("Retrieving shipments requiring attention");
        
        List<ShipmentDto> shipments = shippingService.getShipmentsRequiringAttention();
        return ResponseEntity.ok(shipments);
    }
    
    // Health check endpoint
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns the health status of the shipping service")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Shipping service is healthy");
    }
    
    // Metrics endpoint for monitoring
    @GetMapping("/metrics/status-counts")
    @Operation(summary = "Get status counts", description = "Returns count of shipments by status for monitoring")
    public ResponseEntity<?> getStatusCounts() {
        // This would typically return metrics data
        // Implementation depends on your metrics collection strategy
        return ResponseEntity.ok("Status counts endpoint - implement based on metrics requirements");
    }
}