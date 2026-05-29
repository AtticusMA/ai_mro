<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="字典组">
          <el-input
            v-model="queryParams.dictType"
            placeholder="请输入字典组（如：sex）"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="字典码">
          <el-input
            v-model="queryParams.value"
            placeholder="请输入字典码"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="字典值">
          <el-input
            v-model="queryParams.label"
            placeholder="请输入字典值"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 字典表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">字典列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增字典
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="pagedList" border stripe>
        <el-table-column prop="dictType" label="字典组" min-width="160" show-overflow-tooltip />
        <el-table-column prop="value" label="字典码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="label" label="字典值" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        :page="queryParams.page"
        :limit="queryParams.pageSize"
        @update:page="queryParams.page = $event"
        @update:limit="queryParams.pageSize = $event"
        @change="refreshPage"
      />
    </el-card>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="字典组" prop="dictType">
          <el-input v-model="form.dictType" placeholder="请输入字典组（如：sex）" />
        </el-form-item>
        <el-form-item label="字典码" prop="value">
          <el-input v-model="form.value" placeholder="请输入字典码（如：0）" />
        </el-form-item>
        <el-form-item label="字典值" prop="label">
          <el-input v-model="form.label" placeholder="请输入字典值（如：女）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDictItemList,
  createDictItem,
  updateDictItem,
  deleteDictItem
} from '@/api/dict'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const allList = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const queryParams = reactive({
  dictType: '',
  value: '',
  label: '',
  status: '',
  page: 1,
  pageSize: 10
})

const form = reactive({
  id: undefined,
  dictType: '',
  value: '',
  label: '',
  status: 1
})

const formRules = {
  dictType: [{ required: true, message: '请输入字典组', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典码', trigger: 'blur' }],
  label: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

const dialogTitle = computed(() => (form.id ? '编辑字典' : '新增字典'))

/**
 * 过滤后列表（按字典组 / 字典码 / 字典值 / 状态）
 */
const filteredList = computed(() => {
  return allList.value.filter((row) => {
    if (queryParams.dictType && !String(row.dictType || '').includes(queryParams.dictType)) return false
    if (queryParams.value && !String(row.value || '').includes(queryParams.value)) return false
    if (queryParams.label && !String(row.label || '').includes(queryParams.label)) return false
    if (queryParams.status !== '' && queryParams.status !== undefined && row.status !== Number(queryParams.status)) return false
    return true
  })
})

const total = computed(() => filteredList.value.length)

const pagedList = computed(() => {
  const start = (queryParams.page - 1) * queryParams.pageSize
  return filteredList.value.slice(start, start + queryParams.pageSize)
})

/**
 * 加载所有字典数据
 */
const loadList = async () => {
  loading.value = true
  try {
    const res = await getDictItemList({})
    if (res.code === 200) {
      allList.value = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    }
  } catch (error) {
    ElMessage.error('获取字典列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
}

const handleReset = () => {
  queryParams.dictType = ''
  queryParams.value = ''
  queryParams.label = ''
  queryParams.status = ''
  queryParams.page = 1
}

const refreshPage = () => {
  // 触发 computed 重新求值，pagedList 会随 page/pageSize 自动变化
}

const resetForm = () => {
  form.id = undefined
  form.dictType = ''
  form.value = ''
  form.label = ''
  form.status = 1
  formRef.value?.resetFields()
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  form.id = row.id
  form.dictType = row.dictType
  form.value = row.value
  form.label = row.label
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const payload = {
      id: form.id,
      dictType: form.dictType,
      value: form.value,
      label: form.label,
      status: form.status
    }
    const api = form.id ? updateDictItem : createDictItem
    const res = await api(payload)
    if (res.code === 200) {
      ElMessage.success(form.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认删除字典「${row.dictType} / ${row.value} / ${row.label}」吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteDictItem(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.search-card :deep(.el-card__body) {
  padding: 18px 20px 0;
}

.search-card :deep(.el-form-item) {
  margin-bottom: 18px;
}

.table-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.table-card :deep(.el-card__header) {
  border-bottom: 1px solid #e4e7eb;
  padding: 14px 20px;
}

.table-card :deep(.el-card__body) {
  padding: 20px;
}

/* 按钮主题 */
:deep(.el-button--primary) {
  --el-button-bg-color: #667eea;
  --el-button-border-color: #667eea;
  --el-button-hover-bg-color: #5568d3;
  --el-button-hover-border-color: #5568d3;
  --el-button-active-bg-color: #4a5bc2;
  --el-button-active-border-color: #4a5bc2;
}

:deep(.el-button--primary.is-link) {
  --el-button-text-color: #667eea;
  --el-button-hover-text-color: #5568d3;
}

:deep(.el-button--danger.is-link) {
  --el-button-text-color: #f56c6c;
  --el-button-hover-text-color: #dd5a5a;
}

/* 表格 */
:deep(.el-table) {
  border-radius: 4px;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa;
  color: #333;
  font-weight: 600;
}

:deep(.el-table .el-table__row:hover > td) {
  background-color: #f0f2ff !important;
}

/* 标签 */
:deep(.el-tag--success) {
  --el-tag-bg-color: #f0f9eb;
  --el-tag-border-color: #e1f3d8;
  --el-tag-text-color: #67c23a;
}

:deep(.el-tag--danger) {
  --el-tag-bg-color: #fef0f0;
  --el-tag-border-color: #fde2e2;
  --el-tag-text-color: #f56c6c;
}

/* 单选框 */
:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #667eea;
  border-color: #667eea;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #667eea;
}

/* 输入框聚焦 */
:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #667eea inset !important;
}

/* Select 选中色 */
:deep(.el-select .el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px #667eea inset !important;
}

/* 分页选中色 */
:deep(.el-pagination .el-pager li.active) {
  background-color: #667eea;
  color: #fff;
}

:deep(.el-pagination .el-pager li:hover) {
  color: #667eea;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 12px;
  }

  .search-card :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>
