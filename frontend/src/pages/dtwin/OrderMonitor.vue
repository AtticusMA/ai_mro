<template>
  <div>
    <el-row justify="space-between" align="middle" class="mb-3">
      <el-col :span="18">
        <el-form inline size="small">
          <el-form-item label="计划ID">
            <el-input v-model="query.planId" clearable style="width:120px" @change="load" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="全部" style="width:110px" @change="load">
              <el-option label="待执行" value="pending" />
              <el-option label="执行中" value="executing" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="6" style="text-align:right">
        <el-button type="primary" size="small" @click="openCreate">下发指令</el-button>
      </el-col>
    </el-row>

    <el-table :data="orders" stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="指令ID" width="80" />
      <el-table-column prop="workstationName" label="工位" width="120" />
      <el-table-column prop="assigneeName" label="执行人" width="100" />
      <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column label="进度" width="160">
        <template #default="{ row }">
          <el-progress :percentage="row.progress" :stroke-width="8" />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="orderTagType(row.status)">{{ ORDER_STATUS[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link size="small" @click="openProgress(row)">更新进度</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, prev, pager, next"
      class="mt-3"
      @change="load"
    />

    <!-- Create order dialog -->
    <el-dialog v-model="createVisible" title="下发维修指令" width="440px">
      <el-form :model="createForm" label-width="80px" size="small">
        <el-form-item label="计划ID">
          <el-input v-model="createForm.planId" />
        </el-form-item>
        <el-form-item label="工位ID">
          <el-input v-model="createForm.workstationId" />
        </el-form-item>
        <el-form-item label="执行人ID">
          <el-input v-model="createForm.assigneeId" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="createVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- Update progress dialog -->
    <el-dialog v-model="progressVisible" title="更新进度" width="360px">
      <el-form :model="progressForm" label-width="80px" size="small">
        <el-form-item label="进度">
          <el-slider v-model="progressForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="progressForm.status" style="width:100%">
            <el-option label="待执行" value="pending" />
            <el-option label="执行中" value="executing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="progressVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doUpdateProgress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderList, createOrder, updateOrderProgress } from '@/api/dtwin'

const ORDER_STATUS = { pending: '待执行', executing: '执行中', completed: '已完成' }
function orderTagType(s) {
  return { pending: 'info', executing: 'primary', completed: 'success' }[s] ?? 'info'
}

const route = useRoute()
const loading = ref(false)
const saving  = ref(false)
const orders  = ref([])
const total   = ref(0)
const query   = ref({ pageNum: 1, pageSize: 20, planId: route.query.planId || null, status: null })
const createVisible  = ref(false)
const progressVisible = ref(false)
const createForm  = ref({})
const progressForm = ref({ orderId: null, progress: 0, status: 'executing' })

async function load() {
  loading.value = true
  const res = await getOrderList(query.value).finally(() => loading.value = false)
  if (res.code === 200) { orders.value = res.data.list; total.value = res.data.total }
}

function openCreate() {
  createForm.value = { planId: query.value.planId, workstationId: '', assigneeId: '', description: '' }
  createVisible.value = true
}

async function doCreate() {
  saving.value = true
  const res = await createOrder(createForm.value).finally(() => saving.value = false)
  if (res.code === 200) { ElMessage.success('指令下发成功'); createVisible.value = false; load() }
}

function openProgress(row) {
  progressForm.value = { orderId: row.id, progress: row.progress, status: row.status }
  progressVisible.value = true
}

async function doUpdateProgress() {
  saving.value = true
  const { orderId, progress, status } = progressForm.value
  const res = await updateOrderProgress(orderId, { progress, status }).finally(() => saving.value = false)
  if (res.code === 200) { ElMessage.success('进度已更新'); progressVisible.value = false; load() }
}

onMounted(load)
</script>

<style scoped>
.mb-3 { margin-bottom: 12px; }
.mt-3 { margin-top: 12px; }
</style>
