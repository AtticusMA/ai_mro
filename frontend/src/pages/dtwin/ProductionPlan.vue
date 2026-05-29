<template>
  <div>
    <!-- Toolbar -->
    <el-row justify="space-between" align="middle" class="mb-3">
      <el-col :span="16">
        <el-form inline size="small">
          <el-form-item label="机库">
            <el-select v-model="query.hangarId" clearable placeholder="全部机库" style="width:130px" @change="load">
              <el-option v-for="h in hangars" :key="h.id" :label="h.name" :value="h.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="全部" style="width:110px" @change="load">
              <el-option label="草稿" value="draft" />
              <el-option label="进行中" value="in_progress" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="query.planType" clearable placeholder="全部" style="width:110px" @change="load">
              <el-option label="航线维修" value="line" />
              <el-option label="定检" value="heavy" />
              <el-option label="部件" value="component" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="8" style="text-align:right">
        <el-button type="primary" size="small" @click="openCreate">新建计划</el-button>
      </el-col>
    </el-row>

    <el-table :data="plans" stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="aircraftId" label="飞机注册号" width="110" />
      <el-table-column prop="planType" label="类型" width="90">
        <template #default="{ row }">{{ PLAN_TYPE[row.planType] || row.planType }}</template>
      </el-table-column>
      <el-table-column label="计划开始" width="130">
        <template #default="{ row }">{{ fmtDate(row.scheduledStart) }}</template>
      </el-table-column>
      <el-table-column label="计划结束" width="130">
        <template #default="{ row }">{{ fmtDate(row.scheduledEnd) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="planTagType(row.status)">{{ PLAN_STATUS[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link size="small" type="primary" @click="viewOrders(row)">指令</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑计划' : '新建计划'" width="480px">
      <el-form :model="editForm" label-width="90px" size="small">
        <el-form-item label="机库">
          <el-select v-model="editForm.hangarId" style="width:100%">
            <el-option v-for="h in hangars" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="飞机注册号">
          <el-input v-model="editForm.aircraftId" />
        </el-form-item>
        <el-form-item label="计划类型">
          <el-select v-model="editForm.planType" style="width:100%">
            <el-option label="航线维修" value="line" />
            <el-option label="定检" value="heavy" />
            <el-option label="部件" value="component" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="editForm.scheduledStart" type="datetime" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="editForm.scheduledEnd" type="datetime" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="editForm.id" label="状态">
          <el-select v-model="editForm.status" style="width:100%">
            <el-option label="草稿" value="draft" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHangarList, getPlanList, createPlan, updatePlan } from '@/api/dtwin'

const PLAN_TYPE   = { line: '航线维修', heavy: '定检', component: '部件' }
const PLAN_STATUS = { draft: '草稿', in_progress: '进行中', completed: '已完成' }

function planTagType(s) {
  return { draft: 'info', in_progress: 'primary', completed: 'success' }[s] ?? 'info'
}
function fmtDate(iso) {
  return iso ? iso.replace('T', ' ').replace('Z', '').slice(0, 16) : '—'
}

const router = useRouter()
const loading = ref(false)
const saving  = ref(false)
const plans   = ref([])
const total   = ref(0)
const hangars = ref([])
const query   = ref({ pageNum: 1, pageSize: 20, hangarId: null, status: null, planType: null })
const dialogVisible = ref(false)
const editForm = ref({})

async function load() {
  loading.value = true
  const res = await getPlanList(query.value).finally(() => loading.value = false)
  if (res.code === 200) { plans.value = res.data.list; total.value = res.data.total }
}

function openCreate() {
  editForm.value = { hangarId: null, aircraftId: '', planType: 'heavy', scheduledStart: null, scheduledEnd: null }
  dialogVisible.value = true
}

function openEdit(row) {
  editForm.value = { ...row, scheduledStart: row.scheduledStart ? new Date(row.scheduledStart) : null, scheduledEnd: row.scheduledEnd ? new Date(row.scheduledEnd) : null }
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  const payload = {
    ...editForm.value,
    scheduledStart: editForm.value.scheduledStart ? new Date(editForm.value.scheduledStart).toISOString() : null,
    scheduledEnd:   editForm.value.scheduledEnd   ? new Date(editForm.value.scheduledEnd).toISOString()   : null
  }
  const res = await (editForm.value.id ? updatePlan(payload) : createPlan(payload)).finally(() => saving.value = false)
  if (res.code === 200) {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  }
}

function viewOrders(row) {
  router.push({ name: 'DtwinOrders', query: { planId: row.id } })
}

onMounted(async () => {
  const res = await getHangarList()
  if (res.code === 200) hangars.value = res.data.list
  load()
})
</script>

<style scoped>
.mb-3 { margin-bottom: 12px; }
.mt-3 { margin-top: 12px; }
</style>
