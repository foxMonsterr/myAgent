import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'
import type { ChatHistoryEntity } from '@/types/session'
import type { MonitorStats } from '@/types/monitor'

export const getMonitorStats = () => {
  return request.get<ApiResponse<MonitorStats>>('/api/v1/monitor/stats')
}

export const getMonitorHistory = (params?: { username?: string; page?: number; size?: number }) => {
  return request.get<ApiResponse<{ content: ChatHistoryEntity[]; totalElements: number; totalPages: number; number: number; size: number }>>(
    '/api/v1/monitor/history',
    { params },
  )
}

export const getMonitorConversation = (conversationId: string) => {
  return request.get<ApiResponse<ChatHistoryEntity[]>>(`/api/v1/monitor/conversation/${encodeURIComponent(conversationId)}`)
}

export const getMonitorSessions = (username: string) => {
  return request.get<ApiResponse<string[]>>(`/api/v1/monitor/sessions/${encodeURIComponent(username)}`)
}
