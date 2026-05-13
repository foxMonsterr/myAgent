import request from '@/utils/request'
import type { KnowledgeRequest } from '@/types/knowledge'

export const uploadDocument = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/v1/knowledge/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const loadDirectory = (path?: string) => {
  return request.post('/api/v1/knowledge/load-directory', null, {
    params: { path },
  })
}

export const listDocuments = () => {
  return request.get('/api/v1/knowledge/documents')
}

export const deleteDocument = (fileName: string) => {
  return request.delete(`/api/v1/knowledge/documents/${encodeURIComponent(fileName)}`)
}

export const askKnowledge = (data: KnowledgeRequest) => {
  return request.post('/api/v1/knowledge/ask', data)
}

export const askKnowledgeManual = (data: KnowledgeRequest) => {
  return request.post('/api/v1/knowledge/ask/manual', data)
}

export const searchKnowledge = (query: string) => {
  return request.get('/api/v1/knowledge/search', { params: { query } })
}

export const getKnowledgeStatus = () => {
  return request.get('/api/v1/knowledge/status')
}
