import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';

interface NavItem {
  label: string;
  path: string;
  icon: string;
}

interface NavGroup {
  title: string;
  items: NavItem[];
}

const NAV_GROUPS: NavGroup[] = [
  {
    title: 'Overview',
    items: [
      { label: 'Dashboard', path: '/', icon: '🏠' },
      { label: 'Profile', path: '/profile', icon: '👤' },
    ],
  },
  {
    title: 'Banking',
    items: [
      { label: 'Transfers', path: '/transfer', icon: '💸' },
      { label: 'External Transfers', path: '/external-transfers', icon: '🏦' },
      { label: 'International', path: '/international-transfers', icon: '🌍' },
      { label: 'Bill Pay', path: '/billpay', icon: '📄' },
      { label: 'Beneficiaries', path: '/beneficiaries', icon: '👥' },
    ],
  },
  {
    title: 'Payments',
    items: [
      { label: 'Recurring', path: '/recurring-payments', icon: '🔄' },
      { label: 'Templates', path: '/payment-templates', icon: '📋' },
      { label: 'Bulk Payments', path: '/bulk-payments', icon: '📦' },
      { label: 'Limits', path: '/payment-limits', icon: '⚙️' },
    ],
  },
  {
    title: 'Products',
    items: [
      { label: 'Cards', path: '/card-management', icon: '💳' },
      { label: 'Loans', path: '/loan-management', icon: '📊' },
      { label: 'Investments', path: '/investment-management', icon: '📈' },
    ],
  },
  {
    title: 'Compliance & Security',
    items: [
      { label: 'KYC Verification', path: '/kyc', icon: '🔍' },
      { label: 'Compliance', path: '/compliance-management', icon: '🛡️' },
      { label: 'Security', path: '/security-dashboard', icon: '🔒' },
    ],
  },
  {
    title: 'Support',
    items: [
      { label: 'Help Center', path: '/help', icon: '❓' },
      { label: 'Feedback', path: '/feedback', icon: '💬' },
      { label: 'Live Chat', path: '/chat', icon: '🗨️' },
    ],
  },
  {
    title: 'Administration',
    items: [
      { label: 'Admin Dashboard', path: '/admin-dashboard', icon: '⚡' },
      { label: 'Users', path: '/user-management', icon: '👤' },
      { label: 'Monitoring', path: '/system-monitoring', icon: '📡' },
      { label: 'Configuration', path: '/configuration-management', icon: '🔧' },
      { label: 'Backup', path: '/backup-recovery', icon: '💾' },
    ],
  },
  {
    title: 'Developer',
    items: [
      { label: 'API Docs', path: '/api-documentation', icon: '📖' },
      { label: 'SDKs', path: '/sdk-downloads', icon: '📦' },
      { label: 'Performance', path: '/performance-dashboard', icon: '⚡' },
      { label: 'Testing', path: '/testing-dashboard', icon: '🧪' },
      { label: 'DevOps', path: '/devops-dashboard', icon: '🚀' },
      { label: 'Business', path: '/business-dashboard', icon: '💼' },
    ],
  },
];

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  const user = (() => {
    try {
      const d = localStorage.getItem('user');
      return d ? JSON.parse(d) : null;
    } catch { return null; }
  })();

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  return (
    <div className="flex h-screen bg-[#f5f6f8] overflow-hidden">
      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 bg-black/50 z-40 lg:hidden" onClick={() => setMobileOpen(false)} />
      )}

      {/* Sidebar - UAE deep navy theme */}
      <aside className={`
        fixed lg:static z-50 h-full transition-all duration-300 ease-in-out
        ${mobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        ${sidebarOpen ? 'w-64' : 'w-20'}
        bg-gradient-to-b from-[#0a1628] via-[#0d2240] to-[#0a1628] text-slate-300 flex flex-col
      `}>
        {/* Logo */}
        <div className={`flex items-center gap-3 px-5 py-5 border-b border-[#1a3358]/60 ${!sidebarOpen && 'justify-center px-2'}`}>
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#d4af37] to-[#b8960c] flex items-center justify-center text-white font-bold text-lg shrink-0 shadow-lg shadow-[#d4af37]/10">
            E
          </div>
          {sidebarOpen && (
            <div>
              <h1 className="text-white font-semibold text-sm leading-tight">Emirates Digital</h1>
              <p className="text-[#d4af37]/60 text-[10px] tracking-wide">Banking Platform</p>
            </div>
          )}
        </div>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto py-3 px-3 space-y-1">
          {NAV_GROUPS.map((group) => (
            <div key={group.title} className="mb-2">
              {sidebarOpen && (
                <div className="px-3 py-2 text-[10px] font-semibold uppercase tracking-wider text-[#4a6fa5]">
                  {group.title}
                </div>
              )}
              {group.items.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    onClick={() => setMobileOpen(false)}
                    className={`
                      flex items-center gap-3 px-3 py-2 rounded-lg text-[13px] transition-all duration-150
                      ${isActive
                        ? 'bg-gradient-to-r from-[#d4af37]/15 to-[#d4af37]/5 text-white font-medium border-l-2 border-[#d4af37]'
                        : 'text-slate-400 hover:bg-[#1a3358]/40 hover:text-slate-200'}
                      ${!sidebarOpen && 'justify-center px-2'}
                    `}
                    title={!sidebarOpen ? item.label : undefined}
                  >
                    <span className="text-base shrink-0">{item.icon}</span>
                    {sidebarOpen && <span>{item.label}</span>}
                  </Link>
                );
              })}
            </div>
          ))}
        </nav>

        {/* Sidebar toggle */}
        <button
          onClick={() => setSidebarOpen(!sidebarOpen)}
          className="hidden lg:flex items-center justify-center py-3 border-t border-[#1a3358]/60 text-[#4a6fa5] hover:text-white transition-colors"
        >
          <svg className={`w-5 h-5 transition-transform ${!sidebarOpen && 'rotate-180'}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
          </svg>
        </button>
      </aside>

      {/* Main */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top bar */}
        <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 lg:px-6 shrink-0 shadow-sm">
          <div className="flex items-center gap-4">
            <button
              className="lg:hidden p-2 rounded-lg hover:bg-slate-100 text-slate-600"
              onClick={() => setMobileOpen(!mobileOpen)}
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <div>
              <h2 className="text-base font-semibold text-slate-800">
                {NAV_GROUPS.flatMap(g => g.items).find(i => i.path === location.pathname)?.label || 'Dashboard'}
              </h2>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {/* Notifications bell */}
            <button className="relative p-2 rounded-lg hover:bg-slate-100 text-slate-500">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
            </button>

            {/* User info */}
            <div className="flex items-center gap-3 pl-3 border-l border-slate-200">
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-[#d4af37] to-[#b8960c] flex items-center justify-center text-white text-sm font-semibold">
                {user?.fullName?.[0] || 'A'}
              </div>
              <div className="hidden sm:block">
                <p className="text-sm font-medium text-slate-700">{user?.fullName || 'Ahmed Al-Rashid'}</p>
                <p className="text-[11px] text-slate-400">{user?.email || 'ahmed@emiratesdigital.ae'}</p>
              </div>
              <button
                onClick={handleLogout}
                className="ml-2 p-2 rounded-lg hover:bg-red-50 text-slate-400 hover:text-red-500 transition-colors"
                title="Sign out"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
              </button>
            </div>
          </div>
        </header>

        {/* Content */}
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          {children}
        </main>
      </div>
    </div>
  );
};

export default Layout;
