package com.ecommerce.product.controller;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    // Create product
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductCreateDto createDto) {
        ProductResponseDto product = productService.createProduct(createDto);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }
    
    // Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    // Get product by SKU
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponseDto> getProductBySku(@PathVariable String sku) {
        ProductResponseDto product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }
    
    // Get all products with pagination
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Search products
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Advanced search with filters
    @PostMapping("/search/advanced")
    public ResponseEntity<Page<ProductResponseDto>> searchWithFilters(
            @RequestBody ProductSearchDto searchDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.searchWithFilters(searchDto, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get products by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get products by brand
    @GetMapping("/brand/{brand}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByBrand(
            @PathVariable String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getProductsByBrand(brand, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get products by price range
    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get featured products
    @GetMapping("/featured")
    public ResponseEntity<Page<ProductResponseDto>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getFeaturedProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get top-rated products
    @GetMapping("/top-rated")
    public ResponseEntity<Page<ProductResponseDto>> getTopRatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getTopRatedProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get recent products
    @GetMapping("/recent")
    public ResponseEntity<Page<ProductResponseDto>> getRecentProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getRecentProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get low stock products
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponseDto>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        List<ProductResponseDto> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    // Update product
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductUpdateDto updateDto) {
        ProductResponseDto product = productService.updateProduct(id, updateDto);
        return ResponseEntity.ok(product);
    }
    
    // Update stock quantity
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponseDto> updateStock(
            @PathVariable Long id, 
            @RequestParam Integer quantity) {
        ProductResponseDto product = productService.updateStock(id, quantity);
        return ResponseEntity.ok(product);
    }
    
    // Activate product
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return ResponseEntity.ok().build();
    }
    
    // Deactivate product
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.ok().build();
    }
}