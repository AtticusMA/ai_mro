<template>
  <div class="archive-list">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="来源类型">
          <el-select v-model="queryParams.source_type" placeholder="请选择" clearable>
            <el-option label="巡检录像" value="巡检录像" />
            <el-option label="远程协作录像" value="远程协作录像" />
          </el-select>
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

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">影像档案</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="source_type" label="来源" width="120" />
        <el-table-column prop="aircraft_id" label="飞机" width="90" />
        <el-table-column prop="duration_seconds" label="时长" width="100" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.duration_seconds) }}
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handlePlay(row)">回放</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getArchiveList } from '@/api/ar'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, source_type: '', aircraft_id: '' })

const formatDuration = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return h > 0 ? `${h}时${m}分` : `${m}分`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getArchiveList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.source_type = ''; queryParams.aircraft_id = ''; handleSearch() }

const handlePlay = (row) => {
  ElMessage.info(`播放: ${row.file_url}`)
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
