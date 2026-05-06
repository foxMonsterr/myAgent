import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'
import type { ChatRequest, ChatResponse, StructuredRequest, StructuredResponse } from '@/types/chat'

export const sendSimpleChat = (data: ChatRequest) => {
  return request.post<ApiResponse<ChatResponse>>('/api/v1/chat/simple', data)
}

export const sendMemoryChat = (data: ChatRequest) => {
  const { thinkingMode, ...payload } = data
  return request.post<ApiResponse<ChatResponse>>(
    thinkingMode ? '/api/v1/chat/memory/thinking' : '/api/v1/chat/memory',
    payload,
    thinkingMode ? { params: { thinkingMode } } : undefined,
  )
}

export const sendExpertChat = (data: ChatRequest) => {
  const { thinkingMode, ...payload } = data
  return request.post<ApiResponse<ChatResponse>>(
    thinkingMode ? '/api/v1/chat/expert/thinking' : '/api/v1/chat/expert',
    payload,
    thinkingMode ? { params: { thinkingMode } } : undefined,
  )
}

export const queryPing = () => {
  return request.get<ApiResponse<string>>('/api/v1/chat/ping')
}

export const quickChat = (message: string) => {
  return request.get<ApiResponse<ChatResponse>>('/api/v1/chat/quick', { params: { message } })
}

export const structuredBook = (data: StructuredRequest) => {
  return request.post<ApiResponse<StructuredResponse<any>>>('/api/v1/chat/structured/book', data)
}

export const structuredTask = (data: StructuredRequest) => {
  return request.post<ApiResponse<StructuredResponse<any>>>('/api/v1/chat/structured/task', data)
}

export const structuredSentiment = (data: StructuredRequest) => {
  return request.post<ApiResponse<StructuredResponse<any>>>('/api/v1/chat/structured/sentiment', data)
}
