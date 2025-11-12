import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface SecurityEvent {
  id: string;
  eventType: string;
  severity: string;
  sourceIp: string;
  description: string;
  riskScore: number;
  isResolved: boolean;
  createdAt: string;
}

interface SecurityStatistics {
  totalEvents: number;
  criticalEvents: number;
  highEvents: number;
  mediumEvents: number;
  lowEvents: number;
  unresolvedEvents: number;
}

interface ComplianceAssessment {
  framework: string;
  status: string;
  compliant: boolean;
  controls: ComplianceControl[];
  timestamp: string;
}

interface ComplianceControl {
  controlId: string;
  title: string;
  description: string;
  compliant: boolean;
  evidence: string;
}

interface PenetrationTestResult {
  targetUrl: string;
  hasVulnerabilities: boolean;
  overallRisk: string;
  vulnerabilities: VulnerabilityTest[];
  timestamp: string;
}

interface VulnerabilityTest {
  testName: string;
  severity: string;
  description: string;
  vulnerable: boolean;
}

const SecurityDashboard: React.FC = () => {
  const [securityEvents, setSecurityEvents] = useState<SecurityEvent[]>([]);
  const [statistics, setStatistics] = useState<SecurityStatistics | null>(null);
  const [complianceAssessment, setComplianceAssessment] = useState<ComplianceAssessment | null>(null);
  const [penetrationTestResult, setPenetrationTestResult] = useState<PenetrationTestResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    loadSecurityData();
  }, []);

  const loadSecurityData = async () => {
    try {
      setLoading(true);
      const [eventsRes, statsRes, complianceRes, pentestRes] = await Promise.all([
        http.get('/api/v1/security/events'),
        http.get('/api/v1/security/statistics'),
        http.get('/api/v1/security/compliance/pci-dss'),
        http.get('/api/v1/security/penetration-test')
      ]);
      
      setSecurityEvents(eventsRes.data);
      setStatistics(statsRes.data);
      setComplianceAssessment(complianceRes.data);
      setPenetrationTestResult(pentestRes.data);
    } catch (err) {
      setError('Failed to load security data');
    } finally {
      setLoading(false);
    }
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

  const getRiskColor = (risk: string) => {
    switch (risk) {
      case 'CRITICAL':
        return 'text-red-600 bg-red-100';
      case 'HIGH':
        return 'text-orange-600 bg-orange-100';
      case 'MEDIUM':
        return 'text-yellow-600 bg-yellow-100';
      case 'LOW':
        return 'text-green-600 bg-green-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">Loading security dashboard...</p>
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
            onClick={loadSecurityData}
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
          <h1 className="text-3xl font-bold text-gray-900">Security Dashboard</h1>
          <p className="mt-2 text-gray-600">Comprehensive security monitoring and compliance overview</p>
        </div>

        {/* Tab Navigation */}
        <div className="mb-8">
          <nav className="flex space-x-8">
            {[
              { id: 'overview', name: 'Overview' },
              { id: 'events', name: 'Security Events' },
              { id: 'compliance', name: 'Compliance' },
              { id: 'penetration', name: 'Penetration Testing' }
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
            {/* Statistics Cards */}
            {statistics && (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="bg-white shadow rounded-lg p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                        <span className="text-blue-600 text-sm font-medium">📊</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Total Events</p>
                      <p className="text-2xl font-semibold text-gray-900">{statistics.totalEvents}</p>
                    </div>
                  </div>
                </div>

                <div className="bg-white shadow rounded-lg p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center">
                        <span className="text-red-600 text-sm font-medium">🚨</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Critical Events</p>
                      <p className="text-2xl font-semibold text-gray-900">{statistics.criticalEvents}</p>
                    </div>
                  </div>
                </div>

                <div className="bg-white shadow rounded-lg p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center">
                        <span className="text-orange-600 text-sm font-medium">⚠️</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">High Events</p>
                      <p className="text-2xl font-semibold text-gray-900">{statistics.highEvents}</p>
                    </div>
                  </div>
                </div>

                <div className="bg-white shadow rounded-lg p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
                        <span className="text-yellow-600 text-sm font-medium">⏳</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Unresolved</p>
                      <p className="text-2xl font-semibold text-gray-900">{statistics.unresolvedEvents}</p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Recent Security Events */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Recent Security Events</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {securityEvents.slice(0, 5).map((event) => (
                  <div key={event.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(event.severity)}`}>
                            {event.severity}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{event.eventType}</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">{event.description}</p>
                        <p className="text-xs text-gray-500 mt-1">
                          {event.sourceIp} • {new Date(event.createdAt).toLocaleString()}
                        </p>
                      </div>
                      <div className="ml-4">
                        <span className="text-sm font-medium text-gray-900">Risk: {event.riskScore}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Security Events Tab */}
        {activeTab === 'events' && (
          <div className="bg-white shadow rounded-lg">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">Security Events</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Event Type</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Severity</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Source IP</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Risk Score</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Created</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {securityEvents.map((event) => (
                    <tr key={event.id}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {event.eventType}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(event.severity)}`}>
                          {event.severity}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {event.sourceIp}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {event.description}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {event.riskScore}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          event.isResolved ? 'text-green-600 bg-green-100' : 'text-red-600 bg-red-100'
                        }`}>
                          {event.isResolved ? 'Resolved' : 'Open'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {new Date(event.createdAt).toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Compliance Tab */}
        {activeTab === 'compliance' && complianceAssessment && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-medium text-gray-900">{complianceAssessment.framework} Compliance</h3>
                <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                  complianceAssessment.compliant ? 'text-green-600 bg-green-100' : 'text-red-600 bg-red-100'
                }`}>
                  {complianceAssessment.status}
                </span>
              </div>
              <p className="text-gray-600 mb-6">
                Last assessed: {new Date(complianceAssessment.timestamp).toLocaleString()}
              </p>
            </div>

            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Compliance Controls</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {complianceAssessment.controls.map((control) => (
                  <div key={control.controlId} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className="text-sm font-medium text-gray-900">{control.controlId}</span>
                          <span className="text-sm text-gray-600">{control.title}</span>
                        </div>
                        <p className="text-sm text-gray-500 mt-1">{control.description}</p>
                        <p className="text-sm text-gray-600 mt-1">{control.evidence}</p>
                      </div>
                      <div className="ml-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          control.compliant ? 'text-green-600 bg-green-100' : 'text-red-600 bg-red-100'
                        }`}>
                          {control.compliant ? 'Compliant' : 'Non-Compliant'}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Penetration Testing Tab */}
        {activeTab === 'penetration' && penetrationTestResult && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-medium text-gray-900">Penetration Test Results</h3>
                <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${getRiskColor(penetrationTestResult.overallRisk)}`}>
                  {penetrationTestResult.overallRisk} Risk
                </span>
              </div>
              <p className="text-gray-600 mb-2">
                Target: {penetrationTestResult.targetUrl}
              </p>
              <p className="text-gray-600">
                Last tested: {new Date(penetrationTestResult.timestamp).toLocaleString()}
              </p>
            </div>

            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Vulnerability Tests</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {penetrationTestResult.vulnerabilities.map((vuln, index) => (
                  <div key={index} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className="text-sm font-medium text-gray-900">{vuln.testName}</span>
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityColor(vuln.severity)}`}>
                            {vuln.severity}
                          </span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">{vuln.description}</p>
                      </div>
                      <div className="ml-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          vuln.vulnerable ? 'text-red-600 bg-red-100' : 'text-green-600 bg-green-100'
                        }`}>
                          {vuln.vulnerable ? 'Vulnerable' : 'Secure'}
                        </span>
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

export default SecurityDashboard;

