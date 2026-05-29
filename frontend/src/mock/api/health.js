const aircraftTypes = ['B737-800', 'A320neo', 'B777-300ER', 'A350-900', 'B787-9']
const registrations = ['B-1234', 'B-5678', 'B-9012', 'B-3456', 'B-7890', 'B-2345', 'B-6789', 'B-0123']
const components = ['发动机左', '发动机右', '液压系统', '起落架', '空调组件', '飞控计算机', '燃油系统', 'APU']
const faultCodes = ['ENG-001', 'HYD-012', 'LDG-003', 'ACS-007', 'FCC-002', 'FUEL-005', 'APU-009', 'ENG-015']

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generateAircraftList() {
  return registrations.map((reg, i) => ({
    id: reg,
    aircraft_id: reg,
    aircraft_type: aircraftTypes[i % aircraftTypes.length],
    health_score: Math.floor(70 + Math.random() * 30),
    status: Math.random() > 0.2 ? 'normal' : 'warning',
    active_alerts: Math.floor(Math.random() * 5),
    last_check_time: `2026-05-${String(20 + (i % 5)).padStart(2, '0')} ${String(8 + i).padStart(2, '0')}:00:00`,
    flight_hours: Math.floor(10000 + Math.random() * 50000),
    base: ['北京基地', '上海基地', '广州基地'][i % 3]
  }))
}

function generateFaults(aircraftId) {
  const count = 5 + Math.floor(Math.random() * 10)
  const list = []
  for (let i = 0; i < count; i++) {
    list.push({
      id: 1000 + i,
      aircraft_id: aircraftId,
      fault_code: randomPick(faultCodes),
      severity: randomPick(['critical', 'major', 'minor']),
      component: randomPick(components),
      detected_at: `2026-05-${String(10 + i).padStart(2, '0')} 14:${String(i * 3).padStart(2, '0')}:00`,
      status: randomPick(['open', 'confirmed', 'resolved']),
      description: '传感器检测到异常振动频率超过阈值'
    })
  }
  return list
}

function generateAlerts() {
  const list = []
  for (let i = 0; i < 20; i++) {
    list.push({
      id: i + 1,
      aircraft_id: randomPick(registrations),
      alert_level: randomPick(['red', 'orange', 'yellow']),
      message: randomPick([
        '发动机振动值趋势异常，预计48小时内需检修',
        '液压油压力持续偏低，建议近期排查',
        '起落架减震器性能衰减，建议下次定检更换',
        'APU启动时间异常偏长',
        '空调组件温度传感器读数波动'
      ]),
      predicted_fault_time: `2026-05-${String(26 + (i % 5)).padStart(2, '0')} 10:00:00`,
      created_at: `2026-05-${String(20 + (i % 5)).padStart(2, '0')} ${String(8 + (i % 12)).padStart(2, '0')}:30:00`,
      acknowledged: i < 5
    })
  }
  return list
}

function generatePredictions(aircraftId) {
  return [
    {
      id: 1,
      aircraft_id: aircraftId,
      model_version: 'v2.1.0',
      predicted_at: '2026-05-25 08:00:00',
      result: {
        component: '发动机左',
        failure_probability: 0.73,
        predicted_time: '2026-06-10',
        severity: 'major',
        confidence: 0.85,
        recommendation: '建议在下次定检时重点检查发动机左侧高压涡轮叶片'
      }
    },
    {
      id: 2,
      aircraft_id: aircraftId,
      model_version: 'v2.1.0',
      predicted_at: '2026-05-25 08:00:00',
      result: {
        component: '液压系统',
        failure_probability: 0.45,
        predicted_time: '2026-07-01',
        severity: 'minor',
        confidence: 0.72,
        recommendation: '液压油品质下降趋势，建议按期更换'
      }
    }
  ]
}

function generateStatistics() {
  return {
    total_faults: 156,
    by_severity: { critical: 12, major: 48, minor: 96 },
    by_aircraft_type: aircraftTypes.map(t => ({ type: t, count: Math.floor(20 + Math.random() * 40) })),
    by_component: components.map(c => ({ component: c, count: Math.floor(5 + Math.random() * 30) })),
    by_month: [
      { month: '2026-01', count: 18 },
      { month: '2026-02', count: 22 },
      { month: '2026-03', count: 15 },
      { month: '2026-04', count: 28 },
      { month: '2026-05', count: 21 }
    ],
    avg_resolution_hours: 4.2
  }
}

export default [
  {
    url: '/api/health/aircraft',
    method: 'get',
    response: ({ query }) => {
      const list = generateAircraftList()
      const { page = 1, pageSize = 10, status, base } = query || {}
      let filtered = list
      if (status) filtered = filtered.filter(a => a.status === status)
      if (base) filtered = filtered.filter(a => a.base === base)
      return {
        code: 200,
        data: {
          list: filtered.slice((page - 1) * pageSize, page * pageSize),
          total: filtered.length
        }
      }
    }
  },
  {
    url: '/api/health/aircraft/:id',
    method: 'get',
    response: ({ query, url }) => {
      const id = url.match(/\/api\/health\/aircraft\/([^/]+)/)?.[1] || 'B-1234'
      const list = generateAircraftList()
      const aircraft = list.find(a => a.id === id) || list[0]
      return {
        code: 200,
        data: {
          ...aircraft,
          sensors: [
            { name: '发动机振动', value: 2.3, unit: 'mm/s', status: 'normal', threshold: 5.0 },
            { name: '液压油压', value: 2850, unit: 'psi', status: 'normal', threshold: 2500 },
            { name: '燃油温度', value: 42, unit: '°C', status: 'normal', threshold: 60 },
            { name: 'APU排气温度', value: 620, unit: '°C', status: 'warning', threshold: 600 }
          ],
          recent_flights: 12,
          next_scheduled_check: '2026-06-01'
        }
      }
    }
  },
  {
    url: '/api/health/aircraft/:id/faults',
    method: 'get',
    response: ({ query, url }) => {
      const id = url.match(/\/api\/health\/aircraft\/([^/]+)/)?.[1] || 'B-1234'
      const faults = generateFaults(id)
      const { page = 1, pageSize = 10 } = query || {}
      return {
        code: 200,
        data: {
          list: faults.slice((page - 1) * pageSize, page * pageSize),
          total: faults.length
        }
      }
    }
  },
  {
    url: '/api/health/aircraft/:id/predictions',
    method: 'get',
    response: ({ url }) => {
      const id = url.match(/\/api\/health\/aircraft\/([^/]+)/)?.[1] || 'B-1234'
      return { code: 200, data: generatePredictions(id) }
    }
  },
  {
    url: '/api/health/alerts',
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
  {
    url: '/api/health/alerts/:id/acknowledge',
    method: 'put',
    response: () => ({ code: 200, message: '预警已确认' })
  },
  {
    url: '/api/health/alert-rules',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        list: [
          { id: 1, name: '发动机振动超限', metric: 'engine_vibration', operator: '>', threshold: 5.0, alert_level: 'red', enabled: true },
          { id: 2, name: '液压油压偏低', metric: 'hydraulic_pressure', operator: '<', threshold: 2500, alert_level: 'orange', enabled: true },
          { id: 3, name: 'APU排气温度过高', metric: 'apu_egt', operator: '>', threshold: 600, alert_level: 'yellow', enabled: true }
        ],
        total: 3
      }
    })
  },
  {
    url: '/api/health/alert-rules',
    method: 'post',
    response: () => ({ code: 200, message: '规则创建成功' })
  },
  {
    url: '/api/health/alert-rules/:id',
    method: 'put',
    response: () => ({ code: 200, message: '规则修改成功' })
  },
  {
    url: '/api/health/alert-rules/:id',
    method: 'delete',
    response: () => ({ code: 200, message: '规则删除成功' })
  },
  {
    url: '/api/health/statistics',
    method: 'get',
    response: () => ({ code: 200, data: generateStatistics() })
  },
  {
    url: '/api/health/export',
    method: 'get',
    response: () => ({ code: 200, message: '导出成功' })
  }
]
