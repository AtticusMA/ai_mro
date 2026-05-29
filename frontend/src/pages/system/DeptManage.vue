<template>
  <div class="dept-manage">
    <el-row :gutter="20">
      <!-- 左侧部门树 -->
      <el-col :span="8">
        <el-card class="tree-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="title">部门架构</span>
            </div>
          </template>
          <el-input
            v-model="treeFilter"
            placeholder="搜索部门"
            clearable
            prefix-icon="Search"
            class="tree-search"
          />
          <el-tree
            ref="treeRef"
            :data="deptTree"
            :props="{ label: 'name', children: 'children' }"
            :filter-node-method="filterNode"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          />
        </el-card>
      </el-col>

      <!-- 右侧部门列表 -->
      <el-col :span="16">
        <el-card class="table-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="title">
                {{ currentNode ? currentNode.name : '全部部门' }}
              </span>
              <el-button type="primary" icon="Plus" @click="handleAdd()">
                新增部门
              </el-button>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="tableData"
            row-key="id"
            border
            default-expand-all
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          >
            <el-table-column prop="name" label="部门名称" min-width="160" />
            <el-table-column prop="sort" label="排序" width="80" align="center" />
            <el-table-column prop="leader" label="负责人" width="120" align="center" />
            <el-table-column prop="phone" label="联系电话" width="140" align="center" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link icon="Edit" @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button type="success" link icon="Plus" @click="handleAdd(row)">
                  新增
                </el-button>
                <el-button type="danger" link icon="Delete" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTreeSelect"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            :render-after-expand="false"
            placeholder="请选择上级部门"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="9999"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptList, getDeptDetail, createDept, updateDept, deleteDept } from '@/api/dept'

// ======================== 左侧树相关 ========================

const treeRef = ref(null)
const treeFilter = ref('')
const deptTree = ref([])
const currentNode = ref(null)
const loading = ref(false)

/**
 * 树节点过滤
 */
const filterNode = (value, data) => {
  if (!value) return true
  return data.name.includes(value)
}

/**
 * 监听搜索关键字过滤树
 */
watch(treeFilter, (val) => {
  treeRef.value?.filter(val)
})

/**
 * 点击树节点
 */
const handleNodeClick = (data) => {
  currentNode.value = data
}

// ======================== 右侧表格相关 ========================

/**
 * 表格展示数据：根据左侧选中节点显示其子部门
 */
const tableData = computed(() => {
  if (!currentNode.value) return deptTree.value
  return currentNode.value.children ? [currentNode.value] : []
})

// ======================== 对话框相关 ========================

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = ref({
  id: undefined,
  parentId: undefined,
  name: '',
  sort: 0,
  leader: '',
  phone: '',
  email: '',
  status: 1
})

const formRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

/**
 * 用于 el-tree-select 的部门树（增加顶级选项）
 */
const deptTreeSelect = computed(() => {
  return [{ id: 0, name: '顶级部门', children: deptTree.value }]
})

/**
 * 新增部门
 */
const handleAdd = (row) => {
  isEdit.value = false
  dialogTitle.value = '新增部门'
  form.value = {
    id: undefined,
    parentId: row ? row.id : 0,
    name: '',
    sort: 0,
    leader: '',
    phone: '',
    email: '',
    status: 1
  }
  dialogVisible.value = true
}

/**
 * 编辑部门
 */
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑部门'
  try {
    const res = await getDeptDetail(row.id)
    if (res.code === 200) {
      const data = res.data
      form.value = {
        id: data.id,
        parentId: data.parentId,
        name: data.name,
        sort: data.sort,
        leader: data.leader,
        phone: data.phone,
        email: data.email,
        status: data.status
      }
      dialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取部门详情失败')
    }
  } catch (error) {
    ElMessage.error('获取部门详情失败')
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
    const apiFn = isEdit.value ? updateDept : createDept
    const res = await apiFn(form.value)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      await fetchDeptTree()
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
 * 删除部门
 */
const handleDelete = (row) => {
  if (row.children && row.children.length > 0) {
    ElMessage.error('该部门下存在子部门，无法删除')
    return
  }
  ElMessageBox.confirm(
    `确认删除部门「${row.name}」吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteDept(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await fetchDeptTree()
        // 如果删除的是当前选中的节点，清除选中
        if (currentNode.value && currentNode.value.id === row.id) {
          currentNode.value = null
        }
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

/**
 * 重置表单
 */
const resetForm = () => {
  formRef.value?.resetFields()
  form.value = {
    id: undefined,
    parentId: undefined,
    name: '',
    sort: 0,
    leader: '',
    phone: '',
    email: '',
    status: 1
  }
}

// ======================== 数据加载 ========================

/**
 * 获取部门树数据
 */
const fetchDeptTree = async () => {
  loading.value = true
  try {
    const res = await getDeptList()
    if (res.code === 200) {
      deptTree.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取部门列表失败')
    }
  } catch (error) {
    ElMessage.error('获取部门列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDeptTree()
})
</script>

<style scoped>
.dept-manage {
  padding: 0;
}

.tree-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  min-height: calc(100vh - 180px);
}

.tree-card :deep(.el-card__header) {
  border-bottom: 1px solid #e4e7eb;
  padding: 16px 20px;
}

.tree-card :deep(.el-card__body) {
  padding: 16px;
}

.table-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  min-height: calc(100vh - 180px);
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
  color: #333;
}

.tree-search {
  margin-bottom: 12px;
}

.tree-search :deep(.el-input__wrapper) {
  border-radius: 6px;
}

.tree-card :deep(.el-tree) {
  background: transparent;
}

.tree-card :deep(.el-tree-node__content) {
  height: 36px;
  border-radius: 4px;
}

.tree-card :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #f0f2ff;
  color: #667eea;
  font-weight: 500;
}

.tree-card :deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

.table-card :deep(.el-table) {
  border-radius: 6px;
}

.table-card :deep(.el-table th.el-table__cell) {
  background-color: #fafafa;
  color: #333;
  font-weight: 600;
}

.table-card :deep(.el-button--primary) {
  --el-button-bg-color: #667eea;
  --el-button-border-color: #667eea;
  --el-button-hover-bg-color: #5568d3;
  --el-button-hover-border-color: #5568d3;
  --el-button-active-bg-color: #4a5bc2;
  --el-button-active-border-color: #4a5bc2;
}

.table-card :deep(.el-button--success) {
  --el-button-hover-bg-color: #85ce61;
  --el-button-hover-border-color: #85ce61;
}

.dept-manage :deep(.el-dialog__header) {
  border-bottom: 1px solid #e4e7eb;
  padding-bottom: 16px;
}

.dept-manage :deep(.el-dialog__footer) {
  border-top: 1px solid #e4e7eb;
  padding-top: 16px;
}

.dept-manage :deep(.el-dialog__title) {
  font-weight: 600;
  color: #333;
}

.dept-manage :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #667eea;
  border-color: #667eea;
}

.dept-manage :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #667eea;
}

.dept-manage :deep(.el-input-number .el-input-number__decrease:hover),
.dept-manage :deep(.el-input-number .el-input-number__increase:hover) {
  color: #667eea;
}

.dept-manage :deep(.el-tree-select .el-select-dropdown__item.is-selected) {
  color: #667eea;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dept-manage :deep(.el-row) {
    flex-direction: column;
  }

  .dept-manage :deep(.el-col) {
    max-width: 100%;
    flex: 0 0 100% !important;
  }

  .tree-card {
    min-height: auto;
    margin-bottom: 16px;
  }

  .table-card {
    min-height: auto;
  }
}
</style>
