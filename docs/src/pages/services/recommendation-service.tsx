import React, { useState } from 'react'
import { 
  Star, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  Settings,
  Zap,
  User
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end'
  next?: string[]
  condition?: string
}

const recommendationFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'User Action Event',
    description: 'View/Purchase event via Kafka',
    type: 'start',
    next: ['process-event']
  },
  {
    id: 'process-event',
    title: 'Process Event',
    description: 'Update user preference profile',
    type: 'process',
    next: ['update-model']
  },
  {
    id: 'update-model',
    title: 'Update Model',
    description: 'Recalculate collaborative filtering',
    type: 'process',
    next: ['cache-results']
  },
  {
    id: 'cache-results',
    title: 'Cache Results',
    description: 'Store top recommendations in Redis',
    type: 'end'
  }
]

export default function RecommendationService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-purple-100 rounded-lg mr-4">
            <Star className="h-8 w-8 text-purple-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Recommendation Service</h1>
            <p className="text-lg text-gray-600">Personalized Product Recommendations</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8087</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>MongoDB + Redis</span>
          </div>
          <div className="flex items-center">
            <Zap className="h-4 w-4 mr-1" />
            <span>AI/ML</span>
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
                    <User className="h-4 w-4 mr-2 mt-0.5 text-purple-600" />
                    <span>Track user behavior patterns</span>
                  </li>
                  <li className="flex items-start">
                    <Star className="h-4 w-4 mr-2 mt-0.5 text-purple-600" />
                    <span>Generate personalized suggestions</span>
                  </li>
                  <li className="flex items-start">
                    <Zap className="h-4 w-4 mr-2 mt-0.5 text-purple-600" />
                    <span>Real-time personalization</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• "You might also like" features</li>
                  <li>• Collaborative filtering algorithms</li>
                  <li>• Trending products analysis</li>
                  <li>• User history tracking</li>
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
                  <li>• Spring Data MongoDB</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Data</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• MongoDB (User Profiles)</li>
                  <li>• Redis (Hot Cache)</li>
                  <li>• Kafka (Event Stream)</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Algorithms</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Matrix Factorization</li>
                  <li>• Item-to-Item Similarity</li>
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
                 The Recommendation Service consumes user activity events from Kafka to build user profiles in MongoDB.
                 It pre-calculates recommendations and caches them in Redis for low-latency retrieval during user sessions.
               </p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'flows' && (
         <div className="space-y-12">
           <h2 className="text-2xl font-semibold text-gray-900 mb-6">Recommendation Pipeline</h2>
           <div className="card p-4">
             <div className="space-y-4">
               {recommendationFlow.map((step, index) => (
                 <div key={step.id} className="border-l-2 border-purple-200 pl-4 pb-4">
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
                  <td className="px-4 py-2 font-mono">/api/recommendations</td>
                  <td className="px-4 py-2">Get personalized recommendations</td>
                </tr>
                <tr>
                  <td className="px-4 py-2 font-mono text-green-600">GET</td>
                  <td className="px-4 py-2 font-mono">/api/recommendations/product/{'{id}'}</td>
                  <td className="px-4 py-2">Get similar products</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
