const workcards = [
  { id: 'wc-001', no: 'WC-2024-001', title: 'B737-800发动机左侧高压涡轮叶片检查', aircraft_type: 'B737-800', registration: 'B-1234', base: '北京基地' },
  { id: 'wc-002', no: 'WC-2024-002', title: 'A320neo液压系统油液更换', aircraft_type: 'A320neo', registration: 'B-5678', base: '上海基地' },
  { id: 'wc-003', no: 'WC-2024-003', title: 'B777-300ER起落架减震器性能测试', aircraft_type: 'B777-300ER', registration: 'B-9012', base: '广州基地' },
  { id: 'wc-004', no: 'WC-2024-004', title: 'A350-900空调组件温度传感器校准', aircraft_type: 'A350-900', registration: 'B-3456', base: '北京基地' },
  { id: 'wc-005', no: 'WC-2024-005', title: 'B787-9飞控计算机软件升级', aircraft_type: 'B787-9', registration: 'B-7890', base: '上海基地' },
  { id: 'wc-006', no: 'WC-2024-006', title: 'B737-800APU启动系统故障排查', aircraft_type: 'B737-800', registration: 'B-2345', base: '广州基地' }
]

const materialRequests = [
  {
    id: 'mr-001',
    request_no: 'MR-2024-001',
    workcard_id: 'wc-001',
    workcard_no: 'WC-2024-001',
    title: 'B737-800发动机左侧高压涡轮叶片检查所需备件申请',
    requester: '张工',
    department: '发动机维修部',
    status: 'pending_approval',
    requested_at: '2024-05-15 09:30:00',
    items_count: 4,
    urgency: 'high'
  },
  {
    id: 'mr-002',
    request_no: 'MR-2024-002',
    workcard_id: 'wc-002',
    workcard_no: 'WC-2024-002',
    title: 'A320neo液压系统油液更换所需耗材申请',
    requester: '李工',
    department: '液压系统维修部',
    status: 'approved',
    requested_at: '2024-05-14 14:20:00',
    items_count: 3,
    urgency: 'normal'
  },
  {
    id: 'mr-003',
    request_no: 'MR-2024-003',
    workcard_id: 'wc-003',
    workcard_no: 'WC-2024-003',
    title: 'B777-300ER起落架减震器性能测试所需工具申请',
    requester: '王工',
    department: '起落架维修部',
    status: 'delivered',
    requested_at: '2024-05-13 11:15:00',
    items_count: 2,
    urgency: 'normal'
  },
  {
    id: 'mr-004',
    request_no: 'MR-2024-004',
    workcard_id: 'wc-004',
    workcard_no: 'WC-2024-004',
    title: 'A350-900空调组件温度传感器校准所需备件申请',
    requester: '陈工',
    department: '航电系统维修部',
    status: 'received',
    requested_at: '2024-05-12 16:45:00',
    items_count: 5,
    urgency: 'high'
  },
  {
    id: 'mr-005',
    request_no: 'MR-2024-005',
    workcard_id: 'wc-005',
    workcard_no: 'WC-2024-005',
    title: 'B787-9飞控计算机软件升级所需许可申请',
    requester: '刘工',
    department: '飞控系统维修部',
    status: 'rejected',
    requested_at: '2024-05-11 08:20:00',
    items_count: 1,
    urgency: 'low'
  },
  {
    id: 'mr-006',
    request_no: 'MR-2024-006',
    workcard_id: 'wc-006',
    workcard_no: 'WC-2024-006',
    title: 'B737-800APU启动系统故障排查所需备件申请',
    requester: '赵工',
    department: '动力系统维修部',
    status: 'pending_approval',
    requested_at: '2024-05-10 13:10:00',
    items_count: 6,
    urgency: 'high'
  }
]

const parts = [
  { part_no: 'P-737-1234', part_name: 'B737-800高压涡轮叶片', quantity: 2, unit: '片', warehouse_qty: 5, status: 'available' },
  { part_no: 'P-737-5678', part_name: 'B737-800发动机密封圈', quantity: 10, unit: '个', warehouse_qty: 25, status: 'available' },
  { part_no: 'P-A320-001', part_name: 'A320neo液压油', quantity: 20, unit: '升', warehouse_qty: 15, status: 'insufficient' },
  { part_no: 'P-B777-002', part_name: 'B777-300ER起落架减震器', quantity: 1, unit: '套', warehouse_qty: 0, status: 'ordered' },
  { part_no: 'P-A350-003', part_name: 'A350-900空调温度传感器', quantity: 2, unit: '个', warehouse_qty: 8, status: 'available' },
  { part_no: 'P-B787-004', part_name: 'B787-9飞控计算机软件许可', quantity: 1, unit: '套', warehouse_qty: 0, status: 'ordered' },
  { part_no: 'P-737-9012', part_name: 'B737-800APU启动马达', quantity: 1, unit: '台', warehouse_qty: 3, status: 'available' },
  { part_no: 'P-A320-3456', part_name: 'A320neo液压系统压力表', quantity: 2, unit: '个', warehouse_qty: 0, status: 'ordered' },
  { part_no: 'P-B777-7890', part_name: 'B777-300ER起落架轮胎', quantity: 4, unit: '条', warehouse_qty: 12, status: 'available' },
  { part_no: 'P-A350-2345', part_name: 'A350-900空调组件控制模块', quantity: 1, unit: '个', warehouse_qty: 0, status: 'insufficient' }
]

const timelineEvents = [
  { event: '申请提交', operator: '张工', time: '2024-05-15 09:30:00', note: '完成工作卡MR-2024-001的备件需求分析' },
  { event: '部门主管审批', operator: '李经理', time: '2024-05-15 11:20:00', note: '确认备件需求合理，同意提交至航材部' },
  { event: '航材部初审', operator: '王主任', time: '2024-05-15 14:45:00', note: '库存核查完成，部分备件需采购' }
]

function generateMaterialRequestDetail(id) {
  const request = materialRequests.find(r => r.id === id)
  if (!request) return null
  
  // Get items for this request
  let items = []
  if (request.id === 'mr-001') {
    items = [parts[0], parts[1]]
  } else if (request.id === 'mr-002') {
    items = [parts[2]]
  } else if (request.id === 'mr-003') {
    items = [parts[3]]
  } else if (request.id === 'mr-004') {
    items = [parts[4], parts[5], parts[6], parts[7]]
  } else if (request.id === 'mr-005') {
    items = [parts[5]]
  } else if (request.id === 'mr-006') {
    items = [parts[6], parts[7], parts[8], parts[9]]
  }
  
  const detail = {
    ...request,
    items,
    timeline: timelineEvents
  }
  
  // Add reject_reason for rejected requests
  if (request.status === 'rejected') {
    detail.reject_reason = '该软件许可需经民航局适航部门特别批准，当前申请材料不完整'
  }
  
  return detail
}

function generateWorkcardBom(workcardId) {
  const workcard = workcards.find(w => w.id === workcardId)
  if (!workcard) return null
  
  // BOM for different workcards
  let bomItems = []
  if (workcardId === 'wc-001') {
    bomItems = [
      { part_no: 'P-737-1234', part_name: 'B737-800高压涡轮叶片', quantity: 2, unit: '片', estimated_cost: 125000 },
      { part_no: 'P-737-5678', part_name: 'B737-800发动机密封圈', quantity: 10, unit: '个', estimated_cost: 8500 },
      { part_no: 'P-737-9012', part_name: 'B737-800APU启动马达', quantity: 1, unit: '台', estimated_cost: 280000 },
      { part_no: 'P-737-3456', part_name: 'B737-800发动机润滑油', quantity: 5, unit: '升', estimated_cost: 12000 }
    ]
  } else if (workcardId === 'wc-002') {
    bomItems = [
      { part_no: 'P-A320-001', part_name: 'A320neo液压油', quantity: 20, unit: '升', estimated_cost: 15000 },
      { part_no: 'P-A320-3456', part_name: 'A320neo液压系统压力表', quantity: 2, unit: '个', estimated_cost: 32000 },
      { part_no: 'P-A320-7890', part_name: 'A320neo液压管路接头', quantity: 8, unit: '个', estimated_cost: 6500 },
      { part_no: 'P-A320-2345', part_name: 'A320neo液压系统滤芯', quantity: 4, unit: '个', estimated_cost: 9800 }
    ]
  } else if (workcardId === 'wc-003') {
    bomItems = [
      { part_no: 'P-B777-002', part_name: 'B777-300ER起落架减震器', quantity: 1, unit: '套', estimated_cost: 450000 },
      { part_no: 'P-B777-7890', part_name: 'B777-300ER起落架轮胎', quantity: 4, unit: '条', estimated_cost: 120000 },
      { part_no: 'P-B777-3456', part_name: 'B777-300ER起落架刹车片', quantity: 8, unit: '片', estimated_cost: 85000 },
      { part_no: 'P-B777-9012', part_name: 'B777-300ER起落架液压作动筒', quantity: 2, unit: '个', estimated_cost: 220000 }
    ]
  } else if (workcardId === 'wc-004') {
    bomItems = [
      { part_no: 'P-A350-003', part_name: 'A350-900空调温度传感器', quantity: 2, unit: '个', estimated_cost: 18500 },
      { part_no: 'P-A350-2345', part_name: 'A350-900空调组件控制模块', quantity: 1, unit: '个', estimated_cost: 156000 },
      { part_no: 'P-A350-7890', part_name: 'A350-900空调制冷剂', quantity: 15, unit: '公斤', estimated_cost: 22000 },
      { part_no: 'P-A350-3456', part_name: 'A350-900空调系统压力开关', quantity: 3, unit: '个', estimated_cost: 7500 }
    ]
  } else if (workcardId === 'wc-005') {
    bomItems = [
      { part_no: 'P-B787-004', part_name: 'B787-9飞控计算机软件许可', quantity: 1, unit: '套', estimated_cost: 320000 },
      { part_no: 'P-B787-3456', part_name: 'B787-9飞控计算机硬件模块', quantity: 1, unit: '个', estimated_cost: 850000 },
      { part_no: 'P-B787-7890', part_name: 'B787-9飞控系统测试设备接口', quantity: 2, unit: '个', estimated_cost: 45000 },
      { part_no: 'P-B787-2345', part_name: 'B787-9飞控计算机冷却风扇', quantity: 2, unit: '个', estimated_cost: 12000 }
    ]
  } else if (workcardId === 'wc-006') {
    bomItems = [
      { part_no: 'P-737-9012', part_name: 'B737-800APU启动马达', quantity: 1, unit: '台', estimated_cost: 280000 },
      { part_no: 'P-737-2345', part_name: 'B737-800APU燃油控制单元', quantity: 1, unit: '个', estimated_cost: 156000 },
      { part_no: 'P-737-7890', part_name: 'B737-800APU排气温度传感器', quantity: 2, unit: '个', estimated_cost: 8500 },
      { part_no: 'P-737-3456', part_name: 'B737-800APU滑油滤芯', quantity: 4, unit: '个', estimated_cost: 3200 }
    ]
  }
  
  return { list: bomItems }
}

export default [
  {
    url: '/api/material-requests',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 10 } = query || {}
      const startIndex = (page - 1) * pageSize
      const endIndex = startIndex + pageSize
      
      return {
        code: 200,
        data: {
          list: materialRequests.slice(startIndex, endIndex),
          total: materialRequests.length,
          page: parseInt(page),
          pageSize: parseInt(pageSize)
        }
      }
    }
  },
  {
    url: '/api/material-requests/:id',
    method: 'get',
    response: ({ query, url }) => {
      const id = url.match(/\/api\/material-requests\/([^/]+)/)?.[1] || 'mr-001'
      const detail = generateMaterialRequestDetail(id)
      
      if (!detail) {
        return { code: 404, message: '物料申请未找到' }
      }
      
      return {
        code: 200,
        data: detail
      }
    }
  },
  {
    url: '/api/material-requests',
    method: 'post',
    response: () => ({
      code: 200,
      data: { id: 'mr-new-001', request_no: 'MR-NEW-001' }
    })
  },
  {
    url: '/api/material-requests/:id/approve',
    method: 'post',
    response: ({ url }) => {
      const id = url.match(/\/api\/material-requests\/([^/]+)/)?.[1] || 'mr-001'
      return {
        code: 200,
        data: { success: true, status: 'approved' }
      }
    }
  },
  {
    url: '/api/material-requests/:id/reject',
    method: 'post',
    response: ({ url }) => {
      const id = url.match(/\/api\/material-requests\/([^/]+)/)?.[1] || 'mr-001'
      return {
        code: 200,
        data: { success: true, status: 'rejected' }
      }
    }
  },
  {
    url: '/api/material-requests/:id/receive',
    method: 'post',
    response: ({ url }) => {
      const id = url.match(/\/api\/material-requests\/([^/]+)/)?.[1] || 'mr-001'
      return {
        code: 200,
        data: { success: true, status: 'received' }
      }
    }
  },
  {
    url: '/api/workcards/:id/bom',
    method: 'get',
    response: ({ url }) => {
      const workcardId = url.match(/\/api\/workcards\/([^/]+)/)?.[1] || 'wc-001'
      const bom = generateWorkcardBom(workcardId)
      
      if (!bom) {
        return { code: 404, message: '工卡BOM未找到' }
      }
      
      return {
        code: 200,
        data: bom
      }
    }
  }
]