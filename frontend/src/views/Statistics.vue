<template>
  <div class="statistics">
    <h2>统计报表</h2>
    
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>工单状态分布</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="待处理">
              <el-tag type="info">{{ stats.pending }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="处理中">
              <el-tag>{{ stats.processing }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="已解决">
              <el-tag type="success">{{ stats.resolved }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="已关闭">
              <el-tag type="info">{{ stats.closed }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>核心指标</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="首次响应时间">
              <el-tag :type="metrics.avgResponseTime <= 30 ? 'success' : 'warning'">
                {{ metrics.avgResponseTime }} 分钟
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="平均解决时长">
              {{ metrics.avgResolveTime }} 小时
            </el-descriptions-item>
            <el-descriptions-item label="月解决率">
              <el-tag :type="metrics.resolveRate >= 90 ? 'success' : 'danger'">
                {{ metrics.resolveRate }}%
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="逾期率">
              <el-tag :type="metrics.overdueRate <= 5 ? 'success' : 'danger'">
                {{ metrics.overdueRate }}%
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>负责人工作量</span>
          </template>
          <el-table :data="assigneeStats" style="width: 100%">
            <el-table-column prop="name" label="负责人" />
            <el-table-column prop="total" label="总工单数" />
            <el-table-column prop="resolved" label="已解决" />
            <el-table-column prop="pending" label="待处理" />
            <el-table-column label="解决率">
              <template #default="{ row }">
                <el-progress :percentage="row.total > 0 ? Math.round((row.resolved / row.total) * 100) : 0" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ticketApi } from '../api/ticket'

const stats = ref({
  pending: 0,
  processing: 0,
  resolved: 0,
  closed: 0
})

const metrics = ref({
  avgResponseTime: 0,
  avgResolveTime: 0,
  resolveRate: 0,
  overdueRate: 0
})

const assigneeStats = ref([])
const loading = ref(false)

onMounted(() => {
  loadStats()
})

async function loadStats() {
  loading.value = true
  try {
    const { data: res } = await ticketApi.getStats()
    if (res.code === 200) {
      const data = res.data
      stats.value = data.statusDistribution || {}
      metrics.value = data.metrics || {}
      assigneeStats.value = data.assigneeStats || []
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.statistics {
  padding: 20px;
}
h2 {
  margin-bottom: 20px;
}
</style>
