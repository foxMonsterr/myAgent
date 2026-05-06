<template>
  <div class="page-wrap">
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>Knowledge 知识库调试台</span>
              <el-tag type="info">RAG / 文档管理 / 问答</el-tag>
            </div>
          </template>

          <el-form :model="form" label-width="120px">
            <el-form-item label="上传文件">
              <el-upload
                :auto-upload="false"
                :show-file-list="true"
                :limit="1"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
              >
                <el-button type="primary">选择文件</el-button>
              </el-upload>
            </el-form-item>

            <el-form-item label="批量加载目录">
              <el-input v-model="form.dirPath" placeholder="如：src/main/resources/knowledge/" clearable />
            </el-form-item>

            <el-form-item label="问题">
              <el-input v-model="form.question" type="textarea" :rows="5" placeholder="请输入知识库问题" />
            </el-form-item>

            <el-form-item label="conversationId">
              <el-input v-model="form.conversationId" placeholder="可选，用于多轮追问" clearable />
            </el-form-item>

            <el-form-item label="topK">
              <el-input-number v-model="form.topK" :min="1" :max="20" />
            </el-form-item>

            <el-form-item label="similarityThreshold">
              <el-input-number v-model="form.similarityThreshold" :min="0" :max="1" :step="0.05" />
            </el-form-item>

            <el-form-item>
              <el-space wrap>
                <el-button type="primary" :loading="loading.ask" @click="handleAsk(false)">自动 RAG 问答</el-button>
                <el-button :loading="loading.ask" @click="handleAsk(true)">手动 RAG 问答</el-button>
                <el-button :loading="loading.upload" @click="handleUpload">上传文件</el-button>
                <el-button :loading="loading.dir" @click="handleLoadDirectory">批量加载目录</el-button>
                <el-button :loading="loading.list" @click="handleRefresh">刷新文档列表</el-button>
                <el-button :loading="loading.search" @click="handleSearch">纯检索</el-button>
              </el-space>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="panel-card mt16">
          <template #header>
            <div class="header">
              <span>问答结果</span>
              <el-tag type="success">{{ responseLabel }}</el-tag>
            </div>
          </template>

          <div class="response-box">
            <template v-if="result">
              <div class="result-meta">
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="conversationId">{{ result.conversationId || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="retrievedChunks">{{ result.retrievedChunks ?? '-' }}</el-descriptions-item>
                  <el-descriptions-item label="model">{{ result.model || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="sources">{{ (result.sources || []).join('，') || '-' }}</el-descriptions-item>
                </el-descriptions>
              </div>

              <div class="answer-block">
                <div class="answer-title">Answer</div>
                <pre>{{ result.answer || resultText }}</pre>
              </div>
            </template>
            <el-empty v-else description="暂无结果" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="header">
              <span>文档列表 / 状态</span>
              <el-button text size="small" @click="handleRefresh">刷新</el-button>
            </div>
          </template>

          <el-descriptions :column="1" border size="small" class="mb16">
            <el-descriptions-item label="当前文件数">{{ status.documentCount }}</el-descriptions-item>
            <el-descriptions-item label="总切片数">{{ status.totalChunks }}</el-descriptions-item>
          </el-descriptions>

          <div class="doc-list">
            <div v-for="doc in documents" :key="doc.fileName" class="doc-item">
              <div>
                <strong>{{ doc.fileName }}</strong>
                <div class="doc-meta">{{ doc.fileSizeReadable }} · chunks: {{ doc.chunkCount }} · {{ doc.indexTime }}</div>
              </div>
              <el-button text type="danger" :loading="deletingFileName === doc.fileName" @click="handleDelete(doc.fileName)">删除</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type UploadFile, type UploadFiles } from 'element-plus'
import {
  askKnowledge,
  askKnowledgeManual,
  deleteDocument,
  getKnowledgeStatus,
  listDocuments,
  loadDirectory,
  searchKnowledge,
  uploadDocument,
} from '@/api/knowledge'
import type { KnowledgeDocumentVO, KnowledgeResponse, KnowledgeStatusResponse } from '@/types/knowledge'

const loading = reactive({ ask: false, list: false, upload: false, dir: false, search: false })
const documents = ref<KnowledgeDocumentVO[]>([])
const status = reactive<KnowledgeStatusResponse>({ documentCount: 0, totalChunks: 0, documents: [] })
const result = ref<KnowledgeResponse | null>(null)
const selectedFile = ref<File | null>(null)
const deletingFileName = ref('')

const form = reactive({
  question: '',
  conversationId: '',
  dirPath: 'src/main/resources/knowledge/',
  topK: 5,
  similarityThreshold: 0.5,
})

const responseLabel = computed(() => (result.value ? 'RAG 结果' : '等待问答'))
const resultText = computed(() => (result.value ? JSON.stringify(result.value, null, 2) : ''))

const handleFileChange = (_uploadFile: UploadFile, uploadFiles: UploadFiles) => {
  const raw = uploadFiles.at(-1)?.raw
  selectedFile.value = raw || null
}

const handleFileRemove = () => {
  selectedFile.value = null
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  loading.upload = true
  try {
    await uploadDocument(selectedFile.value)
    ElMessage.success('上传成功')
    selectedFile.value = null
    await handleRefresh()
  } finally {
    loading.upload = false
  }
}

const handleLoadDirectory = async () => {
  loading.dir = true
  try {
    await loadDirectory(form.dirPath.trim() || undefined)
    ElMessage.success('目录加载完成')
    await handleRefresh()
  } finally {
    loading.dir = false
  }
}

const handleRefresh = async () => {
  loading.list = true
  try {
    const [docsRes, statusRes] = await Promise.all([listDocuments(), getKnowledgeStatus()])
    documents.value = (docsRes || []) as KnowledgeDocumentVO[]
    Object.assign(status, statusRes || { documentCount: 0, totalChunks: 0, documents: [] })
  } finally {
    loading.list = false
  }
}

const handleAsk = async (manual: boolean) => {
  if (!form.question.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  loading.ask = true
  try {
    const payload = {
      question: form.question.trim(),
      conversationId: form.conversationId.trim() || undefined,
      topK: form.topK,
      similarityThreshold: form.similarityThreshold,
    }
    result.value = manual ? await askKnowledgeManual(payload) : await askKnowledge(payload)
  } finally {
    loading.ask = false
  }
}

const handleSearch = async () => {
  if (!form.question.trim()) {
    ElMessage.warning('请输入检索关键词')
    return
  }
  loading.search = true
  try {
    const res = await searchKnowledge(form.question.trim())
    result.value = {
      conversationId: form.conversationId || undefined,
      answer: typeof res === 'string' ? res : JSON.stringify(res, null, 2),
      sources: [],
      retrievedChunks: 0,
      model: 'search-only',
    }
  } finally {
    loading.search = false
  }
}

const handleDelete = async (fileName: string) => {
  await ElMessageBox.confirm(`确认删除 ${fileName} 吗？`, '提示', { type: 'warning' })
  deletingFileName.value = fileName
  try {
    await deleteDocument(fileName)
    ElMessage.success('删除成功')
    await handleRefresh()
  } finally {
    deletingFileName.value = ''
  }
}

onMounted(() => {
  handleRefresh()
})
</script>

<style scoped lang="scss">
.page-wrap { height: 100%; }
.panel-card { border-radius: 12px; }
.mt16 { margin-top: 16px; }
.mb16 { margin-bottom: 16px; }
.header { display: flex; align-items: center; justify-content: space-between; font-weight: 600; }
.response-box { min-height: 280px; }
.result-meta { margin-bottom: 16px; }
.answer-block pre { white-space: pre-wrap; word-break: break-word; background: #fafafa; padding: 12px; border-radius: 8px; }
.answer-title { margin-bottom: 8px; font-weight: 600; }
.doc-list { max-height: 720px; overflow: auto; }
.doc-item { display: flex; align-items: center; justify-content: space-between; padding: 12px; border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 12px; background: #fff; }
.doc-meta { color: #909399; font-size: 12px; margin-top: 4px; }
</style>
