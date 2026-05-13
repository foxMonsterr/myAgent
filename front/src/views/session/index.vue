<template>
  <div class="page-wrap">
    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>会话管理</span>
              <el-tag type="info">Session Debug</el-tag>
            </div>
          </template>

          <el-form :model="form" label-width="110px">
            <el-form-item label="conversationId">
              <el-input v-model="form.conversationId" placeholder="请输入会话ID" clearable />
            </el-form-item>
            <el-form-item>
              <el-space wrap>
                <el-button type="primary" :loading="loading.history" @click="handleLoadHistory">加载历史</el-button>
                <el-button type="danger" plain :loading="loading.clear" @click="handleClearSession">清除会话</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-space>
            </el-form-item>
          </el-form>

          <el-alert v-if="message" :title="message" :type="messageType" show-icon :closable="false" class="mt12" />
        </el-card>

        <el-card shadow="never" class="panel-card mt16">
          <template #header>
            <div class="header">
              <span>会话消息列表</span>
              <el-tag type="success">{{ history.length }} 条</el-tag>
            </div>
          </template>

          <div class="message-list">
            <div v-for="(item, index) in history" :key="index" class="message-item" :class="item.role">
              <div class="message-role">{{ item.role }}</div>
              <div class="message-content">{{ item.content }}</div>
            </div>
            <el-empty v-if="!history.length" description="暂无会话历史" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>监控查询入口</span>
              <el-tag type="warning">/api/v1/monitor</el-tag>
            </div>
          </template>

          <el-form :model="monitorForm" label-width="110px">
            <el-form-item label="username">
              <el-input v-model="monitorForm.username" placeholder="可选，查询用户会话列表/历史" clearable />
            </el-form-item>
            <el-form-item label="pagination">
              <el-space>
                <el-input-number v-model="monitorForm.page" :min="0" />
                <el-input-number v-model="monitorForm.size" :min="1" :max="100" />
              </el-space>
            </el-form-item>
            <el-form-item>
              <el-space wrap>
                <el-button :loading="loading.monitorHistory" @click="handleLoadMonitorHistory">查询历史分页</el-button>
                <el-button :loading="loading.monitorSessions" @click="handleLoadUserSessions">查询用户会话列表</el-button>
                <el-button :loading="loading.monitorConversation" @click="handleLoadConversation">查询完整会话</el-button>
                <el-button type="primary" :loading="loading.stats" @click="handleLoadStats">查询统计</el-button>
              </el-space>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="panel-card mt16">
          <template #header>
            <div class="header">
              <span>监控结果</span>
              <el-button text size="small" @click="result = null">清空结果</el-button>
            </div>
          </template>
          <div class="result-box">
            <template v-if="resultText">
              <pre>{{ resultText }}</pre>
            </template>
            <el-empty v-else description="暂无数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { clearSession, getSessionHistory } from '@/api/session'
import { getMonitorConversation, getMonitorHistory, getMonitorSessions, getMonitorStats } from '@/api/monitor'
import type { SessionHistoryItem } from '@/types/session'

const loading = reactive({
  history: false,
  clear: false,
  stats: false,
  monitorHistory: false,
  monitorSessions: false,
  monitorConversation: false,
})

const form = reactive({ conversationId: '' })
const monitorForm = reactive({ username: '', page: 0, size: 20 })
const history = ref<SessionHistoryItem[]>([])
const result = ref<unknown>(null)
const message = ref('')
const messageType = ref<'success' | 'warning' | 'info' | 'error'>('info')

const resultText = computed(() => {
  if (!result.value) return ''
  return JSON.stringify(result.value, null, 2)
})

const setMessage = (text: string, type: 'success' | 'warning' | 'info' | 'error' = 'info') => {
  message.value = text
  messageType.value = type
}

const handleLoadHistory = async () => {
  if (!form.conversationId.trim()) {
    ElMessage.warning('请输入 conversationId')
    return
  }
  loading.history = true
  try {
    const res = await getSessionHistory(form.conversationId.trim())
    history.value = res || []
    setMessage(`加载到 ${history.value.length} 条会话消息`, 'success')
  } finally {
    loading.history = false
  }
}

const handleClearSession = async () => {
  if (!form.conversationId.trim()) {
    ElMessage.warning('请输入 conversationId')
    return
  }
  loading.clear = true
  try {
    await clearSession(form.conversationId.trim())
    history.value = []
    setMessage('会话已清除', 'success')
  } finally {
    loading.clear = false
  }
}

const handleLoadStats = async () => {
  loading.stats = true
  try {
    result.value = await getMonitorStats()
    setMessage('统计查询完成', 'success')
  } finally {
    loading.stats = false
  }
}

const handleLoadMonitorHistory = async () => {
  loading.monitorHistory = true
  try {
    result.value = await getMonitorHistory({
      username: monitorForm.username.trim() || undefined,
      page: monitorForm.page,
      size: monitorForm.size,
    })
    setMessage('分页历史查询完成', 'success')
  } finally {
    loading.monitorHistory = false
  }
}

const handleLoadUserSessions = async () => {
  if (!monitorForm.username.trim()) {
    ElMessage.warning('请输入 username')
    return
  }
  loading.monitorSessions = true
  try {
    result.value = await getMonitorSessions(monitorForm.username.trim())
    setMessage('用户会话列表查询完成', 'success')
  } finally {
    loading.monitorSessions = false
  }
}

const handleLoadConversation = async () => {
  if (!form.conversationId.trim()) {
    ElMessage.warning('请输入 conversationId')
    return
  }
  loading.monitorConversation = true
  try {
    result.value = await getMonitorConversation(form.conversationId.trim())
    setMessage('完整会话查询完成', 'success')
  } finally {
    loading.monitorConversation = false
  }
}

const handleReset = () => {
  form.conversationId = ''
  history.value = []
  message.value = ''
  result.value = null
}
</script>

<style scoped lang="scss">
.page-wrap { height: 100%; }
.panel-card { border-radius: 12px; }
.mt16 { margin-top: 16px; }
.mt12 { margin-top: 12px; }
.header { display: flex; align-items: center; justify-content: space-between; font-weight: 600; }
.message-list { min-height: 280px; }
.message-item { padding: 12px; border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 12px; background: #fff; }
.message-item.user { border-left: 4px solid #e6a23c; }
.message-item.assistant { border-left: 4px solid #67c23a; }
.message-item.system { border-left: 4px solid #909399; }
.message-role { font-size: 12px; color: #909399; margin-bottom: 6px; }
.message-content { white-space: pre-wrap; word-break: break-word; }
.result-box pre { white-space: pre-wrap; word-break: break-word; background: #fafafa; padding: 12px; border-radius: 8px; }
</style>
