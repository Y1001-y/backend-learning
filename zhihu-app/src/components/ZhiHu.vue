<template>
  <div class="zhihu-page">
    <h1 class="page-title">知乎专题</h1>

    <!-- 分类筛选（练习2） -->
    <div class="filter-bar">
      <label>分类：</label>
      <select v-model="selectedCategory" @change="fetchData">
        <option value="">全部</option>
        <option value="科技">科技</option>
        <option value="文化">文化</option>
        <option value="生活">生活</option>
        <option value="其他">其他</option>
      </select>
    </div>

    <!-- 列表渲染 -->
    <div v-if="specialList.length" class="special-list">
      <div v-for="item in specialList" :key="item.id" class="special-card">
        <h3 class="special-title">{{ item.title }}</h3>
        <p class="special-desc">{{ item.introduction }}</p>
        <div class="special-footer">
          <span class="update-time">更新于 {{ formatTime(item.updated) }}</span>
          <!-- 关注条件渲染 -->
          <span v-if="item.isFollowed" class="follow">已关注</span>
          <span v-else class="unfollow">未关注</span>
        </div>
      </div>
    </div>
    <p v-else class="empty-tip">暂无数据</p>

    <!-- 分页（练习3/4） -->
    <div class="pagination">
      <button :disabled="isFirstPage" @click="prevPage">上一页</button>
      <span class="page-info">第 {{ pageNum }} 页</span>
      <button :disabled="isLastPage" @click="nextPage">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// 后端接口地址（可直接写后端地址，或配 vite 代理）
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

async function fetchData() {
  if (selectedCategory.value) {
    // 按分类查询
    const res = await axios.get(`${BASE}/category/${selectedCategory.value}`)
    specialList.value = res.data.data
    total.value = specialList.value.length
  } else {
    // 分页查询全部
    const res = await axios.get(`${BASE}/page`, { params: { limit: limit.value, offset: offset.value } })
    specialList.value = res.data.data.list          // ← 改成 .list
    total.value = res.data.data.total               // ← 直接用后端 total
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

// 初始加载（每页3条）
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.zhihu-page { max-width: 800px; margin: 0 auto; padding: 20px; font-family: 'PingFang SC', sans-serif; }
.page-title { text-align: center; color: #0084ff; }
.filter-bar { margin: 20px 0; display: flex; align-items: center; gap: 10px; }
.filter-bar select { padding: 6px 12px; border-radius: 6px; }
.special-list { display: flex; flex-direction: column; gap: 16px; }
.special-card { border: 1px solid #eee; border-radius: 12px; padding: 16px 20px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,.05); }
.special-title { color: #1f2d3d; margin: 0 0 8px; }
.special-desc { color: #666; line-height: 1.6; }
.special-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
.update-time { color: #999; font-size: 12px; }
.follow { color: #0084ff; font-size: 13px; }
.unfollow { color: #bbb; font-size: 13px; }
.empty-tip { text-align: center; color: #999; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 20px; }
.pagination button { padding: 6px 16px; border-radius: 6px; border: 1px solid #0084ff; background: #fff; color: #0084ff; cursor: pointer; }
.pagination button:disabled { border-color: #ccc; color: #ccc; cursor: not-allowed; }
.page-info { color: #666; }
</style>