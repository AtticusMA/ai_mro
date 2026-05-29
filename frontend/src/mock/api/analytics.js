export default [
  {
    url: '/api/analytics/overview',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        total_tasks: 1256,
        avg_completion_hours: 18.5,
        fleet_availability: 94.2,
        training_pass_rate: 85
      }
    })
  },
  {
    url: '/api/analytics/trends',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        months: ['2026-01', '2026-02', '2026-03', '2026-04', '2026-05', '2026-06'],
        maintenance: [180, 195, 210, 188, 225, 258],
        faults: [45, 38, 52, 41, 48, 35],
        training: [32, 28, 40, 35, 45, 50]
      }
    })
  },
  {
    url: '/api/analytics/module-kpis',
    method: 'get',
    response: () => ({
      code: 200,
      data: {
        indicators: ['健康管理', 'AR协作', 'VR培训', '电子工卡', '工具航材'],
        scores: [82, 75, 78, 88, 71]
      }
    })
  },
  {
    url: '/api/analytics/maintenance-distribution',
    method: 'get',
    response: () => ({
      code: 200,
      data: [
        { name: 'ATA-72 发动机', value: 28 },
        { name: 'ATA-32 起落架', value: 18 },
        { name: 'ATA-29 液压', value: 15 },
        { name: 'ATA-24 电气', value: 14 },
        { name: 'ATA-21 空调', value: 10 },
        { name: 'ATA-34 导航', value: 8 },
        { name: '其他', value: 7 }
      ]
    })
  }
]
