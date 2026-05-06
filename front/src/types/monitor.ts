import type { ChatHistoryEntity } from './session'

export interface MonitorStats {
  session?: Record<string, unknown>
  allTime?: Record<string, unknown>
  totalUsers?: number
}

export interface MonitorHistoryPage {
  content: ChatHistoryEntity[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
