<template>
  <div class="license-management">
    <!-- Statistics cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-number total">{{ statistics.totalLicenses }}</div>
            <div class="stat-label">证照总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-number valid">{{ statistics.validCount }}</div>
            <div class="stat-label">有效证照</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-number expiring">{{ statistics.expiringCount }}</div>
            <div class="stat-label">即将到期</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-number expired">{{ statistics.expiredCount }}</div>
            <div class="stat-label">已过期</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Top action bar -->
    <el-card class="top-bar" shadow="never">
      <div class="top-bar-content">
        <h2 class="page-title">证照管理</h2>
        <div class="top-actions">
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon> 新增证照
          </el-button>
          <el-upload
            action=""
            :before-upload="handleImport"
            :show-file-list="false"
            accept=".xlsx,.xls"
          >
            <el-button>
              <el-icon><Upload /></el-icon> 批量导入
            </el-button>
          </el-upload>
        </div>
      </div>
    </el-card>

    <!-- Search & Filter bar -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="证照类型">
          <el-select v-model="queryParams.licenseType" placeholder="请选择证照类型" clearable>
            <el-option label="CAAC维修人员执照" value="CAAC维修人员执照" />
            <el-option label="特种设备操作证" value="特种设备操作证" />
            <el-option label="无损检测资质" value="无损检测资质" />
            <el-option label="焊接资质" value="焊接资质" />
            <el-option label="电气作业许可证" value="电气作业许可证" />
          </el-select>
        </el-form-item>
        <el-form-item label="机型">
          <el-select v-model="queryParams.aircraftType" placeholder="请选择机型" clearable>
            <el-option label="B737" value="B737" />
            <el-option label="A320" value="A320" />
            <el-option label="B777" value="B777" />
            <el-option label="A330" value="A330" />
            <el-option label="ARJ21" value="ARJ21" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="有效" value="valid" />
            <el-option label="即将到期" value="expiring" />
            <el-option label="已过期" value="expired" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="licenseNo" label="证照编号" width="150" />
        <el-table-column prop="licenseType" label="证照类型" min-width="160" />
        <el-table-column prop="userName" label="持证人" width="100" />
        <el-table-column prop="aircraftType" label="机型" width="100" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="issuer" label="发证机构" width="140" />
        <el-table-column prop="issueDate" label="发证日期" width="120" />
        <el-table-column prop="expiryDate" label="到期日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" size="small" @click="handleRenew(row)">续期</el-button>
            <el-popconfirm
              title="确定要删除该证照吗？"
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
      <Pagination
        :total="total"
        v-model:page="queryParams.page"
        v-model:limit="queryParams.pageSize"
        @change="fetchData"
      />
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEdit ? '编辑证照' : '新增证照'"
      width="600px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="证照编号" prop="licenseNo">
          <el-input v-model="form.licenseNo" placeholder="请输入证照编号" />
        </el-form-item>
        <el-form-item label="证照类型" prop="licenseType">
          <el-select v-model="form.licenseType" placeholder="请选择证照类型" style="width: 100%">
            <el-option label="CAAC维修人员执照" value="CAAC维修人员执照" />
            <el-option label="特种设备操作证" value="特种设备操作证" />
            <el-option label="无损检测资质" value="无损检测资质" />
            <el-option label="焊接资质" value="焊接资质" />
            <el-option label="电气作业许可证" value="电气作业许可证" />
          </el-select>
        </el-form-item>
        <el-form-item label="持证人ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入持证人ID" />
        </el-form-item>
        <el-form-item label="机型" prop="aircraftType">
          <el-select v-model="form.aircraftType" placeholder="请选择机型" style="width: 100%">
            <el-option label="B737" value="B737" />
            <el-option label="A320" value="A320" />
            <el-option label="B777" value="B777" />
            <el-option label="A330" value="A330" />
            <el-option label="ARJ21" value="ARJ21" />
          </el-select>
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-input v-model="form.category" placeholder="请输入类别" />
        </el-form-item>
        <el-form-item label="发证机构" prop="issuer">
          <el-input v-model="form.issuer" placeholder="请输入发证机构" />
        </el-form-item>
        <el-form-item label="发证日期" prop="issueDate">
          <el-date-picker
            v-model="form.issueDate"
            type="date"
            placeholder="请选择发证日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="到期日期" prop="expiryDate">
          <el-date-picker
            v-model="form.expiryDate"
            type="date"
            placeholder="请选择到期日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleFormSubmit">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Renew Dialog -->
    <el-dialog
      v-model="showRenewDialog"
      title="证照续期"
      width="500px"
      :close-on-click-modal="false"
      @closed="resetRenewForm"
    >
      <el-form ref="renewFormRef" :model="renewForm" :rules="renewFormRules" label-width="100px">
        <el-form-item label="证照编号">
          <el-input :model-value="renewForm.licenseNo" disabled />
        </el-form-item>
        <el-form-item label="持证人">
          <el-input :model-value="renewForm.userName" disabled />
        </el-form-item>
        <el-form-item label="新到期日期" prop="newExpiryDate">
          <el-date-picker
            v-model="renewForm.newExpiryDate"
            type="date"
            placeholder="请选择新到期日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="续期备注">
          <el-input v-model="renewForm.remark" type="textarea" :rows="3" placeholder="请输入续期备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRenewDialog = false">取消</el-button>
          <el-button type="primary" @click="handleRenewSubmit">确认续期</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Upload } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import {
  listLicenses,
  getLicense,
  createLicense,
  updateLicense,
  deleteLicense,
  renewLicense,
  getLicenseStats,
  importLicenses
} from '@/api/personnel-license'

// Reactive data
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const showCreateDialog = ref(false)
const showRenewDialog = ref(false)
const isEdit = ref(false)
const currentEditId = ref(null)

// Statistics
const statistics = reactive({
  totalLicenses: 0,
  validCount: 0,
  expiringCount: 0,
  expiredCount: 0
})

// Query parameters
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  licenseType: '',
  aircraftType: '',
  status: ''
})

// Create/Edit form
const formRef = ref(null)
const form = reactive({
  licenseNo: '',
  licenseType: '',
  userId: '',
  aircraftType: '',
  category: '',
  issuer: '',
  issueDate: '',
  expiryDate: '',
  remark: ''
})

// Form validation rules
const formRules = {
  licenseNo: [
    { required: true, message: '请输入证照编号', trigger: 'blur' }
  ],
  licenseType: [
    { required: true, message: '请选择证照类型', trigger: 'change' }
  ],
  userId: [
    { required: true, message: '请输入持证人ID', trigger: 'blur' }
  ],
  aircraftType: [
    { required: true, message: '请选择机型', trigger: 'change' }
  ],
  issuer: [
    { required: true, message: '请输入发证机构', trigger: 'blur' }
  ],
  issueDate: [
    { required: true, message: '请选择发证日期', trigger: 'change' }
  ],
  expiryDate: [
    { required: true, message: '请选择到期日期', trigger: 'change' }
  ]
}

// Renew form
const renewFormRef = ref(null)
const renewForm = reactive({
  id: '',
  licenseNo: '',
  userName: '',
  newExpiryDate: '',
  remark: ''
})

const renewFormRules = {
  newExpiryDate: [
    { required: true, message: '请选择新到期日期', trigger: 'change' }
  ]
}

// Status helpers
const statusTagType = (status) => {
  const map = {
    valid: 'success',
    expiring: 'warning',
    expired: 'danger'
  }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = {
    valid: '有效',
    expiring: '即将到期',
    expired: '已过期'
  }
  return map[status] || status
}

// Fetch statistics
const fetchStatistics = async () => {
  try {
    const res = await getLicenseStats()
    if (res.code === 200) {
      Object.assign(statistics, res.data)
    }
  } catch (error) {
    console.error('获取统计信息失败', error)
  }
}

// Fetch data
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listLicenses(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error('获取证照列表失败')
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
  queryParams.licenseType = ''
  queryParams.aircraftType = ''
  queryParams.status = ''
  handleSearch()
}

// Handle edit
const handleEdit = async (row) => {
  isEdit.value = true
  currentEditId.value = row.id
  try {
    const res = await getLicense(row.id)
    if (res.code === 200) {
      const data = res.data
      form.licenseNo = data.licenseNo || ''
      form.licenseType = data.licenseType || ''
      form.userId = data.userId || ''
      form.aircraftType = data.aircraftType || ''
      form.category = data.category || ''
      form.issuer = data.issuer || ''
      form.issueDate = data.issueDate || ''
      form.expiryDate = data.expiryDate || ''
      form.remark = data.remark || ''
      showCreateDialog.value = true
    } else {
      ElMessage.error('获取证照详情失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查连接')
  }
}

// Handle form submit (create / update)
const handleFormSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      let res
      if (isEdit.value) {
        res = await updateLicense(currentEditId.value, { ...form })
      } else {
        res = await createLicense({ ...form })
      }
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        showCreateDialog.value = false
        fetchData()
        fetchStatistics()
      } else {
        ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      }
    } catch (error) {
      ElMessage.error('网络错误，请检查连接')
    }
  })
}

// Reset form
const resetForm = () => {
  isEdit.value = false
  currentEditId.value = null
  form.licenseNo = ''
  form.licenseType = ''
  form.userId = ''
  form.aircraftType = ''
  form.category = ''
  form.issuer = ''
  form.issueDate = ''
  form.expiryDate = ''
  form.remark = ''
  formRef.value?.resetFields()
}

// Handle delete
const handleDelete = async (id) => {
  try {
    const res = await deleteLicense(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
      fetchStatistics()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查连接')
  }
}

// Handle renew
const handleRenew = (row) => {
  renewForm.id = row.id
  renewForm.licenseNo = row.licenseNo
  renewForm.userName = row.userName
  renewForm.newExpiryDate = ''
  renewForm.remark = ''
  showRenewDialog.value = true
}

// Handle renew submit
const handleRenewSubmit = () => {
  renewFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const res = await renewLicense(renewForm.id, {
        newExpiryDate: renewForm.newExpiryDate,
        remark: renewForm.remark
      })
      if (res.code === 200) {
        ElMessage.success('续期成功')
        showRenewDialog.value = false
        fetchData()
        fetchStatistics()
      } else {
        ElMessage.error('续期失败')
      }
    } catch (error) {
      ElMessage.error('网络错误，请检查连接')
    }
  })
}

// Reset renew form
const resetRenewForm = () => {
  renewForm.id = ''
  renewForm.licenseNo = ''
  renewForm.userName = ''
  renewForm.newExpiryDate = ''
  renewForm.remark = ''
  renewFormRef.value?.resetFields()
}

// Handle import
const handleImport = async (file) => {
  try {
    const res = await importLicenses(file)
    if (res.code === 200) {
      ElMessage.success('导入成功')
      fetchData()
      fetchStatistics()
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (error) {
    ElMessage.error('导入失败，请检查文件格式')
  }
  return false // prevent default upload behavior
}

// Lifecycle
onMounted(() => {
  fetchData()
  fetchStatistics()
})
</script>

<style scoped>
.license-management {
  padding: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 10px 0;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-number.total {
  color: #409eff;
}

.stat-number.valid {
  color: #67c23a;
}

.stat-number.expiring {
  color: #e6a23c;
}

.stat-number.expired {
  color: #f56c6c;
}

.stat-label {
  font-size: 14px;
  color: #909399;
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
  align-items: center;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

@media (max-width: 767px) {
  .stat-row .el-col {
    margin-bottom: 10px;
  }

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
