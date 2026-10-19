<template>
  <el-card class="login-card">
    <h2>手机号登录</h2>
    <el-form :model="form" label-width="80px">
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="验证码">
        <div class="code-row">
          <el-input v-model="form.code" placeholder="请输入验证码" maxlength="4" />
          <el-button :disabled="countdown > 0" @click="handleSend">
            {{ countdown > 0 ? `${countdown}s 后重发` : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendSms, login } from '../api/sms'
import { setToken } from '../utils/auth'

const router = useRouter()
const form = reactive({ phone: '', code: '' })
const loading = ref(false)
const countdown = ref(0)
let timer = null

// 发送验证码
async function handleSend() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  try {
    const res = await sendSms(form.phone)
    if (res.data.code === 200) {
      ElMessage.success('验证码已发送（mock 模式下看后端日志）')
      startCountdown(60)
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('发送失败')
  }
}

// 60 秒倒计时
function startCountdown(seconds) {
  countdown.value = seconds
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// 登录
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
      router.push('/home')
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}

onUnmounted(() => timer && clearInterval(timer))
</script>

<style scoped>
.login-card { max-width: 400px; margin: 80px auto; }
.code-row { display: flex; gap: 10px; width: 100%; }
</style>