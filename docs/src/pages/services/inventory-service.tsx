import React, { useState } from 'react'
import { 
  Package, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  XCircle,
  AlertTriangle,
  TrendingUp,
  TrendingDown,
  BarChart3,
  Warehouse,
  Truck,
  Clock,
  RefreshCw,
  Settings,
  Search,
  Filter
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end' | 'event' | 'external'
  next?: string[]
  condition?: string
  status?: 'success' | 'error' | 'warning' | 'info'
}

const stockUpdateFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Stock Update Request',
    description: 'Receive inventory update request',
    type: 'start',
    next: ['validate-request']
  },
  {
    id: 'validate-request',
    title: 'Validate Request',
    description: 'Check product ID and quantity format',
    type: 'process',
    next: ['request-valid']
  },
  {
    id: 'request-valid',
    title: 'Request Valid?',
    description: 'Verify all required fields are present',
    type: 'decision',
    next: ['check-product', 'validation-error'],
    condition: 'Valid product ID and quantity'
  },
  {
    id: 'validation-error',
    title: 'Validation Error',
    description: 'Return validation error response',
    type: 'end',
    status: 'error'
  },
  {
    id: 'check-product',
    title: 'Check Product Exists',
    description: 'Verify product exists in catalog',
    type: 'process',
    next: ['product-exists']
  },
  {
    id: 'product-exists',
    title: 'Product Exists?',
    description: 'Check if product is in inventory system',
    type: 'decision',
    next: ['acquire-lock', 'product-not-found'],
    condition: 'Product found in database'
  },
  {
    id: 'product-not-found',
    title: 'Product Not Found',
    description: 'Product does not exist in inventory',
    type: 'end',
    status: 'error'
  },
  {
    id: 'acquire-lock',
    title: 'Acquire Row Lock',
    description: 'Lock inventory record for update',
    type: 'process',
    next: ['update-stock']
  },
  {
    id: 'update-stock',
    title: 'Update Stock Level',
    description: 'Modify inventory quantity',
    type: 'process',
    next: ['check-threshold']
  },
  {
    id: 'check-threshold',
    title: 'Check Low Stock Threshold',
    description: 'Verify if stock is below minimum level',
    type: 'process',
    next: ['below-threshold']
  },
  {
    id: 'below-threshold',
    title: 'Below Threshold?',
    description: 'Check if reorder is needed',
    type: 'decision',
    next: ['trigger-reorder', 'release-lock'],
    condition: 'Stock < minimum threshold'
  },
  {
    id: 'trigger-reorder',
    title: 'Trigger Reorder Alert',
    description: 'Send low stock notification',
    type: 'event',
    next: ['release-lock']
  },
  {
    id: 'release-lock',
    title: 'Release Lock',
    description: 'Unlock inventory record',
    type: 'process',
    next: ['update-success']
  },
  {
    id: 'update-success',
    title: 'Update Successful',
    description: 'Stock level updated successfully',
    type: 'end',
    status: 'success'
  }
]

const reservationFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Reservation Request',
    description: 'Receive inventory reservation request',
    type: 'start',
    next: ['validate-request']
  },
  {
    id: 'validate-request',
    title: 'Validate Request',
    description: 'Check order ID and item details',
    type: 'process',
    next: ['request-valid']
  },
  {
    id: 'request-valid',
    title: 'Request Valid?',
    description: 'Verify all required data is present',
    type: 'decision',
    next: ['check-availability', 'validation-error'],
    condition: 'Valid order and items'
  },
  {
    id: 'validation-error',
    title: 'Validation Error',
    description: 'Return validation error response',
    type: 'end',
    status: 'error'
  },
  {
    id: 'check-availability',
    title: 'Check Stock Availability',
    description: 'Verify sufficient stock for all items',
    type: 'process',
    next: ['stock-available']
  },
  {
    id: 'stock-available',
    title: 'Stock Available?',
    description: 'Check if all items are in stock',
    type: 'decision',
    next: ['create-reservation', 'insufficient-stock'],
    condition: 'Sufficient quantity available'
  },
  {
    id: 'insufficient-stock',
    title: 'Insufficient Stock',
    description: 'Not enough inventory for reservation',
    type: 'end',
    status: 'error'
  },
  {
    id: 'create-reservation',
    title: 'Create Reservation',
    description: 'Reserve inventory for order',
    type: 'process',
    next: ['set-expiry']
  },
  {
    id: 'set-expiry',
    title: 'Set Expiry Time',
    description: 'Set reservation expiration (15 minutes)',
    type: 'process',
    next: ['update-available']
  },
  {
    id: 'update-available',
    title: 'Update Available Stock',
    description: 'Reduce available quantity',
    type: 'process',
    next: ['reservation-created']
  },
  {
    id: 'reservation-created',
    title: 'Reservation Created',
    description: 'Inventory successfully reserved',
    type: 'end',
    status: 'success'
  }
]

const FlowDiagram: React.FC<{ steps: FlowStep[]; title: string }> = ({ steps, title }) => {
  const [selectedStep, setSelectedStep] = useState<string | null>(null)
  
  const getStepIcon = (type: string) => {
    switch (type) {
      case 'start': return <Activity className="h-4 w-4" />
      case 'process': return <Settings className="h-4 w-4" />
      case 'decision': return <CheckCircle className="h-4 w-4" />
      case 'event': return <AlertTriangle className="h-4 w-4" />
      case 'external': return <Truck className="h-4 w-4" />
      case 'end': return <CheckCircle className="h-4 w-4" />
      default: return <Activity className="h-4 w-4" />
    }
  }
  
  const getStepColor = (type: string, status?: string) => {
    if (status === 'error') return 'bg-red-100 text-red-700 border-red-300'
    if (status === 'warning') return 'bg-yellow-100 text-yellow-700 border-yellow-300'
    if (status === 'success') return 'bg-green-100 text-green-700 border-green-300'
    if (status === 'info') return 'bg-blue-100 text-blue-700 border-blue-300'
    
    switch (type) {
      case 'start': return 'bg-blue-100 text-blue-700 border-blue-300'
      case 'process': return 'bg-indigo-100 text-indigo-700 border-indigo-300'
      case 'decision': return 'bg-orange-100 text-orange-700 border-orange-300'
      case 'event': return 'bg-teal-100 text-teal-700 border-teal-300'
      case 'external': return 'bg-purple-100 text-purple-700 border-purple-300'
      case 'end': return 'bg-gray-100 text-gray-700 border-gray-300'
      default: return 'bg-gray-100 text-gray-700 border-gray-300'
    }
  }
  
  return (
    <div className="card">
      <h3 className="text-lg font-semibold text-gray-900 mb-6">{title}</h3>
      
      <div className="space-y-4">
        {steps.map((step, index) => (
          <div key={step.id} className="relative">
            <div 
              className={`
                p-4 rounded-lg border-2 cursor-pointer transition-all duration-200
                ${selectedStep === step.id ? 'ring-2 ring-primary-500' : ''}
                ${getStepColor(step.type, step.status)}
              `}
              onClick={() => setSelectedStep(selectedStep === step.id ? null : step.id)}
            >
              <div className="flex items-start">
                <div className="flex-shrink-0 mr-3">
                  {getStepIcon(step.type)}
                </div>
                <div className="flex-1">
                  <h4 className="font-medium text-sm">{step.title}</h4>
                  <p className="text-xs mt-1 opacity-80">{step.description}</p>
                  
                  {step.condition && (
                    <div className="mt-2 text-xs bg-white bg-opacity-50 px-2 py-1 rounded">
                      Condition: {step.condition}
                    </div>
                  )}
                </div>
              </div>
              
              {selectedStep === step.id && (
                <div className="mt-3 pt-3 border-t border-current border-opacity-20">
                  <div className="text-xs space-y-1">
                    <div><strong>Type:</strong> {step.type}</div>
                    {step.status && (
                      <div><strong>Status:</strong> {step.status}</div>
                    )}
                    {step.next && (
                      <div><strong>Next Steps:</strong> {step.next.join(', ')}</div>
                    )}
                  </div>
                </div>
              )}
            </div>
            
            {index < steps.length - 1 && (
              <div className="flex justify-center my-2">
                <ArrowRight className="h-4 w-4 text-gray-400" />
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

export default function InventoryService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-green-100 rounded-lg mr-4">
            <Package className="h-8 w-8 text-green-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Inventory Service</h1>
            <p className="text-lg text-gray-600">Stock Management & Tracking</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8084</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>PostgreSQL + Redis</span>
          </div>
          <div className="flex items-center">
            <Warehouse className="h-4 w-4 mr-1" />
            <span>Real-time Tracking</span>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
          {[
            { key: 'overview', label: 'Overview' },
            { key: 'architecture', label: 'Architecture' },
            { key: 'flows', label: 'Inventory Flows' },
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
                    <Package className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>Real-time stock level tracking and updates</span>
                  </li>
                  <li className="flex items-start">
                    <RefreshCw className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>Inventory reservation and release management</span>
                  </li>
                  <li className="flex items-start">
                    <AlertTriangle className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>Low stock alerts and reorder notifications</span>
                  </li>
                  <li className="flex items-start">
                    <BarChart3 className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>Inventory analytics and reporting</span>
                  </li>
                  <li className="flex items-start">
                    <Warehouse className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>Multi-warehouse inventory management</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• Concurrent stock updates with row-level locking</li>
                  <li>• Automatic inventory reservation for orders</li>
                  <li>• Real-time stock level synchronization</li>
                  <li>• Batch inventory import/export</li>
                  <li>• Historical stock movement tracking</li>
                  <li>• Supplier integration for auto-reordering</li>
                  <li>• Multi-location inventory support</li>
                  <li>• Inventory forecasting and analytics</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Inventory Operations */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Inventory Operations</h2>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {[
                { name: 'Stock In', color: 'bg-green-100 text-green-800', icon: TrendingUp },
                { name: 'Stock Out', color: 'bg-red-100 text-red-800', icon: TrendingDown },
                { name: 'Reservations', color: 'bg-blue-100 text-blue-800', icon: Clock },
                { name: 'Transfers', color: 'bg-purple-100 text-purple-800', icon: Truck },
                { name: 'Adjustments', color: 'bg-yellow-100 text-yellow-800', icon: Settings },
                { name: 'Audits', color: 'bg-indigo-100 text-indigo-800', icon: Search },
                { name: 'Reorders', color: 'bg-orange-100 text-orange-800', icon: RefreshCw },
                { name: 'Reports', color: 'bg-pink-100 text-pink-800', icon: BarChart3 }
              ].map(operation => {
                const Icon = operation.icon
                return (
                  <div key={operation.name} className={`p-4 rounded-lg ${operation.color} text-center`}>
                    <Icon className="h-6 w-6 mx-auto mb-2" />
                    <div className="font-medium text-sm">{operation.name}</div>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Stock Status Types */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Stock Status Types</h2>
            <div className="card">
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
                {[
                  { status: 'IN_STOCK', color: 'bg-green-100 text-green-800', icon: CheckCircle },
                  { status: 'LOW_STOCK', color: 'bg-yellow-100 text-yellow-800', icon: AlertTriangle },
                  { status: 'OUT_OF_STOCK', color: 'bg-red-100 text-red-800', icon: XCircle },
                  { status: 'RESERVED', color: 'bg-blue-100 text-blue-800', icon: Clock },
                  { status: 'DISCONTINUED', color: 'bg-gray-100 text-gray-800', icon: XCircle }
                ].map(state => {
                  const Icon = state.icon
                  return (
                    <div key={state.status} className={`p-4 rounded-lg ${state.color} text-center`}>
                      <Icon className="h-6 w-6 mx-auto mb-2" />
                      <div className="font-medium text-sm">{state.status.replace('_', ' ')}</div>
                    </div>
                  )
                })}
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
                  <li>• Spring Boot 3.0</li>
                  <li>• Spring Data JPA</li>
                  <li>• Spring Security</li>
                  <li>• Maven</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Database & Cache</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• PostgreSQL 15</li>
                  <li>• Redis (Caching)</li>
                  <li>• Connection Pooling</li>
                  <li>• Database Migrations</li>
                  <li>• Read Replicas</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Integration</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Apache Kafka</li>
                  <li>• REST APIs</li>
                  <li>• WebSocket (Real-time)</li>
                  <li>• Supplier APIs</li>
                  <li>• Warehouse Systems</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Performance Metrics */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Performance Metrics</h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="card text-center">
                <div className="text-2xl font-bold text-green-600 mb-2">99.9%</div>
                <div className="text-sm text-gray-600">Availability</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-blue-600 mb-2">&lt; 50ms</div>
                <div className="text-sm text-gray-600">Response Time</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-purple-600 mb-2">10K+</div>
                <div className="text-sm text-gray-600">Products Tracked</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-orange-600 mb-2">1M+</div>
                <div className="text-sm text-gray-600">Daily Transactions</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'architecture' && (
        <div className="space-y-12">
          {/* Component Architecture */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Component Architecture</h2>
            <div className="card">
              <div className="diagram-container">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                  {/* Controller Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Controller Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">InventoryController</div>
                        <div className="text-xs text-blue-700 mt-1">Stock Operations</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">ReservationController</div>
                        <div className="text-xs text-blue-700 mt-1">Reservations</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">ReportController</div>
                        <div className="text-xs text-blue-700 mt-1">Analytics</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Service Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Service Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">InventoryService</div>
                        <div className="text-xs text-green-700 mt-1">Business Logic</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">ReservationService</div>
                        <div className="text-xs text-green-700 mt-1">Reservation Logic</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">AlertService</div>
                        <div className="text-xs text-green-700 mt-1">Stock Alerts</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">ReportService</div>
                        <div className="text-xs text-green-700 mt-1">Analytics</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Repository Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Repository Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">InventoryRepository</div>
                        <div className="text-xs text-purple-700 mt-1">Data Access</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">ReservationRepository</div>
                        <div className="text-xs text-purple-700 mt-1">Reservation Data</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">MovementRepository</div>
                        <div className="text-xs text-purple-700 mt-1">Stock Movements</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">CacheService</div>
                        <div className="text-xs text-purple-700 mt-1">Redis Cache</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* External Systems */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">External Systems</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-orange-100 rounded-lg text-center">
                        <div className="font-medium text-orange-900">PostgreSQL</div>
                        <div className="text-xs text-orange-700 mt-1">Primary Database</div>
                      </div>
                      <div className="p-3 bg-orange-100 rounded-lg text-center">
                        <div className="font-medium text-orange-900">Redis</div>
                        <div className="text-xs text-orange-700 mt-1">Cache Layer</div>
                      </div>
                      <div className="p-3 bg-orange-100 rounded-lg text-center">
                        <div className="font-medium text-orange-900">Kafka</div>
                        <div className="text-xs text-orange-700 mt-1">Event Streaming</div>
                      </div>
                      <div className="p-3 bg-orange-100 rounded-lg text-center">
                        <div className="font-medium text-orange-900">Supplier APIs</div>
                        <div className="text-xs text-orange-700 mt-1">External Data</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Database Schema */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Database Schema</h2>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Inventory Table */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Inventory Table</h3>
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Field</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Description</th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Primary Key</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">product_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Product Identifier</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">warehouse_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Warehouse Location</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">quantity</td>
                        <td className="px-4 py-2 text-sm text-gray-500">INTEGER</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Available Stock</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">reserved</td>
                        <td className="px-4 py-2 text-sm text-gray-500">INTEGER</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Reserved Stock</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">min_threshold</td>
                        <td className="px-4 py-2 text-sm text-gray-500">INTEGER</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Reorder Level</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">updated_at</td>
                        <td className="px-4 py-2 text-sm text-gray-500">TIMESTAMP</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Last Update</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              
              {/* Reservations Table */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Reservations Table</h3>
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Field</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Description</th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Primary Key</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">order_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Order Reference</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">product_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Product ID</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">quantity</td>
                        <td className="px-4 py-2 text-sm text-gray-500">INTEGER</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Reserved Quantity</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">status</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Reservation Status</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">expires_at</td>
                        <td className="px-4 py-2 text-sm text-gray-500">TIMESTAMP</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Expiry Time</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">created_at</td>
                        <td className="px-4 py-2 text-sm text-gray-500">TIMESTAMP</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Creation Time</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'flows' && (
        <div className="space-y-12">
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Inventory Flow Diagrams</h2>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <FlowDiagram steps={stockUpdateFlow} title="Stock Update Flow" />
              <FlowDiagram steps={reservationFlow} title="Inventory Reservation Flow" />
            </div>
          </div>
        </div>
      )}

      {activeTab === 'api' && (
        <div className="space-y-12">
          {/* API Endpoints */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">API Reference</h2>
            
            <div className="space-y-6">
              {/* Inventory Management Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Inventory Management Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-blue-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/inventory/{'{'}productId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get current stock level for a product</p>
                  </div>
                  
                  <div className="border-l-4 border-green-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-green-100 text-green-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/inventory/update</code>
                    </div>
                    <p className="text-sm text-gray-600">Update stock levels (stock in/out)</p>
                  </div>
                  
                  <div className="border-l-4 border-yellow-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-yellow-100 text-yellow-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/inventory/bulk-update</code>
                    </div>
                    <p className="text-sm text-gray-600">Bulk update multiple products</p>
                  </div>
                  
                  <div className="border-l-4 border-purple-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-purple-100 text-purple-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/inventory/low-stock</code>
                    </div>
                    <p className="text-sm text-gray-600">Get products with low stock levels</p>
                  </div>
                </div>
              </div>
              
              {/* Reservation Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Reservation Management Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-indigo-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-indigo-100 text-indigo-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/reservations/create</code>
                    </div>
                    <p className="text-sm text-gray-600">Create inventory reservation for order</p>
                  </div>
                  
                  <div className="border-l-4 border-green-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-green-100 text-green-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/reservations/{'{'}id{'}'}/confirm</code>
                    </div>
                    <p className="text-sm text-gray-600">Confirm reservation (convert to sale)</p>
                  </div>
                  
                  <div className="border-l-4 border-red-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-red-100 text-red-800 text-xs font-medium px-2 py-1 rounded mr-2">DELETE</span>
                      <code className="text-sm font-mono">/api/reservations/{'{'}id{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Cancel reservation and release stock</p>
                  </div>
                  
                  <div className="border-l-4 border-orange-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-orange-100 text-orange-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/reservations/order/{'{'}orderId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get reservations for specific order</p>
                  </div>
                </div>
              </div>
              
              {/* Analytics Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Analytics & Reporting Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-teal-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-teal-100 text-teal-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/reports/stock-levels</code>
                    </div>
                    <p className="text-sm text-gray-600">Current stock levels report</p>
                  </div>
                  
                  <div className="border-l-4 border-pink-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-pink-100 text-pink-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/reports/movements</code>
                    </div>
                    <p className="text-sm text-gray-600">Stock movement history</p>
                  </div>
                  
                  <div className="border-l-4 border-cyan-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-cyan-100 text-cyan-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/reports/turnover</code>
                    </div>
                    <p className="text-sm text-gray-600">Inventory turnover analytics</p>
                  </div>
                  
                  <div className="border-l-4 border-lime-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-lime-100 text-lime-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/reports/forecast</code>
                    </div>
                    <p className="text-sm text-gray-600">Demand forecasting data</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}