<template>
  <div>
    <!-- Filter bar -->
    <el-form inline size="small" class="mb-4">
      <el-form-item label="机库">
        <el-select v-model="filter.hangarId" clearable placeholder="全部" style="width:130px">
          <el-option v-for="h in hangars" :key="h.id" :label="h.name" :value="h.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期范围">
        <el-date-picker
          v-model="filter.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
          value-format="YYYY-MM-DD"
          style="width:240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- KPI cards -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="kpi">
            <div class="kpi-label">平均完成天数</div>
            <div class="kpi-value">{{ efficiency?.avgCompletionDays?.toFixed(1) ?? '—' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="kpi">
            <div class="kpi-label">完成率</div>
            <div class="kpi-value">{{ efficiency ? (efficiency.completionRate * 100).toFixed(1) + '%' : '—' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="kpi">
            <div class="kpi-label">已完成指令</div>
            <div class="kpi-value">{{ efficiency?.completedOrders ?? '—' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="kpi">
            <div class="kpi-label">总指令数</div>
            <div class="kpi-value">{{ efficiency?.totalOrders ?? '—' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- Workload bar chart -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span class="title">工位负载率</span></template>
          <div ref="workloadChartRef" style="height:300px" v-loading="loading" />
        </el-card>
      </el-col>

      <!-- Avg utilization gauge -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span class="title">整体利用率</span></template>
          <div ref="gaugeRef" style="height:300px" v-loading="loading" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, GaugeChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getHangarList, getWorkloadAnalytics, getEfficiencyAnalytics } from '@/api/dtwin'

echarts.use([BarChart, GaugeChart, GridComponent, TooltipComponent, TitleComponent, CanvasRenderer])

const hangars = ref([])
const filter  = ref({ hangarId: null, dateRange: null })
const loading = ref(false)
const workload   = ref(null)
const efficiency = ref(null)

const workloadChartRef = ref(null)
const gaugeRef = ref(null)
let workloadChart, gaugeChart

async function load() {
  loading.value = true
  const [startDate, endDate] = filter.value.dateRange || [null, null]
  const params = { hangarId: filter.value.hangarId, startDate, endDate }
  const [wlRes, effRes] = await Promise.all([
    getWorkloadAnalytics(params),
    getEfficiencyAnalytics(params)
  ]).finally(() => loading.value = false)
  if (wlRes.code === 0) { workload.value = wlRes.data; renderWorkload() }
  if (effRes.code === 0) { efficiency.value = effRes.data; renderGauge() }
}

function renderWorkload() {
  if (!workloadChart) return
  const data = workload.value?.workstationLoads ?? []
  workloadChart.setOption({
    tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>利用率: ${(p[0].value * 100).toFixed(1)}%` },
    xAxis: { type: 'category', data: data.map(d => d.workstationName), axisLabel: { rotate: 20 } },
    yAxis: { type: 'value', max: 1, axisLabel: { formatter: v => (v * 100) + '%' } },
    series: [{
      type: 'bar',
      data: data.map(d => d.utilizationRate),
      itemStyle: { color: p => p.value >= 0.8 ? '#ff6b35' : p.value >= 0.6 ? '#faad14' : '#52c41a' },
      label: { show: true, position: 'top', formatter: p => (p.value * 100).toFixed(0) + '%' }
    }]
  })
}

function renderGauge() {
  if (!gaugeChart) return
  const val = (workload.value?.avgUtilizationRate ?? 0) * 100
  gaugeChart.setOption({
    series: [{
      type: 'gauge',
      detail: { formatter: '{value}%', fontSize: 20 },
      data: [{ value: val.toFixed(1), name: '平均利用率' }],
      axisLine: { lineStyle: { color: [[0.6, '#52c41a'], [0.8, '#faad14'], [1, '#ff6b35']], width: 18 } }
    }]
  })
}

onMounted(async () => {
  const res = await getHangarList()
  if (res.code === 200) hangars.value = res.data.list
  await nextTick()
  workloadChart = echarts.init(workloadChartRef.value)
  gaugeChart    = echarts.init(gaugeRef.value)
  load()
})

onUnmounted(() => {
  workloadChart?.dispose()
  gaugeChart?.dispose()
})
</script>

<style scoped>
.title { font-weight: 600; }
.mb-4 { margin-bottom: 16px; }
.kpi { text-align: center; padding: 8px 0; }
.kpi-label { font-size: 12px; color: var(--el-text-color-secondary); }
.kpi-value { font-size: 28px; font-weight: 700; color: var(--el-color-primary); margin-top: 4px; }
</style>
