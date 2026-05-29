import specsData from '../data/wiki-specs.json'
import codeMappingData from '../data/wiki-code-mapping.json'
import apiDocsData from '../data/wiki-api-docs.json'

const AI_RESPONSES = [
  {
    keywords: ['权限', '403', '禁止访问', '无权'],
    answer: '## 权限校验失败排查\n\n1. **检查用户角色权限**：确认当前用户是否拥有目标操作的权限码（如 `system:user:list`）\n2. **检查路由守卫**：`src/router/guards.js` 中 `beforeEachGuard` 校验 `meta.permissions`\n3. **检查权限指令**：按钮级别使用 `v-permission` 指令控制显示\n4. **超级管理员**：`*:*:*` 权限码跳过所有检查\n\n> 参考 SYS-006 数据权限规格了解完整权限模型',
    relatedSpecs: ['SYS-006', 'SYS-003', 'AUTH-001'],
    codeFiles: ['src/utils/permission.js', 'src/router/guards.js', 'src/mock/api/menu.js'],
  },
  {
    keywords: ['登录', '401', 'token', 'JWT', '认证', '过期'],
    answer: '## 登录/认证问题排查\n\n1. **Token 过期**：检查 `src/utils/request.js` 响应拦截器，401 会触发 `handleTokenExpired` 清除 token 并跳转登录页\n2. **Token 格式**：请求头需要 `Authorization: Bearer <token>` 格式\n3. **刷新机制**：`POST /api/auth/refresh-token` 用 refreshToken 换新 token\n4. **Mock 模式**：token 内容用于推断用户身份（admin/manager/user）\n\n> 参考 AUTH-001 登录与JWT鉴权规格',
    relatedSpecs: ['AUTH-001'],
    codeFiles: ['src/utils/request.js', 'src/utils/storage.js', 'src/store/modules/auth.js', 'src/api/auth.js'],
  },
  {
    keywords: ['工卡', 'workcard', '签放', '步骤'],
    answer: '## 电子工卡问题排查\n\n1. **工卡状态流转**：draft → issued → in_progress → completed\n2. **步骤完成**：`PUT /api/workcards/:id/steps/:stepId/complete`\n3. **签署与区块链**：`POST /api/workcards/:id/sign` 返回 `blockchain_hash`\n4. **工卡告警**：`GET /api/workcards/alerts` 返回即将到期工卡\n\n> 参考 MRO-008 无纸化电子工卡规格',
    relatedSpecs: ['MRO-008'],
    codeFiles: ['src/pages/workcard/WorkcardList.vue', 'src/api/workcard.js', 'src/mock/api/workcard.js'],
  },
  {
    keywords: ['主题', 'theme', '颜色', '配色', '样式'],
    answer: '## 主题/样式问题排查\n\n1. **主题加载**：`Layout.vue` 的 `onMounted` 调用 `themeStore.loadTheme()` → `GET /api/system/theme`\n2. **CSS 变量**：`applyTheme()` 写入 `:root` 的 `--el-color-primary` 等 Element Plus 变量\n3. **可用主题**：internet / finance / medical / education / manufacturing / power / aerospace\n4. **切换接口**：`PUT /api/system/theme` body `{ theme_code: "aerospace" }`\n\n> 参考 SYS-008 主题配置规格',
    relatedSpecs: ['SYS-008'],
    codeFiles: ['src/utils/theme.js', 'src/store/modules/theme.js', 'src/mock/api/theme.js'],
  },
  {
    keywords: ['菜单', 'menu', '侧栏', 'sidebar', '导航'],
    answer: '## 菜单/导航问题排查\n\n1. **菜单来源**：`Sidebar.vue` 调用 `getUserMenus()` → `GET /api/system/menu/user`\n2. **权限过滤**：`buildUserTree` 根据用户 permissions 过滤可见菜单\n3. **菜单类型**：M=目录 C=菜单项 F=按钮权限\n4. **空目录隐藏**：type=M 且无子节点的目录自动过滤\n\n> 参考 SYS-004 菜单管理规格',
    relatedSpecs: ['SYS-004'],
    codeFiles: ['src/components/Sidebar.vue', 'src/mock/api/menu.js', 'src/api/menu.js'],
  },
  {
    keywords: ['健康', 'health', '预警', '故障', '监控'],
    answer: '## 健康管理/故障问题排查\n\n1. **健康态势**：`/mro/health` 页面展示机队整体健康状态\n2. **预警管理**：`/mro/health/alerts` 管理告警阈值和通知\n3. **故障统计**：`/mro/health/statistics` 按 ATA 章节统计故障分布\n4. **飞机详情**：`/mro/health/aircraft/:id` 查看单机健康档案\n\n> 参考 MRO-001 飞机健康管理与预测性维护规格',
    relatedSpecs: ['MRO-001'],
    codeFiles: ['src/pages/health/Dashboard.vue', 'src/api/health.js', 'src/mock/api/health.js'],
  },
  {
    keywords: ['AR', '协作', '远程', '巡检', '视频'],
    answer: '## AR协作问题排查\n\n1. **巡检任务**：`/mro/ar` 创建和管理 AR 巡检任务\n2. **远程协作**：`/mro/ar/sessions` 发起远程音视频协作会话\n3. **影像档案**：`/mro/ar/archives` 查看历史录像和截图\n\n> 参考 MRO-002 AR远程协作规格',
    relatedSpecs: ['MRO-002'],
    codeFiles: ['src/pages/ar/SessionList.vue', 'src/api/ar.js', 'src/mock/api/ar.js'],
  },
]

const DEFAULT_RESPONSE = {
  answer: '## 未找到精确匹配\n\n建议尝试以下方式：\n1. 使用更具体的关键词描述问题\n2. 在「规格文档」中按 domain 浏览相关模块\n3. 在「代码映射」中查找对应文件\n\n常见关键词：权限、登录、工卡、主题、菜单、健康、AR、排故',
  relatedSpecs: [],
  codeFiles: [],
}

function matchAiResponse(question) {
  const q = question.toLowerCase()
  for (const r of AI_RESPONSES) {
    if (r.keywords.some(kw => q.includes(kw))) return r
  }
  return DEFAULT_RESPONSE
}

export default [
  {
    url: '/api/wiki/specs',
    method: 'get',
    response: ({ query }) => {
      let list = [...specsData]
      const { q, domain, status, type } = query || {}
      if (q) {
        const kw = q.toLowerCase()
        list = list.filter(
          s =>
            (s.title && s.title.toLowerCase().includes(kw)) ||
            (s.id && s.id.toLowerCase().includes(kw)) ||
            (s.body && s.body.toLowerCase().includes(kw)),
        )
      }
      if (domain) list = list.filter(s => s.domain === domain)
      if (status) list = list.filter(s => s.status === status)
      if (type) list = list.filter(s => s.type === type)

      const tree = {}
      for (const s of specsData) {
        if (!tree[s.domain]) tree[s.domain] = {}
        if (!tree[s.domain][s.type]) tree[s.domain][s.type] = []
        tree[s.domain][s.type].push({ id: s.id, title: s.title, status: s.status })
      }

      return { code: 200, data: { list, tree, total: list.length } }
    },
  },
  {
    url: '/api/wiki/specs/:id',
    method: 'get',
    response: ({ query }) => {
      const id = query?.id
      const doc = specsData.find(s => s.id === id)
      if (!doc) return { code: 404, message: '文档不存在', data: null }
      return { code: 200, data: doc }
    },
  },
  {
    url: '/api/wiki/code-mapping',
    method: 'get',
    response: () => ({ code: 200, data: codeMappingData }),
  },
  {
    url: '/api/wiki/ai/chat',
    method: 'post',
    response: ({ body }) => {
      const question = body?.question || ''
      const result = matchAiResponse(question)
      return { code: 200, data: result }
    },
  },
  {
    url: '/api/wiki/api-docs',
    method: 'get',
    response: () => ({ code: 200, data: apiDocsData }),
  },
]
