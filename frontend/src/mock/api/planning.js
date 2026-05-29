const aircraftTypes = ['B737-800', 'A320neo', 'B777-300ER', 'A350-900', 'B787-9', 'A330-300', 'B767-300F']
const registrations = ['B-1234', 'B-5678', 'B-9012', 'B-3456', 'B-7890', 'B-2345', 'B-6789', 'B-0123', 'B-4567', 'B-8901']
const checkTypes = ['C检', 'D检', '定检', '年检', '特殊检查']
const statuses = ['planning', 'in_progress', 'completed']
const personnel = [
  { id: 1, name: '张伟', employee_no: 'EMP001', department: '发动机车间', position: '高级工程师' },
  { id: 2, name: '李娜', employee_no: 'EMP002', department: '结构车间', position: '工程师' },
  { id: 3, name: '王强', employee_no: 'EMP003', department: '航电车间', position: '技术主管' },
  { id: 4, name: '陈静', employee_no: 'EMP004', department: '液压系统', position: '工程师' },
  { id: 5, name: '刘洋', employee_no: 'EMP005', department: 'APU车间', position: '高级技师' },
  { id: 6, name: '赵敏', employee_no: 'EMP006', department: '起落架车间', position: '工程师' },
  { id: 7, name: '孙磊', employee_no: 'EMP007', department: '空调系统', position: '技术员' },
  { id: 8, name: '周婷', employee_no: 'EMP008', department: '飞控系统', position: '工程师' }
]

function generateTaskPackages() {
  const tasks = []
  const now = new Date()
  
  for (let i = 0; i < 12; i++) {
    const reg = registrations[i % registrations.length]
    const aircraftType = aircraftTypes[i % aircraftTypes.length]
    const checkType = checkTypes[i % checkTypes.length]
    const status = statuses[i % statuses.length]
    
    // Generate dates
    const plannedStart = new Date(now)
    plannedStart.setDate(now.getDate() + 10 + i * 5)
    
    const plannedEnd = new Date(plannedStart)
    plannedEnd.setDate(plannedStart.getDate() + 3 + Math.floor(i / 2))
    
    const actualStart = status === 'completed' ? new Date(plannedStart) : null
    
    // Workcard counts
    const workcardCount = 8 + Math.floor(Math.random() * 12)
    const completedCount = status === 'completed' ? workcardCount : Math.floor(workcardCount * 0.3) + Math.floor(i / 2)
    
    tasks.push({
      id: `TP-${String(2024 + Math.floor(i / 4)).padStart(4, '0')}-${String(i + 1).padStart(3, '0')}`,
      task_no: `TP-${String(2024 + Math.floor(i / 4)).padStart(4, '0')}-${String(i + 1).padStart(3, '0')}`,
      aircraft_reg: reg,
      aircraft_type: aircraftType,
      check_type: checkType,
      status: status,
      planned_start: `${plannedStart.getFullYear()}-${String(plannedStart.getMonth() + 1).padStart(2, '0')}-${String(plannedStart.getDate()).padStart(2, '0')} 08:00:00`,
      planned_end: `${plannedEnd.getFullYear()}-${String(plannedEnd.getMonth() + 1).padStart(2, '0')}-${String(plannedEnd.getDate()).padStart(2, '0')} 17:00:00`,
      actual_start: actualStart ? `${actualStart.getFullYear()}-${String(actualStart.getMonth() + 1).padStart(2, '0')}-${String(actualStart.getDate()).padStart(2, '0')} 08:00:00` : null,
      workcard_count: workcardCount,
      completed_count: completedCount,
      assigned_personnel: Math.floor(Math.random() * 5) + 2
    })
  }
  
  return tasks
}

function generateWorkcards(taskId) {
  const workcards = []
  const types = ['例行工卡', '非例行工卡', '排故工卡', '改装工卡', '适航指令']
  const statuses = ['pending', 'in_progress', 'completed', 'on_hold']
  
  for (let i = 0; i < 5 + Math.floor(Math.random() * 5); i++) {
    const type = types[i % types.length]
    const status = statuses[i % statuses.length]
    const estimatedHours = 2 + Math.floor(Math.random() * 15)
    const actualHours = status === 'completed' ? estimatedHours : Math.floor(estimatedHours * 0.3) + Math.floor(i / 2)
    
    workcards.push({
      id: `${taskId}-WC-${String(i + 1).padStart(3, '0')}`,
      workcard_no: `WC-${String(2024 + Math.floor(i / 3)).padStart(4, '0')}-${String(i + 1).padStart(4, '0')}`,
      title: [
        '发动机孔探检查',
        '液压系统渗漏检查',
        '起落架收放测试',
        'APU性能测试',
        '空调组件更换',
        '飞控计算机软件升级',
        '燃油系统清洗',
        '客舱氧气系统测试',
        '雷达罩检查与修复',
        '机翼前缘除冰系统检查'
      ][i % 10],
      type: type,
      status: status,
      assigned_to: personnel[Math.floor(Math.random() * personnel.length)].name,
      assigned_to_id: personnel[Math.floor(Math.random() * personnel.length)].id,
      estimated_hours: estimatedHours,
      actual_hours: actualHours
    })
  }
  
  return workcards
}

function generateTaskDetail(taskId) {
  const tasks = generateTaskPackages()
  const task = tasks.find(t => t.id === taskId) || tasks[0]
  const workcards = generateWorkcards(taskId)
  
  // Calculate summary
  const totalWorkcards = workcards.length
  const completed = workcards.filter(w => w.status === 'completed').length
  const inProgress = workcards.filter(w => w.status === 'in_progress').length
  const pending = workcards.filter(w => w.status === 'pending').length
  const totalHoursEstimated = workcards.reduce((sum, wc) => sum + wc.estimated_hours, 0)
  const totalHoursActual = workcards.reduce((sum, wc) => sum + wc.actual_hours, 0)
  
  return {
    ...task,
    workcards: workcards,
    summary: {
      total_workcards: totalWorkcards,
      completed: completed,
      in_progress: inProgress,
      pending: pending,
      total_hours_estimated: totalHoursEstimated,
      total_hours_actual: totalHoursActual
    }
  }
}

function generateWorkloadData() {
  const workload = []
  
  for (let i = 0; i < 8; i++) {
    const person = personnel[i]
    const activeWorkcards = Math.floor(Math.random() * 8) + 1
    const totalHours = Math.floor(Math.random() * 40) + 5
    
    const workcards = []
    for (let j = 0; j < activeWorkcards; j++) {
      workcards.push({
        workcard_no: `WC-${String(2024).padStart(4, '0')}-${String(j + 1).padStart(4, '0')}`,
        title: [
          '发动机孔探检查',
          '液压系统渗漏检查',
          '起落架收放测试',
          'APU性能测试',
          '空调组件更换',
          '飞控计算机软件升级',
          '燃油系统清洗',
          '客舱氧气系统测试',
          '雷达罩检查与修复',
          '机翼前缘除冰系统检查'
        ][j % 10],
        status: ['pending', 'in_progress', 'completed'][Math.floor(Math.random() * 3)],
        hours: Math.floor(Math.random() * 10) + 2
      })
    }
    
    workload.push({
      personnel_id: person.id,
      name: person.name,
      employee_no: person.employee_no,
      department: person.department,
      active_workcards: activeWorkcards,
      total_hours: totalHours,
      workcards: workcards
    })
  }
  
  return workload
}

function generateAvailablePersonnel() {
  const available = []
  
  for (let i = 0; i < 12; i++) {
    const person = personnel[i % personnel.length]
    const qualified = Math.random() > 0.3 // 70% qualified
    const qualificationReason = qualified 
      ? '持有CAAC维修执照，有效期至2026年12月'
      : Math.random() > 0.5 
        ? '维修执照已过期（2025年6月到期）'
        : '未完成年度复训要求'
    
    available.push({
      id: person.id + i,
      name: person.name,
      employee_no: person.employee_no + String(i + 1),
      department: person.department,
      position: person.position,
      qualified: qualified,
      qualification_reason: qualificationReason
    })
  }
  
  return available
}

export default [
  {
    url: '/api/planning/tasks',
    method: 'get',
    response: ({ query }) => {
      const tasks = generateTaskPackages()
      const { page = 1, pageSize = 10 } = query || {}
      const start = (page - 1) * pageSize
      const end = start + pageSize
      
      return {
        code: 200,
        data: {
          list: tasks.slice(start, end),
          total: tasks.length,
          page: parseInt(page),
          pageSize: parseInt(pageSize)
        }
      }
    }
  },
  {
    url: '/api/planning/tasks/:id',
    method: 'get',
    response: ({ url }) => {
      const id = url.match(/\/api\/planning\/tasks\/([^/]+)/)?.[1] || 'TP-2024-001'
      const detail = generateTaskDetail(id)
      
      return {
        code: 200,
        data: detail
      }
    }
  },
  {
    url: '/api/planning/tasks',
    method: 'post',
    response: ({ body }) => {
      const newId = `TP-NEW-${String(Math.floor(Math.random() * 1000)).padStart(3, '0')}`
      
      return {
        code: 200,
        data: {
          id: newId,
          ...body
        }
      }
    }
  },
  {
    url: '/api/planning/tasks/:id',
    method: 'put',
    response: () => ({
      code: 200,
      data: { success: true }
    })
  },
  {
    url: '/api/planning/tasks/:id',
    method: 'delete',
    response: () => ({
      code: 200,
      data: { success: true }
    })
  },
  {
    url: '/api/planning/workcards/:id/assign',
    method: 'post',
    response: ({ url, body }) => {
      const id = url.match(/\/api\/planning\/workcards\/([^/]+)/)?.[1] || 'TP-2024-001-WC-001'
      const person = personnel[Math.floor(Math.random() * personnel.length)]
      
      return {
        code: 200,
        data: {
          success: true,
          assigned_to: person.name,
          assigned_to_id: person.id
        }
      }
    }
  },
  {
    url: '/api/planning/workload',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        list: generateWorkloadData()
      }
    })
  },
  {
    url: '/api/planning/available-personnel',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        list: generateAvailablePersonnel()
      }
    })
  }
]