import React, { useState, useEffect } from 'react'
import { 
  Server, 
  Database, 
  Cloud, 
  GitBranch, 
  Layers, 
  Activity,
  Users,
  ShoppingCart,
  CreditCard,
  Package,
  Star,
  Truck,
  Bell,
  Search,
  Shield,
  Zap
} from 'lucide-react'

interface DiagramNode {
  id: string
  label: string
  type: 'service' | 'database' | 'infrastructure' | 'external'
  icon: React.ComponentType<any>
  color: string
  bgColor: string
  description: string
  port?: string
  dependencies?: string[]
}

const architectureNodes: DiagramNode[] = [
  // Infrastructure Services
  {
    id: 'eureka-server',
    label: 'Eureka Server',
    type: 'infrastructure',
    icon: Search,
    color: 'text-blue-600',
    bgColor: 'bg-blue-100',
    description: 'Service Discovery and Registration',
    port: '8761'
  },
  {
    id: 'config-server',
    label: 'Config Server',
    type: 'infrastructure',
    icon: Shield,
    color: 'text-green-600',
    bgColor: 'bg-green-100',
    description: 'Centralized Configuration Management',
    port: '8888'
  },
  {
    id: 'api-gateway',
    label: 'API Gateway',
    type: 'infrastructure',
    icon: Zap,
    color: 'text-purple-600',
    bgColor: 'bg-purple-100',
    description: 'Request Routing and Load Balancing',
    port: '8080'
  },
  
  // Business Services
  {
    id: 'user-service',
    label: 'User Service',
    type: 'service',
    icon: Users,
    color: 'text-indigo-600',
    bgColor: 'bg-indigo-100',
    description: 'User Management and Authentication',
    port: '8081',
    dependencies: ['postgres', 'redis']
  },
  {
    id: 'product-service',
    label: 'Product Service',
    type: 'service',
    icon: Package,
    color: 'text-orange-600',
    bgColor: 'bg-orange-100',
    description: 'Product Catalog Management',
    port: '8082',
    dependencies: ['mysql', 'elasticsearch']
  },
  {
    id: 'cart-service',
    label: 'Cart Service',
    type: 'service',
    icon: ShoppingCart,
    color: 'text-pink-600',
    bgColor: 'bg-pink-100',
    description: 'Shopping Cart Operations',
    port: '8083',
    dependencies: ['redis']
  },
  {
    id: 'order-service',
    label: 'Order Service',
    type: 'service',
    icon: Activity,
    color: 'text-red-600',
    bgColor: 'bg-red-100',
    description: 'Order Processing and Management',
    port: '8084',
    dependencies: ['postgres', 'kafka']
  },
  {
    id: 'payment-service',
    label: 'Payment Service',
    type: 'service',
    icon: CreditCard,
    color: 'text-yellow-600',
    bgColor: 'bg-yellow-100',
    description: 'Payment Processing',
    port: '8085',
    dependencies: ['postgres', 'kafka']
  },
  {
    id: 'recommendation-service',
    label: 'Recommendation Service',
    type: 'service',
    icon: Star,
    color: 'text-cyan-600',
    bgColor: 'bg-cyan-100',
    description: 'AI-Powered Product Recommendations',
    port: '8087',
    dependencies: ['postgres', 'kafka']
  },
  {
    id: 'shipping-service',
    label: 'Shipping Service',
    type: 'service',
    icon: Truck,
    color: 'text-teal-600',
    bgColor: 'bg-teal-100',
    description: 'Shipping and Delivery Management',
    port: '8088',
    dependencies: ['postgres', 'kafka']
  },
  {
    id: 'notification-service',
    label: 'Notification Service',
    type: 'service',
    icon: Bell,
    color: 'text-violet-600',
    bgColor: 'bg-violet-100',
    description: 'Real-time Notifications',
    port: '8090',
    dependencies: ['kafka']
  },
  
  // Databases
  {
    id: 'postgres',
    label: 'PostgreSQL',
    type: 'database',
    icon: Database,
    color: 'text-blue-700',
    bgColor: 'bg-blue-50',
    description: 'Primary Database for User, Order, Payment Services',
    port: '5432'
  },
  {
    id: 'mysql',
    label: 'MySQL',
    type: 'database',
    icon: Database,
    color: 'text-orange-700',
    bgColor: 'bg-orange-50',
    description: 'Product Catalog Database',
    port: '3306'
  },
  {
    id: 'redis',
    label: 'Redis',
    type: 'database',
    icon: Database,
    color: 'text-red-700',
    bgColor: 'bg-red-50',
    description: 'Caching and Session Storage',
    port: '6379'
  },
  {
    id: 'elasticsearch',
    label: 'Elasticsearch',
    type: 'database',
    icon: Search,
    color: 'text-yellow-700',
    bgColor: 'bg-yellow-50',
    description: 'Search and Analytics Engine',
    port: '9200'
  },
  {
    id: 'kafka',
    label: 'Apache Kafka',
    type: 'infrastructure',
    icon: Activity,
    color: 'text-gray-700',
    bgColor: 'bg-gray-100',
    description: 'Event Streaming Platform',
    port: '9092'
  }
]

const DiagramNode: React.FC<{ node: DiagramNode; isSelected: boolean; onClick: () => void }> = ({ 
  node, 
  isSelected, 
  onClick 
}) => {
  const Icon = node.icon
  
  return (
    <div 
      className={`
        relative p-4 rounded-lg border-2 cursor-pointer transition-all duration-200
        ${isSelected 
          ? 'border-primary-500 shadow-lg scale-105' 
          : 'border-gray-200 hover:border-gray-300 hover:shadow-md'
        }
        ${node.bgColor}
      `}
      onClick={onClick}
    >
      <div className="flex items-center mb-2">
        <div className={`p-2 rounded-lg ${node.bgColor} mr-3`}>
          <Icon className={`h-5 w-5 ${node.color}`} />
        </div>
        <div>
          <h3 className="font-semibold text-sm text-gray-900">{node.label}</h3>
          {node.port && (
            <p className="text-xs text-gray-500">Port: {node.port}</p>
          )}
        </div>
      </div>
      <p className="text-xs text-gray-600">{node.description}</p>
      
      {node.dependencies && node.dependencies.length > 0 && (
        <div className="mt-2 pt-2 border-t border-gray-200">
          <p className="text-xs text-gray-500 mb-1">Dependencies:</p>
          <div className="flex flex-wrap gap-1">
            {node.dependencies.map(dep => (
              <span key={dep} className="text-xs bg-gray-200 text-gray-700 px-2 py-1 rounded">
                {dep}
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default function Architecture() {
  const [selectedNode, setSelectedNode] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState<'overview' | 'services' | 'infrastructure' | 'data'>('overview')

  const filteredNodes = architectureNodes.filter(node => {
    switch (activeTab) {
      case 'services':
        return node.type === 'service'
      case 'infrastructure':
        return node.type === 'infrastructure'
      case 'data':
        return node.type === 'database'
      default:
        return true
    }
  })

  const selectedNodeData = selectedNode ? architectureNodes.find(n => n.id === selectedNode) : null

  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-4">System Architecture</h1>
        <p className="text-lg text-gray-600 max-w-3xl">
          Our e-commerce platform follows a microservices architecture pattern with event-driven communication, 
          distributed databases, and cloud-native deployment strategies.
        </p>
      </div>

      {/* Architecture Principles */}
      <div className="mb-12">
        <h2 className="text-2xl font-semibold text-gray-900 mb-6">Architecture Principles</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="card">
            <div className="flex items-center mb-3">
              <Layers className="h-6 w-6 text-blue-600 mr-3" />
              <h3 className="font-semibold text-gray-900">Microservices</h3>
            </div>
            <p className="text-sm text-gray-600">
              Each service is independently deployable and owns its data
            </p>
          </div>
          
          <div className="card">
            <div className="flex items-center mb-3">
              <Activity className="h-6 w-6 text-green-600 mr-3" />
              <h3 className="font-semibold text-gray-900">Event-Driven</h3>
            </div>
            <p className="text-sm text-gray-600">
              Asynchronous communication through Apache Kafka
            </p>
          </div>
          
          <div className="card">
            <div className="flex items-center mb-3">
              <Database className="h-6 w-6 text-purple-600 mr-3" />
              <h3 className="font-semibold text-gray-900">Polyglot Persistence</h3>
            </div>
            <p className="text-sm text-gray-600">
              Different databases optimized for specific use cases
            </p>
          </div>
          
          <div className="card">
            <div className="flex items-center mb-3">
              <Cloud className="h-6 w-6 text-indigo-600 mr-3" />
              <h3 className="font-semibold text-gray-900">Cloud Native</h3>
            </div>
            <p className="text-sm text-gray-600">
              Containerized and orchestrated with Docker and Kubernetes
            </p>
          </div>
        </div>
      </div>

      {/* Interactive Diagram */}
      <div className="mb-12">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-semibold text-gray-900">Interactive Architecture Diagram</h2>
          
          {/* Filter Tabs */}
          <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg">
            {[
              { key: 'overview', label: 'All' },
              { key: 'services', label: 'Services' },
              { key: 'infrastructure', label: 'Infrastructure' },
              { key: 'data', label: 'Data Layer' }
            ].map(tab => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key as any)}
                className={`px-3 py-1 text-sm font-medium rounded-md transition-colors ${
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
        
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Diagram */}
          <div className="lg:col-span-3">
            <div className="diagram-container min-h-[600px]">
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {filteredNodes.map(node => (
                  <DiagramNode
                    key={node.id}
                    node={node}
                    isSelected={selectedNode === node.id}
                    onClick={() => setSelectedNode(node.id)}
                  />
                ))}n              </div>
            </div>
          </div>
          
          {/* Details Panel */}
          <div className="lg:col-span-1">
            <div className="card sticky top-24">
              <h3 className="font-semibold text-gray-900 mb-4">Component Details</h3>
              
              {selectedNodeData ? (
                <div>
                  <div className="flex items-center mb-4">
                    <div className={`p-3 rounded-lg ${selectedNodeData.bgColor} mr-3`}>
                      <selectedNodeData.icon className={`h-6 w-6 ${selectedNodeData.color}`} />
                    </div>
                    <div>
                      <h4 className="font-semibold text-gray-900">{selectedNodeData.label}</h4>
                      {selectedNodeData.port && (
                        <p className="text-sm text-gray-500">Port: {selectedNodeData.port}</p>
                      )}
                    </div>
                  </div>
                  
                  <p className="text-sm text-gray-600 mb-4">{selectedNodeData.description}</p>
                  
                  <div className="space-y-3">
                    <div>
                      <span className="text-xs font-medium text-gray-500 uppercase tracking-wider">Type</span>
                      <p className="text-sm text-gray-900 capitalize">{selectedNodeData.type}</p>
                    </div>
                    
                    {selectedNodeData.dependencies && selectedNodeData.dependencies.length > 0 && (
                      <div>
                        <span className="text-xs font-medium text-gray-500 uppercase tracking-wider">Dependencies</span>
                        <div className="mt-1 space-y-1">
                          {selectedNodeData.dependencies.map(dep => {
                            const depNode = architectureNodes.find(n => n.id === dep)
                            return (
                              <div key={dep} className="flex items-center text-sm text-gray-700">
                                {depNode && <depNode.icon className="h-4 w-4 mr-2" />}
                                {depNode?.label || dep}
                              </div>
                            )
                          })}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              ) : (
                <p className="text-sm text-gray-500">
                  Click on any component in the diagram to view detailed information
                </p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Technology Stack */}
      <div className="mb-12">
        <h2 className="text-2xl font-semibold text-gray-900 mb-6">Technology Stack</h2>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <div className="card">
            <h3 className="font-semibold text-gray-900 mb-4">Backend Services</h3>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>• Java 17 + Spring Boot 3.x</li>
              <li>• Spring Cloud Gateway</li>
              <li>• Spring Security + JWT</li>
              <li>• Spring Data JPA</li>
              <li>• Maven for dependency management</li>
            </ul>
          </div>
          
          <div className="card">
            <h3 className="font-semibold text-gray-900 mb-4">Data Layer</h3>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>• PostgreSQL (Primary Database)</li>
              <li>• MySQL (Product Catalog)</li>
              <li>• Redis (Caching & Sessions)</li>
              <li>• Elasticsearch (Search & Analytics)</li>
              <li>• Apache Kafka (Event Streaming)</li>
            </ul>
          </div>
          
          <div className="card">
            <h3 className="font-semibold text-gray-900 mb-4">Infrastructure</h3>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>• Docker & Docker Compose</li>
              <li>• Netflix Eureka (Service Discovery)</li>
              <li>• Spring Cloud Config</li>
              <li>• Zipkin (Distributed Tracing)</li>
              <li>• Kibana (Log Analysis)</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Communication Patterns */}
      <div className="mb-12">
        <h2 className="text-2xl font-semibent text-gray-900 mb-6">Communication Patterns</h2>
        
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className="card">
            <h3 className="font-semibold text-gray-900 mb-4">Synchronous Communication</h3>
            <p className="text-sm text-gray-600 mb-4">
              Direct HTTP calls for immediate responses and critical operations
            </p>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>• User authentication requests</li>
              <li>• Product catalog queries</li>
              <li>• Real-time inventory checks</li>
              <li>• Payment processing</li>
            </ul>
          </div>
          
          <div className="card">
            <h3 className="font-semibold text-gray-900 mb-4">Asynchronous Communication</h3>
            <p className="text-sm text-gray-600 mb-4">
              Event-driven messaging through Kafka for loose coupling
            </p>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>• Order status updates</li>
              <li>• Inventory level changes</li>
              <li>• User behavior tracking</li>
              <li>• Notification triggers</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}