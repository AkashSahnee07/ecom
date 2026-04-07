package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find by SKU
    Optional<Product> findBySku(String sku);
    
    // Find by category
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    // Find by brand
    List<Product> findByBrand(String brand);
    Page<Product> findByBrand(String brand, Pageable pageable);
    
    // Find active products
    List<Product> findByActiveTrue();
    Page<Product> findByActiveTrue(Pageable pageable);
    
    // Find featured products
    List<Product> findByFeaturedTrue();
    Page<Product> findByFeaturedTrue(Pageable pageable);
    
    // Find by price range
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                  @Param("maxPrice") BigDecimal maxPrice, 
                                  Pageable pageable);
    
    // Search by name or description
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND p.active = true")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Find products with low stock
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.active = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    // Find products by category and brand
    Page<Product> findByCategoryIdAndBrandAndActiveTrue(Long categoryId, String brand, Pageable pageable);
    
    // Find products by multiple categories
    @Query("SELECT p FROM Product p WHERE p.categoryId IN :categoryIds AND p.active = true")
    Page<Product> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);
    
    // Find products by rating range
    @Query("SELECT p FROM Product p WHERE p.averageRating >= :minRating AND p.active = true")
    Page<Product> findByMinRating(@Param("minRating") BigDecimal minRating, Pageable pageable);
    
    // Count products by category
    Long countByCategoryId(Long categoryId);
    
    // Count active products
    Long countByActiveTrue();
    
    // Find top-rated products
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.averageRating DESC, p.reviewCount DESC")
    Page<Product> findTopRatedProducts(Pageable pageable);
    
    // Find recently added products
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdAt DESC")
    Page<Product> findRecentProducts(Pageable pageable);
    
    // Find by price range (for content-based filtering)
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    List<Product> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    // Find by minimum rating (for content-based filtering)
    @Query("SELECT p FROM Product p WHERE p.averageRating >= :minRating AND p.active = true")
    List<Product> findByAverageRatingGreaterThanEqual(@Param("minRating") BigDecimal minRating);
    
    // Advanced search with filters
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND (:brand IS NULL OR p.brand = :brand) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:minRating IS NULL OR p.averageRating >= :minRating) " +
           "AND p.active = true")
    Page<Product> findWithFilters(@Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 @Param("brand") String brand,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("minRating") BigDecimal minRating,
                                 Pageable pageable);
}
