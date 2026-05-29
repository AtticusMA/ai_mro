export default [
  {
    url: '/api/hangar/list',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        list: [
          { id: 1, name: '一号机库', capacity: 4, occupied: 3, status: 'active', temperature: 22, humidity: 55 },
          { id: 2, name: '二号机库', capacity: 6, occupied: 4, status: 'active', temperature: 23, humidity: 52 },
          { id: 3, name: '三号机库', capacity: 2, occupied: 1, status: 'maintenance', temperature: 21, humidity: 58 }
        ],
        total: 3
      }
    })
  },
  {
    url: '/api/hangar/detail/:id',
    method: 'get',
    response: () => ({
      code: 200,
      data: { id: 1, name: '一号机库', capacity: 4, occupied: 3, area: 12000, height: 25, status: 'active' }
    })
  },
  {
    url: '/api/hangar/:id/bays',
    method: 'get',
    response: () => ({
      code: 200,
      data: [
        { id: 1, bay_no: 'A1', status: 'occupied', aircraft_id: 'B-1234', task: 'C检', progress: 65 },
        { id: 2, bay_no: 'A2', status: 'occupied', aircraft_id: 'B-5678', task: 'A检', progress: 90 },
        { id: 3, bay_no: 'B1', status: 'occupied', aircraft_id: 'B-9012', task: '排故', progress: 30 },
        { id: 4, bay_no: 'B2', status: 'available', aircraft_id: null, task: null, progress: 0 }
      ]
    })
  },
  {
    url: '/api/hangar/equipment',
    method: 'get',
    response: ({ query }) => {
      const list = [
        { id: 1, name: '液压升降平台A', type: '升降设备', hangar: '一号机库', status: 'in_use', next_maintenance: '2026-06-01' },
        { id: 2, name: '牵引车T-01', type: '牵引设备', hangar: '一号机库', status: 'available', next_maintenance: '2026-06-15' },
        { id: 3, name: '喷漆间PB-1', type: '喷漆设备', hangar: '二号机库', status: 'maintenance', next_maintenance: '2026-05-30' }
      ]
      const { page = 1, pageSize = 10 } = query || {}
      return { code: 200, data: { list: list.slice((page - 1) * pageSize, page * pageSize), total: list.length } }
    }
  },
  {
    url: '/api/hangar/:id/environment',
    method: 'get',
    response: () => ({
      code: 200,
      data: { temperature: 22.5, humidity: 54, dust_level: 'low', lighting: 850, ventilation: 'normal' }
    })
  },
  {
    url: '/api/hangar/schedule',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        list: [
          { id: 1, aircraft_id: 'B-3456', task: 'C检', bay: 'A1', start_date: '2026-05-28', end_date: '2026-06-10', status: 'scheduled' },
          { id: 2, aircraft_id: 'B-7890', task: '改装', bay: 'B2', start_date: '2026-06-01', end_date: '2026-06-20', status: 'scheduled' }
        ],
        total: 2
      }
    })
  },
  {
    url: '/api/hangar/schedule',
    method: 'post',
    response: () => ({ code: 200, message: '排程创建成功' })
  }
]
