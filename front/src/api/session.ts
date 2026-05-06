import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'
import type { SessionHistoryItem } from '@/types/session'

export const getSessionHistory = (conversationId: string) => {
  return request.get<ApiResponse<SessionHistoryItem[]>>(`/api/v1/session/${encodeURIComponent(conversationId)}/history`)
}

export const clearSession = (conversationId: string) => {
  return request.delete<ApiResponse<string>>(`/api/v1/session/${encodeURIComponent(conversationId)}`)
}
