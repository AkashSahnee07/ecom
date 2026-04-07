import React, { useState } from 'react'
import { 
  Truck, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  Settings,
  MapPin,
  Package
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end'
  next?: string[]
  condition?: string
}

const shippingFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Order Placed Event',
    description: 'Receive Kafka event from Order Service',
    type: 'start',
    next: ['create-shipment']
  },
  {
    id: 'create-shipment',
    title: 'Create Shipment',
    description: 'Initialize shipment record in pending state',
    type: 'process',
    next: ['calculate-cost']
  },
  {
    id: 'calculate-cost',
    title: 'Calculate Shipping',
    description: 'Determine provider and cost based on address',
    type: 'process',
    next: ['assign-provider']
  },
  {
    id: 'assign-provider',
    title: 'Assign Provider',
    description: 'Select logistics provider (e.g., FedEx, UPS)',
    type: 'process',
    next: ['generate-label']
  },
  {
    id: 'generate-label',
    title: 'Generate Label',
    description: 'Create tracking number and label',
    type: 'process',
    next: ['notify-user']
  },
  {
    id: 'notify-user',
    title: 'Notify User',
    description: 'Send shipping update via Notification Service',
    type: 'end'
  }
]

export default function ShippingService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-teal-100 rounded-lg mr-4">
            <Truck className="h-8 w-8 text-teal-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Shipping Service</h1>
            <p className="text-lg text-gray-600">Logistics and Delivery Management</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8088</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>PostgreSQL</span>
          </div>
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Kafka Consumer</span>
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
                    <Truck className="h-4 w-4 mr-2 mt-0.5 text-teal-600" />
                    <span>Shipment creation and tracking</span>
                  </li>
                  <li className="flex items-start">
                    <MapPin className="h-4 w-4 mr-2 mt-0.5 text-teal-600" />
                    <span>Address validation</span>
                  </li>
                  <li className="flex items-start">
                    <Package className="h-4 w-4 mr-2 mt-0.5 text-teal-600" />
                    <span>Label generation</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• Integration with logistics providers</li>
                  <li>• Real-time tracking updates</li>
                  <li>• Automated shipping cost calculation</li>
                  <li>• Delivery status webhooks</li>
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
                  <li>• Spring Cloud Stream</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Database</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• PostgreSQL</li>
                  <li>• Liquibase Migrations</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Messaging</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Apache Kafka</li>
                  <li>• Avro Schemas</li>
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
                 The Shipping Service is an event-driven microservice that listens for 'OrderPlaced' events. 
                 It manages the entire fulfillment lifecycle from label creation to delivery confirmation.
               </p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'flows' && (
         <div className="space-y-12">
           <h2 className="text-2xl font-semibold text-gray-900 mb-6">Fulfillment Flow</h2>
           <div className="card p-4">
             <div className="space-y-4">
               {shippingFlow.map((step, index) => (
                 <div key={step.id} className="border-l-2 border-teal-200 pl-4 pb-4">
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
                  <td className="px-4 py-2 font-mono">/api/shipping/{'{id}'}</td>
                  <td className="px-4 py-2">Get shipment details</td>
                </tr>
                <tr>
                  <td className="px-4 py-2 font-mono text-green-600">GET</td>
                  <td className="px-4 py-2 font-mono">/api/shipping/tracking/{'{number}'}</td>
                  <td className="px-4 py-2">Track package</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
