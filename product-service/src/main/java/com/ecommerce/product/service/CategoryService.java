package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryCreateDto;
import com.ecommerce.product.dto.CategoryHierarchyDto;
import com.ecommerce.product.dto.CategoryResponseDto;
import com.ecommerce.product.dto.CategoryUpdateDto;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.exception.CategoryAlreadyExistsException;
import com.ecommerce.product.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String CATEGORY_EVENTS_TOPIC = "category-events";
    
    // Create category
    public CategoryResponseDto createCategory(CategoryCreateDto createDto) {
        // Check if category name already exists
        if (categoryRepository.existsByNameIgnoreCase(createDto.getName())) {
            throw new CategoryAlreadyExistsException("Category with name " + createDto.getName() + " already exists");
        }
        
        Category category = convertToEntity(createDto);
        Category savedCategory = categoryRepository.save(category);
        
        // Publish category created event
        publishCategoryEvent("CATEGORY_CREATED", savedCategory);
        
        return convertToResponseDto(savedCategory);
    }
    
    // Get category by ID
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        return convertToResponseDto(category);
    }
    
    // Get category by name
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with name: " + name));
        return convertToResponseDto(category);
    }
    
    // Get all categories
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Get all categories with pagination
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CategoryResponseDto> getAllCategories(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(this::convertToResponseDto);
    }
    
    // Get active categories
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrder()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Get root categories (no parent)
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getRootCategories() {
        return categoryRepository.findByParentIdIsNullAndActiveTrueOrderByDisplayOrder()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Get subcategories by parent ID
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getSubcategories(Long parentId) {
        return categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrder(parentId)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Search categories
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> searchCategories(String keyword) {
        return categoryRepository.searchByKeyword(keyword)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Search categories with pagination
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CategoryResponseDto> searchCategories(String keyword, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Category> categories = categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return categories.map(this::convertToResponseDto);
    }
    
    // Get category hierarchy by parent ID
    @Transactional(readOnly = true)
    public List<CategoryHierarchyDto> getCategoryHierarchyByParent(Long parentId) {
        List<Category> categories;
        if (parentId == null) {
            categories = categoryRepository.findByParentIdIsNullAndActiveTrueOrderByDisplayOrder();
        } else {
            categories = categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrder(parentId);
        }
        
        return categories.stream()
                .map(category -> {
                    CategoryHierarchyDto hierarchyDto = new CategoryHierarchyDto();
                    hierarchyDto.setCategory(convertToResponseDto(category));
                    
                    List<CategoryResponseDto> subcategories = categoryRepository
                            .findByParentIdAndActiveTrueOrderByDisplayOrder(category.getId())
                            .stream()
                            .map(this::convertToResponseDto)
                            .collect(Collectors.toList());
                    
                    hierarchyDto.setSubcategories(subcategories);
                    return hierarchyDto;
                })
                .collect(Collectors.toList());
    }
    
    // Get total category count
    @Transactional(readOnly = true)
    public Long getCategoryCount() {
        return categoryRepository.count();
    }
    
    // Get active category count
    @Transactional(readOnly = true)
    public Long getActiveCategoryCount() {
        return categoryRepository.countByActiveTrue();
    }
    
    // Get complete category hierarchy
    @Transactional(readOnly = true)
    public List<CategoryHierarchyDto> getCategoryHierarchy() {
        return getCategoryHierarchyByParent(null);
    }
    
    // Update category
    public CategoryResponseDto updateCategory(Long id, CategoryUpdateDto updateDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!category.getName().equalsIgnoreCase(updateDto.getName())) {
            if (categoryRepository.existsByNameIgnoreCase(updateDto.getName())) {
                throw new CategoryAlreadyExistsException("Category with name " + updateDto.getName() + " already exists");
            }
        }
        
        updateCategoryFromDto(category, updateDto);
        Category updatedCategory = categoryRepository.save(category);
        
        // Publish category updated event
        publishCategoryEvent("CATEGORY_UPDATED", updatedCategory);
        
        return convertToResponseDto(updatedCategory);
    }
    
    // Deactivate category
    public void deactivateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        
        category.setActive(false);
        categoryRepository.save(category);
        
        // Publish category deactivated event
        publishCategoryEvent("CATEGORY_DEACTIVATED", category);
    }
    
    // Activate category
    public void activateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        
        category.setActive(true);
        categoryRepository.save(category);
        
        // Publish category activated event
        publishCategoryEvent("CATEGORY_ACTIVATED", category);
    }
    
    // Delete category (soft delete by deactivating)
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        
        // Check if category has subcategories
        Long subcategoryCount = categoryRepository.countByParentIdAndActiveTrue(id);
        if (subcategoryCount > 0) {
            throw new IllegalStateException("Cannot delete category with active subcategories");
        }
        
        category.setActive(false);
        categoryRepository.save(category);
        
        // Publish category deleted event
        publishCategoryEvent("CATEGORY_DELETED", category);
    }
    
    // Get category hierarchy (category with its subcategories)
    @Transactional(readOnly = true)
    public CategoryHierarchyDto getCategoryHierarchy(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        
        CategoryHierarchyDto hierarchyDto = new CategoryHierarchyDto();
        hierarchyDto.setCategory(convertToResponseDto(category));
        
        List<CategoryResponseDto> subcategories = categoryRepository
                .findByParentIdAndActiveTrueOrderByDisplayOrder(id)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        
        hierarchyDto.setSubcategories(subcategories);
        return hierarchyDto;
    }
    
    // Helper methods
    private Category convertToEntity(CategoryCreateDto createDto) {
        Category category = new Category();
        category.setName(createDto.getName());
        category.setDescription(createDto.getDescription());
        category.setImageUrl(createDto.getImageUrl());
        category.setParentId(createDto.getParentId());
        category.setDisplayOrder(createDto.getDisplayOrder());
        return category;
    }
    
    private void updateCategoryFromDto(Category category, CategoryUpdateDto updateDto) {
        category.setName(updateDto.getName());
        category.setDescription(updateDto.getDescription());
        category.setImageUrl(updateDto.getImageUrl());
        category.setParentId(updateDto.getParentId());
        category.setDisplayOrder(updateDto.getDisplayOrder());
    }
    
    private CategoryResponseDto convertToResponseDto(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setParentId(category.getParentId());
        dto.setActive(category.getActive());
        dto.setDisplayOrder(category.getDisplayOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
    
    private void publishCategoryEvent(String eventType, Category category) {
        CategoryEventDto event = new CategoryEventDto(eventType, category.getId(), 
                                                     category.getName(), category.getParentId());
        kafkaTemplate.send(CATEGORY_EVENTS_TOPIC, event);
    }
    
    // Event DTO
    public static class CategoryEventDto {
        private String eventType;
        private Long categoryId;
        private String name;
        private Long parentId;
        
        public CategoryEventDto(String eventType, Long categoryId, String name, Long parentId) {
            this.eventType = eventType;
            this.categoryId = categoryId;
            this.name = name;
            this.parentId = parentId;
        }
        
        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
    }
}