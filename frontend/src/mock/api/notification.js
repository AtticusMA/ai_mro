const _notifications = [
  { id: 1, type: 'health_alert', title: '发动机振动值超标预警', content: 'B-6138 左发动机 N1 振动值达到 4.2mil，超过预警阈值 3.8mil', is_read: false, created_at: '2026-05-29 08:30:00', source_id: 101 },
  { id: 2, type: 'workcard_overdue', title: '工卡即将超时', content: 'WC-2026-0523 B737-800 定检工卡已执行 22h，预计超时', is_read: false, created_at: '2026-05-29 07:45:00', source_id: 201 },
  { id: 3, type: 'license_expiring', title: '证照即将到期', content: '张伟 B737 放行授权证照将于 2026-06-05 到期，请及时续期', is_read: false, created_at: '2026-05-28 16:00:00', source_id: 301 },
  { id: 4, type: 'training_assignment', title: '新培训任务分配', content: '您已被分配"发动机拆装"VR培训任务，请于 5/30 前完成', is_read: false, created_at: '2026-05-28 14:20:00', source_id: 401 },
  { id: 5, type: 'health_alert', title: 'APU 启动异常', content: 'B-6215 APU 启动时间延长至 58s，超过正常范围（≤45s）', is_read: true, created_at: '2026-05-28 10:15:00', source_id: 102 },
  { id: 6, type: 'workcard_overdue', title: '工卡已超时', content: 'WC-2026-0518 排故工卡已超时 4h，请关注', is_read: true, created_at: '2026-05-27 18:00:00', source_id: 202 },
  { id: 7, type: 'license_expiring', title: '证照到期提醒', content: '李强 A320 维修授权证照将于 2026-06-10 到期', is_read: false, created_at: '2026-05-27 09:00:00', source_id: 302 },
  { id: 8, type: 'training_assignment', title: '协同培训邀请', content: '王磊邀请您参加"紧急迫降处置"协同培训，时间 5/30 14:00', is_read: true, created_at: '2026-05-26 15:30:00', source_id: 402 },
  { id: 9, type: 'health_alert', title: '液压系统压力波动', content: 'B-6138 液压系统 A 压力波动频繁，建议检查', is_read: false, created_at: '2026-05-26 11:00:00', source_id: 103 },
  { id: 10, type: 'workcard_overdue', title: '工卡签署提醒', content: 'WC-2026-0520 航线工卡待质检签署', is_read: false, created_at: '2026-05-25 17:00:00', source_id: 203 }
]

export default [
  {
    url: '/api/notifications',
    method: 'get',
    response: ({ query }) => {
      let filtered = [..._notifications]
      const { page = 1, pageSize = 10, type, is_read } = query || {}
      if (type) filtered = filtered.filter(n => n.type === type)
      if (is_read !== undefined && is_read !== '') {
        const readVal = is_read === 'true' || is_read === true
        filtered = filtered.filter(n => n.is_read === readVal)
      }
      const total = filtered.length
      const list = filtered.slice((page - 1) * pageSize, page * pageSize)
      return { code: 200, data: { list, total } }
    }
  },
  {
    url: '/api/notifications/unread-count',
    method: 'get',
    response: () => {
      const count = _notifications.filter(n => !n.is_read).length
      return { code: 200, data: { count } }
    }
  },
  {
    url: '/api/notifications/:id/read',
    method: 'put',
    response: ({ url }) => {
      const id = Number(url.match(/notifications\/(\d+)\/read/)?.[1])
      const item = _notifications.find(n => n.id === id)
      if (item) item.is_read = true
      return { code: 200, message: '已标记为已读' }
    }
  },
  {
    url: '/api/notifications/read-all',
    method: 'put',
    response: () => {
      _notifications.forEach(n => { n.is_read = true })
      return { code: 200, message: '全部已标记为已读' }
    }
  }
]
