import React, { useState } from 'react';
import { GetStaticProps } from 'next';
import { serviceReadmes, projectReadmes, ServiceReadme } from '../utils/readmeLoader';
import fs from 'fs';
import path from 'path';

interface ReadmePageProps {
  readmeContents: Record<string, string>;
}

export default function ReadmeDocumentation({ readmeContents }: ReadmePageProps) {
  const [expandedServices, setExpandedServices] = useState<Set<string>>(new Set());
  const [selectedService, setSelectedService] = useState<string | null>(null);

  const toggleService = (servicePath: string) => {
    const newExpanded = new Set(expandedServices);
    if (newExpanded.has(servicePath)) {
      newExpanded.delete(servicePath);
    } else {
      newExpanded.add(servicePath);
    }
    setExpandedServices(newExpanded);
  };

  const selectService = (servicePath: string) => {
    setSelectedService(servicePath);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            Service Documentation
          </h1>
          <p className="text-xl text-gray-600 max-w-3xl">
            Comprehensive README documentation for all microservices in the e-commerce platform.
            Each service includes setup instructions, API documentation, and configuration details.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Service List */}
          <div className="lg:col-span-1 space-y-6">
            {/* Project Documentation */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Project Documentation
              </h2>
              <div className="space-y-2">
                {projectReadmes.map((doc) => (
                  <div key={doc.path} className="border border-gray-100 rounded-lg">
                    <button
                      onClick={() => selectService(doc.path)}
                      className={`w-full text-left p-3 rounded-lg transition-colors ${
                        selectedService === doc.path
                          ? 'bg-blue-50 border-blue-200'
                          : 'hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <h3 className="font-medium text-gray-900">
                            {doc.name}
                          </h3>
                          <p className="text-sm text-gray-500 mt-1">
                            {doc.description}
                          </p>
                        </div>
                        <span className="text-gray-400">→</span>
                      </div>
                    </button>
                  </div>
                ))}
              </div>
            </div>
            
            {/* Microservices Documentation */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Microservices
              </h2>
              <div className="space-y-2">
                {serviceReadmes.map((service) => (
                  <div key={service.path} className="border border-gray-100 rounded-lg">
                    <button
                      onClick={() => selectService(service.path)}
                      className={`w-full text-left p-3 rounded-lg transition-colors ${
                        selectedService === service.path
                          ? 'bg-blue-50 border-blue-200'
                          : 'hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <h3 className="font-medium text-gray-900">
                            {service.name}
                          </h3>
                          <p className="text-sm text-gray-500 mt-1">
                            {service.description}
                          </p>
                        </div>
                        <span className="text-gray-400">→</span>
                      </div>
                    </button>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* README Content */}
          <div className="lg:col-span-2">
            {selectedService ? (
              <ReadmeViewer servicePath={selectedService} readmeContents={readmeContents} />
            ) : (
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8 text-center">
                <div className="text-gray-400 mb-4">
                  <div className="mx-auto h-12 w-12 flex items-center justify-center text-2xl">📄</div>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  Select a Service
                </h3>
                <p className="text-gray-500">
                  Choose a microservice from the list to view its README documentation.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function ReadmeViewer({ servicePath, readmeContents }: { servicePath: string; readmeContents: Record<string, string> }) {
  const service = serviceReadmes.find(s => s.path === servicePath) || projectReadmes.find(s => s.path === servicePath);
  
  if (!service) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
        <p className="text-gray-500">Service not found.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      <div className="border-b border-gray-200 p-6">
        <h2 className="text-2xl font-bold text-gray-900">
          {service.name} README
        </h2>
        <p className="text-gray-600 mt-2">
          {service.description}
        </p>
      </div>
      
      <div className="p-6">
        <div className="prose max-w-none">
          <ReadmeContent servicePath={servicePath} content={readmeContents[servicePath] || 'README content not found.'} />
        </div>
      </div>
    </div>
  );
}

function ReadmeContent({ servicePath, content }: { servicePath: string; content: string }) {
  // Simple markdown-like rendering for basic formatting
  const renderMarkdown = (text: string) => {
    return text
      .split('\n')
      .map((line, index) => {
        // Headers
        if (line.startsWith('# ')) {
          return <h1 key={index} className="text-2xl font-bold text-gray-900 mb-4">{line.slice(2)}</h1>;
        }
        if (line.startsWith('## ')) {
          return <h2 key={index} className="text-xl font-semibold text-gray-900 mb-3 mt-6">{line.slice(3)}</h2>;
        }
        if (line.startsWith('### ')) {
          return <h3 key={index} className="text-lg font-medium text-gray-900 mb-2 mt-4">{line.slice(4)}</h3>;
        }
        
        // Code blocks
        if (line.startsWith('```')) {
          return <div key={index} className="bg-gray-50 rounded-lg p-4 my-4"><code className="text-sm">{line}</code></div>;
        }
        
        // Lists
        if (line.startsWith('- ')) {
          return <li key={index} className="ml-4 text-gray-700">{line.slice(2)}</li>;
        }
        
        // Regular paragraphs
        if (line.trim()) {
          return <p key={index} className="text-gray-700 mb-2">{line}</p>;
        }
        
        // Empty lines
        return <br key={index} />;
      });
  };

  return (
     <div className="space-y-4">
       <div className="prose max-w-none">
         {renderMarkdown(content)}
       </div>
     </div>
   );
 }

function loadReadmeContent(servicePath: string): string {
  try {
    // Get the project root (assuming we're in docs)
    const projectRoot = path.resolve(process.cwd(), '..');
    
    let readmePath: string;
    if (servicePath === '.') {
      readmePath = path.join(projectRoot, 'README.md');
    } else if (servicePath === 'QUICK_START' || servicePath === 'TRACING_SETUP') {
      readmePath = path.join(projectRoot, `${servicePath}.md`);
    } else {
      readmePath = path.join(projectRoot, servicePath, 'README.md');
    }
    
    if (fs.existsSync(readmePath)) {
      return fs.readFileSync(readmePath, 'utf-8');
    } else {
      return generatePlaceholderReadme(servicePath);
    }
  } catch (error) {
    console.error(`Error loading README for ${servicePath}:`, error);
    return generatePlaceholderReadme(servicePath);
  }
}

function generatePlaceholderReadme(servicePath: string): string {
  const serviceName = servicePath === '.' ? 'Main Project' : 
    servicePath.split('-').map(word => 
      word.charAt(0).toUpperCase() + word.slice(1)
    ).join(' ');
  
  return `# ${serviceName}

## Overview
This service is part of the e-commerce microservices architecture.

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker (optional)

## Quick Start
\`\`\`bash
# Clone the repository
git clone <repository-url>

# Navigate to service directory
cd ${servicePath === '.' ? 'ecom' : servicePath}

# Build the service
mvn clean install

# Run the service
mvn spring-boot:run
\`\`\`

## Configuration
Service configuration is managed through application.yml files.

## API Endpoints
For detailed API documentation, refer to the service-specific documentation pages.
`;
}

export const getStaticProps: GetStaticProps = async () => {
  const readmeContents: Record<string, string> = {};
  
  // Load README content for project docs and services
  for (const doc of projectReadmes) {
    readmeContents[doc.path] = loadReadmeContent(doc.path);
  }
  for (const service of serviceReadmes) {
    readmeContents[service.path] = loadReadmeContent(service.path);
  }
  
  return {
    props: {
      readmeContents,
    },
    // Regenerate the page at most once every hour
    revalidate: 3600,
  };
};