# Review Service

## Overview

The Review Service is a comprehensive microservice that manages all product review and rating operations in the e-commerce platform. It provides advanced features including review moderation, sentiment analysis, helpfulness voting, media attachments, and comprehensive analytics to ensure high-quality customer feedback management.

## Features

### Core Features
- **Review Management**
  - Product reviews and ratings (1-5 stars)
  - Review creation, editing, and deletion
  - Review status management (pending, approved, rejected)
  - Verified purchase validation
- **Rating System**
  - Individual product ratings
  - Rating aggregation and statistics
  - Rating distribution analytics
  - Average rating calculations

### Advanced Features
- **Review Moderation**
  - Automated content moderation
  - Manual review approval workflows
  - Spam and inappropriate content detection
  - Moderation queue management
- **Sentiment Analysis**
  - AI-powered sentiment detection
  - Review sentiment scoring
  - Emotional tone analysis
  - Sentiment trend tracking
- **Helpfulness Voting**
  - Helpful/unhelpful voting system
  - Vote aggregation and ranking
  - Most helpful reviews identification
- **Media Support**
  - Image attachments for reviews
  - Media upload and management
  - Image moderation and validation
- **Search & Filtering**
  - Full-text review search
  - Advanced filtering options
  - Sorting by relevance, date, rating
  - Pagination support

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: PostgreSQL 14+
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Monitoring**: Spring Boot Actuator, Micrometer
- **Testing**: JUnit 5, H2 (in-memory for tests)
- **File Storage**: Local/Cloud storage for media

## Database Schema

### Reviews Table
```sql
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(100),
    content TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_purchase BOOLEAN DEFAULT FALSE,
    order_id BIGINT,
    helpful_count INTEGER DEFAULT 0,
    not_helpful_count INTEGER DEFAULT 0,
    sentiment_score DECIMAL(3,2),
    sentiment_label VARCHAR(20),
    image_urls TEXT[],
    moderation_reason TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Indexes for performance
CREATE INDEX idx_review_product_id ON reviews(product_id);
CREATE INDEX idx_review_user_id ON reviews(user_id);
CREATE INDEX idx_review_status ON reviews(status);
CREATE INDEX idx_review_rating ON reviews(rating);
CREATE INDEX idx_review_created_date ON reviews(created_date);
CREATE INDEX idx_review_verified_purchase ON reviews(verified_purchase);
CREATE UNIQUE INDEX idx_review_product_user ON reviews(product_id, user_id);
```

### Review Votes Table
```sql
CREATE TABLE review_votes (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES reviews(id),
    user_id BIGINT NOT NULL,
    vote_type VARCHAR(20) NOT NULL, -- 'HELPFUL' or 'NOT_HELPFUL'
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(review_id, user_id)
);
```

## Key Components

### Application Layer
- **ReviewServiceApplication**: Main Spring Boot application class
- **ReviewController**: REST API endpoints for review operations
- **ReviewService**: Core business logic for review management

### Service Layer
- **ModerationService**: Handles review moderation workflows
- **SentimentAnalysisService**: AI-powered sentiment analysis
- **ImageService**: Media upload and management
- **NotificationService**: Event publishing for notifications
- **AnalyticsService**: Review analytics and reporting

### Data Layer
- **ReviewRepository**: Review data access operations
- **ReviewVoteRepository**: Vote data access operations
- **DatabaseConfig**: Database configuration and connection management

### Integration Layer
- **KafkaConfig**: Message broker configuration
- **EventPublisher**: Kafka event publishing
- **EventConsumer**: Kafka event consumption

## API Endpoints

### Review Management
```http
# Create a new review
POST /api/reviews
Content-Type: application/json
{
  "productId": 123,
  "userId": 456,
  "userName": "John Doe",
  "rating": 5,
  "title": "Great product!",
  "content": "I love this product...",
  "orderId": 789
}

# Get reviews for a product
GET /api/reviews/product/{productId}
GET /api/reviews/product/{productId}?status=APPROVED&minRating=4&verifiedOnly=true

# Get a specific review
GET /api/reviews/{reviewId}

# Update a review
PUT /api/reviews/{reviewId}

# Delete a review
DELETE /api/reviews/{reviewId}
```

### Review Search & Filtering
```http
# Search reviews
GET /api/reviews/search?query=excellent&productId=123

# Get user's reviews
GET /api/reviews/user/{userId}

# Get reviews by rating
GET /api/reviews/product/{productId}/rating/{rating}
```

### Review Voting
```http
# Vote on review helpfulness
POST /api/reviews/{reviewId}/vote
Content-Type: application/json
{
  "userId": 456,
  "voteType": "HELPFUL"
}

# Get review votes
GET /api/reviews/{reviewId}/votes
```

### Review Moderation
```http
# Get pending reviews for moderation
GET /api/reviews/moderation/pending

# Approve a review
PUT /api/reviews/{reviewId}/approve

# Reject a review
PUT /api/reviews/{reviewId}/reject
Content-Type: application/json
{
  "reason": "Inappropriate content"
}
```

### Analytics
```http
# Get product rating statistics
GET /api/reviews/product/{productId}/stats

# Get review analytics
GET /api/reviews/analytics/summary
GET /api/reviews/analytics/sentiment-trends
```

### Health and Monitoring
```http
GET /actuator/health
GET /actuator/metrics
GET /actuator/info
GET /api-docs
GET /swagger-ui.html
```

## Configuration

### Application Properties
```yaml
# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/review_db
    username: review_user
    password: review_password
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000

# JPA Configuration
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 25

# Kafka Configuration
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: review-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
```

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=review_db
DB_USERNAME=review_user
DB_PASSWORD=review_password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
EUREKA_CLIENT_SERVICE_URL=http://localhost:8761/eureka

# File Storage
FILE_UPLOAD_DIR=/uploads/reviews
MAX_FILE_SIZE=10MB
MAX_REQUEST_SIZE=50MB

# Moderation
AUTO_MODERATION_ENABLED=true
SENTIMENT_ANALYSIS_ENABLED=true
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- Apache Kafka 2.8+
- Eureka Server (for service discovery)
- Config Server (for centralized configuration)

## Setup Instructions

### 1. Clone and Build
```bash
# Navigate to review service directory
cd review-service

# Build the application
mvn clean compile
```

### 2. Database Setup
```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE review_db;
CREATE USER review_user WITH PASSWORD 'review_password';
GRANT ALL PRIVILEGES ON DATABASE review_db TO review_user;

# Run database migrations (handled by Hibernate)
# Tables will be created automatically on first run
```

### 3. Start Dependencies
```bash
# Start Kafka
bin/kafka-server-start.sh config/server.properties

# Start Eureka Server
cd ../eureka-server && mvn spring-boot:run

# Start Config Server
cd ../config-server && mvn spring-boot:run
```

### 4. Run the Service
```bash
# Development mode
mvn spring-boot:run

# Production mode
mvn clean package
java -jar target/review-service-1.0.0.jar

# With specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 5. Verify Installation
```bash
# Check service health
curl http://localhost:8085/actuator/health

# View API documentation
open http://localhost:8085/swagger-ui.html

# Test review creation
curl -X POST http://localhost:8085/api/reviews \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"userId":1,"userName":"Test User","rating":5,"title":"Great!","content":"Excellent product"}'
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=ReviewServiceTest

# Run tests with coverage
mvn test jacoco:report
```

### Integration Tests
```bash
# Run integration tests
mvn verify -P integration-tests

# Test with H2 in-memory database
mvn test -Dspring.profiles.active=test
```

### API Testing
```bash
# Test review creation
curl -X POST http://localhost:8085/api/reviews \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"userId":1,"userName":"John","rating":4,"title":"Good","content":"Nice product"}'

# Test review retrieval
curl http://localhost:8085/api/reviews/product/1

# Test review search
curl "http://localhost:8085/api/reviews/search?query=excellent&productId=1"

# Test review voting
curl -X POST http://localhost:8085/api/reviews/1/vote \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"voteType":"HELPFUL"}'
```

## Kafka Events

### Published Events
```yaml
# Review Created
topic: review.created
payload:
  reviewId: string
  productId: string
  userId: string
  rating: number
  status: string
  timestamp: string

# Review Approved
topic: review.approved
payload:
  reviewId: string
  productId: string
  userId: string
  rating: number
  timestamp: string

# Review Rejected
topic: review.rejected
payload:
  reviewId: string
  productId: string
  userId: string
  reason: string
  timestamp: string

# Review Voted
topic: review.voted
payload:
  reviewId: string
  userId: string
  voteType: string
  timestamp: string
```

### Consumed Events
```yaml
# Order Completed
topic: order.completed
handler: enableVerifiedPurchaseReviews()

# Product Updated
topic: product.updated
handler: updateProductReviewMetadata()

# User Updated
topic: user.updated
handler: updateReviewUserInfo()
```

## Review Moderation

### Automated Moderation
- **Content Filtering**: Automatic detection of inappropriate content
- **Spam Detection**: Machine learning-based spam identification
- **Sentiment Analysis**: AI-powered sentiment scoring
- **Language Detection**: Multi-language support and filtering

### Manual Moderation
- **Moderation Queue**: Pending reviews requiring manual review
- **Approval Workflow**: Multi-step approval process
- **Rejection Reasons**: Detailed rejection reason tracking
- **Moderator Dashboard**: Tools for efficient review management

### Moderation Rules
```yaml
auto-moderation:
  enabled: true
  rules:
    - type: profanity
      action: reject
    - type: spam
      action: flag
    - type: short-content
      min-length: 10
      action: flag
    - type: sentiment
      min-score: -0.8
      action: flag
```

## Monitoring & Observability

### Health Checks
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  health:
    db:
      enabled: true
    kafka:
      enabled: true
```

### Custom Metrics
- `review.creation.count`: Number of reviews created
- `review.moderation.queue.size`: Pending moderation queue size
- `review.approval.rate`: Review approval rate
- `review.sentiment.average`: Average sentiment score
- `review.helpfulness.ratio`: Helpful votes ratio

### Logging
```yaml
logging:
  level:
    com.ecommerce.review: INFO
    org.springframework.kafka: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/review-service.log
```

## Performance Optimization

### Database Optimization
- **Indexes**: Optimized indexes for frequent queries
- **Connection Pooling**: HikariCP for efficient connections
- **Batch Processing**: Batch inserts and updates
- **Query Optimization**: Efficient JPA queries

### Caching Strategy
- **Application Cache**: Frequently accessed review data
- **Database Cache**: Query result caching
- **CDN**: Static media content delivery

### Async Processing
- **Event Publishing**: Asynchronous Kafka event publishing
- **Image Processing**: Background image processing
- **Sentiment Analysis**: Async sentiment calculation

## Error Handling

### Custom Exceptions
```java
// Review-specific exceptions
ReviewNotFoundException
DuplicateReviewException
InvalidRatingException
ModerationException
ImageUploadException
```

### Global Exception Handler
```java
@RestControllerAdvice
public class ReviewExceptionHandler {
    // Handles all review-related exceptions
    // Returns appropriate HTTP status codes and error messages
}
```

### Error Response Format
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid rating value. Must be between 1 and 5",
  "path": "/api/reviews",
  "errorCode": "INVALID_RATING"
}
```

## Security Considerations

### Input Validation
- **Content Sanitization**: XSS prevention in review content
- **File Upload Security**: Secure image upload validation
- **SQL Injection Prevention**: Parameterized queries
- **Rate Limiting**: API rate limiting per user

### Data Privacy
- **User Data Protection**: Secure handling of user information
- **Review Anonymization**: Option to anonymize reviews
- **GDPR Compliance**: Right to be forgotten implementation
- **Data Encryption**: Sensitive data encryption at rest

## Troubleshooting

### Common Issues

**Database Connection Issues**
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Test database connection
psql -h localhost -U review_user -d review_db

# Check connection pool
curl http://localhost:8085/actuator/metrics/hikaricp.connections.active
```

**Kafka Issues**
```bash
# Check Kafka topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Monitor consumer lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group review-service-group
```

**Performance Issues**
```bash
# Monitor JVM metrics
curl http://localhost:8085/actuator/metrics/jvm.memory.used

# Check database performance
curl http://localhost:8085/actuator/metrics/jdbc.connections.active

# Monitor review processing
curl http://localhost:8085/actuator/metrics/review.processing.time
```

**Image Upload Issues**
```bash
# Check upload directory permissions
ls -la /uploads/reviews/

# Verify file size limits
curl http://localhost:8085/actuator/configprops | grep multipart
```

## Deployment

### Docker
```dockerfile
# Build image
docker build -t review-service:latest .

# Run container
docker run -d \
  --name review-service \
  -p 8085:8085 \
  -e DB_HOST=postgres \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  review-service:latest
```

### Docker Compose
```yaml
version: '3.8'
services:
  review-service:
    build: .
    ports:
      - "8085:8085"
    environment:
      - DB_HOST=postgres
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - postgres
      - kafka
  
  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=review_db
      - POSTGRES_USER=review_user
      - POSTGRES_PASSWORD=review_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: review-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: review-service
  template:
    spec:
      containers:
      - name: review-service
        image: review-service:latest
        ports:
        - containerPort: 8085
        env:
        - name: DB_HOST
          value: "postgres-service"
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java coding standards
- Write comprehensive unit tests
- Update API documentation
- Test moderation workflows
- Ensure database migrations are backward compatible

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Service Port**: 8085  
**Health Check**: http://localhost:8085/actuator/health  
**API Documentation**: http://localhost:8085/swagger-ui.html  
**Metrics**: http://localhost:8085/actuator/metrics