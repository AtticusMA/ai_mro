export default [
  {
    url: '/api/workcards',
    method: 'get',
    response: ({ query }) => {
      const list = [
        { id: 1, card_no: 'WC-2026-001', title: 'B-1234 A检工卡', card_type: 'line', aircraft_id: 'B-1234', priority: 'normal', status: 'in_progress', due_date: '2026-05-30', progress: 60 },
        { id: 2, card_no: 'WC-2026-002', title: 'B-5678 发动机排故', card_type: 'troubleshoot', aircraft_id: 'B-5678', priority: 'urgent', status: 'issued', due_date: '2026-05-26', progress: 0 },
        { id: 3, card_no: 'WC-2026-003', title: 'B-9012 C检工卡', card_type: 'heavy', aircraft_id: 'B-9012', priority: 'normal', status: 'completed', due_date: '2026-05-20', progress: 100 },
        { id: 4, card_no: 'WC-2026-004', title: 'B-3456 航线检查', card_type: 'line', aircraft_id: 'B-3456', priority: 'low', status: 'draft', due_date: null, progress: 0 }
      ]
      const { page = 1, pageSize = 10, status } = query || {}
      let filtered = list
      if (status) filtered = filtered.filter(w => w.status === status)
      return { code: 200, data: { list: filtered.slice((page - 1) * pageSize, page * pageSize), total: filtered.length } }
    }
  },
  {
    url: '/api/workcards',
    method: 'post',
    response: () => ({ code: 200, message: '工卡创建成功' })
  },
  {
    url: '/api/workcards/:id',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        id: 1, card_no: 'WC-2026-001', title: 'B-1234 A检工卡', card_type: 'line', status: 'in_progress',
        steps: [
          { id: 1, step_no: 1, description: '检查发动机外观', status: 'completed', completed_by: '张伟', completed_at: '2026-05-25 08:30:00' },
          { id: 2, step_no: 2, description: '检查液压系统压力', status: 'completed', completed_by: '张伟', completed_at: '2026-05-25 09:15:00' },
          { id: 3, step_no: 3, description: '检查起落架收放', status: 'in_progress', completed_by: null, completed_at: null },
          { id: 4, step_no: 4, description: '检查空调系统', status: 'pending', completed_by: null, completed_at: null }
        ]
      }
    })
  },
  {
    url: '/api/workcards/:id',
    method: 'put',
    response: () => ({ code: 200, message: '工卡更新成功' })
  },
  {
    url: '/api/workcards/:id/submit',
    method: 'post',
    response: () => ({ code: 200, message: '已提交审批' })
  },
  {
    url: '/api/workcards/:id/approve',
    method: 'post',
    response: () => ({ code: 200, message: '审批通过' })
  },
  {
    url: '/api/workcards/:id/issue',
    method: 'post',
    response: () => ({ code: 200, message: '工卡已下发' })
  },
  {
    url: '/api/workcards/:id/steps/:stepId/complete',
    method: 'put',
    response: () => ({ code: 200, message: '步骤已完成' })
  },
  {
    url: '/api/workcards/:id/sign',
    method: 'post',
    response: () => ({ code: 200, message: '签署成功', data: { blockchain_hash: '0xabc123...' } })
  },
  {
    url: '/api/workcards/:id/signatures',
    method: 'get',
    response: () => ({
      code: 200,
      data: [
        { id: 1, signer: '张伟', signature_type: 'execute', signed_at: '2026-05-25 09:15:00', blockchain_hash: '0xabc123' },
        { id: 2, signer: '李主管', signature_type: 'inspect', signed_at: '2026-05-25 10:00:00', blockchain_hash: '0xdef456' }
      ]
    })
  },
  {
    url: '/api/workcards/:id/blockchain-verify',
    method: 'get',
    response: () => ({ code: 200, data: { verified: true, chain_name: 'MRO-Fabric', block_height: 12580 } })
  },
  {
    url: '/api/workcards/progress',
    method: 'get',
    response: () => ({
      code: 200,
      data: { total: 4, completed: 1, in_progress: 1, issued: 1, draft: 1 }
    })
  },
  {
    url: '/api/workcards/alerts',
    method: 'get',
    response: () => ({
      code: 200,
      data: { list: [{ id: 2, card_no: 'WC-2026-002', title: 'B-5678 发动机排故', due_date: '2026-05-26', hours_remaining: 18 }], total: 1 }
    })
  }
]
