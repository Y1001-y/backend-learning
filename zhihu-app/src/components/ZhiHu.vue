<template>
  <div class="zhihu-page">
    <h1 class="page-title">
      <el-icon class="title-icon"><Star /></el-icon>
      知乎专题
    </h1>

    <!-- 分类筛选 -->
    <div class="filter-bar">
      <span class="filter-label">分类：</span>
      <el-select v-model="selectedCategory" @change="fetchData" placeholder="全部" style="width: 160px">
        <el-option value="" label="全部" />
        <el-option value="科技" label="科技" />
        <el-option value="文化" label="文化" />
        <el-option value="生活" label="生活" />
        <el-option value="其他" label="其他" />
      </el-select>
    </div>

    <!-- 列表渲染 -->
    <div v-if="specialList.length" class="special-list">
      <el-card v-for="item in specialList" :key="item.id" shadow="hover" class="special-card">
        <template #header>
          <div class="card-header">
            <span class="special-title">{{ item.title }}</span>
            <el-tag :type="item.isFollowed ? 'primary' : 'info'" size="small" effect="light">
              {{ item.isFollowed ? '已关注' : '未关注' }}
            </el-tag>
          </div>
        </template>
        <p class="special-desc">{{ item.introduction }}</p>
        <div class="special-footer">
          <span class="update-time">
            <el-icon><Clock /></el-icon>
            更新于 {{ formatTime(item.updated) }}
          </span>
          <!-- 关注按钮：颜色随 isFollowed 变化（保留原条件渲染，不新增切换逻辑） -->
          <el-button
              v-if="item.isFollowed"
              type="primary"
              round
              size="small"
              class="follow-btn"
          >已关注</el-button>
          <el-button
              v-else
              round
              size="small"
              class="unfollow-btn"
          >+ 关注</el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无数据" />

    <!-- 分页 -->
    <div class="pagination">
      <el-button
          :disabled="isFirstPage"
          @click="prevPage"
          round
          class="page-btn"
      >上一页</el-button>
      <span class="page-info">第 {{ pageNum }} 页</span>
      <el-button
          :disabled="isLastPage"
          @click="nextPage"
          type="primary"
          round
          class="page-btn"
      >下一页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { Star, Clock } from '@element-plus/icons-vue'

// 后端接口地址
const BASE = 'http://localhost:8080/api/v1/special'

const specialList = ref<any[]>([])
const selectedCategory = ref('')

// 分页
const limit = ref(3)
const offset = ref(0)
const total = ref(0)

const isFirstPage = computed(() => offset.value === 0)
const isLastPage = computed(() => offset.value + limit.value >= total.value)
const pageNum = computed(() => Math.floor(offset.value / limit.value) + 1)

// 时间戳格式化
function formatTime(ts: number): string {
  if (!ts) return ''
  const d = new Date(ts * 1000)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

// 获取数据（分类 + 分页）
async function fetchData() {
  if (selectedCategory.value) {
    // 按分类查询
    const res = await axios.get(`${BASE}/category/${selectedCategory.value}`)
    specialList.value = res.data.data
  } else {
    // 分页查询全部
    const res = await axios.get(`${BASE}/page`, { params: { limit: limit.value, offset: offset.value } })
    specialList.value = res.data.data.list   // ← 修复：/page 返回的是 {list, total}
    total.value = res.data.data.total        // ← 直接用后端 total
  }
}

// 上一页 / 下一页
function prevPage() {
  if (isFirstPage.value) return
  offset.value -= limit.value
  fetchData()
}

function nextPage() {
  if (isLastPage.value) return
  offset.value += limit.value
  fetchData()
}

// 初始加载
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.zhihu-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 28px 24px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-title {
  text-align: center;
  color: #409eff;
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 24px;
  letter-spacing: 2px;
}
.title-icon {
  margin-right: 6px;
  vertical-align: -2px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 14px 18px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}
.filter-label {
  color: #606266;
  font-size: 14px;
}

.special-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.special-card {
  border-radius: 12px;
  border: none;
  --el-card-border-radius: 12px;
  --el-card-padding: 0;
}
.special-card :deep(.el-card__header) {
  padding: 14px 18px;
  border-bottom: 1px solid #f0f2f5;
  background: #fafbfc;
}
.special-card :deep(.el-card__body) {
  padding: 16px 18px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.special-title {
  color: #1f2d3d;
  font-size: 17px;
  font-weight: 600;
}
.special-desc {
  color: #666;
  line-height: 1.7;
  margin: 0 0 12px;
  font-size: 14px;
}

.special-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.update-time {
  color: #999;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.follow-btn {
  --el-button-bg-color: #409eff;
}
.unfollow-btn {
  --el-button-bg-color: #f0f2f5;
  --el-button-text-color: #909399;
  --el-button-hover-bg-color: #e4e7ed;
  --el-button-border-color: #dcdfe6;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
  margin-top: 26px;
  padding: 16px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}
.page-info {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}
</style>