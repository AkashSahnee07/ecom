# Product Service

## Overview

The Product Service is a core microservice in the e-commerce platform responsible for product catalog management, category management, and intelligent product recommendations. It provides comprehensive product lifecycle management with advanced recommendation algorithms including collaborative filtering and content-based filtering.

## Features

- **Product Catalog Management**: Complete CRUD operations for products
- **Category Management**: Hierarchical category structure
- **Intelligent Recommendations**: ML-based product recommendations
- **Search & Filtering**: Advanced product search capabilities
- **Inventory Integration**: Real-time stock management
- **Event-Driven Architecture**: Kafka integration for product events
- **Service Discovery**: Eureka client integration
- **Distributed Tracing**: Sleuth and Zipkin integration
- **Health Monitoring**: Actuator endpoints for monitoring

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Tracing**: Spring Cloud Sleuth + Zipkin
- **Build Tool**: Maven
- **Java Version**: 17+

## Architecture

### Database Schema

```sql
-- Products table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    category_id BIGINT,
    brand VARCHAR(100),
    stock_quantity INT DEFAULT 0,
    weight DECIMAL(8,2),
    dimensions VARCHAR(100),
    image_urls JSON,
    tags JSON,
    is_active BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_products_sku (sku),
    INDEX idx_products_category (category_id),
    INDEX idx_products_brand (brand),
    INDEX idx_products_active (is_active)
);

-- Categories table
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_categories_parent (parent_id),
    INDEX idx_categories_active (is_active)
);

-- User preferences for recommendations
CREATE TABLE user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    preferred_categories JSON,
    preferred_brands JSON,
    price_range_min DECIMAL(10,2),
    price_range_max DECIMAL(10,2),
    preferred_tags JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_preferences (user_id)
);

-- Product recommendations
CREATE TABLE product_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    recommendation_type ENUM('COLLABORATIVE', 'CONTENT_BASED', 'HYBRID') NOT NULL,
    score DECIMAL(5,4) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recommendations_user (user_id),
    INDEX idx_recommendations_product (product_id),
    INDEX idx_recommendations_score (score DESC)
);
```

### Key Components

1. **ProductController**: REST API endpoints for product operations
2. **CategoryController**: Category management endpoints
3. **RecommendationController**: Product recommendation endpoints
4. **ProductService**: Business logic for product management
5. **CategoryService**: Category management logic
6. **RecommendationService**: Orchestrates recommendation algorithms
7. **CollaborativeFilteringService**: User-based collaborative filtering
8. **ContentBasedFilteringService**: Content similarity recommendations
9. **RealTimeRecommendationService**: Real-time recommendation updates

## API Endpoints

### Product Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/products` | Create new product | Admin/Seller |
| GET | `/products/{id}` | Get product by ID | No |
| PUT | `/products/{id}` | Update product | Admin/Seller |
| DELETE | `/products/{id}` | Delete product | Admin |
| GET | `/products` | Get all products (paginated) | No |
| GET | `/products/search` | Search products | No |
| GET | `/products/category/{categoryId}` | Get products by category | No |
| GET | `/products/brand/{brand}` | Get products by brand | No |
| PUT | `/products/{id}/stock` | Update stock quantity | Admin/Seller |
| PUT | `/products/{id}/status` | Activate/deactivate product | Admin |

### Category Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/categories` | Create new category | Admin |
| GET | `/categories/{id}` | Get category by ID | No |
| PUT | `/categories/{id}` | Update category | Admin |
| DELETE | `/categories/{id}` | Delete category | Admin |
| GET | `/categories` | Get all categories | No |
| GET | `/categories/tree` | Get category hierarchy | No |
| GET | `/categories/{id}/products` | Get products in category | No |

### Recommendations

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/recommendations/user/{userId}` | Get user recommendations | Yes |
| GET | `/recommendations/product/{productId}/similar` | Get similar products | No |
| POST | `/recommendations/preferences` | Update user preferences | Yes |
| GET | `/recommendations/trending` | Get trending products | No |
| GET | `/recommendations/featured` | Get featured products | No |

### Request/Response Examples

#### Create Product

**Request:**
```json
POST /products
{
    "name": "Wireless Bluetooth Headphones",
    "description": "High-quality wireless headphones with noise cancellation",
    "price": 199.99,
    "sku": "WBH-001",
    "categoryId": 1,
    "brand": "TechSound",
    "stockQuantity": 50,
    "weight": 0.25,
    "dimensions": "20x15x8 cm",
    "imageUrls": [
        "https://example.com/images/headphones1.jpg",
        "https://example.com/images/headphones2.jpg"
    ],
    "tags": ["wireless", "bluetooth", "noise-cancellation", "audio"]
}
```

**Response:**
```json
{
    "id": 1,
    "name": "Wireless Bluetooth Headphones",
    "description": "High-quality wireless headphones with noise cancellation",
    "price": 199.99,
    "sku": "WBH-001",
    "categoryId": 1,
    "brand": "TechSound",
    "stockQuantity": 50,
    "weight": 0.25,
    "dimensions": "20x15x8 cm",
    "imageUrls": [
        "https://example.com/images/headphones1.jpg",
        "https://example.com/images/headphones2.jpg"
    ],
    "tags": ["wireless", "bluetooth", "noise-cancellation", "audio"],
    "active": true,
    "featured": false,
    "averageRating": 0.00,
    "reviewCount": 0,
    "createdAt": "2024-01-15 10:30:00",
    "updatedAt": "2024-01-15 10:30:00"
}
```

#### Get Product Recommendations

**Request:**
```bash
GET /recommendations/user/123?limit=10&type=HYBRID
```

**Response:**
```json
{
    "userId": 123,
    "recommendations": [
        {
            "productId": 45,
            "product": {
                "id": 45,
                "name": "Smart Watch Pro",
                "price": 299.99,
                "imageUrls": ["https://example.com/watch.jpg"]
            },
            "score": 0.95,
            "reason": "Based on your interest in electronics and fitness",
            "type": "HYBRID"
        }
    ],
    "totalRecommendations": 10,
    "generatedAt": "2024-01-15 10:30:00"
}
```

## Configuration

### Application Properties

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8082

spring:
  application:
    name: product-service
  datasource:
    url: jdbc:mysql://localhost:3306/product_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: ${DB_USERNAME:product_user}
    password: ${DB_PASSWORD:product123}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all
      retries: 3
      properties:
        enable.idempotence: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

app:
  recommendation:
    collaborative-filtering:
      enabled: true
      min-similarity-threshold: 0.3
    content-based-filtering:
      enabled: true
      min-score-threshold: 0.4
    cache:
      ttl: 3600  # 1 hour
```

### Environment Variables

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `DB_USERNAME` | Database username | `product_user` |
| `DB_PASSWORD` | Database password | `product123` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` |
| `EUREKA_SERVER_URL` | Eureka server URL | `http://localhost:8761/eureka/` |
| `REDIS_HOST` | Redis host for caching | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Apache Kafka 2.8+
- Redis 6.0+ (for caching)
- Eureka Server (for service discovery)
- Config Server (for centralized configuration)

## Setup Instructions

### 1. Database Setup

```bash
# Create MySQL database
mysql -u root -p
CREATE DATABASE product_db;
CREATE USER 'product_user'@'localhost' IDENTIFIED BY 'product123';
GRANT ALL PRIVILEGES ON product_db.* TO 'product_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Environment Setup

```bash
# Set environment variables
export DB_USERNAME=product_user
export DB_PASSWORD=product123
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 3. Build and Run

```bash
# Navigate to product-service directory
cd product-service

# Build the application
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/product-service-1.0.0.jar
```

### 4. Docker Setup (Optional)

```bash
# Build Docker image
docker build -t product-service:1.0.0 .

# Run with Docker Compose
docker-compose up product-service
```

## Testing

### Unit Tests

```bash
# Run unit tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

### Integration Tests

```bash
# Run integration tests
mvn verify -P integration-tests
```

### API Testing with cURL

```bash
# Create a product
curl -X POST http://localhost:8082/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99,
    "sku": "TEST-001",
    "categoryId": 1,
    "brand": "TestBrand",
    "stockQuantity": 10
  }'

# Get product by ID
curl -X GET http://localhost:8082/products/1

# Search products
curl -X GET "http://localhost:8082/products/search?q=headphones&page=0&size=10"

# Get recommendations
curl -X GET http://localhost:8082/recommendations/user/123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Kafka Events

The service publishes the following events to Kafka:

### Product Events Topic: `product-events`

```json
{
  "eventType": "PRODUCT_CREATED",
  "productId": 1,
  "sku": "WBH-001",
  "name": "Wireless Bluetooth Headphones",
  "stockQuantity": 50,
  "timestamp": 1642248600000
}
```

**Event Types:**
- `PRODUCT_CREATED`: New product created
- `PRODUCT_UPDATED`: Product information updated
- `PRODUCT_DELETED`: Product deleted
- `STOCK_UPDATED`: Stock quantity changed
- `PRODUCT_ACTIVATED`: Product activated
- `PRODUCT_DEACTIVATED`: Product deactivated

### Category Events Topic: `category-events`

```json
{
  "eventType": "CATEGORY_CREATED",
  "categoryId": 1,
  "name": "Electronics",
  "parentId": null,
  "timestamp": 1642248600000
}
```

## Recommendation Algorithms

### 1. Collaborative Filtering

- **User-Based**: Finds users with similar preferences
- **Item-Based**: Finds products with similar purchase patterns
- **Matrix Factorization**: Uses SVD for dimensionality reduction

### 2. Content-Based Filtering

- **Feature Similarity**: Compares product attributes
- **Category Matching**: Recommends products from preferred categories
- **Brand Affinity**: Considers brand preferences
- **Price Range**: Matches user's price preferences

### 3. Hybrid Approach

- Combines collaborative and content-based methods
- Weighted scoring based on data availability
- Real-time updates based on user interactions

## Monitoring and Health Checks

### Health Endpoints

- **Health Check**: `GET /actuator/health`
- **Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`

### Custom Metrics

- Product creation rate
- Search query performance
- Recommendation accuracy
- Cache hit/miss ratios

### Logging

Logs are configured with different levels:
- `DEBUG`: Detailed application flow
- `INFO`: General application information
- `WARN`: Warning messages
- `ERROR`: Error conditions

Log files are stored in `logs/product-service.log`

## Performance Optimization

### Database Optimization

1. **Indexes**: Optimized for common queries
   ```sql
   CREATE INDEX idx_products_search ON products(name, brand, is_active);
   CREATE INDEX idx_products_price ON products(price);
   CREATE INDEX idx_products_rating ON products(average_rating DESC);
   ```

2. **Query Optimization**: Use pagination and filtering
3. **Connection Pooling**: HikariCP configuration

### Caching Strategy

1. **Redis Caching**: Product details and search results
2. **Application-Level Caching**: Category hierarchies
3. **Recommendation Caching**: Pre-computed recommendations

### Search Optimization

1. **Full-Text Search**: MySQL full-text indexes
2. **Elasticsearch Integration**: Advanced search capabilities
3. **Search Result Caching**: Frequently searched terms

## Security Considerations

1. **Input Validation**: All inputs validated using Bean Validation
2. **SQL Injection**: Protected by JPA/Hibernate
3. **Authorization**: Role-based access control
4. **Rate Limiting**: API rate limiting for search endpoints
5. **Data Sanitization**: Product descriptions and user inputs

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   ```bash
   # Check MySQL status
   sudo systemctl status mysql
   
   # Test database connection
   mysql -h localhost -u product_user -p product_db
   ```

2. **Kafka Connection Issues**
   ```bash
   # Check Kafka topics
   kafka-topics.sh --bootstrap-server localhost:9092 --list
   ```

3. **Redis Connection Issues**
   ```bash
   # Check Redis status
   redis-cli ping
   ```

4. **Recommendation Performance**
   - Check user preference data completeness
   - Verify recommendation algorithm parameters
   - Monitor cache hit rates

### Debug Mode

```bash
# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.ecommerce.product=DEBUG"
```

## Contributing

1. Follow the existing code style and conventions
2. Write unit tests for new features
3. Update documentation for API changes
4. Ensure all tests pass before submitting
5. Use meaningful commit messages
6. Test recommendation algorithms with sample data

## License

This project is part of the E-commerce Microservices Platform.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review application logs
3. Monitor recommendation performance metrics
4. Contact the development team

---

**Service Status**: ✅ Active  
**Last Updated**: January 2024  
**Version**: 1.0.0