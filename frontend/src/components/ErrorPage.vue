<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-icon">
        <el-icon v-if="type === '404'" :size="120"><QuestionFilled /></el-icon>
        <el-icon v-else-if="type === '500'" :size="120"><WarningFilled /></el-icon>
        <el-icon v-else-if="type === 'network'" :size="120"><Connection /></el-icon>
        <el-icon v-else-if="type === '403'" :size="120"><Lock /></el-icon>
        <el-icon v-else :size="120"><CircleCloseFilled /></el-icon>
      </div>

      <h1 class="error-code">{{ errorCode }}</h1>
      <p class="error-message">{{ errorMessage }}</p>
      <p class="error-description">{{ errorDescription }}</p>

      <div class="error-actions">
        <el-button type="primary" @click="handleRetry" v-if="showRetry">
          <el-icon><Refresh /></el-icon>
          重试
        </el-button>
        <el-button @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button @click="goBack" v-if="showBack">
          <el-icon><Back /></el-icon>
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  QuestionFilled,
  WarningFilled,
  Connection,
  Lock,
  CircleCloseFilled,
  Refresh,
  HomeFilled,
  Back
} from '@element-plus/icons-vue'

interface Props {
  type?: '404' | '500' | 'network' | '403' | 'unknown'
  showRetry?: boolean
  showBack?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: '404',
  showRetry: false,
  showBack: true
})

const emit = defineEmits<{
  retry: []
}>()

const router = useRouter()

const errorCode = computed(() => {
  const codes: Record<string, string> = {
    '404': '404',
    '500': '500',
    'network': '网络错误',
    '403': '403',
    'unknown': '未知错误'
  }
  return codes[props.type] || '错误'
})

const errorMessage = computed(() => {
  const messages: Record<string, string> = {
    '404': '页面未找到',
    '500': '服务器内部错误',
    'network': '网络连接失败',
    '403': '访问被拒绝',
    'unknown': '发生了未知错误'
  }
  return messages[props.type] || '发生错误'
})

const errorDescription = computed(() => {
  const descriptions: Record<string, string> = {
    '404': '抱歉，您访问的页面不存在或已被移除。',
    '500': '服务器开小差了，请稍后重试或联系管理员。',
    'network': '无法连接到服务器，请检查您的网络连接后重试。',
    '403': '抱歉，您没有权限访问此页面。',
    'unknown': '发生了意外错误，请稍后重试。'
  }
  return descriptions[props.type] || '请稍后重试。'
})

function handleRetry() {
  emit('retry')
}

function goHome() {
  router.push('/dashboard')
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.error-content {
  text-align: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 60px 80px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 500px;
}

.error-icon {
  margin-bottom: 20px;
  color: #909399;
}

.error-icon .el-icon {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.error-code {
  font-size: 72px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 10px 0;
  line-height: 1;
}

.error-message {
  font-size: 24px;
  color: #606266;
  margin: 0 0 10px 0;
}

.error-description {
  font-size: 14px;
  color: #909399;
  margin: 0 0 40px 0;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
