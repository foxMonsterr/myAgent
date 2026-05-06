export interface PlanningRequest {
  task: string
  conversationId?: string
  autoExecute?: boolean
}

export interface PlanningResponse {
  conversationId?: string
  task?: string
  plan?: string[]
  result?: string
  executed?: boolean
  model?: string
}
