import axios, { AxiosInstance } from 'axios'

export const http = axios.create({
  baseURL: 'http://localhost:8080', // via API Gateway
})

export const api = http

export function attachAuth(instance: AxiosInstance, token: string) {
  instance.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

// Add request interceptor to attach auth token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers = config.headers || {}
    config.headers['Authorization'] = `Bearer ${token}`
  }
  
  const sessionToken = localStorage.getItem('sessionToken')
  if (sessionToken) {
    config.headers = config.headers || {}
    config.headers['X-Session-Token'] = sessionToken
  }
  
  return config
})

// Add response interceptor to handle token refresh
http.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token expired, redirect to login
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('sessionToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
