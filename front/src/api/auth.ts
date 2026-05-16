import request from '@/utils/request'
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types/auth'

export const login = (data: LoginRequest) => {
  return request.post<AuthResponse>('/api/v1/auth/login', data)
}

export const register = (data: RegisterRequest) => {
  return request.post<AuthResponse>('/api/v1/auth/register', data)
}

export const initAdmin = () => {
  return request.post<AuthResponse>('/api/v1/auth/init-admin')
}

export const sendCaptcha = (data: { phone: string; scene: 'register' | 'forgot' | 'update-phone' }) => {
  return request.post('/api/v1/auth/captcha/send', data)
}

export const logout = () => {
  return request.post('/api/v1/auth/logout')
}
