import React, { useState } from 'react'
import { 
  ShoppingCart, 
  CreditCard, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  XCircle,
  Clock,
  Package,
  Truck,
  AlertCircle,
  DollarSign,
  FileText,
  User,
  Calendar
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end' | 'event'
  next?: string[]
  condition?: string
  status?: 'success' | 'error' | 'warning'
}

const orderCreationFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Order Creation Request',
    description: 'User initiates checkout process',
    type: 'start',
    next: ['validate-cart']
  },
  {
    id: 'validate-cart',
    title: 'Validate Cart Items',
    description: 'Check cart contents and availability',
    type: 'process',
    next: ['cart-valid']
  },
  {
    id: 'cart-valid',
    title: 'Cart Valid?',
    description: 'Verify all items are available',
    type: 'decision',
    next: ['calculate-total', 'cart-error'],
    condition: 'All items available'
  },
  {
    id: 'cart-error',
    title: 'Cart Validation Error',
    description: 'Return unavailable items error',
    type: 'end',
    status: 'error'
  },
  {
    id: 'calculate-total',
    title: 'Calculate Order Total',
    description: 'Calculate subtotal, tax, shipping, discounts',
    type: 'process',
    next: ['reserve-inventory']
  },
  {
    id: 'reserve-inventory',
    title: 'Reserve Inventory',
    description: 'Hold inventory for order items',
    type: 'process',
    next: ['create-order']
  },
  {
    id: 'create-order',
    title: 'Create Order Record',
    description: 'Save order to database with PENDING status',
    type: 'process',
    next: ['publish-order-created']
  },
  {
    id: 'publish-order-created',
    title: 'Publish Order Created Event',
    description: 'Send order created event to Kafka',
    type: 'event',
    next: ['initiate-payment']
  },
  {
    id: 'initiate-payment',
    title: 'Initiate Payment',
    description: 'Call payment service for processing',
    type: 'process',
    next: ['payment-success']
  },
  {
    id: 'payment-success',
    title: 'Payment Successful?',
    description: 'Check payment processing result',
    type: 'decision',
    next: ['confirm-order', 'payment-failed'],
    condition: 'Payment approved'
  },
  {
    id: 'payment-failed',
    title: 'Payment Failed',
    description: 'Release inventory and cancel order',
    type: 'process',
    next: ['order-cancelled'],
    status: 'error'
  },
  {
    id: 'order-cancelled',
    title: 'Order Cancelled',
    description: 'Return payment failed error',
    type: 'end',
    status: 'error'
  },
  {
    id: 'confirm-order',
    title: 'Confirm Order',
    description: 'Update order status to CONFIRMED',
    type: 'process',
    next: ['publish-order-confirmed']
  },
  {
    id: 'publish-order-confirmed',
    title: 'Publish Order Confirmed',
    description: 'Send order confirmed event',
    type: 'event',
    next: ['order-success']
  },
  {
    id: 'order-success',
    title: 'Order Created Successfully',
    description: 'Return order details to client',
    type: 'end',
    status: 'success'
  }
]

const orderFulfillmentFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Order Confirmed Event',
    description: 'Receive order confirmed event from Kafka',
    type: 'start',
    next: ['prepare-shipment']
  },
  {
    id: 'prepare-shipment',
    title: 'Prepare Shipment',
    description: 'Generate picking list and shipping label',
    type: 'process',
    next: ['update-processing']
  },
  {
    id: 'update-processing',
    title: 'Update to Processing',
    description: 'Change order status to PROCESSING',
    type: 'process',
    next: ['notify-warehouse']
  },
  {
    id: 'notify-warehouse',
    title: 'Notify Warehouse',
    description: 'Send fulfillment request to shipping service',
    type: 'event',
    next: ['wait-shipment']
  },
  {
    id: 'wait-shipment',
    title: 'Wait for Shipment',
    description: 'Monitor shipping service for updates',
    type: 'process',
    next: ['shipment-ready']
  },
  {
    id: 'shipment-ready',
    title: 'Shipment Ready?',
    description: 'Check if items are packed and ready',
    type: 'decision',
    next: ['ship-order', 'fulfillment-error'],
    condition: 'Items packed'
  },
  {
    id: 'fulfillment-error',
    title: 'Fulfillment Error',
    description: 'Handle packing or inventory issues',
    type: 'process',
    next: ['notify-customer-delay'],
    status: 'warning'
  },
  {
    id: 'notify-customer-delay',
    title: 'Notify Customer of Delay',
    description: 'Send delay notification',
    type: 'event',
    next: ['wait-shipment']
  },
  {
    id: 'ship-order',
    title: 'Ship Order',
    description: 'Update status to SHIPPED with tracking',
    type: 'process',
    next: ['publish-shipped']
  },
  {
    id: 'publish-shipped',
    title: 'Publish Order Shipped',
    description: 'Send shipped event with tracking info',
    type: 'event',
    next: ['track-delivery']
  },
  {
    id: 'track-delivery',
    title: 'Track Delivery',
    description: 'Monitor delivery status updates',
    type: 'process',
    next: ['delivered']
  },
  {
    id: 'delivered',
    title: 'Order Delivered',
    description: 'Update status to DELIVERED',
    type: 'end',
    status: 'success'
  }
]

const FlowDiagram: React.FC<{ steps: FlowStep[]; title: string }> = ({ steps, title }) => {
  const [selectedStep, setSelectedStep] = useState<string | null>(null)
  
  const getStepIcon = (type: string) => {
    switch (type) {
      case 'start': return <Activity className="h-4 w-4" />
      case 'process': return <Package className="h-4 w-4" />
      case 'decision': return <CheckCircle className="h-4 w-4" />
      case 'event': return <AlertCircle className="h-4 w-4" />
      case 'end': return <CheckCircle className="h-4 w-4" />
      default: return <Activity className="h-4 w-4" />
    }
  }
  
  const getStepColor = (type: string, status?: string) => {
    if (status === 'error') return 'bg-red-100 text-red-700 border-red-300'
    if (status === 'warning') return 'bg-yellow-100 text-yellow-700 border-yellow-300'
    if (status === 'success') return 'bg-green-100 text-green-700 border-green-300'
    
    switch (type) {
      case 'start': return 'bg-blue-100 text-blue-700 border-blue-300'
      case 'process': return 'bg-indigo-100 text-indigo-700 border-indigo-300'
      case 'decision': return 'bg-orange-100 text-orange-700 border-orange-300'
      case 'event': return 'bg-purple-100 text-purple-700 border-purple-300'
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

export default function OrderService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-red-100 rounded-lg mr-4">
            <ShoppingCart className="h-8 w-8 text-red-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Order Service</h1>
            <p className="text-lg text-gray-600">Order Processing and Management</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8084</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>PostgreSQL + Kafka</span>
          </div>
          <div className="flex items-center">
            <CreditCard className="h-4 w-4 mr-1" />
            <span>Payment Integration</span>
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
                    <ShoppingCart className="h-4 w-4 mr-2 mt-0.5 text-red-600" />
                    <span>Order creation and lifecycle management</span>
                  </li>
                  <li className="flex items-start">
                    <CreditCard className="h-4 w-4 mr-2 mt-0.5 text-red-600" />
                    <span>Payment processing coordination</span>
                  </li>
                  <li className="flex items-start">
                    <Package className="h-4 w-4 mr-2 mt-0.5 text-red-600" />
                    <span>Inventory reservation and management</span>
                  </li>
                  <li className="flex items-start">
                    <Truck className="h-4 w-4 mr-2 mt-0.5 text-red-600" />
                    <span>Fulfillment and shipping coordination</span>
                  </li>
                  <li className="flex items-start">
                    <FileText className="h-4 w-4 mr-2 mt-0.5 text-red-600" />
                    <span>Order history and tracking</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• Multi-step order processing workflow</li>
                  <li>• Real-time inventory validation</li>
                  <li>• Automated payment processing</li>
                  <li>• Order status tracking and updates</li>
                  <li>• Event-driven architecture with Kafka</li>
                  <li>• Compensation patterns for failures</li>
                  <li>• Order cancellation and refunds</li>
                  <li>• Integration with shipping providers</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Order States */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Order State Machine</h2>
            <div className="card">
              <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                {[
                  { status: 'PENDING', color: 'bg-yellow-100 text-yellow-800', icon: Clock },
                  { status: 'CONFIRMED', color: 'bg-blue-100 text-blue-800', icon: CheckCircle },
                  { status: 'PROCESSING', color: 'bg-indigo-100 text-indigo-800', icon: Package },
                  { status: 'SHIPPED', color: 'bg-purple-100 text-purple-800', icon: Truck },
                  { status: 'DELIVERED', color: 'bg-green-100 text-green-800', icon: CheckCircle },
                  { status: 'CANCELLED', color: 'bg-red-100 text-red-800', icon: XCircle }
                ].map(state => {
                  const Icon = state.icon
                  return (
                    <div key={state.status} className={`p-4 rounded-lg ${state.color} text-center`}>
                      <Icon className="h-6 w-6 mx-auto mb-2" />
                      <div className="font-medium text-sm">{state.status}</div>
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
                  <li>• Spring Boot 3.x</li>
                  <li>• Spring Data JPA</li>
                  <li>• Spring State Machine</li>
                  <li>• Spring Kafka</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Database & Messaging</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• PostgreSQL (Primary)</li>
                  <li>• Apache Kafka (Events)</li>
                  <li>• Redis (Caching)</li>
                  <li>• JPA/Hibernate ORM</li>
                  <li>• Database Transactions</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Integration</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Payment Service API</li>
                  <li>• Shipping Service API</li>
                  <li>• Inventory Service API</li>
                  <li>• Notification Service</li>
                  <li>• External Payment Gateways</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Performance Metrics */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Performance Metrics</h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="card text-center">
                <div className="text-2xl font-bold text-red-600 mb-2">< 2s</div>
                <div className="text-sm text-gray-600">Order Creation Time</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-green-600 mb-2">99.5%</div>
                <div className="text-sm text-gray-600">Order Success Rate</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-blue-600 mb-2">10K+</div>
                <div className="text-sm text-gray-600">Orders per Day</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-purple-600 mb-2">< 5min</div>
                <div className="text-sm text-gray-600">Average Processing</div>
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
                        <div className="font-medium text-blue-900">OrderController</div>
                        <div className="text-xs text-blue-700 mt-1">Order Operations</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">CheckoutController</div>
                        <div className="text-xs text-blue-700 mt-1">Checkout Process</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Service Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Service Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">OrderService</div>
                        <div className="text-xs text-green-700 mt-1">Order Logic</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">PaymentService</div>
                        <div className="text-xs text-green-700 mt-1">Payment Integration</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">InventoryService</div>
                        <div className="text-xs text-green-700 mt-1">Stock Management</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">EventService</div>
                        <div className="text-xs text-green-700 mt-1">Event Publishing</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Repository Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Repository Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">OrderRepository</div>
                        <div className="text-xs text-purple-700 mt-1">Order Data</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">OrderItemRepository</div>
                        <div className="text-xs text-purple-700 mt-1">Order Items</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Data Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Data Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-indigo-100 rounded-lg text-center">
                        <div className="font-medium text-indigo-900">PostgreSQL</div>
                        <div className="text-xs text-indigo-700 mt-1">Primary Database</div>
                      </div>
                      <div className="p-3 bg-gray-100 rounded-lg text-center">
                        <div className="font-medium text-gray-900">Kafka</div>
                        <div className="text-xs text-gray-700 mt-1">Event Streaming</div>
                      </div>
                      <div className="p-3 bg-red-100 rounded-lg text-center">
                        <div className="font-medium text-red-900">Redis</div>
                        <div className="text-xs text-red-700 mt-1">Cache Layer</div>
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
              {/* Orders Table */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Orders Table</h3>
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Field</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Constraints</th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">PRIMARY KEY</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">user_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">status</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR(50)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">total_amount</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DECIMAL(10,2)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">shipping_address</td>
                        <td className="px-4 py-2 text-sm text-gray-500">JSONB</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">payment_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR(255)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">-</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">created_at</td>
                        <td className="px-4 py-2 text-sm text-gray-500">TIMESTAMP</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DEFAULT NOW()</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              
              {/* Order Items Table */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Order Items Table</h3>
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Field</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Constraints</th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">PRIMARY KEY</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">order_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">FOREIGN KEY</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">product_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">quantity</td>
                        <td className="px-4 py-2 text-sm text-gray-500">INT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">unit_price</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DECIMAL(10,2)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">total_price</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DECIMAL(10,2)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
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
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Process Flow Diagrams</h2>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <FlowDiagram steps={orderCreationFlow} title="Order Creation Flow" />
              <FlowDiagram steps={orderFulfillmentFlow} title="Order Fulfillment Flow" />
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
              {/* Order Management Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Order Management Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-green-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-green-100 text-green-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/orders</code>
                    </div>
                    <p className="text-sm text-gray-600">Create a new order from cart</p>
                  </div>
                  
                  <div className="border-l-4 border-blue-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/orders/{'{'}id{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get order details by ID</p>
                  </div>
                  
                  <div className="border-l-4 border-yellow-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-yellow-100 text-yellow-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/orders/user/{'{'}userId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get user's order history</p>
                  </div>
                  
                  <div className="border-l-4 border-purple-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-purple-100 text-purple-800 text-xs font-medium px-2 py-1 rounded mr-2">PUT</span>
                      <code className="text-sm font-mono">/api/orders/{'{'}id{'}'}/status</code>
                    </div>
                    <p className="text-sm text-gray-600">Update order status</p>
                  </div>
                  
                  <div className="border-l-4 border-red-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-red-100 text-red-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/orders/{'{'}id{'}'}/cancel</code>
                    </div>
                    <p className="text-sm text-gray-600">Cancel an order</p>
                  </div>
                </div>
              </div>
              
              {/* Checkout Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Checkout Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-indigo-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-indigo-100 text-indigo-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/checkout/validate</code>
                    </div>
                    <p className="text-sm text-gray-600">Validate cart before checkout</p>
                  </div>
                  
                  <div className="border-l-4 border-teal-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-teal-100 text-teal-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/checkout/calculate</code>
                    </div>
                    <p className="text-sm text-gray-600">Calculate order totals and taxes</p>
                  </div>
                  
                  <div className="border-l-4 border-pink-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-pink-100 text-pink-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/checkout/process</code>
                    </div>
                    <p className="text-sm text-gray-600">Process checkout and create order</p>
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