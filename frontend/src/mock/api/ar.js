const registrations = ['B-1234', 'B-5678', 'B-9012', 'B-3456', 'B-7890']
const inspectors = ['张伟', '李强', '王磊', '刘洋', '陈明']
const experts = ['赵总工', '孙高工', '周技术经理']
const anomalyTypes = ['盖板未闭合', '安全销缺失', '管路接头渗漏', '紧固件松动', '外表面损伤']
const routeTemplates = ['航线绕机-标准', '航线绕机-雨后', '定检入库-A检', '发动机专项']

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generateInspections() {
  const list = []
  for (let i = 0; i < 15; i++) {
    const status = ['pending', 'in_progress', 'completed'][i % 3]
    list.push({
      id: i + 1,
      aircraft_id: randomPick(registrations),
      inspector_id: i + 1,
      inspector_name: inspectors[i % inspectors.length],
      route_template: randomPick(routeTemplates),
      status,
      anomaly_count: status === 'completed' ? Math.floor(Math.random() * 4) : 0,
      started_at: status !== 'pending' ? `2026-05-${String(20 + (i % 5)).padStart(2, '0')} ${String(8 + i % 8).padStart(2, '0')}:00:00` : null,
      completed_at: status === 'completed' ? `2026-05-${String(20 + (i % 5)).padStart(2, '0')} ${String(9 + i % 8).padStart(2, '0')}:30:00` : null,
      created_at: `2026-05-${String(20 + (i % 5)).padStart(2, '0')} 07:00:00`
    })
  }
  return list
}

function generateAnomalies(taskId) {
  const count = 2 + Math.floor(Math.random() * 3)
  const list = []
  for (let i = 0; i < count; i++) {
    list.push({
      id: taskId * 100 + i,
      task_id: taskId,
      anomaly_type: randomPick(anomalyTypes),
      confidence: (0.85 + Math.random() * 0.14).toFixed(4),
      snapshot_url: `/mock/snapshots/anomaly_${taskId}_${i}.jpg`,
      detected_at: `2026-05-22 ${String(8 + i).padStart(2, '0')}:${String(15 + i * 10).padStart(2, '0')}:00`
    })
  }
  return list
}

function generateSessions() {
  const list = []
  for (let i = 0; i < 12; i++) {
    const status = ['waiting', 'active', 'ended'][i % 3]
    list.push({
      id: i + 1,
      caller_id: i + 1,
      caller_name: inspectors[i % inspectors.length],
      expert_id: status !== 'waiting' ? i + 100 : null,
      expert_name: status !== 'waiting' ? experts[i % experts.length] : null,
      status,
      recording_url: status === 'ended' ? `/mock/recordings/session_${i + 1}.mp4` : null,
      duration_seconds: status === 'ended' ? 300 + Math.floor(Math.random() * 1800) : null,
      created_at: `2026-05-${String(20 + (i % 5)).padStart(2, '0')} ${String(9 + i % 8).padStart(2, '0')}:00:00`
    })
  }
  return list
}

function generateArchives() {
  const list = []
  for (let i = 0; i < 20; i++) {
    list.push({
      id: i + 1,
      task_id: i < 10 ? i + 1 : null,
      session_id: i >= 10 ? i - 9 : null,
      source_type: i < 10 ? '巡检录像' : '远程协作录像',
      aircraft_id: randomPick(registrations),
      file_url: `/mock/archives/video_${i + 1}.mp4`,
      duration_seconds: 600 + Math.floor(Math.random() * 3600),
      created_at: `2026-05-${String(15 + (i % 10)).padStart(2, '0')} 10:00:00`
    })
  }
  return list
}

export default [
  {
    url: '/api/ar/inspections',
    method: 'get',
    response: ({ query }) => {
      const list = generateInspections()
      const { page = 1, pageSize = 10, status } = query || {}
      let filtered = list
      if (status) filtered = filtered.filter(t => t.status === status)
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
    url: '/api/ar/inspections',
    method: 'post',
    response: () => ({ code: 200, message: '巡检任务创建成功' })
  },
  {
    url: '/api/ar/inspections/:id/start',
    method: 'put',
    response: () => ({ code: 200, message: '巡检已开始' })
  },
  {
    url: '/api/ar/inspections/:id/complete',
    method: 'put',
    response: () => ({ code: 200, message: '巡检已完成' })
  },
  {
    url: '/api/ar/inspections/:id/anomalies',
    method: 'get',
    response: ({ url }) => {
      const id = Number(url.match(/inspections\/(\d+)/)?.[1] || 1)
      return { code: 200, data: generateAnomalies(id) }
    }
  },
  {
    url: '/api/ar/sessions',
    method: 'get',
    response: ({ query }) => {
      const list = generateSessions()
      const { page = 1, pageSize = 10, status } = query || {}
      let filtered = list
      if (status) filtered = filtered.filter(s => s.status === status)
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
    url: '/api/ar/sessions',
    method: 'post',
    response: () => ({ code: 200, message: '协作会话已创建', data: { id: Date.now() } })
  },
  {
    url: '/api/ar/sessions/:id/join',
    method: 'put',
    response: () => ({ code: 200, message: '已加入会话' })
  },
  {
    url: '/api/ar/sessions/:id/end',
    method: 'put',
    response: () => ({ code: 200, message: '会话已结束' })
  },
  {
    url: '/api/ar/sessions/:id/annotations',
    method: 'post',
    response: () => ({ code: 200, message: '标注已发送' })
  },
  {
    url: '/api/ar/archives',
    method: 'get',
    response: ({ query }) => {
      const list = generateArchives()
      const { page = 1, pageSize = 10 } = query || {}
      return {
        code: 200,
        data: {
          list: list.slice((page - 1) * pageSize, page * pageSize),
          total: list.length
        }
      }
    }
  },
  {
    url: '/api/ar/archives/:id/playback',
    method: 'get',
    response: ({ url }) => {
      const id = Number(url.match(/archives\/(\d+)/)?.[1] || 1)
      return {
        code: 200,
        data: { id, playback_url: `/mock/archives/video_${id}.mp4`, duration_seconds: 1200 }
      }
    }
  }
]
