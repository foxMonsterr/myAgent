import request from '@/utils/request'
import type { ApiResponse } from '@/types/auth'

export interface DemoFlowStepVO {
  title: string
  description: string
  route: string
  highlight: string
}

export const getDemoFlow = () => request.get<ApiResponse<DemoFlowStepVO[]>>('/api/v1/demo/flow').then(res => res.data)
