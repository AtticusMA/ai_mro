<template>
  <div class="alert-list">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="预警等级">
          <el-select v-model="queryParams.alert_level" placeholder="请选择" clearable>
            <el-option label="红色" value="red" />
            <el-option label="橙色" value="orange" />
            <el-option label="黄色" value="yellow" />
          </el-select>
        </el-form-item>
        <el-form-item label="飞机注册号">
          <el-input
            v-model="queryParams.aircraft_id"
            placeholder="请输入注册号"
            clearable
            @keyup.enter="handleSearch"
          />
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
          <span class="title">预警列表</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="aircraft_id" label="飞机" width="90" />
        <el-table-column prop="alert_level" label="预警等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.alert_level)" effect="dark" size="small">
              {{ levelLabel(row.alert_level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="预警内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="predicted_fault_time" label="预测故障时间" width="160" />
        <el-table-column prop="created_at" label="产生时间" width="160" />
        <el-table-column prop="acknowledged" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.acknowledged ? 'success' : 'danger'" effect="light" size="small">
              {{ row.acknowledged ? '已确认' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.acknowledged"
              type="primary"
              link
              @click="handleAcknowledge(row)"
            >
              确认
            </el-button>
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        @change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getAlertList, acknowledgeAlert } from '@/api/health'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  alert_level: '',
  aircraft_id: ''
})

const levelTagType = (level) => {
  const map = { red: 'danger', orange: 'warning', yellow: '' }
  return map[level] || 'info'
}

const levelLabel = (level) => {
  const map = { red: '红色', orange: '橙色', yellow: '黄色' }
  return map[level] || level
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAlertList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

const handleReset = () => {
  queryParams.alert_level = ''
  queryParams.aircraft_id = ''
  handleSearch()
}

const handleAcknowledge = async (row) => {
  await ElMessageBox.confirm('确认处理此预警？', '确认')
  const res = await acknowledgeAlert(row.id)
  if (res.code === 200) {
    ElMessage.success('预警已确认')
    fetchData()
  }
}

const handleView = (row) => {
  router.push(`/mro/health/aircraft/${row.aircraft_id}`)
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
