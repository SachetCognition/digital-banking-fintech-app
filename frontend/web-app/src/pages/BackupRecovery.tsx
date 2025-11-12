import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface BackupRecord {
  id: string;
  backupName: string;
  backupType: string;
  serviceName: string;
  databaseName: string;
  filePath: string;
  fileSize: number;
  status: string;
  startedAt: string;
  completedAt: string;
  expiresAt: string;
  retentionDays: number;
  checksum: string;
  compressionType: string;
  encryptionEnabled: boolean;
  createdBy: string;
  errorMessage: string;
  metadata: string;
  createdAt: string;
}

interface RecoveryOperation {
  id: string;
  operationName: string;
  backupId: string;
  targetService: string;
  targetDatabase: string;
  status: string;
  startedAt: string;
  completedAt: string;
  createdBy: string;
  errorMessage: string;
  recoveryNotes: string;
  metadata: string;
  createdAt: string;
}

interface CreateBackupRequest {
  serviceName: string;
  databaseName: string;
  backupType: string;
  retentionDays: number;
  compressionType: string;
  encryptionEnabled: boolean;
}

const BackupRecovery: React.FC = () => {
  const [backups, setBackups] = useState<BackupRecord[]>([]);
  const [recoveries, setRecoveries] = useState<RecoveryOperation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showCreateBackupModal, setShowCreateBackupModal] = useState(false);
  const [showRecoveryModal, setShowRecoveryModal] = useState(false);
  const [selectedBackup, setSelectedBackup] = useState<BackupRecord | null>(null);
  const [activeTab, setActiveTab] = useState('backups');

  const [newBackup, setNewBackup] = useState<CreateBackupRequest>({
    serviceName: '',
    databaseName: '',
    backupType: 'FULL',
    retentionDays: 30,
    compressionType: 'GZIP',
    encryptionEnabled: false
  });

  const [newRecovery, setNewRecovery] = useState({
    operationName: '',
    targetService: '',
    targetDatabase: '',
    recoveryNotes: ''
  });

  const services = [
    'customer-service',
    'account-service',
    'ledger-service',
    'payments-service',
    'card-service',
    'loan-service',
    'investment-service',
    'compliance-service',
    'notifications-service',
    'admin-service'
  ];

  const backupTypes = [
    { value: 'FULL', label: 'Full Backup' },
    { value: 'INCREMENTAL', label: 'Incremental Backup' },
    { value: 'DIFFERENTIAL', label: 'Differential Backup' }
  ];

  const compressionTypes = [
    { value: 'NONE', label: 'No Compression' },
    { value: 'GZIP', label: 'GZIP' },
    { value: 'BZIP2', label: 'BZIP2' },
    { value: 'LZ4', label: 'LZ4' }
  ];

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        loadBackups(),
        loadRecoveries()
      ]);
    } catch (err) {
      setError('Failed to load backup and recovery data');
    } finally {
      setLoading(false);
    }
  };

  const loadBackups = async () => {
    try {
      const response = await http.get('/api/v1/admin/backup');
      setBackups(response.data);
    } catch (err) {
      console.error('Failed to load backups:', err);
    }
  };

  const loadRecoveries = async () => {
    try {
      const response = await http.get('/api/v1/admin/recovery');
      setRecoveries(response.data);
    } catch (err) {
      console.error('Failed to load recoveries:', err);
    }
  };

  const handleCreateBackup = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newBackup.serviceName || !newBackup.databaseName) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setLoading(true);
      await http.post('/api/v1/admin/backup', newBackup);
      setSuccess('Backup initiated successfully!');
      setShowCreateBackupModal(false);
      setNewBackup({
        serviceName: '',
        databaseName: '',
        backupType: 'FULL',
        retentionDays: 30,
        compressionType: 'GZIP',
        encryptionEnabled: false
      });
      await loadBackups();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create backup');
    } finally {
      setLoading(false);
    }
  };

  const handleStartRecovery = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newRecovery.operationName || !newRecovery.targetService || !selectedBackup) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setLoading(true);
      await http.post('/api/v1/admin/recovery', {
        ...newRecovery,
        backupId: selectedBackup.id
      });
      setSuccess('Recovery operation initiated successfully!');
      setShowRecoveryModal(false);
      setSelectedBackup(null);
      setNewRecovery({
        operationName: '',
        targetService: '',
        targetDatabase: '',
        recoveryNotes: ''
      });
      await loadRecoveries();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to start recovery');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteBackup = async (backupId: string) => {
    if (!window.confirm('Are you sure you want to delete this backup? This action cannot be undone.')) {
      return;
    }

    try {
      setLoading(true);
      await http.delete(`/api/v1/admin/backup/${backupId}`);
      setSuccess('Backup deleted successfully!');
      await loadBackups();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete backup');
    } finally {
      setLoading(false);
    }
  };

  const formatFileSize = (bytes: number) => {
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    if (bytes === 0) return '0 Bytes';
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i];
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'completed':
        return 'text-green-600 bg-green-100';
      case 'in_progress':
        return 'text-blue-600 bg-blue-100';
      case 'failed':
        return 'text-red-600 bg-red-100';
      case 'expired':
        return 'text-gray-600 bg-gray-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getBackupTypeColor = (type: string) => {
    switch (type.toLowerCase()) {
      case 'full':
        return 'text-blue-600 bg-blue-100';
      case 'incremental':
        return 'text-green-600 bg-green-100';
      case 'differential':
        return 'text-yellow-600 bg-yellow-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Backup & Recovery</h1>
          <p className="mt-2 text-gray-600">Manage system backups and recovery operations</p>
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

        {/* Tab Navigation */}
        <div className="border-b border-gray-200 mb-8">
          <nav className="-mb-px flex space-x-8">
            {[
              { id: 'backups', name: 'Backups', icon: '💾' },
              { id: 'recoveries', name: 'Recoveries', icon: '🔄' }
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

        {/* Backups Tab */}
        {activeTab === 'backups' && (
          <div className="space-y-6">
            {/* Actions */}
            <div className="bg-white shadow rounded-lg p-6">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-medium text-gray-900">Backup Management</h3>
                <button
                  onClick={() => setShowCreateBackupModal(true)}
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  Create Backup
                </button>
              </div>
            </div>

            {/* Backups Table */}
            <div className="bg-white shadow rounded-lg overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Backup</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Service</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Size</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Created</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Expires</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {loading ? (
                      <tr>
                        <td colSpan={8} className="px-6 py-8 text-center">
                          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                          <p className="mt-2 text-gray-600">Loading backups...</p>
                        </td>
                      </tr>
                    ) : backups.length === 0 ? (
                      <tr>
                        <td colSpan={8} className="px-6 py-8 text-center text-gray-500">
                          No backups found
                        </td>
                      </tr>
                    ) : (
                      backups.map((backup) => (
                        <tr key={backup.id}>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div>
                              <div className="text-sm font-medium text-gray-900">{backup.backupName}</div>
                              <div className="text-sm text-gray-500">{backup.databaseName}</div>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {backup.serviceName}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getBackupTypeColor(backup.backupType)}`}>
                              {backup.backupType}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {formatFileSize(backup.fileSize)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(backup.status)}`}>
                              {backup.status}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {formatDate(backup.startedAt)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {formatDate(backup.expiresAt)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                            {backup.status === 'COMPLETED' && (
                              <button
                                onClick={() => {
                                  setSelectedBackup(backup);
                                  setShowRecoveryModal(true);
                                }}
                                className="text-blue-600 hover:text-blue-900"
                              >
                                Recover
                              </button>
                            )}
                            <button
                              onClick={() => handleDeleteBackup(backup.id)}
                              className="text-red-600 hover:text-red-900"
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Recoveries Tab */}
        {activeTab === 'recoveries' && (
          <div className="space-y-6">
            {/* Recovery Operations Table */}
            <div className="bg-white shadow rounded-lg overflow-hidden">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Recovery Operations</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Operation</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Target</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Started</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Completed</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Notes</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {loading ? (
                      <tr>
                        <td colSpan={6} className="px-6 py-8 text-center">
                          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                          <p className="mt-2 text-gray-600">Loading recoveries...</p>
                        </td>
                      </tr>
                    ) : recoveries.length === 0 ? (
                      <tr>
                        <td colSpan={6} className="px-6 py-8 text-center text-gray-500">
                          No recovery operations found
                        </td>
                      </tr>
                    ) : (
                      recoveries.map((recovery) => (
                        <tr key={recovery.id}>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm font-medium text-gray-900">{recovery.operationName}</div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-sm text-gray-900">{recovery.targetService}</div>
                            <div className="text-sm text-gray-500">{recovery.targetDatabase}</div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(recovery.status)}`}>
                              {recovery.status}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {formatDate(recovery.startedAt)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {recovery.completedAt ? formatDate(recovery.completedAt) : '-'}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {recovery.recoveryNotes || '-'}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Create Backup Modal */}
        {showCreateBackupModal && (
          <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
            <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
              <div className="mt-3">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Create Backup</h3>
                <form onSubmit={handleCreateBackup} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Service *</label>
                    <select
                      value={newBackup.serviceName}
                      onChange={(e) => setNewBackup({ ...newBackup, serviceName: e.target.value })}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    >
                      <option value="">Select Service</option>
                      {services.map((service) => (
                        <option key={service} value={service}>
                          {service}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Database *</label>
                    <input
                      type="text"
                      value={newBackup.databaseName}
                      onChange={(e) => setNewBackup({ ...newBackup, databaseName: e.target.value })}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Type *</label>
                      <select
                        value={newBackup.backupType}
                        onChange={(e) => setNewBackup({ ...newBackup, backupType: e.target.value })}
                        className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      >
                        {backupTypes.map((type) => (
                          <option key={type.value} value={type.value}>
                            {type.label}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Retention (days)</label>
                      <input
                        type="number"
                        value={newBackup.retentionDays}
                        onChange={(e) => setNewBackup({ ...newBackup, retentionDays: parseInt(e.target.value) })}
                        className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                        min="1"
                        max="365"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Compression</label>
                    <select
                      value={newBackup.compressionType}
                      onChange={(e) => setNewBackup({ ...newBackup, compressionType: e.target.value })}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    >
                      {compressionTypes.map((type) => (
                        <option key={type.value} value={type.value}>
                          {type.label}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={newBackup.encryptionEnabled}
                        onChange={(e) => setNewBackup({ ...newBackup, encryptionEnabled: e.target.checked })}
                        className="mr-2"
                      />
                      <span className="text-sm text-gray-700">Enable Encryption</span>
                    </label>
                  </div>
                  <div className="flex justify-end space-x-3 pt-4">
                    <button
                      type="button"
                      onClick={() => setShowCreateBackupModal(false)}
                      className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={loading}
                      className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
                    >
                      {loading ? 'Creating...' : 'Create Backup'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}

        {/* Recovery Modal */}
        {showRecoveryModal && selectedBackup && (
          <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
            <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
              <div className="mt-3">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Start Recovery</h3>
                <form onSubmit={handleStartRecovery} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Operation Name *</label>
                    <input
                      type="text"
                      value={newRecovery.operationName}
                      onChange={(e) => setNewRecovery({ ...newRecovery, operationName: e.target.value })}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Target Service *</label>
                    <select
                      value={newRecovery.targetService}
                      onChange={(e) => setNewRecovery({ ...newRecovery, targetService: e.target.value })}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    >
                      <option value="">Select Target Service</option>
                      {services.map((service) => (
                        <option key={service} value={service}>
                          {service}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Target Database *</label>
                    <input
                      type="text"
                      value={newRecovery.targetDatabase}
                      onChange={(e) => setNewRecovery({ ...newRecovery, targetDatabase: e.target.value })}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Recovery Notes</label>
                    <textarea
                      value={newRecovery.recoveryNotes}
                      onChange={(e) => setNewRecovery({ ...newRecovery, recoveryNotes: e.target.value })}
                      rows={3}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                  <div className="bg-yellow-50 border border-yellow-200 rounded-md p-3">
                    <div className="text-sm text-yellow-800">
                      <strong>Warning:</strong> This will restore data from backup: {selectedBackup.backupName}
                    </div>
                  </div>
                  <div className="flex justify-end space-x-3 pt-4">
                    <button
                      type="button"
                      onClick={() => {
                        setShowRecoveryModal(false);
                        setSelectedBackup(null);
                      }}
                      className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={loading}
                      className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50"
                    >
                      {loading ? 'Starting...' : 'Start Recovery'}
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

export default BackupRecovery;

