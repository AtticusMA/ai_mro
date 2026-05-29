<template>
  <div class="quality-pending">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="飞机">
          <el-input v-model="queryParams.aircraft_id" placeholder="飞机注册号" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="工卡类型">
          <el-select v-model="queryParams.card_type" placeholder="请选择" clearable style="width:120px">
            <el-option label="航线" value="line" />
            <el-option label="定检" value="heavy" />
            <el-option label="排故" value="troubleshoot" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">待签工卡</span>
          <el-tag type="warning">共 {{ total }} 条待签</el-tag>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="workcard_no" label="工卡编号" width="140" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="aircraft_id" label="飞机" width="90" align="center" />
        <el-table-column prop="card_type" label="类型" width="80" align="center">
          <template #default="{ row }">
            {{ { line: '航线', heavy: '定检', troubleshoot: '排故' }[row.card_type] || row.card_type }}
          </template>
        </el-table-column>
        <el-table-column prop="mechanic_name" label="维修人员" width="100" align="center" />
        <el-table-column prop="completed_at" label="完工时间" width="160" />
        <el-table-column prop="sign_type" label="签署类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sign_type === 'inspector' ? 'warning' : ''" size="small">
              {{ row.sign_type === 'inspector' ? '检验签署' : '质检签署' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="goSign(row)">签署</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getPendingList } from '@/api/quality'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, aircraft_id: '', card_type: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingList(queryParams)
    if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.aircraft_id = ''; queryParams.card_type = ''; handleSearch() }
const goSign = (row) => router.push(`/mro/quality/sign/${row.id}`)

onMounted(fetchData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
</style>
