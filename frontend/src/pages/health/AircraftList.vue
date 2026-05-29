<template>
  <div class="aircraft-list">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="飞机注册号">
          <el-input
            v-model="queryParams.aircraft_id"
            placeholder="请输入注册号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="健康状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="预警" value="warning" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修基地">
          <el-select v-model="queryParams.base" placeholder="请选择" clearable>
            <el-option label="北京基地" value="北京基地" />
            <el-option label="上海基地" value="上海基地" />
            <el-option label="广州基地" value="广州基地" />
          </el-select>
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

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">机队列表</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="aircraft_id" label="注册号" width="100" />
        <el-table-column prop="aircraft_type" label="机型" width="130" />
        <el-table-column prop="health_score" label="健康评分" width="120" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.health_score"
              :color="scoreColor(row.health_score)"
              :stroke-width="10"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'normal' ? 'success' : 'warning'" effect="light">
              {{ row.status === 'normal' ? '正常' : '预警' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="active_alerts" label="活跃预警" width="100" align="center">
          <template #default="{ row }">
            <el-badge :value="row.active_alerts" :hidden="row.active_alerts === 0" type="danger">
              <span>{{ row.active_alerts }}</span>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="flight_hours" label="飞行小时" width="110" align="right">
          <template #default="{ row }">
            {{ row.flight_hours.toLocaleString() }}h
          </template>
        </el-table-column>
        <el-table-column prop="base" label="基地" width="100" />
        <el-table-column prop="last_check_time" label="最近检查" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        @change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getAircraftList } from '@/api/health'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  aircraft_id: '',
  status: '',
  base: ''
})

const scoreColor = (score) => {
  if (score >= 90) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAircraftList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

const handleReset = () => {
  queryParams.aircraft_id = ''
  queryParams.status = ''
  queryParams.base = ''
  handleSearch()
}

const handleView = (row) => {
  router.push(`/mro/health/aircraft/${row.aircraft_id}`)
}

onMounted(() => { fetchData() })
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
</style>
