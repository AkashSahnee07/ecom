package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.MovementType;
import com.ecommerce.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    List<StockMovement> findByProductId(String productId);
    
    Page<StockMovement> findByProductId(String productId, Pageable pageable);
    
    List<StockMovement> findByWarehouseId(String warehouseId);
    
    Page<StockMovement> findByWarehouseId(String warehouseId, Pageable pageable);
    
    List<StockMovement> findByProductIdAndWarehouseId(String productId, String warehouseId);
    
    Page<StockMovement> findByProductIdAndWarehouseId(String productId, String warehouseId, Pageable pageable);
    
    List<StockMovement> findByMovementType(MovementType movementType);
    
    Page<StockMovement> findByMovementType(MovementType movementType, Pageable pageable);
    
    List<StockMovement> findByReferenceId(String referenceId);
    
    List<StockMovement> findByReferenceType(String referenceType);
    
    List<StockMovement> findByPerformedBy(String performedBy);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.createdAt BETWEEN :startDate AND :endDate")
    List<StockMovement> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.createdAt BETWEEN :startDate AND :endDate")
    Page<StockMovement> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate, 
                                       Pageable pageable);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.productId = :productId AND sm.createdAt BETWEEN :startDate AND :endDate")
    List<StockMovement> findByProductIdAndDateRange(@Param("productId") String productId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.warehouseId = :warehouseId AND sm.createdAt BETWEEN :startDate AND :endDate")
    List<StockMovement> findByWarehouseIdAndDateRange(@Param("warehouseId") String warehouseId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.productId = :productId AND sm.warehouseId = :warehouseId AND sm.createdAt BETWEEN :startDate AND :endDate")
    List<StockMovement> findByProductIdAndWarehouseIdAndDateRange(@Param("productId") String productId,
                                                                 @Param("warehouseId") String warehouseId,
                                                                 @Param("startDate") LocalDateTime startDate,
                                                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementType = :movementType AND sm.createdAt BETWEEN :startDate AND :endDate")
    List<StockMovement> findByMovementTypeAndDateRange(@Param("movementType") MovementType movementType,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(sm.quantity) FROM StockMovement sm WHERE sm.productId = :productId AND sm.movementType = :movementType")
    Integer getTotalQuantityByProductAndMovementType(@Param("productId") String productId, 
                                                    @Param("movementType") MovementType movementType);
    
    @Query("SELECT SUM(sm.quantity) FROM StockMovement sm WHERE sm.productId = :productId AND sm.movementType = :movementType AND sm.createdAt BETWEEN :startDate AND :endDate")
    Integer getTotalQuantityByProductAndMovementTypeAndDateRange(@Param("productId") String productId,
                                                               @Param("movementType") MovementType movementType,
                                                               @Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(sm) FROM StockMovement sm WHERE sm.productId = :productId")
    Long countByProductId(@Param("productId") String productId);
    
    @Query("SELECT COUNT(sm) FROM StockMovement sm WHERE sm.warehouseId = :warehouseId")
    Long countByWarehouseId(@Param("warehouseId") String warehouseId);
    
    @Query("SELECT COUNT(sm) FROM StockMovement sm WHERE sm.movementType = :movementType")
    Long countByMovementType(@Param("movementType") MovementType movementType);
    
    @Query("SELECT DISTINCT sm.productId FROM StockMovement sm WHERE sm.warehouseId = :warehouseId")
    List<String> findDistinctProductIdsByWarehouseId(@Param("warehouseId") String warehouseId);
    
    @Query("SELECT DISTINCT sm.warehouseId FROM StockMovement sm WHERE sm.productId = :productId")
    List<String> findDistinctWarehouseIdsByProductId(@Param("productId") String productId);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.productId IN :productIds")
    List<StockMovement> findByProductIdIn(@Param("productIds") List<String> productIds);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.warehouseId IN :warehouseIds")
    List<StockMovement> findByWarehouseIdIn(@Param("warehouseIds") List<String> warehouseIds);
    
    @Query("SELECT sm FROM StockMovement sm ORDER BY sm.createdAt DESC")
    Page<StockMovement> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.productId = :productId ORDER BY sm.createdAt DESC")
    Page<StockMovement> findByProductIdOrderByCreatedAtDesc(@Param("productId") String productId, Pageable pageable);
}
