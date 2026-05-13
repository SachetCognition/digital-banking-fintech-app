import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface PaymentTemplateRequest {
  templateName: string;
  fromAccountId: string;
  toAccountNumber: string;
  toBankCode?: string;
  toBankName?: string;
  toAccountName: string;
  amount?: number;
  currency: string;
  description?: string;
}

interface PaymentTemplateResponse {
  id: string;
  customerId: string;
  templateName: string;
  fromAccountId: string;
  toAccountNumber: string;
  toBankCode?: string;
  toBankName?: string;
  toAccountName: string;
  amount?: number;
  currency: string;
  description?: string;
  isFavorite: boolean;
  createdAt: string;
  updatedAt: string;
}

const PaymentTemplates: React.FC = () => {
  const [templates, setTemplates] = useState<PaymentTemplateResponse[]>([]);
  const [accounts, setAccounts] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<PaymentTemplateResponse | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [showFavorites, setShowFavorites] = useState(false);
  const [formData, setFormData] = useState<PaymentTemplateRequest>({
    templateName: '',
    fromAccountId: '',
    toAccountNumber: '',
    toBankCode: '',
    toBankName: '',
    toAccountName: '',
    amount: 0,
    currency: 'AED',
    description: ''
  });

  useEffect(() => {
    fetchTemplates();
    fetchAccounts();
  }, []);

  const fetchTemplates = async () => {
    try {
      const response = await http.get('/api/v1/payment-templates');
      setTemplates(response.data);
    } catch (error) {
      console.error('Error fetching payment templates:', error);
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
      if (editingTemplate) {
        await http.put(`/api/v1/payment-templates/${editingTemplate.id}`, formData);
      } else {
        await http.post('/api/v1/payment-templates', formData);
      }
      setShowForm(false);
      setEditingTemplate(null);
      setFormData({
        templateName: '',
        fromAccountId: '',
        toAccountNumber: '',
        toBankCode: '',
        toBankName: '',
        toAccountName: '',
        amount: 0,
        currency: 'AED',
        description: ''
      });
      fetchTemplates();
    } catch (error) {
      console.error('Error saving payment template:', error);
      alert('Error saving payment template. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'amount' ? (value ? parseFloat(value) : 0) : value
    }));
  };

  const handleEdit = (template: PaymentTemplateResponse) => {
    setEditingTemplate(template);
    setFormData({
      templateName: template.templateName,
      fromAccountId: template.fromAccountId,
      toAccountNumber: template.toAccountNumber,
      toBankCode: template.toBankCode || '',
      toBankName: template.toBankName || '',
      toAccountName: template.toAccountName,
      amount: template.amount || 0,
      currency: template.currency,
      description: template.description || ''
    });
    setShowForm(true);
  };

  const handleToggleFavorite = async (id: string) => {
    try {
      await http.post(`/api/v1/payment-templates/${id}/toggle-favorite`);
      fetchTemplates();
    } catch (error) {
      console.error('Error toggling favorite:', error);
      alert('Error updating favorite status. Please try again.');
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this template?')) {
      try {
        await http.delete(`/api/v1/payment-templates/${id}`);
        fetchTemplates();
      } catch (error) {
        console.error('Error deleting payment template:', error);
        alert('Error deleting payment template. Please try again.');
      }
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingTemplate(null);
    setFormData({
      templateName: '',
      fromAccountId: '',
      toAccountNumber: '',
      toBankCode: '',
      toBankName: '',
      toAccountName: '',
      amount: 0,
      currency: 'AED',
      description: ''
    });
  };

  const filteredTemplates = templates.filter(template => {
    const matchesSearch = !searchTerm || 
      template.templateName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      template.toAccountName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesFavorites = !showFavorites || template.isFavorite;
    return matchesSearch && matchesFavorites;
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Payment Templates</h1>
        <p className="mt-2 text-gray-600">Save and reuse payment configurations</p>
      </div>

      <div className="mb-6 flex flex-col sm:flex-row gap-4">
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
        >
          {showForm ? 'Cancel' : 'New Template'}
        </button>
        
        <div className="flex-1 flex gap-4">
          <input
            type="text"
            placeholder="Search templates..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-1 border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
          />
          
          <button
            onClick={() => setShowFavorites(!showFavorites)}
            className={`px-4 py-2 rounded-md transition-colors ${
              showFavorites 
                ? 'bg-yellow-600 text-white hover:bg-yellow-700' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            {showFavorites ? 'Show All' : 'Favorites Only'}
          </button>
        </div>
      </div>

      {showForm && (
        <div className="bg-white shadow rounded-lg p-6 mb-8">
          <h2 className="text-xl font-semibold mb-4">
            {editingTemplate ? 'Edit Template' : 'Create Payment Template'}
          </h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Template Name</label>
                <input
                  type="text"
                  name="templateName"
                  value={formData.templateName}
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
                <label className="block text-sm font-medium text-gray-700">Amount (Optional)</label>
                <input
                  type="number"
                  name="amount"
                  value={formData.amount}
                  onChange={handleInputChange}
                  min="0"
                  step="0.01"
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
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Description (Optional)</label>
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
                {loading ? 'Saving...' : (editingTemplate ? 'Update Template' : 'Create Template')}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">
            Templates ({filteredTemplates.length})
          </h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Template Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  To Account
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Amount
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Currency
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Created
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredTemplates.map((template) => (
                <tr key={template.id}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div>
                        <div className="text-sm font-medium text-gray-900">{template.templateName}</div>
                        {template.isFavorite && (
                          <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                            ⭐ Favorite
                          </span>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">{template.toAccountName}</div>
                      <div className="text-sm text-gray-500">{template.toAccountNumber}</div>
                      {template.toBankName && (
                        <div className="text-sm text-gray-500">{template.toBankName}</div>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {template.amount ? `${template.currency} ${template.amount.toFixed(2)}` : 'Variable'}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {template.currency}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(template.createdAt).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleEdit(template)}
                        className="text-blue-600 hover:text-blue-900"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleToggleFavorite(template.id)}
                        className="text-yellow-600 hover:text-yellow-900"
                      >
                        {template.isFavorite ? 'Unfavorite' : 'Favorite'}
                      </button>
                      <button
                        onClick={() => handleDelete(template.id)}
                        className="text-red-600 hover:text-red-900"
                      >
                        Delete
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
  );
};

export default PaymentTemplates;

