import React, { useState } from 'react'
import { 
  Star, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  Settings,
  MessageSquare,
  ThumbsUp
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end'
  next?: string[]
  condition?: string
}

const reviewFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Submit Review',
    description: 'User submits rating and comment',
    type: 'start',
    next: ['validate-purchase']
  },
  {
    id: 'validate-purchase',
    title: 'Verify Purchase',
    description: 'Check if user purchased the product',
    type: 'process',
    next: ['check-eligibility']
  },
  {
    id: 'check-eligibility',
    title: 'Purchased?',
    description: 'Only verified buyers can review',
    type: 'decision',
    next: ['save-review', 'reject'],
    condition: 'Verified purchase'
  },
  {
    id: 'reject',
    title: 'Reject Review',
    description: 'Return error: verified purchase required',
    type: 'end'
  },
  {
    id: 'save-review',
    title: 'Save Review',
    description: 'Persist review in database',
    type: 'process',
    next: ['update-rating']
  },
  {
    id: 'update-rating',
    title: 'Update Product Rating',
    description: 'Recalculate average rating',
    type: 'process',
    next: ['success']
  },
  {
    id: 'success',
    title: 'Review Posted',
    description: 'Return success response',
    type: 'end'
  }
]

export default function ReviewService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-yellow-100 rounded-lg mr-4">
            <Star className="h-8 w-8 text-yellow-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Review Service</h1>
            <p className="text-lg text-gray-600">Product Ratings and Feedback</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8089</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>PostgreSQL</span>
          </div>
          <div className="flex items-center">
            <MessageSquare className="h-4 w-4 mr-1" />
            <span>User Content</span>
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
                    <Star className="h-4 w-4 mr-2 mt-0.5 text-yellow-600" />
                    <span>Manage product ratings and reviews</span>
                  </li>
                  <li className="flex items-start">
                    <CheckCircle className="h-4 w-4 mr-2 mt-0.5 text-yellow-600" />
                    <span>Verify verified purchases</span>
                  </li>
                  <li className="flex items-start">
                    <ThumbsUp className="h-4 w-4 mr-2 mt-0.5 text-yellow-600" />
                    <span>Helpful votes on reviews</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• 5-star rating system</li>
                  <li>• Text and image reviews</li>
                  <li>• Review moderation capabilities</li>
                  <li>• Aggregated rating statistics</li>
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
                  <li>• Spring Data JPA</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Database</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• PostgreSQL</li>
                  <li>• Materialized Views (Stats)</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Integration</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Order Service (Verification)</li>
                  <li>• Product Service (Sync)</li>
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
                 The Review Service maintains the reputation system of the platform. It integrates with the Order Service
                 to ensure authenticity of reviews by validating purchase history.
               </p>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'flows' && (
         <div className="space-y-12">
           <h2 className="text-2xl font-semibold text-gray-900 mb-6">Review Submission Flow</h2>
           <div className="card p-4">
             <div className="space-y-4">
               {reviewFlow.map((step, index) => (
                 <div key={step.id} className="border-l-2 border-yellow-200 pl-4 pb-4">
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
                  <td className="px-4 py-2 font-mono">/api/reviews/product/{'{id}'}</td>
                  <td className="px-4 py-2">Get product reviews</td>
                </tr>
                <tr>
                  <td className="px-4 py-2 font-mono text-blue-600">POST</td>
                  <td className="px-4 py-2 font-mono">/api/reviews</td>
                  <td className="px-4 py-2">Submit review</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
