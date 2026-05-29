<template>
  <div class="session-list">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="会话状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="等待接入" value="waiting" />
            <el-option label="进行中" value="active" />
            <el-option label="已结束" value="ended" />
          </el-select>
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
          <span class="title">远程协作会话</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Phone /></el-icon>发起协作
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="caller_name" label="发起人" width="100" />
        <el-table-column prop="expert_name" label="专家" width="120">
          <template #default="{ row }">
            {{ row.expert_name || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="sessionStatusType(row.status)" effect="light" size="small">
              {{ sessionStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration_seconds" label="时长" width="100" align="center">
          <template #default="{ row }">
            {{ row.duration_seconds ? formatDuration(row.duration_seconds) : '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'waiting'" type="success" link @click="handleJoin(row)">
              接入
            </el-button>
            <el-button v-if="row.status === 'active'" type="danger" link @click="handleEnd(row)">
              结束
            </el-button>
            <el-button v-if="row.status === 'ended' && row.recording_url" type="primary" link @click="handlePlayback(row)">
              回放
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Phone } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getSessionList, createSession, joinSession, endSession } from '@/api/ar'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: '' })

const sessionStatusType = (s) => ({ waiting: 'warning', active: 'success', ended: 'info' })[s] || ''
const sessionStatusLabel = (s) => ({ waiting: '等待接入', active: '进行中', ended: '已结束' })[s] || s

const formatDuration = (seconds) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSessionList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.status = ''; handleSearch() }

const handleCreate = async () => {
  const res = await createSession({})
  if (res.code === 200) {
    ElMessage.success('协作会话已创建，等待专家接入')
    fetchData()
  }
}

const handleJoin = async (row) => {
  const res = await joinSession(row.id)
  if (res.code === 200) { ElMessage.success('已加入会话'); fetchData() }
}

const handleEnd = async (row) => {
  const res = await endSession(row.id)
  if (res.code === 200) { ElMessage.success('会话已结束'); fetchData() }
}

const handlePlayback = (row) => {
  ElMessage.info(`播放录像: ${row.recording_url}`)
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
