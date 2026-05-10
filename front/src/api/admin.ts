import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'

export interface UserVO {
  id: number
  username: string
  nickname?: string
  role: string
  enabled: boolean
  lastLoginAt?: string
  createdAt?: string
}

export const getUserList = () => request.get<ApiResponse<UserVO[]>>('/api/v1/admin/users').then(res => res.data)
export const toggleUserEnabled = (id: number, enabled: boolean) =>
  request.patch<ApiResponse<UserVO>>(`/api/v1/admin/users/${id}/enabled`, null, { params: { enabled } }).then(res => res.data)
export const changeUserRole = (id: number, role: string) =>
  request.patch<ApiResponse<UserVO>>(`/api/v1/admin/users/${id}/role`, null, { params: { role } }).then(res => res.data)
