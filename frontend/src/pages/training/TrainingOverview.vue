<template>
  <div class="training-overview">
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card shadow="never"><div class="stat-content"><div class="stat-value">{{ overview.total_trainees }}</div><div class="stat-label">学员总数</div></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><div class="stat-content"><div class="stat-value">{{ overview.total_sessions }}</div><div class="stat-label">培训次数</div></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><div class="stat-content"><div class="stat-value">{{ overview.avg_score }}</div><div class="stat-label">平均得分</div></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><div class="stat-content"><div class="stat-value">{{ (overview.pass_rate * 100).toFixed(0) }}%</div><div class="stat-label">通过率</div></div></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="title">培训场景</span></template>
          <el-table :data="scenarios" stripe size="small">
            <el-table-column prop="name" label="场景" min-width="150" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="difficulty" label="难度" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="{beginner:'success',intermediate:'',advanced:'danger'}[row.difficulty]" size="small">{{ {beginner:'初级',intermediate:'中级',advanced:'高级'}[row.difficulty] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'published' ? 'success' : 'info'" size="small">{{ row.status === 'published' ? '已发布' : '草稿' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="title">最近培训</span></template>
          <el-table :data="sessions" stripe size="small">
            <el-table-column prop="trainee_name" label="学员" width="70" />
            <el-table-column prop="scenario_name" label="场景" min-width="130" show-overflow-tooltip />
            <el-table-column prop="mode" label="模式" width="60" align="center" />
            <el-table-column prop="score" label="评分" width="60" align="center">
              <template #default="{ row }">{{ row.score ?? '—' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'completed' ? 'success' : 'warning'" size="small">{{ row.status === 'completed' ? '完成' : '进行中' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getScenarioList, getSessionList, getOverviewReport } from '@/api/training'

const overview = reactive({ total_trainees: 0, total_sessions: 0, avg_score: 0, pass_rate: 0 })
const scenarios = ref([])
const sessions = ref([])

const fetchData = async () => {
  const [overRes, scenRes, sessRes] = await Promise.all([
    getOverviewReport(), getScenarioList({ page: 1, pageSize: 10 }), getSessionList({ page: 1, pageSize: 10 })
  ])
  if (overRes.code === 200) Object.assign(overview, overRes.data)
  if (scenRes.code === 200) scenarios.value = scenRes.data.list
  if (sessRes.code === 200) sessions.value = sessRes.data.list
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.title { font-weight: 600; }
.stat-content { text-align: center; padding: 8px 0; }
.stat-value { font-size: 28px; font-weight: 700; color: #409eff; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
