package com.ecommerce.product.dto;

import java.util.List;

public class CategoryHierarchyDto {
    
    private CategoryResponseDto category;
    private List<CategoryResponseDto> subcategories;
    
    // Constructors
    public CategoryHierarchyDto() {}
    
    public CategoryHierarchyDto(CategoryResponseDto category, List<CategoryResponseDto> subcategories) {
        this.category = category;
        this.subcategories = subcategories;
    }
    
    // Getters and Setters
    public CategoryResponseDto getCategory() {
        return category;
    }
    
    public void setCategory(CategoryResponseDto category) {
        this.category = category;
    }
    
    public List<CategoryResponseDto> getSubcategories() {
        return subcategories;
    }
    
    public void setSubcategories(List<CategoryResponseDto> subcategories) {
        this.subcategories = subcategories;
    }
}