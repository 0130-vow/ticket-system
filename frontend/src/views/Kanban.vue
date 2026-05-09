<template>
  <div class="kanban">
    <h2>看板视图</h2>
    
    <div class="kanban-board">
      <div v-for="status in statuses" :key="status.key" class="kanban-column">
        <div class="column-header" :style="{ borderColor: status.color }">
          <span>{{ status.label }}</span>
          <el-badge :value="getTicketsByStatus(status.key).length" :max="99" />
        </div>
        
        <div class="column-body">
          <el-card v-for="ticket in getTicketsByStatus(status.key)" :key="ticket.id" class="ticket-card" shadow="hover">
            <div class="ticket-header">
              <el-tag :type="getPriorityType(ticket.priority)" size="small">
                {{ ticket.priority }}
              </el-tag>
              <span class="ticket-id">{{ ticket.id }}</span>
            </div>
            <div class="ticket-title">{{ ticket.title }}</div>
            <div class="ticket-footer">
              <span class="assignee">{{ ticket.assignee }}</span>
              <el-button size="small" text @click="viewTicket(ticket.id)">详情</el-button>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ticketApi } from '../api/ticket'

const router = useRouter()
const tickets = ref([])

const statuses = [
  { key: 'pending', label: '待处理', color: '#909399' },
  { key: 'processing', label: '处理中', color: '#409eff' },
  { key: 'resolved', label: '已解决', color: '#67c23a' },
  { key: 'closed', label: '已关闭', color: '#909399' }
]

onMounted(() => {
  loadKanbanData()
})

async function loadKanbanData() {
  try {
    const { data } = await ticketApi.getKanbanData()
    tickets.value = data
  } catch (error) {
    console.error('Failed to load kanban data:', error)
  }
}

function getTicketsByStatus(status: string) {
  return tickets.value.filter((t: any) => t.status === status)
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

function viewTicket(id: string) {
  router.push(`/tickets/${id}`)
}
</script>

<style scoped>
.kanban {
  padding: 20px;
}
h2 {
  margin-bottom: 20px;
}
.kanban-board {
  display: flex;
  gap: 20px;
  overflow-x: auto;
}
.kanban-column {
  flex: 1;
  min-width: 250px;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
}
.column-header {
  font-weight: bold;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 3px solid;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.column-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.ticket-card {
  cursor: pointer;
}
.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.ticket-id {
  font-size: 12px;
  color: #909399;
}
.ticket-title {
  font-size: 14px;
  margin-bottom: 10px;
  line-height: 1.4;
}
.ticket-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}
</style>
