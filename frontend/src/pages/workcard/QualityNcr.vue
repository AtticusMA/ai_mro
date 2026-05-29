<template>
  <div class="quality-ncr">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width:120px">
            <el-option label="开放" value="open" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="飞机">
          <el-input v-model="queryParams.aircraft_id" placeholder="飞机注册号" clearable style="width:140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">不符合项（NCR）</span>
          <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>新建 NCR</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="ncr_no" label="NCR编号" width="140" />
        <el-table-column prop="workcard_no" label="关联工卡" width="140" />
        <el-table-column prop="aircraft_id" label="飞机" width="90" align="center" />
        <el-table-column prop="description" label="问题描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="severity" label="严重程度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="{ critical:'danger', major:'warning', minor:'info' }[row.severity]" size="small">
              {{ { critical:'严重', major:'一般', minor:'轻微' }[row.severity] || row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'closed' ? 'success' : 'danger'" size="small">
              {{ row.status === 'closed' ? '已关闭' : '开放' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_by" label="创建人" width="100" align="center" />
        <el-table-column prop="created_at" label="创建时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'open'" type="success" size="small" @click="handleClose(row)">关闭</el-button>
            <el-tag v-else type="info" size="small">已完结</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>

    <!-- Create NCR Dialog -->
    <el-dialog v-model="createDialogVisible" title="新建不符合项" width="560px">
      <el-form :model="ncrForm" :rules="ncrRules" ref="ncrFormRef" label-width="100px">
        <el-form-item label="关联工卡" prop="workcard_no">
          <el-input v-model="ncrForm.workcard_no" placeholder="请输入工卡编号" />
        </el-form-item>
        <el-form-item label="飞机" prop="aircraft_id">
          <el-input v-model="ncrForm.aircraft_id" placeholder="飞机注册号" />
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="ncrForm.severity" style="width:100%">
            <el-option label="严重" value="critical" />
            <el-option label="一般" value="major" />
            <el-option label="轻微" value="minor" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="ncrForm.description" type="textarea" :rows="4" placeholder="详细描述不符合项内容" />
        </el-form-item>
        <el-form-item label="纠正措施" prop="corrective_action">
          <el-input v-model="ncrForm.corrective_action" type="textarea" :rows="3" placeholder="建议纠正措施" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">确认创建</el-button>
      </template>
    </el-dialog>

    <!-- Close NCR Dialog -->
    <el-dialog v-model="closeDialogVisible" title="关闭不符合项" width="480px">
      <el-form :model="closeForm" :rules="closeRules" ref="closeFormRef" label-width="100px">
        <el-form-item label="关闭原因" prop="close_reason">
          <el-input v-model="closeForm.close_reason" type="textarea" :rows="3" placeholder="请说明关闭原因及处理结果" />
        </el-form-item>
        <el-form-item label="员工号" prop="employee_no">
          <el-input v-model="closeForm.employee_no" placeholder="请输入员工号" />
        </el-form-item>
        <el-form-item label="密码确认" prop="password">
          <el-input v-model="closeForm.password" type="password" show-password placeholder="输入密码确认" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitClose">确认关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getNcrList, createNcr, closeNcr } from '@/api/quality'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: '', aircraft_id: '' })

const createDialogVisible = ref(false)
const closeDialogVisible = ref(false)
const currentNcr = ref(null)
const ncrFormRef = ref()
const closeFormRef = ref()

const ncrForm = reactive({ workcard_no: '', aircraft_id: '', severity: 'major', description: '', corrective_action: '' })
const closeForm = reactive({ close_reason: '', employee_no: '', password: '' })

const ncrRules = {
  workcard_no: [{ required: true, message: '请输入关联工卡' }],
  severity: [{ required: true, message: '请选择严重程度' }],
  description: [{ required: true, message: '请输入问题描述' }]
}
const closeRules = {
  close_reason: [{ required: true, message: '请输入关闭原因' }],
  employee_no: [{ required: true, message: '请输入员工号' }],
  password: [{ required: true, message: '请输入密码' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getNcrList(queryParams)
    if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.status = ''; queryParams.aircraft_id = ''; handleSearch() }

const handleCreate = () => {
  Object.assign(ncrForm, { workcard_no: '', aircraft_id: '', severity: 'major', description: '', corrective_action: '' })
  createDialogVisible.value = true
}

const submitCreate = async () => {
  await ncrFormRef.value.validate()
  submitting.value = true
  try {
    const res = await createNcr(ncrForm)
    if (res.code === 200) {
      ElMessage.success('NCR 创建成功')
      createDialogVisible.value = false
      fetchData()
    }
  } finally { submitting.value = false }
}

const handleClose = (row) => {
  currentNcr.value = row
  Object.assign(closeForm, { close_reason: '', employee_no: '', password: '' })
  closeDialogVisible.value = true
}

const submitClose = async () => {
  await closeFormRef.value.validate()
  submitting.value = true
  try {
    const res = await closeNcr(currentNcr.value.id, closeForm)
    if (res.code === 200) {
      ElMessage.success('NCR 已关闭')
      closeDialogVisible.value = false
      fetchData()
    }
  } finally { submitting.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
</style>
