import request from '@/utils/request'

export interface ResumeProjectVO {
  projectName: string
  summary: string
  highlights: string[]
  techStack: string[]
  responsibilities: string[]
}

export const getResumeProject = () => request.get('/api/v1/resume/project')
