import React, { useState, useEffect } from 'react'
import { useAuth } from 'react-oidc-context'
import { api, attachAuth } from '../api/http'

interface Beneficiary {
  id: string
  customerId: string
  name: string
  accountNumber: string
  bankCode: string
  bankName: string
  currency: string
  type: string
  status: string
  description?: string
  createdAt: string
  updatedAt: string
}

interface CreateBeneficiaryRequest {
  name: string
  accountNumber: string
  bankCode: string
  bankName: string
  currency: string
  type: string
  description?: string
}

export default function Beneficiaries() {
  const auth = useAuth()
  const [beneficiaries, setBeneficiaries] = useState<Beneficiary[]>([])
  const [loading, setLoading] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [filterType, setFilterType] = useState('')
  const [formData, setFormData] = useState<CreateBeneficiaryRequest>({
    name: '',
    accountNumber: '',
    bankCode: '',
    bankName: '',
    currency: 'USD',
    type: 'INTERNAL',
    description: ''
  })

  React.useEffect(() => {
    attachAuth(auth.user)
    loadBeneficiaries()
  }, [auth.user])

  const loadBeneficiaries = async () => {
    setLoading(true)
    try {
      const params = new URLSearchParams()
      if (filterType) params.append('type', filterType)
      if (searchTerm) params.append('search', searchTerm)
      
      const response = await api.get(`/api/v1/beneficiaries?${params.toString()}`)
      setBeneficiaries(response.data)
    } catch (error) {
      console.error('Failed to load beneficiaries:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      await api.post('/api/v1/beneficiaries', formData)
      setShowForm(false)
      setFormData({
        name: '',
        accountNumber: '',
        bankCode: '',
        bankName: '',
        currency: 'USD',
        type: 'INTERNAL',
        description: ''
      })
      loadBeneficiaries()
    } catch (error: any) {
      alert('Failed to create beneficiary: ' + (error.response?.data?.error || error.message))
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this beneficiary?')) return
    
    try {
      await api.delete(`/api/v1/beneficiaries/${id}`)
      loadBeneficiaries()
    } catch (error: any) {
      alert('Failed to delete beneficiary: ' + (error.response?.data?.error || error.message))
    }
  }

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: 20 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>Beneficiaries</h2>
        <button
          onClick={() => setShowForm(!showForm)}
          style={{
            padding: '10px 20px',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: 4,
            cursor: 'pointer'
          }}
        >
          {showForm ? 'Cancel' : 'Add Beneficiary'}
        </button>
      </div>

      {/* Search and Filter */}
      <div style={{ display: 'flex', gap: 16, marginBottom: 20 }}>
        <input
          type="text"
          placeholder="Search by name or account number..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ flex: 1, padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
        />
        <select
          value={filterType}
          onChange={(e) => setFilterType(e.target.value)}
          style={{ padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
        >
          <option value="">All Types</option>
          <option value="INTERNAL">Internal</option>
          <option value="EXTERNAL">External</option>
          <option value="BILL_PAY">Bill Pay</option>
          <option value="MOBILE_MONEY">Mobile Money</option>
        </select>
        <button
          onClick={loadBeneficiaries}
          style={{
            padding: '8px 16px',
            backgroundColor: '#28a745',
            color: 'white',
            border: 'none',
            borderRadius: 4,
            cursor: 'pointer'
          }}
        >
          Search
        </button>
      </div>

      {/* Add Beneficiary Form */}
      {showForm && (
        <div style={{ 
          border: '1px solid #ddd', 
          borderRadius: 8, 
          padding: 20, 
          marginBottom: 20,
          backgroundColor: '#f9f9f9'
        }}>
          <h3>Add New Beneficiary</h3>
          <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Name *</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                required
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Account Number *</label>
              <input
                type="text"
                value={formData.accountNumber}
                onChange={(e) => setFormData(prev => ({ ...prev, accountNumber: e.target.value }))}
                required
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Bank Code *</label>
              <input
                type="text"
                value={formData.bankCode}
                onChange={(e) => setFormData(prev => ({ ...prev, bankCode: e.target.value }))}
                required
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Bank Name *</label>
              <input
                type="text"
                value={formData.bankName}
                onChange={(e) => setFormData(prev => ({ ...prev, bankName: e.target.value }))}
                required
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Currency *</label>
              <select
                value={formData.currency}
                onChange={(e) => setFormData(prev => ({ ...prev, currency: e.target.value }))}
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
              >
                <option value="USD">USD</option>
                <option value="EUR">EUR</option>
                <option value="GBP">GBP</option>
              </select>
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Type *</label>
              <select
                value={formData.type}
                onChange={(e) => setFormData(prev => ({ ...prev, type: e.target.value }))}
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
              >
                <option value="INTERNAL">Internal</option>
                <option value="EXTERNAL">External</option>
                <option value="BILL_PAY">Bill Pay</option>
                <option value="MOBILE_MONEY">Mobile Money</option>
              </select>
            </div>
            <div style={{ gridColumn: '1 / -1' }}>
              <label style={{ display: 'block', marginBottom: 4 }}>Description</label>
              <input
                type="text"
                value={formData.description}
                onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
              />
            </div>
            <div style={{ gridColumn: '1 / -1', display: 'flex', gap: 10 }}>
              <button
                type="submit"
                disabled={loading}
                style={{
                  padding: '10px 20px',
                  backgroundColor: loading ? '#6c757d' : '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: 4,
                  cursor: loading ? 'not-allowed' : 'pointer'
                }}
              >
                {loading ? 'Creating...' : 'Create Beneficiary'}
              </button>
              <button
                type="button"
                onClick={() => setShowForm(false)}
                style={{
                  padding: '10px 20px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: 4,
                  cursor: 'pointer'
                }}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Beneficiaries List */}
      <div style={{ border: '1px solid #ddd', borderRadius: 8, overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead style={{ backgroundColor: '#f8f9fa' }}>
            <tr>
              <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Name</th>
              <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Account</th>
              <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Bank</th>
              <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Type</th>
              <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Status</th>
              <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} style={{ padding: 20, textAlign: 'center' }}>Loading...</td>
              </tr>
            ) : beneficiaries.length === 0 ? (
              <tr>
                <td colSpan={6} style={{ padding: 20, textAlign: 'center' }}>No beneficiaries found</td>
              </tr>
            ) : (
              beneficiaries.map((beneficiary) => (
                <tr key={beneficiary.id}>
                  <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                    <div>
                      <strong>{beneficiary.name}</strong>
                      {beneficiary.description && (
                        <div style={{ fontSize: 12, color: '#666' }}>{beneficiary.description}</div>
                      )}
                    </div>
                  </td>
                  <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                    {beneficiary.accountNumber}
                  </td>
                  <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                    <div>{beneficiary.bankName}</div>
                    <div style={{ fontSize: 12, color: '#666' }}>{beneficiary.bankCode}</div>
                  </td>
                  <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                    <span style={{
                      padding: '4px 8px',
                      borderRadius: 4,
                      fontSize: 12,
                      backgroundColor: '#e9ecef',
                      color: '#495057'
                    }}>
                      {beneficiary.type}
                    </span>
                  </td>
                  <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                    <span style={{
                      padding: '4px 8px',
                      borderRadius: 4,
                      fontSize: 12,
                      backgroundColor: beneficiary.status === 'ACTIVE' ? '#d4edda' : '#f8d7da',
                      color: beneficiary.status === 'ACTIVE' ? '#155724' : '#721c24'
                    }}>
                      {beneficiary.status}
                    </span>
                  </td>
                  <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                    <button
                      onClick={() => handleDelete(beneficiary.id)}
                      style={{
                        padding: '4px 8px',
                        backgroundColor: '#dc3545',
                        color: 'white',
                        border: 'none',
                        borderRadius: 4,
                        cursor: 'pointer',
                        fontSize: 12
                      }}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

