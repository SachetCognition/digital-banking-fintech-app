import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface InternationalTransferRequest {
  fromAccountId: string;
  transferType: 'SWIFT' | 'SEPA' | 'WIRE';
  toSwiftCode?: string;
  toIban?: string;
  toBankName: string;
  toBankAddress?: string;
  toAccountNumber: string;
  toAccountName: string;
  toAddress?: string;
  toCountryCode: string;
  amount: number;
  currency: string;
  description?: string;
  referenceNumber?: string;
  correspondentBankSwift?: string;
  correspondentBankName?: string;
}

interface InternationalTransferResponse {
  id: string;
  customerId: string;
  fromAccountId: string;
  transferType: string;
  toSwiftCode?: string;
  toIban?: string;
  toBankName: string;
  toBankAddress?: string;
  toAccountNumber: string;
  toAccountName: string;
  toAddress?: string;
  toCountryCode: string;
  amount: number;
  currency: string;
  description?: string;
  referenceNumber?: string;
  status: string;
  swiftMessageId?: string;
  correspondentBankSwift?: string;
  correspondentBankName?: string;
  processingFee: number;
  exchangeRate?: number;
  createdAt: string;
  processedAt?: string;
  failureReason?: string;
}

const InternationalTransfers: React.FC = () => {
  const [transfers, setTransfers] = useState<InternationalTransferResponse[]>([]);
  const [accounts, setAccounts] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState<InternationalTransferRequest>({
    fromAccountId: '',
    transferType: 'SWIFT',
    toSwiftCode: '',
    toIban: '',
    toBankName: '',
    toBankAddress: '',
    toAccountNumber: '',
    toAccountName: '',
    toAddress: '',
    toCountryCode: '',
    amount: 0,
    currency: 'AED',
    description: '',
    referenceNumber: '',
    correspondentBankSwift: '',
    correspondentBankName: ''
  });

  useEffect(() => {
    fetchTransfers();
    fetchAccounts();
  }, []);

  const fetchTransfers = async () => {
    try {
      const response = await http.get('/api/v1/international-transfers');
      setTransfers(response.data);
    } catch (error) {
      console.error('Error fetching international transfers:', error);
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
      await http.post('/api/v1/international-transfers', formData);
      setShowForm(false);
      setFormData({
        fromAccountId: '',
        transferType: 'SWIFT',
        toSwiftCode: '',
        toIban: '',
        toBankName: '',
        toBankAddress: '',
        toAccountNumber: '',
        toAccountName: '',
        toAddress: '',
        toCountryCode: '',
        amount: 0,
        currency: 'AED',
        description: '',
        referenceNumber: '',
        correspondentBankSwift: '',
        correspondentBankName: ''
      });
      fetchTransfers();
    } catch (error) {
      console.error('Error creating international transfer:', error);
      alert('Error creating international transfer. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'amount' ? parseFloat(value) || 0 : value
    }));
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'text-green-600';
      case 'SENT': return 'text-blue-600';
      case 'PROCESSING': return 'text-yellow-600';
      case 'FAILED': return 'text-red-600';
      case 'PENDING': return 'text-gray-600';
      default: return 'text-gray-600';
    }
  };

  const getTransferTypeColor = (type: string) => {
    switch (type) {
      case 'SWIFT': return 'bg-blue-100 text-blue-800';
      case 'SEPA': return 'bg-green-100 text-green-800';
      case 'WIRE': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">International Transfers</h1>
        <p className="mt-2 text-gray-600">Send money internationally via SWIFT, SEPA, or Wire transfers</p>
      </div>

      <div className="mb-6">
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
        >
          {showForm ? 'Cancel' : 'New International Transfer'}
        </button>
      </div>

      {showForm && (
        <div className="bg-white shadow rounded-lg p-6 mb-8">
          <h2 className="text-xl font-semibold mb-4">Create International Transfer</h2>
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
                <label className="block text-sm font-medium text-gray-700">Transfer Type</label>
                <select
                  name="transferType"
                  value={formData.transferType}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="SWIFT">SWIFT</option>
                  <option value="SEPA">SEPA</option>
                  <option value="WIRE">Wire Transfer</option>
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
                  <option value="AED">AED - UAE Dirham</option>
                  <option value="USD">USD - US Dollar</option>
                  <option value="EUR">EUR - Euro</option>
                  <option value="GBP">GBP - British Pound</option>
                  <option value="SAR">SAR - Saudi Riyal</option>
                  <option value="EUR">EUR</option>
                  <option value="GBP">GBP</option>
                  <option value="JPY">JPY</option>
                  <option value="CAD">CAD</option>
                  <option value="AUD">AUD</option>
                </select>
              </div>

              {formData.transferType === 'SWIFT' && (
                <div>
                  <label className="block text-sm font-medium text-gray-700">SWIFT Code</label>
                  <input
                    type="text"
                    name="toSwiftCode"
                    value={formData.toSwiftCode}
                    onChange={handleInputChange}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              )}

              {formData.transferType === 'SEPA' && (
                <div>
                  <label className="block text-sm font-medium text-gray-700">IBAN</label>
                  <input
                    type="text"
                    name="toIban"
                    value={formData.toIban}
                    onChange={handleInputChange}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700">Bank Name</label>
                <input
                  type="text"
                  name="toBankName"
                  value={formData.toBankName}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
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
                <label className="block text-sm font-medium text-gray-700">Country Code</label>
                <input
                  type="text"
                  name="toCountryCode"
                  value={formData.toCountryCode}
                  onChange={handleInputChange}
                  required
                  maxLength={2}
                  placeholder="US, GB, DE, etc."
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">Reference Number (Optional)</label>
                <input
                  type="text"
                  name="referenceNumber"
                  value={formData.referenceNumber}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Bank Address (Optional)</label>
              <textarea
                name="toBankAddress"
                value={formData.toBankAddress}
                onChange={handleInputChange}
                rows={2}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Recipient Address (Optional)</label>
              <textarea
                name="toAddress"
                value={formData.toAddress}
                onChange={handleInputChange}
                rows={2}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              />
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
                {loading ? 'Creating...' : 'Create Transfer'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Transfer History</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  To Account
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Amount
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Fee
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Created
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {transfers.map((transfer) => (
                <tr key={transfer.id}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getTransferTypeColor(transfer.transferType)}`}>
                      {transfer.transferType}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">{transfer.toAccountName}</div>
                      <div className="text-sm text-gray-500">{transfer.toAccountNumber}</div>
                      <div className="text-sm text-gray-500">{transfer.toBankName}</div>
                      <div className="text-sm text-gray-500">{transfer.toCountryCode}</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {transfer.currency} {transfer.amount.toFixed(2)}
                    </div>
                    {transfer.exchangeRate && (
                      <div className="text-sm text-gray-500">
                        Rate: {transfer.exchangeRate.toFixed(4)}
                      </div>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`text-sm font-medium ${getStatusColor(transfer.status)}`}>
                      {transfer.status}
                    </span>
                    {transfer.swiftMessageId && (
                      <div className="text-sm text-gray-500">
                        ID: {transfer.swiftMessageId}
                      </div>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {transfer.currency} {transfer.processingFee.toFixed(2)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(transfer.createdAt).toLocaleDateString()}
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

export default InternationalTransfers;

