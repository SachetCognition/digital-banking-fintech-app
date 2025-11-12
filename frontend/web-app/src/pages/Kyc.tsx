import React, { useState, useEffect } from 'react'
import { useAuth } from 'react-oidc-context'
import { api, attachAuth } from '../api/http'

interface KycResponse {
  kycCaseId: string
  customerId: string
  level: string
  status: string
  provider?: string
  providerRef?: string
  riskScore?: number
  rejectionReason?: string
  submittedAt?: string
  reviewedAt?: string
  expiresAt?: string
  createdAt: string
  updatedAt: string
  documents: DocumentInfo[]
}

interface DocumentInfo {
  documentId: string
  type: string
  fileName: string
  contentType: string
  fileSize: number
  fileHash: string
  status: string
  rejectionReason?: string
  uploadedAt: string
  processedAt?: string
}

interface DocumentSubmission {
  type: string
  fileName: string
  contentType: string
  fileSize: number
  fileHash: string
  storageId: string
}

export default function Kyc() {
  const auth = useAuth()
  const [kycHistory, setKycHistory] = useState<KycResponse[]>([])
  const [currentKyc, setCurrentKyc] = useState<KycResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])
  const [kycLevel, setKycLevel] = useState('STANDARD')
  const [polling, setPolling] = useState(false)

  React.useEffect(() => {
    attachAuth(auth.user)
    loadKycHistory()
  }, [auth.user])

  const loadKycHistory = async () => {
    setLoading(true)
    try {
      const response = await api.get('/api/v1/kyc/history')
      setKycHistory(response.data)
      // Set the most recent KYC as current if it's pending
      const recent = response.data.find((kyc: KycResponse) => 
        kyc.status === 'PENDING' || kyc.status === 'SUBMITTED' || kyc.status === 'UNDER_REVIEW'
      )
      if (recent) {
        setCurrentKyc(recent)
      }
    } catch (error) {
      console.error('Failed to load KYC history:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files || [])
    setSelectedFiles(files)
  }

  const generateFileHash = async (file: File): Promise<string> => {
    const buffer = await file.arrayBuffer()
    const hashBuffer = await crypto.subtle.digest('SHA-256', buffer)
    const hashArray = Array.from(new Uint8Array(hashBuffer))
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
  }

  const submitKyc = async () => {
    if (selectedFiles.length === 0) {
      alert('Please select at least one document')
      return
    }

    setSubmitting(true)
    try {
      const documents: DocumentSubmission[] = []
      
      for (const file of selectedFiles) {
        const fileHash = await generateFileHash(file)
        const storageId = `doc_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        
        documents.push({
          type: getDocumentType(file.name),
          fileName: file.name,
          contentType: file.type,
          fileSize: file.size,
          fileHash: fileHash,
          storageId: storageId
        })
      }

      const customerId = auth.user?.profile?.sub || 'test-customer-id'
      
      const response = await api.post('/api/v1/kyc/submit', {
        customerId: customerId,
        level: kycLevel,
        documents: documents
      })

      setCurrentKyc(response.data)
      setSelectedFiles([])
      loadKycHistory()
      alert('KYC submitted successfully! You will receive a callback with the result.')
    } catch (error: any) {
      alert('Failed to submit KYC: ' + (error.response?.data?.error || error.message))
    } finally {
      setSubmitting(false)
    }
  }

  const pollStatus = async () => {
    if (!currentKyc) return

    setPolling(true)
    try {
      const response = await api.get(`/api/v1/kyc/poll/${currentKyc.kycCaseId}`)
      setCurrentKyc(response.data)
      
      if (response.data.status === 'APPROVED' || response.data.status === 'REJECTED') {
        loadKycHistory()
      }
    } catch (error) {
      console.error('Failed to poll KYC status:', error)
    } finally {
      setPolling(false)
    }
  }

  const getDocumentType = (fileName: string): string => {
    const name = fileName.toLowerCase()
    if (name.includes('passport')) return 'PASSPORT'
    if (name.includes('license') || name.includes('drivers')) return 'DRIVERS_LICENSE'
    if (name.includes('id') || name.includes('national')) return 'NATIONAL_ID'
    if (name.includes('utility') || name.includes('bill')) return 'UTILITY_BILL'
    if (name.includes('bank') || name.includes('statement')) return 'BANK_STATEMENT'
    if (name.includes('pay') || name.includes('salary')) return 'PAYSLIP'
    if (name.includes('tax')) return 'TAX_DOCUMENT'
    if (name.includes('address')) return 'PROOF_OF_ADDRESS'
    if (name.includes('selfie')) return 'SELFIE'
    if (name.includes('signature')) return 'SIGNATURE'
    return 'NATIONAL_ID' // Default
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'APPROVED': return '#28a745'
      case 'REJECTED': return '#dc3545'
      case 'PENDING': return '#ffc107'
      case 'SUBMITTED': return '#17a2b8'
      case 'UNDER_REVIEW': return '#6f42c1'
      case 'EXPIRED': return '#6c757d'
      default: return '#6c757d'
    }
  }

  const getRequiredDocuments = (level: string) => {
    switch (level) {
      case 'BASIC': return ['National ID']
      case 'STANDARD': return ['National ID', 'Proof of Address']
      case 'ENHANCED': return ['Passport', 'Utility Bill', 'Bank Statement']
      case 'PREMIUM': return ['Passport', 'Utility Bill', 'Bank Statement', 'Payslip', 'Tax Document']
      default: return ['National ID']
    }
  }

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto', padding: 20 }}>
      <h2>KYC Verification</h2>

      {/* Current KYC Status */}
      {currentKyc && (
        <div style={{ 
          border: '1px solid #ddd', 
          borderRadius: 8, 
          padding: 20, 
          marginBottom: 30,
          backgroundColor: '#f8f9fa'
        }}>
          <h3>Current KYC Status</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16, marginBottom: 20 }}>
            <div>
              <strong>Case ID:</strong><br />
              <code>{currentKyc.kycCaseId}</code>
            </div>
            <div>
              <strong>Level:</strong><br />
              {currentKyc.level}
            </div>
            <div>
              <strong>Status:</strong><br />
              <span style={{ 
                color: getStatusColor(currentKyc.status),
                fontWeight: 'bold'
              }}>
                {currentKyc.status}
              </span>
            </div>
            <div>
              <strong>Provider:</strong><br />
              {currentKyc.provider || 'N/A'}
            </div>
            {currentKyc.riskScore && (
              <div>
                <strong>Risk Score:</strong><br />
                {currentKyc.riskScore}
              </div>
            )}
            {currentKyc.rejectionReason && (
              <div style={{ gridColumn: '1 / -1' }}>
                <strong>Rejection Reason:</strong><br />
                <span style={{ color: '#dc3545' }}>{currentKyc.rejectionReason}</span>
              </div>
            )}
          </div>

          <div style={{ display: 'flex', gap: 10 }}>
            <button
              onClick={pollStatus}
              disabled={polling || currentKyc.status === 'APPROVED' || currentKyc.status === 'REJECTED'}
              style={{
                padding: '8px 16px',
                backgroundColor: polling ? '#6c757d' : '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: 4,
                cursor: polling ? 'not-allowed' : 'pointer'
              }}
            >
              {polling ? 'Polling...' : 'Poll Status'}
            </button>
          </div>
        </div>
      )}

      {/* Submit New KYC */}
      <div style={{ border: '1px solid #ddd', borderRadius: 8, padding: 20, marginBottom: 30 }}>
        <h3>Submit KYC Documents</h3>
        
        <div style={{ marginBottom: 20 }}>
          <label style={{ display: 'block', marginBottom: 8 }}>
            <strong>KYC Level:</strong>
          </label>
          <select
            value={kycLevel}
            onChange={(e) => setKycLevel(e.target.value)}
            style={{ padding: 8, border: '1px solid #ccc', borderRadius: 4, width: 200 }}
          >
            <option value="BASIC">Basic</option>
            <option value="STANDARD">Standard</option>
            <option value="ENHANCED">Enhanced</option>
            <option value="PREMIUM">Premium</option>
          </select>
        </div>

        <div style={{ marginBottom: 20 }}>
          <label style={{ display: 'block', marginBottom: 8 }}>
            <strong>Required Documents for {kycLevel}:</strong>
          </label>
          <ul style={{ margin: 0, paddingLeft: 20 }}>
            {getRequiredDocuments(kycLevel).map((doc, index) => (
              <li key={index}>{doc}</li>
            ))}
          </ul>
        </div>

        <div style={{ marginBottom: 20 }}>
          <label style={{ display: 'block', marginBottom: 8 }}>
            <strong>Select Documents:</strong>
          </label>
          <input
            type="file"
            multiple
            accept="image/*,.pdf"
            onChange={handleFileSelect}
            style={{ padding: 8, border: '1px solid #ccc', borderRadius: 4, width: '100%' }}
          />
          <div style={{ fontSize: 12, color: '#666', marginTop: 4 }}>
            Supported formats: JPEG, PNG, PDF. Max size: 10MB per file.
          </div>
        </div>

        {selectedFiles.length > 0 && (
          <div style={{ marginBottom: 20 }}>
            <strong>Selected Files:</strong>
            <ul style={{ margin: 8, paddingLeft: 20 }}>
              {selectedFiles.map((file, index) => (
                <li key={index}>
                  {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)
                </li>
              ))}
            </ul>
          </div>
        )}

        <button
          onClick={submitKyc}
          disabled={submitting || selectedFiles.length === 0}
          style={{
            padding: '12px 24px',
            backgroundColor: submitting ? '#6c757d' : '#28a745',
            color: 'white',
            border: 'none',
            borderRadius: 4,
            cursor: submitting ? 'not-allowed' : 'pointer',
            fontSize: 16
          }}
        >
          {submitting ? 'Submitting...' : 'Submit KYC'}
        </button>
      </div>

      {/* KYC History */}
      <div style={{ border: '1px solid #ddd', borderRadius: 8, padding: 20 }}>
        <h3>KYC History</h3>
        
        {loading ? (
          <div style={{ textAlign: 'center', padding: 20 }}>Loading...</div>
        ) : kycHistory.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 20, color: '#666' }}>
            No KYC submissions found
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead style={{ backgroundColor: '#f8f9fa' }}>
                <tr>
                  <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Case ID</th>
                  <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Level</th>
                  <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Status</th>
                  <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Submitted</th>
                  <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Reviewed</th>
                  <th style={{ padding: 12, textAlign: 'left', borderBottom: '1px solid #ddd' }}>Documents</th>
                </tr>
              </thead>
              <tbody>
                {kycHistory.map((kyc) => (
                  <tr key={kyc.kycCaseId}>
                    <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                      <code style={{ fontSize: 12 }}>{kyc.kycCaseId.substring(0, 8)}...</code>
                    </td>
                    <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>{kyc.level}</td>
                    <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                      <span style={{ 
                        color: getStatusColor(kyc.status),
                        fontWeight: 'bold'
                      }}>
                        {kyc.status}
                      </span>
                    </td>
                    <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                      {kyc.submittedAt ? new Date(kyc.submittedAt).toLocaleDateString() : 'N/A'}
                    </td>
                    <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                      {kyc.reviewedAt ? new Date(kyc.reviewedAt).toLocaleDateString() : 'N/A'}
                    </td>
                    <td style={{ padding: 12, borderBottom: '1px solid #ddd' }}>
                      {kyc.documents.length} documents
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

