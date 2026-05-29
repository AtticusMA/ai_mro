<template>
  <div class="tool-management">
    <el-row :gutter="16">
      <!-- Left: Tool Ledger -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>工具台账</span>
              <div class="filters">
                <el-select v-model="toolQuery.status" placeholder="状态" clearable size="small" style="width:120px" @change="loadTools">
                  <el-option label="在柜" value="in_cabinet" />
                  <el-option label="已借出" value="borrowed" />
                  <el-option label="维修中" value="maintenance" />
                </el-select>
                <el-select v-model="toolQuery.cabinetId" placeholder="工具柜" clearable size="small" style="width:140px;margin-left:8px" @change="loadTools">
                  <el-option v-for="c in cabinets" :key="c.id" :label="c.name" :value="c.id" />
                </el-select>
              </div>
            </div>
          </template>
          <el-table :data="toolList" size="small" stripe>
            <el-table-column label="工具名称" prop="name" min-width="140" />
            <el-table-column label="编号" prop="toolCode" width="100" />
            <el-table-column label="RFID标签" prop="rfidTag" width="110" />
            <el-table-column label="所在工具柜" prop="cabinetName" width="130" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="检定到期" prop="calibrationDue" width="110" />
            <el-table-column label="使用次数" prop="useCount" width="80" align="center" />
          </el-table>
          <el-pagination
            v-model:current-page="toolQuery.pageNum"
            v-model:page-size="toolQuery.pageSize"
            :total="toolTotal"
            layout="total, prev, pager, next"
            style="margin-top:12px"
            @current-change="loadTools"
          />
        </el-card>
      </el-col>

      <!-- Right: Cabinets + Alerts -->
      <el-col :span="8">
        <!-- Cabinet Overview -->
        <el-card style="margin-bottom:16px">
          <template #header>
            <div class="card-header">
              <span>工具柜状态</span>
              <el-button size="small" @click="loadCabinets">刷新</el-button>
            </div>
          </template>
          <div v-for="cab in cabinets" :key="cab.id" class="cabinet-item" @click="showSlots(cab)">
            <div class="cabinet-name">
              <el-badge :value="cab.onlineStatus === 'offline' ? '离线' : ''" type="danger">
                <span>{{ cab.name }}</span>
              </el-badge>
            </div>
            <div class="cabinet-stats">
              <span>{{ cab.location }}</span>
              <span>可用: {{ cab.availableSlots }}/{{ cab.slotCount }}</span>
              <span v-if="cab.temperature">{{ cab.temperature }}°C / {{ cab.humidity }}%RH</span>
            </div>
          </div>
        </el-card>

        <!-- Overdue Alerts -->
        <el-card>
          <template #header><span>超时预警</span></template>
          <div v-for="alert in alerts" :key="alert.id" class="alert-item">
            <div class="alert-tool">{{ alert.toolName }}</div>
            <div class="alert-info">
              <span>借用人: {{ alert.borrowerName }}</span>
              <el-tag type="danger" size="small">超时 {{ alert.overdueHours }}h</el-tag>
            </div>
          </div>
          <el-empty v-if="alerts.length === 0" description="暂无超时记录" :image-size="40" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Borrow Records -->
    <el-card style="margin-top:16px">
      <template #header>
        <div class="card-header">
          <span>借还记录</span>
          <el-select v-model="recordQuery.status" placeholder="状态" clearable size="small" style="width:120px" @change="loadRecords">
            <el-option label="借出中" value="borrowed" />
            <el-option label="已归还" value="returned" />
            <el-option label="逾期" value="overdue" />
          </el-select>
        </div>
      </template>
      <el-table :data="recordList" size="small" stripe>
        <el-table-column label="工具名称" prop="toolName" min-width="130" />
        <el-table-column label="借用人" prop="userName" width="90" />
        <el-table-column label="借出时间" prop="borrowTime" width="160" />
        <el-table-column label="预计归还" prop="expectedReturn" width="160" />
        <el-table-column label="实际归还" prop="actualReturn" width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="recordStatusType(row.status)" size="small">{{ recordStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="recordQuery.pageNum"
        v-model:page-size="recordQuery.pageSize"
        :total="recordTotal"
        layout="total, prev, pager, next"
        style="margin-top:12px"
        @current-change="loadRecords"
      />
    </el-card>

    <!-- Cabinet Slots Dialog -->
    <el-dialog v-model="slotsDialogVisible" :title="`${selectedCabinet?.name} — 格口详情`" width="700px">
      <el-table :data="slotList" size="small" stripe>
        <el-table-column label="格口" prop="slotNo" width="60" align="center" />
        <el-table-column label="工具名称" prop="toolName" />
        <el-table-column label="编号" prop="toolCode" width="100" />
        <el-table-column label="RFID标签" prop="rfidTag" width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.toolId" :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            <el-tag v-else type="info" size="small">空</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listCabinets, getCabinetSlots, listTools, listBorrowRecords, listAlerts } from '@/api/tool'

const cabinets = ref([])
const toolList = ref([])
const toolTotal = ref(0)
const recordList = ref([])
const recordTotal = ref(0)
const alerts = ref([])
const slotList = ref([])
const slotsDialogVisible = ref(false)
const selectedCabinet = ref(null)

const toolQuery = ref({ pageNum: 1, pageSize: 20, status: null, cabinetId: null })
const recordQuery = ref({ pageNum: 1, pageSize: 20, status: null })

const statusLabel = (s) => ({ in_cabinet: '在柜', borrowed: '借出', maintenance: '维修', lost: '遗失' }[s] || s)
const statusType = (s) => ({ in_cabinet: 'success', borrowed: 'warning', maintenance: 'info', lost: 'danger' }[s] || '')
const recordStatusLabel = (s) => ({ borrowed: '借出中', returned: '已归还', overdue: '逾期' }[s] || s)
const recordStatusType = (s) => ({ borrowed: 'warning', returned: 'success', overdue: 'danger' }[s] || '')

async function loadCabinets() {
  const res = await listCabinets()
  cabinets.value = res.data?.list || []
}

async function loadTools() {
  const params = { ...toolQuery.value }
  Object.keys(params).forEach(k => params[k] == null && delete params[k])
  const res = await listTools(params)
  toolList.value = res.data?.list || []
  toolTotal.value = res.data?.total || 0
}

async function loadRecords() {
  const params = { ...recordQuery.value }
  Object.keys(params).forEach(k => params[k] == null && delete params[k])
  const res = await listBorrowRecords(params)
  recordList.value = res.data?.list || []
  recordTotal.value = res.data?.total || 0
}

async function loadAlerts() {
  const res = await listAlerts({ alertType: 'overdue', pageNum: 1, pageSize: 10 })
  alerts.value = res.data?.list || []
}

async function showSlots(cabinet) {
  selectedCabinet.value = cabinet
  const res = await getCabinetSlots(cabinet.id)
  slotList.value = res.data?.slots || []
  slotsDialogVisible.value = true
}

onMounted(() => {
  loadCabinets()
  loadTools()
  loadRecords()
  loadAlerts()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.filters { display: flex; align-items: center; }
.cabinet-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.cabinet-item:last-child { border-bottom: none; }
.cabinet-name { font-weight: 500; margin-bottom: 4px; }
.cabinet-stats { font-size: 12px; color: #909399; display: flex; gap: 12px; }
.alert-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.alert-item:last-child { border-bottom: none; }
.alert-tool { font-weight: 500; margin-bottom: 4px; }
.alert-info { font-size: 12px; display: flex; justify-content: space-between; align-items: center; }
</style>
