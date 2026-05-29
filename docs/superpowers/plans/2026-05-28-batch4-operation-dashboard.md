# 智慧机务增强 第四批：作业看板大屏 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增作业看板大屏页面（`/mro/dashboard/operation`），汇聚机库全局机位进度、统计指标、异常告警，支持全屏展示，30秒自动刷新。

**Architecture:** 单页面模块，新建独立 API 和 Mock 文件。大屏使用 CSS Grid 布局，全屏通过 `document.documentElement.requestFullscreen()` 实现。所有数据通过聚合接口一次性获取，避免多次请求。

**Tech Stack:** Vue 3 Composition API (`<script setup>`), Element Plus 2, Vue Router 4, Axios, Tailwind CSS 3, vite-plugin-mock

---

## 文件结构

### 新建文件

```
frontend/src/api/dashboard.js                    — 作业看板 API
frontend/src/mock/api/dashboard-operation.js     — 作业看板 Mock
frontend/src/pages/mro/dashboard/
  OperationDashboard.vue                          — 作业看板大屏主页
```

### 修改文件

```
frontend/src/router/routes.js                    — 追加 1 条新路由
```

---

## Task 1: 看板 API 文件

**Files:**
- Create: `frontend/src/api/dashboard.js`

- [ ] **Step 1: 创建 API 文件**

```javascript
import request from '@/utils/request'

export const getOperationDashboard = () => request.get('/api/dashboard/operation')
```

---

## Task 2: 看板 Mock 数据

**Files:**
- Create: `frontend/src/mock/api/dashboard-operation.js`

- [ ] **Step 1: 创建 Mock 文件**

```javascript
function generateBayStatus() {
  const bays = ['A01', 'A02', 'A03', 'B01', 'B02', 'B03', 'C01', 'C02']
  const aircraft = ['B-1234', 'B-5678', 'B-9012', 'B-3456', null, null, 'B-7890', null]
  const checkTypes = ['C检', 'D检', '发动机大修', '特修']
  const statuses = ['in_progress', 'in_progress', 'in_progress', 'in_progress', 'empty', 'empty', 'in_progress', 'empty']

  return bays.map((bay, i) => ({
    bay_id: bay,
    aircraft_id: aircraft[i],
    check_type: aircraft[i] ? checkTypes[i % checkTypes.length] : null,
    status: statuses[i],
    progress: aircraft[i] ? Math.floor(20 + Math.random() * 70) : 0,
    workcard_total: aircraft[i] ? Math.floor(15 + Math.random() * 20) : 0,
    workcard_done: aircraft[i] ? Math.floor(5 + Math.random() * 15) : 0,
    personnel_count: aircraft[i] ? Math.floor(3 + Math.random() * 8) : 0,
    has_ncr: aircraft[i] ? Math.random() > 0.7 : false,
    pending_sign: aircraft[i] ? Math.floor(Math.random() * 4) : 0,
    material_shortage: aircraft[i] ? Math.random() > 0.75 : false,
    planned_end: aircraft[i] ? `2026-06-${String(10 + i * 3).padStart(2, '0')}` : null
  }))
}

export default [
  {
    url: '/api/dashboard/operation',
    method: 'get',
    response: () => {
      const bays = generateBayStatus()
      const activeBays = bays.filter(b => b.status !== 'empty')
      return {
        code: 200,
        data: {
          summary: {
            total_aircraft: activeBays.length,
            personnel_onsite: activeBays.reduce((sum, b) => sum + b.personnel_count, 0),
            workcard_in_progress: activeBays.reduce((sum, b) => sum + (b.workcard_total - b.workcard_done), 0),
            pending_sign: activeBays.reduce((sum, b) => sum + b.pending_sign, 0),
            ncr_open: activeBays.filter(b => b.has_ncr).length,
            material_shortage: activeBays.filter(b => b.material_shortage).length,
            avg_progress: Math.round(activeBays.reduce((sum, b) => sum + b.progress, 0) / (activeBays.length || 1))
          },
          bays,
          alerts: [
            { id: 1, type: 'material_shortage', level: 'danger', message: 'B-9012 缺少 O型密封圈（件号：05-1234-001），影响工卡 WC-2026-0003', time: '10分钟前' },
            { id: 2, type: 'ncr', level: 'warning', message: 'B-1234 存在 2 项 NCR 待整改', time: '35分钟前' },
            { id: 3, type: 'pending_sign', level: 'info', message: '质检待签署积压 4 项，请尽快处理', time: '1小时前' }
          ],
          last_updated: new Date().toISOString().replace('T', ' ').slice(0, 19)
        }
      }
    }
  }
]
```

---

## Task 3: 作业看板大屏页 OperationDashboard.vue

**Files:**
- Create: `frontend/src/pages/mro/dashboard/OperationDashboard.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="operation-dashboard" :class="{ fullscreen: isFullscreen }">
    <!-- 顶部标题栏 -->
    <div class="dashboard-header">
      <div class="header-left">
        <span class="dashboard-title">机库作业看板</span>
        <span class="last-updated text-gray-400 text-sm ml-3">最后更新：{{ data?.last_updated }}</span>
      </div>
      <div class="header-right">
        <el-countdown
          v-if="countdown > 0"
          :value="countdown"
          format="ss"
          prefix="刷新倒计时 "
          suffix="s"
          class="text-gray-400 text-sm mr-4"
        />
        <el-button size="small" @click="fetchData"><el-icon><Refresh /></el-icon>立即刷新</el-button>
        <el-button size="small" @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>{{ isFullscreen ? '退出全屏' : '全屏' }}
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="dashboard-body">
      <!-- 统计卡片行 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon aircraft"><el-icon size="24"><Airplane /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.summary?.total_aircraft ?? '-' }}</div>
            <div class="stat-label">在修飞机</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon personnel"><el-icon size="24"><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.summary?.personnel_onsite ?? '-' }}</div>
            <div class="stat-label">在岗人员</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon progress"><el-icon size="24"><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.summary?.workcard_in_progress ?? '-' }}</div>
            <div class="stat-label">执行中工卡</div>
          </div>
        </div>
        <div class="stat-card" :class="{ alert: data?.summary?.pending_sign > 0 }">
          <div class="stat-icon sign"><el-icon size="24"><EditPen /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.summary?.pending_sign ?? '-' }}</div>
            <div class="stat-label">待质检签署</div>
          </div>
        </div>
        <div class="stat-card" :class="{ alert: data?.summary?.ncr_open > 0 }">
          <div class="stat-icon ncr"><el-icon size="24"><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.summary?.ncr_open ?? '-' }}</div>
            <div class="stat-label">未关闭NCR</div>
          </div>
        </div>
        <div class="stat-card" :class="{ alert: data?.summary?.material_shortage > 0 }">
          <div class="stat-icon material"><el-icon size="24"><Box /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.summary?.material_shortage ?? '-' }}</div>
            <div class="stat-label">缺料告警</div>
          </div>
        </div>
      </div>

      <div class="dashboard-main">
        <!-- 机位网格 -->
        <div class="bays-section">
          <div class="section-title">机位状态</div>
          <div class="bays-grid">
            <div
              v-for="bay in data?.bays"
              :key="bay.bay_id"
              class="bay-card"
              :class="{
                empty: bay.status === 'empty',
                'has-alert': bay.material_shortage || bay.has_ncr
              }"
            >
              <div class="bay-header">
                <span class="bay-id">{{ bay.bay_id }}</span>
                <div class="bay-badges">
                  <el-tooltip v-if="bay.material_shortage" content="缺料告警" placement="top">
                    <el-tag type="danger" size="small">缺料</el-tag>
                  </el-tooltip>
                  <el-tooltip v-if="bay.has_ncr" content="存在未关闭NCR" placement="top">
                    <el-tag type="warning" size="small">NCR</el-tag>
                  </el-tooltip>
                  <el-tooltip v-if="bay.pending_sign > 0" :content="`${bay.pending_sign}项待签署`" placement="top">
                    <el-tag type="info" size="small">待签{{ bay.pending_sign }}</el-tag>
                  </el-tooltip>
                </div>
              </div>

              <div v-if="bay.status !== 'empty'" class="bay-content">
                <div class="aircraft-id">{{ bay.aircraft_id }}</div>
                <div class="check-type">{{ bay.check_type }}</div>
                <el-progress
                  :percentage="bay.progress"
                  :stroke-width="10"
                  :text-inside="true"
                  :status="bay.progress === 100 ? 'success' : bay.material_shortage || bay.has_ncr ? 'exception' : ''"
                  class="bay-progress"
                />
                <div class="bay-meta">
                  <span>工卡 {{ bay.workcard_done }}/{{ bay.workcard_total }}</span>
                  <span>人员 {{ bay.personnel_count }}</span>
                </div>
              </div>
              <div v-else class="bay-empty-label">空位</div>
            </div>
          </div>
        </div>

        <!-- 告警信息 -->
        <div class="alerts-section">
          <div class="section-title">实时告警</div>
          <div class="alerts-list">
            <div
              v-for="alert in data?.alerts"
              :key="alert.id"
              class="alert-item"
              :class="alert.level"
            >
              <el-icon v-if="alert.level === 'danger'" color="#f56c6c"><CircleClose /></el-icon>
              <el-icon v-else-if="alert.level === 'warning'" color="#e6a23c"><Warning /></el-icon>
              <el-icon v-else color="#909399"><InfoFilled /></el-icon>
              <div class="alert-content">
                <div class="alert-message">{{ alert.message }}</div>
                <div class="alert-time">{{ alert.time }}</div>
              </div>
            </div>
            <div v-if="!data?.alerts?.length" class="no-alerts">
              <el-icon color="#67c23a"><CircleCheck /></el-icon>
              <span>暂无告警</span>
            </div>
          </div>

          <!-- 平均进度 -->
          <div class="overall-progress">
            <div class="section-title">总体进度</div>
            <div class="progress-value">{{ data?.summary?.avg_progress ?? 0 }}%</div>
            <el-progress
              :percentage="data?.summary?.avg_progress ?? 0"
              :stroke-width="16"
              :text-inside="true"
              :status="data?.summary?.avg_progress === 100 ? 'success' : ''"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Refresh, FullScreen, Warning, CircleClose, CircleCheck, InfoFilled, User, Document, EditPen, Box } from '@element-plus/icons-vue'
import { getOperationDashboard } from '@/api/dashboard'

const loading = ref(false)
const data = ref(null)
const isFullscreen = ref(false)
const countdown = ref(0)
let refreshTimer = null
let countdownTimer = null

const REFRESH_INTERVAL = 30

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOperationDashboard()
    if (res.code === 200) data.value = res.data
  } finally { loading.value = false }
  startCountdown()
}

const startCountdown = () => {
  clearInterval(countdownTimer)
  countdown.value = Date.now() + REFRESH_INTERVAL * 1000
}

const toggleFullscreen = () => {
  if (!isFullscreen.value) {
    document.documentElement.requestFullscreen?.()
  } else {
    document.exitFullscreen?.()
  }
}

const onFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => {
  fetchData()
  refreshTimer = setInterval(fetchData, REFRESH_INTERVAL * 1000)
  document.addEventListener('fullscreenchange', onFullscreenChange)
})

onUnmounted(() => {
  clearInterval(refreshTimer)
  clearInterval(countdownTimer)
  document.removeEventListener('fullscreenchange', onFullscreenChange)
})
</script>

<style scoped>
.operation-dashboard {
  min-height: 100%;
  background: #f5f7fa;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.operation-dashboard.fullscreen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: #0d1117;
  color: #e6edf3;
  padding: 20px;
  overflow-y: auto;
}
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.dashboard-title { font-size: 20px; font-weight: 700; }
.header-right { display: flex; align-items: center; gap: 8px; }
.stats-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
@media (max-width: 1199px) { .stats-row { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 767px) { .stats-row { grid-template-columns: repeat(2, 1fr); } }
.stat-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  transition: box-shadow 0.2s;
}
.stat-card.alert { border-left: 3px solid #f56c6c; }
.fullscreen .stat-card { background: #161b22; color: #e6edf3; }
.stat-icon { width: 44px; height: 44px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.stat-icon.aircraft { background: #ecf5ff; color: #409eff; }
.stat-icon.personnel { background: #f0f9eb; color: #67c23a; }
.stat-icon.progress { background: #fdf6ec; color: #e6a23c; }
.stat-icon.sign { background: #f0f9eb; color: #67c23a; }
.stat-icon.ncr { background: #fef0f0; color: #f56c6c; }
.stat-icon.material { background: #fef0f0; color: #f56c6c; }
.stat-value { font-size: 24px; font-weight: 700; line-height: 1; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.dashboard-main {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 16px;
  flex: 1;
}
@media (max-width: 1199px) { .dashboard-main { grid-template-columns: 1fr; } }
.section-title { font-weight: 600; font-size: 14px; margin-bottom: 12px; color: #606266; }
.fullscreen .section-title { color: #8b949e; }
.bays-section, .alerts-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.fullscreen .bays-section, .fullscreen .alerts-section { background: #161b22; }
.bays-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
@media (max-width: 1199px) { .bays-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 767px) { .bays-grid { grid-template-columns: repeat(2, 1fr); } }
.bay-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  min-height: 140px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: border-color 0.2s;
}
.bay-card.empty { background: #fafafa; opacity: 0.6; }
.bay-card.has-alert { border-color: #f56c6c; }
.fullscreen .bay-card { border-color: #30363d; background: #0d1117; }
.fullscreen .bay-card.empty { background: #0d1117; }
.bay-header { display: flex; justify-content: space-between; align-items: flex-start; }
.bay-id { font-weight: 700; font-size: 15px; }
.bay-badges { display: flex; flex-wrap: wrap; gap: 3px; }
.aircraft-id { font-size: 16px; font-weight: 600; color: #409eff; }
.check-type { font-size: 12px; color: #909399; }
.bay-progress { margin: 4px 0; }
.bay-meta { display: flex; justify-content: space-between; font-size: 12px; color: #909399; }
.bay-empty-label { flex: 1; display: flex; align-items: center; justify-content: center; color: #c0c4cc; font-size: 14px; }
.alerts-section { display: flex; flex-direction: column; gap: 16px; }
.alerts-list { display: flex; flex-direction: column; gap: 8px; }
.alert-item { display: flex; align-items: flex-start; gap: 8px; padding: 8px; border-radius: 6px; }
.alert-item.danger { background: #fef0f0; }
.alert-item.warning { background: #fdf6ec; }
.alert-item.info { background: #f4f4f5; }
.fullscreen .alert-item.danger { background: rgba(245, 108, 108, 0.1); }
.fullscreen .alert-item.warning { background: rgba(230, 162, 60, 0.1); }
.fullscreen .alert-item.info { background: rgba(144, 147, 153, 0.1); }
.alert-content { flex: 1; }
.alert-message { font-size: 13px; line-height: 1.4; }
.alert-time { font-size: 11px; color: #909399; margin-top: 2px; }
.no-alerts { display: flex; align-items: center; gap: 6px; padding: 12px; color: #67c23a; font-size: 14px; }
.overall-progress { margin-top: auto; }
.progress-value { font-size: 36px; font-weight: 700; color: #409eff; text-align: center; margin-bottom: 8px; }
</style>
```

---

## Task 4: 注册路由

**Files:**
- Modify: `frontend/src/router/routes.js`

- [ ] **Step 1: 追加作业看板路由**

找到 `routes.js`，在所有 `mro/` 路由块之后、`wiki` 之前追加：

```javascript
      {
        path: 'mro/dashboard',
        name: 'OperationDashboardGroup',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '作业看板', requiresAuth: true },
        children: [
          { path: 'operation', name: 'OperationDashboard', component: () => import('@/pages/mro/dashboard/OperationDashboard.vue'), meta: { title: '作业看板', requiresAuth: true, permissions: ['dashboard:operation:view'], breadcrumb: '作业看板' } }
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
cd frontend && git add src/api/dashboard.js src/mock/api/dashboard-operation.js src/pages/mro/dashboard/ src/router/routes.js
git commit -m "feat(mro): add operation dashboard with hangar bay status and real-time alerts

Refs: MRO-012"
```

---

## 自检结果

| 检查项 | 结果 |
|------|------|
| Spec 覆盖 | 机库机位网格视图 ✓、统计指标（在修飞机/在岗人员/工卡/待签署/NCR/缺料）✓、告警列表 ✓、全屏支持 ✓、30秒自动刷新 ✓、倒计时显示 ✓、响应式（平板竖屏列表）✓ |
| 占位符 | 无 TBD/TODO |
| 类型一致性 | `getOperationDashboard()` 返回 `{ summary, bays, alerts, last_updated }` 与页面使用一致 ✓ |
| 全屏清理 | `onUnmounted` 中清理了 timer 和 fullscreenchange 监听 ✓ |
