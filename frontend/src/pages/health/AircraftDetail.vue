<template>
  <div class="aircraft-detail">
    <!-- 基本信息 -->
    <el-card shadow="never" class="mb-4">
      <template #header>
        <div class="card-header">
          <span class="title">{{ detail.aircraft_id }} — {{ detail.aircraft_type }}</span>
          <el-tag :type="detail.status === 'normal' ? 'success' : 'warning'" effect="dark">
            {{ detail.status === 'normal' ? '正常' : '预警' }}
          </el-tag>
        </div>
      </template>
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="健康评分">
          <el-progress
            :percentage="detail.health_score || 0"
            :color="scoreColor(detail.health_score)"
            :stroke-width="10"
            style="width: 120px"
          />
        </el-descriptions-item>
        <el-descriptions-item label="飞行小时">{{ detail.flight_hours?.toLocaleString() }}h</el-descriptions-item>
        <el-descriptions-item label="基地">{{ detail.base }}</el-descriptions-item>
        <el-descriptions-item label="下次检查">{{ detail.next_scheduled_check }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 传感器状态 -->
    <el-card shadow="never" class="mb-4">
      <template #header><span class="title">传感器实时数据</span></template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="sensor in detail.sensors" :key="sensor.name">
          <div class="sensor-card" :class="{ 'sensor-warning': sensor.status === 'warning' }">
            <div class="sensor-name">{{ sensor.name }}</div>
            <div class="sensor-value">{{ sensor.value }} <span class="sensor-unit">{{ sensor.unit }}</span></div>
            <div class="sensor-threshold">阈值: {{ sensor.threshold }} {{ sensor.unit }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16">
      <!-- 故障记录 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="title">故障记录</span></template>
          <el-table :data="faults" size="small" stripe max-height="320">
            <el-table-column prop="fault_code" label="故障码" width="90" />
            <el-table-column prop="component" label="部件" width="100" show-overflow-tooltip />
            <el-table-column prop="severity" label="严重度" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="severityType(row.severity)" size="small">
                  {{ severityLabel(row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="faultStatusType(row.status)" size="small" effect="plain">
                  {{ faultStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="detected_at" label="检测时间" min-width="140" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>

      <!-- 预测报告 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="title">趋势预测</span></template>
          <div v-for="pred in predictions" :key="pred.id" class="prediction-item">
            <div class="prediction-header">
              <el-tag :type="severityType(pred.result.severity)" size="small">
                {{ pred.result.component }}
              </el-tag>
              <span class="prediction-prob">故障概率: {{ (pred.result.failure_probability * 100).toFixed(0) }}%</span>
            </div>
            <div class="prediction-body">
              <p>预测故障时间: {{ pred.result.predicted_time }}</p>
              <p>置信度: {{ (pred.result.confidence * 100).toFixed(0) }}%</p>
              <p class="prediction-rec">{{ pred.result.recommendation }}</p>
            </div>
          </div>
          <el-empty v-if="!predictions.length" description="暂无预测数据" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getAircraftDetail, getAircraftFaults, getAircraftPredictions } from '@/api/health'

const route = useRoute()
const aircraftId = route.params.id

const detail = reactive({
  aircraft_id: '',
  aircraft_type: '',
  health_score: 0,
  status: 'normal',
  flight_hours: 0,
  base: '',
  next_scheduled_check: '',
  sensors: []
})

const faults = ref([])
const predictions = ref([])

const scoreColor = (score) => {
  if (score >= 90) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
}

const severityType = (s) => ({ critical: 'danger', major: 'warning', minor: 'info' })[s] || 'info'
const severityLabel = (s) => ({ critical: '严重', major: '主要', minor: '次要' })[s] || s
const faultStatusType = (s) => ({ open: 'danger', confirmed: 'warning', resolved: 'success' })[s] || ''
const faultStatusLabel = (s) => ({ open: '待处理', confirmed: '已确认', resolved: '已解决' })[s] || s

const fetchData = async () => {
  const [detailRes, faultRes, predRes] = await Promise.all([
    getAircraftDetail(aircraftId),
    getAircraftFaults(aircraftId, { page: 1, pageSize: 10 }),
    getAircraftPredictions(aircraftId)
  ])

  if (detailRes.code === 200) {
    Object.assign(detail, detailRes.data)
  }
  if (faultRes.code === 200) {
    faults.value = faultRes.data.list
  }
  if (predRes.code === 200) {
    predictions.value = predRes.data
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
.card-header .title, .title {
  font-weight: 600;
}
.sensor-card {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  text-align: center;
}
.sensor-warning {
  border-color: #e6a23c;
  background: #fdf6ec;
}
.sensor-name {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}
.sensor-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.sensor-unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}
.sensor-threshold {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.prediction-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 12px;
}
.prediction-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.prediction-prob {
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
}
.prediction-body p {
  font-size: 13px;
  color: #606266;
  margin: 4px 0;
}
.prediction-rec {
  color: #409eff;
  font-style: italic;
}
</style>
