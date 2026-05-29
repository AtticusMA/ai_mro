<template>
  <div class="p-4 md:p-6">
    <!-- Header -->
    <div class="flex items-center gap-3 mb-6">
      <el-button @click="router.push('/mro/planning')" text>← 返回</el-button>
      <h1 class="text-xl font-bold">人员工作负荷</h1>
    </div>

    <!-- Summary & Controls -->
    <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
      <div class="flex gap-4 text-sm text-gray-600">
        <span>共 <strong>{{ filteredList.length }}</strong> 人</span>
        <span>合计 <strong>{{ totalHours }}</strong> 小时</span>
        <span>人均 <strong>{{ avgHours }}</strong> 小时</span>
      </div>
      <div class="flex gap-2 flex-wrap">
        <el-select v-model="filterDept" placeholder="部门筛选" clearable style="width:160px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="机体维修部" value="机体维修部" />
          <el-option label="发动机维修部" value="发动机维修部" />
          <el-option label="航电维修部" value="航电维修部" />
          <el-option label="结构修理部" value="结构修理部" />
        </el-select>
        <el-button-group>
          <el-button :type="sortBy === 'hours' ? 'primary' : ''" @click="sortBy = 'hours'">按负荷排序</el-button>
          <el-button :type="sortBy === 'name' ? 'primary' : ''" @click="sortBy = 'name'">按姓名排序</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- Workload Cards -->
    <div v-loading="loading" class="space-y-3">
      <el-card v-for="item in sortedFilteredList" :key="item.personnel_id" class="hover:shadow-md transition-shadow">
        <div>
          <!-- Person header -->
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-2">
              <span class="font-semibold text-base">{{ item.name }}</span>
              <el-tag size="small" type="info">{{ item.employee_no }}</el-tag>
              <el-tag size="small">{{ item.department }}</el-tag>
            </div>
            <el-badge :value="item.active_workcards" type="primary">
              <el-tag>活跃工卡</el-tag>
            </el-badge>
          </div>
          <!-- Hours progress bar -->
          <div class="mb-2">
            <div class="flex justify-between text-sm text-gray-600 mb-1">
              <span>已分配工时</span>
              <span class="font-medium">{{ item.total_hours }} 小时</span>
            </div>
            <el-progress
              :percentage="Math.min(Math.round(item.total_hours / 80 * 100), 100)"
              :status="item.total_hours > 60 ? 'exception' : item.total_hours > 40 ? 'warning' : 'success'"
              :stroke-width="10"
            />
          </div>
          <!-- Workcard list toggle -->
          <el-collapse-transition>
            <div v-if="expanded.has(item.personnel_id)" class="mt-3 border-t pt-3">
              <el-table :data="item.workcards" size="small" stripe>
                <el-table-column prop="workcard_no" label="工卡编号" width="130" />
                <el-table-column prop="title" label="工卡标题" min-width="180" show-overflow-tooltip />
                <el-table-column label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag size="small" :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="hours" label="工时(h)" width="80" />
              </el-table>
            </div>
          </el-collapse-transition>
          <div class="mt-2 text-right">
            <el-button text size="small" @click="toggleExpand(item.personnel_id)">
              {{ expanded.has(item.personnel_id) ? '收起工卡 ▲' : '展开工卡 ▼' }}
            </el-button>
          </div>
        </div>
      </el-card>
      <el-empty v-if="!loading && sortedFilteredList.length === 0" description="暂无数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getWorkload } from '@/api/planning'

const router = useRouter()
const loading = ref(false)
const workloadList = ref([])
const filterDept = ref('')
const sortBy = ref('hours')
const expanded = reactive(new Set())

function toggleExpand(id) {
  if (expanded.has(id)) {
    expanded.delete(id)
  } else {
    expanded.add(id)
  }
}

function statusType(status) {
  const map = { '待开始': 'info', '执行中': 'warning', '待质检': '', '质检通过': 'success', '已关闭': 'success' }
  return map[status] ?? 'info'
}

function statusLabel(status) {
  return status || '未知'
}

const filteredList = computed(() => {
  if (!filterDept.value) return workloadList.value
  return workloadList.value.filter(p => p.department === filterDept.value)
})

const sortedFilteredList = computed(() => {
  const list = [...filteredList.value]
  if (sortBy.value === 'hours') return list.sort((a, b) => b.total_hours - a.total_hours)
  return list.sort((a, b) => a.name.localeCompare(b.name))
})

const totalHours = computed(() => filteredList.value.reduce((s, p) => s + (p.total_hours || 0), 0))
const avgHours = computed(() => {
  const len = filteredList.value.length
  return len ? Math.round(totalHours.value / len) : 0
})

async function loadData() {
  loading.value = true
  try {
    const res = await getWorkload({ department: filterDept.value || undefined })
    workloadList.value = res.data?.list || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
