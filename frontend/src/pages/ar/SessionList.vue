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

    <!-- 呼叫专家对话框 -->
    <el-dialog v-model="showCallDialog" title="呼叫专家" width="480px" :close-on-click-modal="false">
      <el-form :model="callForm" label-width="80px">
        <el-form-item label="协作主题">
          <el-input v-model="callForm.subject" placeholder="请输入协作主题" />
        </el-form-item>
        <el-form-item label="选择专家">
          <el-select v-model="callForm.expert_id" placeholder="请选择专家" style="width: 100%">
            <el-option v-for="exp in expertOptions" :key="exp.id" :label="`${exp.name}（${exp.specialty}）`" :value="exp.id" :disabled="!exp.available">
              <span>{{ exp.name }}</span>
              <span style="float: right; color: #909399; font-size: 12px">{{ exp.specialty }} {{ exp.available ? '' : '(忙碌)' }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-radio-group v-model="callForm.urgency">
            <el-radio value="normal">普通</el-radio>
            <el-radio value="urgent">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCallDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCallSubmit">发起呼叫</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Phone } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getSessionList, createSession, joinSession, endSession, getExperts } from '@/api/ar'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: '' })

const showCallDialog = ref(false)
const callForm = reactive({ subject: '', expert_id: null, urgency: 'normal' })
const expertOptions = ref([])

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
  const res = await getExperts()
  if (res.code === 200) {
    expertOptions.value = res.data
  }
  callForm.subject = ''
  callForm.expert_id = null
  callForm.urgency = 'normal'
  showCallDialog.value = true
}

const handleCallSubmit = async () => {
  if (!callForm.subject) {
    ElMessage.warning('请输入协作主题')
    return
  }
  if (!callForm.expert_id) {
    ElMessage.warning('请选择专家')
    return
  }
  const res = await createSession(callForm)
  if (res.code === 200) {
    ElMessage.success('协作会话已创建，等待专家接入')
    showCallDialog.value = false
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
