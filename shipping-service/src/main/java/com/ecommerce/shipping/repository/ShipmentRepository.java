package com.ecommerce.shipping.repository;

import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.entity.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    // Find by tracking number
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    // Find by order ID
    Optional<Shipment> findByOrderId(Long orderId);
    
    // Find shipments by status
    List<Shipment> findByStatus(ShipmentStatus status);
    
    // Find shipments by status with pagination
    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);
    
    // Find shipments by multiple statuses
    List<Shipment> findByStatusIn(List<ShipmentStatus> statuses);
    
    // Find shipments by carrier
    List<Shipment> findByCarrier(String carrier);
    
    // Find shipments by carrier with pagination
    Page<Shipment> findByCarrier(String carrier, Pageable pageable);
    
    // Find shipments by carrier and status
    List<Shipment> findByCarrierAndStatus(String carrier, ShipmentStatus status);
    
    // Find shipments created between dates
    List<Shipment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find shipments shipped between dates
    List<Shipment> findByShippedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find shipments delivered between dates
    List<Shipment> findByActualDeliveryDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find shipments by destination city
    @Query("SELECT s FROM Shipment s WHERE s.recipientAddress.city = :city")
    List<Shipment> findByDestinationCity(@Param("city") String city);
    
    // Find shipments by destination state
    @Query("SELECT s FROM Shipment s WHERE s.recipientAddress.state = :state")
    List<Shipment> findByDestinationState(@Param("state") String state);
    
    // Find shipments by destination country
    @Query("SELECT s FROM Shipment s WHERE s.recipientAddress.country = :country")
    List<Shipment> findByDestinationCountry(@Param("country") String country);
    
    // Find overdue shipments (expected delivery date passed but not delivered)
    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryDate < :currentDate AND s.status NOT IN :terminalStatuses")
    List<Shipment> findOverdueShipments(@Param("currentDate") LocalDateTime currentDate, 
                                       @Param("terminalStatuses") List<ShipmentStatus> terminalStatuses);
    
    // Find shipments requiring attention (exceptions, delays, etc.)
    @Query("SELECT s FROM Shipment s WHERE s.status IN :exceptionStatuses")
    List<Shipment> findShipmentsRequiringAttention(@Param("exceptionStatuses") List<ShipmentStatus> exceptionStatuses);
    
    // Count shipments by status
    long countByStatus(ShipmentStatus status);
    
    // Count shipments by carrier
    long countByCarrier(String carrier);
    
    // Find active shipments (not delivered, cancelled, or returned)
    @Query("SELECT s FROM Shipment s WHERE s.status NOT IN :terminalStatuses")
    List<Shipment> findActiveShipments(@Param("terminalStatuses") List<ShipmentStatus> terminalStatuses);
    
    // Find shipments with tracking events count
    @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.trackingEvents WHERE s.id = :shipmentId")
    Optional<Shipment> findByIdWithTrackingEvents(@Param("shipmentId") Long shipmentId);
    
    // Find shipments by tracking number with tracking events
    @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.trackingEvents WHERE s.trackingNumber = :trackingNumber")
    Optional<Shipment> findByTrackingNumberWithEvents(@Param("trackingNumber") String trackingNumber);
    
    // Check if tracking number exists
    boolean existsByTrackingNumber(String trackingNumber);
    
    // Check if order already has shipment
    boolean existsByOrderId(Long orderId);
    
    // Find recent shipments (last N days)
    @Query("SELECT s FROM Shipment s WHERE s.createdAt >= :sinceDate ORDER BY s.createdAt DESC")
    List<Shipment> findRecentShipments(@Param("sinceDate") LocalDateTime sinceDate);
    
    // Custom query for dashboard statistics
    @Query("SELECT s.status, COUNT(s) FROM Shipment s GROUP BY s.status")
    List<Object[]> getShipmentStatusCounts();
    
    // Custom query for carrier performance
    @Query("SELECT s.carrier, COUNT(s), AVG(TIMESTAMPDIFF(DAY, s.shippedDate, s.actualDeliveryDate)) " +
           "FROM Shipment s WHERE s.actualDeliveryDate IS NOT NULL " +
           "GROUP BY s.carrier")
    List<Object[]> getCarrierPerformanceStats();
}
