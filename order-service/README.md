# Order Service

## Overview

The Order Service is a core microservice in the e-commerce platform responsible for managing the complete order lifecycle. It handles order creation, status updates, payment coordination, and order fulfillment processes. The service integrates with other microservices through Kafka events and provides comprehensive order management capabilities.

## Features

### Core Functionality
- **Order Management**: Create, update, retrieve, and cancel orders
- **Order Status Tracking**: Complete lifecycle management from creation to delivery
- **Payment Integration**: Coordinate with payment service for transaction processing
- **Address Management**: Handle shipping and billing addresses
- **Order Items**: Manage product items within orders with quantities and pricing
- **Order History**: Maintain complete audit trail of order changes

### Advanced Features
- **Event-Driven Architecture**: Kafka integration for real-time event processing
- **Transaction Management**: Ensure data consistency across operations
- **Order Search & Filtering**: Advanced querying capabilities
- **Delivery Tracking**: Estimated and actual delivery date management
- **Order Analytics**: Summary and reporting capabilities

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Messaging**: Apache Kafka
- **Service Discovery**: Eureka Client
- **Configuration**: Spring Cloud Config
- **Build Tool**: Maven
- **Java Version**: 17+

## Database Schema

### Orders Table
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(255) UNIQUE NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50),
    total_amount DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2),
    shipping_amount DECIMAL(10,2),
    discount_amount DECIMAL(10,2),
    currency VARCHAR(3),
    payment_method VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_delivery_date TIMESTAMP,
    actual_delivery_date TIMESTAMP
);
```

### Order Items Table
```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(255),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL
);
```

## Key Components

### OrderService
- **Location**: `com.ecommerce.order.service.OrderService`
- **Purpose**: Core business logic for order management
- **Key Methods**:
  - `createOrder()`: Create new orders with validation
  - `updateOrderStatus()`: Update order status with event publishing
  - `cancelOrder()`: Cancel orders with proper cleanup
  - `getOrdersByUserId()`: Retrieve user's order history

### OrderController
- **Location**: `com.ecommerce.order.controller.OrderController`
- **Purpose**: REST API endpoints for order operations
- **Base Path**: `/api/orders`

### Order Entity
- **Location**: `com.ecommerce.order.entity.Order`
- **Purpose**: JPA entity representing order data
- **Features**: Embedded addresses, order items relationship, audit fields

## API Endpoints

### Order Management
```http
POST   /api/orders                    # Create new order
GET    /api/orders/{id}               # Get order by ID
PUT    /api/orders/{id}               # Update order
DELETE /api/orders/{id}               # Cancel order
GET    /api/orders/user/{userId}      # Get user's orders
GET    /api/orders                    # Get all orders (admin)
```

### Order Status
```http
PUT    /api/orders/{id}/status        # Update order status
GET    /api/orders/{id}/history       # Get order status history
```

### Order Analytics
```http
GET    /api/orders/summary            # Get order summary
GET    /api/orders/analytics          # Get order analytics
```

## Configuration

### Application Properties
```yaml
server:
  port: 8083

spring:
  application:
    name: order-service
  datasource:
    url: jdbc:postgresql://localhost:5432/order_db
    username: ${DB_USERNAME:ecommerce}
    password: ${DB_PASSWORD:ecommerce123}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
```

### Environment Variables
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka server addresses
- `EUREKA_SERVER_URL`: Eureka server URL
- `CONFIG_SERVER_URI`: Config server URL

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+
- Eureka Server (for service discovery)
- Config Server (for centralized configuration)

## Setup Instructions

### 1. Database Setup
```sql
-- Create database
CREATE DATABASE order_db;

-- Create user (optional)
CREATE USER ecommerce WITH PASSWORD 'ecommerce123';
GRANT ALL PRIVILEGES ON DATABASE order_db TO ecommerce;
```

### 2. Environment Configuration
```bash
# Set environment variables
export DB_USERNAME=ecommerce
export DB_PASSWORD=ecommerce123
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export EUREKA_SERVER_URL=http://localhost:8761/eureka/
```

### 3. Build and Run
```bash
# Navigate to order-service directory
cd order-service

# Build the application
mvn clean compile

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

### 4. Verify Installation
```bash
# Check health endpoint
curl http://localhost:8083/actuator/health

# Check service registration
curl http://localhost:8761/eureka/apps/ORDER-SERVICE
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=OrderServiceTest
```

### Integration Tests
```bash
# Run integration tests
mvn test -Dtest=OrderServiceApplicationTests
```

### API Testing
```bash
# Create order
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "items": [
      {
        "productId": "prod1",
        "quantity": 2,
        "unitPrice": 29.99
      }
    ],
    "shippingAddress": {
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    }
  }'
```

## Kafka Events

### Published Events
- **order-created**: When new order is created
- **order-updated**: When order status changes
- **order-cancelled**: When order is cancelled
- **payment-status-updated**: When payment status changes

### Consumed Events
- **payment-completed**: From payment service
- **inventory-reserved**: From product service
- **shipping-updated**: From shipping service

### Event Schema
```json
{
  "eventType": "ORDER_CREATED",
  "orderId": "12345",
  "orderNumber": "ORD-2024-001",
  "userId": "user123",
  "status": "PENDING",
  "totalAmount": 59.98,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Monitoring

### Health Checks
- **Endpoint**: `/actuator/health`
- **Database**: Connection and query health
- **Kafka**: Producer and consumer health
- **Eureka**: Service registration status

### Metrics
- **Endpoint**: `/actuator/metrics`
- **Custom Metrics**: Order creation rate, processing time
- **JVM Metrics**: Memory, GC, threads
- **Database Metrics**: Connection pool, query performance

### Logging
- **Framework**: SLF4J with Logback
- **Levels**: INFO for business events, DEBUG for technical details
- **Format**: JSON structured logging for production

## Performance Considerations

### Database Optimization
- **Indexing**: Order number, user ID, status, created date
- **Connection Pooling**: HikariCP with optimized settings
- **Query Optimization**: Use pagination for large result sets

### Caching Strategy
- **Order Status**: Cache frequently accessed order statuses
- **User Orders**: Cache recent orders for active users
- **Order Summary**: Cache aggregated data

### Async Processing
- **Event Publishing**: Asynchronous Kafka event publishing
- **Email Notifications**: Async order confirmation emails
- **Status Updates**: Background processing for status changes

## Security

### Authentication
- **JWT Integration**: Validate JWT tokens from API Gateway
- **User Context**: Extract user information from security context

### Authorization
- **Role-Based**: Admin vs Customer access levels
- **Resource-Based**: Users can only access their own orders

### Data Protection
- **Sensitive Data**: Encrypt payment information
- **Audit Trail**: Log all order modifications
- **Input Validation**: Validate all incoming requests

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check database status
   pg_isready -h localhost -p 5432
   
   # Verify credentials
   psql -h localhost -U ecommerce -d order_db
   ```

2. **Kafka Connection Issues**
   ```bash
   # Check Kafka status
   kafka-topics.sh --bootstrap-server localhost:9092 --list
   
   # Test producer
   kafka-console-producer.sh --bootstrap-server localhost:9092 --topic order-events
   ```

3. **Service Discovery Issues**
   ```bash
   # Check Eureka registration
   curl http://localhost:8761/eureka/apps
   
   # Verify service health
   curl http://localhost:8083/actuator/health
   ```

### Performance Issues
- **Slow Queries**: Enable SQL logging and analyze query plans
- **Memory Issues**: Monitor JVM metrics and adjust heap size
- **High Latency**: Check database connection pool and Kafka producer settings

## Contributing

1. **Code Style**: Follow Spring Boot best practices
2. **Testing**: Maintain test coverage above 80%
3. **Documentation**: Update API documentation for changes
4. **Commits**: Use conventional commit messages

## License

This project is licensed under the MIT License - see the LICENSE file for details.