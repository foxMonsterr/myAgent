export type StreamMode = 'basic' | 'tools'
export type StreamStatus = 'idle' | 'connecting' | 'streaming' | 'completed' | 'stopped' | 'error'

export interface StreamChatParams {
  message: string
  conversationId?: string
  mode?: StreamMode
}

export interface StreamMessageLog {
  id: string
  time: string
  type: 'user' | 'assistant' | 'status' | 'error'
  content: string
}
