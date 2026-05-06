import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'
import type { AgentRequest, AgentResponse } from '@/types/agent'
import type { PlanningRequest, PlanningResponse } from '@/types/planning'

export const planAndExecute = (data: PlanningRequest) => {
  return request.post<ApiResponse<PlanningResponse>>('/api/v1/planning/execute', data)
}

export const planOnly = (data: PlanningRequest) => {
  return request.post<ApiResponse<PlanningResponse>>('/api/v1/planning/plan-only', data)
}

export const planningChat = (data: AgentRequest) => {
  return request.post<ApiResponse<AgentResponse>>('/api/v1/planning/chat', data)
}
