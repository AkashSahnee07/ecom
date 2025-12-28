package com.ecommerce.recommendation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Feign client for Product Service
 */
@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {
    
    @GetMapping("/api/products/{id}")
    ProductDto getProduct(@PathVariable("id") Long productId);
    
    @GetMapping("/api/products")
    List<ProductDto> getProducts(@RequestParam("ids") List<Long> productIds);
    
    @GetMapping("/api/products/category/{category}")
    List<ProductDto> getProductsByCategory(@PathVariable("category") String category);
    
    @GetMapping("/api/products/brand/{brand}")
    List<ProductDto> getProductsByBrand(@PathVariable("brand") String brand);
    
    @GetMapping("/api/products/search")
    List<ProductDto> searchProducts(
        @RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "brand", required = false) String brand,
        @RequestParam(value = "minPrice", required = false) Double minPrice,
        @RequestParam(value = "maxPrice", required = false) Double maxPrice,
        @RequestParam(value = "inStock", required = false) Boolean inStock,
        @RequestParam(value = "limit", defaultValue = "50") Integer limit
    );
    
    @GetMapping("/api/products/trending")
    List<ProductDto> getTrendingProducts(@RequestParam(value = "limit", defaultValue = "20") Integer limit);
    
    @GetMapping("/api/products/popular")
    List<ProductDto> getPopularProducts(@RequestParam(value = "limit", defaultValue = "20") Integer limit);
    
    @GetMapping("/api/products/{id}/similar")
    List<ProductDto> getSimilarProducts(
        @PathVariable("id") Long productId,
        @RequestParam(value = "limit", defaultValue = "10") Integer limit
    );
    
    @GetMapping("/api/products/categories")
    List<String> getAllCategories();
    
    @GetMapping("/api/products/brands")
    List<String> getAllBrands();
    
    /**
     * Product DTO for Feign client responses
     */
    class ProductDto {
        private Long id;
        private String name;
        private String description;
        private String category;
        private String brand;
        private Double price;
        private String imageUrl;
        private Boolean inStock;
        private Integer stockQuantity;
        private Double rating;
        private Integer reviewCount;
        private Double discount;
        private List<String> tags;
        private Map<String, Object> attributes;
        
        // Constructors
        public ProductDto() {}
        
        public ProductDto(Long id, String name, String description, String category, 
                         String brand, Double price, String imageUrl, Boolean inStock, 
                         Integer stockQuantity, Double rating, Integer reviewCount, 
                         Double discount, List<String> tags, Map<String, Object> attributes) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.brand = brand;
            this.price = price;
            this.imageUrl = imageUrl;
            this.inStock = inStock;
            this.stockQuantity = stockQuantity;
            this.rating = rating;
            this.reviewCount = reviewCount;
            this.discount = discount;
            this.tags = tags;
            this.attributes = attributes;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public Boolean getInStock() { return inStock; }
        public void setInStock(Boolean inStock) { this.inStock = inStock; }
        
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public Integer getReviewCount() { return reviewCount; }
        public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
        
        public Double getDiscount() { return discount; }
        public void setDiscount(Double discount) { this.discount = discount; }
        
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        
        public Map<String, Object> getAttributes() { return attributes; }
        public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    }
}