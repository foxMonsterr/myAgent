import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'

export interface ResumeProjectVO {
  projectName: string
  summary: string
  highlights: string[]
  techStack: string[]
  responsibilities: string[]
}

export const getResumeProject = () => request.get<ApiResponse<ResumeProjectVO>>('/api/v1/resume/project').then(res => res.data)
