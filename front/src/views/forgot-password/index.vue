<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="always">
      <div class="title">找回密码</div>
      <div class="subtitle">通过手机号验证码重置密码</div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleSubmit">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <el-row :gutter="12" style="width: 100%">
            <el-col :span="16">
              <el-input v-model="form.captcha" placeholder="请输入4位验证码" maxlength="4" />
            </el-col>
            <el-col :span="8">
              <el-button style="width: 100%" :disabled="captchaCooldown > 0" @click="handleSendCaptcha">
                {{ captchaCooldown > 0 ? `${captchaCooldown}s` : '获取验证码' }}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="不少于6个字符" />
        </el-form-item>

        <el-button type="primary" class="block-btn" :loading="loading" @click="handleSubmit">重置密码</el-button>
      </el-form>

      <div class="links">
        <router-link to="/login">返回登录</router-link>
        <router-link to="/register">去注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { sendCaptcha } from '@/api/auth'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaCooldown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const form = reactive({
  phone: '',
  captcha: '',
  newPassword: '',
})

const phonePattern = /^1[3-9]\d{9}$/
const passwordPattern = /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':",.<>/?\\|`~]+$/

const rules: FormRules = {
  phone: [{ validator: (_, value, callback) => (phonePattern.test(value) ? callback() : callback(new Error('手机号格式不正确'))), trigger: 'blur' }],
  captcha: [{ validator: (_, value, callback) => (/^\d{4}$/.test(value) ? callback() : callback(new Error('验证码必须为4位数字'))), trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度不少于6个字符', trigger: 'blur' },
    { validator: (_, value, callback) => (passwordPattern.test(value) ? callback() : callback(new Error('密码只能包含字母、数字和常用符号'))), trigger: 'blur' },
  ],
}

const startCooldown = () => {
  captchaCooldown.value = 60
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    captchaCooldown.value -= 1
    if (captchaCooldown.value <= 0 && timer) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

const handleSendCaptcha = async () => {
  if (!phonePattern.test(form.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  try {
    await sendCaptcha({ phone: form.phone, scene: 'forgot' })
    ElMessage.success('验证码已发送')
    startCooldown()
  } catch (error) {
    console.error('[forgot-password] send captcha failed:', error)
    ElMessage.error('验证码发送失败')
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    await request.put('/api/v1/user/password/forgot', {
      phone: form.phone,
      captcha: form.captcha,
      newPassword: form.newPassword,
    })
    ElMessage.success('密码重置成功，请重新登录')
    await router.push('/login')
  } catch (error) {
    console.error('[forgot-password] submit failed:', error)
    ElMessage.error('重置密码失败')
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
.auth-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef4ff 0%, #f7faff 100%);
}

.auth-card {
  width: 460px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
}

.subtitle {
  margin: 8px 0 20px;
  text-align: center;
  color: #6b7280;
}

.block-btn {
  width: 100%;
}

.links {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 14px;
}
</style>
