import React, { useEffect, useState } from 'react'
import { api } from '../api/http'
import AccountSelect from '../components/AccountSelect'

interface TransferRequest {
  payerAccountId: string
  payeeAccountId: string
  amount: number
  currency: string
  description: string
  idempotencyKey: string
}

export default function Transfer() {
  const isAuthenticated = !!localStorage.getItem('accessToken')
  const [transfer, setTransfer] = useState<TransferRequest>({
    payerAccountId: '',
    payeeAccountId: '',
    amount: 0,
    currency: 'AED',
    description: '',
    idempotencyKey: ''
  })
  const [result, setResult] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [limits, setLimits] = useState<{ dailyAmount?: number; perTxnAmount?: number } | null>(null)
  const [usage, setUsage] = useState<{ dailyLimit?: number; used?: number; remaining?: number } | null>(null)
  const [friendlyError, setFriendlyError] = useState<string | null>(null)

  // Fetch limits and today's usage when payerAccountId changes and user is authenticated
  useEffect(() => {
    const fetchData = async () => {
      if (!transfer.payerAccountId || !isAuthenticated) return
      try {
        // Account limits (per-account overrides)
        const limRes = await api.get(`/api/v1/accounts/${transfer.payerAccountId}/limits`)
        setLimits({
          dailyAmount: limRes.data?.dailyAmount ? Number(limRes.data.dailyAmount) : undefined,
          perTxnAmount: limRes.data?.perTxnAmount ? Number(limRes.data.perTxnAmount) : undefined
        })
      } catch (e) {
        setLimits(null)
      }

      try {
        // Today's usage & remaining quota from payments-service
        const uRes = await api.get(`/api/v1/transfers/daily-usage`, {
          params: { payerAccountId: transfer.payerAccountId }
        })
        setUsage({
          dailyLimit: uRes.data?.dailyLimit ? Number(uRes.data.dailyLimit) : undefined,
          used: uRes.data?.used ? Number(uRes.data.used) : undefined,
          remaining: uRes.data?.remaining ? Number(uRes.data.remaining) : undefined
        })
      } catch (e) {
        setUsage(null)
      }
    }
    fetchData()
  }, [transfer.payerAccountId, isAuthenticated])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setResult(null)
    setFriendlyError(null)

    try {
      const response = await api.post(
        '/api/v1/transfers',
        { ...transfer, amount: transfer.amount },
        { headers: { 'Idempotency-Key': transfer.idempotencyKey } }
      )
      setResult(response.data)
    } catch (error: any) {
      const raw = error.response?.data?.error || error.message || 'Transfer failed'
      // Friendly mapping
      let friendly = raw
      if (/per-transaction limit/i.test(raw)) friendly = 'Amount exceeds your per-transaction limit.'
      else if (/daily limit/i.test(raw)) friendly = 'This transfer would exceed your daily limit.'
      else if (/insufficient balance/i.test(raw)) friendly = 'Insufficient balance in the payer account.'
      setFriendlyError(friendly)
      setResult({ error: raw })
    } finally {
      setLoading(false)
    }
  }

  const generateIdempotencyKey = () => {
    setTransfer(prev => ({
      ...prev,
      idempotencyKey: `transfer-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    }))
  }

  return (
    <div style={{ maxWidth: 600, margin: '0 auto', padding: 20 }}>
      <h2>P2P Transfer</h2>
      
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        {/* Limits and usage panel */}
        <div style={{ padding: 12, background: '#f6f9ff', border: '1px solid #cfe2ff', borderRadius: 6 }}>
          <strong>Limits & Todays Usage</strong>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginTop: 8 }}>
            <div>
              <div style={{ color: '#6c757d' }}>Per-Transaction Limit</div>
              <div style={{ fontWeight: 600 }}>{limits?.perTxnAmount !== undefined ? limits.perTxnAmount.toFixed(2) : '—'}</div>
            </div>
            <div>
              <div style={{ color: '#6c757d' }}>Daily Limit</div>
              <div style={{ fontWeight: 600 }}>{(usage?.dailyLimit ?? limits?.dailyAmount) !== undefined ? Number((usage?.dailyLimit ?? limits?.dailyAmount)).toFixed(2) : '—'}</div>
            </div>
            <div>
              <div style={{ color: '#6c757d' }}>Used Today</div>
              <div style={{ fontWeight: 600 }}>{usage?.used !== undefined ? usage.used.toFixed(2) : '—'}</div>
            </div>
            <div>
              <div style={{ color: '#6c757d' }}>Remaining Today</div>
              <div style={{ fontWeight: 600 }}>{usage?.remaining !== undefined ? usage.remaining.toFixed(2) : '—'}</div>
            </div>
          </div>
        </div>
        <AccountSelect
          label="Payer Account"
          value={transfer.payerAccountId}
          onChange={(val) => setTransfer(prev => ({ ...prev, payerAccountId: val }))}
        />

        <AccountSelect
          label="Payee Account"
          value={transfer.payeeAccountId}
          onChange={(val) => setTransfer(prev => ({ ...prev, payeeAccountId: val }))}
        />

        <div>
          <label style={{ display: 'block', marginBottom: 4 }}>Amount:</label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            value={transfer.amount}
            onChange={(e) => setTransfer(prev => ({ ...prev, amount: parseFloat(e.target.value) || 0 }))}
            placeholder="Enter amount"
            style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
            required
          />
          {limits?.perTxnAmount !== undefined && transfer.amount > (limits.perTxnAmount || 0) && (
            <div style={{ color: '#dc3545', marginTop: 4 }}>
              Amount exceeds per-transaction limit of {limits.perTxnAmount.toFixed(2)}
            </div>
          )}
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: 4 }}>Currency:</label>
          <select
            value={transfer.currency}
            onChange={(e) => setTransfer(prev => ({ ...prev, currency: e.target.value }))}
            style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
          >
            <option value="AED">AED - UAE Dirham</option>
                  <option value="USD">USD - US Dollar</option>
                  <option value="EUR">EUR - Euro</option>
                  <option value="GBP">GBP - British Pound</option>
                  <option value="SAR">SAR - Saudi Riyal</option>
            <option value="EUR">EUR</option>
            <option value="GBP">GBP</option>
          </select>
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: 4 }}>Description:</label>
          <input
            type="text"
            value={transfer.description}
            onChange={(e) => setTransfer(prev => ({ ...prev, description: e.target.value }))}
            placeholder="Enter description"
            style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: 4 }}>Idempotency Key:</label>
          <div style={{ display: 'flex', gap: 8 }}>
            <input
              type="text"
              value={transfer.idempotencyKey}
              onChange={(e) => setTransfer(prev => ({ ...prev, idempotencyKey: e.target.value }))}
              placeholder="Enter unique idempotency key"
              style={{ flex: 1, padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
              required
            />
            <button
              type="button"
              onClick={generateIdempotencyKey}
              style={{ padding: '8px 16px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: 4, cursor: 'pointer' }}
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
          {loading ? 'Processing...' : 'Create Transfer'}
        </button>
      </form>

      {friendlyError && (
        <div style={{ marginTop: 16, padding: 12, background: '#fff3cd', border: '1px solid #ffeeba', color: '#856404', borderRadius: 4 }}>
          {friendlyError}
        </div>
      )}

      {result && !friendlyError && (
        <div style={{ marginTop: 20, padding: 16, backgroundColor: '#f8f9fa', border: '1px solid #dee2e6', borderRadius: 4 }}>
          <h3>Result:</h3>
          <pre style={{ whiteSpace: 'pre-wrap', fontSize: 14 }}>
            {JSON.stringify(result, null, 2)}
          </pre>
        </div>
      )}
    </div>
  )
}

