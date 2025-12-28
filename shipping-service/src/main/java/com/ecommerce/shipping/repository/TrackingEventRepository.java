package com.ecommerce.shipping.repository;

import com.ecommerce.shipping.entity.TrackingEvent;
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
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    
    // Find events by shipment ID
    List<TrackingEvent> findByShipmentIdOrderByEventTimestampDesc(Long shipmentId);
    
    // Find events by shipment ID with pagination
    Page<TrackingEvent> findByShipmentIdOrderByEventTimestampDesc(Long shipmentId, Pageable pageable);
    
    // Find events by status
    List<TrackingEvent> findByStatus(ShipmentStatus status);
    
    // Find events by status and date range
    List<TrackingEvent> findByStatusAndEventTimestampBetween(ShipmentStatus status, 
                                                           LocalDateTime startDate, 
                                                           LocalDateTime endDate);
    
    // Find events by location
    List<TrackingEvent> findByLocationContainingIgnoreCase(String location);
    
    // Find events by event code
    List<TrackingEvent> findByEventCode(String eventCode);
    
    // Find events by carrier event code
    List<TrackingEvent> findByCarrierEventCode(String carrierEventCode);
    
    // Find latest event for a shipment
    @Query("SELECT te FROM TrackingEvent te WHERE te.shipment.id = :shipmentId " +
           "ORDER BY te.eventTimestamp DESC LIMIT 1")
    Optional<TrackingEvent> findLatestEventByShipmentId(@Param("shipmentId") Long shipmentId);
    
    // Find events by date range
    List<TrackingEvent> findByEventTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find events created between dates
    List<TrackingEvent> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find exception events
    @Query("SELECT te FROM TrackingEvent te WHERE te.status IN :exceptionStatuses " +
           "ORDER BY te.eventTimestamp DESC")
    List<TrackingEvent> findExceptionEvents(@Param("exceptionStatuses") List<ShipmentStatus> exceptionStatuses);
    
    // Find delivery events
    List<TrackingEvent> findByStatusOrderByEventTimestampDesc(ShipmentStatus status);
    
    // Count events by shipment
    long countByShipmentId(Long shipmentId);
    
    // Count events by status
    long countByStatus(ShipmentStatus status);
    
    // Find events by multiple statuses
    List<TrackingEvent> findByStatusInOrderByEventTimestampDesc(List<ShipmentStatus> statuses);
    
    // Find events with notes
    @Query("SELECT te FROM TrackingEvent te WHERE te.notes IS NOT NULL AND te.notes != '' " +
           "ORDER BY te.eventTimestamp DESC")
    List<TrackingEvent> findEventsWithNotes();
    
    // Find events by description pattern
    List<TrackingEvent> findByDescriptionContainingIgnoreCase(String description);
    
    // Find recent events (last N hours/days)
    @Query("SELECT te FROM TrackingEvent te WHERE te.eventTimestamp >= :sinceDate " +
           "ORDER BY te.eventTimestamp DESC")
    List<TrackingEvent> findRecentEvents(@Param("sinceDate") LocalDateTime sinceDate);
    
    // Find first event for a shipment
    @Query("SELECT te FROM TrackingEvent te WHERE te.shipment.id = :shipmentId " +
           "ORDER BY te.eventTimestamp ASC LIMIT 1")
    Optional<TrackingEvent> findFirstEventByShipmentId(@Param("shipmentId") Long shipmentId);
    
    // Find events between two statuses for a shipment
    @Query("SELECT te FROM TrackingEvent te WHERE te.shipment.id = :shipmentId " +
           "AND te.eventTimestamp BETWEEN " +
           "(SELECT MIN(te2.eventTimestamp) FROM TrackingEvent te2 WHERE te2.shipment.id = :shipmentId AND te2.status = :startStatus) " +
           "AND (SELECT MAX(te3.eventTimestamp) FROM TrackingEvent te3 WHERE te3.shipment.id = :shipmentId AND te3.status = :endStatus) " +
           "ORDER BY te.eventTimestamp ASC")
    List<TrackingEvent> findEventsBetweenStatuses(@Param("shipmentId") Long shipmentId,
                                                 @Param("startStatus") ShipmentStatus startStatus,
                                                 @Param("endStatus") ShipmentStatus endStatus);
    
    // Check if event exists for shipment and status
    boolean existsByShipmentIdAndStatus(Long shipmentId, ShipmentStatus status);
    
    // Find duplicate events (same shipment, status, and timestamp)
    @Query("SELECT te FROM TrackingEvent te WHERE te.shipment.id = :shipmentId " +
           "AND te.status = :status AND te.eventTimestamp = :timestamp")
    List<TrackingEvent> findDuplicateEvents(@Param("shipmentId") Long shipmentId,
                                          @Param("status") ShipmentStatus status,
                                          @Param("timestamp") LocalDateTime timestamp);
    
    // Delete events older than specified date
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
    
    // Custom query for event statistics
    @Query("SELECT te.status, COUNT(te) FROM TrackingEvent te " +
           "WHERE te.eventTimestamp >= :sinceDate " +
           "GROUP BY te.status")
    List<Object[]> getEventStatusCounts(@Param("sinceDate") LocalDateTime sinceDate);
    
    // Custom query for location-based statistics
    @Query("SELECT te.location, COUNT(te) FROM TrackingEvent te " +
           "WHERE te.location IS NOT NULL AND te.eventTimestamp >= :sinceDate " +
           "GROUP BY te.location " +
           "ORDER BY COUNT(te) DESC")
    List<Object[]> getLocationEventCounts(@Param("sinceDate") LocalDateTime sinceDate);
}