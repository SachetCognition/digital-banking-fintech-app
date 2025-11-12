import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { LoginLogout } from './auth/AuthProvider'
import { Protected } from './Protected'
import Home from './pages/Home'
import Transfer from './pages/Transfer'
import Beneficiaries from './pages/Beneficiaries'
import BillPay from './pages/BillPay'
import Kyc from './pages/Kyc'
import ExternalTransfers from './pages/ExternalTransfers'
import InternationalTransfers from './pages/InternationalTransfers'
import RecurringPayments from './pages/RecurringPayments'
import PaymentTemplates from './pages/PaymentTemplates'
import BulkPayments from './pages/BulkPayments'
import PaymentLimits from './pages/PaymentLimits'
import CardManagement from './pages/CardManagement'
import LoanManagement from './pages/LoanManagement'
import InvestmentManagement from './pages/InvestmentManagement'
import ComplianceManagement from './pages/ComplianceManagement'
import HelpCenter from './pages/HelpCenter'
import Feedback from './pages/Feedback'
import ChatSupport from './pages/ChatSupport'
import AdminDashboard from './pages/AdminDashboard'
import UserManagement from './pages/UserManagement'
import SystemMonitoring from './pages/SystemMonitoring'
import ConfigurationManagement from './pages/ConfigurationManagement'
import BackupRecovery from './pages/BackupRecovery'
import ApiDocumentation from './pages/ApiDocumentation'
import SdkDownloads from './pages/SdkDownloads'
import SecurityDashboard from './pages/SecurityDashboard'
import PerformanceDashboard from './pages/PerformanceDashboard'
import TestingDashboard from './pages/TestingDashboard'
import DevOpsDashboard from './pages/DevOpsDashboard'
import BusinessDashboard from './pages/BusinessDashboard'
import AdminDashboard from './pages/AdminDashboard'
import Login from './pages/Login'
import Register from './pages/Register'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import VerifyEmail from './pages/VerifyEmail'

function AppContent() {
  return (
    <div style={{ fontFamily: 'Inter, system-ui, sans-serif', padding: 24 }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Digital Banking Fintech</h1>
        <LoginLogout />
      </header>
      <hr />
      <Protected>
        <nav style={{ marginBottom: 20, display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          <a href="/" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Home
          </a>
          <a href="/transfer" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            P2P Transfer
          </a>
          <a href="/external-transfers" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            External Transfers
          </a>
          <a href="/international-transfers" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            International
          </a>
          <a href="/recurring-payments" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Recurring
          </a>
          <a href="/payment-templates" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Templates
          </a>
          <a href="/bulk-payments" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Bulk Payments
          </a>
          <a href="/beneficiaries" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Beneficiaries
          </a>
          <a href="/billpay" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Bill Pay
          </a>
          <a href="/payment-limits" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Limits
          </a>
          <a href="/kyc" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            KYC
          </a>
          <a href="/card-management" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Card Management
          </a>
          <a href="/loan-management" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Loan Management
          </a>
          <a href="/investment-management" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Investment Management
          </a>
          <a href="/compliance-management" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Compliance Management
          </a>
          <a href="/help" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Help Center
          </a>
          <a href="/feedback" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Feedback
          </a>
          <a href="/chat" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Live Chat
          </a>
          <a href="/admin-dashboard" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Admin Dashboard
          </a>
          <a href="/user-management" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            User Management
          </a>
          <a href="/system-monitoring" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            System Monitoring
          </a>
          <a href="/configuration-management" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Configuration
          </a>
          <a href="/backup-recovery" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Backup & Recovery
          </a>
          <a href="/api-documentation" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            API Docs
          </a>
          <a href="/sdk-downloads" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            SDK Downloads
          </a>
          <a href="/security-dashboard" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Security Dashboard
          </a>
          <a href="/performance-dashboard" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Performance Dashboard
          </a>
          <a href="/testing-dashboard" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Testing Dashboard
          </a>
          <a href="/devops-dashboard" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            DevOps Dashboard
          </a>
          <a href="/business-dashboard" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Business Dashboard
          </a>
          <a href="/admin-dashboard" style={{ padding: '8px 16px', backgroundColor: '#f8f9fa', color: 'black', border: '1px solid #dee2e6', borderRadius: 4, textDecoration: 'none' }}>
            Admin Dashboard
          </a>
        </nav>
        
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/transfer" element={<Transfer />} />
          <Route path="/external-transfers" element={<ExternalTransfers />} />
          <Route path="/international-transfers" element={<InternationalTransfers />} />
          <Route path="/recurring-payments" element={<RecurringPayments />} />
          <Route path="/payment-templates" element={<PaymentTemplates />} />
          <Route path="/bulk-payments" element={<BulkPayments />} />
          <Route path="/beneficiaries" element={<Beneficiaries />} />
          <Route path="/billpay" element={<BillPay />} />
          <Route path="/payment-limits" element={<PaymentLimits />} />
          <Route path="/kyc" element={<Kyc />} />
          <Route path="/card-management" element={<CardManagement />} />
          <Route path="/loan-management" element={<LoanManagement />} />
          <Route path="/investment-management" element={<InvestmentManagement />} />
          <Route path="/compliance-management" element={<ComplianceManagement />} />
          <Route path="/help" element={<HelpCenter />} />
          <Route path="/feedback" element={<Feedback />} />
          <Route path="/chat" element={<ChatSupport />} />
          <Route path="/admin-dashboard" element={<AdminDashboard />} />
          <Route path="/user-management" element={<UserManagement />} />
          <Route path="/system-monitoring" element={<SystemMonitoring />} />
          <Route path="/configuration-management" element={<ConfigurationManagement />} />
          <Route path="/backup-recovery" element={<BackupRecovery />} />
          <Route path="/api-documentation" element={<ApiDocumentation />} />
          <Route path="/sdk-downloads" element={<SdkDownloads />} />
          <Route path="/security-dashboard" element={<SecurityDashboard />} />
          <Route path="/performance-dashboard" element={<PerformanceDashboard />} />
          <Route path="/testing-dashboard" element={<TestingDashboard />} />
          <Route path="/devops-dashboard" element={<DevOpsDashboard />} />
          <Route path="/business-dashboard" element={<BusinessDashboard />} />
          <Route path="/admin-dashboard" element={<AdminDashboard />} />
        </Routes>
      </Protected>
    </div>
  )
}

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/verify-email" element={<VerifyEmail />} />
        <Route path="/*" element={<AppContent />} />
      </Routes>
    </Router>
  )
}
