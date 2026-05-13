import request from '@/utils/request'
import type { ChatRequest, ChatResponse, StructuredRequest, StructuredResponse } from '@/types/chat'

export const sendSimpleChat = (data: ChatRequest) => {
  return request.post('/api/v1/chat/simple', data) as Promise<ChatResponse>
}

export const sendMemoryChat = (data: ChatRequest) => {
  const { thinkingMode, ...payload } = data
  return request.post(
    thinkingMode ? '/api/v1/chat/memory/thinking' : '/api/v1/chat/memory',
    payload,
    thinkingMode ? { params: { thinkingMode } } : undefined,
  ) as Promise<ChatResponse>
}

export const sendExpertChat = (data: ChatRequest) => {
  const { thinkingMode, ...payload } = data
  return request.post(
    thinkingMode ? '/api/v1/chat/expert/thinking' : '/api/v1/chat/expert',
    payload,
    thinkingMode ? { params: { thinkingMode } } : undefined,
  ) as Promise<ChatResponse>
}

export const queryPing = () => {
  return request.get('/api/v1/chat/ping') as Promise<string>
}

export const quickChat = (message: string) => {
  return request.get('/api/v1/chat/quick', { params: { message } }) as Promise<ChatResponse>
}

export const structuredBook = (data: StructuredRequest) => {
  return request.post('/api/v1/chat/structured/book', data) as Promise<StructuredResponse<any>>
}

export const structuredTask = (data: StructuredRequest) => {
  return request.post('/api/v1/chat/structured/task', data) as Promise<StructuredResponse<any>>
}

export const structuredSentiment = (data: StructuredRequest) => {
  return request.post('/api/v1/chat/structured/sentiment', data) as Promise<StructuredResponse<any>>
}
