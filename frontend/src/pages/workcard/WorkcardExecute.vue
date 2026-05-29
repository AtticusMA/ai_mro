<template>
  <div class="pb-20 md:pb-4">
    <!-- Sticky header -->
    <div class="sticky top-0 bg-white z-10 border-b px-4 py-3 flex items-center gap-3">
      <el-button @click="router.push('/mro/workcard')" text size="small">← 返回</el-button>
      <div class="flex-1 min-w-0">
        <div class="font-bold truncate">{{ workcard.workcard_no }} — {{ workcard.title }}</div>
        <div class="flex gap-2 mt-1">
          <el-tag size="small" :type="statusType(workcard.status)">{{ workcard.status }}</el-tag>
          <el-tag size="small" type="info">{{ workcard.aircraft_reg }}</el-tag>
        </div>
      </div>
    </div>

    <div class="p-4" v-loading="loading">
      <!-- Progress -->
      <el-card class="mb-4">
        <div class="flex items-center justify-between mb-2">
          <span class="font-medium">执行进度</span>
          <span class="text-sm text-gray-500">{{ confirmedCount }}/{{ steps.length }} 步</span>
        </div>
        <el-progress :percentage="progressPct" :status="progressPct === 100 ? 'success' : ''" />
      </el-card>

      <!-- Check in/out -->
      <el-card class="mb-4">
        <div class="flex gap-3 justify-center">
          <el-button
            v-if="!checkedIn"
            type="success"
            size="large"
            :loading="checkinLoading"
            @click="handleCheckin"
            style="min-width: 160px"
          >签到开始工作</el-button>
          <el-button
            v-else
            type="danger"
            size="large"
            :loading="checkoutLoading"
            @click="handleCheckout"
            style="min-width: 160px"
          >签退完成工作</el-button>
        </div>
        <div v-if="checkinTime" class="text-center text-sm text-gray-500 mt-2">签到时间: {{ checkinTime }}</div>
      </el-card>

      <!-- Steps -->
      <div class="space-y-3">
        <el-card
          v-for="step in steps"
          :key="step.id"
          :class="['transition-all', step.status === 'confirmed' ? 'border-green-300' : '']"
        >
          <div class="flex items-start gap-3">
            <div :class="['flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold',
              step.status === 'confirmed' ? 'bg-green-500 text-white' :
              step.status === 'skipped' ? 'bg-yellow-400 text-white' : 'bg-gray-200 text-gray-600']">
              {{ step.status === 'confirmed' ? '✓' : step.step_no }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-medium">{{ step.title }}</div>
              <div class="text-sm text-gray-500 mt-1">{{ step.description }}</div>
              <div v-if="step.status === 'confirmed'" class="text-xs text-green-600 mt-1">
                ✓ {{ step.confirmed_by }} 于 {{ step.confirmed_at }} 确认
              </div>

              <!-- Photos -->
              <div v-if="step.requires_photo" class="mt-2">
                <div class="flex gap-2 flex-wrap">
                  <img
                    v-for="(photo, i) in (step.photos || [])"
                    :key="i"
                    :src="photo"
                    class="w-16 h-16 object-cover rounded border"
                  />
                  <label v-if="step.status !== 'confirmed'" class="cursor-pointer">
                    <div class="w-16 h-16 border-2 border-dashed border-gray-300 rounded flex items-center justify-center text-gray-400 hover:border-blue-400">
                      <span class="text-2xl">+</span>
                    </div>
                    <input
                      type="file"
                      accept="image/*"
                      capture="environment"
                      class="hidden"
                      @change="(e) => handlePhotoUpload(e, step.id)"
                    />
                  </label>
                </div>
              </div>

              <!-- Confirm button -->
              <div v-if="step.status === 'pending'" class="mt-3">
                <el-button type="primary" size="large" @click="openConfirmDialog(step)" style="width:100%">
                  确认完成此步骤
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- Mobile sticky bottom: anomaly report -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t p-3 flex gap-3 md:hidden">
      <el-button type="warning" size="large" style="flex:1" @click="router.push(`/mro/workcard/${workcardId}/report`)">
        上报异常
      </el-button>
    </div>
    <!-- Desktop anomaly button -->
    <div class="hidden md:block px-4 pb-4">
      <el-button type="warning" @click="router.push(`/mro/workcard/${workcardId}/report`)">上报异常</el-button>
    </div>

    <!-- Confirm Step Dialog -->
    <el-dialog
      v-model="confirmDialogVisible"
      :title="`确认步骤: ${activeStep?.title}`"
      width="90%"
      style="max-width:500px"
    >
      <el-form :model="confirmForm" label-position="top">
        <el-form-item label="备注说明">
          <el-input v-model="confirmForm.notes" type="textarea" :rows="3" placeholder="可填写执行备注..." />
        </el-form-item>
        <el-form-item label="工号 (电子签名)" required>
          <el-input v-model="confirmForm.employee_no" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="密码确认" required>
          <el-input v-model="confirmForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="confirmLoading" @click="submitConfirm">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWorkcardDetail, getWorkcardSteps, confirmStep, checkin, checkout, uploadPhoto } from '@/api/workcard'

const route = useRoute()
const router = useRouter()
const workcardId = route.params.id

const loading = ref(false)
const workcard = ref({})
const steps = ref([])
const checkedIn = ref(false)
const checkinTime = ref('')
const checkinLoading = ref(false)
const checkoutLoading = ref(false)
const confirmDialogVisible = ref(false)
const confirmLoading = ref(false)
const activeStep = ref(null)
const confirmForm = ref({ notes: '', employee_no: '', password: '' })

const confirmedCount = computed(() => steps.value.filter(s => s.status === 'confirmed').length)
const progressPct = computed(() =>
  steps.value.length ? Math.round(confirmedCount.value / steps.value.length * 100) : 0
)

function statusType(status) {
  const map = { '待派工': 'info', '已派工': '', '执行中': 'warning', '待质检': '', '质检通过': 'success', '已关闭': 'success' }
  return map[status] ?? 'info'
}

async function loadData() {
  loading.value = true
  try {
    const [wcRes, stepsRes] = await Promise.all([
      getWorkcardDetail(workcardId),
      getWorkcardSteps(workcardId)
    ])
    workcard.value = wcRes.data || {}
    steps.value = stepsRes.data?.list || []
    if (workcard.value.status === '执行中') checkedIn.value = true
  } catch (e) {
    ElMessage.error('加载工卡数据失败')
  } finally {
    loading.value = false
  }
}

async function handleCheckin() {
  checkinLoading.value = true
  try {
    await checkin(workcardId, { employee_no: 'current-user' })
    checkedIn.value = true
    checkinTime.value = new Date().toLocaleString('zh-CN')
    ElMessage.success('签到成功，开始工作')
  } catch (e) {
    ElMessage.error('签到失败')
  } finally {
    checkinLoading.value = false
  }
}

async function handleCheckout() {
  checkoutLoading.value = true
  try {
    await checkout(workcardId, { employee_no: 'current-user' })
    checkedIn.value = false
    ElMessage.success('签退成功')
    router.push('/mro/workcard')
  } catch (e) {
    ElMessage.error('签退失败')
  } finally {
    checkoutLoading.value = false
  }
}

function openConfirmDialog(step) {
  activeStep.value = step
  confirmForm.value = { notes: '', employee_no: '', password: '' }
  confirmDialogVisible.value = true
}

async function submitConfirm() {
  if (!confirmForm.value.employee_no || !confirmForm.value.password) {
    ElMessage.warning('请填写工号和密码')
    return
  }
  confirmLoading.value = true
  try {
    await confirmStep(workcardId, activeStep.value.id, confirmForm.value)
    const step = steps.value.find(s => s.id === activeStep.value.id)
    if (step) {
      step.status = 'confirmed'
      step.confirmed_by = confirmForm.value.employee_no
      step.confirmed_at = new Date().toLocaleString('zh-CN')
    }
    confirmDialogVisible.value = false
    ElMessage.success('步骤确认成功')
  } catch (e) {
    ElMessage.error('确认失败')
  } finally {
    confirmLoading.value = false
  }
}

async function handlePhotoUpload(event, stepId) {
  const file = event.target.files[0]
  if (!file) return
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('step_id', stepId)
    const res = await uploadPhoto(workcardId, formData)
    const step = steps.value.find(s => s.id === stepId)
    if (step && res.data?.url) {
      step.photos = [...(step.photos || []), res.data.url]
    }
    ElMessage.success('照片上传成功')
  } catch (e) {
    ElMessage.error('照片上传失败')
  }
}

onMounted(loadData)
</script>
