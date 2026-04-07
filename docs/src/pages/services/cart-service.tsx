import React, { useState } from 'react'
import { 
  ShoppingCart, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  Settings,
  Package,
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

const addToCartFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Add to Cart Request',
    description: 'Client sends item details',
    type: 'start',
    next: ['validate']
  },
  {
    id: 'validate',
    title: 'Validate Request',
    description: 'Check item ID and quantity',
    type: 'process',
    next: ['check-inventory']
  },
  {
    id: 'check-inventory',
    title: 'Check Inventory',
    description: 'Verify stock with Inventory Service',
    type: 'decision',
    next: ['stock-ok', 'stock-error'],
    condition: 'Stock available?'
  },
  {
    id: 'stock-error',
    title: 'Return Error',
    description: 'Insufficient stock',
    type: 'end'
  },
  {
    id: 'stock-ok',
    title: 'Update Cart',
    description: 'Add item to Redis cart',
    type: 'process',
    next: ['success']
  },
  {
    id: 'success',
    title: 'Success Response',
    description: 'Return updated cart',
    type: 'end'
  }
]

export default function CartService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-blue-100 rounded-lg mr-4">
            <ShoppingCart className="h-8 w-8 text-blue-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Cart Service</h1>
            <p className="text-lg text-gray-600">Shopping Cart Management</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8083</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>Redis</span>
          </div>
          <div className="flex items-center">
            <Zap className="h-4 w-4 mr-1" />
            <span>High Performance</span>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
          {[
            { key: 'overview', label: 'Overview' },
            { key: 'architecture', label: 'Architecture' },
            { key: 'flows', label: 'Process Flows' },
            { key: 'api', label: 'API Reference' }
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
                    <ShoppingCart className="h-4 w-4 mr-2 mt-0.5 text-blue-600" />
                    <span>Manage temporary shopping sessions</span>
                  </li>
                  <li className="flex items-start">
                    <Database className="h-4 w-4 mr-2 mt-0.5 text-blue-600" />
                    <span>Persist cart items in Redis</span>
                  </li>
                  <li className="flex items-start">
                    <Package className="h-4 w-4 mr-2 mt-0.5 text-blue-600" />
                    <span>Merge anonymous and user carts</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• High-performance in-memory storage</li>
                  <li>• Automatic cart expiration (TTL)</li>
                  <li>• Real-time price validation</li>
                  <li>• Inventory availability checks</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Technology Stack */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Technology Stack</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Backend</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Java 17</li>
                  <li>• Spring Boot 3.x</li>
                  <li>• Spring Data Redis</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Database</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Redis (Primary Store)</li>
                  <li>• Lettuce Client</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Integration</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Feign Clients (Inventory)</li>
                  <li>• Kafka (Events)</li>
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
                 The Cart Service uses a layered architecture with Redis as the primary data store for high-speed access.
                 It communicates with the Inventory Service to validate stock availability before adding items.
               </p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'flows' && (
         <div className="space-y-12">
           <h2 className="text-2xl font-semibold text-gray-900 mb-6">Add to Cart Flow</h2>
           <div className="card p-4">
             <div className="space-y-4">
               {addToCartFlow.map((step, index) => (
                 <div key={step.id} className="border-l-2 border-blue-200 pl-4 pb-4">
                   <h4 className="font-medium">{step.title}</h4>
                   <p className="text-sm text-gray-600">{step.description}</p>
                 </div>
               ))}
             </div>
           </div>
         </div>
      )}

      {activeTab === 'api' && (
        <div className="space-y-12">
          <h2 className="text-2xl font-semibold text-gray-900 mb-6">API Endpoints</h2>
          <div className="card">
            <table className="min-w-full text-sm text-left">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2">Method</th>
                  <th className="px-4 py-2">Endpoint</th>
                  <th className="px-4 py-2">Description</th>
                </tr>
              </thead>
              <tbody className="divide-y">
                <tr>
                  <td className="px-4 py-2 font-mono text-green-600">GET</td>
                  <td className="px-4 py-2 font-mono">/api/cart</td>
                  <td className="px-4 py-2">Get current cart</td>
                </tr>
                <tr>
                  <td className="px-4 py-2 font-mono text-blue-600">POST</td>
                  <td className="px-4 py-2 font-mono">/api/cart/items</td>
                  <td className="px-4 py-2">Add item to cart</td>
                </tr>
                <tr>
                  <td className="px-4 py-2 font-mono text-red-600">DELETE</td>
                  <td className="px-4 py-2 font-mono">/api/cart/items/{'{itemId}'}</td>
                  <td className="px-4 py-2">Remove item</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
