<template>
  <div class="profile-page">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never" class="profile-card">
          <template #header>
            <div class="card-title">个人资料</div>
          </template>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ profile.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ profile.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ profile.role || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ profile.enabled ? '启用' : '禁用' }}</el-descriptions-item>
          </el-descriptions>

          <div class="meta-list">
            <div>最后登录：{{ profile.lastLoginAt || '-' }}</div>
            <div>创建时间：{{ profile.createdAt || '-' }}</div>
            <div>更新时间：{{ profile.updatedAt || '-' }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="profile-card">
          <template #header>
            <div class="card-title">修改资料</div>
          </template>

          <el-form :model="profileForm" label-position="top">
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="profileForm.remark" type="textarea" :rows="4" placeholder="可选" />
            </el-form-item>
            <el-button type="primary" :loading="savingProfile" @click="handleUpdateProfile">保存资料</el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="profile-card">
          <template #header>
            <div class="card-title">安全设置</div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" label-position="top">
                <el-form-item label="旧密码">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-button type="primary" :loading="savingPassword" @click="handleResetPassword">修改密码</el-button>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改手机号" name="phone">
              <el-form :model="phoneForm" label-position="top">
                <el-form-item label="新手机号">
                  <el-input v-model="phoneForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="验证码">
                  <el-row :gutter="12" style="width: 100%">
                    <el-col :span="16">
                      <el-input v-model="phoneForm.captcha" placeholder="请输入4位验证码" maxlength="4" />
                    </el-col>
                    <el-col :span="8">
                      <el-button style="width: 100%" :disabled="phoneCooldown > 0" @click="handleSendPhoneCaptcha">
                        {{ phoneCooldown > 0 ? `${phoneCooldown}s` : '获取验证码' }}
                      </el-button>
                    </el-col>
                  </el-row>
                </el-form-item>
                <el-button type="primary" :loading="savingPhone" @click="handleUpdatePhone">修改手机号</el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, resetPassword, updatePhone, updateProfile, type UserProfileVO } from '@/api/user'
import { sendCaptcha } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const profile = reactive<UserProfileVO>({
  username: '',
  nickname: '',
  phone: '',
  role: '',
  enabled: true,
  lastLoginAt: '',
  createdAt: '',
  updatedAt: '',
})

const profileForm = reactive({
  nickname: '',
  remark: '',
})
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
})
const phoneForm = reactive({
  phone: '',
  captcha: '',
})

const activeTab = ref('password')
const savingProfile = ref(false)
const savingPassword = ref(false)
const savingPhone = ref(false)
const phoneCooldown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const loadProfile = async () => {
  const data = await getProfile()
  Object.assign(profile, data)
  profileForm.nickname = data.nickname || ''
}

const handleUpdateProfile = async () => {
  savingProfile.value = true
  try {
    await updateProfile({ nickname: profileForm.nickname, remark: profileForm.remark })
    userStore.setUserInfo({ username: profile.username, role: profile.role, nickname: profileForm.nickname })
    ElMessage.success('资料更新成功')
  } catch (error) {
    console.error('[profile] update failed:', error)
    ElMessage.error('资料更新失败')
  } finally {
    savingProfile.value = false
  }
}

const handleResetPassword = async () => {
  savingPassword.value = true
  try {
    await resetPassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
  } catch (error) {
    console.error('[profile] reset password failed:', error)
    ElMessage.error('密码修改失败')
  } finally {
    savingPassword.value = false
  }
}

const startCooldown = () => {
  phoneCooldown.value = 60
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    phoneCooldown.value -= 1
    if (phoneCooldown.value <= 0 && timer) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

const handleSendPhoneCaptcha = async () => {
  if (!phoneForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  try {
    await sendCaptcha({ phone: phoneForm.phone, scene: 'update-phone' })
    ElMessage.success('验证码已发送')
    startCooldown()
  } catch (error) {
    console.error('[profile] send phone captcha failed:', error)
    ElMessage.error('验证码发送失败')
  }
}

const handleUpdatePhone = async () => {
  savingPhone.value = true
  try {
    await updatePhone({ phone: phoneForm.phone, captcha: phoneForm.captcha })
    ElMessage.success('手机号修改成功')
    await loadProfile()
    phoneForm.captcha = ''
  } catch (error) {
    console.error('[profile] update phone failed:', error)
    ElMessage.error('手机号修改失败')
  } finally {
    savingPhone.value = false
  }
}

onMounted(loadProfile)

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
.profile-page {
  padding: 12px;
}

.profile-card {
  min-height: 520px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
}

.meta-list {
  margin-top: 16px;
  color: #6b7280;
  line-height: 2;
  font-size: 13px;
}
</style>
