package com.ecommerce.product.controller;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    // Create category
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryCreateDto createDto) {
        CategoryResponseDto category = categoryService.createCategory(createDto);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }
    
    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    // Get category by name
    @GetMapping("/name/{name}")
    public ResponseEntity<CategoryResponseDto> getCategoryByName(@PathVariable String name) {
        CategoryResponseDto category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }
    
    // Get all categories with pagination
    @GetMapping
    public ResponseEntity<Page<CategoryResponseDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CategoryResponseDto> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }
    
    // Get active categories
    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponseDto>> getActiveCategories() {
        List<CategoryResponseDto> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    // Get root categories (no parent)
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories() {
        List<CategoryResponseDto> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }
    
    // Get subcategories by parent ID
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryResponseDto>> getSubcategories(@PathVariable Long parentId) {
        List<CategoryResponseDto> categories = categoryService.getSubcategories(parentId);
        return ResponseEntity.ok(categories);
    }
    
    // Get category hierarchy
    @GetMapping("/hierarchy")
    public ResponseEntity<List<CategoryHierarchyDto>> getCategoryHierarchy() {
        List<CategoryHierarchyDto> hierarchy = categoryService.getCategoryHierarchy();
        return ResponseEntity.ok(hierarchy);
    }
    
    // Get category hierarchy by parent ID
    @GetMapping("/{parentId}/hierarchy")
    public ResponseEntity<List<CategoryHierarchyDto>> getCategoryHierarchyByParent(@PathVariable Long parentId) {
        List<CategoryHierarchyDto> hierarchy = categoryService.getCategoryHierarchyByParent(parentId);
        return ResponseEntity.ok(hierarchy);
    }
    
    // Search categories
    @GetMapping("/search")
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