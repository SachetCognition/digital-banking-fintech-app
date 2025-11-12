import React, { useEffect, useState } from 'react'
import { http } from '../api/http'

export default function Home() {
  const [health, setHealth] = useState('unknown')
  const [user, setUser] = useState<any>(null)

  useEffect(() => {
    const userData = localStorage.getItem('user')
    if (userData) {
      setUser(JSON.parse(userData))
    }

    http
      .get('/api/v1/customers/health')
      .then((res) => setHealth(res.data.status || 'ok'))
      .catch(() => setHealth('error'))
  }, [])

  return (
    <div>
      <h2>Welcome to Digital Bank</h2>
      {user && (
        <div className="mb-4 p-4 bg-blue-50 rounded-lg">
          <h3 className="text-lg font-semibold">Account Information</h3>
          <p><strong>Name:</strong> {user.fullName}</p>
          <p><strong>Email:</strong> {user.email}</p>
          <p><strong>Roles:</strong> {user.roles?.join(', ') || 'None'}</p>
        </div>
      )}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div className="p-4 border rounded-lg">
          <h3 className="text-lg font-semibold mb-2">Quick Actions</h3>
          <ul className="space-y-2">
            <li><a href="/transfer" className="text-blue-600 hover:underline">P2P Transfer</a></li>
            <li><a href="/beneficiaries" className="text-blue-600 hover:underline">Manage Beneficiaries</a></li>
            <li><a href="/billpay" className="text-blue-600 hover:underline">Bill Pay</a></li>
            <li><a href="/kyc" className="text-blue-600 hover:underline">KYC Verification</a></li>
          </ul>
        </div>
        <div className="p-4 border rounded-lg">
          <h3 className="text-lg font-semibold mb-2">System Status</h3>
          <p>Gateway → customer-service health: <strong className={health === 'ok' ? 'text-green-600' : 'text-red-600'}>{health}</strong></p>
        </div>
        <div className="p-4 border rounded-lg">
          <h3 className="text-lg font-semibold mb-2">Security</h3>
          <p className="text-sm text-gray-600">Your account is protected with multi-factor authentication and secure session management.</p>
        </div>
      </div>
    </div>
  )
}
