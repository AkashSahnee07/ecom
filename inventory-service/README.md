# Inventory Service

## Overview

The Inventory Service is a critical microservice in the e-commerce platform responsible for managing product inventory across multiple warehouses. It provides real-time stock tracking, reservation management, automatic reordering, and comprehensive inventory analytics. The service ensures accurate stock levels and prevents overselling through sophisticated locking mechanisms and event-driven architecture.

## Features

### Core Functionality
- **Multi-Warehouse Management**: Support for multiple warehouse locations
- **Real-Time Stock Tracking**: Live inventory updates and availability checks
- **Stock Reservation System**: Reserve stock for orders with automatic expiration
- **Automatic Reordering**: Smart reorder point management and alerts
- **Stock Movement Tracking**: Complete audit trail of all inventory changes
- **Low Stock Alerts**: Configurable thresholds and notifications

### Advanced Features
- **Pessimistic Locking**: Prevent race conditions during stock operations
- **Batch Operations**: Efficient bulk inventory updates
- **Stock Utilization Analytics**: Performance metrics and optimization insights
- **Overstock Detection**: Identify and manage excess inventory
- **Location Management**: Warehouse bin and location tracking
- **Historical Reporting**: Comprehensive inventory movement history

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL with connection pooling
- **ORM**: Spring Data JPA with Hibernate
- **Messaging**: Apache Kafka for event streaming
- **Locking**: JPA Pessimistic Locking
- **Service Discovery**: Eureka Client
- **Configuration**: Spring Cloud Config
- **Monitoring**: Actuator with Prometheus metrics
- **Build Tool**: Maven
- **Java Version**: 17+

## Database Schema

### Inventory Table
```sql
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    warehouse_id VARCHAR(255) NOT NULL,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    total_quantity INTEGER NOT NULL DEFAULT 0,
    minimum_stock_level INTEGER NOT NULL DEFAULT 10,
    maximum_stock_level INTEGER NOT NULL DEFAULT 1000,
    reorder_point INTEGER NOT NULL DEFAULT 20,
    reorder_quantity INTEGER NOT NULL DEFAULT 100,
    location VARCHAR(50),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, warehouse_id)
);

CREATE INDEX idx_product_id ON inventory(product_id);
CREATE INDEX idx_warehouse_id ON inventory(warehouse_id);
CREATE UNIQUE INDEX idx_product_warehouse ON inventory(product_id, warehouse_id);
```

### Stock Movement Table
```sql
CREATE TABLE stock_movement (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    warehouse_id VARCHAR(255) NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    reference_id VARCHAR(255),
    reference_type VARCHAR(50),
    reason VARCHAR(500),
    user_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_movement_product ON stock_movement(product_id);
CREATE INDEX idx_stock_movement_warehouse ON stock_movement(warehouse_id);
CREATE INDEX idx_stock_movement_type ON stock_movement(movement_type);
CREATE INDEX idx_stock_movement_date ON stock_movement(created_at);
```

## Key Components

### InventoryService
- **Location**: `com.ecommerce.inventory.service.InventoryService`
- **Purpose**: Core business logic for inventory management
- **Key Methods**:
  - `createOrUpdateInventory()`: Create or update inventory records
  - `reserveStock()`: Reserve stock for orders with locking
  - `confirmReservation()`: Confirm and consume reserved stock
  - `releaseReservation()`: Release reserved stock back to available
  - `adjustStock()`: Manual stock adjustments with audit trail
  - `getInventorySummary()`: Get comprehensive inventory analytics

### InventoryController
- **Location**: `com.ecommerce.inventory.controller.InventoryController`
- **Purpose**: REST API endpoints for inventory operations
- **Features**: Pagination, sorting, filtering, bulk operations

### InventoryRepository
- **Location**: `com.ecommerce.inventory.repository.InventoryRepository`
- **Purpose**: Data access layer with custom queries
- **Features**: Pessimistic locking, batch operations, analytics queries

### StockMovementRepository
- **Location**: `com.ecommerce.inventory.repository.StockMovementRepository`
- **Purpose**: Track all inventory movements and changes
- **Features**: Audit trail, reporting queries, movement analytics

## API Endpoints

### Inventory Management
```http
GET    /api/inventory/{productId}/{warehouseId}     # Get specific inventory
POST   /api/inventory                               # Create/update inventory
PUT    /api/inventory/{productId}/{warehouseId}     # Update inventory
DELETE /api/inventory/{productId}/{warehouseId}     # Delete inventory
```

### Stock Operations
```http
POST   /api/inventory/reserve                       # Reserve stock
POST   /api/inventory/confirm                       # Confirm reservation
POST   /api/inventory/release                       # Release reservation
POST   /api/inventory/adjust                        # Adjust stock levels
```

### Warehouse Operations
```http
GET    /api/inventory/warehouse/{warehouseId}       # Get warehouse inventory
GET    /api/inventory/product/{productId}           # Get product across warehouses
GET    /api/inventory/low-stock                     # Get low stock items
GET    /api/inventory/reorder-needed                # Get items needing reorder
```

### Analytics & Reporting
```http
GET    /api/inventory/summary                       # Get inventory summary
GET    /api/inventory/movements                     # Get stock movements
GET    /api/inventory/analytics                     # Get inventory analytics
GET    /api/inventory/utilization                   # Get stock utilization
```

### Bulk Operations
```http
POST   /api/inventory/bulk/reserve                  # Bulk stock reservation
POST   /api/inventory/bulk/adjust                   # Bulk stock adjustment
POST   /api/inventory/bulk/import                   # Bulk inventory import
```

## Configuration

### Application Properties
```yaml
server:
  port: 8084

spring:
  application:
    name: inventory-service
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_db
    username: inventory_user
    password: inventory_password
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        jdbc.batch_size: 20
        order_inserts: true
        order_updates: true
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all
      retries: 3

app:
  inventory:
    default-minimum-stock: 10
    default-reorder-point: 20
    default-reorder-quantity: 100
    low-stock-threshold-percentage: 20
    overstock-threshold-percentage: 90
    stock-check-interval: 3600000
  warehouse:
    default-warehouse-id: WH001
    supported-warehouses:
      - WH001
      - WH002
      - WH003
```

### Environment Variables
- `DB_URL`: Database connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `KAFKA_SERVERS`: Kafka bootstrap servers
- `EUREKA_URL`: Eureka server URL
- `CONFIG_SERVER_URL`: Config server URL

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+
- Eureka Server (for service discovery)
- Spring Cloud Config Server

## Setup Instructions

### 1. Database Setup
```sql
-- Create database
CREATE DATABASE inventory_db;

-- Create user
CREATE USER inventory_user WITH PASSWORD 'inventory_password';
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inventory_user;

-- Connect to inventory_db and create tables
\c inventory_db;

-- Tables will be created automatically by Hibernate
-- Or run the SQL scripts from src/main/resources/db/migration/
```

### 2. Kafka Topics Setup
```bash
# Create required Kafka topics
kafka-topics.sh --create --topic inventory-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic reorder-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic stock-alerts --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

### 3. Environment Configuration
```bash
# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/inventory_db
export DB_USERNAME=inventory_user
export DB_PASSWORD=inventory_password
export KAFKA_SERVERS=localhost:9092
export EUREKA_URL=http://localhost:8761/eureka/
export CONFIG_SERVER_URL=http://localhost:8888
```

### 4. Build and Run
```bash
# Navigate to inventory-service directory
cd inventory-service

# Build the application
mvn clean compile

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

### 5. Verify Installation
```bash
# Check health endpoint
curl http://localhost:8084/actuator/health

# Test inventory creation
curl -X POST http://localhost:8084/api/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD001",
    "warehouseId": "WH001",
    "quantity": 100,
    "minimumStockLevel": 10,
    "reorderPoint": 20,
    "reorderQuantity": 50
  }'

# Test stock reservation
curl -X POST http://localhost:8084/api/inventory/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD001",
    "warehouseId": "WH001",
    "quantity": 5,
    "orderId": "ORDER001",
    "userId": "USER001"
  }'
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=InventoryServiceTest

# Run tests with coverage
mvn test jacoco:report
```

### Integration Tests
```bash
# Run integration tests
mvn test -Dtest=InventoryServiceApplicationTests

# Run with test profile
mvn test -Dspring.profiles.active=test
```

### Load Testing
```bash
# Test concurrent stock reservations
for i in {1..100}; do
  curl -X POST http://localhost:8084/api/inventory/reserve \
    -H "Content-Type: application/json" \
    -d '{
      "productId": "PROD001",
      "warehouseId": "WH001",
      "quantity": 1,
      "orderId": "ORDER'$i'",
      "userId": "USER'$i'"
    }' &
done
wait
```

## Kafka Events

### Published Events
- **inventory.updated**: When inventory levels change
- **stock.reserved**: When stock is reserved for orders
- **stock.confirmed**: When reserved stock is confirmed
- **stock.released**: When reserved stock is released
- **reorder.needed**: When stock falls below reorder point
- **stock.alert**: When stock levels are critical

### Consumed Events
- **order.created**: Reserve stock for new orders
- **order.cancelled**: Release reserved stock
- **order.shipped**: Confirm stock consumption
- **product.created**: Initialize inventory for new products

### Event Processing
```java
@KafkaListener(topics = "order-events")
public void handleOrderEvent(OrderEvent event) {
    switch (event.getEventType()) {
        case "ORDER_CREATED":
            reserveStockForOrder(event);
            break;
        case "ORDER_CANCELLED":
            releaseReservedStock(event);
            break;
        case "ORDER_SHIPPED":
            confirmStockConsumption(event);
            break;
    }
}
```

## Stock Management

### Stock States
- **Available**: Stock ready for reservation
- **Reserved**: Stock reserved for specific orders
- **Confirmed**: Reserved stock confirmed for shipment
- **Adjustment**: Manual stock adjustments

### Movement Types
```java
public enum MovementType {
    INITIAL,              // Initial stock setup
    INBOUND,              // Stock received
    OUTBOUND,             // Stock shipped
    RESERVED,             // Stock reserved
    CONFIRMED,            // Reservation confirmed
    RELEASED,             // Reservation released
    ADJUSTMENT_POSITIVE,  // Manual increase
    ADJUSTMENT_NEGATIVE,  // Manual decrease
    TRANSFER_IN,          // Transfer from another warehouse
    TRANSFER_OUT,         // Transfer to another warehouse
    DAMAGED,              // Damaged stock removal
    EXPIRED,              // Expired stock removal
    RETURNED              // Customer return
}
```

### Locking Strategy
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.warehouseId = :warehouseId")
Optional<Inventory> findByProductIdAndWarehouseIdWithLock(
    @Param("productId") String productId, 
    @Param("warehouseId") String warehouseId
);
```

## Monitoring

### Health Checks
- **Endpoint**: `/actuator/health`
- **Database**: Connection and query health
- **Kafka**: Producer and consumer health
- **External Services**: Product service connectivity

### Metrics
- **Stock Levels**: Current inventory across warehouses
- **Reservation Rates**: Stock reservation success/failure rates
- **Movement Velocity**: Stock turnover rates
- **Reorder Frequency**: Automatic reorder trigger rates
- **Lock Contention**: Database locking performance

### Custom Metrics
```java
@Component
public class InventoryMetrics {
    private final Counter stockReservations = Counter.builder("inventory.reservations.total").register(meterRegistry);
    private final Gauge lowStockItems = Gauge.builder("inventory.low_stock.count").register(meterRegistry, this, InventoryMetrics::getLowStockCount);
    private final Timer reservationTime = Timer.builder("inventory.reservation.time").register(meterRegistry);
}
```

### Alerts
- **Low Stock**: When inventory falls below minimum levels
- **Overstock**: When inventory exceeds maximum levels
- **Failed Reservations**: When stock reservations fail
- **Database Locks**: When lock wait times exceed thresholds

## Performance Optimization

### Database Optimization
- **Connection Pooling**: HikariCP with optimized settings
- **Batch Processing**: Hibernate batch operations
- **Indexing**: Strategic database indexes
- **Query Optimization**: Efficient JPA queries

### Caching Strategy
```java
@Cacheable(value = "inventory", key = "#productId + '_' + #warehouseId")
public InventoryResponseDto getInventory(String productId, String warehouseId) {
    // Cache frequently accessed inventory data
}

@CacheEvict(value = "inventory", key = "#productId + '_' + #warehouseId")
public void updateInventory(String productId, String warehouseId, Integer quantity) {
    // Evict cache on updates
}
```

### Async Processing
```java
@Async
public CompletableFuture<Void> processStockMovements(List<StockMovement> movements) {
    // Process stock movements asynchronously
    movements.parallelStream().forEach(this::recordMovement);
    return CompletableFuture.completedFuture(null);
}
```

## Error Handling

### Custom Exceptions
```java
public class InsufficientStockException extends RuntimeException {
    private final String productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;
}

public class InventoryNotFoundException extends RuntimeException {
    private final String productId;
    private final String warehouseId;
}
```

### Global Exception Handler
```java
@ControllerAdvice
public class InventoryExceptionHandler {
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        // Handle insufficient stock errors
    }
}
```

## Security

### Access Control
- **JWT Authentication**: Validate user tokens
- **Role-Based Access**: Different permissions for different operations
- **API Rate Limiting**: Prevent abuse of inventory APIs

### Data Protection
- **Input Validation**: Validate all inventory requests
- **SQL Injection Prevention**: Use parameterized queries
- **Audit Logging**: Track all inventory changes

## Troubleshooting

### Common Issues

1. **Stock Reservation Failures**
   ```bash
   # Check current stock levels
   curl http://localhost:8084/api/inventory/PROD001/WH001
   
   # Check recent stock movements
   curl http://localhost:8084/api/inventory/movements?productId=PROD001
   ```

2. **Database Lock Timeouts**
   ```sql
   -- Check for long-running transactions
   SELECT pid, state, query_start, query 
   FROM pg_stat_activity 
   WHERE state != 'idle' AND query_start < now() - interval '1 minute';
   
   -- Check for locks
   SELECT * FROM pg_locks WHERE NOT granted;
   ```

3. **Kafka Consumer Lag**
   ```bash
   # Check consumer group lag
   kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group inventory-service --describe
   ```

### Performance Issues
- **Slow Queries**: Enable SQL logging and analyze query performance
- **High Memory Usage**: Monitor JVM heap and optimize object creation
- **Lock Contention**: Analyze database lock wait times

## Analytics & Reporting

### Inventory Analytics
```java
public class InventoryAnalytics {
    public InventorySummary getInventorySummary(String warehouseId) {
        // Calculate total value, turnover rates, etc.
    }
    
    public List<ProductPerformance> getTopPerformingProducts(int limit) {
        // Analyze product movement velocity
    }
    
    public StockUtilizationReport getStockUtilization(LocalDate from, LocalDate to) {
        // Calculate stock utilization metrics
    }
}
```

### Reporting Endpoints
```http
GET /api/inventory/reports/summary          # Inventory summary report
GET /api/inventory/reports/movements        # Stock movement report
GET /api/inventory/reports/utilization      # Stock utilization report
GET /api/inventory/reports/reorder          # Reorder analysis report
```

## Contributing

1. **Testing**: Ensure all inventory operations are thoroughly tested
2. **Locking**: Be careful with database locking to prevent deadlocks
3. **Events**: Maintain event consistency across operations
4. **Performance**: Consider performance impact of new features

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For inventory service issues:
- **Stock Discrepancies**: Check stock movement audit trail
- **Performance Issues**: Monitor database and application metrics
- **Integration Issues**: Verify Kafka event processing
- **Data Consistency**: Review transaction boundaries and locking