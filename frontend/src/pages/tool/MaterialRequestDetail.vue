<template>
  <div class="p-4 md:p-6 max-w-4xl mx-auto">
    <!-- Header -->
    <div class="flex items-center gap-3 mb-6">
      <el-button @click="router.push('/mro/material-request')" text>← 返回</el-button>
      <h1 class="text-xl font-bold flex-1">{{ detail.request_no }}</h1>
      <el-tag :type="urgencyType(detail.urgency)" class="mr-2">{{ urgencyLabel(detail.urgency) }}</el-tag>
      <el-tag :type="statusType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
    </div>

    <div v-loading="loading">
      <!-- Rejected alert -->
      <el-alert
        v-if="detail.status === 'rejected' && detail.reject_reason"
        type="error"
        :title="`拒绝原因: ${detail.reject_reason}`"
        :closable="false"
        class="mb-4"
      />

      <!-- Status timeline -->
      <el-card class="mb-4">
        <h3 class="font-semibold mb-4">审批流程</h3>
        <el-steps :active="statusStep" finish-status="success" :process-status="detail.status === 'rejected' ? 'error' : 'process'">
          <el-step title="申请提交" />
          <el-step title="审批中" />
          <el-step :title="detail.status === 'rejected' ? '已拒绝' : '已批准'" />
          <el-step title="发料中" />
          <el-step title="已接收" />
        </el-steps>
      </el-card>

      <!-- Basic info -->
      <el-card class="mb-4">
        <h3 class="font-semibold mb-3">申请信息</h3>
        <el-descriptions :column="isMobile ? 1 : 2" border>
          <el-descriptions-item label="申请编号">{{ detail.request_no }}</el-descriptions-item>
          <el-descriptions-item label="关联工卡">{{ detail.workcard_no }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.requester }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ detail.department }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.requested_at }}</el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag :type="urgencyType(detail.urgency)" size="small">{{ urgencyLabel(detail.urgency) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请标题" :span="2">{{ detail.title }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Items table -->
      <el-card class="mb-4">
        <h3 class="font-semibold mb-3">申请零件清单</h3>
        <el-table :data="detail.items || []" stripe>
          <el-table-column prop="part_no" label="零件编号" width="140" />
          <el-table-column prop="part_name" label="零件名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="quantity" label="申请数量" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="warehouse_qty" label="库存数量" width="100" />
          <el-table-column label="库存状态" width="100">
            <template #default="{ row }">
              <el-tag
                size="small"
                :type="row.status === 'available' ? 'success' : row.status === 'insufficient' ? 'warning' : 'info'"
              >
                {{ row.status === 'available' ? '充足' : row.status === 'insufficient' ? '不足' : '已订购' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- Action buttons -->
      <el-card v-if="detail.status === 'pending_approval' || detail.status === 'delivered'" class="mb-4">
        <h3 class="font-semibold mb-3">操作</h3>
        <div class="flex gap-3 flex-wrap">
          <template v-if="detail.status === 'pending_approval'">
            <el-button type="success" size="large" :loading="approving" @click="handleApprove">批准申请</el-button>
            <el-button type="danger" size="large" @click="rejectDialogVisible = true">拒绝申请</el-button>
          </template>
          <el-button
            v-if="detail.status === 'delivered'"
            type="primary"
            size="large"
            :loading="receiving"
            @click="handleReceive"
          >确认收货</el-button>
        </div>
      </el-card>

      <!-- Timeline -->
      <el-card v-if="(detail.timeline || []).length > 0">
        <h3 class="font-semibold mb-3">操作记录</h3>
        <div class="space-y-3">
          <div
            v-for="(item, i) in detail.timeline"
            :key="i"
            class="flex gap-3 text-sm"
          >
            <div class="w-2 h-2 rounded-full bg-blue-400 mt-1.5 flex-shrink-0"></div>
            <div>
              <span class="font-medium">{{ item.event }}</span>
              <span class="text-gray-500 ml-2">{{ item.operator }}</span>
              <span class="text-gray-400 ml-2">{{ item.time }}</span>
              <div v-if="item.note" class="text-gray-500 mt-0.5">{{ item.note }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Reject dialog -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝申请" width="90%" style="max-width:480px">
      <el-form label-position="top">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请填写拒绝原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getMaterialRequestDetail,
  approveMaterialRequest,
  rejectMaterialRequest,
  confirmReceive
} from '@/api/material-request'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const loading = ref(false)
const detail = ref({})
const approving = ref(false)
const rejecting = ref(false)
const receiving = ref(false)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const isMobile = ref(window.innerWidth < 768)

const statusStep = computed(() => {
  const map = { pending_approval: 1, approved: 2, rejected: 2, delivered: 3, received: 4 }
  return map[detail.value.status] ?? 0
})

function statusType(s) {
  const m = { pending_approval: 'warning', approved: 'success', rejected: 'danger', delivered: '', received: 'success' }
  return m[s] ?? 'info'
}

function statusLabel(s) {
  const m = { pending_approval: '待审批', approved: '已批准', rejected: '已拒绝', delivered: '已发料', received: '已接收' }
  return m[s] ?? s
}

function urgencyType(u) {
  return u === 'high' ? 'danger' : u === 'low' ? 'info' : 'warning'
}

function urgencyLabel(u) {
  return u === 'high' ? '紧急' : u === 'low' ? '低优先' : '普通'
}

async function load() {
  loading.value = true
  try {
    const res = await getMaterialRequestDetail(id)
    detail.value = res.data || {}
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleApprove() {
  approving.value = true
  try {
    await approveMaterialRequest(id, {})
    detail.value.status = 'approved'
    ElMessage.success('已批准申请')
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    approving.value = false
  }
}

async function handleReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  rejecting.value = true
  try {
    await rejectMaterialRequest(id, { reason: rejectReason.value })
    detail.value.status = 'rejected'
    detail.value.reject_reason = rejectReason.value
    rejectDialogVisible.value = false
    ElMessage.success('已拒绝申请')
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    rejecting.value = false
  }
}

async function handleReceive() {
  receiving.value = true
  try {
    await confirmReceive(id, {})
    detail.value.status = 'received'
    ElMessage.success('已确认收货')
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    receiving.value = false
  }
}

onMounted(load)
</script>
