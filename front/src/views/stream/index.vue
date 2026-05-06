<template>
  <div class="stream-page">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">
              <span>SSE 流式对话调试台</span>
              <el-tag :type="statusTagType">{{ statusText }}</el-tag>
            </div>
          </template>

          <el-form :model="form" label-width="110px">
            <el-form-item label="conversationId">
              <el-input v-model="form.conversationId" placeholder="可选，留空则由后端创建或使用默认会话" clearable />
            </el-form-item>

            <el-form-item label="流式模式">
              <el-radio-group v-model="form.mode">
                <el-radio-button label="basic">基础对话</el-radio-button>
                <el-radio-button label="tools">工具调用</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="消息内容">
              <el-input
                v-model="form.message"
                type="textarea"
                :rows="5"
                maxlength="2000"
                show-word-limit
                placeholder="请输入要发送给后端流式接口的消息"
              />
            </el-form-item>

            <el-form-item>
              <el-space wrap>
                <el-button type="primary" :loading="isRunning" @click="handleSend">发送消息</el-button>
                <el-button :disabled="!isRunning" @click="handleStop">停止</el-button>
                <el-button @click="handleClear">清空</el-button>
                <el-button type="success" plain @click="handleRestart">重新开始</el-button>
              </el-space>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="panel-card mt16">
          <template #header>
            <div class="card-header">
              <span>实时输出</span>
              <el-tag type="info">{{ conversationId || '未设置 conversationId' }}</el-tag>
            </div>
          </template>

          <div class="answer-box">
            <div v-if="answer" class="answer-text">{{ answer }}</div>
            <el-empty v-else description="等待后端 SSE 输出" />
          </div>

          <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="mt12" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="panel-card log-card">
          <template #header>
            <div class="card-header">
              <span>日志面板</span>
              <el-button text size="small" @click="clearLogs">清空日志</el-button>
            </div>
          </template>

          <div class="log-list">
            <div v-for="item in logs" :key="item.id" class="log-item" :class="item.type">
              <div class="log-meta">
                <span>{{ item.time }}</span>
                <el-tag size="small" :type="logTagType(item.type)">{{ item.type }}</el-tag>
              </div>
              <div class="log-content">{{ item.content }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useSseStream } from '@/composables/useSseStream'
import type { StreamMode } from '@/types/stream'

const { status, answer, error, conversationId, logs, isRunning, startFetchStream, stop, clear } = useSseStream()

const form = reactive({
  message: '',
  conversationId: '',
  mode: 'basic' as StreamMode,
})

const statusText = computed(() => {
  const map: Record<string, string> = {
    idle: '空闲',
    connecting: '连接中',
    streaming: '输出中',
    completed: '已完成',
    stopped: '已停止',
    error: '错误',
  }
  return map[status.value] || '未知'
})

const statusTagType = computed(() => {
  const map: Record<string, 'info' | 'warning' | 'success' | 'danger'> = {
    idle: 'info',
    connecting: 'warning',
    streaming: 'success',
    completed: 'success',
    stopped: 'info',
    error: 'danger',
  }
  return map[status.value] || 'info'
})

const logTagType = (type: string) => {
  if (type === 'error') return 'danger'
  if (type === 'assistant') return 'success'
  if (type === 'user') return 'warning'
  return 'info'
}

const handleSend = async () => {
  if (!form.message.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  await startFetchStream({
    message: form.message.trim(),
    conversationId: form.conversationId.trim() || undefined,
    mode: form.mode,
  })
}

const handleStop = () => stop()
const handleClear = () => clear()
const clearLogs = () => {
  clear()
  form.message = ''
}
const handleRestart = () => {
  stop()
  answer.value = ''
  error.value = ''
  form.message = ''
}
</script>

<style scoped lang="scss">
.stream-page {
  height: 100%;
}

.panel-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.mt16 {
  margin-top: 16px;
}

.mt12 {
  margin-top: 12px;
}

.answer-box {
  min-height: 280px;
  padding: 16px;
  background: #fafcff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

.answer-text {
  line-height: 1.8;
  font-size: 14px;
}

.log-card {
  height: 100%;
}

.log-list {
  max-height: 760px;
  overflow: auto;
}

.log-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #fff;
}

.log-item.user {
  border-left: 4px solid #e6a23c;
}

.log-item.assistant {
  border-left: 4px solid #67c23a;
}

.log-item.status {
  border-left: 4px solid #909399;
}

.log-item.error {
  border-left: 4px solid #f56c6c;
}

.log-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.log-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}
</style>
