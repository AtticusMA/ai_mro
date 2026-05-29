let _scenarios = [
  { id: 1, name: '航线绕机检查', category: 'line_check', difficulty: 'beginner', duration_minutes: 30, status: 'published' },
  { id: 2, name: '发动机拆装', category: 'engine', difficulty: 'advanced', duration_minutes: 90, status: 'published' },
  { id: 3, name: '紧急迫降处置', category: 'emergency', difficulty: 'advanced', duration_minutes: 45, status: 'published' },
  { id: 4, name: '起落架更换', category: 'component', difficulty: 'intermediate', duration_minutes: 60, status: 'draft' },
  { id: 5, name: '液压系统排故', category: 'component', difficulty: 'intermediate', duration_minutes: 50, status: 'draft' },
  { id: 6, name: '电气线路检测', category: 'line_check', difficulty: 'beginner', duration_minutes: 25, status: 'published' }
]
let _nextScenarioId = 7

export default [
  {
    url: '/api/training/scenarios',
    method: 'get',
    response: ({ query }) => {
      let filtered = [..._scenarios]
      const { page = 1, pageSize = 10, name, category, difficulty, status } = query || {}
      if (name) filtered = filtered.filter(s => s.name.includes(name))
      if (category) filtered = filtered.filter(s => s.category === category)
      if (difficulty) filtered = filtered.filter(s => s.difficulty === difficulty)
      if (status) filtered = filtered.filter(s => s.status === status)
      const total = filtered.length
      const list = filtered.slice((page - 1) * pageSize, page * pageSize)
      return { code: 200, data: { list, total } }
    }
  },
  {
    url: '/api/training/scenarios',
    method: 'post',
    response: ({ body }) => {
      const newItem = { ...body, id: _nextScenarioId++, status: 'draft' }
      _scenarios.push(newItem)
      return { code: 200, message: '场景创建成功', data: { id: newItem.id } }
    }
  },
  {
    url: '/api/training/scenarios/:id',
    method: 'put',
    response: ({ body, url }) => {
      const id = Number(url.match(/scenarios\/(\d+)/)?.[1])
      const idx = _scenarios.findIndex(s => s.id === id)
      if (idx === -1) return { code: 4800, message: '培训场景不存在' }
      _scenarios[idx] = { ..._scenarios[idx], ...body }
      return { code: 200, message: '场景更新成功' }
    }
  },
  {
    url: '/api/training/scenarios/:id/publish',
    method: 'put',
    response: ({ url }) => {
      const id = Number(url.match(/scenarios\/(\d+)/)?.[1])
      const item = _scenarios.find(s => s.id === id)
      if (!item) return { code: 4800, message: '培训场景不存在' }
      item.status = item.status === 'published' ? 'draft' : 'published'
      return { code: 200, message: '场景状态已更新' }
    }
  },
  {
    url: '/api/training/scenarios/:id',
    method: 'delete',
    response: ({ url }) => {
      const id = Number(url.match(/scenarios\/(\d+)/)?.[1])
      const idx = _scenarios.findIndex(s => s.id === id)
      if (idx === -1) return { code: 4800, message: '培训场景不存在' }
      _scenarios.splice(idx, 1)
      return { code: 200, message: '场景删除成功' }
    }
  },
  {
    url: '/api/training/trainees',
    method: 'get',
    response: ({ query }) => {
      const list = [
        { id: 1, user_id: 10, name: '张伟', skill_level: 'junior', total_training_hours: 24.5, last_assessment_date: '2026-05-20' },
        { id: 2, user_id: 11, name: '李强', skill_level: 'mid', total_training_hours: 86.0, last_assessment_date: '2026-05-18' },
        { id: 3, user_id: 12, name: '王磊', skill_level: 'senior', total_training_hours: 210.0, last_assessment_date: '2026-05-22' },
        { id: 4, user_id: 13, name: '赵刚', skill_level: 'junior', total_training_hours: 12.0, last_assessment_date: '2026-05-15' },
        { id: 5, user_id: 14, name: '孙明', skill_level: 'mid', total_training_hours: 65.5, last_assessment_date: '2026-05-21' }
      ]
      let filtered = [...list]
      const { skill_level, name: qName } = query || {}
      if (skill_level) filtered = filtered.filter(t => t.skill_level === skill_level)
      if (qName) filtered = filtered.filter(t => t.name.includes(qName))
      return { code: 200, data: { list: filtered, total: filtered.length } }
    }
  },
  {
    url: '/api/training/trainees/:id/profile',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        id: 1, name: '张伟', skill_level: 'junior', total_training_hours: 24.5,
        last_assessment_date: '2026-05-20',
        skills: [
          { name: '绕机检查', score: 85 }, { name: '发动机维修', score: 45 },
          { name: '液压系统', score: 60 }, { name: '电气系统', score: 72 },
          { name: '应急处置', score: 55 }
        ],
        recent_sessions: [
          { id: 1, scenario_name: '航线绕机检查', mode: 'vr', score: 82, status: 'completed', started_at: '2026-05-22 09:00' },
          { id: 3, scenario_name: '紧急迫降处置', mode: 'vr', score: 68, status: 'completed', started_at: '2026-05-18 14:00' }
        ]
      }
    })
  },
  {
    url: '/api/training/sessions',
    method: 'get',
    response: ({ query }) => {
      const list = [
        { id: 1, scenario_name: '航线绕机检查', trainee_name: '张伟', mode: 'vr', status: 'completed', started_at: '2026-05-22 09:00:00', score: 82 },
        { id: 2, scenario_name: '发动机拆装', trainee_name: '李强', mode: 'vr', status: 'in_progress', started_at: '2026-05-25 10:00:00', score: null },
        { id: 3, scenario_name: '紧急迫降处置', trainee_name: '王磊', mode: 'collaborative', status: 'completed', started_at: '2026-05-21 14:00:00', score: 91 },
        { id: 4, scenario_name: '起落架更换', trainee_name: '赵刚', mode: 'vr', status: 'completed', started_at: '2026-05-20 08:30:00', score: 75 },
        { id: 5, scenario_name: '航线绕机检查', trainee_name: '孙明', mode: 'vr', status: 'in_progress', started_at: '2026-05-28 09:00:00', score: null }
      ]
      const { page = 1, pageSize = 10, status } = query || {}
      let filtered = [...list]
      if (status) filtered = filtered.filter(s => s.status === status)
      return { code: 200, data: { list: filtered.slice((page - 1) * pageSize, page * pageSize), total: filtered.length } }
    }
  },
  {
    url: '/api/training/sessions',
    method: 'post',
    response: () => ({ code: 200, message: '培训任务已创建' })
  },
  {
    url: '/api/training/assessments/:sessionId',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        session_id: 1,
        trainee_name: '张伟',
        scenario_name: '航线绕机检查',
        overall_score: 83.3,
        metrics: [
          { name: '步骤正确性', score: 85, detail: '18/22步正确' },
          { name: '工具使用规范', score: 78, detail: '工具选择正确率89%' },
          { name: '操作时间', score: 90, detail: '28分钟（标准30分钟）' },
          { name: '安全意识', score: 80, detail: '安全检查执行率80%' }
        ]
      }
    })
  },
  {
    url: '/api/training/reports/individual/:traineeId',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        trainee_name: '张伟',
        overall_score: 72,
        trend: [
          { month: '2026-01', score: 60 },
          { month: '2026-02', score: 65 },
          { month: '2026-03', score: 68 },
          { month: '2026-04', score: 72 },
          { month: '2026-05', score: 75 }
        ],
        sessions_completed: 8,
        weak_points: ['发动机维修', '应急处置'],
        recommendations: '建议加强发动机拆装场景训练频次，提升应急处置能力'
      }
    })
  },
  {
    url: '/api/training/reports/overview',
    method: 'get',
    response: () => ({
      code: 200,
      data: { total_trainees: 45, total_sessions: 320, avg_score: 78.5, pass_rate: 0.85, active_scenarios: 3 }
    })
  }
]
