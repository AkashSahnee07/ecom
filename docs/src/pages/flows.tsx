import React, { useState } from 'react'
import { 
  ArrowRight, 
  ShoppingCart, 
  CreditCard, 
  User, 
  Package, 
  Bell, 
  Activity, 
  CheckCircle, 
  XCircle,
  AlertTriangle,
  Clock,
  Database,
  Send,
  RefreshCw,
  Settings,
  Zap,
  Filter
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  service: string
  type: 'start' | 'process' | 'decision' | 'end' | 'event' | 'external'
  next?: string[]
  condition?: string
  status?: 'success' | 'error' | 'warning' | 'info'
}

const completeOrderFlow: FlowStep[] = [
  {
    id: 'user-browse',
    title: 'User Browses Products',
    description: 'Customer views product catalog',
    service: 'Product Service',
    type: 'start',
    next: ['add-to-cart']
  },
  {
    id: 'add-to-cart',
    title: 'Add to Cart',
    description: 'User adds items to shopping cart',
    service: 'Product Service',
    type: 'process',
    next: ['proceed-checkout']
  },
  {
    id: 'proceed-checkout',
    title: 'Proceed to Checkout',
    description: 'User initiates checkout process',
    service: 'Order Service',
    type: 'process',
    next: ['check-inventory']
  },
  {
    id: 'check-inventory',
    title: 'Check Inventory',
    description: 'Verify product availability',
    service: 'Inventory Service',
    type: 'process',
    next: ['inventory-available']
  },
  {
    id: 'inventory-available',
    title: 'Inventory Available?',
    description: 'Check if all items are in stock',
    service: 'Inventory Service',
    type: 'decision',
    next: ['reserve-inventory', 'out-of-stock'],
    condition: 'Sufficient stock available'
  },
  {
    id: 'out-of-stock',
    title: 'Out of Stock',
    description: 'Some items are not available',
    service: 'Inventory Service',
    type: 'end',
    status: 'error'
  },
  {
    id: 'reserve-inventory',
    title: 'Reserve Inventory',
    description: 'Hold items for 15 minutes',
    service: 'Inventory Service',
    type: 'process',
    next: ['create-order']
  },
  {
    id: 'create-order',
    title: 'Create Order',
    description: 'Generate order record',
    service: 'Order Service',
    type: 'process',
    next: ['process-payment']
  },
  {
    id: 'process-payment',
    title: 'Process Payment',
    description: 'Charge customer payment method',
    service: 'Payment Service',
    type: 'external',
    next: ['payment-successful']
  },
  {
    id: 'payment-successful',
    title: 'Payment Successful?',
    description: 'Check payment processing result',
    service: 'Payment Service',
    type: 'decision',
    next: ['confirm-order', 'payment-failed'],
    condition: 'Payment approved'
  },
  {
    id: 'payment-failed',
    title: 'Payment Failed',
    description: 'Payment was declined or failed',
    service: 'Payment Service',
    type: 'process',
    next: ['release-inventory'],
    status: 'error'
  },
  {
    id: 'release-inventory',
    title: 'Release Inventory',
    description: 'Return reserved items to stock',
    service: 'Inventory Service',
    type: 'process',
    next: ['order-failed']
  },
  {
    id: 'order-failed',
    title: 'Order Failed',
    description: 'Order cancelled due to payment failure',
    service: 'Order Service',
    type: 'end',
    status: 'error'
  },
  {
    id: 'confirm-order',
    title: 'Confirm Order',
    description: 'Mark order as confirmed',
    service: 'Order Service',
    type: 'process',
    next: ['update-inventory']
  },
  {
    id: 'update-inventory',
    title: 'Update Inventory',
    description: 'Reduce stock levels permanently',
    service: 'Inventory Service',
    type: 'process',
    next: ['send-confirmation']
  },
  {
    id: 'send-confirmation',
    title: 'Send Order Confirmation',
    description: 'Email confirmation to customer',
    service: 'Notification Service',
    type: 'event',
    next: ['order-complete']
  },
  {
    id: 'order-complete',
    title: 'Order Complete',
    description: 'Order successfully processed',
    service: 'Order Service',
    type: 'end',
    status: 'success'
  }
]

const userRegistrationFlow: FlowStep[] = [
  {
    id: 'registration-start',
    title: 'User Registration',
    description: 'User submits registration form',
    service: 'User Service',
    type: 'start',
    next: ['validate-input']
  },
  {
    id: 'validate-input',
    title: 'Validate Input',
    description: 'Check email format and password strength',
    service: 'User Service',
    type: 'process',
    next: ['input-valid']
  },
  {
    id: 'input-valid',
    title: 'Input Valid?',
    description: 'Verify all fields meet requirements',
    service: 'User Service',
    type: 'decision',
    next: ['check-existing', 'validation-error'],
    condition: 'Valid email and strong password'
  },
  {
    id: 'validation-error',
    title: 'Validation Error',
    description: 'Return validation error to user',
    service: 'User Service',
    type: 'end',
    status: 'error'
  },
  {
    id: 'check-existing',
    title: 'Check Existing User',
    description: 'Verify email is not already registered',
    service: 'User Service',
    type: 'process',
    next: ['user-exists']
  },
  {
    id: 'user-exists',
    title: 'User Already Exists?',
    description: 'Check if email is already in use',
    service: 'User Service',
    type: 'decision',
    next: ['email-taken', 'hash-password'],
    condition: 'Email not in database'
  },
  {
    id: 'email-taken',
    title: 'Email Already Taken',
    description: 'Email address is already registered',
    service: 'User Service',
    type: 'end',
    status: 'error'
  },
  {
    id: 'hash-password',
    title: 'Hash Password',
    description: 'Encrypt password using bcrypt',
    service: 'User Service',
    type: 'process',
    next: ['create-user']
  },
  {
    id: 'create-user',
    title: 'Create User Record',
    description: 'Save user to database',
    service: 'User Service',
    type: 'process',
    next: ['generate-token']
  },
  {
    id: 'generate-token',
    title: 'Generate Verification Token',
    description: 'Create email verification token',
    service: 'User Service',
    type: 'process',
    next: ['send-verification']
  },
  {
    id: 'send-verification',
    title: 'Send Verification Email',
    description: 'Email verification link to user',
    service: 'Notification Service',
    type: 'event',
    next: ['registration-complete']
  },
  {
    id: 'registration-complete',
    title: 'Registration Complete',
    description: 'User account created successfully',
    service: 'User Service',
    type: 'end',
    status: 'success'
  }
]

const paymentProcessingFlow: FlowStep[] = [
  {
    id: 'payment-request',
    title: 'Payment Request',
    description: 'Receive payment processing request',
    service: 'Payment Service',
    type: 'start',
    next: ['validate-payment']
  },
  {
    id: 'validate-payment',
    title: 'Validate Payment Data',
    description: 'Check card details and amount',
    service: 'Payment Service',
    type: 'process',
    next: ['payment-valid']
  },
  {
    id: 'payment-valid',
    title: 'Payment Data Valid?',
    description: 'Verify all payment information',
    service: 'Payment Service',
    type: 'decision',
    next: ['fraud-check', 'payment-invalid'],
    condition: 'Valid card and amount'
  },
  {
    id: 'payment-invalid',
    title: 'Invalid Payment Data',
    description: 'Payment information is incorrect',
    service: 'Payment Service',
    type: 'end',
    status: 'error'
  },
  {
    id: 'fraud-check',
    title: 'Fraud Detection',
    description: 'Run fraud detection algorithms',
    service: 'Payment Service',
    type: 'process',
    next: ['fraud-detected']
  },
  {
    id: 'fraud-detected',
    title: 'Fraud Detected?',
    description: 'Check if transaction is suspicious',
    service: 'Payment Service',
    type: 'decision',
    next: ['process-with-gateway', 'fraud-alert'],
    condition: 'Low fraud risk score'
  },
  {
    id: 'fraud-alert',
    title: 'Fraud Alert',
    description: 'Transaction flagged as suspicious',
    service: 'Payment Service',
    type: 'end',
    status: 'error'
  },
  {
    id: 'process-with-gateway',
    title: 'Process with Gateway',
    description: 'Send to payment gateway (Stripe)',
    service: 'Payment Service',
    type: 'external',
    next: ['gateway-response']
  },
  {
    id: 'gateway-response',
    title: 'Gateway Response',
    description: 'Receive response from payment gateway',
    service: 'Payment Service',
    type: 'process',
    next: ['payment-approved']
  },
  {
    id: 'payment-approved',
    title: 'Payment Approved?',
    description: 'Check gateway approval status',
    service: 'Payment Service',
    type: 'decision',
    next: ['record-transaction', 'payment-declined'],
    condition: 'Gateway approved transaction'
  },
  {
    id: 'payment-declined',
    title: 'Payment Declined',
    description: 'Payment was declined by gateway',
    service: 'Payment Service',
    type: 'end',
    status: 'error'
  },
  {
    id: 'record-transaction',
    title: 'Record Transaction',
    description: 'Save successful payment record',
    service: 'Payment Service',
    type: 'process',
    next: ['send-receipt']
  },
  {
    id: 'send-receipt',
    title: 'Send Payment Receipt',
    description: 'Email receipt to customer',
    service: 'Notification Service',
    type: 'event',
    next: ['payment-complete']
  },
  {
    id: 'payment-complete',
    title: 'Payment Complete',
    description: 'Payment successfully processed',
    service: 'Payment Service',
    type: 'end',
    status: 'success'
  }
]

const FlowDiagram: React.FC<{ steps: FlowStep[]; title: string }> = ({ steps, title }) => {
  const [selectedStep, setSelectedStep] = useState<string | null>(null)
  const [filterService, setFilterService] = useState<string>('all')
  
  const services = Array.from(new Set(steps.map(step => step.service)))
  const filteredSteps = filterService === 'all' ? steps : steps.filter(step => step.service === filterService)
  
  const getStepIcon = (type: string) => {
    switch (type) {
      case 'start': return <Activity className="h-4 w-4" />
      case 'process': return <Settings className="h-4 w-4" />
      case 'decision': return <CheckCircle className="h-4 w-4" />
      case 'event': return <Bell className="h-4 w-4" />
      case 'external': return <Send className="h-4 w-4" />
      case 'end': return <CheckCircle className="h-4 w-4" />
      default: return <Activity className="h-4 w-4" />
    }
  }
  
  const getServiceColor = (service: string) => {
    const colors: { [key: string]: string } = {
      'User Service': 'bg-blue-100 text-blue-700 border-blue-300',
      'Product Service': 'bg-green-100 text-green-700 border-green-300',
      'Order Service': 'bg-purple-100 text-purple-700 border-purple-300',
      'Payment Service': 'bg-orange-100 text-orange-700 border-orange-300',
      'Inventory Service': 'bg-teal-100 text-teal-700 border-teal-300',
      'Notification Service': 'bg-pink-100 text-pink-700 border-pink-300'
    }
    return colors[service] || 'bg-gray-100 text-gray-700 border-gray-300'
  }
  
  const getStepColor = (service: string, status?: string) => {
    if (status === 'error') return 'bg-red-100 text-red-700 border-red-300'
    if (status === 'warning') return 'bg-yellow-100 text-yellow-700 border-yellow-300'
    if (status === 'success') return 'bg-green-100 text-green-700 border-green-300'
    if (status === 'info') return 'bg-blue-100 text-blue-700 border-blue-300'
    
    return getServiceColor(service)
  }
  
  return (
    <div className="card">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
        
        <div className="flex items-center space-x-2">
          <Filter className="h-4 w-4 text-gray-500" />
          <select 
            value={filterService} 
            onChange={(e) => setFilterService(e.target.value)}
            className="text-sm border border-gray-300 rounded-md px-2 py-1"
          >
            <option value="all">All Services</option>
            {services.map(service => (
              <option key={service} value={service}>{service}</option>
            ))}
          </select>
        </div>
      </div>
      
      <div className="space-y-4">
        {filteredSteps.map((step, index) => (
          <div key={step.id} className="relative">
            <div 
              className={`
                p-4 rounded-lg border-2 cursor-pointer transition-all duration-200
                ${selectedStep === step.id ? 'ring-2 ring-primary-500' : ''}
                ${getStepColor(step.service, step.status)}
              `}
              onClick={() => setSelectedStep(selectedStep === step.id ? null : step.id)}
            >
              <div className="flex items-start">
                <div className="flex-shrink-0 mr-3">
                  {getStepIcon(step.type)}
                </div>
                <div className="flex-1">
                  <div className="flex items-center justify-between mb-1">
                    <h4 className="font-medium text-sm">{step.title}</h4>
                    <span className="text-xs bg-white bg-opacity-50 px-2 py-1 rounded">
                      {step.service}
                    </span>
                  </div>
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
                    <div><strong>Service:</strong> {step.service}</div>
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
            
            {index < filteredSteps.length - 1 && (
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

export default function Flows() {
  const [activeFlow, setActiveFlow] = useState<'order' | 'user' | 'payment'>('order')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-4">Business Process Flows</h1>
        <p className="text-lg text-gray-600">
          Comprehensive flow diagrams showing how different services interact to complete key business processes.
        </p>
      </div>

      {/* Flow Navigation */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
          {[
            { key: 'order', label: 'Complete Order Flow', icon: ShoppingCart },
            { key: 'user', label: 'User Registration Flow', icon: User },
            { key: 'payment', label: 'Payment Processing Flow', icon: CreditCard }
          ].map(flow => {
            const Icon = flow.icon
            return (
              <button
                key={flow.key}
                onClick={() => setActiveFlow(flow.key as any)}
                className={`flex items-center px-4 py-2 text-sm font-medium rounded-md transition-colors ${
                  activeFlow === flow.key
                    ? 'bg-white text-gray-900 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                <Icon className="h-4 w-4 mr-2" />
                {flow.label}
              </button>
            )
          })}
        </div>
      </div>

      {/* Flow Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <div className="card">
          <div className="flex items-center mb-4">
            <div className="p-2 bg-purple-100 rounded-lg mr-3">
              <ShoppingCart className="h-5 w-5 text-purple-600" />
            </div>
            <h3 className="font-semibold text-gray-900">Complete Order Flow</h3>
          </div>
          <p className="text-sm text-gray-600 mb-4">
            End-to-end order processing from product browsing to order confirmation, 
            involving inventory checks, payment processing, and notifications.
          </p>
          <div className="text-xs text-gray-500">
            <strong>Services:</strong> Product, Order, Inventory, Payment, Notification
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center mb-4">
            <div className="p-2 bg-blue-100 rounded-lg mr-3">
              <User className="h-5 w-5 text-blue-600" />
            </div>
            <h3 className="font-semibold text-gray-900">User Registration Flow</h3>
          </div>
          <p className="text-sm text-gray-600 mb-4">
            User account creation process including validation, password hashing, 
            and email verification with proper error handling.
          </p>
          <div className="text-xs text-gray-500">
            <strong>Services:</strong> User, Notification
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center mb-4">
            <div className="p-2 bg-orange-100 rounded-lg mr-3">
              <CreditCard className="h-5 w-5 text-orange-600" />
            </div>
            <h3 className="font-semibold text-gray-900">Payment Processing Flow</h3>
          </div>
          <p className="text-sm text-gray-600 mb-4">
            Secure payment processing with fraud detection, gateway integration, 
            and transaction recording with comprehensive error handling.
          </p>
          <div className="text-xs text-gray-500">
            <strong>Services:</strong> Payment, Notification
          </div>
        </div>
      </div>

      {/* Service Legend */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Service Color Legend</h2>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          {[
            { name: 'User Service', color: 'bg-blue-100 text-blue-700 border-blue-300' },
            { name: 'Product Service', color: 'bg-green-100 text-green-700 border-green-300' },
            { name: 'Order Service', color: 'bg-purple-100 text-purple-700 border-purple-300' },
            { name: 'Payment Service', color: 'bg-orange-100 text-orange-700 border-orange-300' },
            { name: 'Inventory Service', color: 'bg-teal-100 text-teal-700 border-teal-300' },
            { name: 'Notification Service', color: 'bg-pink-100 text-pink-700 border-pink-300' }
          ].map(service => (
            <div key={service.name} className={`p-3 rounded-lg border-2 ${service.color} text-center`}>
              <div className="font-medium text-sm">{service.name}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Flow Diagrams */}
      <div className="space-y-12">
        {activeFlow === 'order' && (
          <FlowDiagram steps={completeOrderFlow} title="Complete Order Processing Flow" />
        )}
        
        {activeFlow === 'user' && (
          <FlowDiagram steps={userRegistrationFlow} title="User Registration and Verification Flow" />
        )}
        
        {activeFlow === 'payment' && (
          <FlowDiagram steps={paymentProcessingFlow} title="Payment Processing and Fraud Detection Flow" />
        )}
      </div>

      {/* Flow Statistics */}
      <div className="mt-12">
        <h2 className="text-xl font-semibold text-gray-900 mb-6">Flow Statistics</h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="card text-center">
            <div className="text-2xl font-bold text-purple-600 mb-2">{completeOrderFlow.length}</div>
            <div className="text-sm text-gray-600">Order Flow Steps</div>
          </div>
          <div className="card text-center">
            <div className="text-2xl font-bold text-blue-600 mb-2">{userRegistrationFlow.length}</div>
            <div className="text-sm text-gray-600">Registration Steps</div>
          </div>
          <div className="card text-center">
            <div className="text-2xl font-bold text-orange-600 mb-2">{paymentProcessingFlow.length}</div>
            <div className="text-sm text-gray-600">Payment Steps</div>
          </div>
          <div className="card text-center">
            <div className="text-2xl font-bold text-green-600 mb-2">6</div>
            <div className="text-sm text-gray-600">Services Involved</div>
          </div>
        </div>
      </div>
    </div>
  )
}