package com.ecommerce.shipping.service;

import com.ecommerce.shipping.dto.CreateShipmentDto;
import com.ecommerce.shipping.dto.ShipmentDto;
import com.ecommerce.shipping.dto.TrackingEventDto;
import com.ecommerce.shipping.dto.UpdateShipmentStatusDto;
import com.ecommerce.shipping.entity.Address;
import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.entity.ShipmentStatus;
import com.ecommerce.shipping.entity.TrackingEvent;
import com.ecommerce.shipping.exception.InvalidShipmentStatusException;
import com.ecommerce.shipping.exception.ShipmentNotFoundException;
import com.ecommerce.shipping.repository.ShipmentRepository;
import com.ecommerce.shipping.repository.TrackingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private TrackingEventRepository trackingEventRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShippingService shippingService;

    private CreateShipmentDto createShipmentDto;
    private Shipment shipment;
    private Address originAddress;
    private Address destinationAddress;

    @BeforeEach
    void setUp() {
        // Setup test addresses
        originAddress = new Address(
            "E-Commerce Inc.",
            "123 Warehouse Blvd",
            null,
            "Los Angeles",
            "CA",
            "90210",
            "US",
            "+1-555-0123"
        );

        destinationAddress = new Address(
            "John Doe",
            "456 Customer St",
            "Apt 2B",
            "New York",
            "NY",
            "10001",
            "US",
            "+1-555-0456"
        );

        // Setup CreateShipmentDto
        createShipmentDto = new CreateShipmentDto();
        createShipmentDto.setOrderId(1001L);
        createShipmentDto.setCarrier("FEDEX");
        createShipmentDto.setShippingAddress(originAddress);
        createShipmentDto.setBillingAddress(destinationAddress);
        createShipmentDto.setWeight(new BigDecimal("2.5"));
        // createShipmentDto.setLengthCm(new BigDecimal("30.0"));
        // createShipmentDto.setWidthCm(new BigDecimal("20.0"));
        // createShipmentDto.setHeightCm(new BigDecimal("15.0")); // Field may not exist
        createShipmentDto.setShippingCost(new BigDecimal("12.99"));
        createShipmentDto.setPriority("STANDARD");
        createShipmentDto.setSpecialInstructions("Handle with care");

        // Setup Shipment entity
        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setTrackingNumber("TRK1234567890");
        shipment.setOrderId(1001L);
        shipment.setCarrier("FEDEX");
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setSenderAddress(originAddress);
        shipment.setRecipientAddress(destinationAddress);
        shipment.setWeight(new BigDecimal("2.5"));
        shipment.setShippingCost(new BigDecimal("12.99"));
        shipment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createShipment_ShouldCreateSuccessfully() {
        // Given
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(trackingEventRepository.save(any(TrackingEvent.class))).thenReturn(new TrackingEvent());

        // When
        ShipmentDto result = shippingService.createShipment(createShipmentDto);

        // Then
        assertNotNull(result);
        assertEquals(shipment.getTrackingNumber(), result.getTrackingNumber());
        assertEquals(createShipmentDto.getOrderId(), result.getOrderId());
        assertEquals(shipment.getCarrier(), result.getCarrier());
        assertEquals(ShipmentStatus.CREATED, result.getStatus());

        verify(shipmentRepository).save(any(Shipment.class));
        verify(trackingEventRepository).save(any(TrackingEvent.class));
    }

    @Test
    void createShipment_WithNullDto_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            shippingService.createShipment(null);
        });
    }

    @Test
    void getShipmentByTrackingNumber_ShouldReturnShipment() {
        // Given
        String trackingNumber = "TRK1234567890";
        when(shipmentRepository.findByTrackingNumberWithEvents(trackingNumber))
            .thenReturn(Optional.of(shipment));

        // When
        ShipmentDto result = shippingService.getShipmentByTrackingNumber(trackingNumber);

        // Then
        assertNotNull(result);
        assertEquals(trackingNumber, result.getTrackingNumber());
        verify(shipmentRepository).findByTrackingNumberWithEvents(trackingNumber);
    }

    @Test
    void getShipmentByTrackingNumber_NotFound_ShouldThrowException() {
        // Given
        String trackingNumber = "INVALID-TRACKING";
        when(shipmentRepository.findByTrackingNumberWithEvents(trackingNumber))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ShipmentNotFoundException.class, () -> {
            shippingService.getShipmentByTrackingNumber(trackingNumber);
        });
    }

    @Test
    void getShipmentById_ShouldReturnShipment() {
        // Given
        Long shipmentId = 1L;
        when(shipmentRepository.findByIdWithTrackingEvents(shipmentId)).thenReturn(Optional.of(shipment));

        // When
        ShipmentDto result = shippingService.getShipmentById(shipmentId);

        // Then
        assertNotNull(result);
        assertEquals(shipmentId, result.getId());
        verify(shipmentRepository).findByIdWithTrackingEvents(shipmentId);
    }

    @Test
    void getShipmentById_NotFound_ShouldThrowException() {
        // Given
        Long shipmentId = 999L;
        when(shipmentRepository.findByIdWithTrackingEvents(shipmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ShipmentNotFoundException.class, () -> {
            shippingService.getShipmentById(shipmentId);
        });
    }

    @Test
    void getShipmentsByOrderId_ShouldReturnShipments() {
        // Given
        Long orderId = 1001L;
        when(shipmentRepository.findByOrderId(orderId)).thenReturn(Optional.of(shipment));

        // When
        List<ShipmentDto> result = shippingService.getShipmentsByOrderId(orderId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getOrderId());
        verify(shipmentRepository).findByOrderId(orderId);
    }

    @Test
    void getAllShipments_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Shipment> shipmentPage = new PageImpl<>(Arrays.asList(shipment));
        when(shipmentRepository.findAll(pageable)).thenReturn(shipmentPage);

        // When
        Page<ShipmentDto> result = shippingService.getAllShipments(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(shipmentRepository).findAll(pageable);
    }

    @Test
    void updateShipmentStatus_ValidTransition_ShouldUpdateSuccessfully() {
        // Given
        Long shipmentId = 1L;
        UpdateShipmentStatusDto updateDto = new UpdateShipmentStatusDto();
        updateDto.setStatus(ShipmentStatus.PROCESSING);
        updateDto.setDescription("Shipment is being processed");
        updateDto.setLocation("Los Angeles, CA");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(trackingEventRepository.save(any(TrackingEvent.class))).thenReturn(new TrackingEvent());

        // When
        ShipmentDto result = shippingService.updateShipmentStatus(shipmentId, updateDto);

        // Then
        assertNotNull(result);
        verify(shipmentRepository).save(any(Shipment.class));
        verify(trackingEventRepository).save(any(TrackingEvent.class));
        verify(eventPublisher).publishEvent(any(Map.class));
    }

    @Test
    void updateShipmentStatus_InvalidTransition_ShouldThrowException() {
        // Given
        Long shipmentId = 1L;
        shipment.setStatus(ShipmentStatus.DELIVERED); // Terminal status
        UpdateShipmentStatusDto updateDto = new UpdateShipmentStatusDto();
        updateDto.setStatus(ShipmentStatus.IN_TRANSIT);

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        // When & Then
        assertThrows(InvalidShipmentStatusException.class, () -> {
            shippingService.updateShipmentStatus(shipmentId, updateDto);
        });
    }

    @Test
    void updateShipmentStatus_ShipmentNotFound_ShouldThrowException() {
        // Given
        Long shipmentId = 999L;
        UpdateShipmentStatusDto updateDto = new UpdateShipmentStatusDto();
        updateDto.setStatus(ShipmentStatus.PROCESSING);

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ShipmentNotFoundException.class, () -> {
            shippingService.updateShipmentStatus(shipmentId, updateDto);
        });
    }

    @Test
    void cancelShipment_ValidCancellation_ShouldCancelSuccessfully() {
        // Given
        Long shipmentId = 1L;
        String reason = "Customer requested cancellation";
        shipment.setStatus(ShipmentStatus.PROCESSING); // Cancellable status

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(trackingEventRepository.save(any(TrackingEvent.class))).thenReturn(new TrackingEvent());

        // When
        ShipmentDto result = shippingService.cancelShipment(shipmentId, reason);

        // Then
        assertNotNull(result);
        verify(shipmentRepository).save(any(Shipment.class));
        verify(trackingEventRepository).save(any(TrackingEvent.class));
        verify(eventPublisher).publishEvent(any(Map.class));
    }

    @Test
    void cancelShipment_AlreadyDelivered_ShouldThrowException() {
        // Given
        Long shipmentId = 1L;
        String reason = "Customer requested cancellation";
        shipment.setStatus(ShipmentStatus.DELIVERED); // Non-cancellable status

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        // When & Then
        assertThrows(InvalidShipmentStatusException.class, () -> {
            shippingService.cancelShipment(shipmentId, reason);
        });
    }

    @Test
    void getShipmentsByStatus_ShouldReturnFilteredResults() {
        // Given
        ShipmentStatus status = ShipmentStatus.IN_TRANSIT;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Shipment> shipmentPage = new PageImpl<>(Arrays.asList(shipment));
        
        when(shipmentRepository.findByStatus(status, pageable)).thenReturn(shipmentPage);

        // When
        Page<ShipmentDto> result = shippingService.getShipmentsByStatus(status, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(shipmentRepository).findByStatus(status, pageable);
    }

    @Test
    void getShipmentsByCarrier_ShouldReturnFilteredResults() {
        // Given
        String carrier = "FEDEX";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Shipment> shipmentPage = new PageImpl<>(Arrays.asList(shipment));
        
        when(shipmentRepository.findByCarrier(carrier, pageable)).thenReturn(shipmentPage);

        // When
        Page<ShipmentDto> result = shippingService.getShipmentsByCarrier(carrier, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(shipmentRepository).findByCarrier(carrier, pageable);
    }

    @Test
    void getTrackingEvents_ShouldReturnEvents() {
        // Given
        Long shipmentId = 1L;
        TrackingEvent event = new TrackingEvent();
        event.setId(1L);
        event.setShipment(shipment);
        event.setStatus(ShipmentStatus.CREATED);
        event.setDescription("Shipment created");
        event.setEventTimestamp(LocalDateTime.now());
        
        List<TrackingEvent> events = Arrays.asList(event);
        when(trackingEventRepository.findByShipmentIdOrderByEventTimestampDesc(shipmentId))
            .thenReturn(events);

        // When
        List<TrackingEventDto> result = shippingService.getTrackingEvents(shipmentId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ShipmentStatus.CREATED, result.get(0).getStatus());
        verify(trackingEventRepository).findByShipmentIdOrderByEventTimestampDesc(shipmentId);
    }

    @Test
    void generateTrackingNumber_ShouldGenerateUniqueNumber() {
        // Given
        when(shipmentRepository.findByTrackingNumber(anyString())).thenReturn(Optional.empty());
        
        // When
        String trackingNumber1 = shippingService.generateTrackingNumber();
        String trackingNumber2 = shippingService.generateTrackingNumber();

        // Then
        assertNotNull(trackingNumber1);
        assertNotNull(trackingNumber2);
        assertNotEquals(trackingNumber1, trackingNumber2);
        assertTrue(trackingNumber1.startsWith("TRK"));
        assertTrue(trackingNumber2.startsWith("TRK"));
    }

    @Test
    void isValidStatusTransition_ValidTransitions_ShouldReturnTrue() {
        // Test valid transitions
        assertTrue(shippingService.isValidStatusTransition(ShipmentStatus.CREATED, ShipmentStatus.PROCESSING));
        assertTrue(shippingService.isValidStatusTransition(ShipmentStatus.PROCESSING, ShipmentStatus.SHIPPED));
        assertTrue(shippingService.isValidStatusTransition(ShipmentStatus.SHIPPED, ShipmentStatus.IN_TRANSIT));
        assertTrue(shippingService.isValidStatusTransition(ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY));
        assertTrue(shippingService.isValidStatusTransition(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED));
        
        // Test exception transitions
        assertTrue(shippingService.isValidStatusTransition(ShipmentStatus.IN_TRANSIT, ShipmentStatus.EXCEPTION));
        assertTrue(shippingService.isValidStatusTransition(ShipmentStatus.EXCEPTION, ShipmentStatus.IN_TRANSIT));
        
        // Test cancellation transitions
        assertTrue(shippingService.isValidStatusTransition(ShipmentStatus.CREATED, ShipmentStatus.CANCELLED));
        assertTrue(shippingService.isValidStatusTransition(ShipmentStatus.PROCESSING, ShipmentStatus.CANCELLED));
    }

    @Test
    void isValidStatusTransition_InvalidTransitions_ShouldReturnFalse() {
        // Test invalid transitions
        assertFalse(shippingService.isValidStatusTransition(ShipmentStatus.DELIVERED, ShipmentStatus.IN_TRANSIT));
        assertFalse(shippingService.isValidStatusTransition(ShipmentStatus.CANCELLED, ShipmentStatus.PROCESSING));
        assertFalse(shippingService.isValidStatusTransition(ShipmentStatus.CREATED, ShipmentStatus.DELIVERED));
        assertFalse(shippingService.isValidStatusTransition(ShipmentStatus.PROCESSING, ShipmentStatus.OUT_FOR_DELIVERY));
    }
}