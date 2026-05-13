import request from '@/utils/request'

export interface ReleaseChecklistVO {
  items: string[]
  status: string
}

export interface ReleaseSummaryVO {
  title: string
  highlights: string[]
  status: string
}

export const getReleaseChecklist = () => request.get('/api/v1/release/checklist')
export const getReleaseSummary = () => request.get('/api/v1/release/summary')
