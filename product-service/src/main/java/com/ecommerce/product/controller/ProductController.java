package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductCreateDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.dto.ProductSearchDto;
import com.ecommerce.product.dto.ProductUpdateDto;
import com.ecommerce.product.service.ProductService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    // Create product
    @PostMapping
    @Operation(summary = "Create Product", description = "Creates a new product")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductCreateDto createDto) {
        ProductResponseDto product = productService.createProduct(createDto);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }
    
    // Get product by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get Product by ID", description = "Retrieves product details by ID")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable("id") Long id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    // Get product by SKU
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get Product by SKU", description = "Retrieves product details by SKU")
    public ResponseEntity<ProductResponseDto> getProductBySku(@PathVariable("sku") String sku) {
        ProductResponseDto product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }
    
    // Get all products with pagination
    @GetMapping
    @Operation(summary = "Get All Products", description = "Retrieves all products with pagination")
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Search products
    @GetMapping("/search")
    @Operation(summary = "Search Products", description = "Searches products by keyword")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Advanced search with filters
    @PostMapping("/search/advanced")
    @Operation(summary = "Advanced Search", description = "Searches products using advanced filters")
    public ResponseEntity<Page<ProductResponseDto>> searchWithFilters(
            @RequestBody ProductSearchDto searchDto,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.searchWithFilters(searchDto, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get products by category
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get Products by Category", description = "Retrieves products for a specific category")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get products by brand
    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get Products by Brand", description = "Retrieves products for a specific brand")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByBrand(
            @PathVariable("brand") String brand,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getProductsByBrand(brand, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get products by price range
    @GetMapping("/price-range")
    @Operation(summary = "Get Products by Price Range", description = "Retrieves products within a specific price range")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByPriceRange(
            @RequestParam(name = "minPrice") BigDecimal minPrice,
            @RequestParam(name = "maxPrice") BigDecimal maxPrice,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "price") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get featured products
    @GetMapping("/featured")
    @Operation(summary = "Get Featured Products", description = "Retrieves featured products")
    public ResponseEntity<Page<ProductResponseDto>> getFeaturedProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDto> products = productService.getFeaturedProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get top-rated products
    @GetMapping("/top-rated")
    @Operation(summary = "Get Top Rated Products", description = "Retrieves top rated products")
    public ResponseEntity<Page<ProductResponseDto>> getTopRatedProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getTopRatedProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get recent products
    @GetMapping("/recent")
    @Operation(summary = "Get Recent Products", description = "Retrieves recently added products")
    public ResponseEntity<Page<ProductResponseDto>> getRecentProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDto> products = productService.getRecentProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // Get low stock products
    @GetMapping("/low-stock")
    @Operation(summary = "Get Low Stock Products", description = "Retrieves products with low stock")
    public ResponseEntity<List<ProductResponseDto>> getLowStockProducts(
            @RequestParam(name = "threshold", defaultValue = "10") Integer threshold) {
        List<ProductResponseDto> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    // Update product
    @PutMapping("/{id}")
    @Operation(summary = "Update Product", description = "Updates an existing product")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductUpdateDto updateDto) {
        ProductResponseDto product = productService.updateProduct(id, updateDto);
        return ResponseEntity.ok(product);
    }
    
    // Update stock quantity
    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update Product Stock", description = "Updates the stock quantity of a product")
    public ResponseEntity<ProductResponseDto> updateStock(
            @PathVariable Long id, 
            @RequestParam(name = "quantity") Integer quantity) {
        ProductResponseDto product = productService.updateStock(id, quantity);
        return ResponseEntity.ok(product);
    }
    
    // Activate product
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate Product", description = "Activates a product")
    public ResponseEntity<Void> activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return ResponseEntity.ok().build();
    }
    
    // Deactivate product
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate Product", description = "Deactivates a product")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.ok().build();
    }
}
