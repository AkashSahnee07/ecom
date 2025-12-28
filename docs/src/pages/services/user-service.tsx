import React, { useState } from 'react'
import { 
  Users, 
  Shield, 
  Key, 
  Database, 
  Activity, 
  ArrowRight, 
  CheckCircle, 
  XCircle,
  Clock,
  Lock,
  Mail,
  User,
  Settings
} from 'lucide-react'

interface FlowStep {
  id: string
  title: string
  description: string
  type: 'start' | 'process' | 'decision' | 'end'
  next?: string[]
  condition?: string
}

const userRegistrationFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'User Registration Request',
    description: 'Client submits registration form',
    type: 'start',
    next: ['validate']
  },
  {
    id: 'validate',
    title: 'Input Validation',
    description: 'Validate email format, password strength, required fields',
    type: 'process',
    next: ['check-email']
  },
  {
    id: 'check-email',
    title: 'Email Exists?',
    description: 'Check if email already registered',
    type: 'decision',
    next: ['error-exists', 'hash-password'],
    condition: 'Email already exists'
  },
  {
    id: 'error-exists',
    title: 'Return Error',
    description: 'Email already registered error',
    type: 'end'
  },
  {
    id: 'hash-password',
    title: 'Hash Password',
    description: 'Encrypt password using BCrypt',
    type: 'process',
    next: ['save-user']
  },
  {
    id: 'save-user',
    title: 'Save User',
    description: 'Store user in PostgreSQL database',
    type: 'process',
    next: ['send-verification']
  },
  {
    id: 'send-verification',
    title: 'Send Verification Email',
    description: 'Queue email verification message',
    type: 'process',
    next: ['success']
  },
  {
    id: 'success',
    title: 'Registration Success',
    description: 'Return success response with user ID',
    type: 'end'
  }
]

const authenticationFlow: FlowStep[] = [
  {
    id: 'start',
    title: 'Login Request',
    description: 'Client submits credentials',
    type: 'start',
    next: ['validate-input']
  },
  {
    id: 'validate-input',
    title: 'Validate Input',
    description: 'Check email format and password presence',
    type: 'process',
    next: ['find-user']
  },
  {
    id: 'find-user',
    title: 'Find User',
    description: 'Query user by email from database',
    type: 'process',
    next: ['user-exists']
  },
  {
    id: 'user-exists',
    title: 'User Found?',
    description: 'Check if user exists in database',
    type: 'decision',
    next: ['verify-password', 'auth-failed']
  },
  {
    id: 'verify-password',
    title: 'Verify Password',
    description: 'Compare hashed password with BCrypt',
    type: 'process',
    next: ['password-valid']
  },
  {
    id: 'password-valid',
    title: 'Password Valid?',
    description: 'Check if password matches',
    type: 'decision',
    next: ['generate-jwt', 'auth-failed']
  },
  {
    id: 'generate-jwt',
    title: 'Generate JWT',
    description: 'Create JWT token with user claims',
    type: 'process',
    next: ['update-session']
  },
  {
    id: 'update-session',
    title: 'Update Session',
    description: 'Store session in Redis cache',
    type: 'process',
    next: ['auth-success']
  },
  {
    id: 'auth-success',
    title: 'Authentication Success',
    description: 'Return JWT token and user info',
    type: 'end'
  },
  {
    id: 'auth-failed',
    title: 'Authentication Failed',
    description: 'Return invalid credentials error',
    type: 'end'
  }
]

const FlowDiagram: React.FC<{ steps: FlowStep[]; title: string }> = ({ steps, title }) => {
  const [selectedStep, setSelectedStep] = useState<string | null>(null)
  
  const getStepIcon = (type: string) => {
    switch (type) {
      case 'start': return <Activity className="h-4 w-4" />
      case 'process': return <Settings className="h-4 w-4" />
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
    <div className="card">
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

export default function UserService() {
  const [activeTab, setActiveTab] = useState<'overview' | 'architecture' | 'flows' | 'api'>('overview')
  
  return (
    <div className="px-4 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center mb-4">
          <div className="p-3 bg-indigo-100 rounded-lg mr-4">
            <Users className="h-8 w-8 text-indigo-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">User Service</h1>
            <p className="text-lg text-gray-600">User Management and Authentication</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-4 text-sm text-gray-600">
          <div className="flex items-center">
            <Activity className="h-4 w-4 mr-1" />
            <span>Port: 8081</span>
          </div>
          <div className="flex items-center">
            <Database className="h-4 w-4 mr-1" />
            <span>PostgreSQL + Redis</span>
          </div>
          <div className="flex items-center">
            <Shield className="h-4 w-4 mr-1" />
            <span>JWT Authentication</span>
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
                    <User className="h-4 w-4 mr-2 mt-0.5 text-indigo-600" />
                    <span>User registration and profile management</span>
                  </li>
                  <li className="flex items-start">
                    <Lock className="h-4 w-4 mr-2 mt-0.5 text-indigo-600" />
                    <span>Authentication and authorization</span>
                  </li>
                  <li className="flex items-start">
                    <Key className="h-4 w-4 mr-2 mt-0.5 text-indigo-600" />
                    <span>JWT token generation and validation</span>
                  </li>
                  <li className="flex items-start">
                    <Mail className="h-4 w-4 mr-2 mt-0.5 text-indigo-600" />
                    <span>Email verification and password reset</span>
                  </li>
                  <li className="flex items-start">
                    <Shield className="h-4 w-4 mr-2 mt-0.5 text-indigo-600" />
                    <span>Role-based access control (RBAC)</span>
                  </li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Key Features</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li>• Secure password hashing with BCrypt</li>
                  <li>• Session management with Redis</li>
                  <li>• Email verification workflow</li>
                  <li>• Password reset functionality</li>
                  <li>• User profile CRUD operations</li>
                  <li>• Admin user management</li>
                  <li>• Audit logging for security events</li>
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
                  <li>• Spring Security</li>
                  <li>• Spring Data JPA</li>
                  <li>• JWT (JSON Web Tokens)</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Database</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• PostgreSQL (Primary)</li>
                  <li>• Redis (Sessions)</li>
                  <li>• JPA/Hibernate ORM</li>
                  <li>• Connection Pooling</li>
                  <li>• Database Migrations</li>
                </ul>
              </div>
              
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-3">Security</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                  <li>• BCrypt Password Hashing</li>
                  <li>• JWT Token Authentication</li>
                  <li>• CORS Configuration</li>
                  <li>• Input Validation</li>
                  <li>• Rate Limiting</li>
                </ul>
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
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  {/* Controller Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Controller Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">UserController</div>
                        <div className="text-xs text-blue-700 mt-1">REST Endpoints</div>
                      </div>
                      <div className="p-3 bg-blue-100 rounded-lg text-center">
                        <div className="font-medium text-blue-900">AuthController</div>
                        <div className="text-xs text-blue-700 mt-1">Authentication</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Service Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Service Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">UserService</div>
                        <div className="text-xs text-green-700 mt-1">Business Logic</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">AuthService</div>
                        <div className="text-xs text-green-700 mt-1">JWT Management</div>
                      </div>
                      <div className="p-3 bg-green-100 rounded-lg text-center">
                        <div className="font-medium text-green-900">EmailService</div>
                        <div className="text-xs text-green-700 mt-1">Notifications</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Data Layer */}
                  <div className="space-y-4">
                    <h3 className="font-semibold text-center text-gray-900">Data Layer</h3>
                    <div className="space-y-3">
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">UserRepository</div>
                        <div className="text-xs text-purple-700 mt-1">JPA Repository</div>
                      </div>
                      <div className="p-3 bg-purple-100 rounded-lg text-center">
                        <div className="font-medium text-purple-900">PostgreSQL</div>
                        <div className="text-xs text-purple-700 mt-1">Primary Database</div>
                      </div>
                      <div className="p-3 bg-red-100 rounded-lg text-center">
                        <div className="font-medium text-red-900">Redis</div>
                        <div className="text-xs text-red-700 mt-1">Session Cache</div>
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
            <div className="card">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Field</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Constraints</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    <tr>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">id</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">BIGINT</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">PRIMARY KEY, AUTO_INCREMENT</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">Unique user identifier</td>
                    </tr>
                    <tr>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">email</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">VARCHAR(255)</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">UNIQUE, NOT NULL</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">User email address</td>
                    </tr>
                    <tr>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">password</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">VARCHAR(255)</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">NOT NULL</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">BCrypt hashed password</td>
                    </tr>
                    <tr>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">first_name</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">VARCHAR(100)</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">NOT NULL</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">User's first name</td>
                    </tr>
                    <tr>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">last_name</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">VARCHAR(100)</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">NOT NULL</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">User's last name</td>
                    </tr>
                    <tr>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">role</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">VARCHAR(50)</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">DEFAULT 'USER'</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">User role (USER, ADMIN)</td>
                    </tr>
                    <tr>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">email_verified</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">BOOLEAN</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">DEFAULT FALSE</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">Email verification status</td>
                    </tr>
                    <tr>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">created_at</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">TIMESTAMP</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">DEFAULT CURRENT_TIMESTAMP</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">Account creation time</td>
                    </tr>
                    <tr>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">updated_at</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">TIMESTAMP</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">ON UPDATE CURRENT_TIMESTAMP</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">Last update time</td>
                    </tr>
                  </tbody>
                </table>
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
              <FlowDiagram steps={userRegistrationFlow} title="User Registration Flow" />
              <FlowDiagram steps={authenticationFlow} title="Authentication Flow" />
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
              {/* Authentication Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">Authentication Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-green-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-green-100 text-green-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/auth/register</code>
                    </div>
                    <p className="text-sm text-gray-600">Register a new user account</p>
                  </div>
                  
                  <div className="border-l-4 border-blue-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/auth/login</code>
                    </div>
                    <p className="text-sm text-gray-600">Authenticate user and return JWT token</p>
                  </div>
                  
                  <div className="border-l-4 border-yellow-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-yellow-100 text-yellow-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/auth/refresh</code>
                    </div>
                    <p className="text-sm text-gray-600">Refresh JWT token</p>
                  </div>
                  
                  <div className="border-l-4 border-red-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-red-100 text-red-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/auth/logout</code>
                    </div>
                    <p className="text-sm text-gray-600">Logout user and invalidate token</p>
                  </div>
                </div>
              </div>
              
              {/* User Management Endpoints */}
              <div className="card">
                <h3 className="font-semibold text-gray-900 mb-4">User Management Endpoints</h3>
                <div className="space-y-4">
                  <div className="border-l-4 border-indigo-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-indigo-100 text-indigo-800 text-xs font-medium px-2 py-1 rounded mr-2">GET</span>
                      <code className="text-sm font-mono">/api/users/profile</code>
                    </div>
                    <p className="text-sm text-gray-600">Get current user profile</p>
                  </div>
                  
                  <div className="border-l-4 border-purple-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-purple-100 text-purple-800 text-xs font-medium px-2 py-1 rounded mr-2">PUT</span>
                      <code className="text-sm font-mono">/api/users/profile</code>
                    </div>
                    <p className="text-sm text-gray-600">Update user profile</p>
                  </div>
                  
                  <div className="border-l-4 border-pink-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-pink-100 text-pink-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/users/change-password</code>
                    </div>
                    <p className="text-sm text-gray-600">Change user password</p>
                  </div>
                  
                  <div className="border-l-4 border-teal-400 pl-4">
                    <div className="flex items-center mb-2">
                      <span className="bg-teal-100 text-teal-800 text-xs font-medium px-2 py-1 rounded mr-2">POST</span>
                      <code className="text-sm font-mono">/api/users/verify-email</code>
                    </div>
                    <p className="text-sm text-gray-600">Verify user email address</p>
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