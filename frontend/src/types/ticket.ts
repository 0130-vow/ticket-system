// 优先级
export type Priority = 'P0' | 'P1' | 'P2' | 'P3'

// 工单状态
export type TicketStatus = 'pending' | 'processing' | 'resolved' | 'closed'

// 用户角色
export type UserRole = 'employee' | 'technician' | 'admin'

// 工单
export interface Ticket {
  id: string
  title: string
  description: string
  priority: Priority
  status: TicketStatus
  creator: string
  assignee: string
  createdAt: string
  updatedAt: string
  resolvedAt?: string
  closedAt?: string
}

// 回复
export interface Reply {
  id: string
  ticketId: string
  content: string
  author: string
  isInternal: boolean
  createdAt: string
}

// 用户
export interface User {
  id: string
  username: string
  name: string
  role: UserRole
  department?: string
}

// 优先级配置
export const PRIORITY_CONFIG: Record<Priority, { label: string; color: string; timeLimit: string }> = {
  P0: { label: '紧急', color: '#f56c6c', timeLimit: '30分钟' },
  P1: { label: '高', color: '#e6a23c', timeLimit: '2小时' },
  P2: { label: '普通', color: '#409eff', timeLimit: '1工作日' },
  P3: { label: '低', color: '#909399', timeLimit: '1周' }
}

// 状态配置
export const STATUS_CONFIG: Record<TicketStatus, { label: string; color: string }> = {
  pending: { label: '待处理', color: '#909399' },
  processing: { label: '处理中', color: '#409eff' },
  resolved: { label: '已解决', color: '#67c23a' },
  closed: { label: '已关闭', color: '#909399' }
}
