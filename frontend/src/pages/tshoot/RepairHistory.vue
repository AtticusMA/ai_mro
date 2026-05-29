<template>
  <div class="repair-history">
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
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getRepairHistory } from '@/api/tshoot'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, fault_code: '', aircraft_id: '' })

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
