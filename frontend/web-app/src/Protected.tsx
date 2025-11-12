import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

interface User {
  id: string;
  email: string;
  fullName: string;
  roles: string[];
  permissions: string[];
}

export function Protected({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate()
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const checkAuth = () => {
      const accessToken = localStorage.getItem('accessToken')
      const userData = localStorage.getItem('user')
      
      if (!accessToken || !userData) {
        navigate('/login')
        return
      }

      try {
        const user = JSON.parse(userData)
        setUser(user)
      } catch (error) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('sessionToken')
        localStorage.removeItem('user')
        navigate('/login')
      } finally {
        setLoading(false)
      }
    }

    checkAuth()
  }, [navigate])

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
          <p className="mt-2 text-gray-600">Loading...</p>
        </div>
      </div>
    )
  }

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Redirecting to login...</p>
      </div>
    )
  }

  return <>{children}</>
}
