<template>
  <div class="notification-center">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 160px">
            <el-option label="健康预警" value="health_alert" />
            <el-option label="工卡超时" value="workcard_overdue" />
            <el-option label="证照到期" value="license_expiring" />
            <el-option label="培训分配" value="training_assignment" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.is_read" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="未读" :value="false" />
            <el-option label="已读" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">消息通知</span>
          <el-button type="primary" @click="handleMarkAllRead" :disabled="notificationStore.unreadCount === 0">全部已读</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column label="状态" width="60" align="center">
          <template #default="{ row }">
            <div class="read-dot" :class="{ unread: !row.is_read }"></div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type]" size="small">{{ typeLabelMap[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="created_at" label="时间" width="160" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="!row.is_read" type="primary" link size="small" @click="handleMarkRead(row)">标记已读</el-button>
            <span v-else class="text-gray">已读</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotifications, markNotificationRead, markAllNotificationsRead } from '@/api/notification'
import { useNotificationStore } from '@/store/modules/notification'

const notificationStore = useNotificationStore()

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  type: '',
  is_read: ''
})

const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const typeLabelMap = {
  health_alert: '健康预警',
  workcard_overdue: '工卡超时',
  license_expiring: '证照到期',
  training_assignment: '培训分配'
}

const typeTagMap = {
  health_alert: 'danger',
  workcard_overdue: 'warning',
  license_expiring: '',
  training_assignment: 'success'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (params.type === '') delete params.type
    if (params.is_read === '') delete params.is_read
    const res = await getNotifications(params)
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
  queryParams.type = ''
  queryParams.is_read = ''
  queryParams.page = 1
  fetchData()
}

const handleMarkRead = async (row) => {
  const res = await markNotificationRead(row.id)
  if (res.code === 200) {
    row.is_read = true
    notificationStore.unreadCount = Math.max(0, notificationStore.unreadCount - 1)
    ElMessage.success('已标记为已读')
  }
}

const handleMarkAllRead = async () => {
  const res = await markAllNotificationsRead()
  if (res.code === 200) {
    tableData.value.forEach(n => { n.is_read = true })
    notificationStore.unreadCount = 0
    ElMessage.success('全部已标记为已读')
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-weight: 600;
  font-size: 16px;
}

.read-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dcdfe6;
  margin: 0 auto;
}

.read-dot.unread {
  background: #409eff;
}

.text-gray {
  color: #909399;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
