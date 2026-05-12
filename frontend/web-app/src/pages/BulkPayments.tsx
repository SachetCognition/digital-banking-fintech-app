import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface BulkPaymentItemRequest {
  recipientName: string;
  recipientAccountNumber: string;
  recipientBankCode?: string;
  recipientBankName?: string;
  amount: number;
  description?: string;
}

interface BulkPaymentRequest {
  batchName: string;
  fromAccountId: string;
  totalAmount: number;
  currency: string;
  items: BulkPaymentItemRequest[];
}

interface BulkPaymentItemResponse {
  id: string;
  bulkPaymentId: string;
  recipientName: string;
  recipientAccountNumber: string;
  recipientBankCode?: string;
  recipientBankName?: string;
  amount: number;
  description?: string;
  status: string;
  transferId?: string;
  createdAt: string;
  processedAt?: string;
  failureReason?: string;
}

interface BulkPaymentResponse {
  id: string;
  customerId: string;
  batchName: string;
  fromAccountId: string;
  totalAmount: number;
  currency: string;
  totalRecipients: number;
  status: string;
  createdAt: string;
  processedAt?: string;
  completedAt?: string;
  failureReason?: string;
}

const BulkPayments: React.FC = () => {
  const [bulkPayments, setBulkPayments] = useState<BulkPaymentResponse[]>([]);
  const [accounts, setAccounts] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [selectedBulkPayment, setSelectedBulkPayment] = useState<string | null>(null);
  const [bulkPaymentItems, setBulkPaymentItems] = useState<BulkPaymentItemResponse[]>([]);
  const [formData, setFormData] = useState<BulkPaymentRequest>({
    batchName: '',
    fromAccountId: '',
    totalAmount: 0,
    currency: 'AED',
    items: []
  });

  useEffect(() => {
    fetchBulkPayments();
    fetchAccounts();
  }, []);

  const fetchBulkPayments = async () => {
    try {
      const response = await http.get('/api/v1/bulk-payments');
      setBulkPayments(response.data);
    } catch (error) {
      console.error('Error fetching bulk payments:', error);
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

  const fetchBulkPaymentItems = async (bulkPaymentId: string) => {
    try {
      const response = await http.get(`/api/v1/bulk-payments/${bulkPaymentId}/items`);
      setBulkPaymentItems(response.data);
    } catch (error) {
      console.error('Error fetching bulk payment items:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await http.post('/api/v1/bulk-payments', formData);
      setShowForm(false);
      setFormData({
        batchName: '',
        fromAccountId: '',
        totalAmount: 0,
        currency: 'AED',
        items: []
      });
      fetchBulkPayments();
    } catch (error) {
      console.error('Error creating bulk payment:', error);
      alert('Error creating bulk payment. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'totalAmount' ? parseFloat(value) || 0 : value
    }));
  };

  const addItem = () => {
    setFormData(prev => ({
      ...prev,
      items: [...prev.items, {
        recipientName: '',
        recipientAccountNumber: '',
        recipientBankCode: '',
        recipientBankName: '',
        amount: 0,
        description: ''
      }]
    }));
  };

  const removeItem = (index: number) => {
    setFormData(prev => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index)
    }));
  };

  const updateItem = (index: number, field: keyof BulkPaymentItemRequest, value: string | number) => {
    setFormData(prev => ({
      ...prev,
      items: prev.items.map((item, i) => 
        i === index ? { ...item, [field]: value } : item
      )
    }));
  };

  const calculateTotalAmount = () => {
    return formData.items.reduce((sum, item) => sum + item.amount, 0);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'text-green-600';
      case 'PROCESSING': return 'text-blue-600';
      case 'FAILED': return 'text-red-600';
      case 'PARTIAL': return 'text-yellow-600';
      case 'PENDING': return 'text-gray-600';
      default: return 'text-gray-600';
    }
  };

  const getItemStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'text-green-600';
      case 'PROCESSING': return 'text-blue-600';
      case 'FAILED': return 'text-red-600';
      case 'PENDING': return 'text-gray-600';
      default: return 'text-gray-600';
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Bulk Payments</h1>
        <p className="mt-2 text-gray-600">Process multiple payments in a single batch</p>
      </div>

      <div className="mb-6">
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
        >
          {showForm ? 'Cancel' : 'New Bulk Payment'}
        </button>
      </div>

      {showForm && (
        <div className="bg-white shadow rounded-lg p-6 mb-8">
          <h2 className="text-xl font-semibold mb-4">Create Bulk Payment</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Batch Name</label>
                <input
                  type="text"
                  name="batchName"
                  value={formData.batchName}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

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
                </select>
              </div>
            </div>

            <div className="border-t pt-4">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-medium text-gray-900">Payment Items</h3>
                <button
                  type="button"
                  onClick={addItem}
                  className="bg-green-600 text-white px-3 py-1 rounded-md hover:bg-green-700 transition-colors"
                >
                  Add Item
                </button>
              </div>

              <div className="space-y-4">
                {formData.items.map((item, index) => (
                  <div key={index} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex justify-between items-center mb-2">
                      <h4 className="text-sm font-medium text-gray-700">Item {index + 1}</h4>
                      <button
                        type="button"
                        onClick={() => removeItem(index)}
                        className="text-red-600 hover:text-red-900 text-sm"
                      >
                        Remove
                      </button>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700">Recipient Name</label>
                        <input
                          type="text"
                          value={item.recipientName}
                          onChange={(e) => updateItem(index, 'recipientName', e.target.value)}
                          required
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700">Account Number</label>
                        <input
                          type="text"
                          value={item.recipientAccountNumber}
                          onChange={(e) => updateItem(index, 'recipientAccountNumber', e.target.value)}
                          required
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700">Amount</label>
                        <input
                          type="number"
                          value={item.amount}
                          onChange={(e) => updateItem(index, 'amount', parseFloat(e.target.value) || 0)}
                          min="0.01"
                          step="0.01"
                          required
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700">Bank Code (Optional)</label>
                        <input
                          type="text"
                          value={item.recipientBankCode || ''}
                          onChange={(e) => updateItem(index, 'recipientBankCode', e.target.value)}
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700">Bank Name (Optional)</label>
                        <input
                          type="text"
                          value={item.recipientBankName || ''}
                          onChange={(e) => updateItem(index, 'recipientBankName', e.target.value)}
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700">Description (Optional)</label>
                        <input
                          type="text"
                          value={item.description || ''}
                          onChange={(e) => updateItem(index, 'description', e.target.value)}
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {formData.items.length > 0 && (
                <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                  <div className="text-sm text-gray-600">
                    Total Items: {formData.items.length} | 
                    Total Amount: {formData.currency} {calculateTotalAmount().toFixed(2)}
                  </div>
                </div>
              )}
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
                disabled={loading || formData.items.length === 0}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? 'Creating...' : 'Create Bulk Payment'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white shadow rounded-lg">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-medium text-gray-900">Bulk Payment History</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Batch Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Amount
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Recipients
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {bulkPayments.map((bulkPayment) => (
                  <tr key={bulkPayment.id}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{bulkPayment.batchName}</div>
                      <div className="text-sm text-gray-500">
                        {new Date(bulkPayment.createdAt).toLocaleDateString()}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {bulkPayment.currency} {bulkPayment.totalAmount.toFixed(2)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {bulkPayment.totalRecipients}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`text-sm font-medium ${getStatusColor(bulkPayment.status)}`}>
                        {bulkPayment.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <button
                        onClick={() => {
                          setSelectedBulkPayment(bulkPayment.id);
                          fetchBulkPaymentItems(bulkPayment.id);
                        }}
                        className="text-blue-600 hover:text-blue-900"
                      >
                        View Items
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {selectedBulkPayment && (
          <div className="bg-white shadow rounded-lg">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-medium text-gray-900">Payment Items</h2>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Recipient
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Amount
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {bulkPaymentItems.map((item) => (
                    <tr key={item.id}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-medium text-gray-900">{item.recipientName}</div>
                          <div className="text-sm text-gray-500">{item.recipientAccountNumber}</div>
                          {item.recipientBankName && (
                            <div className="text-sm text-gray-500">{item.recipientBankName}</div>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {item.amount.toFixed(2)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`text-sm font-medium ${getItemStatusColor(item.status)}`}>
                          {item.status}
                        </span>
                        {item.failureReason && (
                          <div className="text-sm text-red-500">{item.failureReason}</div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default BulkPayments;

