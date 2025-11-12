import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface Card {
  id: string;
  customerId: string;
  accountId: string;
  cardNumber: string;
  expiryMonth: number;
  expiryYear: number;
  cardType: string;
  status: string;
  dailyLimit: number;
  monthlyLimit: number;
  perTransactionLimit: number;
  createdAt: string;
}

interface CardControl {
  id: string;
  cardId: string;
  controlType: string;
  enabled: boolean;
  value: string;
  createdAt: string;
}

interface CardTransaction {
  id: string;
  cardId: string;
  transactionType: string;
  status: string;
  amount: number;
  currency: string;
  merchantName: string;
  merchantCategory: string;
  transactionDate: string;
  authorizationCode: string;
  referenceNumber: string;
}

interface CardReplacement {
  id: string;
  cardId: string;
  customerId: string;
  reason: string;
  status: string;
  deliveryAddress: string;
  trackingNumber: string;
  requestedAt: string;
  processedAt: string;
  newCardId: string;
}

const CardManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState('cards');
  const [cards, setCards] = useState<Card[]>([]);
  const [cardControls, setCardControls] = useState<CardControl[]>([]);
  const [cardTransactions, setCardTransactions] = useState<CardTransaction[]>([]);
  const [cardReplacements, setCardReplacements] = useState<CardReplacement[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedCard, setSelectedCard] = useState<Card | null>(null);

  // Form states
  const [createCardForm, setCreateCardForm] = useState({
    accountId: '',
    cardType: 'DEBIT',
    dailyLimit: 1000,
    monthlyLimit: 5000,
    perTransactionLimit: 500
  });

  const [controlForm, setControlForm] = useState({
    controlType: 'SPENDING_LIMIT',
    enabled: true,
    value: ''
  });

  const [replacementForm, setReplacementForm] = useState({
    reason: 'LOST',
    deliveryAddress: ''
  });

  const [pinForm, setPinForm] = useState({
    currentPin: '',
    newPin: '',
    confirmPin: ''
  });

  useEffect(() => {
    loadCards();
  }, []);

  const loadCards = async () => {
    try {
      setLoading(true);
      const response = await http.get('/api/v1/cards');
      setCards(response.data);
    } catch (err) {
      setError('Failed to load cards');
    } finally {
      setLoading(false);
    }
  };

  const loadCardControls = async (cardId: string) => {
    try {
      const response = await http.get(`/api/v1/cards/${cardId}/controls`);
      setCardControls(response.data);
    } catch (err) {
      setError('Failed to load card controls');
    }
  };

  const loadCardTransactions = async (cardId: string) => {
    try {
      const response = await http.get(`/api/v1/cards/${cardId}/transactions`);
      setCardTransactions(response.data);
    } catch (err) {
      setError('Failed to load card transactions');
    }
  };

  const loadCardReplacements = async (cardId: string) => {
    try {
      const response = await http.get(`/api/v1/cards/${cardId}/replacements`);
      setCardReplacements(response.data);
    } catch (err) {
      setError('Failed to load card replacements');
    }
  };

  const handleCreateCard = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await http.post('/api/v1/cards', createCardForm);
      await loadCards();
      setCreateCardForm({
        accountId: '',
        cardType: 'DEBIT',
        dailyLimit: 1000,
        monthlyLimit: 5000,
        perTransactionLimit: 500
      });
    } catch (err) {
      setError('Failed to create card');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateCardLimits = async (cardId: string, limits: any) => {
    try {
      await http.put(`/api/v1/cards/${cardId}/limits`, limits);
      await loadCards();
    } catch (err) {
      setError('Failed to update card limits');
    }
  };

  const handleCreateControl = async (cardId: string) => {
    try {
      await http.post(`/api/v1/cards/${cardId}/controls`, controlForm);
      await loadCardControls(cardId);
      setControlForm({
        controlType: 'SPENDING_LIMIT',
        enabled: true,
        value: ''
      });
    } catch (err) {
      setError('Failed to create card control');
    }
  };

  const handleRequestReplacement = async (cardId: string) => {
    try {
      await http.post(`/api/v1/cards/${cardId}/replace`, replacementForm);
      await loadCardReplacements(cardId);
      setReplacementForm({
        reason: 'LOST',
        deliveryAddress: ''
      });
    } catch (err) {
      setError('Failed to request card replacement');
    }
  };

  const handleChangePin = async (cardId: string) => {
    if (pinForm.newPin !== pinForm.confirmPin) {
      setError('PINs do not match');
      return;
    }
    try {
      await http.post(`/api/v1/cards/${cardId}/pin/change`, {
        currentPin: pinForm.currentPin,
        newPin: pinForm.newPin
      });
      setPinForm({
        currentPin: '',
        newPin: '',
        confirmPin: ''
      });
    } catch (err) {
      setError('Failed to change PIN');
    }
  };

  const formatCardNumber = (cardNumber: string) => {
    return cardNumber.replace(/(\d{4})(?=\d)/g, '$1 ');
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'text-green-600 bg-green-100';
      case 'INACTIVE': return 'text-gray-600 bg-gray-100';
      case 'BLOCKED': return 'text-red-600 bg-red-100';
      case 'LOST': return 'text-orange-600 bg-orange-100';
      case 'STOLEN': return 'text-red-600 bg-red-100';
      case 'EXPIRED': return 'text-gray-600 bg-gray-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Card Management</h1>
          <p className="mt-2 text-gray-600">Manage your virtual cards, controls, and transactions</p>
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
              { id: 'cards', name: 'My Cards', icon: '💳' },
              { id: 'controls', name: 'Card Controls', icon: '🔒' },
              { id: 'transactions', name: 'Transactions', icon: '📊' },
              { id: 'replacements', name: 'Replacements', icon: '🔄' },
              { id: 'pin', name: 'PIN Management', icon: '🔑' }
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

        {/* Cards Tab */}
        {activeTab === 'cards' && (
          <div className="space-y-6">
            {/* Create Card Form */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Create New Card</h3>
              <form onSubmit={handleCreateCard} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Account ID</label>
                  <input
                    type="text"
                    value={createCardForm.accountId}
                    onChange={(e) => setCreateCardForm({ ...createCardForm, accountId: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Card Type</label>
                  <select
                    value={createCardForm.cardType}
                    onChange={(e) => setCreateCardForm({ ...createCardForm, cardType: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="DEBIT">Debit</option>
                    <option value="CREDIT">Credit</option>
                    <option value="VIRTUAL">Virtual</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Daily Limit</label>
                  <input
                    type="number"
                    value={createCardForm.dailyLimit}
                    onChange={(e) => setCreateCardForm({ ...createCardForm, dailyLimit: Number(e.target.value) })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Monthly Limit</label>
                  <input
                    type="number"
                    value={createCardForm.monthlyLimit}
                    onChange={(e) => setCreateCardForm({ ...createCardForm, monthlyLimit: Number(e.target.value) })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div className="sm:col-span-2 lg:col-span-4">
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    {loading ? 'Creating...' : 'Create Card'}
                  </button>
                </div>
              </form>
            </div>

            {/* Cards List */}
            <div className="bg-white shadow rounded-lg">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">My Cards</h3>
              </div>
              <div className="divide-y divide-gray-200">
                {cards.map((card) => (
                  <div key={card.id} className="p-6">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-4">
                        <div className="flex-shrink-0">
                          <div className="w-12 h-8 bg-gradient-to-r from-blue-500 to-purple-600 rounded flex items-center justify-center">
                            <span className="text-white text-xs font-bold">💳</span>
                          </div>
                        </div>
                        <div>
                          <h4 className="text-lg font-medium text-gray-900">
                            {formatCardNumber(card.cardNumber)}
                          </h4>
                          <p className="text-sm text-gray-500">
                            {card.cardType} • Expires {card.expiryMonth}/{card.expiryYear}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center space-x-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(card.status)}`}>
                          {card.status}
                        </span>
                        <button
                          onClick={() => setSelectedCard(card)}
                          className="text-blue-600 hover:text-blue-900 text-sm font-medium"
                        >
                          Manage
                        </button>
                      </div>
                    </div>
                    <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-3">
                      <div>
                        <p className="text-sm text-gray-500">Daily Limit</p>
                        <p className="text-lg font-medium text-gray-900">${card.dailyLimit}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500">Monthly Limit</p>
                        <p className="text-lg font-medium text-gray-900">${card.monthlyLimit}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500">Per Transaction</p>
                        <p className="text-lg font-medium text-gray-900">${card.perTransactionLimit}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Card Controls Tab */}
        {activeTab === 'controls' && (
          <div className="space-y-6">
            {selectedCard && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">
                  Controls for {formatCardNumber(selectedCard.cardNumber)}
                </h3>
                
                {/* Create Control Form */}
                <form onSubmit={(e) => { e.preventDefault(); handleCreateControl(selectedCard.id); }} className="mb-6">
                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Control Type</label>
                      <select
                        value={controlForm.controlType}
                        onChange={(e) => setControlForm({ ...controlForm, controlType: e.target.value })}
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      >
                        <option value="SPENDING_LIMIT">Spending Limit</option>
                        <option value="MERCHANT_BLOCK">Merchant Block</option>
                        <option value="ATM_WITHDRAWAL">ATM Withdrawal</option>
                        <option value="ONLINE_PURCHASES">Online Purchases</option>
                        <option value="INTERNATIONAL_TRANSACTIONS">International Transactions</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Value</label>
                      <input
                        type="text"
                        value={controlForm.value}
                        onChange={(e) => setControlForm({ ...controlForm, value: e.target.value })}
                        placeholder="e.g., 1000, merchant name, etc."
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      />
                    </div>
                    <div className="flex items-end">
                      <button
                        type="submit"
                        className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                      >
                        Add Control
                      </button>
                    </div>
                  </div>
                </form>

                {/* Controls List */}
                <div className="space-y-4">
                  {cardControls.map((control) => (
                    <div key={control.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                      <div>
                        <h4 className="text-sm font-medium text-gray-900">{control.controlType}</h4>
                        <p className="text-sm text-gray-500">Value: {control.value}</p>
                      </div>
                      <div className="flex items-center space-x-2">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          control.enabled ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                        }`}>
                          {control.enabled ? 'Enabled' : 'Disabled'}
                        </span>
                        <button className="text-red-600 hover:text-red-900 text-sm font-medium">
                          Remove
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
            
            {!selectedCard && (
              <div className="text-center py-12">
                <p className="text-gray-500">Please select a card to manage controls</p>
              </div>
            )}
          </div>
        )}

        {/* Transactions Tab */}
        {activeTab === 'transactions' && (
          <div className="space-y-6">
            {selectedCard && (
              <div className="bg-white shadow rounded-lg">
                <div className="px-6 py-4 border-b border-gray-200">
                  <h3 className="text-lg font-medium text-gray-900">
                    Transactions for {formatCardNumber(selectedCard.cardNumber)}
                  </h3>
                </div>
                <div className="divide-y divide-gray-200">
                  {cardTransactions.map((transaction) => (
                    <div key={transaction.id} className="p-6">
                      <div className="flex items-center justify-between">
                        <div>
                          <h4 className="text-sm font-medium text-gray-900">{transaction.merchantName}</h4>
                          <p className="text-sm text-gray-500">{transaction.merchantCategory}</p>
                          <p className="text-sm text-gray-500">{transaction.transactionDate}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-lg font-medium text-gray-900">
                            ${transaction.amount} {transaction.currency}
                          </p>
                          <p className="text-sm text-gray-500">{transaction.transactionType}</p>
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            transaction.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                            transaction.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                            'bg-red-100 text-red-800'
                          }`}>
                            {transaction.status}
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
            
            {!selectedCard && (
              <div className="text-center py-12">
                <p className="text-gray-500">Please select a card to view transactions</p>
              </div>
            )}
          </div>
        )}

        {/* Replacements Tab */}
        {activeTab === 'replacements' && (
          <div className="space-y-6">
            {selectedCard && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">
                  Replace {formatCardNumber(selectedCard.cardNumber)}
                </h3>
                
                {/* Replacement Form */}
                <form onSubmit={(e) => { e.preventDefault(); handleRequestReplacement(selectedCard.id); }} className="mb-6">
                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Reason</label>
                      <select
                        value={replacementForm.reason}
                        onChange={(e) => setReplacementForm({ ...replacementForm, reason: e.target.value })}
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      >
                        <option value="LOST">Lost</option>
                        <option value="STOLEN">Stolen</option>
                        <option value="DAMAGED">Damaged</option>
                        <option value="EXPIRED">Expired</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Delivery Address</label>
                      <input
                        type="text"
                        value={replacementForm.deliveryAddress}
                        onChange={(e) => setReplacementForm({ ...replacementForm, deliveryAddress: e.target.value })}
                        placeholder="Enter delivery address"
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                        required
                      />
                    </div>
                    <div className="sm:col-span-2">
                      <button
                        type="submit"
                        className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                      >
                        Request Replacement
                      </button>
                    </div>
                  </div>
                </form>

                {/* Replacement History */}
                <div className="space-y-4">
                  <h4 className="text-md font-medium text-gray-900">Replacement History</h4>
                  {cardReplacements.map((replacement) => (
                    <div key={replacement.id} className="p-4 border border-gray-200 rounded-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <h5 className="text-sm font-medium text-gray-900">{replacement.reason}</h5>
                          <p className="text-sm text-gray-500">Requested: {replacement.requestedAt}</p>
                          <p className="text-sm text-gray-500">Address: {replacement.deliveryAddress}</p>
                        </div>
                        <div className="text-right">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            replacement.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                            replacement.status === 'PROCESSING' ? 'bg-yellow-100 text-yellow-800' :
                            'bg-gray-100 text-gray-800'
                          }`}>
                            {replacement.status}
                          </span>
                          {replacement.trackingNumber && (
                            <p className="text-sm text-gray-500 mt-1">Tracking: {replacement.trackingNumber}</p>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
            
            {!selectedCard && (
              <div className="text-center py-12">
                <p className="text-gray-500">Please select a card to manage replacements</p>
              </div>
            )}
          </div>
        )}

        {/* PIN Management Tab */}
        {activeTab === 'pin' && (
          <div className="space-y-6">
            {selectedCard && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">
                  Change PIN for {formatCardNumber(selectedCard.cardNumber)}
                </h3>
                
                <form onSubmit={(e) => { e.preventDefault(); handleChangePin(selectedCard.id); }} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Current PIN</label>
                    <input
                      type="password"
                      value={pinForm.currentPin}
                      onChange={(e) => setPinForm({ ...pinForm, currentPin: e.target.value })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">New PIN</label>
                    <input
                      type="password"
                      value={pinForm.newPin}
                      onChange={(e) => setPinForm({ ...pinForm, newPin: e.target.value })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Confirm New PIN</label>
                    <input
                      type="password"
                      value={pinForm.confirmPin}
                      onChange={(e) => setPinForm({ ...pinForm, confirmPin: e.target.value })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div>
                    <button
                      type="submit"
                      className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                      Change PIN
                    </button>
                  </div>
                </form>
              </div>
            )}
            
            {!selectedCard && (
              <div className="text-center py-12">
                <p className="text-gray-500">Please select a card to manage PIN</p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default CardManagement;

