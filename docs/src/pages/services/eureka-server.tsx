import React, { useState } from 'react'
import { 
  Server, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  Search,
  Globe,
  Database
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end'
  next?: string[]
  condition?: string
}

const discoveryFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Service Startup',
    description: 'Microservice boots up',
    type: 'start',
    next: ['register']
  },
  {
    id: 'register',
    title: 'Register',
    description: 'Send IP/Port metadata to Eureka',
    type: 'process',
    next: ['heartbeat']
  },
  {
    id: 'heartbeat',
    title: 'Heartbeat',
    description: 'Send periodic keep-alive signals (30s)',
    type: 'process',
    next: ['client-fetch']
  },
  {
    id: 'client-fetch',
    title: 'Client Discovery',
    description: 'Gateway/Services fetch registry',
    type: 'process',
    next: ['end']
  },
  {
    id: 'end',
    title: 'Service Available',
    description: 'Service is discoverable for routing',
    type: 'end'
  }
]

export default function EurekaServer() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-red-100 rounded-lg mr-4">
            <Search className="h-8 w-8 text-red-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Eureka Server</h1>
            <p className="text-lg text-gray-600">Service Discovery and Registry</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8761</span>
          </div>
          <div className="flex items-center">
            <Globe className="h-4 w-4 mr-1" />
            <span>Discovery</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>In-Memory</span>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
          {[
            { key: 'overview', label: 'Overview' },
            { key: 'architecture', label: 'Architecture' },
            { key: 'flows', label: 'Registry Flow' },
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
                    <Search className="h-4 w-4 mr-2 mt-0.5 text-red-600" />
                    <span>Maintain registry of active services</span>
                  </li>
                  <li className="flex items-start">
                    <Activity className="h-4 w-4 mr-2 mt-0.5 text-red-600" />
                    <span>Monitor service health via heartbeats</span>
                  </li>
                  <li className="flex items-start">
                    <Globe className="h-4 w-4 mr-2 mt-0.5 text-red-600" />
                    <span>Enable client-side load balancing</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• High availability support (Peer Awareness)</li>
                  <li>• Self-preservation mode</li>
                  <li>• REST API for registry access</li>
                  <li>• Dashboard for monitoring</li>
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
                  <li>• Spring Cloud Netflix Eureka</li>
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
                 Eureka Server acts as a phonebook for microservices. Services register themselves upon startup and send periodic heartbeats.
                 Clients (like API Gateway) query Eureka to find the location (IP/Port) of other services.
               </p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'flows' && (
         <div className="space-y-12">
           <h2 className="text-2xl font-semibold text-gray-900 mb-6">Registration Flow</h2>
           <div className="card p-4">
             <div className="space-y-4">
               {discoveryFlow.map((step, index) => (
                 <div key={step.id} className="border-l-2 border-red-200 pl-4 pb-4">
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
                  <td className="px-4 py-2 font-mono">/eureka/apps</td>
                  <td className="px-4 py-2">Get all registered apps</td>
                </tr>
                <tr>
                  <td className="px-4 py-2 font-mono text-green-600">GET</td>
                  <td className="px-4 py-2 font-mono">/eureka/apps/{'{appID}'}</td>
                  <td className="px-4 py-2">Get instances of specific app</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
