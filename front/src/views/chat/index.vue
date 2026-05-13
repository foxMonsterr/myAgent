<template>
  <div class="page-wrap">
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>Chat 联调台</span>
              <el-tag :type="statusTagType">{{ statusText }}</el-tag>
            </div>
          </template>

          <el-form :model="form" label-width="110px">
            <el-form-item label="conversationId">
              <el-input v-model="form.conversationId" placeholder="可选，用于多轮对话隔离" clearable />
            </el-form-item>
            <el-form-item label="模式">
              <el-radio-group v-model="form.mode">
                <el-radio-button label="simple">简单对话</el-radio-button>
                <el-radio-button label="memory">记忆对话</el-radio-button>
                <el-radio-button label="expert">专家对话</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="思考模式">
              <el-switch v-model="form.thinkingMode" active-text="开启" inactive-text="关闭" />
            </el-form-item>
            <template v-if="form.mode === 'expert'">
              <el-form-item label="role">
                <el-input v-model="form.role" placeholder="如：Java、前端、数据库" />
              </el-form-item>
              <el-form-item label="level">
                <el-select v-model="form.level" placeholder="请选择水平">
                  <el-option label="初学者" value="beginner" />
                  <el-option label="中级" value="intermediate" />
                  <el-option label="高级" value="advanced" />
                </el-select>
              </el-form-item>
            </template>
            <el-form-item label="消息内容">
              <el-input v-model="form.message" type="textarea" :rows="6" placeholder="请输入消息" />
            </el-form-item>
            <el-form-item>
              <el-space wrap>
                <el-button type="primary" :loading="loading" @click="handleSend">发送</el-button>
                <el-button @click="handleReset">重置</el-button>
                <el-button @click="handleQuickPing">Ping</el-button>
              </el-space>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="panel-card mt16">
          <template #header>
            <span>响应结果</span>
          </template>

          <div class="response-box">
            <template v-if="response">
              <p><b>conversationId:</b> {{ response.conversationId || '-' }}</p>
              <p><b>traceId:</b> {{ response.traceId || '-' }}</p>
              <p><b>code:</b> {{ response.code ?? '-' }}</p>
              <p><b>message:</b> {{ response.message || '-' }}</p>
              <p><b>reply:</b></p>
              <pre>{{ response.reply || '-' }}</pre>
              <template v-if="response.thinking">
                <p><b>thinking:</b></p>
                <pre>{{ response.thinking }}</pre>
              </template>
              <p><b>model:</b> {{ response.model || '-' }}</p>
              <p><b>historySize:</b> {{ response.historySize ?? '-' }}</p>
              <template v-if="response.tokenUsage">
                <p><b>tokenUsage:</b> {{ response.tokenUsage }}</p>
              </template>
            </template>
            <el-empty v-else description="暂无数据" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>请求日志</span>
              <el-button text size="small" @click="logs = []">清空日志</el-button>
            </div>
          </template>

          <div class="log-list">
            <div v-for="(item, index) in logs" :key="index" class="log-item" :class="item.type">
              <div class="log-meta">
                <span>{{ item.time }}</span>
                <el-tag size="small" :type="tagType(item.type)">{{ item.type }}</el-tag>
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
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { sendExpertChat, sendMemoryChat, sendSimpleChat, quickChat, queryPing } from '@/api/chat'
import type { ChatMode, ChatResponse } from '@/types/chat'

const loading = ref(false)
const response = ref<ChatResponse | null>(null)
const logs = ref<Array<{ type: 'request' | 'success' | 'error' | 'info'; time: string; content: string }>>([])

const form = reactive({
  conversationId: '',
  message: '',
  mode: 'simple' as ChatMode,
  thinkingMode: false,
  role: 'Java',
  level: 'intermediate',
})

const addLog = (type: 'request' | 'success' | 'error' | 'info', content: string) => {
  logs.value.unshift({ type, time: new Date().toLocaleString(), content })
}

const statusText = computed(() => (loading.value ? '请求中' : '空闲'))
const statusTagType = computed(() => (loading.value ? 'warning' : 'success'))

const tagType = (type: string) => {
  if (type === 'error') return 'danger'
  if (type === 'success') return 'success'
  if (type === 'request') return 'warning'
  return 'info'
}

const handleSend = async () => {
  if (!form.message.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  loading.value = true
  response.value = null
  try {
    addLog('request', `mode=${form.mode}, conversationId=${form.conversationId || '-'}`)
    const payload = {
      conversationId: form.conversationId.trim() || undefined,
      message: form.message.trim(),
      role: form.mode === 'expert' ? form.role : undefined,
      level: form.mode === 'expert' ? form.level : undefined,
      thinkingMode: form.thinkingMode,
    }

    if (form.mode === 'memory') response.value = await sendMemoryChat(payload)
    else if (form.mode === 'expert') response.value = await sendExpertChat(payload)
    else response.value = await sendSimpleChat(payload)

    addLog('success', `请求成功 traceId=${response.value?.traceId || '-'}`)
  } catch (e: any) {
    addLog('error', e?.message || '请求失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  form.conversationId = ''
  form.message = ''
  form.mode = 'simple'
  form.thinkingMode = false
  form.role = 'Java'
  form.level = 'intermediate'
  response.value = null
}

const handleQuickPing = async () => {
  try {
    const res = await queryPing()
    ElMessage.success(res || 'pong')
    addLog('info', 'Ping 成功')
  } catch (e: any) {
    addLog('error', e?.message || 'Ping 失败')
  }
}
</script>

<style scoped lang="scss">
.page-wrap { height: 100%; }
.panel-card { border-radius: 12px; }
.mt16 { margin-top: 16px; }
.header { display: flex; align-items: center; justify-content: space-between; font-weight: 600; }
.response-box, .log-list { min-height: 280px; }
.response-box pre { white-space: pre-wrap; word-break: break-word; background: #fafafa; padding: 12px; border-radius: 8px; }
.log-list { max-height: 760px; overflow: auto; }
.log-item { padding: 12px; border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 12px; background: #fff; }
.log-meta { display: flex; justify-content: space-between; color: #909399; font-size: 12px; margin-bottom: 8px; }
.log-item.request { border-left: 4px solid #e6a23c; }
.log-item.success { border-left: 4px solid #67c23a; }
.log-item.error { border-left: 4px solid #f56c6c; }
.log-item.info { border-left: 4px solid #909399; }
</style>
