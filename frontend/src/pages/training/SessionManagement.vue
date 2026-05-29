<template>
  <div class="session-management">
    <el-card shadow="never" class="mb-4">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="已完成" value="completed" />
            <el-option label="进行中" value="in_progress" />
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
        <div class="card-header">
          <span class="title">培训任务</span>
          <el-button type="primary" @click="handleCreate">分配任务</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="trainee_name" label="学员" min-width="100" />
        <el-table-column prop="scenario_name" label="场景" min-width="150" show-overflow-tooltip />
        <el-table-column prop="mode" label="模式" width="100" align="center">
          <template #default="{ row }">{{ modeLabels[row.mode] || row.mode }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : 'warning'" size="small">
              {{ row.status === 'completed' ? '已完成' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="80" align="center">
          <template #default="{ row }">{{ row.score ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="started_at" label="开始时间" width="170" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'completed'"
              type="primary"
              link
              @click="router.push(`/mro/training/assessments/${row.id}`)"
            >AI评估</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" title="分配培训任务" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" label-width="90px">
        <el-form-item label="培训场景" prop="scenario_id" :rules="[{ required: true, message: '请选择培训场景', trigger: 'change' }]">
          <el-select v-model="formData.scenario_id" placeholder="请选择场景" style="width: 100%">
            <el-option
              v-for="item in scenarioOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学员" prop="trainee_id" :rules="[{ required: true, message: '请选择学员', trigger: 'change' }]">
          <el-select v-model="formData.trainee_id" placeholder="请选择学员" style="width: 100%">
            <el-option
              v-for="item in traineeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模式" prop="mode">
          <el-radio-group v-model="formData.mode">
            <el-radio value="vr">VR模式</el-radio>
            <el-radio value="collaborative">协同模式</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSessionList, createTrainingSession, getScenarioList, getTraineeList } from '@/api/training'

const router = useRouter()

const modeLabels = { vr: 'VR', collaborative: '协同' }

const queryParams = reactive({ status: '' })
const tableData = ref([])
const loading = ref(false)

const showDialog = ref(false)
const formRef = ref(null)
const formData = reactive({
  scenario_id: '',
  trainee_id: '',
  mode: 'vr'
})
const scenarioOptions = ref([])
const traineeOptions = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSessionList(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.list
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchData()
}

const handleReset = () => {
  queryParams.status = ''
  fetchData()
}

const handleCreate = async () => {
  const [scenRes, traineeRes] = await Promise.all([
    getScenarioList({ status: 'published' }),
    getTraineeList()
  ])
  if (scenRes.code === 200) scenarioOptions.value = scenRes.data.list
  if (traineeRes.code === 200) traineeOptions.value = traineeRes.data.list
  formData.scenario_id = ''
  formData.trainee_id = ''
  formData.mode = 'vr'
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  const res = await createTrainingSession(formData)
  if (res.code === 200) {
    ElMessage.success('分配成功')
    showDialog.value = false
    fetchData()
  } else {
    ElMessage.error(res.message || '分配失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title {
  font-weight: 600;
}
</style>
