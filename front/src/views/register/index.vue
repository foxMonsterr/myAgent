<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="always">
      <div class="title">用户注册</div>
      <div class="subtitle">支持用户名密码注册，也支持手机号验证码注册</div>

      <el-segmented v-model="registerType" :options="registerOptions" class="mode-switch" />

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="5-32 个字符，仅字母、数字和常用符号" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="不少于 6 个字符" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="可选" />
        </el-form-item>

        <template v-if="registerType === 'phone'">
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
        </template>

        <el-button type="primary" class="block-btn" :loading="loading" @click="handleRegister">注册并登录</el-button>
      </el-form>

      <div class="links">
        <router-link to="/login">返回登录</router-link>
        <router-link to="/init-admin">初始化管理员</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { register, sendCaptcha } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const registerType = ref<'password' | 'phone'>('password')
const captchaCooldown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const registerOptions = [
  { label: '用户名密码注册', value: 'password' },
  { label: '手机号验证码注册', value: 'phone' },
]

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  captcha: '',
})

const usernamePattern = /^[a-zA-Z0-9._-]+$/
const passwordPattern = /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':",.<>/?\\|`~]+$/
const phonePattern = /^1[3-9]\d{9}$/

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 5, max: 32, message: '用户名长度 5-32 个字符', trigger: 'blur' },
    { validator: (_, value, callback) => (usernamePattern.test(value) ? callback() : callback(new Error('用户名只能包含字母、数字和常用符号'))), trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度不少于 6 个字符', trigger: 'blur' },
    { validator: (_, value, callback) => (passwordPattern.test(value) ? callback() : callback(new Error('密码只能包含字母、数字和常用符号'))), trigger: 'blur' },
  ],
  phone: [
    { validator: (_, value, callback) => (registerType.value === 'phone' && !phonePattern.test(value) ? callback(new Error('手机号格式不正确')) : callback()), trigger: 'blur' },
  ],
  captcha: [
    { validator: (_, value, callback) => (registerType.value === 'phone' && !/^\d{4}$/.test(value) ? callback(new Error('验证码必须为4位数字')) : callback()), trigger: 'blur' },
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
    await sendCaptcha({ phone: form.phone, scene: 'register' })
    ElMessage.success('验证码已发送')
    startCooldown()
  } catch (error) {
    console.error('[register] send captcha failed:', error)
    ElMessage.error('验证码发送失败')
  }
}

const handleRegister = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: registerType.value === 'phone' ? form.phone : undefined,
      captcha: registerType.value === 'phone' ? form.captcha : undefined,
      registerType: registerType.value,
    }
    const res = await register(payload)
    userStore.setAuth(res)
    ElMessage.success('注册成功')
    await router.push('/home')
  } catch (error) {
    console.error('[register] failed:', error)
    ElMessage.error('注册失败，请检查填写内容')
  } finally {
    loading.value = false
  }
}

watch(registerType, () => {
  form.phone = ''
  form.captcha = ''
})

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

.mode-switch {
  width: 100%;
  margin-bottom: 16px;
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
