<template>
  <div class="repair-history">
    <el-card class="stats-card" shadow="never" style="margin-bottom: 16px">
      <template #header>
        <div class="card-header">
          <span class="title">维修统计</span>
          <el-button link @click="showStats = !showStats">{{ showStats ? '收起' : '展开' }}</el-button>
        </div>
      </template>
      <div v-show="showStats">
        <el-row :gutter="16" class="stats-row">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ stats.total_repairs }}</div>
              <div class="stat-label">总维修数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ stats.avg_repair_hours }}h</div>
              <div class="stat-label">平均工时</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div ref="statsChartRef" style="height: 150px"></div>
          </el-col>
        </el-row>
        <div class="top-components">
          <span class="top-label">高频部件：</span>
          <el-tag v-for="comp in stats.top_components" :key="comp" size="small" style="margin-right: 6px">{{ comp }}</el-tag>
        </div>
      </div>
    </el-card>

    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="故障代码">
          <el-input v-model="queryParams.fault_code" placeholder="如 ENG-001" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="飞机">
          <el-input v-model="queryParams.aircraft_id" placeholder="注册号" clearable @keyup.enter="handleSearch" />
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

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">历史维修记录</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="aircraft_id" label="飞机" width="90" />
        <el-table-column prop="fault_code" label="故障代码" width="100" />
        <el-table-column prop="repair_action" label="处理措施" min-width="200" show-overflow-tooltip />
        <el-table-column prop="component_replaced" label="更换部件" width="120">
          <template #default="{ row }">
            {{ row.component_replaced || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="repaired_at" label="维修时间" width="160" />
      </el-table>

      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getRepairHistory } from '@/api/tshoot'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
echarts.use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, fault_code: '', aircraft_id: '' })

const showStats = ref(true)
const statsChartRef = ref(null)
let statsChart = null
const stats = reactive({
  total_repairs: 156,
  avg_repair_hours: 4.2,
  top_components: ['发动机', '起落架', '液压系统', 'APU'],
  by_fault_code: [
    { name: '发动机', value: 42 },
    { name: '起落架', value: 28 },
    { name: '液压', value: 22 },
    { name: '电气', value: 35 },
    { name: 'APU', value: 18 }
  ]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRepairHistory(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.fault_code = ''; queryParams.aircraft_id = ''; handleSearch() }

onMounted(() => {
  fetchData()
  nextTick(() => {
    if (statsChartRef.value) {
      statsChart = echarts.init(statsChartRef.value)
      statsChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: stats.by_fault_code.map(i => i.name) },
        yAxis: { type: 'value' },
        series: [{ type: 'bar', data: stats.by_fault_code.map(i => i.value), itemStyle: { color: '#409eff' } }]
      })
    }
  })
})

onBeforeUnmount(() => { statsChart?.dispose() })
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
.stats-row { margin-bottom: 12px; }
.stat-item { text-align: center; }
.stat-value { font-size: 24px; font-weight: 700; color: #409eff; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.top-components { margin-top: 8px; }
.top-label { font-size: 13px; color: #606266; }
</style>
