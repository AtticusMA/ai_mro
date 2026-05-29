<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { getWorkload } from '@/api/planning.js'

// State
const loading = ref(false)
const workloadList = ref([])
const sortBy = ref('hours') // 'hours' or 'name'
const departmentFilter = ref('全部')

// Department options
const departmentOptions = [
  '全部',
  '机体维修部',
  '发动机维修部',
  '航电维修部',
  '结构修理部'
]

// Expanded state for each personnel
const expandedMap = ref(new Map())

// Computed properties
const filteredList = computed(() => {
  if (departmentFilter.value === '全部') {
    return workloadList.value
  }
  return workloadList.value.filter(person => person.department === departmentFilter.value)
})

const sortedList = computed(() => {
  const list = [...filteredList.value]
  if (sortBy.value === 'hours') {
    return list.sort((a, b) => b.total_hours - a.total_hours)
  } else {
    return list.sort((a, b) => a.name.localeCompare(b.name))
  }
})

const summaryStats = computed(() => {
  const list = filteredList.value
  const totalPeople = list.length
  const totalHours = list.reduce((sum, person) => sum + person.total_hours, 0)
  const avgHours = totalPeople > 0 ? (totalHours / totalPeople).toFixed(1) : 0
  
  return {
    totalPeople,
    totalHours,
    avgHours
  }
})

// Methods
const toggleExpand = (personnelId) => {
  const isExpanded = expandedMap.value.get(personnelId) || false
  expandedMap.value.set(personnelId, !isExpanded)
}

const getStatusForHours = (hours) => {
  if (hours < 40) return 'success'
  if (hours <= 60) return 'warning'
  return 'exception'
}

const loadWorkload = async () => {
  loading.value = true
  try {
    const response = await getWorkload({})
    workloadList.value = response.data?.list || []
  } catch (error) {
    ElMessage.error('加载工作负荷数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadWorkload()
})
</script>

<template>
  <div class="p-6 max-w-7xl mx-auto">
    <!-- Page Header -->
    <div class="flex items-center justify-between mb-8">
      <div class="flex items-center space-x-4">
        <router-link to="/mro/planning" class="text-gray-500 hover:text-gray-700">
          <i class="el-icon-arrow-left mr-2"></i>
        </router-link>
        <h1 class="text-2xl font-bold text-gray-800">人员工作负荷</h1>
      </div>
      
      <!-- Department Filter -->
      <div class="flex items-center space-x-4">
        <span class="text-gray-600">部门:</span>
        <el-select v-model="departmentFilter" placeholder="选择部门" size="small" class="w-48">
          <el-option
            v-for="dept in departmentOptions"
            :key="dept"
            :label="dept"
            :value="dept"
          />
        </el-select>
      </div>
    </div>

    <!-- Summary Stats -->
    <div class="bg-white rounded-lg shadow-sm p-6 mb-6 border border-gray-200">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="flex items-center p-4 bg-blue-50 rounded-lg">
          <div class="text-blue-600 font-bold text-2xl">{{ summaryStats.totalPeople }}</div>
          <div class="ml-4">
            <div class="text-sm text-gray-600">总人数</div>
          </div>
        </div>
        <div class="flex items-center p-4 bg-green-50 rounded-lg">
          <div class="text-green-600 font-bold text-2xl">{{ summaryStats.totalHours }}</div>
          <div class="ml-4">
            <div class="text-sm text-gray-600">已分配小时</div>
          </div>
        </div>
        <div class="flex items-center p-4 bg-purple-50 rounded-lg">
          <div class="text-purple-600 font-bold text-2xl">{{ summaryStats.avgHours }}</div>
          <div class="ml-4">
            <div class="text-sm text-gray-600">人均小时</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Sort Options -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-lg font-semibold text-gray-700">排序方式</h2>
      <div class="flex space-x-2">
        <el-button
          size="small"
          :type="sortBy === 'hours' ? 'primary' : 'default'"
          @click="sortBy = 'hours'"
        >
          按负荷排序
        </el-button>
        <el-button
          size="small"
          :type="sortBy === 'name' ? 'primary' : 'default'"
          @click="sortBy = 'name'"
        >
          按姓名排序
        </el-button>
      </div>
    </div>

    <!-- Workload List -->
    <div v-loading="loading" class="space-y-4">
      <div 
        v-for="person in sortedList" 
        :key="person.personnel_id" 
        class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden"
      >
        <!-- Card Header -->
        <div class="p-4 border-b border-gray-200 flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <h3 class="text-lg font-semibold text-gray-800">{{ person.name }}</h3>
            <span class="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded-md">
              {{ person.employee_no }}
            </span>
            <span class="px-2 py-1 bg-blue-100 text-blue-700 text-xs rounded-md">
              {{ person.department }}
            </span>
            <span class="px-2 py-1 bg-yellow-100 text-yellow-700 text-xs rounded-md">
              {{ person.active_workcards }} 个在修工卡
            </span>
          </div>
          
          <div class="flex items-center space-x-2">
            <el-button 
              size="small" 
              type="text" 
              @click="toggleExpand(person.personnel_id)"
            >
              {{ expandedMap.get(person.personnel_id) ? '收起' : '展开工卡' }}
            </el-button>
          </div>
        </div>
        
        <!-- Hours Progress Bar -->
        <div class="p-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-700">
              已分配 {{ person.total_hours }} 小时
            </span>
            <span class="text-sm text-gray-500">
              {{ person.total_hours }}h
            </span>
          </div>
          <el-progress 
            :percentage="Math.min(100, Math.round((person.total_hours / 80) * 100))" 
            :status="getStatusForHours(person.total_hours)"
            :show-text="false"
          />
        </div>
        
        <!-- Workcard List (Collapsible) -->
        <div 
          v-if="expandedMap.get(person.personnel_id)" 
          class="p-4 border-t border-gray-200 bg-gray-50"
        >
          <h4 class="font-medium text-gray-700 mb-3">工卡列表</h4>
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    工卡号
                  </th>
                  <th scope="col" class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    标题
                  </th>
                  <th scope="col" class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    状态
                  </th>
                  <th scope="col" class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    小时
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="workcard in person.workcards" :key="workcard.workcard_no">
                  <td class="px-4 py-2 whitespace-nowrap text-sm font-medium text-gray-900">
                    {{ workcard.workcard_no }}
                  </td>
                  <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-900">
                    {{ workcard.title }}
                  </td>
                  <td class="px-4 py-2 whitespace-nowrap">
                    <span 
                      class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                      :class="{
                        'bg-green-100 text-green-800': workcard.status === '已完成',
                        'bg-yellow-100 text-yellow-800': workcard.status === '进行中',
                        'bg-red-100 text-red-800': workcard.status === '已延期',
                        'bg-gray-100 text-gray-800': workcard.status === '待开始'
                      }"
                    >
                      {{ workcard.status }}
                    </span>
                  </td>
                  <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-500">
                    {{ workcard.hours }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      
      <!-- Empty State -->
      <div v-if="sortedList.length === 0 && !loading" class="text-center py-12">
        <div class="text-gray-400 mb-4">暂无工作负荷数据</div>
        <p class="text-gray-500">请检查筛选条件或稍后重试</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Custom styles for better appearance */
.el-progress__bar {
  height: 12px;
}
.el-progress__text {
  font-size: 12px;
}
</style>