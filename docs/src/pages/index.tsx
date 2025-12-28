import React from 'react'
import Link from 'next/link'
import { 
  ArrowRight, 
  Server, 
  Database, 
  Cloud, 
  Shield, 
  Zap, 
  Users,
  ShoppingCart,
  CreditCard,
  Package,
  Star,
  Truck,
  Bell,
  Activity
} from 'lucide-react'

const features = [
  {
    name: 'Microservices Architecture',
    description: 'Scalable and maintainable microservices built with Spring Boot',
    icon: Server,
    color: 'text-blue-600',
    bgColor: 'bg-blue-100'
  },
  {
    name: 'Event-Driven Communication',
    description: 'Asynchronous messaging with Apache Kafka for real-time updates',
    icon: Activity,
    color: 'text-green-600',
    bgColor: 'bg-green-100'
  },
  {
    name: 'Distributed Database',
    description: 'Multiple database technologies optimized for each service',
    icon: Database,
    color: 'text-purple-600',
    bgColor: 'bg-purple-100'
  },
  {
    name: 'Cloud Native',
    description: 'Containerized services ready for cloud deployment',
    icon: Cloud,
    color: 'text-indigo-600',
    bgColor: 'bg-indigo-100'
  },
  {
    name: 'Security First',
    description: 'JWT authentication and authorization across all services',
    icon: Shield,
    color: 'text-red-600',
    bgColor: 'bg-red-100'
  },
  {
    name: 'High Performance',
    description: 'Optimized for speed with caching and load balancing',
    icon: Zap,
    color: 'text-yellow-600',
    bgColor: 'bg-yellow-100'
  }
]

const services = [
  { name: 'User Service', icon: Users, description: 'User management and authentication' },
  { name: 'Product Service', icon: Package, description: 'Product catalog and inventory' },
  { name: 'Cart Service', icon: ShoppingCart, description: 'Shopping cart management' },
  { name: 'Order Service', icon: Activity, description: 'Order processing and tracking' },
  { name: 'Payment Service', icon: CreditCard, description: 'Payment processing and billing' },
  { name: 'Recommendation Service', icon: Star, description: 'AI-powered product recommendations' },
  { name: 'Shipping Service', icon: Truck, description: 'Shipping and delivery management' },
  { name: 'Notification Service', icon: Bell, description: 'Real-time notifications' }
]

export default function Home() {
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Hero Section */}
      <div className="text-center py-12 lg:py-20">
        <h1 className="text-4xl lg:text-6xl font-bold text-gray-900 mb-6">
          E-commerce <span className="text-gradient">Microservices</span>
        </h1>
        <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
          Comprehensive documentation for a modern, scalable e-commerce platform 
          built with microservices architecture, Spring Boot, and cloud-native technologies.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link href="/architecture" className="btn-primary">
            View Architecture
            <ArrowRight className="ml-2 h-4 w-4" />
          </Link>
          <Link href="/services" className="btn-outline">
            Explore Services
          </Link>
        </div>
      </div>

      {/* Features Grid */}
      <div className="py-16">
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">Key Features</h2>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            Built with modern technologies and best practices for enterprise-grade applications
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {features.map((feature) => {
            const Icon = feature.icon
            return (
              <div key={feature.name} className="card-hover">
                <div className={`inline-flex p-3 rounded-lg ${feature.bgColor} mb-4`}>
                  <Icon className={`h-6 w-6 ${feature.color}`} />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{feature.name}</h3>
                <p className="text-gray-600">{feature.description}</p>
              </div>
            )
          })}
        </div>
      </div>

      {/* Services Overview */}
      <div className="py-16">
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">Microservices</h2>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            Each service is designed for a specific business capability with its own database and API
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {services.map((service) => {
            const Icon = service.icon
            return (
              <Link 
                key={service.name} 
                href={`/services/${service.name.toLowerCase().replace(' ', '-')}`}
                className="card-hover group cursor-pointer"
              >
                <div className="flex items-center mb-3">
                  <div className="p-2 bg-primary-100 rounded-lg mr-3 group-hover:bg-primary-200 transition-colors">
                    <Icon className="h-5 w-5 text-primary-600" />
                  </div>
                  <h3 className="font-semibold text-gray-900 group-hover:text-primary-600 transition-colors">
                    {service.name}
                  </h3>
                </div>
                <p className="text-sm text-gray-600">{service.description}</p>
              </Link>
            )
          })}
        </div>
      </div>

      {/* Quick Start */}
      <div className="py-16">
        <div className="bg-gradient-primary rounded-2xl p-8 lg:p-12 text-white">
          <div className="max-w-3xl mx-auto text-center">
            <h2 className="text-3xl font-bold mb-4">Ready to Get Started?</h2>
            <p className="text-xl mb-8 text-blue-100">
              Explore our comprehensive documentation, API references, and deployment guides
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link href="/deployment" className="btn bg-white text-primary-600 hover:bg-gray-50">
                Deployment Guide
              </Link>
              <Link href="/api" className="btn border-white text-white hover:bg-white hover:text-primary-600">
                API Documentation
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="py-16">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-8">
          <div className="text-center">
            <div className="text-3xl font-bold text-primary-600 mb-2">13</div>
            <div className="text-gray-600">Microservices</div>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-primary-600 mb-2">7</div>
            <div className="text-gray-600">Databases</div>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-primary-600 mb-2">50+</div>
            <div className="text-gray-600">API Endpoints</div>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-primary-600 mb-2">99.9%</div>
            <div className="text-gray-600">Uptime</div>
          </div>
        </div>
      </div>
    </div>
  )
}