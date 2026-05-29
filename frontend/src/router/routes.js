/**
 * 路由定义
 */
import { h, resolveComponent } from 'vue'

// 公开路由（不需要认证）
export const publicRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/pages/NotFound.vue'),
    meta: {
      title: '页面不存在'
    }
  }
]

// 受保护路由（需要认证）
export const protectedRoutes = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/pages/Layout.vue'),
    redirect: '/dashboard',
    meta: {
      title: '首页',
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/pages/Dashboard.vue'),
        meta: {
          title: '仪表板',
          requiresAuth: true,
          breadcrumb: '仪表板'
        }
      },
      {
        path: 'system',
        name: 'System',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: {
          title: '系统管理',
          requiresAuth: true
        },
        children: [
          {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/pages/system/UserManage.vue'),
            meta: {
              title: '用户管理',
              requiresAuth: true,
              permissions: ['system:user:list'],
              breadcrumb: '用户管理'
            }
          },
          {
            path: 'dept',
            name: 'DeptManage',
            component: () => import('@/pages/system/DeptManage.vue'),
            meta: {
              title: '部门管理',
              requiresAuth: true,
              permissions: ['system:dept:list'],
              breadcrumb: '部门管理'
            }
          },
          {
            path: 'role',
            name: 'RoleManage',
            component: () => import('@/pages/system/RoleManage.vue'),
            meta: {
              title: '角色管理',
              requiresAuth: true,
              permissions: ['system:role:list'],
              breadcrumb: '角色管理'
            }
          },
          {
            path: 'menu',
            name: 'MenuManage',
            component: () => import('@/pages/system/MenuManage.vue'),
            meta: {
              title: '菜单管理',
              requiresAuth: true,
              permissions: ['system:menu:list'],
              breadcrumb: '菜单管理'
            }
          },
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
          },
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
          },
          {
            path: 'theme',
            name: 'ThemeConfig',
            component: () => import('@/pages/system/ThemeConfig.vue'),
            meta: {
              title: '主题配置',
              requiresAuth: true,
              permissions: ['system:theme:edit'],
              breadcrumb: '主题配置'
            }
          }
        ]
      },
      {
        path: 'mro/health',
        name: 'HealthMonitoring',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: {
          title: '健康管理',
          requiresAuth: true
        },
        children: [
          {
            path: '',
            name: 'HealthDashboard',
            component: () => import('@/pages/health/Dashboard.vue'),
            meta: {
              title: '健康态势',
              requiresAuth: true,
              permissions: ['health:list'],
              breadcrumb: '健康态势'
            }
          },
          {
            path: 'aircraft',
            name: 'AircraftList',
            component: () => import('@/pages/health/AircraftList.vue'),
            meta: {
              title: '机队状态',
              requiresAuth: true,
              permissions: ['health:list'],
              breadcrumb: '机队状态'
            }
          },
          {
            path: 'aircraft/:id',
            name: 'AircraftDetail',
            component: () => import('@/pages/health/AircraftDetail.vue'),
            meta: {
              title: '飞机详情',
              requiresAuth: true,
              permissions: ['health:view'],
              breadcrumb: '飞机详情'
            }
          },
          {
            path: 'alerts',
            name: 'HealthAlerts',
            component: () => import('@/pages/health/AlertList.vue'),
            meta: {
              title: '预警管理',
              requiresAuth: true,
              permissions: ['health:list'],
              breadcrumb: '预警管理'
            }
          },
          {
            path: 'statistics',
            name: 'HealthStatistics',
            component: () => import('@/pages/health/Statistics.vue'),
            meta: {
              title: '故障统计',
              requiresAuth: true,
              permissions: ['health:view'],
              breadcrumb: '故障统计'
            }
          }
        ]
      },
      {
        path: 'mro/ar',
        name: 'ArCollaboration',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: {
          title: 'AR协作',
          requiresAuth: true
        },
        children: [
          {
            path: '',
            name: 'InspectionList',
            component: () => import('@/pages/ar/InspectionList.vue'),
            meta: {
              title: '巡检任务',
              requiresAuth: true,
              permissions: ['ar:list'],
              breadcrumb: '巡检任务'
            }
          },
          {
            path: 'sessions',
            name: 'ArSessionList',
            component: () => import('@/pages/ar/SessionList.vue'),
            meta: {
              title: '远程协作',
              requiresAuth: true,
              permissions: ['ar:call'],
              breadcrumb: '远程协作'
            }
          },
          {
            path: 'archives',
            name: 'ArArchiveList',
            component: () => import('@/pages/ar/ArchiveList.vue'),
            meta: {
              title: '影像档案',
              requiresAuth: true,
              permissions: ['ar:archive'],
              breadcrumb: '影像档案'
            }
          }
        ]
      },
      {
        path: 'mro/tshoot',
        name: 'Troubleshooting',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: {
          title: '排故助手',
          requiresAuth: true
        },
        children: [
          {
            path: '',
            name: 'TshootQuery',
            component: () => import('@/pages/tshoot/QueryPage.vue'),
            meta: {
              title: '排故查询',
              requiresAuth: true,
              permissions: ['tshoot:query'],
              breadcrumb: '排故查询'
            }
          },
          {
            path: 'knowledge',
            name: 'KnowledgeBase',
            component: () => import('@/pages/tshoot/KnowledgeBase.vue'),
            meta: {
              title: '知识库',
              requiresAuth: true,
              permissions: ['tshoot:manage_kb'],
              breadcrumb: '知识库'
            }
          },
          {
            path: 'history',
            name: 'RepairHistory',
            component: () => import('@/pages/tshoot/RepairHistory.vue'),
            meta: {
              title: '维修记录',
              requiresAuth: true,
              permissions: ['tshoot:history'],
              breadcrumb: '维修记录'
            }
          }
        ]
      },
      {
        path: 'mro/manual',
        name: 'ManualManagement',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '手册管理', requiresAuth: true },
        children: [
          {
            path: '',
            name: 'ManualList',
            component: () => import('@/pages/manual/ManualList.vue'),
            meta: { title: '手册列表', requiresAuth: true, permissions: ['manual:search'], breadcrumb: '手册列表' }
          },
          {
            path: 'search',
            name: 'ManualSearch',
            component: () => import('@/pages/manual/ManualSearch.vue'),
            meta: { title: '手册搜索', requiresAuth: true, permissions: ['manual:search'], breadcrumb: '手册搜索' }
          },
          {
            path: 'translate',
            name: 'ManualTranslate',
            component: () => import('@/pages/manual/ManualTranslate.vue'),
            meta: { title: '翻译管理', requiresAuth: true, permissions: ['manual:translate'], breadcrumb: '翻译管理' }
          },
          {
            path: 'versions',
            name: 'ManualVersions',
            component: () => import('@/pages/manual/ManualVersions.vue'),
            meta: { title: '版本历史', requiresAuth: true, permissions: ['manual:edit'], breadcrumb: '版本历史' }
          },
          {
            path: ':id/read',
            name: 'ManualReader',
            component: () => import('@/pages/manual/ManualReader.vue'),
            meta: { title: '手册阅读', requiresAuth: true, permissions: ['manual:search'], breadcrumb: '手册阅读' }
          }
        ]
      },
      {
        path: 'mro/dtwin',
        name: 'DigitalTwin',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '机库管理', requiresAuth: true },
        children: [
          {
            path: '',
            name: 'HangarDigitalTwin',
            component: () => import('@/pages/dtwin/HangarDigitalTwin.vue'),
            meta: { title: '孪生场景', requiresAuth: true, permissions: ['dtwin:view'], breadcrumb: '孪生场景' }
          },
          {
            path: 'overview',
            name: 'HangarOverview',
            component: () => import('@/pages/dtwin/HangarOverview.vue'),
            meta: { title: '机库概览', requiresAuth: true, permissions: ['hangar:view'], breadcrumb: '机库概览' }
          },
          {
            path: 'plans',
            name: 'DtwinPlans',
            component: () => import('@/pages/dtwin/ProductionPlan.vue'),
            meta: { title: '生产计划', requiresAuth: true, permissions: ['dtwin:plan'], breadcrumb: '生产计划' }
          },
          {
            path: 'orders',
            name: 'DtwinOrders',
            component: () => import('@/pages/dtwin/OrderMonitor.vue'),
            meta: { title: '指令监控', requiresAuth: true, permissions: ['dtwin:monitor'], breadcrumb: '指令监控' }
          },
          {
            path: 'analytics',
            name: 'DtwinAnalytics',
            component: () => import('@/pages/dtwin/Analytics.vue'),
            meta: { title: '数据分析', requiresAuth: true, permissions: ['dtwin:analyze'], breadcrumb: '数据分析' }
          },
          {
            path: 'dashboard',
            name: 'OperationDashboard',
            component: () => import('@/pages/dtwin/OperationDashboard.vue'),
            meta: { title: '运营看板', requiresAuth: true, permissions: ['dashboard:view'], breadcrumb: '运营看板' }
          },
          {
            path: 'planning',
            name: 'TaskList',
            component: () => import('@/pages/dtwin/TaskList.vue'),
            meta: { title: '维修计划', requiresAuth: true, permissions: ['planning:view'], breadcrumb: '维修计划' }
          },
          {
            path: 'planning/workload',
            name: 'WorkloadView',
            component: () => import('@/pages/dtwin/WorkloadView.vue'),
            meta: { title: '工作负荷', requiresAuth: true, permissions: ['planning:view'], breadcrumb: '工作负荷' }
          },
          {
            path: 'planning/tasks/:id',
            name: 'TaskDetail',
            component: () => import('@/pages/dtwin/TaskDetail.vue'),
            meta: { title: '任务详情', requiresAuth: true, permissions: ['planning:view'], breadcrumb: '任务详情' }
          }
        ]
      },
      {
        path: 'mro/tool',
        name: 'ToolMaterial',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '工具航材', requiresAuth: true },
        children: [
          {
            path: '',
            name: 'ToolManagement',
            component: () => import('@/pages/tool/ToolManagement.vue'),
            meta: { title: '工具管理', requiresAuth: true, permissions: ['tool:inventory'], breadcrumb: '工具管理' }
          },
          {
            path: 'material',
            name: 'MaterialManagement',
            component: () => import('@/pages/tool/MaterialManagement.vue'),
            meta: { title: '航材管理', requiresAuth: true, permissions: ['material:list'], breadcrumb: '航材管理' }
          },
          {
            path: 'material-request',
            name: 'MaterialRequestList',
            component: () => import('@/pages/tool/MaterialRequestList.vue'),
            meta: { title: '航材申请', requiresAuth: true, permissions: ['material:view'], breadcrumb: '航材申请' }
          },
          {
            path: 'material-request/:id',
            name: 'MaterialRequestDetail',
            component: () => import('@/pages/tool/MaterialRequestDetail.vue'),
            meta: { title: '申请详情', requiresAuth: true, permissions: ['material:view'], breadcrumb: '申请详情' }
          }
        ]
      },
      {
        path: 'mro/training',
        name: 'TrainingCenter',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: 'VR培训', requiresAuth: true },
        children: [
          {
            path: '',
            name: 'TrainingOverview',
            component: () => import('@/pages/training/TrainingOverview.vue'),
            meta: { title: '培训概览', requiresAuth: true, permissions: ['train:assign'], breadcrumb: '培训概览' }
          }
        ]
      },
      {
        path: 'mro/workcard',
        name: 'WorkcardManagement',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '电子工卡', requiresAuth: true },
        children: [
          {
            path: '',
            name: 'WorkcardList',
            component: () => import('@/pages/workcard/WorkcardList.vue'),
            meta: { title: '工卡列表', requiresAuth: true, permissions: ['workcard:monitor'], breadcrumb: '工卡列表' }
          },
          {
            path: ':id/execute',
            name: 'WorkcardExecute',
            component: () => import('@/pages/workcard/WorkcardExecute.vue'),
            meta: { title: '工卡执行', requiresAuth: true, permissions: ['workcard:monitor'], breadcrumb: '工卡执行' }
          },
          {
            path: ':id/report',
            name: 'WorkcardReport',
            component: () => import('@/pages/workcard/WorkcardReport.vue'),
            meta: { title: '异常上报', requiresAuth: true, permissions: ['workcard:monitor'], breadcrumb: '异常上报' }
          },
          {
            path: 'quality',
            name: 'QualityPending',
            component: () => import('@/pages/workcard/QualityPending.vue'),
            meta: { title: '待签列表', requiresAuth: true, permissions: ['quality:sign'], breadcrumb: '待签列表' }
          },
          {
            path: 'quality/sign/:id',
            name: 'QualitySign',
            component: () => import('@/pages/workcard/QualitySign.vue'),
            meta: { title: '质检签署', requiresAuth: true, permissions: ['quality:sign'], breadcrumb: '质检签署' }
          },
          {
            path: 'ncr',
            name: 'QualityNcr',
            component: () => import('@/pages/workcard/QualityNcr.vue'),
            meta: { title: '不符合项', requiresAuth: true, permissions: ['quality:sign'], breadcrumb: '不符合项' }
          }
        ]
      },
      {
        path: 'mro/personnel',
        name: 'PersonnelManagement',
        component: { render: () => h(resolveComponent('router-view')) },
        meta: { title: '人员资质', requiresAuth: true },
        children: [
          {
            path: '',
            name: 'PersonnelList',
            component: () => import('@/pages/personnel/PersonnelList.vue'),
            meta: { title: '资质台账', requiresAuth: true, permissions: ['personnel:list:view'], breadcrumb: '资质台账' }
          },
          {
            path: 'alerts',
            name: 'PersonnelAlerts',
            component: () => import('@/pages/personnel/PersonnelAlerts.vue'),
            meta: { title: '证照预警', requiresAuth: true, permissions: ['personnel:list:view'], breadcrumb: '证照预警' }
          },
          {
            path: 'license',
            name: 'LicenseManagement',
            component: () => import('@/pages/personnel/LicenseManagement.vue'),
            meta: { title: '证照管理', requiresAuth: true, permissions: ['personnel:license:view'], breadcrumb: '证照管理' }
          },
          {
            path: ':id',
            name: 'PersonnelDetail',
            component: () => import('@/pages/personnel/PersonnelDetail.vue'),
            meta: { title: '人员详情', requiresAuth: true, permissions: ['personnel:list:view'], breadcrumb: '人员详情' }
          }
        ]
      },
      {
        path: 'wiki',
        name: 'Wiki',
        component: () => import('@/pages/wiki/WikiLayout.vue'),
        redirect: '/wiki/specs',
        meta: { title: '知识库', requiresAuth: true, breadcrumb: '知识库' },
        children: [
          {
            path: 'specs',
            name: 'SpecBrowser',
            component: () => import('@/pages/wiki/SpecBrowser.vue'),
            meta: { title: '规格文档', requiresAuth: true, permissions: ['wiki:view'], breadcrumb: '规格文档' }
          },
          {
            path: 'specs/:id',
            name: 'SpecDetail',
            component: () => import('@/pages/wiki/SpecBrowser.vue'),
            meta: { title: '文档详情', requiresAuth: true, permissions: ['wiki:view'], breadcrumb: '文档详情' }
          },
          {
            path: 'code-mapping',
            name: 'CodeMapping',
            component: () => import('@/pages/wiki/CodeMapping.vue'),
            meta: { title: '代码映射', requiresAuth: true, permissions: ['wiki:view'], breadcrumb: '代码映射' }
          },
          {
            path: 'ai-chat',
            name: 'AiChat',
            component: () => import('@/pages/wiki/AiChat.vue'),
            meta: { title: '问答定位', requiresAuth: true, permissions: ['wiki:view'], breadcrumb: '问答定位' }
          },
          {
            path: 'api-docs',
            name: 'ApiDocs',
            component: () => import('@/pages/wiki/ApiDocs.vue'),
            meta: { title: 'API文档', requiresAuth: true, permissions: ['wiki:view'], breadcrumb: 'API文档' }
          }
        ]
      }
    ]
  }
]

// 404 路由（必须放在最后）
export const notFoundRoute = {
  path: '/:pathMatch(.*)*',
  redirect: '/404'
}

// 所有路由
export const allRoutes = [...publicRoutes, ...protectedRoutes, notFoundRoute]
