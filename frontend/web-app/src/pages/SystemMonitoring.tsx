import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface SystemHealth {
  serviceName: string;
  status: string;
  responseTime: number;
  uptime: number;
  lastCheck: string;
  metrics: {
    cpuUsage: number;
    memoryUsage: number;
    diskUsage: number;
    requestCount: number;
    errorRate: number;
  };
}

interface PerformanceMetric {
  timestamp: string;
  serviceName: string;
  endpoint: string;
  responseTime: number;
  statusCode: number;
  cpuUsage: number;
  memoryUsage: number;
}

interface SystemAlert {
  id: string;
  alertType: string;
  severity: string;
  title: string;
  description: string;
  serviceName: string;
  status: string;
  createdAt: string;
}

const SystemMonitoring: React.FC = () => {
  const [systemHealth, setSystemHealth] = useState<SystemHealth[]>([]);
  const [performanceMetrics, setPerformanceMetrics] = useState<PerformanceMetric[]>([]);
  const [alerts, setAlerts] = useState<SystemAlert[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [timeRange, setTimeRange] = useState('1h');
  const [selectedService, setSelectedService] = useState('all');

  useEffect(() => {
    loadMonitoringData();
    const interval = setInterval(loadMonitoringData, 30000); // Refresh every 30 seconds
    return () => clearInterval(interval);
  }, [timeRange, selectedService]);

  const loadMonitoringData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        loadSystemHealth(),
        loadPerformanceMetrics(),
        loadAlerts()
      ]);
    } catch (err) {
      setError('Failed to load monitoring data');
    } finally {
      setLoading(false);
    }
  };

  const loadSystemHealth = async () => {
    try {
      const response = await http.get('/api/v1/admin/monitoring/health');
      setSystemHealth(response.data);
    } catch (err) {
      console.error('Failed to load system health:', err);
    }
  };

  const loadPerformanceMetrics = async () => {
    try {
      const response = await http.get(`/api/v1/admin/monitoring/performance?timeRange=${timeRange}&service=${selectedService}`);
      setPerformanceMetrics(response.data);
    } catch (err) {
      console.error('Failed to load performance metrics:', err);
    }
  };

  const loadAlerts = async () => {
    try {
      const response = await http.get('/api/v1/admin/monitoring/alerts');
      setAlerts(response.data);
    } catch (err) {
      console.error('Failed to load alerts:', err);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'healthy':
        return 'text-green-600 bg-green-100';
      case 'warning':
        return 'text-yellow-600 bg-yellow-100';
      case 'critical':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'low':
        return 'text-blue-600 bg-blue-100';
      case 'medium':
        return 'text-yellow-600 bg-yellow-100';
      case 'high':
        return 'text-orange-600 bg-orange-100';
      case 'critical':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const formatUptime = (uptime: number) => {
    const days = Math.floor(uptime / 86400);
    const hours = Math.floor((uptime % 86400) / 3600);
    const minutes = Math.floor((uptime % 3600) / 60);
    
    if (days > 0) {
      return `${days}d ${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h ${minutes}m`;
    } else {
      return `${minutes}m`;
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const getServices = () => {
    const services = ['all', ...new Set(systemHealth.map(h => h.serviceName))];
    return services;
  };

  const calculateAverageResponseTime = () => {
    if (performanceMetrics.length === 0) return 0;
    const total = performanceMetrics.reduce((sum, metric) => sum + metric.responseTime, 0);
    return total / performanceMetrics.length;
  };

  const calculateErrorRate = () => {
    if (performanceMetrics.length === 0) return 0;
    const errors = performanceMetrics.filter(metric => metric.statusCode >= 400).length;
    return (errors / performanceMetrics.length) * 100;
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">System Monitoring</h1>
          <p className="mt-2 text-gray-600">Real-time system health and performance monitoring</p>
        </div>

        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-md p-4">
            <div className="flex">
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">Error</h3>
                <div className="mt-2 text-sm text-red-700">{error}</div>
              </div>
            </div>
          </div>
        )}

        {/* Filters */}
        <div className="bg-white shadow rounded-lg p-6 mb-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
            <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Time Range</label>
                <select
                  value={timeRange}
                  onChange={(e) => setTimeRange(e.target.value)}
                  className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="15m">Last 15 minutes</option>
                  <option value="1h">Last hour</option>
                  <option value="6h">Last 6 hours</option>
                  <option value="24h">Last 24 hours</option>
                  <option value="7d">Last 7 days</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Service</label>
                <select
                  value={selectedService}
                  onChange={(e) => setSelectedService(e.target.value)}
                  className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  {getServices().map((service) => (
                    <option key={service} value={service}>
                      {service === 'all' ? 'All Services' : service}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <div className={`w-2 h-2 rounded-full ${loading ? 'bg-yellow-500 animate-pulse' : 'bg-green-500'}`}></div>
              <span className="text-sm text-gray-600">
                {loading ? 'Updating...' : 'Live'}
              </span>
            </div>
          </div>
        </div>

        {/* Overview Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-blue-500 rounded-md flex items-center justify-center">
                    <span className="text-white text-sm font-medium">⚡</span>
                  </div>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">Avg Response Time</dt>
                    <dd className="text-lg font-medium text-gray-900">
                      {calculateAverageResponseTime().toFixed(0)}ms
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-red-500 rounded-md flex items-center justify-center">
                    <span className="text-white text-sm font-medium">⚠</span>
                  </div>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">Error Rate</dt>
                    <dd className="text-lg font-medium text-gray-900">
                      {calculateErrorRate().toFixed(2)}%
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-green-500 rounded-md flex items-center justify-center">
                    <span className="text-white text-sm font-medium">✓</span>
                  </div>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">Healthy Services</dt>
                    <dd className="text-lg font-medium text-gray-900">
                      {systemHealth.filter(h => h.status === 'healthy').length}
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-yellow-500 rounded-md flex items-center justify-center">
                    <span className="text-white text-sm font-medium">🚨</span>
                  </div>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">Active Alerts</dt>
                    <dd className="text-lg font-medium text-gray-900">
                      {alerts.filter(a => a.status === 'open').length}
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* System Health */}
          <div className="bg-white shadow rounded-lg">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">System Health</h3>
            </div>
            <div className="divide-y divide-gray-200">
              {systemHealth.map((health) => (
                <div key={health.serviceName} className="px-6 py-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="text-sm font-medium text-gray-900">{health.serviceName}</h4>
                      <div className="mt-1 flex items-center space-x-4 text-sm text-gray-500">
                        <span>Response: {health.responseTime}ms</span>
                        <span>Uptime: {formatUptime(health.uptime)}</span>
                        <span>CPU: {health.metrics.cpuUsage.toFixed(1)}%</span>
                        <span>Memory: {health.metrics.memoryUsage.toFixed(1)}%</span>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(health.status)}`}>
                        {health.status}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Recent Alerts */}
          <div className="bg-white shadow rounded-lg">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">Recent Alerts</h3>
            </div>
            <div className="divide-y divide-gray-200">
              {alerts.slice(0, 5).map((alert) => (
                <div key={alert.id} className="px-6 py-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="text-sm font-medium text-gray-900">{alert.title}</h4>
                      <p className="text-sm text-gray-600">{alert.description}</p>
                      <p className="text-xs text-gray-500 mt-1">
                        {alert.serviceName} • {formatDate(alert.createdAt)}
                      </p>
                    </div>
                    <div className="flex items-center space-x-2">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(alert.severity)}`}>
                        {alert.severity}
                      </span>
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(alert.status)}`}>
                        {alert.status}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Performance Metrics Chart Placeholder */}
        <div className="mt-8 bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Performance Trends</h3>
          <div className="h-64 flex items-center justify-center bg-gray-50 rounded-lg">
            <div className="text-center">
              <div className="text-gray-500 text-lg">Performance Charts</div>
              <div className="text-gray-400 text-sm mt-2">
                Integration with Chart.js, D3.js, or similar charting library
              </div>
              <div className="text-gray-400 text-sm">
                Response time, CPU usage, memory usage, and error rate trends
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SystemMonitoring;

