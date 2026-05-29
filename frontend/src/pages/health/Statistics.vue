<template>
  <div class="health-statistics">
    <!-- 概览指标 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.total_faults }}</div>
            <div class="stat-label">故障总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-value text-red-500">{{ stats.by_severity?.critical || 0 }}</div>
            <div class="stat-label">严重故障</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-value text-orange-500">{{ stats.by_severity?.major || 0 }}</div>
            <div class="stat-label">主要故障</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-content">
            <div class="stat-value text-green-600">{{ stats.avg_resolution_hours }}h</div>
            <div class="stat-label">平均解决时长</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb-4">
      <!-- 按机型分布 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="title">按机型分布</span></template>
          <div class="chart-placeholder">
            <el-table :data="stats.by_aircraft_type || []" size="small" stripe>
              <el-table-column prop="type" label="机型" />
              <el-table-column prop="count" label="故障数" width="100" align="center">
                <template #default="{ row }">
                  <el-progress
                    :percentage="Math.min(100, Math.round(row.count / maxByType * 100))"
                    :stroke-width="12"
                    :text-inside="true"
                    :format="() => row.count"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>

      <!-- 按部件分布 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="title">按部件分布</span></template>
          <div class="chart-placeholder">
            <el-table :data="stats.by_component || []" size="small" stripe>
              <el-table-column prop="component" label="部件" />
              <el-table-column prop="count" label="故障数" width="100" align="center">
                <template #default="{ row }">
                  <el-progress
                    :percentage="Math.min(100, Math.round(row.count / maxByComponent * 100))"
                    :stroke-width="12"
                    :text-inside="true"
                    :format="() => row.count"
                    color="#e6a23c"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 月度趋势 -->
    <el-card shadow="never">
      <template #header><span class="title">月度故障趋势</span></template>
      <div class="trend-chart">
        <div class="trend-bars">
          <div v-for="item in stats.by_month || []" :key="item.month" class="trend-bar-item">
            <div class="trend-bar" :style="{ height: `${item.count / maxByMonth * 200}px` }">
              <span class="trend-bar-value">{{ item.count }}</span>
            </div>
            <div class="trend-bar-label">{{ item.month.slice(5) }}月</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStatistics } from '@/api/health'

const stats = ref({
  total_faults: 0,
  by_severity: {},
  by_aircraft_type: [],
  by_component: [],
  by_month: [],
  avg_resolution_hours: 0
})

const maxByType = computed(() => {
  const arr = stats.value.by_aircraft_type || []
  return Math.max(...arr.map(a => a.count), 1)
})

const maxByComponent = computed(() => {
  const arr = stats.value.by_component || []
  return Math.max(...arr.map(a => a.count), 1)
})

const maxByMonth = computed(() => {
  const arr = stats.value.by_month || []
  return Math.max(...arr.map(a => a.count), 1)
})

const fetchData = async () => {
  const res = await getStatistics()
  if (res.code === 200) {
    stats.value = res.data
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.title {
  font-weight: 600;
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
.trend-chart {
  padding: 20px 0;
}
.trend-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 240px;
  padding-top: 20px;
}
.trend-bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.trend-bar {
  width: 48px;
  background: linear-gradient(180deg, #409eff, #79bbff);
  border-radius: 4px 4px 0 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  min-height: 20px;
}
.trend-bar-value {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  padding-top: 4px;
}
.trend-bar-label {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
</style>
