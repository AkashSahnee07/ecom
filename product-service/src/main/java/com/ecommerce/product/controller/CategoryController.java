package com.ecommerce.product.controller;

import com.ecommerce.product.dto.CategoryCreateDto;
import com.ecommerce.product.dto.CategoryHierarchyDto;
import com.ecommerce.product.dto.CategoryResponseDto;
import com.ecommerce.product.dto.CategoryUpdateDto;
import com.ecommerce.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(name = "Category Management", description = "APIs for managing product categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    // Create category
    @PostMapping
    @Operation(summary = "Create Category", description = "Creates a new product category")
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryCreateDto createDto) {
        CategoryResponseDto category = categoryService.createCategory(createDto);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }
    
    // Get category by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get Category by ID", description = "Retrieves category details by ID")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    // Get category by name
    @GetMapping("/name/{name}")
    @Operation(summary = "Get Category by Name", description = "Retrieves category details by name")
    public ResponseEntity<CategoryResponseDto> getCategoryByName(@PathVariable String name) {
        CategoryResponseDto category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }
    
    // Get all categories with pagination
    @GetMapping
    @Operation(summary = "Get All Categories", description = "Retrieves all categories with pagination")
    public ResponseEntity<Page<CategoryResponseDto>> getAllCategories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "displayOrder") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CategoryResponseDto> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }
    
    // Get active categories
    @GetMapping("/active")
    @Operation(summary = "Get Active Categories", description = "Retrieves all active categories")
    public ResponseEntity<List<CategoryResponseDto>> getActiveCategories() {
        List<CategoryResponseDto> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    // Get root categories (no parent)
    @GetMapping("/root")
    @Operation(summary = "Get Root Categories", description = "Retrieves all top-level categories")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories() {
        List<CategoryResponseDto> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }
    
    // Get subcategories by parent ID
    @GetMapping("/{parentId}/subcategories")
    @Operation(summary = "Get Subcategories", description = "Retrieves subcategories for a given parent category")
    public ResponseEntity<List<CategoryResponseDto>> getSubcategories(@PathVariable Long parentId) {
        List<CategoryResponseDto> categories = categoryService.getSubcategories(parentId);
        return ResponseEntity.ok(categories);
    }
    
    // Get category hierarchy
    @GetMapping("/hierarchy")
    @Operation(summary = "Get Category Hierarchy", description = "Retrieves the full category hierarchy tree")
    public ResponseEntity<List<CategoryHierarchyDto>> getCategoryHierarchy() {
        List<CategoryHierarchyDto> hierarchy = categoryService.getCategoryHierarchy();
        return ResponseEntity.ok(hierarchy);
    }
    
    // Get category hierarchy by parent ID
    @GetMapping("/{parentId}/hierarchy")
    @Operation(summary = "Get Hierarchy by Parent", description = "Retrieves category hierarchy starting from a specific parent")
    public ResponseEntity<List<CategoryHierarchyDto>> getCategoryHierarchyByParent(@PathVariable Long parentId) {
        List<CategoryHierarchyDto> hierarchy = categoryService.getCategoryHierarchyByParent(parentId);
        return ResponseEntity.ok(hierarchy);
    }
    
    // Search categories
    @GetMapping("/search")
    @Operation(summary = "Search Categories", description = "Searches categories by keyword")
    public ResponseEntity<Page<CategoryResponseDto>> searchCategories(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CategoryResponseDto> categories = categoryService.searchCategories(keyword, pageable);
        return ResponseEntity.ok(categories);
    }
    
    // Update category
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryUpdateDto updateDto) {
        CategoryResponseDto category = categoryService.updateCategory(id, updateDto);
        return ResponseEntity.ok(category);
    }
    
    // Activate category
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCategory(@PathVariable Long id) {
        categoryService.activateCategory(id);
        return ResponseEntity.ok().build();
    }
    
    // Deactivate category
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.ok().build();
    }
    
    // Delete category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    // Get category count
    @GetMapping("/count")
    public ResponseEntity<Long> getCategoryCount() {
        Long count = categoryService.getCategoryCount();
        return ResponseEntity.ok(count);
    }
    
    // Get active category count
    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveCategoryCount() {
        Long count = categoryService.getActiveCategoryCount();
        return ResponseEntity.ok(count);
    }
}
