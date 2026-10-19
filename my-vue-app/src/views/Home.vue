<template>
  <el-container class="home-page">
    <!-- 顶部导航 -->
    <el-header class="navbar">
      <div class="nav-left">
        <el-icon :size="20" color="#fff"><HomeFilled /></el-icon>
        <span class="nav-title">欢迎来到首页</span>
      </div>
      <div class="nav-right">
        <el-tag type="success" effect="dark" round>登录成功</el-tag>
        <el-button type="primary" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-main class="content">
      <el-row :gutter="20" justify="center">
        <!-- 用户信息 -->
        <el-col :xs="24" :sm="20" :md="16">
          <el-card class="card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon :size="18" color="#1976d2"><UserFilled /></el-icon>
                <span class="card-title">用户信息</span>
              </div>
            </template>

            <el-skeleton v-if="!userInfo" :rows="3" animated />

            <el-descriptions v-else :column="1" border>
              <el-descriptions-item label="手机号">
                <el-icon><Iphone /></el-icon>
                <span style="margin-left:6px">{{ userInfo.phone }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="登录时间">
                <el-icon><Clock /></el-icon>
                <span style="margin-left:6px">{{ userInfo.loginTime }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="Token状态">
                <el-tag type="success" effect="light">已认证</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <!-- 功能区域 -->
        <el-col :xs="24" :sm="20" :md="16">
          <el-card class="card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon :size="18" color="#1976d2"><Setting /></el-icon>
                <span class="card-title">功能区域</span>
              </div>
            </template>

            <el-empty description="功能开发中..." :image-size="90" />
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  HomeFilled, UserFilled, Setting, Iphone, Clock
} from '@element-plus/icons-vue'
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
.home-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e3f2fd 0%, #ffffff 40%);
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(90deg, #1976d2, #2196f3);
  color: #fff;
  box-shadow: 0 4px 15px rgba(25, 118, 210, 0.3);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-title {
  font-size: 18px;
  font-weight: 600;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content {
  max-width: 760px;
  margin: 0 auto;
}

.card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  font-size: 16px;
  color: #1f2d3d;
}
</style>