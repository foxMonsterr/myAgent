import request from '@/utils/request'

export const streamChat = (data: Record<string, unknown>) => {
  return request.post('/api/v1/stream', data, {
    responseType: 'stream',
  })
}
