import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { ElMessage } from 'element-plus'
import Layout from '@/layouts/BasicLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue'), meta: { title: '登录', requiresAuth: false } },
    { path: '/register', name: 'Register', component: () => import('@/views/register/index.vue'), meta: { title: '注册', requiresAuth: false } },
    { path: '/init-admin', name: 'InitAdmin', component: () => import('@/views/init-admin/index.vue'), meta: { title: '初始化管理员', requiresAuth: false } },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '仪表盘', requiresAuth: true } },
        { path: 'docs', name: 'Docs', component: () => import('@/views/docs/index.vue'), meta: { title: '接口文档', requiresAuth: true } },
        { path: 'chat', name: 'Chat', component: () => import('@/views/chat/index.vue'), meta: { title: '对话管理', requiresAuth: true } },
        { path: 'agent', name: 'Agent', component: () => import('@/views/agent/index.vue'), meta: { title: 'Agent 管理', requiresAuth: true } },
        { path: 'knowledge', name: 'Knowledge', component: () => import('@/views/knowledge/index.vue'), meta: { title: '知识库', requiresAuth: true } },
        { path: 'planning', name: 'Planning', component: () => import('@/views/planning/index.vue'), meta: { title: '任务规划', requiresAuth: true } },
        { path: 'stream', name: 'Stream', component: () => import('@/views/stream/index.vue'), meta: { title: '流式对话', requiresAuth: true } },
        { path: 'session', name: 'Session', component: () => import('@/views/session/index.vue'), meta: { title: '会话管理', requiresAuth: true } },
        { path: 'monitor', name: 'Monitor', component: () => import('@/views/monitor/index.vue'), meta: { title: '监控面板', requiresAuth: true } },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  const hasToken = Boolean(userStore.token)

  if (to.meta.title) document.title = `${to.meta.title} - AI Agent 管理台`

  if (to.meta.requiresAuth && !hasToken) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  if ((to.path === '/login' || to.path === '/register' || to.path === '/init-admin') && hasToken) {
    next('/dashboard')
    return
  }

  next()
})

export default router
