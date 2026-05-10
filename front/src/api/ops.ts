import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'

export interface OpsMetricVO {
  title: string
  value: string
  subTitle?: string
  trend?: string
  status?: string
}

export interface TrendPointVO {
  label: string
  value: number
}

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

export interface OpsDashboardVO {
  metrics: OpsMetricVO[]
  dailyChats: TrendPointVO[]
  dailyRuns: TrendPointVO[]
  dailyTokens: TrendPointVO[]
  recentAudits: AuditLogVO[]
}

export const getOpsMetrics = () => request.get<ApiResponse<OpsMetricVO[]>>('/api/v1/ops/metrics').then(res => res.data)
export const getOpsDashboard = () => request.get<ApiResponse<OpsDashboardVO>>('/api/v1/ops/dashboard').then(res => res.data)
