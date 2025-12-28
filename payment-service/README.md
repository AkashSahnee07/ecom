# Payment Service

## Overview

The Payment Service is a critical microservice in the e-commerce platform responsible for handling all payment-related operations. It provides secure payment processing, multiple payment method support, refund management, and comprehensive transaction tracking. The service integrates with various payment gateways and ensures PCI DSS compliance for secure financial transactions.

## Features

### Core Functionality
- **Payment Processing**: Secure payment transaction handling
- **Multiple Payment Methods**: Credit/Debit cards, Digital wallets, Bank transfers
- **Payment Gateway Integration**: Support for multiple payment providers
- **Refund Management**: Full and partial refund processing
- **Transaction Tracking**: Complete payment lifecycle monitoring
- **Payment Status Management**: Real-time status updates

### Advanced Features
- **Fraud Detection**: Basic fraud prevention mechanisms
- **Payment Retry Logic**: Automatic retry for failed payments
- **Payment Expiry**: Automatic payment expiration handling
- **Event-Driven Architecture**: Kafka integration for payment events
- **Payment Analytics**: Transaction reporting and analytics
- **Multi-Currency Support**: Handle payments in different currencies

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Messaging**: Apache Kafka
- **Service Discovery**: Eureka Client
- **Configuration**: Spring Cloud Config
- **Security**: Spring Security
- **Build Tool**: Maven
- **Java Version**: 17+

## Database Schema

### Payments Table
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(255) UNIQUE NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_gateway VARCHAR(100),
    transaction_id VARCHAR(255),
    gateway_response TEXT,
    failure_reason VARCHAR(500),
    refund_amount DECIMAL(19,2),
    refund_reason VARCHAR(500),
    processed_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Payment Methods Table
```sql
CREATE TABLE payment_methods (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    method_type VARCHAR(50) NOT NULL,
    provider VARCHAR(100),
    token VARCHAR(255),
    last_four VARCHAR(4),
    expiry_month INTEGER,
    expiry_year INTEGER,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Key Components

### PaymentService
- **Location**: `com.ecommerce.payment.service.PaymentService`
- **Purpose**: Core business logic for payment processing
- **Key Methods**:
  - `createPayment()`: Initialize new payment transactions
  - `processPayment()`: Execute payment processing
  - `refundPayment()`: Handle refund requests
  - `getPaymentStatus()`: Retrieve payment status

### PaymentController
- **Location**: `com.ecommerce.payment.controller.PaymentController`
- **Purpose**: REST API endpoints for payment operations
- **Base Path**: `/api/payments`

### Payment Entity
- **Location**: `com.ecommerce.payment.entity.Payment`
- **Purpose**: JPA entity representing payment data
- **Features**: Audit fields, status tracking, gateway integration

## API Endpoints

### Payment Management
```http
POST   /api/payments                    # Create new payment
GET    /api/payments/{id}               # Get payment by ID
PUT    /api/payments/{id}/process       # Process payment
PUT    /api/payments/{id}/cancel        # Cancel payment
GET    /api/payments/order/{orderId}    # Get payments by order
GET    /api/payments/user/{userId}      # Get user's payments
```

### Refund Management
```http
POST   /api/payments/{id}/refund        # Create refund
GET    /api/payments/{id}/refunds       # Get payment refunds
PUT    /api/payments/refunds/{id}       # Update refund status
```

### Payment Methods
```http
POST   /api/payments/methods            # Add payment method
GET    /api/payments/methods/user/{userId} # Get user's payment methods
DELETE /api/payments/methods/{id}       # Remove payment method
```

### Analytics
```http
GET    /api/payments/summary            # Get payment summary
GET    /api/payments/analytics          # Get payment analytics
```

## Configuration

### Application Properties
```yaml
server:
  port: 8084

spring:
  application:
    name: payment-service
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_db
    username: payment_user
    password: payment_password
  kafka:
    bootstrap-servers: localhost:9092

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

payment:
  gateway:
    stripe:
      api-key: ${STRIPE_API_KEY}
      webhook-secret: ${STRIPE_WEBHOOK_SECRET}
    paypal:
      client-id: ${PAYPAL_CLIENT_ID}
      client-secret: ${PAYPAL_CLIENT_SECRET}
```

### Environment Variables
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `STRIPE_API_KEY`: Stripe payment gateway API key
- `STRIPE_WEBHOOK_SECRET`: Stripe webhook secret
- `PAYPAL_CLIENT_ID`: PayPal client ID
- `PAYPAL_CLIENT_SECRET`: PayPal client secret
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka server addresses

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+
- Eureka Server (for service discovery)
- Config Server (for centralized configuration)
- Payment Gateway Accounts (Stripe, PayPal, etc.)

## Setup Instructions

### 1. Database Setup
```sql
-- Create database
CREATE DATABASE payment_db;

-- Create user
CREATE USER payment_user WITH PASSWORD 'payment_password';
GRANT ALL PRIVILEGES ON DATABASE payment_db TO payment_user;
```

### 2. Payment Gateway Setup

#### Stripe Configuration
1. Create Stripe account at https://stripe.com
2. Get API keys from Stripe Dashboard
3. Set up webhook endpoints for payment events

#### PayPal Configuration
1. Create PayPal Developer account
2. Create application and get client credentials
3. Configure webhook notifications

### 3. Environment Configuration
```bash
# Set environment variables
export DB_USERNAME=payment_user
export DB_PASSWORD=payment_password
export STRIPE_API_KEY=sk_test_...
export STRIPE_WEBHOOK_SECRET=whsec_...
export PAYPAL_CLIENT_ID=your_paypal_client_id
export PAYPAL_CLIENT_SECRET=your_paypal_client_secret
```

### 4. Build and Run
```bash
# Navigate to payment-service directory
cd payment-service

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

# Test payment creation
curl -X POST http://localhost:8084/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "order123",
    "userId": "user123",
    "amount": 99.99,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD"
  }'
```

## Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=PaymentServiceTest
```

### Integration Tests
```bash
# Run integration tests
mvn test -Dtest=PaymentServiceApplicationTests
```

### Payment Gateway Testing
```bash
# Use test card numbers for Stripe
# Visa: 4242424242424242
# Mastercard: 5555555555554444
# Declined: 4000000000000002
```

## Kafka Events

### Published Events
- **payment.created**: When payment is initialized
- **payment.processing**: When payment processing starts
- **payment.completed**: When payment is successful
- **payment.failed**: When payment fails
- **payment.refunded**: When refund is processed

### Consumed Events
- **order.created**: From order service
- **order.cancelled**: From order service
- **user.updated**: From user service

### Event Schema
```json
{
  "eventType": "payment.completed",
  "paymentId": "pay_123456",
  "orderId": "order123",
  "userId": "user123",
  "amount": 99.99,
  "currency": "USD",
  "status": "COMPLETED",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Security

### PCI DSS Compliance
- **Data Encryption**: All sensitive payment data encrypted
- **Tokenization**: Card details replaced with secure tokens
- **Access Control**: Strict access controls for payment data
- **Audit Logging**: Complete audit trail for all transactions

### Authentication & Authorization
- **JWT Integration**: Validate JWT tokens from API Gateway
- **Role-Based Access**: Different access levels for users and admins
- **API Security**: Rate limiting and request validation

### Data Protection
- **Encryption at Rest**: Database encryption for sensitive fields
- **Encryption in Transit**: TLS/SSL for all communications
- **Key Management**: Secure key storage and rotation

## Monitoring

### Health Checks
- **Endpoint**: `/actuator/health`
- **Database**: Connection and transaction health
- **Payment Gateways**: Gateway connectivity status
- **Kafka**: Producer and consumer health

### Metrics
- **Payment Volume**: Transaction count and amounts
- **Success Rate**: Payment success/failure ratios
- **Response Time**: Payment processing latency
- **Gateway Performance**: Individual gateway metrics

### Alerting
- **Failed Payments**: Alert on high failure rates
- **Gateway Issues**: Alert on gateway connectivity problems
- **Fraud Detection**: Alert on suspicious activities

## Performance Optimization

### Database Optimization
- **Indexing**: Payment ID, order ID, user ID, status
- **Connection Pooling**: Optimized HikariCP settings
- **Query Optimization**: Efficient queries for reporting

### Caching Strategy
- **Payment Status**: Cache recent payment statuses
- **User Payment Methods**: Cache saved payment methods
- **Gateway Responses**: Cache successful gateway responses

### Async Processing
- **Event Publishing**: Asynchronous Kafka event publishing
- **Webhook Processing**: Background webhook handling
- **Refund Processing**: Async refund operations

## Error Handling

### Payment Failures
- **Retry Logic**: Automatic retry for transient failures
- **Fallback Gateways**: Switch to backup payment gateways
- **Error Codes**: Standardized error response codes

### Gateway Integration
- **Timeout Handling**: Proper timeout configuration
- **Circuit Breaker**: Prevent cascade failures
- **Graceful Degradation**: Fallback mechanisms

## Troubleshooting

### Common Issues

1. **Payment Gateway Connection Failed**
   ```bash
   # Check gateway credentials
   curl -H "Authorization: Bearer $STRIPE_API_KEY" \
        https://api.stripe.com/v1/charges
   
   # Verify webhook endpoints
   curl -X GET https://api.stripe.com/v1/webhook_endpoints \
        -H "Authorization: Bearer $STRIPE_API_KEY"
   ```

2. **Database Connection Issues**
   ```bash
   # Check database connectivity
   psql -h localhost -U payment_user -d payment_db
   
   # Verify table structure
   \dt payments
   ```

3. **Kafka Event Issues**
   ```bash
   # Check payment events topic
   kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic payment-events
   
   # Monitor payment events
   kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic payment-events
   ```

### Performance Issues
- **Slow Payments**: Check gateway response times
- **Database Locks**: Monitor transaction isolation levels
- **Memory Leaks**: Monitor JVM heap usage

## Compliance

### PCI DSS Requirements
- **Requirement 1**: Firewall configuration
- **Requirement 2**: Default passwords and security parameters
- **Requirement 3**: Cardholder data protection
- **Requirement 4**: Encryption of cardholder data transmission

### Audit Requirements
- **Transaction Logging**: All payment transactions logged
- **Access Logging**: All system access logged
- **Change Management**: All system changes tracked

## Contributing

1. **Security First**: Always consider security implications
2. **Testing**: Comprehensive testing including security tests
3. **Documentation**: Update security and compliance documentation
4. **Code Review**: Mandatory security-focused code reviews

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For payment-related issues:
- **Technical Issues**: Check logs and monitoring dashboards
- **Gateway Issues**: Contact respective payment gateway support
- **Security Concerns**: Follow incident response procedures