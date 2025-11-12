import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { http } from '../api/http'

interface User {
  id: string;
  email: string;
  fullName: string;
  roles: string[];
  permissions: string[];
}

export function LoginLogout() {
  const navigate = useNavigate()
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const checkAuth = () => {
      const accessToken = localStorage.getItem('accessToken')
      const userData = localStorage.getItem('user')
      
      if (accessToken && userData) {
        try {
          const user = JSON.parse(userData)
          setUser(user)
        } catch (error) {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('sessionToken')
          localStorage.removeItem('user')
        }
      }
      setLoading(false)
    }

    checkAuth()
  }, [])

  const handleLogout = async () => {
    try {
      const sessionToken = localStorage.getItem('sessionToken')
      if (sessionToken) {
        await http.post('/api/v1/auth/logout', null, {
          headers: { 'X-Session-Token': sessionToken }
        })
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('sessionToken')
      localStorage.removeItem('user')
      setUser(null)
      navigate('/login')
    }
  }

  if (loading) {
    return <button disabled>Loading...</button>
  }

  if (!user) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <button 
          onClick={() => navigate('/login')}
          style={{
            padding: '8px 16px',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: 4,
            cursor: 'pointer'
          }}
        >
          Login
        </button>
        <button 
          onClick={() => navigate('/register')}
          style={{
            padding: '8px 16px',
            backgroundColor: '#28a745',
            color: 'white',
            border: 'none',
            borderRadius: 4,
            cursor: 'pointer'
          }}
        >
          Sign Up
        </button>
      </div>
    )
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
      <span>Hi, {user.fullName}</span>
      <button 
        onClick={handleLogout}
        style={{
          padding: '8px 16px',
          backgroundColor: '#dc3545',
          color: 'white',
          border: 'none',
          borderRadius: 4,
          cursor: 'pointer'
        }}
      >
        Logout
      </button>
    </div>
  )
}
