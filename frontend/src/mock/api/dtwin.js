export default [
  {
    url: '/api/dtwin/hangars',
    method: 'get',
    response: () => ({
      code: 200, msg: 'ok',
      data: {
        list: [
          { id: 1, name: '一号机库' },
          { id: 2, name: '二号机库' }
        ],
        total: 2, pageNum: 1, pageSize: 10
      }
    })
  },
  {
    url: '/api/dtwin/hangars/:id/model',
    method: 'get',
    response: () => ({
      code: 200, msg: 'ok',
      data: { id: 1, name: '一号机库', modelUrl: null, version: '1.0' }
    })
  },
  {
    url: '/api/dtwin/hangars/:id/workstations',
    method: 'get',
    response: () => ({
      code: 200, msg: 'ok',
      data: {
        list: [
          { id: 101, hangarId: 1, name: 'C检工位A', positionX: 15.50, positionY: 0, positionZ: 30.20, status: 'occupied', currentAircraftId: 'B-1234' },
          { id: 102, hangarId: 1, name: 'C检工位B', positionX: 35.50, positionY: 0, positionZ: 30.20, status: 'idle', currentAircraftId: null },
          { id: 103, hangarId: 1, name: 'A检工位A', positionX: 15.50, positionY: 0, positionZ: 60.20, status: 'occupied', currentAircraftId: 'B-5678' },
          { id: 104, hangarId: 1, name: 'A检工位B', positionX: 35.50, positionY: 0, positionZ: 60.20, status: 'maintenance', currentAircraftId: null },
          { id: 105, hangarId: 1, name: '排故工位', positionX: 55.50, positionY: 0, positionZ: 45.00, status: 'idle', currentAircraftId: null }
        ],
        total: 5, pageNum: 1, pageSize: 50
      }
    })
  },
  {
    url: '/api/dtwin/plans',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20 } = query || {}
      return {
        code: 200, msg: 'ok',
        data: {
          list: [
            { id: 2001, hangarId: 1, aircraftId: 'B-1234', planType: 'heavy', scheduledStart: '2026-05-20T08:00:00Z', scheduledEnd: '2026-06-10T18:00:00Z', status: 'in_progress' },
            { id: 2002, hangarId: 1, aircraftId: 'B-5678', planType: 'line',  scheduledStart: '2026-05-25T08:00:00Z', scheduledEnd: '2026-05-27T18:00:00Z', status: 'draft' },
            { id: 2003, hangarId: 2, aircraftId: 'B-9012', planType: 'heavy', scheduledStart: '2026-06-01T08:00:00Z', scheduledEnd: '2026-07-15T18:00:00Z', status: 'draft' }
          ],
          total: 3, pageNum: Number(page), pageSize: Number(pageSize)
        }
      }
    }
  },
  {
    url: '/api/dtwin/plans',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: { id: 2099 } })
  },
  {
    url: '/api/dtwin/plans/:id',
    method: 'put',
    response: () => ({ code: 200, msg: 'ok', data: null })
  },
  {
    url: '/api/dtwin/orders',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20 } = query || {}
      return {
        code: 200, msg: 'ok',
        data: {
          list: [
            { id: 3001, planId: 2001, workstationId: 101, workstationName: 'C检工位A', assigneeId: 1, assigneeName: '王工程师', description: '发动机拆卸检查', progress: 45, status: 'executing' },
            { id: 3002, planId: 2001, workstationId: 103, workstationName: 'A检工位A', assigneeId: 2, assigneeName: '李技术员', description: '起落架检查', progress: 80, status: 'executing' },
            { id: 3003, planId: 2002, workstationId: 102, workstationName: 'C检工位B', assigneeId: 3, assigneeName: '张工程师', description: '例行检查', progress: 0, status: 'pending' }
          ],
          total: 3, pageNum: Number(page), pageSize: Number(pageSize)
        }
      }
    }
  },
  {
    url: '/api/dtwin/orders',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: { id: 3099 } })
  },
  {
    url: '/api/dtwin/orders/:id/progress',
    method: 'put',
    response: () => ({ code: 200, msg: 'ok', data: null })
  },
  {
    url: '/api/dtwin/analytics/workload',
    method: 'get',
    response: () => ({
      code: 200, msg: 'ok',
      data: {
        workstationLoads: [
          { workstationId: 101, workstationName: 'C检工位A', utilizationRate: 0.87, orderCount: 15 },
          { workstationId: 102, workstationName: 'C检工位B', utilizationRate: 0.62, orderCount: 10 },
          { workstationId: 103, workstationName: 'A检工位A', utilizationRate: 0.75, orderCount: 12 },
          { workstationId: 104, workstationName: 'A检工位B', utilizationRate: 0.45, orderCount: 7 },
          { workstationId: 105, workstationName: '排故工位',  utilizationRate: 0.30, orderCount: 4 }
        ],
        avgUtilizationRate: 0.60
      }
    })
  },
  {
    url: '/api/dtwin/analytics/efficiency',
    method: 'get',
    response: () => ({
      code: 200, msg: 'ok',
      data: { avgCompletionDays: 4.2, completionRate: 0.86, completedOrders: 37, totalOrders: 43 }
    })
  },
  // Task Packages (per MRO-005 spec)
  {
    url: '/api/dtwin/tasks',
    method: 'get',
    response: ({ query }) => {
      const { pageNum = 1, pageSize = 20 } = query || {}
      const packages = [
        { id: 1, packageNo: 'TP-20260528-1234', title: 'B737-800发动机大修任务包', hangarId: 1,
          workstationId: 2, aircraftType: 'B737-800', registration: 'B-1234',
          planStart: '2026-06-01', planEnd: '2026-06-15', status: 'in_progress', priority: 'high',
          createTime: '2026-05-28T08:00:00' },
        { id: 2, packageNo: 'TP-20260528-5678', title: 'A320neo定检C检任务包', hangarId: 1,
          workstationId: 3, aircraftType: 'A320neo', registration: 'B-5678',
          planStart: '2026-06-05', planEnd: '2026-06-20', status: 'submitted', priority: 'normal',
          createTime: '2026-05-28T09:00:00' },
        { id: 3, packageNo: 'TP-20260527-9012', title: 'B777-300ER起落架检查任务包', hangarId: 2,
          workstationId: 5, aircraftType: 'B777-300ER', registration: 'B-9012',
          planStart: '2026-05-25', planEnd: '2026-05-30', status: 'completed', priority: 'urgent',
          createTime: '2026-05-27T10:00:00' }
      ]
      const filtered = packages.slice((pageNum-1)*pageSize, pageNum*pageSize)
      return { code: 200, msg: 'ok', data: { list: filtered, total: packages.length, pageNum: parseInt(pageNum), pageSize: parseInt(pageSize) }, timestamp: Date.now() }
    }
  },
  {
    url: '/api/dtwin/tasks',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: 100 + Math.floor(Math.random() * 900), timestamp: Date.now() })
  },
  {
    url: '/api/dtwin/tasks/:id/status',
    method: 'put',
    response: () => ({ code: 200, msg: 'ok', data: null, timestamp: Date.now() })
  },
  {
    url: '/api/dtwin/dashboard/operation',
    method: 'get',
    response: () => ({
      code: 200, msg: 'ok', timestamp: Date.now(),
      data: {
        totalPackages: 12, inProgressPackages: 4, completedToday: 2, totalPersonnel: 28,
        activePackages: [
          { id: 1, packageNo: 'TP-20260528-1234', title: 'B737-800发动机大修任务包', hangarId: 1,
            status: 'in_progress', priority: 'high', planStart: '2026-06-01', planEnd: '2026-06-15',
            createTime: '2026-05-28T08:00:00' }
        ]
      }
    })
  },
  {
    url: '/api/dtwin/assignments',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: 200 + Math.floor(Math.random() * 100), timestamp: Date.now() })
  },
  {
    url: '/api/dtwin/assignments',
    method: 'get',
    response: () => ({
      code: 200, msg: 'ok', timestamp: Date.now(),
      data: [
        { id: 1, packageId: 1, userId: 101, role: 'lead', workDate: '2026-06-01', shift: 'day' },
        { id: 2, packageId: 1, userId: 102, role: 'member', workDate: '2026-06-01', shift: 'day' },
        { id: 3, packageId: 1, userId: 103, role: 'inspector', workDate: '2026-06-02', shift: 'full' }
      ]
    })
  }
]
