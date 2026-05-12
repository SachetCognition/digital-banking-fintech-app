import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface FeeCalculation {
  id: string;
  customerId: string;
  accountId: string;
  transactionId: string;
  transactionAmount: number;
  feeType: string;
  feeAmount: number;
  baseAmount: number;
  feeRate: number;
  calculationMethod: string;
  status: string;
  calculatedAt: string;
}

interface InterestCalculation {
  id: string;
  customerId: string;
  accountId: string;
  calculationDate: string;
  accountBalance: number;
  accountType: string;
  interestRate: number;
  dailyInterest: number;
  monthlyInterest: number;
  yearToDateInterest: number;
  compoundingFrequency: string;
  calculationMethod: string;
  isProcessed: boolean;
  status: string;
  calculatedAt: string;
}

interface TaxDocument {
  id: string;
  customerId: string;
  taxYear: number;
  documentType: string;
  documentNumber: string;
  totalInterest: number;
  totalDividends: number;
  totalCapitalGains: number;
  totalOtherIncome: number;
  federalTaxWithheld: number;
  stateTaxWithheld: number;
  documentStatus: string;
  generatedAt: string;
  mailedAt: string;
  deliveredAt: string;
  filePath: string;
}

interface LoyaltyAccount {
  id: string;
  customerId: string;
  accountNumber: string;
  totalPoints: number;
  availablePoints: number;
  lifetimePoints: number;
  tierLevel: string;
  tierPoints: number;
  nextTierPoints: number;
  enrollmentDate: string;
  lastActivityDate: string;
  isActive: boolean;
}

interface ReferralCode {
  id: string;
  customerId: string;
  referralCode: string;
  isActive: boolean;
  usageCount: number;
  maxUsage: number;
  createdAt: string;
  expiresAt: string;
}

const BusinessDashboard: React.FC = () => {
  const [fees, setFees] = useState<FeeCalculation[]>([]);
  const [interest, setInterest] = useState<InterestCalculation[]>([]);
  const [taxDocuments, setTaxDocuments] = useState<TaxDocument[]>([]);
  const [loyaltyAccount, setLoyaltyAccount] = useState<LoyaltyAccount | null>(null);
  const [referralCode, setReferralCode] = useState<ReferralCode | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    loadBusinessData();
  }, []);

  const loadBusinessData = async () => {
    try {
      setLoading(true);
      const [feesRes, interestRes, taxRes, loyaltyRes, referralRes] = await Promise.all([
        http.get('/api/v1/business/fees'),
        http.get('/api/v1/business/interest'),
        http.get('/api/v1/business/tax-documents'),
        http.get('/api/v1/business/loyalty/account'),
        http.get('/api/v1/business/referral/code')
      ]);
      
      setFees(feesRes.data);
      setInterest(interestRes.data);
      setTaxDocuments(taxRes.data);
      setLoyaltyAccount(loyaltyRes.data);
      setReferralCode(referralRes.data);
    } catch (err) {
      setError('Failed to load business data');
    } finally {
      setLoading(false);
    }
  };

  const calculateFee = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/business/fees/calculate', {
        customerId: 'customer-123',
        accountId: 'account-123',
        transactionId: 'txn-123',
        transactionAmount: 1000.00,
        feeType: 'transaction'
      });
      await loadBusinessData();
    } catch (err) {
      setError('Failed to calculate fee');
    } finally {
      setLoading(false);
    }
  };

  const calculateInterest = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/business/interest/calculate', {
        customerId: 'customer-123',
        accountId: 'account-123',
        accountType: 'savings',
        accountBalance: 5000.00
      });
      await loadBusinessData();
    } catch (err) {
      setError('Failed to calculate interest');
    } finally {
      setLoading(false);
    }
  };

  const generateTaxDocument = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/business/tax-documents/generate', {
        customerId: 'customer-123',
        taxYear: 2024,
        documentType: '1099-INT'
      });
      await loadBusinessData();
    } catch (err) {
      setError('Failed to generate tax document');
    } finally {
      setLoading(false);
    }
  };

  const enrollLoyalty = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/business/loyalty/enroll', {
        customerId: 'customer-123'
      });
      await loadBusinessData();
    } catch (err) {
      setError('Failed to enroll in loyalty program');
    } finally {
      setLoading(false);
    }
  };

  const generateReferralCode = async () => {
    try {
      setLoading(true);
      await http.post('/api/v1/business/referral/generate-code', {
        customerId: 'customer-123'
      });
      await loadBusinessData();
    } catch (err) {
      setError('Failed to generate referral code');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'success':
      case 'completed':
      case 'calculated':
      case 'generated':
        return 'text-green-600 bg-green-100';
      case 'failed':
      case 'error':
        return 'text-red-600 bg-red-100';
      case 'pending':
      case 'processing':
        return 'text-blue-600 bg-blue-100';
      case 'warning':
        return 'text-yellow-600 bg-yellow-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getTierColor = (tier: string) => {
    switch (tier.toLowerCase()) {
      case 'platinum':
        return 'text-purple-600 bg-purple-100';
      case 'gold':
        return 'text-yellow-600 bg-yellow-100';
      case 'silver':
        return 'text-gray-600 bg-gray-100';
      case 'bronze':
        return 'text-orange-600 bg-orange-100';
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

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">Loading business data...</p>
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
            onClick={loadBusinessData}
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
              <h1 className="text-3xl font-bold text-gray-900">Business Features Dashboard</h1>
              <p className="mt-2 text-gray-600">Fee management, interest calculations, tax reporting, loyalty program, and referral system</p>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={calculateFee}
                className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
              >
                Calculate Fee
              </button>
              <button
                onClick={calculateInterest}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                Calculate Interest
              </button>
              <button
                onClick={generateTaxDocument}
                className="px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700"
              >
                Generate Tax Doc
              </button>
              <button
                onClick={enrollLoyalty}
                className="px-4 py-2 bg-orange-600 text-white rounded-md hover:bg-orange-700"
              >
                Enroll Loyalty
              </button>
              <button
                onClick={generateReferralCode}
                className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
              >
                Generate Referral Code
              </button>
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="mb-8">
          <nav className="flex space-x-8">
            {[
              { id: 'overview', name: 'Overview' },
              { id: 'fees', name: 'Fee Management' },
              { id: 'interest', name: 'Interest Calculations' },
              { id: 'tax', name: 'Tax Reporting' },
              { id: 'loyalty', name: 'Loyalty Program' },
              { id: 'referral', name: 'Referral System' }
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
            {/* Fee Summary */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Fee Management Summary</h3>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">
                    {fees.filter(f => f.status === 'calculated').length}
                  </div>
                  <div className="text-sm text-gray-500">Calculated</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">
                    {fees.filter(f => f.status === 'pending').length}
                  </div>
                  <div className="text-sm text-gray-500">Pending</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-600">
                    {fees.length}
                  </div>
                  <div className="text-sm text-gray-500">Total</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-purple-600">
                    {formatCurrency(fees.reduce((sum, f) => sum + f.feeAmount, 0))}
                  </div>
                  <div className="text-sm text-gray-500">Total Fees</div>
                </div>
              </div>
            </div>

            {/* Interest Summary */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Interest Calculations Summary</h3>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">
                    {interest.filter(i => i.isProcessed).length}
                  </div>
                  <div className="text-sm text-gray-500">Processed</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">
                    {interest.filter(i => !i.isProcessed).length}
                  </div>
                  <div className="text-sm text-gray-500">Pending</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-600">
                    {interest.length}
                  </div>
                  <div className="text-sm text-gray-500">Total</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-purple-600">
                    {formatCurrency(interest.reduce((sum, i) => sum + i.monthlyInterest, 0))}
                  </div>
                  <div className="text-sm text-gray-500">Total Interest</div>
                </div>
              </div>
            </div>

            {/* Tax Documents Summary */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Tax Documents Summary</h3>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">
                    {taxDocuments.filter(t => t.documentStatus === 'generated').length}
                  </div>
                  <div className="text-sm text-gray-500">Generated</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">
                    {taxDocuments.filter(t => t.documentStatus === 'mailed').length}
                  </div>
                  <div className="text-sm text-gray-500">Mailed</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-600">
                    {taxDocuments.length}
                  </div>
                  <div className="text-sm text-gray-500">Total</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-purple-600">
                    {formatCurrency(taxDocuments.reduce((sum, t) => sum + t.totalInterest, 0))}
                  </div>
                  <div className="text-sm text-gray-500">Total Interest</div>
                </div>
              </div>
            </div>

            {/* Loyalty Program Summary */}
            {loyaltyAccount && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Loyalty Program Summary</h3>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-green-600">
                      {loyaltyAccount.totalPoints.toLocaleString()}
                    </div>
                    <div className="text-sm text-gray-500">Total Points</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-blue-600">
                      {loyaltyAccount.availablePoints.toLocaleString()}
                    </div>
                    <div className="text-sm text-gray-500">Available Points</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-purple-600">
                      {loyaltyAccount.lifetimePoints.toLocaleString()}
                    </div>
                    <div className="text-sm text-gray-500">Lifetime Points</div>
                  </div>
                  <div className="text-center">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getTierColor(loyaltyAccount.tierLevel)}`}>
                      {loyaltyAccount.tierLevel.toUpperCase()}
                    </span>
                    <div className="text-sm text-gray-500 mt-1">Tier Level</div>
                  </div>
                </div>
              </div>
            )}

            {/* Referral System Summary */}
            {referralCode && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Referral System Summary</h3>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-green-600">
                      {referralCode.referralCode}
                    </div>
                    <div className="text-sm text-gray-500">Referral Code</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-blue-600">
                      {referralCode.usageCount}
                    </div>
                    <div className="text-sm text-gray-500">Usage Count</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-purple-600">
                      {referralCode.maxUsage}
                    </div>
                    <div className="text-sm text-gray-500">Max Usage</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-orange-600">
                      {formatDate(referralCode.expiresAt)}
                    </div>
                    <div className="text-sm text-gray-500">Expires</div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Fees Tab */}
        {activeTab === 'fees' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Fee Calculations</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {fees.map((fee) => (
                  <div key={fee.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(fee.status)}`}>
                            {fee.status}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{fee.feeType}</span>
                          <span className="text-sm text-gray-500">({fee.transactionId})</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Amount: {formatCurrency(fee.transactionAmount)} • 
                          Fee: {formatCurrency(fee.feeAmount)} • 
                          Rate: {(fee.feeRate * 100).toFixed(4)}% • 
                          {formatDateTime(fee.calculatedAt)}
                        </p>
                        <p className="text-sm text-gray-500 mt-1">{fee.calculationMethod}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Interest Tab */}
        {activeTab === 'interest' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Interest Calculations</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {interest.map((calc) => (
                  <div key={calc.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(calc.isProcessed ? 'processed' : 'pending')}`}>
                            {calc.isProcessed ? 'Processed' : 'Pending'}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{calc.accountType}</span>
                          <span className="text-sm text-gray-500">({calc.accountId})</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Balance: {formatCurrency(calc.accountBalance)} • 
                          Rate: {(calc.interestRate * 100).toFixed(4)}% • 
                          Daily: {formatCurrency(calc.dailyInterest)} • 
                          Monthly: {formatCurrency(calc.monthlyInterest)} • 
                          YTD: {formatCurrency(calc.yearToDateInterest)}
                        </p>
                        <p className="text-sm text-gray-500 mt-1">
                          {calc.compoundingFrequency} • {formatDate(calc.calculationDate)}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Tax Tab */}
        {activeTab === 'tax' && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Tax Documents</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {taxDocuments.map((doc) => (
                  <div key={doc.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(doc.documentStatus)}`}>
                            {doc.documentStatus}
                          </span>
                          <span className="text-sm font-medium text-gray-900">{doc.documentType}</span>
                          <span className="text-sm text-gray-500">({doc.documentNumber})</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Tax Year: {doc.taxYear} • 
                          Interest: {formatCurrency(doc.totalInterest)} • 
                          Dividends: {formatCurrency(doc.totalDividends)} • 
                          Capital Gains: {formatCurrency(doc.totalCapitalGains)} • 
                          Other Income: {formatCurrency(doc.totalOtherIncome)}
                        </p>
                        <p className="text-sm text-gray-500 mt-1">
                          Federal Withheld: {formatCurrency(doc.federalTaxWithheld)} • 
                          State Withheld: {formatCurrency(doc.stateTaxWithheld)} • 
                          {formatDateTime(doc.generatedAt)}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Loyalty Tab */}
        {activeTab === 'loyalty' && loyaltyAccount && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Loyalty Account Details</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h4 className="text-sm font-medium text-gray-500 mb-2">Account Information</h4>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Account Number:</span>
                      <span className="text-sm font-medium">{loyaltyAccount.accountNumber}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Tier Level:</span>
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getTierColor(loyaltyAccount.tierLevel)}`}>
                        {loyaltyAccount.tierLevel.toUpperCase()}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Enrollment Date:</span>
                      <span className="text-sm font-medium">{formatDate(loyaltyAccount.enrollmentDate)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Last Activity:</span>
                      <span className="text-sm font-medium">{formatDate(loyaltyAccount.lastActivityDate)}</span>
                    </div>
                  </div>
                </div>
                <div>
                  <h4 className="text-sm font-medium text-gray-500 mb-2">Points Summary</h4>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Total Points:</span>
                      <span className="text-sm font-medium">{loyaltyAccount.totalPoints.toLocaleString()}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Available Points:</span>
                      <span className="text-sm font-medium">{loyaltyAccount.availablePoints.toLocaleString()}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Lifetime Points:</span>
                      <span className="text-sm font-medium">{loyaltyAccount.lifetimePoints.toLocaleString()}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Tier Points:</span>
                      <span className="text-sm font-medium">{loyaltyAccount.tierPoints.toLocaleString()}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Next Tier:</span>
                      <span className="text-sm font-medium">{loyaltyAccount.nextTierPoints.toLocaleString()}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Referral Tab */}
        {activeTab === 'referral' && referralCode && (
          <div className="space-y-6">
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Referral Code Details</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h4 className="text-sm font-medium text-gray-500 mb-2">Code Information</h4>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Referral Code:</span>
                      <span className="text-sm font-medium font-mono">{referralCode.referralCode}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Status:</span>
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(referralCode.isActive ? 'active' : 'inactive')}`}>
                        {referralCode.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Usage Count:</span>
                      <span className="text-sm font-medium">{referralCode.usageCount}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Max Usage:</span>
                      <span className="text-sm font-medium">{referralCode.maxUsage}</span>
                    </div>
                  </div>
                </div>
                <div>
                  <h4 className="text-sm font-medium text-gray-500 mb-2">Timeline</h4>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Created:</span>
                      <span className="text-sm font-medium">{formatDateTime(referralCode.createdAt)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Expires:</span>
                      <span className="text-sm font-medium">{formatDateTime(referralCode.expiresAt)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Remaining Usage:</span>
                      <span className="text-sm font-medium">{referralCode.maxUsage - referralCode.usageCount}</span>
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

export default BusinessDashboard;

