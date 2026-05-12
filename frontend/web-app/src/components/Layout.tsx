import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LoginLogout } from '../auth/AuthProvider';

interface NavGroup {
  title: string;
  items: { label: string; path: string }[];
}

const NAV_GROUPS: NavGroup[] = [
  {
    title: 'Core Banking',
    items: [
      { label: 'Home', path: '/' },
      { label: 'KYC', path: '/kyc' },
      { label: 'Profile', path: '/profile' },
    ],
  },
  {
    title: 'Payments',
    items: [
      { label: 'P2P Transfer', path: '/transfer' },
      { label: 'External Transfers', path: '/external-transfers' },
      { label: 'International', path: '/international-transfers' },
      { label: 'Recurring', path: '/recurring-payments' },
      { label: 'Templates', path: '/payment-templates' },
      { label: 'Bulk Payments', path: '/bulk-payments' },
      { label: 'Beneficiaries', path: '/beneficiaries' },
      { label: 'Bill Pay', path: '/billpay' },
      { label: 'Limits', path: '/payment-limits' },
    ],
  },
  {
    title: 'Products',
    items: [
      { label: 'Cards', path: '/card-management' },
      { label: 'Loans', path: '/loan-management' },
      { label: 'Investments', path: '/investment-management' },
    ],
  },
  {
    title: 'Compliance',
    items: [
      { label: 'Compliance', path: '/compliance-management' },
      { label: 'Security', path: '/security-dashboard' },
    ],
  },
  {
    title: 'Support',
    items: [
      { label: 'Help Center', path: '/help' },
      { label: 'Feedback', path: '/feedback' },
      { label: 'Live Chat', path: '/chat' },
    ],
  },
  {
    title: 'Admin',
    items: [
      { label: 'Dashboard', path: '/admin-dashboard' },
      { label: 'Users', path: '/user-management' },
      { label: 'Monitoring', path: '/system-monitoring' },
      { label: 'Configuration', path: '/configuration-management' },
      { label: 'Backup', path: '/backup-recovery' },
    ],
  },
  {
    title: 'Developer',
    items: [
      { label: 'API Docs', path: '/api-documentation' },
      { label: 'SDKs', path: '/sdk-downloads' },
      { label: 'Performance', path: '/performance-dashboard' },
      { label: 'Testing', path: '/testing-dashboard' },
      { label: 'DevOps', path: '/devops-dashboard' },
      { label: 'Business', path: '/business-dashboard' },
    ],
  },
];

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  return (
    <div style={{ display: 'flex', minHeight: '100vh', fontFamily: 'Inter, system-ui, sans-serif' }}>
      {/* Mobile hamburger */}
      <button
        onClick={() => setSidebarOpen(!sidebarOpen)}
        style={{
          position: 'fixed', top: 12, left: 12, zIndex: 1000,
          display: 'none', padding: '8px 12px', background: '#1e293b',
          color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer',
          fontSize: 18,
        }}
        className="mobile-menu-btn"
      >
        {sidebarOpen ? '\u2715' : '\u2630'}
      </button>

      {/* Sidebar */}
      <aside style={{
        width: 240, background: '#1e293b', color: '#e2e8f0',
        padding: '16px 0', overflowY: 'auto', position: 'fixed',
        height: '100vh', transform: sidebarOpen ? 'translateX(0)' : undefined,
        transition: 'transform 0.2s',
      }}>
        <div style={{ padding: '0 16px 16px', borderBottom: '1px solid #334155' }}>
          <h2 style={{ margin: 0, fontSize: 16, color: '#fff' }}>Digital Banking</h2>
        </div>
        <nav style={{ padding: '8px 0' }}>
          {NAV_GROUPS.map((group) => (
            <div key={group.title} style={{ marginBottom: 8 }}>
              <div style={{ padding: '8px 16px', fontSize: 11, textTransform: 'uppercase', color: '#94a3b8', letterSpacing: 1 }}>
                {group.title}
              </div>
              {group.items.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    style={{
                      display: 'block', padding: '6px 16px 6px 24px',
                      color: isActive ? '#fff' : '#cbd5e1',
                      background: isActive ? '#334155' : 'transparent',
                      textDecoration: 'none', fontSize: 13, borderRadius: 4,
                      margin: '0 8px',
                    }}
                  >
                    {item.label}
                  </Link>
                );
              })}
            </div>
          ))}
        </nav>
      </aside>

      {/* Main content */}
      <main style={{ marginLeft: 240, flex: 1, display: 'flex', flexDirection: 'column' }}>
        <header style={{
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          padding: '12px 24px', borderBottom: '1px solid #e2e8f0', background: '#fff',
        }}>
          <h1 style={{ margin: 0, fontSize: 18 }}>Digital Banking Fintech</h1>
          <LoginLogout />
        </header>
        <div style={{ padding: 24, flex: 1 }}>
          {children}
        </div>
      </main>
    </div>
  );
};

export default Layout;
