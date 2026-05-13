<template>
  <el-container v-if="userStore.token" style="height: 100vh">
    <el-aside width="200px" style="background: #304156">
      <div class="logo">工单系统</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/tickets">
          <el-icon><Tickets /></el-icon>
          <span>工单列表</span>
        </el-menu-item>
        <el-menu-item index="/kanban">
          <el-icon><Grid /></el-icon>
          <span>看板视图</span>
        </el-menu-item>
        <el-menu-item index="/stats">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计报表</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header style="background: #fff; border-bottom: 1px solid #dcdfe6; display: flex; justify-content: space-between; align-items: center">
        <span></span>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            {{ userStore.userInfo?.name || '用户' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
  
  <router-view v-else />
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { HomeFilled, Tickets, Grid, DataAnalysis, ArrowDown } from '@element-plus/icons-vue'
import { onErrorCaptured } from 'vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

// 全局错误处理
onErrorCaptured((err, _, info) => {
  console.error('Global error captured:', err, info)
  ElMessage.error('发生了一个错误，请稍后重试')
  return false
})

// 监听未处理的 Promise 错误
window.addEventListener('unhandledrejection', (event) => {
  console.error('Unhandled promise rejection:', event.reason)
  ElMessage.error('操作失败，请稍后重试')
})

// 监听全局错误
window.addEventListener('error', (event) => {
  console.error('Global error:', event.error)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
.logo {
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}
</style>
