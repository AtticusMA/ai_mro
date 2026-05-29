<template>
  <div class="trainee-list">
    <el-card shadow="never" class="mb-4">
      <el-form :model="queryParams" inline>
        <el-form-item label="学员姓名">
          <el-input v-model="queryParams.name" placeholder="学员姓名" clearable />
        </el-form-item>
        <el-form-item label="技能等级">
          <el-select v-model="queryParams.skill_level" placeholder="请选择" clearable>
            <el-option label="初级" value="junior" />
            <el-option label="中级" value="mid" />
            <el-option label="高级" value="senior" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <span class="title">学员列表</span>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="skill_level" label="技能等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="tagTypeMap[row.skill_level]" size="small">{{ skillLevelMap[row.skill_level] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="total_training_hours" label="累计培训时长" width="130" align="center" />
        <el-table-column prop="last_assessment_date" label="最近考核日期" width="150" align="center" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="router.push(`/mro/training/trainees/${row.id}`)">查看档案</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTraineeList } from '@/api/training'

const router = useRouter()

const skillLevelMap = { junior: '初级', mid: '中级', senior: '高级' }
const tagTypeMap = { junior: 'info', mid: '', senior: 'success' }

const queryParams = reactive({
  name: '',
  skill_level: ''
})

const tableData = ref([])
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTraineeList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
    }
  } catch (e) {
    ElMessage.error('获取学员列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchData()
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.skill_level = ''
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.title {
  font-weight: 600;
}
</style>
