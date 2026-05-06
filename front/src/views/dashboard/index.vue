<template>
  <div class="dashboard-page">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in cards" :key="item.title">
        <el-card shadow="never" class="stat-card">
          <div class="stat-title">{{ item.title }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :span="16">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">模块快捷入口</div>
          </template>
          <div class="module-grid">
            <el-button v-for="item in modules" :key="item.path" class="module-btn" @click="go(item.path)">{{ item.label }}</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">使用说明</div>
          </template>
          <el-steps direction="vertical" :active="3">
            <el-step title="登录" description="使用认证接口获取 JWT" />
            <el-step title="选择模块" description="进入对话、知识库、流式等页面" />
            <el-step title="调试接口" description="页面已对齐后端 Spring Boot 接口" />
          </el-steps>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const cards = [
  { title: '认证状态', value: 'JWT Ready', desc: '登录后自动携带 Token' },
  { title: '接口模块', value: '8', desc: '已拆分统一 API' },
  { title: '调试页面', value: '9', desc: '全部可联调' },
  { title: '流式支持', value: 'SSE', desc: '支持实时输出' },
]

const modules = [
  { label: '对话管理', path: '/chat' },
  { label: 'Agent 管理', path: '/agent' },
  { label: '知识库', path: '/knowledge' },
  { label: '任务规划', path: '/planning' },
  { label: '流式对话', path: '/stream' },
  { label: '会话管理', path: '/session' },
  { label: '监控面板', path: '/monitor' },
]

const go = (path: string) => router.push(path)
</script>

<style scoped lang="scss">
.dashboard-page {
  width: 100%;
}

.stat-card,
.panel-card {
  border-radius: 12px;
}

.stat-card {
  min-height: 140px;
}

.stat-title {
  color: #6b7280;
  font-size: 14px;
}

.stat-value {
  margin-top: 12px;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.stat-desc {
  margin-top: 8px;
  color: #9ca3af;
}

.mt16 {
  margin-top: 16px;
}

.card-header {
  font-weight: 600;
}

.module-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.module-btn {
  min-width: 120px;
}
</style>
