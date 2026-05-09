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
  creatorId: number
  assigneeId: number
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
  authorId: number
  isInternal: boolean
  createdAt: string
}

// 用户
export interface User {
  id: number
  username: string
  name: string
  role: UserRole
  department?: string
}

// 优先级配置
export const PRIORITY_CONFIG: Record<Priority, { label: string; color: string; timeLimit: string; description: string }> = {
  P0: { 
    label: '紧急', 
    color: '#f56c6c', 
    timeLimit: '30分钟',
    description: '生产环境宕机、数据丢失风险'
  },
  P1: { 
    label: '高', 
    color: '#e6a23c', 
    timeLimit: '2小时',
    description: '核心功能受损，影响多人'
  },
  P2: { 
    label: '普通', 
    color: '#409eff', 
    timeLimit: '1工作日',
    description: '功能异常，有临时方案可用'
  },
  P3: { 
    label: '低', 
    color: '#909399', 
    timeLimit: '1周',
    description: '体验优化、非紧迫性需求'
  }
}

// 状态配置
export const STATUS_CONFIG: Record<TicketStatus, { label: string; color: string; description: string }> = {
  pending: { 
    label: '待处理', 
    color: '#909399',
    description: '工单已创建，等待负责人接单'
  },
  processing: { 
    label: '处理中', 
    color: '#409eff',
    description: '负责人已接单，正在处理'
  },
  resolved: { 
    label: '已解决', 
    color: '#67c23a',
    description: '问题已修复，等待确认'
  },
  closed: { 
    label: '已关闭', 
    color: '#909399',
    description: '工单已归档'
  }
}

// 状态流转规则
export const STATUS_TRANSITIONS: Record<TicketStatus, TicketStatus[]> = {
  pending: ['processing'],
  processing: ['resolved', 'pending'],
  resolved: ['closed', 'processing'],
  closed: ['pending']
}
