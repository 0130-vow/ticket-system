<template>
  <div class="ticket-list">
    <div class="header">
      <h2>工单列表</h2>
      <el-button type="primary" @click="$router.push('/tickets/create')">
        新建工单
      </el-button>
    </div>

    <div class="filters">
      <el-select v-model="filters.status" placeholder="状态" clearable @change="handleSearch">
        <el-option label="待处理" value="pending" />
        <el-option label="处理中" value="processing" />
        <el-option label="已解决" value="resolved" />
        <el-option label="已关闭" value="closed" />
      </el-select>

      <el-select v-model="filters.priority" placeholder="优先级" clearable @change="handleSearch">
        <el-option label="P0 紧急" value="P0" />
        <el-option label="P1 高" value="P1" />
        <el-option label="P2 普通" value="P2" />
        <el-option label="P3 低" value="P3" />
      </el-select>

      <el-select v-model="filters.assigneeId" placeholder="负责人" clearable filterable @change="handleSearch">
        <el-option v-for="user in users" :key="user.id" :label="user.name" :value="user.id" />
      </el-select>

      <el-select v-model="filters.creatorId" placeholder="提交人" clearable filterable @change="handleSearch">
        <el-option v-for="user in users" :key="user.id" :label="user.name" :value="user.id" />
      </el-select>

      <el-date-picker
        v-model="filters.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="handleSearch"
      />

      <el-autocomplete
        v-model="filters.keyword"
        :fetch-suggestions="querySearchHistory"
        placeholder="搜索标题..."
        clearable
        @keyup.enter="handleSearch"
        @select="handleHistorySelect"
        style="width: 240px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #default="{ item }">
          <div class="history-item">
            <span class="history-text">{{ item.value }}</span>
            <el-icon class="history-delete" @click.stop="removeHistoryItem(item.value)">
              <Close />
            </el-icon>
          </div>
        </template>
        <template #append>
          <el-button @click="clearAllHistory" v-if="searchHistory.length > 0">
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-autocomplete>

      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tickets" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="工单号" width="100" />
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <span v-html="highlightKeyword(row.title)"></span>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)" effect="dark">
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
      <el-table-column label="负责人" width="120">
        <template #default="{ row }">
          {{ getUserName(row.assigneeId) }}
        </template>
      </el-table-column>
      <el-table-column label="提交人" width="120">
        <template #default="{ row }">
          {{ getUserName(row.creatorId) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="viewTicket(row.id)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Close, Delete } from '@element-plus/icons-vue'
import { ticketApi, userApi } from '../api/ticket'
import { STATUS_CONFIG, type Ticket, type User } from '../types/ticket'
import { ElMessage } from 'element-plus'

const router = useRouter()
const tickets = ref<Ticket[]>([])
const users = ref<User[]>([])
const loading = ref(false)

const filters = reactive({
  status: '',
  priority: '',
  keyword: '',
  creatorId: null as number | null,
  assigneeId: null as number | null,
  dateRange: null as string[] | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 搜索历史
const searchHistory = ref<string[]>([])
const HISTORY_KEY = 'ticket_search_history'
const MAX_HISTORY = 10

// 加载搜索历史
function loadSearchHistory() {
  try {
    const history = localStorage.getItem(HISTORY_KEY)
    if (history) {
      searchHistory.value = JSON.parse(history)
    }
  } catch (e) {
    console.error('Failed to load search history:', e)
  }
}

// 保存搜索历史
function saveSearchHistory(keyword: string) {
  if (!keyword.trim()) return

  // 移除重复项
  const index = searchHistory.value.indexOf(keyword)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
  }

  // 添加到开头
  searchHistory.value.unshift(keyword)

  // 限制数量
  if (searchHistory.value.length > MAX_HISTORY) {
    searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY)
  }

  // 保存到本地存储
  try {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(searchHistory.value))
  } catch (e) {
    console.error('Failed to save search history:', e)
  }
}

// 删除单条历史
function removeHistoryItem(keyword: string) {
  const index = searchHistory.value.indexOf(keyword)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(searchHistory.value))
    } catch (e) {
      console.error('Failed to save search history:', e)
    }
  }
}

// 清空所有历史
function clearAllHistory() {
  searchHistory.value = []
  try {
    localStorage.removeItem(HISTORY_KEY)
  } catch (e) {
    console.error('Failed to clear search history:', e)
  }
}

// 搜索历史查询
function querySearchHistory(queryString: string, cb: (results: any[]) => void) {
  const history = searchHistory.value.map(item => ({ value: item }))
  if (queryString) {
    const results = history.filter(item =>
      item.value.toLowerCase().includes(queryString.toLowerCase())
    )
    cb(results)
  } else {
    cb(history)
  }
}

// 选择历史记录
function handleHistorySelect(item: { value: string }) {
  filters.keyword = item.value
  handleSearch()
}

onMounted(() => {
  loadUsers()
  loadTickets()
  loadSearchHistory()
})

async function loadUsers() {
  try {
    const { data: res } = await userApi.getUsers()
    if (res.code === 200) {
      users.value = res.data
    }
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

async function loadTickets() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }

    if (filters.status) params.status = filters.status
    if (filters.priority) params.priority = filters.priority
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.creatorId) params.creatorId = filters.creatorId
    if (filters.assigneeId) params.assigneeId = filters.assigneeId
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }

    const { data: res } = await ticketApi.getList(params)
    if (res.code === 200) {
      tickets.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('Failed to load tickets:', error)
    ElMessage.error('加载工单列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  // 保存搜索历史
  if (filters.keyword) {
    saveSearchHistory(filters.keyword)
  }
  pagination.page = 1
  loadTickets()
}

function handleReset() {
  filters.status = ''
  filters.priority = ''
  filters.keyword = ''
  filters.creatorId = null
  filters.assigneeId = null
  filters.dateRange = null
  pagination.page = 1
  loadTickets()
}

function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  loadTickets()
}

function handlePageChange(page: number) {
  pagination.page = page
  loadTickets()
}

function viewTicket(id: string) {
  router.push(`/tickets/${id}`)
}

function getUserName(userId: number) {
  const user = users.value.find((u: any) => u.id === userId)
  return user ? user.name : userId || '-'
}

function highlightKeyword(text: string) {
  if (!filters.keyword || !text) return text
  const regex = new RegExp(`(${filters.keyword})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
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

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
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
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
:deep(.highlight) {
  color: #409eff;
  font-weight: bold;
  background-color: #ecf5ff;
  padding: 0 2px;
  border-radius: 2px;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.history-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-delete {
  margin-left: 8px;
  color: #909399;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.history-item:hover .history-delete {
  opacity: 1;
}

.history-delete:hover {
  color: #f56c6c;
}
</style>
