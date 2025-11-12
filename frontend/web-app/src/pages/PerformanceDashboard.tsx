import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface PerformanceMetrics {
  serviceName: string;
  endpoint: string;
  method: string;
  responseTimeMs: number;
  statusCode: number;
  requestSizeBytes: number;
  responseSizeBytes: number;
  userId: string;
  sessionId: string;
  ipAddress: string;
  userAgent: string;
  timestamp: string;
}

interface SystemMetrics {
  cpuUsage: number;
  memoryUsage: {
    usagePercent: number;
    usedMb: number;
    totalMb: number;
  };
  diskUsage: {
    usagePercent: number;
    usedGb: number;
    totalGb: number;
  };
  networkUsage: {
    inBytes: number;
    outBytes: number;
  };
}

interface ApplicationMetrics {
  responseTime: {
    average: number;
    p95: number;
    p99: number;
  };
  throughput: {
    requestsPerSecond: number;
    requestsPerMinute: number;
  };
  errorRate: {
    rate: number;
    threshold: number;
  };
}

interface DatabaseMetrics {
  connectionPool: {
    activeConnections: number;
    maxConnections: number;
    usagePercent: number;
  };
  queryPerformance: {
    averageExecutionTime: number;
    maxExecutionTime: number;
  };
}

interface CacheMetrics {
  hitRates: Record<string, number>;
  cacheSizes: Record<string, number>;
}

interface PerformanceAlert {
  id: string;
  alertType: string;
  serviceName: string;
  severity: string;
  message: string;
  createdAt: string;
}

interface PerformanceDashboard {
  systemMetrics: SystemMetrics;
  applicationMetrics: ApplicationMetrics;
  databaseMetrics: DatabaseMetrics;
  cacheMetrics: CacheMetrics;
  alerts: PerformanceAlert[];
  healthScore: number;
}

const PerformanceDashboard: React.FC = () => {
  const [dashboard, setDashboard] = useState<PerformanceDashboard | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [timeRange, setTimeRange] = useState('1h');

  useEffect(() => {
    loadPerformanceData();
  }, [timeRange]);

  const loadPerformanceData = async () => {
    try {
      setLoading(true);
      const response = await http.get(`/api/v1/performance/dashboard?timeRange=${timeRange}`);
      setDashboard(response.data);
    } catch (err) {
      setError('Failed to load performance data');
    } finally {
      setLoading(false);
    }
  };

  const getHealthScoreColor = (score: number) => {
    if (score >= 90) return 'text-green-600 bg-green-100';
    if (score >= 70) return 'text-yellow-600 bg-yellow-100';
    if (score >= 50) return 'text-orange-600 bg-orange-100';
    return 'text-red-600 bg-red-100';
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return 'text-red-600 bg-red-100';
      case 'HIGH':
        return 'text-orange-600 bg-orange-100';
      case 'MEDIUM':
        return 'text-yellow-600 bg-yellow-100';
      case 'LOW':
        return 'text-blue-600 bg-blue-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const formatBytes = (bytes: number) => {
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    if (bytes === 0) return '0 Bytes';
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i];
  };

  const formatPercentage = (value: number) => {
    return Math.round(value * 100) / 100 + '%';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">Loading performance data...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 text-6xl mb-4">⚠️</div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Error Loading Dashboard</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={loadPerformanceData}
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
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Performance Dashboard</h1>
              <p className="mt-2 text-gray-600">Real-time performance monitoring and optimization</p>
            </div>
            <div className="flex items-center space-x-4">
              <select
                value={timeRange}
                onChange={(e) => setTimeRange(e.target.value)}
                className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="1h">Last Hour</option>
                <option value="24h">Last 24 Hours</option>
                <option value="7d">Last 7 Days</option>
                <option value="30d">Last 30 Days</option>
              </select>
              <button
                onClick={loadPerformanceData}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                Refresh
              </button>
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="mb-8">
          <nav className="flex space-x-8">
            {[
              { id: 'overview', name: 'Overview' },
              { id: 'system', name: 'System' },
              { id: 'application', name: 'Application' },
              { id: 'database', name: 'Database' },
              { id: 'cache', name: 'Cache' },
              { id: 'alerts', name: 'Alerts' }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                {tab.name}
              </button>
            ))}
          </nav>
        </div>

        {/* Overview Tab */}
        {activeTab === 'overview' && dashboard && (
          <div className="space-y-6">
            {/* Health Score */}
            <div className="bg-white shadow rounded-lg p-6">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-lg font-medium text-gray-900">Overall Health Score</h3>
                  <p className="text-sm text-gray-500">System performance indicator</p>
                </div>
                <div className="text-right">
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-2xl font-bold ${getHealthScoreColor(dashboard.healthScore)}`}>
                    {dashboard.healthScore}
                  </span>
                </div>
              </div>
            </div>

            {/* Key Metrics */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="bg-white shadow rounded-lg p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                      <span className="text-blue-600 text-sm font-medium">⚡</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Avg Response Time</p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {dashboard.applicationMetrics.responseTime.average.toFixed(0)}ms
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                      <span className="text-green-600 text-sm font-medium">📊</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Throughput</p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {dashboard.applicationMetrics.throughput.requestsPerSecond.toFixed(0)}/s
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center">
                      <span className="text-red-600 text-sm font-medium">❌</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Error Rate</p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {formatPercentage(dashboard.applicationMetrics.errorRate.rate)}
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center">
                      <span className="text-purple-600 text-sm font-medium">💾</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Cache Hit Rate</p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {formatPercentage(Object.values(dashboard.cacheMetrics.hitRates)[0] || 0)}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Recent Alerts */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Recent Alerts</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {dashboard.alerts.slice(0, 5).map((alert) => (
                  <div key={alert.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(alert.severity)}`}>
                            {alert.severity}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{alert.alertType}</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">{alert.message}</p>
                        <p className="text-xs text-gray-500 mt-1">
                          {alert.serviceName} • {new Date(alert.createdAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* System Tab */}
        {activeTab === 'system' && dashboard && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">CPU Usage</h3>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold text-gray-900">
                    {formatPercentage(dashboard.systemMetrics.cpuUsage)}
                  </span>
                  <div className="w-16 h-16">
                    <svg className="w-16 h-16 transform -rotate-90" viewBox="0 0 36 36">
                      <path
                        className="text-gray-200"
                        stroke="currentColor"
                        strokeWidth="3"
                        fill="none"
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                      <path
                        className="text-blue-600"
                        stroke="currentColor"
                        strokeWidth="3"
                        fill="none"
                        strokeDasharray={`${dashboard.systemMetrics.cpuUsage}, 100`}
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      />
                    </svg>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Memory Usage</h3>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Used</span>
                    <span className="text-gray-900">{dashboard.systemMetrics.memoryUsage.usedMb} MB</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Total</span>
                    <span className="text-gray-900">{dashboard.systemMetrics.memoryUsage.totalMb} MB</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-blue-600 h-2 rounded-full"
                      style={{ width: `${dashboard.systemMetrics.memoryUsage.usagePercent}%` }}
                    ></div>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Disk Usage</h3>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Used</span>
                    <span className="text-gray-900">{dashboard.systemMetrics.diskUsage.usedGb} GB</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Total</span>
                    <span className="text-gray-900">{dashboard.systemMetrics.diskUsage.totalGb} GB</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-green-600 h-2 rounded-full"
                      style={{ width: `${dashboard.systemMetrics.diskUsage.usagePercent}%` }}
                    ></div>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Network Usage</h3>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">In</span>
                    <span className="text-gray-900">{formatBytes(dashboard.systemMetrics.networkUsage.inBytes)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Out</span>
                    <span className="text-gray-900">{formatBytes(dashboard.systemMetrics.networkUsage.outBytes)}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Application Tab */}
        {activeTab === 'application' && dashboard && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Response Time</h3>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Average</span>
                    <span className="text-gray-900">{dashboard.applicationMetrics.responseTime.average.toFixed(0)}ms</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">P95</span>
                    <span className="text-gray-900">{dashboard.applicationMetrics.responseTime.p95.toFixed(0)}ms</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">P99</span>
                    <span className="text-gray-900">{dashboard.applicationMetrics.responseTime.p99.toFixed(0)}ms</span>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Throughput</h3>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Per Second</span>
                    <span className="text-gray-900">{dashboard.applicationMetrics.throughput.requestsPerSecond.toFixed(0)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Per Minute</span>
                    <span className="text-gray-900">{dashboard.applicationMetrics.throughput.requestsPerMinute.toFixed(0)}</span>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Error Rate</h3>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Current</span>
                    <span className="text-gray-900">{formatPercentage(dashboard.applicationMetrics.errorRate.rate)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Threshold</span>
                    <span className="text-gray-900">{formatPercentage(dashboard.applicationMetrics.errorRate.threshold)}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Database Tab */}
        {activeTab === 'database' && dashboard && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Connection Pool</h3>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Active</span>
                    <span className="text-gray-900">{dashboard.databaseMetrics.connectionPool.activeConnections}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Max</span>
                    <span className="text-gray-900">{dashboard.databaseMetrics.connectionPool.maxConnections}</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-blue-600 h-2 rounded-full"
                      style={{ width: `${dashboard.databaseMetrics.connectionPool.usagePercent}%` }}
                    ></div>
                  </div>
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Query Performance</h3>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Average</span>
                    <span className="text-gray-900">{dashboard.databaseMetrics.queryPerformance.averageExecutionTime.toFixed(0)}ms</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Max</span>
                    <span className="text-gray-900">{dashboard.databaseMetrics.queryPerformance.maxExecutionTime.toFixed(0)}ms</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Cache Tab */}
        {activeTab === 'cache' && dashboard && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Cache Hit Rates</h3>
                <div className="space-y-2">
                  {Object.entries(dashboard.cacheMetrics.hitRates).map(([cacheName, hitRate]) => (
                    <div key={cacheName} className="flex justify-between text-sm">
                      <span className="text-gray-500">{cacheName}</span>
                      <span className="text-gray-900">{formatPercentage(hitRate)}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Cache Sizes</h3>
                <div className="space-y-2">
                  {Object.entries(dashboard.cacheMetrics.cacheSizes).map(([cacheName, size]) => (
                    <div key={cacheName} className="flex justify-between text-sm">
                      <span className="text-gray-500">{cacheName}</span>
                      <span className="text-gray-900">{size.toLocaleString()} entries</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Alerts Tab */}
        {activeTab === 'alerts' && dashboard && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Performance Alerts</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {dashboard.alerts.map((alert) => (
                  <div key={alert.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(alert.severity)}`}>
                            {alert.severity}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{alert.alertType}</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">{alert.message}</p>
                        <p className="text-xs text-gray-500 mt-1">
                          {alert.serviceName} • {new Date(alert.createdAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PerformanceDashboard;

