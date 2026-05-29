const manualTypes = ['AMM', 'TSM', 'SRM', 'WDM', 'IPC', 'CMM']
const aircraftTypes = ['B737-800', 'A320neo', 'B777-300ER']
const formats = ['PDF', 'XML', 'SGML']

function generateManuals() {
  const list = []
  for (let i = 0; i < 23; i++) {
    list.push({
      id: 1000 + i + 1,
      title: `${aircraftTypes[i % 3]} ${manualTypes[i % 6]} 修订版本 ${30 + i}`,
      manualNo: `${manualTypes[i % 6]}-${aircraftTypes[i % 3].replace(/-/g, '')}-${30 + i}`,
      aircraftType: aircraftTypes[i % 3],
      format: formats[i % 3],
      parsedStatus: i < 18 ? 'parsed' : i < 21 ? 'pending' : 'failed',
      uploadedAt: `2026-0${(i % 5) + 1}-${String(10 + (i % 15)).padStart(2, '0')}T00:00:00Z`
    })
  }
  return list
}

function generateVersions(docId) {
  return Array.from({ length: 5 }, (_, i) => ({
    id: 2000 + i,
    documentId: docId,
    versionNo: `Rev.${48 - i}`,
    changeSummary: [
      '更新液压系统维修程序第3章',
      '修正燃油系统力矩表',
      '新增起落架检查单',
      '修订发动机拆装步骤',
      '补充APU维护说明'
    ][i],
    effectiveDate: `2026-0${6 - i}-01`,
    revisedByName: ['李工程师', '王技师', '张主任', '陈工', '刘技师'][i],
    createdAt: `2026-0${6 - i}-01T00:00:00Z`
  }))
}

const allManuals = generateManuals()

export default [
  {
    url: '/api/manuals',
    method: 'get',
    response: ({ query }) => {
      let list = [...allManuals]
      const { pageNum = 1, pageSize = 20, aircraftType, parsedStatus } = query || {}
      if (aircraftType) list = list.filter((m) => m.aircraftType === aircraftType)
      if (parsedStatus) list = list.filter((m) => m.parsedStatus === parsedStatus)
      const start = (Number(pageNum) - 1) * Number(pageSize)
      return {
        code: 200,
        msg: 'ok',
        data: {
          list: list.slice(start, start + Number(pageSize)),
          total: list.length,
          pageNum: Number(pageNum),
          pageSize: Number(pageSize)
        },
        timestamp: Date.now()
      }
    }
  },
  {
    url: '/api/manuals',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: { id: 1100 }, timestamp: Date.now() })
  },
  {
    url: '/api/manuals/:id',
    method: 'get',
    response: ({ params }) => {
      const manual = allManuals.find((m) => m.id === Number(params.id)) || allManuals[0]
      return {
        code: 200,
        msg: 'ok',
        data: {
          ...manual,
          chapters: [
            { id: 1, number: '05-00-00', title: '时间限制/维修检查', pageCount: 45 },
            { id: 2, number: '12-00-00', title: '勤务', pageCount: 88 },
            { id: 3, number: '20-00-00', title: '标准实施方法', pageCount: 120 },
            { id: 4, number: '24-00-00', title: '电源', pageCount: 156 },
            { id: 5, number: '29-00-00', title: '液压', pageCount: 203 },
            { id: 6, number: '32-00-00', title: '起落架', pageCount: 178 },
            { id: 7, number: '72-00-00', title: '发动机', pageCount: 380 },
            { id: 8, number: '80-00-00', title: '起动', pageCount: 62 }
          ]
        },
        timestamp: Date.now()
      }
    }
  },
  {
    url: '/api/manuals/:id',
    method: 'delete',
    response: () => ({ code: 200, msg: 'ok', data: null, timestamp: Date.now() })
  },
  {
    url: '/api/manuals/:id/parse',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: null, timestamp: Date.now() })
  },
  {
    url: '/api/manuals/:id/publish',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: null, timestamp: Date.now() })
  },
  {
    url: '/api/manuals/:id/versions',
    method: 'get',
    response: ({ params, query }) => {
      const versions = generateVersions(Number(params.id))
      const { pageNum = 1, pageSize = 20 } = query || {}
      const start = (Number(pageNum) - 1) * Number(pageSize)
      return {
        code: 200,
        msg: 'ok',
        data: {
          list: versions.slice(start, start + Number(pageSize)),
          total: versions.length,
          pageNum: Number(pageNum),
          pageSize: Number(pageSize)
        },
        timestamp: Date.now()
      }
    }
  },
  {
    url: '/api/manuals/:id/versions',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: { versionId: 2100 }, timestamp: Date.now() })
  },
  {
    url: '/api/manuals/:id/translate',
    method: 'post',
    response: () => ({ code: 200, msg: 'ok', data: { taskId: 3001 }, timestamp: Date.now() })
  },
  {
    url: '/api/manuals/translations/:taskId',
    method: 'get',
    response: ({ params }) => ({
      code: 200,
      msg: 'ok',
      data: {
        taskId: Number(params.taskId),
        status: Number(params.taskId) % 2 === 1 ? 'completed' : 'processing',
        accuracyScore: 0.9621,
        resultUrl: 'https://oss.example.com/translations/3001.pdf'
      },
      timestamp: Date.now()
    })
  },
  {
    url: '/api/manuals/search',
    method: 'get',
    response: ({ query }) => {
      const q = query?.q || ''
      return {
        code: 200,
        msg: 'ok',
        data: {
          list: [
            {
              documentId: 1001,
              manualNo: 'AMM-B737800-47',
              chapterRef: '29-10-01',
              highlight: `...拆卸<em>${q || '液压泵'}</em>时需断开电源，力矩值参见 AMM 29-10-01 P203...`,
              score: 0.97
            },
            {
              documentId: 1001,
              manualNo: 'AMM-B737800-47',
              chapterRef: '29-00-00',
              highlight: `...${q || '液压'}系统工作压力为 3000 PSI，检查前确认压力释放...`,
              score: 0.89
            },
            {
              documentId: 1003,
              manualNo: 'TSM-A320neo-22',
              chapterRef: '29-12-00',
              highlight: `...${q || '液压'}泵电气接头检查标准，参见 TSM 29-12-00...`,
              score: 0.82
            }
          ],
          total: 3,
          pageNum: 1,
          pageSize: 20
        },
        timestamp: Date.now()
      }
    }
  }
]
