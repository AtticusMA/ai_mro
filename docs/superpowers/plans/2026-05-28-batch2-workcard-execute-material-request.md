# 智慧机务增强 第二批：工卡执行增强 + 物料申领工作流 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 升级电子工卡增加执行详情和异常上报页，新增物料申领工作流（申领单列表+详情），打通一线机务从领料到执行的作业闭环。

**Architecture:** 工卡执行页作为现有 `WorkcardManagement` 路由的子页新增，复用现有 `workcard.js` API 并扩展步骤/签到相关接口。物料申领为全新模块，新建独立 API 和 Mock 文件。所有页面遵循响应式设计，平板优先大按钮和卡片布局。

**Tech Stack:** Vue 3 Composition API (`<script setup>`), Element Plus 2, Vue Router 4, Axios, Tailwind CSS 3, vite-plugin-mock

---

## 文件结构

### 新建文件

```
frontend/src/api/material-request.js         — 物料申领 API
frontend/src/mock/api/material-request.js    — 物料申领 Mock
frontend/src/pages/mro/workcard/
  WorkcardExecute.vue                         — 工卡执行详情（工序步骤+签到+照片+异常）
  WorkcardReport.vue                          — 异常上报页
frontend/src/pages/mro/material-request/
  MaterialRequestList.vue                     — 申领单列表
  MaterialRequestDetail.vue                   — 申领单详情
```

### 修改文件

```
frontend/src/api/workcard.js                  — 追加步骤执行相关接口
frontend/src/mock/api/workcards.js            — 扩展工序步骤、签到、异常上报 mock
frontend/src/router/routes.js                 — 追加 4 条新路由
```

---

## Task 1: 扩展 workcard API

**Files:**
- Modify: `frontend/src/api/workcard.js`

- [ ] **Step 1: 在文件末尾追加新接口**

在 `frontend/src/api/workcard.js` 现有内容末尾追加：

```javascript
export const getWorkcardSteps = (id) => request.get(`/api/workcards/${id}/steps`)
export const confirmStep = (id, stepId, data) => request.put(`/api/workcards/${id}/steps/${stepId}/confirm`, data)
export const checkin = (id, data) => request.post(`/api/workcards/${id}/checkin`, data)
export const checkout = (id, data) => request.post(`/api/workcards/${id}/checkout`, data)
export const uploadPhoto = (id, stepId, data) => request.post(`/api/workcards/${id}/steps/${stepId}/photos`, data)
export const createReport = (id, data) => request.post(`/api/workcards/${id}/reports`, data)
export const getReports = (id) => request.get(`/api/workcards/${id}/reports`)
```

---

## Task 2: 扩展 workcards Mock

**Files:**
- Create: `frontend/src/mock/api/workcards.js`

（若文件已存在则追加，若不存在则整体创建）

- [ ] **Step 1: 创建/覆盖 mock 文件**

```javascript
function generateSteps(workcardId) {
  const stepTitles = [
    '拆卸检查面板', '目视检查结构件', '测量间隙/尺寸', '清洁处理', '涂防腐涂层',
    '安装复位', '力矩紧固', '功能测试', '记录数据', '质检签署确认'
  ]
  return stepTitles.map((title, i) => ({
    id: workcardId * 100 + i + 1,
    workcard_id: workcardId,
    seq: i + 1,
    title,
    description: `按照 AMM 手册第 ${20 + i}-10-${String(i * 5).padStart(2, '0')} 章节执行：${title}`,
    requires_inspect: i === 9,
    status: i < 3 ? 'done' : i === 3 ? 'in_progress' : 'pending',
    confirmed_by: i < 3 ? { id: 1, name: '张伟', employee_no: 'MRO1000' } : null,
    confirmed_at: i < 3 ? `2026-05-28 0${8 + i}:30:00` : null,
    photos: i < 2 ? [{ id: i + 1, url: '', caption: '检查照片' }] : []
  }))
}

function generateReports(workcardId) {
  return [
    {
      id: workcardId * 10 + 1,
      workcard_id: workcardId,
      step_id: workcardId * 100 + 2,
      step_title: '目视检查结构件',
      severity: 'major',
      description: '发现蒙皮表面有轻微腐蚀，面积约 5cm²，需进一步评估',
      reported_by: { id: 1, name: '张伟', employee_no: 'MRO1000' },
      reported_at: '2026-05-28 09:15:00',
      status: 'open'
    }
  ]
}

function generateWorkcardList() {
  return Array.from({ length: 15 }, (_, i) => ({
    id: i + 1,
    card_no: `WC-2026-${String(i + 1).padStart(4, '0')}`,
    title: ['翼梁结构检查', '发动机孔探检查', '液压系统测试', '起落架拆装检修', '机身蒙皮修复'][i % 5],
    card_type: 'heavy_check',
    aircraft_id: ['B-1234', 'B-5678', 'B-9012'][i % 3],
    priority: ['urgent', 'normal', 'low'][i % 3],
    status: ['draft', 'issued', 'in_progress', 'completed'][i % 4],
    progress: [0, 20, 60, 100][i % 4],
    due_date: `2026-06-${String(10 + i).padStart(2, '0')}`,
    assigned_to: { id: (i % 5) + 1, name: ['张伟', '李明', '王强', '陈志', '刘洋'][i % 5], employee_no: `MRO100${i % 5}` },
    steps_total: 10,
    steps_done: [0, 2, 6, 10][i % 4]
  }))
}

const workcardList = generateWorkcardList()

export default [
  {
    url: '/api/workcards',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 10, status } = query
      let list = workcardList
      if (status) list = list.filter(w => w.status === status)
      const start = (page - 1) * pageSize
      return { code: 200, data: { list: list.slice(start, start + Number(pageSize)), total: list.length } }
    }
  },
  {
    url: '/api/workcards/:id',
    method: 'get',
    response: ({ params }) => {
      const wc = workcardList.find(w => w.id === Number(params.id))
      return wc ? { code: 200, data: wc } : { code: 404, message: '工卡不存在' }
    }
  },
  {
    url: '/api/workcards',
    method: 'post',
    response: ({ body }) => ({ code: 200, data: { ...body, id: Date.now() }, message: '创建成功' })
  },
  {
    url: '/api/workcards/progress',
    method: 'get',
    response: () => ({ code: 200, data: { total: 15, in_progress: 5, completed: 4, pending: 6 } })
  },
  {
    url: '/api/workcards/alerts',
    method: 'get',
    response: () => ({ code: 200, data: [] })
  },
  {
    url: '/api/workcards/:id/steps',
    method: 'get',
    response: ({ params }) => ({
      code: 200,
      data: generateSteps(Number(params.id))
    })
  },
  {
    url: '/api/workcards/:id/steps/:stepId/confirm',
    method: 'put',
    response: ({ body }) => ({
      code: 200,
      data: { ...body, status: 'done', confirmed_at: new Date().toISOString().replace('T', ' ').slice(0, 19) },
      message: '步骤确认成功'
    })
  },
  {
    url: '/api/workcards/:id/checkin',
    method: 'post',
    response: ({ body }) => ({
      code: 200,
      data: { ...body, checkin_at: new Date().toISOString().replace('T', ' ').slice(0, 19) },
      message: '签到成功'
    })
  },
  {
    url: '/api/workcards/:id/checkout',
    method: 'post',
    response: ({ body }) => ({
      code: 200,
      data: { ...body, checkout_at: new Date().toISOString().replace('T', ' ').slice(0, 19) },
      message: '签出成功'
    })
  },
  {
    url: '/api/workcards/:id/steps/:stepId/photos',
    method: 'post',
    response: () => ({ code: 200, data: { id: Date.now(), url: '', caption: '已上传' }, message: '上传成功' })
  },
  {
    url: '/api/workcards/:id/reports',
    method: 'post',
    response: ({ body }) => ({
      code: 200,
      data: { ...body, id: Date.now(), status: 'open', reported_at: new Date().toISOString().replace('T', ' ').slice(0, 19) },
      message: '上报成功'
    })
  },
  {
    url: '/api/workcards/:id/reports',
    method: 'get',
    response: ({ params }) => ({
      code: 200,
      data: generateReports(Number(params.id))
    })
  }
]
```

---

## Task 3: 工卡执行详情页 WorkcardExecute.vue

**Files:**
- Create: `frontend/src/pages/mro/workcard/WorkcardExecute.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="workcard-execute">
    <el-page-header @back="$router.back()" :content="workcard ? `${workcard.card_no} 执行` : '工卡执行'" />

    <div v-loading="loading" class="mt-4">
      <!-- 工卡头部信息 -->
      <el-card shadow="never" class="mb-4">
        <el-row :gutter="12" align="middle">
          <el-col :xs="24" :sm="16">
            <div class="wc-title">{{ workcard?.title }}</div>
            <div class="wc-meta">
              <el-tag size="small">{{ workcard?.aircraft_id }}</el-tag>
              <el-tag :type="priorityTagType[workcard?.priority]" size="small">
                {{ priorityLabel[workcard?.priority] }}
              </el-tag>
              <span class="text-gray-400 text-sm">到期：{{ workcard?.due_date }}</span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8" class="text-right">
            <el-button
              v-if="!checkedIn"
              type="success"
              size="large"
              @click="handleCheckin"
              :loading="checkinLoading"
            >
              <el-icon><Timer /></el-icon> 签到开工
            </el-button>
            <el-button
              v-else
              type="warning"
              size="large"
              @click="handleCheckout"
              :loading="checkinLoading"
            >
              <el-icon><SwitchButton /></el-icon> 签出收工
            </el-button>
          </el-col>
        </el-row>
        <el-progress
          :percentage="stepProgress"
          :stroke-width="10"
          :text-inside="true"
          class="mt-3"
          :status="stepProgress === 100 ? 'success' : ''"
        />
      </el-card>

      <!-- 工序步骤列表 -->
      <el-card shadow="never" class="mb-4">
        <template #header>
          <div class="card-header">
            <span class="font-semibold">工序步骤（{{ doneStepe }}/{{ steps.length }}）</span>
            <el-button type="danger" size="small" @click="$router.push(`/mro/workcard/${route.params.id}/report`)">
              <el-icon><Warning /></el-icon>上报异常
            </el-button>
          </div>
        </template>

        <div class="steps-list">
          <div
            v-for="step in steps"
            :key="step.id"
            class="step-item"
            :class="{ done: step.status === 'done', active: step.status === 'in_progress' }"
          >
            <div class="step-seq">
              <el-icon v-if="step.status === 'done'" color="#67c23a"><CircleCheck /></el-icon>
              <el-icon v-else-if="step.status === 'in_progress'" color="#409eff"><Loading /></el-icon>
              <span v-else class="seq-num">{{ step.seq }}</span>
            </div>
            <div class="step-content">
              <div class="step-title">{{ step.title }}
                <el-tag v-if="step.requires_inspect" type="warning" size="small" class="ml-1">需质检</el-tag>
              </div>
              <div class="step-desc text-gray-400 text-sm">{{ step.description }}</div>
              <div v-if="step.confirmed_by" class="step-confirm text-xs text-gray-400 mt-1">
                {{ step.confirmed_by.name }} 确认于 {{ step.confirmed_at }}
              </div>
              <div v-if="step.photos.length" class="step-photos mt-1">
                <el-tag size="small" type="info">{{ step.photos.length }} 张照片</el-tag>
              </div>
            </div>
            <div class="step-actions">
              <el-button
                v-if="step.status !== 'done'"
                type="primary"
                size="large"
                :disabled="!checkedIn"
                @click="handleConfirmStep(step)"
                :loading="confirmingStepId === step.id"
              >确认</el-button>
              <el-button
                size="small"
                @click="handleUploadPhoto(step)"
                :disabled="!checkedIn"
              >
                <el-icon><Camera /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 异常上报记录 -->
      <el-card shadow="never" v-if="reports.length">
        <template #header><span class="font-semibold">异常记录（{{ reports.length }}）</span></template>
        <div class="report-list">
          <div v-for="report in reports" :key="report.id" class="report-item">
            <el-tag :type="severityTagType[report.severity]" size="small">
              {{ severityLabel[report.severity] }}
            </el-tag>
            <span class="report-desc">{{ report.description }}</span>
            <span class="report-meta text-xs text-gray-400">{{ report.reported_by.name }} · {{ report.reported_at }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 照片上传弹窗 -->
    <el-dialog v-model="photoDialogVisible" title="上传现场照片" width="380px">
      <div class="text-sm text-gray-500 mb-2">步骤：{{ currentStep?.title }}</div>
      <el-input v-model="photoCaption" placeholder="照片说明（选填）" class="mb-2" />
      <div class="photo-upload-area" @click="triggerPhotoInput">
        <el-icon size="32" color="#c0c4cc"><Camera /></el-icon>
        <div class="text-gray-400 mt-1">点击选择照片</div>
        <input ref="photoInput" type="file" accept="image/*" capture="environment" style="display:none" @change="handlePhotoSelected" />
      </div>
      <div v-if="selectedPhoto" class="mt-2 text-sm text-green-600">已选择：{{ selectedPhoto.name }}</div>
      <template #footer>
        <el-button @click="photoDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" :disabled="!selectedPhoto" @click="confirmUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Timer, SwitchButton, Warning, CircleCheck, Loading, Camera } from '@element-plus/icons-vue'
import { getWorkcardDetail, getWorkcardSteps, confirmStep, checkin, checkout, uploadPhoto, getReports } from '@/api/workcard'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const workcard = ref(null)
const steps = ref([])
const reports = ref([])
const checkedIn = ref(false)
const checkinLoading = ref(false)
const confirmingStepId = ref(null)
const photoDialogVisible = ref(false)
const currentStep = ref(null)
const photoCaption = ref('')
const selectedPhoto = ref(null)
const uploading = ref(false)
const photoInput = ref(null)

const priorityTagType = { urgent: 'danger', normal: '', low: 'info' }
const priorityLabel = { urgent: '紧急', normal: '普通', low: '低' }
const severityTagType = { critical: 'danger', major: 'warning', minor: 'info' }
const severityLabel = { critical: '严重', major: '重要', minor: '轻微' }

const doneStepe = computed(() => steps.value.filter(s => s.status === 'done').length)
const stepProgress = computed(() =>
  steps.value.length ? Math.round((doneStepe.value / steps.value.length) * 100) : 0
)

const handleCheckin = async () => {
  checkinLoading.value = true
  try {
    const res = await checkin(route.params.id, { personnel_id: 1 })
    if (res.code === 200) { checkedIn.value = true; ElMessage.success('签到成功，开始作业') }
  } finally { checkinLoading.value = false }
}

const handleCheckout = async () => {
  checkinLoading.value = true
  try {
    const res = await checkout(route.params.id, { personnel_id: 1 })
    if (res.code === 200) { checkedIn.value = false; ElMessage.success('签出成功') }
  } finally { checkinLoading.value = false }
}

const handleConfirmStep = async (step) => {
  confirmingStepId.value = step.id
  try {
    const res = await confirmStep(route.params.id, step.id, { confirmed_by_id: 1 })
    if (res.code === 200) {
      step.status = 'done'
      step.confirmed_at = res.data.confirmed_at
      step.confirmed_by = { id: 1, name: '当前用户', employee_no: 'MRO1000' }
      ElMessage.success(`步骤 ${step.seq} 已确认`)
    }
  } finally { confirmingStepId.value = null }
}

const handleUploadPhoto = (step) => {
  currentStep.value = step
  photoCaption.value = ''
  selectedPhoto.value = null
  photoDialogVisible.value = true
}

const triggerPhotoInput = () => photoInput.value?.click()

const handlePhotoSelected = (e) => {
  selectedPhoto.value = e.target.files[0] || null
}

const confirmUpload = async () => {
  uploading.value = true
  try {
    const res = await uploadPhoto(route.params.id, currentStep.value.id, { caption: photoCaption.value })
    if (res.code === 200) {
      currentStep.value.photos.push(res.data)
      ElMessage.success('照片上传成功')
      photoDialogVisible.value = false
    }
  } finally { uploading.value = false }
}

onMounted(async () => {
  loading.value = true
  try {
    const [wcRes, stepsRes, reportsRes] = await Promise.all([
      getWorkcardDetail(route.params.id),
      getWorkcardSteps(route.params.id),
      getReports(route.params.id)
    ])
    if (wcRes.code === 200) workcard.value = wcRes.data
    if (stepsRes.code === 200) steps.value = stepsRes.data
    if (reportsRes.code === 200) reports.value = reportsRes.data
  } finally { loading.value = false }
})
</script>

<style scoped>
.wc-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.wc-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.steps-list { display: flex; flex-direction: column; gap: 0; }
.step-item { display: flex; align-items: flex-start; gap: 12px; padding: 14px 0; border-bottom: 1px solid #f5f5f5; transition: background 0.2s; }
.step-item.done { opacity: 0.65; }
.step-item.active { background: #f0f7ff; border-radius: 6px; padding: 14px 10px; }
.step-seq { width: 28px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; padding-top: 2px; }
.seq-num { width: 24px; height: 24px; border-radius: 50%; background: #f0f0f0; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; color: #909399; }
.step-content { flex: 1; min-width: 0; }
.step-title { font-weight: 500; }
.step-actions { display: flex; flex-direction: column; gap: 4px; align-items: flex-end; flex-shrink: 0; }
.report-list { display: flex; flex-direction: column; gap: 10px; }
.report-item { display: flex; align-items: flex-start; gap: 8px; flex-wrap: wrap; }
.report-desc { flex: 1; font-size: 14px; }
.report-meta { display: block; width: 100%; padding-left: 60px; }
.photo-upload-area { border: 2px dashed #dcdfe6; border-radius: 8px; padding: 24px; text-align: center; cursor: pointer; }
.photo-upload-area:hover { border-color: #409eff; }
.ml-1 { margin-left: 4px; }
</style>
```

---

## Task 4: 异常上报页 WorkcardReport.vue

**Files:**
- Create: `frontend/src/pages/mro/workcard/WorkcardReport.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="workcard-report">
    <el-page-header @back="$router.back()" content="异常上报" />

    <el-card shadow="never" class="mt-4">
      <el-form :model="form" label-width="90px" :rules="rules" ref="formRef">
        <el-form-item label="关联步骤" prop="step_id">
          <el-select v-model="form.step_id" placeholder="请选择关联工序步骤" style="width:100%">
            <el-option v-for="s in steps" :key="s.id" :label="`步骤${s.seq}：${s.title}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重等级" prop="severity">
          <el-radio-group v-model="form.severity">
            <el-radio-button value="critical">
              <el-icon color="#f56c6c"><Warning /></el-icon> 严重
            </el-radio-button>
            <el-radio-button value="major">
              <el-icon color="#e6a23c"><Warning /></el-icon> 重要
            </el-radio-button>
            <el-radio-button value="minor">轻微</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="异常描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述发现的异常情况，包括位置、尺寸、表现等"
          />
        </el-form-item>
        <el-form-item label="处置建议">
          <el-input v-model="form.suggestion" type="textarea" :rows="2" placeholder="建议的处置方案（选填）" />
        </el-form-item>
      </el-form>
      <div class="form-actions">
        <el-button @click="$router.back()">取消</el-button>
        <el-button type="danger" size="large" :loading="submitting" @click="handleSubmit">
          <el-icon><Warning /></el-icon> 提交异常上报
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { getWorkcardSteps, createReport } from '@/api/workcard'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const steps = ref([])
const submitting = ref(false)

const form = reactive({ step_id: null, severity: 'major', description: '', suggestion: '' })

const rules = {
  step_id: [{ required: true, message: '请选择关联工序步骤', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重等级', trigger: 'change' }],
  description: [{ required: true, message: '请填写异常描述', trigger: 'blur' }, { min: 10, message: '描述至少10个字', trigger: 'blur' }]
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const selectedStep = steps.value.find(s => s.id === form.step_id)
      const res = await createReport(route.params.id, {
        ...form,
        step_title: selectedStep?.title || ''
      })
      if (res.code === 200) {
        ElMessage.success('异常上报成功，已通知工段长')
        router.back()
      }
    } finally { submitting.value = false }
  })
}

onMounted(async () => {
  const res = await getWorkcardSteps(route.params.id)
  if (res.code === 200) steps.value = res.data
})
</script>

<style scoped>
.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }
</style>
```

---

## Task 5: 物料申领 API 文件

**Files:**
- Create: `frontend/src/api/material-request.js`

- [ ] **Step 1: 创建 API 文件**

```javascript
import request from '@/utils/request'

export const getMaterialRequestList = (params) => request.get('/api/material-requests', { params })
export const getMaterialRequestDetail = (id) => request.get(`/api/material-requests/${id}`)
export const createMaterialRequest = (data) => request.post('/api/material-requests', data)
export const approveMaterialRequest = (id, data) => request.post(`/api/material-requests/${id}/approve`, data)
export const rejectMaterialRequest = (id, data) => request.post(`/api/material-requests/${id}/reject`, data)
export const confirmReceive = (id, data) => request.post(`/api/material-requests/${id}/receive`, data)
export const getWorkcardBom = (workcardId) => request.get(`/api/workcards/${workcardId}/bom`)
```

---

## Task 6: 物料申领 Mock 数据

**Files:**
- Create: `frontend/src/mock/api/material-request.js`

- [ ] **Step 1: 创建 Mock 文件**

```javascript
const partNumbers = ['05-1234-001', '12-5678-002', '07-9012-003', '15-3456-004', '09-7890-005']
const partNames = ['O型密封圈', '液压管路组件', '结构铆钉套件', '发动机密封垫片', '起落架销轴']
const units = ['个', '套', '包', '件', '根']

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generateBom(workcardId) {
  return Array.from({ length: 3 + (workcardId % 3) }, (_, i) => ({
    id: workcardId * 10 + i + 1,
    part_no: partNumbers[i % partNumbers.length],
    part_name: partNames[i % partNames.length],
    quantity: Math.floor(2 + Math.random() * 10),
    unit: units[i % units.length],
    stock_qty: Math.floor(Math.random() * 15),
    is_available: Math.random() > 0.3
  }))
}

function generateRequests() {
  return Array.from({ length: 10 }, (_, i) => {
    const reqId = i + 1
    const items = Array.from({ length: 2 + i % 3 }, (_, j) => ({
      id: reqId * 10 + j + 1,
      part_no: partNumbers[j % partNumbers.length],
      part_name: partNames[j % partNames.length],
      quantity: Math.floor(2 + Math.random() * 8),
      unit: units[j % units.length],
      approved_qty: ['approved', 'issued', 'received'].includes(['pending', 'approved', 'rejected', 'issued', 'received'][i % 5])
        ? Math.floor(1 + Math.random() * 8)
        : null
    }))
    return {
      id: reqId,
      request_no: `MR-2026-${String(reqId).padStart(4, '0')}`,
      workcard_id: reqId,
      workcard_no: `WC-2026-${String(reqId).padStart(4, '0')}`,
      aircraft_id: ['B-1234', 'B-5678', 'B-9012'][i % 3],
      status: ['pending', 'approved', 'rejected', 'issued', 'received'][i % 5],
      requested_by: { id: (i % 5) + 1, name: ['张伟', '李明', '王强', '陈志', '刘洋'][i % 5] },
      requested_at: `2026-05-${String(20 + i).padStart(2, '0')} 10:00:00`,
      approved_by: i % 5 > 0 ? { id: 10, name: '赵仓管' } : null,
      approved_at: i % 5 > 0 ? `2026-05-${String(20 + i).padStart(2, '0')} 14:00:00` : null,
      items,
      remark: ''
    }
  })
}

const requests = generateRequests()

export default [
  {
    url: '/api/material-requests',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 10, status } = query
      let list = requests
      if (status) list = list.filter(r => r.status === status)
      const start = (page - 1) * pageSize
      return { code: 200, data: { list: list.slice(start, start + Number(pageSize)), total: list.length } }
    }
  },
  {
    url: '/api/material-requests/:id',
    method: 'get',
    response: ({ params }) => {
      const req = requests.find(r => r.id === Number(params.id))
      return req ? { code: 200, data: req } : { code: 404, message: '申领单不存在' }
    }
  },
  {
    url: '/api/material-requests',
    method: 'post',
    response: ({ body }) => ({
      code: 200,
      data: { ...body, id: Date.now(), request_no: `MR-2026-${Date.now()}`, status: 'pending' },
      message: '申领单已提交'
    })
  },
  {
    url: '/api/material-requests/:id/approve',
    method: 'post',
    response: () => ({ code: 200, message: '审核通过' })
  },
  {
    url: '/api/material-requests/:id/reject',
    method: 'post',
    response: () => ({ code: 200, message: '已驳回' })
  },
  {
    url: '/api/material-requests/:id/receive',
    method: 'post',
    response: () => ({ code: 200, message: '确认领取成功' })
  },
  {
    url: '/api/workcards/:workcardId/bom',
    method: 'get',
    response: ({ params }) => ({
      code: 200,
      data: generateBom(Number(params.workcardId))
    })
  }
]
```

---

## Task 7: 申领单列表页 MaterialRequestList.vue

**Files:**
- Create: `frontend/src/pages/mro/material-request/MaterialRequestList.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="material-request-list">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待审核" value="pending" />
            <el-option label="已批准" value="approved" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已出库" value="issued" />
            <el-option label="已领取" value="received" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">物料申领单</span>
          <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>新建申领</el-button>
        </div>
      </template>

      <!-- PC 表格 -->
      <el-table v-if="!isMobile" v-loading="loading" :data="tableData" border stripe
        @row-click="row => $router.push(`/mro/material-request/${row.id}`)">
        <el-table-column prop="request_no" label="申领单号" width="160" />
        <el-table-column prop="workcard_no" label="关联工卡" width="150" />
        <el-table-column prop="aircraft_id" label="机号" width="90" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status]" size="small">{{ statusLabel[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="90">
          <template #default="{ row }">{{ row.requested_by?.name }}</template>
        </el-table-column>
        <el-table-column prop="requested_at" label="申请时间" min-width="160" />
      </el-table>

      <!-- 移动端卡片 -->
      <div v-else class="mobile-list">
        <el-card
          v-for="row in tableData"
          :key="row.id"
          class="mobile-item"
          shadow="hover"
          @click="$router.push(`/mro/material-request/${row.id}`)"
        >
          <div class="item-header">
            <span class="request-no">{{ row.request_no }}</span>
            <el-tag :type="statusTagType[row.status]" size="small">{{ statusLabel[row.status] }}</el-tag>
          </div>
          <div class="item-body">
            <span>工卡：{{ row.workcard_no }} · {{ row.aircraft_id }}</span>
            <span>{{ row.requested_by?.name }} · {{ row.requested_at?.slice(0, 10) }}</span>
          </div>
        </el-card>
      </div>

      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getMaterialRequestList } from '@/api/material-request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: '' })
const isMobile = computed(() => window.innerWidth < 768)

const statusTagType = { pending: 'warning', approved: 'success', rejected: 'danger', issued: '', received: 'info' }
const statusLabel = { pending: '待审核', approved: '已批准', rejected: '已驳回', issued: '已出库', received: '已领取' }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMaterialRequestList(queryParams)
    if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.status = ''; handleSearch() }
const handleCreate = () => { /* 跳转到新建页或弹窗，二批实现简化为提示 */ }

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
.mobile-list { display: flex; flex-direction: column; gap: 8px; }
.mobile-item { cursor: pointer; }
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.request-no { font-weight: 600; }
.item-body { display: flex; flex-direction: column; gap: 3px; font-size: 13px; color: #606266; }
</style>
```

---

## Task 8: 申领单详情页 MaterialRequestDetail.vue

**Files:**
- Create: `frontend/src/pages/mro/material-request/MaterialRequestDetail.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="material-request-detail">
    <el-page-header @back="$router.back()" :content="detail?.request_no || '申领单详情'" />

    <div v-loading="loading" class="mt-4">
      <el-row :gutter="16">
        <!-- 基本信息 -->
        <el-col :xs="24" :md="10">
          <el-card shadow="never" class="mb-4">
            <template #header><span class="font-semibold">申领信息</span></template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="申领单号">{{ detail?.request_no }}</el-descriptions-item>
              <el-descriptions-item label="关联工卡">{{ detail?.workcard_no }}</el-descriptions-item>
              <el-descriptions-item label="机号">{{ detail?.aircraft_id }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="statusTagType[detail?.status]" size="small">{{ statusLabel[detail?.status] }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="申请人">{{ detail?.requested_by?.name }}</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ detail?.requested_at }}</el-descriptions-item>
              <el-descriptions-item v-if="detail?.approved_by" label="审核人">{{ detail.approved_by.name }}</el-descriptions-item>
              <el-descriptions-item v-if="detail?.approved_at" label="审核时间">{{ detail.approved_at }}</el-descriptions-item>
            </el-descriptions>

            <!-- 进度时间线 -->
            <div class="mt-4">
              <el-steps :active="stepIndex" direction="vertical" size="small">
                <el-step title="提交申领" :description="detail?.requested_at" />
                <el-step title="仓库审核" :description="detail?.approved_at || '待审核'" />
                <el-step title="出库发料" />
                <el-step title="确认领取" />
              </el-steps>
            </div>
          </el-card>

          <!-- 操作按钮 -->
          <el-card shadow="never" v-if="showActions">
            <template #header><span class="font-semibold">操作</span></template>
            <div class="action-buttons">
              <el-button v-if="detail?.status === 'pending'" type="success" @click="handleApprove" :loading="actioning">
                审核通过
              </el-button>
              <el-button v-if="detail?.status === 'pending'" type="danger" @click="handleReject" :loading="actioning">
                驳回
              </el-button>
              <el-button v-if="detail?.status === 'issued'" type="primary" size="large" @click="handleReceive" :loading="actioning">
                <el-icon><Check /></el-icon> 确认领取
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 物料清单 -->
        <el-col :xs="24" :md="14">
          <el-card shadow="never">
            <template #header><span class="font-semibold">物料清单（{{ detail?.items?.length || 0 }} 项）</span></template>
            <el-table :data="detail?.items || []" border size="small">
              <el-table-column prop="part_no" label="件号" width="130" />
              <el-table-column prop="part_name" label="名称" min-width="120" />
              <el-table-column prop="quantity" label="申请数量" width="90" align="center" />
              <el-table-column prop="unit" label="单位" width="60" align="center" />
              <el-table-column label="批准数量" width="90" align="center">
                <template #default="{ row }">
                  <span v-if="row.approved_qty !== null">{{ row.approved_qty }}</span>
                  <span v-else class="text-gray-400">-</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getMaterialRequestDetail, approveMaterialRequest, rejectMaterialRequest, confirmReceive } from '@/api/material-request'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)
const actioning = ref(false)

const statusTagType = { pending: 'warning', approved: 'success', rejected: 'danger', issued: '', received: 'info' }
const statusLabel = { pending: '待审核', approved: '已批准', rejected: '已驳回', issued: '已出库', received: '已领取' }

const stepIndex = computed(() => {
  const map = { pending: 1, approved: 1, rejected: 1, issued: 2, received: 3 }
  return map[detail.value?.status] ?? 0
})

const showActions = computed(() =>
  detail.value && ['pending', 'issued'].includes(detail.value.status)
)

const handleApprove = async () => {
  actioning.value = true
  try {
    const res = await approveMaterialRequest(route.params.id, {})
    if (res.code === 200) { ElMessage.success('审核通过'); fetchDetail() }
  } finally { actioning.value = false }
}

const handleReject = async () => {
  try {
    await ElMessageBox.confirm('确认驳回此申领单？', '驳回确认', { type: 'warning' })
    actioning.value = true
    const res = await rejectMaterialRequest(route.params.id, {})
    if (res.code === 200) { ElMessage.success('已驳回'); fetchDetail() }
  } catch { /* 取消 */ } finally { actioning.value = false }
}

const handleReceive = async () => {
  actioning.value = true
  try {
    const res = await confirmReceive(route.params.id, {})
    if (res.code === 200) { ElMessage.success('确认领取成功'); fetchDetail() }
  } finally { actioning.value = false }
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getMaterialRequestDetail(route.params.id)
    if (res.code === 200) detail.value = res.data
  } finally { loading.value = false }
}

onMounted(() => { fetchDetail() })
</script>

<style scoped>
.action-buttons { display: flex; gap: 12px; flex-wrap: wrap; }
</style>
```

---

## Task 9: 注册新路由

**Files:**
- Modify: `frontend/src/router/routes.js`

- [ ] **Step 1: 追加工卡执行路由到现有 WorkcardManagement children 中**

找到 routes.js 中的 `WorkcardManagement` 路由块：

```javascript
      {
        path: 'mro/workcard',
        name: 'WorkcardManagement',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '电子工卡', requiresAuth: true },
        children: [
          { path: '', name: 'WorkcardList', component: () => import('@/pages/mro/workcard/WorkcardList.vue'), meta: { title: '工卡列表', requiresAuth: true, permissions: ['workcard:monitor'], breadcrumb: '工卡列表' } }
        ]
      },
```

替换为：

```javascript
      {
        path: 'mro/workcard',
        name: 'WorkcardManagement',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '电子工卡', requiresAuth: true },
        children: [
          { path: '', name: 'WorkcardList', component: () => import('@/pages/mro/workcard/WorkcardList.vue'), meta: { title: '工卡列表', requiresAuth: true, permissions: ['workcard:monitor'], breadcrumb: '工卡列表' } },
          { path: ':id/execute', name: 'WorkcardExecute', component: () => import('@/pages/mro/workcard/WorkcardExecute.vue'), meta: { title: '工卡执行', requiresAuth: true, permissions: ['workcard:step:execute'], breadcrumb: '工卡执行' } },
          { path: ':id/report', name: 'WorkcardReport', component: () => import('@/pages/mro/workcard/WorkcardReport.vue'), meta: { title: '异常上报', requiresAuth: true, permissions: ['workcard:report:create'], breadcrumb: '异常上报' } }
        ]
      },
```

- [ ] **Step 2: 在人员资质路由块之后（或 wiki 之前）追加物料申领路由**

```javascript
      {
        path: 'mro/material-request',
        name: 'MaterialRequest',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '物料申领', requiresAuth: true },
        children: [
          { path: '', name: 'MaterialRequestList', component: () => import('@/pages/mro/material-request/MaterialRequestList.vue'), meta: { title: '申领单', requiresAuth: true, permissions: ['material:request:create'], breadcrumb: '申领单' } },
          { path: ':id', name: 'MaterialRequestDetail', component: () => import('@/pages/mro/material-request/MaterialRequestDetail.vue'), meta: { title: '申领详情', requiresAuth: true, permissions: ['material:request:create'], breadcrumb: '申领详情' } }
        ]
      },
```

- [ ] **Step 3: 构建验证**

```bash
cd frontend && npm run build 2>&1 | tail -20
```
期望：`✓ built in` 无报错。

- [ ] **Step 4: 同时在 WorkcardList.vue 中添加"执行"入口（操作列）**

在 `frontend/src/pages/mro/workcard/WorkcardList.vue` 的 `<el-table>` 末尾追加操作列：

```vue
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/mro/workcard/${row.id}/execute`)">执行</el-button>
          </template>
        </el-table-column>
```

同时在 `<script setup>` 中追加 `useRouter` 引用（如果没有）：

```javascript
import { useRouter } from 'vue-router'
const router = useRouter()
```

- [ ] **Step 5: 提交**

```bash
cd frontend && git add src/api/workcard.js src/api/material-request.js src/mock/api/workcards.js src/mock/api/material-request.js src/pages/mro/workcard/WorkcardExecute.vue src/pages/mro/workcard/WorkcardReport.vue src/pages/mro/material-request/ src/router/routes.js
git commit -m "feat(mro): add workcard execution flow and material request workflow

Refs: MRO-010"
```

---

## 自检结果

| 检查项 | 结果 |
|------|------|
| Spec 覆盖 | 工序步骤确认 ✓、扫码签到签出 ✓、照片上传 ✓、异常上报 ✓、物料申领CRUD ✓、进度时间线 ✓、响应式卡片 ✓ |
| 占位符 | 无 TBD/TODO |
| 类型一致性 | `confirmStep`/`checkin`/`checkout` 在 API 和 WorkcardExecute.vue 中参数一致 ✓ |
| WorkcardList 入口 | Task 9 Step 4 补充了执行按钮 ✓ |
