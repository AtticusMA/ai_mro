<template>
  <div class="operation-dashboard" :class="{ fullscreen: isFullscreen }">
    <!-- Header -->
    <div class="dashboard-header">
      <div class="header-left">
        <span class="title">运营看板</span>
        <el-tag type="success" size="small" class="live-badge">LIVE</el-tag>
      </div>
      <div class="header-right">
        <span class="update-time">更新时间: {{ lastUpdated }}</span>
        <el-button :icon="isFullscreen ? Aim : FullScreen" size="small" @click="toggleFullscreen">
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </el-button>
        <el-button :icon="Refresh" size="small" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- Summary Stats -->
    <div class="stat-grid">
      <el-card v-for="stat in stats" :key="stat.key" class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <el-icon class="stat-icon" :style="{ color: stat.color }">
          <component :is="stat.icon" />
        </el-icon>
      </el-card>
    </div>

    <!-- Bay Status Grid -->
    <el-card class="bay-card" shadow="never">
      <template #header>
        <span class="section-title">机位状态</span>
      </template>
      <div class="bay-grid">
        <div
          v-for="bay in bays"
          :key="bay.bay_no"
          class="bay-cell"
          :class="[`bay-${bay.status}`]"
        >
          <div class="bay-no">{{ bay.bay_no }}</div>
          <div class="bay-aircraft">{{ bay.aircraft_id || '—' }}</div>
          <div class="bay-status-label">{{ bayStatusLabel(bay.status) }}</div>
          <div v-if="bay.progress !== undefined" class="bay-progress">
            <el-progress :percentage="bay.progress" :stroke-width="4" :show-text="false" />
            <span class="progress-text">{{ bay.progress }}%</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Alerts Panel -->
    <el-card class="alerts-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="section-title">预警信息</span>
          <el-badge :value="alerts.length" type="danger" />
        </div>
      </template>
      <div v-if="alerts.length === 0" class="no-alerts">
        <el-icon color="#67C23A" size="24"><CircleCheck /></el-icon>
        <span>暂无预警</span>
      </div>
      <el-timeline v-else>
        <el-timeline-item
          v-for="alert in alerts"
          :key="alert.id"
          :type="alertType(alert.level)"
          :timestamp="alert.created_at"
        >
          <div class="alert-item">
            <el-tag :type="alertType(alert.level)" size="small" class="mr-2">{{ alertLevelLabel(alert.level) }}</el-tag>
            <span>{{ alert.message }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Refresh, FullScreen, Aim, CircleCheck, DataAnalysis, Tickets, Tools, WarningFilled, Clock, Trophy } from '@element-plus/icons-vue'
import { getOperationDashboard } from '@/api/dashboard'

const loading = ref(false)
const isFullscreen = ref(false)
const lastUpdated = ref('—')
const bays = ref([])
const alerts = ref([])
const rawStats = ref({})

let refreshTimer = null

const stats = computed(() => [
  { key: 'total_workcards', label: '在修工卡', value: rawStats.value.total_workcards ?? 0, color: '#409EFF', icon: Tickets },
  { key: 'in_progress', label: '执行中', value: rawStats.value.in_progress ?? 0, color: '#E6A23C', icon: Tools },
  { key: 'pending_sign', label: '待质检签署', value: rawStats.value.pending_sign ?? 0, color: '#F56C6C', icon: WarningFilled },
  { key: 'overdue', label: '逾期工卡', value: rawStats.value.overdue ?? 0, color: '#F56C6C', icon: Clock },
  { key: 'completed_today', label: '今日完工', value: rawStats.value.completed_today ?? 0, color: '#67C23A', icon: Trophy },
  { key: 'open_ncr', label: '开放NCR', value: rawStats.value.open_ncr ?? 0, color: '#F56C6C', icon: DataAnalysis }
])

const bayStatusLabel = (status) => {
  return { idle: '空闲', occupied: '占用', maintenance: '维修中', closed: '关闭' }[status] || status
}
const alertType = (level) => ({ critical: 'danger', warning: 'warning', info: '' }[level] || '')
const alertLevelLabel = (level) => ({ critical: '严重', warning: '警告', info: '提示' }[level] || level)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOperationDashboard()
    if (res.code === 200) {
      bays.value = res.data.bays || []
      alerts.value = res.data.alerts || []
      rawStats.value = res.data.stats || {}
      lastUpdated.value = new Date().toLocaleTimeString('zh-CN')
    }
  } finally { loading.value = false }
}

const toggleFullscreen = () => { isFullscreen.value = !isFullscreen.value }

onMounted(() => {
  fetchData()
  refreshTimer = setInterval(fetchData, 30000)
})

onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped>
.operation-dashboard { padding: 0; }
.operation-dashboard.fullscreen {
  position: fixed; inset: 0; z-index: 9999;
  background: #0a0e1a; overflow-y: auto; padding: 16px;
}

.dashboard-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; padding: 12px 16px;
  background: var(--el-bg-color); border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
}
.header-left { display: flex; align-items: center; gap: 8px; }
.title { font-size: 20px; font-weight: 700; }
.live-badge { animation: pulse 2s infinite; }
@keyframes pulse { 0%,100% { opacity:1 } 50% { opacity:0.5 } }
.header-right { display: flex; align-items: center; gap: 8px; }
.update-time { font-size: 12px; color: var(--el-text-color-secondary); }

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px; margin-bottom: 16px;
}
.stat-card { cursor: default; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; justify-content: space-between; padding: 16px; }
.stat-value { font-size: 28px; font-weight: 700; line-height: 1; }
.stat-label { font-size: 12px; color: var(--el-text-color-secondary); margin-top: 4px; }
.stat-icon { font-size: 32px; opacity: 0.3; }

.bay-card { margin-bottom: 16px; }
.section-title { font-weight: 600; }
.bay-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}
.bay-cell {
  border: 2px solid var(--el-border-color);
  border-radius: 8px; padding: 12px; text-align: center;
  transition: all 0.3s;
}
.bay-idle { border-color: #909399; background: #f5f7fa; }
.bay-occupied { border-color: #409EFF; background: #ecf5ff; }
.bay-maintenance { border-color: #E6A23C; background: #fdf6ec; }
.bay-closed { border-color: #F56C6C; background: #fef0f0; }
.bay-no { font-size: 18px; font-weight: 700; }
.bay-aircraft { font-size: 14px; color: var(--el-text-color-regular); margin: 4px 0; }
.bay-status-label { font-size: 12px; color: var(--el-text-color-secondary); margin-bottom: 6px; }
.bay-progress { display: flex; align-items: center; gap: 4px; }
.progress-text { font-size: 12px; color: var(--el-text-color-secondary); white-space: nowrap; }

.alerts-card { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; gap: 8px; }
.no-alerts { display: flex; align-items: center; justify-content: center; gap: 8px; padding: 24px; color: #67C23A; }
.alert-item { display: flex; align-items: center; }
.mr-2 { margin-right: 8px; }
</style>
