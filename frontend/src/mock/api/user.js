const users = [
  { id: 1, username: 'admin', realName: '张三', email: 'zhangsan@example.com', phone: '13800138001', gender: 1, deptId: 1, deptName: 'A集团', roleIds: [1], status: 1, createTime: '2024-01-01 08:00:00' },
  { id: 2, username: 'manager', realName: '李四', email: 'lisi@example.com', phone: '13800138002', gender: 1, deptId: 2, deptName: '技术部', roleIds: [2], status: 1, createTime: '2024-01-15 09:00:00' },
  { id: 3, username: 'wangwu', realName: '王五', email: 'wangwu@example.com', phone: '13800138003', gender: 1, deptId: 3, deptName: '市场部', roleIds: [3], status: 1, createTime: '2024-02-01 10:00:00' },
  { id: 4, username: 'zhaoliu', realName: '赵六', email: 'zhaoliu@example.com', phone: '13800138004', gender: 2, deptId: 4, deptName: '前端组', roleIds: [3], status: 1, createTime: '2024-02-15 08:30:00' },
  { id: 5, username: 'qianqi', realName: '钱七', email: 'qianqi@example.com', phone: '13800138005', gender: 1, deptId: 5, deptName: '后端组', roleIds: [2], status: 1, createTime: '2024-03-01 09:00:00' },
  { id: 6, username: 'sunba', realName: '孙八', email: 'sunba@example.com', phone: '13800138006', gender: 2, deptId: 6, deptName: '财务部', roleIds: [3], status: 1, createTime: '2024-03-15 10:00:00' },
  { id: 7, username: 'zhoujiu', realName: '周九', email: 'zhoujiu@example.com', phone: '13800138007', gender: 1, deptId: 7, deptName: '人事部', roleIds: [2], status: 1, createTime: '2024-04-01 08:00:00' },
  { id: 8, username: 'wushi', realName: '吴十', email: 'wushi@example.com', phone: '13800138008', gender: 1, deptId: 8, deptName: '品牌组', roleIds: [3], status: 1, createTime: '2024-04-15 09:00:00' },
  { id: 9, username: 'zhengyi', realName: '郑一', email: 'zhengyi@example.com', phone: '13800138009', gender: 2, deptId: 9, deptName: '推广组', roleIds: [3], status: 0, createTime: '2024-05-01 10:00:00' },
  { id: 10, username: 'huanger', realName: '黄二', email: 'huanger@example.com', phone: '13800138010', gender: 1, deptId: 10, deptName: '运营部', roleIds: [2], status: 1, createTime: '2024-05-15 08:00:00' },
  { id: 11, username: 'lindasan', realName: '林三', email: 'linsan@example.com', phone: '13800138011', gender: 1, deptId: 2, deptName: '技术部', roleIds: [3], status: 1, createTime: '2024-06-01 09:00:00' },
  { id: 12, username: 'hesisi', realName: '何四', email: 'hesi@example.com', phone: '13800138012', gender: 2, deptId: 4, deptName: '前端组', roleIds: [3], status: 1, createTime: '2024-06-15 10:00:00' },
  { id: 13, username: 'gaowuwu', realName: '高五', email: 'gaowu@example.com', phone: '13800138013', gender: 1, deptId: 5, deptName: '后端组', roleIds: [3], status: 0, createTime: '2024-07-01 08:00:00' },
  { id: 14, username: 'maliiu', realName: '马六', email: 'maliu@example.com', phone: '13800138014', gender: 1, deptId: 3, deptName: '市场部', roleIds: [3], status: 1, createTime: '2024-07-15 09:00:00' },
  { id: 15, username: 'luqqi', realName: '陆七', email: 'luqi@example.com', phone: '13800138015', gender: 2, deptId: 6, deptName: '财务部', roleIds: [4], status: 1, createTime: '2024-08-01 10:00:00' },
]

let _users = [...users]
let _nextId = 1000

export default [
  {
    url: '/api/system/user/page',
    method: 'get',
    response: ({ query }) => {
      let list = [..._users]
      if (query.username) list = list.filter(u => u.username.includes(query.username))
      if (query.realName) list = list.filter(u => u.realName.includes(query.realName))
      if (query.phone) list = list.filter(u => u.phone.includes(query.phone))
      if (query.status !== undefined && query.status !== '') list = list.filter(u => u.status === Number(query.status))
      if (query.deptId) list = list.filter(u => u.deptId === Number(query.deptId))
      const page = Number(query.page) || 1
      const pageSize = Number(query.pageSize) || 10
      const total = list.length
      const data = list.slice((page - 1) * pageSize, page * pageSize)
      return { code: 200, message: '获取成功', data: { list: data, total, page, pageSize } }
    },
  },
  {
    url: '/api/system/user/detail',
    method: 'get',
    response: ({ query }) => {
      const user = _users.find(u => u.id === Number(query.id))
      return user ? { code: 200, message: '获取成功', data: user } : { code: 404, message: '用户不存在', data: null }
    },
  },
  {
    url: '/api/system/user/create',
    method: 'post',
    response: ({ body }) => {
      const newUser = { ...body, id: ++_nextId, createTime: new Date().toLocaleString() }
      _users.unshift(newUser)
      return { code: 200, message: '创建成功', data: newUser }
    },
  },
  {
    url: '/api/system/user/update',
    method: 'put',
    response: ({ body }) => {
      const idx = _users.findIndex(u => u.id === body.id)
      if (idx === -1) return { code: 404, message: '用户不存在', data: null }
      _users[idx] = { ..._users[idx], ...body }
      return { code: 200, message: '更新成功', data: _users[idx] }
    },
  },
  {
    url: '/api/system/user/remove',
    method: 'delete',
    response: ({ query }) => {
      const idx = _users.findIndex(u => u.id === Number(query.id))
      if (idx === -1) return { code: 404, message: '用户不存在', data: null }
      _users.splice(idx, 1)
      return { code: 200, message: '删除成功', data: null }
    },
  },
  {
    url: '/api/system/user/reset-password',
    method: 'put',
    response: () => ({ code: 200, message: '密码已重置为 123456', data: null }),
  },
]
