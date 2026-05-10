import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'

export interface ReleaseChecklistVO {
  items: string[]
  status: string
}

export interface ReleaseSummaryVO {
  title: string
  highlights: string[]
  status: string
}

export const getReleaseChecklist = () => request.get<ApiResponse<ReleaseChecklistVO>>('/api/v1/release/checklist').then(res => res.data)
export const getReleaseSummary = () => request.get<ApiResponse<ReleaseSummaryVO>>('/api/v1/release/summary').then(res => res.data)
