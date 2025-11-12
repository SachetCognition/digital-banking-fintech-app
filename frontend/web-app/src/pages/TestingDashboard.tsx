import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface TestExecutionResult {
  testSuite: string;
  testType: string;
  executionId: string;
  status: string;
  startTime: string;
  endTime: string;
  durationMs: number;
  environment: string;
  triggeredBy: string;
  errorMessage?: string;
}

interface PerformanceTestResult {
  executionId: string;
  testName: string;
  simulationName: string;
  totalUsers: number;
  durationSeconds: number;
  requestsPerSecond: number;
  averageResponseTimeMs: number;
  p95ResponseTimeMs: number;
  p99ResponseTimeMs: number;
  errorRate: number;
  throughputMbPerSecond: number;
}

interface SecurityTestResult {
  executionId: string;
  testType: string;
  targetUrl: string;
  vulnerabilityCount: number;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  infoCount: number;
}

interface ChaosExperiment {
  experimentName: string;
  experimentType: string;
  targetService: string;
  status: string;
  startTime: string;
  endTime: string;
  durationSeconds: number;
  severity: string;
  recoveryTimeSeconds: number;
}

interface CodeCoverageReport {
  serviceName: string;
  executionId: string;
  lineCoverage: number;
  branchCoverage: number;
  instructionCoverage: number;
  methodCoverage: number;
  classCoverage: number;
  generatedAt: string;
}

interface QualityGateStatus {
  serviceName: string;
  overallStatus: string;
  codeCoverageStatus: string;
  codeDuplicationStatus: string;
  maintainabilityStatus: string;
  reliabilityStatus: string;
  securityStatus: string;
  lastChecked: string;
}

const TestingDashboard: React.FC = () => {
  const [testExecutions, setTestExecutions] = useState<TestExecutionResult[]>([]);
  const [performanceTests, setPerformanceTests] = useState<PerformanceTestResult[]>([]);
  const [securityTests, setSecurityTests] = useState<SecurityTestResult[]>([]);
  const [chaosExperiments, setChaosExperiments] = useState<ChaosExperiment[]>([]);
  const [codeCoverage, setCodeCoverage] = useState<CodeCoverageReport[]>([]);
  const [qualityGates, setQualityGates] = useState<QualityGateStatus[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    loadTestingData();
  }, []);

  const loadTestingData = async () => {
    try {
      setLoading(true);
      const [executionsRes, performanceRes, securityRes, chaosRes, coverageRes, qualityRes] = await Promise.all([
        http.get('/api/v1/testing/executions'),
        http.get('/api/v1/testing/performance'),
        http.get('/api/v1/testing/security'),
        http.get('/api/v1/testing/chaos'),
        http.get('/api/v1/testing/coverage'),
        http.get('/api/v1/testing/quality-gates')
      ]);
      
      setTestExecutions(executionsRes.data);
      setPerformanceTests(performanceRes.data);
      setSecurityTests(securityRes.data);
      setChaosExperiments(chaosRes.data);
      setCodeCoverage(coverageRes.data);
      setQualityGates(qualityRes.data);
    } catch (err) {
      setError('Failed to load testing data');
    } finally {
      setLoading(false);
    }
  };

  const executeUnitTests = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/testing/unit-tests', { testSuite: 'all' });
      await loadTestingData();
    } catch (err) {
      setError('Failed to execute unit tests');
    } finally {
      setLoading(false);
    }
  };

  const executeIntegrationTests = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/testing/integration-tests', { testSuite: 'all' });
      await loadTestingData();
    } catch (err) {
      setError('Failed to execute integration tests');
    } finally {
      setLoading(false);
    }
  };

  const executePerformanceTests = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/testing/performance-tests', { simulationName: 'LoadTestSimulation' });
      await loadTestingData();
    } catch (err) {
      setError('Failed to execute performance tests');
    } finally {
      setLoading(false);
    }
  };

  const executeSecurityTests = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/testing/security-tests', { testType: 'vulnerability_scan' });
      await loadTestingData();
    } catch (err) {
      setError('Failed to execute security tests');
    } finally {
      setLoading(false);
    }
  };

  const executeChaosExperiment = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/testing/chaos-experiment', {
        experimentName: 'Network Latency Test',
        experimentType: 'network_latency',
        targetService: 'customer-service'
      });
      await loadTestingData();
    } catch (err) {
      setError('Failed to execute chaos experiment');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'passed':
      case 'completed':
        return 'text-green-600 bg-green-100';
      case 'failed':
      case 'error':
        return 'text-red-600 bg-red-100';
      case 'running':
        return 'text-blue-600 bg-blue-100';
      case 'skipped':
        return 'text-yellow-600 bg-yellow-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'critical':
        return 'text-red-600 bg-red-100';
      case 'high':
        return 'text-orange-600 bg-orange-100';
      case 'medium':
        return 'text-yellow-600 bg-yellow-100';
      case 'low':
        return 'text-blue-600 bg-blue-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const formatDuration = (ms: number) => {
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}m`;
  };

  const formatPercentage = (value: number) => {
    return `${value.toFixed(1)}%`;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">Loading testing data...</p>
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
            onClick={loadTestingData}
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
              <h1 className="text-3xl font-bold text-gray-900">Testing & Quality Dashboard</h1>
              <p className="mt-2 text-gray-600">Comprehensive testing and quality assurance management</p>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={executeUnitTests}
                className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
              >
                Run Unit Tests
              </button>
              <button
                onClick={executeIntegrationTests}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                Run Integration Tests
              </button>
              <button
                onClick={executePerformanceTests}
                className="px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700"
              >
                Run Performance Tests
              </button>
              <button
                onClick={executeSecurityTests}
                className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
              >
                Run Security Tests
              </button>
              <button
                onClick={executeChaosExperiment}
                className="px-4 py-2 bg-orange-600 text-white rounded-md hover:bg-orange-700"
              >
                Run Chaos Experiment
              </button>
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="mb-8">
          <nav className="flex space-x-8">
            {[
              { id: 'overview', name: 'Overview' },
              { id: 'unit-tests', name: 'Unit Tests' },
              { id: 'integration-tests', name: 'Integration Tests' },
              { id: 'performance-tests', name: 'Performance Tests' },
              { id: 'security-tests', name: 'Security Tests' },
              { id: 'chaos-engineering', name: 'Chaos Engineering' },
              { id: 'code-coverage', name: 'Code Coverage' },
              { id: 'quality-gates', name: 'Quality Gates' }
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
            {/* Test Execution Summary */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Test Execution Summary</h3>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">
                    {testExecutions.filter(t => t.status === 'passed').length}
                  </div>
                  <div className="text-sm text-gray-500">Passed</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-red-600">
                    {testExecutions.filter(t => t.status === 'failed').length}
                  </div>
                  <div className="text-sm text-gray-500">Failed</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">
                    {testExecutions.filter(t => t.status === 'running').length}
                  </div>
                  <div className="text-sm text-gray-500">Running</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-600">
                    {testExecutions.length}
                  </div>
                  <div className="text-sm text-gray-500">Total</div>
                </div>
              </div>
            </div>

            {/* Quality Gates Status */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Quality Gates Status</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {qualityGates.map((gate) => (
                  <div key={gate.serviceName} className="border rounded-lg p-4">
                    <div className="flex items-center justify-between mb-2">
                      <h4 className="font-medium text-gray-900">{gate.serviceName}</h4>
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(gate.overallStatus)}`}>
                        {gate.overallStatus}
                      </span>
                    </div>
                    <div className="space-y-1 text-sm text-gray-600">
                      <div>Coverage: {gate.codeCoverageStatus}</div>
                      <div>Duplication: {gate.codeDuplicationStatus}</div>
                      <div>Maintainability: {gate.maintainabilityStatus}</div>
                      <div>Reliability: {gate.reliabilityStatus}</div>
                      <div>Security: {gate.securityStatus}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Recent Test Executions */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Recent Test Executions</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {testExecutions.slice(0, 10).map((execution) => (
                  <div key={execution.executionId} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(execution.status)}`}>
                            {execution.status}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{execution.testSuite}</span>
                          <span className="text-sm text-gray-500">({execution.testType})</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Duration: {formatDuration(execution.durationMs)} • 
                          Environment: {execution.environment} • 
                          {new Date(execution.startTime).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Unit Tests Tab */}
        {activeTab === 'unit-tests' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Unit Test Executions</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {testExecutions.filter(t => t.testType === 'unit').map((execution) => (
                  <div key={execution.executionId} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(execution.status)}`}>
                            {execution.status}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{execution.testSuite}</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Duration: {formatDuration(execution.durationMs)} • 
                          {new Date(execution.startTime).toLocaleString()}
                        </p>
                        {execution.errorMessage && (
                          <p className="text-sm text-red-600 mt-1">{execution.errorMessage}</p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Integration Tests Tab */}
        {activeTab === 'integration-tests' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Integration Test Executions</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {testExecutions.filter(t => t.testType === 'integration').map((execution) => (
                  <div key={execution.executionId} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(execution.status)}`}>
                            {execution.status}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{execution.testSuite}</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Duration: {formatDuration(execution.durationMs)} • 
                          {new Date(execution.startTime).toLocaleString()}
                        </p>
                        {execution.errorMessage && (
                          <p className="text-sm text-red-600 mt-1">{execution.errorMessage}</p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Performance Tests Tab */}
        {activeTab === 'performance-tests' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Performance Test Results</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {performanceTests.map((test) => (
                  <div key={test.executionId} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className="text-sm font-medium text-gray-900">{test.testName}</span>
                          <span className="text-sm text-gray-500">({test.simulationName})</span>
                        </div>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-2 text-sm text-gray-600">
                          <div>Users: {test.totalUsers}</div>
                          <div>Duration: {test.durationSeconds}s</div>
                          <div>RPS: {test.requestsPerSecond.toFixed(1)}</div>
                          <div>Avg Response: {test.averageResponseTimeMs.toFixed(0)}ms</div>
                          <div>P95: {test.p95ResponseTimeMs.toFixed(0)}ms</div>
                          <div>P99: {test.p99ResponseTimeMs.toFixed(0)}ms</div>
                          <div>Error Rate: {formatPercentage(test.errorRate)}</div>
                          <div>Throughput: {test.throughputMbPerSecond.toFixed(1)} MB/s</div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Security Tests Tab */}
        {activeTab === 'security-tests' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Security Test Results</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {securityTests.map((test) => (
                  <div key={test.executionId} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className="text-sm font-medium text-gray-900">{test.testType}</span>
                          <span className="text-sm text-gray-500">({test.targetUrl})</span>
                        </div>
                        <div className="grid grid-cols-2 md:grid-cols-6 gap-4 mt-2 text-sm text-gray-600">
                          <div>Total: {test.vulnerabilityCount}</div>
                          <div className="text-red-600">Critical: {test.criticalCount}</div>
                          <div className="text-orange-600">High: {test.highCount}</div>
                          <div className="text-yellow-600">Medium: {test.mediumCount}</div>
                          <div className="text-blue-600">Low: {test.lowCount}</div>
                          <div className="text-gray-600">Info: {test.infoCount}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Chaos Engineering Tab */}
        {activeTab === 'chaos-engineering' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Chaos Engineering Experiments</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {chaosExperiments.map((experiment) => (
                  <div key={experiment.experimentName} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(experiment.status)}`}>
                            {experiment.status}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{experiment.experimentName}</span>
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(experiment.severity)}`}>
                            {experiment.severity}
                          </span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Type: {experiment.experimentType} • 
                          Target: {experiment.targetService} • 
                          Duration: {experiment.durationSeconds}s • 
                          Recovery: {experiment.recoveryTimeSeconds}s
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Code Coverage Tab */}
        {activeTab === 'code-coverage' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Code Coverage Reports</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {codeCoverage.map((report) => (
                  <div key={report.executionId} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className="text-sm font-medium text-gray-900">{report.serviceName}</span>
                          <span className="text-sm text-gray-500">
                            Generated: {new Date(report.generatedAt).toLocaleString()}
                          </span>
                        </div>
                        <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mt-2 text-sm text-gray-600">
                          <div>Line: {formatPercentage(report.lineCoverage)}</div>
                          <div>Branch: {formatPercentage(report.branchCoverage)}</div>
                          <div>Instruction: {formatPercentage(report.instructionCoverage)}</div>
                          <div>Method: {formatPercentage(report.methodCoverage)}</div>
                          <div>Class: {formatPercentage(report.classCoverage)}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Quality Gates Tab */}
        {activeTab === 'quality-gates' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Quality Gates Status</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {qualityGates.map((gate) => (
                  <div key={gate.serviceName} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className="text-sm font-medium text-gray-900">{gate.serviceName}</span>
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(gate.overallStatus)}`}>
                            {gate.overallStatus}
                          </span>
                        </div>
                        <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mt-2 text-sm text-gray-600">
                          <div>Coverage: {gate.codeCoverageStatus}</div>
                          <div>Duplication: {gate.codeDuplicationStatus}</div>
                          <div>Maintainability: {gate.maintainabilityStatus}</div>
                          <div>Reliability: {gate.reliabilityStatus}</div>
                          <div>Security: {gate.securityStatus}</div>
                        </div>
                        <p className="text-xs text-gray-500 mt-1">
                          Last checked: {new Date(gate.lastChecked).toLocaleString()}
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

export default TestingDashboard;

