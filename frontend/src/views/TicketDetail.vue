<template>
  <div class="ticket-detail" v-loading="loading">
    <div class="header">
      <el-button @click="$router.back()">返回</el-button>
      <h2>工单详情 #{{ ticket.id }}</h2>
    </div>
    
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ ticket.title }}</span>
              <el-tag :type="getStatusType(ticket.status)">
                {{ getStatusLabel(ticket.status) }}
              </el-tag>
            </div>
          </template>
          
          <div class="description">
            <h4>问题描述</h4>
            <p>{{ ticket.description }}</p>
          </div>
          
          <el-divider />
          
          <div class="replies">
            <h4>回复记录</h4>
            <div v-for="reply in replies" :key="reply.id" class="reply-item">
              <div class="reply-header">
                <span class="author">用户 {{ reply.authorId }}</span>
                <span class="time">{{ formatDate(reply.createdAt) }}</span>
                <el-tag v-if="reply.isInternal" type="warning" size="small">内部备注</el-tag>
              </div>
              <div class="reply-content">{{ reply.content }}</div>
            </div>
            <el-empty v-if="replies.length === 0" description="暂无回复" />
          </div>
          
          <el-divider />
          
          <div class="add-reply">
            <h4>添加回复</h4>
            <el-input v-model="newReply" type="textarea" :rows="3" placeholder="请输入回复内容..." />
            <div class="reply-actions">
              <el-checkbox v-if="!isEmployee" v-model="isInternal">设为内部备注</el-checkbox>
              <el-button type="primary" @click="submitReply" :loading="submitting">提交回复</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>工单信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="优先级">
              <el-tag :type="getPriorityType(ticket.priority)">{{ ticket.priority }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(ticket.status)">{{ getStatusLabel(ticket.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDate(ticket.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDate(ticket.updatedAt) }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="actions" style="margin-top: 20px">
            <el-button-group v-if="canOperate">
              <el-button v-if="ticket.status === 'pending'" type="primary" @click="updateStatus('processing')">
                接单处理
              </el-button>
              <el-button v-if="ticket.status === 'processing'" type="success" @click="updateStatus('resolved')">
                标记解决
              </el-button>
              <el-button v-if="ticket.status === 'resolved'" type="warning" @click="updateStatus('closed')">
                关闭工单
              </el-button>
              <el-button v-if="ticket.status === 'closed'" type="info" @click="updateStatus('pending')">
                重新开启
              </el-button>
            </el-button-group>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ticketApi } from '../api/ticket'
import { useUserStore } from '../stores/user'
import { STATUS_CONFIG } from '../types/ticket'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const ticket = ref<any>({})
const replies = ref([])
const loading = ref(false)
const submitting = ref(false)
const newReply = ref('')
const isInternal = ref(false)

const isEmployee = computed(() => userStore.userInfo?.role === 'employee')
const canOperate = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'technician' || role === 'admin'
})

onMounted(() => {
  loadTicket()
  loadReplies()
})

async function loadTicket() {
  loading.value = true
  try {
    const { data: res } = await ticketApi.getDetail(route.params.id as string)
    if (res.code === 200) {
      ticket.value = res.data
    }
  } catch (error) {
    console.error('Failed to load ticket:', error)
  } finally {
    loading.value = false
  }
}

async function loadReplies() {
  try {
    const { data: res } = await ticketApi.getReplies(route.params.id as string)
    if (res.code === 200) {
      replies.value = res.data
    }
  } catch (error) {
    console.error('Failed to load replies:', error)
  }
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
      loadReplies()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '回复失败')
  } finally {
    submitting.value = false
  }
}

async function updateStatus(status: string) {
  try {
    const { data: res } = await ticketApi.updateStatus(route.params.id as string, status)
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      loadTicket()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}

function getPriorityType(priority: string) {
  const types: Record<string, string> = { P0: 'danger', P1: 'warning', P2: '', P3: 'info' }
  return types[priority] || ''
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
</script>

<style scoped>
.ticket-detail {
  padding: 20px;
}
.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.description {
  margin-bottom: 20px;
}
.description h4,
.replies h4,
.add-reply h4 {
  margin-bottom: 10px;
  color: #303133;
}
.reply-item {
  padding: 10px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}
.reply-header {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}
.author {
  font-weight: bold;
}
.time {
  color: #909399;
}
.reply-content {
  line-height: 1.6;
}
.reply-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
