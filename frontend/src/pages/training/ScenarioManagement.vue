<template>
  <div class="scenario-management">
    <!-- 搜索区域 -->
    <el-card shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="场景名称">
          <el-input
            v-model="queryParams.name"
            placeholder="场景名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="queryParams.category" placeholder="请选择" clearable>
            <el-option label="航线检查" value="line_check" />
            <el-option label="发动机" value="engine" />
            <el-option label="紧急处置" value="emergency" />
            <el-option label="部件维修" value="component" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="queryParams.difficulty" placeholder="请选择" clearable>
            <el-option label="初级" value="beginner" />
            <el-option label="中级" value="intermediate" />
            <el-option label="高级" value="advanced" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>培训场景</span>
          <el-button type="primary" @click="handleCreate">新建场景</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="name" label="场景名称" />
        <el-table-column prop="category" label="类别">
          <template #default="{ row }">
            {{ categoryMap[row.category] || row.category }}
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度">
          <template #default="{ row }">
            <el-tag :type="difficultyTagType[row.difficulty]">
              {{ difficultyLabel[row.difficulty] || row.difficulty }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration_minutes" label="时长(分钟)" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'info'">
              {{ row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handlePublish(row)">
              {{ row.status === 'published' ? '撤回' : '发布' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑场景' : '新建场景'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="场景名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="formData.category" placeholder="请选择类别">
            <el-option label="航线检查" value="line_check" />
            <el-option label="发动机" value="engine" />
            <el-option label="紧急处置" value="emergency" />
            <el-option label="部件维修" value="component" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="formData.difficulty" placeholder="请选择难度">
            <el-option label="初级" value="beginner" />
            <el-option label="中级" value="intermediate" />
            <el-option label="高级" value="advanced" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration_minutes">
          <el-input-number
            v-model="formData.duration_minutes"
            :min="1"
            placeholder="请输入时长"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getScenarioList,
  createScenario,
  updateScenario,
  deleteScenario,
  publishScenario
} from '@/api/training'

const categoryMap = {
  line_check: '航线检查',
  engine: '发动机',
  emergency: '紧急处置',
  component: '部件维修'
}

const difficultyTagType = {
  beginner: 'success',
  intermediate: '',
  advanced: 'danger'
}

const difficultyLabel = {
  beginner: '初级',
  intermediate: '中级',
  advanced: '高级'
}

const queryParams = reactive({
  name: '',
  category: '',
  difficulty: ''
})

const tableData = ref([])
const loading = ref(false)

const showDialog = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  name: '',
  category: '',
  difficulty: '',
  duration_minutes: null
})

const formRules = {
  name: [{ required: true, message: '请输入场景名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  duration_minutes: [{ required: true, message: '请输入时长', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getScenarioList(queryParams)
    tableData.value = res.data || []
  } catch (e) {
    ElMessage.error('获取场景列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchData()
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.category = ''
  queryParams.difficulty = ''
  fetchData()
}

const handleCreate = () => {
  isEdit.value = false
  formData.id = null
  formData.name = ''
  formData.category = ''
  formData.difficulty = ''
  formData.duration_minutes = null
  showDialog.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.id = row.id
  formData.name = row.name
  formData.category = row.category
  formData.difficulty = row.difficulty
  formData.duration_minutes = row.duration_minutes
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value) {
        await updateScenario(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await createScenario(formData)
        ElMessage.success('创建成功')
      }
      showDialog.value = false
      fetchData()
    } catch (e) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  })
}

const handlePublish = async (row) => {
  try {
    await publishScenario(row.id)
    ElMessage.success(row.status === 'published' ? '已撤回' : '已发布')
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该场景吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteScenario(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
