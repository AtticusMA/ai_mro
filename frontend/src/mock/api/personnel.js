// 飞机维修人员数据
const personnelList = [
  {
    id: 1,
    employee_no: 'MRO-001',
    name: '张伟',
    department: '发动机维修部',
    position: '机械师',
    license_count: 3,
    expiring_count: 1,
    expired_count: 0,
    status: 'active'
  },
  {
    id: 2,
    employee_no: 'MRO-002',
    name: '李娜',
    department: '电气系统部',
    position: '电气工程师',
    license_count: 4,
    expiring_count: 0,
    expired_count: 0,
    status: 'active'
  },
  {
    id: 3,
    employee_no: 'MRO-003',
    name: '王强',
    department: '结构维修部',
    position: '无损检测员',
    license_count: 5,
    expiring_count: 2,
    expired_count: 0,
    status: 'active'
  },
  {
    id: 4,
    employee_no: 'MRO-004',
    name: '陈静',
    department: '航电系统部',
    position: '质检员',
    license_count: 2,
    expiring_count: 0,
    expired_count: 1,
    status: 'inactive'
  },
  {
    id: 5,
    employee_no: 'MRO-005',
    name: '刘洋',
    department: '发动机维修部',
    position: '机械师',
    license_count: 4,
    expiring_count: 1,
    expired_count: 0,
    status: 'active'
  },
  {
    id: 6,
    employee_no: 'MRO-006',
    name: '赵敏',
    department: '电气系统部',
    position: '电气工程师',
    license_count: 3,
    expiring_count: 0,
    expired_count: 0,
    status: 'active'
  },
  {
    id: 7,
    employee_no: 'MRO-007',
    name: '孙浩',
    department: '结构维修部',
    position: '无损检测员',
    license_count: 4,
    expiring_count: 1,
    expired_count: 0,
    status: 'active'
  },
  {
    id: 8,
    employee_no: 'MRO-008',
    name: '周婷',
    department: '航电系统部',
    position: '质检员',
    license_count: 3,
    expiring_count: 0,
    expired_count: 0,
    status: 'active'
  }
]

// 人员详细信息（含执照）
const personnelDetails = {
  1: {
    id: 1,
    employee_no: 'MRO-001',
    name: '张伟',
    department: '发动机维修部',
    position: '机械师',
    contact_info: '13800138000',
    hire_date: '2020-03-15',
    seniority: '高级',
    status: 'active',
    licenses: [
      {
        id: 101,
        license_name: '飞机维修执照(AME)',
        license_no: 'CAAC-AME-2020-001',
        issuing_authority: '中国民航局',
        issue_date: '2020-03-15',
        expiry_date: '2025-03-15',
        status: 'valid'
      },
      {
        id: 102,
        license_name: '特种设备操作证',
        license_no: 'TS-2021-001',
        issuing_authority: '国家市场监督管理总局',
        issue_date: '2021-06-20',
        expiry_date: '2026-06-20',
        status: 'valid'
      },
      {
        id: 103,
        license_name: '高处作业证',
        license_no: 'GZ-2022-001',
        issuing_authority: '应急管理部',
        issue_date: '2022-09-10',
        expiry_date: '2025-09-10',
        status: 'expiring'
      }
    ]
  },
  2: {
    id: 2,
    employee_no: 'MRO-002',
    name: '李娜',
    department: '电气系统部',
    position: '电气工程师',
    contact_info: '13900139000',
    hire_date: '2019-07-01',
    seniority: '高级',
    status: 'active',
    licenses: [
      {
        id: 201,
        license_name: '飞机维修执照(AME)',
        license_no: 'CAAC-AME-2019-002',
        issuing_authority: '中国民航局',
        issue_date: '2019-07-01',
        expiry_date: '2024-07-01',
        status: 'expired'
      },
      {
        id: 202,
        license_name: '电工证',
        license_no: 'DG-2020-002',
        issuing_authority: '应急管理部',
        issue_date: '2020-11-15',
        expiry_date: '2025-11-15',
        status: 'valid'
      },
      {
        id: 203,
        license_name: '无损检测证',
        license_no: 'WSD-2021-002',
        issuing_authority: '中国特种设备检验协会',
        issue_date: '2021-02-20',
        expiry_date: '2026-02-20',
        status: 'valid'
      },
      {
        id: 204,
        license_name: '焊工证',
        license_no: 'HJ-2022-002',
        issuing_authority: '人力资源和社会保障部',
        issue_date: '2022-05-10',
        expiry_date: '2027-05-10',
        status: 'valid'
      }
    ]
  },
  3: {
    id: 3,
    employee_no: 'MRO-003',
    name: '王强',
    department: '结构维修部',
    position: '无损检测员',
    contact_info: '13700137000',
    hire_date: '2021-01-10',
    seniority: '中级',
    status: 'active',
    licenses: [
      {
        id: 301,
        license_name: '无损检测证',
        license_no: 'WSD-2021-003',
        issuing_authority: '中国特种设备检验协会',
        issue_date: '2021-01-10',
        expiry_date: '2026-01-10',
        status: 'valid'
      },
      {
        id: 302,
        license_name: '飞机维修执照(AME)',
        license_no: 'CAAC-AME-2021-003',
        issuing_authority: '中国民航局',
        issue_date: '2021-04-20',
        expiry_date: '2026-04-20',
        status: 'valid'
      },
      {
        id: 303,
        license_name: '特种设备操作证',
        license_no: 'TS-2021-003',
        issuing_authority: '国家市场监督管理总局',
        issue_date: '2021-08-15',
        expiry_date: '2026-08-15',
        status: 'valid'
      },
      {
        id: 304,
        license_name: '高处作业证',
        license_no: 'GZ-2022-003',
        issuing_authority: '应急管理部',
        issue_date: '2022-03-25',
        expiry_date: '2025-03-25',
        status: 'expiring'
      },
      {
        id: 305,
        license_name: '焊工证',
        license_no: 'HJ-2022-003',
        issuing_authority: '人力资源和社会保障部',
        issue_date: '2022-07-10',
        expiry_date: '2027-07-10',
        status: 'valid'
      }
    ]
  },
  4: {
    id: 4,
    employee_no: 'MRO-004',
    name: '陈静',
    department: '航电系统部',
    position: '质检员',
    contact_info: '13600136000',
    hire_date: '2018-05-20',
    seniority: '高级',
    status: 'inactive',
    licenses: [
      {
        id: 401,
        license_name: '飞机维修执照(AME)',
        license_no: 'CAAC-AME-2018-004',
        issuing_authority: '中国民航局',
        issue_date: '2018-05-20',
        expiry_date: '2023-05-20',
        status: 'expired'
      },
      {
        id: 402,
        license_name: '电工证',
        license_no: 'DG-2019-004',
        issuing_authority: '应急管理部',
        issue_date: '2019-09-15',
        expiry_date: '2024-09-15',
        status: 'expired'
      }
    ]
  },
  5: {
    id: 5,
    employee_no: 'MRO-005',
    name: '刘洋',
    department: '发动机维修部',
    position: '机械师',
    contact_info: '13500135000',
    hire_date: '2022-02-10',
    seniority: '初级',
    status: 'active',
    licenses: [
      {
        id: 501,
        license_name: '飞机维修执照(AME)',
        license_no: 'CAAC-AME-2022-005',
        issuing_authority: '中国民航局',
        issue_date: '2022-02-10',
        expiry_date: '2027-02-10',
        status: 'valid'
      },
      {
        id: 502,
        license_name: '特种设备操作证',
        license_no: 'TS-2022-005',
        issuing_authority: '国家市场监督管理总局',
        issue_date: '2022-06-25',
        expiry_date: '2027-06-25',
        status: 'valid'
      },
      {
        id: 503,
        license_name: '高处作业证',
        license_no: 'GZ-2023-005',
        issuing_authority: '应急管理部',
        issue_date: '2023-01-15',
        expiry_date: '2026-01-15',
        status: 'valid'
      },
      {
        id: 504,
        license_name: '焊工证',
        license_no: 'HJ-2023-005',
        issuing_authority: '人力资源和社会保障部',
        issue_date: '2023-04-20',
        expiry_date: '2028-04-20',
        status: 'valid'
      }
    ]
  },
  6: {
    id: 6,
    employee_no: 'MRO-006',
    name: '赵敏',
    department: '电气系统部',
    position: '电气工程师',
    contact_info: '13400134000',
    hire_date: '2021-08-15',
    seniority: '中级',
    status: 'active',
    licenses: [
      {
        id: 601,
        license_name: '飞机维修执照(AME)',
        license_no: 'CAAC-AME-2021-006',
        issuing_authority: '中国民航局',
        issue_date: '2021-08-15',
        expiry_date: '2026-08-15',
        status: 'valid'
      },
      {
        id: 602,
        license_name: '电工证',
        license_no: 'DG-2022-006',
        issuing_authority: '应急管理部',
        issue_date: '2022-02-10',
        expiry_date: '2027-02-10',
        status: 'valid'
      },
      {
        id: 603,
        license_name: '无损检测证',
        license_no: 'WSD-2022-006',
        issuing_authority: '中国特种设备检验协会',
        issue_date: '2022-05-20',
        expiry_date: '2027-05-20',
        status: 'valid'
      }
    ]
  },
  7: {
    id: 7,
    employee_no: 'MRO-007',
    name: '孙浩',
    department: '结构维修部',
    position: '无损检测员',
    contact_info: '13300133000',
    hire_date: '2020-11-05',
    seniority: '中级',
    status: 'active',
    licenses: [
      {
        id: 701,
        license_name: '无损检测证',
        license_no: 'WSD-2020-007',
        issuing_authority: '中国特种设备检验协会',
        issue_date: '2020-11-05',
        expiry_date: '2025-11-05',
        status: 'expiring'
      },
      {
        id: 702,
        license_name: '飞机维修执照(AME)',
        license_no: 'CAAC-AME-2021-007',
        issuing_authority: '中国民航局',
        issue_date: '2021-03-15',
        expiry_date: '2026-03-15',
        status: 'valid'
      },
      {
        id: 703,
        license_name: '特种设备操作证',
        license_no: 'TS-2021-007',
        issuing_authority: '国家市场监督管理总局',
        issue_date: '2021-07-20',
        expiry_date: '2026-07-20',
        status: 'valid'
      },
      {
        id: 704,
        license_name: '高处作业证',
        license_no: 'GZ-2022-007',
        issuing_authority: '应急管理部',
        issue_date: '2022-10-10',
        expiry_date: '2025-10-10',
        status: 'expiring'
      }
    ]
  },
  8: {
    id: 8,
    employee_no: 'MRO-008',
    name: '周婷',
    department: '航电系统部',
    position: '质检员',
    contact_info: '13200132000',
    hire_date: '2022-04-20',
    seniority: '初级',
    status: 'active',
    licenses: [
      {
        id: 801,
        license_name: '飞机维修执照(AME)',
        license_no: 'CAAC-AME-2022-008',
        issuing_authority: '中国民航局',
        issue_date: '2022-04-20',
        expiry_date: '2027-04-20',
        status: 'valid'
      },
      {
        id: 802,
        license_name: '电工证',
        license_no: 'DG-2022-008',
        issuing_authority: '应急管理部',
        issue_date: '2022-08-15',
        expiry_date: '2027-08-15',
        status: 'valid'
      },
      {
        id: 803,
        license_name: '无损检测证',
        license_no: 'WSD-2023-008',
        issuing_authority: '中国特种设备检验协会',
        issue_date: '2023-01-20',
        expiry_date: '2028-01-20',
        status: 'valid'
      }
    ]
  }
}

// 执照类型选项
const licenseTypes = [
  { id: 1, name: '飞机维修执照(AME)' },
  { id: 2, name: '无损检测证' },
  { id: 3, name: '特种设备操作证' },
  { id: 4, name: '高处作业证' },
  { id: 5, name: '电工证' },
  { id: 6, name: '焊工证' }
]

// 执照到期预警
const licenseAlerts = [
  {
    id: 1,
    personnel_id: 1,
    personnel_name: '张伟',
    employee_no: 'MRO-001',
    license_name: '高处作业证',
    license_no: 'GZ-2022-001',
    expiry_date: '2025-09-10',
    days_remaining: 120,
    status: 'notice'
  },
  {
    id: 2,
    personnel_id: 2,
    personnel_name: '李娜',
    employee_no: 'MRO-002',
    license_name: '飞机维修执照(AME)',
    license_no: 'CAAC-AME-2019-002',
    expiry_date: '2024-07-01',
    days_remaining: -15,
    status: 'expired'
  },
  {
    id: 3,
    personnel_id: 3,
    personnel_name: '王强',
    employee_no: 'MRO-003',
    license_name: '高处作业证',
    license_no: 'GZ-2022-003',
    expiry_date: '2025-03-25',
    days_remaining: 30,
    status: 'critical'
  },
  {
    id: 4,
    personnel_id: 4,
    personnel_name: '陈静',
    employee_no: 'MRO-004',
    license_name: '飞机维修执照(AME)',
    license_no: 'CAAC-AME-2018-004',
    expiry_date: '2023-05-20',
    days_remaining: -365,
    status: 'expired'
  },
  {
    id: 5,
    personnel_id: 4,
    personnel_name: '陈静',
    employee_no: 'MRO-004',
    license_name: '电工证',
    license_no: 'DG-2019-004',
    expiry_date: '2024-09-15',
    days_remaining: -120,
    status: 'expired'
  },
  {
    id: 6,
    personnel_id: 7,
    personnel_name: '孙浩',
    employee_no: 'MRO-007',
    license_name: '无损检测证',
    license_no: 'WSD-2020-007',
    expiry_date: '2025-11-05',
    days_remaining: 60,
    status: 'warning'
  },
  {
    id: 7,
    personnel_id: 7,
    personnel_name: '孙浩',
    employee_no: 'MRO-007',
    license_name: '高处作业证',
    license_no: 'GZ-2022-007',
    expiry_date: '2025-10-10',
    days_remaining: 45,
    status: 'warning'
  }
]

// 资格检查结果
const qualificationCheckResults = {
  '1': { qualified: true, reason: '具备飞机维修执照和相关资质，符合B737-800机型维修要求' },
  '2': { qualified: false, reason: '飞机维修执照已过期，需重新认证后方可执行维修任务' },
  '3': { qualified: true, reason: '具备无损检测证和飞机维修执照，符合所有结构检测要求' },
  '4': { qualified: false, reason: '所有执照均已过期，不具备任何维修资格' },
  '5': { qualified: true, reason: '具备完整的飞机维修执照和特种设备操作证，符合发动机维修要求' },
  '6': { qualified: true, reason: '具备飞机维修执照和电工证，符合航电系统维修要求' },
  '7': { qualified: true, reason: '具备无损检测证和飞机维修执照，符合结构检测要求' },
  '8': { qualified: true, reason: '具备飞机维修执照和电工证，符合航电系统质检要求' }
}

export default [
  // GET /api/personnel/list - 分页人员列表
  {
    url: '/api/personnel/list',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20, department, position, status } = query || {}
      let filtered = [...personnelList]
      
      if (department) {
        filtered = filtered.filter(p => p.department === department)
      }
      if (position) {
        filtered = filtered.filter(p => p.position === position)
      }
      if (status) {
        filtered = filtered.filter(p => p.status === status)
      }
      
      const start = (page - 1) * pageSize
      const end = start + pageSize
      
      return {
        code: 200,
        data: {
          list: filtered.slice(start, end),
          total: filtered.length,
          page: Number(page),
          pageSize: Number(pageSize)
        }
      }
    }
  },
  
  // GET /api/personnel/:id - 单个人员详情
  {
    url: '/api/personnel/:id',
    method: 'get',
    response: ({ params }) => {
      const id = Number(params?.id || '1')
      const detail = personnelDetails[id]
      
      if (!detail) {
        return {
          code: 404,
          message: '人员不存在'
        }
      }
      
      return {
        code: 200,
        data: detail
      }
    }
  },
  
  // POST /api/personnel - 创建人员
  {
    url: '/api/personnel',
    method: 'post',
    response: ({ body }) => {
      const newId = personnelList.length + 1
      const newPersonnel = {
        id: newId,
        employee_no: `MRO-${String(newId).padStart(3, '0')}`,
        ...body,
        status: 'active'
      }
      
      // Add to our in-memory list
      personnelList.push(newPersonnel)
      
      return {
        code: 200,
        data: newPersonnel
      }
    }
  },
  
  // PUT /api/personnel/:id - 更新人员
  {
    url: '/api/personnel/:id',
    method: 'put',
    response: ({ params, body }) => {
      const id = Number(params?.id || '1')
      const index = personnelList.findIndex(p => p.id === id)
      
      if (index === -1) {
        return {
          code: 404,
          message: '人员不存在'
        }
      }
      
      personnelList[index] = {
        ...personnelList[index],
        ...body
      }
      
      return {
        code: 200,
        data: personnelList[index]
      }
    }
  },
  
  // DELETE /api/personnel/:id - 删除人员
  {
    url: '/api/personnel/:id',
    method: 'delete',
    response: ({ params }) => {
      const id = Number(params?.id || '1')
      const index = personnelList.findIndex(p => p.id === id)
      
      if (index === -1) {
        return {
          code: 404,
          message: '人员不存在'
        }
      }
      
      personnelList.splice(index, 1)
      
      return {
        code: 200,
        message: '删除成功'
      }
    }
  },
  
  // GET /api/personnel/alerts - 执照到期预警
  {
    url: '/api/personnel/alerts',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20, status } = query || {}
      let filtered = [...licenseAlerts]
      
      if (status) {
        filtered = filtered.filter(alert => alert.status === status)
      }
      
      const start = (page - 1) * pageSize
      const end = start + pageSize
      
      return {
        code: 200,
        data: {
          list: filtered.slice(start, end),
          total: filtered.length,
          page: Number(page),
          pageSize: Number(pageSize)
        }
      }
    }
  },
  
  // GET /api/personnel/license-types - 执照类型选项
  {
    url: '/api/personnel/license-types',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: {
          list: licenseTypes
        }
      }
    }
  },
  
  // GET /api/personnel/check-qualification - 检查资格
  {
    url: '/api/personnel/check-qualification',
    method: 'get',
    response: ({ query }) => {
      const { personnelId, workcardType } = query || {}
      
      if (!personnelId) {
        return {
          code: 400,
          message: '缺少 personnelId 参数'
        }
      }
      
      const result = qualificationCheckResults[personnelId]
      
      if (!result) {
        return {
          code: 404,
          message: '人员不存在'
        }
      }
      
      return {
        code: 200,
        data: result
      }
    }
  }
]