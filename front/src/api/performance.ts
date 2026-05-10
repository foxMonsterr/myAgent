import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'

export interface PerformanceSummaryVO {
  requestSuccessRate: number
  agentSuccessRate: number
  ragHitRate: number
  toolSuccessRate: number
  modelSuccessRate: number
  errorRate: number
  avgLatencyMs: number
}

export const getPerformanceSummary = () =>
  request.get<ApiResponse<PerformanceSummaryVO>>('/api/v1/performance/summary').then(res => res.data)
