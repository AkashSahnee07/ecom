package com.ecommerce.recommendation.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation for Product Service client
 */
@Component
public class ProductServiceClientFallback implements ProductServiceClient {
    
    private static final Logger log = LoggerFactory.getLogger(ProductServiceClientFallback.class);
    
    @Override
    public ProductDto getProduct(Long productId) {
        log.warn("Product service is unavailable. Returning empty product for ID: {}", productId);
        return createFallbackProduct(productId);
    }
    
    @Override
    public List<ProductDto> getProducts(List<Long> productIds) {
        log.warn("Product service is unavailable. Returning empty list for product IDs: {}", productIds);
        return Collections.emptyList();
    }
    
    @Override
    public List<ProductDto> getProductsByCategory(String category) {
        log.warn("Product service is unavailable. Returning empty list for category: {}", category);
        return Collections.emptyList();
    }
    
    @Override
    public List<ProductDto> getProductsByBrand(String brand) {
        log.warn("Product service is unavailable. Returning empty list for brand: {}", brand);
        return Collections.emptyList();
    }
    
    @Override
    public List<ProductDto> searchProducts(String query, String category, String brand, 
                                         Double minPrice, Double maxPrice, Boolean inStock, Integer limit) {
        log.warn("Product service is unavailable. Returning empty search results for query: {}", query);
        return Collections.emptyList();
    }
    
    @Override
    public List<ProductDto> getTrendingProducts(Integer limit) {
        log.warn("Product service is unavailable. Returning empty trending products list");
        return Collections.emptyList();
    }
    
    @Override
    public List<ProductDto> getPopularProducts(Integer limit) {
        log.warn("Product service is unavailable. Returning empty popular products list");
        return Collections.emptyList();
    }
    
    @Override
    public List<ProductDto> getSimilarProducts(Long productId, Integer limit) {
        log.warn("Product service is unavailable. Returning empty similar products for ID: {}", productId);
        return Collections.emptyList();
    }
    
    @Override
    public List<String> getAllCategories() {
        log.warn("Product service is unavailable. Returning empty categories list");
        return Collections.emptyList();
    }
    
    @Override
    public List<String> getAllBrands() {
        log.warn("Product service is unavailable. Returning empty brands list");
        return Collections.emptyList();
    }
    
    private ProductDto createFallbackProduct(Long productId) {
        ProductDto fallbackProduct = new ProductDto();
        fallbackProduct.setId(productId);
        fallbackProduct.setName("Product Unavailable");
        fallbackProduct.setDescription("Product information is currently unavailable");
        fallbackProduct.setCategory("Unknown");
        fallbackProduct.setBrand("Unknown");
        fallbackProduct.setPrice(0.0);
        fallbackProduct.setImageUrl("");
        fallbackProduct.setInStock(false);
        fallbackProduct.setStockQuantity(0);
        fallbackProduct.setRating(0.0);
        fallbackProduct.setReviewCount(0);
        fallbackProduct.setDiscount(0.0);
        fallbackProduct.setTags(Collections.emptyList());
        fallbackProduct.setAttributes(Collections.emptyMap());
        return fallbackProduct;
    }
}
