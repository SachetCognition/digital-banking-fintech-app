import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface RecurringPaymentRequest {
  fromAccountId: string;
  toAccountNumber: string;
  toBankCode?: string;
  toBankName?: string;
  toAccountName: string;
  amount: number;
  currency: string;
  description?: string;
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  dayOfMonth?: number;
  dayOfWeek?: number;
  startDate: string;
  endDate?: string;
  maxExecutions?: number;
}

interface RecurringPaymentResponse {
  id: string;
  customerId: string;
  fromAccountId: string;
  toAccountNumber: string;
  toBankCode?: string;
  toBankName?: string;
  toAccountName: string;
  amount: number;
  currency: string;
  description?: string;
  frequency: string;
  dayOfMonth?: number;
  dayOfWeek?: number;
  startDate: string;
  endDate?: string;
  nextExecutionDate: string;
  status: string;
  maxExecutions?: number;
  executionCount: number;
  createdAt: string;
  lastExecutedAt?: string;
  cancellationReason?: string;
}

const RecurringPayments: React.FC = () => {
  const [payments, setPayments] = useState<RecurringPaymentResponse[]>([]);
  const [accounts, setAccounts] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState<RecurringPaymentRequest>({
    fromAccountId: '',
    toAccountNumber: '',
    toBankCode: '',
    toBankName: '',
    toAccountName: '',
    amount: 0,
    currency: 'USD',
    description: '',
    frequency: 'MONTHLY',
    dayOfMonth: 1,
    dayOfWeek: 1,
    startDate: '',
    endDate: '',
    maxExecutions: undefined
  });

  useEffect(() => {
    fetchPayments();
    fetchAccounts();
  }, []);

  const fetchPayments = async () => {
    try {
      const response = await http.get('/api/v1/recurring-payments');
      setPayments(response.data);
    } catch (error) {
      console.error('Error fetching recurring payments:', error);
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
      await http.post('/api/v1/recurring-payments', formData);
      setShowForm(false);
      setFormData({
        fromAccountId: '',
        toAccountNumber: '',
        toBankCode: '',
        toBankName: '',
        toAccountName: '',
        amount: 0,
        currency: 'USD',
        description: '',
        frequency: 'MONTHLY',
        dayOfMonth: 1,
        dayOfWeek: 1,
        startDate: '',
        endDate: '',
        maxExecutions: undefined
      });
      fetchPayments();
    } catch (error) {
      console.error('Error creating recurring payment:', error);
      alert('Error creating recurring payment. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'amount' || name === 'dayOfMonth' || name === 'dayOfWeek' || name === 'maxExecutions' 
        ? (value ? parseInt(value) || 0 : undefined)
        : value
    }));
  };

  const handlePause = async (id: string) => {
    try {
      await http.post(`/api/v1/recurring-payments/${id}/pause`);
      fetchPayments();
    } catch (error) {
      console.error('Error pausing recurring payment:', error);
      alert('Error pausing recurring payment. Please try again.');
    }
  };

  const handleResume = async (id: string) => {
    try {
      await http.post(`/api/v1/recurring-payments/${id}/resume`);
      fetchPayments();
    } catch (error) {
      console.error('Error resuming recurring payment:', error);
      alert('Error resuming recurring payment. Please try again.');
    }
  };

  const handleCancel = async (id: string) => {
    const reason = prompt('Please provide a reason for cancellation:');
    if (reason) {
      try {
        await http.post(`/api/v1/recurring-payments/${id}/cancel?reason=${encodeURIComponent(reason)}`);
        fetchPayments();
      } catch (error) {
        console.error('Error cancelling recurring payment:', error);
        alert('Error cancelling recurring payment. Please try again.');
      }
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'text-green-600';
      case 'PAUSED': return 'text-yellow-600';
      case 'CANCELLED': return 'text-red-600';
      case 'COMPLETED': return 'text-blue-600';
      default: return 'text-gray-600';
    }
  };

  const getFrequencyLabel = (frequency: string) => {
    switch (frequency) {
      case 'DAILY': return 'Daily';
      case 'WEEKLY': return 'Weekly';
      case 'MONTHLY': return 'Monthly';
      case 'QUARTERLY': return 'Quarterly';
      case 'YEARLY': return 'Yearly';
      default: return frequency;
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Recurring Payments</h1>
        <p className="mt-2 text-gray-600">Set up and manage recurring payments</p>
      </div>

      <div className="mb-6">
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
        >
          {showForm ? 'Cancel' : 'New Recurring Payment'}
        </button>
      </div>

      {showForm && (
        <div className="bg-white shadow rounded-lg p-6 mb-8">
          <h2 className="text-xl font-semibold mb-4">Create Recurring Payment</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">From Account</label>
                <select
                  name="fromAccountId"
                  value={formData.fromAccountId}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="">Select Account</option>
                  {accounts.map(account => (
                    <option key={account.id} value={account.id}>
                      {account.accountName} - {account.accountNumber} ({account.currency})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Amount</label>
                <input
                  type="number"
                  name="amount"
                  value={formData.amount}
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
                <label className="block text-sm font-medium text-gray-700">Frequency</label>
                <select
                  name="frequency"
                  value={formData.frequency}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="DAILY">Daily</option>
                  <option value="WEEKLY">Weekly</option>
                  <option value="MONTHLY">Monthly</option>
                  <option value="QUARTERLY">Quarterly</option>
                  <option value="YEARLY">Yearly</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Account Number</label>
                <input
                  type="text"
                  name="toAccountNumber"
                  value={formData.toAccountNumber}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Account Name</label>
                <input
                  type="text"
                  name="toAccountName"
                  value={formData.toAccountName}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Bank Code (Optional)</label>
                <input
                  type="text"
                  name="toBankCode"
                  value={formData.toBankCode}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Bank Name (Optional)</label>
                <input
                  type="text"
                  name="toBankName"
                  value={formData.toBankName}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              {formData.frequency === 'MONTHLY' && (
                <div>
                  <label className="block text-sm font-medium text-gray-700">Day of Month</label>
                  <input
                    type="number"
                    name="dayOfMonth"
                    value={formData.dayOfMonth}
                    onChange={handleInputChange}
                    min="1"
                    max="31"
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              )}

              {formData.frequency === 'WEEKLY' && (
                <div>
                  <label className="block text-sm font-medium text-gray-700">Day of Week</label>
                  <select
                    name="dayOfWeek"
                    value={formData.dayOfWeek}
                    onChange={handleInputChange}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value={1}>Monday</option>
                    <option value={2}>Tuesday</option>
                    <option value={3}>Wednesday</option>
                    <option value={4}>Thursday</option>
                    <option value={5}>Friday</option>
                    <option value={6}>Saturday</option>
                    <option value={7}>Sunday</option>
                  </select>
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700">Start Date</label>
                <input
                  type="date"
                  name="startDate"
                  value={formData.startDate}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">End Date (Optional)</label>
                <input
                  type="date"
                  name="endDate"
                  value={formData.endDate}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Max Executions (Optional)</label>
                <input
                  type="number"
                  name="maxExecutions"
                  value={formData.maxExecutions || ''}
                  onChange={handleInputChange}
                  min="1"
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Description</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                rows={3}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            <div className="flex justify-end space-x-4">
              <button
                type="button"
                onClick={() => setShowForm(false)}
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? 'Creating...' : 'Create Recurring Payment'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Recurring Payments</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  To Account
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Amount
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Frequency
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Next Execution
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Executions
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {payments.map((payment) => (
                <tr key={payment.id}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">{payment.toAccountName}</div>
                      <div className="text-sm text-gray-500">{payment.toAccountNumber}</div>
                      {payment.toBankName && (
                        <div className="text-sm text-gray-500">{payment.toBankName}</div>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {payment.currency} {payment.amount.toFixed(2)}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {getFrequencyLabel(payment.frequency)}
                    </div>
                    {payment.dayOfMonth && (
                      <div className="text-sm text-gray-500">Day {payment.dayOfMonth}</div>
                    )}
                    {payment.dayOfWeek && (
                      <div className="text-sm text-gray-500">
                        {['', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'][payment.dayOfWeek]}
                      </div>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(payment.nextExecutionDate).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`text-sm font-medium ${getStatusColor(payment.status)}`}>
                      {payment.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {payment.executionCount}
                    {payment.maxExecutions && ` / ${payment.maxExecutions}`}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      {payment.status === 'ACTIVE' && (
                        <button
                          onClick={() => handlePause(payment.id)}
                          className="text-yellow-600 hover:text-yellow-900"
                        >
                          Pause
                        </button>
                      )}
                      {payment.status === 'PAUSED' && (
                        <button
                          onClick={() => handleResume(payment.id)}
                          className="text-green-600 hover:text-green-900"
                        >
                          Resume
                        </button>
                      )}
                      {(payment.status === 'ACTIVE' || payment.status === 'PAUSED') && (
                        <button
                          onClick={() => handleCancel(payment.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          Cancel
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default RecurringPayments;

