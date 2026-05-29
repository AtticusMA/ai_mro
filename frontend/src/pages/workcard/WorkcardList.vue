<template>
  <div class="workcard-list">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="草稿" value="draft" />
            <el-option label="已下发" value="issued" />
            <el-option label="执行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
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
          <span class="title">电子工卡</span>
          <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>创建工卡</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="card_no" label="工卡编号" width="140" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="card_type" label="类型" width="80" align="center">
          <template #default="{ row }">
            {{ {line:'航线',heavy:'定检',troubleshoot:'排故'}[row.card_type] || row.card_type }}
          </template>
        </el-table-column>
        <el-table-column prop="aircraft_id" label="飞机" width="80" />
        <el-table-column prop="priority" label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="{urgent:'danger',normal:'',low:'info'}[row.priority]" size="small">
              {{ {urgent:'紧急',normal:'普通',low:'低'}[row.priority] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="{draft:'info',issued:'warning',in_progress:'',completed:'success'}[row.status]" size="small">
              {{ {draft:'草稿',issued:'已下发',in_progress:'执行中',completed:'已完成'}[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="8" :text-inside="true" />
          </template>
        </el-table-column>
        <el-table-column prop="due_date" label="到期" width="110" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="router.push(`/mro/workcard/${row.id}/execute`)"
            >执行</el-button>
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
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getWorkcardList } from '@/api/workcard'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getWorkcardList(queryParams)
    if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.status = ''; handleSearch() }
const handleCreate = () => { ElMessage.info('创建工卡') }

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
</style>
