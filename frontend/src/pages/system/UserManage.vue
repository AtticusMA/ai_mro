<template>
  <div class="user-manage">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="queryParams.realName"
            placeholder="请输入姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="queryParams.phone"
            placeholder="请输入手机号"
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
          <span class="title">用户列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增用户
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="username" label="用户名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="realName" label="姓名" min-width="90" show-overflow-tooltip />
        <el-table-column prop="deptName" label="部门" min-width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="warning" link @click="handleResetPwd(row)">
              <el-icon><RefreshRight /></el-icon>重置密码
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        :total="total"
        :page="queryParams.page"
        :limit="queryParams.pageSize"
        @update:page="queryParams.page = $event"
        @update:limit="queryParams.pageSize = $event"
        @change="fetchData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="600px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="formData.username"
                placeholder="请输入用户名"
                :disabled="dialogType === 'edit'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col v-if="dialogType === 'add'" :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="formData.password"
                type="password"
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>
          </el-col>
          <el-col :span="dialogType === 'add' ? 12 : 12">
            <el-form-item label="部门" prop="deptId">
              <el-tree-select
                v-model="formData.deptId"
                :data="deptTreeData"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                placeholder="请选择部门"
                check-strictly
                filterable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="roleIds">
              <el-select
                v-model="formData.roleIds"
                multiple
                placeholder="请选择角色"
                style="width: 100%"
              >
                <el-option
                  v-for="role in roleList"
                  :key="role.id"
                  :label="role.name"
                  :value="role.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getUserPage, getUserDetail, createUser, updateUser, deleteUser, resetUserPassword } from '@/api/user'
import { getDeptList } from '@/api/dept'
import { getRoleList } from '@/api/role'

// ======================== 搜索相关 ========================

const queryParams = reactive({
  username: '',
  realName: '',
  phone: '',
  status: undefined,
  page: 1,
  pageSize: 10
})

const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

const handleReset = () => {
  queryParams.username = ''
  queryParams.realName = ''
  queryParams.phone = ''
  queryParams.status = undefined
  queryParams.page = 1
  queryParams.pageSize = 10
  fetchData()
}

// ======================== 表格相关 ========================

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedRows = ref([])

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

/**
 * 获取用户分页数据
 */
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// ======================== 对话框相关 ========================

const dialogVisible = ref(false)
const dialogType = ref('add')
const submitLoading = ref(false)
const formRef = ref(null)

const initFormData = () => ({
  id: undefined,
  username: '',
  realName: '',
  password: '',
  deptId: undefined,
  roleIds: [],
  phone: '',
  email: '',
  gender: 1,
  status: 1
})

const formData = reactive(initFormData())

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  deptId: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  roleIds: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

/**
 * 重置表单数据
 */
const resetForm = () => {
  const init = initFormData()
  Object.keys(init).forEach((key) => {
    formData[key] = init[key]
  })
  formRef.value?.resetFields()
}

/**
 * 对话框关闭后重置表单
 */
const handleDialogClosed = () => {
  resetForm()
}

/**
 * 新增用户
 */
const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

/**
 * 编辑用户
 */
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  resetForm()
  try {
    const res = await getUserDetail(row.id)
    const detail = res.data
    Object.keys(formData).forEach((key) => {
      if (detail[key] !== undefined) {
        formData[key] = detail[key]
      }
    })
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '获取用户详情失败')
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
    if (dialogType.value === 'add') {
      await createUser(formData)
      ElMessage.success('新增用户成功')
    } else {
      await updateUser(formData)
      ElMessage.success('更新用户成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// ======================== 删除 & 重置密码 ========================

/**
 * 删除用户
 */
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认要删除用户「${row.realName}」吗？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

/**
 * 重置密码
 */
const handleResetPwd = (row) => {
  ElMessageBox.confirm(
    `确认要重置用户「${row.realName}」的密码吗？重置后密码将恢复为默认密码。`,
    '重置密码确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await resetUserPassword(row.id)
      ElMessage.success('密码重置成功')
    } catch (error) {
      ElMessage.error(error.message || '密码重置失败')
    }
  }).catch(() => {})
}

// ======================== 部门 & 角色数据 ========================

const deptTreeData = ref([])
const roleList = ref([])

/**
 * 加载部门树数据
 */
const loadDeptTree = async () => {
  try {
    const res = await getDeptList()
    deptTreeData.value = res.data
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

/**
 * 加载角色列表
 */
const loadRoleList = async () => {
  try {
    const res = await getRoleList()
    roleList.value = res.data
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// ======================== 初始化 ========================

onMounted(() => {
  fetchData()
  loadDeptTree()
  loadRoleList()
})
</script>

<style scoped>
.user-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 搜索卡片 */
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

.search-card :deep(.el-input) {
  width: 200px;
}

.search-card :deep(.el-select) {
  width: 200px;
}

.search-card :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.search-card :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

/* 表格卡片 */
.table-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.table-card :deep(.el-card__header) {
  border-bottom: 1px solid #e4e7eb;
  padding: 16px 20px;
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

.card-header :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.card-header :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

/* 表格样式 */
.table-card :deep(.el-table) {
  font-size: 14px;
}

.table-card :deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.table-card :deep(.el-table .el-button--primary) {
  color: #667eea;
}

.table-card :deep(.el-table .el-button--primary:hover) {
  color: #5568d3;
}

.table-card :deep(.el-table .el-button--warning) {
  color: #e6a23c;
}

.table-card :deep(.el-table .el-button--warning:hover) {
  color: #cf9236;
}

.table-card :deep(.el-table .el-button--danger) {
  color: #f56c6c;
}

.table-card :deep(.el-table .el-button--danger:hover) {
  color: #dd6161;
}

/* 对话框样式 */
:deep(.el-dialog__header) {
  border-bottom: 1px solid #e4e7eb;
  padding: 16px 20px;
  margin-right: 0;
}

:deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #e4e7eb;
  padding: 12px 20px;
}

:deep(.el-dialog__footer .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.el-dialog__footer .el-button--primary:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

/* 状态标签 */
:deep(.el-tag--success) {
  background-color: #f0f9eb;
  border-color: #e1f3d8;
  color: #67c23a;
}

:deep(.el-tag--danger) {
  background-color: #fef0f0;
  border-color: #fde2e2;
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-card :deep(.el-input),
  .search-card :deep(.el-select) {
    width: 100%;
  }

  .search-card :deep(.el-form-item) {
    margin-right: 0;
  }

  :deep(.el-dialog) {
    width: 90% !important;
  }

  .table-card :deep(.el-table__fixed-right) {
    width: auto !important;
  }
}
</style>
