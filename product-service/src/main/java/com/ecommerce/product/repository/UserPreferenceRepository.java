package com.ecommerce.product.repository;

import com.ecommerce.product.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    
    // Find user preference by user ID
    Optional<UserPreference> findByUserId(Long userId);
    
    // Find users with similar preferences (same categories)
    @Query("SELECT up FROM UserPreference up WHERE up.userId != :userId " +
           "AND EXISTS (SELECT 1 FROM up.preferredCategories pc WHERE pc IN :categories)")
    List<UserPreference> findUsersWithSimilarCategories(@Param("userId") Long userId, 
                                                        @Param("categories") List<Long> categories);
    
    // Find users with similar brand preferences
    @Query("SELECT up FROM UserPreference up WHERE up.userId != :userId " +
           "AND EXISTS (SELECT 1 FROM up.preferredBrands pb WHERE pb IN :brands)")
    List<UserPreference> findUsersWithSimilarBrands(@Param("userId") Long userId, 
                                                    @Param("brands") List<String> brands);
    
    // Find users with similar price range
    @Query("SELECT up FROM UserPreference up WHERE up.userId != :userId " +
           "AND ((up.minPrice <= :maxPrice AND up.maxPrice >= :minPrice) " +
           "OR (up.minPrice IS NULL OR up.maxPrice IS NULL))")
    List<UserPreference> findUsersWithSimilarPriceRange(@Param("userId") Long userId,
                                                        @Param("minPrice") Double minPrice,
                                                        @Param("maxPrice") Double maxPrice);
    
    // Find all users who prefer a specific category
    @Query("SELECT up FROM UserPreference up WHERE :categoryId MEMBER OF up.preferredCategories")
    List<UserPreference> findUsersByPreferredCategory(@Param("categoryId") Long categoryId);
    
    // Find all users who prefer a specific brand
    @Query("SELECT up FROM UserPreference up WHERE :brand MEMBER OF up.preferredBrands")
    List<UserPreference> findUsersByPreferredBrand(@Param("brand") String brand);
}