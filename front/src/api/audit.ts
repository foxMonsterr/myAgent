import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'

export interface AuditLogVO {
  traceId?: string
  conversationId?: string
  agentType?: string
  model?: string
  input?: string
  output?: string
  thinking?: string
  status?: string
  latencyMs?: number
  createdAt?: string
}

export const getAuditLogs = (conversationId?: string) =>
  request.get<ApiResponse<AuditLogVO[]>>('/api/v1/audit/logs', { params: conversationId ? { conversationId } : {} }).then(res => res.data)
