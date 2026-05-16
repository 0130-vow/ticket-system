<template>
  <div class="ticket-detail" v-loading="loading">
    <div class="header">
      <el-button @click="$router.back()" :icon="ArrowLeft">返回列表</el-button>
      <div class="header-title">
        <h2>工单详情</h2>
        <el-tag size="large">#{{ ticket.id }}</el-tag>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData" :icon="Refresh">刷新</el-button>
      </div>
    </div>
    
    <el-row :gutter="20">
      <!-- 左侧主要内容 -->
      <el-col :span="16">
        <!-- 工单基本信息 -->
        <el-card class="main-card">
          <template #header>
            <div class="card-header">
              <div class="title-section">
                <el-tag :type="getPriorityType(ticket.priority)" size="large" effect="dark">
                  {{ ticket.priority }} {{ getPriorityLabel(ticket.priority) }}
                </el-tag>
                <h3 class="ticket-title">{{ ticket.title }}</h3>
              </div>
              <el-tag :type="getStatusType(ticket.status)" size="large">
                {{ getStatusLabel(ticket.status) }}
              </el-tag>
            </div>
          </template>
          
          <div class="description-section">
            <div class="section-header">
              <el-icon><Document /></el-icon>
              <span>问题描述</span>
            </div>
            <div class="description-content">
              {{ ticket.description || '暂无描述' }}
            </div>
          </div>
          
          <!-- 优先级提示 -->
          <el-alert
            v-if="ticket.priority"
            :title="getPriorityTip(ticket.priority)"
            :type="getPriorityAlertType(ticket.priority)"
            show-icon
            :closable="false"
            style="margin-top: 16px"
          />
        </el-card>

        <!-- 附件区域 -->
        <el-card class="attachments-card">
          <template #header>
            <div class="card-header">
              <div class="section-header">
                <el-icon><Upload /></el-icon>
                <span>附件 ({{ attachments.length }})</span>
              </div>
              <el-upload
                :show-file-list="false"
                :before-upload="beforeUpload"
                :http-request="handleUpload"
                accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.xls,.xlsx,.txt,.log"
              >
                <el-button type="primary" size="small" :loading="uploading" :icon="Upload">
                  上传附件
                </el-button>
              </el-upload>
            </div>
          </template>

          <div v-if="attachments.length > 0" class="attachment-list">
            <div v-for="att in attachments" :key="att.id" class="attachment-item">
              <div class="attachment-info">
                <el-icon class="attachment-icon" :size="20">
                  <component :is="getFileIcon(att.fileType)" />
                </el-icon>
                <div class="attachment-meta">
                  <span class="attachment-name" @click="handleDownload(att.id)">{{ att.originalName }}</span>
                  <span class="attachment-size">{{ formatFileSize(att.fileSize) }}</span>
                </div>
              </div>
              <div class="attachment-actions">
                <el-button link type="primary" size="small" @click="handleDownload(att.id)" :icon="Download">
                  下载
                </el-button>
                <el-button link type="danger" size="small" @click="handleDeleteAttachment(att.id)" :icon="Delete">
                  删除
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无附件" :image-size="60" />
        </el-card>

        <!-- 回复记录 -->
        <el-card class="replies-card">
          <template #header>
            <div class="card-header">
              <div class="section-header">
                <el-icon><ChatDotRound /></el-icon>
                <span>回复记录 ({{ replyPagination.total }})</span>
              </div>
              <el-radio-group v-model="replyFilter" size="small" @change="handleReplyFilterChange">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="public">公开</el-radio-button>
                <el-radio-button label="internal" v-if="!isEmployee">内部</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div class="replies-list" v-if="filteredReplies.length > 0">
            <div v-for="reply in filteredReplies" :key="reply.id"
                 :class="['reply-item', { 'internal-reply': reply.isInternal }]">
              <div class="reply-avatar">
                <el-avatar :size="40" :style="{ backgroundColor: getAvatarColor(reply.authorId) }">
                  {{ getUserName(reply.authorId).charAt(0) }}
                </el-avatar>
              </div>
              <div class="reply-body">
                <div class="reply-header">
                  <span class="author-name">{{ getUserName(reply.authorId) }}</span>
                  <el-tag v-if="getUserRole(reply.authorId) === 'admin'" type="danger" size="small">管理员</el-tag>
                  <el-tag v-else-if="getUserRole(reply.authorId) === 'technician'" type="warning" size="small">技术</el-tag>
                  <el-tag v-if="reply.isInternal" type="info" size="small" effect="plain">内部备注</el-tag>
                  <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无回复记录" />

          <!-- 回复分页 -->
          <div class="reply-pagination" v-if="replyPagination.total > 0">
            <el-pagination
              v-model:current-page="replyPagination.page"
              v-model:page-size="replyPagination.size"
              :page-sizes="[10, 20, 50]"
              :total="replyPagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="handleReplySizeChange"
              @current-change="handleReplyPageChange"
            />
          </div>
          
          <!-- 添加回复 -->
          <el-divider />
          <div class="add-reply">
            <div class="section-header">
              <el-icon><EditPen /></el-icon>
              <span>添加回复</span>
            </div>
            
            <!-- 快捷回复 -->
            <div class="quick-replies" v-if="!isEmployee">
              <span class="quick-label">快捷回复：</span>
              <el-button size="small" @click="insertQuickReply('正在处理中，请稍候...')">处理中</el-button>
              <el-button size="small" @click="insertQuickReply('问题已定位，预计XX小时内修复')">已定位</el-button>
              <el-button size="small" @click="insertQuickReply('问题已解决，请确认是否正常')">已解决</el-button>
              <el-button size="small" @click="insertQuickReply('需要更多信息，请提供截图或日志')">需补充</el-button>
            </div>
            
            <el-input 
              v-model="newReply" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入回复内容...&#10;&#10;建议包含：&#10;1. 问题根因&#10;2. 解决方案&#10;3. 预防措施"
              maxlength="2000"
              show-word-limit
            />
            <div class="reply-actions">
              <div class="reply-options">
                <el-checkbox v-if="!isEmployee" v-model="isInternal">
                  <el-tooltip content="内部备注仅对技术负责人和管理员可见" placement="top">
                    <span>设为内部备注 <el-icon><QuestionFilled /></el-icon></span>
                  </el-tooltip>
                </el-checkbox>
              </div>
              <el-button type="primary" @click="submitReply" :loading="submitting" :icon="Promotion">
                提交回复
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧信息栏 -->
      <el-col :span="8">
        <!-- 工单信息卡片 -->
        <el-card class="info-card">
          <template #header>
            <div class="section-header">
              <el-icon><InfoFilled /></el-icon>
              <span>工单信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="工单编号">
              <span class="ticket-id">#{{ ticket.id }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="getPriorityType(ticket.priority)" effect="dark">
                {{ ticket.priority }} {{ getPriorityLabel(ticket.priority) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusType(ticket.status)">
                {{ getStatusLabel(ticket.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="提交人">
              <div class="user-info">
                <el-avatar :size="24" :style="{ backgroundColor: getAvatarColor(ticket.creatorId) }">
                  {{ getUserName(ticket.creatorId).charAt(0) }}
                </el-avatar>
                <span>{{ getUserName(ticket.creatorId) }}</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="负责人">
              <div class="user-info" v-if="ticket.assigneeId">
                <el-avatar :size="24" :style="{ backgroundColor: getAvatarColor(ticket.assigneeId) }">
                  {{ getUserName(ticket.assigneeId).charAt(0) }}
                </el-avatar>
                <span>{{ getUserName(ticket.assigneeId) }}</span>
              </div>
              <el-tag v-else type="info">未分配</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="工单分类">
              {{ getCategoryName(ticket.categoryId) }}
            </el-descriptions-item>
            <el-descriptions-item label="标签">
              <div class="tags-container" v-if="ticketTags.length > 0">
                <el-tag v-for="tag in ticketTags" :key="tag.id" :color="tag.color" style="color: #fff; margin-right: 8px; margin-bottom: 4px">
                  {{ tag.name }}
                </el-tag>
              </div>
              <span v-else style="color: #909399">无标签</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDate(ticket.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDate(ticket.updatedAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="解决时间" v-if="ticket.resolvedAt">
              {{ formatDate(ticket.resolvedAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="耗时" v-if="ticket.resolvedAt">
              <el-tag type="success">{{ calculateDuration(ticket.createdAt, ticket.resolvedAt) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
        
        <!-- 操作面板 -->
        <el-card class="action-card" v-if="canOperate">
          <template #header>
            <div class="section-header">
              <el-icon><Operation /></el-icon>
              <span>操作</span>
            </div>
          </template>
          
          <div class="action-buttons">
            <template v-if="ticket.status === 'pending'">
              <el-button type="primary" @click="handleStatusChange('processing')" :icon="VideoPlay" block>
                接单处理
              </el-button>
              <p class="action-tip">点击后工单状态将变为"处理中"</p>
            </template>
            
            <template v-if="ticket.status === 'processing'">
              <el-button type="success" @click="handleStatusChange('resolved')" :icon="Select" block>
                标记解决
              </el-button>
              <p class="action-tip">请确保已在回复中说明解决方案</p>
              <el-button @click="handleStatusChange('pending')" :icon="Back" block>
                退回待处理
              </el-button>
            </template>
            
            <template v-if="ticket.status === 'resolved'">
              <el-button type="warning" @click="handleStatusChange('closed')" :icon="CircleCheck" block>
                确认关闭
              </el-button>
              <p class="action-tip">关闭后工单将归档</p>
              <el-button @click="handleStatusChange('processing')" :icon="RefreshLeft" block>
                重新处理
              </el-button>
            </template>
            
            <template v-if="ticket.status === 'closed'">
              <el-button type="info" @click="handleStatusChange('pending')" :icon="RefreshRight" block>
                重新开启
              </el-button>
              <p class="action-tip">重新开启后工单将回到待处理状态</p>
            </template>
          </div>
        </el-card>
        
        <!-- 员工操作 -->
        <el-card class="action-card" v-if="isEmployee && ticket.status === 'resolved'">
          <template #header>
            <div class="section-header">
              <el-icon><Operation /></el-icon>
              <span>确认</span>
            </div>
          </template>
          <el-button type="success" @click="handleStatusChange('closed')" :icon="CircleCheck" block>
            确认问题已解决
          </el-button>
          <p class="action-tip">确认后工单将关闭</p>
        </el-card>
        
        <!-- 状态流转历史 -->
        <el-card class="history-card">
          <template #header>
            <div class="section-header">
              <el-icon><Clock /></el-icon>
              <span>状态流转</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(log, index) in statusHistory"
              :key="index"
              :timestamp="log.time"
              :type="log.type"
              placement="top"
            >
              <p>{{ log.content }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ticketApi, categoryApi, attachmentApi } from '../api/ticket'
import { useUserStore } from '../stores/user'
import { STATUS_CONFIG, PRIORITY_CONFIG, type Category, type Tag, type Attachment } from '../types/ticket'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Refresh, Document, ChatDotRound, EditPen, Promotion,
  InfoFilled, Operation, Clock, VideoPlay, Select, Back, CircleCheck,
  RefreshLeft, RefreshRight, QuestionFilled, Upload, Delete, Download,
} from '@element-plus/icons-vue'

interface UserBrief {
  id: number
  name: string
  role: string
  department?: string
}

const route = useRoute()
const userStore = useUserStore()

const ticket = ref<any>({})
const replies = ref<any[]>([])
const userMap = ref<Record<number, UserBrief>>({})
const categoryOptions = ref<Category[]>([])
const ticketTags = ref<Tag[]>([])
const attachments = ref<Attachment[]>([])
const uploading = ref(false)
const loading = ref(false)
const submitting = ref(false)
const newReply = ref('')
const isInternal = ref(false)
const replyFilter = ref('all')

// 回复分页
const replyPagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 计算属性
const isEmployee = computed(() => userStore.userInfo?.role === 'employee')
const canOperate = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'technician' || role === 'admin'
})

const filteredReplies = computed(() => {
  if (replyFilter.value === 'all') return replies.value
  if (replyFilter.value === 'internal') return replies.value.filter(r => r.isInternal)
  return replies.value.filter(r => !r.isInternal)
})

const statusHistory = computed(() => {
  const history = []
  const t = ticket.value
  
  if (t.createdAt) {
    history.push({
      time: formatDate(t.createdAt),
      content: `工单创建 - ${getUserName(t.creatorId)}`,
      type: 'primary'
    })
  }
  
  if (t.status === 'processing' || t.status === 'resolved' || t.status === 'closed') {
    history.push({
      time: formatDate(t.updatedAt),
      content: '开始处理',
      type: 'warning'
    })
  }
  
  if (t.resolvedAt) {
    history.push({
      time: formatDate(t.resolvedAt),
      content: '标记解决',
      type: 'success'
    })
  }
  
  if (t.closedAt) {
    history.push({
      time: formatDate(t.closedAt),
      content: '工单关闭',
      type: 'info'
    })
  }
  
  return history.reverse()
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    await Promise.all([
      loadTicket(),
      loadReplies(),
      loadCategories(),
      loadAttachments()
    ])
  } finally {
    loading.value = false
  }
}

async function loadTicket() {
  try {
    const { data: res } = await ticketApi.getDetail(route.params.id as string)
    if (res.code === 200) {
      ticket.value = res.data
      // 合并用户信息
      if (res.userMap) {
        userMap.value = { ...userMap.value, ...res.userMap }
      }
      // 获取标签
      if (res.tags) {
        ticketTags.value = res.tags
      }
    }
  } catch (error) {
    console.error('Failed to load ticket:', error)
    ElMessage.error('加载工单失败')
  }
}

async function loadCategories() {
  try {
    const { data: res } = await categoryApi.getList(true)
    if (res.code === 200) {
      categoryOptions.value = res.data
    }
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

async function loadAttachments() {
  try {
    const { data: res } = await attachmentApi.getByTicketId(route.params.id as string)
    if (res.code === 200) {
      attachments.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load attachments:', error)
  }
}

async function handleUpload(uploadFile: any) {
  uploading.value = true
  try {
    const { data: res } = await attachmentApi.upload(uploadFile.raw, Number(route.params.id))
    if (res.code === 200) {
      ElMessage.success('上传成功')
      await loadAttachments()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleDeleteAttachment(id: number) {
  try {
    await ElMessageBox.confirm('确定删除此附件？', '确认', { type: 'warning' })
    const { data: res } = await attachmentApi.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadAttachments()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

function handleDownload(id: number) {
  window.open(attachmentApi.getDownloadUrl(id), '_blank')
}

function formatFileSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function getFileIcon(fileType: string) {
  if (fileType.startsWith('image/')) return 'Picture'
  if (fileType.includes('pdf')) return 'Document'
  if (fileType.includes('word') || fileType.includes('document')) return 'Document'
  if (fileType.includes('excel') || fileType.includes('sheet')) return 'Grid'
  return 'Document'
}

async function loadReplies() {
  try {
    const params = {
      page: replyPagination.page,
      size: replyPagination.size
    }
    const { data: res } = await ticketApi.getReplies(route.params.id as string, params)
    if (res.code === 200) {
      if (res.data.records) {
        // 分页数据
        replies.value = res.data.records
        replyPagination.total = res.data.total
      } else {
        // 兼容旧接口
        replies.value = res.data || []
        replyPagination.total = replies.value.length
      }
      // 合并用户信息
      if (res.userMap) {
        userMap.value = { ...userMap.value, ...res.userMap }
      }
    }
  } catch (error) {
    console.error('Failed to load replies:', error)
  }
}

function refreshData() {
  loadData()
  ElMessage.success('数据已刷新')
}

function handleReplySizeChange(size: number) {
  replyPagination.size = size
  replyPagination.page = 1
  loadReplies()
}

function handleReplyPageChange(page: number) {
  replyPagination.page = page
  loadReplies()
}

function handleReplyFilterChange() {
  // 切换筛选时重新加载
  replyPagination.page = 1
  loadReplies()
}

function insertQuickReply(text: string) {
  newReply.value = text
}

function beforeUpload(file: File) {
  const allowedTypes = [
    'image/jpeg', 'image/png', 'image/gif',
    'application/pdf',
    'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/plain'
  ]
  const maxSize = 10 * 1024 * 1024

  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('不支持的文件类型')
    return false
  }
  if (file.size > maxSize) {
    ElMessage.error('文件大小超过10MB限制')
    return false
  }
  return true
}

async function submitReply() {
  if (!newReply.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  submitting.value = true
  try {
    const { data: res } = await ticketApi.addReply(route.params.id as string, {
      content: newReply.value,
      isInternal: isInternal.value
    })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      newReply.value = ''
      isInternal.value = false
      await loadReplies()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '回复失败')
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(newStatus: string) {
  const statusLabel = getStatusLabel(newStatus)
  
  try {
    await ElMessageBox.confirm(
      `确定要将工单状态更改为"${statusLabel}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const { data: res } = await ticketApi.updateStatus(route.params.id as string, newStatus)
    if (res.code === 200) {
      ElMessage.success(`工单已${statusLabel}`)
      await loadTicket()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

// 辅助函数
function getUserName(userId: number) {
  return userMap.value[userId]?.name || `用户${userId}`
}

function getUserRole(userId: number) {
  return userMap.value[userId]?.role || ''
}

function getAvatarColor(id: number) {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
  return colors[id % colors.length]
}

function getPriorityLabel(priority: string) {
  return PRIORITY_CONFIG[priority as keyof typeof PRIORITY_CONFIG]?.label || ''
}

function getPriorityType(priority: string) {
  const types: Record<string, string> = { P0: 'danger', P1: 'warning', P2: '', P3: 'info' }
  return types[priority] || ''
}

function getPriorityTip(priority: string) {
  const tips: Record<string, string> = {
    P0: '紧急工单需在30分钟内响应，请立即处理！',
    P1: '高优先级工单需在2小时内响应',
    P2: '普通工单需在1个工作日内处理',
    P3: '低优先级工单可在本周内处理'
  }
  return tips[priority] || ''
}

function getPriorityAlertType(priority: string) {
  const types: Record<string, string> = { P0: 'error', P1: 'warning', P2: 'info', P3: 'info' }
  return types[priority] || 'info'
}

function getStatusType(status: string) {
  const types: Record<string, string> = { pending: 'info', processing: '', resolved: 'success', closed: 'info' }
  return types[status] || ''
}

function getStatusLabel(status: string) {
  return STATUS_CONFIG[status as keyof typeof STATUS_CONFIG]?.label || status
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

function calculateDuration(start: string, end: string) {
  if (!start || !end) return '-'
  const diff = new Date(end).getTime() - new Date(start).getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))

  if (hours > 24) {
    const days = Math.floor(hours / 24)
    return `${days}天${hours % 24}小时`
  }
  return `${hours}小时${minutes}分钟`
}

function getCategoryName(categoryId: number | null | undefined) {
  if (!categoryId) return '未分类'
  const findCategory = (categories: Category[]): string => {
    for (const cat of categories) {
      if (cat.id === categoryId) return cat.name
      if (cat.children) {
        const found = findCategory(cat.children)
        if (found) return found
      }
    }
    return ''
  }
  return findCategory(categoryOptions.value) || '未分类'
}
</script>

<style scoped>
.ticket-detail {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 80px);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.main-card,
.replies-card,
.attachments-card,
.info-card,
.action-card,
.history-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ticket-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.section-header .el-icon {
  font-size: 18px;
  color: #409eff;
}

.description-section {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.description-content {
  margin-top: 12px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.replies-list {
  max-height: 500px;
  overflow-y: auto;
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  margin-bottom: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.reply-item:hover {
  background: #ecf5ff;
}

.reply-item.internal-reply {
  background: #fdf6ec;
  border-left: 3px solid #e6a23c;
}

.reply-body {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.author-name {
  font-weight: 600;
  color: #303133;
}

.reply-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.reply-content {
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.add-reply {
  padding-top: 16px;
}

.quick-replies {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.quick-label {
  font-size: 14px;
  color: #909399;
}

.reply-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.reply-options {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-card :deep(.el-descriptions__label) {
  width: 80px;
  font-weight: 600;
}

.ticket-id {
  font-family: monospace;
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-buttons .el-button {
  width: 100%;
}

.action-tip {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.history-card :deep(.el-timeline) {
  padding-left: 2px;
}

.history-card :deep(.el-timeline-item__content) {
  font-size: 14px;
}

.history-card :deep(.el-timeline-item__timestamp) {
  font-size: 12px;
}

/* 滚动条样式 */
.replies-list::-webkit-scrollbar {
  width: 6px;
}

.replies-list::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.replies-list::-webkit-scrollbar-track {
  background: transparent;
}

.reply-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  transition: background 0.2s;
}

.attachment-item:hover {
  background: #ecf5ff;
}

.attachment-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.attachment-icon {
  color: #409eff;
  flex-shrink: 0;
}

.attachment-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.attachment-name {
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-name:hover {
  color: #409eff;
}

.attachment-size {
  font-size: 12px;
  color: #909399;
}

.attachment-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
</style>
