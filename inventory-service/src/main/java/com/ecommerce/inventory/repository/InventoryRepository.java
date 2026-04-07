package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.warehouseId = :warehouseId")
    Optional<Inventory> findByProductIdAndWarehouseIdWithLock(@Param("productId") String productId, 
                                                             @Param("warehouseId") String warehouseId);
    
    Optional<Inventory> findByProductIdAndWarehouseId(String productId, String warehouseId);
    
    List<Inventory> findByProductId(String productId);
    
    List<Inventory> findByWarehouseId(String warehouseId);
    
    Page<Inventory> findByWarehouseId(String warehouseId, Pageable pageable);
    
    @Query("SELECT i FROM Inventory i WHERE i.totalQuantity <= i.minimumStockLevel")
    List<Inventory> findLowStockItems();
    
    @Query("SELECT i FROM Inventory i WHERE i.totalQuantity <= i.reorderPoint")
    List<Inventory> findItemsNeedingReorder();
    
    @Query("SELECT i FROM Inventory i WHERE i.totalQuantity >= i.maximumStockLevel")
    List<Inventory> findOverstockItems();
    
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity >= :quantity AND i.productId = :productId")
    List<Inventory> findAvailableInventoryForProduct(@Param("productId") String productId, 
                                                    @Param("quantity") Integer quantity);
    
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity >= :quantity AND i.productId = :productId AND i.warehouseId = :warehouseId")
    Optional<Inventory> findAvailableInventoryForProductInWarehouse(@Param("productId") String productId, 
                                                                   @Param("quantity") Integer quantity,
                                                                   @Param("warehouseId") String warehouseId);
    
    @Query("SELECT SUM(i.totalQuantity) FROM Inventory i WHERE i.productId = :productId")
    Integer getTotalQuantityForProduct(@Param("productId") String productId);
    
    @Query("SELECT SUM(i.availableQuantity) FROM Inventory i WHERE i.productId = :productId")
    Integer getAvailableQuantityForProduct(@Param("productId") String productId);
    
    @Query("SELECT SUM(i.reservedQuantity) FROM Inventory i WHERE i.productId = :productId")
    Integer getReservedQuantityForProduct(@Param("productId") String productId);
    
    @Query("SELECT i FROM Inventory i WHERE i.warehouseId = :warehouseId AND i.totalQuantity <= i.minimumStockLevel")
    List<Inventory> findLowStockItemsByWarehouse(@Param("warehouseId") String warehouseId);
    
    @Query("SELECT i FROM Inventory i WHERE i.warehouseId = :warehouseId AND i.totalQuantity <= i.reorderPoint")
    List<Inventory> findItemsNeedingReorderByWarehouse(@Param("warehouseId") String warehouseId);
    
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.warehouseId = :warehouseId")
    Long countByWarehouseId(@Param("warehouseId") String warehouseId);
    
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.productId = :productId")
    Long countByProductId(@Param("productId") String productId);
    
    @Query("SELECT i FROM Inventory i WHERE i.productId IN :productIds")
    List<Inventory> findByProductIdIn(@Param("productIds") List<String> productIds);
    
    @Query("SELECT i FROM Inventory i WHERE i.warehouseId IN :warehouseIds")
    List<Inventory> findByWarehouseIdIn(@Param("warehouseIds") List<String> warehouseIds);
    
    @Query("SELECT i FROM Inventory i WHERE i.location = :location")
    List<Inventory> findByLocation(@Param("location") String location);
    
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity - :quantity, i.reservedQuantity = i.reservedQuantity + :quantity WHERE i.id = :id AND i.availableQuantity >= :quantity")
    int reserveStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity + :quantity, i.reservedQuantity = i.reservedQuantity - :quantity WHERE i.id = :id AND i.reservedQuantity >= :quantity")
    int releaseReservation(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity, i.totalQuantity = i.totalQuantity - :quantity WHERE i.id = :id AND i.reservedQuantity >= :quantity")
    int confirmReservation(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Query("SELECT DISTINCT i.warehouseId FROM Inventory i")
    List<String> findAllWarehouseIds();
    
    @Query("SELECT DISTINCT i.productId FROM Inventory i WHERE i.warehouseId = :warehouseId")
    List<String> findProductIdsByWarehouseId(@Param("warehouseId") String warehouseId);
}
