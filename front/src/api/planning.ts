import request from '@/utils/request'
import type { AgentRequest, AgentResponse } from '@/types/agent'
import type { PlanningRequest, PlanningResponse } from '@/types/planning'

export const planAndExecute = (data: PlanningRequest) => {
  return request.post('/api/v1/planning/execute', data) as Promise<PlanningResponse>
}

export const planOnly = (data: PlanningRequest) => {
  return request.post('/api/v1/planning/plan-only', data) as Promise<PlanningResponse>
}

export const planningChat = (data: AgentRequest) => {
  return request.post('/api/v1/planning/chat', data) as Promise<AgentResponse>
}
