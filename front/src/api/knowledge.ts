import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'
import type { KnowledgeDocumentVO, KnowledgeRequest, KnowledgeResponse, KnowledgeStatusResponse } from '@/types/knowledge'

export const uploadDocument = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<KnowledgeDocumentVO>>('/api/v1/knowledge/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const loadDirectory = (path?: string) => {
  return request.post<ApiResponse<KnowledgeDocumentVO[]>>('/api/v1/knowledge/load-directory', null, {
    params: { path },
  })
}

export const listDocuments = () => {
  return request.get<ApiResponse<KnowledgeDocumentVO[]>>('/api/v1/knowledge/documents')
}

export const deleteDocument = (fileName: string) => {
  return request.delete<ApiResponse<string>>(`/api/v1/knowledge/documents/${encodeURIComponent(fileName)}`)
}

export const askKnowledge = (data: KnowledgeRequest) => {
  return request.post<ApiResponse<KnowledgeResponse>>('/api/v1/knowledge/ask', data)
}

export const askKnowledgeManual = (data: KnowledgeRequest) => {
  return request.post<ApiResponse<KnowledgeResponse>>('/api/v1/knowledge/ask/manual', data)
}

export const searchKnowledge = (query: string) => {
  return request.get<ApiResponse<string>>('/api/v1/knowledge/search', { params: { query } })
}

export const getKnowledgeStatus = () => {
  return request.get<ApiResponse<KnowledgeStatusResponse>>('/api/v1/knowledge/status')
}
