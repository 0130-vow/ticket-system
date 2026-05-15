<template>
  <div class="create-ticket">
    <h2>新建工单</h2>
    
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 600px">
      <el-form-item label="工单标题" prop="title">
        <el-input v-model="form.title" placeholder="【系统名称+问题现象】" maxlength="50" show-word-limit />
      </el-form-item>
      
      <el-form-item label="问题描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请详细描述故障现象、影响范围、复现步骤" />
      </el-form-item>
      
      <el-form-item label="优先级" prop="priority">
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
      
      <el-form-item label="负责人" prop="assigneeId">
        <el-select v-model="form.assigneeId" placeholder="请选择负责人" style="width: 100%">
          <el-option v-for="user in users" :key="user.id" :label="user.name" :value="user.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="工单分类">
        <el-cascader
          v-model="form.categoryId"
          :options="categoryOptions"
          :props="{ checkStrictly: true, value: 'id', label: 'name', children: 'children' }"
          placeholder="请选择工单分类"
          clearable
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="标签">
        <el-select v-model="form.tagIds" multiple placeholder="请选择标签" style="width: 100%">
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id">
            <el-tag :color="tag.color" style="color: #fff; margin-right: 8px">{{ tag.name }}</el-tag>
          </el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">提交工单</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ticketApi, userApi, categoryApi, tagApi } from '../api/ticket'
import { type User, type Category, type Tag } from '../types/ticket'
import { ElMessage, type FormInstance } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const users = ref<User[]>([])
const categoryOptions = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)

const form = reactive({
  title: '',
  description: '',
  priority: 'P2',
  assigneeId: null as number | null,
  categoryId: null as number | null,
  tagIds: [] as number[]
})

// XSS 防护：过滤特殊字符
function sanitizeInput(value: string) {
  return value
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .replace(/\//g, '&#x2F;')
}

const validateTitle = (_: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入工单标题'))
  } else if (value.length < 2) {
    callback(new Error('标题至少2个字符'))
  } else if (value.length > 50) {
    callback(new Error('标题最多50个字符'))
  } else if (/[<>\"'&]/.test(value)) {
    callback(new Error('标题包含非法字符'))
  } else {
    callback()
  }
}

const validateDescription = (_: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入问题描述'))
  } else if (value.length < 10) {
    callback(new Error('问题描述至少10个字符'))
  } else if (value.length > 2000) {
    callback(new Error('问题描述最多2000个字符'))
  } else {
    callback()
  }
}

const rules = {
  title: [{ validator: validateTitle, trigger: 'blur' }],
  description: [{ validator: validateDescription, trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  assigneeId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

onMounted(() => {
  loadUsers()
  loadCategories()
  loadTags()
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

async function loadTags() {
  try {
    const { data: res } = await tagApi.getList()
    if (res.code === 200) {
      tags.value = res.data
    }
  } catch (error) {
    console.error('Failed to load tags:', error)
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 提交前清理输入，防止 XSS
      const submitData = {
        ...form,
        title: sanitizeInput(form.title),
        description: sanitizeInput(form.description),
        // cascader 返回数组，取最后一个值作为最终分类ID
        categoryId: Array.isArray(form.categoryId)
          ? form.categoryId[form.categoryId.length - 1]
          : form.categoryId
      }
      const { data: res } = await ticketApi.create(submitData)
      if (res.code === 200) {
        ElMessage.success('工单创建成功')
        router.push('/tickets')
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '创建失败')
    } finally {
      loading.value = false
    }
  })
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
