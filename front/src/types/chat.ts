export type ChatMode = 'simple' | 'memory' | 'expert' | 'structured-book' | 'structured-task' | 'structured-sentiment'

export interface ChatRequest {
  conversationId?: string
  message: string
  model?: string
  role?: string
  level?: string
  thinkingMode?: boolean
}

export interface StructuredRequest {
  input: string
}

export interface ChatResponse {
  conversationId?: string
  reply?: string
  thinking?: string
  model?: string
  historySize?: number
  tokenUsage?: {
    promptTokens?: number
    completionTokens?: number
    totalTokens?: number
  }
}

export interface StructuredResponse<T> {
  result: T
  outputType: string
  originalInput: string
}
