import React, { useState } from 'react'
import Head from 'next/head'
import Link from 'next/link'
import { useRouter } from 'next/router'
import { 
  Menu, 
  X, 
  Home, 
  Server, 
  GitBranch, 
  Database, 
  Settings, 
  BookOpen,
  Layers,
  Activity,
  Users,
  ShoppingCart,
  CreditCard,
  Package,
  Star,
  Truck,
  Bell,
  Search,
  Workflow,
  MessageSquare,
  FileText
} from 'lucide-react'

interface LayoutProps {
  children: React.ReactNode
}

const navigation = [
  { name: 'Overview', href: '/', icon: Home },
  { name: 'Architecture', href: '/architecture', icon: Layers },
  { name: 'Flow Diagrams', href: '/flows', icon: Workflow },
  { name: 'Sequence Diagrams', href: '/sequences', icon: MessageSquare },
  { name: 'Services', href: '/services', icon: Server },
  { name: 'README Files', href: '/readme', icon: FileText },
  { name: 'API Documentation', href: '/api', icon: BookOpen },
  { name: 'Deployment', href: '/deployment', icon: Settings },
]

const serviceNavigation = [
  { name: 'User Service', href: '/services/user-service', icon: Users },
  { name: 'Product Service', href: '/services/product-service', icon: Package },
  { name: 'Cart Service', href: '/services/cart-service', icon: ShoppingCart },
  { name: 'Order Service', href: '/services/order-service', icon: Activity },
  { name: 'Payment Service', href: '/services/payment-service', icon: CreditCard },
  { name: 'Inventory Service', href: '/services/inventory-service', icon: Database },
  { name: 'Recommendation Service', href: '/services/recommendation-service', icon: Star },
  { name: 'Shipping Service', href: '/services/shipping-service', icon: Truck },
  { name: 'Review Service', href: '/services/review-service', icon: Star },
  { name: 'Notification Service', href: '/services/notification-service', icon: Bell },
]

export default function Layout({ children }: LayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const router = useRouter()

  const isActive = (href: string) => {
    if (href === '/') {
      return router.pathname === '/'
    }
    return router.pathname.startsWith(href)
  }

  return (
    <>
      <Head>
        <title>E-commerce Microservices Documentation</title>
        <meta name="description" content="Comprehensive documentation for E-commerce Microservices Architecture" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.ico" />
      </Head>

      <div className="min-h-screen bg-gray-50">
        {/* Mobile sidebar */}
        <div className={`fixed inset-0 z-50 lg:hidden ${sidebarOpen ? 'block' : 'hidden'}`}>
          <div className="fixed inset-0 bg-gray-600 bg-opacity-75" onClick={() => setSidebarOpen(false)} />
          <div className="fixed inset-y-0 left-0 flex w-full max-w-xs flex-col bg-white shadow-xl">
            <div className="flex h-16 items-center justify-between px-4">
              <h2 className="text-lg font-semibold text-gray-900">Documentation</h2>
              <button
                type="button"
                className="text-gray-400 hover:text-gray-600"
                onClick={() => setSidebarOpen(false)}
              >
                <X className="h-6 w-6" />
              </button>
            </div>
            <nav className="flex-1 space-y-1 px-4 pb-4">
              {navigation.map((item) => {
                const Icon = item.icon
                return (
                  <Link
                    key={item.name}
                    href={item.href}
                    className={isActive(item.href) ? 'sidebar-link-active' : 'sidebar-link-inactive'}
                    onClick={() => setSidebarOpen(false)}
                  >
                    <Icon className="mr-3 h-5 w-5" />
                    {item.name}
                  </Link>
                )
              })}
              
              <div className="pt-4">
                <h3 className="px-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Services
                </h3>
                <div className="mt-2 space-y-1">
                  {serviceNavigation.map((item) => {
                    const Icon = item.icon
                    return (
                      <Link
                        key={item.name}
                        href={item.href}
                        className={isActive(item.href) ? 'sidebar-link-active' : 'sidebar-link-inactive'}
                        onClick={() => setSidebarOpen(false)}
                      >
                        <Icon className="mr-3 h-4 w-4" />
                        {item.name}
                      </Link>
                    )
                  })}
                </div>
              </div>
            </nav>
          </div>
        </div>

        {/* Desktop sidebar */}
        <div className="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-64 lg:flex-col">
          <div className="flex flex-col flex-grow bg-white border-r border-gray-200 pt-5 pb-4 overflow-y-auto">
            <div className="flex items-center flex-shrink-0 px-4">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="h-8 w-8 bg-gradient-primary rounded-lg flex items-center justify-center">
                    <Server className="h-5 w-5 text-white" />
                  </div>
                </div>
                <div className="ml-3">
                  <h1 className="text-lg font-semibold text-gray-900">E-commerce</h1>
                  <p className="text-sm text-gray-500">Microservices Docs</p>
                </div>
              </div>
            </div>
            
            <nav className="mt-8 flex-1 space-y-1 px-4">
              {navigation.map((item) => {
                const Icon = item.icon
                return (
                  <Link
                    key={item.name}
                    href={item.href}
                    className={isActive(item.href) ? 'sidebar-link-active' : 'sidebar-link-inactive'}
                  >
                    <Icon className="mr-3 h-5 w-5" />
                    {item.name}
                  </Link>
                )
              })}
              
              <div className="pt-6">
                <h3 className="px-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Microservices
                </h3>
                <div className="mt-2 space-y-1">
                  {serviceNavigation.map((item) => {
                    const Icon = item.icon
                    return (
                      <Link
                        key={item.name}
                        href={item.href}
                        className={isActive(item.href) ? 'sidebar-link-active' : 'sidebar-link-inactive'}
                      >
                        <Icon className="mr-3 h-4 w-4" />
                        {item.name}
                      </Link>
                    )
                  })}
                </div>
              </div>
            </nav>
          </div>
        </div>

        {/* Main content */}
        <div className="lg:pl-64">
          {/* Top navigation */}
          <div className="sticky top-0 z-40 bg-white shadow-sm border-b border-gray-200">
            <div className="flex h-16 items-center justify-between px-4 sm:px-6 lg:px-8">
              <button
                type="button"
                className="text-gray-500 hover:text-gray-600 lg:hidden"
                onClick={() => setSidebarOpen(true)}
              >
                <Menu className="h-6 w-6" />
              </button>
              
              <div className="flex items-center space-x-4">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <input
                    type="text"
                    placeholder="Search documentation..."
                    className="pl-10 pr-4 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  />
                </div>
                
                <a
                  href="https://github.com/your-repo/ecommerce-microservices"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-gray-500 hover:text-gray-700"
                >
                  <GitBranch className="h-5 w-5" />
                </a>
              </div>
            </div>
          </div>

          {/* Page content */}
          <main className="flex-1">
            <div className="py-6">
              {children}
            </div>
          </main>
        </div>
      </div>
    </>
  )
}