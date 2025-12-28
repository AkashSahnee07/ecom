import React, { useState } from 'react'
import { 
  Package, 
  Search, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  XCircle,
  Clock,
  Tag,
  Star,
  Filter,
  Grid,
  List,
  Eye,
  Edit,
  Trash2
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end'
  next?: string[]
  condition?: string
}

const productSearchFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Search Request',
    description: 'Client submits search query with filters',
    type: 'start',
    next: ['validate-query']
  },
  {
    id: 'validate-query',
    title: 'Validate Query',
    description: 'Check query parameters and sanitize input',
    type: 'process',
    next: ['check-cache']
  },
  {
    id: 'check-cache',
    title: 'Check Redis Cache',
    description: 'Look for cached search results',
    type: 'decision',
    next: ['return-cached', 'search-elasticsearch'],
    condition: 'Cache hit'
  },
  {
    id: 'return-cached',
    title: 'Return Cached Results',
    description: 'Return results from Redis cache',
    type: 'end'
  },
  {
    id: 'search-elasticsearch',
    title: 'Search Elasticsearch',
    description: 'Execute full-text search with filters',
    type: 'process',
    next: ['enrich-results']
  },
  {
    id: 'enrich-results',
    title: 'Enrich Results',
    description: 'Add pricing, inventory, and rating data',
    type: 'process',
    next: ['cache-results']
  },
  {
    id: 'cache-results',
    title: 'Cache Results',
    description: 'Store results in Redis for future queries',
    type: 'process',
    next: ['return-results']
  },
  {
    id: 'return-results',
    title: 'Return Results',
    description: 'Send paginated search results to client',
    type: 'end'
  }
]

const productCrudFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Product Operation Request',
    description: 'Admin submits product CRUD operation',
    type: 'start',
    next: ['validate-auth']
  },
  {
    id: 'validate-auth',
    title: 'Validate Authorization',
    description: 'Check admin permissions and JWT token',
    type: 'process',
    next: ['auth-valid']
  },
  {
    id: 'auth-valid',
    title: 'Authorized?',
    description: 'Check if user has admin privileges',
    type: 'decision',
    next: ['validate-data', 'auth-error']
  },
  {
    id: 'auth-error',
    title: 'Authorization Error',
    description: 'Return 403 Forbidden error',
    type: 'end'
  },
  {
    id: 'validate-data',
    title: 'Validate Product Data',
    description: 'Check required fields and data formats',
    type: 'process',
    next: ['data-valid']
  },
  {
    id: 'data-valid',
    title: 'Data Valid?',
    description: 'Check if product data is valid',
    type: 'decision',
    next: ['save-mysql', 'validation-error']
  },
  {
    id: 'validation-error',
    title: 'Validation Error',
    description: 'Return 400 Bad Request with errors',
    type: 'end'
  },
  {
    id: 'save-mysql',
    title: 'Save to MySQL',
    description: 'Store product data in MySQL database',
    type: 'process',
    next: ['index-elasticsearch']
  },
  {
    id: 'index-elasticsearch',
    title: 'Index in Elasticsearch',
    description: 'Add/update product in search index',
    type: 'process',
    next: ['invalidate-cache']
  },
  {
    id: 'invalidate-cache',
    title: 'Invalidate Cache',
    description: 'Clear related Redis cache entries',
    type: 'process',
    next: ['publish-event']
  },
  {
    id: 'publish-event',
    title: 'Publish Event',
    description: 'Send product change event to Kafka',
    type: 'process',
    next: ['success']
  },
  {
    id: 'success',
    title: 'Operation Success',
    description: 'Return success response with product data',
    type: 'end'
  }
]

const FlowDiagram: React.FC<{ steps: FlowStep[]; title: string }> = ({ steps, title }) => {
  const [selectedStep, setSelectedStep] = useState<string | null>(null)
  
  const getStepIcon = (type: string) => {
    switch (type) {
      case 'start': return <Activity className="h-4 w-4" />
      case 'process': return <Grid className="h-4 w-4" />
      case 'decision': return <CheckCircle className="h-4 w-4" />
      case 'end': return <CheckCircle className="h-4 w-4" />
      default: return <Activity className="h-4 w-4" />
    }
  }
  
  const getStepColor = (type: string) => {
    switch (type) {
      case 'start': return 'bg-green-100 text-green-700 border-green-300'
      case 'process': return 'bg-blue-100 text-blue-700 border-blue-300'
      case 'decision': return 'bg-yellow-100 text-yellow-700 border-yellow-300'
      case 'end': return 'bg-purple-100 text-purple-700 border-purple-300'
      default: return 'bg-gray-100 text-gray-700 border-gray-300'
    }
  }
  
  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
      <h3 className="text-lg font-semibold text-gray-900 mb-6">{title}</h3>
      
      <div className="space-y-4">
        {steps.map((step, index) => (
          <div key={step.id} className="relative">
            <div 
              className={`
                p-4 rounded-lg border-2 cursor-pointer transition-all duration-200
                ${selectedStep === step.id ? 'ring-2 ring-primary-500' : ''}
                ${getStepColor(step.type)}
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

export default function ProductService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-orange-100 rounded-lg mr-4">
            <Package className="h-8 w-8 text-orange-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Product Service</h1>
            <p className="text-lg text-gray-600">Product Catalog Management</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8082</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>MySQL + Elasticsearch</span>
          </div>
          <div className="flex items-center">
            <Search className="h-4 w-4 mr-1" />
            <span>Full-text Search</span>
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
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-4">Core Responsibilities</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li className="flex items-start">
                    <Package className="h-4 w-4 mr-2 mt-0.5 text-orange-600" />
                    <span>Product catalog management and CRUD operations</span>
                  </li>
                  <li className="flex items-start">
                    <Search className="h-4 w-4 mr-2 mt-0.5 text-orange-600" />
                    <span>Advanced product search and filtering</span>
                  </li>
                  <li className="flex items-start">
                    <Tag className="h-4 w-4 mr-2 mt-0.5 text-orange-600" />
                    <span>Category and tag management</span>
                  </li>
                  <li className="flex items-start">
                    <Star className="h-4 w-4 mr-2 mt-0.5 text-orange-600" />
                    <span>Product ratings and reviews</span>
                  </li>
                  <li className="flex items-start">
                    <Activity className="h-4 w-4 mr-2 mt-0.5 text-orange-600" />
                    <span>Inventory tracking and management</span>
                  </li>
                </ul>
              </div>
              
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• Full-text search with Elasticsearch</li>
                  <li>• Advanced filtering and faceted search</li>
                  <li>• Product image management</li>
                  <li>• Multi-variant product support</li>
                  <li>• Real-time inventory updates</li>
                  <li>• Product recommendation engine</li>
                  <li>• Bulk import/export capabilities</li>
                  <li>• SEO-friendly URLs and metadata</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Technology Stack */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Technology Stack</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-3">Backend</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Java 17</li>
                  <li>• Spring Boot 3.x</li>
                  <li>• Spring Data JPA</li>
                  <li>• Spring Data Elasticsearch</li>
                  <li>• Spring Cache</li>
                </ul>
              </div>
              
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-3">Database</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• MySQL (Primary Storage)</li>
                  <li>• Elasticsearch (Search Index)</li>
                  <li>• Redis (Caching Layer)</li>
                  <li>• JPA/Hibernate ORM</li>
                  <li>• Database Migrations</li>
                </ul>
              </div>
              
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-3">Search & Cache</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• Elasticsearch 8.x</li>
                  <li>• Full-text Search</li>
                  <li>• Faceted Search</li>
                  <li>• Redis Caching</li>
                  <li>• Query Optimization</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Performance Metrics */}
          <div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Performance Metrics</h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm text-center">
                <div className="text-2xl font-bold text-orange-600 mb-2">&lt; 100ms</div>
                <div className="text-sm text-gray-600">Average Search Response</div>
              </div>
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm text-center">
                <div className="text-2xl font-bold text-green-600 mb-2">99.9%</div>
                <div className="text-sm text-gray-600">Search Accuracy</div>
              </div>
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm text-center">
                <div className="text-2xl font-bold text-blue-600 mb-2">1M+</div>
                <div className="text-sm text-gray-600">Products Indexed</div>
              </div>
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm text-center">
                <div className="text-2xl font-bold text-purple-600 mb-2">85%</div>
                <div className="text-sm text-gray-600">Cache Hit Rate</div>
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
            <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
              <div className="diagram-container">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                  {/* Controller Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Controller Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">ProductController</div>
                        <div className="text-xs text-blue-700 mt-1">CRUD Operations</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">SearchController</div>
                        <div className="text-xs text-blue-700 mt-1">Search & Filter</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">CategoryController</div>
                        <div className="text-xs text-blue-700 mt-1">Category Management</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Service Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Service Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">ProductService</div>
                        <div className="text-xs text-green-700 mt-1">Business Logic</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">SearchService</div>
                        <div className="text-xs text-green-700 mt-1">Search Logic</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">CacheService</div>
                        <div className="text-xs text-green-700 mt-1">Cache Management</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">ImageService</div>
                        <div className="text-xs text-green-700 mt-1">Image Processing</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Repository Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Repository Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">ProductRepository</div>
                        <div className="text-xs text-purple-700 mt-1">JPA Repository</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">CategoryRepository</div>
                        <div className="text-xs text-purple-700 mt-1">Category Data</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">ElasticsearchRepository</div>
                        <div className="text-xs text-purple-700 mt-1">Search Index</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Data Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Data Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-orange-100 rounded-lg text-center">
                        <div className="font-medium text-orange-900">MySQL</div>
                        <div className="text-xs text-orange-700 mt-1">Primary Database</div>
                      </div>
                      <div className="p-3 bg-yellow-100 rounded-lg text-center">
                        <div className="font-medium text-yellow-900">Elasticsearch</div>
                        <div className="text-xs text-yellow-700 mt-1">Search Engine</div>
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
              {/* Products Table */}
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-4">Products Table</h3>
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
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">name</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR(255)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">description</td>
                        <td className="px-4 py-2 text-sm text-gray-500">TEXT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">-</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">price</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DECIMAL(10,2)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">category_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">FOREIGN KEY</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">stock_quantity</td>
                        <td className="px-4 py-2 text-sm text-gray-500">INT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DEFAULT 0</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">is_active</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BOOLEAN</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DEFAULT TRUE</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              
              {/* Categories Table */}
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-4">Categories Table</h3>
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
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">name</td>
                        <td className="px-4 py-2 text-sm text-gray-500">VARCHAR(100)</td>
                        <td className="px-4 py-2 text-sm text-gray-500">UNIQUE, NOT NULL</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">description</td>
                        <td className="px-4 py-2 text-sm text-gray-500">TEXT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">-</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">parent_id</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BIGINT</td>
                        <td className="px-4 py-2 text-sm text-gray-500">FOREIGN KEY</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-2 text-sm font-medium text-gray-900">is_active</td>
                        <td className="px-4 py-2 text-sm text-gray-500">BOOLEAN</td>
                        <td className="px-4 py-2 text-sm text-gray-500">DEFAULT TRUE</td>
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
              <FlowDiagram steps={productSearchFlow} title="Product Search Flow" />
              <FlowDiagram steps={productCrudFlow} title="Product CRUD Flow" />
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
              {/* Product Management Endpoints */}
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-4">Product Management Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-green-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-green-100 text-green-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/products</code>
                    </div>
                    <p className="text-sm text-gray-600">Get paginated list of products with filters</p>
                  </div>
                  
                  <div className="border-l-4 border-blue-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/products/{'{'}{"id"}{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get product details by ID</p>
                  </div>
                  
                  <div className="border-l-4 border-yellow-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-yellow-100 text-yellow-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/products</code>
                    </div>
                    <p className="text-sm text-gray-600">Create new product (Admin only)</p>
                  </div>
                  
                  <div className="border-l-4 border-purple-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-purple-100 text-purple-800 text-xs font-medium px-2 py-1 rounded mr-2">PUT</span>
                      <code className="text-sm font-mono">/api/products/{'{'}{"id"}{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Update product (Admin only)</p>
                  </div>
                  
                  <div className="border-l-4 border-red-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-red-100 text-red-800 text-xs font-medium px-2 py-1 rounded mr-2">DELETE</span>
                      <code className="text-sm font-mono">/api/products/{'{'}{"id"}{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Delete product (Admin only)</p>
                  </div>
                </div>
              </div>
              
              {/* Search Endpoints */}
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-4">Search Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-indigo-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-indigo-100 text-indigo-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/products/search</code>
                    </div>
                    <p className="text-sm text-gray-600">Full-text search with filters and facets</p>
                  </div>
                  
                  <div className="border-l-4 border-teal-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-teal-100 text-teal-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/products/categories/{'{'}{"categoryId"}{'}'}</code>
                    </div>
                    <p className="text-sm text-gray-600">Get products by category</p>
                  </div>
                  
                  <div className="border-l-4 border-pink-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-pink-100 text-pink-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/products/recommendations</code>
                    </div>
                    <p className="text-sm text-gray-600">Get personalized product recommendations</p>
                  </div>
                </div>
              </div>
              
              {/* Category Endpoints */}
              <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                <h3 className="font-semibold text-gray-900 mb-4">Category Management Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-cyan-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-cyan-100 text-cyan-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/categories</code>
                    </div>
                    <p className="text-sm text-gray-600">Get category hierarchy</p>
                  </div>
                  
                  <div className="border-l-4 border-lime-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-lime-100 text-lime-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/categories</code>
                    </div>
                    <p className="text-sm text-gray-600">Create new category (Admin only)</p>
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