<template>
  <div class="analytics-dashboard">
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-value">{{ overview.total_tasks }}</div>
            <div class="kpi-label">维修任务总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-value">{{ overview.avg_completion_hours }}h</div>
            <div class="kpi-label">平均完成时长</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-value">{{ overview.fleet_availability }}%</div>
            <div class="kpi-label">机队可用率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-value">{{ overview.training_pass_rate }}%</div>
            <div class="kpi-label">培训通过率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb-4">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span class="card-title">月度趋势</span></template>
          <div ref="trendRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span class="card-title">模块能力评分</span></template>
          <div ref="radarRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">维修分布（ATA章节）</span></template>
          <div ref="pieRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">数据摘要</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="统计周期">2026-01 至 2026-06</el-descriptions-item>
            <el-descriptions-item label="维修任务总量">{{ overview.total_tasks }} 项</el-descriptions-item>
            <el-descriptions-item label="平均工时">{{ overview.avg_completion_hours }} 小时/任务</el-descriptions-item>
            <el-descriptions-item label="机队可用率">{{ overview.fleet_availability }}%</el-descriptions-item>
            <el-descriptions-item label="培训通过率">{{ overview.training_pass_rate }}%</el-descriptions-item>
            <el-descriptions-item label="故障高发区域">ATA-72 发动机（28%）</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, RadarChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  RadarComponent,
  TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import {
  getAnalyticsOverview,
  getAnalyticsTrends,
  getModuleKpis,
  getMaintenanceDistribution
} from '@/api/analytics'

echarts.use([
  LineChart, RadarChart, PieChart,
  GridComponent, TooltipComponent, LegendComponent, RadarComponent, TitleComponent,
  CanvasRenderer
])

const overview = reactive({
  total_tasks: 0,
  avg_completion_hours: 0,
  fleet_availability: 0,
  training_pass_rate: 0
})

const trendRef = ref(null)
const radarRef = ref(null)
const pieRef = ref(null)

let trendChart = null
let radarChart = null
let pieChart = null

const initTrendChart = (data) => {
  trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['维修任务', '故障数', '培训次数'], bottom: 0 },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: data.months },
    yAxis: { type: 'value' },
    series: [
      { name: '维修任务', type: 'line', data: data.maintenance, smooth: true, itemStyle: { color: '#409eff' } },
      { name: '故障数', type: 'line', data: data.faults, smooth: true, itemStyle: { color: '#f56c6c' } },
      { name: '培训次数', type: 'line', data: data.training, smooth: true, itemStyle: { color: '#67c23a' } }
    ]
  })
}

const initRadarChart = (data) => {
  radarChart = echarts.init(radarRef.value)
  radarChart.setOption({
    tooltip: {},
    radar: {
      indicator: data.indicators.map(name => ({ name, max: 100 })),
      radius: '65%'
    },
    series: [{
      type: 'radar',
      data: [{
        value: data.scores,
        name: '模块评分',
        areaStyle: { opacity: 0.3 }
      }]
    }]
  })
}

const initPieChart = (data) => {
  pieChart = echarts.init(pieRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}% ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      data,
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
      },
      label: { formatter: '{b}\n{d}%' }
    }]
  })
}

const fetchData = async () => {
  const [overRes, trendRes, radarRes, pieRes] = await Promise.all([
    getAnalyticsOverview(),
    getAnalyticsTrends(),
    getModuleKpis(),
    getMaintenanceDistribution()
  ])
  if (overRes.code === 200) Object.assign(overview, overRes.data)
  if (trendRes.code === 200) initTrendChart(trendRes.data)
  if (radarRes.code === 200) initRadarChart(radarRes.data)
  if (pieRes.code === 200) initPieChart(pieRes.data)
}

onMounted(() => { fetchData() })

onBeforeUnmount(() => {
  trendChart?.dispose()
  radarChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }

.kpi-card {
  text-align: center;
}

.kpi-content {
  padding: 12px 0;
}

.kpi-value {
  font-size: 30px;
  font-weight: 700;
  color: #409eff;
}

.kpi-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.card-title {
  font-weight: 600;
}
</style>
