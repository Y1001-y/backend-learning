import axios from 'axios'
import { getToken } from '../utils/auth'

// 创建 axios 实例
const request = axios.create({
    baseURL: '',
    timeout: 10000
})

// 请求拦截器：自动带上 token
request.interceptors.request.use((config) => {
    const token = getToken()
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// 发送验证码
export function sendSms(phone) {
    return request.post('/sms/send', null, { params: { phone } })
}

// 验证码登录
export function login(phone, code) {
    return request.post('/sms/login', { phone, code })
}

// 获取用户信息
export function getUserInfo() {
    return request.get('/user/me')
}