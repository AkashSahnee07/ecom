package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.AddToCartDto;
import com.ecommerce.cart.dto.CartMergeDto;
import com.ecommerce.cart.dto.CartResponseDto;
import com.ecommerce.cart.dto.CartSummaryDto;
import com.ecommerce.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart Management", description = "APIs for managing shopping carts")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get cart by User ID", description = "Retrieves the shopping cart for a specific user")
    public ResponseEntity<CartResponseDto> getCart(@PathVariable("userId") String userId) {
        CartResponseDto cart = cartService.getOrCreateCart(Long.parseLong(userId));
        return ResponseEntity.ok(cart);
    }
    
    @PostMapping("/{userId}/items")
    @Operation(summary = "Add item to cart", description = "Adds a product to the user's cart")
    public ResponseEntity<CartResponseDto> addToCart(
            @PathVariable("userId") String userId,
            @Valid @RequestBody AddToCartDto addToCartDto) {
        CartResponseDto cart = cartService.addItemToCart(Long.parseLong(userId), addToCartDto);
        return ResponseEntity.ok(cart);
    }
    
    @PutMapping("/{userId}/items/{productId}")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of a specific product in the cart")
    public ResponseEntity<CartResponseDto> updateItemQuantity(
            @PathVariable("userId") String userId,
            @PathVariable("productId") String productId,
            @RequestParam(name = "quantity") Integer quantity) {
        CartResponseDto cart = cartService.updateItemQuantity(Long.parseLong(userId), Long.parseLong(productId), quantity);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/{userId}/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Removes a product from the user's cart")
    public ResponseEntity<CartResponseDto> removeFromCart(
            @PathVariable("userId") String userId,
            @PathVariable("productId") String productId) {
        CartResponseDto cart = cartService.removeItemFromCart(Long.parseLong(userId), Long.parseLong(productId));
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/{userId}/clear")
    @Operation(summary = "Clear cart", description = "Removes all items from the user's cart")
    public ResponseEntity<Void> clearCart(@PathVariable("userId") String userId) {
        cartService.clearCart(Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete cart", description = "Completely deletes the user's cart")
    public ResponseEntity<Void> deleteCart(@PathVariable("userId") String userId) {
        cartService.deleteCart(Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{userId}/summary")
    @Operation(summary = "Get cart summary", description = "Retrieves a summary of the cart including total price and items")
    public ResponseEntity<CartSummaryDto> getCartSummary(@PathVariable("userId") String userId) {
        CartSummaryDto summary = cartService.getCartSummary(Long.parseLong(userId));
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/{userId}/exists")
    @Operation(summary = "Check if cart exists", description = "Checks if a cart exists for the given user")
    public ResponseEntity<Boolean> cartExists(@PathVariable("userId") String userId) {
        boolean exists = cartService.cartExists(Long.parseLong(userId));
        return ResponseEntity.ok(exists);
    }
    
    @PostMapping("/{userId}/merge")
    @Operation(summary = "Merge guest cart", description = "Merges a guest cart into the user's main cart")
    public ResponseEntity<CartResponseDto> mergeGuestCart(
            @PathVariable("userId") String userId,
            @Valid @RequestBody CartMergeDto cartMergeDto) {
        CartResponseDto cart = cartService.mergeGuestCart(Long.parseLong(userId), cartMergeDto);
        return ResponseEntity.ok(cart);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks if the cart service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Cart Service is running");
    }
}