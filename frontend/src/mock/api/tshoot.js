const faultCodes = ['ENG-001', 'HYD-012', 'LDG-003', 'ACS-007', 'FCC-002', 'FUEL-005']
const aircraftTypes = ['B737-800', 'A320neo', 'B777-300ER']

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generateKnowledgeBases() {
  return [
    { id: 1, name: 'B737-800 维修手册', aircraft_type: 'B737-800', doc_count: 128, vector_count: 45600, status: 'ready', updated_at: '2026-05-20 10:00:00' },
    { id: 2, name: 'A320neo 维修手册', aircraft_type: 'A320neo', doc_count: 96, vector_count: 33200, status: 'ready', updated_at: '2026-05-18 14:00:00' },
    { id: 3, name: 'B777-300ER 维修手册', aircraft_type: 'B777-300ER', doc_count: 156, vector_count: 52800, status: 'indexing', updated_at: '2026-05-24 09:00:00' },
    { id: 4, name: '通用排故经验库', aircraft_type: '通用', doc_count: 312, vector_count: 89000, status: 'ready', updated_at: '2026-05-22 16:00:00' }
  ]
}

function generateReports() {
  const list = []
  for (let i = 0; i < 15; i++) {
    list.push({
      id: i + 1,
      fault_code: randomPick(faultCodes),
      fault_description: randomPick(['发动机振动超标', '液压系统压力异常', '起落架收放缓慢', '空调温度失控', '飞控计算机告警']),
      aircraft_type: randomPick(aircraftTypes),
      status: ['completed', 'processing', 'completed'][i % 3],
      confidence: (0.75 + Math.random() * 0.2).toFixed(4),
      created_at: `2026-05-${String(15 + (i % 10)).padStart(2, '0')} ${String(8 + i % 10).padStart(2, '0')}:00:00`
    })
  }
  return list
}

function generateHistory() {
  const list = []
  for (let i = 0; i < 20; i++) {
    list.push({
      id: i + 1,
      aircraft_id: randomPick(['B-1234', 'B-5678', 'B-9012', 'B-3456']),
      fault_code: randomPick(faultCodes),
      repair_action: randomPick(['更换高压涡轮叶片', '补充液压油并排气', '调整起落架收放机构', '更换温控阀', '重置飞控计算机软件']),
      component_replaced: i % 3 === 0 ? randomPick(['涡轮叶片', '液压泵', '温控阀', '传感器']) : null,
      repaired_at: `2026-${String(1 + (i % 5)).padStart(2, '0')}-${String(10 + i).padStart(2, '0')} 14:00:00`
    })
  }
  return list
}

export default [
  {
    url: '/api/tshoot/knowledge-bases',
    method: 'get',
    response: () => ({ code: 200, data: { list: generateKnowledgeBases(), total: 4 } })
  },
  {
    url: '/api/tshoot/knowledge-bases',
    method: 'post',
    response: () => ({ code: 200, message: '知识库创建成功' })
  },
  {
    url: '/api/tshoot/knowledge-bases/:id/documents',
    method: 'post',
    response: () => ({ code: 200, message: '文档上传成功，正在向量化处理' })
  },
  {
    url: '/api/tshoot/knowledge-bases/:id/documents/:docId',
    method: 'delete',
    response: () => ({ code: 200, message: '文档删除成功' })
  },
  {
    url: '/api/tshoot/query',
    method: 'post',
    response: () => ({ code: 200, message: '查询已提交', data: { query_id: Date.now() } })
  },
  {
    url: '/api/tshoot/query/:id/result',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        status: 'completed',
        report: {
          diagnosis: '根据故障代码和症状描述，初步判断为高压涡轮第一级叶片疲劳裂纹导致的异常振动。',
          steps: [
            '1. 使用孔探仪检查高压涡轮第一级叶片前缘',
            '2. 对比振动频谱与历史数据，确认是否为特征频率偏移',
            '3. 如确认裂纹，按AMM 72-00-00执行叶片更换',
            '4. 更换后进行地面试车验证振动值回归正常范围'
          ],
          references: [
            { source: 'AMM 72-00-00', chapter: '72-31-00', page: 'P201-P205', relevance: 0.95 },
            { source: 'TSM 72-00', chapter: 'Troubleshooting', page: 'T72-15', relevance: 0.88 }
          ],
          confidence: 0.87,
          similar_cases: 3
        }
      }
    })
  },
  {
    url: '/api/tshoot/history',
    method: 'get',
    response: ({ query }) => {
      const list = generateHistory()
      const { page = 1, pageSize = 10 } = query || {}
      return { code: 200, data: { list: list.slice((page - 1) * pageSize, page * pageSize), total: list.length } }
    }
  },
  {
    url: '/api/tshoot/history/statistics',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        total_repairs: 320,
        by_fault_code: faultCodes.map(c => ({ code: c, count: Math.floor(20 + Math.random() * 60) })),
        avg_repair_hours: 6.5,
        top_components: ['涡轮叶片', '液压泵', '温控阀', '传感器', '密封圈']
      }
    })
  },
  {
    url: '/api/tshoot/reports',
    method: 'get',
    response: ({ query }) => {
      const list = generateReports()
      const { page = 1, pageSize = 10 } = query || {}
      return { code: 200, data: { list: list.slice((page - 1) * pageSize, page * pageSize), total: list.length } }
    }
  },
  {
    url: '/api/tshoot/reports/:id',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        id: 1,
        fault_code: 'ENG-001',
        fault_description: '发动机振动超标',
        diagnosis: '高压涡轮叶片疲劳裂纹',
        steps: ['孔探检查', '频谱对比', '叶片更换', '试车验证'],
        references: [{ source: 'AMM 72-00-00', chapter: '72-31-00', page: 'P201' }],
        confidence: 0.87,
        created_at: '2026-05-20 10:00:00'
      }
    })
  },
  {
    url: '/api/tshoot/export/:id',
    method: 'get',
    response: () => ({ code: 200, message: '导出成功' })
  }
]
