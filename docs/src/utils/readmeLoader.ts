export interface ServiceReadme {
  name: string;
  path: string;
  description: string;
  content?: string;
}

export const projectReadmes: ServiceReadme[] = [
  {
    name: 'Main README',
    path: '.',
    description: 'Complete project overview, architecture, and setup instructions'
  },
  {
    name: 'Quick Start Guide',
    path: 'QUICK_START',
    description: 'Fast setup guide to get the platform running quickly'
  },
  {
    name: 'Tracing Setup',
    path: 'TRACING_SETUP',
    description: 'Distributed tracing configuration and monitoring setup'
  }
];

export const serviceReadmes: ServiceReadme[] = [
  {
    name: 'API Gateway',
    path: 'api-gateway',
    description: 'Central entry point for all microservice requests with routing and load balancing'
  },
  {
    name: 'Cart Service',
    path: 'cart-service',
    description: 'Manages shopping cart operations and user cart persistence'
  },
  {
    name: 'Config Server',
    path: 'config-server',
    description: 'Centralized configuration management for all microservices'
  },
  {
    name: 'Eureka Server',
    path: 'eureka-server',
    description: 'Service discovery and registration server for microservices'
  },
  {
    name: 'Inventory Service',
    path: 'inventory-service',
    description: 'Manages product inventory, stock levels, and availability'
  },
  {
    name: 'Notification Service',
    path: 'notification-service',
    description: 'Handles email, SMS, and push notifications across the platform'
  },
  {
    name: 'Order Service',
    path: 'order-service',
    description: 'Processes orders, manages order lifecycle, and coordinates with other services'
  },
  {
    name: 'Payment Service',
    path: 'payment-service',
    description: 'Handles payment processing, transactions, and payment gateway integration'
  },
  {
    name: 'Product Service',
    path: 'product-service',
    description: 'Manages product catalog, categories, and product information'
  },
  {
    name: 'Recommendation Service',
    path: 'recommendation-service',
    description: 'Provides personalized product recommendations using ML algorithms'
  },
  {
    name: 'Review Service',
    path: 'review-service',
    description: 'Manages product reviews, ratings, and customer feedback'
  },
  {
    name: 'Shipping Service',
    path: 'shipping-service',
    description: 'Handles shipping calculations, tracking, and delivery management'
  },
  {
    name: 'User Service',
    path: 'user-service',
    description: 'Manages user authentication, profiles, and user-related operations'
  }
];