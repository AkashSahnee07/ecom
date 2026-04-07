package com.ecommerce.shipping.service;

import com.ecommerce.shipping.dto.CreateShipmentDto;
import com.ecommerce.shipping.dto.ShipmentDto;
import com.ecommerce.shipping.dto.TrackingEventDto;
import com.ecommerce.shipping.dto.UpdateShipmentStatusDto;
import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.entity.ShipmentStatus;
import com.ecommerce.shipping.entity.TrackingEvent;
import com.ecommerce.shipping.exception.ShipmentNotFoundException;
import com.ecommerce.shipping.exception.InvalidShipmentStatusException;
import com.ecommerce.shipping.repository.ShipmentRepository;
import com.ecommerce.shipping.repository.TrackingEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShippingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ShippingService.class);
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private TrackingEventRepository trackingEventRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // Create new shipment
    public ShipmentDto createShipment(CreateShipmentDto createShipmentDto) {
        if (createShipmentDto == null) {
            throw new IllegalArgumentException("CreateShipmentDto cannot be null");
        }
        logger.info("Creating shipment for order: {}", createShipmentDto.getOrderId());
        
        // Check if shipment already exists for this order
        if (shipmentRepository.existsByOrderId(createShipmentDto.getOrderId())) {
            throw new IllegalArgumentException("Shipment already exists for order: " + createShipmentDto.getOrderId());
        }
        
        // Generate unique tracking number
        String trackingNumber = generateTrackingNumber();
        
        // Create shipment entity
        Shipment shipment = new Shipment();
        shipment.setOrderId(createShipmentDto.getOrderId());
        shipment.setTrackingNumber(trackingNumber);
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setCarrier(createShipmentDto.getCarrier());
        shipment.setRecipientAddress(createShipmentDto.getShippingAddress());
        shipment.setSenderAddress(createShipmentDto.getBillingAddress());
        shipment.setWeight(createShipmentDto.getWeight());
        shipment.setDimensions(createShipmentDto.getDimensions());
        shipment.setShippingCost(createShipmentDto.getShippingCost());
        // Note: Priority is not stored in Shipment entity
        shipment.setNotes(createShipmentDto.getSpecialInstructions());
        
        // Calculate expected delivery date based on carrier and priority
        shipment.setEstimatedDeliveryDate(calculateExpectedDeliveryDate(createShipmentDto.getCarrier(), createShipmentDto.getPriority()));
        
        // Save shipment
        shipment = shipmentRepository.save(shipment);
        
        // Create initial tracking event
        createTrackingEvent(shipment, ShipmentStatus.CREATED, "Shipment created and ready for processing");
        
        // Publish shipment created event
        publishShipmentEvent("shipment.created", shipment);
        
        logger.info("Shipment created successfully with tracking number: {}", trackingNumber);
        return convertToDto(shipment);
    }
    
    // Update shipment status
    public ShipmentDto updateShipmentStatus(Long shipmentId, UpdateShipmentStatusDto updateDto) {
        logger.info("Updating shipment status for shipment: {} to status: {}", shipmentId, updateDto.getStatus());
        
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with id: " + shipmentId));
        
        // Validate status transition
        validateStatusTransition(shipment.getStatus(), updateDto.getStatus());
        
        // Update shipment status
        ShipmentStatus oldStatus = shipment.getStatus();
        shipment.updateStatus(updateDto.getStatus());
        
        // Update specific timestamps based on status
        updateTimestampsForStatus(shipment, updateDto.getStatus());
        
        // Save shipment
        shipment = shipmentRepository.save(shipment);
        
        // Create tracking event
        createTrackingEvent(shipment, updateDto.getStatus(), updateDto.getDescription(), updateDto.getLocation());
        
        // Publish status change event
        publishStatusChangeEvent(shipment, oldStatus, updateDto.getStatus());
        
        logger.info("Shipment status updated successfully from {} to {}", oldStatus, updateDto.getStatus());
        return convertToDto(shipment);
    }
    
    // Get shipment by ID
    @Transactional(readOnly = true)
    public ShipmentDto getShipmentById(Long shipmentId) {
        Shipment shipment = shipmentRepository.findByIdWithTrackingEvents(shipmentId)
            .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with id: " + shipmentId));
        return convertToDto(shipment);
    }
    
    // Get shipment by tracking number
    @Transactional(readOnly = true)
    public ShipmentDto getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumberWithEvents(trackingNumber)
            .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with tracking number: " + trackingNumber));
        return convertToDto(shipment);
    }
    
    // Get shipment by order ID
    @Transactional(readOnly = true)
    public ShipmentDto getShipmentByOrderId(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found for order: " + orderId));
        return convertToDto(shipment);
    }
    
    // Get shipments by status
    @Transactional(readOnly = true)
    public Page<ShipmentDto> getShipmentsByStatus(ShipmentStatus status, Pageable pageable) {
        Page<Shipment> shipments = shipmentRepository.findByStatus(status, pageable);
        return shipments.map(this::convertToDto);
    }
    
    // Get shipments by carrier
    @Transactional(readOnly = true)
    public Page<ShipmentDto> getShipmentsByCarrier(String carrier, Pageable pageable) {
        return shipmentRepository.findByCarrier(carrier, pageable).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<ShipmentDto> getShipmentsByOrderId(Long orderId) {
        // Since findByOrderId returns Optional<Shipment>, we need to handle it properly
        Optional<Shipment> shipment = shipmentRepository.findByOrderId(orderId);
        return shipment.map(s -> List.of(convertToDto(s))).orElse(List.of());
    }

    @Transactional(readOnly = true)
    public Page<ShipmentDto> getAllShipments(Pageable pageable) {
        return shipmentRepository.findAll(pageable).map(this::convertToDto);
    }
    
    // Get tracking events for shipment
    @Transactional(readOnly = true)
    public List<TrackingEventDto> getTrackingEvents(Long shipmentId) {
        List<TrackingEvent> events = trackingEventRepository.findByShipmentIdOrderByEventTimestampDesc(shipmentId);
        return events.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // Cancel shipment
    public ShipmentDto cancelShipment(Long shipmentId, String reason) {
        logger.info("Cancelling shipment: {} with reason: {}", shipmentId, reason);
        
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with id: " + shipmentId));
        
        // Check if shipment can be cancelled
        if (shipment.getStatus() == ShipmentStatus.DELIVERED || shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new InvalidShipmentStatusException("Shipment cannot be cancelled in current status: " + shipment.getStatus());
        }
        
        // Update status to cancelled
        ShipmentStatus oldStatus = shipment.getStatus();
        shipment.updateStatus(ShipmentStatus.CANCELLED);
        
        shipment = shipmentRepository.save(shipment);
        
        // Create tracking event
        createTrackingEvent(shipment, ShipmentStatus.CANCELLED, "Shipment cancelled: " + reason);
        
        // Publish cancellation event
        eventPublisher.publishEvent(Map.of(
            "eventType", "shipment.cancelled",
            "shipmentId", shipment.getId(),
            "trackingNumber", shipment.getTrackingNumber(),
            "reason", reason,
            "timestamp", LocalDateTime.now()
        ));
        publishShipmentEvent("shipment.cancelled", shipment);
        
        logger.info("Shipment cancelled successfully");
        return convertToDto(shipment);
    }
    
    // Get overdue shipments
    @Transactional(readOnly = true)
    public List<ShipmentDto> getOverdueShipments() {
        List<ShipmentStatus> terminalStatuses = List.of(
            ShipmentStatus.DELIVERED, 
            ShipmentStatus.CANCELLED
        );
        
        List<Shipment> overdueShipments = shipmentRepository.findOverdueShipments(
            LocalDateTime.now(), terminalStatuses
        );
        
        return overdueShipments.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // Get shipments requiring attention
    @Transactional(readOnly = true)
    public List<ShipmentDto> getShipmentsRequiringAttention() {
        List<ShipmentStatus> exceptionStatuses = List.of(
            ShipmentStatus.EXCEPTION
        );
        
        List<Shipment> shipments = shipmentRepository.findShipmentsRequiringAttention(exceptionStatuses);
        return shipments.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // Helper methods
    public String generateTrackingNumber() {
        String trackingNumber;
        do {
            String prefix = "TRK";
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            trackingNumber = prefix + uuid;
        } while (shipmentRepository.findByTrackingNumber(trackingNumber).isPresent());
        return trackingNumber;
    }
    
    private LocalDateTime calculateExpectedDeliveryDate(String carrier, String priority) {
        LocalDateTime now = LocalDateTime.now();
        
        // Simple logic - can be enhanced based on carrier and priority
        switch (priority != null ? priority.toUpperCase() : "STANDARD") {
            case "EXPRESS":
                return now.plusDays(1);
            case "PRIORITY":
                return now.plusDays(2);
            case "STANDARD":
            default:
                return now.plusDays(5);
        }
    }
    
    private void validateStatusTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        // Implement status transition validation logic
        if (currentStatus == ShipmentStatus.DELIVERED) {
            throw new InvalidShipmentStatusException("Cannot change status from DELIVERED to " + newStatus);
        }
        
        if (currentStatus == ShipmentStatus.CANCELLED) {
            throw new InvalidShipmentStatusException("Cannot change status from CANCELLED");
        }
    }
    
    private void updateTimestampsForStatus(Shipment shipment, ShipmentStatus status) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (status) {
            case SHIPPED:
                shipment.setShippedDate(now);
                break;
            case DELIVERED:
                shipment.setActualDeliveryDate(now);
                break;
            case CANCELLED:
                // No cancelled date field in entity
                break;
            default:
                break;
        }
    }
    
    private void createTrackingEvent(Shipment shipment, ShipmentStatus status, String description) {
        createTrackingEvent(shipment, status, description, null);
    }
    
    private void createTrackingEvent(Shipment shipment, ShipmentStatus status, String description, String location) {
        TrackingEvent event = new TrackingEvent();
        event.setShipment(shipment);
        event.setStatus(status);
        event.setEventTimestamp(LocalDateTime.now());
        event.setDescription(description);
        event.setLocation(location);
        
        trackingEventRepository.save(event);
    }
    
    private void publishShipmentEvent(String eventType, Shipment shipment) {
        try {
            kafkaTemplate.send("shipment-events", eventType, convertToDto(shipment));
        } catch (Exception e) {
            logger.error("Failed to publish shipment event: {}", eventType, e);
        }
    }
    
    private void publishStatusChangeEvent(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus) {
        try {
            // Publish Spring application event
            eventPublisher.publishEvent(Map.of(
                "shipmentId", shipment.getId(),
                "trackingNumber", shipment.getTrackingNumber(),
                "orderId", shipment.getOrderId(),
                "oldStatus", oldStatus,
                "newStatus", newStatus,
                "timestamp", LocalDateTime.now()
            ));
            
            // Also publish to Kafka
            kafkaTemplate.send("shipment-status-changes", 
                String.valueOf(shipment.getId()), 
                Map.of(
                    "shipmentId", shipment.getId(),
                    "trackingNumber", shipment.getTrackingNumber(),
                    "orderId", shipment.getOrderId(),
                    "oldStatus", oldStatus,
                    "newStatus", newStatus,
                    "timestamp", LocalDateTime.now()
                )
            );
        } catch (Exception e) {
            logger.error("Failed to publish status change event", e);
        }
    }
    
    private ShipmentDto convertToDto(Shipment shipment) {
        ShipmentDto dto = new ShipmentDto();
        dto.setId(shipment.getId());
        dto.setOrderId(shipment.getOrderId());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setStatus(shipment.getStatus());
        dto.setCarrier(shipment.getCarrier());
        dto.setShippingAddress(shipment.getRecipientAddress());
        dto.setBillingAddress(shipment.getSenderAddress());
        dto.setWeight(shipment.getWeight());
        dto.setDimensions(shipment.getDimensions());
        dto.setShippingCost(shipment.getShippingCost());
        dto.setPriority(null); // No priority field in entity
        dto.setSpecialInstructions(shipment.getNotes());
        dto.setExpectedDeliveryDate(shipment.getEstimatedDeliveryDate());
        dto.setShippedAt(shipment.getShippedDate());
        dto.setDeliveredAt(shipment.getActualDeliveryDate());
        dto.setCancelledAt(null); // No cancelled date field in entity
        dto.setCreatedAt(shipment.getCreatedAt());
        dto.setUpdatedAt(shipment.getUpdatedAt());
        
        if (shipment.getTrackingEvents() != null) {
            dto.setTrackingEvents(
                shipment.getTrackingEvents().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList())
            );
        }
        
        return dto;
    }
    
    private TrackingEventDto convertToDto(TrackingEvent event) {
        TrackingEventDto dto = new TrackingEventDto();
        dto.setId(event.getId());
        dto.setStatus(event.getStatus());
        dto.setEventTimestamp(event.getEventTimestamp());
        dto.setLocation(event.getLocation());
        dto.setDescription(event.getDescription());
        dto.setEventCode(event.getEventCode());
        dto.setCarrierEventCode(event.getCarrierEventCode());
        dto.setNotes(event.getNotes());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }

    public boolean isValidStatusTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case CREATED:
                return newStatus == ShipmentStatus.PROCESSING || newStatus == ShipmentStatus.CANCELLED;
            case PROCESSING:
                return newStatus == ShipmentStatus.SHIPPED || newStatus == ShipmentStatus.CANCELLED;
            case SHIPPED:
                return newStatus == ShipmentStatus.IN_TRANSIT || newStatus == ShipmentStatus.EXCEPTION;
            case IN_TRANSIT:
                return newStatus == ShipmentStatus.OUT_FOR_DELIVERY || newStatus == ShipmentStatus.EXCEPTION;
            case OUT_FOR_DELIVERY:
                return newStatus == ShipmentStatus.DELIVERED || newStatus == ShipmentStatus.EXCEPTION;
            case EXCEPTION:
                return newStatus == ShipmentStatus.IN_TRANSIT || newStatus == ShipmentStatus.CANCELLED;
            case DELIVERED:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }
}
