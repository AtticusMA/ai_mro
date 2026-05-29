<template>
  <div class="hangar-overview">
    <el-row :gutter="16" class="mb-4">
      <el-col :span="8" v-for="hangar in hangars" :key="hangar.id">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="title">{{ hangar.name }}</span>
              <el-tag :type="hangar.status === 'active' ? 'success' : 'warning'" size="small">
                {{ hangar.status === 'active' ? '运行中' : '维护中' }}
              </el-tag>
            </div>
          </template>
          <el-descriptions :column="2" size="small">
            <el-descriptions-item label="容量">{{ hangar.capacity }}架</el-descriptions-item>
            <el-descriptions-item label="在位">{{ hangar.occupied }}架</el-descriptions-item>
            <el-descriptions-item label="温度">{{ hangar.temperature }}°C</el-descriptions-item>
            <el-descriptions-item label="湿度">{{ hangar.humidity }}%</el-descriptions-item>
          </el-descriptions>
          <el-progress :percentage="Math.round(hangar.occupied / hangar.capacity * 100)" :stroke-width="10" class="mt-2" />
        </el-card>
      </el-col>
    </el-row>
    <el-card shadow="never">
      <template #header><span class="title">机位排程</span></template>
      <el-table :data="schedule" stripe size="small">
        <el-table-column prop="aircraft_id" label="飞机" width="90" />
        <el-table-column prop="task" label="任务" width="80" />
        <el-table-column prop="bay" label="机位" width="70" />
        <el-table-column prop="start_date" label="开始" width="110" />
        <el-table-column prop="end_date" label="结束" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.status === 'scheduled' ? '已排' : row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHangarList, getScheduleList } from '@/api/hangar'

const hangars = ref([])
const schedule = ref([])

const fetchData = async () => {
  const [hangarRes, schedRes] = await Promise.all([getHangarList(), getScheduleList()])
  if (hangarRes.code === 200) hangars.value = hangarRes.data.list
  if (schedRes.code === 200) schedule.value = schedRes.data.list
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-weight: 600; }
</style>
