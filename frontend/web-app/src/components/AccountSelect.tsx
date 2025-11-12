import React, { useEffect, useState } from 'react'
import { api } from '../api/http'

interface Account {
  id: string
  accountNo?: string
  account_no?: string
  currency?: string
}

interface Props {
  label: string
  value: string
  onChange: (value: string) => void
  customerId?: string
}

export default function AccountSelect({ label, value, onChange, customerId }: Props) {
  const [accounts, setAccounts] = useState<Account[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const load = async () => {
      setError(null)
      try {
        const res = await api.get('/api/v1/accounts', {
          params: customerId ? { customerId } : undefined,
        })
        setAccounts(res.data)
      } catch (e: any) {
        setError('Could not load accounts (feature may not be ready). You can paste an Account ID below.')
        setAccounts(null)
      }
    }
    load()
  }, [customerId])

  if (!accounts) {
    return (
      <div>
        <label style={{ display: 'block', marginBottom: 4 }}>{label} (Account ID):</label>
        {error && (
          <div style={{ marginBottom: 8, color: '#dc3545' }}>{error}</div>
        )}
        <input
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder="Enter account UUID"
          style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
        />
      </div>
    )
  }

  return (
    <div>
      <label style={{ display: 'block', marginBottom: 4 }}>{label}:</label>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        style={{ width: '100%', padding: 8, border: '1px solid #ccc', borderRadius: 4 }}
      >
        <option value="">Select an account</option>
        {accounts.map((a) => (
          <option key={a.id} value={a.id}>
            {(a.accountNo || a.account_no || a.id) + (a.currency ? ` (${a.currency})` : '')}
          </option>
        ))}
      </select>
    </div>
  )
}
