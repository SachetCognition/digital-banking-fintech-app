import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface AmlCase {
  id: string;
  caseNumber: string;
  customerId: string;
  caseType: string;
  status: string;
  priority: string;
  riskScore: number;
  description: string;
  assignedTo: string;
  createdAt: string;
  updatedAt: string;
  closedAt: string;
  resolutionNotes: string;
}

interface TransactionMonitoring {
  id: string;
  transactionId: string;
  customerId: string;
  transactionType: string;
  amount: number;
  currency: string;
  transactionDate: string;
  riskScore: number;
  riskFactors: string;
  isFlagged: boolean;
  flagReason: string;
  reviewStatus: string;
  reviewedBy: string;
  reviewedAt: string;
  reviewNotes: string;
  createdAt: string;
  updatedAt: string;
}

interface FraudDetection {
  id: string;
  transactionId: string;
  customerId: string;
  fraudType: string;
  fraudScore: number;
  fraudIndicators: string;
  detectionMethod: string;
  isConfirmed: boolean;
  isFalsePositive: boolean;
  investigationStatus: string;
  investigator: string;
  investigationNotes: string;
  createdAt: string;
  updatedAt: string;
}

const ComplianceManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState('aml');
  const [amlCases, setAmlCases] = useState<AmlCase[]>([]);
  const [monitoredTransactions, setMonitoredTransactions] = useState<TransactionMonitoring[]>([]);
  const [fraudDetections, setFraudDetections] = useState<FraudDetection[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Form states
  const [selectedCaseId, setSelectedCaseId] = useState<string>('');
  const [caseStatus, setCaseStatus] = useState<string>('OPEN');
  const [resolutionNotes, setResolutionNotes] = useState<string>('');

  const [selectedMonitoringId, setSelectedMonitoringId] = useState<string>('');
  const [reviewStatus, setReviewStatus] = useState<string>('PENDING');
  const [reviewedBy, setReviewedBy] = useState<string>('');
  const [reviewNotes, setReviewNotes] = useState<string>('');

  const [selectedFraudId, setSelectedFraudId] = useState<string>('');
  const [investigationStatus, setInvestigationStatus] = useState<string>('PENDING');
  const [investigator, setInvestigator] = useState<string>('');
  const [investigationNotes, setInvestigationNotes] = useState<string>('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        loadAmlCases(),
        loadMonitoredTransactions(),
        loadFraudDetections()
      ]);
    } catch (err) {
      setError('Failed to load compliance data');
    } finally {
      setLoading(false);
    }
  };

  const loadAmlCases = async () => {
    try {
      const response = await http.get('/api/v1/aml/cases');
      setAmlCases(response.data);
    } catch (err) {
      console.error('Failed to load AML cases:', err);
    }
  };

  const loadMonitoredTransactions = async () => {
    try {
      const response = await http.get('/api/v1/transaction-monitoring/flagged');
      setMonitoredTransactions(response.data);
    } catch (err) {
      console.error('Failed to load monitored transactions:', err);
    }
  };

  const loadFraudDetections = async () => {
    try {
      const response = await http.get('/api/v1/fraud-detection/active');
      setFraudDetections(response.data);
    } catch (err) {
      console.error('Failed to load fraud detections:', err);
    }
  };

  const handleUpdateCaseStatus = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCaseId) return;
    
    try {
      setLoading(true);
      await http.put(`/api/v1/aml/cases/${selectedCaseId}/status`, null, {
        params: {
          status: caseStatus,
          resolutionNotes: resolutionNotes
        }
      });
      await loadAmlCases();
      setSelectedCaseId('');
      setCaseStatus('OPEN');
      setResolutionNotes('');
    } catch (err) {
      setError('Failed to update case status');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateReviewStatus = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedMonitoringId) return;
    
    try {
      setLoading(true);
      await http.put(`/api/v1/transaction-monitoring/${selectedMonitoringId}/review`, null, {
        params: {
          status: reviewStatus,
          reviewedBy: reviewedBy,
          reviewNotes: reviewNotes
        }
      });
      await loadMonitoredTransactions();
      setSelectedMonitoringId('');
      setReviewStatus('PENDING');
      setReviewedBy('');
      setReviewNotes('');
    } catch (err) {
      setError('Failed to update review status');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateInvestigationStatus = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFraudId) return;
    
    try {
      setLoading(true);
      await http.put(`/api/v1/fraud-detection/${selectedFraudId}/investigation`, null, {
        params: {
          status: investigationStatus,
          investigator: investigator,
          notes: investigationNotes
        }
      });
      await loadFraudDetections();
      setSelectedFraudId('');
      setInvestigationStatus('PENDING');
      setInvestigator('');
      setInvestigationNotes('');
    } catch (err) {
      setError('Failed to update investigation status');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmFraud = async (fraudId: string) => {
    try {
      setLoading(true);
      await http.post(`/api/v1/fraud-detection/${fraudId}/confirm`, null, {
        params: {
          investigator: 'Compliance Officer',
          notes: 'Fraud confirmed by automated analysis'
        }
      });
      await loadFraudDetections();
    } catch (err) {
      setError('Failed to confirm fraud');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkFalsePositive = async (fraudId: string) => {
    try {
      setLoading(true);
      await http.post(`/api/v1/fraud-detection/${fraudId}/false-positive`, null, {
        params: {
          investigator: 'Compliance Officer',
          notes: 'Marked as false positive after review'
        }
      });
      await loadFraudDetections();
    } catch (err) {
      setError('Failed to mark as false positive');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'open':
      case 'pending':
      case 'under_review':
        return 'text-yellow-600 bg-yellow-100';
      case 'approved':
      case 'resolved':
      case 'cleared':
        return 'text-green-600 bg-green-100';
      case 'rejected':
      case 'closed':
        return 'text-red-600 bg-red-100';
      case 'high':
      case 'critical':
        return 'text-red-600 bg-red-100';
      case 'medium':
        return 'text-yellow-600 bg-yellow-100';
      case 'low':
        return 'text-blue-600 bg-blue-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getRiskScoreColor = (score: number) => {
    if (score >= 80) return 'text-red-600';
    if (score >= 60) return 'text-yellow-600';
    return 'text-green-600';
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Compliance & Risk Management</h1>
          <p className="mt-2 text-gray-600">Monitor AML cases, transaction monitoring, and fraud detection</p>
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

        {/* Tab Navigation */}
        <div className="border-b border-gray-200 mb-8">
          <nav className="-mb-px flex space-x-8">
            {[
              { id: 'aml', name: 'AML Cases', icon: '🚨' },
              { id: 'monitoring', name: 'Transaction Monitoring', icon: '📊' },
              { id: 'fraud', name: 'Fraud Detection', icon: '🛡️' }
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
                <span className="mr-2">{tab.icon}</span>
                {tab.name}
              </button>
            ))}
          </nav>
        </div>

        {/* AML Cases Tab */}
        {activeTab === 'aml' && (
          <div className="space-y-6">
            {/* Update Case Status Form */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Update AML Case Status</h3>
              <form onSubmit={handleUpdateCaseStatus} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Select Case</label>
                  <select
                    value={selectedCaseId}
                    onChange={(e) => setSelectedCaseId(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="">Select a case</option>
                    {amlCases.map((case_) => (
                      <option key={case_.id} value={case_.id}>
                        {case_.caseNumber} - {case_.caseType}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Status</label>
                  <select
                    value={caseStatus}
                    onChange={(e) => setCaseStatus(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="OPEN">Open</option>
                    <option value="UNDER_REVIEW">Under Review</option>
                    <option value="PENDING_APPROVAL">Pending Approval</option>
                    <option value="APPROVED">Approved</option>
                    <option value="REJECTED">Rejected</option>
                    <option value="CLOSED">Closed</option>
                  </select>
                </div>
                <div className="sm:col-span-2">
                  <label className="block text-sm font-medium text-gray-700">Resolution Notes</label>
                  <input
                    type="text"
                    value={resolutionNotes}
                    onChange={(e) => setResolutionNotes(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Enter resolution notes"
                  />
                </div>
                <div className="sm:col-span-4">
                  <button
                    type="submit"
                    disabled={loading || !selectedCaseId}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    {loading ? 'Updating...' : 'Update Status'}
                  </button>
                </div>
              </form>
            </div>

            {/* AML Cases List */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">AML Cases</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Case #</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Priority</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Risk Score</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Assigned To</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Created</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {amlCases.map((case_) => (
                      <tr key={case_.id}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {case_.caseNumber}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {case_.caseType}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(case_.status)}`}>
                            {case_.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(case_.priority)}`}>
                            {case_.priority}
                          </span>
                        </td>
                        <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${getRiskScoreColor(case_.riskScore)}`}>
                          {case_.riskScore}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {case_.assignedTo || 'Unassigned'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDate(case_.createdAt)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Transaction Monitoring Tab */}
        {activeTab === 'monitoring' && (
          <div className="space-y-6">
            {/* Update Review Status Form */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Update Transaction Review Status</h3>
              <form onSubmit={handleUpdateReviewStatus} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Select Transaction</label>
                  <select
                    value={selectedMonitoringId}
                    onChange={(e) => setSelectedMonitoringId(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="">Select a transaction</option>
                    {monitoredTransactions.map((tx) => (
                      <option key={tx.id} value={tx.id}>
                        {tx.transactionId} - {formatCurrency(tx.amount)}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Review Status</label>
                  <select
                    value={reviewStatus}
                    onChange={(e) => setReviewStatus(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="PENDING">Pending</option>
                    <option value="UNDER_REVIEW">Under Review</option>
                    <option value="APPROVED">Approved</option>
                    <option value="REJECTED">Rejected</option>
                    <option value="FLAGGED">Flagged</option>
                    <option value="CLEARED">Cleared</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Reviewed By</label>
                  <input
                    type="text"
                    value={reviewedBy}
                    onChange={(e) => setReviewedBy(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Reviewer name"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Review Notes</label>
                  <input
                    type="text"
                    value={reviewNotes}
                    onChange={(e) => setReviewNotes(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Review notes"
                  />
                </div>
                <div className="sm:col-span-4">
                  <button
                    type="submit"
                    disabled={loading || !selectedMonitoringId}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    {loading ? 'Updating...' : 'Update Review Status'}
                  </button>
                </div>
              </form>
            </div>

            {/* Monitored Transactions List */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Flagged Transactions</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Transaction ID</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Risk Score</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Flag Reason</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {monitoredTransactions.map((tx) => (
                      <tr key={tx.id}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {tx.transactionId}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {tx.transactionType}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatCurrency(tx.amount)}
                        </td>
                        <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${getRiskScoreColor(tx.riskScore)}`}>
                          {tx.riskScore}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(tx.reviewStatus)}`}>
                            {tx.reviewStatus}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {tx.flagReason}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDate(tx.transactionDate)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Fraud Detection Tab */}
        {activeTab === 'fraud' && (
          <div className="space-y-6">
            {/* Update Investigation Status Form */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Update Fraud Investigation Status</h3>
              <form onSubmit={handleUpdateInvestigationStatus} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Select Fraud Detection</label>
                  <select
                    value={selectedFraudId}
                    onChange={(e) => setSelectedFraudId(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="">Select a fraud detection</option>
                    {fraudDetections.map((fraud) => (
                      <option key={fraud.id} value={fraud.id}>
                        {fraud.fraudType} - Score: {fraud.fraudScore}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Investigation Status</label>
                  <select
                    value={investigationStatus}
                    onChange={(e) => setInvestigationStatus(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="PENDING">Pending</option>
                    <option value="ASSIGNED">Assigned</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="UNDER_REVIEW">Under Review</option>
                    <option value="RESOLVED">Resolved</option>
                    <option value="CLOSED">Closed</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Investigator</label>
                  <input
                    type="text"
                    value={investigator}
                    onChange={(e) => setInvestigator(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Investigator name"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Investigation Notes</label>
                  <input
                    type="text"
                    value={investigationNotes}
                    onChange={(e) => setInvestigationNotes(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Investigation notes"
                  />
                </div>
                <div className="sm:col-span-4">
                  <button
                    type="submit"
                    disabled={loading || !selectedFraudId}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    {loading ? 'Updating...' : 'Update Investigation Status'}
                  </button>
                </div>
              </form>
            </div>

            {/* Fraud Detections List */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Active Fraud Detections</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Transaction ID</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fraud Type</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Score</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Method</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Investigator</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {fraudDetections.map((fraud) => (
                      <tr key={fraud.id}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {fraud.transactionId}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {fraud.fraudType}
                        </td>
                        <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${getRiskScoreColor(fraud.fraudScore)}`}>
                          {fraud.fraudScore}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {fraud.detectionMethod}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(fraud.investigationStatus)}`}>
                            {fraud.investigationStatus}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {fraud.investigator || 'Unassigned'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          <div className="flex space-x-2">
                            <button
                              onClick={() => handleConfirmFraud(fraud.id)}
                              className="text-red-600 hover:text-red-900 text-xs"
                            >
                              Confirm
                            </button>
                            <button
                              onClick={() => handleMarkFalsePositive(fraud.id)}
                              className="text-green-600 hover:text-green-900 text-xs"
                            >
                              False Positive
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ComplianceManagement;

