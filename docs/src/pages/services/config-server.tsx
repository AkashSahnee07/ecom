import React, { useState } from 'react'
import { 
  Server, 
  Settings, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  FileText,
  GitBranch,
  RefreshCw
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end'
  next?: string[]
  condition?: string
}

const configFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Service Startup',
    description: 'Microservice initiates boot process',
    type: 'start',
    next: ['request-config']
  },
  {
    id: 'request-config',
    title: 'Request Config',
    description: 'Fetch configuration from Config Server',
    type: 'process',
    next: ['load-profile']
  },
  {
    id: 'load-profile',
    title: 'Load Profile',
    description: 'Retrieve profile-specific properties (dev/prod)',
    type: 'process',
    next: ['apply-config']
  },
  {
    id: 'apply-config',
    title: 'Apply Configuration',
    description: 'Initialize beans with retrieved properties',
    type: 'end'
  }
]

export default function ConfigServer() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-orange-100 rounded-lg mr-4">
            <Settings className="h-8 w-8 text-orange-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Config Server</h1>
            <p className="text-lg text-gray-600">Centralized Configuration Management</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8888</span>
          </div>
          <div className="flex items-center">
            <GitBranch className="h-4 w-4 mr-1" />
            <span>Git/Native</span>
          </div>
          <div className="flex items-center">
            <RefreshCw className="h-4 w-4 mr-1" />
            <span>Hot Reload</span>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="mb-8">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
          {[
            { key: 'overview', label: 'Overview' },
            { key: 'architecture', label: 'Architecture' },
            { key: 'flows', label: 'Boot Sequence' },
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
                    <FileText className="h-4 w-4 mr-2 mt-0.5 text-orange-600" />
                    <span>Serve configuration properties to services</span>
                  </li>
                  <li className="flex items-start">
                    <GitBranch className="h-4 w-4 mr-2 mt-0.5 text-orange-600" />
                    <span>Version control for configuration</span>
                  </li>
                  <li className="flex items-start">
                    <Settings className="h-4 w-4 mr-2 mt-0.5 text-orange-600" />
                    <span>Environment-specific profiles</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• Centralized management of application.yml</li>
                  <li>• Encryption/Decryption of secrets</li>
                  <li>• Support for multiple backends (Git, Vault, Native)</li>
                  <li>• Dynamic configuration refresh</li>
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
                  <li>• Spring Cloud Config Server</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Backend</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Native File System (Dev)</li>
                  <li>• Git Repository (Prod)</li>
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
                 The Config Server externalizes configuration from application code. Services connect to it during startup to fetch their
                 configuration. This allows changing configuration without rebuilding services.
               </p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'flows' && (
         <div className="space-y-12">
           <h2 className="text-2xl font-semibold text-gray-900 mb-6">Configuration Loading Flow</h2>
           <div className="card p-4">
             <div className="space-y-4">
               {configFlow.map((step, index) => (
                 <div key={step.id} className="border-l-2 border-orange-200 pl-4 pb-4">
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
                  <td className="px-4 py-2 font-mono">/{'{application}'}/{'{profile}'}</td>
                  <td className="px-4 py-2">Get configuration for app/profile</td>
                </tr>
                <tr>
                  <td className="px-4 py-2 font-mono text-green-600">GET</td>
                  <td className="px-4 py-2 font-mono">/decrypt</td>
                  <td className="px-4 py-2">Decrypt value (if encryption enabled)</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
