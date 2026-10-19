<template>
  <el-card class="home-card">
    <h2>欢迎回来</h2>
    <p v-if="userInfo">手机号：{{ userInfo.phone }}</p>
    <p v-if="userInfo">登录时间：{{ userInfo.loginTime }}</p>
    <el-button @click="handleLogout">退出登录</el-button>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '../api/sms'
import { getToken, clearToken } from '../utils/auth'

const router = useRouter()
const userInfo = ref(null)

onMounted(async () => {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  try {
    const res = await getUserInfo()
    if (res.data.code === 200) {
      userInfo.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
      handleLogout()
    }
  } catch (e) {
    ElMessage.error('获取用户信息失败')
  }
})

function handleLogout() {
  clearToken()
  router.replace('/login')
}
</script>

<style scoped>
.home-card { max-width: 400px; margin: 80px auto; }
</style>