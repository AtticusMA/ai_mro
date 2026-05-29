const aircraftTypes = ['B737-800', 'A320neo', 'B777-300ER', 'A350-900', 'B787-9']
const registrations = ['B-1234', 'B-5678', 'B-9012', 'B-3456', 'B-7890', 'B-2345', 'B-6789', 'B-0123']
const workcardNos = ['WC-2024-001', 'WC-2024-002', 'WC-2024-003', 'WC-2024-004', 'WC-2024-005', 'WC-2024-006', 'WC-2024-007', 'WC-2024-008', 'WC-2024-009', 'WC-2024-010']
const mechanics = [
  { name: '张伟', no: 'MECH-001' },
  { name: '李强', no: 'MECH-002' },
  { name: '王芳', no: 'MECH-003' },
  { name: '陈静', no: 'MECH-004' },
  { name: '刘洋', no: 'MECH-005' }
]
const checkTypes = ['机体', '发动机', '航电']
const priorities = ['urgent', 'normal']

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generatePendingList() {
  const list = []
  for (let i = 0; i < 12; i++) {
    const workcardNo = workcardNos[i % workcardNos.length]
    const aircraftReg = registrations[i % registrations.length]
    const aircraftType = aircraftTypes[i % aircraftTypes.length]
    const mechanic = mechanics[i % mechanics.length]
    
    list.push({
      id: i + 1,
      workcard_id: i + 1000,
      workcard_no: workcardNo,
      workcard_title: `${aircraftType} ${i % 3 === 0 ? 'C检' : i % 3 === 1 ? '定检' : '航线维护'} - ${i % 2 === 0 ? '左发' : '右发'}更换`,
      aircraft_reg: aircraftReg,
      aircraft_type: aircraftType,
      mechanic_name: mechanic.name,
      mechanic_no: mechanic.no,
      completed_at: `2024-01-${String(10 + (i % 15)).padStart(2, '0')} ${String(8 + (i % 8)).padStart(2, '0')}:30:00`,
      check_type: checkTypes[i % checkTypes.length],
      has_ncr: i % 3 === 0,
      ncr_count: i % 3 === 0 ? Math.floor(1 + Math.random() * 3) : 0,
      priority: priorities[i % priorities.length]
    })
  }
  return list
}

function generatePendingDetail(id) {
  const pendingList = generatePendingList()
  const item = pendingList.find(p => p.id === parseInt(id)) || pendingList[0]
  
  // Generate execution records
  const executionRecords = []
  for (let i = 0; i < 5; i++) {
    executionRecords.push({
      step_no: i + 1,
      title: [
        '拆卸旧发动机',
        '安装新发动机',
        '管路连接与密封检查',
        '电气系统连接测试',
        '整机功能测试'
      ][i % 5],
      confirmed_by: mechanics[i % mechanics.length].name,
      confirmed_at: `2024-01-${String(10 + (i % 15)).padStart(2, '0')} ${String(9 + (i % 8)).padStart(2, '0')}:15:00`,
      notes: i % 2 === 0 ? '符合技术标准，无异常' : '发现轻微渗漏，已处理',
      photos: i % 3 === 0 ? [
        `https://example.com/photos/${item.workcard_no}-step${i+1}-1.jpg`,
        `https://example.com/photos/${item.workcard_no}-step${i+1}-2.jpg`
      ] : []
    })
  }
  
  return {
    ...item,
    execution_records: executionRecords,
    workcard_description: `对${item.aircraft_type} ${item.aircraft_reg}执行${item.workcard_title}工作。包括发动机拆装、系统连接、功能测试等全部工序。所有工序均按AMM手册要求完成。`
  }
}

function generateNcrList() {
  const list = []
  for (let i = 0; i < 8; i++) {
    const workcardNo = workcardNos[i % workcardNos.length]
    const severity = ['高', '中', '低'][i % 3]
    const status = ['open', 'in_rectification', 'closed'][i % 3]
    
    list.push({
      id: i + 1,
      ncr_no: `NCR-2024-${String(i + 1).padStart(3, '0')}`,
      workcard_no: workcardNo,
      title: [
        '发动机振动值超标',
        '液压管路接头渗漏',
        '航电设备安装位置偏差',
        '起落架收放时间超限',
        'APU启动故障',
        '空调组件制冷效果不佳',
        '飞控计算机软件版本不匹配',
        '燃油系统压力波动'
      ][i % 8],
      severity: severity,
      status: status,
      created_by: mechanics[i % mechanics.length].name,
      created_at: `2024-01-${String(5 + (i % 15)).padStart(2, '0')} ${String(14 + (i % 6)).padStart(2, '0')}:00:00`,
      description: [
        '发动机在慢车状态振动值达到8.2mm/s，超过手册规定的5.0mm/s限制',
        '左主起落架液压管路接头处发现持续渗漏，每分钟约3滴',
        'ADIRU航电设备安装支架孔位偏差0.8mm，超出允许公差0.5mm',
        '右主起落架收上时间达12.5秒，超过手册规定最大时间10秒',
        'APU启动过程中出现EGT超温警告，温度峰值达780°C',
        '客舱空调组件出风口温度仅18°C，低于设定温度22°C',
        '飞行控制计算机软件版本为V2.1.0，但当前构型要求V2.3.0',
        '燃油系统压力在巡航阶段波动范围达±150psi，超出±50psi允许范围'
      ][i % 8],
      assigned_to: ['张工', '李工', '王工', '陈工'][i % 4],
      due_date: `2024-01-${String(20 + (i % 10)).padStart(2, '0')}`
    })
  }
  return list
}

function generateNcrId() {
  return `ncr-${Date.now().toString().slice(-6)}`
}

export default [
  {
    url: '/api/workcards/pending-sign',
    method: 'get',
    response: ({ query }) => {
      const list = generatePendingList()
      const { page = 1, pageSize = 10 } = query || {}
      const startIndex = (page - 1) * pageSize
      const paginatedList = list.slice(startIndex, startIndex + pageSize)
      
      return {
        code: 200,
        data: {
          list: paginatedList,
          total: list.length,
          page: parseInt(page),
          pageSize: parseInt(pageSize)
        }
      }
    }
  },
  {
    url: '/api/workcards/:id/quality-sign',
    method: 'get',
    response: ({ url }) => {
      const id = url.match(/\/api\/workcards\/([^/]+)\/quality-sign/)?.[1] || '1'
      const detail = generatePendingDetail(id)
      
      return {
        code: 200,
        data: detail
      }
    }
  },
  {
    url: '/api/workcards/:id/quality-sign',
    method: 'post',
    response: ({ url }) => {
      const id = url.match(/\/api\/workcards\/([^/]+)\/quality-sign/)?.[1] || '1'
      
      // Randomly decide between pass and NCR creation
      if (Math.random() > 0.7) {
        return {
          code: 200,
          data: {
            success: true,
            result: 'ncr_created',
            ncr_id: `NCR-2024-${String(Math.floor(Math.random() * 1000)).padStart(3, '0')}`
          }
        }
      } else {
        return {
          code: 200,
          data: {
            success: true,
            signed_by: 'QC-001',
            signed_at: '2024-01-15T14:00:00',
            result: 'pass'
          }
        }
      }
    }
  },
  {
    url: '/api/ncr',
    method: 'get',
    response: ({ query }) => {
      const list = generateNcrList()
      const { page = 1, pageSize = 10 } = query || {}
      const startIndex = (page - 1) * pageSize
      const paginatedList = list.slice(startIndex, startIndex + pageSize)
      
      return {
        code: 200,
        data: {
          list: paginatedList,
          total: list.length
        }
      }
    }
  },
  {
    url: '/api/ncr',
    method: 'post',
    response: () => {
      return {
        code: 200,
        data: {
          id: generateNcrId(),
          ncr_no: `NCR-NEW-${String(Math.floor(Math.random() * 1000)).padStart(3, '0')}`
        }
      }
    }
  },
  {
    url: '/api/ncr/:id/close',
    method: 'post',
    response: ({ url }) => {
      const id = url.match(/\/api\/ncr\/([^/]+)/)?.[1] || '1'
      
      return {
        code: 200,
        data: {
          success: true,
          status: 'closed'
        }
      }
    }
  }
]