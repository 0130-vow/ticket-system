import axios from 'axios'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000
})

// 重试配置
const MAX_RETRIES = 2
const RETRY_DELAY = 1000

// 请求拦截器
api.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 延迟函数
function delay(ms: number) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const data = response.data
    // 处理业务错误
    if (data.code && data.code !== 200) {
      const message = data.message || '请求失败'
      // 参数验证错误
      if (data.errors) {
        const errorMessages = Object.values(data.errors).join('\n')
        ElMessage.error(errorMessages)
      } else {
        ElMessage.error(message)
      }
      return Promise.reject(new Error(message))
    }
    return response
  },
  async (error) => {
    const config = error.config

    // 网络错误重试机制
    if (!error.response && config && !config.__retried) {
      config.__retried = 0
    }

    if (!error.response && config && config.__retried < MAX_RETRIES) {
      config.__retried++
      await delay(RETRY_DELAY * config.__retried)
      return api(config)
    }

    if (error.response) {
      const { status, data } = error.response
      const message = data?.message || '请求失败'

      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限执行此操作')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 422:
          // 参数验证错误
          if (data?.errors) {
            const errorMessages = Object.values(data.errors).join('\n')
            ElMessage.error(errorMessages)
          } else {
            ElMessage.error(message)
          }
          break
        case 500:
          ElMessage.error('服务器内部错误，请稍后重试')
          break
        default:
          ElMessage.error(message)
      }
    } else if (error.message?.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络连接')
    } else if (error.message?.includes('Network Error')) {
      ElMessage.error('网络连接失败，请检查网络')
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
  getReplies(ticketId: string, params?: { page?: number; size?: number }) {
    return api.get(`/tickets/${ticketId}/replies`, { params })
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
