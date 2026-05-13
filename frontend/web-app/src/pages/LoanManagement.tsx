import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface LoanApplication {
  id: string;
  applicationNumber: string;
  loanType: string;
  requestedAmount: number;
  requestedTermMonths: number;
  purpose: string;
  employmentStatus: string;
  annualIncome: number;
  monthlyExpenses: number;
  status: string;
  creditScore: number;
  riskLevel: string;
  interestRate: number;
  approvedAmount: number;
  approvedTermMonths: number;
  rejectionReason: string;
  submittedAt: string;
  reviewedAt: string;
  approvedAt: string;
}

interface Loan {
  id: string;
  loanNumber: string;
  loanType: string;
  principalAmount: number;
  interestRate: number;
  termMonths: number;
  monthlyPayment: number;
  remainingBalance: number;
  status: string;
  disbursementDate: string;
  maturityDate: string;
  nextPaymentDate: string;
  gracePeriodDays: number;
  lateFeeRate: number;
  createdAt: string;
}

interface CreditLine {
  id: string;
  creditLineNumber: string;
  creditLimit: number;
  availableCredit: number;
  usedCredit: number;
  interestRate: number;
  status: string;
  creditScore: number;
  riskLevel: string;
  annualFee: number;
  minimumPaymentRate: number;
  gracePeriodDays: number;
  utilizationRate: number;
  createdAt: string;
}

interface CreditScore {
  id: string;
  score: number;
  scoreType: string;
  riskLevel: string;
  factors: string;
  calculatedAt: string;
  expiresAt: string;
  isExpired: boolean;
  isGoodScore: boolean;
  isExcellentScore: boolean;
}

const LoanManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState('applications');
  const [applications, setApplications] = useState<LoanApplication[]>([]);
  const [loans, setLoans] = useState<Loan[]>([]);
  const [creditLines, setCreditLines] = useState<CreditLine[]>([]);
  const [creditScore, setCreditScore] = useState<CreditScore | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Form states
  const [applicationForm, setApplicationForm] = useState({
    loanType: 'PERSONAL',
    requestedAmount: 10000,
    requestedTermMonths: 24,
    purpose: '',
    employmentStatus: 'EMPLOYED',
    annualIncome: 50000,
    monthlyExpenses: 3000
  });

  const [creditLineForm, setCreditLineForm] = useState({
    requestedCreditLimit: 10000,
    purpose: '',
    autoIncrease: false
  });

  const [paymentForm, setPaymentForm] = useState({
    amount: 0,
    paymentDate: new Date().toISOString().split('T')[0],
    paymentMethod: 'BANK_TRANSFER',
    referenceNumber: '',
    notes: ''
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        loadApplications(),
        loadLoans(),
        loadCreditLines(),
        loadCreditScore()
      ]);
    } catch (err) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const loadApplications = async () => {
    try {
      const response = await http.get('/api/v1/loan-applications');
      setApplications(response.data);
    } catch (err) {
      console.error('Failed to load applications:', err);
    }
  };

  const loadLoans = async () => {
    try {
      const response = await http.get('/api/v1/loans');
      setLoans(response.data);
    } catch (err) {
      console.error('Failed to load loans:', err);
    }
  };

  const loadCreditLines = async () => {
    try {
      const response = await http.get('/api/v1/credit-lines');
      setCreditLines(response.data);
    } catch (err) {
      console.error('Failed to load credit lines:', err);
    }
  };

  const loadCreditScore = async () => {
    try {
      const response = await http.get('/api/v1/credit-scores/valid');
      setCreditScore(response.data);
    } catch (err) {
      console.error('Failed to load credit score:', err);
    }
  };

  const handleSubmitApplication = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await http.post('/api/v1/loan-applications', applicationForm);
      await loadApplications();
      setApplicationForm({
        loanType: 'PERSONAL',
        requestedAmount: 10000,
        requestedTermMonths: 24,
        purpose: '',
        employmentStatus: 'EMPLOYED',
        annualIncome: 50000,
        monthlyExpenses: 3000
      });
    } catch (err) {
      setError('Failed to submit application');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCreditLine = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await http.post('/api/v1/credit-lines', creditLineForm);
      await loadCreditLines();
      setCreditLineForm({
        requestedCreditLimit: 10000,
        purpose: '',
        autoIncrease: false
      });
    } catch (err) {
      setError('Failed to create credit line');
    } finally {
      setLoading(false);
    }
  };

  const handleProcessPayment = async (loanId: string) => {
    try {
      await http.post(`/api/v1/loans/${loanId}/payments`, paymentForm);
      await loadLoans();
      setPaymentForm({
        amount: 0,
        paymentDate: new Date().toISOString().split('T')[0],
        paymentMethod: 'BANK_TRANSFER',
        referenceNumber: '',
        notes: ''
      });
    } catch (err) {
      setError('Failed to process payment');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'approved':
      case 'active':
      case 'completed':
        return 'text-green-600 bg-green-100';
      case 'pending':
      case 'under review':
        return 'text-yellow-600 bg-yellow-100';
      case 'rejected':
      case 'defaulted':
      case 'over limit':
        return 'text-red-600 bg-red-100';
      case 'paid off':
        return 'text-blue-600 bg-blue-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'AED'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Loan & Credit Management</h1>
          <p className="mt-2 text-gray-600">Manage your loans, credit lines, and applications</p>
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

        {/* Credit Score Display */}
        {creditScore && (
          <div className="mb-6 bg-white shadow rounded-lg p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Your Credit Score</h3>
            <div className="flex items-center space-x-6">
              <div className="text-center">
                <div className={`text-4xl font-bold ${
                  creditScore.isExcellentScore ? 'text-green-600' :
                  creditScore.isGoodScore ? 'text-blue-600' : 'text-orange-600'
                }`}>
                  {creditScore.score}
                </div>
                <div className="text-sm text-gray-500">{creditScore.scoreType}</div>
              </div>
              <div>
                <div className="text-sm text-gray-500">Risk Level</div>
                <div className="font-medium">{creditScore.riskLevel}</div>
                <div className="text-sm text-gray-500">
                  Calculated: {formatDate(creditScore.calculatedAt)}
                </div>
                {creditScore.isExpired && (
                  <div className="text-sm text-red-600">Expired</div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Tab Navigation */}
        <div className="border-b border-gray-200 mb-8">
          <nav className="-mb-px flex space-x-8">
            {[
              { id: 'applications', name: 'Loan Applications', icon: '📋' },
              { id: 'loans', name: 'My Loans', icon: '💰' },
              { id: 'credit-lines', name: 'Credit Lines', icon: '💳' },
              { id: 'payments', name: 'Make Payment', icon: '💸' }
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

        {/* Applications Tab */}
        {activeTab === 'applications' && (
          <div className="space-y-6">
            {/* Submit Application Form */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Submit Loan Application</h3>
              <form onSubmit={handleSubmitApplication} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Loan Type</label>
                  <select
                    value={applicationForm.loanType}
                    onChange={(e) => setApplicationForm({ ...applicationForm, loanType: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="PERSONAL">Personal Loan</option>
                    <option value="HOME">Home Loan</option>
                    <option value="AUTO">Auto Loan</option>
                    <option value="BUSINESS">Business Loan</option>
                    <option value="EDUCATION">Education Loan</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Requested Amount</label>
                  <input
                    type="number"
                    value={applicationForm.requestedAmount}
                    onChange={(e) => setApplicationForm({ ...applicationForm, requestedAmount: Number(e.target.value) })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Term (Months)</label>
                  <input
                    type="number"
                    value={applicationForm.requestedTermMonths}
                    onChange={(e) => setApplicationForm({ ...applicationForm, requestedTermMonths: Number(e.target.value) })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Purpose</label>
                  <input
                    type="text"
                    value={applicationForm.purpose}
                    onChange={(e) => setApplicationForm({ ...applicationForm, purpose: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Employment Status</label>
                  <select
                    value={applicationForm.employmentStatus}
                    onChange={(e) => setApplicationForm({ ...applicationForm, employmentStatus: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="EMPLOYED">Employed</option>
                    <option value="SELF_EMPLOYED">Self-Employed</option>
                    <option value="UNEMPLOYED">Unemployed</option>
                    <option value="RETIRED">Retired</option>
                    <option value="STUDENT">Student</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Annual Income</label>
                  <input
                    type="number"
                    value={applicationForm.annualIncome}
                    onChange={(e) => setApplicationForm({ ...applicationForm, annualIncome: Number(e.target.value) })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div className="sm:col-span-2 lg:col-span-3">
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    {loading ? 'Submitting...' : 'Submit Application'}
                  </button>
                </div>
              </form>
            </div>

            {/* Applications List */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">My Applications</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {applications.map((application) => (
                  <div key={application.id} className="p-6">
                    <div className="flex items-center justify-between">
                      <div>
                        <h4 className="text-lg font-medium text-gray-900">
                          {application.applicationNumber}
                        </h4>
                        <p className="text-sm text-gray-500">{application.loanType}</p>
                        <p className="text-sm text-gray-500">
                          {formatCurrency(application.requestedAmount)} • {application.requestedTermMonths} months
                        </p>
                      </div>
                      <div className="text-right">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(application.status)}`}>
                          {application.status}
                        </span>
                        <p className="text-sm text-gray-500 mt-1">
                          {formatDate(application.submittedAt)}
                        </p>
                        {application.creditScore && (
                          <p className="text-sm text-gray-500">
                            Credit Score: {application.creditScore}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Loans Tab */}
        {activeTab === 'loans' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">My Loans</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {loans.map((loan) => (
                  <div key={loan.id} className="p-6">
                    <div className="flex items-center justify-between">
                      <div>
                        <h4 className="text-lg font-medium text-gray-900">
                          {loan.loanNumber}
                        </h4>
                        <p className="text-sm text-gray-500">{loan.loanType}</p>
                        <p className="text-sm text-gray-500">
                          Principal: {formatCurrency(loan.principalAmount)} • 
                          Rate: {loan.interestRate}% • 
                          Term: {loan.termMonths} months
                        </p>
                      </div>
                      <div className="text-right">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(loan.status)}`}>
                          {loan.status}
                        </span>
                        <p className="text-lg font-medium text-gray-900 mt-1">
                          {formatCurrency(loan.remainingBalance)} remaining
                        </p>
                        <p className="text-sm text-gray-500">
                          Monthly: {formatCurrency(loan.monthlyPayment)}
                        </p>
                        {loan.nextPaymentDate && (
                          <p className="text-sm text-gray-500">
                            Next Payment: {formatDate(loan.nextPaymentDate)}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Credit Lines Tab */}
        {activeTab === 'credit-lines' && (
          <div className="space-y-6">
            {/* Create Credit Line Form */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Create Credit Line</h3>
              <form onSubmit={handleCreateCreditLine} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Requested Credit Limit</label>
                  <input
                    type="number"
                    value={creditLineForm.requestedCreditLimit}
                    onChange={(e) => setCreditLineForm({ ...creditLineForm, requestedCreditLimit: Number(e.target.value) })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Purpose</label>
                  <input
                    type="text"
                    value={creditLineForm.purpose}
                    onChange={(e) => setCreditLineForm({ ...creditLineForm, purpose: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div className="sm:col-span-2">
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    {loading ? 'Creating...' : 'Create Credit Line'}
                  </button>
                </div>
              </form>
            </div>

            {/* Credit Lines List */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">My Credit Lines</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {creditLines.map((creditLine) => (
                  <div key={creditLine.id} className="p-6">
                    <div className="flex items-center justify-between">
                      <div>
                        <h4 className="text-lg font-medium text-gray-900">
                          {creditLine.creditLineNumber}
                        </h4>
                        <p className="text-sm text-gray-500">
                          Limit: {formatCurrency(creditLine.creditLimit)} • 
                          Rate: {creditLine.interestRate}%
                        </p>
                        <p className="text-sm text-gray-500">
                          Utilization: {(creditLine.utilizationRate * 100).toFixed(1)}%
                        </p>
                      </div>
                      <div className="text-right">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(creditLine.status)}`}>
                          {creditLine.status}
                        </span>
                        <p className="text-lg font-medium text-gray-900 mt-1">
                          {formatCurrency(creditLine.availableCredit)} available
                        </p>
                        <p className="text-sm text-gray-500">
                          Used: {formatCurrency(creditLine.usedCredit)}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Payments Tab */}
        {activeTab === 'payments' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Make Payment</h3>
              <form className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Select Loan</label>
                  <select className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    <option value="">Select a loan</option>
                    {loans.map((loan) => (
                      <option key={loan.id} value={loan.id}>
                        {loan.loanNumber} - {formatCurrency(loan.remainingBalance)} remaining
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Payment Amount</label>
                  <input
                    type="number"
                    value={paymentForm.amount}
                    onChange={(e) => setPaymentForm({ ...paymentForm, amount: Number(e.target.value) })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Payment Date</label>
                  <input
                    type="date"
                    value={paymentForm.paymentDate}
                    onChange={(e) => setPaymentForm({ ...paymentForm, paymentDate: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Payment Method</label>
                  <select
                    value={paymentForm.paymentMethod}
                    onChange={(e) => setPaymentForm({ ...paymentForm, paymentMethod: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="BANK_TRANSFER">Bank Transfer</option>
                    <option value="DEBIT_CARD">Debit Card</option>
                    <option value="CREDIT_CARD">Credit Card</option>
                    <option value="ACH">ACH</option>
                    <option value="WIRE">Wire Transfer</option>
                  </select>
                </div>
                <div className="sm:col-span-2">
                  <button
                    type="button"
                    onClick={() => handleProcessPayment('')}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    Process Payment
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default LoanManagement;

