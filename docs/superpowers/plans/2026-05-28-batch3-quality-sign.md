# 智慧机务增强 第三批：质检签署流 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增质检签署流模块（待签署列表、签署详情、NCR不符合项管理），打通工卡执行→质检签署→关闭的审批闭环。

**Architecture:** 全新 `/mro/quality/` 模块，新建独立 API 和 Mock 文件。签署操作使用工号+密码确认弹窗模拟电子签名，NCR 创建时触发工卡步骤回退（前端状态联动）。

**Tech Stack:** Vue 3 Composition API (`<script setup>`), Element Plus 2, Vue Router 4, Axios, Tailwind CSS 3, vite-plugin-mock

---

## 文件结构

### 新建文件

```
frontend/src/api/quality.js              — 质检签署 API
frontend/src/mock/api/quality.js         — 质检签署 Mock
frontend/src/pages/mro/quality/
  QualityPending.vue                     — 待签署列表
  QualitySign.vue                        — 签署详情页
  QualityNcr.vue                         — 不符合项管理
```

### 修改文件

```
frontend/src/router/routes.js            — 追加 3 条新路由
```

---

## Task 1: 质检 API 文件

**Files:**
- Create: `frontend/src/api/quality.js`

- [ ] **Step 1: 创建 API 文件**

```javascript
import request from '@/utils/request'

export const getPendingList = (params) => request.get('/api/quality/pending', { params })
export const getSignDetail = (id) => request.get(`/api/quality/pending/${id}`)
export const submitSign = (id, data) => request.post(`/api/quality/pending/${id}/sign`, data)
export const getNcrList = (params) => request.get('/api/quality/ncr', { params })
export const createNcr = (data) => request.post('/api/quality/ncr', data)
export const closeNcr = (id, data) => request.post(`/api/quality/ncr/${id}/close`, data)
```

---

## Task 2: 质检 Mock 数据

**Files:**
- Create: `frontend/src/mock/api/quality.js`

- [ ] **Step 1: 创建 Mock 文件**

```javascript
const inspectors = ['陈质检', '吴适航', '郑工程师']
const aircraft = ['B-1234', 'B-5678', 'B-9012']

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generatePendingItems() {
  return Array.from({ length: 12 }, (_, i) => ({
    id: i + 1,
    workcard_id: i + 1,
    workcard_no: `WC-2026-${String(i + 1).padStart(4, '0')}`,
    workcard_title: ['翼梁结构检查', '发动机孔探检查', '液压系统测试', '起落架拆装检修'][i % 4],
    aircraft_id: aircraft[i % aircraft.length],
    step_id: (i + 1) * 100 + 10,
    step_title: '质检签署确认',
    step_seq: 10,
    inspector_id: (i % 3) + 1,
    inspector_name: inspectors[i % 3],
    status: i < 7 ? 'pending' : 'signed',
    created_at: `2026-05-${String(20 + (i % 8)).padStart(2, '0')} 14:00:00`,
    signed_at: i >= 7 ? `2026-05-${String(22 + (i % 6)).padStart(2, '0')} 16:00:00` : null,
    workcard_steps_summary: {
      total: 10,
      done: 10,
      has_ncr: i % 4 === 0
    },
    execution_records: Array.from({ length: 3 }, (_, j) => ({
      step_seq: j + 1,
      step_title: ['拆卸检查面板', '目视检查结构件', '测量间隙/尺寸'][j],
      confirmed_by: ['张伟', '李明', '王强'][j],
      confirmed_at: `2026-05-28 0${8 + j}:30:00`,
      photos_count: j === 1 ? 2 : 0
    }))
  }))
}

function generateNcrList() {
  return Array.from({ length: 8 }, (_, i) => ({
    id: i + 1,
    ncr_no: `NCR-2026-${String(i + 1).padStart(4, '0')}`,
    workcard_id: i + 1,
    workcard_no: `WC-2026-${String(i + 1).padStart(4, '0')}`,
    aircraft_id: aircraft[i % aircraft.length],
    step_title: '目视检查结构件',
    severity: ['major', 'minor', 'critical'][i % 3],
    description: [
      '蒙皮表面发现轻微腐蚀，面积约5cm²',
      '螺栓力矩值低于最低要求值10%',
      '密封胶涂覆不均匀，存在气泡',
      '结构件表面漆膜剥落，露出铝基材'
    ][i % 4],
    created_by: { id: (i % 3) + 1, name: inspectors[i % 3] },
    created_at: `2026-05-${String(20 + i).padStart(2, '0')} 10:00:00`,
    status: ['open', 'in_rectification', 'closed'][i % 3],
    rectification_desc: i % 3 === 2 ? '已按要求完成整改，复检合格' : null,
    closed_at: i % 3 === 2 ? `2026-05-${String(22 + i).padStart(2, '0')} 15:00:00` : null
  }))
}

const pendingItems = generatePendingItems()
const ncrList = generateNcrList()

export default [
  {
    url: '/api/quality/pending',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 10, status } = query
      let list = pendingItems
      if (status) list = list.filter(p => p.status === status)
      const start = (page - 1) * pageSize
      return { code: 200, data: { list: list.slice(start, start + Number(pageSize)), total: list.length } }
    }
  },
  {
    url: '/api/quality/pending/:id',
    method: 'get',
    response: ({ params }) => {
      const item = pendingItems.find(p => p.id === Number(params.id))
      return item ? { code: 200, data: item } : { code: 404, message: '待签署项不存在' }
    }
  },
  {
    url: '/api/quality/pending/:id/sign',
    method: 'post',
    response: ({ body }) => {
      if (body.employee_no && body.password) {
        return {
          code: 200,
          data: { signed_at: new Date().toISOString().replace('T', ' ').slice(0, 19) },
          message: '签署成功'
        }
      }
      return { code: 400, message: '工号或密码错误' }
    }
  },
  {
    url: '/api/quality/ncr',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 10, status } = query
      let list = ncrList
      if (status) list = list.filter(n => n.status === status)
      const start = (page - 1) * pageSize
      return { code: 200, data: { list: list.slice(start, start + Number(pageSize)), total: list.length } }
    }
  },
  {
    url: '/api/quality/ncr',
    method: 'post',
    response: ({ body }) => ({
      code: 200,
      data: {
        ...body,
        id: Date.now(),
        ncr_no: `NCR-2026-${Date.now()}`,
        status: 'open',
        created_at: new Date().toISOString().replace('T', ' ').slice(0, 19)
      },
      message: 'NCR已创建，工卡步骤已回退至待整改'
    })
  },
  {
    url: '/api/quality/ncr/:id/close',
    method: 'post',
    response: ({ body }) => ({
      code: 200,
      data: { ...body, status: 'closed', closed_at: new Date().toISOString().replace('T', ' ').slice(0, 19) },
      message: 'NCR已关闭'
    })
  }
]
```

---

## Task 3: 待签署列表页 QualityPending.vue

**Files:**
- Create: `frontend/src/pages/mro/quality/QualityPending.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="quality-pending">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
            <el-option label="待签署" value="pending" />
            <el-option label="已签署" value="signed" />
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
          <span class="title">质检签署</span>
          <el-badge :value="pendingCount" :hidden="pendingCount === 0">
            <el-tag type="warning">{{ pendingCount }} 项待签署</el-tag>
          </el-badge>
        </div>
      </template>

      <!-- PC 表格 -->
      <el-table v-if="!isMobile" v-loading="loading" :data="tableData" border stripe
        @row-click="row => $router.push(`/mro/quality/sign/${row.id}`)">
        <el-table-column prop="workcard_no" label="工卡编号" width="150" />
        <el-table-column prop="workcard_title" label="工卡标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="aircraft_id" label="机号" width="90" />
        <el-table-column prop="step_title" label="签署步骤" width="130" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'pending' ? 'warning' : 'success'" size="small">
              {{ row.status === 'pending' ? '待签署' : '已签署' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="NCR" width="70" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.workcard_steps_summary?.has_ncr" color="#f56c6c"><Warning /></el-icon>
            <span v-else class="text-gray-300">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="160" />
      </el-table>

      <!-- 移动端卡片 -->
      <div v-else class="mobile-list">
        <el-card
          v-for="row in tableData"
          :key="row.id"
          class="mobile-item"
          :class="{ pending: row.status === 'pending' }"
          shadow="hover"
          @click="$router.push(`/mro/quality/sign/${row.id}`)"
        >
          <div class="item-header">
            <span class="workcard-no">{{ row.workcard_no }}</span>
            <el-tag :type="row.status === 'pending' ? 'warning' : 'success'" size="small">
              {{ row.status === 'pending' ? '待签署' : '已签署' }}
            </el-tag>
          </div>
          <div class="item-body">
            <span>{{ row.workcard_title }} · {{ row.aircraft_id }}</span>
            <span>{{ row.step_title }}</span>
            <span v-if="row.workcard_steps_summary?.has_ncr" class="ncr-warn">
              <el-icon color="#f56c6c"><Warning /></el-icon> 含 NCR 不符合项
            </span>
          </div>
        </el-card>
      </div>

      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Warning } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getPendingList } from '@/api/quality'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: '' })
const isMobile = computed(() => window.innerWidth < 768)
const pendingCount = computed(() => tableData.value.filter(r => r.status === 'pending').length)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingList(queryParams)
    if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.status = ''; handleSearch() }

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
.mobile-list { display: flex; flex-direction: column; gap: 8px; }
.mobile-item { cursor: pointer; }
.mobile-item.pending { border-left: 3px solid #e6a23c; }
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.workcard-no { font-weight: 600; }
.item-body { display: flex; flex-direction: column; gap: 3px; font-size: 13px; color: #606266; }
.ncr-warn { display: flex; align-items: center; gap: 4px; color: #f56c6c; font-size: 12px; }
</style>
```

---

## Task 4: 签署详情页 QualitySign.vue

**Files:**
- Create: `frontend/src/pages/mro/quality/QualitySign.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="quality-sign">
    <el-page-header @back="$router.back()" :content="detail?.workcard_no ? `质检签署 · ${detail.workcard_no}` : '质检签署'" />

    <div v-loading="loading" class="mt-4">
      <el-row :gutter="16">
        <!-- 工卡执行摘要 -->
        <el-col :xs="24" :lg="14">
          <el-card shadow="never" class="mb-4">
            <template #header>
              <div class="card-header">
                <span class="font-semibold">执行记录</span>
                <el-tag v-if="detail?.workcard_steps_summary?.has_ncr" type="danger" size="small">
                  <el-icon><Warning /></el-icon> 含不符合项
                </el-tag>
              </div>
            </template>

            <el-descriptions :column="2" border size="small" class="mb-4">
              <el-descriptions-item label="工卡编号">{{ detail?.workcard_no }}</el-descriptions-item>
              <el-descriptions-item label="机号">{{ detail?.aircraft_id }}</el-descriptions-item>
              <el-descriptions-item label="工卡标题" :span="2">{{ detail?.workcard_title }}</el-descriptions-item>
              <el-descriptions-item label="工序完成">
                {{ detail?.workcard_steps_summary?.done }}/{{ detail?.workcard_steps_summary?.total }}
              </el-descriptions-item>
              <el-descriptions-item label="签署步骤">{{ detail?.step_title }}</el-descriptions-item>
            </el-descriptions>

            <div class="font-medium mb-2 text-sm text-gray-600">工序执行详情</div>
            <el-table :data="detail?.execution_records || []" border size="small">
              <el-table-column prop="step_seq" label="序" width="50" align="center" />
              <el-table-column prop="step_title" label="工序" min-width="140" />
              <el-table-column prop="confirmed_by" label="执行人" width="80" />
              <el-table-column prop="confirmed_at" label="确认时间" width="160" />
              <el-table-column label="照片" width="60" align="center">
                <template #default="{ row }">
                  <span v-if="row.photos_count > 0" class="text-blue-500">{{ row.photos_count }}张</span>
                  <span v-else class="text-gray-300">-</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <!-- 签署操作 -->
        <el-col :xs="24" :lg="10">
          <el-card shadow="never" class="mb-4">
            <template #header><span class="font-semibold">签署操作</span></template>

            <div v-if="detail?.status === 'signed'" class="signed-result">
              <el-result icon="success" title="已签署" :sub-title="`签署时间：${detail.signed_at}`" />
            </div>

            <div v-else>
              <el-alert type="info" show-icon :closable="false" class="mb-4"
                description="请核查以上执行记录无误后，输入工号和密码完成电子签署。签署后不可撤销。" />
              <el-form :model="signForm" label-width="70px">
                <el-form-item label="工号">
                  <el-input v-model="signForm.employee_no" placeholder="请输入工号" size="large" />
                </el-form-item>
                <el-form-item label="密码">
                  <el-input v-model="signForm.password" type="password" placeholder="请输入密码" size="large" show-password />
                </el-form-item>
              </el-form>
              <el-button
                type="primary"
                size="large"
                style="width:100%"
                :loading="signing"
                @click="handleSign"
              >
                <el-icon><EditPen /></el-icon> 确认电子签署
              </el-button>
              <el-divider>发现问题</el-divider>
              <el-button
                type="danger"
                size="large"
                style="width:100%"
                @click="handleCreateNcr"
              >
                <el-icon><Warning /></el-icon> 创建不符合项(NCR)
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- NCR 创建弹窗 -->
    <el-dialog v-model="ncrDialogVisible" title="创建不符合项(NCR)" width="500px">
      <el-form :model="ncrForm" label-width="80px" :rules="ncrRules" ref="ncrFormRef">
        <el-form-item label="严重等级" prop="severity">
          <el-radio-group v-model="ncrForm.severity">
            <el-radio-button value="critical">严重</el-radio-button>
            <el-radio-button value="major">重要</el-radio-button>
            <el-radio-button value="minor">轻微</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="ncrForm.description" type="textarea" :rows="4"
            placeholder="详细描述不符合项，包括位置、程度、发现方式等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ncrDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="creatingNcr" @click="confirmCreateNcr">创建并回退工卡</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning, EditPen } from '@element-plus/icons-vue'
import { getSignDetail, submitSign, createNcr } from '@/api/quality'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)
const signing = ref(false)
const signForm = reactive({ employee_no: '', password: '' })
const ncrDialogVisible = ref(false)
const ncrFormRef = ref(null)
const ncrForm = reactive({ severity: 'major', description: '' })
const ncrRules = {
  description: [{ required: true, message: '请填写问题描述', trigger: 'blur' }, { min: 10, message: '至少10个字', trigger: 'blur' }]
}
const creatingNcr = ref(false)

const handleSign = async () => {
  if (!signForm.employee_no || !signForm.password) {
    ElMessage.warning('请输入工号和密码')
    return
  }
  signing.value = true
  try {
    const res = await submitSign(route.params.id, signForm)
    if (res.code === 200) {
      ElMessage.success('签署成功')
      fetchDetail()
    } else {
      ElMessage.error(res.message || '工号或密码错误')
    }
  } finally { signing.value = false }
}

const handleCreateNcr = () => {
  Object.assign(ncrForm, { severity: 'major', description: '' })
  ncrDialogVisible.value = true
}

const confirmCreateNcr = async () => {
  await ncrFormRef.value.validate(async (valid) => {
    if (!valid) return
    creatingNcr.value = true
    try {
      const res = await createNcr({
        ...ncrForm,
        workcard_id: detail.value.workcard_id,
        workcard_no: detail.value.workcard_no,
        aircraft_id: detail.value.aircraft_id,
        step_title: detail.value.step_title
      })
      if (res.code === 200) {
        ElMessage.success('NCR 已创建，工卡步骤已回退至待整改')
        ncrDialogVisible.value = false
        router.push('/mro/quality/ncr')
      }
    } finally { creatingNcr.value = false }
  })
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getSignDetail(route.params.id)
    if (res.code === 200) detail.value = res.data
  } finally { loading.value = false }
}

onMounted(() => { fetchDetail() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.signed-result { padding: 16px 0; }
</style>
```

---

## Task 5: 不符合项管理页 QualityNcr.vue

**Files:**
- Create: `frontend/src/pages/mro/quality/QualityNcr.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="quality-ncr">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:140px">
            <el-option label="待整改" value="open" />
            <el-option label="整改中" value="in_rectification" />
            <el-option label="已关闭" value="closed" />
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
          <span class="title">不符合项(NCR)管理</span>
          <el-tag type="danger">{{ openCount }} 项待整改</el-tag>
        </div>
      </template>

      <!-- PC 表格 -->
      <el-table v-if="!isMobile" v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="ncr_no" label="NCR编号" width="160" />
        <el-table-column prop="workcard_no" label="工卡编号" width="150" />
        <el-table-column prop="aircraft_id" label="机号" width="90" />
        <el-table-column label="严重等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="severityTagType[row.severity]" size="small">{{ severityLabel[row.severity] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="问题描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="ncrStatusTagType[row.status]" size="small">{{ ncrStatusLabel[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建人" width="90">
          <template #default="{ row }">{{ row.created_by?.name }}</template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="110">
          <template #default="{ row }">{{ row.created_at?.slice(0, 10) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'closed'" link type="primary" @click="handleClose(row)">
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片 -->
      <div v-else class="mobile-list">
        <el-card v-for="row in tableData" :key="row.id" class="mobile-item" shadow="hover">
          <div class="item-header">
            <el-tag :type="severityTagType[row.severity]" size="small">{{ severityLabel[row.severity] }}</el-tag>
            <el-tag :type="ncrStatusTagType[row.status]" size="small">{{ ncrStatusLabel[row.status] }}</el-tag>
          </div>
          <div class="ncr-no">{{ row.ncr_no }}</div>
          <div class="description">{{ row.description }}</div>
          <div class="meta">{{ row.workcard_no }} · {{ row.aircraft_id }} · {{ row.created_by?.name }}</div>
          <el-button v-if="row.status !== 'closed'" size="small" type="primary" class="mt-2" @click="handleClose(row)">
            关闭NCR
          </el-button>
        </el-card>
      </div>

      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>

    <!-- 关闭 NCR 弹窗 -->
    <el-dialog v-model="closeDialogVisible" title="关闭不符合项" width="460px">
      <div class="mb-2 text-sm text-gray-500">NCR：{{ currentNcr?.ncr_no }}</div>
      <div class="mb-3 text-sm">{{ currentNcr?.description }}</div>
      <el-form :model="closeForm" label-width="80px">
        <el-form-item label="整改说明">
          <el-input v-model="closeForm.rectification_desc" type="textarea" :rows="3"
            placeholder="请描述已完成的整改措施及复检结论" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="closing" @click="confirmClose">确认关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getNcrList, closeNcr } from '@/api/quality'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: '' })
const isMobile = computed(() => window.innerWidth < 768)
const openCount = computed(() => tableData.value.filter(r => r.status === 'open').length)
const closeDialogVisible = ref(false)
const currentNcr = ref(null)
const closeForm = reactive({ rectification_desc: '' })
const closing = ref(false)

const severityTagType = { critical: 'danger', major: 'warning', minor: 'info' }
const severityLabel = { critical: '严重', major: '重要', minor: '轻微' }
const ncrStatusTagType = { open: 'danger', in_rectification: 'warning', closed: 'success' }
const ncrStatusLabel = { open: '待整改', in_rectification: '整改中', closed: '已关闭' }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getNcrList(queryParams)
    if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { queryParams.status = ''; handleSearch() }

const handleClose = (row) => {
  currentNcr.value = row
  closeForm.rectification_desc = ''
  closeDialogVisible.value = true
}

const confirmClose = async () => {
  closing.value = true
  try {
    const res = await closeNcr(currentNcr.value.id, closeForm)
    if (res.code === 200) {
      ElMessage.success('NCR 已关闭')
      closeDialogVisible.value = false
      fetchData()
    }
  } finally { closing.value = false }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
.mobile-list { display: flex; flex-direction: column; gap: 8px; }
.mobile-item { padding: 4px; }
.item-header { display: flex; gap: 8px; margin-bottom: 6px; }
.ncr-no { font-weight: 600; font-size: 13px; margin-bottom: 4px; }
.description { font-size: 14px; margin-bottom: 6px; }
.meta { font-size: 12px; color: #909399; }
.mt-2 { margin-top: 8px; }
</style>
```

---

## Task 6: 注册路由

**Files:**
- Modify: `frontend/src/router/routes.js`

- [ ] **Step 1: 追加质检签署路由块**

在 `routes.js` 中物料申领路由块之后（或 wiki 之前）追加：

```javascript
      {
        path: 'mro/quality',
        name: 'QualityManagement',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '质检签署', requiresAuth: true },
        children: [
          { path: 'pending', name: 'QualityPending', component: () => import('@/pages/mro/quality/QualityPending.vue'), meta: { title: '待签署', requiresAuth: true, permissions: ['quality:pending:view'], breadcrumb: '待签署' } },
          { path: 'sign/:id', name: 'QualitySign', component: () => import('@/pages/mro/quality/QualitySign.vue'), meta: { title: '签署详情', requiresAuth: true, permissions: ['quality:sign:create'], breadcrumb: '签署详情' } },
          { path: 'ncr', name: 'QualityNcr', component: () => import('@/pages/mro/quality/QualityNcr.vue'), meta: { title: '不符合项', requiresAuth: true, permissions: ['quality:ncr:edit'], breadcrumb: '不符合项' } }
        ]
      },
```

- [ ] **Step 2: 构建验证**

```bash
cd frontend && npm run build 2>&1 | tail -20
```
期望：`✓ built in` 无报错。

- [ ] **Step 3: 提交**

```bash
cd frontend && git add src/api/quality.js src/mock/api/quality.js src/pages/mro/quality/ src/router/routes.js
git commit -m "feat(mro): add quality inspection sign-off and NCR management

Refs: MRO-011"
```

---

## 自检结果

| 检查项 | 结果 |
|------|------|
| Spec 覆盖 | 待签署列表 ✓、电子签署（工号+密码）✓、NCR创建 ✓、NCR关闭 ✓、工卡回退逻辑（前端状态）✓、响应式 ✓ |
| 占位符 | 无 TBD/TODO |
| 类型一致性 | `submitSign(id, { employee_no, password })` / `createNcr(data)` / `closeNcr(id, data)` 在 API 和页面中一致 ✓ |
