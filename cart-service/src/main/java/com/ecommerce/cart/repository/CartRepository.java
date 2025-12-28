package com.ecommerce.cart.repository;

import com.ecommerce.cart.entity.Cart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends CrudRepository<Cart, String> {
    
    /**
     * Find cart by user ID
     * @param userId the user ID
     * @return Optional cart
     */
    Optional<Cart> findByUserId(Long userId);
    
    /**
     * Check if cart exists for user
     * @param userId the user ID
     * @return true if exists
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Delete cart by user ID
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);
}