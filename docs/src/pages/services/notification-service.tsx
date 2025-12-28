import React, { useState } from 'react'
import { 
  Bell, 
  Mail, 
  MessageSquare, 
  Smartphone, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  XCircle,
  Clock,
  Send,
  AlertTriangle,
  Settings,
  Users,
  Zap,
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

const emailNotificationFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Email Request',
    description: 'Receive email notification request',
    type: 'start',
    next: ['validate-request']
  },
  {
    id: 'validate-request',
    title: 'Validate Request',
    description: 'Check email format and required fields',
    type: 'process',
    next: ['request-valid']
  },
  {
    id: 'request-valid',
    title: 'Request Valid?',
    description: 'Verify all required data is present',
    type: 'decision',
    next: ['check-preferences', 'validation-error'],
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
    id: 'check-preferences',
    title: 'Check User Preferences',
    description: 'Verify user email notification settings',
    type: 'process',
    next: ['preferences-allow']
  },
  {
    id: 'preferences-allow',
    title: 'Email Allowed?',
    description: 'Check if user allows email notifications',
    type: 'decision',
    next: ['render-template', 'skip-notification'],
    condition: 'Email notifications enabled'
  },
  {
    id: 'skip-notification',
    title: 'Skip Notification',
    description: 'User has disabled email notifications',
    type: 'end',
    status: 'info'
  },
  {
    id: 'render-template',
    title: 'Render Email Template',
    description: 'Generate email content from template',
    type: 'process',
    next: ['send-email']
  },
  {
    id: 'send-email',
    title: 'Send via SMTP',
    description: 'Send email through SMTP provider',
    type: 'external',
    next: ['email-sent']
  },
  {
    id: 'email-sent',
    title: 'Email Sent?',
    description: 'Check if email was sent successfully',
    type: 'decision',
    next: ['log-success', 'handle-failure'],
    condition: 'SMTP success response'
  },
  {
    id: 'handle-failure',
    title: 'Handle Send Failure',
    description: 'Log error and queue for retry',
    type: 'process',
    next: ['failure-logged'],
    status: 'error'
  },
  {
    id: 'failure-logged',
    title: 'Failure Logged',
    description: 'Error logged, notification queued for retry',
    type: 'end',
    status: 'error'
  },
  {
    id: 'log-success',
    title: 'Log Success',
    description: 'Record successful email delivery',
    type: 'process',
    next: ['success-response']
  },
  {
    id: 'success-response',
    title: 'Email Delivered',
    description: 'Email successfully sent and logged',
    type: 'end',
    status: 'success'
  }
]

const pushNotificationFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Push Request',
    description: 'Receive push notification request',
    type: 'start',
    next: ['validate-request']
  },
  {
    id: 'validate-request',
    title: 'Validate Request',
    description: 'Check message format and device tokens',
    type: 'process',
    next: ['request-valid']
  },
  {
    id: 'request-valid',
    title: 'Request Valid?',
    description: 'Verify all required data is present',
    type: 'decision',
    next: ['get-device-tokens', 'validation-error'],
    condition: 'Valid format and tokens'
  },
  {
    id: 'validation-error',
    title: 'Validation Error',
    description: 'Return validation error response',
    type: 'end',
    status: 'error'
  },
  {
    id: 'get-device-tokens',
    title: 'Get Device Tokens',
    description: 'Retrieve active device tokens for user',
    type: 'process',
    next: ['tokens-found']
  },
  {
    id: 'tokens-found',
    title: 'Tokens Found?',
    description: 'Check if user has registered devices',
    type: 'decision',
    next: ['send-to-fcm', 'no-devices'],
    condition: 'Active tokens exist'
  },
  {
    id: 'no-devices',
    title: 'No Registered Devices',
    description: 'User has no active device registrations',
    type: 'end',
    status: 'info'
  },
  {
    id: 'send-to-fcm',
    title: 'Send to FCM',
    description: 'Send push notification via Firebase',
    type: 'external',
    next: ['fcm-response']
  },
  {
    id: 'fcm-response',
    title: 'FCM Response',
    description: 'Process Firebase Cloud Messaging response',
    type: 'process',
    next: ['push-delivered']
  },
  {
    id: 'push-delivered',
    title: 'Push Delivered?',
    description: 'Check FCM delivery status',
    type: 'decision',
    next: ['log-delivery', 'handle-fcm-error'],
    condition: 'FCM success response'
  },
  {
    id: 'handle-fcm-error',
    title: 'Handle FCM Error',
    description: 'Process FCM error and update token status',
    type: 'process',
    next: ['error-logged'],
    status: 'error'
  },
  {
    id: 'error-logged',
    title: 'Error Logged',
    description: 'FCM error logged and invalid tokens removed',
    type: 'end',
    status: 'error'
  },
  {
    id: 'log-delivery',
    title: 'Log Delivery',
    description: 'Record successful push notification delivery',
    type: 'process',
    next: ['delivery-success']
  },
  {
    id: 'delivery-success',
    title: 'Push Delivered',
    description: 'Push notification successfully delivered',
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
      case 'event': return <Bell className="h-4 w-4" />
      case 'external': return <Send className="h-4 w-4" />
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

export default function NotificationService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-blue-100 rounded-lg mr-4">
            <Bell className="h-8 w-8 text-blue-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Notification Service</h1>
            <p className="text-lg text-gray-600">Multi-Channel Communication</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8086</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>MongoDB + Redis</span>
          </div>
          <div className="flex items-center">
            <Send className="h-4 w-4 mr-1" />
            <span>Multi-Channel</span>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
          {[
            { key: 'overview', label: 'Overview' },
            { key: 'architecture', label: 'Architecture' },
            { key: 'flows', label: 'Notification Flows' },
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
                    <Mail className="h-4 w-4 mr-2 mt-0.5 text-blue-600" />
                    <span>Email notification delivery and templating</span>
                  </li>
                  <li className="flex items-start">
                    <Smartphone className="h-4 w-4 mr-2 mt-0.5 text-blue-600" />
                    <span>Push notification management (FCM/APNS)</span>
                  </li>
                  <li className="flex items-start">
                    <MessageSquare className="h-4 w-4 mr-2 mt-0.5 text-blue-600" />
                    <span>SMS and in-app messaging</span>
                  </li>
                  <li className="flex items-start">
                    <Settings className="h-4 w-4 mr-2 mt-0.5 text-blue-600" />
                    <span>User notification preferences management</span>
                  </li>
                  <li className="flex items-start">
                    <Filter className="h-4 w-4 mr-2 mt-0.5 text-blue-600" />
                    <span>Template management and personalization</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Notification Channels</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• Email (SMTP/SendGrid/SES)</li>
                  <li>• Push Notifications (FCM/APNS)</li>
                  <li>• SMS (Twilio/AWS SNS)</li>
                  <li>• In-App Notifications</li>
                  <li>• Webhook Notifications</li>
                  <li>• Slack/Teams Integration</li>
                  <li>• WhatsApp Business API</li>
                  <li>• Real-time WebSocket</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Notification Types */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Notification Types</h2>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {[
                { name: 'Order Updates', color: 'bg-green-100 text-green-800', icon: CheckCircle },
                { name: 'Payment Alerts', color: 'bg-blue-100 text-blue-800', icon: Bell },
                { name: 'Security Alerts', color: 'bg-red-100 text-red-800', icon: AlertTriangle },
                { name: 'Marketing', color: 'bg-purple-100 text-purple-800', icon: Send },
                { name: 'System Updates', color: 'bg-yellow-100 text-yellow-800', icon: Settings },
                { name: 'User Actions', color: 'bg-indigo-100 text-indigo-800', icon: Users },
                { name: 'Reminders', color: 'bg-orange-100 text-orange-800', icon: Clock },
                { name: 'Promotions', color: 'bg-pink-100 text-pink-800', icon: Zap }
              ].map(type => {
                const Icon = type.icon
                return (
                  <div key={type.name} className={`p-4 rounded-lg ${type.color} text-center`}>
                    <Icon className="h-6 w-6 mx-auto mb-2" />
                    <div className="font-medium text-sm">{type.name}</div>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Delivery Status */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Delivery Status Tracking</h2>
            <div className="card">
              <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                {[
                  { status: 'QUEUED', color: 'bg-gray-100 text-gray-800', icon: Clock },
                  { status: 'SENDING', color: 'bg-blue-100 text-blue-800', icon: Send },
                  { status: 'DELIVERED', color: 'bg-green-100 text-green-800', icon: CheckCircle },
                  { status: 'FAILED', color: 'bg-red-100 text-red-800', icon: XCircle },
                  { status: 'BOUNCED', color: 'bg-orange-100 text-orange-800', icon: AlertTriangle },
                  { status: 'OPENED', color: 'bg-purple-100 text-purple-800', icon: Mail }
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
                  <li>• Node.js 18</li>
                  <li>• Express.js</li>
                  <li>• TypeScript</li>
                  <li>• Bull Queue (Redis)</li>
                  <li>• Socket.io</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Storage & Queue</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• MongoDB (Templates)</li>
                  <li>• Redis (Queue & Cache)</li>
                  <li>• AWS S3 (Assets)</li>
                  <li>• Elasticsearch (Logs)</li>
                  <li>• Apache Kafka (Events)</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">External Services</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• SendGrid (Email)</li>
                  <li>• Firebase FCM (Push)</li>
                  <li>• Twilio (SMS)</li>
                  <li>• AWS SES (Email)</li>
                  <li>• Apple APNS (iOS Push)</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Performance Metrics */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Performance Metrics</h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="card text-center">
                <div className="text-2xl font-bold text-blue-600 mb-2">99.5%</div>
                <div className="text-sm text-gray-600">Delivery Rate</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-green-600 mb-2">&lt; 2s</div>
                <div className="text-sm text-gray-600">Processing Time</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-purple-600 mb-2">1M+</div>
                <div className="text-sm text-gray-600">Daily Notifications</div>
              </div>
              <div className="card text-center">
                <div className="text-2xl font-bold text-orange-600 mb-2">24/7</div>
                <div className="text-sm text-gray-600">Monitoring</div>
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
                        <div className="font-medium text-blue-900">NotificationController</div>
                        <div className="text-xs text-blue-700 mt-1">Send Notifications</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">TemplateController</div>
                        <div className="text-xs text-blue-700 mt-1">Template Management</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">PreferencesController</div>
                        <div className="text-xs text-blue-700 mt-1">User Preferences</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Service Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Service Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">EmailService</div>
                        <div className="text-xs text-green-700 mt-1">Email Processing</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">PushService</div>
                        <div className="text-xs text-green-700 mt-1">Push Notifications</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">SMSService</div>
                        <div className="text-xs text-green-700 mt-1">SMS Delivery</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">TemplateService</div>
                        <div className="text-xs text-green-700 mt-1">Template Engine</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Queue Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Queue Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">EmailQueue</div>
                        <div className="text-xs text-purple-700 mt-1">Email Processing</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">PushQueue</div>
                        <div className="text-xs text-purple-700 mt-1">Push Processing</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">SMSQueue</div>
                        <div className="text-xs text-purple-700 mt-1">SMS Processing</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">RetryQueue</div>
                        <div className="text-xs text-purple-700 mt-1">Failed Retries</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* External Services */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">External Services</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-orange-100 rounded-lg text-center">
                        <div className="font-medium text-orange-900">SendGrid</div>
                        <div className="text-xs text-orange-700 mt-1">Email Provider</div>
                      </div>
                      <div className="p-3 bg-orange-100 rounded-lg text-center">
                        <div className="font-medium text-orange-900">Firebase FCM</div>
                        <div className="text-xs text-orange-700 mt-1">Push Provider</div>
                      </div>
                      <div className="p-3 bg-orange-100 rounded-lg text-center">
                        <div className="font-medium text-orange-900">Twilio</div>
                        <div className="text-xs text-orange-700 mt-1">SMS Provider</div>
                      </div>
                      <div className="p-3 bg-orange-100 rounded-lg text-center">
                        <div className="font-medium text-orange-900">WebSocket</div>
                        <div className="text-xs text-orange-700 mt-1">Real-time</div>
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
              {/* Notifications Collection */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Notifications Collection</h3>
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
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">ObjectId</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Primary Key</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">userId</td>
                        <td className="px-4 py-2 text-sm text-gray-500">String</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Target User ID</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">type</td>
                        <td className="px-4 py-2 text-sm text-gray-500">String</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Notification Type</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">channel</td>
                        <td className="px-4 py-2 text-sm text-gray-500">String</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Delivery Channel</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">status</td>
                        <td className="px-4 py-2 text-sm text-gray-500">String</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Delivery Status</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">content</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Object</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Message Content</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">createdAt</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Date</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Creation Time</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              
              {/* Templates Collection */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Templates Collection</h3>
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
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">ObjectId</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Primary Key</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">name</td>
                        <td className="px-4 py-2 text-sm text-gray-500">String</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Template Name</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">type</td>
                        <td className="px-4 py-2 text-sm text-gray-500">String</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Template Type</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">channel</td>
                        <td className="px-4 py-2 text-sm text-gray-500">String</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Target Channel</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">subject</td>
                        <td className="px-4 py-2 text-sm text-gray-500">String</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Email Subject</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">body</td>
                        <td className="px-4 py-2 text-sm text-gray-500">String</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Template Body</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">variables</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Array</td>
                        <td className="px-4 py-2 text-sm text-gray-500">Template Variables</td>
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
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Notification Flow Diagrams</h2>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <FlowDiagram steps={emailNotificationFlow} title="Email Notification Flow" />
              <FlowDiagram steps={pushNotificationFlow} title="Push Notification Flow" />
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
              {/* Notification Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Notification Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-green-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-green-100 text-green-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/notifications/send</code>
                    </div>
                    <p className="text-sm text-gray-600">Send a notification to user(s)</p>
                  </div>
                  
                  <div className="border-l-4 border-blue-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/notifications/{'{'}userId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get user notifications</p>
                  </div>
                  
                  <div className="border-l-4 border-yellow-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-yellow-100 text-yellow-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/notifications/bulk</code>
                    </div>
                    <p className="text-sm text-gray-600">Send bulk notifications</p>
                  </div>
                  
                  <div className="border-l-4 border-purple-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-purple-100 text-purple-800 text-xs font-medium px-2 py-1 rounded mr-2">PUT</span>
                      <code className="text-sm font-mono">/api/notifications/{'{'}id{'}'}/read</code>
                    </div>
                    <p className="text-sm text-gray-600">Mark notification as read</p>
                  </div>
                </div>
              </div>
              
              {/* Template Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Template Management Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-indigo-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-indigo-100 text-indigo-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/templates</code>
                    </div>
                    <p className="text-sm text-gray-600">List all notification templates</p>
                  </div>
                  
                  <div className="border-l-4 border-green-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-green-100 text-green-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/templates</code>
                    </div>
                    <p className="text-sm text-gray-600">Create a new template</p>
                  </div>
                  
                  <div className="border-l-4 border-yellow-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-yellow-100 text-yellow-800 text-xs font-medium px-2 py-1 rounded mr-2">PUT</span>
                      <code className="text-sm font-mono">/api/templates/{'{'}id{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Update an existing template</p>
                  </div>
                  
                  <div className="border-l-4 border-red-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-red-100 text-red-800 text-xs font-medium px-2 py-1 rounded mr-2">DELETE</span>
                      <code className="text-sm font-mono">/api/templates/{'{'}id{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Delete a template</p>
                  </div>
                </div>
              </div>
              
              {/* Preferences Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">User Preferences Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-teal-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-teal-100 text-teal-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/preferences/{'{'}userId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get user notification preferences</p>
                  </div>
                  
                  <div className="border-l-4 border-orange-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-orange-100 text-orange-800 text-xs font-medium px-2 py-1 rounded mr-2">PUT</span>
                      <code className="text-sm font-mono">/api/preferences/{'{'}userId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Update user preferences</p>
                  </div>
                  
                  <div className="border-l-4 border-pink-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-pink-100 text-pink-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/devices/register</code>
                    </div>
                    <p className="text-sm text-gray-600">Register device for push notifications</p>
                  </div>
                  
                  <div className="border-l-4 border-gray-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-gray-100 text-gray-800 text-xs font-medium px-2 py-1 rounded mr-2">DELETE</span>
                      <code className="text-sm font-mono">/api/devices/{'{'}deviceId{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Unregister device</p>
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