package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find by name
    Optional<Category> findByName(String name);
    
    // Find active categories
    List<Category> findByActiveTrueOrderByDisplayOrder();
    
    // Find root categories (no parent)
    List<Category> findByParentIdIsNullAndActiveTrueOrderByDisplayOrder();
    
    // Find subcategories by parent ID
    List<Category> findByParentIdAndActiveTrueOrderByDisplayOrder(Long parentId);
    
    // Find all subcategories (has parent)
    List<Category> findByParentIdIsNotNullAndActiveTrueOrderByDisplayOrder();
    
    // Check if category exists by name (case insensitive)
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);
    
    // Count active categories
    Long countByActiveTrue();
    
    // Count subcategories by parent
    Long countByParentIdAndActiveTrue(Long parentId);
    
    // Find categories by name containing (search)
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.active = true " +
           "ORDER BY c.displayOrder")
    List<Category> searchByKeyword(@Param("keyword") String keyword);
    
    // Find categories by name containing (case insensitive)
    List<Category> findByNameContainingIgnoreCase(String keyword);
    
    // Find categories by name containing with pagination
    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
