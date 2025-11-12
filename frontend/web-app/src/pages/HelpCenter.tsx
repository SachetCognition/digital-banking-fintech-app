import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface HelpArticle {
  id: string;
  title: string;
  slug: string;
  content: string;
  category: string;
  subcategory: string;
  tags: string[];
  viewCount: number;
  helpfulCount: number;
  notHelpfulCount: number;
  createdAt: string;
  updatedAt: string;
}

interface HelpCategory {
  id: string;
  name: string;
  slug: string;
  description: string;
  parentId: string;
  sortOrder: number;
  isActive: boolean;
  articles: HelpArticle[];
}

const HelpCenter: React.FC = () => {
  const [categories, setCategories] = useState<HelpCategory[]>([]);
  const [articles, setArticles] = useState<HelpArticle[]>([]);
  const [selectedArticle, setSelectedArticle] = useState<HelpArticle | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        loadCategories(),
        loadArticles()
      ]);
    } catch (err) {
      setError('Failed to load help center data');
    } finally {
      setLoading(false);
    }
  };

  const loadCategories = async () => {
    try {
      const response = await http.get('/api/v1/help/categories');
      setCategories(response.data);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const loadArticles = async () => {
    try {
      const response = await http.get('/api/v1/help/articles');
      setArticles(response.data);
    } catch (err) {
      console.error('Failed to load articles:', err);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadArticles();
      return;
    }

    try {
      setLoading(true);
      const response = await http.get(`/api/v1/help/articles/search?q=${encodeURIComponent(searchQuery)}`);
      setArticles(response.data);
    } catch (err) {
      setError('Search failed');
    } finally {
      setLoading(false);
    }
  };

  const handleCategoryFilter = (categorySlug: string) => {
    setSelectedCategory(categorySlug);
    if (categorySlug === 'all') {
      loadArticles();
    } else {
      const filteredArticles = articles.filter(article => article.category === categorySlug);
      setArticles(filteredArticles);
    }
  };

  const handleArticleClick = async (article: HelpArticle) => {
    setSelectedArticle(article);
    // Track article view
    try {
      await http.post(`/api/v1/help/articles/${article.id}/view`);
    } catch (err) {
      console.error('Failed to track article view:', err);
    }
  };

  const handleHelpful = async (articleId: string, isHelpful: boolean) => {
    try {
      await http.post(`/api/v1/help/articles/${articleId}/feedback`, {
        isHelpful
      });
      // Update local state
      setArticles(prev => prev.map(article => 
        article.id === articleId 
          ? { 
              ...article, 
              helpfulCount: isHelpful ? article.helpfulCount + 1 : article.helpfulCount,
              notHelpfulCount: !isHelpful ? article.notHelpfulCount + 1 : article.notHelpfulCount
            }
          : article
      ));
    } catch (err) {
      console.error('Failed to submit feedback:', err);
    }
  };

  const filteredArticles = selectedCategory === 'all' 
    ? articles 
    : articles.filter(article => article.category === selectedCategory);

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Help Center</h1>
          <p className="mt-2 text-gray-600">Find answers to your questions and get support</p>
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

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Sidebar */}
          <div className="lg:col-span-1">
            {/* Search */}
            <div className="bg-white shadow rounded-lg p-6 mb-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Search</h3>
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="flex-1 border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Search articles..."
                />
                <button
                  onClick={handleSearch}
                  className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  Search
                </button>
              </div>
            </div>

            {/* Categories */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Categories</h3>
              <nav className="space-y-2">
                <button
                  onClick={() => handleCategoryFilter('all')}
                  className={`block w-full text-left px-3 py-2 rounded-md text-sm font-medium ${
                    selectedCategory === 'all'
                      ? 'bg-blue-100 text-blue-700'
                      : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                  }`}
                >
                  All Articles
                </button>
                {categories.map((category) => (
                  <button
                    key={category.id}
                    onClick={() => handleCategoryFilter(category.slug)}
                    className={`block w-full text-left px-3 py-2 rounded-md text-sm font-medium ${
                      selectedCategory === category.slug
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    }`}
                  >
                    {category.name}
                  </button>
                ))}
              </nav>
            </div>
          </div>

          {/* Main Content */}
          <div className="lg:col-span-3">
            {selectedArticle ? (
              /* Article View */
              <div className="bg-white shadow rounded-lg">
                <div className="px-6 py-4 border-b border-gray-200">
                  <button
                    onClick={() => setSelectedArticle(null)}
                    className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                  >
                    ← Back to Articles
                  </button>
                </div>
                <div className="px-6 py-6">
                  <h1 className="text-2xl font-bold text-gray-900 mb-4">
                    {selectedArticle.title}
                  </h1>
                  <div className="prose max-w-none">
                    <div dangerouslySetInnerHTML={{ __html: selectedArticle.content }} />
                  </div>
                  <div className="mt-8 pt-6 border-t border-gray-200">
                    <p className="text-sm text-gray-600 mb-4">
                      Was this article helpful?
                    </p>
                    <div className="flex space-x-4">
                      <button
                        onClick={() => handleHelpful(selectedArticle.id, true)}
                        className="flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                      >
                        <span className="mr-2">👍</span>
                        Yes ({selectedArticle.helpfulCount})
                      </button>
                      <button
                        onClick={() => handleHelpful(selectedArticle.id, false)}
                        className="flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                      >
                        <span className="mr-2">👎</span>
                        No ({selectedArticle.notHelpfulCount})
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              /* Articles List */
              <div className="space-y-6">
                <div className="bg-white shadow rounded-lg">
                  <div className="px-6 py-4 border-b border-gray-200">
                    <h2 className="text-lg font-medium text-gray-900">
                      {selectedCategory === 'all' ? 'All Articles' : categories.find(c => c.slug === selectedCategory)?.name}
                    </h2>
                  </div>
                  <div className="divide-y divide-gray-200">
                    {loading ? (
                      <div className="px-6 py-8 text-center">
                        <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                        <p className="mt-2 text-gray-600">Loading articles...</p>
                      </div>
                    ) : filteredArticles.length === 0 ? (
                      <div className="px-6 py-8 text-center">
                        <p className="text-gray-600">No articles found.</p>
                      </div>
                    ) : (
                      filteredArticles.map((article) => (
                        <div
                          key={article.id}
                          className="px-6 py-4 hover:bg-gray-50 cursor-pointer"
                          onClick={() => handleArticleClick(article)}
                        >
                          <h3 className="text-lg font-medium text-gray-900 mb-2">
                            {article.title}
                          </h3>
                          <p className="text-sm text-gray-600 mb-2">
                            {article.category} • {article.viewCount} views
                          </p>
                          <div className="flex items-center space-x-4 text-sm text-gray-500">
                            <span>👍 {article.helpfulCount}</span>
                            <span>👎 {article.notHelpfulCount}</span>
                            <span>Updated {new Date(article.updatedAt).toLocaleDateString()}</span>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default HelpCenter;

