const logs = [
  { id: 1,  requestPath: '/api/system/user/page',           requestTime: '2026-05-23 09:00:00', costTime: 87,  userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-001', requestParams: '{"page":1,"pageSize":10}',                               resultStatus: 1 },
  { id: 2,  requestPath: '/api/system/user/create',         requestTime: '2026-05-23 09:05:00', costTime: 213, userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-002', requestParams: '{"username":"test","realName":"测试用户"}',               resultStatus: 1 },
  { id: 3,  requestPath: '/api/system/user/update',         requestTime: '2026-05-23 09:10:00', costTime: 145, userId: 2, userName: '李四', deptId: 2, requestId: 'fe-req-003', requestParams: '{"id":5,"status":0}',                                    resultStatus: 1 },
  { id: 4,  requestPath: '/api/system/role/create',         requestTime: '2026-05-23 09:15:00', costTime: 32,  userId: 2, userName: '李四', deptId: 2, requestId: null,          requestParams: '{"name":"测试角色","permissions":["system:user:list"]}',  resultStatus: 0 },
  { id: 5,  requestPath: '/api/system/dept/update',         requestTime: '2026-05-23 09:20:00', costTime: 178, userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-005', requestParams: '{"id":3,"name":"市场部（已更名）"}',                       resultStatus: 1 },
  { id: 6,  requestPath: '/api/system/user/remove',         requestTime: '2026-05-23 10:00:00', costTime: 95,  userId: 3, userName: '王五', deptId: 3, requestId: 'fe-req-006', requestParams: '{"id":9}',                                               resultStatus: 1 },
  { id: 7,  requestPath: '/api/system/menu/create',         requestTime: '2026-05-23 10:05:00', costTime: 302, userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-007', requestParams: '{"name":"新菜单","type":"C","path":"/system/test"}',      resultStatus: 1 },
  { id: 8,  requestPath: '/api/system/dict/update',         requestTime: '2026-05-23 10:10:00', costTime: 18,  userId: 2, userName: '李四', deptId: 2, requestId: null,          requestParams: '{"id":1,"remark":"已更新备注"}',                          resultStatus: 0 },
  { id: 9,  requestPath: '/api/system/user/reset-password', requestTime: '2026-05-23 10:30:00', costTime: 256, userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-009', requestParams: '{"id":7}',                                               resultStatus: 1 },
  { id: 10, requestPath: '/api/system/role/update',         requestTime: '2026-05-23 11:00:00', costTime: 189, userId: 4, userName: '赵六', deptId: 4, requestId: 'fe-req-010', requestParams: '{"id":2,"menuIds":[1,2,3,4,5]}',                         resultStatus: 1 },
  { id: 11, requestPath: '/api/system/user/page',           requestTime: '2026-05-23 11:05:00', costTime: 112, userId: 4, userName: '赵六', deptId: 4, requestId: 'fe-req-011', requestParams: '{"page":2,"pageSize":10,"realName":"张"}',               resultStatus: 1 },
  { id: 12, requestPath: '/api/system/dept/create',         requestTime: '2026-05-23 11:10:00', costTime: 234, userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-012', requestParams: '{"name":"新部门","parentId":2}',                          resultStatus: 1 },
  { id: 13, requestPath: '/api/system/menu/remove',         requestTime: '2026-05-23 11:15:00', costTime: 27,  userId: 2, userName: '李四', deptId: 2, requestId: null,          requestParams: '{"id":22}',                                              resultStatus: 0 },
  { id: 14, requestPath: '/api/system/user/update',         requestTime: '2026-05-23 13:00:00', costTime: 167, userId: 5, userName: '钱七', deptId: 5, requestId: 'fe-req-014', requestParams: '{"id":3,"phone":"13900139000"}',                         resultStatus: 1 },
  { id: 15, requestPath: '/api/system/dict/create',         requestTime: '2026-05-23 13:05:00', costTime: 198, userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-015', requestParams: '{"dictType":"sys_status","dictLabel":"启用","dictValue":"1"}', resultStatus: 1 },
  { id: 16, requestPath: '/api/system/role/remove',         requestTime: '2026-05-23 13:10:00', costTime: 76,  userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-016', requestParams: '{"id":5}',                                               resultStatus: 1 },
  { id: 17, requestPath: '/api/system/user/page',           requestTime: '2026-05-23 14:00:00', costTime: 93,  userId: 6, userName: '孙八', deptId: 6, requestId: 'fe-req-017', requestParams: '{"page":1,"pageSize":20}',                               resultStatus: 1 },
  { id: 18, requestPath: '/api/system/dept/remove',         requestTime: '2026-05-23 14:05:00', costTime: 41,  userId: 2, userName: '李四', deptId: 2, requestId: null,          requestParams: '{"id":10}',                                              resultStatus: 0 },
  { id: 19, requestPath: '/api/system/menu/update',         requestTime: '2026-05-23 14:10:00', costTime: 154, userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-019', requestParams: '{"id":3,"sort":5}',                                      resultStatus: 1 },
  { id: 20, requestPath: '/api/system/user/create',         requestTime: '2026-05-23 15:00:00', costTime: 287, userId: 3, userName: '王五', deptId: 3, requestId: 'fe-req-020', requestParams: '{"username":"newuser","realName":"新用户","deptId":3}',   resultStatus: 1 },
  { id: 21, requestPath: '/api/system/user/page',           requestTime: '2026-05-22 09:00:00', costTime: 68,  userId: 1, userName: '张三', deptId: 1, requestId: 'fe-req-021', requestParams: '{"page":1,"pageSize":10}',                               resultStatus: 1 },
  { id: 22, requestPath: '/api/system/role/create',         requestTime: '2026-05-22 10:00:00', costTime: 221, userId: 2, userName: '李四', deptId: 2, requestId: 'fe-req-022', requestParams: '{"name":"运营角色"}',                                     resultStatus: 1 },
  { id: 23, requestPath: '/api/system/dict/update',         requestTime: '2026-05-22 11:00:00', costTime: 143, userId: 5, userName: '钱七', deptId: 5, requestId: 'fe-req-023', requestParams: '{"id":2,"remark":"备注更新"}',                            resultStatus: 1 },
  { id: 24, requestPath: '/api/system/user/remove',         requestTime: '2026-05-22 14:00:00', costTime: 55,  userId: 1, userName: '张三', deptId: 1, requestId: null,          requestParams: '{"id":13}',                                              resultStatus: 0 },
  { id: 25, requestPath: '/api/system/dept/update',         requestTime: '2026-05-21 09:30:00', costTime: 172, userId: 4, userName: '赵六', deptId: 4, requestId: 'fe-req-025', requestParams: '{"id":5,"status":0}',                                    resultStatus: 1 },
]

export default [
  {
    url: '/api/system/operation-log',
    method: 'get',
    response: ({ query }) => {
      let list = [...logs]

      if (query.userName) {
        list = list.filter(l => l.userName.includes(query.userName))
      }
      if (query.requestPath) {
        list = list.filter(l => l.requestPath.includes(query.requestPath))
      }
      if (query.resultStatus !== undefined && query.resultStatus !== '') {
        list = list.filter(l => l.resultStatus === Number(query.resultStatus))
      }
      if (query.beginTime) {
        list = list.filter(l => l.requestTime >= query.beginTime)
      }
      if (query.endTime) {
        list = list.filter(l => l.requestTime <= query.endTime)
      }

      const page = Number(query.page) || 1
      const pageSize = Number(query.pageSize) || 20
      const total = list.length
      const data = list.slice((page - 1) * pageSize, page * pageSize)

      return { code: 200, message: '获取成功', data: { list: data, total, page, pageSize } }
    },
  },
  {
    url: '/api/system/operation-log/:id',
    method: 'get',
    response: ({ params }) => {
      const log = logs.find(l => l.id === Number(params.id))
      return log
        ? { code: 200, message: '获取成功', data: log }
        : { code: 404, message: '日志不存在', data: null }
    },
  },
]
