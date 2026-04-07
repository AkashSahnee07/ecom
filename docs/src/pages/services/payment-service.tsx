import React, { useState } from 'react'
import { 
  CreditCard, 
  Shield, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  XCircle,
  Clock,
  DollarSign,
  AlertTriangle,
  Lock,
  RefreshCw,
  FileText,
  Zap,
  TrendingUp
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end' | 'event' | 'security'
  next?: string[]
  condition?: string
  status?: 'success' | 'error' | 'warning' | 'secure'
}

const paymentProcessingFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Payment Request',
    description: 'Receive payment request from order service',
    type: 'start',
    next: ['validate-request']
  },
  {
    id: 'validate-request',
    title: 'Validate Payment Request',
    description: 'Check request format and required fields',
    type: 'process',
    next: ['request-valid']
  },
  {
    id: 'request-valid',
    title: 'Request Valid?',
    description: 'Verify all required payment data is present',
    type: 'decision',
    next: ['tokenize-card', 'validation-error'],
    condition: 'All fields present'
  },
  {
    id: 'validation-error',
    title: 'Validation Error',
    description: 'Return validation error response',
    type: 'end',
    status: 'error'
  },
  {
    id: 'tokenize-card',
    title: 'Tokenize Card Data',
    description: 'Convert sensitive card data to secure token',
    type: 'security',
    next: ['fraud-check'],
    status: 'secure'
  },
  {
    id: 'fraud-check',
    title: 'Fraud Detection',
    description: 'Run fraud detection algorithms',
    type: 'security',
    next: ['fraud-result'],
    status: 'secure'
  },
  {
    id: 'fraud-result',
    title: 'Fraud Check Passed?',
    description: 'Evaluate fraud detection results',
    type: 'decision',
    next: ['process-payment', 'fraud-detected'],
    condition: 'Low risk score'
  },
  {
    id: 'fraud-detected',
    title: 'Fraud Detected',
    description: 'Block transaction and log security event',
    type: 'end',
    status: 'error'
  },
  {
    id: 'process-payment',
    title: 'Process with Gateway',
    description: 'Send payment to external payment gateway',
    type: 'process',
    next: ['gateway-response']
  },
  {
    id: 'gateway-response',
    title: 'Gateway Response',
    description: 'Receive response from payment gateway',
    type: 'process',
    next: ['payment-approved']
  },
  {
    id: 'payment-approved',
    title: 'Payment Approved?',
    description: 'Check gateway approval status',
    type: 'decision',
    next: ['save-transaction', 'payment-declined'],
    condition: 'Gateway approved'
  },
  {
    id: 'payment-declined',
    title: 'Payment Declined',
    description: 'Handle declined payment',
    type: 'process',
    next: ['decline-response'],
    status: 'error'
  },
  {
    id: 'decline-response',
    title: 'Return Decline Response',
    description: 'Send decline reason to order service',
    type: 'end',
    status: 'error'
  },
  {
    id: 'save-transaction',
    title: 'Save Transaction',
    description: 'Store successful transaction record',
    type: 'process',
    next: ['publish-success']
  },
  {
    id: 'publish-success',
    title: 'Publish Payment Success',
    description: 'Send payment success event to Kafka',
    type: 'event',
    next: ['success-response']
  },
  {
    id: 'success-response',
    title: 'Payment Successful',
    description: 'Return success response with transaction ID',
    type: 'end',
    status: 'success'
  }
]

const refundProcessingFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Refund Request',
    description: 'Receive refund request from order service',
    type: 'start',
    next: ['validate-refund']
  },
  {
    id: 'validate-refund',
    title: 'Validate Refund Request',
    description: 'Check original transaction and refund eligibility',
    type: 'process',
    next: ['refund-eligible']
  },
  {
    id: 'refund-eligible',
    title: 'Refund Eligible?',
    description: 'Verify transaction exists and can be refunded',
    type: 'decision',
    next: ['calculate-refund', 'refund-error'],
    condition: 'Transaction found and refundable'
  },
  {
    id: 'refund-error',
    title: 'Refund Not Eligible',
    description: 'Return refund eligibility error',
    type: 'end',
    status: 'error'
  },
  {
    id: 'calculate-refund',
    title: 'Calculate Refund Amount',
    description: 'Determine refund amount based on request',
    type: 'process',
    next: ['process-refund']
  },
  {
    id: 'process-refund',
    title: 'Process Refund',
    description: 'Send refund request to payment gateway',
    type: 'process',
    next: ['refund-response']
  },
  {
    id: 'refund-response',
    title: 'Refund Response',
    description: 'Receive refund response from gateway',
    type: 'process',
    next: ['refund-approved']
  },
  {
    id: 'refund-approved',
    title: 'Refund Approved?',
    description: 'Check if refund was processed successfully',
    type: 'decision',
    next: ['save-refund', 'refund-failed'],
    condition: 'Gateway approved refund'
  },
  {
    id: 'refund-failed',
    title: 'Refund Failed',
    description: 'Handle failed refund processing',
    type: 'end',
    status: 'error'
  },
  {
    id: 'save-refund',
    title: 'Save Refund Record',
    description: 'Store refund transaction in database',
    type: 'process',
    next: ['publish-refund']
  },
  {
    id: 'publish-refund',
    title: 'Publish Refund Event',
    description: 'Send refund success event to Kafka',
    type: 'event',
    next: ['refund-success']
  },
  {
    id: 'refund-success',
    title: 'Refund Successful',
    description: 'Return refund success response',
    type: 'end',
    status: 'success'
  }
]

const FlowDiagram: React.FC<{ steps: FlowStep[]; title: string }> = ({ steps, title }) => {
  const [selectedStep, setSelectedStep] = useState<string | null>(null)
  
  const getStepIcon = (type: string) => {
    switch (type) {
      case 'start': return <Activity className="h-4 w-4" />
      case 'process': return <Zap className="h-4 w-4" />
      case 'decision': return <CheckCircle className="h-4 w-4" />
      case 'event': return <AlertTriangle className="h-4 w-4" />
      case 'security': return <Shield className="h-4 w-4" />
      case 'end': return <CheckCircle className="h-4 w-4" />
      default: return <Activity className="h-4 w-4" />
    }
  }
  
  const getStepColor = (type: string, status?: string) => {
    if (status === 'error') return 'bg-red-100 text-red-700 border-red-300'
    if (status === 'warning') return 'bg-yellow-100 text-yellow-700 border-yellow-300'
    if (status === 'success') return 'bg-green-100 text-green-700 border-green-300'
    if (status === 'secure') return 'bg-purple-100 text-purple-700 border-purple-300'
    
    switch (type) {
      case 'start': return 'bg-blue-100 text-blue-700 border-blue-300'
      case 'process': return 'bg-indigo-100 text-indigo-700 border-indigo-300'
      case 'decision': return 'bg-orange-100 text-orange-700 border-orange-300'
      case 'event': return 'bg-teal-100 text-teal-700 border-teal-300'
      case 'security': return 'bg-purple-100 text-purple-700 border-purple-300'
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

export default function PaymentService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-green-100 rounded-lg mr-4">
            <CreditCard className="h-8 w-8 text-green-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Payment Service</h1>
            <p className="text-lg text-gray-600">Secure Payment Processing</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8085</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>PostgreSQL + Redis</span>
          </div>
          <div className="flex items-center">
            <Shield className="h-4 w-4 mr-1" />
            <span>PCI DSS Compliant</span>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
          {[
            { key: 'overview', label: 'Overview' },
            { key: 'architecture', label: 'Architecture' },
            { key: 'flows', label: 'Payment Flows' },
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
                    <CreditCard className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>Secure payment processing and authorization</span>
                  </li>
                  <li className="flex items-start">
                    <Shield className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>PCI DSS compliant card data handling</span>
                  </li>
                  <li className="flex items-start">
                    <Lock className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>Fraud detection and prevention</span>
                  </li>
                  <li className="flex items-start">
                    <RefreshCw className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>Refund and chargeback management</span>
                  </li>
                  <li className="flex items-start">
                    <FileText className="h-4 w-4 mr-2 mt-0.5 text-green-600" />
                    <span>Transaction history and reporting</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Security Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• End-to-end encryption (TLS 1.3)</li>
                  <li>• Card data tokenization</li>
                  <li>• Real-time fraud detection</li>
                  <li>• 3D Secure authentication</li>
                  <li>• PCI DSS Level 1 compliance</li>
                  <li>• Rate limiting and DDoS protection</li>
                  <li>• Audit logging and monitoring</li>
                  <li>• Secure key management (HSM)</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Payment Methods */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Supported Payment Methods</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
              {[
                { name: 'Visa', color: 'bg-blue-100 text-blue-800' },
                { name: 'Mastercard', color: 'bg-red-100 text-red-800' },
                { name: 'American Express', color: 'bg-green-100 text-green-800' },
                { name: 'Discover', color: 'bg-orange-100 text-orange-800' },
                { name: 'PayPal', color: 'bg-indigo-100 text-indigo-800' },
                { name: 'Apple Pay', color: 'bg-gray-100 text-gray-800' }
              ].map(method => (
                <div key={method.name} className={`p-4 rounded-lg ${method.color} text-center`}>
                  <CreditCard className="h-6 w-6 mx-auto mb-2" />
                  <div className="font-medium text-sm">{method.name}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Transaction States */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Transaction States</h2>
            <div className="card">
              <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                {[
                  { status: 'PENDING', color: 'bg-yellow-100 text-yellow-800', icon: Clock },
                  { status: 'AUTHORIZED', color: 'bg-blue-100 text-blue-800', icon: Lock },
                  { status: 'CAPTURED', color: 'bg-green-100 text-green-800', icon: CheckCircle },
                  { status: 'DECLINED', color: 'bg-red-100 text-red-800', icon: XCircle },
                  { status: 'REFUNDED', color: 'bg-purple-100 text-purple-800', icon: RefreshCw },
                  { status: 'FAILED', color: 'bg-gray-100 text-gray-800', icon: AlertTriangle }
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
                  <li>• Spring Security</li>
                  <li>• Spring Data JPA</li>
                  <li>• Spring Kafka</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Security & Storage</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• PostgreSQL (Encrypted)</li>
                  <li>• Redis (Session Cache)</li>
                  <li>• HashiCorp Vault (Secrets)</li>
                  <li>• AWS KMS (Key Management)</li>
                  <li>• TLS 1.3 Encryption</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Payment Gateways</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Stripe API</li>
                  <li>• PayPal REST API</li>
                  <li>• Square Payment API</li>
                  <li>• Authorize.Net</li>
                  <li>• Adyen Platform</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Performance Metrics */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Performance & Security Metrics</h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="card text-center">
                <div className="text-2xl font-bold text-green-600 mb-2">&lt; 500ms</div>
                <div className="text-sm text-gray-600">Payment Processing</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-blue-600 mb-2">99.9%</div>
                <div className="text-sm text-gray-600">Uptime SLA</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-purple-600 mb-2">&lt; 0.1%</div>
                <div className="text-sm text-gray-600">Fraud Rate</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-red-600 mb-2">PCI L1</div>
                <div className="text-sm text-gray-600">Compliance Level</div>
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
                  {/* API Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">API Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">PaymentController</div>
                        <div className="text-xs text-blue-700 mt-1">Payment Operations</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">RefundController</div>
                        <div className="text-xs text-blue-700 mt-1">Refund Management</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">WebhookController</div>
                        <div className="text-xs text-blue-700 mt-1">Gateway Callbacks</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Service Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Service Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">PaymentService</div>
                        <div className="text-xs text-green-700 mt-1">Core Logic</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">FraudService</div>
                        <div className="text-xs text-green-700 mt-1">Fraud Detection</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">TokenService</div>
                        <div className="text-xs text-green-700 mt-1">Card Tokenization</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">GatewayService</div>
                        <div className="text-xs text-green-700 mt-1">Gateway Integration</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Security Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Security Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">EncryptionService</div>
                        <div className="text-xs text-purple-700 mt-1">Data Encryption</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">AuditService</div>
                        <div className="text-xs text-purple-700 mt-1">Security Logging</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">ComplianceService</div>
                        <div className="text-xs text-purple-700 mt-1">PCI Compliance</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Data Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Data Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-indigo-100 rounded-lg text-center">
                        <div className="font-medium text-indigo-900">PostgreSQL</div>
                        <div className="text-xs text-indigo-700 mt-1">Transaction Data</div>
                      </div>
                      <div className="p-3 bg-red-100 rounded-lg text-center">
                        <div className="font-medium text-red-900">Redis</div>
                        <div className="text-xs text-red-700 mt-1">Session Cache</div>
                      </div>
                      <div className="p-3 bg-yellow-100 rounded-lg text-center">
                        <div className="font-medium text-yellow-900">Vault</div>
                        <div className="text-xs text-yellow-700 mt-1">Secret Storage</div>
                      </div>
                      <div className="p-3 bg-gray-100 rounded-lg text-center">
                        <div className="font-medium text-gray-900">Kafka</div>
                        <div className="text-xs text-gray-700 mt-1">Event Streaming</div>
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
              {/* Transactions Table */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Transactions Table</h3>
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
                        <td className="px-4 py-2 text-sm text-gray-500">UUID</td>
                        <td className="px-4 py-2 text-sm text-gray-500">PRIMARY KEY</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">order_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">amount</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DECIMAL(10,2)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">currency</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR(3)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">status</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR(20)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">gateway_id</td>
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
              
              {/* Payment Methods Table */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Payment Methods Table</h3>
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
                        <td className="px-4 py-2 text-sm text-gray-500">UUID</td>
                        <td className="px-4 py-2 text-sm text-gray-500">PRIMARY KEY</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">user_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">token</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR(255)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">ENCRYPTED</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">card_type</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR(20)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">last_four</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR(4)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">expires_at</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DATE</td>
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
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Payment Flow Diagrams</h2>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <FlowDiagram steps={paymentProcessingFlow} title="Payment Processing Flow" />
              <FlowDiagram steps={refundProcessingFlow} title="Refund Processing Flow" />
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
              {/* Payment Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Payment Processing Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-green-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-green-100 text-green-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/payments/process</code>
                    </div>
                    <p className="text-sm text-gray-600">Process a payment transaction</p>
                  </div>
                  
                  <div className="border-l-4 border-blue-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/payments/{'{'}transactionId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get payment transaction details</p>
                  </div>
                  
                  <div className="border-l-4 border-yellow-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-yellow-100 text-yellow-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/payments/{'{'}transactionId{'}'}/capture</code>
                    </div>
                    <p className="text-sm text-gray-600">Capture an authorized payment</p>
                  </div>
                  
                  <div className="border-l-4 border-red-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-red-100 text-red-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/payments/{'{'}transactionId{'}'}/void</code>
                    </div>
                    <p className="text-sm text-gray-600">Void an authorized payment</p>
                  </div>
                </div>
              </div>
              
              {/* Refund Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Refund Management Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-purple-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-purple-100 text-purple-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/refunds/process</code>
                    </div>
                    <p className="text-sm text-gray-600">Process a refund request</p>
                  </div>
                  
                  <div className="border-l-4 border-indigo-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-indigo-100 text-indigo-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/refunds/{'{'}refundId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get refund transaction details</p>
                  </div>
                  
                  <div className="border-l-4 border-teal-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-teal-100 text-teal-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/refunds/transaction/{'{'}transactionId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get refunds for a transaction</p>
                  </div>
                </div>
              </div>
              
              {/* Security Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Security & Tokenization Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-pink-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-pink-100 text-pink-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/tokens/create</code>
                    </div>
                    <p className="text-sm text-gray-600">Create payment method token</p>
                  </div>
                  
                  <div className="border-l-4 border-orange-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-orange-100 text-orange-800 text-xs font-medium px-2 py-1 rounded mr-2">DELETE</span>
                      <code className="text-sm font-mono">/api/tokens/{'{'}tokenId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Delete payment method token</p>
                  </div>
                  
                  <div className="border-l-4 border-gray-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-gray-100 text-gray-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/fraud/check</code>
                    </div>
                    <p className="text-sm text-gray-600">Run fraud detection check</p>
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