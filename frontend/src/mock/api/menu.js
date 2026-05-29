const buildTree = (list, parentId = 0) =>
  list.filter(m => m.parentId === parentId).sort((a, b) => a.sort - b.sort).map(m => ({ ...m, children: buildTree(list, m.id) }))

// 根据权限码过滤出有权访问的菜单节点（M/C 类型），剔除无子节点的目录
const buildUserTree = (list, permissions) => {
  const isAdmin = permissions.includes('*:*:*')
  const visible = list.filter(m =>
    (m.type === 'M' || m.type === 'C') &&
    m.status === 1 &&
    (isAdmin || !m.permission || permissions.includes(m.permission))
  )
  const build = (parentId) =>
    visible
      .filter(m => m.parentId === parentId)
      .sort((a, b) => a.sort - b.sort)
      .map(m => {
        const children = build(m.id)
        if (m.type === 'M' && children.length === 0) return null
        return { ...m, children }
      })
      .filter(Boolean)
  return build(0)
}

let _menus = [
  { id: 1,  parentId: 0, name: '仪表板',   type: 'C', icon: 'Odometer',     path: '/dashboard',    component: 'Dashboard',          permission: '',                  sort: 1, status: 1 },
  { id: 130, parentId: 0, name: '数据分析', type: 'C', icon: 'TrendCharts',  path: '/analytics',    component: 'AnalyticsDashboard', permission: 'dashboard:view',    sort: 1.5, status: 1 },
  { id: 2,  parentId: 0, name: '系统管理', type: 'M', icon: 'Setting',       path: '/system',       component: '',                   permission: '',                  sort: 99, status: 1 },
  { id: 3,  parentId: 2, name: '用户管理', type: 'C', icon: 'User',          path: '/system/user',  component: 'system/UserManage',  permission: 'system:user:list',  sort: 1, status: 1 },
  { id: 4,  parentId: 3, name: '用户新增', type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:user:add',   sort: 1, status: 1 },
  { id: 5,  parentId: 3, name: '用户修改', type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:user:edit',  sort: 2, status: 1 },
  { id: 6,  parentId: 3, name: '用户删除', type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:user:remove',sort: 3, status: 1 },
  { id: 7,  parentId: 2, name: '部门管理', type: 'C', icon: 'OfficeBuilding',path: '/system/dept',  component: 'system/DeptManage',  permission: 'system:dept:list',  sort: 2, status: 1 },
  { id: 8,  parentId: 7, name: '部门新增', type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:dept:add',   sort: 1, status: 1 },
  { id: 9,  parentId: 7, name: '部门修改', type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:dept:edit',  sort: 2, status: 1 },
  { id: 10, parentId: 7, name: '部门删除', type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:dept:remove',sort: 3, status: 1 },
  { id: 11, parentId: 2, name: '角色管理', type: 'C', icon: 'UserFilled',    path: '/system/role',  component: 'system/RoleManage',  permission: 'system:role:list',  sort: 3, status: 1 },
  { id: 12, parentId: 11, name: '角色新增',type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:role:add',   sort: 1, status: 1 },
  { id: 13, parentId: 11, name: '角色修改',type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:role:edit',  sort: 2, status: 1 },
  { id: 14, parentId: 11, name: '角色删除',type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:role:remove',sort: 3, status: 1 },
  { id: 15, parentId: 2, name: '菜单管理', type: 'C', icon: 'Menu',          path: '/system/menu',  component: 'system/MenuManage',  permission: 'system:menu:list',  sort: 4, status: 1 },
  { id: 16, parentId: 15, name: '菜单新增',type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:menu:add',   sort: 1, status: 1 },
  { id: 17, parentId: 15, name: '菜单修改',type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:menu:edit',  sort: 2, status: 1 },
  { id: 18, parentId: 15, name: '菜单删除',type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:menu:remove',sort: 3, status: 1 },
  { id: 19, parentId: 2, name: '字典管理', type: 'C', icon: 'Collection',    path: '/system/dict',  component: 'system/DictManage',  permission: 'system:dict:list',  sort: 5, status: 1 },
  { id: 20, parentId: 19, name: '字典新增',type: 'F', icon: '',              path: '',              component: '',                   permission: 'system:dict:add',   sort: 1, status: 1 },
  { id: 21, parentId: 2,  name: '操作日志', type: 'C', icon: 'Document',      path: '/system/operation-log', component: 'system/OperationLog', permission: 'log:list',        sort: 6, status: 1 },
  { id: 22, parentId: 2,  name: '主题配置', type: 'C', icon: 'Brush',         path: '/system/theme',         component: 'system/ThemeConfig',  permission: 'system:theme:edit', sort: 7, status: 1 },
  { id: 50, parentId: 0,  name: '健康管理', type: 'M', icon: 'Monitor',       path: '/mro/health',           component: '',                    permission: '',                  sort: 2, status: 1 },
  { id: 51, parentId: 50, name: '健康态势', type: 'C', icon: 'DataAnalysis',  path: '/mro/health',           component: 'mro/health/Dashboard',permission: 'health:list',       sort: 1, status: 1 },
  { id: 52, parentId: 50, name: '机队状态', type: 'C', icon: 'Ship',          path: '/mro/health/aircraft',  component: 'mro/health/AircraftList', permission: 'health:list',  sort: 2, status: 1 },
  { id: 53, parentId: 50, name: '预警管理', type: 'C', icon: 'Bell',          path: '/mro/health/alerts',    component: 'mro/health/AlertList',permission: 'health:list',       sort: 3, status: 1 },
  { id: 54, parentId: 50, name: '故障统计', type: 'C', icon: 'TrendCharts',   path: '/mro/health/statistics',component: 'mro/health/Statistics',permission: 'health:view',      sort: 4, status: 1 },
  { id: 60, parentId: 0,  name: 'AR协作',   type: 'M', icon: 'VideoCamera',   path: '/mro/ar',               component: '',                    permission: '',                  sort: 3, status: 1 },
  { id: 61, parentId: 60, name: '巡检任务', type: 'C', icon: 'Compass',       path: '/mro/ar',               component: 'mro/ar/InspectionList',permission: 'ar:list',          sort: 1, status: 1 },
  { id: 62, parentId: 60, name: '远程协作', type: 'C', icon: 'Phone',         path: '/mro/ar/sessions',      component: 'mro/ar/SessionList',permission: 'ar:call',            sort: 2, status: 1 },
  { id: 63, parentId: 60, name: '影像档案', type: 'C', icon: 'Film',          path: '/mro/ar/archives',      component: 'mro/ar/ArchiveList',permission: 'ar:archive',         sort: 3, status: 1 },
  { id: 70, parentId: 0,  name: '排故助手', type: 'M', icon: 'MagicStick',    path: '/mro/tshoot',           component: '',                    permission: '',                  sort: 4, status: 1 },
  { id: 71, parentId: 70, name: '排故查询', type: 'C', icon: 'Search',        path: '/mro/tshoot',           component: 'mro/tshoot/QueryPage',permission: 'tshoot:query',      sort: 1, status: 1 },
  { id: 72, parentId: 70, name: '知识库',   type: 'C', icon: 'Files',         path: '/mro/tshoot/knowledge', component: 'mro/tshoot/KnowledgeBase',permission: 'tshoot:manage_kb',sort: 2, status: 1 },
  { id: 73, parentId: 70, name: '维修记录', type: 'C', icon: 'Notebook',      path: '/mro/tshoot/history',   component: 'mro/tshoot/RepairHistory',permission: 'tshoot:history', sort: 3, status: 1 },
  { id: 80, parentId: 0,  name: '手册管理', type: 'M', icon: 'Reading',       path: '/mro/manual',           component: '',                    permission: '',                  sort: 5, status: 1 },
  { id: 81, parentId: 80, name: '技术手册', type: 'C', icon: 'Document',      path: '/mro/manual',           component: 'mro/manual/ManualList',permission: 'manual:list',      sort: 1, status: 1 },
  { id: 85, parentId: 0,  name: '机库管理', type: 'M', icon: 'House',         path: '/mro/dtwin',            component: '',                    permission: '',                  sort: 6, status: 1 },
  { id: 86, parentId: 85, name: '数字孪生', type: 'C', icon: 'Cpu',           path: '/mro/dtwin',            component: 'mro/dtwin/HangarDigitalTwin', permission: 'dtwin:view',  sort: 1, status: 1 },
  { id: 87, parentId: 85, name: '机库概览', type: 'C', icon: 'MapLocation',   path: '/mro/dtwin/overview',   component: 'mro/dtwin/HangarOverview', permission: 'hangar:view',     sort: 2, status: 1 },
  { id: 88, parentId: 85, name: '运维看板', type: 'C', icon: 'DataBoard',     path: '/mro/dtwin/dashboard',  component: 'mro/dtwin/OperationDashboard', permission: 'dtwin:view',  sort: 3, status: 1 },
  { id: 89, parentId: 85, name: '维修计划', type: 'C', icon: 'Calendar',      path: '/mro/dtwin/planning',   component: 'mro/dtwin/TaskList',       permission: 'planning:view',   sort: 4, status: 1 },
  { id: 90, parentId: 0,  name: '工具航材', type: 'M', icon: 'SetUp',         path: '/mro/tool',             component: '',                    permission: '',                  sort: 7, status: 1 },
  { id: 91, parentId: 90, name: '工具管理', type: 'C', icon: 'Wrench',        path: '/mro/tool',             component: 'mro/tool/ToolManagement',permission: 'tool:list',       sort: 1, status: 1 },
  { id: 92, parentId: 90, name: '航材管理', type: 'C', icon: 'Box',           path: '/mro/tool/material',    component: 'mro/tool/MaterialManagement',permission: 'tool:list',    sort: 2, status: 1 },
  { id: 93, parentId: 90, name: '领料申请', type: 'C', icon: 'DocumentCopy',  path: '/mro/tool/material-request', component: 'mro/tool/MaterialRequestList', permission: 'material:list', sort: 3, status: 1 },
  { id: 95, parentId: 0,  name: 'VR培训',   type: 'M', icon: 'View',          path: '/mro/training',         component: '',                    permission: '',                  sort: 8, status: 1 },
  { id: 96, parentId: 95, name: '培训概览', type: 'C', icon: 'DataLine',      path: '/mro/training',         component: 'mro/training/TrainingOverview',permission: 'train:assign',sort: 1, status: 1 },
  { id: 97, parentId: 95, name: '场景管理', type: 'C', icon: 'Film',          path: '/mro/training/scenarios', component: 'mro/training/ScenarioManagement',permission: 'train:assign',sort: 2, status: 1 },
  { id: 98, parentId: 95, name: '学员列表', type: 'C', icon: 'User',          path: '/mro/training/trainees',  component: 'mro/training/TraineeList',permission: 'train:assign',sort: 3, status: 1 },
  { id: 99, parentId: 95, name: '培训任务', type: 'C', icon: 'Finished',      path: '/mro/training/sessions',  component: 'mro/training/SessionManagement',permission: 'train:assign',sort: 4, status: 1 },
  { id: 100, parentId: 0, name: '电子工卡', type: 'M', icon: 'Tickets',       path: '/mro/workcard',         component: '',                    permission: '',                  sort: 9, status: 1 },
  { id: 101, parentId: 100, name: '工卡列表', type: 'C', icon: 'List',        path: '/mro/workcard',         component: 'mro/workcard/WorkcardList',permission: 'workcard:monitor',sort: 1, status: 1 },
  { id: 102, parentId: 100, name: '待检工卡', type: 'C', icon: 'Document',    path: '/mro/workcard/quality', component: 'mro/workcard/QualityPending', permission: 'quality:list', sort: 2, status: 1 },
  { id: 103, parentId: 100, name: 'NCR管理',  type: 'C', icon: 'Warning',     path: '/mro/workcard/ncr',     component: 'mro/workcard/QualityNcr',   permission: 'quality:list', sort: 3, status: 1 },
  { id: 110, parentId: 0,   name: '知识库',   type: 'M', icon: 'Reading',      path: '/wiki',                 component: '',                         permission: '',                sort: 10, status: 1 },
  { id: 111, parentId: 110, name: '规格文档', type: 'C', icon: 'Document',     path: '/wiki/specs',           component: 'wiki/SpecBrowser',         permission: 'wiki:view',       sort: 1, status: 1 },
  { id: 112, parentId: 110, name: '代码映射', type: 'C', icon: 'Connection',   path: '/wiki/code-mapping',    component: 'wiki/CodeMapping',         permission: 'wiki:view',       sort: 2, status: 1 },
  { id: 113, parentId: 110, name: '问答定位', type: 'C', icon: 'ChatDotRound', path: '/wiki/ai-chat',         component: 'wiki/AiChat',              permission: 'wiki:view',       sort: 3, status: 1 },
  { id: 114, parentId: 110, name: 'API文档',  type: 'C', icon: 'Notebook',     path: '/wiki/api-docs',        component: 'wiki/ApiDocs',             permission: 'wiki:view',       sort: 4, status: 1 },
  { id: 120, parentId: 0,   name: '人员资质', type: 'M', icon: 'Avatar',       path: '/mro/personnel',        component: '',                         permission: '',                sort: 11, status: 1 },
  { id: 121, parentId: 120, name: '资质台账', type: 'C', icon: 'Postcard',     path: '/mro/personnel',        component: 'mro/personnel/PersonnelList', permission: 'personnel:list:view', sort: 1, status: 1 },
  { id: 122, parentId: 120, name: '证照管理', type: 'C', icon: 'Wallet',       path: '/mro/personnel/license',component: 'mro/personnel/LicenseManagement', permission: 'personnel:license:view', sort: 2, status: 1 },
  { id: 123, parentId: 120, name: '证照预警', type: 'C', icon: 'AlarmClock',   path: '/mro/personnel/alerts', component: 'mro/personnel/PersonnelAlerts', permission: 'personnel:list:view', sort: 3, status: 1 },
]
let _nextId = 200

// Mock 用户权限表（与 auth.js 保持一致）
const _userPermissions = {
  admin:   ['*:*:*'],
  manager: ['system:user:list','system:dept:list','system:role:list','system:menu:list','system:dict:list','wiki:view'],
  user:    ['system:user:list','system:dept:list'],
}

export default [
  {
    url: '/api/system/menu/user',
    method: 'get',
    response: ({ headers }) => {
      // Mock 模式下从 token 内容推断用户身份；真实环境由 JWT 解析
      const token = (headers && (headers.authorization || headers.Authorization)) || ''
      let permissions = ['*:*:*']
      if (token.includes('manager')) {
        permissions = _userPermissions.manager
      } else if (token.includes('user-')) {
        permissions = _userPermissions.user
      }
      const menus = buildUserTree(_menus, permissions)
      return { code: 200, message: '获取成功', data: { menus, permissions } }
    },
  },
  {
    url: '/api/system/menu/tree',
    method: 'get',
    response: () => ({ code: 200, message: '获取成功', data: buildTree(_menus) }),
  },
  {
    url: '/api/system/menu/detail',
    method: 'get',
    response: ({ query }) => {
      const menu = _menus.find(m => m.id === Number(query.id))
      return menu ? { code: 200, message: '获取成功', data: menu } : { code: 404, message: '菜单不存在', data: null }
    },
  },
  {
    url: '/api/system/menu/create',
    method: 'post',
    response: ({ body }) => {
      const newMenu = { ...body, id: ++_nextId }
      _menus.push(newMenu)
      return { code: 200, message: '创建成功', data: newMenu }
    },
  },
  {
    url: '/api/system/menu/update',
    method: 'put',
    response: ({ body }) => {
      const idx = _menus.findIndex(m => m.id === body.id)
      if (idx === -1) return { code: 404, message: '菜单不存在', data: null }
      _menus[idx] = { ..._menus[idx], ...body }
      return { code: 200, message: '更新成功', data: _menus[idx] }
    },
  },
  {
    url: '/api/system/menu/remove',
    method: 'delete',
    response: ({ query }) => {
      const id = Number(query.id)
      if (_menus.some(m => m.parentId === id)) return { code: 400, message: '存在子菜单，不允许删除', data: null }
      const idx = _menus.findIndex(m => m.id === id)
      if (idx === -1) return { code: 404, message: '菜单不存在', data: null }
      _menus.splice(idx, 1)
      return { code: 200, message: '删除成功', data: null }
    },
  },
]
