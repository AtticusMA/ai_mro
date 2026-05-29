<template>
  <div class="personnel-list">
    <!-- Top action bar -->
    <el-card class="top-bar" shadow="never">
      <div class="top-bar-content">
        <h2 class="page-title">人员资质台账</h2>
        <div class="top-actions">
          <el-button type="primary" @click="showCreateDialog = true"><el-icon><Plus /></el-icon> 新增人员</el-button>
          <el-button type="default" @click="handleViewAlerts"><el-icon><Bell /></el-icon> 查看预警</el-button>
        </div>
      </div>
    </el-card>

    <!-- Search & Filter bar -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="搜索">
          <el-input v-model="queryParams.q" placeholder="请输入姓名或工号" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="queryParams.department" placeholder="请选择部门" clearable>
            <el-option label="机体维修部" value="机体维修部" />
            <el-option label="发动机维修部" value="发动机维修部" />
            <el-option label="航电维修部" value="航电维修部" />
            <el-option label="结构修理部" value="结构修理部" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="全部" value="" />
            <el-option label="在职" value="active" />
            <el-option label="离职" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- PC View (Table) -->
    <el-card v-if="!isMobile" class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">人员列表</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="employee_no" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" min-width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row.id)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="140" />
        <el-table-column prop="position" label="岗位" width="120" />
        <el-table-column prop="license_count" label="持证数量" width="100" />
        <el-table-column prop="expiring_count" label="到期预警" width="160">
          <template #default="{ row }">
            <el-tag v-if="row.expired_count > 0" type="danger" size="small">
              {{ row.expired_count }}个已过期
            </el-tag>
            <el-tag v-else-if="row.expiring_count > 0" type="warning" size="small">
              {{ row.expiring_count }}个即将到期
            </el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row.id)">查看详情</el-button>
            <el-popconfirm
              title="确定要删除该人员吗？"
              confirm-button-text="是"
              cancel-button-text="否"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>

    <!-- Mobile View (Card list) -->
    <div v-else class="mobile-card-list">
      <div class="card-header">
        <h3 class="title">人员列表</h3>
      </div>
      <div v-for="person in tableData" :key="person.id" class="mobile-card">
        <div class="card-content">
          <div class="card-header-row">
            <h4 class="name">{{ person.name }}</h4>
            <el-tag v-if="person.expired_count > 0" type="danger" size="small">
              {{ person.expired_count }}个已过期
            </el-tag>
            <el-tag v-else-if="person.expiring_count > 0" type="warning" size="small">
              {{ person.expiring_count }}个即将到期
            </el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </div>
          <div class="card-info">
            <div class="info-item">
              <span class="label">工号：</span>
              <span class="value">{{ person.employee_no }}</span>
            </div>
            <div class="info-item">
              <span class="label">部门：</span>
              <span class="value">{{ person.department }}</span>
            </div>
            <div class="info-item">
              <span class="label">岗位：</span>
              <span class="value">{{ person.position }}</span>
            </div>
            <div class="info-item">
              <span class="label">持证数量：</span>
              <span class="value">{{ person.license_count }}</span>
            </div>
            <div class="info-item">
              <span class="label">状态：</span>
              <span class="value">{{ person.status === 'active' ? '在职' : '离职' }}</span>
            </div>
          </div>
        </div>
        <div class="card-actions">
          <el-button type="primary" size="small" @click="handleViewDetail(person.id)">查看详情</el-button>
          <el-popconfirm
            title="确定要删除该人员吗？"
            confirm-button-text="是"
            cancel-button-text="否"
            @confirm="handleDelete(person.id)"
          >
            <template #reference>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
      <div v-if="tableData.length === 0" class="no-data">
        <p>暂无人员信息</p>
      </div>
      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </div>

    <!-- Create Personnel Dialog -->
    <el-dialog v-model="showCreateDialog" title="新增人员" width="50%" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="rules" label-width="100px">
        <el-form-item label="工号" prop="employee_no">
          <el-input v-model="createForm.employee_no" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-select v-model="createForm.department" placeholder="请选择部门" clearable>
            <el-option label="机体维修部" value="机体维修部" />
            <el-option label="发动机维修部" value="发动机维修部" />
            <el-option label="航电维修部" value="航电维修部" />
            <el-option label="结构修理部" value="结构修理部" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <el-select v-model="createForm.position" placeholder="请选择岗位" clearable>
            <el-option label="机械师" value="机械师" />
            <el-option label="电气工程师" value="电气工程师" />
            <el-option label="质检员" value="质检员" />
            <el-option label="无损检测员" value="无损检测员" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="createForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleCreateSubmit">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Bell } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getPersonnelList, createPersonnel, deletePersonnel } from '@/api/personnel'
import { useRouter } from 'vue-router'

const router = useRouter()

// Reactive data
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const isMobile = ref(window.innerWidth < 768)
const showCreateDialog = ref(false)

// Query parameters
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  q: '',
  department: '',
  status: ''
})

// Create form
const createFormRef = ref(null)
const createForm = reactive({
  employee_no: '',
  name: '',
  department: '',
  position: '',
  phone: ''
})

// Form validation rules
const rules = {
  employee_no: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
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

// Fetch data function
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPersonnelList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error('获取人员列表失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查连接')
  } finally {
    loading.value = false
  }
}

// Handle search
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// Handle reset
const handleReset = () => {
  queryParams.q = ''
  queryParams.department = ''
  queryParams.status = ''
  handleSearch()
}

// Handle view detail
const handleViewDetail = (id) => {
  router.push(`/mro/personnel/${id}`)
}

// Handle view alerts
const handleViewAlerts = () => {
  router.push('/mro/personnel/alerts')
}

// Handle delete
const handleDelete = async (id) => {
  try {
    const res = await deletePersonnel(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查连接')
  }
}

// Handle create submit
const handleCreateSubmit = () => {
  createFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await createPersonnel(createForm)
        if (res.code === 200) {
          ElMessage.success('创建成功')
          showCreateDialog.value = false
          // Reset form
          createForm.employee_no = ''
          createForm.name = ''
          createForm.department = ''
          createForm.position = ''
          createForm.phone = ''
          fetchData()
        } else {
          ElMessage.error('创建失败')
        }
      } catch (error) {
        ElMessage.error('网络错误，请检查连接')
      }
    }
  })
}

// Handle window resize for mobile detection
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

// Lifecycle hooks
onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.personnel-list {
  padding: 20px;
}

.top-bar {
  margin-bottom: 20px;
}

.top-bar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-weight: 600;
  font-size: 20px;
  color: #303133;
}

.top-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
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

.mobile-card-list {
  padding: 0 10px;
}

.card-header {
  margin-bottom: 15px;
}

.title {
  font-weight: 600;
  font-size: 18px;
}

.mobile-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 15px;
  overflow: hidden;
}

.card-content {
  padding: 15px;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.name {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.card-info {
  border-top: 1px solid #eee;
  padding-top: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
  font-size: 14px;
}

.label {
  color: #909399;
}

.value {
  font-weight: 500;
}

.card-actions {
  padding: 10px 15px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background-color: #f8f9fa;
}

.no-data {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

@media (max-width: 767px) {
  .top-bar-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .top-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .search-card {
    .el-form {
      flex-wrap: wrap;
    }
    .el-form-item {
      margin-bottom: 10px;
      width: 100%;
    }
  }
}
</style>
