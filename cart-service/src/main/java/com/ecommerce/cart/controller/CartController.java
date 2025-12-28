package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.*;
import com.ecommerce.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin(origins = "*")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponseDto> getCart(@PathVariable String userId) {
        CartResponseDto cart = cartService.getOrCreateCart(Long.parseLong(userId));
        return ResponseEntity.ok(cart);
    }
    
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponseDto> addToCart(
            @PathVariable String userId,
            @Valid @RequestBody AddToCartDto addToCartDto) {
        CartResponseDto cart = cartService.addItemToCart(Long.parseLong(userId), addToCartDto);
        return ResponseEntity.ok(cart);
    }
    
    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponseDto> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        CartResponseDto cart = cartService.updateItemQuantity(Long.parseLong(userId), Long.parseLong(productId), quantity);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponseDto> removeFromCart(
            @PathVariable String userId,
            @PathVariable String productId) {
        CartResponseDto cart = cartService.removeItemFromCart(Long.parseLong(userId), Long.parseLong(productId));
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteCart(@PathVariable String userId) {
        cartService.deleteCart(Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{userId}/summary")
    public ResponseEntity<CartSummaryDto> getCartSummary(@PathVariable String userId) {
        CartSummaryDto summary = cartService.getCartSummary(Long.parseLong(userId));
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> cartExists(@PathVariable String userId) {
        boolean exists = cartService.cartExists(Long.parseLong(userId));
        return ResponseEntity.ok(exists);
    }
    
    @PostMapping("/{userId}/merge")
    public ResponseEntity<CartResponseDto> mergeGuestCart(
            @PathVariable String userId,
            @Valid @RequestBody CartMergeDto cartMergeDto) {
        CartResponseDto cart = cartService.mergeGuestCart(Long.parseLong(userId), cartMergeDto);
        return ResponseEntity.ok(cart);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Cart Service is running");
    }
}