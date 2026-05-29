// 人员证照管理 mock — 路径符合 MRO-009 spec (/api/personnel/licenses/*)
const licenseList = [
  {
    id: 1,
    userId: 101,
    userName: '张三',
    licenseNo: 'CAAC-2024-001',
    licenseType: '维修执照',
    aircraftType: 'B737',
    category: '机械',
    issuer: 'CAAC',
    issueDate: '2024-01-15',
    expiryDate: '2027-01-15',
    status: 'valid',
    fileUrl: null,
    remark: null,
    createTime: '2024-01-15T10:00:00'
  },
  {
    id: 2,
    userId: 102,
    userName: '李四',
    licenseNo: 'CAAC-2023-088',
    licenseType: '维修执照',
    aircraftType: 'A320',
    category: '电气',
    issuer: 'CAAC',
    issueDate: '2023-06-01',
    expiryDate: '2026-06-01',
    status: 'expiring',
    fileUrl: null,
    remark: null,
    createTime: '2023-06-01T09:00:00'
  },
  {
    id: 3,
    userId: 103,
    userName: '王五',
    licenseNo: 'CAAC-2021-033',
    licenseType: '特种设备证',
    aircraftType: null,
    category: '无损检测',
    issuer: '市场监管局',
    issueDate: '2021-03-20',
    expiryDate: '2025-03-20',
    status: 'expired',
    fileUrl: null,
    remark: '需续期',
    createTime: '2021-03-20T14:00:00'
  }
]

export default [
  // GET /api/personnel/licenses
  {
    url: '/api/personnel/licenses',
    method: 'get',
    response: ({ query }) => {
      const { pageNum = 1, pageSize = 10, status, licenseType, userName } = query || {}
      let filtered = [...licenseList]
      if (status) filtered = filtered.filter(i => i.status === status)
      if (licenseType) filtered = filtered.filter(i => i.licenseType === licenseType)
      if (userName) filtered = filtered.filter(i => i.userName.includes(userName))
      const start = (pageNum - 1) * pageSize
      const end = start + Number(pageSize)
      return {
        code: 200,
        data: {
          list: filtered.slice(start, end),
          total: filtered.length,
          pageNum: Number(pageNum),
          pageSize: Number(pageSize)
        }
      }
    }
  },

  // GET /api/personnel/licenses/alerts
  {
    url: '/api/personnel/licenses/alerts',
    method: 'get',
    response: () => ({
      code: 200,
      data: licenseList.filter(l => l.status !== 'valid').map(l => ({
        licenseId: l.id,
        userId: l.userId,
        userName: l.userName,
        licenseNo: l.licenseNo,
        licenseType: l.licenseType,
        expiryDate: l.expiryDate,
        daysRemaining: l.status === 'expired' ? -1 : 25,
        alertLevel: l.status === 'expired' ? 'urgent' : 'warning'
      }))
    })
  },

  // GET /api/personnel/licenses/stats
  {
    url: '/api/personnel/licenses/stats',
    method: 'get',
    response: () => ({
      code: 200,
      data: { totalLicenses: 45, validCount: 38, expiringCount: 5, expiredCount: 2 }
    })
  },

  // POST /api/personnel/licenses/check-qualification
  {
    url: '/api/personnel/licenses/check-qualification',
    method: 'post',
    response: () => ({
      code: 200,
      data: {
        qualified: true,
        licenseNo: 'CAAC-2024-001',
        expiryDate: '2027-01-15',
        reason: null
      }
    })
  },

  // POST /api/personnel/licenses/import
  {
    url: '/api/personnel/licenses/import',
    method: 'post',
    response: () => ({
      code: 200,
      data: { totalRows: 10, successCount: 9, failCount: 1, errorDetails: '行5: 证照编号不能为空' }
    })
  },

  // GET /api/personnel/licenses/:id
  {
    url: '/api/personnel/licenses/:id',
    method: 'get',
    response: ({ url }) => {
      const id = Number(url.match(/\/api\/personnel\/licenses\/(\d+)$/)?.[1] || '1')
      const license = licenseList.find(i => i.id === id)
      return license
        ? { code: 200, data: license }
        : { code: 404, message: '执照不存在' }
    }
  },

  // POST /api/personnel/licenses
  {
    url: '/api/personnel/licenses',
    method: 'post',
    response: () => ({ code: 200, data: licenseList.length + 1 })
  },

  // PUT /api/personnel/licenses/:id
  {
    url: '/api/personnel/licenses/:id',
    method: 'put',
    response: () => ({ code: 200, data: null })
  },

  // DELETE /api/personnel/licenses/:id
  {
    url: '/api/personnel/licenses/:id',
    method: 'delete',
    response: () => ({ code: 200, data: null })
  },

  // POST /api/personnel/licenses/:id/renew
  {
    url: '/api/personnel/licenses/:id/renew',
    method: 'post',
    response: () => ({ code: 200, data: null })
  },

  // POST /api/personnel/licenses/:id/attachment
  {
    url: '/api/personnel/licenses/:id/attachment',
    method: 'post',
    response: ({ url }) => {
      const id = url.match(/\/api\/personnel\/licenses\/(\d+)\/attachment/)?.[1] || '1'
      return {
        code: 200,
        data: { attachmentUrl: `/uploads/licenses/${id}/sample.pdf` }
      }
    }
  }
]
