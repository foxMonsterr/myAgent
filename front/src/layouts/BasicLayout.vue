<template>
  <el-container class="layout-shell">
    <el-aside class="layout-aside" width="230px">
      <div class="brand">
        <div class="brand-logo">AI</div>
        <div class="brand-text">
          <div class="brand-title">AI Agent 管理台</div>
          <div class="brand-subtitle">Spring Boot Debug Console</div>
        </div>
      </div>

      <el-menu router :default-active="$route.path" class="side-menu" :collapse-transition="false">
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/docs">接口文档</el-menu-item>
        <el-menu-item index="/chat">对话管理</el-menu-item>
        <el-menu-item index="/agent">Agent 管理</el-menu-item>
        <el-menu-item index="/knowledge">知识库</el-menu-item>
        <el-menu-item index="/planning">任务规划</el-menu-item>
        <el-menu-item index="/stream">流式对话</el-menu-item>
        <el-menu-item index="/session">会话管理</el-menu-item>
        <el-menu-item index="/monitor">监控面板</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <div class="page-title">{{ pageTitle }}</div>
          <div class="page-desc">用于联调 AI Agent 后端接口</div>
        </div>

        <div class="header-right">
          <el-tag type="info" effect="plain">{{ userStore.role || 'GUEST' }}</el-tag>
          <el-dropdown trigger="click">
            <span class="user-chip">
              {{ userStore.nickname || userStore.username || '未登录' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ userStore.username || '未登录' }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const pageTitle = computed(() => (route.meta.title as string) || 'AI Agent 管理台')

const handleLogout = async () => {
  await ElMessageBox.confirm('确认退出登录吗？', '提示', { type: 'warning' })
  userStore.clearUser()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout-shell { height: 100%; background: #f5f7fb; }
.layout-aside { background: #fff; border-right: 1px solid #ebeef5; }
.brand { height: 72px; display: flex; align-items: center; gap: 12px; padding: 0 16px; border-bottom: 1px solid #ebeef5; }
.brand-logo { width: 40px; height: 40px; border-radius: 10px; background: linear-gradient(135deg, #409eff, #67c23a); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; }
.brand-title { font-weight: 700; color: #1f2937; }
.brand-subtitle { font-size: 12px; color: #909399; margin-top: 2px; }
.side-menu { border-right: none; }
.layout-header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #ebeef5; padding: 0 20px; }
.page-title { font-size: 16px; font-weight: 700; color: #111827; }
.page-desc { font-size: 12px; color: #909399; margin-top: 2px; }
.header-left, .header-right { display: flex; align-items: center; gap: 12px; }
.user-chip { display: inline-flex; align-items: center; gap: 6px; color: #374151; cursor: pointer; }
.layout-main { padding: 20px; }
</style>
