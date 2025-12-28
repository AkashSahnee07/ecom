import React, { useState } from 'react'
import { 
  ArrowRight, 
  ArrowDown, 
  MessageSquare, 
  Clock, 
  CheckCircle, 
  XCircle, 
  AlertTriangle, 
  Database, 
  Send, 
  RefreshCw, 
  Zap,
  Filter,
  Play,
  Pause,
  RotateCcw
} from 'lucide-react'

interface SequenceMessage {
  id: string
  from: string
  to: string
  message: string
  type: 'request' | 'response' | 'event' | 'error' | 'async'
  timestamp: number
  details?: string
  status?: 'success' | 'error' | 'warning' | 'info'
}

interface SequenceActor {
  id: string
  name: string
  type: 'service' | 'external' | 'database' | 'user'
  color: string
}

const orderSequenceActors: SequenceActor[] = [
  { id: 'user', name: 'User', type: 'user', color: 'bg-gray-100' },
  { id: 'order', name: 'Order Service', type: 'service', color: 'bg-purple-100' },
  { id: 'inventory', name: 'Inventory Service', type: 'service', color: 'bg-teal-100' },
  { id: 'payment', name: 'Payment Service', type: 'service', color: 'bg-orange-100' },
  { id: 'notification', name: 'Notification Service', type: 'service', color: 'bg-pink-100' },
  { id: 'db', name: 'Database', type: 'database', color: 'bg-blue-100' }
]

const orderSequenceMessages: SequenceMessage[] = [
  {
    id: '1',
    from: 'user',
    to: 'order',
    message: 'POST /orders',
    type: 'request',
    timestamp: 1,
    details: 'Create new order with items: [product1, product2]'
  },
  {
    id: '2',
    from: 'order',
    to: 'inventory',
    message: 'POST /inventory/check',
    type: 'request',
    timestamp: 2,
    details: 'Check availability for products'
  },
  {
    id: '3',
    from: 'inventory',
    to: 'db',
    message: 'SELECT stock FROM products',
    type: 'request',
    timestamp: 3,
    details: 'Query current stock levels'
  },
  {
    id: '4',
    from: 'db',
    to: 'inventory',
    message: 'Stock data',
    type: 'response',
    timestamp: 4,
    status: 'success'
  },
  {
    id: '5',
    from: 'inventory',
    to: 'order',
    message: 'Availability confirmed',
    type: 'response',
    timestamp: 5,
    status: 'success',
    details: 'All items available'
  },
  {
    id: '6',
    from: 'order',
    to: 'inventory',
    message: 'POST /inventory/reserve',
    type: 'request',
    timestamp: 6,
    details: 'Reserve items for 15 minutes'
  },
  {
    id: '7',
    from: 'inventory',
    to: 'db',
    message: 'UPDATE reservations',
    type: 'request',
    timestamp: 7,
    details: 'Create inventory reservations'
  },
  {
    id: '8',
    from: 'db',
    to: 'inventory',
    message: 'Reservation created',
    type: 'response',
    timestamp: 8,
    status: 'success'
  },
  {
    id: '9',
    from: 'inventory',
    to: 'order',
    message: 'Items reserved',
    type: 'response',
    timestamp: 9,
    status: 'success'
  },
  {
    id: '10',
    from: 'order',
    to: 'db',
    message: 'INSERT order',
    type: 'request',
    timestamp: 10,
    details: 'Create order record'
  },
  {
    id: '11',
    from: 'db',
    to: 'order',
    message: 'Order created',
    type: 'response',
    timestamp: 11,
    status: 'success'
  },
  {
    id: '12',
    from: 'order',
    to: 'payment',
    message: 'POST /payments/process',
    type: 'request',
    timestamp: 12,
    details: 'Process payment for $99.99'
  },
  {
    id: '13',
    from: 'payment',
    to: 'db',
    message: 'INSERT payment_attempt',
    type: 'request',
    timestamp: 13,
    details: 'Log payment attempt'
  },
  {
    id: '14',
    from: 'db',
    to: 'payment',
    message: 'Payment logged',
    type: 'response',
    timestamp: 14,
    status: 'success'
  },
  {
    id: '15',
    from: 'payment',
    to: 'order',
    message: 'Payment successful',
    type: 'response',
    timestamp: 15,
    status: 'success',
    details: 'Transaction ID: txn_123456'
  },
  {
    id: '16',
    from: 'order',
    to: 'inventory',
    message: 'POST /inventory/confirm',
    type: 'request',
    timestamp: 16,
    details: 'Confirm inventory usage'
  },
  {
    id: '17',
    from: 'inventory',
    to: 'db',
    message: 'UPDATE stock levels',
    type: 'request',
    timestamp: 17,
    details: 'Reduce stock permanently'
  },
  {
    id: '18',
    from: 'db',
    to: 'inventory',
    message: 'Stock updated',
    type: 'response',
    timestamp: 18,
    status: 'success'
  },
  {
    id: '19',
    from: 'inventory',
    to: 'order',
    message: 'Inventory confirmed',
    type: 'response',
    timestamp: 19,
    status: 'success'
  },
  {
    id: '20',
    from: 'order',
    to: 'notification',
    message: 'POST /notifications/send',
    type: 'async',
    timestamp: 20,
    details: 'Send order confirmation email'
  },
  {
    id: '21',
    from: 'order',
    to: 'user',
    message: 'Order confirmed',
    type: 'response',
    timestamp: 21,
    status: 'success',
    details: 'Order ID: ORD-123456'
  }
]

const userAuthSequenceActors: SequenceActor[] = [
  { id: 'client', name: 'Client App', type: 'user', color: 'bg-gray-100' },
  { id: 'user', name: 'User Service', type: 'service', color: 'bg-blue-100' },
  { id: 'auth', name: 'Auth Provider', type: 'external', color: 'bg-green-100' },
  { id: 'db', name: 'Database', type: 'database', color: 'bg-blue-100' }
]

const userAuthSequenceMessages: SequenceMessage[] = [
  {
    id: '1',
    from: 'client',
    to: 'user',
    message: 'POST /auth/login',
    type: 'request',
    timestamp: 1,
    details: 'Login with email and password'
  },
  {
    id: '2',
    from: 'user',
    to: 'db',
    message: 'SELECT user BY email',
    type: 'request',
    timestamp: 2,
    details: 'Find user by email address'
  },
  {
    id: '3',
    from: 'db',
    to: 'user',
    message: 'User data',
    type: 'response',
    timestamp: 3,
    status: 'success'
  },
  {
    id: '4',
    from: 'user',
    to: 'user',
    message: 'Verify password',
    type: 'request',
    timestamp: 4,
    details: 'Compare hashed passwords'
  },
  {
    id: '5',
    from: 'user',
    to: 'auth',
    message: 'Generate JWT token',
    type: 'request',
    timestamp: 5,
    details: 'Create authentication token'
  },
  {
    id: '6',
    from: 'auth',
    to: 'user',
    message: 'JWT token',
    type: 'response',
    timestamp: 6,
    status: 'success'
  },
  {
    id: '7',
    from: 'user',
    to: 'db',
    message: 'UPDATE last_login',
    type: 'request',
    timestamp: 7,
    details: 'Update user login timestamp'
  },
  {
    id: '8',
    from: 'db',
    to: 'user',
    message: 'Login updated',
    type: 'response',
    timestamp: 8,
    status: 'success'
  },
  {
    id: '9',
    from: 'user',
    to: 'client',
    message: 'Authentication successful',
    type: 'response',
    timestamp: 9,
    status: 'success',
    details: 'Return JWT token and user profile'
  }
]

const paymentFailureSequenceActors: SequenceActor[] = [
  { id: 'order', name: 'Order Service', type: 'service', color: 'bg-purple-100' },
  { id: 'payment', name: 'Payment Service', type: 'service', color: 'bg-orange-100' },
  { id: 'gateway', name: 'Payment Gateway', type: 'external', color: 'bg-red-100' },
  { id: 'inventory', name: 'Inventory Service', type: 'service', color: 'bg-teal-100' },
  { id: 'notification', name: 'Notification Service', type: 'service', color: 'bg-pink-100' }
]

const paymentFailureSequenceMessages: SequenceMessage[] = [
  {
    id: '1',
    from: 'order',
    to: 'payment',
    message: 'POST /payments/process',
    type: 'request',
    timestamp: 1,
    details: 'Process payment for $299.99'
  },
  {
    id: '2',
    from: 'payment',
    to: 'gateway',
    message: 'Charge card',
    type: 'request',
    timestamp: 2,
    details: 'Attempt to charge credit card'
  },
  {
    id: '3',
    from: 'gateway',
    to: 'payment',
    message: 'Payment declined',
    type: 'response',
    timestamp: 3,
    status: 'error',
    details: 'Insufficient funds'
  },
  {
    id: '4',
    from: 'payment',
    to: 'order',
    message: 'Payment failed',
    type: 'response',
    timestamp: 4,
    status: 'error',
    details: 'Card declined by issuer'
  },
  {
    id: '5',
    from: 'order',
    to: 'inventory',
    message: 'POST /inventory/release',
    type: 'request',
    timestamp: 5,
    details: 'Release reserved inventory'
  },
  {
    id: '6',
    from: 'inventory',
    to: 'order',
    message: 'Inventory released',
    type: 'response',
    timestamp: 6,
    status: 'success'
  },
  {
    id: '7',
    from: 'order',
    to: 'notification',
    message: 'POST /notifications/send',
    type: 'async',
    timestamp: 7,
    details: 'Send payment failure notification'
  },
  {
    id: '8',
    from: 'order',
    to: 'order',
    message: 'Cancel order',
    type: 'request',
    timestamp: 8,
    details: 'Mark order as cancelled'
  }
]

const SequenceDiagram: React.FC<{ 
  actors: SequenceActor[]
  messages: SequenceMessage[]
  title: string 
}> = ({ actors, messages, title }) => {
  const [currentStep, setCurrentStep] = useState(0)
  const [isPlaying, setIsPlaying] = useState(false)
  const [selectedMessage, setSelectedMessage] = useState<string | null>(null)
  
  React.useEffect(() => {
    let interval: NodeJS.Timeout
    if (isPlaying && currentStep < messages.length) {
      interval = setInterval(() => {
        setCurrentStep(prev => {
          if (prev >= messages.length - 1) {
            setIsPlaying(false)
            return prev
          }
          return prev + 1
        })
      }, 1500)
    }
    return () => clearInterval(interval)
  }, [isPlaying, currentStep, messages.length])
  
  const getMessageIcon = (type: string) => {
    switch (type) {
      case 'request': return <ArrowRight className="h-3 w-3" />
      case 'response': return <ArrowRight className="h-3 w-3" />
      case 'event': return <Zap className="h-3 w-3" />
      case 'async': return <Send className="h-3 w-3" />
      case 'error': return <XCircle className="h-3 w-3" />
      default: return <MessageSquare className="h-3 w-3" />
    }
  }
  
  const getMessageColor = (type: string, status?: string) => {
    if (status === 'error') return 'text-red-600 bg-red-50 border-red-200'
    if (status === 'success') return 'text-green-600 bg-green-50 border-green-200'
    if (status === 'warning') return 'text-yellow-600 bg-yellow-50 border-yellow-200'
    
    switch (type) {
      case 'request': return 'text-blue-600 bg-blue-50 border-blue-200'
      case 'response': return 'text-purple-600 bg-purple-50 border-purple-200'
      case 'event': return 'text-orange-600 bg-orange-50 border-orange-200'
      case 'async': return 'text-pink-600 bg-pink-50 border-pink-200'
      default: return 'text-gray-600 bg-gray-50 border-gray-200'
    }
  }
  
  const visibleMessages = messages.slice(0, currentStep + 1)
  
  return (
    <div className="card">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
        
        <div className="flex items-center space-x-2">
          <button
            onClick={() => {
              setCurrentStep(0)
              setIsPlaying(false)
            }}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded"
          >
            <RotateCcw className="h-4 w-4" />
          </button>
          <button
            onClick={() => setIsPlaying(!isPlaying)}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded"
          >
            {isPlaying ? <Pause className="h-4 w-4" /> : <Play className="h-4 w-4" />}
          </button>
          <span className="text-sm text-gray-500">
            {currentStep + 1} / {messages.length}
          </span>
        </div>
      </div>
      
      {/* Actors Header */}
      <div className="flex justify-between mb-8 pb-4 border-b">
        {actors.map(actor => (
          <div key={actor.id} className="flex-1 text-center">
            <div className={`inline-block px-4 py-2 rounded-lg ${actor.color} border`}>
              <div className="font-medium text-sm">{actor.name}</div>
              <div className="text-xs text-gray-500 capitalize">{actor.type}</div>
            </div>
          </div>
        ))}
      </div>
      
      {/* Sequence Messages */}
      <div className="relative">
        {/* Lifelines */}
        {actors.map((actor, index) => (
          <div 
            key={actor.id}
            className="absolute top-0 bottom-0 w-px bg-gray-300 dashed"
            style={{ left: `${(100 / actors.length) * index + (100 / actors.length / 2)}%` }}
          />
        ))}
        
        {/* Messages */}
        <div className="space-y-6 pb-8">
          {visibleMessages.map((message, index) => {
            const fromIndex = actors.findIndex(a => a.id === message.from)
            const toIndex = actors.findIndex(a => a.id === message.to)
            const isReverse = fromIndex > toIndex
            const leftPos = Math.min(fromIndex, toIndex)
            const width = Math.abs(fromIndex - toIndex)
            
            return (
              <div 
                key={message.id}
                className="relative"
                style={{ height: '40px' }}
              >
                {/* Message Arrow */}
                <div 
                  className="absolute top-4 h-px bg-gray-400 flex items-center"
                  style={{
                    left: `${(100 / actors.length) * leftPos + (100 / actors.length / 2)}%`,
                    width: `${(100 / actors.length) * width}%`
                  }}
                >
                  <div className={`absolute ${isReverse ? 'right-0' : 'left-0'}`}>
                    {isReverse ? '◀' : '▶'}
                  </div>
                </div>
                
                {/* Message Label */}
                <div 
                  className={`
                    absolute top-0 px-2 py-1 text-xs border rounded cursor-pointer
                    transition-all duration-200 z-10
                    ${selectedMessage === message.id ? 'ring-2 ring-primary-500' : ''}
                    ${getMessageColor(message.type, message.status)}
                  `}
                  style={{
                    left: `${(100 / actors.length) * leftPos + (100 / actors.length / 2) + (100 / actors.length) * width / 2}%`,
                    transform: 'translateX(-50%)'
                  }}
                  onClick={() => setSelectedMessage(
                    selectedMessage === message.id ? null : message.id
                  )}
                >
                  <div className="flex items-center space-x-1">
                    {getMessageIcon(message.type)}
                    <span className="font-medium">{message.message}</span>
                  </div>
                  
                  {selectedMessage === message.id && message.details && (
                    <div className="mt-2 pt-2 border-t border-current border-opacity-20">
                      <div className="text-xs">{message.details}</div>
                    </div>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      </div>
      
      {/* Message Details Panel */}
      {selectedMessage && (
        <div className="mt-6 pt-6 border-t">
          {(() => {
            const message = messages.find(m => m.id === selectedMessage)
            if (!message) return null
            
            return (
              <div className="bg-gray-50 p-4 rounded-lg">
                <h4 className="font-medium text-gray-900 mb-2">Message Details</h4>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div><strong>From:</strong> {actors.find(a => a.id === message.from)?.name}</div>
                  <div><strong>To:</strong> {actors.find(a => a.id === message.to)?.name}</div>
                  <div><strong>Type:</strong> {message.type}</div>
                  <div><strong>Timestamp:</strong> {message.timestamp}</div>
                  {message.status && (
                    <div><strong>Status:</strong> {message.status}</div>
                  )}
                  {message.details && (
                    <div className="col-span-2"><strong>Details:</strong> {message.details}</div>
                  )}
                </div>
              </div>
            )
          })()}
        </div>
      )}
    </div>
  )
}

export default function Sequences() {
  const [activeSequence, setActiveSequence] = useState<'order' | 'auth' | 'failure'>('order')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-4">Sequence Diagrams</h1>
        <p className="text-lg text-gray-600">
          Interactive sequence diagrams showing detailed inter-service communication patterns 
          and message flows for different scenarios.
        </p>
      </div>

      {/* Sequence Navigation */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
          {[
            { key: 'order', label: 'Order Processing', icon: MessageSquare },
            { key: 'auth', label: 'User Authentication', icon: CheckCircle },
            { key: 'failure', label: 'Payment Failure', icon: XCircle }
          ].map(sequence => {
            const Icon = sequence.icon
            return (
              <button
                key={sequence.key}
                onClick={() => setActiveSequence(sequence.key as any)}
                className={`flex items-center px-4 py-2 text-sm font-medium rounded-md transition-colors ${
                  activeSequence === sequence.key
                    ? 'bg-white text-gray-900 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                <Icon className="h-4 w-4 mr-2" />
                {sequence.label}
              </button>
            )
          })}
        </div>
      </div>

      {/* Sequence Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <div className="card">
          <div className="flex items-center mb-4">
            <div className="p-2 bg-purple-100 rounded-lg mr-3">
              <MessageSquare className="h-5 w-5 text-purple-600" />
            </div>
            <h3 className="font-semibold text-gray-900">Order Processing</h3>
          </div>
          <p className="text-sm text-gray-600 mb-4">
            Complete sequence showing how services communicate during order creation, 
            from inventory checks to payment processing and confirmation.
          </p>
          <div className="text-xs text-gray-500">
            <strong>Messages:</strong> {orderSequenceMessages.length} interactions
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center mb-4">
            <div className="p-2 bg-blue-100 rounded-lg mr-3">
              <CheckCircle className="h-5 w-5 text-blue-600" />
            </div>
            <h3 className="font-semibold text-gray-900">User Authentication</h3>
          </div>
          <p className="text-sm text-gray-600 mb-4">
            Authentication flow showing JWT token generation, password verification, 
            and session management between client and services.
          </p>
          <div className="text-xs text-gray-500">
            <strong>Messages:</strong> {userAuthSequenceMessages.length} interactions
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center mb-4">
            <div className="p-2 bg-red-100 rounded-lg mr-3">
              <XCircle className="h-5 w-5 text-red-600" />
            </div>
            <h3 className="font-semibold text-gray-900">Payment Failure</h3>
          </div>
          <p className="text-sm text-gray-600 mb-4">
            Error handling sequence showing how services handle payment failures, 
            inventory rollback, and user notification processes.
          </p>
          <div className="text-xs text-gray-500">
            <strong>Messages:</strong> {paymentFailureSequenceMessages.length} interactions
          </div>
        </div>
      </div>

      {/* Message Type Legend */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Message Type Legend</h2>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          {[
            { type: 'request', label: 'Request', color: 'text-blue-600 bg-blue-50 border-blue-200' },
            { type: 'response', label: 'Response', color: 'text-purple-600 bg-purple-50 border-purple-200' },
            { type: 'event', label: 'Event', color: 'text-orange-600 bg-orange-50 border-orange-200' },
            { type: 'async', label: 'Async', color: 'text-pink-600 bg-pink-50 border-pink-200' },
            { type: 'error', label: 'Error', color: 'text-red-600 bg-red-50 border-red-200' }
          ].map(messageType => (
            <div key={messageType.type} className={`p-3 rounded-lg border ${messageType.color} text-center`}>
              <div className="font-medium text-sm">{messageType.label}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Sequence Diagrams */}
      <div className="space-y-12">
        {activeSequence === 'order' && (
          <SequenceDiagram 
            actors={orderSequenceActors}
            messages={orderSequenceMessages}
            title="Order Processing Sequence"
          />
        )}
        
        {activeSequence === 'auth' && (
          <SequenceDiagram 
            actors={userAuthSequenceActors}
            messages={userAuthSequenceMessages}
            title="User Authentication Sequence"
          />
        )}
        
        {activeSequence === 'failure' && (
          <SequenceDiagram 
            actors={paymentFailureSequenceActors}
            messages={paymentFailureSequenceMessages}
            title="Payment Failure Handling Sequence"
          />
        )}
      </div>

      {/* Communication Patterns */}
      <div className="mt-12">
        <h2 className="text-xl font-semibold text-gray-900 mb-6">Communication Patterns</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="card">
            <h3 className="font-semibold text-gray-900 mb-4">Synchronous Communication</h3>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>• Direct HTTP API calls between services</li>
              <li>• Request-response pattern with immediate feedback</li>
              <li>• Used for critical operations requiring confirmation</li>
              <li>• Includes timeout and retry mechanisms</li>
            </ul>
          </div>
          
          <div className="card">
            <h3 className="font-semibold text-gray-900 mb-4">Asynchronous Communication</h3>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>• Event-driven messaging for notifications</li>
              <li>• Fire-and-forget pattern for non-critical operations</li>
              <li>• Used for email notifications and logging</li>
              <li>• Improves system resilience and performance</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}