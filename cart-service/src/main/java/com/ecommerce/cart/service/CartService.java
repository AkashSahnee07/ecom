package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.*;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.exception.CartNotFoundException;
import com.ecommerce.cart.exception.CartItemNotFoundException;
import com.ecommerce.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String CART_UPDATED_TOPIC = "cart.updated";
    private static final String CART_CLEARED_TOPIC = "cart.cleared";
    private static final String ITEM_ADDED_TOPIC = "cart.item.added";
    private static final String ITEM_REMOVED_TOPIC = "cart.item.removed";
    
    /**
     * Get cart by user ID
     */
    public CartResponseDto getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        return convertToResponseDto(cart);
    }
    
    /**
     * Get or create cart for user
     */
    public CartResponseDto getOrCreateCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(userId);
                    return cartRepository.save(newCart);
                });
        return convertToResponseDto(cart);
    }
    
    /**
     * Add item to cart
     */
    public CartResponseDto addItemToCart(Long userId, AddToCartDto addToCartDto) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(new Cart(userId));
        
        CartItem cartItem = new CartItem(
                addToCartDto.getProductId(),
                addToCartDto.getProductName(),
                addToCartDto.getProductSku(),
                addToCartDto.getPrice(),
                addToCartDto.getQuantity(),
                addToCartDto.getImageUrl(),
                addToCartDto.getBrand(),
                addToCartDto.getCategoryId(),
                addToCartDto.getCategoryName()
        );
        
        cart.addItem(cartItem);
        cart = cartRepository.save(cart);
        
        // Publish event
        publishItemAddedEvent(userId, addToCartDto);
        
        return convertToResponseDto(cart);
    }
    
    /**
     * Update item quantity in cart
     */
    public CartResponseDto updateItemQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        
        // Check if item exists
        boolean itemExists = cart.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(productId));
        
        if (!itemExists) {
            throw new CartItemNotFoundException("Item not found in cart: " + productId);
        }
        
        cart.updateItemQuantity(productId, quantity);
        cart = cartRepository.save(cart);
        
        // Publish event
        publishCartUpdatedEvent(userId, cart);
        
        return convertToResponseDto(cart);
    }
    
    /**
     * Remove item from cart
     */
    public CartResponseDto removeItemFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        
        // Check if item exists
        boolean itemExists = cart.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(productId));
        
        if (!itemExists) {
            throw new CartItemNotFoundException("Item not found in cart: " + productId);
        }
        
        cart.removeItem(productId);
        cart = cartRepository.save(cart);
        
        // Publish event
        publishItemRemovedEvent(userId, productId);
        
        return convertToResponseDto(cart);
    }
    
    /**
     * Clear cart
     */
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        
        cart.clearCart();
        cartRepository.save(cart);
        
        // Publish event
        publishCartClearedEvent(userId);
    }
    
    /**
     * Delete cart
     */
    public void deleteCart(Long userId) {
        if (!cartRepository.existsByUserId(userId)) {
            throw new CartNotFoundException("Cart not found for user: " + userId);
        }
        
        cartRepository.deleteByUserId(userId);
        
        // Publish event
        publishCartClearedEvent(userId);
    }
    
    /**
     * Get cart summary
     */
    public CartSummaryDto getCartSummary(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        
        return new CartSummaryDto(
                cart.getTotalItems(),
                cart.getTotalAmount(),
                cart.getItems().size(),
                cart.getUpdatedAt()
        );
    }
    
    /**
     * Check if cart exists for user
     */
    public boolean cartExists(Long userId) {
        return cartRepository.existsByUserId(userId);
    }
    
    /**
     * Merge guest cart with user cart
     */
    public CartResponseDto mergeGuestCart(Long userId, CartMergeDto guestCartDto) {
        Cart userCart = cartRepository.findByUserId(userId)
                .orElse(new Cart(userId));
        
        // Add guest cart items to user cart
        for (CartItemDto guestItem : guestCartDto.getItems()) {
            CartItem cartItem = new CartItem(
                    guestItem.getProductId(),
                    guestItem.getProductName(),
                    guestItem.getProductSku(),
                    guestItem.getPrice(),
                    guestItem.getQuantity(),
                    guestItem.getImageUrl(),
                    guestItem.getBrand(),
                    guestItem.getCategoryId(),
                    guestItem.getCategoryName()
            );
            userCart.addItem(cartItem);
        }
        
        userCart = cartRepository.save(userCart);
        
        // Publish event
        publishCartUpdatedEvent(userId, userCart);
        
        return convertToResponseDto(userCart);
    }
    
    // Helper methods
    private CartResponseDto convertToResponseDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());
        
        return new CartResponseDto(
                cart.getId(),
                cart.getUserId(),
                itemDtos,
                cart.getTotalAmount(),
                cart.getTotalItems(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
    
    private CartItemDto convertToItemDto(CartItem item) {
        return new CartItemDto(
                item.getProductId(),
                item.getProductName(),
                item.getProductSku(),
                item.getPrice(),
                item.getQuantity(),
                item.getSubtotal(),
                item.getImageUrl(),
                item.getBrand(),
                item.getCategoryId(),
                item.getCategoryName(),
                item.getAddedAt()
        );
    }
    
    // Event publishing methods
    private void publishItemAddedEvent(Long userId, AddToCartDto item) {
        try {
            kafkaTemplate.send(ITEM_ADDED_TOPIC, userId.toString(), item);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to publish item added event: " + e.getMessage());
        }
    }
    
    private void publishItemRemovedEvent(Long userId, Long productId) {
        try {
            kafkaTemplate.send(ITEM_REMOVED_TOPIC, userId.toString(), 
                    new CartItemRemovedEvent(userId, productId, LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("Failed to publish item removed event: " + e.getMessage());
        }
    }
    
    private void publishCartUpdatedEvent(Long userId, Cart cart) {
        try {
            kafkaTemplate.send(CART_UPDATED_TOPIC, userId.toString(), convertToResponseDto(cart));
        } catch (Exception e) {
            System.err.println("Failed to publish cart updated event: " + e.getMessage());
        }
    }
    
    private void publishCartClearedEvent(Long userId) {
        try {
            kafkaTemplate.send(CART_CLEARED_TOPIC, userId.toString(), 
                    new CartClearedEvent(userId, LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("Failed to publish cart cleared event: " + e.getMessage());
        }
    }
    
    // Event classes
    public static class CartItemRemovedEvent {
        private Long userId;
        private Long productId;
        private LocalDateTime timestamp;
        
        public CartItemRemovedEvent(Long userId, Long productId, LocalDateTime timestamp) {
            this.userId = userId;
            this.productId = productId;
            this.timestamp = timestamp;
        }
        
        // Getters
        public Long getUserId() { return userId; }
        public Long getProductId() { return productId; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class CartClearedEvent {
        private Long userId;
        private LocalDateTime timestamp;
        
        public CartClearedEvent(Long userId, LocalDateTime timestamp) {
            this.userId = userId;
            this.timestamp = timestamp;
        }
        
        // Getters
        public Long getUserId() { return userId; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}