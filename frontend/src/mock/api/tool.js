export default [
  // ---- Tool cabinets ----
  {
    url: '/api/tool/cabinets',
    method: 'get',
    response: () => ({
      code: 200, msg: 'ok',
      data: {
        list: [
          { id: 1, name: 'C检工具柜-01', location: '机库A区', slotCount: 50, availableSlots: 32, temperature: 22.5, humidity: 45.2, onlineStatus: 'online' },
          { id: 2, name: 'C检工具柜-02', location: '机库B区', slotCount: 40, availableSlots: 28, temperature: 23.1, humidity: 47.0, onlineStatus: 'online' },
          { id: 3, name: '精密工具柜-01', location: '机库C区', slotCount: 30, availableSlots: 18, temperature: 21.8, humidity: 44.5, onlineStatus: 'offline' }
        ],
        total: 3, pageNum: 1, pageSize: 20
      },
      timestamp: Date.now()
    })
  },
  {
    url: '/api/tool/cabinets/:id/slots',
    method: 'get',
    response: ({ params }) => ({
      code: 200, msg: 'ok',
      data: {
        cabinetId: Number(params.id),
        slots: [
          { slotNo: 1, toolId: 2001, toolName: '扭力扳手 1/2"', toolCode: 'TW-001', rfidTag: 'RFID-001', status: 'in_cabinet' },
          { slotNo: 2, toolId: null, status: 'empty' },
          { slotNo: 3, toolId: 2002, toolName: '数字万用表', toolCode: 'DM-001', rfidTag: 'RFID-002', status: 'in_cabinet' },
          { slotNo: 4, toolId: 2003, toolName: '孔探仪', toolCode: 'BS-001', rfidTag: 'RFID-003', status: 'borrowed' }
        ]
      },
      timestamp: Date.now()
    })
  },
  {
    url: '/api/tool/cabinets/:id/inventory',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: null, timestamp: Date.now() })
  },
  // ---- Tools ----
  {
    url: '/api/tool/tools',
    method: 'get',
    response: ({ query }) => {
      const { pageNum = 1, pageSize = 20 } = query || {}
      const list = [
        { id: 2001, name: '扭力扳手 1/2"', toolCode: 'TW-001', rfidTag: 'RFID-001', category: '手动工具', cabinetId: 1, cabinetName: 'C检工具柜-01', slotNo: 1, status: 'in_cabinet', calibrationDue: '2026-12-01', useCount: 45 },
        { id: 2002, name: '数字万用表', toolCode: 'DM-001', rfidTag: 'RFID-002', category: '电气工具', cabinetId: 1, cabinetName: 'C检工具柜-01', slotNo: 3, status: 'in_cabinet', calibrationDue: '2026-08-15', useCount: 120 },
        { id: 2003, name: '孔探仪', toolCode: 'BS-001', rfidTag: 'RFID-003', category: '检测工具', cabinetId: 1, cabinetName: 'C检工具柜-01', slotNo: 4, status: 'borrowed', calibrationDue: '2027-03-01', useCount: 38 },
        { id: 2004, name: '液压千斤顶', toolCode: 'HJ-001', rfidTag: 'RFID-004', category: '液压工具', cabinetId: 2, cabinetName: 'C检工具柜-02', slotNo: 1, status: 'in_cabinet', calibrationDue: '2026-11-30', useCount: 67 },
        { id: 2005, name: '安全线钳', toolCode: 'SP-001', rfidTag: 'RFID-005', category: '手动工具', cabinetId: 2, cabinetName: 'C检工具柜-02', slotNo: 2, status: 'in_cabinet', calibrationDue: null, useCount: 23 }
      ]
      const p = Number(pageNum), s = Number(pageSize)
      return {
        code: 200, msg: 'ok',
        data: { list: list.slice((p - 1) * s, p * s), total: list.length, pageNum: p, pageSize: s },
        timestamp: Date.now()
      }
    }
  },
  {
    url: '/api/tool/tools/:id/lifecycle',
    method: 'get',
    response: ({ params }) => ({
      code: 200, msg: 'ok',
      data: { toolId: Number(params.id), toolName: '扭力扳手 1/2"', toolCode: 'TW-001', useCount: 45, calibrationDue: '2026-12-01', calibrationStatus: 'normal', repairHistory: null },
      timestamp: Date.now()
    })
  },
  // ---- Borrow / Return ----
  {
    url: '/api/tool/borrow',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: { borrowRecordIds: [4001, 4002] }, timestamp: Date.now() })
  },
  {
    url: '/api/tool/return',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: { returnedCount: 2, missingRfids: [] }, timestamp: Date.now() })
  },
  {
    url: '/api/tool/borrow-records',
    method: 'get',
    response: ({ query }) => {
      const { pageNum = 1, pageSize = 20 } = query || {}
      const list = Array.from({ length: 12 }, (_, i) => ({
        id: i + 1,
        toolId: 2001 + (i % 5),
        toolName: ['扭力扳手 1/2"', '数字万用表', '孔探仪', '液压千斤顶', '安全线钳'][i % 5],
        userId: 101 + (i % 3),
        userName: ['张三', '李四', '王五'][i % 3],
        borrowTime: `2026-05-${String(20 + (i % 6)).padStart(2, '0')}T08:00:00Z`,
        expectedReturn: `2026-05-${String(20 + (i % 6)).padStart(2, '0')}T18:00:00Z`,
        actualReturn: i < 8 ? `2026-05-${String(20 + (i % 6)).padStart(2, '0')}T17:30:00Z` : null,
        status: i < 8 ? 'returned' : 'borrowed',
        workcardId: 3000 + i
      }))
      const p = Number(pageNum), s = Number(pageSize)
      return { code: 200, msg: 'ok', data: { list: list.slice((p - 1) * s, p * s), total: list.length, pageNum: p, pageSize: s }, timestamp: Date.now() }
    }
  },
  // ---- Alerts ----
  {
    url: '/api/tool/alerts',
    method: 'get',
    response: ({ query }) => {
      const { pageNum = 1, pageSize = 20 } = query || {}
      const list = [
        { id: 5001, toolId: 2001, toolName: '扭力扳手 1/2"', alertType: 'overdue', borrowerId: 101, borrowerName: '张三', borrowTime: '2026-05-26T08:00:00Z', expectedReturn: '2026-05-26T16:00:00Z', overdueHours: 2.5 },
        { id: 5002, toolId: 2003, toolName: '孔探仪', alertType: 'overdue', borrowerId: 102, borrowerName: '李四', borrowTime: '2026-05-25T09:00:00Z', expectedReturn: '2026-05-25T17:00:00Z', overdueHours: 15.0 }
      ]
      const p = Number(pageNum), s = Number(pageSize)
      return { code: 200, msg: 'ok', data: { list: list.slice((p - 1) * s, p * s), total: list.length, pageNum: p, pageSize: s }, timestamp: Date.now() }
    }
  },
  // ---- Materials ----
  {
    url: '/api/material/items',
    method: 'get',
    response: ({ query }) => {
      const { pageNum = 1, pageSize = 20 } = query || {}
      const list = [
        { id: 6001, partNo: 'B737-SEAL-029', name: '液压系统密封圈', category: '密封件', stockQty: 3, minStock: 10, location: 'B区-架位12', expiryDate: '2027-12-31', belowMinStock: true },
        { id: 6002, partNo: 'B737-FILT-045', name: '液压滤芯', category: '滤件', stockQty: 25, minStock: 15, location: 'B区-架位08', expiryDate: '2028-06-30', belowMinStock: false },
        { id: 6003, partNo: 'A320-FUSE-010', name: '保险丝10A', category: '电气件', stockQty: 8, minStock: 20, location: 'C区-架位03', expiryDate: null, belowMinStock: true },
        { id: 6004, partNo: 'COM-BOLT-M8', name: '螺栓 M8×25', category: '紧固件', stockQty: 200, minStock: 50, location: 'A区-架位01', expiryDate: null, belowMinStock: false },
        { id: 6005, partNo: 'COM-OIL-MIL5606', name: '液压油 MIL-5606', category: '油液', stockQty: 12, minStock: 10, location: 'D区-架位02', expiryDate: '2027-03-15', belowMinStock: false }
      ]
      const p = Number(pageNum), s = Number(pageSize)
      return { code: 200, msg: 'ok', data: { list: list.slice((p - 1) * s, p * s), total: list.length, pageNum: p, pageSize: s }, timestamp: Date.now() }
    }
  },
  {
    url: '/api/material/items',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: { id: 6006 }, timestamp: Date.now() })
  },
  {
    url: '/api/material/items/:id',
    method: 'put',
    response: () => ({ code: 200, msg: 'ok', data: null, timestamp: Date.now() })
  },
  {
    url: '/api/material/alerts',
    method: 'get',
    response: () => ({
      code: 200, msg: 'ok',
      data: {
        list: [
          { id: 6001, partNo: 'B737-SEAL-029', name: '液压系统密封圈', stockQty: 3, minStock: 10, location: 'B区-架位12' },
          { id: 6003, partNo: 'A320-FUSE-010', name: '保险丝10A', stockQty: 8, minStock: 20, location: 'C区-架位03' }
        ],
        total: 2, pageNum: 1, pageSize: 20
      },
      timestamp: Date.now()
    })
  },
  {
    url: '/api/material/repair-orders',
    method: 'get',
    response: ({ query }) => {
      const { pageNum = 1, pageSize = 20 } = query || {}
      const list = [
        { id: 7001, materialId: 6001, materialName: '液压系统密封圈', partNo: 'B737-SEAL-029', quantity: 2, faultDescription: '密封性能下降', vendorId: 301, status: 'pending', createdAt: '2026-05-20T09:00:00Z' },
        { id: 7002, materialId: 6003, materialName: '保险丝10A', partNo: 'A320-FUSE-010', quantity: 5, faultDescription: '熔断失效', vendorId: 302, status: 'in_repair', createdAt: '2026-05-18T14:00:00Z' }
      ]
      const p = Number(pageNum), s = Number(pageSize)
      return { code: 200, msg: 'ok', data: { list: list.slice((p - 1) * s, p * s), total: list.length, pageNum: p, pageSize: s }, timestamp: Date.now() }
    }
  },
  {
    url: '/api/material/repair-orders',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: { orderId: 7003 }, timestamp: Date.now() })
  }
]
