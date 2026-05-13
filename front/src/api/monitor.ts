import request from '@/utils/request'

export const getMonitorOverview = () => {
  return request.get('/api/v1/monitor/overview')
}

export const getMonitorStats = () => {
  return request.get('/api/v1/monitor/stats')
}

export const getMonitorHistory = (params: { username?: string; page?: number; size?: number }) => {
  return request.get('/api/v1/monitor/history', { params })
}

export const getMonitorConversation = (conversationId: string) => {
  return request.get(`/api/v1/monitor/conversation/${encodeURIComponent(conversationId)}`)
}

export const getMonitorSessions = (username: string) => {
  return request.get(`/api/v1/monitor/sessions/${encodeURIComponent(username)}`)
}
