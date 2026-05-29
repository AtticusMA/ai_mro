<template>
  <div class="inspection-list">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="任务状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待执行" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="飞机注册号">
          <el-input v-model="queryParams.aircraft_id" placeholder="请输入" clearable @keyup.enter="handleSearch" />
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

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">巡检任务</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>创建任务
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="aircraft_id" label="飞机" width="90" />
        <el-table-column prop="inspector_name" label="执行人" width="90" />
        <el-table-column prop="route_template" label="巡检路线" min-width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="anomaly_count" label="异常数" width="80" align="center">
          <template #default="{ row }">
            <el-badge :value="row.anomaly_count" :hidden="!row.anomaly_count" type="danger">
              <span>{{ row.anomaly_count }}</span>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="started_at" label="开始时间" width="160" show-overflow-tooltip />
        <el-table-column prop="completed_at" label="完成时间" width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'completed'" type="primary" link @click="handleViewAnomalies(row)">
              异常记录
            </el-button>
            <el-button v-if="row.status === 'pending'" type="success" link @click="handleStart(row)">
              开始
            </el-button>
            <el-button v-if="row.status === 'in_progress'" type="warning" link @click="handleComplete(row)">
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>

    <!-- 创建任务对话框 -->
    <el-dialog v-model="createVisible" title="创建巡检任务" width="500">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="飞机" prop="aircraft_id">
          <el-select v-model="formData.aircraft_id" placeholder="请选择飞机">
            <el-option v-for="r in ['B-1234','B-5678','B-9012','B-3456','B-7890']" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="巡检路线" prop="route_template">
          <el-select v-model="formData.route_template" placeholder="请选择路线">
            <el-option v-for="r in ['航线绕机-标准','航线绕机-雨后','定检入库-A检','发动机专项']" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 异常记录对话框 -->
    <el-dialog v-model="anomalyVisible" title="异常记录" width="700">
      <el-table :data="anomalies" stripe size="small">
        <el-table-column prop="anomaly_type" label="异常类型" width="140" />
        <el-table-column prop="confidence" label="置信度" width="90" align="center">
          <template #default="{ row }">
            {{ (row.confidence * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column prop="detected_at" label="检测时间" width="160" />
        <el-table-column prop="snapshot_url" label="截图" min-width="120" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getInspectionList, createInspection, startInspection, completeInspection, getInspectionAnomalies } from '@/api/ar'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: '', aircraft_id: '' })

const createVisible = ref(false)
const formRef = ref(null)
const formData = reactive({ aircraft_id: '', route_template: '' })
const formRules = {
  aircraft_id: [{ required: true, message: '请选择飞机', trigger: 'change' }],
  route_template: [{ required: true, message: '请选择路线', trigger: 'change' }]
}

const anomalyVisible = ref(false)
const anomalies = ref([])

const statusType = (s) => ({ pending: 'info', in_progress: 'warning', completed: 'success' })[s] || ''
const statusLabel = (s) => ({ pending: '待执行', in_progress: '进行中', completed: '已完成' })[s] || s

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getInspectionList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.status = ''; queryParams.aircraft_id = ''; handleSearch() }

const handleCreate = () => {
  formData.aircraft_id = ''
  formData.route_template = ''
  createVisible.value = true
}

const handleSubmitCreate = async () => {
  await formRef.value.validate()
  const res = await createInspection(formData)
  if (res.code === 200) {
    ElMessage.success('任务创建成功')
    createVisible.value = false
    fetchData()
  }
}

const handleStart = async (row) => {
  const res = await startInspection(row.id)
  if (res.code === 200) { ElMessage.success('巡检已开始'); fetchData() }
}

const handleComplete = async (row) => {
  const res = await completeInspection(row.id)
  if (res.code === 200) { ElMessage.success('巡检已完成'); fetchData() }
}

const handleViewAnomalies = async (row) => {
  const res = await getInspectionAnomalies(row.id)
  if (res.code === 200) {
    anomalies.value = res.data
    anomalyVisible.value = true
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header .title {
  font-weight: 600;
}
</style>
