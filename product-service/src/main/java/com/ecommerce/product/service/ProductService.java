package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductCreateDto;
import com.ecommerce.product.dto.ProductResponseDto;
import com.ecommerce.product.dto.ProductSearchDto;
import com.ecommerce.product.dto.ProductUpdateDto;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.exception.ProductAlreadyExistsException;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String PRODUCT_EVENTS_TOPIC = "product-events";
    
    // Create product
    public ProductResponseDto createProduct(ProductCreateDto createDto) {
        // Check if SKU already exists
        if (productRepository.findBySku(createDto.getSku()).isPresent()) {
            throw new ProductAlreadyExistsException("Product with SKU " + createDto.getSku() + " already exists");
        }
        
        Product product = convertToEntity(createDto);
        Product savedProduct = productRepository.save(product);
        
        // Publish product created event
        publishProductEvent("PRODUCT_CREATED", savedProduct);
        
        return convertToResponseDto(savedProduct);
    }
    
    // Get product by ID
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        return convertToResponseDto(product);
    }
    
    // Get product by SKU
    @Transactional(readOnly = true)
    public ProductResponseDto getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        return convertToResponseDto(product);
    }
    
    // Get all products with pagination
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::convertToResponseDto);
    }
    
    // Search products
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable)
                .map(this::convertToResponseDto);
    }
    
    // Get products by category
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToResponseDto);
    }
    
    // Get products by brand
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByBrand(String brand, Pageable pageable) {
        return productRepository.findByBrand(brand, pageable)
                .map(this::convertToResponseDto);
    }
    
    // Get products by price range
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable)
                .map(this::convertToResponseDto);
    }
    
    // Advanced search with filters
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchWithFilters(ProductSearchDto searchDto, Pageable pageable) {
        return productRepository.findWithFilters(
                searchDto.getKeyword(),
                searchDto.getCategoryId(),
                searchDto.getBrand(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getMinRating(),
                pageable
        ).map(this::convertToResponseDto);
    }
    
    // Get featured products
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getFeaturedProducts(Pageable pageable) {
        return productRepository.findByFeaturedTrue(pageable)
                .map(this::convertToResponseDto);
    }
    
    // Get top-rated products
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getTopRatedProducts(Pageable pageable) {
        return productRepository.findTopRatedProducts(pageable)
                .map(this::convertToResponseDto);
    }
    
    // Get recent products
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getRecentProducts(Pageable pageable) {
        return productRepository.findRecentProducts(pageable)
                .map(this::convertToResponseDto);
    }
    
    // Update product
    public ProductResponseDto updateProduct(Long id, ProductUpdateDto updateDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        
        // Check if SKU is being changed and if new SKU already exists
        if (!product.getSku().equals(updateDto.getSku())) {
            Optional<Product> existingProduct = productRepository.findBySku(updateDto.getSku());
            if (existingProduct.isPresent() && !existingProduct.get().getId().equals(id)) {
                throw new ProductAlreadyExistsException("Product with SKU " + updateDto.getSku() + " already exists");
            }
        }
        
        updateProductFromDto(product, updateDto);
        Product updatedProduct = productRepository.save(product);
        
        // Publish product updated event
        publishProductEvent("PRODUCT_UPDATED", updatedProduct);
        
        return convertToResponseDto(updatedProduct);
    }
    
    // Update stock quantity
    public ProductResponseDto updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        
        product.setStockQuantity(quantity);
        Product updatedProduct = productRepository.save(product);
        
        // Publish stock updated event
        publishProductEvent("STOCK_UPDATED", updatedProduct);
        
        return convertToResponseDto(updatedProduct);
    }
    
    // Deactivate product
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        
        product.setActive(false);
        productRepository.save(product);
        
        // Publish product deactivated event
        publishProductEvent("PRODUCT_DEACTIVATED", product);
    }
    
    // Activate product
    public void activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        
        product.setActive(true);
        productRepository.save(product);
        
        // Publish product activated event
        publishProductEvent("PRODUCT_ACTIVATED", product);
    }
    
    // Get low stock products
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Helper methods
    private Product convertToEntity(ProductCreateDto createDto) {
        Product product = new Product();
        product.setName(createDto.getName());
        product.setDescription(createDto.getDescription());
        product.setPrice(createDto.getPrice());
        product.setSku(createDto.getSku());
        product.setCategoryId(createDto.getCategoryId());
        product.setBrand(createDto.getBrand());
        product.setStockQuantity(createDto.getStockQuantity());
        product.setWeight(createDto.getWeight());
        product.setDimensions(createDto.getDimensions());
        product.setImageUrls(createDto.getImageUrls());
        product.setTags(createDto.getTags());
        return product;
    }
    
    private void updateProductFromDto(Product product, ProductUpdateDto updateDto) {
        product.setName(updateDto.getName());
        product.setDescription(updateDto.getDescription());
        product.setPrice(updateDto.getPrice());
        product.setSku(updateDto.getSku());
        product.setCategoryId(updateDto.getCategoryId());
        product.setBrand(updateDto.getBrand());
        product.setStockQuantity(updateDto.getStockQuantity());
        product.setWeight(updateDto.getWeight());
        product.setDimensions(updateDto.getDimensions());
        product.setImageUrls(updateDto.getImageUrls());
        product.setTags(updateDto.getTags());
        product.setFeatured(updateDto.getFeatured());
    }
    
    private ProductResponseDto convertToResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setSku(product.getSku());
        dto.setCategoryId(product.getCategoryId());
        dto.setBrand(product.getBrand());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setWeight(product.getWeight());
        dto.setDimensions(product.getDimensions());
        dto.setImageUrls(product.getImageUrls());
        dto.setTags(product.getTags());
        dto.setActive(product.getActive());
        dto.setFeatured(product.getFeatured());
        dto.setAverageRating(product.getAverageRating());
        dto.setReviewCount(product.getReviewCount());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
    
    private void publishProductEvent(String eventType, Product product) {
        ProductEventDto event = new ProductEventDto(eventType, product.getId(), product.getSku(), 
                                                   product.getName(), product.getStockQuantity());
        kafkaTemplate.send(PRODUCT_EVENTS_TOPIC, event);
    }
    
    // Event DTO
    public static class ProductEventDto {
        private String eventType;
        private Long productId;
        private String sku;
        private String name;
        private Integer stockQuantity;
        
        public ProductEventDto(String eventType, Long productId, String sku, String name, Integer stockQuantity) {
            this.eventType = eventType;
            this.productId = productId;
            this.sku = sku;
            this.name = name;
            this.stockQuantity = stockQuantity;
        }
        
        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    }
}
