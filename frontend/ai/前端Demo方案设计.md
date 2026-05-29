。。# 前端 Demo 方案设计

## 1. 概述

### 1.1 目标
在后端开发完成前，通过前端 Mock 数据和动态演示页面，让您能够看到完整的系统功能演示，及时验证需求和调整设计。

### 1.2 核心思路
- **Mock 数据**：使用 Mock.js 模拟后端 API 返回的数据
- **动态演示**：创建演示页面展示各个功能模块
- **无缝切换**：后端开发完成后，只需替换 Mock 数据为真实 API 调用，无需修改前端代码

### 1.3 优势
- ✅ 快速看到系统效果
- ✅ 及时发现需求问题
- ✅ 前后端并行开发
- ✅ 减少集成时间
- ✅ 提高开发效率

---

## 2. Mock 数据方案

### 2.1 Mock.js 集成

#### 安装依赖
```bash
npm install mockjs
```

#### 项目结构
```
src/
├── mock/                           # Mock 数据目录
│   ├── index.js                    # Mock 配置入口
│   ├── modules/
│   │   ├── auth.js                 # 认证相关 Mock
│   │   ├── user.js                 # 用户相关 Mock
│   │   ├── dept.js                 # 部门相关 Mock
│   │   ├── role.js                 # 角色相关 Mock
│   │   ├── menu.js                 # 菜单相关 Mock
│   │   └── dict.js                 # 字典相关 Mock
│   └── data/
│       ├── users.json              # 用户数据
│       ├── depts.json              # 部门数据
│       ├── roles.json              # 角色数据
│       ├── menus.json              # 菜单数据
│       └── dicts.json              # 字典数据
```

### 2.2 Mock 数据示例

#### auth.js - 认证 Mock
```javascript
// 模拟登录接口
Mock.mock('/api/auth/login', 'post', (options) => {
  const { username, password } = JSON.parse(options.body)
  
  // 模拟用户验证
  if (username === 'admin' && password === 'admin123') {
    return {
      code: 200,
      message: '登录成功',
      data: {
        token: 'mock-jwt-token-' + Date.now(),
        refreshToken: 'mock-refresh-token-' + Date.now(),
        user: {
          id: 1,
          username: 'admin',
          realName: '管理员',
          avatar: 'https://via.placeholder.com/150',
          dept: { id: 1, name: 'A集团' },
          roles: ['admin']
        }
      }
    }
  }
  
  return {
    code: 401,
    message: '用户名或密码错误'
  }
})

// 模拟获取用户信息接口
Mock.mock('/api/auth/user-info', 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: {
      id: 1,
      username: 'admin',
      realName: '管理员',
      avatar: 'https://via.placeholder.com/150',
      dept: { id: 1, name: 'A集团' },
      roles: ['admin'],
      permissions: ['*:*:*']
    }
  }
})

// 模拟登出接口
Mock.mock('/api/auth/logout', 'post', () => {
  return {
    code: 200,
    message: '登出成功'
  }
})
```

#### user.js - 用户 Mock
```javascript
// 模拟用户列表接口
Mock.mock('/api/system/users', 'get', (options) => {
  const url = new URL('http://localhost' + options.url)
  const page = parseInt(url.searchParams.get('page')) || 1
  const pageSize = parseInt(url.searchParams.get('pageSize')) || 10
  
  const users = Mock.mock({
    'list|100': [
      {
        'id|+1': 1,
        username: '@word',
        realName: '@cname',
        'gender|1': ['男', '女', '未知'],
        phone: /^1[3-9]\d{9}$/,
        email: '@email',
        'status|1': [0, 1],
        deptId: '@integer(1, 10)',
        createTime: '@datetime'
      }
    ]
  })
  
  const start = (page - 1) * pageSize
  const end = start + pageSize
  
  return {
    code: 200,
    message: '获取成功',
    data: {
      list: users.list.slice(start, end),
      total: users.list.length,
      page,
      pageSize
    }
  }
})

// 模拟创建用户接口
Mock.mock('/api/system/users', 'post', (options) => {
  return {
    code: 200,
    message: '创建成功',
    data: {
      id: Mock.Random.guid(),
      ...JSON.parse(options.body)
    }
  }
})

// 模拟更新用户接口
Mock.mock(/\/api\/system\/users\/\d+/, 'put', (options) => {
  return {
    code: 200,
    message: '更新成功',
    data: JSON.parse(options.body)
  }
})

// 模拟删除用户接口
Mock.mock(/\/api\/system\/users\/\d+/, 'delete', () => {
  return {
    code: 200,
    message: '删除成功'
  }
})
```

#### dept.js - 部门 Mock
```javascript
// 模拟部门树接口
Mock.mock('/api/system/depts/tree', 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: [
      {
        id: 1,
        name: 'A集团',
        parentId: 0,
        children: [
          {
            id: 2,
            name: 'B1公司',
            parentId: 1,
            children: [
              { id: 4, name: 'C1分公司', parentId: 2, children: [] },
              { id: 5, name: 'C2分公司', parentId: 2, children: [] }
            ]
          },
          {
            id: 3,
            name: 'B2公司',
            parentId: 1,
            children: [
              { id: 6, name: 'C3分公司', parentId: 3, children: [] }
            ]
          }
        ]
      }
    ]
  }
})

// 模拟部门列表接口
Mock.mock('/api/system/depts', 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: [
      { id: 1, name: 'A集团', parentId: 0, leader: '张三', phone: '13800138000' },
      { id: 2, name: 'B1公司', parentId: 1, leader: '李四', phone: '13800138001' },
      { id: 3, name: 'B2公司', parentId: 1, leader: '王五', phone: '13800138002' }
    ]
  }
})
```

#### role.js - 角色 Mock
```javascript
// 模拟角色列表接口
Mock.mock('/api/system/roles', 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: [
      {
        id: 1,
        name: '超级管理员',
        key: 'admin',
        dataScope: 1,
        status: 1,
        createTime: '@datetime'
      },
      {
        id: 2,
        name: '部门经理',
        key: 'manager',
        dataScope: 3,
        status: 1,
        createTime: '@datetime'
      },
      {
        id: 3,
        name: '普通员工',
        key: 'user',
        dataScope: 4,
        status: 1,
        createTime: '@datetime'
      }
    ]
  }
})
```

#### menu.js - 菜单 Mock
```javascript
// 模拟获取用户菜单接口
Mock.mock('/api/system/menus', 'get', () => {
  return {
    code: 200,
    message: '获取成功',
    data: [
      {
        id: 1,
        name: '系统管理',
        path: '/system',
        component: null,
        type: 'M',
        icon: 'setting',
        children: [
          {
            id: 11,
            name: '部门管理',
            path: '/system/dept',
            component: 'system/DeptManage',
            type: 'C',
            icon: 'organization',
            perms: 'dept:list'
          },
          {
            id: 12,
            name: '用户管理',
            path: '/system/user',
            component: 'system/UserManage',
            type: 'C',
            icon: 'user',
            perms: 'user:list'
          },
          {
            id: 13,
            name: '角色管理',
            path: '/system/role',
            component: 'system/RoleManage',
            type: 'C',
            icon: 'role',
            perms: 'role:list'
          }
        ]
      },
      {
        id: 2,
        name: '仪表板',
        path: '/dashboard',
        component: 'Dashboard',
        type: 'C',
        icon: 'dashboard'
      }
    ]
  }
})
```

### 2.3 Mock 配置入口（mock/index.js）

```javascript
import Mock from 'mockjs'

// 导入所有 Mock 模块
import './modules/auth'
import './modules/user'
import './modules/dept'
import './modules/role'
import './modules/menu'

// 配置 Mock
Mock.setup({
  timeout: '200-600'  // 模拟网络延迟
})

export default Mock
```

### 2.4 在 main.js 中引入 Mock

```javascript
// 仅在开发环境使用 Mock
if (import.meta.env.DEV) {
  import('./mock')
}
```

---

## 3. 动态演示页面方案

### 3.1 演示页面结构

#### Demo.vue - 主演示页面
```
演示页面
├── 功能导航（左侧菜单）
│   ├── 登录演示
│   ├── 部门管理演示
│   ├── 用户管理演示
│   ├── 角色管理演示
│   ├── 菜单管理演示
│   └── 权限演示
├── 演示内容区域（右侧）
│   ├── 功能说明
│   ├── 交互演示
│   ├── 数据展示
│   └── 操作按钮
└── 底部信息
    ├── 当前演示功能
    ├── 数据统计
    └── 操作日志
```

### 3.2 演示页面功能

#### 3.2.1 登录演示
- 显示登录表单
- 支持多个测试账号
- 显示登录流程
- 显示 Token 信息

**测试账号**：
```
账号1：admin / admin123（超级管理员）
账号2：manager / manager123（部门经理）
账号3：user / user123（普通员工）
```

#### 3.2.2 部门管理演示
- 显示部门树形结构
- 支持展开/收缩
- 支持搜索部门
- 显示部门详情
- 演示增删改查操作

#### 3.2.3 用户管理演示
- 显示用户列表（分页）
- 支持搜索和筛选
- 显示用户详情
- 演示增删改查操作
- 演示批量操作

#### 3.2.4 角色管理演示
- 显示角色列表
- 显示角色权限
- 演示权限分配
- 演示数据权限配置

#### 3.2.5 菜单管理演示
- 显示菜单树形结构
- 显示菜单权限
- 演示菜单权限分配

#### 3.2.6 权限演示
- 显示不同角色的菜单差异
- 演示权限检查
- 显示数据权限过滤效果

### 3.3 演示页面实现方式

#### 方式一：独立演示页面（推荐）
```
src/pages/
├── Demo.vue                    # 演示主页面
├── demo/
│   ├── LoginDemo.vue           # 登录演示
│   ├── DeptDemo.vue            # 部门管理演示
│   ├── UserDemo.vue            # 用户管理演示
│   ├── RoleDemo.vue            # 角色管理演示
│   ├── MenuDemo.vue            # 菜单管理演示
│   └── PermissionDemo.vue      # 权限演示
```

#### 方式二：功能页面 + 演示模式
在实际功能页面中添加演示模式开关：
```javascript
// 在页面中添加演示模式
const isDemoMode = ref(true)

// 根据模式选择数据源
const fetchData = () => {
  if (isDemoMode.value) {
    // 使用 Mock 数据
    return mockData
  } else {
    // 调用真实 API
    return api.getData()
  }
}
```

### 3.4 演示数据生成

#### 使用 Mock.js 生成大量测试数据
```javascript
// 生成 100 个用户
const users = Mock.mock({
  'list|100': [
    {
      'id|+1': 1,
      username: '@word',
      realName: '@cname',
      'gender|1': ['男', '女'],
      phone: /^1[3-9]\d{9}$/,
      email: '@email',
      'status|1': [0, 1],
      createTime: '@datetime'
    }
  ]
})

// 生成树形部门结构
const depts = Mock.mock({
  'list|5': [
    {
      'id|+1': 1,
      name: '@word',
      'children|3': [
        {
          'id|+1': 100,
          name: '@word'
        }
      ]
    }
  ]
})
```

---

## 4. 前端开发流程

### 4.1 开发阶段（使用 Mock 数据）

```
第一阶段：前端开发
├── 第1周：项目初始化 + 登录页面
│   ├── 项目结构搭建
│   ├── 登录页面开发
│   ├── 路由配置
│   ├── 状态管理
│   └── Mock 认证接口
│
├── 第2周：主页面布局 + 演示页面
│   ├── 主页面布局开发
│   ├── 菜单栏开发
│   ├── 演示页面开发
│   ├── Mock 菜单接口
│   └── 功能演示
│
├── 第3周：各功能模块页面
│   ├── 部门管理页面
│   ├── 用户管理页面
│   ├── 角色管理页面
│   ├── 菜单管理页面
│   └── Mock 各模块接口
│
└── 第4周：完善和优化
    ├── 响应式设计
    ├── 性能优化
    ├── 代码审查
    └── 文档完善
```

### 4.2 集成阶段（替换为真实 API）

```
第二阶段：后端开发 + 前端集成
├── 后端开发认证接口
├── 前端替换 Mock 为真实 API
├── 集成测试
├── Bug 修复
└── 性能优化
```

---

## 5. Mock 数据到真实 API 的切换

### 5.1 切换方式

#### 方式一：环境变量控制
```javascript
// .env.development
VITE_USE_MOCK=true

// .env.production
VITE_USE_MOCK=false

// main.js
if (import.meta.env.VITE_USE_MOCK === 'true') {
  import('./mock')
}
```

#### 方式二：条件判断
```javascript
// utils/request.js
const useRealAPI = import.meta.env.PROD

if (!useRealAPI) {
  import('../mock')
}
```

### 5.2 无缝切换

由于前端代码使用统一的 API 调用方式（通过 api/ 目录的函数），只需修改 API 函数的实现，无需修改页面代码：

```javascript
// api/user.js - 开发阶段（使用 Mock）
export const getUsers = (params) => {
  return request.get('/api/system/users', { params })
}

// 后端开发完成后，无需修改此代码
// 只需确保后端 API 返回相同的数据格式
```

---

## 6. 演示场景设计

### 6.1 场景一：登录流程演示

**步骤**：
1. 显示登录页面
2. 输入测试账号（admin / admin123）
3. 点击登录
4. 显示登录过程（加载动画）
5. 登录成功后跳转到首页
6. 显示用户信息和菜单

**预期效果**：
- 完整的登录流程演示
- 展示 Token 存储
- 展示路由守卫工作

### 6.2 场景二：权限演示

**步骤**：
1. 使用不同角色登录
2. 显示不同角色的菜单差异
3. 演示权限检查
4. 显示无权限提示

**预期效果**：
- 展示 RBAC 权限模型
- 展示菜单权限控制
- 展示按钮权限控制

### 6.3 场景三：数据权限演示

**步骤**：
1. 使用不同数据权限的角色登录
2. 查看用户列表
3. 显示数据过滤效果
4. 切换角色查看数据变化

**预期效果**：
- 展示 5 种数据权限类型
- 展示数据过滤效果
- 展示权限宽松度排序

### 6.4 场景四：CRUD 操作演示

**步骤**：
1. 显示列表页面
2. 演示新增操作
3. 演示编辑操作
4. 演示删除操作
5. 演示搜索和筛选

**预期效果**：
- 完整的 CRUD 流程
- 表单验证
- 操作反馈

---

## 7. 演示页面交互设计

### 7.1 演示控制面板

```
┌─────────────────────────────────────────┐
│ 演示控制面板                             │
├─────────────────────────────────────────┤
│ 当前演示：用户管理                       │
│ 当前角色：超级管理员                     │
│ 当前用户：admin                          │
│                                         │
│ [重置演示] [切换角色] [查看日志]        │
│ [导出数据] [导入数据] [清空缓存]        │
└─────────────────────────────────────────┘
```

### 7.2 演示操作日志

```
操作日志：
├── 14:30:25 - 用户登录 (admin)
├── 14:30:30 - 查看用户列表 (10 条)
├── 14:30:45 - 新增用户 (张三)
├── 14:31:00 - 编辑用户 (李四)
├── 14:31:15 - 删除用户 (王五)
└── 14:31:30 - 搜索用户 (关键词: 张)
```

### 7.3 演示数据统计

```
数据统计：
├── 总用户数：100
├── 总部门数：15
├── 总角色数：5
├── 总菜单数：20
└── 总权限数：50
```

---

## 8. 演示页面路由配置

```javascript
// router/routes.js
const demoRoutes = [
  {
    path: '/demo',
    component: () => import('@/pages/Demo.vue'),
    meta: { title: '功能演示' },
    children: [
      {
        path: 'login',
        component: () => import('@/pages/demo/LoginDemo.vue'),
        meta: { title: '登录演示' }
      },
      {
        path: 'dept',
        component: () => import('@/pages/demo/DeptDemo.vue'),
        meta: { title: '部门管理演示' }
      },
      {
        path: 'user',
        component: () => import('@/pages/demo/UserDemo.vue'),
        meta: { title: '用户管理演示' }
      },
      {
        path: 'role',
        component: () => import('@/pages/demo/RoleDemo.vue'),
        meta: { title: '角色管理演示' }
      },
      {
        path: 'menu',
        component: () => import('@/pages/demo/MenuDemo.vue'),
        meta: { title: '菜单管理演示' }
      },
      {
        path: 'permission',
        component: () => import('@/pages/demo/PermissionDemo.vue'),
        meta: { title: '权限演示' }
      }
    ]
  }
]
```

---

## 9. 演示页面访问方式

### 9.1 直接访问
```
http://localhost:5173/demo
```

### 9.2 从首页导航
在首页添加"功能演示"链接，方便快速访问

### 9.3 演示模式开关
在应用设置中添加"演示模式"开关，可以随时切换

---

## 10. 演示数据管理

### 10.1 导出演示数据
```javascript
// 导出当前演示数据为 JSON 文件
const exportDemoData = () => {
  const data = {
    users: mockUsers,
    depts: mockDepts,
    roles: mockRoles,
    menus: mockMenus,
    exportTime: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'demo-data.json'
  a.click()
}
```

### 10.2 导入演示数据
```javascript
// 从 JSON 文件导入演示数据
const importDemoData = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    const data = JSON.parse(e.target.result)
    // 更新 Mock 数据
    mockUsers = data.users
    mockDepts = data.depts
    // ...
  }
  reader.readAsText(file)
}
```

---

## 11. 演示页面性能优化

### 11.1 虚拟滚动
对于大列表（100+ 条数据），使用虚拟滚动提高性能

### 11.2 数据缓存
缓存演示数据，避免重复生成

### 11.3 懒加载
演示页面使用路由懒加载

### 11.4 分页
列表数据使用分页显示

---

## 12. 演示页面文档

### 12.1 演示指南
创建详细的演示指南，说明：
- 如何访问演示页面
- 各个演示场景的操作步骤
- 预期效果
- 常见问题

### 12.2 测试账号
```
账号1：admin / admin123（超级管理员）
  - 权限：全部数据
  - 菜单：所有菜单
  - 可操作：所有功能

账号2：manager / manager123（部门经理）
  - 权限：本部门及子部门
  - 菜单：部分菜单
  - 可操作：部分功能

账号3：user / user123（普通员工）
  - 权限：本人数据
  - 菜单：最少菜单
  - 可操作：最少功能
```

---

## 13. 从演示到生产的迁移

### 13.1 迁移检查清单

- [ ] 后端 API 开发完成
- [ ] API 文档完成
- [ ] 前端 API 调用代码更新
- [ ] Mock 数据移除或禁用
- [ ] 集成测试通过
- [ ] 性能测试通过
- [ ] 安全测试通过
- [ ] 用户验收测试通过

### 13.2 迁移步骤

1. **禁用 Mock 数据**
   ```javascript
   // 在 .env.production 中设置
   VITE_USE_MOCK=false
   ```

2. **更新 API 调用**
   - 确保所有 API 调用指向真实后端
   - 验证 API 返回数据格式一致

3. **测试和验证**
   - 功能测试
   - 性能测试
   - 安全测试

4. **部署**
   - 构建生产版本
   - 部署到服务器

---

## 14. 总结

### 14.1 Demo 方案的优势

✅ **快速验证**：无需等待后端开发完成，快速看到系统效果
✅ **及时反馈**：发现需求问题，及时调整
✅ **并行开发**：前后端可以并行开发，提高效率
✅ **无缝切换**：后端完成后，无需修改前端代码
✅ **完整演示**：展示所有功能和交互效果
✅ **测试友好**：方便进行功能测试和演示

### 14.2 建议

1. **优先开发核心功能**：登录、菜单、权限
2. **创建完整的演示场景**：展示各个功能模块
3. **收集反馈**：通过演示收集用户反馈
4. **及时调整**：根据反馈调整需求和设计
5. **准备迁移**：提前准备从 Mock 到真实 API 的迁移

---

## 15. 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|--------|
| 1.0 | 2026-05-21 | 初版设计文档，包含 Mock 方案、演示页面、迁移指南 |
