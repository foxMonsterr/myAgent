import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'
import type { AgentRequest, AgentResponse } from '@/types/agent'

export const sendAgentChat = (data: AgentRequest) => {
  return request.post<ApiResponse<AgentResponse>>('/api/v1/agent/chat', data)
}

export const sendAgentChatWithMemory = (data: AgentRequest) => {
  return request.post<ApiResponse<AgentResponse>>('/api/v1/agent/chat/memory', data)
}

export const sendAgentChatWithSpecificTools = (data: AgentRequest) => {
  return request.post<ApiResponse<AgentResponse>>('/api/v1/agent/chat/specific', data)
}
