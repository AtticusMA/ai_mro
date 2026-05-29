const registrations = ['B-1234', 'B-5678', 'B-9012', 'B-3456', 'B-7890', 'B-2345', 'B-6789', 'B-0123']
const aircraftTypes = ['B737-800', 'A320neo', 'B777-300ER', 'A350-900', 'B787-9']
const workcardTypes = ['机体', '发动机', '航电', '结构']
const statuses = ['待派工', '已派工', '执行中', '待质检', '质检通过', '已关闭']
const stepStatuses = ['pending', 'confirmed', 'skipped']

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generateWorkcards() {
  const workcards = []
  
  for (let i = 0; i < 8; i++) {
    const reg = registrations[i]
    const aircraftType = aircraftTypes[i % aircraftTypes.length]
    const workcardType = workcardTypes[i % workcardTypes.length]
    
    // Generate 5-7 steps for each workcard
    const steps = []
    const stepTitles = [
      '检查发动机舱内部结构',
      '清洁滑油滤清器',
      '检查液压系统管路',
      '确认安全销已拆除',
      '测量间隙并记录',
      '完成签字确认',
      '检查起落架收放机构',
      '测试航电系统自检功能',
      '检查机身蒙皮损伤情况',
      '校准惯性导航系统',
      '检查空调组件工作状态',
      '测试防冰系统功能',
      '检查燃油系统密封性',
      '检查APU启动性能'
    ]
    
    const stepCount = 5 + Math.floor(Math.random() * 3)
    for (let j = 0; j < stepCount; j++) {
      steps.push({
        id: `${i + 1}-${j + 1}`,
        title: stepTitles[j % stepTitles.length],
        status: randomPick(stepStatuses),
        completed_at: j < 3 ? `2026-05-${String(15 + j).padStart(2, '0')} ${String(9 + j).padStart(2, '0')}:30:00` : null,
        notes: j === 0 ? '首次检查，重点关注区域' : j === 2 ? '发现轻微渗漏，已处理' : ''
      })
    }
    
    workcards.push({
      id: i + 1,
      workcard_id: `WC-${String(1000 + i).padStart(4, '0')}`,
      aircraft_id: reg,
      aircraft_type: aircraftType,
      type: workcardType,
      title: `${workcardType}定检工卡`,
      description: `${aircraftType} ${reg} ${workcardType}定期维护任务`,
      status: statuses[i % statuses.length],
      priority: i < 3 ? 'high' : i < 6 ? 'medium' : 'low',
      created_by: `工程师${String(i + 1).padStart(2, '0')}`,
      created_at: `2026-05-${String(10 + i).padStart(2, '0')} ${String(8 + i).padStart(2, '0')}:00:00`,
      updated_at: `2026-05-${String(12 + i).padStart(2, '0')} ${String(10 + i).padStart(2, '0')}:00:00`,
      due_date: `2026-06-${String(15 + (i % 10)).padStart(2, '0')}`, 
      assigned_to: [`王工`, `李工`, `张工`, `陈工`, `刘工`][i % 5],
      base: ['北京基地', '上海基地', '广州基地'][i % 3],
      hours_estimated: 4 + Math.floor(Math.random() * 12),
      hours_actual: i > 2 ? 4 + Math.floor(Math.random() * 12) : 0,
      photos: i > 3 ? [{id: 1, url: `/images/workcards/${reg}-1.jpg`, uploaded_at: `2026-05-${String(13 + i).padStart(2, '0')} 14:20:00`}] : [],
      reports: i > 4 ? [{id: 1, title: '初步检查报告', uploaded_at: `2026-05-${String(14 + i).padStart(2, '0')} 16:45:00`}]: [],
      steps: steps
    })
  }
  
  return workcards
}

function generateProgressData() {
  return {
    total_workcards: 8,
    by_status: [
      { status: '待派工', count: 2 },
      { status: '已派工', count: 1 },
      { status: '执行中', count: 3 },
      { status: '待质检', count: 1 },
      { status: '质检通过', count: 1 },
      { status: '已关闭', count: 0 }
    ],
    by_base: [
      { base: '北京基地', count: 4 },
      { base: '上海基地', count: 3 },
      { base: '广州基地', count: 1 }
    ],
    by_type: [
      { type: '机体', count: 2 },
      { type: '发动机', count: 3 },
      { type: '航电', count: 2 },
      { type: '结构', count: 1 }
    ],
    completion_rate: 62.5,
    avg_completion_time: 3.2
  }
}

function generateAlerts() {
  return [
    {
      id: 1,
      workcard_id: 'WC-1001',
      aircraft_id: 'B-1234',
      alert_level: 'red',
      message: '发动机工卡进度严重滞后，已超期2天',
      created_at: '2026-05-25 09:15:00',
      acknowledged: false
    },
    {
      id: 2,
      workcard_id: 'WC-1003',
      aircraft_id: 'B-9012',
      alert_level: 'orange',
      message: '航电系统检查步骤未完成，影响后续测试',
      created_at: '2026-05-25 11:30:00',
      acknowledged: true
    },
    {
      id: 3,
      workcard_id: 'WC-1005',
      aircraft_id: 'B-7890',
      alert_level: 'yellow',
      message: '结构检查报告未上传，需在24小时内补交',
      created_at: '2026-05-25 14:45:00',
      acknowledged: false
    }
  ]
}

const workcards = generateWorkcards()

export default [
  // GET /api/workcards — list
  {
    url: '/api/workcards',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 10, status, aircraft_id, type, base } = query || {}
      let filtered = workcards
      if (status) filtered = filtered.filter(w => w.status === status)
      if (aircraft_id) filtered = filtered.filter(w => w.aircraft_id === aircraft_id)
      if (type) filtered = filtered.filter(w => w.type === type)
      if (base) filtered = filtered.filter(w => w.base === base)
      return {
        code: 200,
        data: {
          list: filtered.slice((page - 1) * pageSize, page * pageSize),
          total: filtered.length
        }
      }
    }
  },
  // GET /api/workcards/:id — detail
  {
    url: '/api/workcards/:id',
    method: 'get',
    response: ({ url }) => {
      const id = parseInt(url.match(/\/api\/workcards\/([^/]+)/)?.[1] || '1')
      const workcard = workcards.find(w => w.id === id) || workcards[0]
      return {
        code: 200,
        data: workcard
      }
    }
  },
  // GET /api/workcards/:id/steps — steps list
  {
    url: '/api/workcards/:id/steps',
    method: 'get',
    response: ({ url }) => {
      const id = parseInt(url.match(/\/api\/workcards\/([^/]+)/)?.[1] || '1')
      const workcard = workcards.find(w => w.id === id) || workcards[0]
      return {
        code: 200,
        data: {
          list: workcard.steps,
          total: workcard.steps.length
        }
      }
    }
  },
  // POST /api/workcards/:id/steps/:stepId/confirm — confirm step
  {
    url: '/api/workcards/:id/steps/:stepId/confirm',
    method: 'post',
    response: ({ url, body }) => {
      const id = parseInt(url.match(/\/api\/workcards\/([^/]+)/)?.[1] || '1')
      const stepId = url.match(/\/steps\/([^/]+)/)?.[1] || '1-1'
      const workcard = workcards.find(w => w.id === id) || workcards[0]
      const step = workcard.steps.find(s => s.id === stepId)
      if (step) {
        step.status = 'confirmed'
        step.completed_at = new Date().toISOString().slice(0, 19).replace('T', ' ')
        step.notes = body.notes || ''
      }
      return { code: 200, message: '步骤确认成功', data: step }
    }
  },
  // POST /api/workcards/:id/checkin
  {
    url: '/api/workcards/:id/checkin',
    method: 'post',
    response: ({ url }) => {
      const id = parseInt(url.match(/\/api\/workcards\/([^/]+)/)?.[1] || '1')
      const workcard = workcards.find(w => w.id === id) || workcards[0]
      workcard.status = '执行中'
      workcard.updated_at = new Date().toISOString().slice(0, 19).replace('T', ' ')
      return { code: 200, message: '工卡已签入', data: workcard }
    }
  },
  // POST /api/workcards/:id/checkout
  {
    url: '/api/workcards/:id/checkout',
    method: 'post',
    response: ({ url }) => {
      const id = parseInt(url.match(/\/api\/workcards\/([^/]+)/)?.[1] || '1')
      const workcard = workcards.find(w => w.id === id) || workcards[0]
      workcard.status = '待质检'
      workcard.updated_at = new Date().toISOString().slice(0, 19).replace('T', ' ')
      return { code: 200, message: '工卡已签出', data: workcard }
    }
  },
  // POST /api/workcards/:id/photos
  {
    url: '/api/workcards/:id/photos',
    method: 'post',
    response: ({ url, body }) => {
      const id = parseInt(url.match(/\/api\/workcards\/([^/]+)/)?.[1] || '1')
      const workcard = workcards.find(w => w.id === id) || workcards[0]
      const photo = {
        id: workcard.photos.length + 1,
        url: body.url || `/images/workcards/${workcard.aircraft_id}-${workcard.photos.length + 1}.jpg`,
        uploaded_at: new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      workcard.photos.push(photo)
      return { code: 200, message: '照片上传成功', data: photo }
    }
  },
  // POST /api/workcards/:id/reports
  {
    url: '/api/workcards/:id/reports',
    method: 'post',
    response: ({ url, body }) => {
      const id = parseInt(url.match(/\/api\/workcards\/([^/]+)/)?.[1] || '1')
      const workcard = workcards.find(w => w.id === id) || workcards[0]
      const report = {
        id: workcard.reports.length + 1,
        title: body.title || '检查报告',
        uploaded_at: new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      workcard.reports.push(report)
      return { code: 200, message: '报告上传成功', data: report }
    }
  },
  // GET /api/workcards/:id/reports
  {
    url: '/api/workcards/:id/reports',
    method: 'get',
    response: ({ url }) => {
      const id = parseInt(url.match(/\/api\/workcards\/([^/]+)/)?.[1] || '1')
      const workcard = workcards.find(w => w.id === id) || workcards[0]
      return {
        code: 200,
        data: {
          list: workcard.reports,
          total: workcard.reports.length
        }
      }
    }
  },
  // GET /api/workcards/progress
  {
    url: '/api/workcards/progress',
    method: 'get',
    response: () => ({
      code: 200,
      data: generateProgressData()
    })
  },
  // GET /api/workcards/alerts
  {
    url: '/api/workcards/alerts',
    method: 'get',
    response: ({ query }) => {
      const alerts = generateAlerts()
      const { page = 1, pageSize = 10, alert_level } = query || {}
      let filtered = alerts
      if (alert_level) filtered = filtered.filter(a => a.alert_level === alert_level)
      return {
        code: 200,
        data: {
          list: filtered.slice((page - 1) * pageSize, page * pageSize),
          total: filtered.length
        }
      }
    }
  },

  // ─── Quality Sign Records (auxiliary) ────────────────────────────────────────

  // GET /api/workcards/:workcardId/sign-records
  {
    url: '/api/workcards/:workcardId/sign-records',
    method: 'get',
    response: ({ url }) => {
      const workcardId = parseInt(url.match(/\/api\/workcards\/([^/]+)\/sign-records/)?.[1] || '100')
      return {
        code: 200,
        msg: 'ok',
        data: [
          { id: 1001, workcardId, stepId: 10, signerId: 1, signerName: '张工', result: 'pass', comment: '检查通过', signTime: '2026-05-01T09:00:00', signatureHash: 'abc123' },
          { id: 1002, workcardId, stepId: 11, signerId: 2, signerName: '李检', result: 'fail', comment: '焊点存在缺陷', signTime: '2026-05-01T10:30:00', signatureHash: 'def456' }
        ],
        timestamp: Date.now()
      }
    }
  },
  // GET /api/workcards/sign-records/:id
  {
    url: '/api/workcards/sign-records/:id',
    method: 'get',
    response: ({ url }) => {
      const id = parseInt(url.match(/\/api\/workcards\/sign-records\/([^/]+)/)?.[1] || '1001')
      return {
        code: 200,
        msg: 'ok',
        data: { id, workcardId: 100, stepId: 10, signerId: 1, signerName: '张工', result: 'pass', comment: '检查通过', signTime: '2026-05-01T09:00:00', signatureHash: 'abc123' },
        timestamp: Date.now()
      }
    }
  },

  // ─── Checkin (per MRO-008 spec) ──────────────────────────────────────────────

  // POST /api/workcards/:id/checkin
  {
    url: '/api/workcards/:id/checkin',
    method: 'post',
    response: () => ({
      code: 200,
      msg: 'ok',
      data: 3001,
      timestamp: Date.now()
    })
  },
  // POST /api/workcards/:id/checkout
  {
    url: '/api/workcards/:id/checkout',
    method: 'post',
    response: () => ({
      code: 200,
      msg: 'ok',
      data: null,
      timestamp: Date.now()
    })
  },
  // GET /api/workcards/:workcardId/checkins
  {
    url: '/api/workcards/:workcardId/checkins',
    method: 'get',
    response: ({ url }) => {
      const workcardId = parseInt(url.match(/\/api\/workcards\/([^/]+)\/checkins/)?.[1] || '100')
      return {
        code: 200,
        msg: 'ok',
        data: [
          { id: 3001, workcardId, userId: 1, userName: '张工', checkInTime: '2026-05-01T08:00:00', checkOutTime: '2026-05-01T17:00:00', location: 'A机库', deviceId: 'DEV001' },
          { id: 3002, workcardId, userId: 2, userName: '李工', checkInTime: '2026-05-01T08:15:00', checkOutTime: '2026-05-01T17:30:00', location: 'A机库', deviceId: 'DEV002' }
        ],
        timestamp: Date.now()
      }
    }
  }
]