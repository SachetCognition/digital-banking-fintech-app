import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface ApiEndpoint {
  path: string;
  method: string;
  summary: string;
  description: string;
  tags: string[];
  parameters: Array<{
    name: string;
    in: string;
    required: boolean;
    description: string;
    schema: {
      type: string;
      example?: any;
    };
  }>;
  responses: Record<string, {
    description: string;
    content?: {
      'application/json': {
        schema: any;
        example?: any;
      };
    };
  }>;
}

interface ApiSpec {
  openapi: string;
  info: {
    title: string;
    version: string;
    description: string;
  };
  servers: Array<{
    url: string;
    description: string;
  }>;
  paths: Record<string, Record<string, ApiEndpoint>>;
  components: {
    securitySchemes: Record<string, any>;
  };
}

const ApiDocumentation: React.FC = () => {
  const [apiSpec, setApiSpec] = useState<ApiSpec | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedEndpoint, setSelectedEndpoint] = useState<ApiEndpoint | null>(null);
  const [selectedPath, setSelectedPath] = useState<string>('');
  const [selectedMethod, setSelectedMethod] = useState<string>('');
  const [activeTab, setActiveTab] = useState('overview');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTag, setSelectedTag] = useState('all');

  useEffect(() => {
    loadApiSpec();
  }, []);

  const loadApiSpec = async () => {
    try {
      setLoading(true);
      const response = await http.get('/api-docs');
      setApiSpec(response.data);
    } catch (err) {
      setError('Failed to load API documentation');
    } finally {
      setLoading(false);
    }
  };

  const getEndpoints = () => {
    if (!apiSpec) return [];
    
    const endpoints: Array<{ path: string; method: string; endpoint: ApiEndpoint }> = [];
    
    Object.entries(apiSpec.paths).forEach(([path, methods]) => {
      Object.entries(methods).forEach(([method, endpoint]) => {
        endpoints.push({ path, method: method.toUpperCase(), endpoint });
      });
    });
    
    return endpoints;
  };

  const getFilteredEndpoints = () => {
    const endpoints = getEndpoints();
    
    return endpoints.filter(({ endpoint }) => {
      const matchesSearch = endpoint.summary.toLowerCase().includes(searchQuery.toLowerCase()) ||
                           endpoint.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
                           endpoint.path.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesTag = selectedTag === 'all' || endpoint.tags.includes(selectedTag);
      return matchesSearch && matchesTag;
    });
  };

  const getTags = () => {
    if (!apiSpec) return [];
    
    const tags = new Set<string>();
    getEndpoints().forEach(({ endpoint }) => {
      endpoint.tags.forEach(tag => tags.add(tag));
    });
    
    return Array.from(tags).sort();
  };

  const getMethodColor = (method: string) => {
    switch (method.toUpperCase()) {
      case 'GET':
        return 'text-green-600 bg-green-100';
      case 'POST':
        return 'text-blue-600 bg-blue-100';
      case 'PUT':
        return 'text-yellow-600 bg-yellow-100';
      case 'DELETE':
        return 'text-red-600 bg-red-100';
      case 'PATCH':
        return 'text-purple-600 bg-purple-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const handleEndpointClick = (path: string, method: string, endpoint: ApiEndpoint) => {
    setSelectedPath(path);
    setSelectedMethod(method);
    setSelectedEndpoint(endpoint);
    setActiveTab('details');
  };

  const formatSchema = (schema: any): string => {
    if (!schema) return 'object';
    if (schema.type) return schema.type;
    if (schema.$ref) return schema.$ref.split('/').pop() || 'object';
    return 'object';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">Loading API documentation...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 text-6xl mb-4">⚠️</div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Error Loading Documentation</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={loadApiSpec}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">API Documentation</h1>
          <p className="mt-2 text-gray-600">Interactive API documentation and testing interface</p>
        </div>

        {apiSpec && (
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            {/* Sidebar */}
            <div className="lg:col-span-1">
              <div className="bg-white shadow rounded-lg p-6 mb-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">API Information</h3>
                <div className="space-y-2">
                  <div>
                    <span className="text-sm font-medium text-gray-500">Title:</span>
                    <p className="text-sm text-gray-900">{apiSpec.info.title}</p>
                  </div>
                  <div>
                    <span className="text-sm font-medium text-gray-500">Version:</span>
                    <p className="text-sm text-gray-900">{apiSpec.info.version}</p>
                  </div>
                  <div>
                    <span className="text-sm font-medium text-gray-500">OpenAPI:</span>
                    <p className="text-sm text-gray-900">{apiSpec.openapi}</p>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Filters</h3>
                
                <div className="mb-4">
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Search endpoints..."
                  />
                </div>

                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Tags</label>
                  <select
                    value={selectedTag}
                    onChange={(e) => setSelectedTag(e.target.value)}
                    className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="all">All Tags</option>
                    {getTags().map((tag) => (
                      <option key={tag} value={tag}>
                        {tag}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            {/* Main Content */}
            <div className="lg:col-span-3">
              {activeTab === 'overview' && (
                <div className="space-y-6">
                  <div className="bg-white shadow rounded-lg p-6">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4">API Overview</h2>
                    <p className="text-gray-600 mb-4">{apiSpec.info.description}</p>
                    
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                      <div className="bg-gray-50 p-4 rounded-lg">
                        <h3 className="text-sm font-medium text-gray-500">Total Endpoints</h3>
                        <p className="text-2xl font-bold text-gray-900">{getEndpoints().length}</p>
                      </div>
                      <div className="bg-gray-50 p-4 rounded-lg">
                        <h3 className="text-sm font-medium text-gray-500">Tags</h3>
                        <p className="text-2xl font-bold text-gray-900">{getTags().length}</p>
                      </div>
                      <div className="bg-gray-50 p-4 rounded-lg">
                        <h3 className="text-sm font-medium text-gray-500">Servers</h3>
                        <p className="text-2xl font-bold text-gray-900">{apiSpec.servers.length}</p>
                      </div>
                    </div>

                    <div>
                      <h3 className="text-lg font-medium text-gray-900 mb-4">Available Servers</h3>
                      <div className="space-y-2">
                        {apiSpec.servers.map((server, index) => (
                          <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                            <div>
                              <p className="text-sm font-medium text-gray-900">{server.url}</p>
                              <p className="text-sm text-gray-500">{server.description}</p>
                            </div>
                            <button
                              onClick={() => {
                                navigator.clipboard.writeText(server.url);
                              }}
                              className="text-blue-600 hover:text-blue-800 text-sm"
                            >
                              Copy URL
                            </button>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>

                  <div className="bg-white shadow rounded-lg">
                    <div className="px-6 py-4 border-b border-gray-200">
                      <h2 className="text-lg font-medium text-gray-900">API Endpoints</h2>
                    </div>
                    <div className="divide-y divide-gray-200">
                      {getFilteredEndpoints().map(({ path, method, endpoint }) => (
                        <div
                          key={`${path}-${method}`}
                          className="px-6 py-4 hover:bg-gray-50 cursor-pointer"
                          onClick={() => handleEndpointClick(path, method, endpoint)}
                        >
                          <div className="flex items-center justify-between">
                            <div className="flex-1">
                              <div className="flex items-center space-x-3">
                                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getMethodColor(method)}`}>
                                  {method}
                                </span>
                                <code className="text-sm font-mono text-gray-900">{path}</code>
                              </div>
                              <h3 className="text-sm font-medium text-gray-900 mt-1">
                                {endpoint.summary}
                              </h3>
                              <p className="text-sm text-gray-600 mt-1">
                                {endpoint.description}
                              </p>
                              <div className="flex flex-wrap gap-1 mt-2">
                                {endpoint.tags.map((tag) => (
                                  <span
                                    key={tag}
                                    className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800"
                                  >
                                    {tag}
                                  </span>
                                ))}
                              </div>
                            </div>
                            <div className="ml-4">
                              <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                              </svg>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {activeTab === 'details' && selectedEndpoint && (
                <div className="space-y-6">
                  <div className="bg-white shadow rounded-lg p-6">
                    <div className="flex items-center justify-between mb-4">
                      <div className="flex items-center space-x-3">
                        <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${getMethodColor(selectedMethod)}`}>
                          {selectedMethod}
                        </span>
                        <code className="text-lg font-mono text-gray-900">{selectedPath}</code>
                      </div>
                      <button
                        onClick={() => setActiveTab('overview')}
                        className="text-gray-500 hover:text-gray-700"
                      >
                        ← Back to Overview
                      </button>
                    </div>
                    
                    <h2 className="text-xl font-semibold text-gray-900 mb-2">
                      {selectedEndpoint.summary}
                    </h2>
                    <p className="text-gray-600 mb-6">{selectedEndpoint.description}</p>

                    {/* Parameters */}
                    {selectedEndpoint.parameters && selectedEndpoint.parameters.length > 0 && (
                      <div className="mb-6">
                        <h3 className="text-lg font-medium text-gray-900 mb-4">Parameters</h3>
                        <div className="overflow-x-auto">
                          <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                              <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">In</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Required</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                              </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                              {selectedEndpoint.parameters.map((param, index) => (
                                <tr key={index}>
                                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                    {param.name}
                                  </td>
                                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                    {param.in}
                                  </td>
                                  <td className="px-6 py-4 whitespace-nowrap">
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                      param.required ? 'text-red-600 bg-red-100' : 'text-gray-600 bg-gray-100'
                                    }`}>
                                      {param.required ? 'Required' : 'Optional'}
                                    </span>
                                  </td>
                                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                    {formatSchema(param.schema)}
                                  </td>
                                  <td className="px-6 py-4 text-sm text-gray-500">
                                    {param.description}
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    )}

                    {/* Responses */}
                    <div>
                      <h3 className="text-lg font-medium text-gray-900 mb-4">Responses</h3>
                      <div className="space-y-4">
                        {Object.entries(selectedEndpoint.responses).map(([statusCode, response]) => (
                          <div key={statusCode} className="border border-gray-200 rounded-lg p-4">
                            <div className="flex items-center justify-between mb-2">
                              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                statusCode.startsWith('2') ? 'text-green-600 bg-green-100' :
                                statusCode.startsWith('4') ? 'text-red-600 bg-red-100' :
                                statusCode.startsWith('5') ? 'text-red-600 bg-red-100' :
                                'text-gray-600 bg-gray-100'
                              }`}>
                                {statusCode}
                              </span>
                            </div>
                            <p className="text-sm text-gray-600 mb-2">{response.description}</p>
                            {response.content && response.content['application/json'] && (
                              <div className="mt-2">
                                <h4 className="text-sm font-medium text-gray-900 mb-1">Response Body</h4>
                                <pre className="bg-gray-50 p-3 rounded text-xs overflow-x-auto">
                                  {JSON.stringify(response.content['application/json'].example || {}, null, 2)}
                                </pre>
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ApiDocumentation;

