<template>
  <div class="personnel-detail">
    <!-- Header section -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          <span>← 返回列表</span>
        </el-button>
        <div class="title-section">
          <h2 class="page-title">{{ personnelData.name }}</h2>
          <el-tag v-if="personnelData.employee_no" size="small" class="employee-no-tag">
            {{ personnelData.employee_no }}
          </el-tag>
        </div>
        <div class="action-buttons">
          <el-button type="primary" @click="showEditDialog = true">编辑</el-button>
          <el-popconfirm
            title="确定要删除该人员吗？"
            confirm-button-text="是"
            cancel-button-text="否"
            @confirm="handleDelete"
          >
            <template #reference>
              <el-button type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </el-card>

    <!-- Basic Info section -->
    <el-card v-loading="loading" class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">基本信息</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工号">{{ personnelData.employee_no }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ personnelData.name }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ personnelData.department }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ personnelData.position }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ personnelData.phone }}</el-descriptions-item>
        <el-descriptions-item label="在职状态">
          <el-tag :type="personnelData.status === 'active' ? 'success' : 'info'">
            {{ personnelData.status === 'active' ? '在职' : '离职' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- License Table section -->
    <el-card v-loading="loading" class="license-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">持有证件 ({{ personnelData.licenses?.length || 0 }}张)</span>
        </div>
      </template>
      <el-table :data="personnelData.licenses || []" stripe>
        <el-table-column prop="license_name" label="证件名称" min-width="120" />
        <el-table-column prop="license_no" label="证件编号" min-width="120" />
        <el-table-column prop="issuing_authority" label="发证机构" min-width="120" />
        <el-table-column prop="issue_date" label="发证日期" width="120" />
        <el-table-column prop="expiry_date" label="有效期至" width="120">
          <template #default="{ row }">
            <span :class="{
              'text-red-500': row.status === 'expired',
              'text-orange-500': row.status === 'warning' || row.status === 'critical',
              'text-green-500': row.status === 'valid'
            }">
              {{ row.expiry_date }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 'valid' ? 'success' : 
                    row.status === 'warning' ? 'warning' : 
                    row.status === 'critical' ? 'warning' : 'danger'"
              size="small"
            >
              {{ row.status === 'valid' ? '有效' : 
                 row.status === 'warning' ? '即将到期' : 
                 row.status === 'critical' ? '即将到期' : '已过期' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog v-model="showEditDialog" title="编辑人员信息" width="50%" :close-on-click-modal="false">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="工号" prop="employee_no">
          <el-input v-model="editForm.employee_no" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-select v-model="editForm.department" placeholder="请选择部门" clearable>
            <el-option label="机体维修部" value="机体维修部" />
            <el-option label="发动机维修部" value="发动机维修部" />
            <el-option label="航电维修部" value="航电维修部" />
            <el-option label="结构修理部" value="结构修理部" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <el-select v-model="editForm.position" placeholder="请选择岗位" clearable>
            <el-option label="机械师" value="机械师" />
            <el-option label="电气工程师" value="电气工程师" />
            <el-option label="质检员" value="质检员" />
            <el-option label="无损检测员" value="无损检测员" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="handleEditSubmit">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { getPersonnelDetail, updatePersonnel, deletePersonnel } from '@/api/personnel'

const route = useRoute()
const router = useRouter()

// Reactive data
const loading = ref(false)
const personnelData = ref({
  id: '',
  employee_no: '',
  name: '',
  department: '',
  position: '',
  phone: '',
  status: '',
  licenses: []
})
const showEditDialog = ref(false)

// Edit form
const editFormRef = ref(null)
const editForm = reactive({
  id: '',
  employee_no: '',
  name: '',
  department: '',
  position: '',
  phone: ''
})

// Form validation rules
const editRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  position: [
    { required: true, message: '请选择岗位', trigger: 'change' }
  ]
}

// Load personnel detail
const loadPersonnelDetail = async () => {
  if (!route.params.id) return
  
  loading.value = true
  try {
    const res = await getPersonnelDetail(route.params.id)
    if (res.data) {
      personnelData.value = res.data
      // Initialize edit form with current data
      editForm.id = res.data.id
      editForm.employee_no = res.data.employee_no
      editForm.name = res.data.name
      editForm.department = res.data.department
      editForm.position = res.data.position
      editForm.phone = res.data.phone
    } else {
      ElMessage.error('获取人员详情失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查连接')
  } finally {
    loading.value = false
  }
}

// Go back to list
const goBack = () => {
  router.push('/mro/personnel')
}

// Handle delete
const handleDelete = async () => {
  try {
    const res = await deletePersonnel(route.params.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      router.push('/mro/personnel')
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查连接')
  }
}

// Handle edit submit
const handleEditSubmit = () => {
  editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await updatePersonnel(editForm.id, editForm)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          showEditDialog.value = false
          // Refresh data
          await loadPersonnelDetail()
        } else {
          ElMessage.error('更新失败')
        }
      } catch (error) {
        ElMessage.error('网络错误，请检查连接')
      }
    }
  })
}

// Lifecycle hooks
onMounted(() => {
  loadPersonnelDetail()
})

onBeforeUnmount(() => {
  // Cleanup if needed
})
</script>

<style scoped>
.personnel-detail {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title {
  font-weight: 600;
  font-size: 20px;
  color: #303133;
  margin: 0;
}

.employee-no-tag {
  background-color: #f0f9ff;
  color: #007bff;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.info-card,
.license-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-weight: 600;
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.text-red-500 {
  color: #f56c6c;
}

.text-orange-500 {
  color: #e6a23c;
}

.text-green-500 {
  color: #67c23a;
}

@media (max-width: 767px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .action-buttons {
    width: 100%;
    justify-content: flex-end;
  }
  
  .title-section {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
