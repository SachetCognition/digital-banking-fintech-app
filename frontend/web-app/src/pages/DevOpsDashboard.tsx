import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface PipelineExecution {
  id: string;
  pipelineName: string;
  serviceName: string;
  status: string;
  triggerType: string;
  startedAt: string;
  completedAt: string;
  durationSeconds: number;
  buildNumber: string;
  commitHash: string;
  branchName: string;
  pullRequestNumber: number;
  buildLog: string;
  codeCoverage: number;
  dockerBuildStatus: string;
  kubernetesDeployStatus: string;
}

interface DeploymentResult {
  id: string;
  serviceName: string;
  imageName: string;
  namespace: string;
  status: string;
  startedAt: string;
  completedAt: string;
  durationSeconds: number;
  deploymentName: string;
  ingressName: string;
  url: string;
  errorMessage: string;
}

interface MonitoringAlert {
  id: string;
  alertName: string;
  serviceName: string;
  severity: string;
  status: string;
  description: string;
  summary: string;
  startedAt: string;
  resolvedAt: string;
  notificationSent: boolean;
}

interface InfrastructureDeployment {
  id: string;
  environment: string;
  region: string;
  status: string;
  startedAt: string;
  completedAt: string;
  provider: string;
  planOutput: string;
  applyOutput: string;
  errorMessage: string;
}

interface LogEntry {
  id: string;
  serviceName: string;
  level: string;
  message: string;
  timestamp: string;
  threadName: string;
  loggerName: string;
  correlationId: string;
  traceId: string;
  spanId: string;
}

const DevOpsDashboard: React.FC = () => {
  const [pipelines, setPipelines] = useState<PipelineExecution[]>([]);
  const [deployments, setDeployments] = useState<DeploymentResult[]>([]);
  const [alerts, setAlerts] = useState<MonitoringAlert[]>([]);
  const [infrastructure, setInfrastructure] = useState<InfrastructureDeployment[]>([]);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    loadDevOpsData();
  }, []);

  const loadDevOpsData = async () => {
    try {
      setLoading(true);
      const [pipelinesRes, deploymentsRes, alertsRes, infrastructureRes, logsRes] = await Promise.all([
        http.get('/api/v1/devops/pipelines'),
        http.get('/api/v1/devops/deployments'),
        http.get('/api/v1/devops/alerts'),
        http.get('/api/v1/devops/infrastructure'),
        http.get('/api/v1/devops/logs')
      ]);
      
      setPipelines(pipelinesRes.data);
      setDeployments(deploymentsRes.data);
      setAlerts(alertsRes.data);
      setInfrastructure(infrastructureRes.data);
      setLogs(logsRes.data);
    } catch (err) {
      setError('Failed to load DevOps data');
    } finally {
      setLoading(false);
    }
  };

  const triggerPipeline = async (serviceName: string) => {
    try {
      setLoading(true);
      await http.post('/api/v1/devops/pipelines/trigger', {
        pipelineName: serviceName + '-pipeline',
        serviceName: serviceName,
        triggerType: 'manual',
        parameters: {
          commitHash: 'abc123',
          branchName: 'main'
        }
      });
      await loadDevOpsData();
    } catch (err) {
      setError('Failed to trigger pipeline');
    } finally {
      setLoading(false);
    }
  };

  const deployApplication = async (serviceName: string) => {
    try {
      setLoading(true);
      await http.post('/api/v1/devops/deployments', {
        serviceName: serviceName,
        imageName: 'digital-banking/' + serviceName + ':latest',
        namespace: 'digital-banking',
        environmentVariables: {
          SPRING_PROFILES_ACTIVE: 'prod',
          DATABASE_URL: 'jdbc:postgresql://postgres:5432/' + serviceName + '_db'
        }
      });
      await loadDevOpsData();
    } catch (err) {
      setError('Failed to deploy application');
    } finally {
      setLoading(false);
    }
  };

  const scaleApplication = async (serviceName: string, replicas: number) => {
    try {
      setLoading(true);
      await http.post(`/api/v1/devops/kubernetes/scale`, {
        serviceName: serviceName,
        namespace: 'digital-banking',
        replicas: replicas
      });
      await loadDevOpsData();
    } catch (err) {
      setError('Failed to scale application');
    } finally {
      setLoading(false);
    }
  };

  const createAlert = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/devops/monitoring/alerts', {
        alertName: 'High CPU Usage',
        serviceName: 'customer-service',
        severity: 'warning',
        description: 'CPU usage is above 80%',
        labels: {
          severity: 'warning',
          service: 'customer-service'
        }
      });
      await loadDevOpsData();
    } catch (err) {
      setError('Failed to create alert');
    } finally {
      setLoading(false);
    }
  };

  const deployInfrastructure = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/devops/infrastructure/deploy', {
        environment: 'production',
        region: 'us-east-1',
        variables: {
          instance_type: 't3.medium',
          instance_count: 3
        }
      });
      await loadDevOpsData();
    } catch (err) {
      setError('Failed to deploy infrastructure');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'success':
      case 'completed':
      case 'healthy':
        return 'text-green-600 bg-green-100';
      case 'failed':
      case 'error':
      case 'unhealthy':
        return 'text-red-600 bg-red-100';
      case 'running':
      case 'deploying':
      case 'pending':
        return 'text-blue-600 bg-blue-100';
      case 'warning':
        return 'text-yellow-600 bg-yellow-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'critical':
        return 'text-red-600 bg-red-100';
      case 'warning':
        return 'text-yellow-600 bg-yellow-100';
      case 'info':
        return 'text-blue-600 bg-blue-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const formatDuration = (seconds: number) => {
    if (seconds < 60) return `${seconds}s`;
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m ${seconds % 60}s`;
    return `${Math.floor(seconds / 3600)}h ${Math.floor((seconds % 3600) / 60)}m`;
  };

  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">Loading DevOps data...</p>
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
            onClick={loadDevOpsData}
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
              <h1 className="text-3xl font-bold text-gray-900">DevOps & Deployment Dashboard</h1>
              <p className="mt-2 text-gray-600">Comprehensive DevOps and deployment management</p>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => triggerPipeline('customer-service')}
                className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
              >
                Trigger Pipeline
              </button>
              <button
                onClick={() => deployApplication('customer-service')}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                Deploy App
              </button>
              <button
                onClick={() => scaleApplication('customer-service', 5)}
                className="px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700"
              >
                Scale App
              </button>
              <button
                onClick={createAlert}
                className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
              >
                Create Alert
              </button>
              <button
                onClick={deployInfrastructure}
                className="px-4 py-2 bg-orange-600 text-white rounded-md hover:bg-orange-700"
              >
                Deploy Infrastructure
              </button>
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="mb-8">
          <nav className="flex space-x-8">
            {[
              { id: 'overview', name: 'Overview' },
              { id: 'pipelines', name: 'CI/CD Pipelines' },
              { id: 'deployments', name: 'Deployments' },
              { id: 'kubernetes', name: 'Kubernetes' },
              { id: 'monitoring', name: 'Monitoring' },
              { id: 'infrastructure', name: 'Infrastructure' },
              { id: 'logs', name: 'Logs' }
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
        {activeTab === 'overview' && (
          <div className="space-y-6">
            {/* Pipeline Summary */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">CI/CD Pipeline Summary</h3>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">
                    {pipelines.filter(p => p.status === 'success').length}
                  </div>
                  <div className="text-sm text-gray-500">Successful</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-red-600">
                    {pipelines.filter(p => p.status === 'failed').length}
                  </div>
                  <div className="text-sm text-gray-500">Failed</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">
                    {pipelines.filter(p => p.status === 'running').length}
                  </div>
                  <div className="text-sm text-gray-500">Running</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-600">
                    {pipelines.length}
                  </div>
                  <div className="text-sm text-gray-500">Total</div>
                </div>
              </div>
            </div>

            {/* Deployment Summary */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Deployment Summary</h3>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">
                    {deployments.filter(d => d.status === 'success').length}
                  </div>
                  <div className="text-sm text-gray-500">Successful</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-red-600">
                    {deployments.filter(d => d.status === 'failed').length}
                  </div>
                  <div className="text-sm text-gray-500">Failed</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">
                    {deployments.filter(d => d.status === 'deploying').length}
                  </div>
                  <div className="text-sm text-gray-500">Deploying</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-600">
                    {deployments.length}
                  </div>
                  <div className="text-sm text-gray-500">Total</div>
                </div>
              </div>
            </div>

            {/* Alert Summary */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Alert Summary</h3>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-red-600">
                    {alerts.filter(a => a.severity === 'critical').length}
                  </div>
                  <div className="text-sm text-gray-500">Critical</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-yellow-600">
                    {alerts.filter(a => a.severity === 'warning').length}
                  </div>
                  <div className="text-sm text-gray-500">Warning</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">
                    {alerts.filter(a => a.status === 'firing').length}
                  </div>
                  <div className="text-sm text-gray-500">Active</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-600">
                    {alerts.length}
                  </div>
                  <div className="text-sm text-gray-500">Total</div>
                </div>
              </div>
            </div>

            {/* Recent Activity */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Recent Activity</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {[...pipelines, ...deployments, ...alerts]
                  .sort((a, b) => new Date(b.startedAt || b.startedAt).getTime() - new Date(a.startedAt || a.startedAt).getTime())
                  .slice(0, 10)
                  .map((item, index) => (
                    <div key={index} className="px-6 py-4">
                      <div className="flex items-center justify-between">
                        <div className="flex-1">
                          <div className="flex items-center space-x-3">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(item.status)}`}>
                              {item.status}
                            </span>
                            <span className="text-sm font-medium text-gray-900">
                              {('pipelineName' in item && item.pipelineName) || ('serviceName' in item && item.serviceName) || ('alertName' in item && item.alertName)}
                            </span>
                            <span className="text-sm text-gray-500">
                              {('triggerType' in item && item.triggerType) || ('namespace' in item && item.namespace) || ('severity' in item && item.severity)}
                            </span>
                          </div>
                          <p className="text-sm text-gray-600 mt-1">
                            {formatTimestamp(item.startedAt)}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
              </div>
            </div>
          </div>
        )}

        {/* Pipelines Tab */}
        {activeTab === 'pipelines' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">CI/CD Pipelines</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {pipelines.map((pipeline) => (
                  <div key={pipeline.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(pipeline.status)}`}>
                            {pipeline.status}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{pipeline.pipelineName}</span>
                          <span className="text-sm text-gray-500">({pipeline.serviceName})</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Build: {pipeline.buildNumber} • 
                          Branch: {pipeline.branchName} • 
                          Duration: {formatDuration(pipeline.durationSeconds)} • 
                          Coverage: {pipeline.codeCoverage?.toFixed(1)}% • 
                          {formatTimestamp(pipeline.startedAt)}
                        </p>
                        {pipeline.buildLog && (
                          <details className="mt-2">
                            <summary className="text-sm text-blue-600 cursor-pointer">View Build Log</summary>
                            <pre className="mt-2 text-xs text-gray-600 bg-gray-50 p-2 rounded overflow-x-auto">
                              {pipeline.buildLog}
                            </pre>
                          </details>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Deployments Tab */}
        {activeTab === 'deployments' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Deployments</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {deployments.map((deployment) => (
                  <div key={deployment.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(deployment.status)}`}>
                            {deployment.status}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{deployment.serviceName}</span>
                          <span className="text-sm text-gray-500">({deployment.namespace})</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Image: {deployment.imageName} • 
                          Duration: {formatDuration(deployment.durationSeconds)} • 
                          URL: {deployment.url} • 
                          {formatTimestamp(deployment.startedAt)}
                        </p>
                        {deployment.errorMessage && (
                          <p className="text-sm text-red-600 mt-1">{deployment.errorMessage}</p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Monitoring Tab */}
        {activeTab === 'monitoring' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Monitoring Alerts</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {alerts.map((alert) => (
                  <div key={alert.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(alert.status)}`}>
                            {alert.status}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{alert.alertName}</span>
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(alert.severity)}`}>
                            {alert.severity}
                          </span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Service: {alert.serviceName} • 
                          {formatTimestamp(alert.startedAt)}
                        </p>
                        <p className="text-sm text-gray-700 mt-1">{alert.description}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Infrastructure Tab */}
        {activeTab === 'infrastructure' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Infrastructure Deployments</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {infrastructure.map((infra) => (
                  <div key={infra.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(infra.status)}`}>
                            {infra.status}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{infra.environment}</span>
                          <span className="text-sm text-gray-500">({infra.region})</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Provider: {infra.provider} • 
                          {formatTimestamp(infra.startedAt)}
                        </p>
                        {infra.errorMessage && (
                          <p className="text-sm text-red-600 mt-1">{infra.errorMessage}</p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Logs Tab */}
        {activeTab === 'logs' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Application Logs</h3>
              </div>
              <div className="divide-y divide-gray-200 max-h-96 overflow-y-auto">
                {logs.map((log) => (
                  <div key={log.id} className="px-6 py-2">
                    <div className="flex items-center space-x-3">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(log.level)}`}>
                        {log.level}
                      </span>
                      <span className="text-sm font-medium text-gray-900">{log.serviceName}</span>
                      <span className="text-xs text-gray-500">{formatTimestamp(log.timestamp)}</span>
                    </div>
                    <p className="text-sm text-gray-700 mt-1 ml-20">{log.message}</p>
                    {log.correlationId && (
                      <p className="text-xs text-gray-500 mt-1 ml-20">
                        Correlation ID: {log.correlationId} • Trace ID: {log.traceId}
                      </p>
                    )}
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

export default DevOpsDashboard;

