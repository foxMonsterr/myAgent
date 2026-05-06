<template>
  <div class="page-wrap">
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>监控总览</span>
              <el-space>
                <el-tag type="success">ADMIN 建议访问</el-tag>
                <el-button type="primary" :loading="loading.stats" @click="loadStats">刷新统计</el-button>
              </el-space>
            </div>
          </template>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-card shadow="never" class="stat-card">
                <div class="stat-title">总用户数</div>
                <div class="stat-value">{{ stats.totalUsers ?? '-' }}</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="never" class="stat-card">
                <div class="stat-title">Session 统计</div>
                <pre>{{ formatJson(stats.session) }}</pre>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="never" class="stat-card">
                <div class="stat-title">All Time 统计</div>
                <pre>{{ formatJson(stats.allTime) }}</pre>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :span="24">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>分页历史查询</span>
              <el-space>
                <el-input v-model="query.username" placeholder="username(可选)" clearable />
                <el-input-number v-model="query.page" :min="0" />
                <el-input-number v-model="query.size" :min="1" :max="100" />
                <el-button :loading="loading.history" @click="loadHistory">查询</el-button>
              </el-space>
            </div>
          </template>

          <el-table :data="history.content" stripe border height="420">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="conversationId" label="Conversation ID" width="220" />
            <el-table-column prop="username" label="Username" width="140" />
            <el-table-column prop="messageRole" label="Role" width="110" />
            <el-table-column prop="agentType" label="Agent" width="120" />
            <el-table-column prop="model" label="Model" width="120" />
            <el-table-column prop="content" label="Content" min-width="260" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="Created At" width="180" />
          </el-table>

          <div class="pager">
            <el-pagination
              layout="prev, pager, next, sizes, total"
              :current-page="history.number + 1"
              :page-size="history.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="history.totalElements"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :span="12">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>用户会话列表</span>
              <el-space>
                <el-input v-model="sessionQuery.username" placeholder="username" clearable />
                <el-button :loading="loading.sessions" @click="loadSessions">查询</el-button>
              </el-space>
            </div>
          </template>
          <el-empty v-if="!sessions.length" description="暂无会话列表" />
          <el-space v-else wrap>
            <el-tag v-for="id in sessions" :key="id" type="info">{{ id }}</el-tag>
          </el-space>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>指定会话完整记录</span>
              <el-space>
                <el-input v-model="conversationId" placeholder="conversationId" clearable />
                <el-button :loading="loading.conversation" @click="loadConversation">查询</el-button>
              </el-space>
            </div>
          </template>
          <div class="conversation-list">
            <div v-for="item in conversation" :key="item.id" class="conversation-item">
              <div class="conversation-meta">
                <span>#{{ item.id }}</span>
                <span>{{ item.messageRole }} · {{ item.agentType || '-' }} · {{ item.createdAt || '-' }}</span>
              </div>
              <div class="conversation-content">{{ item.content }}</div>
            </div>
            <el-empty v-if="!conversation.length" description="暂无会话内容" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMonitorConversation, getMonitorHistory, getMonitorSessions, getMonitorStats } from '@/api/monitor'
import type { ChatHistoryEntity } from '@/types/session'

const loading = reactive({ stats: false, history: false, sessions: false, conversation: false })
const stats = reactive<{ session?: Record<string, unknown>; allTime?: Record<string, unknown>; totalUsers?: number }>({})
const query = reactive({ username: '', page: 0, size: 20 })
const sessionQuery = reactive({ username: '' })
const conversationId = ref('')
const history = reactive<{ content: ChatHistoryEntity[]; totalElements: number; totalPages: number; number: number; size: number }>({
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  size: 20,
})
const sessions = ref<string[]>([])
const conversation = ref<ChatHistoryEntity[]>([])

const formatJson = (val: unknown) => JSON.stringify(val ?? {}, null, 2)

const loadStats = async () => {
  loading.stats = true
  try {
    const res: any = await getMonitorStats()
    Object.assign(stats, res || {})
  } finally {
    loading.stats = false
  }
}

const loadHistory = async () => {
  loading.history = true
  try {
    const res: any = await getMonitorHistory({ username: query.username || undefined, page: query.page, size: query.size })
    Object.assign(history, res || { content: [], totalElements: 0, totalPages: 0, number: 0, size: query.size })
  } finally {
    loading.history = false
  }
}

const loadSessions = async () => {
  if (!sessionQuery.username.trim()) {
    ElMessage.warning('请输入 username')
    return
  }
  loading.sessions = true
  try {
    sessions.value = (await getMonitorSessions(sessionQuery.username.trim())) as any
  } finally {
    loading.sessions = false
  }
}

const loadConversation = async () => {
  if (!conversationId.value.trim()) {
    ElMessage.warning('请输入 conversationId')
    return
  }
  loading.conversation = true
  try {
    conversation.value = (await getMonitorConversation(conversationId.value.trim())) as any
  } finally {
    loading.conversation = false
  }
}

const handlePageChange = (page: number) => {
  query.page = page - 1
  loadHistory()
}

const handleSizeChange = (size: number) => {
  query.size = size
  query.page = 0
  loadHistory()
}

onMounted(() => {
  loadStats()
  loadHistory()
})
</script>

<style scoped lang="scss">
.page-wrap { height: 100%; }
.panel-card { border-radius: 12px; }
.mt16 { margin-top: 16px; }
.header { display: flex; align-items: center; justify-content: space-between; font-weight: 600; }
.stat-card pre { white-space: pre-wrap; word-break: break-word; background: #fafafa; padding: 12px; border-radius: 8px; min-height: 120px; }
.stat-title { color: #606266; margin-bottom: 10px; }
.stat-value { font-size: 28px; font-weight: 700; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
.conversation-list { min-height: 260px; }
.conversation-item { padding: 12px; border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 12px; background: #fff; }
.conversation-meta { display: flex; justify-content: space-between; font-size: 12px; color: #909399; margin-bottom: 8px; }
.conversation-content { white-space: pre-wrap; word-break: break-word; }
</style>
