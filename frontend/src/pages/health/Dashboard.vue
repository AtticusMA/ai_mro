<template>
  <div class="health-dashboard">
    <!-- 概览指标卡片 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-value text-green-600">{{ stats.normalCount }}</div>
            <div class="stat-label">正常运行</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-value text-orange-500">{{ stats.warningCount }}</div>
            <div class="stat-label">预警中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-value text-blue-600">{{ stats.totalAircraft }}</div>
            <div class="stat-label">机队总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-value text-red-500">{{ stats.criticalAlerts }}</div>
            <div class="stat-label">红色预警</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预警列表 -->
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="title">最新预警</span>
              <el-button type="primary" link @click="$router.push('/mro/health/alerts')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="recentAlerts" stripe size="small">
            <el-table-column prop="aircraft_id" label="飞机" width="90" />
            <el-table-column prop="alert_level" label="等级" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="levelTagType(row.alert_level)" effect="dark" size="small">
                  {{ levelLabel(row.alert_level) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="预警内容" show-overflow-tooltip />
            <el-table-column prop="created_at" label="时间" width="160" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="title">机队健康分布</span>
          </template>
          <div class="health-distribution">
            <div v-for="item in aircraftList" :key="item.id" class="aircraft-item">
              <div class="aircraft-info">
                <span class="aircraft-id">{{ item.aircraft_id }}</span>
                <span class="aircraft-type text-gray-400 text-xs">{{ item.aircraft_type }}</span>
              </div>
              <el-progress
                :percentage="item.health_score"
                :color="scoreColor(item.health_score)"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAircraftList, getAlertList } from '@/api/health'

const stats = reactive({
  totalAircraft: 0,
  normalCount: 0,
  warningCount: 0,
  criticalAlerts: 0
})

const recentAlerts = ref([])
const aircraftList = ref([])

const levelTagType = (level) => {
  const map = { red: 'danger', orange: 'warning', yellow: '' }
  return map[level] || 'info'
}

const levelLabel = (level) => {
  const map = { red: '红色', orange: '橙色', yellow: '黄色' }
  return map[level] || level
}

const scoreColor = (score) => {
  if (score >= 90) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
}

const fetchData = async () => {
  const [aircraftRes, alertRes] = await Promise.all([
    getAircraftList({ page: 1, pageSize: 20 }),
    getAlertList({ page: 1, pageSize: 8 })
  ])

  if (aircraftRes.code === 200) {
    const list = aircraftRes.data.list
    aircraftList.value = list
    stats.totalAircraft = aircraftRes.data.total
    stats.normalCount = list.filter(a => a.status === 'normal').length
    stats.warningCount = list.filter(a => a.status === 'warning').length
  }

  if (alertRes.code === 200) {
    recentAlerts.value = alertRes.data.list
    stats.criticalAlerts = alertRes.data.list.filter(a => a.alert_level === 'red').length
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.health-dashboard {
  padding: 0;
}
.stat-card .stat-content {
  text-align: center;
  padding: 8px 0;
}
.stat-card .stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-card .stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header .title {
  font-weight: 600;
}
.health-distribution {
  max-height: 360px;
  overflow-y: auto;
}
.aircraft-item {
  margin-bottom: 12px;
}
.aircraft-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}
.aircraft-id {
  font-weight: 500;
  font-size: 13px;
}
</style>
