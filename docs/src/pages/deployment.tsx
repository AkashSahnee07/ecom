import React, { useState } from 'react';
import { CheckCircle, AlertTriangle, Info, Terminal, Server, Cloud, Container } from 'lucide-react';

const DeploymentGuide = () => {
  const [activeTab, setActiveTab] = useState('overview');

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-4">Deployment Guide</h1>
        <p className="text-xl text-gray-600">
          Comprehensive deployment strategies for the e-commerce microservices platform
        </p>
      </div>

      <div className="space-y-6">
        <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg">
          {[
            { key: 'overview', label: 'Overview' },
            { key: 'local', label: 'Local Development' },
            { key: 'docker', label: 'Docker' },
            { key: 'kubernetes', label: 'Kubernetes' },
            { key: 'cloud', label: 'Cloud Deployment' },
            { key: 'monitoring', label: 'Monitoring' }
          ].map(tab => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
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

        {activeTab === 'overview' && (
          <div className="space-y-6">
            <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
              <div className="mb-6">
                <h2 className="text-2xl font-semibold text-gray-900 mb-2 flex items-center gap-2">
                  <Server className="h-5 w-5" />
                  Deployment Overview
                </h2>
                <p className="text-gray-600">
                  Choose the right deployment strategy for your environment
                </p>
              </div>
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  <div className="bg-white rounded-lg border-2 border-blue-200 p-6 shadow-sm">
                    <div className="mb-4">
                      <h3 className="text-lg font-semibold flex items-center gap-2">
                        <Terminal className="h-4 w-4" />
                        Local Development
                      </h3>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 mb-3">
                        Perfect for development and testing
                      </p>
                      <div className="space-y-2">
                        <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded border mr-2">Quick Setup</span>
                        <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded border mr-2">Hot Reload</span>
                        <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded border">Debug Mode</span>
                      </div>
                    </div>
                  </div>

                  <div className="bg-white rounded-lg border-2 border-green-200 p-6 shadow-sm">
                    <div className="mb-4">
                      <h3 className="text-lg font-semibold flex items-center gap-2">
                        <Container className="h-4 w-4" />
                        Docker Compose
                      </h3>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 mb-3">
                        Containerized deployment for consistency
                      </p>
                      <div className="space-y-2">
                        <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded border mr-2">Isolated</span>
                        <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded border mr-2">Scalable</span>
                        <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded border">Portable</span>
                      </div>
                    </div>
                  </div>

                  <div className="bg-white rounded-lg border-2 border-purple-200 p-6 shadow-sm">
                    <div className="mb-4">
                      <h3 className="text-lg font-semibold flex items-center gap-2">
                        <Cloud className="h-4 w-4" />
                        Production
                      </h3>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 mb-3">
                        Enterprise-grade deployment
                      </p>
                      <div className="space-y-2">
                        <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded border mr-2">High Availability</span>
                        <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded border mr-2">Auto-scaling</span>
                        <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-800 rounded border">Load Balancing</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <div className="flex items-start">
                    <Info className="h-4 w-4 text-blue-500 mt-0.5 mr-3" />
                    <div>
                      <strong>Recommendation:</strong> Start with local development, then move to Docker for staging, 
                      and finally Kubernetes for production deployment.
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
              <div className="mb-6">
                <h2 className="text-2xl font-semibold text-gray-900">Infrastructure Requirements</h2>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h4 className="font-semibold mb-3">Minimum Requirements</h4>
                  <ul className="space-y-2 text-sm">
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-500" />
                      4 CPU cores
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-500" />
                      8GB RAM
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-500" />
                      50GB Storage
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-500" />
                      Docker 20.10+
                    </li>
                  </ul>
                </div>
                <div>
                  <h4 className="font-semibold mb-3">Recommended for Production</h4>
                  <ul className="space-y-2 text-sm">
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-blue-500" />
                      16+ CPU cores
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-blue-500" />
                      32GB+ RAM
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-blue-500" />
                      500GB+ SSD Storage
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-blue-500" />
                      Kubernetes 1.25+
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'local' && (
          <div className="space-y-6">
            <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
              <div className="mb-6">
                <h2 className="text-2xl font-semibold text-gray-900">Local Development Setup</h2>
                <p className="text-gray-600">
                  Step-by-step guide to run the platform locally
                </p>
              </div>
              <div className="space-y-6">
                <div className="space-y-4">
                  <div>
                    <h4 className="font-semibold mb-2">1. Prerequisites Installation</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Install Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Install Maven
sudo apt install maven

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose-plugin`}</pre>
                    </div>
                  </div>

                  <div>
                    <h4 className="font-semibold mb-2">2. Clone and Setup</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Clone the repository
git clone <repository-url>
cd ecom

# Make scripts executable
chmod +x *.sh

# Build all services
mvn clean install -DskipTests`}</pre>
                    </div>
                  </div>

                  <div>
                    <h4 className="font-semibold mb-2">3. Start Infrastructure</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Start databases and supporting services
docker-compose up -d

# Wait for services to be ready
docker-compose logs -f`}</pre>
                    </div>
                  </div>

                  <div>
                    <h4 className="font-semibold mb-2">4. Start Microservices</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Option 1: Use automated script
./start-services.sh

# Option 2: Manual startup (in order)
./manage-services.sh start eureka-server
./manage-services.sh start config-server
./manage-services.sh start api-gateway
./manage-services.sh start user-service
# ... continue with other services`}</pre>
                    </div>
                  </div>

                  <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                    <div className="flex items-start">
                      <AlertTriangle className="h-4 w-4 text-yellow-500 mt-0.5 mr-3" />
                      <div>
                        Services must be started in the correct order. The startup script handles this automatically.
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
              <div className="mb-6">
                <h2 className="text-2xl font-semibold text-gray-900">Service Verification</h2>
              </div>
              <div className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <h4 className="font-semibold mb-2">Health Check URLs</h4>
                    <div className="space-y-1 text-sm">
                      <div>Eureka: <code className="bg-gray-100 px-1 rounded">http://localhost:8761</code></div>
                      <div>Config Server: <code className="bg-gray-100 px-1 rounded">http://localhost:8888/actuator/health</code></div>
                      <div>API Gateway: <code className="bg-gray-100 px-1 rounded">http://localhost:8080/actuator/health</code></div>
                      <div>User Service: <code className="bg-gray-100 px-1 rounded">http://localhost:8081/actuator/health</code></div>
                    </div>
                  </div>
                  <div>
                    <h4 className="font-semibold mb-2">Management Commands</h4>
                    <div className="space-y-1 text-sm">
                      <div><code className="bg-gray-100 px-1 rounded">./manage-services.sh status</code> - Check all services</div>
                      <div><code className="bg-gray-100 px-1 rounded">./manage-services.sh stop all</code> - Stop all services</div>
                      <div><code className="bg-gray-100 px-1 rounded">./manage-services.sh restart &lt;service&gt;</code> - Restart service</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'docker' && (
          <div className="space-y-6">
            <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
              <div className="mb-6">
                <h2 className="text-2xl font-semibold text-gray-900">Docker Deployment</h2>
                <p className="text-gray-600">
                  Containerized deployment with Docker Compose
                </p>
              </div>
              <div className="space-y-6">
                <div className="space-y-4">
                  <div>
                    <h4 className="font-semibold mb-2">Infrastructure Services</h4>
                    <p className="text-sm text-gray-600 mb-3">
                      The docker-compose.yml includes all necessary infrastructure components:
                    </p>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                      <span className="inline-block px-2 py-1 text-xs bg-gray-200 text-gray-800 rounded">PostgreSQL</span>
                      <span className="inline-block px-2 py-1 text-xs bg-gray-200 text-gray-800 rounded">MySQL</span>
                      <span className="inline-block px-2 py-1 text-xs bg-gray-200 text-gray-800 rounded">Redis</span>
                      <span className="inline-block px-2 py-1 text-xs bg-gray-200 text-gray-800 rounded">Kafka</span>
                      <span className="inline-block px-2 py-1 text-xs bg-gray-200 text-gray-800 rounded">Zookeeper</span>
                      <span className="inline-block px-2 py-1 text-xs bg-gray-200 text-gray-800 rounded">Zipkin</span>
                      <span className="inline-block px-2 py-1 text-xs bg-gray-200 text-gray-800 rounded">Elasticsearch</span>
                      <span className="inline-block px-2 py-1 text-xs bg-gray-200 text-gray-800 rounded">Kibana</span>
                    </div>
                  </div>

                  <div>
                    <h4 className="font-semibold mb-2">Quick Start</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Start all infrastructure services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f [service-name]

# Stop all services
docker-compose down`}</pre>
                    </div>
                  </div>

                  <div>
                    <h4 className="font-semibold mb-2">Building Microservice Images</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Build individual service
cd user-service
docker build -t ecom/user-service:latest .

# Build all services
for service in */; do
  if [ -f "$service/Dockerfile" ]; then
    cd "$service"
    docker build -t "ecom/\${service%/}:latest" .
    cd ..
  fi
done`}</pre>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'kubernetes' && (
          <div className="space-y-6">
            <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
              <div className="mb-6">
                <h2 className="text-2xl font-semibold text-gray-900">Kubernetes Deployment</h2>
                <p className="text-gray-600">
                  Production-ready Kubernetes deployment manifests
                </p>
              </div>
              <div className="space-y-6">
                <div className="space-y-4">
                  <div>
                    <h4 className="font-semibold mb-2">Namespace Setup</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Create namespace
kubectl create namespace ecommerce

# Set default namespace
kubectl config set-context --current --namespace=ecommerce`}</pre>
                    </div>
                  </div>

                  <div>
                    <h4 className="font-semibold mb-2">ConfigMaps and Secrets</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Create database secrets
kubectl create secret generic db-credentials \
  --from-literal=postgres-user=ecommerce \
  --from-literal=postgres-password=ecommerce123 \
  --from-literal=mysql-user=product_user \
  --from-literal=mysql-password=product123

# Create JWT secret
kubectl create secret generic jwt-secret \
  --from-literal=jwt-key=your-super-secret-jwt-key`}</pre>
                    </div>
                  </div>

                  <div>
                    <h4 className="font-semibold mb-2">Infrastructure Deployment</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Deploy databases
kubectl apply -f k8s/infrastructure/postgres.yaml
kubectl apply -f k8s/infrastructure/mysql.yaml
kubectl apply -f k8s/infrastructure/redis.yaml

# Deploy messaging
kubectl apply -f k8s/infrastructure/kafka.yaml
kubectl apply -f k8s/infrastructure/zookeeper.yaml

# Deploy monitoring
kubectl apply -f k8s/infrastructure/zipkin.yaml`}</pre>
                    </div>
                  </div>

                  <div>
                    <h4 className="font-semibold mb-2">Microservices Deployment</h4>
                    <div className="bg-gray-100 p-4 rounded-lg">
                      <pre className="text-sm">{`# Deploy core services
kubectl apply -f k8s/services/eureka-server.yaml
kubectl apply -f k8s/services/config-server.yaml
kubectl apply -f k8s/services/api-gateway.yaml

# Deploy business services
kubectl apply -f k8s/services/user-service.yaml
kubectl apply -f k8s/services/product-service.yaml
kubectl apply -f k8s/services/order-service.yaml
# ... continue with other services`}</pre>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'cloud' && (
          <div className="space-y-6">
            <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
              <div className="mb-6">
                <h2 className="text-2xl font-semibold text-gray-900">Cloud Deployment Options</h2>
                <p className="text-gray-600">
                  Deploy to major cloud providers
                </p>
              </div>
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-white rounded-lg border-2 border-orange-200 p-6 shadow-sm">
                    <div className="mb-4">
                      <h3 className="text-lg font-semibold">AWS EKS</h3>
                    </div>
                    <ul className="text-sm space-y-1">
                      <li>• Managed Kubernetes</li>
                      <li>• RDS for databases</li>
                      <li>• ElastiCache for Redis</li>
                      <li>• MSK for Kafka</li>
                      <li>• ALB for load balancing</li>
                    </ul>
                  </div>

                  <div className="bg-white rounded-lg border-2 border-blue-200 p-6 shadow-sm">
                    <div className="mb-4">
                      <h3 className="text-lg font-semibold">Azure AKS</h3>
                    </div>
                    <ul className="text-sm space-y-1">
                      <li>• Azure Kubernetes Service</li>
                      <li>• Azure Database</li>
                      <li>• Azure Cache for Redis</li>
                      <li>• Event Hubs for Kafka</li>
                      <li>• Application Gateway</li>
                    </ul>
                  </div>

                  <div className="bg-white rounded-lg border-2 border-green-200 p-6 shadow-sm">
                    <div className="mb-4">
                      <h3 className="text-lg font-semibold">Google GKE</h3>
                    </div>
                    <ul className="text-sm space-y-1">
                      <li>• Google Kubernetes Engine</li>
                      <li>• Cloud SQL</li>
                      <li>• Memorystore for Redis</li>
                      <li>• Pub/Sub for messaging</li>
                      <li>• Cloud Load Balancing</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'monitoring' && (
          <div className="space-y-6">
            <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
              <div className="mb-6">
                <h2 className="text-2xl font-semibold text-gray-900">Monitoring and Observability</h2>
                <p className="text-gray-600">
                  Comprehensive monitoring setup for production environments
                </p>
              </div>
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <h4 className="font-semibold mb-3">Metrics Collection</h4>
                    <ul className="space-y-2 text-sm">
                      <li className="flex items-center gap-2">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        Prometheus for metrics scraping
                      </li>
                      <li className="flex items-center gap-2">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        Grafana for visualization
                      </li>
                      <li className="flex items-center gap-2">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        Spring Boot Actuator endpoints
                      </li>
                      <li className="flex items-center gap-2">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        Custom business metrics
                      </li>
                    </ul>
                  </div>
                  <div>
                    <h4 className="font-semibold mb-3">Distributed Tracing</h4>
                    <ul className="space-y-2 text-sm">
                      <li className="flex items-center gap-2">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        Zipkin for trace collection
                      </li>
                      <li className="flex items-center gap-2">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        Spring Cloud Sleuth integration
                      </li>
                      <li className="flex items-center gap-2">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        Request correlation IDs
                      </li>
                      <li className="flex items-center gap-2">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        Performance bottleneck identification
                      </li>
                    </ul>
                  </div>
                </div>

                <div>
                  <h4 className="font-semibold mb-2">Monitoring Stack Setup</h4>
                  <div className="bg-gray-100 p-4 rounded-lg">
                    <pre className="text-sm">{`# Deploy Prometheus
kubectl apply -f k8s/monitoring/prometheus/

# Deploy Grafana
kubectl apply -f k8s/monitoring/grafana/

# Deploy AlertManager
kubectl apply -f k8s/monitoring/alertmanager/

# Access Grafana dashboard
kubectl port-forward svc/grafana 3000:3000`}</pre>
                  </div>
                </div>

                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <div className="flex items-start">
                    <Info className="h-4 w-4 text-blue-500 mt-0.5 mr-3" />
                    <div>
                      All microservices expose metrics at <code className="bg-gray-100 px-1 rounded">/actuator/prometheus</code> endpoint for Prometheus scraping.
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default DeploymentGuide;