import React, { useState } from 'react'
import { 
  Server, 
  Shield, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  Settings,
  Globe,
  Zap
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end'
  next?: string[]
  condition?: string
}

const requestFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Incoming Request',
    description: 'Client sends request to Gateway',
    type: 'start',
    next: ['route-match']
  },
  {
    id: 'route-match',
    title: 'Route Matching',
    description: 'Match URL path to configured route',
    type: 'process',
    next: ['auth-filter']
  },
  {
    id: 'auth-filter',
    title: 'Authentication',
    description: 'Validate JWT token if required',
    type: 'decision',
    next: ['authorized', 'unauthorized'],
    condition: 'Token Valid?'
  },
  {
    id: 'unauthorized',
    title: 'Block Request',
    description: 'Return 401 Unauthorized',
    type: 'end'
  },
  {
    id: 'authorized',
    title: 'Load Balancing',
    description: 'Select service instance via Eureka',
    type: 'process',
    next: ['forward']
  },
  {
    id: 'forward',
    title: 'Forward Request',
    description: 'Send request to downstream service',
    type: 'end'
  }
]

export default function ApiGateway() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'config'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-gray-100 rounded-lg mr-4">
            <Server className="h-8 w-8 text-gray-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">API Gateway</h1>
            <p className="text-lg text-gray-600">Unified Entry Point and Routing</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8080</span>
          </div>
          <div className="flex items-center">
            <Shield className="h-4 w-4 mr-1" />
            <span>Security</span>
          </div>
          <div className="flex items-center">
            <Globe className="h-4 w-4 mr-1" />
            <span>Routing</span>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
          {[
            { key: 'overview', label: 'Overview' },
            { key: 'architecture', label: 'Architecture' },
            { key: 'flows', label: 'Request Flow' },
            { key: 'config', label: 'Configuration' }
          ].map(tab => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as any)}
              className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
                activeTab === tab.key
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Content based on active tab */}
      {activeTab === 'overview' && (
        <div className="space-y-12">
          {/* Service Overview */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Service Overview</h2>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Core Responsibilities</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li className="flex items-start">
                    <Globe className="h-4 w-4 mr-2 mt-0.5 text-gray-600" />
                    <span>Route external requests to services</span>
                  </li>
                  <li className="flex items-start">
                    <Shield className="h-4 w-4 mr-2 mt-0.5 text-gray-600" />
                    <span>Centralized authentication/authorization</span>
                  </li>
                  <li className="flex items-start">
                    <Zap className="h-4 w-4 mr-2 mt-0.5 text-gray-600" />
                    <span>Rate limiting and throttling</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• Dynamic routing with Eureka discovery</li>
                  <li>• Cross-Cutting Concerns (CORS, Logging)</li>
                  <li>• Circuit Breaking fallback options</li>
                  <li>• Request/Response transformation</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Technology Stack */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Technology Stack</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Core</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Java 17</li>
                  <li>• Spring Boot 3.x</li>
                  <li>• Spring Cloud Gateway</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Discovery</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Netflix Eureka Client</li>
                  <li>• Load Balancer</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Security</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Spring Security</li>
                  <li>• JWT Filter</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'architecture' && (
        <div className="space-y-12">
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Component Architecture</h2>
            <div className="card">
               <p className="text-gray-600">
                 The API Gateway acts as the single entry point for all client requests. It abstracts the internal microservice architecture
                 and handles cross-cutting concerns like security and routing. It integrates with Eureka for dynamic service discovery.
               </p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'flows' && (
         <div className="space-y-12">
           <h2 className="text-2xl font-semibold text-gray-900 mb-6">Request Routing Flow</h2>
           <div className="card p-4">
             <div className="space-y-4">
               {requestFlow.map((step, index) => (
                 <div key={step.id} className="border-l-2 border-gray-200 pl-4 pb-4">
                   <h4 className="font-medium">{step.title}</h4>
                   <p className="text-sm text-gray-600">{step.description}</p>
                 </div>
               ))}
             </div>
           </div>
         </div>
      )}

      {activeTab === 'config' && (
        <div className="space-y-12">
          <h2 className="text-2xl font-semibold text-gray-900 mb-6">Route Configuration</h2>
          <div className="card">
            <pre className="bg-gray-50 p-4 rounded-md text-sm overflow-x-auto">
{`spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
        # ... other routes
`}
            </pre>
          </div>
        </div>
      )}
    </div>
  )
}
