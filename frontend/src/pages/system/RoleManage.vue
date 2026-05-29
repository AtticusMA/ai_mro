<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="角色名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入角色名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input
            v-model="queryParams.code"
            placeholder="请输入角色编码"
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

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">角色列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增角色
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="name" label="角色名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="角色编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="warning" link size="small" @click="handleAssignPerm(row)">
              <el-icon><Key /></el-icon>分配权限
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:page="queryParams.page"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @change="getPageList"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="permDialogVisible"
      title="分配权限"
      width="500px"
      destroy-on-close
      @open="handlePermDialogOpen"
    >
      <el-tree
        ref="treeRef"
        :data="menuTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="defaultCheckedKeys"
        :check-strictly="false"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="permSubmitLoading" @click="handlePermSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRolePage, getRoleDetail, createRole, updateRole, deleteRole } from '@/api/role'
import { getMenuTree } from '@/api/menu'
import Pagination from '@/components/Pagination.vue'

// 查询参数
const queryParams = reactive({
  name: '',
  code: '',
  status: undefined,
  page: 1,
  pageSize: 10
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 新增/编辑对话框
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: undefined,
  name: '',
  code: '',
  sort: 0,
  status: 1,
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const dialogTitle = computed(() => (formData.id ? '编辑角色' : '新增角色'))

// 分配权限对话框
const permDialogVisible = ref(false)
const permSubmitLoading = ref(false)
const treeRef = ref(null)
const menuTree = ref([])
const defaultCheckedKeys = ref([])
const currentRole = ref(null)

/**
 * 获取角色分页列表
 */
const getPageList = async () => {
  loading.value = true
  try {
    const res = await getRolePage(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  queryParams.page = 1
  getPageList()
}

/**
 * 重置
 */
const handleReset = () => {
  queryParams.name = ''
  queryParams.code = ''
  queryParams.status = undefined
  queryParams.page = 1
  queryParams.pageSize = 10
  getPageList()
}

/**
 * 重置表单
 */
const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.code = ''
  formData.sort = 0
  formData.status = 1
  formData.remark = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

/**
 * 新增角色
 */
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

/**
 * 编辑角色
 */
const handleEdit = async (row) => {
  resetForm()
  try {
    const res = await getRoleDetail(row.id)
    if (res.code === 200) {
      const data = res.data
      formData.id = data.id
      formData.name = data.name
      formData.code = data.code
      formData.sort = data.sort
      formData.status = data.status
      formData.remark = data.remark
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取角色详情失败')
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
    const res = formData.id
      ? await updateRole({ ...formData })
      : await createRole({ ...formData })
    if (res.code === 200) {
      ElMessage.success(formData.id ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      getPageList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 删除角色
 */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const res = await deleteRole(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          getPageList()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

/**
 * 分配权限 - 打开对话框
 */
const handleAssignPerm = (row) => {
  currentRole.value = row
  defaultCheckedKeys.value = []
  permDialogVisible.value = true
}

/**
 * 权限对话框打开时加载数据
 */
const handlePermDialogOpen = async () => {
  try {
    // 加载菜单树
    const treeRes = await getMenuTree()
    if (treeRes.code === 200) {
      menuTree.value = treeRes.data
    }

    // 加载角色详情获取已分配菜单
    if (currentRole.value) {
      const detailRes = await getRoleDetail(currentRole.value.id)
      if (detailRes.code === 200 && detailRes.data.menuIds) {
        // 过滤出叶子节点，避免父节点勾选导致子节点全部被选中
        const leafIds = filterLeafIds(menuTree.value, detailRes.data.menuIds)
        defaultCheckedKeys.value = leafIds
      }
    }
  } catch (error) {
    ElMessage.error('加载权限数据失败')
  }
}

/**
 * 从菜单树中过滤出叶子节点 ID
 * el-tree 在 check-strictly=false 时，设置 default-checked-keys 只应包含叶子节点
 */
const filterLeafIds = (tree, checkedIds) => {
  const leafIds = []
  const findLeaf = (nodes) => {
    if (!nodes) return
    for (const node of nodes) {
      if (node.children && node.children.length > 0) {
        findLeaf(node.children)
      } else {
        if (checkedIds.includes(node.id)) {
          leafIds.push(node.id)
        }
      }
    }
  }
  findLeaf(tree)
  return leafIds
}

/**
 * 提交权限分配
 */
const handlePermSubmit = async () => {
  if (!treeRef.value || !currentRole.value) return

  permSubmitLoading.value = true
  try {
    // 获取所有选中的节点（包含半选的父节点）
    const checkedKeys = treeRef.value.getCheckedKeys()
    const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
    const menuIds = [...checkedKeys, ...halfCheckedKeys]

    const res = await updateRole({
      id: currentRole.value.id,
      menuIds
    })

    if (res.code === 200) {
      ElMessage.success('权限分配成功')
      permDialogVisible.value = false
      getPageList()
    } else {
      ElMessage.error(res.message || '权限分配失败')
    }
  } catch (error) {
    ElMessage.error('权限分配失败')
  } finally {
    permSubmitLoading.value = false
  }
}

/**
 * 组件挂载
 */
onMounted(() => {
  getPageList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
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
  color: #303133;
}

.search-card :deep(.el-input__wrapper),
.search-card :deep(.el-select__wrapper) {
  width: 200px;
}

.table-card :deep(.el-table) {
  font-size: 14px;
}

.table-card :deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.table-card :deep(.el-button--primary) {
  --el-button-bg-color: #667eea;
  --el-button-border-color: #667eea;
  --el-button-hover-bg-color: #5568d3;
  --el-button-hover-border-color: #5568d3;
}

.table-card :deep(.el-button--primary.is-link) {
  --el-button-text-color: #667eea;
  --el-button-hover-text-color: #5568d3;
}

.table-card :deep(.el-button--warning.is-link) {
  --el-button-text-color: #e6a23c;
  --el-button-hover-text-color: #c8861f;
}

.search-card :deep(.el-button--primary) {
  --el-button-bg-color: #667eea;
  --el-button-border-color: #667eea;
  --el-button-hover-bg-color: #5568d3;
  --el-button-hover-border-color: #5568d3;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #e4e7eb;
  padding-bottom: 15px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #e4e7eb;
  padding-top: 15px;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog .el-button--primary) {
  --el-button-bg-color: #667eea;
  --el-button-border-color: #667eea;
  --el-button-hover-bg-color: #5568d3;
  --el-button-hover-border-color: #5568d3;
}

:deep(.el-tree) {
  max-height: 400px;
  overflow-y: auto;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #f0f2ff;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #667eea;
  border-color: #667eea;
}

:deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  background-color: #667eea;
  border-color: #667eea;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #667eea;
  border-color: #667eea;
}

:deep(.el-input-number .el-input-number__decrease:hover),
:deep(.el-input-number .el-input-number__increase:hover) {
  color: #667eea;
}

@media (max-width: 768px) {
  .page-container {
    padding: 15px;
  }

  .search-card :deep(.el-input__wrapper),
  .search-card :deep(.el-select__wrapper) {
    width: 160px;
  }
}
</style>
