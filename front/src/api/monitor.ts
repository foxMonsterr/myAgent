import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'

export const getMonitorOverview = () => {
  return request.get<ApiResponse<any>>('/api/v1/monitor/overview').then(res => res.data)
}

export const getMonitorStats = () => {
  return request.get<ApiResponse<any>>('/api/v1/monitor/stats').then(res => res.data)
}
