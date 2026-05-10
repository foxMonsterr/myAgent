<template>
  <div class="page-wrap">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>简历项目描述</span>
          <el-tag type="success">Resume Ready</el-tag>
        </div>
      </template>

      <div class="title">{{ resume?.projectName }}</div>
      <div class="summary">{{ resume?.summary }}</div>

      <el-row :gutter="16" class="mt16">
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>项目亮点</template>
            <ul class="list">
              <li v-for="item in resume?.highlights || []" :key="item">{{ item }}</li>
            </ul>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>技术栈</template>
            <el-space wrap>
              <el-tag v-for="item in resume?.techStack || []" :key="item">{{ item }}</el-tag>
            </el-space>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>职责</template>
            <ul class="list">
              <li v-for="item in resume?.responsibilities || []" :key="item">{{ item }}</li>
            </ul>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getResumeProject, type ResumeProjectVO } from '@/api/resume'

const resume = ref<ResumeProjectVO | null>(null)

onMounted(async () => {
  resume.value = await getResumeProject()
})
</script>

<style scoped>
.page-wrap { padding: 12px; }
.header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 20px; font-weight: 700; margin-bottom: 12px; }
.summary { line-height: 1.9; color: #374151; }
.mt16 { margin-top: 16px; }
.list { padding-left: 18px; line-height: 2; color: #4b5563; }
</style>
