<template>
  <div class="page-wrap">
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>Planning 联调台</span>
              <el-tag type="info">任务规划 / 全能 Agent</el-tag>
            </div>
          </template>

          <el-form :model="form" label-width="110px">
            <el-form-item label="conversationId">
              <el-input v-model="form.conversationId" placeholder="可选，用于多轮规划" clearable />
            </el-form-item>
            <el-form-item label="autoExecute">
              <el-switch v-model="form.autoExecute" active-text="自动执行" inactive-text="仅规划" />
            </el-form-item>
            <el-form-item label="任务内容">
              <el-input v-model="form.task" type="textarea" :rows="7" placeholder="请输入复杂任务，如：读取文档并总结" />
            </el-form-item>
            <el-form-item>
              <el-space wrap>
                <el-button type="primary" :loading="loading.execute" @click="handleExecute">规划并执行</el-button>
                <el-button :loading="loading.execute" @click="handlePlanOnly">仅规划</el-button>
                <el-button type="success" plain :loading="loading.chat" @click="handleChat">全能 Agent 对话</el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-space>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="panel-card mt16">
          <template #header>
            <span>规划 / 执行结果</span>
          </template>
          <div class="response-box">
            <template v-if="resultText">
              <pre>{{ resultText }}</pre>
            </template>
            <el-empty v-else description="暂无结果" />
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
import { planAndExecute, planOnly, planningChat } from '@/api/planning'
import type { PlanningResponse } from '@/types/planning'
import type { AgentResponse } from '@/types/agent'

const loading = reactive({ execute: false, chat: false })
const logs = ref<Array<{ type: 'request' | 'success' | 'error' | 'info'; time: string; content: string }>>([])
const result = ref<PlanningResponse | AgentResponse | string | null>(null)

const form = reactive({
  conversationId: '',
  task: '',
  autoExecute: true,
})

const addLog = (type: 'request' | 'success' | 'error' | 'info', content: string) => {
  logs.value.unshift({ type, time: new Date().toLocaleString(), content })
}

const tagType = (type: string) => {
  if (type === 'error') return 'danger'
  if (type === 'success') return 'success'
  if (type === 'request') return 'warning'
  return 'info'
}

const resultText = computed(() => {
  if (!result.value) return ''
  return typeof result.value === 'string' ? result.value : JSON.stringify(result.value, null, 2)
})

const validateTask = () => {
  if (!form.task.trim()) {
    ElMessage.warning('请输入任务内容')
    return false
  }
  return true
}

const handleExecute = async () => {
  if (!validateTask()) return
  loading.execute = true
  try {
    addLog('request', '执行任务规划并执行')
    const res: any = await planAndExecute({
      task: form.task.trim(),
      conversationId: form.conversationId.trim() || undefined,
      autoExecute: form.autoExecute,
    })
    result.value = res
    addLog('success', '执行成功')
  } catch (e: any) {
    addLog('error', e?.message || '执行失败')
  } finally {
    loading.execute = false
  }
}

const handlePlanOnly = async () => {
  if (!validateTask()) return
  loading.execute = true
  try {
    addLog('request', '仅执行规划')
    const res: any = await planOnly({
      task: form.task.trim(),
      conversationId: form.conversationId.trim() || undefined,
      autoExecute: false,
    })
    result.value = res
    addLog('success', '规划成功')
  } catch (e: any) {
    addLog('error', e?.message || '规划失败')
  } finally {
    loading.execute = false
  }
}

const handleChat = async () => {
  if (!validateTask()) return
  loading.chat = true
  try {
    addLog('request', '调用全能 Agent 入口')
    const res: any = await planningChat({
      message: form.task.trim(),
      conversationId: form.conversationId.trim() || undefined,
    })
    result.value = res
    addLog('success', 'Agent 调用成功')
  } catch (e: any) {
    addLog('error', e?.message || 'Agent 调用失败')
  } finally {
    loading.chat = false
  }
}

const handleReset = () => {
  form.conversationId = ''
  form.task = ''
  form.autoExecute = true
  result.value = null
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
