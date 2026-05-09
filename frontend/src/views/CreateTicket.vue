<template>
  <div class="create-ticket">
    <h2>新建工单</h2>
    
    <el-form :model="form" label-width="100px" style="max-width: 600px">
      <el-form-item label="工单标题" required>
        <el-input v-model="form.title" placeholder="【系统名称+问题现象】" maxlength="20" show-word-limit />
      </el-form-item>
      
      <el-form-item label="问题描述" required>
        <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请详细描述故障现象、影响范围、复现步骤" />
      </el-form-item>
      
      <el-form-item label="优先级" required>
        <el-radio-group v-model="form.priority">
          <el-radio-button label="P0">
            <span style="color: #f56c6c">紧急</span>
          </el-radio-button>
          <el-radio-button label="P1">
            <span style="color: #e6a23c">高</span>
          </el-radio-button>
          <el-radio-button label="P2">普通</el-radio-button>
          <el-radio-button label="P3">低</el-radio-button>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="负责人" required>
        <el-select v-model="form.assignee" placeholder="请选择负责人">
          <el-option v-for="user in users" :key="user.id" :label="user.name" :value="user.id" />
        </el-select>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交工单</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ticketApi, userApi } from '../api/ticket'
import { ElMessage } from 'element-plus'

const router = useRouter()
const users = ref([])

const form = reactive({
  title: '',
  description: '',
  priority: 'P2',
  assignee: ''
})

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  try {
    const { data } = await userApi.getUsers()
    users.value = data
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

async function handleSubmit() {
  try {
    await ticketApi.create(form)
    ElMessage.success('工单创建成功')
    router.push('/tickets')
  } catch (error) {
    ElMessage.error('创建失败')
  }
}
</script>

<style scoped>
.create-ticket {
  padding: 20px;
  max-width: 700px;
}
h2 {
  margin-bottom: 30px;
}
</style>
