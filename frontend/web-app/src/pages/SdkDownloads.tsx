import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface SdkInfo {
  id: string;
  sdkName: string;
  version: string;
  language: string;
  filePath: string;
  fileSize: number;
  downloadCount: number;
  isActive: boolean;
  description: string;
  features: string[];
  requirements: string[];
  installationInstructions: string;
  usageExample: string;
  documentationUrl: string;
  githubUrl: string;
  createdAt: string;
  updatedAt: string;
}

const SdkDownloads: React.FC = () => {
  const [sdks, setSdks] = useState<SdkInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedLanguage, setSelectedLanguage] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');

  const languages = [
    'all',
    'javascript',
    'python',
    'java',
    'csharp',
    'php',
    'go',
    'ruby',
    'swift',
    'kotlin',
    'typescript'
  ];

  useEffect(() => {
    loadSdks();
  }, []);

  const loadSdks = async () => {
    try {
      setLoading(true);
      const response = await http.get('/api/v1/sdk');
      setSdks(response.data);
    } catch (err) {
      setError('Failed to load SDK information');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (sdk: SdkInfo) => {
    try {
      // Track download
      await http.post(`/api/v1/sdk/${sdk.id}/download`);
      
      // Create download link
      const link = document.createElement('a');
      link.href = sdk.filePath;
      link.download = `${sdk.sdkName}-${sdk.version}.${getFileExtension(sdk.language)}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      // Update local state
      setSdks(prev => prev.map(s => 
        s.id === sdk.id ? { ...s, downloadCount: s.downloadCount + 1 } : s
      ));
    } catch (err) {
      console.error('Download failed:', err);
    }
  };

  const getFileExtension = (language: string) => {
    switch (language.toLowerCase()) {
      case 'javascript':
      case 'typescript':
        return 'tgz';
      case 'python':
        return 'whl';
      case 'java':
        return 'jar';
      case 'csharp':
        return 'nupkg';
      case 'php':
        return 'zip';
      case 'go':
        return 'zip';
      case 'ruby':
        return 'gem';
      case 'swift':
        return 'zip';
      case 'kotlin':
        return 'zip';
      default:
        return 'zip';
    }
  };

  const formatFileSize = (bytes: number) => {
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    if (bytes === 0) return '0 Bytes';
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i];
  };

  const getLanguageColor = (language: string) => {
    switch (language.toLowerCase()) {
      case 'javascript':
      case 'typescript':
        return 'text-yellow-600 bg-yellow-100';
      case 'python':
        return 'text-blue-600 bg-blue-100';
      case 'java':
        return 'text-red-600 bg-red-100';
      case 'csharp':
        return 'text-purple-600 bg-purple-100';
      case 'php':
        return 'text-indigo-600 bg-indigo-100';
      case 'go':
        return 'text-cyan-600 bg-cyan-100';
      case 'ruby':
        return 'text-red-600 bg-red-100';
      case 'swift':
        return 'text-orange-600 bg-orange-100';
      case 'kotlin':
        return 'text-purple-600 bg-purple-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getLanguageIcon = (language: string) => {
    switch (language.toLowerCase()) {
      case 'javascript':
      case 'typescript':
        return '🟨';
      case 'python':
        return '🐍';
      case 'java':
        return '☕';
      case 'csharp':
        return '🔷';
      case 'php':
        return '🐘';
      case 'go':
        return '🐹';
      case 'ruby':
        return '💎';
      case 'swift':
        return '🦉';
      case 'kotlin':
        return '🟣';
      default:
        return '📦';
    }
  };

  const filteredSdks = sdks.filter(sdk => {
    const matchesSearch = sdk.sdkName.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         sdk.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesLanguage = selectedLanguage === 'all' || sdk.language === selectedLanguage;
    return matchesSearch && matchesLanguage && sdk.isActive;
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">Loading SDK information...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 text-6xl mb-4">⚠️</div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Error Loading SDKs</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={loadSdks}
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
          <h1 className="text-3xl font-bold text-gray-900">SDK Downloads</h1>
          <p className="mt-2 text-gray-600">Client libraries and SDKs for integrating with our banking APIs</p>
        </div>

        {/* Filters */}
        <div className="bg-white shadow rounded-lg p-6 mb-8">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
            <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4">
              <div className="flex-1">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Search SDKs..."
                />
              </div>
              <div>
                <select
                  value={selectedLanguage}
                  onChange={(e) => setSelectedLanguage(e.target.value)}
                  className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  {languages.map((language) => (
                    <option key={language} value={language}>
                      {language === 'all' ? 'All Languages' : language.charAt(0).toUpperCase() + language.slice(1)}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="text-sm text-gray-500">
              {filteredSdks.length} SDK{filteredSdks.length !== 1 ? 's' : ''} available
            </div>
          </div>
        </div>

        {/* SDK Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredSdks.map((sdk) => (
            <div key={sdk.id} className="bg-white shadow rounded-lg overflow-hidden">
              <div className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center space-x-3">
                    <span className="text-2xl">{getLanguageIcon(sdk.language)}</span>
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900">{sdk.sdkName}</h3>
                      <p className="text-sm text-gray-500">v{sdk.version}</p>
                    </div>
                  </div>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getLanguageColor(sdk.language)}`}>
                    {sdk.language}
                  </span>
                </div>

                <p className="text-gray-600 mb-4">{sdk.description}</p>

                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">File Size:</span>
                    <span className="text-gray-900">{formatFileSize(sdk.fileSize)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Downloads:</span>
                    <span className="text-gray-900">{sdk.downloadCount.toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Updated:</span>
                    <span className="text-gray-900">{new Date(sdk.updatedAt).toLocaleDateString()}</span>
                  </div>
                </div>

                {sdk.features && sdk.features.length > 0 && (
                  <div className="mb-4">
                    <h4 className="text-sm font-medium text-gray-900 mb-2">Features</h4>
                    <div className="flex flex-wrap gap-1">
                      {sdk.features.slice(0, 3).map((feature, index) => (
                        <span
                          key={index}
                          className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                        >
                          {feature}
                        </span>
                      ))}
                      {sdk.features.length > 3 && (
                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                          +{sdk.features.length - 3} more
                        </span>
                      )}
                    </div>
                  </div>
                )}

                <div className="flex space-x-2">
                  <button
                    onClick={() => handleDownload(sdk)}
                    className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    Download
                  </button>
                  {sdk.documentationUrl && (
                    <button
                      onClick={() => window.open(sdk.documentationUrl, '_blank')}
                      className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                      Docs
                    </button>
                  )}
                  {sdk.githubUrl && (
                    <button
                      onClick={() => window.open(sdk.githubUrl, '_blank')}
                      className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                      GitHub
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {filteredSdks.length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-500 text-lg">No SDKs found</div>
            <div className="text-gray-400 text-sm mt-2">
              Try adjusting your search criteria
            </div>
          </div>
        )}

        {/* Quick Start Section */}
        <div className="mt-12 bg-white shadow rounded-lg p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Quick Start Guide</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Getting Started</h3>
              <ol className="list-decimal list-inside space-y-2 text-gray-600">
                <li>Choose your preferred programming language</li>
                <li>Download the corresponding SDK</li>
                <li>Install the SDK using your package manager</li>
                <li>Configure your API credentials</li>
                <li>Start making API calls!</li>
              </ol>
            </div>
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">API Credentials</h3>
              <p className="text-gray-600 mb-4">
                You'll need API credentials to use our SDKs. Get started by:
              </p>
              <ul className="list-disc list-inside space-y-2 text-gray-600">
                <li>Creating a developer account</li>
                <li>Generating API keys in the dashboard</li>
                <li>Configuring webhook endpoints</li>
                <li>Reviewing our API documentation</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Support Section */}
        <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-6">
          <div className="flex items-start">
            <div className="flex-shrink-0">
              <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <span className="text-blue-600 text-sm font-medium">?</span>
              </div>
            </div>
            <div className="ml-4">
              <h3 className="text-lg font-medium text-blue-900">Need Help?</h3>
              <p className="mt-1 text-blue-700">
                Check out our comprehensive documentation, join our developer community, or contact our support team.
              </p>
              <div className="mt-4 flex space-x-4">
                <a
                  href="/api-documentation"
                  className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                >
                  API Documentation →
                </a>
                <a
                  href="/help"
                  className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                >
                  Help Center →
                </a>
                <a
                  href="/feedback"
                  className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                >
                  Contact Support →
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SdkDownloads;

