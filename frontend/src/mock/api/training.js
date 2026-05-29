export default [
  {
    url: '/api/training/scenarios',
    method: 'get',
    response: ({ query }) => {
      const list = [
        { id: 1, name: '航线绕机检查', category: 'line_check', difficulty: 'beginner', duration_minutes: 30, status: 'published' },
        { id: 2, name: '发动机拆装', category: 'engine', difficulty: 'advanced', duration_minutes: 90, status: 'published' },
        { id: 3, name: '紧急迫降处置', category: 'emergency', difficulty: 'advanced', duration_minutes: 45, status: 'published' },
        { id: 4, name: '起落架更换', category: 'component', difficulty: 'intermediate', duration_minutes: 60, status: 'draft' }
      ]
      const { page = 1, pageSize = 10 } = query || {}
      return { code: 200, data: { list: list.slice((page - 1) * pageSize, page * pageSize), total: list.length } }
    }
  },
  {
    url: '/api/training/scenarios',
    method: 'post',
    response: () => ({ code: 200, message: '场景创建成功' })
  },
  {
    url: '/api/training/trainees',
    method: 'get',
    response: () => ({
      code: 200,
      data: { list: [
        { id: 1, user_id: 10, name: '张伟', skill_level: 'junior', total_training_hours: 24.5, last_assessment_date: '2026-05-20' },
        { id: 2, user_id: 11, name: '李强', skill_level: 'mid', total_training_hours: 86.0, last_assessment_date: '2026-05-18' },
        { id: 3, user_id: 12, name: '王磊', skill_level: 'senior', total_training_hours: 210.0, last_assessment_date: '2026-05-22' }
      ], total: 3 }
    })
  },
  {
    url: '/api/training/trainees/:id/profile',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        id: 1, name: '张伟', skill_level: 'junior', total_training_hours: 24.5,
        skills: [
          { name: '绕机检查', score: 85 }, { name: '发动机维修', score: 45 },
          { name: '液压系统', score: 60 }, { name: '电气系统', score: 72 },
          { name: '应急处置', score: 55 }
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
        { id: 3, scenario_name: '紧急迫降处置', trainee_name: '王磊', mode: 'collaborative', status: 'completed', started_at: '2026-05-21 14:00:00', score: 91 }
      ]
      const { page = 1, pageSize = 10 } = query || {}
      return { code: 200, data: { list: list.slice((page - 1) * pageSize, page * pageSize), total: list.length } }
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
      data: { session_id: 1, metrics: [
        { name: '步骤正确性', score: 85 }, { name: '工具使用规范', score: 78 },
        { name: '操作时间', score: 90 }, { name: '安全意识', score: 80 }
      ] }
    })
  },
  {
    url: '/api/training/reports/individual/:traineeId',
    method: 'get',
    response: () => ({
      code: 200,
      data: { trainee_name: '张伟', overall_score: 72, trend: [60, 65, 68, 72, 75, 78], sessions_completed: 8 }
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
