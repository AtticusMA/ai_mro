<template>
  <div class="assessment-detail">
    <el-card shadow="never" class="mb-4">
      <div class="header-bar">
        <el-button class="back-btn" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <span class="title">AI 评估详情</span>
      </div>
    </el-card>

    <el-card shadow="never" class="mb-4" v-if="assessment">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="培训场景">{{ assessment.scenario_name }}</el-descriptions-item>
        <el-descriptions-item label="学员">{{ assessment.trainee_name }}</el-descriptions-item>
        <el-descriptions-item label="综合评分">
          <span class="score-value" :class="scoreClass(assessment.overall_score)">
            {{ assessment.overall_score }}
          </span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="mb-4" v-if="assessment">
      <template #header><span class="title">评估指标得分</span></template>
      <div ref="chartRef" style="height: 350px"></div>
    </el-card>

    <el-card shadow="never" v-if="assessment">
      <template #header><span class="title">评估明细</span></template>
      <el-table :data="assessment.metrics" border stripe>
        <el-table-column prop="name" label="指标" min-width="120" />
        <el-table-column prop="score" label="得分" width="200" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.score"
              :color="progressColor(row.score)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getAssessment } from '@/api/training'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

const route = useRoute()
const router = useRouter()

const assessment = ref(null)
const chartRef = ref(null)
let chartInstance = null

const scoreClass = (score) => {
  if (score >= 80) return 'score-green'
  if (score >= 60) return 'score-orange'
  return 'score-red'
}

const progressColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const initChart = (metrics) => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    xAxis: {
      type: 'category',
      data: metrics.map(m => m.name)
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100
    },
    series: [{
      type: 'bar',
      data: metrics.map(m => m.score),
      itemStyle: {
        color: (params) => params.data >= 80 ? '#67c23a' : params.data >= 60 ? '#e6a23c' : '#f56c6c'
      }
    }],
    tooltip: {
      trigger: 'axis'
    }
  })
}

onMounted(async () => {
  const sessionId = route.params.sessionId
  const res = await getAssessment(sessionId)
  if (res.code === 200) {
    assessment.value = res.data
    await nextTick()
    initChart(res.data.metrics)
  }
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.header-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.back-btn {
  padding: 8px 12px;
}
.title {
  font-weight: 600;
  font-size: 16px;
}
.score-value {
  font-weight: 700;
  font-size: 18px;
}
.score-green {
  color: #67c23a;
}
.score-orange {
  color: #e6a23c;
}
.score-red {
  color: #f56c6c;
}
</style>
