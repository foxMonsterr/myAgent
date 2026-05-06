import request from '@/utils/request'
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '@/types/auth'

export const login = (data: LoginRequest) => {
  return request.post<ApiResponse<AuthResponse>>('/api/v1/auth/login', data)
}

export const register = (data: RegisterRequest) => {
  return request.post<ApiResponse<AuthResponse>>('/api/v1/auth/register', data)
}

export const initAdmin = () => {
  return request.post<ApiResponse<AuthResponse>>('/api/v1/auth/init-admin')
}
