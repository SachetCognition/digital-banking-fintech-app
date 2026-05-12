import React from 'react'
import { Link } from 'react-router-dom'

const accounts = [
  { name: 'Current Account', number: '****7842', balance: 91250.00, type: 'current', trend: '+4.2%' },
  { name: 'Savings Account (Wakala)', number: '****3156', balance: 574800.50, type: 'savings', trend: '+6.8%' },
  { name: 'Investment Portfolio', number: '****9024', balance: 328150.00, type: 'investment', trend: '+15.7%' },
];

const recentTransactions = [
  { id: 1, desc: 'Carrefour City Centre Deira', amount: -467.50, date: 'Today, 2:30 PM', category: 'Shopping', icon: '🛒' },
  { id: 2, desc: 'Salary - ADNOC Group', amount: 31200.00, date: 'Today, 9:00 AM', category: 'Income', icon: '💰' },
  { id: 3, desc: 'DEWA Bill Payment', amount: -1285.40, date: 'Yesterday', category: 'Utilities', icon: '⚡' },
  { id: 4, desc: 'Transfer to Wakala Savings', amount: -5000.00, date: 'Yesterday', category: 'Transfer', icon: '💸' },
  { id: 5, desc: 'Tim Hortons - Dubai Mall', amount: -42.00, date: 'May 10', category: 'Food', icon: '☕' },
  { id: 6, desc: 'Etisalat Mobile Recharge', amount: -200.00, date: 'May 10', category: 'Telecom', icon: '📱' },
  { id: 7, desc: 'Emirates Airlines Booking', amount: -3750.00, date: 'May 9', category: 'Travel', icon: '✈️' },
];

const quickActions = [
  { label: 'Send Money', path: '/transfer', icon: '💸', color: 'from-[#0d2847] to-[#1a4a7a]' },
  { label: 'Pay Bills', path: '/billpay', icon: '📄', color: 'from-[#d4af37] to-[#b8960c]' },
  { label: 'Cards', path: '/card-management', icon: '💳', color: 'from-[#1a3a5c] to-[#2a5a8c]' },
  { label: 'Investments', path: '/investment-management', icon: '📈', color: 'from-[#2d6a4f] to-[#40916c]' },
];

const formatAED = (amount: number) => {
  const abs = Math.abs(amount);
  return `AED ${abs.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
};

export default function Home() {
  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
  })();

  const totalBalance = accounts.reduce((sum, a) => sum + a.balance, 0);

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-[#0a1628] via-[#0d2847] to-[#1a3a5c] rounded-2xl p-6 lg:p-8 text-white relative overflow-hidden">
        <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZGVmcz48cGF0dGVybiBpZD0iYSIgcGF0dGVyblVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBwYXR0ZXJuVHJhbnNmb3JtPSJyb3RhdGUoNDUpIj48cGF0aCBkPSJNLTEwIDMwaDYwIiBzdHJva2U9InJnYmEoMjEyLDE3NSw1NSwwLjA1KSIgc3Ryb2tlLXdpZHRoPSIxIi8+PC9wYXR0ZXJuPjwvZGVmcz48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSJ1cmwoI2EpIi8+PC9zdmc+')] opacity-50"></div>
        <div className="relative z-10">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-slate-400 text-sm">Marhaba,</p>
              <h2 className="text-2xl font-bold mt-1">{user.fullName || 'Ahmed Al-Rashid'}</h2>
            </div>
            <div className="text-right hidden sm:block">
              <p className="text-slate-400 text-xs">Account Status</p>
              <span className="inline-flex items-center gap-1.5 text-emerald-400 text-sm font-medium mt-1">
                <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                Active - Premium
              </span>
            </div>
          </div>
          <div className="mt-5">
            <span className="text-slate-400 text-sm">Total Balance</span>
            <div className="flex items-baseline gap-3 mt-1">
              <span className="text-4xl font-bold tracking-tight">AED {totalBalance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
              <span className="text-[#d4af37] text-sm font-medium bg-[#d4af37]/10 px-2.5 py-0.5 rounded-full">+5.4% this month</span>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {quickActions.map((action) => (
          <Link
            key={action.path}
            to={action.path}
            className="group bg-white rounded-xl p-4 border border-slate-200 hover:border-slate-300 hover:shadow-md transition-all duration-200"
          >
            <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${action.color} flex items-center justify-center text-lg mb-3`}>
              {action.icon}
            </div>
            <p className="text-sm font-medium text-slate-700 group-hover:text-slate-900">{action.label}</p>
          </Link>
        ))}
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        {/* Accounts */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-slate-800">Your Accounts</h3>
            <button className="text-sm text-[#0d2847] hover:text-[#d4af37] font-medium">View All</button>
          </div>
          <div className="space-y-3">
            {accounts.map((account, i) => (
              <div key={i} className="bg-white rounded-xl p-5 border border-slate-200 hover:border-slate-300 hover:shadow-sm transition-all flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-lg ${
                    account.type === 'current' ? 'bg-[#0d2847]/10 text-[#0d2847]' :
                    account.type === 'savings' ? 'bg-[#d4af37]/10 text-[#d4af37]' :
                    'bg-emerald-50 text-emerald-600'
                  }`}>
                    {account.type === 'current' ? '🏦' : account.type === 'savings' ? '🌙' : '📊'}
                  </div>
                  <div>
                    <p className="font-medium text-slate-800">{account.name}</p>
                    <p className="text-sm text-slate-400">{account.number}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-slate-800">{formatAED(account.balance)}</p>
                  <p className="text-xs text-emerald-500 font-medium">{account.trend}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Recent Transactions */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-slate-800">Recent Activity</h3>
            <button className="text-sm text-[#0d2847] hover:text-[#d4af37] font-medium">See All</button>
          </div>
          <div className="bg-white rounded-xl border border-slate-200 divide-y divide-slate-100">
            {recentTransactions.map((tx) => (
              <div key={tx.id} className="px-4 py-3 flex items-center justify-between hover:bg-slate-50 transition-colors">
                <div className="flex items-center gap-3">
                  <span className="text-lg">{tx.icon}</span>
                  <div>
                    <p className="text-sm font-medium text-slate-700">{tx.desc}</p>
                    <p className="text-xs text-slate-400">{tx.date}</p>
                  </div>
                </div>
                <span className={`text-sm font-semibold whitespace-nowrap ${tx.amount >= 0 ? 'text-emerald-600' : 'text-slate-700'}`}>
                  {tx.amount >= 0 ? '+' : '-'}{formatAED(tx.amount)}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: 'Monthly Income', value: 'AED 45,800', change: '+12.5%', positive: true },
          { label: 'Monthly Expenses', value: 'AED 15,720', change: '-2.8%', positive: true },
          { label: 'Active Cards', value: '4', change: '2 Visa, 2 MC', positive: true },
          { label: 'KYC / Emirates ID', value: 'Verified', change: 'Gold Tier', positive: true },
        ].map((stat, i) => (
          <div key={i} className="bg-white rounded-xl p-4 border border-slate-200">
            <p className="text-xs text-slate-500 uppercase tracking-wide font-medium">{stat.label}</p>
            <p className="text-xl font-bold text-slate-800 mt-1">{stat.value}</p>
            <p className={`text-xs mt-1 font-medium ${stat.positive ? 'text-emerald-500' : 'text-red-500'}`}>{stat.change}</p>
          </div>
        ))}
      </div>

      {/* Exchange Rates */}
      <div className="bg-white rounded-xl p-5 border border-slate-200">
        <h3 className="text-lg font-semibold text-slate-800 mb-4">Exchange Rates (AED)</h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          {[
            { currency: 'USD', rate: '3.6725', flag: '🇺🇸', change: '+0.01%' },
            { currency: 'EUR', rate: '4.0142', flag: '🇪🇺', change: '-0.15%' },
            { currency: 'GBP', rate: '4.6531', flag: '🇬🇧', change: '+0.08%' },
            { currency: 'SAR', rate: '0.9793', flag: '🇸🇦', change: '0.00%' },
            { currency: 'INR', rate: '0.0437', flag: '🇮🇳', change: '-0.22%' },
            { currency: 'PKR', rate: '0.0132', flag: '🇵🇰', change: '-0.10%' },
          ].map((fx) => (
            <div key={fx.currency} className="text-center p-3 rounded-lg bg-slate-50">
              <span className="text-2xl">{fx.flag}</span>
              <p className="text-sm font-bold text-slate-800 mt-1">{fx.currency}</p>
              <p className="text-sm text-[#0d2847] font-semibold">{fx.rate}</p>
              <p className={`text-[10px] ${fx.change.startsWith('-') ? 'text-red-500' : fx.change === '0.00%' ? 'text-slate-400' : 'text-emerald-500'}`}>{fx.change}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
