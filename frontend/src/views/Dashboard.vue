<template>
  <div class="dashboard">
    <h2>工作台</h2>
    
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" style="color: #909399">{{ stats.pending }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" style="color: #409eff">{{ stats.processing }}</div>
            <div class="stat-label">处理中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" style="color: #67c23a">{{ stats.resolved }}</div>
            <div class="stat-label">已解决</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" style="color: #e6a23c">{{ stats.overdue }}</div>
            <div class="stat-label">逾期工单</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待我处理</span>
          </template>
          <el-table :data="myTickets" style="width: 100%" max-height="300" v-loading="loading">
            <el-table-column prop="id" label="工单号" width="100" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="priority" label="优先级" width="80">
              <template #default="{ row }">
                <el-tag :type="getPriorityType(row.priority)" size="small">
                  {{ row.priority }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button size="small" text @click="$router.push(`/tickets/${row.id}`)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && myTickets.length === 0" description="暂无待处理工单" />
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>我提交的工单</span>
          </template>
          <el-table :data="createdTickets" style="width: 100%" max-height="300" v-loading="loading">
            <el-table-column prop="id" label="工单号" width="100" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button size="small" text @click="$router.push(`/tickets/${row.id}`)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && createdTickets.length === 0" description="暂无工单" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ticketApi } from '../api/ticket'
import { STATUS_CONFIG } from '../types/ticket'

const stats = ref({
  pending: 0,
  processing: 0,
  resolved: 0,
  overdue: 0
})

const myTickets = ref([])
const createdTickets = ref([])
const loading = ref(false)

onMounted(() => {
  loadDashboardData()
})

async function loadDashboardData() {
  loading.value = true
  try {
    const { data: res } = await ticketApi.getStats()
    if (res.code === 200 || res.statusDistribution) {
      // 兼容两种响应格式
      const data = res.data || res
      stats.value = {
        pending: data.statusDistribution?.pending || 0,
        processing: data.statusDistribution?.processing || 0,
        resolved: data.statusDistribution?.resolved || 0,
        overdue: data.metrics?.overdueRate || 0
      }
      myTickets.value = data.myTickets || []
      createdTickets.value = data.createdTickets || []
    }
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  } finally {
    loading.value = false
  }
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
.dashboard {
  padding: 20px;
}
h2 {
  margin-bottom: 20px;
}
.stat-cards {
  margin-bottom: 20px;
}
.stat-item {
  text-align: center;
  padding: 10px;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>
