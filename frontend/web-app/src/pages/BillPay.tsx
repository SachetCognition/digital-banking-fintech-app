import React, { useState, useEffect } from 'react'
import { useAuth } from 'react-oidc-context'
import { api, attachAuth } from '../api/http'

interface Beneficiary {
  id: string
  name: string
  accountNumber: string
  bankName: string
  currency: string
  type: string
}

interface BillPayRequest {
  payerAccountId: string
  beneficiaryId: string
  amount: number
  currency: string
  description: string
  idempotencyKey: string
}

export default function BillPay() {
  const auth = useAuth()
  const [beneficiaries, setBeneficiaries] = useState<Beneficiary[]>([])
  const [selectedBeneficiary, setSelectedBeneficiary] = useState<Beneficiary | null>(null)
  const [billPay, setBillPay] = useState<BillPayRequest>({
    payerAccountId: '',
    beneficiaryId: '',
    amount: 0,
    currency: 'USD',
    description: '',
    idempotencyKey: ''
  })
  const [result, setResult] = useState<any>(null)
  const [loading, setLoading] = useState(false)

  React.useEffect(() => {
    attachAuth(auth.user)
    loadBeneficiaries()
  }, [auth.user])

  const loadBeneficiaries = async () => {
    try {
      const response = await api.get('/api/v1/beneficiaries?type=BILL_PAY')
      setBeneficiaries(response.data)
    } catch (error) {
      console.error('Failed to load beneficiaries:', error)
    }
  }

  const handleBeneficiarySelect = (beneficiary: Beneficiary) => {
    setSelectedBeneficiary(beneficiary)
    setBillPay(prev => ({
      ...prev,
      beneficiaryId: beneficiary.id,
      currency: beneficiary.currency
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setResult(null)

    try {
      const response = await api.post('/api/v1/bill-pay', {
        ...billPay,
        amount: billPay.amount
      })
      setResult(response.data)
    } catch (error: any) {
      setResult({ error: error.response?.data?.error || error.message })
    } finally {
      setLoading(false)
    }
  }

  const generateIdempotencyKey = () => {
    setBillPay(prev => ({
      ...prev,
      idempotencyKey: `billpay-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    }))
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 20 }}>
      <h2>Bill Pay</h2>
      
      {/* Beneficiary Selection */}
      <div style={{ marginBottom: 30 }}>
        <h3>Select Beneficiary</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: 16 }}>
          {beneficiaries.map((beneficiary) => (
            <div
              key={beneficiary.id}
              onClick={() => handleBeneficiarySelect(beneficiary)}
              style={{
                padding: 16,
                border: selectedBeneficiary?.id === beneficiary.id ? '2px solid #007bff' : '1px solid #ddd',
                borderRadius: 8,
                cursor: 'pointer',
                backgroundColor: selectedBeneficiary?.id === beneficiary.id ? '#f8f9fa' : 'white',
                transition: 'all 0.2s'
              }}
            >
              <div style={{ fontWeight: 'bold', marginBottom: 8 }}>{beneficiary.name}</div>
              <div style={{ fontSize: 14, color: '#666', marginBottom: 4 }}>
                {beneficiary.accountNumber}
              </div>
              <div style={{ fontSize: 14, color: '#666', marginBottom: 4 }}>
                {beneficiary.bankName}
              </div>
              <div style={{ fontSize: 12, color: '#999' }}>
                {beneficiary.currency} • {beneficiary.type}
              </div>
            </div>
          ))}
        </div>
        
        {beneficiaries.length === 0 && (
          <div style={{ 
            padding: 20, 
            textAlign: 'center', 
            color: '#666',
            border: '1px dashed #ddd',
            borderRadius: 8
          }}>
            No bill pay beneficiaries found. <br />
            <a href="/beneficiaries" style={{ color: '#007bff' }}>Add beneficiaries first</a>
          </div>
        )}
      </div>

      {/* Bill Pay Form */}
      {selectedBeneficiary && (
        <div style={{ border: '1px solid #ddd', borderRadius: 8, padding: 20 }}>
          <h3>Pay Bill to {selectedBeneficiary.name}</h3>
          
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Payer Account ID:</label>
              <input
                type="text"
                value={billPay.payerAccountId}
                onChange={(e) => setBillPay(prev => ({ ...prev, payerAccountId: e.target.value }))}
                placeholder="Enter your account UUID"
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
                required
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Amount ({selectedBeneficiary.currency}):</label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                value={billPay.amount}
                onChange={(e) => setBillPay(prev => ({ ...prev, amount: parseFloat(e.target.value) || 0 }))}
                placeholder="Enter amount"
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
                required
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Description:</label>
              <input
                type="text"
                value={billPay.description}
                onChange={(e) => setBillPay(prev => ({ ...prev, description: e.target.value }))}
                placeholder="Enter description"
                style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: 4 }}>Idempotency Key:</label>
              <div style={{ display: 'flex', gap: 8 }}>
                <input
                  type="text"
                  value={billPay.idempotencyKey}
                  onChange={(e) => setBillPay(prev => ({ ...prev, idempotencyKey: e.target.value }))}
                  placeholder="Enter unique idempotency key"
                  style={{ flex: 1, padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
                  required
                />
                <button
                  type="button"
                  onClick={generateIdempotencyKey}
                  style={{
                    padding: '8px 16px',
                    backgroundColor: '#007bff',
                    color: 'white',
                    border: 'none',
                    borderRadius: 4,
                    cursor: 'pointer'
                  }}
                >
                  Generate
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              style={{
                padding: 12,
                backgroundColor: loading ? '#6c757d' : '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: 4,
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: 16
              }}
            >
              {loading ? 'Processing...' : 'Pay Bill'}
            </button>
          </form>
        </div>
      )}

      {/* Result Display */}
      {result && (
        <div style={{ 
          marginTop: 20, 
          padding: 16, 
          backgroundColor: '#f8f9fa', 
          border: '1px solid #dee2e6', 
          borderRadius: 4 
        }}>
          <h3>Result:</h3>
          <pre style={{ whiteSpace: 'pre-wrap', fontSize: 14 }}>
            {JSON.stringify(result, null, 2)}
          </pre>
        </div>
      )}
    </div>
  )
}

