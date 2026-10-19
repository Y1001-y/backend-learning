import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('../views/Login.vue') },
    { path: '/home', component: () => import('../views/Home.vue') }
  ]
})

// 路由守卫：未登录访问 /home 跳转 /login
router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path === '/home' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router