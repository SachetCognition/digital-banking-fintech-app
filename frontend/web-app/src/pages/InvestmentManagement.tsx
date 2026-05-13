import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface InvestmentAccount {
  id: string;
  accountNumber: string;
  accountType: string;
  accountName: string;
  status: string;
  initialDeposit: number;
  currentBalance: number;
  availableCash: number;
  investedAmount: number;
  unrealizedPnl: number;
  realizedPnl: number;
  totalReturn: number;
  totalReturnPercentage: number;
  riskTolerance: string;
  investmentObjective: string;
  totalValue: number;
  totalPnl: number;
  createdAt: string;
}

interface PortfolioHolding {
  id: string;
  symbol: string;
  quantity: number;
  averageCost: number;
  currentPrice: number;
  marketValue: number;
  costBasis: number;
  unrealizedPnl: number;
  unrealizedPnlPercentage: number;
  weightPercentage: number;
  lastUpdated: string;
}

interface MarketData {
  symbol: string;
  price: number;
  previousClose: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  volume: number;
  marketCap: number;
  peRatio: number;
  dividendYield: number;
  priceChange: number;
  priceChangePercentage: number;
  lastUpdated: string;
}

interface TradingOrder {
  id: string;
  orderNumber: string;
  symbol: string;
  orderType: string;
  side: string;
  quantity: number;
  price: number;
  stopPrice: number;
  limitPrice: number;
  timeInForce: string;
  status: string;
  filledQuantity: number;
  filledPrice: number;
  filledValue: number;
  commission: number;
  fees: number;
  totalCost: number;
  submittedAt: string;
  filledAt: string;
  cancelledAt: string;
  expiresAt: string;
  remainingQuantity: number;
  isFullyFilled: boolean;
  isPartiallyFilled: boolean;
}

const InvestmentManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState('accounts');
  const [accounts, setAccounts] = useState<InvestmentAccount[]>([]);
  const [holdings, setHoldings] = useState<PortfolioHolding[]>([]);
  const [marketData, setMarketData] = useState<MarketData[]>([]);
  const [orders, setOrders] = useState<TradingOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedAccount, setSelectedAccount] = useState<string>('');

  // Form states
  const [accountForm, setAccountForm] = useState({
    accountType: 'INDIVIDUAL',
    accountName: '',
    initialDeposit: 1000,
    riskTolerance: 'MODERATE',
    investmentObjective: ''
  });

  const [orderForm, setOrderForm] = useState({
    symbol: '',
    orderType: 'MARKET',
    side: 'BUY',
    quantity: 0,
    price: 0,
    stopPrice: 0,
    limitPrice: 0,
    timeInForce: 'DAY'
  });

  const [searchSymbol, setSearchSymbol] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        loadAccounts(),
        loadMarketData()
      ]);
    } catch (err) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const loadAccounts = async () => {
    try {
      const response = await http.get('/api/v1/investment-accounts');
      setAccounts(response.data);
      if (response.data.length > 0) {
        setSelectedAccount(response.data[0].id);
        loadHoldings(response.data[0].id);
        loadOrders(response.data[0].id);
      }
    } catch (err) {
      console.error('Failed to load accounts:', err);
    }
  };

  const loadHoldings = async (accountId: string) => {
    try {
      const response = await http.get(`/api/v1/portfolio/accounts/${accountId}/holdings`);
      setHoldings(response.data);
    } catch (err) {
      console.error('Failed to load holdings:', err);
    }
  };

  const loadOrders = async (accountId: string) => {
    try {
      const response = await http.get(`/api/v1/trading/accounts/${accountId}/orders`);
      setOrders(response.data);
    } catch (err) {
      console.error('Failed to load orders:', err);
    }
  };

  const loadMarketData = async () => {
    try {
      const symbols = ['AAPL', 'GOOGL', 'MSFT', 'AMZN', 'TSLA', 'META', 'NVDA', 'SPY', 'QQQ', 'VTI'];
      const response = await http.post('/api/v1/market-data/batch', symbols);
      setMarketData(response.data);
    } catch (err) {
      console.error('Failed to load market data:', err);
    }
  };

  const handleCreateAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await http.post('/api/v1/investment-accounts', accountForm);
      await loadAccounts();
      setAccountForm({
        accountType: 'INDIVIDUAL',
        accountName: '',
        initialDeposit: 1000,
        riskTolerance: 'MODERATE',
        investmentObjective: ''
      });
    } catch (err) {
      setError('Failed to create account');
    } finally {
      setLoading(false);
    }
  };

  const handlePlaceOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAccount) return;
    
    try {
      setLoading(true);
      await http.post(`/api/v1/trading/accounts/${selectedAccount}/orders`, orderForm);
      await loadOrders(selectedAccount);
      setOrderForm({
        symbol: '',
        orderType: 'MARKET',
        side: 'BUY',
        quantity: 0,
        price: 0,
        stopPrice: 0,
        limitPrice: 0,
        timeInForce: 'DAY'
      });
    } catch (err) {
      setError('Failed to place order');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSymbol = async () => {
    if (!searchSymbol) return;
    
    try {
      const response = await http.get(`/api/v1/market-data/${searchSymbol}`);
      setMarketData(prev => {
        const existing = prev.find(md => md.symbol === searchSymbol);
        if (existing) {
          return prev.map(md => md.symbol === searchSymbol ? response.data : md);
        } else {
          return [...prev, response.data];
        }
      });
    } catch (err) {
      setError('Failed to fetch market data');
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'AED'
    }).format(amount);
  };

  const formatPercentage = (percentage: number) => {
    return `${percentage >= 0 ? '+' : ''}${percentage.toFixed(2)}%`;
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
      case 'filled':
      case 'completed':
        return 'text-green-600 bg-green-100';
      case 'pending':
      case 'submitted':
        return 'text-yellow-600 bg-yellow-100';
      case 'cancelled':
      case 'rejected':
      case 'closed':
        return 'text-red-600 bg-red-100';
      case 'partially filled':
        return 'text-blue-600 bg-blue-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getPnlColor = (pnl: number) => {
    return pnl >= 0 ? 'text-green-600' : 'text-red-600';
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Investment Management</h1>
          <p className="mt-2 text-gray-600">Manage your investment accounts, portfolio, and trading</p>
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

        {/* Tab Navigation */}
        <div className="border-b border-gray-200 mb-8">
          <nav className="-mb-px flex space-x-8">
            {[
              { id: 'accounts', name: 'Investment Accounts', icon: '🏦' },
              { id: 'portfolio', name: 'Portfolio', icon: '📊' },
              { id: 'trading', name: 'Trading', icon: '💹' },
              { id: 'market', name: 'Market Data', icon: '📈' }
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

        {/* Accounts Tab */}
        {activeTab === 'accounts' && (
          <div className="space-y-6">
            {/* Create Account Form */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Create Investment Account</h3>
              <form onSubmit={handleCreateAccount} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Account Type</label>
                  <select
                    value={accountForm.accountType}
                    onChange={(e) => setAccountForm({ ...accountForm, accountType: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="INDIVIDUAL">Individual</option>
                    <option value="JOINT">Joint</option>
                    <option value="IRA">IRA</option>
                    <option value="ROTH_IRA">Roth IRA</option>
                    <option value="TRADITIONAL_401K">Traditional 401(k)</option>
                    <option value="ROTH_401K">Roth 401(k)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Account Name</label>
                  <input
                    type="text"
                    value={accountForm.accountName}
                    onChange={(e) => setAccountForm({ ...accountForm, accountName: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="My Investment Account"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Initial Deposit</label>
                  <input
                    type="number"
                    value={accountForm.initialDeposit}
                    onChange={(e) => setAccountForm({ ...accountForm, initialDeposit: Number(e.target.value) })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    min="1000"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Risk Tolerance</label>
                  <select
                    value={accountForm.riskTolerance}
                    onChange={(e) => setAccountForm({ ...accountForm, riskTolerance: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="CONSERVATIVE">Conservative</option>
                    <option value="MODERATE">Moderate</option>
                    <option value="AGGRESSIVE">Aggressive</option>
                    <option value="VERY_AGGRESSIVE">Very Aggressive</option>
                  </select>
                </div>
                <div className="sm:col-span-2">
                  <label className="block text-sm font-medium text-gray-700">Investment Objective</label>
                  <input
                    type="text"
                    value={accountForm.investmentObjective}
                    onChange={(e) => setAccountForm({ ...accountForm, investmentObjective: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Long-term growth, retirement savings, etc."
                  />
                </div>
                <div className="sm:col-span-3">
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    {loading ? 'Creating...' : 'Create Account'}
                  </button>
                </div>
              </form>
            </div>

            {/* Accounts List */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">My Investment Accounts</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {accounts.map((account) => (
                  <div key={account.id} className="p-6">
                    <div className="flex items-center justify-between">
                      <div>
                        <h4 className="text-lg font-medium text-gray-900">
                          {account.accountName}
                        </h4>
                        <p className="text-sm text-gray-500">{account.accountNumber} • {account.accountType}</p>
                        <p className="text-sm text-gray-500">
                          Risk: {account.riskTolerance} • Objective: {account.investmentObjective}
                        </p>
                      </div>
                      <div className="text-right">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(account.status)}`}>
                          {account.status}
                        </span>
                        <p className="text-lg font-medium text-gray-900 mt-1">
                          {formatCurrency(account.totalValue)}
                        </p>
                        <p className="text-sm text-gray-500">
                          Cash: {formatCurrency(account.availableCash)} • 
                          Invested: {formatCurrency(account.investedAmount)}
                        </p>
                        <p className={`text-sm ${getPnlColor(account.totalPnl)}`}>
                          P&L: {formatCurrency(account.totalPnl)} ({formatPercentage(account.totalReturnPercentage)})
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Portfolio Tab */}
        {activeTab === 'portfolio' && (
          <div className="space-y-6">
            {/* Account Selector */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Select Account</h3>
              <select
                value={selectedAccount}
                onChange={(e) => {
                  setSelectedAccount(e.target.value);
                  loadHoldings(e.target.value);
                  loadOrders(e.target.value);
                }}
                className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              >
                {accounts.map((account) => (
                  <option key={account.id} value={account.id}>
                    {account.accountName} - {formatCurrency(account.totalValue)}
                  </option>
                ))}
              </select>
            </div>

            {/* Portfolio Holdings */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Portfolio Holdings</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Symbol</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Quantity</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Avg Cost</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Current Price</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Market Value</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">P&L</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Weight</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {holdings.map((holding) => (
                      <tr key={holding.id}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {holding.symbol}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {holding.quantity.toFixed(4)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatCurrency(holding.averageCost)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatCurrency(holding.currentPrice)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatCurrency(holding.marketValue)}
                        </td>
                        <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${getPnlColor(holding.unrealizedPnl)}`}>
                          {formatCurrency(holding.unrealizedPnl)} ({formatPercentage(holding.unrealizedPnlPercentage)})
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {holding.weightPercentage.toFixed(2)}%
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Trading Tab */}
        {activeTab === 'trading' && (
          <div className="space-y-6">
            {/* Place Order Form */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Place Order</h3>
              <form onSubmit={handlePlaceOrder} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Account</label>
                  <select
                    value={selectedAccount}
                    onChange={(e) => setSelectedAccount(e.target.value)}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    {accounts.map((account) => (
                      <option key={account.id} value={account.id}>
                        {account.accountName}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Symbol</label>
                  <input
                    type="text"
                    value={orderForm.symbol}
                    onChange={(e) => setOrderForm({ ...orderForm, symbol: e.target.value.toUpperCase() })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="AAPL"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Side</label>
                  <select
                    value={orderForm.side}
                    onChange={(e) => setOrderForm({ ...orderForm, side: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="BUY">Buy</option>
                    <option value="SELL">Sell</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Order Type</label>
                  <select
                    value={orderForm.orderType}
                    onChange={(e) => setOrderForm({ ...orderForm, orderType: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="MARKET">Market</option>
                    <option value="LIMIT">Limit</option>
                    <option value="STOP">Stop</option>
                    <option value="STOP_LIMIT">Stop Limit</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Quantity</label>
                  <input
                    type="number"
                    value={orderForm.quantity}
                    onChange={(e) => setOrderForm({ ...orderForm, quantity: Number(e.target.value) })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    min="1"
                    step="0.000001"
                  />
                </div>
                {orderForm.orderType === 'LIMIT' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Limit Price</label>
                    <input
                      type="number"
                      value={orderForm.limitPrice}
                      onChange={(e) => setOrderForm({ ...orderForm, limitPrice: Number(e.target.value) })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      step="0.01"
                    />
                  </div>
                )}
                <div className="sm:col-span-3">
                  <button
                    type="submit"
                    disabled={loading || !selectedAccount}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    {loading ? 'Placing Order...' : 'Place Order'}
                  </button>
                </div>
              </form>
            </div>

            {/* Orders List */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Recent Orders</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Order #</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Symbol</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Side</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Quantity</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Price</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {orders.map((order) => (
                      <tr key={order.id}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {order.orderNumber}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {order.symbol}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {order.side}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {order.orderType}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {order.quantity.toFixed(4)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {order.filledPrice ? formatCurrency(order.filledPrice) : formatCurrency(order.price)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(order.status)}`}>
                            {order.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Market Data Tab */}
        {activeTab === 'market' && (
          <div className="space-y-6">
            {/* Search Symbol */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Search Symbol</h3>
              <div className="flex space-x-4">
                <input
                  type="text"
                  value={searchSymbol}
                  onChange={(e) => setSearchSymbol(e.target.value.toUpperCase())}
                  className="flex-1 border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter symbol (e.g., AAPL)"
                />
                <button
                  onClick={handleSearchSymbol}
                  className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  Search
                </button>
              </div>
            </div>

            {/* Market Data Table */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">Market Data</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Symbol</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Price</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Change</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">% Change</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Volume</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Market Cap</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">P/E</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {marketData.map((data) => (
                      <tr key={data.symbol}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {data.symbol}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatCurrency(data.price)}
                        </td>
                        <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${getPnlColor(data.priceChange)}`}>
                          {formatCurrency(data.priceChange)}
                        </td>
                        <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${getPnlColor(data.priceChangePercentage)}`}>
                          {formatPercentage(data.priceChangePercentage)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {data.volume?.toLocaleString()}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {data.marketCap ? `$${(data.marketCap / 1000000000).toFixed(1)}B` : 'N/A'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {data.peRatio?.toFixed(2) || 'N/A'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default InvestmentManagement;

