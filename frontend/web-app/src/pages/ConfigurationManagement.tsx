import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface SystemConfiguration {
  id: string;
  configKey: string;
  configValue: string;
  configType: string;
  description: string;
  category: string;
  isEncrypted: boolean;
  isSensitive: boolean;
  isReadonly: boolean;
  validationRules: string;
  createdBy: string;
  updatedBy: string;
  createdAt: string;
  updatedAt: string;
}

interface CreateConfigRequest {
  configKey: string;
  configValue: string;
  configType: string;
  description: string;
  category: string;
  isEncrypted: boolean;
  isSensitive: boolean;
  isReadonly: boolean;
  validationRules: string;
}

const ConfigurationManagement: React.FC = () => {
  const [configurations, setConfigurations] = useState<SystemConfiguration[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingConfig, setEditingConfig] = useState<SystemConfiguration | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [showSensitive, setShowSensitive] = useState(false);

  const [newConfig, setNewConfig] = useState<CreateConfigRequest>({
    configKey: '',
    configValue: '',
    configType: 'STRING',
    description: '',
    category: '',
    isEncrypted: false,
    isSensitive: false,
    isReadonly: false,
    validationRules: ''
  });

  const configTypes = [
    { value: 'STRING', label: 'String' },
    { value: 'NUMBER', label: 'Number' },
    { value: 'BOOLEAN', label: 'Boolean' },
    { value: 'JSON', label: 'JSON' },
    { value: 'URL', label: 'URL' },
    { value: 'EMAIL', label: 'Email' },
    { value: 'PASSWORD', label: 'Password' },
    { value: 'ENCRYPTED', label: 'Encrypted' }
  ];

  const categories = [
    'all',
    'database',
    'security',
    'email',
    'kafka',
    'redis',
    'monitoring',
    'backup',
    'api',
    'ui',
    'general'
  ];

  useEffect(() => {
    loadConfigurations();
  }, []);

  const loadConfigurations = async () => {
    try {
      setLoading(true);
      const response = await http.get('/api/v1/admin/configuration');
      setConfigurations(response.data);
    } catch (err) {
      setError('Failed to load configurations');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateConfig = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newConfig.configKey || !newConfig.configValue || !newConfig.category) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setLoading(true);
      await http.post('/api/v1/admin/configuration', newConfig);
      setSuccess('Configuration created successfully!');
      setShowCreateModal(false);
      setNewConfig({
        configKey: '',
        configValue: '',
        configType: 'STRING',
        description: '',
        category: '',
        isEncrypted: false,
        isSensitive: false,
        isReadonly: false,
        validationRules: ''
      });
      await loadConfigurations();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateConfig = async (config: SystemConfiguration, newValue: string) => {
    if (config.isReadonly) {
      setError('Cannot update readonly configuration');
      return;
    }

    try {
      setLoading(true);
      await http.put(`/api/v1/admin/configuration/${config.id}`, {
        configValue: newValue
      });
      setSuccess('Configuration updated successfully!');
      await loadConfigurations();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteConfig = async (configId: string) => {
    if (!window.confirm('Are you sure you want to delete this configuration?')) {
      return;
    }

    try {
      setLoading(true);
      await http.delete(`/api/v1/admin/configuration/${configId}`);
      setSuccess('Configuration deleted successfully!');
      await loadConfigurations();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete configuration');
    } finally {
      setLoading(false);
    }
  };

  const handleRefreshConfigurations = async () => {
    try {
      await http.post('/api/v1/admin/configuration/refresh');
      setSuccess('Configurations refreshed successfully!');
      await loadConfigurations();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to refresh configurations');
    }
  };

  const filteredConfigurations = configurations.filter(config => {
    const matchesSearch = config.configKey.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         config.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = categoryFilter === 'all' || config.category === categoryFilter;
    const matchesSensitive = showSensitive || !config.isSensitive;
    return matchesSearch && matchesCategory && matchesSensitive;
  });

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const getConfigTypeColor = (type: string) => {
    switch (type) {
      case 'STRING':
        return 'text-blue-600 bg-blue-100';
      case 'NUMBER':
        return 'text-green-600 bg-green-100';
      case 'BOOLEAN':
        return 'text-purple-600 bg-purple-100';
      case 'JSON':
        return 'text-orange-600 bg-orange-100';
      case 'PASSWORD':
      case 'ENCRYPTED':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const maskSensitiveValue = (value: string, isSensitive: boolean) => {
    if (isSensitive && !showSensitive) {
      return '••••••••';
    }
    return value;
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Configuration Management</h1>
          <p className="mt-2 text-gray-600">Manage system configurations and settings</p>
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

        {success && (
          <div className="mb-6 bg-green-50 border border-green-200 rounded-md p-4">
            <div className="flex">
              <div className="ml-3">
                <h3 className="text-sm font-medium text-green-800">Success</h3>
                <div className="mt-2 text-sm text-green-700">{success}</div>
              </div>
            </div>
          </div>
        )}

        {/* Filters and Actions */}
        <div className="bg-white shadow rounded-lg p-6 mb-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
            <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4">
              <div className="flex-1">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Search configurations..."
                />
              </div>
              <div>
                <select
                  value={categoryFilter}
                  onChange={(e) => setCategoryFilter(e.target.value)}
                  className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  {categories.map((category) => (
                    <option key={category} value={category}>
                      {category === 'all' ? 'All Categories' : category.charAt(0).toUpperCase() + category.slice(1)}
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="showSensitive"
                  checked={showSensitive}
                  onChange={(e) => setShowSensitive(e.target.checked)}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor="showSensitive" className="ml-2 block text-sm text-gray-900">
                  Show sensitive values
                </label>
              </div>
            </div>
            <div className="flex space-x-2">
              <button
                onClick={handleRefreshConfigurations}
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
              >
                Refresh
              </button>
              <button
                onClick={() => setShowCreateModal(true)}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                Create Config
              </button>
            </div>
          </div>
        </div>

        {/* Configurations Table */}
        <div className="bg-white shadow rounded-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Key</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Value</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Category</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Properties</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Updated</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {loading ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-8 text-center">
                      <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                      <p className="mt-2 text-gray-600">Loading configurations...</p>
                    </td>
                  </tr>
                ) : filteredConfigurations.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-8 text-center text-gray-500">
                      No configurations found
                    </td>
                  </tr>
                ) : (
                  filteredConfigurations.map((config) => (
                    <tr key={config.id}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-medium text-gray-900">{config.configKey}</div>
                          {config.description && (
                            <div className="text-sm text-gray-500">{config.description}</div>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-gray-900 font-mono max-w-xs truncate">
                          {maskSensitiveValue(config.configValue, config.isSensitive)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getConfigTypeColor(config.configType)}`}>
                          {config.configType}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {config.category}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex flex-wrap gap-1">
                          {config.isEncrypted && (
                            <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                              Encrypted
                            </span>
                          )}
                          {config.isSensitive && (
                            <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                              Sensitive
                            </span>
                          )}
                          {config.isReadonly && (
                            <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                              Readonly
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {formatDate(config.updatedAt)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                        <button
                          onClick={() => setEditingConfig(config)}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          Edit
                        </button>
                        {!config.isReadonly && (
                          <button
                            onClick={() => handleDeleteConfig(config.id)}
                            className="text-red-600 hover:text-red-900"
                          >
                            Delete
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Create Configuration Modal */}
        {showCreateModal && (
          <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
            <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
              <div className="mt-3">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Create Configuration</h3>
                <form onSubmit={handleCreateConfig} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Config Key *</label>
                    <input
                      type="text"
                      value={newConfig.configKey}
                      onChange={(e) => setNewConfig({ ...newConfig, configKey: e.target.value })}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Config Value *</label>
                    <textarea
                      value={newConfig.configValue}
                      onChange={(e) => setNewConfig({ ...newConfig, configValue: e.target.value })}
                      rows={3}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Type *</label>
                      <select
                        value={newConfig.configType}
                        onChange={(e) => setNewConfig({ ...newConfig, configType: e.target.value })}
                        className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      >
                        {configTypes.map((type) => (
                          <option key={type.value} value={type.value}>
                            {type.label}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Category *</label>
                      <input
                        type="text"
                        value={newConfig.category}
                        onChange={(e) => setNewConfig({ ...newConfig, category: e.target.value })}
                        className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                        required
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                    <textarea
                      value={newConfig.description}
                      onChange={(e) => setNewConfig({ ...newConfig, description: e.target.value })}
                      rows={2}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                  <div className="grid grid-cols-3 gap-4">
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={newConfig.isEncrypted}
                        onChange={(e) => setNewConfig({ ...newConfig, isEncrypted: e.target.checked })}
                        className="mr-2"
                      />
                      <span className="text-sm text-gray-700">Encrypted</span>
                    </label>
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={newConfig.isSensitive}
                        onChange={(e) => setNewConfig({ ...newConfig, isSensitive: e.target.checked })}
                        className="mr-2"
                      />
                      <span className="text-sm text-gray-700">Sensitive</span>
                    </label>
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={newConfig.isReadonly}
                        onChange={(e) => setNewConfig({ ...newConfig, isReadonly: e.target.checked })}
                        className="mr-2"
                      />
                      <span className="text-sm text-gray-700">Readonly</span>
                    </label>
                  </div>
                  <div className="flex justify-end space-x-3 pt-4">
                    <button
                      type="button"
                      onClick={() => setShowCreateModal(false)}
                      className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={loading}
                      className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
                    >
                      {loading ? 'Creating...' : 'Create Config'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ConfigurationManagement;

