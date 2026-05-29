<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { getTaskDetail, assignPersonnel, getAvailablePersonnel } from '@/api/planning.js'

const router = useRouter()
const route = useRoute()

// State
const task = ref(null)
const loading = ref(false)
const dialogVisible = ref(false)
const selectedWorkcard = ref(null)
availablePersonnel = ref([])
const personnelLoading = ref(false)

// Computed properties
const statusColor = computed(() => {
  switch (task.value?.status) {
    case 'planning': return 'blue'
    case 'in_progress': return 'orange'
    case 'completed': return 'green'
    default: return 'gray'
  }
})

// Load task detail
const loadTaskDetail = async () => {
  if (!route.params.id) return
  
  loading.value = true
  try {
    const response = await getTaskDetail(route.params.id)
    task.value = response.data
  } catch (error) {
    ElMessage.error('加载任务详情失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// Open assign dialog
const openAssignDialog = (workcard) => {
  selectedWorkcard.value = workcard
  loadAvailablePersonnel(workcard.type)
  dialogVisible.value = true
}

// Load available personnel
const loadAvailablePersonnel = async (workcardType) => {
  personnelLoading.value = true
  try {
    const response = await getAvailablePersonnel({ workcard_type: workcardType })
    availablePersonnel.value = response.data.list || []
  } catch (error) {
    ElMessage.error('加载可用人员失败: ' + (error.response?.data?.message || error.message))
  } finally {
    personnelLoading.value = false
  }
}

// Assign personnel
const handleAssign = async (personnelId) => {
  if (!selectedWorkcard.value || !personnelId) return
  
  try {
    await assignPersonnel(selectedWorkcard.value.id, { personnel_id: personnelId })
    ElMessage.success('派工成功')
    dialogVisible.value = false
    // Refresh task detail to update assigned_to
    await loadTaskDetail()
  } catch (error) {
    ElMessage.error('派工失败: ' + (error.response?.data?.message || error.message))
  }
}

// Back to planning page
const goBack = () => {
  router.push('/mro/planning')
}

// Initialize
onMounted(() => {
  loadTaskDetail()
})
</script>

<template>
  <div class="p-6">
    <!-- Header -->
    <div class="flex items-center mb-6">
      <el-button @click="goBack" icon="el-icon-back" circle class="mr-4" />
      <div class="flex-1">
        <h1 class="text-2xl font-bold text-gray-800">
          {{ task?.task_no }} - {{ task?.aircraft_reg }}
        </h1>
        <div class="mt-2">
          <el-tag :type="statusColor" size="large">{{ task?.status }}</el-tag>
        </div>
      </div>
    </div>

    <!-- Summary cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white p-4 rounded-lg shadow border">
        <p class="text-sm text-gray-500">总工卡数</p>
        <p class="text-2xl font-bold text-gray-800">{{ task?.summary?.total_workcards || 0 }}</p>
      </div>
      <div class="bg-white p-4 rounded-lg shadow border">
        <p class="text-sm text-gray-500">已完成</p>
        <p class="text-2xl font-bold text-green-600">{{ task?.summary?.completed || 0 }}</p>
      </div>
      <div class="bg-white p-4 rounded-lg shadow border">
        <p class="text-sm text-gray-500">进行中</p>
        <p class="text-2xl font-bold text-orange-600">{{ task?.summary?.in_progress || 0 }}</p>
      </div>
      <div class="bg-white p-4 rounded-lg shadow border">
        <p class="text-sm text-gray-500">待开始</p>
        <p class="text-2xl font-bold text-blue-600">{{ task?.summary?.pending || 0 }}</p>
      </div>
    </div>

    <!-- Task Overview -->
    <div class="bg-white p-6 rounded-lg shadow mb-6">
      <h2 class="text-xl font-semibold text-gray-800 mb-4">任务概览</h2>
      <el-descriptions :column="2" size="large" border>
        <el-descriptions-item label="注册号">{{ task?.aircraft_reg }}</el-descriptions-item>
        <el-descriptions-item label="机型">{{ task?.aircraft_type }}</el-descriptions-item>
        <el-descriptions-item label="检修类型">{{ task?.check_type }}</el-descriptions-item>
        <el-descriptions-item label="计划开始">{{ task?.planned_start }}</el-descriptions-item>
        <el-descriptions-item label="计划结束">{{ task?.planned_end }}</el-descriptions-item>
        <el-descriptions-item label="实际开始">{{ task?.actual_start }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- Workcard List -->
    <div class="bg-white p-6 rounded-lg shadow">
      <h2 class="text-xl font-semibold text-gray-800 mb-4">工卡列表</h2>
      <el-table 
        :data="task?.workcards || []" 
        stripe 
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="workcard_no" label="工卡编号" width="120" />
        <el-table-column prop="title" label="工卡标题" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag 
              :type="scope.row.status === 'completed' ? 'success' : 
                     scope.row.status === 'in_progress' ? 'warning' : 'info'"
              size="small"
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigned_to" label="负责人" width="150">
          <template #default="scope">
            <span v-if="scope.row.assigned_to">{{ scope.row.assigned_to }}</span>
            <span v-else class="text-gray-500">未派工</span>
          </template>
        </el-table-column>
        <el-table-column label="工时" width="150">
          <template #default="scope">
            <div>{{ scope.row.estimated_hours }} / {{ scope.row.actual_hours }}</div>
            <div class="text-xs text-gray-500">预计 / 实际</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click="openAssignDialog(scope.row)"
            >
              {{ scope.row.assigned_to ? '重新派工' : '派工' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Assign Personnel Dialog -->
    <el-dialog 
      v-model="dialogVisible" 
      title="派工" 
      width="80%"
      :before-close="() => dialogVisible = false"
    >
      <div class="mb-4">
        <h3 class="font-semibold text-lg">工卡信息</h3>
        <p class="text-gray-600">{{ selectedWorkcard?.workcard_no }} - {{ selectedWorkcard?.title }}</p>
      </div>
      
      <el-table 
        :data="availablePersonnel" 
        stripe 
        style="width: 100%"
        v-loading="personnelLoading"
      >
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="employee_no" label="工号" width="100" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="岗位" width="120" />
        <el-table-column label="资质" width="100">
          <template #default="scope">
            <el-tooltip 
              v-if="!scope.row.qualified" 
              :content="scope.row.qualification_reason" 
              placement="top"
            >
              <el-icon class="text-red-500"><WarningFilled /></el-icon>
            </el-tooltip>
            <el-icon v-else class="text-green-500"><SuccessFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click="handleAssign(scope.row.id)"
            >
              确认派工
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* Add any custom styles here if needed */
</style>
