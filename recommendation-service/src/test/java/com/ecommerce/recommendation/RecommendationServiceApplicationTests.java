package com.ecommerce.recommendation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for Recommendation Service Application
 */
@SpringBootTest
@ActiveProfiles("test")
class RecommendationServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // It will fail if there are any configuration issues or missing dependencies
    }

}