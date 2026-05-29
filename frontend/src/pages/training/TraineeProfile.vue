<template>
  <div class="trainee-profile">
    <el-card shadow="never" class="header-card mb-4">
      <el-button link @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <span class="page-title">学员档案</span>
    </el-card>

    <el-card shadow="never" class="mb-4">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
        <el-descriptions-item label="技能等级">
          <el-tag :type="tagTypeMap[profile.skill_level]" size="small">{{ skillLevelMap[profile.skill_level] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="累计培训时长">{{ profile.total_training_hours }} hours</el-descriptions-item>
        <el-descriptions-item label="最近考核日期">{{ profile.last_assessment_date }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="16" class="mb-4">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="title">能力雷达图</span></template>
          <div ref="radarRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="title">能力趋势</span></template>
          <div ref="trendRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header><span class="title">培训记录</span></template>
      <el-table :data="profile.sessions" border stripe>
        <el-table-column prop="scenario_name" label="场景" min-width="150" />
        <el-table-column prop="mode" label="模式" width="100" align="center" />
        <el-table-column prop="score" label="评分" width="80" align="center">
          <template #default="{ row }">{{ row.score ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : 'warning'" size="small">
              {{ row.status === 'completed' ? '完成' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="started_at" label="开始时间" width="170" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getTraineeProfile, getIndividualReport } from '@/api/training'
import * as echarts from 'echarts/core'
import { RadarChart, LineChart } from 'echarts/charts'
import { RadarComponent, GridComponent, TooltipComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([RadarChart, LineChart, RadarComponent, GridComponent, TooltipComponent, TitleComponent, CanvasRenderer])

const route = useRoute()
const router = useRouter()
const traineeId = route.params.id

const skillLevelMap = { junior: '初级', mid: '中级', senior: '高级' }
const tagTypeMap = { junior: 'info', mid: '', senior: 'success' }

const profile = reactive({
  name: '',
  skill_level: '',
  total_training_hours: 0,
  last_assessment_date: '',
  skills: [],
  sessions: []
})

const report = ref({})
const radarRef = ref(null)
const trendRef = ref(null)
let radarChart = null
let trendChart = null

const initRadarChart = () => {
  if (!radarRef.value || !profile.skills.length) return
  radarChart = echarts.init(radarRef.value)
  radarChart.setOption({
    radar: {
      indicator: profile.skills.map(s => ({ name: s.name, max: 100 }))
    },
    series: [{
      type: 'radar',
      data: [{ value: profile.skills.map(s => s.score), name: '技能评分' }]
    }]
  })
}

const initTrendChart = () => {
  if (!trendRef.value || !report.value.trend?.length) return
  trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    xAxis: { type: 'category', data: report.value.trend.map(t => t.month) },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [{ type: 'line', data: report.value.trend.map(t => t.score), smooth: true }],
    tooltip: { trigger: 'axis' }
  })
}

onMounted(async () => {
  try {
    const [profileRes, reportRes] = await Promise.all([
      getTraineeProfile(traineeId),
      getIndividualReport(traineeId)
    ])
    if (profileRes.code === 200) {
      Object.assign(profile, profileRes.data)
    }
    if (reportRes.code === 200) {
      report.value = reportRes.data
    }
    initRadarChart()
    initTrendChart()
  } catch (e) {
    ElMessage.error('获取学员档案失败')
  }
})

onBeforeUnmount(() => {
  if (radarChart) {
    radarChart.dispose()
    radarChart = null
  }
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
})
</script>

<style scoped>
.header-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.title {
  font-weight: 600;
}
</style>
