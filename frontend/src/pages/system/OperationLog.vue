<template>
  <div class="operation-log">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="操作人">
          <el-input
            v-model="queryParams.userName"
            placeholder="请输入操作人姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="请求路径">
          <el-input
            v-model="queryParams.requestPath"
            placeholder="请输入路径关键词"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="操作结果">
          <el-select v-model="queryParams.resultStatus" placeholder="请选择结果" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleTimeRangeChange"
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

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">操作日志列表</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="userName" label="操作人" min-width="90" show-overflow-tooltip />
        <el-table-column prop="requestPath" label="请求路径" min-width="220" show-overflow-tooltip />
        <el-table-column prop="requestTime" label="请求时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="costTime" label="耗时(ms)" width="100" align="center" />
        <el-table-column prop="resultStatus" label="操作结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.resultStatus === 1 ? 'success' : 'danger'" effect="light">
              {{ row.resultStatus === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>详情
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

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="操作日志详情"
      size="500px"
      :destroy-on-close="true"
    >
      <template v-if="currentLog">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="操作人">{{ currentLog.userName }}</el-descriptions-item>
          <el-descriptions-item label="请求ID">{{ currentLog.requestId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="请求路径">{{ currentLog.requestPath }}</el-descriptions-item>
          <el-descriptions-item label="请求时间">{{ currentLog.requestTime }}</el-descriptions-item>
          <el-descriptions-item label="请求耗时">{{ currentLog.costTime }} ms</el-descriptions-item>
          <el-descriptions-item label="操作结果">
            <el-tag :type="currentLog.resultStatus === 1 ? 'success' : 'danger'" effect="light">
              {{ currentLog.resultStatus === 1 ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="params-section">
          <div class="params-label">请求参数</div>
          <pre class="params-content">{{ formatJson(currentLog.requestParams) }}</pre>
        </div>
      </template>
      <div v-else class="loading-placeholder">
        <el-skeleton :rows="5" animated />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getOperationLogList, getOperationLogDetail } from '@/api/operationLog'

// ======================== 搜索相关 ========================

const timeRange = ref(null)

const queryParams = reactive({
  userName: '',
  requestPath: '',
  resultStatus: undefined,
  beginTime: '',
  endTime: '',
  page: 1,
  pageSize: 20,
})

const handleTimeRangeChange = (val) => {
  if (val) {
    queryParams.beginTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.beginTime = ''
    queryParams.endTime = ''
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

const handleReset = () => {
  timeRange.value = null
  queryParams.userName = ''
  queryParams.requestPath = ''
  queryParams.resultStatus = undefined
  queryParams.beginTime = ''
  queryParams.endTime = ''
  queryParams.page = 1
  queryParams.pageSize = 20
  fetchData()
}

// ======================== 表格相关 ========================

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOperationLogList(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '获取操作日志失败')
  } finally {
    loading.value = false
  }
}

// ======================== 详情抽屉 ========================

const drawerVisible = ref(false)
const currentLog = ref(null)

const handleViewDetail = async (row) => {
  currentLog.value = null
  drawerVisible.value = true
  try {
    const res = await getOperationLogDetail(row.id)
    currentLog.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
    drawerVisible.value = false
  }
}

const formatJson = (jsonStr) => {
  if (!jsonStr) return '（无参数）'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

// ======================== 初始化 ========================

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.operation-log {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

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
  width: 160px;
}

.search-card :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.search-card :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

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

.params-section {
  margin-top: 20px;
}

.params-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.params-content {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  font-size: 13px;
  font-family: 'Courier New', Courier, monospace;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 400px;
  overflow-y: auto;
  margin: 0;
}

.loading-placeholder {
  padding: 20px;
}
</style>
