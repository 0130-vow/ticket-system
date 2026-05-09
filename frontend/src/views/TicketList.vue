<template>
  <div class="ticket-list">
    <div class="header">
      <h2>工单列表</h2>
      <el-button type="primary" @click="$router.push('/tickets/create')">
        新建工单
      </el-button>
    </div>
    
    <div class="filters">
      <el-select v-model="filters.status" placeholder="状态" clearable>
        <el-option label="待处理" value="pending" />
        <el-option label="处理中" value="processing" />
        <el-option label="已解决" value="resolved" />
        <el-option label="已关闭" value="closed" />
      </el-select>
      
      <el-select v-model="filters.priority" placeholder="优先级" clearable>
        <el-option label="P0 紧急" value="P0" />
        <el-option label="P1 高" value="P1" />
        <el-option label="P2 普通" value="P2" />
        <el-option label="P3 低" value="P3" />
      </el-select>
      
      <el-input v-model="filters.keyword" placeholder="搜索标题..." clearable />
    </div>
    
    <el-table :data="tickets" style="width: 100%">
      <el-table-column prop="id" label="工单号" width="120" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)">
            {{ row.priority }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="assignee" label="负责人" width="120" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="viewTicket(row.id)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ticketApi } from '../api/ticket'
import { PRIORITY_CONFIG, STATUS_CONFIG } from '../types/ticket'

const router = useRouter()
const tickets = ref([])
const filters = reactive({
  status: '',
  priority: '',
  keyword: ''
})

onMounted(() => {
  loadTickets()
})

async function loadTickets() {
  try {
    const { data } = await ticketApi.getList(filters)
    tickets.value = data
  } catch (error) {
    console.error('Failed to load tickets:', error)
  }
}

function viewTicket(id: string) {
  router.push(`/tickets/${id}`)
}

function getPriorityType(priority: string) {
  const types: Record<string, string> = {
    P0: 'danger',
    P1: 'warning',
    P2: '',
    P3: 'info'
  }
  return types[priority] || ''
}

function getStatusType(status: string) {
  const types: Record<string, string> = {
    pending: 'info',
    processing: '',
    resolved: 'success',
    closed: 'info'
  }
  return types[status] || ''
}

function getStatusLabel(status: string) {
  return STATUS_CONFIG[status as keyof typeof STATUS_CONFIG]?.label || status
}
</script>

<style scoped>
.ticket-list {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>
