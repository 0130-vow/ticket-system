<template>
  <div class="category-manage">
    <div class="header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="showAddCategory">添加分类</el-button>
    </div>

    <el-table :data="categories" style="width: 100%" v-loading="loading" row-key="id" :tree-props="{ children: 'children' }">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" min-width="200" />
      <el-table-column prop="level" label="级别" width="100">
        <template #default="{ row }">
          <el-tag :type="row.level === 1 ? '' : 'success'">
            {{ row.level === 1 ? '一级分类' : '二级分类' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="showEditCategory(row)">编辑</el-button>
          <el-button size="small" type="success" link @click="showAddSubCategory(row)" v-if="row.level === 1">
            添加子分类
          </el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 标签管理区域 -->
    <div class="header" style="margin-top: 40px">
      <h2>标签管理</h2>
      <el-button type="primary" @click="showAddTag">添加标签</el-button>
    </div>

    <el-table :data="tags" style="width: 100%" v-loading="tagsLoading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="标签名称" min-width="200">
        <template #default="{ row }">
          <el-tag :color="row.color" style="color: #fff">{{ row.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="color" label="颜色" width="120">
        <template #default="{ row }">
          <div class="color-preview" :style="{ backgroundColor: row.color }"></div>
          <span>{{ row.color }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="showEditTag(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDeleteTag(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分类对话框 -->
    <el-dialog v-model="categoryDialogVisible" :title="categoryDialogTitle" width="500px">
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="categoryForm.parentId" placeholder="无（一级分类）" clearable style="width: 100%">
            <el-option v-for="cat in level1Categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCategory" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 标签对话框 -->
    <el-dialog v-model="tagDialogVisible" :title="tagDialogTitle" width="500px">
      <el-form :model="tagForm" :rules="tagRules" ref="tagFormRef" label-width="100px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <el-color-picker v-model="tagForm.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTag" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { categoryApi, tagApi } from '../api/ticket'
import { type Category, type Tag } from '../types/ticket'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)
const tagsLoading = ref(false)
const submitting = ref(false)

// 分类相关
const categoryDialogVisible = ref(false)
const categoryDialogTitle = ref('添加分类')
const categoryFormRef = ref<FormInstance>()
const editingCategoryId = ref<number | null>(null)

const categoryForm = reactive({
  name: '',
  parentId: null as number | null,
  sortOrder: 0
})

const categoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// 标签相关
const tagDialogVisible = ref(false)
const tagDialogTitle = ref('添加标签')
const tagFormRef = ref<FormInstance>()
const editingTagId = ref<number | null>(null)

const tagForm = reactive({
  name: '',
  color: '#409eff'
})

const tagRules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

// 一级分类列表（用于选择父分类）
const level1Categories = computed(() => {
  return categories.value.filter(c => c.level === 1)
})

onMounted(() => {
  loadCategories()
  loadTags()
})

async function loadCategories() {
  loading.value = true
  try {
    const { data: res } = await categoryApi.getList(true)
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('Failed to load categories:', error)
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  tagsLoading.value = true
  try {
    const { data: res } = await tagApi.getList()
    if (res.code === 200) {
      tags.value = res.data
    }
  } catch (error) {
    console.error('Failed to load tags:', error)
    ElMessage.error('加载标签列表失败')
  } finally {
    tagsLoading.value = false
  }
}

// 分类操作
function showAddCategory() {
  categoryDialogTitle.value = '添加分类'
  editingCategoryId.value = null
  categoryForm.name = ''
  categoryForm.parentId = null
  categoryForm.sortOrder = 0
  categoryDialogVisible.value = true
}

function showAddSubCategory(parent: Category) {
  categoryDialogTitle.value = '添加子分类'
  editingCategoryId.value = null
  categoryForm.name = ''
  categoryForm.parentId = parent.id
  categoryForm.sortOrder = 0
  categoryDialogVisible.value = true
}

function showEditCategory(category: Category) {
  categoryDialogTitle.value = '编辑分类'
  editingCategoryId.value = category.id
  categoryForm.name = category.name
  categoryForm.parentId = category.parentId || null
  categoryForm.sortOrder = category.sortOrder
  categoryDialogVisible.value = true
}

async function handleSubmitCategory() {
  if (!categoryFormRef.value) return

  await categoryFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const data = {
        name: categoryForm.name,
        parentId: categoryForm.parentId || 0,
        sortOrder: categoryForm.sortOrder
      }

      if (editingCategoryId.value) {
        const { data: res } = await categoryApi.update(editingCategoryId.value, data)
        if (res.code === 200) {
          ElMessage.success('分类更新成功')
          categoryDialogVisible.value = false
          loadCategories()
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } else {
        const { data: res } = await categoryApi.create(data)
        if (res.code === 200) {
          ElMessage.success('分类创建成功')
          categoryDialogVisible.value = false
          loadCategories()
        } else {
          ElMessage.error(res.message || '创建失败')
        }
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(category: Category) {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      type: 'warning'
    })

    const { data: res } = await categoryApi.delete(category.id)
    if (res.code === 200) {
      ElMessage.success('分类删除成功')
      loadCategories()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 标签操作
function showAddTag() {
  tagDialogTitle.value = '添加标签'
  editingTagId.value = null
  tagForm.name = ''
  tagForm.color = '#409eff'
  tagDialogVisible.value = true
}

function showEditTag(tag: Tag) {
  tagDialogTitle.value = '编辑标签'
  editingTagId.value = tag.id
  tagForm.name = tag.name
  tagForm.color = tag.color
  tagDialogVisible.value = true
}

async function handleSubmitTag() {
  if (!tagFormRef.value) return

  await tagFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (editingTagId.value) {
        const { data: res } = await tagApi.update(editingTagId.value, tagForm)
        if (res.code === 200) {
          ElMessage.success('标签更新成功')
          tagDialogVisible.value = false
          loadTags()
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } else {
        const { data: res } = await tagApi.create(tagForm)
        if (res.code === 200) {
          ElMessage.success('标签创建成功')
          tagDialogVisible.value = false
          loadTags()
        } else {
          ElMessage.error(res.message || '创建失败')
        }
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

async function handleDeleteTag(tag: Tag) {
  try {
    await ElMessageBox.confirm('确定要删除该标签吗？删除后关联的工单标签也会被移除。', '提示', {
      type: 'warning'
    })

    const { data: res } = await tagApi.delete(tag.id)
    if (res.code === 200) {
      ElMessage.success('标签删除成功')
      loadTags()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.category-manage {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.color-preview {
  display: inline-block;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  margin-right: 8px;
  vertical-align: middle;
}
</style>
