# 用户操作日志（前端）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 frontend 项目中新增操作日志模块，包含 Mock 数据、API 模块、列表页（带多条件搜索+分页）、详情抽屉、路由及菜单配置，完全遵循项目已有的 Mock-first 约定。

**Architecture:** Mock 文件先行（`src/mock/api/operationLog.js`），API 模块封装（`src/api/operationLog.js`），页面组件（`src/pages/system/OperationLog.vue`）复用项目已有的 el-card + el-table + Pagination 模式，详情通过 el-drawer 展示格式化 JSON。路由和菜单 Mock 同步更新，无新增依赖。

**Tech Stack:** Vue 3 (Composition API + `<script setup>`)、Element Plus、Pinia（`useAuthStore`）、`vite-plugin-mock`、`src/utils/request.js` Axios 封装

**Spec:** `specs/system/007-operation-log.spec.md` (SYS-007)

---

## 文件清单

| 操作 | 文件 | 说明 |
|------|------|------|
| 新建 | `src/mock/api/operationLog.js` | Mock 数据：列表分页+过滤、详情 |
| 新建 | `src/api/operationLog.js` | API 模块：封装列表查询、详情查询 |
| 新建 | `src/pages/system/OperationLog.vue` | 操作日志页面：搜索栏 + 表格 + 详情抽屉 |
| 修改 | `src/router/routes.js` | 在 system 子路由中新增 operation-log 路由 |
| 修改 | `src/mock/api/menu.js` | 在菜单 Mock 数据中新增「操作日志」菜单项 |

---

## Task 1: 创建 Mock 文件

**Files:**
- Create: `src/mock/api/operationLog.js`

Mock 文件必须先于 API 模块和页面存在，这样开发期 `VITE_USE_MOCK=true` 即可验证。

- [ ] **Step 1: 创建 Mock 文件**

创建 `D:\ai_code\ui\frontend\src\mock\api\operationLog.js`，内容如下：

```javascript
const logs = [
  { id: 1,  requestPath: '/api/system/user/page',       requestTime: '2026-05-23 09:00:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"page":1,"pageSize":10}',                          resultStatus: 1 },
  { id: 2,  requestPath: '/api/system/user/create',     requestTime: '2026-05-23 09:05:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"username":"test","realName":"测试用户"}',            resultStatus: 1 },
  { id: 3,  requestPath: '/api/system/user/update',     requestTime: '2026-05-23 09:10:00', userId: 2, userName: '李四', deptId: 2, requestParams: '{"id":5,"status":0}',                                 resultStatus: 1 },
  { id: 4,  requestPath: '/api/system/role/create',     requestTime: '2026-05-23 09:15:00', userId: 2, userName: '李四', deptId: 2, requestParams: '{"name":"测试角色","permissions":["system:user:list"]}', resultStatus: 0 },
  { id: 5,  requestPath: '/api/system/dept/update',     requestTime: '2026-05-23 09:20:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"id":3,"name":"市场部（已更名）"}',                    resultStatus: 1 },
  { id: 6,  requestPath: '/api/system/user/remove',     requestTime: '2026-05-23 10:00:00', userId: 3, userName: '王五', deptId: 3, requestParams: '{"id":9}',                                            resultStatus: 1 },
  { id: 7,  requestPath: '/api/system/menu/create',     requestTime: '2026-05-23 10:05:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"name":"新菜单","type":"C","path":"/system/test"}',   resultStatus: 1 },
  { id: 8,  requestPath: '/api/system/dict/update',     requestTime: '2026-05-23 10:10:00', userId: 2, userName: '李四', deptId: 2, requestParams: '{"id":1,"remark":"已更新备注"}',                       resultStatus: 0 },
  { id: 9,  requestPath: '/api/system/user/reset-password', requestTime: '2026-05-23 10:30:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"id":7}',                                      resultStatus: 1 },
  { id: 10, requestPath: '/api/system/role/update',     requestTime: '2026-05-23 11:00:00', userId: 4, userName: '赵六', deptId: 4, requestParams: '{"id":2,"menuIds":[1,2,3,4,5]}',                      resultStatus: 1 },
  { id: 11, requestPath: '/api/system/user/page',       requestTime: '2026-05-23 11:05:00', userId: 4, userName: '赵六', deptId: 4, requestParams: '{"page":2,"pageSize":10,"realName":"张"}',            resultStatus: 1 },
  { id: 12, requestPath: '/api/system/dept/create',     requestTime: '2026-05-23 11:10:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"name":"新部门","parentId":2}',                       resultStatus: 1 },
  { id: 13, requestPath: '/api/system/menu/remove',     requestTime: '2026-05-23 11:15:00', userId: 2, userName: '李四', deptId: 2, requestParams: '{"id":22}',                                          resultStatus: 0 },
  { id: 14, requestPath: '/api/system/user/update',     requestTime: '2026-05-23 13:00:00', userId: 5, userName: '钱七', deptId: 5, requestParams: '{"id":3,"phone":"13900139000"}',                      resultStatus: 1 },
  { id: 15, requestPath: '/api/system/dict/create',     requestTime: '2026-05-23 13:05:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"dictType":"sys_status","dictLabel":"启用","dictValue":"1"}', resultStatus: 1 },
  { id: 16, requestPath: '/api/system/role/remove',     requestTime: '2026-05-23 13:10:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"id":5}',                                           resultStatus: 1 },
  { id: 17, requestPath: '/api/system/user/page',       requestTime: '2026-05-23 14:00:00', userId: 6, userName: '孙八', deptId: 6, requestParams: '{"page":1,"pageSize":20}',                            resultStatus: 1 },
  { id: 18, requestPath: '/api/system/dept/remove',     requestTime: '2026-05-23 14:05:00', userId: 2, userName: '李四', deptId: 2, requestParams: '{"id":10}',                                          resultStatus: 0 },
  { id: 19, requestPath: '/api/system/menu/update',     requestTime: '2026-05-23 14:10:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"id":3,"sort":5}',                                   resultStatus: 1 },
  { id: 20, requestPath: '/api/system/user/create',     requestTime: '2026-05-23 15:00:00', userId: 3, userName: '王五', deptId: 3, requestParams: '{"username":"newuser","realName":"新用户","deptId":3}', resultStatus: 1 },
  { id: 21, requestPath: '/api/system/user/page',       requestTime: '2026-05-22 09:00:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"page":1,"pageSize":10}',                            resultStatus: 1 },
  { id: 22, requestPath: '/api/system/role/create',     requestTime: '2026-05-22 10:00:00', userId: 2, userName: '李四', deptId: 2, requestParams: '{"name":"运营角色"}',                                  resultStatus: 1 },
  { id: 23, requestPath: '/api/system/dict/update',     requestTime: '2026-05-22 11:00:00', userId: 5, userName: '钱七', deptId: 5, requestParams: '{"id":2,"remark":"备注更新"}',                        resultStatus: 1 },
  { id: 24, requestPath: '/api/system/user/remove',     requestTime: '2026-05-22 14:00:00', userId: 1, userName: '张三', deptId: 1, requestParams: '{"id":13}',                                          resultStatus: 0 },
  { id: 25, requestPath: '/api/system/dept/update',     requestTime: '2026-05-21 09:30:00', userId: 4, userName: '赵六', deptId: 4, requestParams: '{"id":5,"status":0}',                                 resultStatus: 1 },
]

export default [
  {
    url: '/api/system/operation-log',
    method: 'get',
    response: ({ query }) => {
      let list = [...logs]

      if (query.userName) {
        list = list.filter(l => l.userName.includes(query.userName))
      }
      if (query.requestPath) {
        list = list.filter(l => l.requestPath.includes(query.requestPath))
      }
      if (query.resultStatus !== undefined && query.resultStatus !== '') {
        list = list.filter(l => l.resultStatus === Number(query.resultStatus))
      }
      if (query.beginTime) {
        list = list.filter(l => l.requestTime >= query.beginTime)
      }
      if (query.endTime) {
        list = list.filter(l => l.requestTime <= query.endTime)
      }

      const page = Number(query.page) || 1
      const pageSize = Number(query.pageSize) || 20
      const total = list.length
      const data = list.slice((page - 1) * pageSize, page * pageSize)

      return { code: 200, message: '获取成功', data: { list: data, total, page, pageSize } }
    },
  },
  {
    url: '/api/system/operation-log/:id',
    method: 'get',
    response: ({ params }) => {
      const log = logs.find(l => l.id === Number(params.id))
      return log
        ? { code: 200, message: '获取成功', data: log }
        : { code: 404, message: '日志不存在', data: null }
    },
  },
]
```

- [ ] **Step 2: 验证文件已创建**

```bash
ls src/mock/api/operationLog.js
```

期望输出：文件存在，无报错。

---

## Task 2: 创建 API 模块

**Files:**
- Create: `src/api/operationLog.js`

- [ ] **Step 1: 创建 API 模块**

创建 `D:\ai_code\ui\frontend\src\api\operationLog.js`，内容如下：

```javascript
import request from '@/utils/request'

export const getOperationLogList = (params) => {
  return request.get('/api/system/operation-log', { params })
}

export const getOperationLogDetail = (id) => {
  return request.get(`/api/system/operation-log/${id}`)
}
```

- [ ] **Step 2: 验证文件已创建**

```bash
ls src/api/operationLog.js
```

期望输出：文件存在，无报错。

---

## Task 3: 创建操作日志页面

**Files:**
- Create: `src/pages/system/OperationLog.vue`

此页面包含：搜索栏（操作人、时间范围、路径、结果）、只读 el-table、分页、详情 el-drawer（格式化 JSON）。无新增/编辑/删除功能。

- [ ] **Step 1: 创建页面文件**

创建 `D:\ai_code\ui\frontend\src\pages\system\OperationLog.vue`，内容如下：

```vue
<template>
  <div class="operation-log">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="操作人">
          <el-input
            v-model="queryParams.userName"
            placeholder="请输入操作人姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="请求路径">
          <el-input
            v-model="queryParams.requestPath"
            placeholder="请输入路径关键词"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="操作结果">
          <el-select v-model="queryParams.resultStatus" placeholder="请选择结果" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleTimeRangeChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">操作日志列表</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="userName" label="操作人" min-width="90" show-overflow-tooltip />
        <el-table-column prop="requestPath" label="请求路径" min-width="220" show-overflow-tooltip />
        <el-table-column prop="requestTime" label="请求时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="resultStatus" label="操作结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.resultStatus === 1 ? 'success' : 'danger'" effect="light">
              {{ row.resultStatus === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        :total="total"
        :page="queryParams.page"
        :limit="queryParams.pageSize"
        @update:page="queryParams.page = $event"
        @update:limit="queryParams.pageSize = $event"
        @change="fetchData"
      />
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="操作日志详情"
      size="500px"
      :destroy-on-close="true"
    >
      <template v-if="currentLog">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="操作人">{{ currentLog.userName }}</el-descriptions-item>
          <el-descriptions-item label="请求路径">{{ currentLog.requestPath }}</el-descriptions-item>
          <el-descriptions-item label="请求时间">{{ currentLog.requestTime }}</el-descriptions-item>
          <el-descriptions-item label="操作结果">
            <el-tag :type="currentLog.resultStatus === 1 ? 'success' : 'danger'" effect="light">
              {{ currentLog.resultStatus === 1 ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="params-section">
          <div class="params-label">请求参数</div>
          <pre class="params-content">{{ formatJson(currentLog.requestParams) }}</pre>
        </div>
      </template>
      <div v-else class="loading-placeholder">
        <el-skeleton :rows="5" animated />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import { getOperationLogList, getOperationLogDetail } from '@/api/operationLog'

// ======================== 搜索相关 ========================

const timeRange = ref(null)

const queryParams = reactive({
  userName: '',
  requestPath: '',
  resultStatus: undefined,
  beginTime: '',
  endTime: '',
  page: 1,
  pageSize: 20,
})

const handleTimeRangeChange = (val) => {
  if (val) {
    queryParams.beginTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.beginTime = ''
    queryParams.endTime = ''
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

const handleReset = () => {
  timeRange.value = null
  queryParams.userName = ''
  queryParams.requestPath = ''
  queryParams.resultStatus = undefined
  queryParams.beginTime = ''
  queryParams.endTime = ''
  queryParams.page = 1
  queryParams.pageSize = 20
  fetchData()
}

// ======================== 表格相关 ========================

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOperationLogList(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '获取操作日志失败')
  } finally {
    loading.value = false
  }
}

// ======================== 详情抽屉 ========================

const drawerVisible = ref(false)
const currentLog = ref(null)

const handleViewDetail = async (row) => {
  currentLog.value = null
  drawerVisible.value = true
  try {
    const res = await getOperationLogDetail(row.id)
    currentLog.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
    drawerVisible.value = false
  }
}

const formatJson = (jsonStr) => {
  if (!jsonStr) return '（无参数）'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

// ======================== 初始化 ========================

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.operation-log {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.search-card :deep(.el-card__body) {
  padding: 18px 20px 0;
}

.search-card :deep(.el-form-item) {
  margin-bottom: 18px;
}

.search-card :deep(.el-input) {
  width: 200px;
}

.search-card :deep(.el-select) {
  width: 160px;
}

.search-card :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.search-card :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

.table-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.table-card :deep(.el-card__header) {
  border-bottom: 1px solid #e4e7eb;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.table-card :deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.table-card :deep(.el-table .el-button--primary) {
  color: #667eea;
}

.table-card :deep(.el-table .el-button--primary:hover) {
  color: #5568d3;
}

.params-section {
  margin-top: 20px;
}

.params-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.params-content {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  font-size: 13px;
  font-family: 'Courier New', Courier, monospace;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 400px;
  overflow-y: auto;
  margin: 0;
}

.loading-placeholder {
  padding: 20px;
}
</style>
```

- [ ] **Step 2: 验证文件已创建**

```bash
ls src/pages/system/OperationLog.vue
```

期望输出：文件存在，无报错。

---

## Task 4: 注册路由

**Files:**
- Modify: `src/router/routes.js`

在 `system` children 数组末尾新增操作日志路由。

- [ ] **Step 1: 修改 routes.js**

在 `src/router/routes.js` 文件中，找到 `dict` 路由条目（当前最后一个 system 子路由）：

```javascript
          {
            path: 'dict',
            name: 'DictManage',
            component: () => import('@/pages/system/DictManage.vue'),
            meta: {
              title: '字典管理',
              requiresAuth: true,
              permissions: ['system:dict:list'],
              breadcrumb: '字典管理'
            }
          }
```

在其后追加：

```javascript
          {
            path: 'operation-log',
            name: 'OperationLog',
            component: () => import('@/pages/system/OperationLog.vue'),
            meta: {
              title: '操作日志',
              requiresAuth: true,
              permissions: ['log:list'],
              breadcrumb: '操作日志'
            }
          }
```

- [ ] **Step 2: 确认修改正确**

```bash
grep -n "operation-log\|OperationLog" src/router/routes.js
```

期望输出：至少两行，分别是 `path: 'operation-log'` 和 `name: 'OperationLog'`。

---

## Task 5: 更新菜单 Mock 数据

**Files:**
- Modify: `src/mock/api/menu.js`

在菜单 Mock 数据中新增「操作日志」菜单项（`type: 'C'`，挂在「系统管理」目录 `parentId: 2` 下），使侧边栏可以渲染出该菜单项。

- [ ] **Step 1: 查看当前菜单 Mock 数据末尾**

确认当前最后一个菜单条目的 id，以便确定新条目的 id（当前最大 id 为 20，`_nextId = 100`，新条目用 id: 21）。

```bash
grep "id:" src/mock/api/menu.js | head -25
```

- [ ] **Step 2: 修改 menu.js**

在 `src/mock/api/menu.js` 中，找到如下行（id: 20 的字典新增权限条目，是最后一条 `const menus` 数据）：

```javascript
  { id: 20, parentId: 19, name: '字典新增',type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:dict:add',   sort: 1, status: 1 },
]
```

将其改为（在末尾追加操作日志菜单项，然后闭合数组）：

```javascript
  { id: 20, parentId: 19, name: '字典新增',type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:dict:add',   sort: 1, status: 1 },
  { id: 21, parentId: 2,  name: '操作日志', type: 'C', icon: 'Document',      path: '/system/operation-log', component: 'system/OperationLog', permission: 'log:list',         sort: 6, status: 1 },
]
```

- [ ] **Step 3: 确认修改正确**

```bash
grep -n "操作日志\|operation-log" src/mock/api/menu.js
```

期望输出：至少一行包含「操作日志」。

---

## Task 6: 构建验证

- [ ] **Step 1: 运行构建**

```bash
npm run build
```

期望输出：`built in X.XXs`，无 ERROR。

- [ ] **Step 2: 如有报错，检查常见问题**

- `View` 图标未导入：OperationLog.vue 使用了 `<el-icon><View /></el-icon>`，Element Plus 图标默认全局注册，无需手动导入（与项目其他页面一致）。
- Mock URL 路径参数：`/api/system/operation-log/:id` 用于 `vite-plugin-mock` 的参数匹配，如遇问题可将 detail 接口改为 query 参数形式：`url: '/api/system/operation-log/detail'` + `response: ({ query }) => logs.find(l => l.id === Number(query.id))`，并同步更新 `src/api/operationLog.js` 的 `getOperationLogDetail` 为 `request.get('/api/system/operation-log/detail', { params: { id } })`。

---

## 自检清单（对照 SYS-007 验收标准）

| 验收条目 | 对应 Task |
|---------|-----------|
| Mock 数据与接口契约 Spec 7 节一致 | Task 1 |
| 列表分页查询正确 | Task 1 + Task 3 |
| 操作人姓名模糊查询 | Task 1（`includes`）+ Task 3（`queryParams.userName`） |
| 时间范围查询 | Task 1（`>=` / `<=`）+ Task 3（`el-date-picker`） |
| 请求路径模糊查询 | Task 1 + Task 3 |
| 操作结果过滤 | Task 1 + Task 3 |
| 详情展示格式化 JSON | Task 3（`formatJson()`）|
| 日志只读，无删除/修改 | Task 3（模板无删除按钮）|
| 权限标识 `log:list` | Task 4（路由 meta）+ Task 5（菜单 permission）|
| 路由可访问 | Task 4 |
| 菜单可见 | Task 5 |
