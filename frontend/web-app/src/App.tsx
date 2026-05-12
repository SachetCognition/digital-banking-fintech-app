import React, { useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
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
import Profile from './pages/Profile'
import Login from './pages/Login'
import Register from './pages/Register'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import VerifyEmail from './pages/VerifyEmail'

function AppContent() {
  useEffect(() => {
    if (!localStorage.getItem('user')) {
      localStorage.setItem('user', JSON.stringify({
        id: 'demo-001',
        email: 'ahmed.alrashid@emiratesdigital.ae',
        fullName: 'Ahmed Al-Rashid',
        roles: ['CUSTOMER', 'ADMIN'],
        permissions: ['*']
      }));
      localStorage.setItem('accessToken', 'demo-token');
    }
  }, []);

  return (
    <Layout>
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
        <Route path="/profile" element={<Profile />} />
      </Routes>
    </Layout>
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
