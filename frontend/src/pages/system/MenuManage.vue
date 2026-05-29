<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">菜单管理</h2>
      <el-button type="primary" @click="handleAdd()">
        <el-icon><Plus /></el-icon>
        新增菜单
      </el-button>
    </div>

    <!-- 菜单树表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="menuTree"
        row-key="id"
        :tree-props="{ children: 'children' }"
        border
        default-expand-all
        class="menu-table"
      >
        <el-table-column prop="name" label="菜单名称" min-width="180" />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon" class="menu-icon">
              <component :is="row.icon" />
            </el-icon>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'M'" type="danger" size="small">目录</el-tag>
            <el-tag v-else-if="row.type === 'C'" type="primary" size="small">菜单</el-tag>
            <el-tag v-else-if="row.type === 'F'" type="info" size="small">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column prop="permission" label="权限标识" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.permission">{{ row.permission }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.path">{{ row.path }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="component" label="组件路径" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.component">{{ row.component }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="danger" size="small">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="primary" link size="small" @click="handleAdd(row)">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="620px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="menu-form"
      >
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentTreeOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            :render-after-expand="false"
            placeholder="请选择上级菜单（留空为顶级）"
            clearable
            class="form-input"
          />
        </el-form-item>

        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" class="form-input" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
        </el-form-item>

        <!-- 目录/菜单 共有字段：图标 -->
        <el-form-item v-if="form.type === 'M' || form.type === 'C'" label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称" class="form-input" />
        </el-form-item>

        <!-- 目录/菜单 共有字段：路由路径 -->
        <el-form-item v-if="form.type === 'M' || form.type === 'C'" label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由路径" class="form-input" />
        </el-form-item>

        <!-- 菜单专有字段：组件路径 -->
        <el-form-item v-if="form.type === 'C'" label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件路径" class="form-input" />
        </el-form-item>

        <!-- 菜单/按钮 共有字段：权限标识 -->
        <el-form-item v-if="form.type === 'C' || form.type === 'F'" label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="请输入权限标识" class="form-input" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确 定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getMenuTree, getMenuDetail, createMenu, updateMenu, deleteMenu } from '@/api/menu'

// ======================== 状态 ========================

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const menuTree = ref([])

const form = reactive({
  id: undefined,
  parentId: undefined,
  type: 'M',
  name: '',
  icon: '',
  path: '',
  component: '',
  permission: '',
  sort: 0,
  status: 1
})

const isEdit = ref(false)

// ======================== 计算属性 ========================

const dialogTitle = computed(() => (isEdit.value ? '编辑菜单' : '新增菜单'))

/**
 * 上级菜单树选项：为顶级节点增加"根目录"选项
 */
const parentTreeOptions = computed(() => {
  return [{ id: 0, name: '根目录', children: menuTree.value }]
})

// ======================== 表单校验 ========================

const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

// ======================== 数据加载 ========================

/**
 * 加载菜单树
 */
const fetchMenuTree = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    menuTree.value = res.data || []
  } catch (error) {
    ElMessage.error('加载菜单数据失败')
  } finally {
    loading.value = false
  }
}

// ======================== 对话框操作 ========================

/**
 * 重置表单
 */
const resetForm = () => {
  form.id = undefined
  form.parentId = undefined
  form.type = 'M'
  form.name = ''
  form.icon = ''
  form.path = ''
  form.component = ''
  form.permission = ''
  form.sort = 0
  form.status = 1
  formRef.value?.resetFields()
}

/**
 * 类型切换时清除不相关字段
 */
const handleTypeChange = () => {
  // 切换类型时清空非共有字段，避免残留数据
  if (form.type === 'F') {
    form.icon = ''
    form.path = ''
    form.component = ''
  } else if (form.type === 'M') {
    form.component = ''
    form.permission = ''
  }
  // type === 'C' 保留所有字段
}

/**
 * 新增菜单
 * @param {Object} [parent] - 父菜单行数据，传入时自动填充 parentId
 */
const handleAdd = (parent) => {
  isEdit.value = false
  resetForm()
  if (parent) {
    form.parentId = parent.id
    form.type = parent.type === 'M' ? 'C' : 'F'
  }
  dialogVisible.value = true
}

/**
 * 编辑菜单
 */
const handleEdit = async (row) => {
  isEdit.value = true
  resetForm()
  try {
    const res = await getMenuDetail(row.id)
    const data = res.data
    Object.keys(form).forEach((key) => {
      if (data[key] !== undefined && data[key] !== null) {
        form[key] = data[key]
      }
    })
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取菜单详情失败')
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const submitData = { ...form }
    // parentId 为 0 或 undefined 时表示顶级菜单
    if (submitData.parentId === 0) {
      submitData.parentId = undefined
    }

    if (isEdit.value) {
      await updateMenu(submitData)
      ElMessage.success('修改成功')
    } else {
      await createMenu(submitData)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    await fetchMenuTree()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 删除菜单
 */
const handleDelete = (row) => {
  // 检查是否有子菜单
  if (row.children && row.children.length > 0) {
    ElMessage.error('该菜单下存在子菜单，无法删除')
    return
  }

  ElMessageBox.confirm(
    `确定要删除菜单「${row.name}」吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteMenu(row.id)
      ElMessage.success('删除成功')
      await fetchMenuTree()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// ======================== 生命周期 ========================

onMounted(() => {
  fetchMenuTree()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.page-header :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.page-header :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

.table-card {
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.menu-table {
  width: 100%;
}

/* 表格头部样式 */
.menu-table :deep(.el-table__header th) {
  background: #f5f7fa;
  color: #333;
  font-weight: 600;
}

/* 表格行悬停 */
.menu-table :deep(.el-table__body tr:hover > td) {
  background: #f0f5ff;
}

/* 菜单图标 */
.menu-icon {
  font-size: 18px;
  color: #667eea;
}

/* 空值文字 */
.text-muted {
  color: #c0c4cc;
}

/* 操作按钮样式 */
.menu-table :deep(.el-button--primary) {
  --el-button-text-color: #667eea;
  --el-button-hover-text-color: #5568d3;
}

.menu-table :deep(.el-button--danger) {
  --el-button-text-color: #f56c6c;
  --el-button-hover-text-color: #e04040;
}

/* 对话框样式 */
:deep(.el-dialog__header) {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 16px;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

/* 确认按钮渐变 */
:deep(.el-dialog__footer .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.el-dialog__footer .el-button--primary:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

/* 表单样式 */
.menu-form {
  padding-right: 20px;
}

.menu-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.form-input {
  width: 100%;
}

.menu-form :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #667eea;
  border-color: #667eea;
}

.menu-form :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #667eea;
}

.menu-form :deep(.el-input-number .el-input-number__decrease:hover),
.menu-form :deep(.el-input-number .el-input-number__increase:hover) {
  color: #667eea;
}

/* 标签微调 */
.menu-table :deep(.el-tag--danger) {
  --el-tag-bg-color: #fef0f0;
  --el-tag-border-color: #fde2e2;
  --el-tag-text-color: #f56c6c;
}

.menu-table :deep(.el-tag--primary) {
  --el-tag-bg-color: #f0f5ff;
  --el-tag-border-color: #d9e4ff;
  --el-tag-text-color: #667eea;
}

.menu-table :deep(.el-tag--info) {
  --el-tag-bg-color: #f4f4f5;
  --el-tag-border-color: #e9e9eb;
  --el-tag-text-color: #909399;
}

.menu-table :deep(.el-tag--success) {
  --el-tag-bg-color: #f0f9eb;
  --el-tag-border-color: #e1f3d8;
  --el-tag-text-color: #67c23a;
}

/* tree-select 选中色 */
:deep(.el-tree-node.is-current > .el-tree-node__content) {
  color: #667eea;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .menu-form {
    padding-right: 0;
  }

  :deep(.el-dialog) {
    width: 92% !important;
  }
}
</style>
