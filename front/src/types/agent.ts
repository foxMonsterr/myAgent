export type AgentMode = 'chat' | 'memory' | 'specific'

export interface AgentRequest {
  conversationId?: string
  message: string
  tools?: string[]
}

export interface AgentResponse {
  conversationId?: string
  reply?: string
  model?: string
  agentType?: string
}
