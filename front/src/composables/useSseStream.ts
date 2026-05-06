import { computed, onBeforeUnmount, ref } from 'vue'
import { useUserStore } from '@/store/modules/user'
import type { StreamChatParams, StreamMessageLog, StreamStatus } from '@/types/stream'

function buildSseUrl(params: StreamChatParams) {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  const modePath = params.mode === 'tools' ? '/chat/tools' : '/chat'
  const query = new URLSearchParams({ message: params.message })
  if (params.conversationId) query.append('conversationId', params.conversationId)
  return `${baseUrl}/v1/stream${modePath}?${query.toString()}`
}

function parseSseChunk(buffer: string) {
  const events = buffer.split(/\n\n|\r\n\r\n/)
  const remain = buffer.endsWith('\n\n') || buffer.endsWith('\r\n\r\n') ? '' : events.pop() || ''
  const payloads: string[] = []

  for (const event of events) {
    const lines = event.split(/\r?\n/)
    for (const line of lines) {
      if (line.startsWith('data:')) {
        payloads.push(line.replace(/^data:\s?/, ''))
      }
    }
  }

  return { payloads, remain }
}

export function useSseStream() {
  const userStore = useUserStore()

  const status = ref<StreamStatus>('idle')
  const answer = ref('')
  const error = ref('')
  const conversationId = ref('')
  const logs = ref<StreamMessageLog[]>([])
  const abortController = ref<AbortController | null>(null)

  const isRunning = computed(() => ['connecting', 'streaming'].includes(status.value))

  const addLog = (type: StreamMessageLog['type'], content: string) => {
    logs.value.unshift({
      id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
      time: new Date().toLocaleString(),
      type,
      content,
    })
  }

  const stop = () => {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
    if (status.value === 'streaming' || status.value === 'connecting') {
      status.value = 'stopped'
      addLog('status', '已主动停止流式连接')
    }
  }

  const clear = () => {
    stop()
    answer.value = ''
    error.value = ''
    logs.value = []
    status.value = 'idle'
  }

  const startFetchStream = async (params: StreamChatParams) => {
    stop()
    answer.value = ''
    error.value = ''
    status.value = 'connecting'

    if (params.conversationId) conversationId.value = params.conversationId

    const url = buildSseUrl(params)
    addLog('user', params.message)
    addLog('status', `开始流式请求: ${params.mode === 'tools' ? 'tools' : 'basic'}`)

    abortController.value = new AbortController()

    try {
      const headers: Record<string, string> = {
        Accept: 'text/event-stream',
      }
      if (userStore.token) {
        headers.Authorization = `Bearer ${userStore.token}`
      }

      const res = await fetch(url, {
        method: 'GET',
        headers,
        signal: abortController.value.signal,
      })

      if (res.status === 401) {
        userStore.clearUser()
        error.value = '未授权，请重新登录'
        status.value = 'error'
        addLog('error', error.value)
        return
      }

      if (!res.ok || !res.body) {
        throw new Error(`请求失败，HTTP ${res.status}`)
      }

      status.value = 'streaming'
      addLog('status', '已建立 SSE 连接')

      const reader = res.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        const parsed = parseSseChunk(buffer)
        buffer = parsed.remain

        for (const chunk of parsed.payloads) {
          const text = chunk.trim()
          if (!text || text === '[DONE]') continue
          answer.value += text
          addLog('assistant', text)
        }
      }

      status.value = 'completed'
      addLog('status', '流式输出完成')
    } catch (err: any) {
      if (err?.name === 'AbortError') {
        status.value = 'stopped'
        addLog('status', '请求已中断')
        return
      }
      status.value = 'error'
      error.value = err?.message || '流式请求失败'
      addLog('error', error.value)
    } finally {
      abortController.value = null
    }
  }

  onBeforeUnmount(() => {
    stop()
  })

  return {
    status,
    answer,
    error,
    conversationId,
    logs,
    isRunning,
    startFetchStream,
    stop,
    clear,
  }
}
