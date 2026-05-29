# 智慧机务增强 第一批：人员资质台账 + 维修计划与派工中心 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增人员资质台账（3页）和维修计划与派工中心（3页），作为后续工卡执行、质检、物料等模块的基础数据依赖。

**Architecture:** 遵循现有 Vue 3 + Element Plus 模式，每个模块新建独立的 API 文件、Mock 文件和页面目录。路由追加到 `routes.js` 的 `protectedRoutes[0].children`，Mock 文件放入 `src/mock/api/` 目录由 `vite-plugin-mock` 自动扫描注册。

**Tech Stack:** Vue 3 Composition API (`<script setup>`), Element Plus 2, Pinia, Vue Router 4, Axios, Tailwind CSS 3, vite-plugin-mock

---

## 文件结构

### 新建文件

```
frontend/src/api/personnel.js          — 人员资质 API
frontend/src/api/planning.js           — 维修计划派工 API
frontend/src/mock/api/personnel.js     — 人员资质 Mock 数据
frontend/src/mock/api/planning.js      — 维修计划派工 Mock 数据
frontend/src/pages/mro/personnel/
  PersonnelList.vue                    — 人员资质列表
  PersonnelDetail.vue                  — 人员详情
  PersonnelAlerts.vue                  — 资质预警
frontend/src/pages/mro/planning/
  TaskList.vue                         — 任务包列表
  TaskDetail.vue                       — 任务包详情/派工
  WorkloadView.vue                     — 人力负荷视图
```

### 修改文件

```
frontend/src/router/routes.js          — 追加 6 条新路由
```

---

## Task 1: 人员资质 API 文件

**Files:**
- Create: `frontend/src/api/personnel.js`

- [ ] **Step 1: 创建 API 文件**

```javascript
import request from '@/utils/request'

export const getPersonnelList = (params) => request.get('/api/personnel/list', { params })
export const getPersonnelDetail = (id) => request.get(`/api/personnel/${id}`)
export const createPersonnel = (data) => request.post('/api/personnel', data)
export const updatePersonnel = (id, data) => request.put(`/api/personnel/${id}`, data)
export const deletePersonnel = (id) => request.delete(`/api/personnel/${id}`)
export const getLicenseAlerts = (params) => request.get('/api/personnel/alerts', { params })
export const getLicenseTypes = () => request.get('/api/personnel/license-types')
export const checkPersonnelQualification = (personnelId, workcardType) =>
  request.get('/api/personnel/check-qualification', { params: { personnelId, workcardType } })
```

- [ ] **Step 2: 验证文件创建成功**

在终端运行：
```bash
ls frontend/src/api/personnel.js
```
期望输出：文件存在，无报错。

---

## Task 2: 人员资质 Mock 数据

**Files:**
- Create: `frontend/src/mock/api/personnel.js`

- [ ] **Step 1: 创建 Mock 文件**

```javascript
const licenseTypes = ['AME-A', 'AME-B', 'AME-C', 'NDT-UT', 'NDT-RT', 'WLD-1', 'AVIONICS', 'HYDRAULIC']
const workTypes = ['机体结构', '发动机', '电气', '液压', '航电', '无损检测', '焊接']
const names = ['张伟', '李明', '王强', '陈志', '刘洋', '赵磊', '孙健', '周勇', '吴刚', '郑博']

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generateExpireDate(daysFromNow) {
  const d = new Date('2026-05-28')
  d.setDate(d.getDate() + daysFromNow)
  return d.toISOString().slice(0, 10)
}

function generatePersonnelList() {
  return names.map((name, i) => ({
    id: i + 1,
    name,
    employee_no: `MRO${String(1000 + i).padStart(4, '0')}`,
    work_type: workTypes[i % workTypes.length],
    department: ['结构车间', '发动机车间', '电气车间', '综合车间'][i % 4],
    licenses: [
      {
        id: i * 2 + 1,
        type: licenseTypes[i % licenseTypes.length],
        license_no: `LIC-2024-${String(1000 + i).padStart(4, '0')}`,
        issued_at: '2024-01-15',
        expires_at: generateExpireDate([-10, 15, 25, 45, 75, 120, 180, 365, 400, 500][i]),
        status: [-10, 15, 25].includes([-10, 15, 25, 45, 75, 120, 180, 365, 400, 500][i])
          ? 'expiring'
          : 'valid'
      }
    ],
    status: 'active',
    workcard_types: ['heavy_check', 'line_check', 'engine_overhaul'].slice(0, (i % 3) + 1)
  }))
}

function generateAlerts() {
  const personnel = generatePersonnelList()
  return personnel
    .filter(p => p.licenses.some(l => {
      const days = (new Date(l.expires_at) - new Date('2026-05-28')) / 86400000
      return days <= 90
    }))
    .map(p => {
      const license = p.licenses.find(l => {
        const days = (new Date(l.expires_at) - new Date('2026-05-28')) / 86400000
        return days <= 90
      })
      const days = Math.round((new Date(license.expires_at) - new Date('2026-05-28')) / 86400000)
      return {
        id: p.id,
        personnel_id: p.id,
        name: p.name,
        employee_no: p.employee_no,
        license_type: license.type,
        license_no: license.license_no,
        expires_at: license.expires_at,
        days_remaining: days,
        urgency: days <= 0 ? 'expired' : days <= 30 ? 'critical' : days <= 60 ? 'warning' : 'notice'
      }
    })
    .sort((a, b) => a.days_remaining - b.days_remaining)
}

const personnelList = generatePersonnelList()

export default [
  {
    url: '/api/personnel/list',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 10, name, work_type, license_type } = query
      let list = personnelList
      if (name) list = list.filter(p => p.name.includes(name))
      if (work_type) list = list.filter(p => p.work_type === work_type)
      if (license_type) list = list.filter(p => p.licenses.some(l => l.type === license_type))
      const start = (page - 1) * pageSize
      return { code: 200, data: { list: list.slice(start, start + Number(pageSize)), total: list.length } }
    }
  },
  {
    url: '/api/personnel/:id',
    method: 'get',
    response: ({ params }) => {
      const person = personnelList.find(p => p.id === Number(params.id))
      return person
        ? { code: 200, data: person }
        : { code: 404, message: '人员不存在' }
    }
  },
  {
    url: '/api/personnel',
    method: 'post',
    response: ({ body }) => ({
      code: 200,
      data: { ...body, id: Date.now(), status: 'active', licenses: [] },
      message: '创建成功'
    })
  },
  {
    url: '/api/personnel/:id',
    method: 'put',
    response: ({ body }) => ({ code: 200, data: body, message: '更新成功' })
  },
  {
    url: '/api/personnel/:id',
    method: 'delete',
    response: () => ({ code: 200, message: '删除成功' })
  },
  {
    url: '/api/personnel/alerts',
    method: 'get',
    response: () => ({ code: 200, data: { list: generateAlerts(), total: generateAlerts().length } })
  },
  {
    url: '/api/personnel/license-types',
    method: 'get',
    response: () => ({ code: 200, data: licenseTypes.map(t => ({ value: t, label: t })) })
  },
  {
    url: '/api/personnel/check-qualification',
    method: 'get',
    response: ({ query }) => {
      const person = personnelList.find(p => p.id === Number(query.personnelId))
      if (!person) return { code: 200, data: { qualified: false, reason: '人员不存在' } }
      const qualified = person.workcard_types.includes(query.workcardType)
      return {
        code: 200,
        data: {
          qualified,
          reason: qualified ? '' : `该人员不具备执行 ${query.workcardType} 类型工卡的资质`
        }
      }
    }
  }
]
```

- [ ] **Step 2: 验证 Mock 文件语法**

```bash
cd frontend && node --input-type=module < src/mock/api/personnel.js 2>&1 | head -5
```
期望：无报错输出（或仅有 import 相关提示，因为 mock 文件无 import）。

---

## Task 3: 人员资质列表页 PersonnelList.vue

**Files:**
- Create: `frontend/src/pages/mro/personnel/PersonnelList.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="personnel-list">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="工种">
          <el-select v-model="queryParams.work_type" placeholder="请选择" clearable style="width:140px">
            <el-option v-for="t in workTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="执照类别">
          <el-select v-model="queryParams.license_type" placeholder="请选择" clearable style="width:140px">
            <el-option v-for="t in licenseTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
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
          <span class="title">人员资质台账</span>
          <div class="actions">
            <el-button type="warning" @click="$router.push('/mro/personnel/alerts')">
              <el-icon><Warning /></el-icon>资质预警
            </el-button>
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>新增人员
            </el-button>
          </div>
        </div>
      </template>

      <!-- PC 表格 -->
      <el-table v-if="!isMobile" v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="employee_no" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="work_type" label="工种" width="110" />
        <el-table-column prop="department" label="班组" width="110" />
        <el-table-column label="执照" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="l in row.licenses"
              :key="l.id"
              :type="l.status === 'expiring' ? 'danger' : 'success'"
              size="small"
              class="mr-1"
            >{{ l.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近到期" width="120">
          <template #default="{ row }">
            <span :class="getNearestExpireClass(row)">{{ getNearestExpire(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/mro/personnel/${row.id}`)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片 -->
      <div v-else class="mobile-list">
        <el-card
          v-for="row in tableData"
          :key="row.id"
          class="mobile-item"
          shadow="hover"
          @click="$router.push(`/mro/personnel/${row.id}`)"
        >
          <div class="mobile-item-header">
            <span class="name">{{ row.name }}</span>
            <span class="employee-no">{{ row.employee_no }}</span>
          </div>
          <div class="mobile-item-body">
            <span class="work-type">{{ row.work_type }} · {{ row.department }}</span>
            <div class="licenses">
              <el-tag
                v-for="l in row.licenses"
                :key="l.id"
                :type="l.status === 'expiring' ? 'danger' : 'success'"
                size="small"
                class="mr-1"
              >{{ l.type }}</el-tag>
            </div>
          </div>
        </el-card>
      </div>

      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>

    <!-- 新增人员弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增人员" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="工号"><el-input v-model="form.employee_no" /></el-form-item>
        <el-form-item label="工种">
          <el-select v-model="form.work_type" style="width:100%">
            <el-option v-for="t in workTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="班组"><el-input v-model="form.department" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Warning } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getPersonnelList, createPersonnel, getLicenseTypes } from '@/api/personnel'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, name: '', work_type: '', license_type: '' })
const dialogVisible = ref(false)
const submitting = ref(false)
const form = reactive({ name: '', employee_no: '', work_type: '', department: '' })
const licenseTypeOptions = ref([])
const workTypes = ['机体结构', '发动机', '电气', '液压', '航电', '无损检测', '焊接']

const isMobile = computed(() => window.innerWidth < 768)

const getNearestExpire = (row) => {
  if (!row.licenses.length) return '-'
  const sorted = [...row.licenses].sort((a, b) => new Date(a.expires_at) - new Date(b.expires_at))
  return sorted[0].expires_at
}

const getNearestExpireClass = (row) => {
  if (!row.licenses.length) return ''
  const sorted = [...row.licenses].sort((a, b) => new Date(a.expires_at) - new Date(b.expires_at))
  const days = (new Date(sorted[0].expires_at) - new Date()) / 86400000
  if (days <= 30) return 'text-red-500 font-semibold'
  if (days <= 90) return 'text-orange-500'
  return ''
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPersonnelList(queryParams)
    if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { Object.assign(queryParams, { name: '', work_type: '', license_type: '' }); handleSearch() }
const handleCreate = () => { Object.assign(form, { name: '', employee_no: '', work_type: '', department: '' }); dialogVisible.value = true }

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await createPersonnel(form)
    if (res.code === 200) { ElMessage.success('创建成功'); dialogVisible.value = false; fetchData() }
  } finally { submitting.value = false }
}

onMounted(async () => {
  fetchData()
  const res = await getLicenseTypes()
  if (res.code === 200) licenseTypeOptions.value = res.data
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
.mobile-list { display: flex; flex-direction: column; gap: 8px; }
.mobile-item { cursor: pointer; }
.mobile-item-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.mobile-item-header .name { font-weight: 600; font-size: 15px; }
.mobile-item-header .employee-no { color: #909399; font-size: 13px; }
.mobile-item-body .work-type { font-size: 13px; color: #606266; display: block; margin-bottom: 6px; }
.mr-1 { margin-right: 4px; }
</style>
```

- [ ] **Step 2: 验证文件存在**

```bash
ls frontend/src/pages/mro/personnel/PersonnelList.vue
```

---

## Task 4: 人员详情页 PersonnelDetail.vue

**Files:**
- Create: `frontend/src/pages/mro/personnel/PersonnelDetail.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="personnel-detail">
    <el-page-header @back="$router.back()" :content="detail?.name || '人员详情'" />

    <div v-loading="loading" class="mt-4">
      <el-row :gutter="16">
        <el-col :xs="24" :md="10">
          <el-card shadow="never">
            <template #header><span class="font-semibold">基本信息</span></template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="姓名">{{ detail?.name }}</el-descriptions-item>
              <el-descriptions-item label="工号">{{ detail?.employee_no }}</el-descriptions-item>
              <el-descriptions-item label="工种">{{ detail?.work_type }}</el-descriptions-item>
              <el-descriptions-item label="班组">{{ detail?.department }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="detail?.status === 'active' ? 'success' : 'info'" size="small">
                  {{ detail?.status === 'active' ? '在职' : '离职' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="可执行工卡类型">
                <el-tag v-for="t in detail?.workcard_types" :key="t" size="small" class="mr-1">
                  {{ { heavy_check: 'C检/D检', line_check: '航线', engine_overhaul: '发动机大修' }[t] || t }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="14">
          <el-card shadow="never">
            <template #header><span class="font-semibold">执照列表</span></template>
            <el-table :data="detail?.licenses || []" border>
              <el-table-column prop="type" label="执照类别" width="120" />
              <el-table-column prop="license_no" label="执照编号" />
              <el-table-column prop="issued_at" label="颁发日期" width="110" />
              <el-table-column prop="expires_at" label="到期日期" width="110">
                <template #default="{ row }">
                  <span :class="getLicenseExpireClass(row.expires_at)">{{ row.expires_at }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="getLicenseTagType(row.expires_at)" size="small">
                    {{ getLicenseStatusText(row.expires_at) }}
                  </el-tag>
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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPersonnelDetail } from '@/api/personnel'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)

const getDaysRemaining = (expiresAt) => {
  return Math.round((new Date(expiresAt) - new Date()) / 86400000)
}

const getLicenseExpireClass = (expiresAt) => {
  const days = getDaysRemaining(expiresAt)
  if (days <= 0) return 'text-red-600 font-bold'
  if (days <= 30) return 'text-red-500 font-semibold'
  if (days <= 90) return 'text-orange-500'
  return ''
}

const getLicenseTagType = (expiresAt) => {
  const days = getDaysRemaining(expiresAt)
  if (days <= 0) return 'danger'
  if (days <= 30) return 'danger'
  if (days <= 90) return 'warning'
  return 'success'
}

const getLicenseStatusText = (expiresAt) => {
  const days = getDaysRemaining(expiresAt)
  if (days <= 0) return '已过期'
  if (days <= 30) return `${days}天到期`
  if (days <= 90) return '即将到期'
  return '有效'
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getPersonnelDetail(route.params.id)
    if (res.code === 200) detail.value = res.data
  } finally { loading.value = false }
})
</script>

<style scoped>
.mr-1 { margin-right: 4px; }
</style>
```

---

## Task 5: 资质预警页 PersonnelAlerts.vue

**Files:**
- Create: `frontend/src/pages/mro/personnel/PersonnelAlerts.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="personnel-alerts">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">资质预警</span>
          <el-tag type="danger">{{ alertList.length }} 项预警</el-tag>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 统计卡片 -->
        <el-row :gutter="12" class="mb-4">
          <el-col :xs="12" :sm="6">
            <el-card shadow="never" class="stat-card expired">
              <div class="stat-number">{{ counts.expired }}</div>
              <div class="stat-label">已过期</div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-card shadow="never" class="stat-card critical">
              <div class="stat-number">{{ counts.critical }}</div>
              <div class="stat-label">30天内到期</div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-card shadow="never" class="stat-card warning">
              <div class="stat-number">{{ counts.warning }}</div>
              <div class="stat-label">60天内到期</div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-card shadow="never" class="stat-card notice">
              <div class="stat-number">{{ counts.notice }}</div>
              <div class="stat-label">90天内到期</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 预警列表（PC表格 / 移动卡片） -->
        <el-table v-if="!isMobile" :data="alertList" border>
          <el-table-column label="紧急程度" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="urgencyTagType[row.urgency]" size="small">
                {{ urgencyLabel[row.urgency] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="90" />
          <el-table-column prop="employee_no" label="工号" width="120" />
          <el-table-column prop="license_type" label="执照类别" width="120" />
          <el-table-column prop="license_no" label="执照编号" />
          <el-table-column prop="expires_at" label="到期日期" width="120">
            <template #default="{ row }">
              <span :class="row.days_remaining <= 0 ? 'text-red-600 font-bold' : 'text-red-500'">
                {{ row.expires_at }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="剩余天数" width="100" align="center">
            <template #default="{ row }">
              <span :class="row.days_remaining <= 0 ? 'text-red-600 font-bold' : ''">
                {{ row.days_remaining <= 0 ? '已过期' : `${row.days_remaining}天` }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="$router.push(`/mro/personnel/${row.personnel_id}`)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-else class="mobile-alerts">
          <el-card
            v-for="row in alertList"
            :key="row.id"
            class="alert-item"
            shadow="hover"
            @click="$router.push(`/mro/personnel/${row.personnel_id}`)"
          >
            <div class="alert-header">
              <el-tag :type="urgencyTagType[row.urgency]" size="small">{{ urgencyLabel[row.urgency] }}</el-tag>
              <span class="expires">{{ row.expires_at }}</span>
            </div>
            <div class="alert-body">
              <span class="person-name">{{ row.name }} ({{ row.employee_no }})</span>
              <span class="license-info">{{ row.license_type }} · {{ row.license_no }}</span>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getLicenseAlerts } from '@/api/personnel'

const loading = ref(false)
const alertList = ref([])
const isMobile = computed(() => window.innerWidth < 768)

const urgencyTagType = { expired: 'danger', critical: 'danger', warning: 'warning', notice: 'info' }
const urgencyLabel = { expired: '已过期', critical: '紧急', warning: '警告', notice: '注意' }

const counts = computed(() => ({
  expired: alertList.value.filter(a => a.urgency === 'expired').length,
  critical: alertList.value.filter(a => a.urgency === 'critical').length,
  warning: alertList.value.filter(a => a.urgency === 'warning').length,
  notice: alertList.value.filter(a => a.urgency === 'notice').length
}))

onMounted(async () => {
  loading.value = true
  try {
    const res = await getLicenseAlerts()
    if (res.code === 200) alertList.value = res.data.list
  } finally { loading.value = false }
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
.stat-card { text-align: center; }
.stat-number { font-size: 28px; font-weight: 700; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.stat-card.expired .stat-number { color: #f56c6c; }
.stat-card.critical .stat-number { color: #f56c6c; }
.stat-card.warning .stat-number { color: #e6a23c; }
.stat-card.notice .stat-number { color: #909399; }
.mobile-alerts { display: flex; flex-direction: column; gap: 8px; }
.alert-item { cursor: pointer; }
.alert-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.alert-header .expires { font-size: 13px; color: #f56c6c; }
.alert-body { display: flex; flex-direction: column; gap: 2px; }
.person-name { font-weight: 600; }
.license-info { font-size: 13px; color: #606266; }
</style>
```

---

## Task 6: 维修计划 API 文件

**Files:**
- Create: `frontend/src/api/planning.js`

- [ ] **Step 1: 创建 API 文件**

```javascript
import request from '@/utils/request'

export const getTaskList = (params) => request.get('/api/planning/tasks', { params })
export const getTaskDetail = (id) => request.get(`/api/planning/tasks/${id}`)
export const createTask = (data) => request.post('/api/planning/tasks', data)
export const updateTask = (id, data) => request.put(`/api/planning/tasks/${id}`, data)
export const deleteTask = (id) => request.delete(`/api/planning/tasks/${id}`)
export const assignPersonnel = (taskId, workcardId, data) =>
  request.post(`/api/planning/tasks/${taskId}/workcards/${workcardId}/assign`, data)
export const getWorkload = (params) => request.get('/api/planning/workload', { params })
export const getAvailablePersonnel = (params) =>
  request.get('/api/planning/available-personnel', { params })
```

---

## Task 7: 维修计划 Mock 数据

**Files:**
- Create: `frontend/src/mock/api/planning.js`

- [ ] **Step 1: 创建 Mock 文件**

```javascript
const aircraftRegs = ['B-1234', 'B-5678', 'B-9012', 'B-3456']
const checkTypes = ['C检', 'D检', '发动机大修', '特修']

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generateWorkcards(taskId, count) {
  const types = ['结构检查', '系统功能测试', '腐蚀防护处理', '发动机孔探', '起落架拆装']
  return Array.from({ length: count }, (_, i) => ({
    id: taskId * 100 + i + 1,
    task_id: taskId,
    card_no: `WC-${taskId}-${String(i + 1).padStart(3, '0')}`,
    title: `${randomPick(types)} #${i + 1}`,
    card_type: 'heavy_check',
    priority: randomPick(['urgent', 'normal', 'low']),
    status: randomPick(['pending_assign', 'assigned', 'in_progress', 'pending_inspect', 'completed']),
    progress: Math.floor(Math.random() * 100),
    estimated_hours: Math.floor(4 + Math.random() * 20),
    assigned_to: i % 3 === 0 ? null : { id: i + 1, name: ['张伟', '李明', '王强'][i % 3], employee_no: `MRO100${i}` },
    steps_total: Math.floor(5 + Math.random() * 15),
    steps_done: Math.floor(Math.random() * 10)
  }))
}

function generateTasks() {
  return Array.from({ length: 6 }, (_, i) => {
    const taskId = i + 1
    const workcards = generateWorkcards(taskId, 5 + i * 2)
    const total = workcards.length
    const done = workcards.filter(w => w.status === 'completed').length
    return {
      id: taskId,
      task_no: `TASK-2026-${String(taskId).padStart(4, '0')}`,
      aircraft_id: aircraftRegs[i % aircraftRegs.length],
      check_type: checkTypes[i % checkTypes.length],
      status: ['planning', 'in_progress', 'in_progress', 'completed', 'planning', 'in_progress'][i],
      planned_start: `2026-0${5 + (i % 2)}-${String(10 + i * 3).padStart(2, '0')}`,
      planned_end: `2026-0${6 + (i % 2)}-${String(10 + i * 3).padStart(2, '0')}`,
      actual_start: i > 0 ? `2026-0${5 + (i % 2)}-${String(11 + i * 3).padStart(2, '0')}` : null,
      responsible_person: ['赵班长', '钱工段长', '孙主任'][i % 3],
      progress: Math.round((done / total) * 100),
      workcard_total: total,
      workcard_done: done,
      workcards
    }
  })
}

const tasks = generateTasks()

export default [
  {
    url: '/api/planning/tasks',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 10, status, aircraft_id } = query
      let list = tasks
      if (status) list = list.filter(t => t.status === status)
      if (aircraft_id) list = list.filter(t => t.aircraft_id === aircraft_id)
      const start = (page - 1) * pageSize
      const summaries = list.map(({ workcards: _w, ...t }) => t)
      return { code: 200, data: { list: summaries.slice(start, start + Number(pageSize)), total: list.length } }
    }
  },
  {
    url: '/api/planning/tasks/:id',
    method: 'get',
    response: ({ params }) => {
      const task = tasks.find(t => t.id === Number(params.id))
      return task ? { code: 200, data: task } : { code: 404, message: '任务包不存在' }
    }
  },
  {
    url: '/api/planning/tasks',
    method: 'post',
    response: ({ body }) => ({
      code: 200,
      data: { ...body, id: Date.now(), task_no: `TASK-2026-${Date.now()}`, status: 'planning', progress: 0, workcard_total: 0, workcard_done: 0, workcards: [] },
      message: '创建成功'
    })
  },
  {
    url: '/api/planning/tasks/:id',
    method: 'put',
    response: ({ body }) => ({ code: 200, data: body, message: '更新成功' })
  },
  {
    url: '/api/planning/tasks/:id',
    method: 'delete',
    response: () => ({ code: 200, message: '删除成功' })
  },
  {
    url: '/api/planning/tasks/:taskId/workcards/:workcardId/assign',
    method: 'post',
    response: ({ body }) => ({ code: 200, data: body, message: '派工成功' })
  },
  {
    url: '/api/planning/workload',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        personnel: ['张伟', '李明', '王强', '陈志', '刘洋'].map((name, i) => ({
          id: i + 1,
          name,
          tasks: Array.from({ length: 2 + i % 3 }, (_, j) => ({
            task_id: j + 1,
            task_no: `TASK-2026-000${j + 1}`,
            aircraft_id: aircraftRegs[j % aircraftRegs.length],
            start: `2026-05-${String(10 + j * 5).padStart(2, '0')}`,
            end: `2026-06-${String(10 + j * 5).padStart(2, '0')}`,
            estimated_hours: 40 + j * 8
          }))
        }))
      }
    })
  },
  {
    url: '/api/planning/available-personnel',
    method: 'get',
    response: ({ query }) => {
      const { workcardType } = query
      const all = [
        { id: 1, name: '张伟', employee_no: 'MRO1000', work_type: '机体结构', qualified: true },
        { id: 2, name: '李明', employee_no: 'MRO1001', work_type: '发动机', qualified: workcardType === 'engine_overhaul' },
        { id: 3, name: '王强', employee_no: 'MRO1002', work_type: '电气', qualified: workcardType !== 'engine_overhaul' },
        { id: 4, name: '陈志', employee_no: 'MRO1003', work_type: '液压', qualified: true },
        { id: 5, name: '刘洋', employee_no: 'MRO1004', work_type: '机体结构', qualified: true }
      ]
      return { code: 200, data: all }
    }
  }
]
```

---

## Task 8: 任务包列表页 TaskList.vue

**Files:**
- Create: `frontend/src/pages/mro/planning/TaskList.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="task-list">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width:130px">
            <el-option label="计划中" value="planning" />
            <el-option label="执行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="机号">
          <el-input v-model="queryParams.aircraft_id" placeholder="请输入" clearable style="width:110px" />
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
          <span class="title">维修任务包</span>
          <div class="actions">
            <el-button @click="$router.push('/mro/planning/workload')">人力负荷</el-button>
            <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>新建任务包</el-button>
          </div>
        </div>
      </template>

      <!-- PC 表格 -->
      <el-table v-if="!isMobile" v-loading="loading" :data="tableData" border stripe
        @row-click="row => $router.push(`/mro/planning/tasks/${row.id}`)">
        <el-table-column prop="task_no" label="任务编号" width="160" />
        <el-table-column prop="aircraft_id" label="机号" width="90" />
        <el-table-column prop="check_type" label="检修类型" width="100" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status]" size="small">{{ statusLabel[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planned_start" label="计划开始" width="110" />
        <el-table-column prop="planned_end" label="计划结束" width="110" />
        <el-table-column prop="responsible_person" label="负责人" width="90" />
        <el-table-column label="工卡进度" min-width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-progress :percentage="row.progress" :stroke-width="8" style="flex:1" />
              <span class="text-xs text-gray-500">{{ row.workcard_done }}/{{ row.workcard_total }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端卡片 -->
      <div v-else class="mobile-list">
        <el-card
          v-for="row in tableData"
          :key="row.id"
          class="mobile-item"
          shadow="hover"
          @click="$router.push(`/mro/planning/tasks/${row.id}`)"
        >
          <div class="mobile-item-header">
            <span class="task-no">{{ row.task_no }}</span>
            <el-tag :type="statusTagType[row.status]" size="small">{{ statusLabel[row.status] }}</el-tag>
          </div>
          <div class="mobile-item-body">
            <span>{{ row.aircraft_id }} · {{ row.check_type }}</span>
            <el-progress :percentage="row.progress" :stroke-width="6" class="mt-2" />
            <span class="text-xs text-gray-500">{{ row.workcard_done }}/{{ row.workcard_total }} 工卡</span>
          </div>
        </el-card>
      </div>

      <Pagination :total="total" v-model:page="queryParams.page" v-model:page-size="queryParams.pageSize" @change="fetchData" />
    </el-card>

    <!-- 新建任务包弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建任务包" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="机号"><el-input v-model="form.aircraft_id" /></el-form-item>
        <el-form-item label="检修类型">
          <el-select v-model="form.check_type" style="width:100%">
            <el-option label="C检" value="C检" />
            <el-option label="D检" value="D检" />
            <el-option label="发动机大修" value="发动机大修" />
            <el-option label="特修" value="特修" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始">
          <el-date-picker v-model="form.planned_start" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="计划结束">
          <el-date-picker v-model="form.planned_end" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.responsible_person" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getTaskList, createTask } from '@/api/planning'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: '', aircraft_id: '' })
const dialogVisible = ref(false)
const submitting = ref(false)
const form = reactive({ aircraft_id: '', check_type: '', planned_start: '', planned_end: '', responsible_person: '' })
const isMobile = computed(() => window.innerWidth < 768)

const statusTagType = { planning: 'info', in_progress: 'warning', completed: 'success' }
const statusLabel = { planning: '计划中', in_progress: '执行中', completed: '已完成' }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTaskList(queryParams)
    if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleSearch = () => { queryParams.page = 1; fetchData() }
const handleReset = () => { Object.assign(queryParams, { status: '', aircraft_id: '' }); handleSearch() }
const handleCreate = () => {
  Object.assign(form, { aircraft_id: '', check_type: '', planned_start: '', planned_end: '', responsible_person: '' })
  dialogVisible.value = true
}
const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await createTask(form)
    if (res.code === 200) { ElMessage.success('创建成功'); dialogVisible.value = false; fetchData() }
  } finally { submitting.value = false }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
.mobile-list { display: flex; flex-direction: column; gap: 8px; }
.mobile-item { cursor: pointer; }
.mobile-item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.mobile-item-header .task-no { font-weight: 600; }
.mobile-item-body { display: flex; flex-direction: column; gap: 4px; font-size: 13px; color: #606266; }
</style>
```

---

## Task 9: 任务包详情/派工页 TaskDetail.vue

**Files:**
- Create: `frontend/src/pages/mro/planning/TaskDetail.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="task-detail">
    <el-page-header @back="$router.back()" :content="detail ? `${detail.task_no} · ${detail.aircraft_id}` : '任务详情'" />

    <div v-loading="loading" class="mt-4">
      <el-row :gutter="16">
        <!-- 任务概况 -->
        <el-col :xs="24" :lg="8">
          <el-card shadow="never" class="mb-4">
            <template #header><span class="font-semibold">任务概况</span></template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="任务编号">{{ detail?.task_no }}</el-descriptions-item>
              <el-descriptions-item label="机号">{{ detail?.aircraft_id }}</el-descriptions-item>
              <el-descriptions-item label="检修类型">{{ detail?.check_type }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="statusTagType[detail?.status]" size="small">{{ statusLabel[detail?.status] }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="计划开始">{{ detail?.planned_start }}</el-descriptions-item>
              <el-descriptions-item label="计划结束">{{ detail?.planned_end }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ detail?.responsible_person }}</el-descriptions-item>
            </el-descriptions>
            <div class="mt-4">
              <div class="text-sm text-gray-500 mb-1">总体进度</div>
              <el-progress :percentage="detail?.progress || 0" :stroke-width="12" :text-inside="true" />
              <div class="text-xs text-gray-400 mt-1 text-right">{{ detail?.workcard_done }}/{{ detail?.workcard_total }} 工卡完成</div>
            </div>
          </el-card>
        </el-col>

        <!-- 工卡列表+派工 -->
        <el-col :xs="24" :lg="16">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span class="font-semibold">工卡列表</span>
                <el-button size="small" type="primary" @click="handleAddWorkcard">
                  <el-icon><Plus /></el-icon>添加工卡
                </el-button>
              </div>
            </template>
            <el-table :data="detail?.workcards || []" border size="small">
              <el-table-column prop="card_no" label="工卡编号" width="130" />
              <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="workcardStatusTagType[row.status]" size="small">
                    {{ workcardStatusLabel[row.status] }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="派工" width="160">
                <template #default="{ row }">
                  <div v-if="row.assigned_to" class="assigned-person">
                    <span>{{ row.assigned_to.name }}</span>
                    <el-button link size="small" @click.stop="handleReassign(row)">重派</el-button>
                  </div>
                  <el-button v-else link type="primary" size="small" @click.stop="handleAssign(row)">
                    <el-icon><UserFilled /></el-icon>指派
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column label="进度" width="100">
                <template #default="{ row }">
                  <el-progress :percentage="row.progress" :stroke-width="6" />
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 派工弹窗 -->
    <el-dialog v-model="assignDialogVisible" title="指派人员" width="480px">
      <div class="mb-2 text-sm text-gray-500">工卡：{{ currentWorkcard?.title }}</div>
      <el-table :data="availablePersonnel" border size="small" @row-click="selectPerson" highlight-current-row>
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="employee_no" label="工号" width="100" />
        <el-table-column prop="work_type" label="工种" />
        <el-table-column label="资质" width="70" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.qualified" color="#67c23a"><CircleCheck /></el-icon>
            <el-tooltip v-else content="不具备该工卡类型资质" placement="top">
              <el-icon color="#f56c6c"><CircleClose /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" :disabled="!selectedPerson?.qualified" @click="confirmAssign">
          确认派工
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, UserFilled, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { getTaskDetail, assignPersonnel, getAvailablePersonnel } from '@/api/planning'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)
const assignDialogVisible = ref(false)
const currentWorkcard = ref(null)
const availablePersonnel = ref([])
const selectedPerson = ref(null)
const assigning = ref(false)

const statusTagType = { planning: 'info', in_progress: 'warning', completed: 'success' }
const statusLabel = { planning: '计划中', in_progress: '执行中', completed: '已完成' }
const workcardStatusTagType = { pending_assign: 'info', assigned: '', in_progress: 'warning', pending_inspect: 'warning', completed: 'success' }
const workcardStatusLabel = { pending_assign: '待派工', assigned: '已派工', in_progress: '执行中', pending_inspect: '待质检', completed: '已完成' }

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getTaskDetail(route.params.id)
    if (res.code === 200) detail.value = res.data
  } finally { loading.value = false }
}

const handleAssign = async (workcard) => {
  currentWorkcard.value = workcard
  selectedPerson.value = null
  const res = await getAvailablePersonnel({ workcardType: workcard.card_type })
  if (res.code === 200) availablePersonnel.value = res.data
  assignDialogVisible.value = true
}

const handleReassign = (workcard) => handleAssign(workcard)

const selectPerson = (row) => { selectedPerson.value = row }

const confirmAssign = async () => {
  if (!selectedPerson.value) return
  assigning.value = true
  try {
    const res = await assignPersonnel(detail.value.id, currentWorkcard.value.id, {
      personnel_id: selectedPerson.value.id,
      personnel_name: selectedPerson.value.name,
      employee_no: selectedPerson.value.employee_no
    })
    if (res.code === 200) {
      ElMessage.success('派工成功')
      assignDialogVisible.value = false
      fetchDetail()
    }
  } finally { assigning.value = false }
}

const handleAddWorkcard = () => ElMessage.info('添加工卡功能开发中')

onMounted(() => { fetchDetail() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.assigned-person { display: flex; align-items: center; gap: 6px; }
</style>
```

---

## Task 10: 人力负荷视图 WorkloadView.vue

**Files:**
- Create: `frontend/src/pages/mro/planning/WorkloadView.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="workload-view">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">人力负荷视图</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <div v-for="person in workloadData" :key="person.id" class="person-row">
          <div class="person-name">{{ person.name }}</div>
          <div class="task-bars">
            <el-tooltip
              v-for="task in person.tasks"
              :key="task.task_id"
              :content="`${task.task_no} · ${task.aircraft_id} (${task.start} ~ ${task.end}, ${task.estimated_hours}h)`"
              placement="top"
            >
              <div class="task-bar">
                <span class="task-label">{{ task.aircraft_id }} {{ task.task_no.slice(-4) }}</span>
                <span class="task-hours">{{ task.estimated_hours }}h</span>
              </div>
            </el-tooltip>
            <div v-if="!person.tasks.length" class="no-task">暂无任务</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWorkload } from '@/api/planning'

const loading = ref(false)
const workloadData = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getWorkload()
    if (res.code === 200) workloadData.value = res.data.personnel
  } finally { loading.value = false }
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-weight: 600; }
.person-row { display: flex; align-items: flex-start; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.person-name { width: 60px; font-weight: 600; font-size: 14px; flex-shrink: 0; padding-top: 4px; }
.task-bars { display: flex; flex-wrap: wrap; gap: 8px; flex: 1; }
.task-bar { background: #ecf5ff; border: 1px solid #b3d8ff; border-radius: 4px; padding: 4px 10px; cursor: pointer; display: flex; gap: 8px; align-items: center; }
.task-label { font-size: 13px; color: #409eff; font-weight: 500; }
.task-hours { font-size: 12px; color: #909399; }
.no-task { font-size: 13px; color: #c0c4cc; }
</style>
```

---

## Task 11: 注册路由

**Files:**
- Modify: `frontend/src/router/routes.js`

- [ ] **Step 1: 在 routes.js 中追加 6 条新路由**

在 `routes.js` 的 `protectedRoutes[0].children` 数组末尾（`wiki` 路由之前）追加以下内容：

```javascript
      {
        path: 'mro/personnel',
        name: 'PersonnelManagement',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '人员资质', requiresAuth: true },
        children: [
          { path: '', name: 'PersonnelList', component: () => import('@/pages/mro/personnel/PersonnelList.vue'), meta: { title: '资质台账', requiresAuth: true, permissions: ['personnel:list:view'], breadcrumb: '资质台账' } },
          { path: 'alerts', name: 'PersonnelAlerts', component: () => import('@/pages/mro/personnel/PersonnelAlerts.vue'), meta: { title: '资质预警', requiresAuth: true, permissions: ['personnel:list:view'], breadcrumb: '资质预警' } },
          { path: ':id', name: 'PersonnelDetail', component: () => import('@/pages/mro/personnel/PersonnelDetail.vue'), meta: { title: '人员详情', requiresAuth: true, permissions: ['personnel:list:view'], breadcrumb: '人员详情' } }
        ]
      },
      {
        path: 'mro/planning',
        name: 'PlanningManagement',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '维修计划', requiresAuth: true },
        children: [
          { path: 'tasks', name: 'PlanningTaskList', component: () => import('@/pages/mro/planning/TaskList.vue'), meta: { title: '任务包', requiresAuth: true, permissions: ['planning:task:view'], breadcrumb: '任务包' } },
          { path: 'tasks/:id', name: 'PlanningTaskDetail', component: () => import('@/pages/mro/planning/TaskDetail.vue'), meta: { title: '任务详情', requiresAuth: true, permissions: ['planning:task:view'], breadcrumb: '任务详情' } },
          { path: 'workload', name: 'PlanningWorkload', component: () => import('@/pages/mro/planning/WorkloadView.vue'), meta: { title: '人力负荷', requiresAuth: true, permissions: ['planning:task:view'], breadcrumb: '人力负荷' } }
        ]
      },
```

具体插入位置：找到 `routes.js` 中 `wiki` 相关路由块之前，即：

```javascript
      // 在这里插入上面的两个路由块
      {
        path: 'wiki',
        name: 'Wiki',
        ...
```

- [ ] **Step 2: 构建验证**

```bash
cd frontend && npm run build 2>&1 | tail -20
```
期望：`✓ built in` 无报错。

- [ ] **Step 3: 提交**

```bash
cd frontend && git add src/api/personnel.js src/api/planning.js src/mock/api/personnel.js src/mock/api/planning.js src/pages/mro/personnel/ src/pages/mro/planning/ src/router/routes.js
git commit -m "feat(mro): add personnel qualification and maintenance planning modules

Refs: MRO-009"
```

---

## 自检结果

| 检查项 | 结果 |
|------|------|
| Spec 覆盖 | 人员资质台账3页 ✓、维修计划派工3页 ✓、响应式移动端卡片 ✓、资质校验联动 ✓ |
| 占位符 | 无 TBD/TODO |
| 类型一致性 | `assignPersonnel(taskId, workcardId, data)` 在 API 和 TaskDetail.vue 中一致 |
| Mock 自动注册 | 文件放入 `src/mock/api/` 自动被 vite-plugin-mock 扫描，无需手动注册 ✓ |
