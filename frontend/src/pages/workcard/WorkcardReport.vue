<template>
  <div class="p-4 max-w-2xl mx-auto">
    <!-- Header -->
    <div class="flex items-center gap-3 mb-6">
      <el-button @click="router.back()" text>← 返回</el-button>
      <h1 class="text-xl font-bold">上报异常</h1>
    </div>

    <!-- Workcard info banner -->
    <el-alert
      v-if="workcard.workcard_no"
      :title="`工卡: ${workcard.workcard_no} — ${workcard.title}`"
      type="info"
      :closable="false"
      class="mb-4"
    />

    <!-- Report form -->
    <el-card class="mb-6">
      <h3 class="font-semibold mb-4">新增异常报告</h3>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="关联步骤" prop="step_id">
          <el-select v-model="form.step_id" placeholder="选择关联步骤（可选）" clearable style="width:100%">
            <el-option
              v-for="step in steps"
              :key="step.id"
              :label="`步骤${step.step_no}: ${step.title}`"
              :value="step.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="异常标题" prop="title">
          <el-input v-model="form.title" placeholder="简要描述异常情况" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="严重程度" prop="severity">
          <el-radio-group v-model="form.severity">
            <el-radio-button value="高">
              <span class="text-red-500 font-bold">高危</span>
            </el-radio-button>
            <el-radio-button value="中">
              <span class="text-orange-500 font-bold">中等</span>
            </el-radio-button>
            <el-radio-button value="低">
              <span class="text-blue-500">低风险</span>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述异常情况、发现位置、可能原因等..."
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="现场照片">
          <div class="flex gap-2 flex-wrap">
            <div v-for="(photo, i) in form.photos" :key="i" class="relative">
              <img :src="photo" class="w-20 h-20 object-cover rounded border" />
              <el-button
                circle size="small" type="danger"
                class="absolute -top-2 -right-2"
                @click="removePhoto(i)"
              >×</el-button>
            </div>
            <label class="cursor-pointer">
              <div class="w-20 h-20 border-2 border-dashed border-gray-300 rounded flex flex-col items-center justify-center text-gray-400 hover:border-blue-400 hover:text-blue-400">
                <span class="text-2xl">+</span>
                <span class="text-xs mt-1">添加照片</span>
              </div>
              <input type="file" accept="image/*" capture="environment" multiple class="hidden"
                @change="handlePhotoSelect" />
            </label>
          </div>
        </el-form-item>

        <div class="flex gap-3 mt-4">
          <el-button @click="router.back()" style="flex:1">取消</el-button>
          <el-button type="warning" :loading="submitting" @click="submitReport" style="flex:2">
            提交异常报告
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- Existing reports -->
    <el-card v-if="reports.length > 0">
      <h3 class="font-semibold mb-4">已有异常报告 ({{ reports.length }})</h3>
      <div class="space-y-3">
        <div
          v-for="report in reports"
          :key="report.id"
          class="border rounded p-3"
          :class="report.severity === '高' ? 'border-red-200 bg-red-50' :
                  report.severity === '中' ? 'border-orange-200 bg-orange-50' : 'border-blue-100'"
        >
          <div class="flex items-start justify-between">
            <div>
              <span class="font-medium">{{ report.title }}</span>
              <el-tag
                size="small"
                :type="report.severity === '高' ? 'danger' : report.severity === '中' ? 'warning' : 'info'"
                class="ml-2"
              >{{ report.severity }}风险</el-tag>
            </div>
            <el-tag size="small" :type="report.status === 'closed' ? 'success' : ''">
              {{ report.status === 'open' ? '待处理' : report.status === 'in_rectification' ? '整改中' : '已关闭' }}
            </el-tag>
          </div>
          <p class="text-sm text-gray-600 mt-1">{{ report.description }}</p>
          <p class="text-xs text-gray-400 mt-1">{{ report.created_by }} · {{ report.created_at }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWorkcardDetail, getWorkcardSteps, createReport, getReports } from '@/api/workcard'

const route = useRoute()
const router = useRouter()
const workcardId = route.params.id

const workcard = ref({})
const steps = ref([])
const reports = ref([])
const submitting = ref(false)
const formRef = ref(null)

const form = ref({
  step_id: '',
  title: '',
  severity: '中',
  description: '',
  photos: []
})

const rules = {
  title: [{ required: true, message: '请填写异常标题', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
  description: [{ required: true, message: '请填写详细描述', trigger: 'blur', min: 10, message: '描述不少于10字' }]
}

function removePhoto(index) {
  form.value.photos.splice(index, 1)
}

function handlePhotoSelect(event) {
  const files = Array.from(event.target.files)
  files.forEach(file => {
    const url = URL.createObjectURL(file)
    form.value.photos.push(url)
  })
}

async function submitReport() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createReport(workcardId, {
      step_id: form.value.step_id || null,
      title: form.value.title,
      severity: form.value.severity,
      description: form.value.description
    })
    ElMessage.success('异常报告已提交')
    form.value = { step_id: '', title: '', severity: '中', description: '', photos: [] }
    await loadReports()
  } catch (e) {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

async function loadReports() {
  const res = await getReports(workcardId)
  reports.value = res.data?.list || []
}

onMounted(async () => {
  const [wcRes, stepsRes] = await Promise.all([
    getWorkcardDetail(workcardId),
    getWorkcardSteps(workcardId)
  ])
  workcard.value = wcRes.data || {}
  steps.value = stepsRes.data?.list || []
  await loadReports()
})
</script>
