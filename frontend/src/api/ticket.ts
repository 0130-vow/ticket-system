import axios from 'axios'
import { useUserStore } from '../stores/user'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 工单相关接口
export const ticketApi = {
  // 获取工单列表
  getList(params?: any) {
    return api.get('/tickets', { params })
  },
  
  // 获取工单详情
  getDetail(id: string) {
    return api.get(`/tickets/${id}`)
  },
  
  // 创建工单
  create(data: any) {
    return api.post('/tickets', data)
  },
  
  // 更新工单状态
  updateStatus(id: string, status: string) {
    return api.patch(`/tickets/${id}/status`, { status })
  },
  
  // 获取回复列表
  getReplies(ticketId: string) {
    return api.get(`/tickets/${ticketId}/replies`)
  },
  
  // 添加回复
  addReply(ticketId: string, data: { content: string; isInternal?: boolean }) {
    return api.post(`/tickets/${ticketId}/replies`, data)
  },
  
  // 获取统计信息
  getStats() {
    return api.get('/stats')
  },
  
  // 获取看板数据
  getKanbanData() {
    return api.get('/tickets/kanban')
  }
}

// 用户相关接口
export const userApi = {
  login(data: { username: string; password: string }) {
    return api.post('/auth/login', data)
  },
  
  getUsers() {
    return api.get('/users')
  }
}
