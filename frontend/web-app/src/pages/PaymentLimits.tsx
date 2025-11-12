import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface PaymentLimitRequest {
  accountId?: string;
  limitType: 'DAILY_AMOUNT' | 'MONTHLY_AMOUNT' | 'PER_TRANSACTION' | 'DAILY_COUNT' | 'MONTHLY_COUNT' | 'WEEKLY_AMOUNT' | 'YEARLY_AMOUNT';
  limitValue: number;
  currency: string;
  expiresAt?: string;
}

interface PaymentLimitResponse {
  id: string;
  customerId: string;
  accountId?: string;
  limitType: string;
  limitValue: number;
  currency: string;
  currentUsage: number;
  usagePeriodStart: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  expiresAt?: string;
}

const PaymentLimits: React.FC = () => {
  const [limits, setLimits] = useState<PaymentLimitResponse[]>([]);
  const [accounts, setAccounts] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingLimit, setEditingLimit] = useState<PaymentLimitResponse | null>(null);
  const [showAccountLimits, setShowAccountLimits] = useState(false);
  const [formData, setFormData] = useState<PaymentLimitRequest>({
    accountId: '',
    limitType: 'DAILY_AMOUNT',
    limitValue: 0,
    currency: 'USD',
    expiresAt: ''
  });

  useEffect(() => {
    fetchLimits();
    fetchAccounts();
  }, []);

  const fetchLimits = async () => {
    try {
      const response = await http.get('/api/v1/payment-limits');
      setLimits(response.data);
    } catch (error) {
      console.error('Error fetching payment limits:', error);
    }
  };

  const fetchAccounts = async () => {
    try {
      const response = await http.get('/api/v1/accounts');
      setAccounts(response.data);
    } catch (error) {
      console.error('Error fetching accounts:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (editingLimit) {
        await http.put(`/api/v1/payment-limits/${editingLimit.id}`, formData);
      } else {
        await http.post('/api/v1/payment-limits', formData);
      }
      setShowForm(false);
      setEditingLimit(null);
      setFormData({
        accountId: '',
        limitType: 'DAILY_AMOUNT',
        limitValue: 0,
        currency: 'USD',
        expiresAt: ''
      });
      fetchLimits();
    } catch (error) {
      console.error('Error saving payment limit:', error);
      alert('Error saving payment limit. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'limitValue' ? parseFloat(value) || 0 : value
    }));
  };

  const handleEdit = (limit: PaymentLimitResponse) => {
    setEditingLimit(limit);
    setFormData({
      accountId: limit.accountId || '',
      limitType: limit.limitType as any,
      limitValue: limit.limitValue,
      currency: limit.currency,
      expiresAt: limit.expiresAt ? new Date(limit.expiresAt).toISOString().split('T')[0] : ''
    });
    setShowForm(true);
  };

  const handleToggle = async (id: string) => {
    try {
      await http.post(`/api/v1/payment-limits/${id}/toggle`);
      fetchLimits();
    } catch (error) {
      console.error('Error toggling payment limit:', error);
      alert('Error updating payment limit. Please try again.');
    }
  };

  const handleResetUsage = async (id: string) => {
    try {
      await http.post(`/api/v1/payment-limits/${id}/reset-usage`);
      fetchLimits();
    } catch (error) {
      console.error('Error resetting usage:', error);
      alert('Error resetting usage. Please try again.');
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this payment limit?')) {
      try {
        await http.delete(`/api/v1/payment-limits/${id}`);
        fetchLimits();
      } catch (error) {
        console.error('Error deleting payment limit:', error);
        alert('Error deleting payment limit. Please try again.');
      }
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingLimit(null);
    setFormData({
      accountId: '',
      limitType: 'DAILY_AMOUNT',
      limitValue: 0,
      currency: 'USD',
      expiresAt: ''
    });
  };

  const getLimitTypeLabel = (type: string) => {
    switch (type) {
      case 'DAILY_AMOUNT': return 'Daily Amount';
      case 'MONTHLY_AMOUNT': return 'Monthly Amount';
      case 'PER_TRANSACTION': return 'Per Transaction';
      case 'DAILY_COUNT': return 'Daily Count';
      case 'MONTHLY_COUNT': return 'Monthly Count';
      case 'WEEKLY_AMOUNT': return 'Weekly Amount';
      case 'YEARLY_AMOUNT': return 'Yearly Amount';
      default: return type;
    }
  };

  const getUsagePercentage = (currentUsage: number, limitValue: number) => {
    return Math.min((currentUsage / limitValue) * 100, 100);
  };

  const getUsageColor = (percentage: number) => {
    if (percentage >= 100) return 'bg-red-500';
    if (percentage >= 80) return 'bg-yellow-500';
    return 'bg-green-500';
  };

  const filteredLimits = showAccountLimits 
    ? limits.filter(limit => limit.accountId)
    : limits;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Payment Limits</h1>
        <p className="mt-2 text-gray-600">Manage your payment limits and restrictions</p>
      </div>

      <div className="mb-6 flex flex-col sm:flex-row gap-4">
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
        >
          {showForm ? 'Cancel' : 'New Payment Limit'}
        </button>
        
        <button
          onClick={() => setShowAccountLimits(!showAccountLimits)}
          className={`px-4 py-2 rounded-md transition-colors ${
            showAccountLimits 
              ? 'bg-yellow-600 text-white hover:bg-yellow-700' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          {showAccountLimits ? 'Show All Limits' : 'Show Account Limits Only'}
        </button>
      </div>

      {showForm && (
        <div className="bg-white shadow rounded-lg p-6 mb-8">
          <h2 className="text-xl font-semibold mb-4">
            {editingLimit ? 'Edit Payment Limit' : 'Create Payment Limit'}
          </h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Account (Optional)</label>
                <select
                  name="accountId"
                  value={formData.accountId}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="">All Accounts (Customer Level)</option>
                  {accounts.map(account => (
                    <option key={account.id} value={account.id}>
                      {account.accountName} - {account.accountNumber} ({account.currency})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Limit Type</label>
                <select
                  name="limitType"
                  value={formData.limitType}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="DAILY_AMOUNT">Daily Amount</option>
                  <option value="MONTHLY_AMOUNT">Monthly Amount</option>
                  <option value="PER_TRANSACTION">Per Transaction</option>
                  <option value="DAILY_COUNT">Daily Count</option>
                  <option value="MONTHLY_COUNT">Monthly Count</option>
                  <option value="WEEKLY_AMOUNT">Weekly Amount</option>
                  <option value="YEARLY_AMOUNT">Yearly Amount</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Limit Value</label>
                <input
                  type="number"
                  name="limitValue"
                  value={formData.limitValue}
                  onChange={handleInputChange}
                  min="0.01"
                  step="0.01"
                  required
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Currency</label>
                <select
                  name="currency"
                  value={formData.currency}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="USD">USD</option>
                  <option value="EUR">EUR</option>
                  <option value="GBP">GBP</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Expires At (Optional)</label>
                <input
                  type="date"
                  name="expiresAt"
                  value={formData.expiresAt}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>

            <div className="flex justify-end space-x-4">
              <button
                type="button"
                onClick={handleCancel}
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? 'Saving...' : (editingLimit ? 'Update Limit' : 'Create Limit')}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">
            Payment Limits ({filteredLimits.length})
          </h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Account
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Limit Value
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Current Usage
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Expires
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredLimits.map((limit) => {
                const usagePercentage = getUsagePercentage(limit.currentUsage, limit.limitValue);
                return (
                  <tr key={limit.id}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {getLimitTypeLabel(limit.limitType)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {limit.accountId ? 
                          accounts.find(acc => acc.id === limit.accountId)?.accountName || 'Unknown Account' :
                          'All Accounts'
                        }
                      </div>
                      {limit.accountId && (
                        <div className="text-sm text-gray-500">
                          {accounts.find(acc => acc.id === limit.accountId)?.accountNumber}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {limit.currency} {limit.limitValue.toFixed(2)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-1 bg-gray-200 rounded-full h-2 mr-2">
                          <div 
                            className={`h-2 rounded-full ${getUsageColor(usagePercentage)}`}
                            style={{ width: `${usagePercentage}%` }}
                          ></div>
                        </div>
                        <div className="text-sm text-gray-900">
                          {limit.currency} {limit.currentUsage.toFixed(2)}
                        </div>
                      </div>
                      <div className="text-sm text-gray-500">
                        {usagePercentage.toFixed(1)}% used
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        limit.isActive 
                          ? 'bg-green-100 text-green-800' 
                          : 'bg-red-100 text-red-800'
                      }`}>
                        {limit.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {limit.expiresAt ? new Date(limit.expiresAt).toLocaleDateString() : 'Never'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleEdit(limit)}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleToggle(limit.id)}
                          className="text-yellow-600 hover:text-yellow-900"
                        >
                          {limit.isActive ? 'Deactivate' : 'Activate'}
                        </button>
                        <button
                          onClick={() => handleResetUsage(limit.id)}
                          className="text-green-600 hover:text-green-900"
                        >
                          Reset Usage
                        </button>
                        <button
                          onClick={() => handleDelete(limit.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default PaymentLimits;

