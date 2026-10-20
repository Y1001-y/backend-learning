<template>
  <div class="container">
    <h1 class="page-title">知乎专题</h1>

    <!-- 分类筛选（扩展练习1） -->
    <div class="filter-bar">
      <el-select v-model="selectedCategory" @change="fetchData" placeholder="全部" style="width:160px">
        <el-option value="" label="全部" />
        <el-option value="文化" label="文化" />
        <el-option value="科技" label="科技" />
        <el-option value="生活" label="生活" />
      </el-select>
    </div>

    <!-- 教程卡片：左图右文 -->
    <div v-for="(item, index) in list" :key="index" class="card">
      <div class="left">
        <img :src="item.banner" alt="" referrerpolicy="no-referrer" />
      </div>
      <div class="right">
        <h3>{{ item.title }}</h3>
        <p class="desc">{{ item.introduction }}</p>
        <p class="meta">
          <span class="updated">{{ formatTime(item.updated) }}</span>
          <span>{{ item.viewCount }}次浏览</span>
        </p>
      </div>
      <!-- 关注按钮：颜色随 isFollowing 变化 -->
      <el-button
          v-if="item.isFollowing === 'TRUE'"
          size="small"
          class="follow followed"
      >已关注</el-button>
      <el-button
          v-else
          size="small"
          class="follow unfollow"
      >+ 关注</el-button>
    </div>

    <!-- 分页（扩展练习） -->
    <div class="pagination">
      <el-button :disabled="isFirstPage" @click="prevPage" round>上一页</el-button>
      <span class="page-info">第 {{ pageNum }} 页</span>
      <el-button type="primary" :disabled="isLastPage" @click="nextPage" round>下一页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const BASE = 'http://localhost:8080/api/v1/special'

interface Special {
  id: string
  title: string
  introduction: string
  banner: string
  viewCount: number
  followersCount: number
  isFollowing: string
  updated: number
  category: string
}

const list = ref<Special[]>([])
const selectedCategory = ref('')

// 分页
const limit = ref(3)
const offset = ref(0)
const total = ref(0)
const isFirstPage = computed(() => offset.value === 0)
const isLastPage = computed(() => offset.value + limit.value >= total.value)
const pageNum = computed(() => Math.floor(offset.value / limit.value) + 1)

// 时间格式化
function formatTime(ts: number): string {
  if (!ts) return ''
  const d = new Date(ts * 1000)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

// 加载数据
async function fetchData() {
  if (selectedCategory.value) {
    const res = await axios.get(`${BASE}/category/${selectedCategory.value}`)
    list.value = res.data.data as Special[]
  } else {
    const res = await axios.get(`${BASE}/page`, { params: { limit: limit.value, offset: offset.value } })
    list.value = res.data.data.list as Special[]
    total.value = res.data.data.total
  }
}

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

onMounted(() => { fetchData() })
</script>

<style scoped>
.container { width: 70%; margin: 0 auto; font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif; }
.page-title { text-align: center; color: #1f2d3d; }
.filter-bar { margin-bottom: 20px; }

.card {
  display: flex;
  height: 160px;
  margin-bottom: 15px;
  border: 1px solid #ccc;
  box-shadow: 10px 5px 5px #ddd;
  border-radius: 8px;
  position: relative;
  background: #fff;
}
.left { flex: 1; }
.right { flex: 4; padding: 10px; }
.right h3 { margin: 0 0 6px; color: #1f2d3d; }
.right .desc { margin: 0 0 6px; color: #666; font-size: 13px; line-height: 1.5; }
.right .meta { margin: 0; }
.right .meta span { font-size: 13px; color: #333; }
.right .updated { margin-right: 20px; color: #999; }

.left img {
  width: 100%;
  height: 100%;
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
  object-fit: cover;
}

.follow { position: absolute; right: 20px; top: 20px; }
.followed {
  --el-button-bg-color: #f6f6f6;
  --el-button-text-color: #716c6c;
  --el-button-border-color: #dcdfe6;
}
.unfollow {
  --el-button-bg-color: #eff4fe;
  --el-button-text-color: #3670ee;
  --el-button-border-color: #3670ee;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
  margin-top: 20px;
}
.page-info { color: #606266; }
</style>