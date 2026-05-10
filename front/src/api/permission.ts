import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'

export interface PermissionVO {
  role: string
  menus: string[]
  actions: string[]
}

export const getCurrentPermission = () => {
  return request.get<ApiResponse<PermissionVO>>('/api/v1/permission/current').then(res => res.data)
}
