# Shipping Service

## Overview

The Shipping Service is a comprehensive microservice that manages all shipment operations in the e-commerce platform. It handles shipment creation, tracking, status updates, carrier integration, and real-time notifications to provide end-to-end shipping management capabilities.

## Features

### Core Features
- **Shipment Management**
  - Shipment creation and lifecycle management
  - Unique tracking number generation
  - Multi-carrier support (FedEx, UPS, DHL)
  - Service type selection (Standard, Express, Overnight)
- **Tracking System**
  - Real-time tracking updates
  - Comprehensive tracking event history
  - Status transition validation
  - Location-based tracking events

### Advanced Features
- **Carrier Integration**
  - Multiple shipping carrier APIs
  - Automatic carrier selection
  - Rate comparison and optimization
  - Label generation and printing
- **Status Management**
  - Automated status transitions
  - Business rule validation
  - Exception handling and alerts
  - Delivery confirmation
- **Notifications**
  - Real-time status notifications
  - Event-driven updates via Kafka
  - Customer communication integration
  - Delivery alerts and confirmations
- **Analytics & Reporting**
  - Shipment performance metrics
  - Carrier performance analysis
  - Delivery time analytics
  - Cost optimization insights

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: PostgreSQL 14+
- **Migration**: Liquibase
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Monitoring**: Spring Boot Actuator, Micrometer
- **Testing**: JUnit 5, Mockito

## Database Schema

### Shipments Table
```sql
CREATE TABLE shipments (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL UNIQUE,
    tracking_number VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    carrier VARCHAR(100) NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    shipping_cost DECIMAL(10,2),
    weight DECIMAL(10,2),
    dimensions VARCHAR(100),
    
    -- Sender Address
    sender_name VARCHAR(255),
    sender_address_line1 VARCHAR(255),
    sender_address_line2 VARCHAR(255),
    sender_city VARCHAR(100),
    sender_state VARCHAR(100),
    sender_postal_code VARCHAR(20),
    sender_country VARCHAR(100),
    
    -- Recipient Address
    recipient_name VARCHAR(255),
    recipient_address_line1 VARCHAR(255),
    recipient_address_line2 VARCHAR(255),
    recipient_city VARCHAR(100),
    recipient_state VARCHAR(100),
    recipient_postal_code VARCHAR(20),
    recipient_country VARCHAR(100),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    expected_delivery_date TIMESTAMP,
    
    -- Additional fields
    priority VARCHAR(20),
    special_instructions TEXT,
    version BIGINT DEFAULT 0
);

-- Indexes for performance
CREATE INDEX idx_shipment_order_id ON shipments(order_id);
CREATE INDEX idx_shipment_tracking_number ON shipments(tracking_number);
CREATE INDEX idx_shipment_status ON shipments(status);
CREATE INDEX idx_shipment_carrier ON shipments(carrier);
CREATE INDEX idx_shipment_created_at ON shipments(created_at);
```

### Tracking Events Table
```sql
CREATE TABLE tracking_events (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id),
    event_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    event_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    carrier_event_id VARCHAR(255),
    metadata JSONB
);

CREATE INDEX idx_tracking_events_shipment_id ON tracking_events(shipment_id);
CREATE INDEX idx_tracking_events_timestamp ON tracking_events(event_timestamp);
```

## Key Components

### Application Layer
- **ShippingServiceApplication**: Main Spring Boot application class
- **ShippingController**: REST API endpoints for shipping operations
- **ShippingService**: Core business logic for shipment management

### Service Layer
- **TrackingService**: Handles tracking events and updates
- **CarrierService**: Integration with shipping carriers
- **NotificationService**: Event publishing and notifications
- **RateCalculationService**: Shipping cost calculations
- **LabelService**: Shipping label generation

### Data Layer
- **ShipmentRepository**: Shipment data access operations
- **TrackingEventRepository**: Tracking event data access
- **DatabaseConfig**: Database configuration and connection management

### Integration Layer
- **KafkaConfig**: Message broker configuration
- **CarrierIntegration**: External carrier API integration
- **EventPublisher**: Kafka event publishing

## API Endpoints

### Shipment Management
```http
# Create a new shipment
POST /shipping/api/v1/shipping/shipments
Content-Type: application/json
{
  "orderId": 12345,
  "carrier": "FEDEX",
  "serviceType": "STANDARD",
  "shippingAddress": {
    "name": "John Doe",
    "addressLine1": "123 Main St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  },
  "billingAddress": {...},
  "weight": 2.5,
  "dimensions": "30x20x15",
  "priority": "STANDARD"
}

# Get shipment by ID
GET /shipping/api/v1/shipping/shipments/{shipmentId}

# Get shipment by tracking number
GET /shipping/api/v1/shipping/track/{trackingNumber}

# Update shipment status
PUT /shipping/api/v1/shipping/shipments/{shipmentId}/status
Content-Type: application/json
{
  "status": "SHIPPED",
  "description": "Package picked up by carrier",
  "location": "New York, NY"
}

# Cancel shipment
DELETE /shipping/api/v1/shipping/shipments/{shipmentId}
```

### Tracking Operations
```http
# Get tracking history
GET /shipping/api/v1/shipping/shipments/{shipmentId}/tracking

# Add tracking event
POST /shipping/api/v1/shipping/shipments/{shipmentId}/tracking
Content-Type: application/json
{
  "eventType": "IN_TRANSIT",
  "description": "Package in transit",
  "location": "Chicago, IL",
  "eventTimestamp": "2024-01-15T10:30:00Z"
}

# Get shipments by status
GET /shipping/api/v1/shipping/shipments?status=IN_TRANSIT

# Get shipments by carrier
GET /shipping/api/v1/shipping/shipments?carrier=FEDEX
```

### Analytics & Reporting
```http
# Get shipment statistics
GET /shipping/api/v1/shipping/analytics/stats

# Get carrier performance
GET /shipping/api/v1/shipping/analytics/carriers

# Get delivery metrics
GET /shipping/api/v1/shipping/analytics/delivery-metrics
```

### Health and Monitoring
```http
GET /shipping/actuator/health
GET /shipping/actuator/metrics
GET /shipping/actuator/info
GET /shipping/api-docs
GET /shipping/swagger-ui.html
```

## Configuration

### Application Properties
```yaml
server:
  port: 8086
  servlet:
    context-path: /shipping

spring:
  application:
    name: shipping-service
  
  # Database Configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/shipping_db
    username: shipping_user
    password: shipping_pass
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
  
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 25
  
  # Liquibase Configuration
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
  
  # Kafka Configuration
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: shipping-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer

# Shipping Configuration
shipping:
  carriers:
    fedex:
      api-key: ${FEDEX_API_KEY}
      secret-key: ${FEDEX_SECRET_KEY}
      account-number: ${FEDEX_ACCOUNT_NUMBER}
      meter-number: ${FEDEX_METER_NUMBER}
    ups:
      api-key: ${UPS_API_KEY}
      username: ${UPS_USERNAME}
      password: ${UPS_PASSWORD}
    dhl:
      api-key: ${DHL_API_KEY}
      account-number: ${DHL_ACCOUNT_NUMBER}
  
  notifications:
    enabled: true
    events:
      - SHIPPED
      - OUT_FOR_DELIVERY
      - DELIVERED
      - EXCEPTION
      - CANCELLED
  
  rules:
    auto-cancel-after-days: 30
    max-weight-kg: 70
    max-dimensions-cm: 150
    default-carrier: FEDEX
```

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=shipping_db
DB_USERNAME=shipping_user
DB_PASSWORD=shipping_pass

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
EUREKA_CLIENT_SERVICE_URL=http://localhost:8761/eureka

# Carrier APIs
FEDEX_API_KEY=your_fedex_api_key
FEDEX_SECRET_KEY=your_fedex_secret_key
FEDEX_ACCOUNT_NUMBER=your_fedex_account
FEDEX_METER_NUMBER=your_fedex_meter

UPS_API_KEY=your_ups_api_key
UPS_USERNAME=your_ups_username
UPS_PASSWORD=your_ups_password

DHL_API_KEY=your_dhl_api_key
DHL_ACCOUNT_NUMBER=your_dhl_account
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- Apache Kafka 2.8+
- Eureka Server (for service discovery)
- Config Server (for centralized configuration)
- Carrier API accounts (FedEx, UPS, DHL)

## Setup Instructions

### 1. Clone and Build
```bash
# Navigate to shipping service directory
cd shipping-service

# Build the application
mvn clean compile
```

### 2. Database Setup
```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE shipping_db;
CREATE USER shipping_user WITH PASSWORD 'shipping_pass';
GRANT ALL PRIVILEGES ON DATABASE shipping_db TO shipping_user;

# Liquibase will handle schema creation automatically
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
java -jar target/shipping-service-1.0.0.jar

# With specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 5. Verify Installation
```bash
# Check service health
curl http://localhost:8086/shipping/actuator/health

# View API documentation
open http://localhost:8086/shipping/swagger-ui.html

# Test shipment creation
curl -X POST http://localhost:8086/shipping/api/v1/shipping/shipments \
  -H "Content-Type: application/json" \
  -d '{"orderId":12345,"carrier":"FEDEX","serviceType":"STANDARD"}'
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=ShippingServiceTest

# Run tests with coverage
mvn test jacoco:report
```

### Integration Tests
```bash
# Run integration tests
mvn verify -P integration-tests

# Test with embedded database
mvn test -Dspring.profiles.active=test
```

### API Testing
```bash
# Test shipment creation
curl -X POST http://localhost:8086/shipping/api/v1/shipping/shipments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 12345,
    "carrier": "FEDEX",
    "serviceType": "STANDARD",
    "shippingAddress": {
      "name": "John Doe",
      "addressLine1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    },
    "weight": 2.5
  }'

# Test tracking
curl http://localhost:8086/shipping/api/v1/shipping/track/TRK1234567890

# Test status update
curl -X PUT http://localhost:8086/shipping/api/v1/shipping/shipments/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"SHIPPED","description":"Package shipped","location":"New York, NY"}'
```

## Kafka Events

### Published Events
```yaml
# Shipment Created
topic: shipment.created
payload:
  shipmentId: string
  orderId: string
  trackingNumber: string
  carrier: string
  status: string
  timestamp: string

# Shipment Status Updated
topic: shipment.updated
payload:
  shipmentId: string
  orderId: string
  trackingNumber: string
  oldStatus: string
  newStatus: string
  location: string
  timestamp: string

# Shipment Delivered
topic: shipment.delivered
payload:
  shipmentId: string
  orderId: string
  trackingNumber: string
  deliveredAt: string
  location: string
  timestamp: string

# Tracking Event Added
topic: tracking.updated
payload:
  shipmentId: string
  trackingNumber: string
  eventType: string
  description: string
  location: string
  timestamp: string
```

### Consumed Events
```yaml
# Order Completed
topic: order.completed
handler: createShipmentForOrder()

# Payment Confirmed
topic: payment.confirmed
handler: processShipmentCreation()

# Inventory Reserved
topic: inventory.reserved
handler: prepareShipment()
```

## Shipment Status Flow

### Status Transitions
```
CREATED → PROCESSING → SHIPPED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
    ↓         ↓          ↓           ↓              ↓
 CANCELLED  CANCELLED  EXCEPTION  EXCEPTION    EXCEPTION
```

### Status Descriptions
- **CREATED**: Shipment record created, awaiting processing
- **PROCESSING**: Shipment being prepared for pickup
- **SHIPPED**: Package picked up by carrier
- **IN_TRANSIT**: Package in transit to destination
- **OUT_FOR_DELIVERY**: Package out for final delivery
- **DELIVERED**: Package successfully delivered
- **EXCEPTION**: Delivery exception occurred
- **CANCELLED**: Shipment cancelled

## Carrier Integration

### Supported Carriers

#### FedEx Integration
```java
@Service
public class FedExCarrierService implements CarrierService {
    // Rate calculation
    // Label generation
    // Tracking updates
    // Pickup scheduling
}
```

#### UPS Integration
```java
@Service
public class UPSCarrierService implements CarrierService {
    // Rate calculation
    // Label generation
    // Tracking updates
    // Pickup scheduling
}
```

#### DHL Integration
```java
@Service
public class DHLCarrierService implements CarrierService {
    // Rate calculation
    // Label generation
    // Tracking updates
    // Pickup scheduling
}
```

### Carrier Selection Logic
```yaml
carrier-selection:
  rules:
    - condition: "weight > 50kg"
      carrier: "DHL"
    - condition: "priority == 'OVERNIGHT'"
      carrier: "FEDEX"
    - condition: "destination.country == 'USA'"
      carrier: "UPS"
    - default: "FEDEX"
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
    custom:
      carrier-apis:
        enabled: true
```

### Custom Metrics
- `shipment.creation.count`: Number of shipments created
- `shipment.delivery.time`: Average delivery time
- `carrier.performance.score`: Carrier performance metrics
- `tracking.events.count`: Number of tracking events
- `exception.rate`: Shipment exception rate

### Logging
```yaml
logging:
  level:
    com.ecommerce.shipping: INFO
    org.springframework.kafka: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/shipping-service.log
```

## Performance Optimization

### Database Optimization
- **Indexes**: Optimized indexes for frequent queries
- **Connection Pooling**: HikariCP for efficient connections
- **Batch Processing**: Batch inserts and updates
- **Query Optimization**: Efficient JPA queries with projections

### Caching Strategy
- **Application Cache**: Frequently accessed shipment data
- **Carrier Rate Cache**: Shipping rate caching
- **Tracking Cache**: Recent tracking events

### Async Processing
- **Event Publishing**: Asynchronous Kafka event publishing
- **Carrier API calls**: Non-blocking carrier integrations
- **Notification Processing**: Background notification sending

## Error Handling

### Custom Exceptions
```java
// Shipping-specific exceptions
ShipmentNotFoundException
InvalidShipmentStatusException
CarrierIntegrationException
TrackingNumberGenerationException
InvalidAddressException
```

### Global Exception Handler
```java
@RestControllerAdvice
public class ShippingExceptionHandler {
    // Handles all shipping-related exceptions
    // Returns appropriate HTTP status codes and error messages
}
```

### Error Response Format
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Shipment not found with tracking number: TRK123",
  "path": "/shipping/api/v1/shipping/track/TRK123",
  "errorCode": "SHIPMENT_NOT_FOUND"
}
```

## Security Considerations

### API Security
- **Input Validation**: Comprehensive request validation
- **Rate Limiting**: API rate limiting per client
- **Authentication**: JWT token validation
- **Authorization**: Role-based access control

### Data Security
- **Address Encryption**: Sensitive address data encryption
- **Carrier API Security**: Secure API key management
- **Audit Logging**: Comprehensive audit trails
- **Data Masking**: PII data masking in logs

## Troubleshooting

### Common Issues

**Database Connection Issues**
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Test database connection
psql -h localhost -U shipping_user -d shipping_db

# Check connection pool
curl http://localhost:8086/shipping/actuator/metrics/hikaricp.connections.active
```

**Carrier API Issues**
```bash
# Check carrier API connectivity
curl -H "Authorization: Bearer $FEDEX_API_KEY" https://api.fedex.com/test

# Monitor API response times
curl http://localhost:8086/shipping/actuator/metrics/http.client.requests
```

**Kafka Issues**
```bash
# Check Kafka topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Monitor consumer lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group shipping-service-group
```

**Tracking Issues**
```bash
# Check tracking number generation
curl http://localhost:8086/shipping/actuator/metrics/tracking.number.generation

# Verify tracking events
curl http://localhost:8086/shipping/api/v1/shipping/shipments/1/tracking
```

### Performance Issues
```bash
# Monitor JVM metrics
curl http://localhost:8086/shipping/actuator/metrics/jvm.memory.used

# Check database performance
curl http://localhost:8086/shipping/actuator/metrics/jdbc.connections.active

# Monitor shipment processing
curl http://localhost:8086/shipping/actuator/metrics/shipment.processing.time
```

## Deployment

### Docker
```dockerfile
# Build image
docker build -t shipping-service:latest .

# Run container
docker run -d \
  --name shipping-service \
  -p 8086:8086 \
  -e DB_HOST=postgres \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e FEDEX_API_KEY=your_api_key \
  shipping-service:latest
```

### Docker Compose
```yaml
version: '3.8'
services:
  shipping-service:
    build: .
    ports:
      - "8086:8086"
    environment:
      - DB_HOST=postgres
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - FEDEX_API_KEY=${FEDEX_API_KEY}
    depends_on:
      - postgres
      - kafka
  
  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=shipping_db
      - POSTGRES_USER=shipping_user
      - POSTGRES_PASSWORD=shipping_pass
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: shipping-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: shipping-service
  template:
    spec:
      containers:
      - name: shipping-service
        image: shipping-service:latest
        ports:
        - containerPort: 8086
        env:
        - name: DB_HOST
          value: "postgres-service"
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        - name: FEDEX_API_KEY
          valueFrom:
            secretKeyRef:
              name: carrier-secrets
              key: fedex-api-key
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
- Test carrier integrations thoroughly
- Ensure database migrations are backward compatible

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Service Port**: 8086  
**Context Path**: /shipping  
**Health Check**: http://localhost:8086/shipping/actuator/health  
**API Documentation**: http://localhost:8086/shipping/swagger-ui.html  
**Metrics**: http://localhost:8086/shipping/actuator/metrics