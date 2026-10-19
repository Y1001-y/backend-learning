<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <template #header>
        <div class="card-header">
          <el-icon :size="32" color="#26c6da"><Phone /></el-icon>
          <div>
            <h1 class="title">短信登录</h1>
            <span class="subtitle">使用手机号 + 验证码快速登录</span>
          </div>
        </div>
      </template>

      <el-form :model="form" size="large" @submit.prevent>
        <el-form-item>
          <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              maxlength="11"
              :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item>
          <el-row :gutter="12" style="width:100%">
            <el-col :span="14">
              <el-input
                  v-model="form.code"
                  placeholder="请输入验证码"
                  maxlength="4"
                  :prefix-icon="Lock"
              />
            </el-col>
            <el-col :span="10">
              <el-button
                  type="primary"
                  :disabled="countdown > 0"
                  @click="handleSend"
                  style="width:100%"
              >
                {{ countdown > 0 ? `${countdown}s 后重发` : '发送验证码' }}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-btn"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <el-alert
          v-if="countdown > 0"
          type="success"
          :closable="false"
          title="验证码已发送，5分钟内有效"
          show-icon
          center
          class="tip"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, User, Lock } from '@element-plus/icons-vue'
import { sendSms, login } from '../api/sms'
import { setToken } from '../utils/auth'

const router = useRouter()
const form = reactive({ phone: '', code: '' })
const loading = ref(false)
const countdown = ref(0)
let timer = null

async function handleSend() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  try {
    const res = await sendSms(form.phone)
    if (res.data.code === 200) {
      ElMessage.success('验证码已发送，请查收！')
      startCountdown(60)
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('发送失败，请重试')
  }
}

function startCountdown(seconds) {
  countdown.value = seconds
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function handleLogin() {
  if (!form.phone || !form.code) {
    ElMessage.error('请填写手机号和验证码')
    return
  }
  loading.value = true
  try {
    const res = await login(form.phone, form.code)
    if (res.data.code === 200) {
      setToken(res.data.data.token)
      ElMessage.success('登录成功')
      setTimeout(() => router.push('/home'), 600)
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}

onUnmounted(() => timer && clearInterval(timer))
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 50%, #80deea 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 440px;
  border-radius: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 0;
}

.title {
  font-size: 24px;
  color: #1f2d3d;
  margin: 0 0 4px;
}

.subtitle {
  font-size: 13px;
  color: #8a9ba8;
}

.login-btn {
  width: 100%;
  letter-spacing: 4px;
}

.tip {
  margin-top: 8px;
}
</style>